package ar.edu.um.appointment.turnos.application.usecases;

import ar.edu.um.appointment.turnos.application.ports.in.QueryAppointmentsUseCase;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationPersistencePort;
import ar.edu.um.appointment.turnos.domain.model.ReservationDetailDTO;
import ar.edu.um.appointment.turnos.domain.model.UserReservationSummaryDTO;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.web.errors.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryAppointmentsService implements QueryAppointmentsUseCase {

    private final ReservationPersistencePort reservationPersistencePort;

    @Override
    public ReservationDetailDTO getReservationStatus(String reservationProcessId, String username) {
        ReservationEntity entity = reservationPersistencePort.findByReservationId(reservationProcessId)
                .orElseThrow(() -> new DomainException(
                        "RESERVATION_NOT_FOUND",
                        HttpStatus.NOT_FOUND,
                        "Reservation process not found"
                ));

        if (entity.getUsername() == null || !entity.getUsername().equals(username)) {
            throw new DomainException(
                    "FORBIDDEN_RESERVATION",
                    HttpStatus.FORBIDDEN,
                    "Reservation belongs to another user"
            );
        }

        return new ReservationDetailDTO(
                entity.getReservationId(),
                entity.getHoldId(),
                entity.getProfessionalId(),
                entity.getAppointmentTime(),
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getExpiresAt()
        );
    }

    @Override
    public List<UserReservationSummaryDTO> getMyReservations(String username) {
        return reservationPersistencePort.findAllByUsername(username)
                .stream()
                .map(entity -> new UserReservationSummaryDTO(
                        entity.getReservationId(),
                        entity.getProfessionalId(),
                        entity.getAppointmentTime(),
                        entity.getStatus() != null ? entity.getStatus().name() : null,
                        entity.getExpiresAt(),
                        entity.getCreatedAt()
                ))
                .toList();
    }
}
