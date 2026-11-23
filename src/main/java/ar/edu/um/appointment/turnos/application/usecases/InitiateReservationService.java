package ar.edu.um.appointment.turnos.application.usecases;

import ar.edu.um.appointment.turnos.application.ports.in.InitiateReservationUseCase;
import ar.edu.um.appointment.turnos.application.ports.out.CatalogSchedulePort;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationHoldPort;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationPersistencePort;
import ar.edu.um.appointment.turnos.domain.model.HoldResultDTO;
import ar.edu.um.appointment.turnos.domain.model.ReservationResponseDTO;
import ar.edu.um.appointment.turnos.domain.model.ReserveAppointmentRequestDTO;
import ar.edu.um.appointment.turnos.domain.model.WeeklyScheduleDTO;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationStatus;
import ar.edu.um.appointment.web.errors.DomainException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class InitiateReservationService implements InitiateReservationUseCase {

    private final CatalogSchedulePort catalogSchedulePort;
    private final ReservationHoldPort reservationHoldPort;
    private final ReservationPersistencePort reservationPersistencePort;
    private final Clock clock;

    @Autowired
    public InitiateReservationService(
            CatalogSchedulePort catalogSchedulePort,
            ReservationHoldPort reservationHoldPort,
            ReservationPersistencePort reservationPersistencePort
    ) {
        this(catalogSchedulePort, reservationHoldPort, reservationPersistencePort, Clock.systemUTC());
    }

    InitiateReservationService(
            CatalogSchedulePort catalogSchedulePort,
            ReservationHoldPort reservationHoldPort,
            ReservationPersistencePort reservationPersistencePort,
            Clock clock
    ) {
        this.catalogSchedulePort = catalogSchedulePort;
        this.reservationHoldPort = reservationHoldPort;
        this.reservationPersistencePort = reservationPersistencePort;
        this.clock = clock;
    }

    @Override
    public ReservationResponseDTO initiateReservation(
            ReserveAppointmentRequestDTO request,
            String username,
            String userJwt
    ) {
        validateRequest(request);

        if (reservationPersistencePort.hasActivePendingReservation(username, clock.instant())) {
            throw new DomainException(
                    "ACTIVE_RESERVATION_EXISTS",
                    HttpStatus.CONFLICT,
                    "User already has an active pending reservation process"
            );
        }

        validateScheduleSlot(request.professionalId(), request.date(), request.startTime(), userJwt);

        String reservationProcessId = UUID.randomUUID().toString();
        HoldResultDTO hold = reservationHoldPort.createHold(
                request.professionalId(),
                request.date(),
                request.startTime(),
                reservationProcessId
        );

        ReservationEntity entity = ReservationEntity.builder()
                .reservationId(reservationProcessId)
                .username(username)
                .holdId(hold.holdId())
                .professionalId(request.professionalId())
                .appointmentTime(LocalDateTime.of(request.date(), request.startTime()))
                .status(ReservationStatus.HOLD_CREATED)
                .expiresAt(hold.expiresAt())
                .build();
        entity = reservationPersistencePort.save(entity);

        reservationHoldPort.confirmHold(hold.holdId(), reservationProcessId);
        entity.setStatus(ReservationStatus.WAITING_FOR_PHONE);
        entity = reservationPersistencePort.save(entity);

        return new ReservationResponseDTO(
                entity.getReservationId(),
                entity.getHoldId(),
                entity.getProfessionalId(),
                request.date(),
                request.startTime(),
                entity.getStatus().name(),
                entity.getExpiresAt()
        );
    }

    private void validateRequest(ReserveAppointmentRequestDTO request) {
        if (request == null || request.professionalId() == null
                || request.date() == null || request.startTime() == null) {
            throw new DomainException(
                    "INVALID_RESERVATION_REQUEST",
                    HttpStatus.BAD_REQUEST,
                    "professionalId, date and startTime are required"
            );
        }
    }

    private void validateScheduleSlot(Long professionalId, LocalDate date, LocalTime startTime, String userJwt) {
        List<WeeklyScheduleDTO> schedules = catalogSchedulePort.fetchWeeklySchedules(professionalId, userJwt);
        boolean valid = schedules.stream()
                .filter(s -> s.enabled() && date.getDayOfWeek().equals(s.dayOfWeek()) && s.slotDurationMinutes() > 0)
                .anyMatch(s -> !startTime.isBefore(s.startTime())
                        && !startTime.plusMinutes(s.slotDurationMinutes()).isAfter(s.endTime()));

        if (!valid) {
            throw new DomainException(
                    "INVALID_SCHEDULE_SLOT",
                    HttpStatus.BAD_REQUEST,
                    "Requested slot does not belong to an enabled schedule for this professional"
            );
        }
    }
}
