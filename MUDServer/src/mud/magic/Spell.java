package mud.magic;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import mud.Effect;
import mud.magic.Spell.SpellType;

/**
 * class to implement Dungeons and Dragons spells
 * 
 * @author Jeremy
 *
 */
public class Spell
{
	public enum Category { NONE }
	//public enum RangeType {NORMAL, PER_LEVEL, PERSONAL, TOUCH, AREA };
	public enum RangeType {
		STD("Standard", 0, -1, -1),         // ?
		PERSONAL("Personal", 0, 0, -1),     // ?
		TOUCH("Touch", 0, 0, 0),            // ?
		CLOSE("Close", 25, 5, 2),           // close range (25ft.), increment of 5ft./2 caster levels
		MEDIUM("Medium", 100, 10, 1),       // medium range (100ft.), increment of 10ft./caster level
		LONG("Long", 400, 40, 1),           // long range (400ft.), increment of 40ft./caster level
		UNLIMITED("Unlimited", -1, -1, -1); // no limit on range, no increment, not affected by caster level
		
		private String typeName;
		private int range, rangeIncrement, casterLevels;
		
		RangeType(String typename, int range, int rInc, int casterlevels) {
			this.typeName = typename;
			this.range = range;
			this.rangeIncrement = rInc;
			this.casterLevels = casterlevels;
		}
	}
	
	public enum RangeClass { CONICAL, LINEAR, PLANAR, SPHERICAL }
	public enum SpellClass { NONE, CLERIC, DRUID, PALADIN, RANGER, SORCERER, WIZARD };
	public enum SpellType { ARCANE, DIVINE };
	
	protected int spellLevel;
	protected RangeType rangeT;
	protected RangeClass rangeC;
	protected int castTime; // time taken to cast the spell
	protected int duration; // duration of the spell 
	protected int range;    // range of the spell in feet (spherical, radius length?)
	protected School school;
	protected Category cat;
	protected List<SpellClass> sc;
	protected SpellType sType;
	protected String name, castMsg;
	protected ArrayList<Effect> effects;  // an arraylist of spell effects (e.g. invisibility, acid resistance)
	protected HashMap<String, Reagent> reagents; // a matched set of reagent:quantity spell requirements?

	protected int manaCost = 5;
	
	public Spell() {
		effects = new ArrayList<Effect>();
		reagents = new HashMap<String, Reagent>();
		sc = new LinkedList<SpellClass>();
	}

	public Spell(String tName, String tSchool, String tCastMsg, ArrayList<Effect> tEffects)
	{
        this(tName, tSchool, tCastMsg, tEffects, new HashMap<String, Reagent>());
	}

	public Spell(String tName, String tSchool, String tCastMsg, ArrayList<Effect> tEffects, HashMap<String, Reagent> tReagents)
	{
        this(tName, tSchool, tCastMsg, SpellType.ARCANE, tEffects, tReagents);
	}

	public Spell(String tName, String tSchool, String tCastMsg, SpellType sType, ArrayList<Effect> tEffects, HashMap<String, Reagent> tReagents)
	{
        this(tName, tSchool, sType, Spell.Category.NONE, SpellClass.NONE, tCastMsg, tEffects, tReagents);
	}

	//Spell spell = new Spell("Teleport", School.CONJURATION, "", Spell.Category.NONE, Spell.SpellClass.WIZARD, 5, "You cast teleport.", null, null);
    public Spell(String tName, String tSchool, SpellType sType, Category tCategory, SpellClass tSpellClass, String tCastMsg, ArrayList<Effect> tEffects, HashMap<String, Reagent> tReagents)
    {
        this(tName, tSchool, sType, tCategory, tSpellClass, 1, tCastMsg, tEffects, tReagents);
    }

