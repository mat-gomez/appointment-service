package ar.edu.um.appointment.turnos.application.usecases;

import ar.edu.um.appointment.turnos.application.ports.in.CancelReservationUseCase;
import ar.edu.um.appointment.turnos.application.ports.out.CancelAppointmentPort;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationPersistencePort;
import ar.edu.um.appointment.turnos.domain.model.ReservationDetailDTO;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationStatus;
import ar.edu.um.appointment.web.errors.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CancelReservationService implements CancelReservationUseCase {

    private static final Set<ReservationStatus> ABANDONABLE_STATUSES = EnumSet.of(
            ReservationStatus.HOLD_CREATED,
            ReservationStatus.WAITING_FOR_PHONE,
            ReservationStatus.PHONE_REJECTED
    );

    private final ReservationPersistencePort reservationPersistencePort;
    private final CancelAppointmentPort cancelAppointmentPort;

    @Override
    public ReservationDetailDTO cancelReservation(String reservationProcessId, String username, String userJwt) {
        ReservationEntity entity = findOwnedReservation(reservationProcessId, username);

        if (entity.getStatus() == ReservationStatus.CANCELLED) {
            return toDetailDTO(entity);
        }

        if (entity.getStatus() != ReservationStatus.CONFIRMED) {
            throw new DomainException(
                    "INVALID_RESERVATION_STATE",
                    HttpStatus.CONFLICT,
                    "Only confirmed reservations can be cancelled"
            );
        }

        cancelAppointmentPort.cancelConfirmedAppointment(
                entity.getReservationId(),
                entity.getHoldId(),
                userJwt
        );
        entity.setStatus(ReservationStatus.CANCELLED);
        ReservationEntity saved = reservationPersistencePort.save(entity);
        return toDetailDTO(saved);
    }

    @Override
    public ReservationDetailDTO abandonReservation(String reservationProcessId, String username) {
        ReservationEntity entity = findOwnedReservation(reservationProcessId, username);

        if (entity.getStatus() == ReservationStatus.CANCELLED) {
            return toDetailDTO(entity);
        }

        if (!ABANDONABLE_STATUSES.contains(entity.getStatus())) {
            throw new DomainException(
                    "INVALID_PENDING_STATE",
                    HttpStatus.CONFLICT,
                    "Only pending reservation processes can be abandoned"
            );
        }

        entity.setStatus(ReservationStatus.CANCELLED);
        ReservationEntity saved = reservationPersistencePort.save(entity);
        return toDetailDTO(saved);
    }

    private ReservationEntity findOwnedReservation(String reservationProcessId, String username) {
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
        return entity;
    }

    private ReservationDetailDTO toDetailDTO(ReservationEntity entity) {
        return new ReservationDetailDTO(
                entity.getReservationId(),
                entity.getHoldId(),
                entity.getProfessionalId(),
                entity.getAppointmentTime(),
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getExpiresAt()
        );
    }
}
