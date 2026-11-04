package ar.edu.um.appointment.turnos.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationEntityTest {

    @Test
    void shouldInitializeAuditFieldsOnPrePersist() {
        ReservationEntity entity = ReservationEntity.builder()
                .reservationId("res-test-01")
                .professionalId(5L)
                .appointmentTime(LocalDateTime.now().plusDays(2))
                .status(ReservationStatus.HOLD_CREATED)
                .build();

        entity.onCreate();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldUpdateTimestampOnPreUpdate() {
        ReservationEntity entity = new ReservationEntity();
        entity.onCreate();

        entity.onUpdate();

        assertThat(entity.getUpdatedAt()).isNotNull();
    }
}