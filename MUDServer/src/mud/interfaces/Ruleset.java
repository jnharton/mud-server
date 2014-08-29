package mud.interfaces;

import java.util.Hashtable;

import mud.game.Ability;
import mud.game.Skill;
import mud.utils.Tuple;

/**
 * An interface that specifies methods that a ruleset, for a game,
 * should have.
 * 
 * @author Jeremy
 * @param <E>
 *
 */
public interface Ruleset<E> {
	public String getName();
	
	public Ability getAbility(int id);
	public Ability getAbility(String name);
	public Ability[] getAbilities();
	
	public Skill getSkill(int id);
	public Skill getSkill(String name);
	public Skill[] getSkills();
	
	//public Hashtable<Tuple<String, E>> getExtendedData();
}