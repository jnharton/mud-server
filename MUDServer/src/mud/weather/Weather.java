package mud.weather;

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
	public Season season;
	public WeatherState ws;

	public Weather(final Season initialSeason, final WeatherState initialState) {
		this.season = initialSeason;
		this.ws = initialState;
	}

	public void setState(WeatherState nextState) {
		this.ws = nextState;
	}
	
	public void nextState() {
		setState(this.season.getNextState( this.ws ));
	}
}