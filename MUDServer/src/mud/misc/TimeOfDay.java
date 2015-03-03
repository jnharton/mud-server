package mud.misc;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * A holder class for stuff that was originally in
 * TimeLoop as hardcoded strings
 * 
 * @author joshgit
 *
 */
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