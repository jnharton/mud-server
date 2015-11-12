package mud.rulesets.foe;

import java.util.Hashtable;

import mud.game.Ability;
import mud.game.Skill;
import mud.rulesets.d20.Abilities;
import mud.rulesets.d20.Skills;
import mud.rulesets.special.Perk;
import mud.rulesets.special.Perk.Type;

public final class Perks {
	// Name
	// Description
	// Type
	// Ranks
	// Required Level
	// Required Stats
	// Required Skills
	
	public static final Perk AWARENESS = new Perk(
			"Awareness",
			"Examining a target shows hitpoints, weapon and ammunition count",
			Type.REGULAR, 1, 3,
			null,
			new Hashtable<Skill, Integer>() { { put( Skills.SENSE_MOTIVE, 5 ); } });
	public static final Perk SILENT_RUNNING = new Perk(
			"Silent Running",
			"+10 Sneak, foot speed and armor weight no longer affect sneaking",
			Type.REGULAR, 1, 12,
			new Hashtable<Ability, Integer>() { { put( Abilities.CHARISMA, 5); } },
			new Hashtable<Skill, Integer>() { { put( Skills.MOVE_SILENTLY, 50 ); } });
	public static final Perk CUTIE_MARK = new Perk(
			"Cutie Mark",
			 "You earned your cutie mark! Given that you make your home in the Equestrian Wasteland this is"
			 + "an even more dicey outcome than usual.\n(You get to pick your cutiemark.)\n*Based on your"
			 + "selections a description will be cobbled together by a generator based on your skills, etc"
			 + "acquisition so far and those things will be tagged automatically at no cost.",
			 Type.UNIQUE, 1, 3,
			 null,
			 null );
	public static final Perk CHERCHEZ_LA_FILLY = new Perk(
			"Cherchez La Filly",
			"+10% damage to the same sex and unique dialogue options with certain ponies.",
			Type.REGULAR, 1, 2,
			null,
			null
			);
}