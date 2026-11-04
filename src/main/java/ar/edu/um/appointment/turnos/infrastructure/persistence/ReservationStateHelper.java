package ar.edu.um.appointment.turnos.infrastructure.persistence;

import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class ReservationStateHelper {

    private static final Set<ReservationStatus> FINAL_STATES = EnumSet.of(
            ReservationStatus.CONFIRMED,
            ReservationStatus.EXPIRED,
            ReservationStatus.INVALID,
            ReservationStatus.CANCELLED
    );

    public boolean canTransition(ReservationStatus currentState, ReservationStatus targetState) {
        if (currentState == null) {
            return targetState == ReservationStatus.HOLD_CREATED;
        }

        if (FINAL_STATES.contains(currentState)) {
            return false;
        }

        return true;
    }
}