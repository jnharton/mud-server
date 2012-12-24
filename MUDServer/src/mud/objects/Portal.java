package mud.objects;

import java.util.ArrayList;
import java.util.Random;

import mud.objects.Player;
import mud.utils.Utils;

/*
 * currently this class does not support keyed portals that require a physical key object to pass
 * potentially I could implement the following kinds of locks:
 * 	- time of day (sun's position)
 * 	- time of month (lunar cycle)
 * 	- time of year (solar cycle)
 * 	- race
 * 	- physical key
 *	- verbal key
 *
 * I think the best way to utilize this class is to stick it to something, not sure how.
 * For instance, it's not really a tangible object but it has a presence and a location
 * Do I keep a giant portal table?
 * 
 * I assume herein that all portal are two-way (despite wanting to code for either)
 */

/**
 * 
 * @author Jeremy
 *
 */
//public class Portal extends Exit implements Usable<Portal> {
public class Portal extends Exit {
	private PortalType type;                        // type of portal
	private Object key;                             // if it's locked with a key what is it
	private int origin;                             // portal origin
	private Integer destination = null;             // a single destination (single destination portal)
	private ArrayList<Integer> destinations = null; // a list of destinations by dbref (multi-destination portal)
	private boolean active;                         // is the portal currently active?
	private boolean requiresKey = false;            // does it require a key?

	private static Random generator = new Random();

	// standard, "always open" portal (default is active)
	public Portal(int pOrigin, int pDestination) {
		super(ExitType.PORTAL);
		this.type = PortalType.STD;
		this.key = null;
		
		this.location = pOrigin;
		
		this.origin = pOrigin;
		this.destination = pDestination;
		this.active = true;
	}

	// keyed portal (default is inactive)
	public Portal(Object pKey, int pOrigin, int pDestination) {
		super(ExitType.PORTAL);
		this.type = PortalType.STD;
		this.key = pKey;
		if(pKey != null) { this.requiresKey = true; }
		
		this.location = pOrigin;
		
		this.origin = pOrigin;
		this.destination = pDestination;
		this.active = false;
	}

	// typed portal (default is active)
	public Portal(PortalType pType, int pOrigin, int[] pDestinations) {
		super(ExitType.PORTAL);
		this.type = pType;
		this.key = null;
		
		this.location = pOrigin;
		
		this.origin = pOrigin;
		if(pType == PortalType.STD) {
			this.destination = pDestinations[0];
		}
		else if(pType == PortalType.RANDOM) {
			this.destinations = new ArrayList<Integer>(pDestinations.length);
			for(int d : pDestinations) {
				this.destinations.add(d);
			}
		}
		this.active = true;
	}

	// typed, keyed portal (default is inactive)
	public Portal(PortalType pType, Object pKey, int pOrigin, int[] pDestinations) {
		super(ExitType.PORTAL);
		this.type = pType;
		this.key = pKey;
		if(pKey != null) { this.requiresKey = true; }
		
		this.location = pOrigin;
		
		this.origin = pOrigin;
		if(pType == PortalType.STD) { this.destination = pDestinations[0]; }
		else if(pType == PortalType.RANDOM) {
			this.destinations = new ArrayList<Integer>(pDestinations.length);
			for(int d : pDestinations) {
				this.destinations.add(d);
			}
		}
		this.active = false;
	}
	
	public PortalType getPortalType() {
		return this.type;
	}

	public void setType(PortalType newType) {
		PortalType last = this.type;
		this.type = newType;
		if(last != newType) { // don't permit changing to the same type as it is already
			if( newType == PortalType.RANDOM ) {
				this.destinations = new ArrayList<Integer>(1); // instantiate the destinations array list
				this.destinations.add(this.destination);       // add the current destination
				this.destination = null;                       // clear the normal destination
			}
			else if( last == PortalType.RANDOM) {
				this.destination = getDestination();           // select a random destination
				this.destinations = null;                      // clear the destinaton array list
			}
		}
	}
	
	public Object getKey() {
		return this.key;
	}
	
	public void setKey(Object newKey) {
		this.key = newKey;
	}

	public int getOrigin() {
		return this.origin;
	}

	public void setOrigin(int newOrigin) {
		this.origin = newOrigin;
	}

	public int getDestination() {
		if(this.type == PortalType.RANDOM) {
			if(destinations != null && destinations.size() > 0) {
				int roll = generator.nextInt(destinations.size()); // roll a random destination
				return destinations.get(roll);	
			}
			else {
				return -1;
			}
		}
		else {
			return destination;
		}
	}

	public void setDestination(int newDestination) {
		if(destinations == null) {
			this.destination = newDestination;
		}
		else {
			destinations.add(newDestination);
		}
	}

	/**
	 * Activation via words
	 * 
	 * @param key
	 */
	public void activate(Object pKey) {
		if(key instanceof String) {
			if( ( (String) pKey ).equals( (String) this.key ) ) {
				this.active = true;
				System.out.println("Portal: activated");
			}
		}
	}

	/**
	 * Deactivation via words
	 * @param key
	 */
	public void deactivate(Object pKey) {
		if(key instanceof String) {
			if( ( (String) pKey ).equals( (String) this.key ) == true ) {
				this.active = false;
				System.out.println("Portal: deactivated");
			}
		}
	}

	public boolean isActive() {
		return this.active;
	}
	
	public boolean requiresKey() {
		return this.requiresKey;
	}
	
	public boolean hasKey( Player p ) {
		return p.getInventory().contains(this.key);
	}
	
	@Override
	public String toDB() {
		String[] output = new String[8];
		output[0] = Utils.str(this.getDBRef());    // portal database reference number
		output[1] = this.getName();                // portal name
		output[2] = this.getFlags();               // portal flags
		output[3] = this.getDesc();                // portal description
		output[4] = Utils.str(this.getLocation()); // portal location (a.k.a source)
		if(this.type == PortalType.STD) {
			output[5] = Utils.str(this.destination); // portal destination
		}
		else if(this.type == PortalType.RANDOM) {
			ArrayList<String> d = new ArrayList<String>();
			for(int dest : this.destinations) {
				d.add(Utils.str(dest));
			}
			String[] destStringArr = Utils.arraylistToString(d);
			//output[5] = Utils.join(this.destinations, ","); // portal destination(s)
			output[5] = Utils.join(destStringArr, ","); // portal destination(s)
		}
		output[6] = Utils.str(this.getExitType().ordinal()); // exit type
		output[7] = Utils.str(type.ordinal());               // portal type
		
		String output1 = Utils.join(output, "#");  //
		return output1;
	}
}