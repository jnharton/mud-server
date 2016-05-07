package mud;

/*
 * Copyright (c) 2012-2015 Jeremy N. Harton
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.EnumSet;
import java.util.List;

import mud.misc.Effect;
import mud.objects.Player;
import mud.utils.MudUtils;
import mud.utils.Point;

/**
 * MUDObject Class
 * 
 * @author Jeremy N. Harton
 * 
 */
public abstract class MUDObject {
	private static final int NUM_PROPS = 10;
	private static final float LOAD_FACT = 0.75f;
	
	/* object data - persistent */
	private Integer dbref;               // database reference number
	protected String name;               // object name
	protected String desc;               // object description
	protected EnumSet<ObjectFlag> flags; // object flags
	protected Integer location;          // object location
	
	protected TypeFlag type;             // object type
	
	protected Player owner;              // who owns the object (dbref of owner? only player can own?)
	
	/* object data - related to game (persistent) */
	protected double weight = 0;        // the weight in whatever units are used of the equippable object
	
	protected Point pos;                 // object's position on a cartesian plane (3D Point)
	
	protected LinkedHashMap<String, Object> properties;

	protected List<Effect> effects;      // Effects set on the object

	/* object state - transient? */
	public boolean Edit_Ok = true;  // is this object allowed to be edited

	/**
	 * Parent constructor for subclasses. Allows you to initialize a subclass with
	 * a dbref number without having issues with the dbref being private.
	 * 
	 * @param tempDBRef
	 */
	protected MUDObject(final Integer tempDBRef)
	{
		this.dbref = tempDBRef;
		
		this.name = "";
		this.desc = "";
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		this.location = -1;
		
		this.type = TypeFlag.OBJECT;
		
		this.owner = null;
		
		this.pos = new Point(0, 0, 0);
		this.properties = new LinkedHashMap<String, Object>(MUDObject.NUM_PROPS, MUDObject.LOAD_FACT);
		this.effects = new ArrayList<Effect>();
	}
	
	/**
	 * object loading constructor?
	 * 
	 * @param tempDBRef
	 * @param tempName
	 * @param tempFlags
	 * @param tempDesc
	 * @param tempLoc
	 */
	protected MUDObject(final Integer tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc) {
		this.dbref = tempDBRef;
		
		this.name = tempName;
		this.desc = tempDesc;
		
		this.type = TypeFlag.OBJECT;
		
		this.flags = tempFlags;
		this.location = tempLoc;
		
		this.owner = null;
		
		this.pos = new Point(0, 0, 0);
		this.properties = new LinkedHashMap<String, Object>(MUDObject.NUM_PROPS, MUDObject.LOAD_FACT);
		this.effects = new ArrayList<Effect>();
	}
	
	/**
	 * MUDObject Copy Constructor
	 * 
	 * exists here for what reason?
	 * 
	 * @param template
	 */
	protected MUDObject(final MUDObject template) {
		this.dbref = -1;
		
		this.name = template.name;
		this.desc = template.desc;
		
		this.type = TypeFlag.OBJECT;
				
		this.flags = template.flags;
		this.location = -1;
		
		this.owner = null;
		
		// TODO restore/duplicate template properties?
		this.pos = new Point(0, 0, 0);
		this.properties = new LinkedHashMap<String, Object>(MUDObject.NUM_PROPS, MUDObject.LOAD_FACT);
		this.effects = new ArrayList<Effect>();
	}

	/**
	 * Get database reference number of MUDObject
	 * 
	 * @return
	 */
	public final Integer getDBRef()
	{
		return this.dbref;
	}

	/**
	 * Set database reference number of MUDObject
	 * 
	 * @param newDBRef
	 */
	public final void setDBRef(final Integer newDBRef)
	{
		this.dbref = newDBRef;
	}
	
