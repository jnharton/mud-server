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
	
	public HashMap<String, Object> attributes;
	
	/**
	 * Thing - no parameters
	 * 
	 * A no-argument constructor for making a sub-class of Thing
	 */
	public Thing() {
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
		
		this.attributes = new HashMap<String, Object>();
	}
	
	public double getWeight() {
		return this.weight;
	}
	
	public ArrayList<String> look() {
		ArrayList<String> result = new ArrayList<String>();
		result.add(name + " (#" + getDBRef() + ")");
		result.add(this.desc);
		return result;
	}

	public String toDB() {
		String[] output = new String[6];
		output[0] = this.getDBRef() + "";         // thing database reference number
		output[1] = this.getName();               // thing name
		output[2] = this.getFlagsAsString();               // thing flags
		output[3] = this.getDesc();               // thing description
		output[4] = this.getLocation() + "";      // thing location (a.k.a parent)
		output[5] = "*";                          // "blank" field which is used for something in every other class
		String output1 = Utils.join(output, "#"); //
		return output1;
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
