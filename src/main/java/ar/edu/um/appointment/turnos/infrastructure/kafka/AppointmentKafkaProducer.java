package ar.edu.um.appointment.turnos.infrastructure.kafka;

import ar.edu.um.appointment.turnos.application.ports.in.SubmitPhoneUseCase;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationPhoneEventPort;
import ar.edu.um.appointment.turnos.domain.model.AdditionalInformationSubmittedEvent;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationRepository;
import ar.edu.um.appointment.web.errors.DomainException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class AppointmentKafkaProducer implements SubmitPhoneUseCase, ReservationPhoneEventPort {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReservationRepository reservationRepository;
    private final String topic;
    private final Clock clock;

    @Autowired
    public AppointmentKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            ReservationRepository reservationRepository,
            @Value("${app.kafka.topics.phone-prefix:catedra.turnos.telefono.}${app.kafka.group-id:grupo-default}") String topic
    ) {
        this(kafkaTemplate, reservationRepository, topic, Clock.systemUTC());
    }

    AppointmentKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            ReservationRepository reservationRepository,
            String topic,
            Clock clock
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.reservationRepository = reservationRepository;
        this.topic = topic;
        this.clock = clock;
    }

    public String normalizeAndValidatePhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new DomainException("INVALID_PHONE", HttpStatus.BAD_REQUEST, "Phone number is required");
        }
        String normalized = rawPhone.replaceAll("[\\s\\-().]", "");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new DomainException("INVALID_PHONE", HttpStatus.BAD_REQUEST, "Phone number format is invalid");
        }
        return normalized;
    }

    @Override
    public AdditionalInformationSubmittedEvent submitPhone(
            String reservationProcessId,
            String rawPhone,
            String username
    ) {
        if (reservationProcessId == null || reservationProcessId.isBlank()) {
            throw new DomainException("INVALID_RESERVATION_ID", HttpStatus.BAD_REQUEST, "reservationProcessId is required");
        }
        if (reservationRepository != null) {
            reservationRepository.findByReservationId(reservationProcessId).ifPresent(entity -> {
                if (entity.getUsername() != null && !entity.getUsername().equals(username)) {
                    throw new DomainException("FORBIDDEN_RESERVATION", HttpStatus.FORBIDDEN, "Reservation belongs to another user");
                }
            });
        }
        String normalizedPhone = normalizeAndValidatePhone(rawPhone);
        AdditionalInformationSubmittedEvent event = new AdditionalInformationSubmittedEvent(
                UUID.randomUUID().toString(),
                reservationProcessId,
                normalizedPhone,
                clock.instant()
        );
        publishAdditionalInformationSubmitted(event);
        return event;
    }

    @Override
    public void publishAdditionalInformationSubmitted(AdditionalInformationSubmittedEvent event) {
        kafkaTemplate.send(topic, event.reservationProcessId(), event);
    }
}
