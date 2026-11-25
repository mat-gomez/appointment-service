package ar.edu.um.appointment.turnos.infrastructure.web;

import ar.edu.um.appointment.turnos.application.ports.in.CancelReservationUseCase;
import ar.edu.um.appointment.turnos.application.ports.in.InitiateReservationUseCase;
import ar.edu.um.appointment.turnos.application.ports.in.QueryAppointmentsUseCase;
import ar.edu.um.appointment.turnos.application.ports.in.SubmitPhoneUseCase;
import ar.edu.um.appointment.turnos.domain.model.AdditionalInformationSubmittedEvent;
import ar.edu.um.appointment.turnos.domain.model.ReservationDetailDTO;
import ar.edu.um.appointment.turnos.domain.model.ReservationResponseDTO;
import ar.edu.um.appointment.turnos.domain.model.ReserveAppointmentRequestDTO;
import ar.edu.um.appointment.turnos.domain.model.SubmitPhoneRequestDTO;
import ar.edu.um.appointment.turnos.domain.model.UserReservationSummaryDTO;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

    @Mock
    private QueryAppointmentsUseCase queryAppointmentsUseCase;

    @Mock
    private CancelReservationUseCase cancelReservationUseCase;

    private AppointmentController appointmentController;

    @BeforeEach
    void setUp() {
        appointmentController = new AppointmentController(
                initiateReservationUseCase,
                submitPhoneUseCase,
                queryAppointmentsUseCase,
                cancelReservationUseCase
        );
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
                "evt-1",
                "AdditionalInformationSubmitted",
                "grupo-test",
                "proc-1",
                "hold-1",
                "req-1",
                new AdditionalInformationSubmittedEvent.PhoneInformation("+542615551234"),
                Instant.parse("2026-11-23T18:00:00Z"),
                1
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

    @Test
    void shouldReturnUserReservationsAndSpecificReservationStatus() {
        Principal principal = () -> "melina";
        LocalDateTime slot = LocalDateTime.of(2026, 11, 24, 9, 30);
        UserReservationSummaryDTO summary = new UserReservationSummaryDTO(
                "proc-1", 202L, slot, "WAITING_FOR_PHONE", null, slot
        );
        ReservationDetailDTO detail = new ReservationDetailDTO(
                "proc-1", "hold-1", 202L, slot, "WAITING_FOR_PHONE", null
        );
        when(queryAppointmentsUseCase.getMyReservations("melina")).thenReturn(List.of(summary));
        when(queryAppointmentsUseCase.getReservationStatus("proc-1", "melina")).thenReturn(detail);

        ResponseEntity<List<UserReservationSummaryDTO>> myResponse = appointmentController.getMyReservations(principal);
        ResponseEntity<ReservationDetailDTO> detailResponse =
                appointmentController.getReservationStatus("proc-1", principal);

        assertThat(myResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(myResponse.getBody()).containsExactly(summary);
        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detailResponse.getBody()).isEqualTo(detail);
    }

    @Test
    void shouldCancelAndAbandonReservationForAuthenticatedUser() {
        Principal principal = () -> "melina";
        LocalDateTime slot = LocalDateTime.of(2026, 11, 24, 9, 30);
        ReservationDetailDTO cancelled = new ReservationDetailDTO(
                "proc-1", "hold-1", 202L, slot, "CANCELLED", null
        );
        when(cancelReservationUseCase.cancelReservation("proc-1", "melina", "jwt-1")).thenReturn(cancelled);
        when(cancelReservationUseCase.abandonReservation("proc-2", "melina")).thenReturn(cancelled);

        ResponseEntity<ReservationDetailDTO> cancelRes =
                appointmentController.cancelReservation("proc-1", principal, "Bearer jwt-1");
        ResponseEntity<ReservationDetailDTO> abandonRes =
                appointmentController.abandonReservation("proc-2", principal);

        assertThat(cancelRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelRes.getBody()).isEqualTo(cancelled);
        assertThat(abandonRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(abandonRes.getBody()).isEqualTo(cancelled);
    }
}
