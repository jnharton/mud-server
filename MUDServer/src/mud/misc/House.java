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
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

// TODO I'm thinking house should be a subclass of building
public class House {
	// where dropped stuff goes
	private static final int FLOOR   = 1;
	private static final int STORAGE = 2;
	private static final int TRASH   = 3;
	
	// house sizes
	public static final int SMALL = 1;
	public static final int MEDIUM = 2;
	public static final int LARGE = 3;

	final private List<Room> rooms;
	
	// lists of placeable objects
	final private List<Thing> things;
	final private List<Item> items;
	
	// positions of placeable objects
	final private Map<Thing, Point> thingPos;
	final private Map<Item,  Point> itemPos;
	
	final private Map<Player, String> accessPerms;

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
	 * that can be fit in the database without too much trouble
	 */
	
	public House(final Player owner, final int maxRooms) {
		this.rooms = new ArrayList<Room>();

		this.things = new LinkedList<Thing>();
		this.items = new LinkedList<Item>();

		this.thingPos = new Hashtable<Thing, Point>();
		this.itemPos  = new Hashtable<Item,  Point>();
		
		this.accessPerms = new Hashtable<Player, String>();
		
		this.owner = owner;
		this.max_rooms = maxRooms;
	}

	public int getMaxRooms() {
		return this.max_rooms;
	}
	
	public Player getOwner() {
		return this.owner;
	}
	
	public void setOwner(final Player newOwner) {
		this.owner = newOwner;
	}

	public int getSize() {
		return rooms.size();
	}
	
	// TODO return list? map?
	public String[] getInfo() {
		final String[] info = new String[] {
				"House",
				" Owner: " + this.owner.getName(),
				"  Size: " + getSize(),
				" Rooms: " + this.rooms.size() + " / " + this.max_rooms,
				" Items: " + this.items.size(),
				"Things: " + this.things.size()
		};

		return info;
	}
	
	public void getRoom() {
		
	}
	
	public List<Room> getRooms() {
		return Collections.unmodifiableList(this.rooms);
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
		for(final Item item : room.getItems()) {
			item.setLocation(-1);
		}
		room.removeItems( room.getItems() );
		
		// remove things
		for(final Thing thing : room.getThings()) {
			thing.setLocation(-1);
		}
		room.removeThings( room.getThings() );
		
		// remove the room
		rooms.remove( room );
	}

	public void addItem(final Item item) {
		this.items.add(item);
		this.itemPos.put(item, new Point(0,0));
	}

	public void placeItem(final Item item, final Point point) {
		this.itemPos.put(item, point);
	}

	public void removeItem(final Item item) {
		this.items.remove(item);
		this.itemPos.remove(item);
	}

	public void addThing(final Thing thing) {
		this.things.add(thing);
		this.thingPos.put(thing, new Point(0,0));
	}

	public void placeThing(final Thing thing, final Point point) {
		this.thingPos.put(thing, point);
	}

	public void removeThing(final Thing thing) {
		this.things.remove(thing);
		this.thingPos.remove(thing);
	}
}