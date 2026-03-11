package smartparking;

/**
 * Represents a parking spot occupancy change reported by a sensor.
 *
 * This is the domain event that flows through the system.
 * It is intentionally simple — do not modify it during the lab.
 */
public class OccupancyEvent {

    private final String spotId;
    private final String zone;
    private final boolean occupied;
    private final long timestampMs;

    public OccupancyEvent(String spotId, String zone, boolean occupied) {
        this.spotId      = spotId;
        this.zone        = zone;
        this.occupied    = occupied;
        this.timestampMs = System.currentTimeMillis();
    }

    public String  getSpotId()      { return spotId; }
    public String  getZone()        { return zone; }
    public boolean isOccupied()     { return occupied; }
    public long    getTimestampMs() { return timestampMs; }

    @Override
    public String toString() {
        return String.format("OccupancyEvent[spot=%s, zone=%s, occupied=%b, ts=%d]",
                spotId, zone, occupied, timestampMs);
    }
}
