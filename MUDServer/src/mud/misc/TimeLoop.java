package mud.misc;

import java.util.Hashtable;
import java.util.Map;

import mud.MUDServer;
import mud.utils.Date;
import mud.utils.Message;
import mud.utils.Time;
import mud.utils.Message.MessageType;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * A runnable time loop to supply game time and changes in date according to it.
 * 
 * @author Jeremy
 *
 */
public class TimeLoop implements Runnable
{
	private boolean running = true;
	private boolean paused = false;

	private int day;
	private int month;
	private int year;

	private MoonPhase moonPhase = MoonPhase.FULL_MOON;

	private int second;
	private int minute;
	private int hour;

	private TimeOfDay timeOfDay = TimeOfDay.MIDNIGHT;

	private boolean isDay = false;
	private String celestialBody = "moon";

	private int ms_per_second = 167;

	private int weather_update_interval = 3; // how many minutes between weather broadcasts

	final private int[] DAYS;              // array containing the number of days in each month
	
	final private Map<TimeOfDay, Message> messages = new Hashtable<TimeOfDay, Message>();
	
	final private MUDServer server;

	/**
	 * This uses a 6:1 timescale. That is, 6 minutes of game time is equal to 1 minute
	 * of real time. To adjust that simply change the ms_per_second variable to the desired
	 * number of milliseconds/second to adjust how much real time (in seconds) is equal to
	 * 1 minute of game time.
	 * 
	 * Modifications: x:1 timescale
	 */

	public TimeLoop(final MUDServer server, final int[] DAYS, final Date startDate, final Time startTime) {
		this.server = server;

		this.DAYS = DAYS;

		this.day = startDate.getDay();     // the initial day (start day)
		this.month = startDate.getMonth(); // the initial month (start month)
		this.year = startDate.getYear();   // the initial year (start year)

		this.hour = startTime.hour;        // the initial hour (start time)
		this.minute = startTime.minute;    // the initial minute (start time)
		
		setMessage(TimeOfDay.MIDNIGHT,    "It is now midnight.");
		setMessage(TimeOfDay.BEFORE_DAWN, "It is now just before dawn.");
		setMessage(TimeOfDay.DAWN,        "It is now dawn.");
		setMessage(TimeOfDay.MORNING,     "It is now morning.");
		setMessage(TimeOfDay.MIDDAY,      "It is now midday.");
		setMessage(TimeOfDay.AFTERNOON,   "It is now afternoon.");
		setMessage(TimeOfDay.DUSK,        "It is now dusk.");
		setMessage(TimeOfDay.NIGHT,       "It is now night.");
	}

	// message sending with specifics needs a loginCheck(client), but it needs to not cause the game to crash
	@Override
	public void run() {
		while (running) {
			try {
				Thread.sleep(ms_per_second);
			}
			catch(InterruptedException ie) {
				ie.printStackTrace();
			}

			if (!paused) doLoop();
			
			//System.out.println( Utils.padLeft("" + hour, '0', 2) + ":" + Utils.padLeft("" + minute, '0', 2)+ ":" + Utils.padLeft("" + second, '0', 2));
		}
	}

	private void doLoop() {
		incrementSecond();
		//incrementMinute();
	}

	private void incrementSecond() {
		second += 1;

		server.onSecondIncrement();

		if(second > 59) {
			second = 0;
			incrementMinute();
		}
	}

	private void incrementMinute() {
		minute += 1;

		server.onMinuteIncrement();

		if (minute > 59) {
			minute = 0;
			incrementHour();
		}
	}

