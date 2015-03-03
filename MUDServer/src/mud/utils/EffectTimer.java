package mud.utils;

import java.util.TimerTask;

import mud.misc.Effect;

/**
 * A TimerTask that handles Effect durations on
 * a task per Effect basis.
 * 
 * @author Jeremy
 *
 */
public class EffectTimer extends TimerTask {
	private Effect effect;
	private int remaining;
	
	public EffectTimer(Effect effect, int duration) {
		super();
		this.effect = effect;
		this.remaining = duration;
	}
	
	public Effect getEffect() {
		return this.effect;
	}
	
	public int getTimeRemaining() {
		return this.remaining;
	}

	@Override
	public void run() {
		if( this.remaining > 0 ) {
			// do task
			// increment counter
			this.remaining--; // count down a "second"
		}
		else { cancel(); }
	}
}