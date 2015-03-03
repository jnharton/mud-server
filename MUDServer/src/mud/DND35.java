package mud;

import java.util.HashMap;

import mud.game.Ability;
import mud.game.PClass;
import mud.game.Skill;

import mud.interfaces.Ruleset;

public final class DND35 implements Ruleset {
	// Abilities/Primary Statistics
	private static final Ability STRENGTH = new Ability("strength", "str", 0);
	private static final Ability DEXTERITY = new Ability("dexterity", "dex", 1);
	private static final Ability CONSTITUTION = new Ability("constitution", "con", 2);
	private static final Ability INTELLIGENCE= new Ability("intelligence", "int", 3);
	private static final Ability CHARISMA = new Ability("charisma", "cha", 4);
	private static final Ability WISDOM = new Ability("wisdom", "wis", 5);

	private static final Ability[] abilities = new Ability[] { 
		STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, CHARISMA, WISDOM
	};

	private static final HashMap<String, Ability> abilityMap = new HashMap<String, Ability>() {
		{
			put("STR", STRENGTH);
			put("DEX", DEXTERITY);
			put("CON", CONSTITUTION);
			put("INT", INTELLIGENCE);
			put("CHA", CHARISMA);
			put("WIS", WISDOM);
		}
	};

	// Classes
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

	private static final HashMap<String, PClass> classMap = new HashMap<String, PClass>() {
		{
			put("ADEPT",      ADEPT);
			put("ARISTOCRAT", ARISTOCRAT);
			put("BARBARIAN",  BARBARIAN);
			put("BARD",       BARD);
			put("COMMONER",   COMMONER);
			put("CLERIC",     CLERIC);
			put("DRUID",      DRUID);
			put("FIGHTER",    FIGHTER);
			put("MONK",       MONK);
			put("PALADIN",    PALADIN);
			put("RANGER",     RANGER);
			put("ROGUE",      ROGUE);
			put("SORCEROR",   SORCERER);
			put("WARRIOR",    WARRIOR);
			put("WIZARD",     WIZARD);
		}
	};

	private static final PClass[] myValues = {
		NONE,
		BARBARIAN, BARD, CLERIC, DRUID, FIGHTER, MONK, PALADIN, RANGER, ROGUE, SORCERER, WIZARD,
		ADEPT, ARISTOCRAT, COMMONER, EXPERT, WARRIOR
	};

