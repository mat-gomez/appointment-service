package ar.edu.um.appointment.turnos.application.ports.out;

import ar.edu.um.appointment.turnos.domain.model.OccupancyDTO;

import java.time.LocalDate;
import java.util.List;

public interface OccupancyPort {
    List<OccupancyDTO> fetchOccupancies(Long professionalId, LocalDate from, LocalDate to);
}
