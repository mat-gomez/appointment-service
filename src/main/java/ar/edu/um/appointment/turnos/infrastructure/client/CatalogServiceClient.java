package ar.edu.um.appointment.turnos.infrastructure.client;

import ar.edu.um.appointment.web.errors.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class CatalogServiceClient {

    private final RestClient catalogRestClient;

    public boolean isProfessionalEnabled(Long professionalId) {
        try {
            Boolean enabled = catalogRestClient.get()
                    .uri("/api/professionals/{id}/enabled", professionalId)
                    .retrieve()
                    .body(Boolean.class);
            return Boolean.TRUE.equals(enabled);
        } catch (RestClientException e) {
            throw new DomainException(
                    "CATALOG_SERVICE_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Catalog service is unavailable or failed to respond"
            );
        }
    }

    public Object getWeeklySchedules(Long professionalId) {
        try {
            return catalogRestClient.get()
                    .uri("/api/professionals/{id}/weekly-schedules", professionalId)
                    .retrieve()
                    .body(Object.class);
        } catch (RestClientException e) {
            throw new DomainException(
                    "CATALOG_SERVICE_UNAVAILABLE",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Catalog service is unavailable or failed to respond"
            );
        }
    }
}