	// TODO getName() should be final, but some subclasses do weird stuff with it
	// TODO I'd make setName() final, but Player names shouldn't be able to change arbitrarily
	// TODO you'd think getDesc() might be final too, but Wand (subclass) uses it to show a dynamic description
	
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
	public boolean setName(final String newName) {
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
	public final void setDesc(final String newDescription) {
		this.desc = newDescription;
	}

	/**
	 * Get the flags set on the MUDObject
	 * 
	 * @return the flags set on the MUDObject (String)
	 */
	public final EnumSet<ObjectFlag> getFlags()
	{
		return this.flags;
	}
	
	// should I remove this and make the call inside from the main program?
	/**
	 * Get MUDObject flags as a String, rather than as
	 * a EnumSet<ObjectFlag>
	 * 
	 * @return
	 */
	public final String getFlagsAsString()
	{
		return MudUtils.flagsToString( this.flags );
	}
	
	public final void setFlag(final ObjectFlag flag) {
		this.flags.add(flag);
	}

	/**
	 * Set the flags set on the MUDObject
	 * 
	 * @param tempFlags the new flags for the MUDObject (String)
	 */
	public final void setFlags(final EnumSet<ObjectFlag> tempFlags)
	{
		this.flags = tempFlags;
	}

	/**
	 * Remove a single ObjectFlag from the MUDObject (EnumSet flags)
	 * 
	 * @param flag
	 */
	public final void removeFlag(final ObjectFlag flag) {
		this.flags.remove(flag);
	}

	/**
	 * Remove several ObjectFlags from the MUDObject (EnumSet flags)
	 * 
	 * @param tempFlags
	 */
	public final void removeFlags(final EnumSet<ObjectFlag> tempFlags)
	{
		this.flags.removeAll(tempFlags);
	}

	/**
	 * Get this MUDObject's location.
	 * 
	 * @return int database reference of the object this object is located at/in
	 */
	public final Integer getLocation() {
		return this.location;
	}

	/**
	 * Set this MUDObject's location.<br>
	 * 
	 * <br>
	 * Generally speaking, this should be:<br>
	 * - a positive integer<br>
	 * - a valid database reference in that it refers to an existing object.<br>
	 * 
	 * <br>
	 * appropriate object types whose dbrefs should be used here include:<br>
	 * <b>Players, NPCs, Things, Items, Rooms</b><br>
	 * 
	 * <br>
	 * The sole exception to the principle stated above is using -1 to indicate
	 * that the object is not in the world and/or does not have a valid location.
	 * 
	 * @param newLocation integer (database reference) of another MUDObject
	 */
	public final void setLocation(final Integer newLocation) {
		this.location = newLocation;
	}

	/**
	 * Get the ownership of this object
	 * @return
	 */
	public final Player getOwner() {
		return this.owner;
	}

	/**
	 * Set ownership of this object
	 * 
	 * NOTE: takes a Player parameter to ensure that we don't try and set other
	 * objects as owning objects (only Player's can "own" objects?)
	 * 
	 * @param player
	 */
	public final void setOwner(final Player player) {
		this.owner = player;
	}
	
	public Double getWeight() {
		return this.weight;
	}
	
	public void setWeight(Double newWeight) {
		this.weight = newWeight;
	}
	
	/**
	 * Retrieve the property by it's key
	 * 
	 * @param key property name
	 * @return property value
	 */
	public final Object getProperty(final String key) {
		return this.properties.get(key);
	}
	
	/**
	 * getProperty
	 * 
	 * NOTE: will throw a class cast exception if the object
	 * is not the type we specify
	 * 
	 * @param key
	 * @param c
	 * @return
	 */
	public final <T> T getProperty(final String key, Class<T> c) throws ClassCastException {
		// TODO this is an ugly, ugly kludge...
		if( c.getSimpleName().equals("Boolean") ) {
			final Object obj = this.properties.get(key);
			
			if( obj instanceof String ) {
				final String s = (String) obj;
				
				if( s.equals(":true") ) return (T) c.cast(true);
				else                    return (T) c.cast(false);
			}
		}
		
		return (T) c.cast(this.properties.get(key));
	}
	
	/**
	 * Set a property on this object, where the name
	 * of the property is the key and the value of the
	 * property is the value.
	 * 
	 * @param key   property name
	 * @param value property value
	 */
	public final void setProperty(final String key, final Object value) {
		this.properties.put(key,  value);
	}

	/**
	 * Get the Properties HashMap, a mutable hashmap of strings and objects
	 * that can be used to store "properties" (a.k.a. "props) of/on the object.
	 * 
	 * @return
	 */
	public final LinkedHashMap<String, Object> getProperties() {
		return this.properties;
	}
	
	public final LinkedHashMap<String, Object> getProperties(final String propdir) {
		if( propdir.endsWith("/") ) {
			final LinkedHashMap<String, Object> props = new LinkedHashMap<String, Object>();
			
			for(final String key : properties.keySet()) {
				if( key.startsWith(propdir) ) {
					props.put( key, properties.get(key) );
				}
			}
			
			return props;
		}
		
		return null;
	}

	public final LinkedHashMap<String, Object> getVisualProperties() {
		return getProperties("visual/");
	}
	
	/**
	 * Get Effect
	 * 
	 * @param id
	 * @return
	 */
	public final Effect getEffect(final Integer id) {
		return this.effects.get(id);
	}
	
	/**
	 * Get Effect(s)
	 * 
	 * @return
	 */
	public final List<Effect> getEffects() {
		return this.effects;
	}

	/**
	 * Set an effect
	 * 
	 * <br><br>
	 * This sets an Effect on the object in question.
	 * 
	 * @param effect
	 */
	public final void addEffect(final Effect effect)
	{
		this.effects.add(effect);
	}
	
	/**
	 * Remove an Effect (String)
	 * 
	 * Decides which effect to remove based on whether the String
	 * supplied matches the name of any effects.
	 * 
	 * @param tEffect
	 */
	public final void removeEffect(final String tEffect)
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
	
	/**
	 * Remove an Effect (Effect)
	 * 
	 * Removes the specified effects object from the list of effects.
	 * 
	 * @param effect
	 */
	public final void removeEffect(final Effect effect) {
		this.effects.remove(effect);
	}
	
	/**
	 * Removes all effects from the MUDObject.
	 */
	public final void clearEffects()
	{
		this.effects.clear();
	}

	public final Point getPosition() {
		return this.pos;
	}

	public final void setPosition(int x, int y) {
		setPosition(x, y, 0);
	}

	public final void setPosition(int x, int y, int z) {
		this.pos.setX(x);
		this.pos.setY(y);
		this.pos.setZ(z);
	}
	
	public final void setPosition(final Point pos) {
		if( pos.isType(Point.Type.PT_2D ) ) {
			setPosition(pos.getX(), pos.getY());
		}
		else if( pos.isType(Point.Type.PT_3D ) ) {
			setPosition(pos.getX(), pos.getY(), pos.getZ());
		}
	}
	
	public final TypeFlag getType() {
		return ((this.type != null) ? this.type : TypeFlag.OBJECT);
	}
	
	/* Check Methods */

	/**
	 * Does this MUDObject have the indicated ObjectFlag?
	 * 
	 * @param tempFlag
	 * @return
	 */
	public final boolean hasFlag(ObjectFlag tempFlag) {
		return this.flags.contains(tempFlag);
	}
	
	/**
	 * Does the specified MUDObject have a property of the
	 * specified name (i.e. does that key exist in the properties map)?
	 * 
	 * @param key
	 * @return
	 */
	public final boolean hasProperty(final String key) {
		return this.properties.containsKey(key);
	}
	
	/**
	 * Is an effect with the name specified set on the object?
	 * 
	 * @param arg      name of the effect (String)
	 * @return does the object have this effect, yes/no? (boolean)
	 */
	public final boolean hasEffect(final String arg)
	{
		for(final Effect effect : this.effects) {
			if( effect.getName().equalsIgnoreCase( arg ) ) {
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
	public final boolean hasEffectType(final Effect.Type effectType)
	{
		for (final Effect effect : this.effects) {
			if ( effect.getType() == effectType ) {
				return true;
			}
		}

		return false;
	}

	public final boolean hasEffect(final Effect effect) {
		return this.effects.contains(effect);
	}
	
	/**
	 * Is this MUDObject of the specified type indicated by the TypeFlag?
	 * 
	 * @param type
	 * @return
	 */
	public final boolean isType(final TypeFlag type) {
		return this.type == type;
	}
	
	/**
	 * Does the specified player own this object?
	 * 
	 * @param player
	 * @return
	 */
	public final boolean isOwnedBy(final Player player) {
		// TODO I'd like to think I could compare player objects, but I'm not sure it would work
		return this.owner.getDBRef() == player.getDBRef() ? true : false;
	}
	
	/**
	 * Convert the object to a String based representation
	 * for storage in the plain text database.
	 * 
	 * NOTE: Every subclass of MUDObject should implement
	 * this for itself.
	 * 
	 * @return
	 */
	public abstract String toDB();

	/**
	 * Convert the object to a JSON based representation
	 * for storage in some kind of database system
	 * 
	 * NOTE: Every subclass of MUDObject should implement
	 * this for itself. 
	 * 
	 * @return
	 */
	public abstract String toJSON();

	public abstract MUDObject fromJSON();
	
	@Override
	public String toString() {
		return this.name;
	}
}