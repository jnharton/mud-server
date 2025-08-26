package mud.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mud.ObjectFlag;
import mud.MUDObject;
import mud.TypeFlag;
import mud.events.EventSource;
import mud.events.SayEvent;
import mud.events.SayEventListener;
import mud.interfaces.Instance;
import mud.misc.Direction;
import mud.misc.Terrain;
import mud.misc.Trigger;
import mud.misc.TriggerType;
import mud.misc.Zone;
import mud.objects.Thing;
import mud.objects.exits.Door;
import mud.utils.Utils;
import mud.weather.Weather;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * Room Class
 * 
 * @author Jeremy N. Harton
 * 
 */
public class Room extends MUDObject implements EventSource, Instance
{	
	//public static final String DAY = "DAY";
	//public static final String NIGHT = "NIGHT";
	
	// NOTE: for room, location and "parent" are the same
	private RoomType roomType = RoomType.NONE;              // the type of room (I = Inside, O = Outside, P = Protected, N = None)
	private Terrain terrain = Terrain.NONE;                 // terrain type of the room (affects movement speed?)
	
	// x, y - size of the room ( 10x10 default )
	// z - height of room ( 10 default )
	private int x = 10, y = 10, z = 10;
	
	private transient Zone zone = null;                     // the zone this room belongs to
	
	private transient List<Exit> exits;                     // the exits leading away from the room
	private transient String exitNames;                     // formatted string containing the usable exit names
	
	private transient List<Thing> things;                   // the objects the room contains (things)
	private transient List<Item> items;                     // the objects the room contains (items)

	public String music;                                    // the ambient background music for this room (filename, probably a wav file)
	//public String timeOfDay = DAY;                        // replace this with an enum with one type per each or a hashmap string, boolean?
	// DAY or NIGHT
	
	private Weather weather;                                // the weather in this room
	
	private Integer instance_id = -1;                       // instance_id (transient?)
	
	public transient char[] tiles;
	
	private transient List<Player> listeners;               // Player(s) in the Room listening to what is being said
	private transient List<SayEventListener> _listeners;    // Other things listening to say events? 

	private transient Map<TriggerType, List<Trigger>> triggers; //
	
	// initialize trigger lists
	/*{
		this.triggers.put(TriggerType.onEnter, new LinkedList<Trigger>());
		this.triggers.put(TriggerType.onLeave, new LinkedList<Trigger>());
	}*/
	
	private Exit[] dirMap = new Exit[9]; // dirMap[Direction.NORTH]
	
	// misc note: parent == location

	/**
	 * Construct a room using only default parameter
	 */
	public Room() {
		super(-1);
		
		this.name = "room";
		this.desc = "You see nothing.";
		this.flags = EnumSet.of(ObjectFlag.SILENT);
		this.location = 0;
		
		this.type = TypeFlag.ROOM;
		
		this.exits = new ArrayList<Exit>();
		
		this.tiles = new char[x * y];
		Arrays.fill(tiles, 'X');
		
		this.items = new ArrayList<Item>();
		this.things = new ArrayList<Thing>();
		
		this.listeners = new ArrayList<Player>();
		this._listeners = new ArrayList<SayEventListener>();
		
		this.triggers = new HashMap<TriggerType, List<Trigger>>();
		
		this.triggers.put(TriggerType.onEnter, new LinkedList<Trigger>());
		this.triggers.put(TriggerType.onLeave, new LinkedList<Trigger>());
	}
	
	/**
	 * Copy Constructor
	 * 
	 * @param toCopy
	 */
	public Room(final Room toCopy)
	{
		super(toCopy.getDBRef());
		
		this.name = toCopy.getName();         // Set the name
		this.desc = toCopy.getDesc();         // Set the description to the default
		this.flags = toCopy.getFlags();       // Set the flags
		this.location = toCopy.getLocation(); // Set the location
		
		this.type = TypeFlag.ROOM;
		
		this.exits = new ArrayList<Exit>();
		
		this.tiles = new char[x * y];
		Arrays.fill(tiles, 'X');
		
		this.items = new ArrayList<Item>();
		this.things = new ArrayList<Thing>();
		
		this.listeners = new ArrayList<Player>();
		this._listeners = new ArrayList<SayEventListener>();
		
		this.triggers = new HashMap<TriggerType, List<Trigger>>();
		
		this.triggers.put(TriggerType.onEnter, new LinkedList<Trigger>());
		this.triggers.put(TriggerType.onLeave, new LinkedList<Trigger>());
	}
	
