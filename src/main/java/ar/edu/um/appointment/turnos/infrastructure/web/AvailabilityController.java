package ar.edu.um.appointment.turnos.infrastructure.web;

import ar.edu.um.appointment.turnos.application.ports.in.GetAvailabilityUseCase;
import ar.edu.um.appointment.turnos.domain.model.AvailabilityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final GetAvailabilityUseCase getAvailabilityUseCase;

    @GetMapping
    public ResponseEntity<AvailabilityResponseDTO> getAvailability(
            @RequestParam Long professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String jwt = extractJwt(authorizationHeader);
        return ResponseEntity.ok(getAvailabilityUseCase.getAvailability(professionalId, date, jwt));
    }

    private String extractJwt(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
