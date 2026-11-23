package ar.edu.um.appointment.turnos.application.ports.in;

import ar.edu.um.appointment.turnos.domain.model.ReservationDetailDTO;

public interface CancelReservationUseCase {
    ReservationDetailDTO cancelReservation(String reservationProcessId, String username, String userJwt);
    ReservationDetailDTO abandonReservation(String reservationProcessId, String username);
}
