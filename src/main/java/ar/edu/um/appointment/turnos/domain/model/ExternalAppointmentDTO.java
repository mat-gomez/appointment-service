package ar.edu.um.appointment.turnos.domain.model;

public record ExternalAppointmentDTO(
        String id,
        String reservationProcessId,
        String holdId,
        String status
) {}
