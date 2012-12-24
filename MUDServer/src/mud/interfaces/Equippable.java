package mud.interfaces;

import mud.objects.Player;

/**
 * Defines an interface for "equippable" objects. (items)
 * 
 * @author Jeremy
 *
 * @param <T> Some object type that will implement equippable.
 */
public interface Equippable<T> {
	public void equip();
	public void equip(Player p);
	public T unequip();
}