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
	private Integer[] statAdj;
	private boolean restricted;
	
	public Race(String name, int id, boolean restricted) {
		this(name, id, new Integer[] { 0, 0, 0, 0 , 0, 0 }, restricted);
	}

	public Race(String name, int id, Integer[] statAdj, boolean restricted) {
		this.name = name;
		this.id = id;
		this.statAdj = statAdj;
		this.restricted = restricted;
	}
	
	public int getId() {
		return this.id;
	}

	public Integer[] getStatAdjust() {
		return this.statAdj;
	}
	
	public Subrace getSubrace() {
		return this.sub;
	}

	public void setSubrace(Subrace sub) {
		this.sub = sub;
	}

	public String getName() {
		return this.name;
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
	
	public class Subrace {
		public Race parentRace;
		public String name;
		public String alt;

		Subrace(Race pRace, String name, String alt) {
			this.parentRace = pRace;
			this.name = name;
			this.alt = alt;
		}
	}
	
	public final class Subraces {
		Subrace DARK_ELF = new Subrace(Races.ELF, "Dark Elf", "Drow");
		Subrace MOON_ELF = new Subrace(Races.ELF, "Moon Elf", "Gray Elf");
		Subrace SEA_ELF = new Subrace(Races.ELF, "Sea Elf", "");
		Subrace SUN_ELF = new Subrace(Races.ELF, "Sun Elf", "Gold Elf");
		Subrace WILD_ELF = new Subrace(Races.ELF, "Wild Elf", "");
		Subrace WOOD_ELF = new Subrace(Races.ELF, "Wood Elf", "");
	}
}