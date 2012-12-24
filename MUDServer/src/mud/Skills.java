package mud;

public final class Skills {
	public static final Skill APPRAISE = new Skill("Appraise", 0, "INT", new Classes[] { Classes.BARD, Classes.ROGUE });
	public static final Skill BALANCE = new Skill("Balance", 1, "DEX", new Classes[] { Classes.BARD, Classes.MONK, Classes.ROGUE });
	public static final Skill BLUFF = new Skill("Bluff", 2, "CHA", new Classes[] { Classes.BARD, Classes.ROGUE, Classes.SORCERER });
	public static final Skill CLIMB = new Skill("Climb", 3, "STR", new Classes[] { Classes.BARBARIAN, Classes.BARD, Classes.FIGHTER, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill CONCENTRATION = new Skill("Concentration", 4, "CON", new Classes[] { Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.MONK, Classes.PALADIN, Classes.RANGER, Classes.SORCERER, Classes.WIZARD });
	public static final Skill CRAFT = new Skill("Craft", 5, "INT", new Classes[] { Classes.BARBARIAN, Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.FIGHTER, Classes.MONK, Classes.PALADIN, Classes.RANGER, Classes.ROGUE, Classes.SORCERER, Classes.WIZARD });
	public static final Skill DECIPHER_SCRIPT = new Skill("Decipher Script", 6, "INT", new Classes[] { Classes.BARD, Classes.ROGUE });
	public static final Skill DIPLOMACY = new Skill("Diplomacy", 7, "CHA", new Classes[] { Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.MONK, Classes.PALADIN, Classes.ROGUE });
	public static final Skill DISGUISE = new Skill("Disguise", 8, "CHA", new Classes[] { Classes.BARD, Classes.ROGUE });
	public static final Skill ESCAPE_ARTIST = new Skill("Escape Artist", 9, "DEX", new Classes[] { Classes.BARD, Classes.MONK, Classes.ROGUE });
	public static final Skill GATHER_INFORMATION = new Skill("Gather Information", 10, "CHA", new Classes[] { Classes.BARD, Classes.ROGUE });
	public static final Skill HANDLE_ANIMAL = new Skill("Handle Animal", 11, "CHA", new Classes[] { Classes.BARBARIAN, Classes.DRUID, Classes.FIGHTER, Classes.PALADIN });
	public static final Skill HEAL = new Skill("Heal", 12, "CHA", new Classes[] { Classes.CLERIC, Classes.DRUID, Classes.PALADIN, Classes.RANGER });
	public static final Skill HIDE = new Skill("Hide", 13, "DEX", new Classes[] { Classes.BARD, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill INTIMIDATE = new Skill("Intimidate", 14, "CHA", new Classes[] { Classes.BARBARIAN, Classes.FIGHTER, Classes.ROGUE });
	public static final Skill JUMP = new Skill("Jump", 15, "STR", new Classes[] { Classes.BARBARIAN, Classes.BARD, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill KNOWLEDGE = new Skill("Knowledge", 16, "INT", new Classes[] { Classes.BARD, Classes.WIZARD });
	public static final Skill KNOWLEDGE_ARCANA = new Skill("Knowledge (arcana)", 17, "INT", "arcana", new Classes[] { Classes.BARD, Classes.CLERIC, Classes.MONK, Classes.SORCERER, Classes.WIZARD });
	public static final Skill KNOWLEDGE_DUNGEONEERING = new Skill("Knowledge (dungeoneering)", 18, "INT", "dungeoneering", new Classes[] { Classes.BARD, Classes.RANGER, Classes.WIZARD });
	public static final Skill KNOWLEDGE_GEOGRAPHY = new Skill("Knowledge (geography)", 19, "INT", "geography", new Classes[] { Classes.BARD, Classes.RANGER, Classes.WIZARD });
	public static final Skill KNOWLEDGE_HISTORY = new Skill("Knowledge (history)", 20, "INT", "history", new Classes[] { Classes.BARD, Classes.CLERIC, Classes.WIZARD });
	public static final Skill KNOWLEDGE_LOCAL = new Skill("Knowledge (local)", 21, "INT", "local", new Classes[] { Classes.BARD, Classes.ROGUE, Classes.WIZARD });
	public static final Skill KNOWLEDGE_NATURE = new Skill("Knowledge (nature)", 22, "INT", "nature", new Classes[] { Classes.BARD, Classes.DRUID, Classes.RANGER, Classes.WIZARD });
	public static final Skill KNOWLEDGE_NOBILITY = new Skill("Knowledge (nobility)", 23, "INT", "nobility", new Classes[] { Classes.BARD, Classes.PALADIN, Classes.WIZARD });
	public static final Skill KNOWLEDGE_PLANAR = new Skill("Knowledge (planar)", 24, "INT", "planar", new Classes[] { Classes.BARD, Classes.CLERIC, Classes.WIZARD });
	public static final Skill KNOWLEDGE_RELIGION = new Skill("Knowledge (religion)", 25, "INT", "religion", new Classes[] { Classes.BARD, Classes.CLERIC, Classes.MONK, Classes.PALADIN, Classes.WIZARD });
	public static final Skill LISTEN = new Skill("Listen", 26, "WIS", new Classes[] { Classes.BARBARIAN, Classes.DRUID, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill MOVE_SILENTLY = new Skill("Move Silently", 27, "DEX", new Classes[] { Classes.BARD, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill PERFORM = new Skill("Perform", 28, "CHA", new Classes[] { Classes.BARD, Classes.MONK, Classes.ROGUE });
	public static final Skill PROFESSION = new Skill("Profession", 29, "WIS", new Classes[] { Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.MONK, Classes.PALADIN, Classes.RANGER, Classes.ROGUE, Classes.SORCERER, Classes.WIZARD });
	public static final Skill RIDE = new Skill("Ride", 30, "DEX", new Classes[] { Classes.BARBARIAN, Classes.DRUID, Classes.FIGHTER, Classes.PALADIN, Classes.RANGER });
	public static final Skill SEARCH = new Skill("Search", 31, "INT", new Classes[] { Classes.RANGER, Classes.ROGUE });
	public static final Skill SENSE_MOTIVE = new Skill("Sense Motive", 32, "WIS", new Classes[] { Classes.BARD, Classes.PALADIN, Classes.ROGUE });
	public static final Skill SLEIGHT_OF_HAND = new Skill("Sleight of Hand", 33, "DEX", new Classes[] { Classes.BARD, Classes.ROGUE });
	public static final Skill SPEAK_LANGUAGE = new Skill("Speak Language", 34, "NONE", new Classes[] { Classes.BARD });
	public static final Skill SPELLCRAFT = new Skill("Spellcraft", 35, "INT", new Classes[] { Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.SORCERER, Classes.WIZARD });
	public static final Skill SPOT = new Skill("Spot", 36, "WIS", new Classes[] { Classes.DRUID, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill SURVIVAL = new Skill("Survival", 37, "WIS", new Classes[] { Classes.BARBARIAN, Classes.DRUID, Classes.RANGER });
	public static final Skill SWIM = new Skill("Swim", 38, "STR", new Classes[] { Classes.BARBARIAN, Classes.BARD, Classes.DRUID, Classes.FIGHTER, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill TUMBLE = new Skill("Tumble", 39, "DEX", new Classes[] { Classes.BARD, Classes.MONK, Classes.ROGUE });
	public static final Skill USE_MAGIC_DEVICE = new Skill("Use Magic Device", 40, "CHA", new Classes[] { Classes.BARD, Classes.ROGUE });
	public static final Skill USE_ROPE = new Skill("Use Rope", 41, "DEX", new Classes[] { Classes.RANGER, Classes.ROGUE });
	
	public static final Skill NAVIGATION = new Skill("Navigation", 42, "", null);
	public static final Skill TRACKING = new Skill("Tracking", 43, "", null);
}