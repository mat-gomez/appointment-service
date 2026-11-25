package ar.edu.um.appointment.turnos.infrastructure.kafka;

import ar.edu.um.appointment.turnos.application.ports.in.SubmitPhoneUseCase;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationPhoneEventPort;
import ar.edu.um.appointment.turnos.domain.model.AdditionalInformationSubmittedEvent;
import ar.edu.um.appointment.turnos.domain.model.AdditionalInformationSubmittedEvent.PhoneInformation;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationRepository;
import ar.edu.um.appointment.web.errors.DomainException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class AppointmentKafkaProducer implements SubmitPhoneUseCase, ReservationPhoneEventPort {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final String EVENT_TYPE = "AdditionalInformationSubmitted";
    private static final String PHONE_TOPIC_PREFIX = "catedra.turnos.telefono.";
    private static final int SCHEMA_VERSION = 1;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ReservationRepository reservationRepository;
    private final String topic;
    private final String groupId;
    private final Clock clock;

    @Autowired
    public AppointmentKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            ReservationRepository reservationRepository,
            @Value("${app.kafka.topics.phone-prefix:catedra.turnos.telefono.}${app.kafka.group-id:grupo-default}") String topic,
            @Value("${app.kafka.group-id:grupo-default}") String groupId
    ) {
        this(kafkaTemplate, reservationRepository, topic, groupId, Clock.systemUTC());
    }

    AppointmentKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            ReservationRepository reservationRepository,
            String topic,
            Clock clock
    ) {
        this(kafkaTemplate, reservationRepository, topic, resolveGroupId(topic, "grupo-test"), clock);
    }

    AppointmentKafkaProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            ReservationRepository reservationRepository,
            String topic,
            String groupId,
            Clock clock
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.reservationRepository = reservationRepository;
        this.topic = topic;
        this.groupId = (groupId != null && !groupId.isBlank()) ? groupId : resolveGroupId(topic, "grupo-default");
        this.clock = clock;
    }

    private static String resolveGroupId(String topic, String fallback) {
        if (topic != null && topic.startsWith(PHONE_TOPIC_PREFIX) && topic.length() > PHONE_TOPIC_PREFIX.length()) {
            return topic.substring(PHONE_TOPIC_PREFIX.length());
        }
        return fallback;
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
        Optional<ReservationEntity> reservationOpt = Optional.empty();
        if (reservationRepository != null) {
            reservationOpt = reservationRepository.findByReservationId(reservationProcessId);
            reservationOpt.ifPresent(entity -> {
                if (entity.getUsername() != null && !entity.getUsername().equals(username)) {
                    throw new DomainException("FORBIDDEN_RESERVATION", HttpStatus.FORBIDDEN, "Reservation belongs to another user");
                }
            });
        }
        String normalizedPhone = normalizeAndValidatePhone(rawPhone);
        String holdId = reservationOpt.map(ReservationEntity::getHoldId).orElse("");
        String requestEventId = reservationOpt.map(ReservationEntity::getLastRequestEventId)
                .filter(id -> id != null && !id.isBlank())
                .orElse("request-event-" + reservationProcessId);

        AdditionalInformationSubmittedEvent event = new AdditionalInformationSubmittedEvent(
                "phone-submitted-" + UUID.randomUUID(),
                EVENT_TYPE,
                groupId,
                reservationProcessId,
                holdId,
                requestEventId,
                new PhoneInformation(normalizedPhone),
                clock.instant(),
                SCHEMA_VERSION
        );
        publishAdditionalInformationSubmitted(event);
        return event;
    }

    @Override
    public void publishAdditionalInformationSubmitted(AdditionalInformationSubmittedEvent event) {
        kafkaTemplate.send(topic, event.reservationProcessId(), event);
    }
}
