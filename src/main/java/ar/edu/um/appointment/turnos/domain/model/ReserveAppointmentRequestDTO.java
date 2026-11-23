package ar.edu.um.appointment.turnos.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReserveAppointmentRequestDTO(
        Long professionalId,
        LocalDate date,
        LocalTime startTime
) {}
