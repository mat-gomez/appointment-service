package ar.edu.um.appointment.turnos.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationPersistenceAdapterTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationPersistenceAdapter adapter;

    @Test
    void shouldReturnTrueWhenUserHasActiveOrUnboundedPendingReservation() {
        Instant now = Instant.parse("2026-11-23T12:00:00Z");
        ReservationEntity active = ReservationEntity.builder()
                .status(ReservationStatus.WAITING_FOR_PHONE)
                .expiresAt(now.plusSeconds(180))
                .build();
        when(reservationRepository.findByUsernameAndStatusIn(eq("melina"), anyCollection()))
                .thenReturn(List.of(active));

        assertThat(adapter.hasActivePendingReservation("melina", now)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPendingReservationsAreExpired() {
        Instant now = Instant.parse("2026-11-23T12:00:00Z");
        ReservationEntity expired = ReservationEntity.builder()
                .status(ReservationStatus.HOLD_CREATED)
                .expiresAt(now.minusSeconds(10))
                .build();
        when(reservationRepository.findByUsernameAndStatusIn(eq("melina"), anyCollection()))
                .thenReturn(List.of(expired));

        assertThat(adapter.hasActivePendingReservation("melina", now)).isFalse();
    }

    @Test
    void shouldDelegateSaveFindByReservationIdFindAllByUsernameAndFindAllByStatuses() {
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("proc-1")
                .username("melina")
                .build();
        List<ReservationStatus> statuses = List.of(ReservationStatus.HOLD_CREATED);
        when(reservationRepository.save(entity)).thenReturn(entity);
        when(reservationRepository.findByReservationId("proc-1")).thenReturn(Optional.of(entity));
        when(reservationRepository.findAllByUsernameOrderByCreatedAtDesc("melina")).thenReturn(List.of(entity));
        when(reservationRepository.findAllByStatusIn(statuses)).thenReturn(List.of(entity));

        assertThat(adapter.save(entity)).isSameAs(entity);
        assertThat(adapter.findByReservationId("proc-1")).contains(entity);
        assertThat(adapter.findAllByUsername("melina")).containsExactly(entity);
        assertThat(adapter.findAllByStatuses(statuses)).containsExactly(entity);
        verify(reservationRepository).save(entity);
    }
}
