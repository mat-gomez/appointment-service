package ar.edu.um.appointment.turnos.infrastructure.client;

import ar.edu.um.appointment.web.errors.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class CatedraAppointmentsClient {

    private final RestClient catedraRestClient;

    private String getAuthHeader(String technicalToken) {
        return "Bearer " + technicalToken;
    }

    public Object getAppointmentOccupancies(String technicalToken, Long professionalId) {
        try {
            return catedraRestClient.get()
                    .uri("/api/appointment-occupancies?professionalId={id}", professionalId)
                    .header("Authorization", getAuthHeader(technicalToken))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
        } catch (RestClientException e) {
            throw new DomainException(
                    "CATEDRA_SERVICE_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Catedra service failed to respond to getOccupancies"
            );
        }
    }

    public Object createAppointmentHold(String technicalToken, Object holdRequest) {
        try {
            return catedraRestClient.post()
                    .uri("/api/appointment-holds")
                    .header("Authorization", getAuthHeader(technicalToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                    .body(holdRequest)
                    .retrieve()
                    .body(Object.class);
        } catch (RestClientException e) {
            throw new DomainException(
                    "CATEDRA_SERVICE_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Catedra service failed to create hold"
            );
        }
    }

    public Object confirmAppointment(String technicalToken, Long appointmentId) {
        try {
            return catedraRestClient.post()
                    .uri("/api/appointments/{id}/confirm", appointmentId)
                    .header("Authorization", getAuthHeader(technicalToken))
                    .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Object.class);
        } catch (RestClientException e) {
            throw new DomainException(
                    "CATEDRA_SERVICE_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Catedra service failed to confirm appointment"
            );
        }
    }

    public Object createAppointment(String technicalToken, Object appointmentRequest) {
        try {
            return catedraRestClient.post()
                    .uri("/api/appointments")
                    .header("Authorization", getAuthHeader(technicalToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                    .body(appointmentRequest)
                    .retrieve()
                    .body(Object.class);
        } catch (RestClientException e) {
            throw new DomainException(
                    "CATEDRA_SERVICE_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Catedra service failed to create appointment"
            );
        }
    }

    public void cancelAppointment(String technicalToken, Long appointmentId) {
        try {
            catedraRestClient.delete()
                    .uri("/api/appointments/{id}/cancel", appointmentId)
                    .header("Authorization", getAuthHeader(technicalToken))
                    .accept(MediaType.APPLICATION_PROBLEM_JSON, MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new DomainException(
                    "CATEDRA_SERVICE_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Catedra service failed to cancel appointment"
            );
        }
    }
}