	private void incrementHour() {
		hour += 1;

		server.onHourIncrement();

		if (hour > 23) {
			hour = 0;
			incrementDay();
		}
		
		if (this.hour == 0)       setTimeOfDay(TimeOfDay.MIDNIGHT);
		else if (this.hour == 5)  setTimeOfDay(TimeOfDay.BEFORE_DAWN);
		else if (this.hour == 6)  setTimeOfDay(TimeOfDay.DAWN);
		else if (this.hour == 7)  setTimeOfDay(TimeOfDay.MORNING);
		else if (this.hour == 12) setTimeOfDay(TimeOfDay.MIDDAY);
		else if (this.hour == 13) setTimeOfDay(TimeOfDay.AFTERNOON);
		else if (this.hour == 18) setTimeOfDay(TimeOfDay.DUSK);
		else if (this.hour == 19) setTimeOfDay(TimeOfDay.NIGHT);

		/*if (minute != 0) {
            return;
        }*/
	}

	private void incrementDay() {
		day += 1;

		server.onDayIncrement();

		if (day >= DAYS[month]) {
			day = 0;
			incrementMonth();
		}
	}

	private void incrementMonth() {
		month += 1;

		server.onMonthIncrement();

		if (month >= DAYS.length) {
			month = 0;
			incrementYear();
		}
	}

	private void incrementYear() {
		year += 1;

		server.onYearIncrement();
	}
	
	public void setMessage(final TimeOfDay tod, final String msg) {
		this.messages.put(tod, new Message(msg, MessageType.BROADCAST));
	}

	public TimeOfDay getTimeOfDay() {
		return timeOfDay;
	}

	protected void setTimeOfDay(final TimeOfDay tod) {
		final Message msg = this.messages.get(tod);
		
		server.addMessage( msg.getCopy() );
		server.debug( msg.getMessage() );

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

	public void setDate(final Date newDate) {
		pauseLoop();
		this.day = newDate.getDay();
		this.month = newDate.getMonth();
		this.year = newDate.getYear();

		this.hour = 6;    // 6am?
		this.minute  = 0;
		this.second = 0;
		unpauseLoop();
	}

	public int getDay() {
		return this.day;
	}

	public int getMonth() {
		return this.month;
	}

	public int getYear() {
		return this.year;
	}

	public void setHours(int hour) {
		pauseLoop();
		this.hour = hour;
		this.minute = 0;
		unpauseLoop();
	}

	public int getHours() {
		return this.hour;
	}

	public void setMinutes(int minute) {
		pauseLoop();
		this.minute = minute;
		unpauseLoop();
	}

	public int getMinutes() {
		return this.minute;
	}

	public void setSeconds(int second) {
		pauseLoop();
		this.second = second;
		unpauseLoop();
	}

	public int getSeconds() {
		return this.second;
	}

	public void setTime(int hours, int minutes, int seconds) {
		pauseLoop();
		this.hour = hours;
		this.minute = minutes;
		this.second = seconds;
		unpauseLoop();
	}

	public void setTime(final Time newTime) {
		pauseLoop();
		this.hour = newTime.hour;
		this.minute = newTime.minute;
		this.second = newTime.second;
		unpauseLoop();
	}

	/**
	 * Set the scale at which time moves in the game world relative
	 * to the real world.
	 * 
	 * E.g. In real time, there are 1000ms to 1s. To speed the game
	 * up you'd reduce that number. If you made it 500ms to 1s then the
	 * game time would run at 2x compared to real time.
	 * 
	 * @param ms the number of milliseconds per second (ms)
	 */
	public void setScale(int ms) {
		this.ms_per_second = ms;
		//this.ms_per_minute = ms;
	}

	/**
	 * Get the scale, number of milliseconds (ms) per second, at which
	 * time currently moves in the game world relative to the real world.
	 * 
	 * @return the number of milliseconds per second (ms)
	 */
	public int getScale() {
		return ms_per_second;
		//return ms_per_minute;
	}

	public String getCelestialBody() {
		return celestialBody;
	}

	public boolean isDaytime() {
		return isDay;
	}

	public void setMoonPhase(MoonPhase next_phase) {
		this.moonPhase = next_phase;
	}

	public MoonPhase getMoonPhase() {
		return moonPhase;
	}

	public int getWeatherUpdateInterval() {
		return this.weather_update_interval;
	}
}