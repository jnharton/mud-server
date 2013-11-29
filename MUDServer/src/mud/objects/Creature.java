package mud.objects;

import mud.MUDObject;
import mud.Race;
import mud.Races;
import mud.ObjectFlag;
import mud.Skills;
import mud.TypeFlag;

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
 * Creature class
 * 
 * This class is for use in representing creatures of non-player races, such
 * as wolves, cats, dogs, horses, deer, bears, etc. I'm not sure if dragons will fit in
 * here yet, because I'd like to think that it could be interesting to play the game
 * as a dragon.
 * 
 * @author Jeremy
 */
public class Creature extends MUDObject {

	// type flag = C
	String race = "kobold";
	
	private boolean ridable = false; // can this creature be ridden (default: false)
	
	int hp = 10;
	int maxhp = 10;
	
	public Creature() {
	}
	
	/**
	 * Copy Constructor
	 */
	public Creature(final Creature template) {
		super(-1);
		this.type = TypeFlag.OBJECT;
		
		this.name = template.name;
		this.flags = template.flags;
		this.locks = template.locks;
		this.desc = template.desc;
		this.location = template.location;
		
		this.ridable = template.ridable;
	}
	
	public Creature(int dbref, String race, String name, String desc) {
		super(dbref);
		this.type = TypeFlag.OBJECT;
		this.name = name;
		this.flags = null;
		this.locks = "";
		this.desc = desc;
		this.location = 8;
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * @param tempDBRef
	 * @param tempName
	 * @param tempFlags
	 * @param tempDesc
	 * @param tempLoc
	 */
	public Creature(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc) {
		super(tempDBRef);
		this.type = TypeFlag.OBJECT;
		
		this.name = tempName;
		this.flags = tempFlags;
		this.locks = ""; // should take tempLocks argument  
		this.desc = tempDesc;
		this.location = tempLoc;
	}
	
	public void setHP(int change) {
		this.hp += change;
	}
	
	public int getHP() {
		return this.hp;
	}
	
	/**
	 * Translate the persistent aspects of the creature into the string
	 * format used by the database
	 * 
	 * ex.
	 * 250#Kobold#C#A filthy kobold.#247#*#*#*
	 * 250 - dbref
	 * kobold - name
	 * C - type (creature)
	 * A filthy kobold - description
	 * 247 - location
	 * * - denotes an unused field
	 */
	public String toDB() {
		String[] output = new String[10];    // used to be 8
		output[0] = this.getDBRef() + "";    // creature database reference number
		output[1] = this.getName();          // creature name
		output[2] = this.getFlagsAsString(); // creature flags
		output[3] = this.getDesc();          // creature description
		output[4] = this.getLocation() + ""; // creature location
		output[5] = "*";                     //
		output[6] = "*";                     //
		output[7] = "*";                     //
		output[8] = "*";                     //
		
		return Utils.join(output, "#");
	}
	
	public void setRace(Race race) {
		this.race = race.getName();
	}
	
	public Race getRace() {
		return Races.getRace(this.race);
	}
	
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Creature fromJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}