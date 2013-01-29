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

public enum Races {
	DRAGON("Dragon", 0, true),
	DROW("Drow", 2, true),
	DWARF("Dwarf", 4, new Integer[]{ 0, 0, 2, 0, -2, 0 }, false),
	ELF("Elf", 1, new Integer[]{ 0, 2, -2, 0, 0, 0 }, false),
	GNOME("Gnome", 5, new Integer[]{ -2, 0, 2, 0, 0, 0 }, false),
	HALF_ELF("Half-Elf", 7, true),
	HALF_ORC("Half-Orc", 12, new Integer[]{ 2, 0, 0, -2, -2, 0 }, true),
	HALFLING("Halfling", 13, true),
	HUMAN("Human", 3, false),
	ORC("Orc", 6, false),
	ILLITHID("Illithid", 8, true),
	KOBOLD("Kobold", 9, true),
	NONE("None", 10, true),
	UNKNOWN("Unknown", 11, true);
	
	/*
	 * Strength
	 * Dexterity
	 * Constitution
	 * Intelligence
	 * Charisma
	 * Wisdom
	 */
	
	private String name;
	private Subraces sub;
	private int id;
	private Integer[] statAdj;
	private boolean restricted;
	
	private Races(String name, int id, boolean restricted) {
		this.name = name;
		this.id = id;
		this.restricted = restricted;
	}
	
	private Races(String name, int id, Integer[] statAdj, boolean restricted) {
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
	
	public static Races getRace(int id) {
		if (Races.ELF.getId() == id) { return Races.ELF; }
		else if (Races.DROW.getId() == id) { return Races.DROW; }
		else if (Races.HUMAN.getId() == id) { return Races.HUMAN; }
		else if (Races.DWARF.getId() == id) { return Races.DWARF; }
		else if (Races.GNOME.getId() == id) { return Races.GNOME; }
		else if (Races.ORC.getId() == id) { return Races.ORC; }
		else if (Races.NONE.getId() == id) { return Races.NONE; }
		else { return null; }
	}
	
	public Subraces getSubrace() {
		return this.sub;
	}
	
	public void setSubrace(Subraces sub) {
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
	
	public enum Subraces{
		MOON_ELF(Races.ELF, "Moon Elf", "Gray Elf"),
		SEA_ELF(Races.ELF, "Sea Elf", ""),
		SUN_ELF(Races.ELF, "Sun Elf", "Gold Elf"),
		WILD_ELF(Races.ELF, "Wild Elf", ""),
		WOOD_ELF(Races.ELF, "Wood Elf", "");
		
		public Races parentRace;
		public String name;
		public String alt;
		
		Subraces(Races r, String name, String alt) {
			this.parentRace = r;
			this.name = name;
			this.alt = alt;
		}
	}
}