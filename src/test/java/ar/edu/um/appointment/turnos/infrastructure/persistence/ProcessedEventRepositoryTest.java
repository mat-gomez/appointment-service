package ar.edu.um.appointment.turnos.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ProcessedEventRepositoryTest {

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void shouldDetectExistingProcessedEvent() {
        String eventId = "evt-kafka-999";
        ProcessedEventEntity eventEntity = ProcessedEventEntity.builder()
                .eventId(eventId)
                .build();

        processedEventRepository.save(eventEntity);

        boolean exists = processedEventRepository.existsByEventId(eventId);
        boolean notExists = processedEventRepository.existsByEventId("evt-kafka-000");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}