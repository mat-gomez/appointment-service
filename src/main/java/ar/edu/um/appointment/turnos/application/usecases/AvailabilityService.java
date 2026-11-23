package ar.edu.um.appointment.turnos.application.usecases;

import ar.edu.um.appointment.turnos.application.ports.in.GetAvailabilityUseCase;
import ar.edu.um.appointment.turnos.application.ports.out.CatalogSchedulePort;
import ar.edu.um.appointment.turnos.application.ports.out.OccupancyPort;
import ar.edu.um.appointment.turnos.domain.model.AvailabilityResponseDTO;
import ar.edu.um.appointment.turnos.domain.model.AvailabilitySlotDTO;
import ar.edu.um.appointment.turnos.domain.model.OccupancyDTO;
import ar.edu.um.appointment.turnos.domain.model.WeeklyScheduleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvailabilityService implements GetAvailabilityUseCase {

    private final CatalogSchedulePort catalogSchedulePort;
    private final OccupancyPort occupancyPort;
    private final Clock clock;

    @Autowired
    public AvailabilityService(CatalogSchedulePort catalogSchedulePort, OccupancyPort occupancyPort) {
        this(catalogSchedulePort, occupancyPort, Clock.systemUTC());
    }

    AvailabilityService(CatalogSchedulePort catalogSchedulePort, OccupancyPort occupancyPort, Clock clock) {
        this.catalogSchedulePort = catalogSchedulePort;
        this.occupancyPort = occupancyPort;
        this.clock = clock;
    }

    @Override
    public AvailabilityResponseDTO getAvailability(Long professionalId, LocalDate date, String userJwt) {
        List<WeeklyScheduleDTO> matchingSchedules = catalogSchedulePort
                .fetchWeeklySchedules(professionalId, userJwt)
                .stream()
                .filter(s -> s.enabled() && s.dayOfWeek() == date.getDayOfWeek())
                .toList();

        if (matchingSchedules.isEmpty()) {
            return new AvailabilityResponseDTO(professionalId, date, List.of());
        }

        Instant now = clock.instant();
        Set<LocalTime> occupiedStartTimes = occupancyPort
                .fetchOccupancies(professionalId, date, date)
                .stream()
                .filter(o -> isBlocking(o, now))
                .map(OccupancyDTO::startTime)
                .collect(Collectors.toSet());

        List<AvailabilitySlotDTO> availableSlots = new ArrayList<>();
        for (WeeklyScheduleDTO schedule : matchingSchedules) {
            generateSlots(schedule, occupiedStartTimes, availableSlots);
        }

        availableSlots.sort(Comparator.comparing(AvailabilitySlotDTO::startTime));
        return new AvailabilityResponseDTO(professionalId, date, availableSlots);
    }

    private boolean isBlocking(OccupancyDTO occupancy, Instant now) {
        if ("CONFIRMED".equalsIgnoreCase(occupancy.status())) {
            return true;
        }
        return "HELD".equalsIgnoreCase(occupancy.status())
                && (occupancy.expiresAt() == null || occupancy.expiresAt().isAfter(now));
    }

    private void generateSlots(
            WeeklyScheduleDTO schedule,
            Set<LocalTime> occupiedStartTimes,
            List<AvailabilitySlotDTO> target
    ) {
        int duration = schedule.slotDurationMinutes();
        if (duration <= 0) {
            return;
        }
        LocalTime cursor = schedule.startTime();
        while (!cursor.plusMinutes(duration).isAfter(schedule.endTime())) {
            LocalTime next = cursor.plusMinutes(duration);
            if (!occupiedStartTimes.contains(cursor)) {
                target.add(new AvailabilitySlotDTO(cursor, next));
            }
            cursor = next;
        }
    }
}
