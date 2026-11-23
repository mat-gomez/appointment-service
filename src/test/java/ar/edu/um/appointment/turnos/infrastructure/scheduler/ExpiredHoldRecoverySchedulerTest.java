package ar.edu.um.appointment.turnos.infrastructure.scheduler;

import ar.edu.um.appointment.turnos.application.ports.out.AppointmentReconciliationPort;
import ar.edu.um.appointment.turnos.application.ports.out.ReservationPersistencePort;
import ar.edu.um.appointment.turnos.domain.model.ExternalAppointmentDTO;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationEntity;
import ar.edu.um.appointment.turnos.infrastructure.persistence.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredHoldRecoverySchedulerTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-11-23T21:00:00Z");

    @Mock
    private ReservationPersistencePort reservationPersistencePort;

    @Mock
    private AppointmentReconciliationPort appointmentReconciliationPort;

    private ExpiredHoldRecoveryScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ExpiredHoldRecoveryScheduler(
                reservationPersistencePort,
                appointmentReconciliationPort,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldMarkExpiredProcessesWhenExpiresAtPassed() {
        ReservationEntity expired = ReservationEntity.builder()
                .reservationId("proc-exp-1")
                .status(ReservationStatus.WAITING_FOR_PHONE)
                .expiresAt(FIXED_NOW.minusSeconds(1))
                .build();
        when(reservationPersistencePort.findAllByStatuses(any())).thenReturn(List.of(expired));
        when(appointmentReconciliationPort.fetchAppointments()).thenReturn(List.of());

        scheduler.recoverExpiredAndUncertainProcesses();

        assertThat(expired.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        verify(reservationPersistencePort).save(expired);
    }

    @Test
    void shouldReconcileConfirmedStatusFromCatedraAppointments() {
        ReservationEntity uncertain = ReservationEntity.builder()
                .reservationId("proc-rec-1")
                .status(ReservationStatus.WAITING_FOR_PHONE)
                .expiresAt(FIXED_NOW.plusSeconds(300))
                .build();
        ExternalAppointmentDTO remote = new ExternalAppointmentDTO(
                "ext-1", "proc-rec-1", "hold-1", "CONFIRMED"
        );
        when(reservationPersistencePort.findAllByStatuses(any())).thenReturn(List.of(uncertain));
        when(appointmentReconciliationPort.fetchAppointments()).thenReturn(List.of(remote));

        scheduler.recoverExpiredAndUncertainProcesses();

        assertThat(uncertain.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(reservationPersistencePort).save(uncertain);
    }

    @Test
    void shouldRunOnApplicationReadyAndHandleRemoteFailureGracefully() {
        ReservationEntity active = ReservationEntity.builder()
                .reservationId("proc-act-1")
                .status(ReservationStatus.HOLD_CREATED)
                .expiresAt(FIXED_NOW.plusSeconds(300))
                .build();
        when(reservationPersistencePort.findAllByStatuses(any())).thenReturn(List.of(active));
        when(appointmentReconciliationPort.fetchAppointments()).thenThrow(new RuntimeException("boom"));

        scheduler.onApplicationReady();

        assertThat(active.getStatus()).isEqualTo(ReservationStatus.HOLD_CREATED);
        verify(reservationPersistencePort, never()).save(any());
    }
}
