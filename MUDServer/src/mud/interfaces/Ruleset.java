package mud.interfaces;

import java.util.Hashtable;

import mud.game.Ability;
import mud.game.PClass;
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
public interface Ruleset {
	// TODO I want to make ruleset generic somehow or be able to store metadata that I need
	public String getName();
	
	public Ability getAbility(int id);
	public Ability getAbility(String abilityName);
	public Ability[] getAbilities();
	
	public PClass getClass(int id);
	public PClass getClass(String className);
	public PClass[] getClasses();
	
	
	public Skill getSkill(int id);
	public Skill getSkill(String skillName);
	public Skill[] getSkills();
	
	//public Hashtable<Tuple<String, E>> getExtendedData();
}