package ar.edu.um.appointment.turnos.infrastructure.web;

import ar.edu.um.appointment.turnos.application.ports.in.InitiateReservationUseCase;
import ar.edu.um.appointment.turnos.application.ports.in.SubmitPhoneUseCase;
import ar.edu.um.appointment.turnos.domain.model.AdditionalInformationSubmittedEvent;
import ar.edu.um.appointment.turnos.domain.model.ReservationResponseDTO;
import ar.edu.um.appointment.turnos.domain.model.ReserveAppointmentRequestDTO;
import ar.edu.um.appointment.turnos.domain.model.SubmitPhoneRequestDTO;
import ar.edu.um.appointment.web.errors.DomainException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final InitiateReservationUseCase initiateReservationUseCase;
    private final SubmitPhoneUseCase submitPhoneUseCase;

    public AppointmentController(InitiateReservationUseCase initiateReservationUseCase) {
        this(initiateReservationUseCase, null);
    }

    @Autowired
    public AppointmentController(
            InitiateReservationUseCase initiateReservationUseCase,
            SubmitPhoneUseCase submitPhoneUseCase
    ) {
        this.initiateReservationUseCase = initiateReservationUseCase;
        this.submitPhoneUseCase = submitPhoneUseCase;
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponseDTO> reserve(
            @RequestBody ReserveAppointmentRequestDTO request,
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String username = requireAuthenticatedUser(principal);
        String jwt = extractJwt(authorizationHeader);
        ReservationResponseDTO response =
                initiateReservationUseCase.initiateReservation(request, username, jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{reservationProcessId}/phone")
    public ResponseEntity<AdditionalInformationSubmittedEvent> submitPhone(
            @PathVariable String reservationProcessId,
            @RequestBody SubmitPhoneRequestDTO request,
            Principal principal
    ) {
        String username = requireAuthenticatedUser(principal);
        String rawPhone = request != null ? request.phone() : null;
        AdditionalInformationSubmittedEvent event =
                submitPhoneUseCase.submitPhone(reservationProcessId, rawPhone, username);
        return ResponseEntity.accepted().body(event);
    }

    private String requireAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new DomainException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authenticated user is required");
        }
        return principal.getName();
    }

    private String extractJwt(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
