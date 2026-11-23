package ar.edu.um.appointment.turnos.application.ports.out;

import ar.edu.um.appointment.turnos.domain.model.AdditionalInformationSubmittedEvent;

public interface ReservationPhoneEventPort {
    void publishAdditionalInformationSubmitted(AdditionalInformationSubmittedEvent event);
}
