package mud.misc;

import mud.objects.Room;

/**
 * basically an "Area" class, rename?
 * probably use this to lump rooms into an area
 * 
 * @author Jeremy
 *
 */
public class Zone {
	int id;                             // if this is the first one, it should be -1/0
	String name;
	Room room;                          // "parent room" for the zone?
	private Integer instance_id = null; // instance_id, if this is the original, it should be null

	public Zone(final String name, final Room room) {
		this.id = -1;
		this.name = name;
		this.room = room;
	}

	public Zone(Zone toCopy) {
    }

	public String getName() {
		return this.name;
	}

	public Room getRoom() {
		return this.room;
	}

	public Integer getInstanceId() {
		if(this.instance_id != null) {
			return this.instance_id;
		}
		else {
			return -1;
		}
	}
}
