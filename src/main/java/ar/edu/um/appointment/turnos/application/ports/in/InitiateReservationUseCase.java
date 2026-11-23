package ar.edu.um.appointment.turnos.application.ports.in;

import ar.edu.um.appointment.turnos.domain.model.ReservationResponseDTO;
import ar.edu.um.appointment.turnos.domain.model.ReserveAppointmentRequestDTO;

public interface InitiateReservationUseCase {
    ReservationResponseDTO initiateReservation(
            ReserveAppointmentRequestDTO request,
            String username,
            String userJwt
    );
}
