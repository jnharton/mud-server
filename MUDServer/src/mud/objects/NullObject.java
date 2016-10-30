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
		
		cleanup();
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
	
	private void cleanup() {
		// we don't need properties
		//this.properties = null;
		// we don't need effects
		//this.effects = null;
		// we don't need a position
		//this.pos = null;
		
		this.Edit_Ok = false;
	}
	
	@Override
	public String toDB() {
		String[] output = new String[5];
		
		output[0] = getDBRef() + ""; // database reference number
		output[1] = "null";          // no name
		output[2] = "null";          // no flags
		output[3] = "null";          // no description
		output[4] = "-1";            // no location
		
		return Utils.join(output, "#");
	}
	
	public String toString() {
		return "$NULLOBJECT$";
	}
}