package ar.edu.um.appointment.turnos.domain.model;

public record AppointmentKafkaEventDTO(
        String eventId,
        String eventType,
        String reservationProcessId,
        String reason
) {}
