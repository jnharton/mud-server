package mud.weather;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class WeatherPattern {
	private List<WeatherState> weather_states;
	
	public WeatherPattern(final WeatherState...sWeatherStates) {
		this.weather_states = new LinkedList<WeatherState>(Arrays.asList(sWeatherStates));
	}
	
	public List<WeatherState> getStates() {
		return Collections.unmodifiableList( weather_states );
	}
	
	public WeatherState getNextState(final WeatherState cs) {
		ListIterator<WeatherState> li = this.weather_states.listIterator(weather_states.indexOf(cs) + 1);

		boolean transitionDown = Math.random() <= cs.getTransDownProb();

        WeatherState newState;
        
		if (transitionDown && li.hasNext()) {
            newState = li.next();
            newState.upDown = -1;
        }
		else if (!transitionDown && li.hasPrevious()) {
            newState = li.previous();
            newState.upDown = 1;
        }
        else {
            newState = cs;
            newState.upDown = 0;
        }

		return newState;
	}
}