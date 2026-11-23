package ar.edu.um.appointment.turnos.domain.model;

import java.time.Instant;

public record AdditionalInformationSubmittedEvent(
        String eventId,
        String reservationProcessId,
        String phone,
        Instant occurredAt
) {}
