package mud.rulesets.special;

import mud.interfaces.Ruleset;

/**
 * Implements aspects of the S.P.E.C.I.A.L ruleset used in the
 * Fallout games made, respectively, by Interplay and Bethesda.
 * 
 * @author Jeremy
 *
 */
public interface SpecialRuleset extends Ruleset {
	// get Perks
	public Perk getPerk(int id);
	public Perk getPerk(String name);
	
	// get Traits
	public Trait getTrait(int id);
	public Trait getTrait(String name);
}