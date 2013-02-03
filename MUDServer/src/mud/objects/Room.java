package mud.objects;

import java.util.ArrayList;
import mud.MUDObject;
//import mud.miscellaneous.Atmosphere;
import mud.objects.Thing;
import mud.utils.Utils;
import mud.weather.Weather;

/**
 * Room Class
 * 
 * @author Jeremy N. Harton
 * 
 */
public class Room extends MUDObject
{
	private enum Terrain { FOREST, MARSH, HILLS, MOUNTAIN, DESERT, PLAINS, AQUATIC };

	private int parent;                                         // the room inside of which this room is located
	
	private Weather weather;                                    // the weather in this room
	
	private ArrayList<Exit> exits = new ArrayList<Exit>();      // the exits leading away from the room
	
	public ArrayList<Thing> contents = new ArrayList<Thing>();  // the objects the room contains (things)
	public ArrayList<Item> contents1 = new ArrayList<Item>();   // the objects the room contains (items)
	
	public String exitNames;                                    // formatted string containing the usable exit names

	private RoomType roomType = RoomType.NONE;                   // the type of room (I = Inside, O = Outside, P = Protected, N = None)

	public String music;                                        // the ambient background music for this room
	public String timeOfDay = "DAY";                            // replace this with an enum with one type per each or a hashmap string, boolean?
	// DAY or NIGHT
	
	private Integer instance_id = null;                         // instance_id, if this is the original, it should be null

	public int x = 10, y = 10; // size of the room ( 10x10 default )
	public int z = 10;         // height of room ( 10 default )
	
	//private Atmosphere atmosphere = new Atmosphere();         // the atmosphere of the room (weather related)
	//private Terrain terrain;                                  // terrain type of the room (affects movement speed?)
	
	private ArrayList<Player> listeners;
	
	/**
	 * Construct a room using only default parameter
	 */
	public Room() {
		this.flags = "RS";
		this.locks = "";            // Set the locks
		this.location = 0;          // Set the location
		
		this.listeners = new ArrayList<Player>();
	}

	// misc note: parent == location
	public Room(int tempDBREF, String tempName, String tempFlags, String tempDesc, int tempParent)
	{
		super(tempDBREF);
		//this.dbref = tempDBREF;     // Set the dbref (database reference)
		this.name = tempName;       // Set the name
		this.desc = tempDesc;       // Set the description to the default
		this.parent = tempParent;   // Set the parent room
		this.flags = "RS";          // Set the flags
		this.locks = "";            // Set the locks
		this.location = tempParent; // Set the location
		
		this.listeners = new ArrayList<Player>();
	}

	// copy constructor
	public Room(Room toCopy)
	{
		super(toCopy.getDBRef());
		//this.dbref = toCopy.getDBRef();     // Set the dbref (database reference)
		this.name = toCopy.getName();       // Set the name
		this.desc = toCopy.getDesc();       // Set the description to the default
		this.parent = toCopy.getParent();   // Set the parent room
		this.flags = toCopy.getFlags();     // Set the flags
		this.locks = "";                    // Set the locks
		this.location = toCopy.getParent(); // Set the location
		
		this.listeners = new ArrayList<Player>();
	}
	
	@Override
	public int getLocation() {
		return this.parent;
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

	/*public void setAtmosphere(Atmosphere a) {
		this.atmosphere = a;
	}*/

	/*public Atmosphere getAtmosphere() {
		return this.atmosphere;
	}*/

	public void setWeather(Weather weather) {
		this.weather = weather;
	}

	public Weather getWeather() {
		return this.weather;
	}

	/**
	 * @return the exits
	 */
	public ArrayList<Exit> getExits() {
		return new ArrayList<Exit>(exits);
	}

	public String getExitNames() {
        final StringBuilder buf = new StringBuilder();
        for (final Exit e : exits) {
            buf.append(", ").append(e.getName());
        }
		return buf.toString().substring(2);
	}

	public String getVisibleExitNames() {
        final StringBuilder buf = new StringBuilder();
        for (final Exit e : exits) {
            if (!e.getFlags().contains("D")) {
                buf.append(", ").append(e.getName());
            }
        }
		return buf.toString().substring(2);
	}

	/**
	 * @param exits the exits to set
	 */
	public void setExits(ArrayList<Exit> exits) {
		this.exits = exits;
	}

	/*public String weather() { // ought to return string
		return this.getAtmosphere().weather.name;
	}*/

	public Integer getInstanceId() {
		if (this.instance_id != null) {
			return this.instance_id;
		}
		else {
			return -1;
		}
	}
	
	public ArrayList<Player> getListeners() {
		return this.listeners;
	}
	
	public void addListener(Player player) {
		listeners.add(player);
	}
	
	public void removeListener(Player player) {
		listeners.remove(player);
	}
	
	/**
	 * Translate the persistent aspects of the room into the string
	 * format used by the database
	 */
	public String toDB() {
		String[] output = new String[9];
		output[0] = this.getDBRef() + "";           // room database reference number
		output[1] = this.getName();                       // room name
		output[2] = this.getFlags();                      // room flags
		output[3] = this.getDesc();                       // room description
		output[4] = this.getLocation() + "";        // room location (a.k.a parent)
		output[5] = this.getRoomType().toString().substring(0, 1);                   // room type
		output[6] = this.x + "," + this.y + "," + this.z; // room dimensions (x,y,z)
		output[7] = "-1";                        // room terrain
		output[8] = "";                                   //
		
		return Utils.join(output, "#");
	}
	
	public String toJSON() {
		return null;
	}

	public String toString() {
		return "";
	}
}