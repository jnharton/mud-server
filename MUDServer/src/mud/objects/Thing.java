package mud.objects;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;

import mud.ObjectFlag;
import mud.MUDObject;
import mud.Trigger;
import mud.TriggerType;
import mud.TypeFlag;
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
 * Thing Class
 * 
 * things are objects, you can interact with them, but you can't put them in your inventory
 * bashing a thing, for example a table, might conceptually produced a busted table
 * and items such as table legs?
 * 
 * examples: doors, tables, chairs
 * 
 * @author Jeremy N. Harton
 * 
 */
public class Thing extends MUDObject {

	/**
	 * 
	 */

	protected double weight = 40; // the weight in whatever units are used of the equippable object
	
	public ThingType thing_type = ThingType.NONE;
	
	public Hashtable<String, String> attributes; 
	
	protected ArrayList<Item> contents = null;
	
	// triggers
	public Trigger onUse;
	
	/**
	 * Thing - no parameters
	 * 
	 * A no-argument constructor for making a sub-class of Thing
	 */
	public Thing() {
		this.type = TypeFlag.THING;
		attributes = new Hashtable<String, String>();
	}
	
	public Thing(String name) {
		super(-1);
		this.type = TypeFlag.THING;
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		this.name = name;
		this.location = -1;
		attributes = new Hashtable<String, String>();
	}
	
	public Thing(String name, String desc) {
		this(name);
		this.type = TypeFlag.THING;
		this.desc = desc;
	}
	
	public Thing(boolean isContainer) {
		this.type = TypeFlag.THING;
		if( isContainer ) {
			this.contents = new ArrayList<Item>();
		}
		
		attributes = new Hashtable<String, String>();
	}

	// usual constructor
	public Thing(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc)
	{
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc);
		this.type = TypeFlag.THING;
		/*
		// Set the name
		this.name = tempName;
		// Set the description to the default
		this.desc = tempDesc;
		// Set the flags
		this.flags = tempFlags;
		// Set the locks
		this.locks = "";
		// Set the dbref (database reference)
		this.dbref = tempDBRef;
		// Set the location
		this.location = tempLoc;
		*/
		
		this.attributes = new Hashtable<String, String>();
		
		this.contents = new ArrayList<Item>();
	}
	
	public void setScriptOnTrigger(TriggerType type, String script) {
		switch(type) {
		case onUse:
			onUse = new Trigger(script);
			break;
		default:
			break;
		}
	}
	
	public double getWeight() {
		return this.weight;
	}

	public String toDB() {
		String[] output = new String[6];
		output[0] = this.getDBRef() + "";                // database reference number
		output[1] = this.getName();                      // name
		output[2] = this.type + this.getFlagsAsString(); // flags
		output[3] = this.getDesc();                      // description
		output[4] = this.getLocation() + "";             // location (a.k.a parent)
		output[5] = this.thing_type.ordinal() + "";      // thing type
		return Utils.join(output, "#"); 
	}

	public String toString() {
		return this.name;
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Thing fromJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}
