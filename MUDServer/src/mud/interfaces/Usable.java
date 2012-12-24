package mud.interfaces;

import java.util.ArrayList;
import java.util.List;

import mud.Effect;
import mud.magic.Spell;

/**
 * Defines an interface for "usable" objects. (i.e. potions, wands, etc)
 * 
 * @author Jeremy
 *
 * @param <T> Some object type that will implement usable.
 */
public interface Usable<T> {
	public Spell getSpell();
	
	public List<Spell> getSpells();
	
	public Effect getEffect();
	
	/**
	 * 
	 * NOTE: Object types that either have no effects, or only one
	 * should implement this method as below.
	 * 
	 * return new List<Effect>(); // return an empty list with no effects by default
	 * 
	 * @return
	 */
	public List<Effect> getEffects();
}