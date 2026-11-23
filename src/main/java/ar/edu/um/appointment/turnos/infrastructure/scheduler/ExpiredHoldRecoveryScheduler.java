package ar.edu.um.appointment.turnos.infrastructure.scheduler;

import ar.edu.um.appointment.turnos.application.ports.out.AppointmentReconciliationPort;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationPersistencePort;
import ar.edu.um.appointment.turnos.domain.model.ExternalAppointmentDTO;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExpiredHoldRecoveryScheduler {

    private static final List<ReservationStatus> RECOVERABLE_STATUSES = List.of(
            ReservationStatus.HOLD_CREATED,
            ReservationStatus.WAITING_FOR_PHONE,
            ReservationStatus.PHONE_REJECTED
    );

    private final ReservationPersistencePort reservationPersistencePort;
    private final AppointmentReconciliationPort appointmentReconciliationPort;
    private final Clock clock;

    @Autowired
    public ExpiredHoldRecoveryScheduler(
            ReservationPersistencePort reservationPersistencePort,
            AppointmentReconciliationPort appointmentReconciliationPort
    ) {
        this(reservationPersistencePort, appointmentReconciliationPort, Clock.systemUTC());
    }

    ExpiredHoldRecoveryScheduler(
            ReservationPersistencePort reservationPersistencePort,
            AppointmentReconciliationPort appointmentReconciliationPort,
            Clock clock
    ) {
        this.reservationPersistencePort = reservationPersistencePort;
        this.appointmentReconciliationPort = appointmentReconciliationPort;
        this.clock = clock;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        recoverExpiredAndUncertainProcesses();
    }

    @Scheduled(fixedDelayString = "${app.recovery.fixed-delay-ms:60000}")
    public void recoverExpiredAndUncertainProcesses() {
        List<ReservationEntity> candidates = reservationPersistencePort.findAllByStatuses(RECOVERABLE_STATUSES);
        if (candidates.isEmpty()) {
            return;
        }

        Map<String, ExternalAppointmentDTO> remoteByProcessId = fetchRemoteAppointmentsSafely()
                .stream()
                .filter(dto -> dto.reservationProcessId() != null)
                .collect(Collectors.toMap(ExternalAppointmentDTO::reservationProcessId, dto -> dto, (a, b) -> b));

        Instant now = clock.instant();

        for (ReservationEntity reservation : candidates) {
            Optional<ReservationStatus> reconciledStatus = resolveReconciledStatus(
                    reservation,
                    remoteByProcessId.get(reservation.getReservationId()),
                    now
            );
            reconciledStatus.ifPresent(nextStatus -> {
                reservation.setStatus(nextStatus);
                reservationPersistencePort.save(reservation);
            });
        }
    }

    private List<ExternalAppointmentDTO> fetchRemoteAppointmentsSafely() {
        try {
            return appointmentReconciliationPort.fetchAppointments();
        } catch (RuntimeException ex) {
            log.warn("Could not fetch remote appointments for reconciliation: {}", ex.getMessage());
            return List.of();
        }
    }

    private Optional<ReservationStatus> resolveReconciledStatus(
            ReservationEntity reservation,
            ExternalAppointmentDTO remote,
            Instant now
    ) {
        if (remote != null && remote.status() != null) {
            ReservationStatus mapped = mapRemoteStatus(remote.status());
            if (mapped != null && mapped != reservation.getStatus()) {
                return Optional.of(mapped);
            }
        }

        if (reservation.getExpiresAt() != null && !reservation.getExpiresAt().isAfter(now)) {
            return Optional.of(ReservationStatus.EXPIRED);
        }

        return Optional.empty();
    }

    private ReservationStatus mapRemoteStatus(String remoteStatus) {
        return switch (remoteStatus) {
            case "CONFIRMED" -> ReservationStatus.CONFIRMED;
            case "CANCELLED" -> ReservationStatus.CANCELLED;
            case "EXPIRED" -> ReservationStatus.EXPIRED;
            case "INVALID" -> ReservationStatus.INVALID;
            default -> null;
        };
    }
}