	/**
	 * Loading Constructor
	 * 
	 * @param tempDBREF
	 * @param tempName
	 * @param tempFlags
	 * @param tempDesc
	 * @param tempLocation
	 */
	public Room(int tempDBREF, String tempName, final EnumSet<ObjectFlag> tempFlags, String tempDesc, int tempLocation)
	{
		super(tempDBREF);
		
		this.name = tempName;         // Set the name
		this.desc = tempDesc;         // Set the description
		this.flags = tempFlags;       // Set room flags
		this.location = tempLocation; // Set the location
		
		this.type = TypeFlag.ROOM;
		
		this.exits = new ArrayList<Exit>();
		
		this.tiles = new char[x * y];
		Arrays.fill(tiles, 'X');
		
		this.items = new ArrayList<Item>();
		this.things = new ArrayList<Thing>();
		
		this.listeners = new ArrayList<Player>();
		this._listeners = new ArrayList<SayEventListener>();
		
		this.triggers = new HashMap<TriggerType, List<Trigger>>();
		this.triggers.put(TriggerType.onEnter, new LinkedList<Trigger>());
		this.triggers.put(TriggerType.onLeave, new LinkedList<Trigger>());
	}

	/**
	 * Set parent room.
	 * 
	 * @param parentRoomId
	 */
	public void setParent(int parentRoomId) {
		this.setLocation(parentRoomId);
	}

	/**
	 * Get parent room.
	 * 
	 * @return int parent room dbref number
	 */
	public int getParent() {
		return this.getLocation();
	}

	/**
	 * Set the type of room.
	 * 
	 * @param newRoomType
	 */
	public void setRoomType(final RoomType newRoomType) {
		this.roomType = newRoomType;
	}

	/**
	 * Get the type of room.
	 * 
	 * @return
	 */
	public RoomType getRoomType() {
		return this.roomType;
	}
	
	public Weather getWeather() {
		return this.weather;
	}
	
	public void setWeather(Weather weather) {
		this.weather = weather;
	}
	
	public Exit getExit(final Direction dir) {
		return this.dirMap[dir.getValue()];
	}
	/**
	 * getExits
	 * 
	 * NOTE: the returned list /should/ be unmodifiable. that is, the
	 * return here is not for modifying the list the exits
	 * 
	 * @return the exits
	 */
	public List<Exit> getExits() {
		return Collections.unmodifiableList(exits);
	}
	
	// TODO excise method
	public String getExitNames() {
		if( exits.size() > 0 ) {
			final StringBuilder buf = new StringBuilder();
			
			for (final Exit e : exits) {
				if( e instanceof Door ) {
					Door d = (Door) e;
					
					if( getDBRef() == d.getLocation() ) {
						if( d.isLocked() ) buf.append(", ").append(d.getName().split("/")[0] + " (locked)");
						else               buf.append(", ").append(d.getName().split("/")[0]);
					}
					else if( getDBRef() == d.getDestination() ) {
						if( d.isLocked() ) buf.append(", ").append(d.getName().split("/")[1] + " (locked)");
						else               buf.append(", ").append(d.getName().split("/")[1]);
					}
				} 
				else buf.append(", ").append(e.getName());
			}
			
			return buf.toString().substring(2); // clip off the initial, unnecessary " ,"
		}
		else return "";
	}
	
	public String getExitNames(boolean visible) {
		return "";
	}
	
	// TODO excise method
	public String getVisibleExitNames() {
		final StringBuilder buf = new StringBuilder();
		for (final Exit e : exits) {
			if (!e.getFlags().contains("D")) {
				if( e instanceof Door ) {
					Door d = (Door) e;
					
					if( getDBRef() == d.getLocation() ) {
						if( d.isLocked() ) buf.append(", ").append(d.getName().split("/")[0] + " (locked)");
						else buf.append(", ").append(d.getName().split("/")[0]);
					}
					else if( getDBRef() == d.getDestination() ) {
						if( d.isLocked() ) buf.append(", ").append(d.getName().split("/")[1] + " (locked)");
						else buf.append(", ").append(d.getName().split("/")[1]);
					}
				} 
				else buf.append(", ").append(e.getName());
			}
		}
		return buf.toString().substring(2); // clip off the initial, unecessary " ,"
	}

	/**
	 * setExits
	 * 
	 * Make this room have the exits specified
	 * 
	 * @param exits the exits to set
	 */
	public void setExits(final List<Exit> exits) {
		this.exits.clear();
		this.exits.addAll( exits );
	}
	
	public void addExit(final Exit exit) {
		this.exits.add(exit);
	}
	
	public void removeExit(final Exit exit) {
		this.exits.remove(exit);
	}
	
	public void setDimension(final String dim, final int size) {
		switch( dim.toLowerCase() ) {
		case "x": this.x = size; break;
		case "y": this.y = size; break;
		case "z": this.z = size; break;
		default:  break;
		}
	}
	
	public int getDimension(final String dim) {
		int m = 0; // magnitude?
		
		switch( dim.toLowerCase() ) {
		case "x": m = this.x; break;
		case "y": m = this.y; break;
		case "z": m = this.z; break;
		default:  m = -1;     break;
		}
		
		return m;
	}
	
	// adjustDimensions with just change data? does this render set/get redundant?
	public void setDimensions(int xSize, int ySize, int zSize) {
		this.x = xSize;
		this.y = ySize;
		this.z = zSize;
	}

	public List<Player> getListeners() {
		return this.listeners;
	}

	public void addListener(Player player) {
		this.listeners.add(player);
	}

	public void removeListener(Player player) {
		this.listeners.remove(player);
	}
	
