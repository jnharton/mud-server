package mud.interfaces;

import mud.Ability;
import mud.Skill;

/**
 * An interface that specifies methods that a ruleset, for a game,
 * should have.
 * 
 * @author Jeremy
 *
 */
public interface Ruleset {
	public Ability getAbility(int id);
	public Ability getAbility(String name);
	public Ability[] getAbilities();
	
	public Skill getSkill(int id);
	public Skill getSkill(String name);
}