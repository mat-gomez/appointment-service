package ar.edu.um.appointment.turnos.application.ports.out;

import ar.edu.um.appointment.turnos.domain.model.ExternalAppointmentDTO;

import java.util.List;

public interface AppointmentReconciliationPort {
    List<ExternalAppointmentDTO> fetchAppointments();
}
