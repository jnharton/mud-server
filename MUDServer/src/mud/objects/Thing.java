package mud.objects;

import java.util.ArrayList;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.MUDObject;
import mud.utils.Utils;

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
	
	protected ArrayList<Item> contents = null;
	
	/**
	 * Thing - no parameters
	 * 
	 * A no-argument constructor for making a sub-class of Thing
	 */
	public Thing() {
	}
	
	public Thing(boolean isContainer) {
		if( isContainer ) {
			this.contents = new ArrayList<Item>();
		}
	}

	// usual constructor
	public Thing(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc)
	{
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc);
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
	}
	
	public double getWeight() {
		return this.weight;
	}

	public String toDB() {
		String[] output = new String[6];
		output[0] = this.getDBRef() + "";           // database reference number
		output[1] = this.getName();                 // name
		output[2] = this.getFlagsAsString();        // flags
		output[3] = this.getDesc();                 // description
		output[4] = this.getLocation() + "";        // location (a.k.a parent)
		output[5] = this.thing_type.ordinal() + ""; // thing type
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
}
