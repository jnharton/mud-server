package mud.rulesets.d20;

import mud.game.Race;


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

/* Stat modifiers used from Hypertext d20SRD */

public final class Races {
	public static final Race DRAGON = new Race(null, "Dragon", 0, true, true);
	public static final Race DWARF = new Race(null, "Dwarf", 3, false, false, new Integer[]{ 0, 0, 2, 0, -2, 0 });
	public static final Race ELF = new Race(null, "Elf", 1, false, false, new Integer[]{ 0, 2, -2, 0, 0, 0 });
	public static final Race GNOME = new Race(null, "Gnome", 4, false, false, new Integer[]{ -2, 0, 2, 0, 0, 0 });
	public static final Race HALF_ELF = new Race(null, "Half-Elf", 6, false, true);
	public static final Race HALF_ORC = new Race(null, "Half-Orc", 10, false, true, new Integer[]{ 2, 0, 0, -2, -2, 0 });
	public static final Race HALFLING = new Race(null, "Halfling", 11, false, true);
	public static final Race HUMAN = new Race(null, "Human", 2, false, false);
	public static final Race ORC = new Race(null, "Orc", 5, false, false);
	public static final Race KOBOLD = new Race(null, "Kobold", 7, false, true, new Integer[] { -4, 2, -2, 0, 0, 0 });
	public static final Race NONE = new Race(null, "None", 8, false, true);
	public static final Race UNKNOWN = new Race(null, "Unknown", 9, false, true);

	/*
	 * Strength
	 * Dexterity
	 * Constitution
	 * Intelligence
	 * Charisma
	 * Wisdom
	 */
	
	static private final Race[] myValues = {
		DRAGON, ELF, HUMAN, DWARF, GNOME, ORC, HALF_ELF, KOBOLD, NONE, UNKNOWN, HALF_ORC, HALFLING
	};

	public static Race getRace(int id) {
		return myValues[id];
	}
	
	public static Race getRace(String name) {
		Race race = null;
		
		for(final Race r : myValues) {
			if( r.getName().equalsIgnoreCase(name) ) {
				race = r;
			}
		}
		
		return race;
	}
}