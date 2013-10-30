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
import java.util.LinkedHashMap;
import java.util.EnumSet;
import java.util.List;

import mud.objects.Player;
import mud.utils.Point;

/**
 * MUDObject Class
 * 
 * @author Jeremy N. Harton
 * 
 */
public abstract class MUDObject {
	public static MUDServer parent;

	/* object data - persistent */
	private int dbref = 0;                                                  // database reference number
	protected String name = "";                                             // object name
	protected String desc = "";                                             // object description
	protected TypeFlag type = TypeFlag.OBJECT;                              // object type
	protected EnumSet<ObjectFlag> flags = EnumSet.noneOf(ObjectFlag.class); // object flags
	protected Object locks = "";                                            // object locks
	protected int location = 0;                                             // object location
	
	protected LinkedHashMap<String, Object> props = new LinkedHashMap<String, Object>(1, 0.75f);
	
	protected int owner = 0; // who owns the object (dbref of owner)

	protected ArrayList<Effect> effects = new ArrayList<Effect>(); // Effects set on the object

	public Point coord = new Point(0, 0); // object's' location on a cartesian plane within a room?
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
	protected MUDObject(int tempDBRef)
	{
		this.type = TypeFlag.OBJECT;
		this.dbref = tempDBRef;
	}

	protected MUDObject(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc) {
		this.type = TypeFlag.OBJECT;
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
	
	/**
	 * Remove a single ObjectFlag from the MUDObject (EnumSet flags)
	 * 
	 * @param flag
	 */
	public void removeFlag(final ObjectFlag flag) {
		this.flags.remove(flag);
	}
	
	/**
	 * Remove several ObjectFlags from the MUDObject (EnumSet flags)
	 * 
	 * @param tempFlags
	 */
	public void removeFlags(final EnumSet<ObjectFlag> tempFlags)
	{
        this.flags.removeAll(tempFlags);
	}
	
	/**
	 * Does this MUDObject have the indicated ObjectFlag?
	 * 
	 * @param tempFlag
	 * @return
	 */
	public boolean hasFlag(ObjectFlag tempFlag) {
		return this.flags.contains(tempFlag);
	}

	/**
	 * Return the "lock string" set on this object.
	 * 
	 * <br><br>
	 * <b>NOTE:</b> Not currently used for anything.
	 * 
	 * @return
	 */
	public String getLocks() {
		return (String) this.locks;
	}

	/**
	 * Set the "lock string" on this object.
	 * 
	 * <br><br>
	 * <b>NOTE:</b> Not currently used for anything.
	 * 
	 * @param newLocks
	 */
	public void setLocks(String newLocks) {
		this.locks = newLocks;
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
	
	/**
	 * Set this object's location. Generally speaking, this
	 * should be a integer that is a valid database reference
	 * in that it refers to an existing object. Generally speaking,
	 * appropriate object types whose dbrefs should be used here
	 * include: <b>Players, NPCs, Things, Items, Rooms</b>
	 * 
	 * <br><br>
	 * The sole exception to the principle stated above is using -1 to indicate
	 * that the object is not in the world and/or does not have a valid location.
	 * 
	 * @param newLocation integer (database reference) of another MUDObject
	 */
	public void setLocation(int newLocation)
	{
		this.location = newLocation;
	}
	
	/**
	 * Get the Properties HashMap, a mutable hashmap of strings and objects
	 * that can be used to store "properties" (a.k.a. "props) of the object.
	 * 
	 * @return
	 */
	public LinkedHashMap<String, Object> getProps() {
		return this.props;
	}
	
	/**
	 * Set a property on this object, where the name
	 * of the property is the key and the value of the
	 * property is the value.
	 * 
	 * @param key   property name
	 * @param value property value
	 */
	public void setProperty(final String key, final Object value) {
		this.props.put(key,  value);
	}
	
	/**
	 * Retrieve the property by it's key
	 * 
	 * @param key property name
	 * @return property value
	 */
	public Object getProperty(final String key) {
		return this.props.get(key);
	}
	
	//public abstract ArrayList<String> look();

	// set an effect (string, the simple version)
	/**
	 * Set an effect (simple)
	 * 
	 * <br><br>
	 * This sets an Effect on the objects (generally adding it
	 * to the list of current affects). This simple form of the method
	 * makes a brand new Effect object, passing it the input string. It
	 * is very simple and is not useful for complex effects.
	 * 
	 * @param effect
	 */
	public void addEffect(String effect)
	{
		effects.add(new Effect(effect));
	}
	
	/**
	 * Set an effect (complex)
	 * 
	 * <br><br>
	 * This sets an Effect on the object in question. This more complicated
	 * form of the method makes takes a pre-existing Effect object and sets
	 * it on the object . It is very simple and is not useful for complex 
	 * effects (generally adding it to the list of current affects).
	 * 
	 * @param effect
	 */
	public void addEffect(Effect effect)
	{
		effects.add(effect);
	}
	
	/**
	 * Is an effect with the name specified set on the object?
	 * 
	 * @param arg      name of the effect (String)
	 * @return does the object have this effect, yes/no? (boolean)
	 */
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
	
	/**
	 * Is an effect with the specified type set on the object?
	 * 
	 * @param arg      type of the effect (Effect.Type)
	 * @return does the object have this effect, yes/no? (boolean)
	 */
	public boolean hasEffectType(Effect.Type effectType)
	{
		for (Effect effect : this.effects) {
			if ( effect.getType() == effectType ) { return true; }
		}
		
		return false;
	}
	
	public boolean hasEffect(Effect effect) {
		return this.effects.contains(effect);
	}

	// get effect
	public Effect getEffect(int id) {
		return this.effects.get(id);
	}

	// get effect
	public List<Effect> getEffects() {
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
	
	public void removeEffect(Effect effect) {
		this.effects.remove(effect);
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
		return this.coord.getY();
	}
	
	public void setYCoord(int newYCoord) {
		this.coord.setY(newYCoord);
	}
	
	public int getZCoord() {
		return this.coord.getZ();
	}
	
	public void setZCoord(int newZCoord) {
		this.coord.setZ(newZCoord);
	}
	
	public Point getCoordinates() {
		return this.coord;
	}
	
	public void setCoordinates(int x, int y) {
		this.coord.setX(x);
		this.coord.setY(y);
	}
	
	/**
	 * Get the ownership of this object
	 * @return
	 */
	public int getOwner() {
		return this.owner;
	}
	
	/**
	 * Set ownership of this object
	 * 
	 * @param player
	 */
	public void setOwner(Player player) {
		this.owner = player.getDBRef();
	}
	
	/**
	 * Does the specified player own this object?
	 * 
	 * @param player
	 * @return
	 */
	public boolean isOwnedBy(Player player) {
		return this.owner == player.getDBRef() ? true : false;
	}
	
	/**
	 * Convert the object to a String based representation
	 * for storage in the plain text database.
	 * 
	 * @return
	 */
	public abstract String toDB();
	
	/**
	 * Convert the object to a JSON based representation
	 * for storage in some kind of database system
	 * @return
	 */
	public abstract String toJSON();
	
	public String toString() {
		return this.name;
	}
}