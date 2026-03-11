package smartparking;

/**
 * Receives raw sensor payloads and distributes occupancy events
 * to all downstream consumers.
 *
 * ─────────────────────────────────────────────────────────────
 *  THIS CLASS CONTAINS THE ARCHITECTURAL PROBLEM FOR LAB 7.
 * ─────────────────────────────────────────────────────────────
 *
 * Current design problems (your Part 1 diagnosis should name these):
 *
 *   1. LATENCY — consumers are notified one after another on the
 *      same calling thread. A slow Analytics write blocks the
 *      entire notification chain. SensorService cannot ingest the
 *      next event until all consumers finish.
 *
 *   2. RELIABILITY — if AlertsService throws an exception, the
 *      forEach loop stops. MobileApp never receives the event.
 *      One faulty consumer silences all downstream consumers.
 *
 *   3. TIGHT COUPLING — SensorService constructs its own consumers
 *      (new Analytics(), new AlertsService(), new MobileApp()).
 *      Adding a new consumer requires modifying this class.
 *      There is no way to test SensorService in isolation.
 *
 * Your task in Part 2: apply a pattern composition from the
 * Lecture A catalogue to remove at least one of these problems.
 * The most direct fix is the Observer + interface approach:
 * extract IOccupancyConsumer, make each downstream class implement
 * it, and inject the consumers from outside rather than constructing
 * them here.
 *
 * Do NOT change the public API: process(String spotId, String zone,
 * boolean occupied) must remain callable from Main.java.
 */
public class SensorService {

    // ── ARCHITECTURE SMELL: direct construction of concrete consumers ────────
    // SensorService owns and creates its consumers. This prevents substitution,
    // isolation testing, and runtime reconfiguration.
    private final Analytics    analytics    = new Analytics();
    private final AlertsService alerts      = new AlertsService();
    private final MobileApp    mobileApp    = new MobileApp();
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Accepts a raw sensor reading and notifies all consumers.
     *
     * Called by sensor adapters after payload normalisation.
     * The method name and signature must not change.
     */
    public void process(String spotId, String zone, boolean occupied) {
        OccupancyEvent event = new OccupancyEvent(spotId, zone, occupied);
        System.out.println("\n[SensorService] processing -> " + event);

        // ── ARCHITECTURE SMELL: synchronous sequential fan-out ───────────────
        // Each notify() call blocks until the consumer finishes.
        // An exception in any consumer aborts the remaining ones.
        notifyAll(event);
        // ────────────────────────────────────────────────────────────────────
    }

    private void notifyAll(OccupancyEvent event) {
        long start = System.currentTimeMillis();

        analytics.record(event);      // ← may be slow (150 ms)
        alerts.evaluate(event);       // ← may throw (FAULT_ZONE)
        mobileApp.push(event);        // ← blocked until line above returns

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("[SensorService] fan-out complete in %d ms%n", elapsed);
    }
}
