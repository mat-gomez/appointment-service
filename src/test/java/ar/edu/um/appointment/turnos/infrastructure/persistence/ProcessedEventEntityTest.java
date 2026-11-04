package ar.edu.um.appointment.turnos.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessedEventEntityTest {

    @Test
    void shouldInitializeProcessedAtOnCreateWhenNull() {
        ProcessedEventEntity entity = ProcessedEventEntity.builder()
                .eventId("evt-123")
                .build();

        entity.onCreate();

        assertThat(entity.getEventId()).isEqualTo("evt-123");
        assertThat(entity.getProcessedAt()).isNotNull();
    }
}