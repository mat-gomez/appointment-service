package ar.edu.um.appointment.turnos.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record OccupancyDTO(
        LocalDate date,
        LocalTime startTime,
        String status,
        Instant expiresAt
) {}
