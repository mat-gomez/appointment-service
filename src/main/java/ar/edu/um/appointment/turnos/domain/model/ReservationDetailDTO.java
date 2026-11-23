package ar.edu.um.appointment.turnos.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;

public record ReservationDetailDTO(
        String reservationProcessId,
        String holdId,
        Long professionalId,
        LocalDateTime appointmentTime,
        String status,
        Instant expiresAt
) {}
