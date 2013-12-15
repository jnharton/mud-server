package mud.weather;

import java.util.Arrays;
import java.util.LinkedList;
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
	public LinkedList<WeatherState> weather_states;
	
	public WeatherPattern(WeatherState...sWeatherStates) {
		this.weather_states = new LinkedList<WeatherState>(Arrays.asList(sWeatherStates));
	}
	
	public WeatherState getNextState(final WeatherState cs) {
		ListIterator<WeatherState> li = this.weather_states.listIterator(weather_states.indexOf(cs) + 1);

		boolean transitionDown = Math.random() <= cs.getTransDownProb();

        WeatherState newState;
		if (transitionDown && li.hasNext()) {
            //System.out.println("Transition Down");
            newState = li.next();
            //System.out.println("Is current equal to next? " + cs.equals(newState));
            newState.upDown = -1;
        }
		else if (!transitionDown && li.hasPrevious()) {
            //System.out.println("Transition Up");
            newState = li.previous();
            newState.upDown = 1;
        }
        else {
            //System.out.println("No Transition");
            newState = cs;
            newState.upDown = 0;
        }

		return newState;
	}
}