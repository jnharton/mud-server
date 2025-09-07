package mud.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

import mud.ObjectFlag;
import mud.MUDObject;
import mud.TypeFlag;
import mud.misc.Script;
import mud.misc.Trigger;
import mud.misc.TriggerType;
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
	protected ThingType thing_type = ThingTypes.NONE;
	
	private Map<String, String> attributes;
	
	public int durability = 100;
	
	// triggers
	private Trigger onUse = null;
	
	public Thing(final String name) {
		super(-1, name, "");
		
		this.type = TypeFlag.THING;
		
		this.attributes = new Hashtable<String, String>();
	}
	
	//public Thing(int tempDBREF, final String name, final String desc) {
	public Thing(final String name, final String desc) {
		super(-1, name, desc);
		
		this.type = TypeFlag.THING;
		
		this.attributes = new Hashtable<String, String>();
	}
	
	/**
	 * <b>Copy Constructor</b><br/><br/>
	 * 
	 * Create a new Thing with the same attributes as a template object
	 * 
	 * @param template
	 */
	public Thing(final Thing template) {
		super(template);
		
		this.type = TypeFlag.THING;
		
		this.thing_type = template.thing_type;
		
		this.weight = template.weight;
		
		if( template.onUse != null ) {
			this.onUse = new Trigger( template.onUse.getScript().getText() );
		}
		
	}
	
	public Thing(final int dbref, final String name, final EnumSet<ObjectFlag> flags, final String description, final int location)
	{
		super(dbref, name, flags, description, location);
		
		this.type = TypeFlag.THING;
		
		this.thing_type = ThingTypes.NONE;
		
		this.attributes = new Hashtable<String, String>();
	}
	
	public ThingType getThingType() {
		return this.thing_type;
	}
	
	public void setThingType(ThingType newType) {
		this.thing_type = newType;
	}
	
	public Map<String, String> getAttributes() {
		return Collections.unmodifiableMap(this.attributes);
	}
	
	public void setScriptOnTrigger(TriggerType type, String script) {
		switch(type) {
		case onUse:
			onUse = new Trigger(script);
			break;
		default:
			break;
		}
	}
	
	public Script getScript(TriggerType type) {
		switch(type) {
		case onUse:
			return onUse.getScript();
		default:
			return null;
		}
	}
	
	@Override
	public Double getWeight() {
		return this.weight;
	}

	public String toDB() {
		String[] output = new String[6];
		
		output[0] = this.getDBRef() + "";           // database reference number
		output[1] = this.getName();                 // name
		output[2] = type + getFlagsAsString();      // flags
		output[3] = this.getDesc();                 // description
		output[4] = this.getLocation() + "";        // location
		
		output[5] = this.thing_type.getId() + "";   // thing type
		
		return Utils.join(output, "#"); 
	}

	public String toString() {
		return this.name;
	}
	
	public Thing getCopy() {
		return new Thing(this);
	}
}
