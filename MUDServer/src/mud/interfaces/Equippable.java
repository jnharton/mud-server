package mud.interfaces;

import mud.misc.SlotType;
import mud.objects.ItemType;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * Defines an interface for "equippable" objects. (items)
 * 
 * @author Jeremy
 */
public interface Equippable {
	public ItemType getItemType();
	public SlotType getSlotType();
	
	public boolean isEquipped();
	public void setEquipped(boolean equipped);
}