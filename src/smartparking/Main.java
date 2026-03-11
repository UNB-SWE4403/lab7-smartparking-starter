package smartparking;

/**
 * Application entry point.
 *
 * Runs three scenarios that make the architectural problems in
 * SensorService directly observable before you start the lab.
 *
 * Run with:
 *   javac -d out src/smartparking/*.java
 *   java  -cp out smartparking.Main
 *
 * Expected output BEFORE your refactoring:
 *   Scenario 1 — normal event takes ~150 ms (Analytics blocks everything)
 *   Scenario 2 — FAULT_ZONE event: AlertsService throws; MobileApp never fires
 *   Scenario 3 — three normal events take ~450 ms total (150 ms × 3, sequential)
 *
 * Expected output AFTER your refactoring:
 *   Each consumer runs independently; a fault in one does not affect others.
 *   The calling thread is not blocked by slow consumers.
 *   Main.java may need minor wiring changes if you introduce constructor injection
 *   — that is expected and acceptable.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {

        SensorService sensor = new SensorService();

        // ── Scenario 1: Normal event ─────────────────────────────────────────
        System.out.println("=== Scenario 1: Normal occupancy change ===");
        long t1 = System.currentTimeMillis();
        sensor.process("A-042", "LEVEL_1", true);
        System.out.printf("Wall time: %d ms%n%n", System.currentTimeMillis() - t1);

        // ── Scenario 2: Faulty consumer ──────────────────────────────────────
        // AlertsService throws for FAULT_ZONE events.
        // Watch whether MobileApp receives the event.
        System.out.println("=== Scenario 2: Event in FAULT_ZONE ===");
        try {
            sensor.process("B-017", "FAULT_ZONE", false);
        } catch (RuntimeException e) {
            System.out.println("[Main] caught exception: " + e.getMessage());
            System.out.println("[Main] -- did MobileApp receive the event? --");
        }
        System.out.println();

        // ── Scenario 3: Burst of events ──────────────────────────────────────
        // Three rapid updates. Observe total wall time.
        System.out.println("=== Scenario 3: Burst of three events ===");
        long t3 = System.currentTimeMillis();
        sensor.process("C-001", "LEVEL_2", true);
        sensor.process("C-002", "LEVEL_2", true);
        sensor.process("C-003", "LEVEL_2", false);
        System.out.printf("Total wall time for 3 events: %d ms%n", System.currentTimeMillis() - t3);
    }
}
