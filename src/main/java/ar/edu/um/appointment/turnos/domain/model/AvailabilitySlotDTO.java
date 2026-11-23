package ar.edu.um.appointment.turnos.domain.model;

import java.time.LocalTime;

public record AvailabilitySlotDTO(
        LocalTime startTime,
        LocalTime endTime
) {}
