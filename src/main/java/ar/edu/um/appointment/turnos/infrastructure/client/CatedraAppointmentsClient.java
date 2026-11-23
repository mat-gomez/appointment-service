package ar.edu.um.appointment.turnos.infrastructure.client;

import ar.edu.um.appointment.web.errors.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CatedraAppointmentsClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ERROR_CODE = "CATEDRA_SERVICE_UNAVAILABLE";

    private final RestClient catedraRestClient;

    private String getAuthHeader(String technicalToken) {
        return "Bearer " + technicalToken;
    }

    private <T> T execute(Supplier<T> call, String errorMessage) {
        try {
            return call.get();
        } catch (RestClientException e) {
            throw new DomainException(ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE, errorMessage);
        }
    }

    public Object getAppointmentOccupancies(String technicalToken, Long professionalId) {
        return execute(() -> catedraRestClient.get()
                .uri("/api/appointment-occupancies?professionalId={id}", professionalId)
                .header(AUTHORIZATION_HEADER, getAuthHeader(technicalToken))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class), "Catedra service failed to respond to getOccupancies");
    }

    public Object createAppointmentHold(String technicalToken, Object holdRequest) {
        return execute(() -> catedraRestClient.post()
                .uri("/api/appointment-holds")
                .header(AUTHORIZATION_HEADER, getAuthHeader(technicalToken))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                .body(holdRequest)
                .retrieve()
                .body(Object.class), "Catedra service failed to create hold");
    }

    public Object confirmAppointment(String technicalToken, Long appointmentId) {
        return execute(() -> catedraRestClient.post()
                .uri("/api/appointments/{id}/confirm", appointmentId)
                .header(AUTHORIZATION_HEADER, getAuthHeader(technicalToken))
                .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Object.class), "Catedra service failed to confirm appointment");
    }

    public Object createAppointment(String technicalToken, Object appointmentRequest) {
        return execute(() -> catedraRestClient.post()
                .uri("/api/appointments")
                .header(AUTHORIZATION_HEADER, getAuthHeader(technicalToken))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                .body(appointmentRequest)
                .retrieve()
                .body(Object.class), "Catedra service failed to create appointment");
    }

    public void cancelAppointment(String technicalToken, Long appointmentId) {
        execute(() -> {
            catedraRestClient.delete()
                    .uri("/api/appointments/{id}/cancel", appointmentId)
                    .header(AUTHORIZATION_HEADER, getAuthHeader(technicalToken))
                    .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        }, "Catedra service failed to cancel appointment");
    }
}