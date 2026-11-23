package ar.edu.um.appointment.turnos.application.ports.in;

import ar.edu.um.appointment.turnos.domain.model.AdditionalInformationSubmittedEvent;

public interface SubmitPhoneUseCase {
    AdditionalInformationSubmittedEvent submitPhone(
            String reservationProcessId,
            String rawPhone,
            String username
    );
}
