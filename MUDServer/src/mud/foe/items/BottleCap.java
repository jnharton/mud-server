package mud.foe.items;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.foe.FOEItemTypes;
import mud.interfaces.Stackable;
import mud.objects.Item;

public class BottleCap extends Item implements Stackable<BottleCap> {
	private BottleCap bc;
	
	public BottleCap() {
		this(-1);
	}
	
	public BottleCap(int dbref) {
		super(dbref, "Bottle Cap", EnumSet.noneOf(ObjectFlag.class), "A bottle cap.", -1);
		
		// Item class members
		this.type = TypeFlag.ITEM;
		
		this.item_type = FOEItemTypes.BOTTLE_CAP;
	}
	
	protected BottleCap(final BottleCap template) {
		super(template);
		
		this.item_type = FOEItemTypes.BOTTLE_CAP;
	}
	
	public int size() {
		return stackSize();
	}

	public int stackSize() {
		if (this.bc != null) {
			return 1 + bc.stackSize();
		}
		else {
			return 1;
		}
	}
	
	public boolean stack(BottleCap object) {
		if( this.getName().equals( object.getName() ) ) { // name equality is treated as Item equality
			if (bc == null && stackSize() < Stackable.maxDepth) {
				object.setLocation( this.getDBRef() ); // the new location of the arrow in question is the old arrow
				bc = object;
				return true;
			}
			else {
				return bc.stack(object);
			}
		}
		
		return false;
	}
	
	public BottleCap split(int number) {
		if (number > 0 && stackSize() > number) {
			if (bc == null) {
				return this;
			}
			else {
				BottleCap prev = null; // the arrow before the current one (initially null)
				BottleCap curr = bc; // the current arrows

				int qty = 0;       // the total quantity of arrows, how we know if we have split off enough

				while (qty < number) {
					prev = curr;
					curr = curr.bc;
					qty++;
				}

				prev.bc = null;

				return curr;
			}
		}
		else {
			return null;
		}
	}
	
	@Override
	public BottleCap clone() {
		return new BottleCap(this);
	}
	
	@Override
	public String toString() {
		return this.name + " (" + stackSize() + ")";
	}
}