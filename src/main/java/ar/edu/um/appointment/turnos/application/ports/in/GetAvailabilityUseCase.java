package ar.edu.um.appointment.turnos.application.ports.in;

import ar.edu.um.appointment.turnos.domain.model.AvailabilityResponseDTO;

import java.time.LocalDate;

public interface GetAvailabilityUseCase {
    AvailabilityResponseDTO getAvailability(Long professionalId, LocalDate date, String userJwt);
}
