package mud.weather;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

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
	public LinkedList<WeatherState> weather_states;
	
	public Season(String sName, WeatherState...sWeatherStates) {
		this.name = sName;
		this.weather_states = new LinkedList<WeatherState>(Arrays.asList(sWeatherStates));
	}
	
	public WeatherState getNextState(final WeatherState cs) {
		ListIterator<WeatherState> li = this.weather_states.listIterator(weather_states.indexOf(cs) + 1);

		boolean transitionDown = Math.random() <= cs.getTransDownProb();

        WeatherState newState;
		if (transitionDown && li.hasNext()) {
            System.out.println("Transition Down");
            newState = li.next();
            System.out.println("Is current equal to next? " + cs.equals(newState));
            newState.upDown = -1;
        }
		else if (!transitionDown && li.hasPrevious()) {
            System.out.println("Transition Up");
            newState = li.previous();
            newState.upDown = 1;
        }
        else {
            System.out.println("No Transition");
            newState = cs;
            newState.upDown = 0;
        }

		return newState;
	}
}