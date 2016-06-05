package mud.weather;

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
 * Season.java
 * 
 * Represents a season for the weather system,
 * which basically supplies possible possible weather
 * states that can be changed to.
 * 
 * @author Jeremy Harton
 *
 */
public class Season {
	private String name;
	public int beginMonth;
	public int endMonth;
	private WeatherPattern weatherPattern;
	
	public Season(final String sName, final int beginMonth, final int endMonth, final WeatherState...sWeatherStates) {
		this.name = sName;
		this.beginMonth = beginMonth;
		this.endMonth = endMonth;
		this.weatherPattern = new WeatherPattern(sWeatherStates);
	}
	
	public String getName() {
		return this.name;
	}
	
	public WeatherPattern getPattern() {
		return this.weatherPattern;
	}

	public String toString() {
		return this.name;
	}
}