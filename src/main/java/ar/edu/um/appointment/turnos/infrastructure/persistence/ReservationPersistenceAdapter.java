package ar.edu.um.appointment.turnos.infrastructure.persistence;

import ar.edu.um.appointment.turnos.application.ports.out.ReservationPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<ReservationEntity> findByReservationId(String reservationId) {
        return reservationRepository.findByReservationId(reservationId);
    }

    @Override
    public List<ReservationEntity> findAllByUsername(String username) {
        return reservationRepository.findAllByUsernameOrderByCreatedAtDesc(username);
    }

    @Override
    public List<ReservationEntity> findAllByStatuses(Collection<ReservationStatus> statuses) {
        return reservationRepository.findAllByStatusIn(statuses);
    }
}
