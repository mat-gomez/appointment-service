package ar.edu.um.appointment.turnos.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationStateHelperTest {

    private ReservationStateHelper stateHelper;

    @BeforeEach
    void setUp() {
        stateHelper = new ReservationStateHelper();
    }

    @Test
    void shouldPreventTransitionFromFinalStates() {
        assertThat(stateHelper.canTransition(ReservationStatus.CONFIRMED, ReservationStatus.CANCELLED)).isFalse();
        assertThat(stateHelper.canTransition(ReservationStatus.EXPIRED, ReservationStatus.HOLD_CREATED)).isFalse();
        assertThat(stateHelper.canTransition(ReservationStatus.INVALID, ReservationStatus.WAITING_FOR_PHONE)).isFalse();
        assertThat(stateHelper.canTransition(ReservationStatus.CANCELLED, ReservationStatus.CONFIRMED)).isFalse();
    }

    @Test
    void shouldAllowTransitionFromNonFinalStates() {
        assertThat(stateHelper.canTransition(ReservationStatus.HOLD_CREATED, ReservationStatus.WAITING_FOR_PHONE)).isTrue();
        assertThat(stateHelper.canTransition(ReservationStatus.WAITING_FOR_PHONE, ReservationStatus.CONFIRMED)).isTrue();
    }

    @Test
    void shouldHandleNullCurrentState() {
        assertThat(stateHelper.canTransition(null, ReservationStatus.HOLD_CREATED)).isTrue();
        assertThat(stateHelper.canTransition(null, ReservationStatus.CONFIRMED)).isFalse();
    }
}