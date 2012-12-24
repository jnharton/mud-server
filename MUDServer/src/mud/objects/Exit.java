package mud.objects;

import java.util.ArrayList;

import mud.MUDObject;
import mud.interfaces.Lockable;
import mud.utils.Utils;

/**
 * Exit Class
 * 
 * @author Jeremy N. Harton
 * 
 */
public class Exit extends MUDObject implements Lockable<Exit>
{
	/**
	 * 
	 */

	private ExitType eType = ExitType.STD;

	private boolean isLocked;

	private int destination = 0;
	
	public String succMsg;
	public String osuccMsg;
	public String failMsg;
	public String ofailMsg;
	
	public ArrayList<String> aliases = new ArrayList<String>();
	
	// empty default constructor for subclasses
	public Exit() {
		this.flags = "E";
	}
	
	public Exit(ExitType eType) {
		this.flags = "E";
		this.eType = eType;
	}
	
	public Exit(String name, int location, int destination) {
		// Set the name
		this.name = name;
		// Set the description to the default
		this.desc = "You see nothing.";
		// Set the flags
		this.flags = "E";
		// Set the locks
		this.locks = "";
		// Set the location
		this.location = location;
		// Set the destination
		this.destination = destination;
		// set messages to clear
		this.succMsg = "";  // Success Message
		this.osuccMsg = ""; // OSuccess Message
		this.failMsg = "";  // Failure Message
		this.ofailMsg = ""; // OFailure Message
	}
	
	// Object Loading Constructor
	// Name, Flags, Description, DBRef #, Location of exit (DBRef #), destination of exit (DBRef #)
	public Exit(int tempDBRef, String tempName, String tempFlags, String tempDesc, int tempLoc, int tempDestination)
	{
		// Set the dbref (database reference)
		super(tempDBRef);
		// Set the name
		this.name = tempName;
		// Set the description to the default
		this.desc = "You see nothing.";
		// Set the flags
		this.flags = "E";
		// Set the locks
		this.locks = "";
		// Set the location
		this.location = tempLoc;
		// Set the destination
		this.destination = tempDestination;
		// set messages to clear
		this.succMsg = "";  // Success Message
		this.osuccMsg = ""; // OSuccess Message
		this.failMsg = "";  // Failure Message
		this.ofailMsg = ""; // OFailure Message
	}
	
	public void setDest(int newDestination) {
		this.destination = newDestination;
	}

	public int getDest()
	{
		return destination;
	}
	
	public ArrayList<String> getAliases() {
		return this.aliases;
	}

	public void setMessage(String name, String newMsg) {
		if(name.toLowerCase().equals("succmsg") == true) { this.succMsg = newMsg; }
		else if(name.toLowerCase().equals("osuccmsg") == true) { this.osuccMsg = newMsg; }
		else if(name.toLowerCase().equals("failmsg") == true) { this.failMsg = newMsg; }
		else if (name.toLowerCase().equals("ofailmsg") == true) { this.ofailMsg = newMsg; }
	}

	@Override
	public void lock() {
		if(this.eType == ExitType.DOOR) {
			this.isLocked = true;
		}
	}

	@Override
	public void unlock() {
		if(this.eType == ExitType.DOOR) {
			this.isLocked = false;
		}
	}

	@Override
	public boolean isLocked() {
		return this.isLocked;
	}
	
	public void setExitType(ExitType newType) {
		this.eType = newType;
	}
	
	public ExitType getExitType() {
		return this.eType;
	}

	public String toDB() {
		String[] output = new String[7];
		output[0] = Utils.str(this.getDBRef());     // exit database reference number
		output[1] = this.getName();                 // exit name
		output[2] = this.getFlags();                // exit flags
		output[3] = this.getDesc();                 // exit description
		output[4] = Utils.str(this.getLocation());  // exit location (a.k.a source)
		output[5] = Utils.str(this.getDest());      // exit destination
		output[6] = Utils.str(this.eType.ordinal()); // exit type
		return Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		return "";
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}