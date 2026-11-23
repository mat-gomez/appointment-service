package ar.edu.um.appointment.turnos.application.usecases;

import ar.edu.um.appointment.turnos.application.ports.out.CatalogSchedulePort;
import ar.edu.um.appointment.turnos.application.ports.out.OccupancyPort;
import ar.edu.um.appointment.turnos.domain.model.AvailabilityResponseDTO;
import ar.edu.um.appointment.turnos.domain.model.AvailabilitySlotDTO;
import ar.edu.um.appointment.turnos.domain.model.OccupancyDTO;
import ar.edu.um.appointment.turnos.domain.model.WeeklyScheduleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-11-23T12:00:00Z");
    private static final LocalDate TUESDAY = LocalDate.of(2026, 11, 24);

    @Mock
    private CatalogSchedulePort catalogSchedulePort;

    @Mock
    private OccupancyPort occupancyPort;

    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        availabilityService = new AvailabilityService(catalogSchedulePort, occupancyPort, fixedClock);
    }

    @Test
    void shouldReturnAvailableSlotsExcludingConfirmedAndActiveHolds() {
        WeeklyScheduleDTO schedule = new WeeklyScheduleDTO(
                303L, 202L, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 0), 30, true
        );
        when(catalogSchedulePort.fetchWeeklySchedules(202L, "jwt")).thenReturn(List.of(schedule));
        when(occupancyPort.fetchOccupancies(202L, TUESDAY, TUESDAY)).thenReturn(List.of(
                new OccupancyDTO(TUESDAY, LocalTime.of(9, 0), "CONFIRMED", null),
                new OccupancyDTO(TUESDAY, LocalTime.of(9, 30), "HELD", FIXED_NOW.plusSeconds(300)),
                new OccupancyDTO(TUESDAY, LocalTime.of(10, 0), "HELD", FIXED_NOW.minusSeconds(60))
        ));

        AvailabilityResponseDTO result = availabilityService.getAvailability(202L, TUESDAY, "jwt");

        assertThat(result.professionalId()).isEqualTo(202L);
        assertThat(result.date()).isEqualTo(TUESDAY);
        assertThat(result.availableSlots()).containsExactly(
                new AvailabilitySlotDTO(LocalTime.of(10, 0), LocalTime.of(10, 30)),
                new AvailabilitySlotDTO(LocalTime.of(10, 30), LocalTime.of(11, 0))
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoEnabledScheduleForDay() {
        WeeklyScheduleDTO otherDay = new WeeklyScheduleDTO(
                304L, 202L, DayOfWeek.THURSDAY,
                LocalTime.of(18, 0), LocalTime.of(21, 0), 30, true
        );
        WeeklyScheduleDTO disabled = new WeeklyScheduleDTO(
                305L, 202L, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 30, false
        );
        when(catalogSchedulePort.fetchWeeklySchedules(202L, "jwt")).thenReturn(List.of(otherDay, disabled));

        AvailabilityResponseDTO result = availabilityService.getAvailability(202L, TUESDAY, "jwt");

        assertThat(result.availableSlots()).isEmpty();
        verifyNoInteractions(occupancyPort);
    }

    @Test
    void shouldIgnoreScheduleWithNonPositiveDurationAndBlockHoldWithoutExpiry() {
        AvailabilityService defaultClockService = new AvailabilityService(catalogSchedulePort, occupancyPort);
        WeeklyScheduleDTO invalidDuration = new WeeklyScheduleDTO(
                306L, 202L, DayOfWeek.TUESDAY,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 0, true
        );
        WeeklyScheduleDTO valid = new WeeklyScheduleDTO(
                307L, 202L, DayOfWeek.TUESDAY,
                LocalTime.of(10, 0), LocalTime.of(10, 30), 30, true
        );
        when(catalogSchedulePort.fetchWeeklySchedules(202L, "jwt")).thenReturn(List.of(invalidDuration, valid));
        when(occupancyPort.fetchOccupancies(202L, TUESDAY, TUESDAY)).thenReturn(List.of(
                new OccupancyDTO(TUESDAY, LocalTime.of(10, 0), "HELD", null)
        ));

        AvailabilityResponseDTO result = defaultClockService.getAvailability(202L, TUESDAY, "jwt");

        assertThat(result.availableSlots()).isEmpty();
    }
}
