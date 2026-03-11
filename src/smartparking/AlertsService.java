package smartparking;

/**
 * Evaluates occupancy events and fires operational alerts.
 *
 * In production this would page an operations team.
 * For the lab: throws a RuntimeException when the event is in
 * the "FAULT_ZONE" to make the reliability problem observable.
 */
public class AlertsService {

    private static final String FAULT_ZONE = "FAULT_ZONE";

    public void evaluate(OccupancyEvent event) {
        if (FAULT_ZONE.equals(event.getZone())) {
            // Simulates a transient downstream failure.
            // In the current design this exception propagates to SensorService
            // and prevents MobileApp from ever receiving the event.
            throw new RuntimeException(
                "[AlertsService] downstream failure for zone: " + event.getZone());
        }
        System.out.println("[AlertsService] evaluated -> " + event);
    }
}
