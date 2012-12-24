package mud;

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
import java.util.HashMap;

import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.Thing;

public class House {
	private Player owner;
	private int max_rooms;
	public ArrayList<Room> rooms;
	public HouseSize house_size;
	public Integer size;
	
	public HashMap<Thing, Point> things;
	
	public HashMap<Item, Point> items;
	
	/*
	 * want to include configurable permissions for the rooms belonging to the house in here,
	 * especially controlling who can set stuff down, pick stuff up, etc Where stuff goes if dropped without
	 * drop permissions (purely prevented, or acts as trash bin)
	 * 
	 * in future, might be useful to store item positions in a room here, so that the
	 * system can auto tidy the house if desired
	 * 
	 * # I really, really need a way for rooms to know what Zone, House, etc they belong to,
	 * that can be fit in the database wihout too much trouble
	 */
	
	enum HouseSize {
		SMALL(1),
		MEDIUM(2),
		LARGE(3),
		CUSTOM(-1);
		
		private int size;
		
		private HouseSize() {
		}
		
		private HouseSize(int tSize) {
			this.size = tSize;
		}
		
		public int getSize() {
			return this.size;
		}
		
		public int getMaxRooms() {
			// rooms have 5 rooms per size grade
			// size 1, rooms 5
			// size 2, rooms 10
			// size 3  rooms 15
			if(size != -1) {
				return 5 * size;
			}
			else {
				return size;
			}
		}
	};
	
	House(HouseSize hSize) {
		this.owner = null;
		this.house_size = hSize;
		this.size = this.house_size.getSize();
		this.max_rooms = this.house_size.getMaxRooms();
		this.rooms = new ArrayList<Room>();
	}
	
	House(Player hOwner, HouseSize hSize) {
		this.owner = hOwner;
		this.house_size = hSize;
		this.size = this.house_size.getSize();
		this.max_rooms = this.house_size.getMaxRooms();
		this.rooms = new ArrayList<Room>();
	}
	
	House(Player hOwner, int hMax_Rooms) {
		this.owner = hOwner;
		this.max_rooms = hMax_Rooms;
	}
	
	public String[] getInfo() {
		String[] info = new String[4];
		info[0] = "House";
		info[1] = "Owner: " + this.owner.getName();
		info[2] = "Size: " + this.size.toString() + "(" + this.size + ")";
		info[3] = "Max Rooms: " + this.max_rooms;
		return info;
	}
	// House
	// Owner: Nathan
	// Size: Small (1)
	// Max. Rooms: 5
}
