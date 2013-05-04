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

public final class Races {
	public static final Race DRAGON = new Race("Dragon", 0, true);
	public static final Race DROW = new Race("Drow", 2, true);
	public static final Race DWARF = new Race("Dwarf", 4, new Integer[]{ 0, 0, 2, 0, -2, 0 }, false);
	public static final Race ELF = new Race("Elf", 1, new Integer[]{ 0, 2, -2, 0, 0, 0 }, false);
	public static final Race GNOME = new Race("Gnome", 5, new Integer[]{ -2, 0, 2, 0, 0, 0 }, false);
	public static final Race HALF_ELF = new Race("Half-Elf", 7, true);
	public static final Race HALF_ORC = new Race("Half-Orc", 12, new Integer[]{ 2, 0, 0, -2, -2, 0 }, true);
	public static final Race HALFLING = new Race("Halfling", 13, true);
	public static final Race HUMAN = new Race("Human", 3, false);
	public static final Race ORC = new Race("Orc", 6, false);
	public static final Race ILLITHID = new Race("Illithid", 8, true);
	public static final Race KOBOLD = new Race("Kobold", 9, true);
	public static final Race NONE = new Race("None", 10, true);
	public static final Race UNKNOWN = new Race("Unknown", 11, true);

	/*
	 * Strength
	 * Dexterity
	 * Constitution
	 * Intelligence
	 * Charisma
	 * Wisdom
	 */
	
	static private final Race[] myValues = {
		DRAGON, ELF, DROW, HUMAN, DWARF, GNOME, ORC, HALF_ELF, ILLITHID, KOBOLD, NONE, UNKNOWN, HALF_ORC, HALFLING
	};

	public static Race getRace(int id) {
		if (Races.ELF.getId() == id) { return Races.ELF; }
		else if (Races.DROW.getId() == id) { return Races.DROW; }
		else if (Races.HUMAN.getId() == id) { return Races.HUMAN; }
		else if (Races.DWARF.getId() == id) { return Races.DWARF; }
		else if (Races.GNOME.getId() == id) { return Races.GNOME; }
		else if (Races.ORC.getId() == id) { return Races.ORC; }
		else if (Races.NONE.getId() == id) { return Races.NONE; }
		else { return null; }
	}


	public enum Subraces{
		MOON_ELF(Races.ELF, "Moon Elf", "Gray Elf"),
		SEA_ELF(Races.ELF, "Sea Elf", ""),
		SUN_ELF(Races.ELF, "Sun Elf", "Gold Elf"),
		WILD_ELF(Races.ELF, "Wild Elf", ""),
		WOOD_ELF(Races.ELF, "Wood Elf", "");

		public Race parentRace;
		public String name;
		public String alt;

		Subraces(Race r, String name, String alt) {
			this.parentRace = r;
			this.name = name;
			this.alt = alt;
		}
	}
}