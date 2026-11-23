package ar.edu.um.appointment.turnos.application.ports.out;

import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;

import java.time.Instant;

public interface ReservationPersistencePort {
    boolean hasActivePendingReservation(String username, Instant now);
    ReservationEntity save(ReservationEntity entity);
}
