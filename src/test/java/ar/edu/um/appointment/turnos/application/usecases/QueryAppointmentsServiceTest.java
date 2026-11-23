package ar.edu.um.appointment.turnos.application.usecases;

import ar.edu.um.appointment.turnos.application.ports.out.ReservationPersistencePort;
import ar.edu.um.appointment.turnos.domain.model.ReservationDetailDTO;
import ar.edu.um.appointment.turnos.domain.model.UserReservationSummaryDTO;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationStatus;
import ar.edu.um.appointment.web.errors.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryAppointmentsServiceTest {

    @Mock
    private ReservationPersistencePort reservationPersistencePort;

    @InjectMocks
    private QueryAppointmentsService queryAppointmentsService;

    @Test
    void shouldReturnReservationDetailWhenOwnedByAuthenticatedUser() {
        Instant expiresAt = Instant.parse("2026-11-24T12:35:00Z");
        LocalDateTime slot = LocalDateTime.of(2026, 11, 24, 9, 30);
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("proc-10")
                .holdId("hold-10")
                .username("melina")
                .professionalId(202L)
                .appointmentTime(slot)
                .status(ReservationStatus.WAITING_FOR_PHONE)
                .expiresAt(expiresAt)
                .build();
        when(reservationPersistencePort.findByReservationId("proc-10")).thenReturn(Optional.of(entity));

        ReservationDetailDTO detail = queryAppointmentsService.getReservationStatus("proc-10", "melina");

        assertThat(detail.reservationProcessId()).isEqualTo("proc-10");
        assertThat(detail.holdId()).isEqualTo("hold-10");
        assertThat(detail.professionalId()).isEqualTo(202L);
        assertThat(detail.appointmentTime()).isEqualTo(slot);
        assertThat(detail.status()).isEqualTo("WAITING_FOR_PHONE");
        assertThat(detail.expiresAt()).isEqualTo(expiresAt);
    }

    @Test
    void shouldThrowNotFoundWhenReservationDoesNotExist() {
        when(reservationPersistencePort.findByReservationId("missing-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queryAppointmentsService.getReservationStatus("missing-id", "melina"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldThrowForbiddenWhenReservationBelongsToAnotherUser() {
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("proc-20")
                .username("otro_usuario")
                .status(ReservationStatus.CONFIRMED)
                .build();
        when(reservationPersistencePort.findByReservationId("proc-20")).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> queryAppointmentsService.getReservationStatus("proc-20", "melina"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void shouldReturnUserReservationsList() {
        LocalDateTime slot = LocalDateTime.of(2026, 11, 24, 10, 0);
        LocalDateTime created = LocalDateTime.of(2026, 11, 23, 18, 0);
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("proc-30")
                .username("melina")
                .professionalId(202L)
                .appointmentTime(slot)
                .status(ReservationStatus.CONFIRMED)
                .createdAt(created)
                .build();
        when(reservationPersistencePort.findAllByUsername("melina")).thenReturn(List.of(entity));

        List<UserReservationSummaryDTO> list = queryAppointmentsService.getMyReservations("melina");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).reservationProcessId()).isEqualTo("proc-30");
        assertThat(list.get(0).status()).isEqualTo("CONFIRMED");
        assertThat(list.get(0).createdAt()).isEqualTo(created);
    }
}
