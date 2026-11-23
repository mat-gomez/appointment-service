package ar.edu.um.appointment.turnos.infrastructure.web;

import ar.edu.um.appointment.turnos.application.ports.in.InitiateReservationUseCase;
import ar.edu.um.appointment.turnos.domain.model.ReservationResponseDTO;
import ar.edu.um.appointment.turnos.domain.model.ReserveAppointmentRequestDTO;
import ar.edu.um.appointment.web.errors.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final InitiateReservationUseCase initiateReservationUseCase;

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponseDTO> reserve(
            @RequestBody ReserveAppointmentRequestDTO request,
            Principal principal,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new DomainException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authenticated user is required");
        }
        String jwt = extractJwt(authorizationHeader);
        ReservationResponseDTO response =
                initiateReservationUseCase.initiateReservation(request, principal.getName(), jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String extractJwt(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
