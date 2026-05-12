package mud.objects.items;

import mud.TypeFlag;
import mud.interfaces.Drinkable;
import mud.interfaces.Stackable;
import mud.misc.Trigger;
import mud.objects.Item;
import mud.objects.ItemTypes;

public class Drink extends Item implements Drinkable, Stackable<Drink> {
	private int sips = 5;
	
	private transient Drink drink = null;

	public Drink(int tempDBREF) {
		super(tempDBREF);
		
		this.drinkable = true;
		
		this.item_type = ItemTypes.DRINK;
	}
	
	public Drink(int tempDBREF, String name, String description) {
		super(tempDBREF, name, description);
		
		this.drinkable = true;
		
		this.item_type = ItemTypes.DRINK;
	}
	
	/**
	 * <b>Copy Constructor</b><br/><br/>
	 * 
	 * Create a new drink with the same attributes as a template object
	 * 
	 * @param template
	 */
	protected Drink(final Drink template) {
		super(template);
		
		this.item_type = ItemTypes.DRINK;
		
		this.sips = template.sips;
	}
	
	public void sip() {
		this.sips--;
	}
	
	public void tossdown() {
		this.sips = 0;
	}
	
	public int volume() {
		return this.sips;
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
		
		// split(0) would be meaningless, as would split(size of stack)
		if (number > 0 && stackSize() > number) {
			if (this.drink == null) {
				drink = this; // no stack if this item has no nested items
			}
			else {
				Drink prev = null; // the drink before the current one (initially null)
				Drink curr = this; // the current drink
				
				int qty = 0;       // the total quantity of drink, how we know if we have split off enough

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
	
	public Drink getCopy() {
		return new Drink(this);
	}
	
	public String toString() {
		return ( stackSize() > 0 ) ? getName() + " (" + stackSize() + ")" : getName();
	}
}