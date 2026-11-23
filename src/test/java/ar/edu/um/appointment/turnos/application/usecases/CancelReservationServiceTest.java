package ar.edu.um.appointment.turnos.application.usecases;

import ar.edu.um.appointment.turnos.application.ports.out.CancelAppointmentPort;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationPersistencePort;
import ar.edu.um.appointment.turnos.domain.model.ReservationDetailDTO;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationStatus;
import ar.edu.um.appointment.web.errors.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelReservationServiceTest {

    @Mock
    private ReservationPersistencePort reservationPersistencePort;

    @Mock
    private CancelAppointmentPort cancelAppointmentPort;

    @InjectMocks
    private CancelReservationService cancelReservationService;

    @Test
    void shouldCancelConfirmedReservationCallingCatedra() {
        LocalDateTime slot = LocalDateTime.of(2026, 11, 24, 9, 30);
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("proc-50")
                .holdId("hold-50")
                .username("melina")
                .professionalId(202L)
                .appointmentTime(slot)
                .status(ReservationStatus.CONFIRMED)
                .build();
        when(reservationPersistencePort.findByReservationId("proc-50")).thenReturn(Optional.of(entity));
        when(reservationPersistencePort.save(any(ReservationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationDetailDTO result = cancelReservationService.cancelReservation("proc-50", "melina", "jwt-1");

        assertThat(result.status()).isEqualTo("CANCELLED");
        verify(cancelAppointmentPort).cancelConfirmedAppointment("proc-50", "hold-50", "jwt-1");
        verify(reservationPersistencePort).save(entity);
    }

    @Test
    void shouldBeIdempotentWhenCancellingAlreadyCancelledReservation() {
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("proc-51")
                .holdId("hold-51")
                .username("melina")
                .status(ReservationStatus.CANCELLED)
                .build();
        when(reservationPersistencePort.findByReservationId("proc-51")).thenReturn(Optional.of(entity));

        ReservationDetailDTO result = cancelReservationService.cancelReservation("proc-51", "melina", "jwt-1");

        assertThat(result.status()).isEqualTo("CANCELLED");
        verifyNoInteractions(cancelAppointmentPort);
        verify(reservationPersistencePort, never()).save(any());
    }

    @Test
    void shouldRejectCancellingNonConfirmedReservation() {
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("proc-52")
                .username("melina")
                .status(ReservationStatus.WAITING_FOR_PHONE)
                .build();
        when(reservationPersistencePort.findByReservationId("proc-52")).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> cancelReservationService.cancelReservation("proc-52", "melina", "jwt-1"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verifyNoInteractions(cancelAppointmentPort);
    }

    @Test
    void shouldAbandonPendingReservationWithoutCallingCatedra() {
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("proc-60")
                .holdId("hold-60")
                .username("melina")
                .professionalId(202L)
                .status(ReservationStatus.WAITING_FOR_PHONE)
                .build();
        when(reservationPersistencePort.findByReservationId("proc-60")).thenReturn(Optional.of(entity));
        when(reservationPersistencePort.save(any(ReservationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationDetailDTO result = cancelReservationService.abandonReservation("proc-60", "melina");

        assertThat(result.status()).isEqualTo("CANCELLED");
        verifyNoInteractions(cancelAppointmentPort);
        verify(reservationPersistencePort).save(entity);
    }

    @Test
    void shouldRejectAbandoningNonPendingReservationOrOtherUserReservation() {
        ReservationEntity confirmed = ReservationEntity.builder()
                .reservationId("proc-61")
                .username("melina")
                .status(ReservationStatus.CONFIRMED)
                .build();
        when(reservationPersistencePort.findByReservationId("proc-61")).thenReturn(Optional.of(confirmed));

        assertThatThrownBy(() -> cancelReservationService.abandonReservation("proc-61", "melina"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        ReservationEntity otherUser = ReservationEntity.builder()
                .reservationId("proc-62")
                .username("otro")
                .status(ReservationStatus.HOLD_CREATED)
                .build();
        when(reservationPersistencePort.findByReservationId("proc-62")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> cancelReservationService.abandonReservation("proc-62", "melina"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        when(reservationPersistencePort.findByReservationId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cancelReservationService.abandonReservation("missing", "melina"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
