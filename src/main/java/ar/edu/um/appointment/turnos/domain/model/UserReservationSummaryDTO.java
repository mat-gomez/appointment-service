package ar.edu.um.appointment.turnos.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;

public record UserReservationSummaryDTO(
        String reservationProcessId,
        Long professionalId,
        LocalDateTime appointmentTime,
        String status,
        Instant expiresAt,
        LocalDateTime createdAt
) {}
