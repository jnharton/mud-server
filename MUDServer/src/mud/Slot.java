package mud;

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
import mud.objects.items.Clothing;
import mud.objects.items.ClothingType;

/**
 * Slot is a class which represents the logical possibility of equipping/wearing
 * something on the body of a Player in a text-based game, here known generally
 * as a MUD
 * 
 * Last edited: April 20, 2012 (4/20/2012)
 * 
 * @author Jeremy
 * @version %M%.%m%
 */
public class Slot {
	private String description;        // descriptive text
	private SlotType[] slotTypes;       // the type of slot this is
	private ItemType itemType;         // the type of item the slot can hold
	private ClothingType clothingType; // the type of clothing item the slot can hold
	private Item item;                 // the item the slot currently holds

	/**
	 * Constructs a slot based on a single parameter, an
	 * item type that it can hold.
	 * 
	 * @param slotType
	 * @param itemType
	 */
	public Slot(final SlotType[] slotTypes, final ItemType itemType) {
		this(slotTypes, itemType, null);
	}

	/**
	 * Constructs a slot based on two parameters: the
	 * item type it can hold and an item to place in the
	 * slot as it is being constructed.
	 * 
	 * @param slotType
	 * @param itemType
	 * @param item
	 */
	public Slot(final SlotType[] slotTypes, final ItemType itemType, final Item item) {
		this.slotTypes = slotTypes;
		this.itemType = itemType;
		
		if (item != null) { // ensure that putting null in doesn't register as full
			this.item = item;
		}
		else {
			this.item = null;
		}
	}
	
	/**
	 * Constructs a slot based on a single parameter, an
	 * item type that it can hold.
	 * 
	 * @param slotType
	 * @param clothingType
	 */
	public Slot(final SlotType[] slotTypes, final ClothingType clothingType) {
		this(slotTypes, clothingType, null);
	}
	
	/**
	 * Constructs a slot based on two parameters: the
	 * item type it can hold and an item to place in the
	 * slot as it is being constructed.
	 * 
	 * @param slotType
	 * @param clothingType
	 * @param clothing
	 */
	public Slot(final SlotType[] slotTypes, final ClothingType clothingType, final Clothing clothing) {
		this.slotTypes = slotTypes;
		this.itemType = ItemType.CLOTHING;
		this.clothingType = clothingType;
		
		if (clothing != null) { // ensure that putting null in doesn't register as full
			this.item = clothing;
		}
		else {
			this.item = null;
		}
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
	
	/**
	 * Tell us what type of item can go in the slot.
	 * 
	 * @return a string indicating the item type that fits in the slot
	 */
	public ItemType getType() {
		return this.itemType;
	}
	
	/**
	 * Tell us what type of of clothing, if any, can go in the slot
	 * 
	 * @return a string indicating the clothing type that fits in the slot
	 */
	public ClothingType getCType() {
		return this.clothingType;
	}
	
	public SlotType[] getSlotTypes() {
		return this.slotTypes;
	}
	
	/**
	 * Returns a true/false value telling whether a specific item
	 * type will fit in this slot.
	 * 
	 * @param tType the type to check against
	 * @return a boolean indicative of whether this slot is of that type
	 */
	public boolean isType(final ItemType iType) {
		return this.itemType.equals(iType);
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
