package mud.game;

import mud.interfaces.Ruleset;
import mud.rulesets.d20.Races;


/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Race {
	private Ruleset rules;
	
	private String name;
	private Subrace sub;
	private int id;
	private Integer[] statAdj;  // stats adjustments pertaining to a particular race
	private boolean canFly;     // is this race capable of flight
	
	private boolean playable;   // is the race a playable one
	private boolean restricted; // is the race restricted (not available from normal selection)
	
	// no args constructor
	/*public Race() {
		this.name = "noname";
		this.sub = null;
		this.id = -1;
		this.statAdj = new Integer[] { 0, 0, 0, 0, 0, 0 };
		this.restricted = false;
		this.canFly = false;
	}*/
	
	public Race(final Ruleset rs, final String name, final int id, final boolean canFly, final boolean restricted) {
		this(rs, name, id, canFly, restricted, null);
	}
	
	public Race(final Ruleset rs, final String name, final int id, final boolean canFly, final boolean restricted, final Integer[] statAdj) {
		this.rules = rs;
		this.name = name;
		this.id = id;
		this.statAdj = statAdj;
		this.restricted = restricted;
		this.canFly = canFly;
		
		this.playable = true;
	}
	
	public Ruleset getRules() {
		return this.rules;
	}
	
	public String getName() {
		return this.name;
	}
	
	/*public void setSubrace(Subrace sub) {
		this.sub = sub;
	}*/
	
	public Subrace getSubrace() {
		return this.sub;
	}
	
	public int getId() {
		return this.id;
	}

	public Integer[] getStatAdjust() {
		if( this.statAdj == null ) {
			if( this.rules != null ) {
				return new Integer[this.rules.getAbilities().length];
			}
			else return new Integer[0];
		}
		
		return this.statAdj;
	}
	
	public void setStatAdjust(final Integer[] newAdjust) {
		this.statAdj = newAdjust;
	}
	
	public boolean canFly() {
		return this.canFly;
	}
	
	public boolean isPlayable() {
		return this.playable;
	}

	public boolean isRestricted() {
		return this.restricted;
	}
	
	public String toString() {
		if (sub == null) {
			return this.name;
		}
		else {
			return this.sub.name;
		}
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof Race) {
			final Race otherRace = (Race) object;
			
			// TODO fix kludge, two races of the same name are equal according to this
			if(this.getName().equals( otherRace.getName() )) {
				return true;
			}
			
			return false;
		}
		
		return false;
	}
	
	public final class Subraces {
		public final Subrace DARK_ELF = new Subrace(Races.ELF, "Dark Elf", "Drow");
		public final Subrace MOON_ELF = new Subrace(Races.ELF, "Moon Elf", "Gray Elf");
		public final Subrace SEA_ELF = new Subrace(Races.ELF, "Sea Elf", "");
		public final Subrace SUN_ELF = new Subrace(Races.ELF, "Sun Elf", "Gold Elf");
		public final Subrace WILD_ELF = new Subrace(Races.ELF, "Wild Elf", "");
		public final Subrace WOOD_ELF = new Subrace(Races.ELF, "Wood Elf", "");
	}
}