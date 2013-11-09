package mud;

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
	private String name;
	private Subrace sub;
	private int id;
	private Integer[] statAdj;  // stats adjustments pertaining to a particular race
	private boolean restricted; // is the race restricted (not available from normal selection)
	private boolean canFly;     // is this race capable of flight
	
	public Race(String name, int id, boolean restricted) {
		this(name, id, new Integer[] { 0, 0, 0, 0 , 0, 0 }, restricted);
	}
	
	public Race(String name, int id, boolean restricted, boolean canFly) {
		this(name, id, new Integer[] { 0, 0, 0, 0 , 0, 0 }, restricted, canFly);
	}

	public Race(String name, int id, Integer[] statAdj, boolean restricted) {
		this(name, id, statAdj, restricted, false);
	}
	
	public Race(String name, int id, Integer[] statAdj, boolean restricted, boolean canFly) {
		this.name = name;
		this.id = id;
		this.statAdj = statAdj;
		this.restricted = restricted;
		this.canFly = canFly;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setSubrace(Subrace sub) {
		this.sub = sub;
	}
	
	public Subrace getSubrace() {
		return this.sub;
	}
	
	public int getId() {
		return this.id;
	}

	public Integer[] getStatAdjust() {
		return this.statAdj;
	}

	public boolean isRestricted() {
		return this.restricted;
	}
	
	public boolean canFly() {
		return this.canFly;
	}

	public String toString() {
		if (sub == null) {
			return this.name;
		}
		else {
			return this.sub.name;
		}
	}
	
	public class Subrace {
		public Race parentRace;
		public String name;     // name of the subrace
		public String alt;      // common alternate name for the subrace

		public Subrace(Race pRace, String name, String alt) {
			this.parentRace = pRace;
			this.name = name;
			this.alt = alt;
		}
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