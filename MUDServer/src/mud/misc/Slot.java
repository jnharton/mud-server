package mud.misc;

/*
  Copyright (c) 2012 Jeremy N. Harton
  
  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
  persons to whom the Software is furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
  Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import mud.objects.Item;
import mud.objects.ItemType;

/**
 * Slot is a class which represents the logical possibility of equipping/wearing
 * something on the body of a Player in a text-based game, here known generally
 * as a MUD
 * 
 * 
 * @author Jeremy
 * @version %M%.%m%
 */
public class Slot {
	private String description;        // descriptive text
	
	private SlotType slotType;         // the type of slot this is
	private ItemType[] itemTypes;      // the type of item the slot can hold
	
	private Item item;                 // the item the slot currently holds

	/**
	 * Constructs a slot based on a two parameters, the type of s
	 * item type that it can hold.
	 * 
	 * @param slotType
	 * @param itemType
	 */
	//public Slot(final SlotType[] slotTypes, final ItemType itemType) {
	public Slot(final SlotType slotType, final ItemType...itemTypes) {
		this.slotType = slotType;
		this.itemTypes = itemTypes;
	}
	
	/**
	 * Tell us what item is stored in the slot.
	 * 
	 * @return
	 */
	public Item getItem() {
		return this.item;
	}
	
	/**
	 * Insert an item into the slot, and mark the
	 * slot as full, since a slot can only hold one
	 * item.
	 * 
	 * @param item
	 */
	public void insert(final Item item) {
		this.item = item;
	}
	
	/**
	 * Remove an item from the slot, marking the item
	 * member as null and the slot as not full.
	 * 
	 * @return item the item that was in the slot
	 */
	public Item remove() {
		final Item item = this.item;
		this.item = null;
		return item;
	}
	
	/**
	 * Get the descriptive text for this slot.
	 * 
	 * @return string containing descriptive text
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Set the descriptive text for this slot.
	 * 
	 * @param newDescription new descriptive text for this slot
	 */
	public void setDescription(final String newDescription) {
		this.description = newDescription;
	}
	
	public SlotType getSlotType() {
		return this.slotType;
	}
	
	/**
	 * Tell us what type of item can go in the slot.
	 * 
	 * @return a string indicating the item type that fits in the slot
	 */
	public ItemType getItemType() {
		return this.itemTypes[0];
	}
	
	public ItemType[] getItemTypes() {
		return this.itemTypes;
	}
	
	/**
	 * 
	 * 
	 * @param tType the type to check against
	 * @return a boolean indicative of whether this slot is of that type
	 */
	public boolean isType(final ItemType iType) {
		boolean typeMatch = false;
		
		for(final ItemType it : this.itemTypes) {
			typeMatch = (it == iType);
			
			if( typeMatch ) break;
		}
		
		return typeMatch;
	}
	
	/**
	 * Allows us to determine whether the slot is full or not full.
	 * 
	 * NOTE: Redundant, because isFull() can be used or it's negative
	 * checked against. Still might be potentially useful for clarity of logic
	 * 
	 * @return a boolean representing whether the slot is empty or not empty
	 */
	public boolean isEmpty() {
		return this.item == null;
	}
	
	/**
	 * Allows us to determine whether the slot is full or not full.
	 * 
	 * @return a boolean representing whether the slot is full or not full.
	 */
	public boolean isFull() {
		return !isEmpty();
	}
	
	public String toString() {
		return "no string representation";
	}
}