	//Spell spell = new Spell("Teleport", School.CONJURATION, "", Spell.Category.NONE, Spell.SpellClass.WIZARD, 5, "You cast teleport.", null, null);
	public Spell(String tName, String tSchool, SpellType sType, Category tCategory, SpellClass tSpellClass, int tLevel, String tCastMsg, ArrayList<Effect> tEffects, HashMap<String, Reagent> tReagents)
	{
		this();
		this.name = tName;
		this.school = getSchool(tSchool);
		this.sType = sType;
		this.spellLevel = tLevel;
		//this.cat = tCategory;
		this.sc = new ArrayList<SpellClass>();
		if( tSpellClass != SpellClass.NONE) { this.sc.add(tSpellClass); };
		this.castMsg = tCastMsg;
		this.effects = tEffects;
		this.reagents = tReagents;
	}
	
	public void setLevel(int tLevel) {
		this.spellLevel = tLevel;
	}
	
	/**
	 * compare this to the player's level to figure out whether they can use it
	 */
	public int getLevel() {
		return this.spellLevel;
	}
	
	public String getCastMessage() {
		return this.castMsg;
	}
	
	public void setCastMessage(String newCastMsg) {
		this.castMsg = newCastMsg;
	}
	
	public List<Effect> getEffects() {
		return this.effects;
	}
	
	public void setManaCost(int manaCost) {
		this.manaCost = manaCost;
	}

	public int getManaCost() {
		return this.manaCost;
	}
	
	public void setSpellClass(SpellClass newSpellClass) {
		if( !this.sc.contains(newSpellClass) ) {
			this.sc.add(newSpellClass);
		}
	}

	/**
	 * compare this to the player's class to figure out whether they can use it
	 */
	public List<SpellClass> getSpellClasses() {
		return this.sc;
	}
	
	public SpellClass getSpellClass(int index) {
		return this.sc.get(index);
	}

	public School getSchool() {
		return this.school;
	}

	public static School getSchool(final String schoolName) {
		final String sN = schoolName.toLowerCase();
		
		if (sN.equals("abjuration") || sN.equals("abj")) { return School.ABJURATION; }
		else if (sN.equals("conjuration") || sN.equals("con")) { return School.CONJURATION; }
		else if (sN.equals("divination") || sN.equals("div")) { return School.DIVINATION; }
		else if (sN.equals("enchantment") || sN.equals("enc")) { return School.ENCHANTMENT; }
		else if (sN.equals("evocation") || sN.equals("evo")) { return School.EVOCATION; }
		else if (sN.equals("illusion") || sN.equals("ill")) { return School.ILLUSION; }
		else if (sN.equals("invocation") || sN.equals("inv")) { return School.INVOCATION; }
		else if (sN.equals("transmutation") || sN.equals("tra")) { return School.TRANSMUTATION; }
		else if (sN.equals("necromancy") || sN.equals("nec")) { return School.NECROMANCY; }
		else { return School.OTHER; }
	}
	
	public void setSchool(School s) {
		this.school = s;
	}

	public void setSchool(final String schoolName) {
		if (schoolName.toLowerCase().equals("abjuration")) { this.school = School.ABJURATION; }
		else if (schoolName.toLowerCase().equals("conjuration")) { this.school = School.CONJURATION; }
		else if (schoolName.toLowerCase().equals("divination")) { this.school = School.DIVINATION; }
		else if (schoolName.toLowerCase().equals("enchantment")) { this.school = School.ENCHANTMENT; }
		else if (schoolName.toLowerCase().equals("evocation")) { this.school = School.EVOCATION; }
		else if (schoolName.toLowerCase().equals("illusion")) { this.school = School.ILLUSION; }
		else if (schoolName.toLowerCase().equals("invocation")) { this.school = School.INVOCATION; }
		else if (schoolName.toLowerCase().equals("transmutation")) { this.school = School.TRANSMUTATION; }
		else if (schoolName.toLowerCase().equals("necromancy")) { this.school = School.NECROMANCY; }
		else { this.school = School.OTHER; }
	}
	
	public void setName(String spellName) {
		this.name = spellName;
	}

	public String getName() {
		return this.name;
	}
	
	public HashMap<String, Reagent> getReagents() {
		return this.reagents;
	}

    @Override
	public String toString() {
		return this.name;
	}

    @Override
	public boolean equals(Object obj) {
		return obj instanceof Spell && ((Spell) obj).name.equals(name);
	}

    @Override
	public int hashCode() {
		return name.hashCode();
	}

}