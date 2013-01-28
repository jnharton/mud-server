package mud.utils;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

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
		
		for(Room room : rooms) {
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
		for(Integer r_id :this.room_ids) {
			if(id == r_id) {
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