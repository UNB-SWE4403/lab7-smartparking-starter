package smartparking;

/**
 * Receives occupancy events and writes them to an analytics pipeline.
 *
 * In production this would stream to a data warehouse.
 * For the lab: simulates a slow consumer (150 ms) to make the
 * latency cost of synchronous fan-out observable.
 */
public class Analytics {

    public void record(OccupancyEvent event) {
        simulateSlowWrite();
        System.out.println("[Analytics ] recorded  -> " + event);
    }

    // Simulates a slow network write to a data warehouse.
    // DO NOT remove — this is what makes the latency problem visible.
    private void simulateSlowWrite() {
        try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
