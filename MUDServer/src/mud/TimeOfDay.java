package mud;

public enum TimeOfDay {

    BEFORE_DAWN("Before Dawn", "setting"),
    DAWN("Dawn", "rising"),
    MORNING("Morning", "up"),
    MIDDAY("Midday", "high in the sky"),
    AFTERNOON("Afternoon", "up"),
    DUSK("Dusk", "setting"),
    NIGHT("Night", "moon"),
    MIDNIGHT("Midnight", "high in the sky");

    final public String timeOfDay, bodyLoc;

    private TimeOfDay(final String time, final String loc) {
        timeOfDay = time;
        bodyLoc = loc;
    }

    public String toString() {
        return timeOfDay;
    }
}