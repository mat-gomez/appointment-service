package ar.edu.um.appointment.turnos.domain.model;

import java.time.Instant;

public record HoldResultDTO(
        String holdId,
        Instant expiresAt
) {}
