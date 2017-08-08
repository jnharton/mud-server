package mud.objects.items;

import mud.interfaces.Drinkable;
import mud.interfaces.Stackable;
import mud.objects.Item;

public class Drink extends Item implements Drinkable, Stackable<Drink> {
	private int sips = 1;
	
	private Drink drink = null;

	public Drink(int tempDBREF) {
		super(tempDBREF);
		
		this.drinkable = true;
	}
	
	public Drink(int tempDBREF, String name, String description) {
		super(tempDBREF, name, description);
		
		this.drinkable = true;
	}
	
	public void sip() {
		this.sips--;
	}
	
	public void tossdown() {
		this.sips = 0;
	}
	
	public boolean isEmpty() {
		return this.sips == 0;
	}

	@Override
	public boolean stack(Drink object) {
		boolean result = false;
		
		if( this.getName().equals( object.getName() ) ) { // name equality is treated as Item equality
			if( stackSize() < Stackable.maxDepth ) { 
				if (drink == null ) {
					object.setLocation( this.getDBRef() );
					drink = object;
					result = true;
				}
				else result = drink.stack(object);
			}
		}

		return result;
	}

	@Override
	public Drink split(int number) {
		Drink drink = null;
		
		if (number > 0 && stackSize() > number) {
			if (this.drink == null) {
				drink = this;
			}
			else {
				Drink prev = null; // the arrow before the current one (initially null)
				Drink curr = this; // the current arrow
				
				int qty = 0;       // the total quantity of arrows, how we know if we have split off enough

				while (qty < stackSize() - number) {
					prev = curr;
					curr = curr.drink;
					qty++;
				}

				prev.drink = null;
				
				drink = curr;
			}
		}
		
		return drink;
	}
	
	@Override
	/**
	 * Calculates and returns the size of the stack of arrows. I
	 * guess by definition, all arrows are technically stacks of
	 * arrows containing one ore more arrows. With the exception
	 * of the "there is only one" case, this uses recursion to
	 * count the arrows in the stack
	 * 
	 * @return int the size of the stack
	 */
	public int stackSize() {
		if (this.drink != null) {
			return 1 + this.drink.stackSize();
		}
		else {
			return 1;
		}
	}
	
	public String toString() {
		return ( stackSize() > 0 ) ? getName() + " (" + stackSize() + ")" : getName();
	}
}