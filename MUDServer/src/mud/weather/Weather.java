package mud.weather;

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
 * Weather.java
 * 
 * Represents the weather in a room of the game, where weather is one
 * of a set of possible weather states specified by the season.
 * 
 * This is used to implement a weather system of sorts inspired by the ideas here:
 * http://textgaming.blogspot.com/2011/10/weather-ii.html
 * 
 * @author Jeremy Harton
 *
 */
public class Weather
{
	private Season season;
	private WeatherState ws;

	public Weather(final Season initialSeason, final WeatherState initialState) {
		this.season = initialSeason;
		this.ws = initialState;
	}
	
	public Season getSeason() {
		return this.season;
	}
	
	public WeatherState getState() {
		return this.ws;
	}
	
	public void setState(final WeatherState nextState) {
		this.ws = nextState;
	}
	
	public void nextState() {
		final WeatherPattern wp = this.season.getPattern(); 
		setState( wp.getNextState(this.ws) );
	}
}