	// Skills
	public static final Skill APPRAISE = new Skill("Appraise", 0, "INT", new PClass[] { BARD, ROGUE });
	public static final Skill BALANCE = new Skill("Balance", 1, "DEX", new PClass[] { BARD, MONK, ROGUE });
	public static final Skill BLUFF = new Skill("Bluff", 2, "CHA", new PClass[] { BARD, ROGUE, SORCERER });
	public static final Skill CLIMB = new Skill("Climb", 3, "STR", new PClass[] { BARBARIAN, BARD, FIGHTER, MONK, RANGER, ROGUE });
	public static final Skill CONCENTRATION = new Skill("Concentration", 4, "CON", new PClass[] { BARD, CLERIC, DRUID, MONK, PALADIN, RANGER, SORCERER, WIZARD });
	public static final Skill CRAFT = new Skill("Craft", 5, "INT", new PClass[] { BARBARIAN, BARD, CLERIC, DRUID, FIGHTER, MONK, PALADIN, RANGER, ROGUE, SORCERER, WIZARD });
	public static final Skill DECIPHER_SCRIPT = new Skill("Decipher Script", 6, "INT", new PClass[] { BARD, ROGUE });
	public static final Skill DIPLOMACY = new Skill("Diplomacy", 7, "CHA", new PClass[] { BARD, CLERIC, DRUID, MONK, PALADIN, ROGUE });
	public static final Skill DISGUISE = new Skill("Disguise", 8, "CHA", new PClass[] { BARD, ROGUE });
	public static final Skill ESCAPE_ARTIST = new Skill("Escape Artist", 9, "DEX", new PClass[] { BARD, MONK, ROGUE });
	public static final Skill GATHER_INFORMATION = new Skill("Gather Information", 10, "CHA", new PClass[] { BARD, ROGUE });
	public static final Skill HANDLE_ANIMAL = new Skill("Handle Animal", 11, "CHA", new PClass[] { BARBARIAN, DRUID, FIGHTER, PALADIN });
	public static final Skill HEAL = new Skill("Heal", 12, "CHA", new PClass[] { CLERIC, DRUID, PALADIN, RANGER });
	public static final Skill HIDE = new Skill("Hide", 13, "DEX", new PClass[] { BARD, MONK, RANGER, ROGUE });
	public static final Skill INTIMIDATE = new Skill("Intimidate", 14, "CHA", new PClass[] { BARBARIAN, FIGHTER, ROGUE });
	public static final Skill JUMP = new Skill("Jump", 15, "STR", new PClass[] { BARBARIAN, BARD, MONK, RANGER, ROGUE });
	public static final Skill KNOWLEDGE = new Skill("Knowledge", 16, "INT", new PClass[] { BARD, WIZARD });
	public static final Skill KNOWLEDGE_ARCANA = new Skill("Knowledge (arcana)", 17, "INT", new PClass[] { BARD, CLERIC, MONK, SORCERER, WIZARD });
	public static final Skill KNOWLEDGE_DUNGEONEERING = new Skill("Knowledge (dungeoneering)", 18, "INT", new PClass[] { BARD, RANGER, WIZARD });
	public static final Skill KNOWLEDGE_GEOGRAPHY = new Skill("Knowledge (geography)", 19, "INT", new PClass[] { BARD, RANGER, WIZARD });
	public static final Skill KNOWLEDGE_HISTORY = new Skill("Knowledge (history)", 20, "INT", new PClass[] { BARD, CLERIC, WIZARD });
	public static final Skill KNOWLEDGE_LOCAL = new Skill("Knowledge (local)", 21, "INT", new PClass[] { BARD, ROGUE, WIZARD });
	public static final Skill KNOWLEDGE_NATURE = new Skill("Knowledge (nature)", 22, "INT", new PClass[] { BARD, DRUID, RANGER, WIZARD });
	public static final Skill KNOWLEDGE_NOBILITY = new Skill("Knowledge (nobility)", 23, "INT", new PClass[] { BARD, PALADIN, WIZARD });
	public static final Skill KNOWLEDGE_PLANAR = new Skill("Knowledge (planar)", 24, "INT", new PClass[] { BARD, CLERIC, WIZARD });
	public static final Skill KNOWLEDGE_RELIGION = new Skill("Knowledge (religion)", 25, "INT", new PClass[] { BARD, CLERIC, MONK, PALADIN, WIZARD });
	public static final Skill LISTEN = new Skill("Listen", 26, "WIS", new PClass[] { BARBARIAN, DRUID, MONK, RANGER, ROGUE });
	public static final Skill MOVE_SILENTLY = new Skill("Move Silently", 27, "DEX", new PClass[] { BARD, MONK, RANGER, ROGUE });
	public static final Skill PERFORM = new Skill("Perform", 28, "CHA", new PClass[] { BARD, MONK, ROGUE });
	public static final Skill PROFESSION = new Skill("Profession", 29, "WIS", new PClass[] { BARD, CLERIC, DRUID, MONK, PALADIN, RANGER, ROGUE, SORCERER, WIZARD });
	public static final Skill RIDE = new Skill("Ride", 30, "DEX", new PClass[] { BARBARIAN, DRUID, FIGHTER, PALADIN, RANGER });
	public static final Skill SEARCH = new Skill("Search", 31, "INT", new PClass[] { RANGER, ROGUE });
	public static final Skill SENSE_MOTIVE = new Skill("Sense Motive", 32, "WIS", new PClass[] { BARD, PALADIN, ROGUE });
	public static final Skill SLEIGHT_OF_HAND = new Skill("Sleight of Hand", 33, "DEX", new PClass[] { BARD, ROGUE });
	public static final Skill SPEAK_LANGUAGE = new Skill("Speak Language", 34, "NONE", new PClass[] { BARD });
	public static final Skill SPELLCRAFT = new Skill("Spellcraft", 35, "INT", new PClass[] { BARD, CLERIC, DRUID, SORCERER, WIZARD });
	public static final Skill SPOT = new Skill("Spot", 36, "WIS", new PClass[] { DRUID, MONK, RANGER, ROGUE });
	public static final Skill SURVIVAL = new Skill("Survival", 37, "WIS", new PClass[] { BARBARIAN, DRUID, RANGER });
	public static final Skill SWIM = new Skill("Swim", 38, "STR", new PClass[] { BARBARIAN, BARD, DRUID, FIGHTER, MONK, RANGER, ROGUE });
	public static final Skill TUMBLE = new Skill("Tumble", 39, "DEX", new PClass[] { BARD, MONK, ROGUE });
	public static final Skill USE_MAGIC_DEVICE = new Skill("Use Magic Device", 40, "CHA", new PClass[] { BARD, ROGUE });
	public static final Skill USE_ROPE = new Skill("Use Rope", 41, "DEX", new PClass[] { RANGER, ROGUE });

