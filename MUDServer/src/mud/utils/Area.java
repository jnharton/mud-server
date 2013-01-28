package mud.utils;

import java.util.ArrayList;

import mud.objects.Room;

public class Area {
	private Integer id;        // area id
	private String name;       // area name
	
	public int entrance_room; // the room you arrive in if you 'goto' the area
	
	//
	private ArrayList<Integer> room_ids; // ids for the rooms in this are
	
	/**
	 * create a completely empty new area
	 */
	public Area() {
		this.room_ids = new ArrayList<Integer>();
	}
	
	/**
	 * create a completely empty new area,
	 * of a known size
	 */
	public Area(int size) {
		this.room_ids = new ArrayList<Integer>(size);
	}
	
	public Area(Room room) {
		this.room_ids = new ArrayList<Integer>(1);
		this.room_ids.add(room.getDBRef());
		this.entrance_room = room.getDBRef();
	}
	
	public Area(Room...rooms) {
		this.room_ids = new ArrayList<Integer>(rooms.length); // initialize room id holder
		
		for (Room room : rooms) {
			this.room_ids.add(room.getDBRef());   // add the room ids to the list
		}
		
		this.entrance_room = rooms[0].getDBRef(); // use the first room as the entrace
	}
	
	
	/**
	 * Does this area contained the specified room
	 * 
	 * Time Complexity: O(n)
	 * 
	 * @param id
	 * @return
	 */
	public boolean hasRoom(int id) {
		for (Integer r_id :this.room_ids) {
			if (id == r_id) {
				return true;
			}
		}
		
		return false;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void addRoom(Room newRoom) {
		this.room_ids.add(newRoom.getDBRef());
	}
	
	public void addRoom(int dbref) {
		this.room_ids.add(dbref);
	}
	
	public void deleteRoom(Room oldRoom) {
		this.room_ids.remove((Integer) oldRoom.getDBRef());
	}
	
	public void deleteRoom(int dbref) {
		this.room_ids.remove((Integer) dbref);
	}
}