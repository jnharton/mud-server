package mud.weather;

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
		this.weather_states = new LinkedList<WeatherState>();
		for (WeatherState ws : sWeatherStates) {
			//System.out.println("Added " + ws);
			this.weather_states.add(ws);
		}
	}
	
	public WeatherState getNextState(WeatherState cs) {
		ListIterator<WeatherState> li = this.weather_states.listIterator(weather_states.indexOf(cs) + 1);
		
		WeatherState newState;
		
		// probability calculation
		double transitionDownP = cs.getTransDownProb() * 10; // 0.1 * 10 = 1.0
		
		System.out.println("Probability of transitioning down: " + cs.getTransDownProb());
		
		boolean transitionDown = false;

		int roll = ((int)(Math.random()));
		
		System.out.println(roll);

		if (roll <= transitionDownP) {
			transitionDown = true;
		}
		else if (roll > transitionDownP) {
			transitionDown = false;
		}

		// return transition result
		if ( transitionDown ) { // transition down
			System.out.println("Transition Down?");
			if ( li.hasNext() ) {
				System.out.println("Transition Down");
				newState = li.next();
				System.out.println("Is current equal to next? " + cs.equals(newState));
				newState.upDown = -1;
			}
			else {
				System.out.println("No Transition");
				newState = cs;
				newState.upDown = 0;
			}
		}
		else { // transition up
			System.out.println("Transition Up?");
			if ( li.hasPrevious() ) {
				System.out.println("Transition Up");
				newState = li.previous();
				newState.upDown = 1;
			}
			else {
				System.out.println("No Transition");
				newState = cs;
				newState.upDown = 0;
			}
		}

		return newState;
	}
}