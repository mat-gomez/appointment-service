package ar.edu.um.appointment.turnos.application.usecases;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitiateReservationServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-11-23T12:00:00Z");
    private static final LocalDate TUESDAY = LocalDate.of(2026, 11, 24);

    @Mock
    private CatalogSchedulePort catalogSchedulePort;

    @Mock
    private ReservationHoldPort reservationHoldPort;

    @Mock
    private ReservationPersistencePort reservationPersistencePort;

    private InitiateReservationService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        service = new InitiateReservationService(
                catalogSchedulePort, reservationHoldPort, reservationPersistencePort, fixedClock
        );
    }

    @Test
    void shouldCreateHoldPersistAndConfirmLeavingWaitingForPhone() {
        ReserveAppointmentRequestDTO request = new ReserveAppointmentRequestDTO(202L, TUESDAY, LocalTime.of(9, 30));
        WeeklyScheduleDTO schedule = new WeeklyScheduleDTO(
                303L, 202L, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), 30, true
        );
        Instant expiresAt = FIXED_NOW.plusSeconds(300);

        when(reservationPersistencePort.hasActivePendingReservation("melina", FIXED_NOW)).thenReturn(false);
        when(catalogSchedulePort.fetchWeeklySchedules(202L, "jwt")).thenReturn(List.of(schedule));
        when(reservationHoldPort.createHold(eq(202L), eq(TUESDAY), eq(LocalTime.of(9, 30)), eq("melina"), anyString()))
                .thenReturn(new HoldResultDTO("hold-123", expiresAt));
        when(reservationHoldPort.confirmHold(eq("hold-123"), eq("melina"), anyString()))
                .thenReturn("process-catedra-123");
        when(reservationPersistencePort.save(any(ReservationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReservationResponseDTO response = service.initiateReservation(request, "melina", "jwt");

        assertThat(response.holdId()).isEqualTo("hold-123");
        assertThat(response.professionalId()).isEqualTo(202L);
        assertThat(response.status()).isEqualTo(ReservationStatus.WAITING_FOR_PHONE.name());
        assertThat(response.expiresAt()).isEqualTo(expiresAt);
        assertThat(response.reservationProcessId()).isEqualTo("process-catedra-123");
        verify(reservationPersistencePort, times(2)).save(any(ReservationEntity.class));
    }

    @Test
    void shouldKeepGeneratedProcessIdWhenConfirmHoldReturnsBlank() {
        ReserveAppointmentRequestDTO request = new ReserveAppointmentRequestDTO(202L, TUESDAY, LocalTime.of(9, 30));
        WeeklyScheduleDTO schedule = new WeeklyScheduleDTO(
                303L, 202L, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), 30, true
        );
        Instant expiresAt = FIXED_NOW.plusSeconds(300);

        when(reservationPersistencePort.hasActivePendingReservation("melina", FIXED_NOW)).thenReturn(false);
        when(catalogSchedulePort.fetchWeeklySchedules(202L, "jwt")).thenReturn(List.of(schedule));
        when(reservationHoldPort.createHold(eq(202L), eq(TUESDAY), eq(LocalTime.of(9, 30)), eq("melina"), anyString()))
                .thenReturn(new HoldResultDTO("hold-123", expiresAt));
        when(reservationHoldPort.confirmHold(eq("hold-123"), eq("melina"), anyString()))
                .thenReturn("  ");
        when(reservationPersistencePort.save(any(ReservationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReservationResponseDTO response = service.initiateReservation(request, "melina", "jwt");

        assertThat(response.reservationProcessId()).isNotBlank().doesNotContain(" ");
        assertThat(response.status()).isEqualTo(ReservationStatus.WAITING_FOR_PHONE.name());
    }

    @Test
    void shouldThrowConflictWhenUserAlreadyHasActivePendingReservation() {
        ReserveAppointmentRequestDTO request = new ReserveAppointmentRequestDTO(202L, TUESDAY, LocalTime.of(9, 30));
        when(reservationPersistencePort.hasActivePendingReservation("melina", FIXED_NOW)).thenReturn(true);

        assertThatThrownBy(() -> service.initiateReservation(request, "melina", "jwt"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        verifyNoInteractions(catalogSchedulePort, reservationHoldPort);
    }

    @Test
    void shouldThrowBadRequestWhenSlotIsOutsideActiveSchedule() {
        ReserveAppointmentRequestDTO request = new ReserveAppointmentRequestDTO(202L, TUESDAY, LocalTime.of(15, 0));
        WeeklyScheduleDTO schedule = new WeeklyScheduleDTO(
                303L, 202L, DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(11, 0), 30, true
        );
        when(reservationPersistencePort.hasActivePendingReservation("melina", FIXED_NOW)).thenReturn(false);
        when(catalogSchedulePort.fetchWeeklySchedules(202L, "jwt")).thenReturn(List.of(schedule));

        assertThatThrownBy(() -> service.initiateReservation(request, "melina", "jwt"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(reservationHoldPort);
    }

    @Test
    void shouldThrowBadRequestWhenRequestHasMissingFields() {
        InitiateReservationService defaultClockService = new InitiateReservationService(
                catalogSchedulePort, reservationHoldPort, reservationPersistencePort
        );
        assertThatThrownBy(() -> defaultClockService.initiateReservation(null, "melina", "jwt"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
