package ar.edu.um.appointment.turnos.infrastructure.web;

import ar.edu.um.appointment.turnos.application.ports.in.InitiateReservationUseCase;
import ar.edu.um.appointment.turnos.application.ports.in.SubmitPhoneUseCase;
import ar.edu.um.appointment.turnos.domain.model.AdditionalInformationSubmittedEvent;
import ar.edu.um.appointment.turnos.domain.model.ReservationResponseDTO;
import ar.edu.um.appointment.turnos.domain.model.ReserveAppointmentRequestDTO;
import ar.edu.um.appointment.turnos.domain.model.SubmitPhoneRequestDTO;
import ar.edu.um.appointment.web.errors.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private InitiateReservationUseCase initiateReservationUseCase;

    @Mock
    private SubmitPhoneUseCase submitPhoneUseCase;

    private AppointmentController appointmentController;

    @BeforeEach
    void setUp() {
        appointmentController = new AppointmentController(initiateReservationUseCase, submitPhoneUseCase);
    }

    @Test
    void shouldReturnCreatedWhenReservationIsInitiated() {
        LocalDate date = LocalDate.of(2026, 11, 24);
        ReserveAppointmentRequestDTO request = new ReserveAppointmentRequestDTO(202L, date, LocalTime.of(9, 30));
        ReservationResponseDTO expected = new ReservationResponseDTO(
                "proc-1", "hold-1", 202L, date, LocalTime.of(9, 30), "WAITING_FOR_PHONE", Instant.now()
        );
        Principal principal = () -> "melina";
        when(initiateReservationUseCase.initiateReservation(request, "melina", "jwt-token")).thenReturn(expected);

        ResponseEntity<ReservationResponseDTO> response =
                appointmentController.reserve(request, principal, "Bearer jwt-token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(initiateReservationUseCase).initiateReservation(request, "melina", "jwt-token");
    }

    @Test
    void shouldPassNullJwtWhenAuthorizationHeaderIsNotBearer() {
        LocalDate date = LocalDate.of(2026, 11, 24);
        ReserveAppointmentRequestDTO request = new ReserveAppointmentRequestDTO(202L, date, LocalTime.of(9, 30));
        Principal principal = () -> "melina";

        ResponseEntity<ReservationResponseDTO> response =
                appointmentController.reserve(request, principal, "Basic abc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(initiateReservationUseCase).initiateReservation(request, "melina", null);
    }

    @Test
    void shouldThrowUnauthorizedWhenPrincipalIsMissing() {
        ReserveAppointmentRequestDTO request =
                new ReserveAppointmentRequestDTO(202L, LocalDate.of(2026, 11, 24), LocalTime.of(9, 30));

        assertThatThrownBy(() -> appointmentController.reserve(request, null, "Bearer jwt"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void shouldAcceptPhoneSubmissionForAuthenticatedUser() {
        AdditionalInformationSubmittedEvent expected = new AdditionalInformationSubmittedEvent(
                "evt-phone-1", "proc-1", "+542615551234", Instant.now()
        );
        Principal principal = () -> "melina";
        when(submitPhoneUseCase.submitPhone("proc-1", "+54 261 555-1234", "melina")).thenReturn(expected);

        ResponseEntity<AdditionalInformationSubmittedEvent> response = appointmentController.submitPhone(
                "proc-1", new SubmitPhoneRequestDTO("+54 261 555-1234"), principal
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(submitPhoneUseCase).submitPhone("proc-1", "+54 261 555-1234", "melina");
    }
}
