package mud;

import mud.utils.Message;

/**
 * A runnable time loop to supply game time and changes in date according to it.
 * 
 * @author Jeremy
 *
 */
public class TimeLoop implements Runnable
{
    private MoonPhase moonPhase = MoonPhase.FULL_MOON;

    private int minutes;
    private int hours;
    private int days;
    private int months;
    private int years;

    private TimeOfDay timeOfDay = TimeOfDay.MIDNIGHT;

    private boolean isDay = false;
    private String celestialBody = "moon";

    private boolean paused = false;
    private boolean running = true;

    private int ms_per_minute = 10 * 1000; // 10k ms (10 s) 
    
    final private MUDServer server;
    final private int[] DAYS;

    /**
     * This uses a 6:1 timescale. That is, 6 minutes of game time is equal to 1 minute
     * of real time. To adjust that simply change the ms_per_minute variable to the of seconds
     * to adjust how much real time (in seconds) is equal to 1 minute of game time.
     * 
     * Modifications: x:1 timescale
     */

    public TimeLoop(final MUDServer server, final int[] DAYS, final int months, final int days, final int hours, final int mins) {
        this.DAYS = DAYS;
        this.server = server;
        this.minutes = mins;  // the initial number of minutes (start time)
        this.hours = hours;   // the initial number of hours (start time)
        this.days = days;     // the initial number of days (start day)
        this.months = months; // the initial number of months (start month)
    }

    // message sending with specifics needs a loginCheck(client), but it needs to not cause the game to crash
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(ms_per_minute);
            }
            catch(InterruptedException ie) {
                ie.printStackTrace();
            }
            if (!paused)    doLoop();
        }
    }
    
    private void doLoop() {
        incrementMinute();
    }
    
    private void incrementMinute() {
        minutes += 1;
        if (minutes > 59) {
            minutes = 0;
            incrementHour();
        }
        server.debug("" + hours + ":" + minutes);
        server.handleMovement();
    }

    private void incrementHour() {
        hours += 1;
        if (hours > 23) {
            hours = 0;
            incrementDay();
        }
        server.onHourIncrement();

        if (minutes != 0) {
            return;
        }

        if (this.hours == 5) {
            setTimeOfDay(TimeOfDay.BEFORE_DAWN, new Message("It is now just before dawn."));
            server.debug("It is now just before dawn.");
        }
        else if (this.hours == 6) {
            setTimeOfDay(TimeOfDay.DAWN, new Message("It is now dawn."));
            server.debug("It is now dawn.");
        }
        else if (this.hours == 7) {
            setTimeOfDay(TimeOfDay.MORNING, new Message("It is now morning."));
            server.debug("It is now morning.");
        }
        else if (this.hours == 12) {
            setTimeOfDay(TimeOfDay.MIDDAY, new Message("It is now midday."));
            server.debug("It is now midday.");
        }
        else if (this.hours == 13) {
            setTimeOfDay(TimeOfDay.AFTERNOON, new Message("It is now afternoon."));
            server.debug("It is now afternoon.");
        }
        else if (this.hours == 18) {
            setTimeOfDay(TimeOfDay.DUSK, new Message("It is now dusk."));
            server.debug("It is now dusk.");
        }
        else if (this.hours == 19) {
            setTimeOfDay(TimeOfDay.NIGHT, new Message("It is now night."));
            server.debug("It is now night.");
        }
        else if (this.hours == 0) {
            setTimeOfDay(TimeOfDay.MIDNIGHT, new Message("It is now midnight."));
            server.debug("It is now midnight.");
        }
    }

    private void incrementDay() {
        days += 1;
        if (days >= DAYS[months]) {
            days = 0;
            incrementMonth();
        }
    }

    private void incrementMonth() {
        months += 1;
        if (months >= DAYS.length) {
            months = 0;
            years += 1;
        }
    }

    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    private void setTimeOfDay(final TimeOfDay tod, final Message msg) {
        server.addMessage(msg);
        timeOfDay = tod;
        if (TimeOfDay.DAWN.equals(tod)) {
            celestialBody = "sun";
            isDay = true;
        }
        else if (TimeOfDay.NIGHT.equals(tod)) {
            celestialBody = "moon";
            isDay = false;
        }
    }

    public void pauseLoop() {
        paused = true;
    }

    public void unpauseLoop() {
        paused = false;
    }

    public void killLoop() {
        running = false;
    }

    public void setHours(int hour) {
        this.hours = hour;
        this.minutes = 0;
    }

    public int getHours() {
        return this.hours;
    }

    public void setMinutes(int minute) {
        this.minutes = minute;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public void setScale(int ms) {
        this.ms_per_minute = ms;
    }

    public int getScale() {
        return ms_per_minute;
    }

    public String getCelestialBody() {
        return celestialBody;
    }

    public boolean isDaytime() {
        return isDay;
    }
    
    public MoonPhase getMoonPhase() {
        return moonPhase;
    }

}
