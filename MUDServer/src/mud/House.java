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

public class House
{
	final public HashMap<Thing, Point> things = new HashMap<Thing, Point>();
	final public HashMap<Item,  Point> items  = new HashMap<Item,  Point>();
	final public ArrayList<Room> rooms = new ArrayList<Room>();

	private Player owner;
	private int max_rooms;
	public HouseSize houseSize;

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

		final public int size;

		private HouseSize(int tSize) {
			this.size = tSize;
		}

		public int getMaxRooms() {
			// rooms have 5 rooms per size grade
			// size 1, rooms 5
			// size 2, rooms 10
			// size 3  rooms 15
			return size == -1 ? -1 : 5 * size;
		}
	};

	public House(final HouseSize size) {
        this(null, size);
	}

	public House(final Player owner, final HouseSize hSize) {
		this.owner = owner;
		this.houseSize = hSize;
		this.max_rooms = this.houseSize.getMaxRooms();
	}

	public House(final Player owner, final int maxRooms) {
		this.owner = owner;
		this.max_rooms = maxRooms;
	}

    public int getSize() {
        return houseSize.size;
    }

	public String[] getInfo() {
		String[] info = new String[4];
		info[0] = "House";
		info[1] = "Owner: " + this.owner.getName();
		info[2] = "Size: " + getSize();
		info[3] = "Max Rooms: " + this.max_rooms;
		return info;
	}
	// House
	// Owner: Nathan
	// Size: Small (1)
	// Max. Rooms: 5

}
