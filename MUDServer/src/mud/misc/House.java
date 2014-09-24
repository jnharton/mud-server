package mud.misc;

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
import java.util.LinkedList;

import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.Thing;
import mud.utils.Point;

/**
 * A metadata structure defining a player house.
 * 
 * NOTES/IDEAS:
 * The intention here is to have a way to recognize rooms as belonging to a structure owned
 * by a player, particularly a house. This should hold a reference to any room belonging to
 * the house, a maximum size as a number of rooms (to control how "big" the house can be),
 * a reference to the player who is the owner of it, as well any ancillary data.
 * # it might be useful to extend this more generally to any structure a player can own
 * 
 * Right now, this holds coordinate data for things and items within the house, with the notion
 * of persisting location. At some level that data belongs to the item, but this way if the item is
 * in the house we can theoretically set a location for it to stay at it. Useful for positioning furniture
 * on a 2D coordinate plane, and making it stay in the same spot. Obviously if we implementing a means of
 * moving it, that change should be reflected.
 * 
 * It would be useful to extend this to hold permission data for who can move stuff, if we want stuff
 * to not be movable, or who has the right to extend this (i.e. for a guild which might need to expand)
 * structure. Should this also maintain information on spells (such as warding, locking, or positive
 * effects) place on the structure? If doors are locked with keys, does this know which doors are
 * locked and with what? 
 * 
 * @author Jeremy
 *
 */
 
 public class House
{
	final public HashMap<Thing, Point> thingPos = new HashMap<Thing, Point>();
	final public HashMap<Item,  Point> itemPos  = new HashMap<Item,  Point>();
	
	final public LinkedList<Thing> things = new LinkedList<Thing>();
	final public LinkedList<Item> items = new LinkedList<Item>();
	
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

	private enum HouseSize {
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
		this.houseSize = HouseSize.CUSTOM;
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
	
	public void addItem(Item item) {
		items.add(item);
		placeItem(item, new Point(0,0));
	}
	
	public void placeItem(Item item, Point point) {
		itemPos.put(item, point);
	}
	
	public void removeItem(Item item) {
		items.remove(item);
		itemPos.remove(item);
	}
	
	public void addThing(Thing thing) {
		things.add(thing);
		placeThing(thing, new Point(0,0));
	}
	
	public void placeThing(Thing thing, Point point) {
		thingPos.put(thing, point);
	}
	
	public void removeThing(Thing thing) {
		things.remove(thing);
		thingPos.remove(thing);
	}
	
	// House
	// Owner: Nathan
	// Size: Small (1)
	// Max. Rooms: 5
}