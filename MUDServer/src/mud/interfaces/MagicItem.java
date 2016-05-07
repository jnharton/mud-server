package mud.interfaces;

import java.util.List;

import mud.magic.Spell;
import mud.misc.Effect;

public interface MagicItem {
	public Spell getSpell();

	/**
	 * getSpells
	 * 
	 * NOTE: Object types that either have no effects, or only one
	 * should implement this method as below.
	 * 
	 * return new List<Effect>(); // return an empty list with no effects by default
	 * 
	 * @return
	 */
	public List<Spell> getSpells();

	public Effect getEffect();

	/**
	 * getEffects
	 * 
	 * NOTE: Object types that either have no effects, or only one
	 * should implement this method as below.
	 * 
	 * return new List<Effect>(); // return an empty list with no effects by default
	 * 
	 * @return
	 */
	//public List<Effect> getEffects();
}