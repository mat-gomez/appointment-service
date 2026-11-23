package ar.edu.um.appointment.turnos.domain.model;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityResponseDTO(
        Long professionalId,
        LocalDate date,
        List<AvailabilitySlotDTO> availableSlots
) {}
