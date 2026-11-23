package ar.edu.um.appointment.turnos.application.ports.out;

import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationPersistencePort {
    boolean hasActivePendingReservation(String username, Instant now);
    ReservationEntity save(ReservationEntity entity);
    Optional<ReservationEntity> findByReservationId(String reservationId);
    List<ReservationEntity> findAllByUsername(String username);
    List<ReservationEntity> findAllByStatuses(Collection<ReservationStatus> statuses);
}
