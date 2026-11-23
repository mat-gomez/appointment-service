package ar.edu.um.appointment.turnos.infrastructure.kafka;

import ar.edu.um.appointment.turnos.domain.model.AppointmentKafkaEventDTO;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ProcessedEventEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ProcessedEventRepository;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationRepository;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AppointmentKafkaConsumer {

    private final ProcessedEventRepository processedEventRepository;
    private final ReservationRepository reservationRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.actions-prefix:alumnos.turnos.acciones.}${app.kafka.group-id:grupo-default}",
            groupId = "${app.kafka.group-id:grupo-default}"
    )
    @Transactional
    public void consume(AppointmentKafkaEventDTO event) {
        if (event == null || event.eventId() == null || event.eventId().isBlank()) {
            return;
        }
        if (processedEventRepository.existsByEventId(event.eventId())) {
            return;
        }

        processedEventRepository.save(ProcessedEventEntity.builder()
                .eventId(event.eventId())
                .processedAt(LocalDateTime.now())
                .build());

        if (event.reservationProcessId() == null || event.eventType() == null) {
            return;
        }

        ReservationStatus nextStatus = mapEventToStatus(event.eventType());
        if (nextStatus != null) {
            reservationRepository.findByReservationId(event.reservationProcessId()).ifPresent(reservation -> {
                reservation.setStatus(nextStatus);
                reservationRepository.save(reservation);
            });
        }
    }

    private ReservationStatus mapEventToStatus(String eventType) {
        return switch (eventType) {
            case "AdditionalInformationRequested" -> ReservationStatus.WAITING_FOR_PHONE;
            case "AppointmentConfirmed" -> ReservationStatus.CONFIRMED;
            case "AdditionalInformationRejected" -> ReservationStatus.PHONE_REJECTED;
            case "AppointmentProcessExpired" -> ReservationStatus.EXPIRED;
            case "AppointmentProcessInvalid" -> ReservationStatus.INVALID;
            case "AppointmentCancelled" -> ReservationStatus.CANCELLED;
            default -> null;
        };
    }
}
