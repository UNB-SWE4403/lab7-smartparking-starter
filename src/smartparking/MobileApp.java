package smartparking;

/**
 * Pushes occupancy updates to the driver-facing mobile application.
 *
 * In production this would send a push notification or update a
 * WebSocket feed. For the lab: prints a push notification.
 *
 * This consumer should always receive every event — even when
 * AlertsService fails. In the current design it often does not.
 */
public class MobileApp {

    public void push(OccupancyEvent event) {
        System.out.println("[MobileApp ] pushed    -> " + event);
    }
}
