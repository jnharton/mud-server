package mud.foe.items;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.foe.FOEItemTypes;
import mud.foe.FOESlotTypes;
import mud.foe.misc.Memory;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;

public class MemoryOrb extends Item {
	private Memory memory;
	
	public MemoryOrb() {
		this(-1);
	}
	
	public MemoryOrb(int dbref) {
		super(dbref, "Memory Orb", EnumSet.noneOf(ObjectFlag.class), "A memory orb", -1);
		
		// Item class members
		this.type = TypeFlag.ITEM;
		
		this.item_type = FOEItemTypes.MEMORY_ORB;
		this.equip_type = ItemTypes.NONE;
		this.slot_type = FOESlotTypes.NONE;
		
		this.durability = 20;
		
		// Memory class members
		this.memory = null;
		
		// Make other adjustments
		this.setAuctionable(false);
	}
	
	public void setMemory(Memory memory) {
		this.memory = memory;
	}
	
	public Memory getMemory() {
		return this.memory;
	}
}