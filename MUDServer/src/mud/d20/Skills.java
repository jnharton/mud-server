package mud.d20;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

import java.util.HashMap;

import mud.game.PClass;
import mud.game.Skill;

public final class Skills {
	public static final Skill APPRAISE = new Skill("Appraise", 0, "INT", new PClass[] { Classes.BARD, Classes.ROGUE });
	public static final Skill BALANCE = new Skill("Balance", 1, "DEX", new PClass[] { Classes.BARD, Classes.MONK, Classes.ROGUE });
	public static final Skill BLUFF = new Skill("Bluff", 2, "CHA", new PClass[] { Classes.BARD, Classes.ROGUE, Classes.SORCERER });
	public static final Skill CLIMB = new Skill("Climb", 3, "STR", new PClass[] { Classes.BARBARIAN, Classes.BARD, Classes.FIGHTER, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill CONCENTRATION = new Skill("Concentration", 4, "CON", new PClass[] { Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.MONK, Classes.PALADIN, Classes.RANGER, Classes.SORCERER, Classes.WIZARD });
	public static final Skill CRAFT = new Skill("Craft", 5, "INT", new PClass[] { Classes.BARBARIAN, Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.FIGHTER, Classes.MONK, Classes.PALADIN, Classes.RANGER, Classes.ROGUE, Classes.SORCERER, Classes.WIZARD });
	public static final Skill DECIPHER_SCRIPT = new Skill("Decipher Script", 6, "INT", new PClass[] { Classes.BARD, Classes.ROGUE });
	public static final Skill DIPLOMACY = new Skill("Diplomacy", 7, "CHA", new PClass[] { Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.MONK, Classes.PALADIN, Classes.ROGUE });
	public static final Skill DISGUISE = new Skill("Disguise", 8, "CHA", new PClass[] { Classes.BARD, Classes.ROGUE });
	public static final Skill ESCAPE_ARTIST = new Skill("Escape Artist", 9, "DEX", new PClass[] { Classes.BARD, Classes.MONK, Classes.ROGUE });
	public static final Skill GATHER_INFORMATION = new Skill("Gather Information", 10, "CHA", new PClass[] { Classes.BARD, Classes.ROGUE });
	public static final Skill HANDLE_ANIMAL = new Skill("Handle Animal", 11, "CHA", new PClass[] { Classes.BARBARIAN, Classes.DRUID, Classes.FIGHTER, Classes.PALADIN });
	public static final Skill HEAL = new Skill("Heal", 12, "CHA", new PClass[] { Classes.CLERIC, Classes.DRUID, Classes.PALADIN, Classes.RANGER });
	public static final Skill HIDE = new Skill("Hide", 13, "DEX", new PClass[] { Classes.BARD, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill INTIMIDATE = new Skill("Intimidate", 14, "CHA", new PClass[] { Classes.BARBARIAN, Classes.FIGHTER, Classes.ROGUE });
	public static final Skill JUMP = new Skill("Jump", 15, "STR", new PClass[] { Classes.BARBARIAN, Classes.BARD, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill KNOWLEDGE = new Skill("Knowledge", 16, "INT", new PClass[] { Classes.BARD, Classes.WIZARD });
	public static final Skill KNOWLEDGE_ARCANA = new Skill("Knowledge (arcana)", 17, "INT", new PClass[] { Classes.BARD, Classes.CLERIC, Classes.MONK, Classes.SORCERER, Classes.WIZARD });
	public static final Skill KNOWLEDGE_DUNGEONEERING = new Skill("Knowledge (dungeoneering)", 18, "INT", new PClass[] { Classes.BARD, Classes.RANGER, Classes.WIZARD });
	public static final Skill KNOWLEDGE_GEOGRAPHY = new Skill("Knowledge (geography)", 19, "INT", new PClass[] { Classes.BARD, Classes.RANGER, Classes.WIZARD });
	public static final Skill KNOWLEDGE_HISTORY = new Skill("Knowledge (history)", 20, "INT", new PClass[] { Classes.BARD, Classes.CLERIC, Classes.WIZARD });
	public static final Skill KNOWLEDGE_LOCAL = new Skill("Knowledge (local)", 21, "INT", new PClass[] { Classes.BARD, Classes.ROGUE, Classes.WIZARD });
	public static final Skill KNOWLEDGE_NATURE = new Skill("Knowledge (nature)", 22, "INT", new PClass[] { Classes.BARD, Classes.DRUID, Classes.RANGER, Classes.WIZARD });
	public static final Skill KNOWLEDGE_NOBILITY = new Skill("Knowledge (nobility)", 23, "INT", new PClass[] { Classes.BARD, Classes.PALADIN, Classes.WIZARD });
	public static final Skill KNOWLEDGE_PLANAR = new Skill("Knowledge (planar)", 24, "INT", new PClass[] { Classes.BARD, Classes.CLERIC, Classes.WIZARD });
	public static final Skill KNOWLEDGE_RELIGION = new Skill("Knowledge (religion)", 25, "INT", new PClass[] { Classes.BARD, Classes.CLERIC, Classes.MONK, Classes.PALADIN, Classes.WIZARD });
	public static final Skill LISTEN = new Skill("Listen", 26, "WIS", new PClass[] { Classes.BARBARIAN, Classes.DRUID, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill MOVE_SILENTLY = new Skill("Move Silently", 27, "DEX", new PClass[] { Classes.BARD, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill PERFORM = new Skill("Perform", 28, "CHA", new PClass[] { Classes.BARD, Classes.MONK, Classes.ROGUE });
	public static final Skill PROFESSION = new Skill("Profession", 29, "WIS", new PClass[] { Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.MONK, Classes.PALADIN, Classes.RANGER, Classes.ROGUE, Classes.SORCERER, Classes.WIZARD });
	public static final Skill RIDE = new Skill("Ride", 30, "DEX", new PClass[] { Classes.BARBARIAN, Classes.DRUID, Classes.FIGHTER, Classes.PALADIN, Classes.RANGER });
	public static final Skill SEARCH = new Skill("Search", 31, "INT", new PClass[] { Classes.RANGER, Classes.ROGUE });
	public static final Skill SENSE_MOTIVE = new Skill("Sense Motive", 32, "WIS", new PClass[] { Classes.BARD, Classes.PALADIN, Classes.ROGUE });
	public static final Skill SLEIGHT_OF_HAND = new Skill("Sleight of Hand", 33, "DEX", new PClass[] { Classes.BARD, Classes.ROGUE });
	public static final Skill SPEAK_LANGUAGE = new Skill("Speak Language", 34, "NONE", new PClass[] { Classes.BARD });
	public static final Skill SPELLCRAFT = new Skill("Spellcraft", 35, "INT", new PClass[] { Classes.BARD, Classes.CLERIC, Classes.DRUID, Classes.SORCERER, Classes.WIZARD });
	public static final Skill SPOT = new Skill("Spot", 36, "WIS", new PClass[] { Classes.DRUID, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill SURVIVAL = new Skill("Survival", 37, "WIS", new PClass[] { Classes.BARBARIAN, Classes.DRUID, Classes.RANGER });
	public static final Skill SWIM = new Skill("Swim", 38, "STR", new PClass[] { Classes.BARBARIAN, Classes.BARD, Classes.DRUID, Classes.FIGHTER, Classes.MONK, Classes.RANGER, Classes.ROGUE });
	public static final Skill TUMBLE = new Skill("Tumble", 39, "DEX", new PClass[] { Classes.BARD, Classes.MONK, Classes.ROGUE });
	public static final Skill USE_MAGIC_DEVICE = new Skill("Use Magic Device", 40, "CHA", new PClass[] { Classes.BARD, Classes.ROGUE });
	public static final Skill USE_ROPE = new Skill("Use Rope", 41, "DEX", new PClass[] { Classes.RANGER, Classes.ROGUE });
	
	public static final Skill NAVIGATION = new Skill("Navigation", 42, "INT", new PClass[0]);
	public static final Skill TRACKING = new Skill("Tracking", 43, "WIS", new PClass[0]);
	
	public static final HashMap<String, Skill> skillMap = new HashMap<String, Skill>(43, 0.75f) {
		{
			put("appraise", Skills.APPRAISE);
			put("balance", Skills.BALANCE);
			put("bluff", Skills.BLUFF);
			put("climb", Skills.CLIMB);
			put("concentration", Skills.CONCENTRATION);
			put("craft", Skills.CRAFT);
			put("deciper script", Skills.DECIPHER_SCRIPT);
			put("diplomacy", Skills.DIPLOMACY);
			put("disguise", Skills.DISGUISE);
			put("escape artist", Skills.ESCAPE_ARTIST);
			put("ea", Skills.ESCAPE_ARTIST);                      // 'escape artist' alias
			put("gather information", Skills.GATHER_INFORMATION);
			put("gi", Skills.GATHER_INFORMATION);                 // 'gather information' alias
			put("handle animal", Skills.HANDLE_ANIMAL);
			put("ha", Skills.HANDLE_ANIMAL);                      // 'handle animal' alias
			put("heal", Skills.HEAL);
			put("hide", Skills.HIDE);
			put("intimidate", Skills.INTIMIDATE);
			put("jump", Skills.JUMP);
			put("knowledge", Skills.KNOWLEDGE);
			put("knowledge arcana", Skills.KNOWLEDGE_ARCANA);
			put("ka", Skills.KNOWLEDGE_ARCANA);
			put("knowledge dungeoneering", Skills.KNOWLEDGE_DUNGEONEERING);
			put("kd", Skills.KNOWLEDGE_DUNGEONEERING);
			put("knowledge geography", Skills.KNOWLEDGE_GEOGRAPHY);
			put("kg", Skills.KNOWLEDGE_GEOGRAPHY);
			put("knowledge history", Skills.KNOWLEDGE_HISTORY);
			put("kh", Skills.KNOWLEDGE_HISTORY);
			put("knowledge local", Skills.KNOWLEDGE_LOCAL);
			put("kl", Skills.KNOWLEDGE_LOCAL);
			put("knowledge nature", Skills.KNOWLEDGE_NATURE);
			put("kna", Skills.KNOWLEDGE_NATURE);
			put("knowledge nobility", Skills.KNOWLEDGE_NOBILITY);
			put("kno", Skills.KNOWLEDGE_NOBILITY);
			put("knowledge planar", Skills.KNOWLEDGE_PLANAR);
			put("kp", Skills.KNOWLEDGE_PLANAR);
			put("knowledge religion", Skills.KNOWLEDGE_RELIGION);
			put("kr", Skills.KNOWLEDGE_RELIGION);
			put("listen", Skills.LISTEN);
			put("move silently", Skills.MOVE_SILENTLY);
			put("ms", Skills.MOVE_SILENTLY);
			//...
			put("use magic device", Skills.USE_MAGIC_DEVICE);
			put("umd", Skills.USE_MAGIC_DEVICE);                  // 'use magic device' alias
			put("use rope", Skills.USE_ROPE);
			put("ur", Skills.USE_ROPE);                           // 'use rope' alias
			
			put("navigation", Skills.NAVIGATION);
			put("nav", Skills.NAVIGATION);
			put("tracking", Skills.TRACKING);
			put("track", Skills.TRACKING);
		}
	};
}