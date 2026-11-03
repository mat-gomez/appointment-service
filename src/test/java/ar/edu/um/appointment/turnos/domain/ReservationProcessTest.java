package ar.edu.um.appointment.turnos.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ReservationProcessTest {

    @Test
    void testReservationProcessCreation() {
        Instant now = Instant.now();
        ReservationProcess process = new ReservationProcess(
                "process-123",
                "hold-456",
                15L,
                "user-42",
                "HELD",
                now
        );

        assertNotNull(process);
        assertEquals("process-123", process.getReservationProcessId());
        assertEquals("hold-456", process.getHoldId());
        assertEquals(15L, process.getProfessionalId());
        assertEquals("user-42", process.getExternalPatientId());
        assertEquals("HELD", process.getStatus());
        assertEquals(now, process.getExpiresAt());
    }
}
