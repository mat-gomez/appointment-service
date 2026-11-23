package ar.edu.um.appointment.turnos.infrastructure.kafka;

import ar.edu.um.appointment.turnos.domain.model.AppointmentKafkaEventDTO;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ProcessedEventEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ProcessedEventRepository;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationRepository;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentKafkaConsumerTest {

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private AppointmentKafkaConsumer consumer;

    @Test
    void shouldProcessConfirmedEventAndUpdateReservationStatus() {
        AppointmentKafkaEventDTO event = new AppointmentKafkaEventDTO(
                "evt-1", "AppointmentConfirmed", "proc-100", null
        );
        ReservationEntity reservation = ReservationEntity.builder()
                .reservationId("proc-100")
                .status(ReservationStatus.WAITING_FOR_PHONE)
                .build();
        when(processedEventRepository.existsByEventId("evt-1")).thenReturn(false);
        when(reservationRepository.findByReservationId("proc-100")).thenReturn(Optional.of(reservation));

        consumer.consume(event);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(processedEventRepository).save(any(ProcessedEventEntity.class));
        verify(reservationRepository).save(reservation);
    }

    @Test
    void shouldIgnoreDuplicateEventsByEventId() {
        AppointmentKafkaEventDTO event = new AppointmentKafkaEventDTO(
                "evt-dup", "AppointmentConfirmed", "proc-100", null
        );
        when(processedEventRepository.existsByEventId("evt-dup")).thenReturn(true);

        consumer.consume(event);

        verify(processedEventRepository, never()).save(any());
        verify(reservationRepository, never()).findByReservationId(any());
    }

    @Test
    void shouldMapAllSupportedEventTypes() {
        when(processedEventRepository.existsByEventId(any())).thenReturn(false);
        ReservationEntity res = ReservationEntity.builder().reservationId("proc-x").build();
        when(reservationRepository.findByReservationId("proc-x")).thenReturn(Optional.of(res));

        consumer.consume(new AppointmentKafkaEventDTO("e1", "AdditionalInformationRequested", "proc-x", null));
        assertThat(res.getStatus()).isEqualTo(ReservationStatus.WAITING_FOR_PHONE);

        consumer.consume(new AppointmentKafkaEventDTO("e2", "AdditionalInformationRejected", "proc-x", "bad phone"));
        assertThat(res.getStatus()).isEqualTo(ReservationStatus.PHONE_REJECTED);

        consumer.consume(new AppointmentKafkaEventDTO("e3", "AppointmentProcessExpired", "proc-x", null));
        assertThat(res.getStatus()).isEqualTo(ReservationStatus.EXPIRED);

        consumer.consume(new AppointmentKafkaEventDTO("e4", "AppointmentProcessInvalid", "proc-x", null));
        assertThat(res.getStatus()).isEqualTo(ReservationStatus.INVALID);

        consumer.consume(new AppointmentKafkaEventDTO("e5", "AppointmentCancelled", "proc-x", null));
        assertThat(res.getStatus()).isEqualTo(ReservationStatus.CANCELLED);

        consumer.consume(new AppointmentKafkaEventDTO("e6", "UnknownEvent", "proc-x", null));
        consumer.consume(new AppointmentKafkaEventDTO("", "AppointmentConfirmed", "proc-x", null));
    }
}