	/* Say Event Stuff */
	public synchronized void addSayEventListener(SayEventListener listener)  {
		this._listeners.add(listener);
	}
	
	public synchronized void removeSayEventListener(SayEventListener listener)   {
		this._listeners.remove(listener);
	}
	
	/**
	 * Add an Item to the room.
	 * 
	 * @param item
	 */
	public void addItem(final Item item) {
		this.items.add(item);
	}
	
	/**
	 * Add multiple items to the room at the same time.
	 * 
	 * @param items List of Item(s)
	 */
	public void addItems(final List<Item> items) {
		this.items.addAll(items);
	}
	
	/**
	 * Remove an Item from the room.
	 * @param item
	 */
	public void removeItem(final Item item) {
		this.items.remove(item);
	}
	
	public void removeItems(final List<Item> items) {
		this.items.removeAll(items);
	}
	
	/**
	 * Get a List of the Item(s) in the Room.
	 * 
	 * @return an unmodifiable list
	 */
	public List<Item> getItems() {
		return Collections.unmodifiableList(this.items);
	}
	
	/**
	 * Add a Thing to the room
	 * 
	 * @param thing the Thing to add
	 */
	public void addThing(final Thing thing) {
		this.things.add(thing);
	}
	
	/**
	 * Add multiple Thing(s) to the room at the same time.
	 * 
	 * @param things List of Thing(s) to add
	 */
	public void addThings(final List<Thing> things) {
		this.things.addAll( things );
	}
	
	/**
	 * Remove a Thing from the room
	 * 
	 * @param thing
	 */
	public void removeThing(final Thing thing) {
		this.things.remove(thing);
	}
	
	public void removeThings(final List<Thing> things) {
		this.things.removeAll( things );
	}

	public List<Thing> getThings() {
		return this.things;
	}
	
	/* Triggers & Scripting */
	public void setTrigger(TriggerType type, Trigger trigger) {
		this.triggers.get(type).add(trigger);
	}

	public List<Trigger> getTriggers(TriggerType triggerType) {
		return Collections.unmodifiableList(this.triggers.get(triggerType));
	}
	
	public void setTerrain(final Terrain newTerrain) {
		this.terrain = newTerrain;
	}

	public Terrain getTerrain() {
		return this.terrain;
	}

	public void setZone(final Zone zone) {
		this.zone = zone;
	}

	public Zone getZone() {
		return this.zone;
	}
	
	public void setDirectionExit(final Integer direction, final Exit exit) {
		this.dirMap[direction] = exit;
	}

	// call this method whenever you want to notify the event listeners
	@Override
	public synchronized void fireEvent(String message) {
		final SayEvent sayEvent = new SayEvent(this, message);
		
		for(final SayEventListener sl : this._listeners) {
			sl.handleSayEvent(sayEvent);
		}
	}
	
	// instancing stuff
	
	// implements
	public Boolean isInstance() {		
		if( this.instance_id > 0 ) {
			return true;
		}
		
		return false;
	}
	
	// implements
	public Integer getInstanceId() {
		return this.instance_id;
	}

	/**
	 * Translate the persistent aspects of the room into the string
	 * format used by the database
	 */
	public String toDB() {
		String[] output = new String[9];
		output[0] = "" + this.getDBRef();                         // database reference number
		output[1] = this.getName();                               // name
		output[2] = "" + TypeFlag.asLetter(this.type);            // flags
		output[2] = output[2] + getFlagsAsString();
		output[3] = this.getDesc();                               // description
		output[4] = "" + this.getLocation();                      // location (a.k.a parent)
		output[5] = "" + this.getRoomType().toString().charAt(0); // room type
		output[6] = this.x + "," + this.y + "," + this.z;         // room dimensions (x,y,z)
		output[7] = "-1";                                         // room terrain
		output[8] = (zone != null) ? "" + zone.getId() : "-1";    // zone data

		return Utils.join(output, "#");
	}
	
	// TODO remove?
	public String toJSON() {
		final StringBuilder sb = new StringBuilder();

		int count = 0;

		sb.append("{\n");

		sb.append("\t\"dbref\"" + ": \"" + getDBRef() + "\",\n");
		sb.append("\t\"name\"" + ": \"" + getName() + "\",\n");
		sb.append("\t\"flags\"" + ": \"" + TypeFlag.asLetter(this.type) + getFlagsAsString() + "\",\n");
		sb.append("\t\"desc\"" + ": \"" + getDesc() + "\",\n");
		sb.append("\t\"location\"" + ": \"" + getLocation() + "\",\n");

		/*sb.append("\n");

		sb.append("\t\"names\"" + ":\n");

		sb.append("\t{\n");

		int numNames = names.size();
		count = 0;

		for(final String name : names) {
			sb.append("\t\t\"name\"" + ": \"" + name + "\"");
			if( count < numNames ) sb.append(",\n");
			else                   sb.append("\n");
			count++;
		}

		sb.append("\t},\n");*/

		sb.append("}\n");

		return sb.toString();
	}
	
	public String toString() {
		return getName() + " (#" + getDBRef() + ")";
	}
}