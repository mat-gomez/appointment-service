package ar.edu.um.appointment.turnos.infrastructure.persistence;

import ar.edu.um.appointment.turnos.application.ports.out.ReservationPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationPersistenceAdapter implements ReservationPersistencePort {

    private static final List<ReservationStatus> PENDING_STATUSES = List.of(
            ReservationStatus.HOLD_CREATED,
            ReservationStatus.WAITING_FOR_PHONE,
            ReservationStatus.PHONE_REJECTED
    );

    private final ReservationRepository reservationRepository;

    @Override
    public boolean hasActivePendingReservation(String username, Instant now) {
        return reservationRepository.findByUsernameAndStatusIn(username, PENDING_STATUSES)
                .stream()
                .anyMatch(r -> r.getExpiresAt() == null || r.getExpiresAt().isAfter(now));
    }

    @Override
    public ReservationEntity save(ReservationEntity entity) {
        return reservationRepository.save(entity);
    }
}
