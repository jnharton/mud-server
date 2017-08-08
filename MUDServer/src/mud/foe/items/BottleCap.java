package mud.foe.items;

import mud.foe.FOEItemTypes;
import mud.interfaces.Stackable;
import mud.objects.Item;

public class BottleCap extends Item implements Stackable<BottleCap> {
	private BottleCap bc;
	
	public BottleCap() {
		this(-1);
	}
	
	public BottleCap(int dbref) {
		super(dbref, "Bottle Cap", "A bottle cap.");
		
		this.item_type = FOEItemTypes.BOTTLE_CAP;
	}
	
	protected BottleCap(final BottleCap template) {
		super(template);
		
		this.item_type = FOEItemTypes.BOTTLE_CAP;
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
		boolean result = false;

		if( this.getName().equals( object.getName() ) ) { // name equality is treated as Item equality
			if( stackSize() < Stackable.maxDepth ) {
				if ( bc == null ) {
					object.setLocation( this.getDBRef() ); // the new location of the arrow in question is the old arrow
					bc = object;

					result = true;
				}
				else result = bc.stack(object);
			}
		}

		return result;
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
	public BottleCap getCopy() {
		return new BottleCap(this);
	}
	
	@Override
	public String toString() {
		return ( stackSize() > 0 ) ? getName() + " (" + stackSize() + ")" : getName();
	}
}