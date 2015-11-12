package mud.rulesets.special;

import java.util.Map;

import mud.game.Ability;
import mud.game.Skill;

/**
 * References: http://fallout.wikia.com/wiki/Traits
 * 
 * @author Jeremy
 *
 */
public final class Trait {
	private int id;
	
	private String name;
	
	private Map<Ability, Integer> abilityModifiers;
	private Map<Skill, Integer> skillModifiers;
}