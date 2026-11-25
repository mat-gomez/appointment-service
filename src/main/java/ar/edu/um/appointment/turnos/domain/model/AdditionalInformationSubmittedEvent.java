package ar.edu.um.appointment.turnos.domain.model;

import java.time.Instant;

public record AdditionalInformationSubmittedEvent(
        String eventId,
        String eventType,
        String groupId,
        String reservationProcessId,
        String holdId,
        String requestEventId,
        PhoneInformation information,
        Instant occurredAt,
        int schemaVersion
) {
    public record PhoneInformation(String phoneNumber) {}
}
