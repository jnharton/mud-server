package mud.foe.misc;

/*import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mud.Command;
import mud.interfaces.ExtraCommands;*/

/**
 * An object that delineates data about memories for Memory Orbs,
 * which are an Item that exists in Fallout Equestria.
 * 
 * A memory consists of a fixed number of memory segments. These segments
 * contain visual, aural, and other sensory descriptions in addition 
 * 
 * @author Jeremy
 *
 */
public class Memory {
	private MemorySegment segments[];
	
	public Memory() {
		this.segments = new MemorySegment[1];
	}
	
	public Memory(int segments) {
		this.segments = new MemorySegment[segments];
	}
	
	public Memory(MemorySegment...segments){
		this.segments = segments;
	}
	
	public void setSegment(int index, MemorySegment segment) {
		if( this.segments != null ) {
			this.segments[index] = segment;
		}
	}
	
	public MemorySegment getSegment(int id) {
		if( id < this.segments.length ) {
			return this.segments[id];
		}
		
		return null;
	}
	
	public int numSegments() {
		return this.segments.length;
	}
}