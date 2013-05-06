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
	public static final Race DWARF = new Race("Dwarf", 3, new Integer[]{ 0, 0, 2, 0, -2, 0 }, false);
	public static final Race ELF = new Race("Elf", 1, new Integer[]{ 0, 2, -2, 0, 0, 0 }, false);
	public static final Race GNOME = new Race("Gnome", 4, new Integer[]{ -2, 0, 2, 0, 0, 0 }, false);
	public static final Race HALF_ELF = new Race("Half-Elf", 6, true);
	public static final Race HALF_ORC = new Race("Half-Orc", 11, new Integer[]{ 2, 0, 0, -2, -2, 0 }, true);
	public static final Race HALFLING = new Race("Halfling", 12, true);
	public static final Race HUMAN = new Race("Human", 2, false);
	public static final Race ORC = new Race("Orc", 5, false);
	public static final Race ILLITHID = new Race("Illithid", 7, true);
	public static final Race KOBOLD = new Race("Kobold", 8, true);
	public static final Race NONE = new Race("None", 9, true);
	public static final Race UNKNOWN = new Race("Unknown", 10, true);

	/*
	 * Strength
	 * Dexterity
	 * Constitution
	 * Intelligence
	 * Charisma
	 * Wisdom
	 */
	
	static private final Race[] myValues = {
		DRAGON, ELF, HUMAN, DWARF, GNOME, ORC, HALF_ELF, ILLITHID, KOBOLD, NONE, UNKNOWN, HALF_ORC, HALFLING
	};

	public static Race getRace(int id) {
		return myValues[id];
	}
}