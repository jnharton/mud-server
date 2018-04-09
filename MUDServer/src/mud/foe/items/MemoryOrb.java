package mud.foe.items;

import mud.foe.FOEItemTypes;
import mud.foe.FOESlotTypes;
import mud.foe.misc.Memory;

import mud.objects.Item;

public class MemoryOrb extends Item {
	private Memory memory;
	
	public MemoryOrb() {
		this(-1);
	}
	
	public MemoryOrb(int dbref) {
		this(dbref, null);
	}
	
	public MemoryOrb(final Integer dbref, final Memory memory) {
		super(dbref, "Memory Orb", "A memory orb");

		this.item_type = FOEItemTypes.MEMORY_ORB;
		this.slot_type = FOESlotTypes.NONE;

		this.durability = 20;

		this.memory = memory;

		this.setAuctionable(false);
	}
	
	protected MemoryOrb(final MemoryOrb template) {
		super(-1, template.name, template.desc);
		
		this.item_type = FOEItemTypes.MEMORY_ORB;
		this.slot_type = FOESlotTypes.NONE;
		
		this.durability = 20;
		
		this.memory = template.memory; // TODO should this be a deep copy?
		
		this.setAuctionable(false);
	}
	
	public Memory getMemory() {
		return this.memory;
	}
	
	public void setMemory(Memory memory) {
		this.memory = memory;
	}
	
	@Override
	public MemoryOrb getCopy() {
		return new MemoryOrb(this);
	}
}