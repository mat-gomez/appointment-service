package ar.edu.um.appointment.turnos.application.ports.in;

import ar.edu.um.appointment.turnos.domain.model.ReservationDetailDTO;
import ar.edu.um.appointment.turnos.domain.model.UserReservationSummaryDTO;

import java.util.List;

public interface QueryAppointmentsUseCase {
    ReservationDetailDTO getReservationStatus(String reservationProcessId, String username);
    List<UserReservationSummaryDTO> getMyReservations(String username);
}
