package ar.edu.um.appointment.turnos.infrastructure.client;

import ar.edu.um.appointment.turnos.application.ports.out.OccupancyPort;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationHoldPort;
import ar.edu.um.appointment.turnos.domain.model.HoldResultDTO;
import ar.edu.um.appointment.turnos.domain.model.OccupancyDTO;
import ar.edu.um.appointment.web.errors.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CatedraAppointmentsClient implements OccupancyPort, ReservationHoldPort {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ERROR_CODE = "CATEDRA_SERVICE_UNAVAILABLE";

    private final RestClient catedraRestClient;

    @Value("${catedra.jwt-token:}")
    private String technicalToken;

    private record OccupanciesResponse(List<OccupancyDTO> occupancies) {}

    private record HoldResponse(String holdId, Instant expiresAt) {}

    private String getAuthHeader(String token) {
        return "Bearer " + token;
    }

    private <T> T execute(Supplier<T> call, String errorMessage) {
        try {
            return call.get();
        } catch (RestClientException e) {
            throw new DomainException(ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE, errorMessage);
        }
    }

    @Override
    public List<OccupancyDTO> fetchOccupancies(Long professionalId, LocalDate from, LocalDate to) {
        OccupanciesResponse response = execute(() -> catedraRestClient.get()
                .uri("/api/appointment-occupancies?professionalId={id}&from={from}&to={to}", professionalId, from, to)
                .header(AUTHORIZATION_HEADER, getAuthHeader(technicalToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(OccupanciesResponse.class), "Catedra service failed to respond to getOccupancies");
        return response != null && response.occupancies() != null ? response.occupancies() : List.of();
    }

    @Override
    public HoldResultDTO createHold(Long professionalId, LocalDate date, LocalTime startTime, String reservationProcessId) {
        Map<String, Object> body = Map.of(
                "professionalId", professionalId,
                "date", date.toString(),
                "startTime", startTime.toString(),
                "reservationProcessId", reservationProcessId
        );
        HoldResponse response = execute(() -> catedraRestClient.post()
                .uri("/api/appointment-holds")
                .header(AUTHORIZATION_HEADER, getAuthHeader(technicalToken))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(HoldResponse.class), "Catedra service failed to create hold");
        if (response == null || response.holdId() == null) {
            throw new DomainException(ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE, "Catedra returned empty hold response");
        }
        return new HoldResultDTO(response.holdId(), response.expiresAt());
    }

    @Override
    public void confirmHold(String holdId, String reservationProcessId) {
        Map<String, Object> body = Map.of("reservationProcessId", reservationProcessId);
        execute(() -> {
            catedraRestClient.post()
                    .uri("/api/appointment-holds/{holdId}/confirm", holdId)
                    .header(AUTHORIZATION_HEADER, getAuthHeader(technicalToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        }, "Catedra service failed to confirm hold");
    }

    public Object getAppointmentOccupancies(String token, Long professionalId) {
        return execute(() -> catedraRestClient.get()
                .uri("/api/appointment-occupancies?professionalId={id}", professionalId)
                .header(AUTHORIZATION_HEADER, getAuthHeader(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class), "Catedra service failed to respond to getOccupancies");
    }

    public Object createAppointmentHold(String token, Object holdRequest) {
        return execute(() -> catedraRestClient.post()
                .uri("/api/appointment-holds")
                .header(AUTHORIZATION_HEADER, getAuthHeader(token))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                .body(holdRequest)
                .retrieve()
                .body(Object.class), "Catedra service failed to create hold");
    }

    public Object confirmAppointment(String token, Long appointmentId) {
        return execute(() -> catedraRestClient.post()
                .uri("/api/appointments/{id}/confirm", appointmentId)
                .header(AUTHORIZATION_HEADER, getAuthHeader(token))
                .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class), "Catedra service failed to confirm appointment");
    }

    public Object createAppointment(String token, Object appointmentRequest) {
        return execute(() -> catedraRestClient.post()
                .uri("/api/appointments")
                .header(AUTHORIZATION_HEADER, getAuthHeader(token))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                .body(appointmentRequest)
                .retrieve()
                .body(Object.class), "Catedra service failed to create appointment");
    }

    public void cancelAppointment(String token, Long appointmentId) {
        execute(() -> {
            catedraRestClient.delete()
                    .uri("/api/appointments/{id}/cancel", appointmentId)
                    .header(AUTHORIZATION_HEADER, getAuthHeader(token))
                    .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        }, "Catedra service failed to cancel appointment");
    }
}
