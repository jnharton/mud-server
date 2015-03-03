package mud.weather;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import mud.misc.Seasons;

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
	public String name;
	public int beginMonth;
	public int endMonth;
	public WeatherPattern weatherPattern;
	
	public Season(String name, int beginMonth, int endMonth) {
		this.name = name;
		this.beginMonth = beginMonth;
		this.endMonth = endMonth;
		this.weatherPattern = null;
	}
	
	private Season(String name, int beginMonth, int endMonth, WeatherState...sWeatherStates) {
		this.name = name;
		this.beginMonth = beginMonth;
		this.endMonth = endMonth;
		this.weatherPattern = new WeatherPattern(sWeatherStates);
	}
	
	public Season(String sName, WeatherState...sWeatherStates) {
		this.name = sName;
		this.beginMonth = -1;
		this.endMonth = -1;
		this.weatherPattern = new WeatherPattern(sWeatherStates);
	}
	
	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name;
	}
}