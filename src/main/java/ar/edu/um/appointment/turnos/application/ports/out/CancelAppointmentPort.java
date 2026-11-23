package ar.edu.um.appointment.turnos.application.ports.out;

public interface CancelAppointmentPort {
    void cancelConfirmedAppointment(String reservationProcessId, String holdId, String userJwt);
}
