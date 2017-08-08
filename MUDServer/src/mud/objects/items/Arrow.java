package mud.objects.items;

import mud.interfaces.Projectile;
import mud.interfaces.Stackable;
import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.utils.Utils;
import mud.ObjectFlag;

import java.util.EnumSet;

public class Arrow extends Item implements Projectile, Stackable<Arrow> {
	private Arrow arrow = null;
	
	public Arrow() {
		super(-1, "Arrow", "");
		
		this.item_type = ItemTypes.ARROW;
		
		this.arrow = null;
	}

	public Arrow(final String name, final String desc) {
		super(-1, name, desc);
		
		this.item_type = ItemTypes.ARROW;
		
		this.arrow = null;
	}
	
	protected Arrow(final Arrow template) {
		super(template);
		
		this.location = template.location;
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		
		this.item_type = ItemTypes.ARROW;
		
		this.arrow = null;
	}
	
	public Arrow(final int dbref, final String name, final EnumSet<ObjectFlag> flags, final String description, final int location) {
		super(dbref, name, EnumSet.noneOf(ObjectFlag.class), description, location);
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
		if (this.arrow != null) {
			return 1 + this.arrow.stackSize();
		}
		else {
			return 1;
		}
	}

	@Override
	public boolean stack(Arrow object) {
		boolean result = false;
		
		if( this.getName().equals( object.getName() ) ) { // name equality is treated as Item equality
			if( stackSize() < Stackable.maxDepth ) {
				if (arrow == null ) {
					object.setLocation( this.getDBRef() ); // the new location of the arrow in question is the old arrow
					arrow = object;
					
					result = true;
				}
				else result = arrow.stack(object);
			}
		}

		return result;
	}

	@Override
	public Arrow split(int number) {
		if (number > 0 && stackSize() > number) {
			if (arrow == null) {
				return this;
			}
			else {
				Arrow prev = null; // the arrow before the current one (initially null)
				Arrow curr = this; // the current arrow
				
				int qty = 0;       // the total quantity of arrows, how we know if we have split off enough

				while (qty < stackSize() - number) {
					prev = curr;
					curr = curr.arrow;
					qty++;
				}

				prev.arrow = null;
				
				return curr;
			}
		}
		else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return name + " (" + stackSize() + ")";
	}

	@Override
	public String toDB() {
		/*
		 * recording stacks of these is messy
		 * 
		 * 241#Flaming Arrow#I#A flaming arrow.#8#1#5#*
		 * 
		 * should description be dynamic, and indicate the number of arrows
		 * 
		 * data for whether a thing is a stack
		 * data for whether a thing is in a stack?
		 */
		
		final String[] output = new String[2];
		
		output[0] = this.stackSize() + ""; // how many arrows are stacked together
		output[1] = "*";                   // blank
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	public Arrow getCopy() {
		return new Arrow(this);
	}
}