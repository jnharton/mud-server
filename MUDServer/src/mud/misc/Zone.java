package mud.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mud.objects.Room;

/**
 * basically an "Area" class, rename?
 * probably use this to lump rooms into an area
 * 
 * @author Jeremy
 *
 */
public class Zone {
	private int id = -1;
	private String name;
	private Zone parent;
	
	private Room entry_room;
	
	private Integer instance_id = -1; // if this is the first one, it should be -1/0
	
	private List<Room> rooms = new ArrayList<Room>();
	private Map<Integer, Room> roomsById = new TreeMap<Integer, Room>();
	private Map<String, Room> roomsByName = new TreeMap<String, Room>();

	public Zone(final String name, final Zone parent) {
		this.name = name;
		this.parent = parent;
	}

	public Zone(final Zone toCopy) {
    }
	
	public boolean setId(int newId) {
		if( id == -1 ) {
			this.id = newId; return true;
		}
		else {
			return false;
		}
	}
	
	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
	
	public Zone getParent() {
		return this.parent;
	}
	
	public void addRoom(final Room room) {
		if( this.rooms.size() == 0 ) {
			this.entry_room = room;
		}
		
		this.rooms.add(room);
		this.roomsById.put(room.getDBRef(), room);
		this.roomsByName.put(room.getName(), room);
	}
	
	public void removeRoom(final Room room) {
		this.rooms.remove(room);
		this.roomsById.put(room.getDBRef(), room);
		this.roomsByName.remove(room.getName());
	}
	
	public boolean hasRoom(final Room room) {
		return this.rooms.contains(room);
	}
	
	public List<Room> getRooms() {
		return this.rooms;
	}
	
	public void setEntryRoom(final Room entryRoom) {
		this.entry_room = entryRoom;
	}
	
	public Room getEntryRoom() {
		return this.entry_room;
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