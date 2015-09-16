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
	// where dropped stuff goes
	public final int FLOOR   = 1;
	public final int STORAGE = 2;
	public final int TRASH   = 3;

	final public ArrayList<Room> rooms = new ArrayList<Room>();

	final public LinkedList<Thing> things = new LinkedList<Thing>();
	final public LinkedList<Item> items = new LinkedList<Item>();

	final public HashMap<Thing, Point> thingPos = new HashMap<Thing, Point>();
	final public HashMap<Item,  Point> itemPos  = new HashMap<Item,  Point>();

	private Player owner;

	private int max_rooms;

	public int dropTo = FLOOR; 

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
	};

	public House(final HouseSize size) {
		this(null, size);
	}

	public House(final Player owner, final HouseSize hSize) {
		this.owner = owner;
		this.max_rooms = hSize.size;
	}

	public House(final Player owner, final int maxRooms) {
		this.owner = owner;
		this.max_rooms = maxRooms;
	}

	public int getMaxRooms() {
		return this.max_rooms;
	}

	public int getSize() {
		return rooms.size();
	}

	public String[] getInfo() {
		final String[] info = new String[] {
				"House",
				"Owner: " + this.owner.getName(),
				"Size:  " + getSize(),
				"Rooms: " + this.rooms.size() + " / " + this.max_rooms
		};

		return info;
	}

	public boolean addRoom(final Room room) {
		boolean success = false;

		if( getSize() < getMaxRooms() ) {
			this.rooms.add( room );
			success = true;
		}

		return success;
	}

	public void removeRoom(final Room room) {
		// remove items
		// remove things
		// remove the room
		rooms.remove( room );
	}

	public void addItem(final Item item) {
		items.add(item);
		placeItem(item, new Point(0,0));
	}

	public void placeItem(final Item item, final Point point) {
		itemPos.put(item, point);
	}

	public void removeItem(final Item item) {
		items.remove(item);
		itemPos.remove(item);
	}

	public void addThing(final Thing thing) {
		things.add(thing);
		placeThing(thing, new Point(0,0));
	}

	public void placeThing(final Thing thing, final Point point) {
		thingPos.put(thing, point);
	}

	public void removeThing(final Thing thing) {
		things.remove(thing);
		thingPos.remove(thing);
	}
}