package ar.edu.um.appointment.turnos.application.ports.out;

import ar.edu.um.appointment.turnos.domain.model.HoldResultDTO;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ReservationHoldPort {
    HoldResultDTO createHold(Long professionalId, LocalDate date, LocalTime startTime, String externalPatientId, String reservationProcessId);
    String confirmHold(String holdId, String externalPatientId, String reservationProcessId);
}
