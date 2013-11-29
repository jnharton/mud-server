package mud.objects;

import mud.MUDObject;
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
 * This object exists solely as a place-holder type to represent destroyed objects. It has
 * a dbref value, a default location of 0 and the rests of the fields are "null"
 * 
 * @author Jeremy
 *
 */
public class NullObject extends MUDObject {
	
	private boolean locked = false;
	
	public NullObject(int dbref) {
		super(dbref);
	}
	
	@Override
	public String getName() {
		return "$NULLOBJECT$"; 
	}
	
	/**
	 * use to lock NullObjects that are placeholders for objects explicity marked
	 * as ignore in the db. This should be used to prevent those NullObjects being reused
	 * since an item does exist in the db, but isn't loaded.
	 */
	public void lock() {
		this.locked = true;
	}
	
	public boolean isLocked() {
		return this.locked;
	}
	
	@Override
	public String toDB() {
		// ex. 0#null#null#null#-1
		String[] output = new String[11];
		
		output[0] = getDBRef() + ""; // database reference number
		output[1] = "null";          // no name
		output[2] = "null";          // no flags
		output[3] = "null";          // no description
		output[4] = "-1";            // no location
		output[5] = "null";
		output[6] = "null";
		output[7] = "null";
		output[8] = "null";
		output[9] = "null";
		output[10] = "null";
		
		return Utils.join(output, "#");
	}
	
	public String toString() {
		return "$NULLOBJECT$";
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MUDObject fromJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}