	public static final Skill NAVIGATION = new Skill("Navigation", 42, "INT", new PClass[0]);
	public static final Skill TRACKING = new Skill("Tracking", 43, "WIS", new PClass[0]);

	public static final HashMap<String, Skill> skillMap = new HashMap<String, Skill>(43, 0.75f) {
		{
			put("appraise", APPRAISE);
			put("balance", BALANCE);
			put("bluff", BLUFF);
			put("climb", CLIMB);
			put("concentration", CONCENTRATION);
			put("craft", CRAFT);
			put("deciper script", DECIPHER_SCRIPT);
			put("diplomacy", DIPLOMACY);
			put("disguise", DISGUISE);
			put("escape artist", ESCAPE_ARTIST);
			put("ea", ESCAPE_ARTIST);                      // 'escape artist' alias
			put("gather information", GATHER_INFORMATION);
			put("gi", GATHER_INFORMATION);                 // 'gather information' alias
			put("handle animal", HANDLE_ANIMAL);
			put("ha", HANDLE_ANIMAL);                      // 'handle animal' alias
			put("heal", HEAL);
			put("hide", HIDE);
			put("intimidate", INTIMIDATE);
			put("jump", JUMP);
			put("knowledge", KNOWLEDGE);
			put("knowledge arcana", KNOWLEDGE_ARCANA);
			put("ka", KNOWLEDGE_ARCANA);
			put("knowledge dungeoneering", KNOWLEDGE_DUNGEONEERING);
			put("kd", KNOWLEDGE_DUNGEONEERING);
			put("knowledge geography", KNOWLEDGE_GEOGRAPHY);
			put("kg", KNOWLEDGE_GEOGRAPHY);
			put("knowledge history", KNOWLEDGE_HISTORY);
			put("kh", KNOWLEDGE_HISTORY);
			put("knowledge local", KNOWLEDGE_LOCAL);
			put("kl", KNOWLEDGE_LOCAL);
			put("knowledge nature", KNOWLEDGE_NATURE);
			put("kna", KNOWLEDGE_NATURE);
			put("knowledge nobility", KNOWLEDGE_NOBILITY);
			put("kno", KNOWLEDGE_NOBILITY);
			put("knowledge planar", KNOWLEDGE_PLANAR);
			put("kp", KNOWLEDGE_PLANAR);
			put("knowledge religion", KNOWLEDGE_RELIGION);
			put("kr", KNOWLEDGE_RELIGION);
			put("listen", LISTEN);
			put("move silently", MOVE_SILENTLY);
			put("ms", MOVE_SILENTLY);
			//...
			put("use magic device", USE_MAGIC_DEVICE);
			put("umd", USE_MAGIC_DEVICE);                  // 'use magic device' alias
			put("use rope", USE_ROPE);
			put("ur", USE_ROPE);                           // 'use rope' alias

			put("navigation", NAVIGATION);
			put("nav", NAVIGATION);
			put("tracking", TRACKING);
			put("track", TRACKING);
		}
	};

	private static DND35 instance;

	private DND35() {
	}

	public static DND35 getInstance() {
		if( instance == null ) {
			instance = new DND35();
		}

		return instance;
	}

	@Override
	public String getName() {
		return "D20";
	}

	// Abilities Methods
	@Override
	public Ability getAbility(int id) {
		return abilities[id];
	}

	@Override
	public Ability getAbility(String abilityName) {
		for(final String s : abilityMap.keySet()) {
			if( s.equals(abilityName) ) {
				return abilityMap.get(s);
			}
		}

		return null;
	}

	@Override
	public Ability[] getAbilities() {
		return DND35.abilities;
	}

	// Classes Methods
	@Override
	public PClass getClass(int id) {
		return myValues[id];
	}
	
	@Override
	public PClass getClass(String className) {
		return classMap.get( className.toUpperCase() );
	}
	
	@Override
	public PClass[] getClasses() {
		return myValues;
	}

	// Skills Methods
	@Override
	public Skill getSkill(int id) {
		for(final Skill s : skillMap.values()) {
			if( s.getId() == id ) return s;
		}

		return null;
	}

	@Override
	public Skill getSkill(String skillName) {
		return skillMap.get( skillName.toLowerCase() );
	}

	@Override
	public Skill[] getSkills() {
		Skill[] skills = null;

		skillMap.values().toArray(skills);

		return skills; 
	}
}