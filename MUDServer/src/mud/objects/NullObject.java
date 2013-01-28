package mud.objects;

import mud.MUDObject;
import mud.utils.Utils;

/**
 * This object exists solely as a place-holder type to represent destroyed objects. It has
 * a dbref value, a default location of 0 and the rests of the fields are "null"
 * 
 * @author Jeremy
 *
 */
public class NullObject extends MUDObject {
	
	public NullObject(int dbref) {
		super(dbref);
	}
	
	@Override
	public String getName() {
		return "$NULLOBJECT$"; 
	}
	
	@Override
	public String toDB() {
		// ex. 0#null#null#null#-1
		String[] output = new String[11];
		
		output[0] = getDBRef() + ""; // database reference number
		output[1] = "null";                // no name
		output[2] = "null";                // no flags
		output[3] = "null";                // no description
		output[4] = "-1";                  // no location
		
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
}
