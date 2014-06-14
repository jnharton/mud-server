package mud.game;

import java.util.HashMap;

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

public class Classes {
	// Name, Abbreviation (3 chars), ID, hitdice string, npc class?, caster class? color
	public static PClass NONE = new PClass("None", "NON", 0, "", false, false, "red");                 // ANY
	public static PClass ADEPT = new PClass("Adept", "ADP", 12, "d6", true, false, "green");           // NPC Class
	public static PClass ARISTOCRAT = new PClass("Aristocrat", "ARC", 13, "d8", true, false, "green"); // NPC Class
	public static PClass BARBARIAN = new PClass("Barbarian", "BAR", 1, "d12", false, false, "red");    // PC Class
	public static PClass BARD = new PClass("Bard", "BRD", 2, "d6", false, true, "yellow");             // PC Class
	public static PClass COMMONER = new PClass("Commoner", "COM", 14, "d4", true, false, "green");     // NPC Class
	public static PClass CLERIC = new PClass("Cleric", "CLR", 3, "d8", false, true, "yellow");         // PC Class
	public static PClass DRUID = new PClass("Druid", "DRD", 4, "d8", false, true, "yellow");           // PC Class
	public static PClass EXPERT = new PClass("Expert", "EXP", 15, "d6", true, false, "green");         // NPC Class
	public static PClass FIGHTER = new PClass("Fighter", "FTR", 5, "d10", false, false, "red");        // PC Class
	public static PClass MONK = new PClass("Monk", "MON", 6, "d8", false, false, "red");               // PC Class
	public static PClass PALADIN = new PClass("Paladin", "PAL", 7, "d10", false, true, "yellow");      // PC Class
	public static PClass RANGER = new PClass("Ranger", "RGR", 8, "d8", false, true, "yellow");         // PC Class
	public static PClass ROGUE = new PClass("Rogue", "ROG", 9, "d6", false, false, "red");             // PC Class
	public static PClass SORCERER = new PClass("Sorcerer", "SOR", 10, "d4", false, true, "yellow");    // PC Class
	public static PClass WARRIOR = new PClass("Warrior", "WAR", 16, "d8", true, false, "green");       // NPC Class
	public static PClass WIZARD = new PClass("Wizard", "WIZ", 11, "d4", false, true, "yellow");        // PC Class
	
	// green - npc class
	// yellow - spell caster
	// red - melee/ranged fighting
    
	private static final PClass[] myValues = {
		NONE,
		BARBARIAN, BARD, CLERIC, DRUID, FIGHTER, MONK, PALADIN, RANGER, ROGUE, SORCERER, WIZARD,
		ADEPT, ARISTOCRAT, COMMONER, EXPERT, WARRIOR
	};
	
	private static final HashMap<String, PClass> classMap = new HashMap<String, PClass>() {
		{
			put("ADEPT",      Classes.ADEPT);
			put("ARISTOCRAT", Classes.ARISTOCRAT);
			put("BARBARIAN",  Classes.BARBARIAN);
			put("BARD",       Classes.BARD);
			put("COMMONER",   Classes.COMMONER);
			put("CLERIC",     Classes.CLERIC);
			put("DRUID",      Classes.DRUID);
			put("FIGHTER",    Classes.FIGHTER);
			put("MONK",       Classes.MONK);
			put("PALADIN",    Classes.PALADIN);
			put("RANGER",     Classes.RANGER);
			put("ROGUE",      Classes.ROGUE);
			put("SORCEROR",   Classes.SORCERER);
			put("WARRIOR",    Classes.WARRIOR);
			put("WIZARD",     Classes.WIZARD);
		}
	};
	
	public static PClass getClass(int id) {
        return myValues[id];
	}
	
	public static PClass getClass(String className) {
		return classMap.get( className.toUpperCase() );
	}
	
	public static PClass[] getClasses() {
		return myValues;
	}
}