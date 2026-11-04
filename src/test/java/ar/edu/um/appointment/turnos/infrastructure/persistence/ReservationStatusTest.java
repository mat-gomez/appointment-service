package ar.edu.um.appointment.turnos.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationStatusTest {

    @Test
    void shouldContainAllExpectedStatuses() {
        ReservationStatus[] statuses = ReservationStatus.values();

        assertThat(statuses).contains(
                ReservationStatus.HOLD_CREATED,
                ReservationStatus.WAITING_FOR_PHONE,
                ReservationStatus.PHONE_REJECTED,
                ReservationStatus.CONFIRMED,
                ReservationStatus.EXPIRED,
                ReservationStatus.INVALID,
                ReservationStatus.CANCELLED
        );
    }
}