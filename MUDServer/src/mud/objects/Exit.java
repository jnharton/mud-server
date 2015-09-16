package mud.objects;

import java.util.ArrayList;

import mud.MUDObject;
import mud.ObjectFlag;
import mud.TypeFlag;
import mud.utils.Point;
import mud.utils.Utils;

import java.util.EnumSet;

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
 * Exit Class
 * 
 * Represents an in-game exit or entrance, generally one way
 * and connecting two rooms. A two-directional is possible, but
 * may cause weirdness in other code.
 * 
 * @author Jeremy N. Harton
 * 
 */
public class Exit extends MUDObject
{
	/**
	 * 
	 */

	protected ExitType eType = ExitType.STD;

	protected Integer origin = 0;      // strictly for doors?
	protected Integer destination = 0; //
	
	protected Point p;                 // the spot in the destination room where you arrive
	
	private String succMsg;            // message about successfully using exit
	private String osuccMsg;           // message others see about you using exit
	private String failMsg;            // message about failing to use the exit (locked, etc)
	private String ofailMsg;           // message others see about you failing to use the exit
	
	private ArrayList<String> aliases = new ArrayList<String>();
	
	// empty default constructor for subclasses
	public Exit() {
		super(-1);
		
		this.type  = TypeFlag.EXIT;
		
		// set messages to clear
		this.succMsg = "";  // Success Message
		this.osuccMsg = ""; // OSuccess Message
		this.failMsg = "";  // Failure Message
		this.ofailMsg = ""; // OFailure Message
	}
	
	public Exit(String name, int location, int destination) {
		super(-1);                                     // Set the dbref (database reference)
		
		this.type = TypeFlag.EXIT;                     // Set the type
		
		this.name = name;                              // Set the name
		this.desc = "You see nothing.";                // Set the description to the default
		this.flags = EnumSet.noneOf(ObjectFlag.class); // Set flags
		this.locks = "";                               // Set the locks
		this.location = location;                      // Set the location
		this.destination = destination;                // Set the destination
		
		// set messages to clear
		this.succMsg = "";  // Success Message
		this.osuccMsg = ""; // OSuccess Message
		this.failMsg = "";  // Failure Message
		this.ofailMsg = ""; // OFailure Message
	}
	
	// Object Loading Constructor
	// Name, Flags, Description, DBRef #, Location of exit (DBRef #), destination of exit (DBRef #)
	public Exit(int tempDBRef, String tempName, final EnumSet<ObjectFlag> flagsNotUsed, String tempDesc, int tempLoc, int tempDestination)
	{
		super(tempDBRef);                   // Set the dbref (database reference)
		
		this.type = TypeFlag.EXIT;          // Set the type
		
		this.name = tempName;               // Set the name
		this.desc = tempDesc;               // Set the description to the default
		this.flags = flagsNotUsed;          // Set flags
		this.locks = "";                    // Set the locks
		this.location = tempLoc;            // Set the location
		this.destination = tempDestination; // Set the destination
		
		// set messages to clear
		this.succMsg = "";  // Success Message
		this.osuccMsg = ""; // OSuccess Message
		this.failMsg = "";  // Failure Message
		this.ofailMsg = ""; // OFailure Message
	}
	
	public void setDestination(final Room newDestination) {
		if( newDestination != null ) destination = newDestination.getDBRef();
		else                         destination = -1;
	}
	public void setDestination(int newDestination) {
		this.destination = newDestination;
	}

	public int getDestination()
	{
		return destination;
	}
	
	public void addAlias(final String alias) {
		this.aliases.add(alias);
	}
	
	public ArrayList<String> getAliases() {
		return this.aliases;
	}
	
	public boolean hasAlias(final String alias) {
		return this.aliases.contains( alias );
	}

	public void setMessage(final String name, final String newMsg) {
		final String msgName = name.toLowerCase();
		
		if( msgName.equals("succmsg") )       this.succMsg = newMsg;
		else if( msgName.equals("osuccmsg") ) this.osuccMsg = newMsg;
		else if( msgName.equals("failmsg") )  this.failMsg = newMsg;
		else if( msgName.equals("ofailmsg"))  this.ofailMsg = newMsg;
	}
	
	public String getMessage(final String name) {
		final String msgName = name.toLowerCase();
		
		if( msgName.equals("succmsg") )       return this.succMsg;
		else if( msgName.equals("osuccmsg") ) return this.osuccMsg;
		else if( msgName.equals("failmsg") )  return this.failMsg;
		else if( msgName.equals("ofailmsg"))  return this.ofailMsg;
		else                                  return null;
	}
	
	public void setExitType(final ExitType newType) {
		this.eType = newType;
	}
	
	public ExitType getExitType() {
		return this.eType;
	}

	public String toDB() {
		String[] output = new String[7];
		output[0] = this.getDBRef() + "";                // database reference number
		output[1] = this.getName();                      // name
		output[2] = TypeFlag.asLetter(this.type) + "";   // flags
		output[2] = output[2] + this.getFlagsAsString();
		output[3] = this.getDesc();                      // description
		output[4] = this.getLocation() + "";             // location (a.k.a source)
		output[5] = this.getDestination() + "";          // destination
		output[6] = this.eType.ordinal() + "";           // exit type
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

	@Override
	public Exit fromJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}