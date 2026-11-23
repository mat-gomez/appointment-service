package ar.edu.um.appointment.turnos.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WeeklyScheduleDTO(
        Long id,
        Long professionalId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int slotDurationMinutes,
        boolean enabled
) {}
