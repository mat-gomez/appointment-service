package ar.edu.um.appointment.turnos.infrastructure.client;

import ar.edu.um.appointment.turnos.application.ports.out.CatalogSchedulePort;
import ar.edu.um.appointment.turnos.domain.model.WeeklyScheduleDTO;
import ar.edu.um.appointment.web.errors.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CatalogServiceClient implements CatalogSchedulePort {

    private static final String ERROR_CODE = "CATALOG_SERVICE_UNAVAILABLE";
    private static final String ERROR_MSG = "Catalog service is unavailable or failed to respond";

    private final RestClient catalogRestClient;

    private <T> T execute(Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientException e) {
            throw new DomainException(ERROR_CODE, HttpStatus.SERVICE_UNAVAILABLE, ERROR_MSG);
        }
    }

    public boolean isProfessionalEnabled(Long professionalId) {
        Boolean enabled = execute(() -> catalogRestClient.get()
                .uri("/api/professionals/{id}/enabled", professionalId)
                .retrieve()
                .body(Boolean.class));
        return Boolean.TRUE.equals(enabled);
    }

    public Object getWeeklySchedules(Long professionalId) {
        return execute(() -> catalogRestClient.get()
                .uri("/api/professionals/{id}/weekly-schedules", professionalId)
                .retrieve()
                .body(Object.class));
    }

    @Override
    public List<WeeklyScheduleDTO> fetchWeeklySchedules(Long professionalId, String userJwt) {
        List<WeeklyScheduleDTO> schedules = execute(() -> {
            RestClient.RequestHeadersSpec<?> request = catalogRestClient.get()
                    .uri("/api/catalog/professionals/{id}/schedules", professionalId);
            if (userJwt != null && !userJwt.isBlank()) {
                request = request.header("Authorization", "Bearer " + userJwt);
            }
            return request.retrieve().body(new ParameterizedTypeReference<>() {});
        });
        return schedules != null ? schedules : List.of();
    }
}
