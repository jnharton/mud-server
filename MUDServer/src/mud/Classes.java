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

public enum Classes {
	NONE("None", "NON", 0, "", false, false, "red"),                 // ANY
	ADEPT("Adept", "ADP", 12, "d6", true, false, "green"),           // NPC Class
	ARISTOCRAT("Aristocrat", "ARC", 13, "d8", true, false, "green"), // NPC Class
	BARBARIAN("Barbarian", "BAR", 1, "d12", false, false, "red"),    // PC Class
	BARD("Bard", "BRD", 2, "d6", false, true, "yellow"),             // PC Class
	COMMONER("Commoner", "COM", 14, "d4", true, false, "green"),     // NPC Class
	CLERIC("Cleric", "CLR", 3, "d8", false, true, "yellow"),         // PC Class
	DRUID("Druid", "DRD", 4, "d8", false, true, "yellow"),           // PC Class
	EXPERT("Expert", "EXP", 15, "d6", true, false, "green"),         // NPC Class
	FIGHTER("Fighter", "FTR", 5, "d10", false, false, "red"),        // PC Class
	MONK("Monk", "MON", 6, "d8", false, false, "red"),               // PC Class
	PALADIN("Paladin", "PAL", 7, "d10", false, true, "yellow"),      // PC Class
	RANGER("Ranger", "RGR", 8, "d8", false, true, "yellow"),         // PC Class
	ROGUE("Rogue", "ROG", 9, "d6", false, false, "red"),             // PC Class
	SORCERER("Sorcerer", "SOR", 10, "d4", false, true, "yellow"),    // PC Class
	WARRIOR("Warrior", "WAR", 16, "d8", true, false, "green"),       // NPC Class
	WIZARD("Wizard", "WIZ", 11, "d4", false, true, "yellow");        // PC Class

	private String name;
	private String abrv;
	private int id;
	private String hit_dice;
	private boolean npc;
	private boolean caster;
	private String color;
	
	/**
	 * The constructor for a enum class that represent D&D-like Player Classes
	 * 
	 * @param name the name of the class
	 * @param id the unique numerical id of the class (essentially meaningless)
	 * @param hit_dice the hit dice (D&D multi-sided dice, in multiples of 2)
	 * @param npc a boolean value indicating whether the class is an NPC class
	 * @param caster a boolean value indicating whether the class is a caster (spell-casting) class
	 * @param color the name of an ansi color used in some certain circumstances when printing the class abbreviation
	 */
	private Classes(String name, String abrv, int id, String hit_dice, boolean npc, boolean caster, String color) {
		this.name = name;
		this.abrv = abrv;
		this.id = id;
		this.hit_dice = hit_dice;
		this.npc = npc;
		this.caster = caster;
		this.color = color;
	}
	
	/**
	 * 
	 * @return String the name of the class
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * 
	 * @return String hit dice string for the class
	 */
	public String getHDString() { // HD = hit dice
		return this.hit_dice;
	}
	
	/**
	 * 
	 * @return Integer hit dice integer for the class (the number of sides the class' hit dice has)
	 */
	public Integer getHD() {
		Integer i = Integer.parseInt(this.hit_dice.substring(1,-1));
		return i;
	}
	
	public String getAbrv() {
		return this.abrv;
	}
	
	/**
	 * 
	 * @return int the 'id' of the class, which is just a meaningless number representing it
	 */
	public int getId() {
		return this.id;
	}
	
	public static Classes getClass(int id) {
		if(Classes.BARBARIAN.getId() == id) { return Classes.BARBARIAN; }
		else if(Classes.BARD.getId() == id) { return Classes.BARD; }
		else if(Classes.CLERIC.getId() == id) { return Classes.CLERIC; }
		else if(Classes.DRUID.getId() == id) { return Classes.DRUID; }
		else if(Classes.FIGHTER.getId() == id) { return Classes.FIGHTER; }
		else if(Classes.MONK.getId() == id) { return Classes.MONK; }
		else if(Classes.PALADIN.getId() == id) { return Classes.PALADIN; }
		else if(Classes.RANGER.getId() == id) { return Classes.RANGER; }
		else if(Classes.ROGUE.getId() == id) { return Classes.ROGUE; }
		else if(Classes.SORCERER.getId() == id) { return Classes.SORCERER; }
		else if(Classes.WARRIOR.getId() == id) { return Classes.WARRIOR; }
		else if(Classes.WIZARD.getId() == id) { return Classes.WIZARD; }
		else if(Classes.NONE.getId() == id) { return Classes.NONE; }
		else { return null; }
	}
	
	/**
	 * 
	 * @return boolean true/false value indicates whether a class is an NPC class or not.
	 */
	public boolean isNPC() {
		return this.npc;
	}
	
	/**
	 * 
	 * @return boolean true/false value indicates whether a class is a caster (spell-casting) class or not.
	 */
	public boolean isCaster() {
		return this.caster;
	}
	
	public String getColor() {
		return this.color;
	}
	
	public String toString() {
		return this.name;
	}
}