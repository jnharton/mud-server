package mud.rulesets.foe;

import java.util.LinkedHashMap;

import mud.game.Skill;

public final class Skills {
	// Skills
	/* References:
	 * http://fallout.wikia.com/wiki/Skills
	 */

	public static final Skill ACROBATICS      = new Skill("Acrobatics",       0, "AGI", null);
	public static final Skill BARTER          = new Skill("Barter",           1, "INT", null);
	public static final Skill BATTLE_SADDLE   = new Skill("Battle Saddle",    2, "END", null);
	//public static final Skill CRAFT
	// weapons, armor, tech, food?
	//CRAFT_ARMOR
	//CRAFT_WEAPONS
	public static final Skill ENERGY_WEAPONS  = new Skill("Energy Weapons",   5, "INT", null);
	public static final Skill EXPLOSIVES      = new Skill("Explosives",       6, "INT", null);
	public static final Skill FLY             = new Skill("Fly",              7, "AGI", null);
	public static final Skill SMALL_GUNS      = new Skill("Small Guns",       8, "AGI", null);
	public static final Skill BIG_GUNS        = new Skill("Big Guns",         9, "AGI", null);
	public static final Skill INTIMIDATE      = new Skill("Intimidate",      10, "CHA", null);
	public static final Skill LISTEN          = new Skill("Listen",          11, "PER", null);
	public static final Skill LOCKPICK        = new Skill("Lockpick",        12, "AGI", null);
	public static final Skill MEDICINE        = new Skill("Medicine",        13, "PER", null);
	public static final Skill MELEE_WEAPONS   = new Skill("Melee Weapons",   14, "STR", null);
	public static final Skill PERFORM         = new Skill("Perform",         15, "CHA", null);
	//public static final Skill PROFESSION      = new Skill("Profession",      16, "PER", null);
	public static final Skill REPAIR          = new Skill("Repair",          17, "INT", null);
	public static final Skill RIDE            = new Skill("Ride",            18, "AGI", null);
	public static final Skill SCIENCE         = new Skill("Science",         19, "INT", null);
	public static final Skill SEARCH          = new Skill("Search",          20, "PER", null);
	public static final Skill SENSE_MOTIVE    = new Skill("Sense Motive",    21, "PER", null);
	public static final Skill SNEAK           = new Skill("Sneak",           22, "AGI", null);
	public static final Skill SPEECH          = new Skill("Speech",          23, "CHA", null);
	public static final Skill SPOT            = new Skill("Spot",            24, "PER", null);
	public static final Skill SURVIVAL        = new Skill("Survival",        25, "PER", null);
	public static final Skill SWIM            = new Skill("Swim",            26, "STR", null);
	public static final Skill TWO_HOOFED_GUNS = new Skill("Two-Hoofed Guns", 27, "AGI", null);
	public static final Skill THROWING        = new Skill("Throwing",        28, "STR", null);
	public static final Skill UNARMED         = new Skill("Unarmed",         29, "AGI", null);
	
	public static final LinkedHashMap<String, Skill> skillMap = new LinkedHashMap<String, Skill>() {
		{
			put("acrobatics",      Skills.ACROBATICS);
			put("barter",          Skills.BARTER);
			put("battle_saddle",   Skills.BATTLE_SADDLE);
			put("energy_weapons",  Skills.ENERGY_WEAPONS);
			put("explosives",      Skills.EXPLOSIVES);
			put("fly",             Skills.FLY);
			put("small_guns",      Skills.SMALL_GUNS);
			put("big_guns",        Skills.BIG_GUNS);
			put("intimidate",      Skills.INTIMIDATE);
			put("listen",          Skills.LISTEN);
			put("lockpick",        Skills.LOCKPICK);
			put("medicine",        Skills.MEDICINE);
			put("melee_weapons",   Skills.MELEE_WEAPONS);
			put("perform",         Skills.PERFORM);
			put("repair",          Skills.REPAIR);
			put("ride",            Skills.RIDE);
			put("science",         Skills.SCIENCE);
			put("search",          Skills.SEARCH);
			put("sense_motive",    Skills.SENSE_MOTIVE);
			put("sneak",           Skills.SNEAK);
			put("speech",          Skills.SPEECH);
			put("spot",            Skills.SPOT);
			put("survival",        Skills.SURVIVAL);
			put("swim",            Skills.SWIM);
			put("two_hoofed_guns", Skills.TWO_HOOFED_GUNS);
			put("throwing",        Skills.THROWING);
			put("unarmed",         Skills.UNARMED);
		}
	};
}