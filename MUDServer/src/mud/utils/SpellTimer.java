package mud.utils;

import java.util.TimerTask;

import mud.magic.Spell;

/**
 * A TimerTask that handles duration counts for
 * Spell(s) on a task per spell basis.
 * 
 * @author Jeremy
 *
 */
public class SpellTimer extends TimerTask {
	private Spell spell;
	private int remaining;
	
	public SpellTimer(Spell spell, int duration) {
		this.spell = spell;
		this.remaining = duration;
	}
	
	public Spell getSpell() {
		return this.spell;
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