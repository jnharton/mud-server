package mud.foe.misc;

import java.util.Hashtable;
import java.util.List;

/**
 * An object representing a single segment of memory.
 * 
 * :
 * characters - a hashtable of character names and actor objects
 * lines - the things an Actor can say
 * 
 * @author Jeremy
 *
 */
public final class MemorySegment {
	private Hashtable<String, Actor> characters;
	private Hashtable<Actor, List<String>> lines;
	private Hashtable<Integer, String> narrative;
	
	public MemorySegment() {
		this.characters = new Hashtable<String, Actor>();
		this.lines = new Hashtable<Actor, List<String>>();
		this.narrative = new Hashtable<Integer, String>();
	}
	
	public void addActor(final Actor actor) {
		this.characters.put( actor.getName(), actor );
	}
	
	public Actor getActor(final String actorName) {
		return this.characters.get( actorName );
	}
}