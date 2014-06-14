package mud.objects;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.events.EventSource;
import mud.events.PortalEvent;
import mud.events.PortalEventListener;
import mud.events.SayEvent;
import mud.events.SayEventListener;
import mud.interfaces.Lockable;
import mud.objects.Player;
import mud.utils.Utils;

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
 * 
 * public class Portal extends Exit implements Usable<Portal> ?
 * 
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
 * @author Jeremy
 *
 */
public class Portal extends Exit implements EventSource, SayEventListener {
	private PortalType pType;                       // type of portal
	private Object key;                             // if it's locked with a key what is it
	//private int origin;                             // portal origin
	//private Integer destination = null;             // a single destination (single destination portal)
	private ArrayList<Integer> destinations = null; // a list of destinations by dbref (multi-destination portal)
	
	// portal state
	private boolean active;                         // is the portal currently active?
	private boolean requiresKey = false;            // does it require a key?

	private static Random generator = new Random();
	
	private List<PortalEventListener> _listeners = new ArrayList<PortalEventListener>();

	// standard, "always open" portal (default is active)
	public Portal(int pOrigin, int pDestination) {
		this.eType = ExitType.PORTAL;
		this.pType = PortalType.STD;
		this.key = null;
		
		this.location = pOrigin;
		
		this.origin = pOrigin;
		this.destination = pDestination;
		this.active = true;
	}

	// keyed portal (default is inactive)
	public Portal(Object pKey, int pOrigin, int pDestination) {
		super();
		
		this.eType = ExitType.PORTAL;
		this.pType = PortalType.STD;
		this.key = pKey;
		if (pKey != null) { this.requiresKey = true; }
		
		this.location = pOrigin;
		
		this.origin = pOrigin;
		this.destination = pDestination;
		this.active = false;
	}
	
	public Portal(PortalType pType, int pOrigin, int pDestination) {
		super();
		
		this.eType = ExitType.PORTAL;
		this.pType = pType;
		this.key = null;
		
		this.location = pOrigin;
		
		this.origin = pOrigin;
		this.destination = pDestination;
		this.active = true;
	}

	// typed portal (default is active)
	public Portal(PortalType pType, int pOrigin, int[] pDestinations) {
		super();
		
		this.eType = ExitType.PORTAL;
		this.pType = pType;
		this.key = null;
		
		this.location = pOrigin;
		
		this.origin = pOrigin;
		if (pType == PortalType.STD) {
			this.destination = pDestinations[0];
		}
		else if (pType == PortalType.RANDOM) {
			this.destinations = new ArrayList<Integer>(pDestinations.length);
			for (int d : pDestinations) {
				this.destinations.add(d);
			}
		}
		this.active = true;
	}

	// typed, keyed portal (default is inactive)
	public Portal(PortalType pType, Object pKey, int pOrigin, int[] pDestinations) {
		super();
		
		this.eType = ExitType.PORTAL;
		this.pType = pType;
		this.key = pKey;
		if (pKey != null) { this.requiresKey = true; }
		
		this.location = pOrigin;
		
		this.origin = pOrigin;
		if (pType == PortalType.STD) { this.destination = pDestinations[0]; }
		else if (pType == PortalType.RANDOM) {
			this.destinations = new ArrayList<Integer>(pDestinations.length);
			for (int d : pDestinations) {
				this.destinations.add(d);
			}
		}
		this.active = false;
	}
	
	public Portal(int tempDBRef, String tempName, final EnumSet<ObjectFlag> flagsNotUsed, String tempDesc, int tempLocation, int tempDestination) {
		super( tempDBRef, tempName, flagsNotUsed, tempDesc, tempLocation, tempDestination );
		
		this.eType = ExitType.PORTAL;
	}
	
	public PortalType getPortalType() {
		return this.pType;
	}

	public void setType(PortalType newType) {
		PortalType last = this.pType;
		this.pType = newType;
		if (last != newType) { // don't permit changing to the same type as it is already
			if ( newType == PortalType.RANDOM ) {
				this.destinations = new ArrayList<Integer>(1); // instantiate the destinations array list
				this.destinations.add(this.destination);       // add the current destination
				this.destination = null;                       // clear the normal destination
			}
			else if ( last == PortalType.RANDOM) {
				this.destination = getDestination();           // select a random destination
				this.destinations = null;                      // clear the destinaton array list
			}
		}
	}
	
	/**
	 * Get the Portal's key
	 * 
	 * @return
	 */
	public Object getKey() {
		return this.key;
	}
	
	/**
	 * Set the Portal's key
	 * 
	 * @param newKey
	 */
	public void setKey(Object newKey) {
		this.key = newKey;
	}
	
	/**
	 * Get the origin of the portal
	 * 
	 * @return
	 */
	public int getOrigin() {
		return this.origin;
	}
	
	/**
	 * Set the origin of the portal
	 * 
	 * @param newOrigin
	 */
	public void setOrigin(int newOrigin) {
		this.origin = newOrigin;
	}
	
	/**
	 * Set the Portal's destination, or add the destination
	 * to the list of possible destination
	 * 
	 * @param newDestination
	 */
	@Override
	public void setDestination(int newDestination) {
		if (destinations == null) {
			this.destination = newDestination;
		}
		else {
			destinations.add(newDestination);
		}
	}
	
	/**
	 * Get the portal's or a destination
	 * 
	 * @return
	 */
	@Override
	public int getDestination() {
		if (this.pType == PortalType.RANDOM) {
			if (destinations != null && destinations.size() > 0) {
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

	/**
	 * Activation via words
	 * 
	 * @param key
	 */
	public boolean activate(Object pKey) {
		if (key instanceof String) {
			if ( ( (String) pKey ).equals( (String) this.key ) ) {
				this.active = true;
				System.out.println("Portal: activated");
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Deactivation via words
	 * @param key
	 */
	public boolean deactivate(Object pKey) {
		if (key instanceof String) {
			if (((String) pKey).equals((String) this.key)) {
				this.active = false;
				System.out.println("Portal: deactivated");
				return true;
			}
		}
		
		return false;
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
		output[0] = this.getDBRef() + "";                // database reference number
		output[1] = this.getName();                      // name
		output[2] = TypeFlag.asLetter(this.type) + "";   // flags
		output[2] = output[2] + this.getFlagsAsString();
		output[3] = this.getDesc();                      // description
		output[4] = this.getLocation() + "";             // portal location (a.k.a source)
		if (this.pType == PortalType.STD) {
			output[5] = this.destination + "";           // portal destination
		}
		else if (this.pType == PortalType.RANDOM) {
			final ArrayList<String> d = new ArrayList<String>();
			for (int dest : this.destinations) {
				d.add(dest + "");
			}
			output[5] = Utils.join(d, ",");              // portal destination(s)
		}
		output[6] = this.getExitType().ordinal() + "";   // exit type
		output[7] = pType.ordinal() + "";                // portal type
		return Utils.join(output, "#");
	}

	@Override
	public void handleSayEvent(SayEvent se) {
		if( active ) {
			if( deactivate(se.getMessage() ) ) {
			}
		}
		else {
			if( activate(se.getMessage()) ) {
			}
		}
	}

	@Override
	public void fireEvent(String message) {
		PortalEvent event = new PortalEvent(this);
		Iterator<PortalEventListener> iter = _listeners.iterator();
		while(iter.hasNext())  {
			 iter.next().handlePortalEvent(event);
		}
	}
}