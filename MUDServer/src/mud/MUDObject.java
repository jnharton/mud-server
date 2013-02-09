package mud;

/*
  Copyright (c) 2012 Jeremy N. Harton
  
  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
  persons to whom the Software is furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
  Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.EnumSet;

import mud.utils.Utils;

/**
 * MUDObject Class
 * 
 * @author Jeremy N. Harton
 * 
 */
public abstract class MUDObject {
	public static MUDServer parent;

	/* object data - persistent */
	private int dbref = 0;       // database reference number (an index of sorts), private so nothing can change it
	protected String name = "";  // object name
	protected String desc = "";  // object description
	protected EnumSet<ObjectFlag> flags = EnumSet.noneOf(ObjectFlag.class);
	protected Object locks = ""; // object locks
	protected int location = 0;  // object location
	
	protected LinkedHashMap<String, Object> props = new LinkedHashMap<String, Object>(1, 0.75f);
	
	protected int owner = 0; // who owns the object (dbref of owner)

	protected ArrayList<Effect> effects = new ArrayList<Effect>(); // Effects set on the object

	public Point coord = new Point(0, 0); // the coordinate point specifying the location on a cartesian plain within a room
	public Point pos = new Point(0, 0);   //
	
	/* object state - transient? */
	public boolean Edit_Ok = true; // is this object allowed to be edited
	
	/**
	 * Parent constructor for no argument subclass constructors
	 */
	public MUDObject() {}
	
	/**
	 * Parent constructor for subclasses. Allows you to initialize a subclass with
	 * a dbref number without having issues with the dbref being private.
	 * 
	 * @param tempDBRef
	 */
	public MUDObject(int tempDBRef)
	{
		this.dbref = tempDBRef;
	}

	public MUDObject(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc) {
		this.dbref = tempDBRef;
		this.name = tempName;
		this.flags = tempFlags;
		this.desc = tempDesc;
		this.location = tempLoc;
	}
	
	/**
	 * Get the name of the MUDObject.
	 * 
	 * @return the name of the MUDObject (String)
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set the name of the MUDObject;
	 * 
	 * @param newName the new name for the MUDObject
	 * @return true if succeeded, false if failed
	 */
	public boolean setName(String newName) {
		this.name = newName;
		return true;
	}
	
	/**
	 * Get the description of the MUDObject
	 * 
	 * @return the description of the MUDObject (String)
	 */
	public String getDesc()
	{
		return this.desc;
	}
	
	/**
	 * Set the description of the MUDObject
	 * 
	 * @param newDescription the new description for the MUDObject
	 */
	public void setDesc(String newDescription) {
		this.desc = newDescription;
	}

	/**
	 * Get the flags set on the MUDObject
	 * 
	 * @return the flags set on the MUDObject (String)
	 */
	public EnumSet<ObjectFlag> getFlags()
	{
		return this.flags;
	}

    public String getFlagsAsString()
	{
        final StringBuilder buf = new StringBuilder();
        for (final ObjectFlag f : flags) {
            buf.append(f.toString().charAt(0));
        }
		return buf.toString();
	}

	/**
	 * Set the flags set on the MUDObject
	 * 
	 * @param tempFlags the new flags for the MUDObject (String)
	 */
	public void setFlags(final EnumSet<ObjectFlag> tempFlags)
	{
        this.flags = tempFlags;
	}

	public void removeFlags(final EnumSet<ObjectFlag> tempFlags)
	{
        this.flags.removeAll(tempFlags);
	}

	/**
	 * unimplemented
	 * 
	 * @return
	 */
	public String getLocks() {
		return (String) this.locks;
	}

	/**
	 * unimplemented
	 * 
	 * @param newLocks
	 */
	public void setLocks(String newLocks) {
	}

	public void lock(String tempLock, String tempLockString)
	{
		// locks = home:here|teleport:no
		this.locks = locks + "|" + tempLock + ":" + tempLockString;
	}

	public void unlock() {
	}
	
	/**
	 * Get database reference number of MUDObject
	 * 
	 * @return
	 */
	public int getDBRef()
	{
		return this.dbref;
	}
	
	/**
	 * Set database reference number of MUDObject
	 * 
	 * @param newDBRef
	 */
	public void setDBRef(int newDBRef)
	{
		this.dbref = newDBRef;
	}

	// arg -- int playerDBREF
	public int getLocation()
	{
		return this.location;
	}

	// set the player object's location
	public void setLocation(int newLocation)
	{
		this.location = newLocation;
	}

	public LinkedHashMap<String, Object> getProps() {
		return this.props;
	}

	//public abstract void examine();

	//public abstract ArrayList<String> look();

	// set an effect (string, the simple version)
	public void addEffect(String effect)
	{
		effects.add(new Effect(effect));
	}

	// set an effect (effect, the more complicated version)
	public void addEffect(Effect effect)
	{
		effects.add(effect);
	}

	public boolean hasEffect(String arg)
	{
		for (int e = 0; e < this.effects.size(); e++)
		{
			if (this.effects.get(e).toString().equals(arg))
			{
				return true;
			}
		}
		return false;
	}

	// get effect
	public Effect getEffect(int id) {
		return this.effects.get(id);
	}

	// get effect
	public ArrayList<Effect> getEffects() {
		return this.effects;
	}

	// get effects
	public String listEffects()
	{
		String effectList = "Effects: ";
		String sep = ", ";
		for (int e = 0; e < this.effects.size(); e++)
		{
			if (this.effects.size() - 1 == e)
			{
				sep = "";
			}
			effectList += this.effects.get(e) + sep;
		}
		return effectList;
	}

	// clear an effect
	public void removeEffect(String tEffect)
	{
		Effect effect;

		for (int e = 0; e < this.effects.size(); e++)
		{
			effect = this.effects.get(e);
			if (effect.getName().equals(tEffect)) {
				this.effects.remove(effect);
			}
		}
	}

	// clear all effects
	public void removeEffects()
	{
		this.effects.clear();
	}
	
	/*
	 * Coordinate System related methods 
	 */
	
	public int getXCoord() {
		return this.coord.getX();
	}
	
	public void setXCoord(int newXCoord) {
		this.coord.setX(newXCoord);
	}
	
	public int getYCoord() {
		return this.coord.getX();
	}
	
	public void setYCoord(int newYCoord) {
		this.coord.setY(newYCoord);
	}
	
	public Point getCoordinates() {
		return this.coord;
	}
	
	public void setCoordinates(int x, int y) {
		this.coord.setX(x);
		this.coord.setY(y);
	}

	public abstract String toDB();
	
	public abstract String toJSON();

}
