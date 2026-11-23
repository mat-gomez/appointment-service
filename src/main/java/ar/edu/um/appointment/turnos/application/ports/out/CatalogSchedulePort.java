package ar.edu.um.appointment.turnos.application.ports.out;

import ar.edu.um.appointment.turnos.domain.model.WeeklyScheduleDTO;

import java.util.List;

public interface CatalogSchedulePort {
    List<WeeklyScheduleDTO> fetchWeeklySchedules(Long professionalId, String userJwt);
}
