package mud.magic;

import java.util.ArrayList;
import java.util.HashMap;

import mud.Effect;

/**
 * class to implement Dungeons and Dragons spells
 * 
 * @author Jeremy
 *
 */
public class Spell
{
	public enum Category { NONE }
	public enum RangeType { NORMAL, PER_LEVEL, PERSONAL, TOUCH, AREA };
	public enum RangeClass { CONICAL, LINEAR, PLANAR, SPHERICAL }
	public enum School {
		ABJURATION("Abjuration"),
		CONJURATION("Conjuration"),
		DIVINATION("Divination"),
		ENCHANTMENT("Enchantment"),
		EVOCATION("Evocation"),
		ILLUSION("Illusion"),
		INVOCATION("Invocation"),
		TRANSMUTATION("Transmutation"),
		NECROMANCY("Necromancy"),
		OTHER("Other");
		
		private String name;
		
		School(String name) {
			this.name = name;
		}
		
		public String toString() {
			return this.name;
		}
	}
	public enum SpellClass { CLERIC, DRUID, PALADIN, RANGER, SORCERER, WIZARD };
	public enum SpellType { ARCANE, DIVINE };
	int spell_level;
	RangeType rangeT;
	RangeClass rangeC;
	int castTime; // time taken to cast the spell
	int duration; // duration of the spell 
	int range;    // range of the spell in feet (spherical, radius length?)
	public School school;
	private Category cat;
	private SpellClass sc;
	private SpellType st;
	public String name, castMsg, type;
	public ArrayList<Effect> effects;  // an arraylist of spell effects (e.g. invisibility, acid resistance)
	HashMap<String, Reagent> reagents; // a matched set of  reagent:quantity spell requirements
	
	public Spell(String tName, String tSchool, String tCastMsg, ArrayList<Effect> tEffects)
	{
		this.name = tName;
		this.school = getSchool(tSchool);
		this.castMsg = tCastMsg;
		this.effects = tEffects;
		this.reagents = null;
	}

	public Spell(String tName, String tSchool, String tCastMsg, ArrayList<Effect> tEffects, HashMap<String, Reagent> tReagents)
	{
		this.name = tName;
		this.school = getSchool(tSchool);
		this.castMsg = tCastMsg;
		this.effects = tEffects;
		this.reagents = tReagents;
	}
	
	public Spell(String tSchool, String tName, String tCastMsg, String tType, ArrayList<Effect> tEffects, HashMap<String, Reagent> tReagents)
	{
		this.name = tName;
		this.school = getSchool(tSchool);
		this.castMsg = tCastMsg;
		this.type = tType;
		this.effects = tEffects;
		this.reagents = tReagents;
	}
	
	//Spell spell = new Spell("Teleport", School.CONJURATION, "", Spell.Category.NONE, Spell.SpellClass.WIZARD, 5, "You cast teleport.", null, null);
		public Spell(String tName, String tSchool, String tType, Category tCategory, SpellClass tSpellClass, String tCastMsg, ArrayList<Effect> tEffects, HashMap<String, Reagent> tReagents)
		{
			this.name = tName;
			this.school = getSchool(tSchool);
			this.type = tType;
			this.spell_level = 0;
			//this.cat = tCategory;
			//this.sc = tSpellClass;
			this.castMsg = tCastMsg;
			this.effects = tEffects;
			this.reagents = tReagents;
		}
	
	//Spell spell = new Spell("Teleport", School.CONJURATION, "", Spell.Category.NONE, Spell.SpellClass.WIZARD, 5, "You cast teleport.", null, null);
	public Spell(String tName, String tSchool, String tType, Category tCategory, SpellClass tSpellClass, int tLevel, String tCastMsg, ArrayList<Effect> tEffects, HashMap<String, Reagent> tReagents)
	{
		this.name = tName;
		this.school = getSchool(tSchool);
		this.type = tType;
		this.spell_level = tLevel;
		//this.cat = tCategory;
		//this.sc = tSpellClass;
		this.castMsg = tCastMsg;
		this.effects = tEffects;
		this.reagents = tReagents;
	}
	
	public void setLevel(int tLevel) {
		this.spell_level = tLevel;
	}
	
	/**
	 * 
	 * @return
	 * compare this to the player's level to figure out whether they can use it
	 */
	public int getLevel() { // returns the integer that represents the spell's level
		return this.spell_level;
	}
	
	/**
	 * 
	 * @return
	 * compare this to the player's class to figure out whether they can use it
	 */
	public int spellClass() {
		return this.sc.ordinal();
	}
	
	public School getSchool() {
		return this.school;
	}
	
	public void setSchool(School s) {
		this.school = s;
	}
	
	public School getSchool(String schoolName) {
		if(schoolName.toLowerCase().equals("abjuration") == true) { return School.ABJURATION; }
		else if(schoolName.toLowerCase().equals("conjuration") == true) { return School.CONJURATION; }
		else if(schoolName.toLowerCase().equals("divination") == true) { return School.DIVINATION; }
		else if(schoolName.toLowerCase().equals("enchantment") == true) { return School.ENCHANTMENT; }
		else if(schoolName.toLowerCase().equals("evocation") == true) { return School.EVOCATION; }
		else if(schoolName.toLowerCase().equals("illusion") == true) { return School.ILLUSION; }
		else if(schoolName.toLowerCase().equals("invocation") == true) { return School.INVOCATION; }
		else if(schoolName.toLowerCase().equals("transmutation") == true) { return School.TRANSMUTATION; }
		else if(schoolName.toLowerCase().equals("necromancy") == true) { return School.NECROMANCY; }
		else { return School.OTHER; }
	}
	
	public void setSchool(String schoolName) {
		if(schoolName.toLowerCase().equals("abjuration") == true) { this.school = School.ABJURATION; }
		else if(schoolName.toLowerCase().equals("conjuration") == true) { this.school = School.CONJURATION; }
		else if(schoolName.toLowerCase().equals("divination") == true) { this.school = School.DIVINATION; }
		else if(schoolName.toLowerCase().equals("enchantment") == true) { this.school = School.ENCHANTMENT; }
		else if(schoolName.toLowerCase().equals("evocation") == true) { this.school = School.EVOCATION; }
		else if(schoolName.toLowerCase().equals("illusion") == true) { this.school = School.ILLUSION; }
		else if(schoolName.toLowerCase().equals("invocation") == true) { this.school = School.INVOCATION; }
		else if(schoolName.toLowerCase().equals("transmutation") == true) { this.school = School.TRANSMUTATION; }
		else if(schoolName.toLowerCase().equals("necromancy") == true) { this.school = School.NECROMANCY; }
		else { this.school = School.OTHER; }
	}

	public String toString() {
		return this.name;
	}
}