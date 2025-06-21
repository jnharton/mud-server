package mud.rulesets.foe;

import java.util.LinkedHashMap;

import mud.game.Ability;

public final class Abilities {
	// Abilities/Primary Statistics
	public static final Ability STRENGTH =     new Ability("Strength",     "str", 0);
	public static final Ability PERCEPTION =   new Ability("Perception",   "per", 1);
	public static final Ability ENDURANCE =    new Ability("Endurance",    "end", 2);
	public static final Ability CHARISMA =     new Ability("Charisma",     "cha", 3);
	public static final Ability INTELLIGENCE = new Ability("Intelligence", "int", 4);
	public static final Ability AGILITY =      new Ability("Agility",      "agi", 5);
	public static final Ability LUCK =         new Ability("Luck",         "luc", 6);
	
	@SuppressWarnings("serial")
	public static final LinkedHashMap<String, Ability> abilityMap = new LinkedHashMap<String, Ability>() {
		{
			put("STR", Abilities.STRENGTH);
			put("PER", Abilities.PERCEPTION);
			put("END", Abilities.ENDURANCE);
			put("CHA", Abilities.CHARISMA);
			put("INT", Abilities.INTELLIGENCE);
			put("AGI", Abilities.AGILITY);
			put("LUC", Abilities.LUCK);
		}
	};
}