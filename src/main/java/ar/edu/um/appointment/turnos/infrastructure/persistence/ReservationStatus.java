package ar.edu.um.appointment.turnos.infrastructure.persistence;

public enum ReservationStatus {
    HOLD_CREATED,
    WAITING_FOR_PHONE,
    PHONE_REJECTED,
    CONFIRMED,
    EXPIRED,
    INVALID,
    CANCELLED
}