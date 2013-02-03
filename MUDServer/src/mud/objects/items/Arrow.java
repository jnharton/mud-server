package mud.objects.items;

import mud.interfaces.Projectile;
import mud.interfaces.Stackable;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.utils.Utils;
import mud.ObjectFlag;

import java.util.EnumSet;

public class Arrow extends Item implements  Projectile<Arrow>, Stackable<Arrow> {
	private Arrow a;

	public Arrow() {
	}

	public Arrow(int dbref, String name, String desc, int location) {
		super(dbref, name, EnumSet.of(ObjectFlag.ITEM), desc, location);
		this.item_type = ItemType.ARROW;
	}
	
	public int size() {
		return stackSize();
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
		if (this.a != null) {
			return 1 + a.stackSize();
		}
		else {
			return 1;
		}
	}

	@Override
	public boolean stack(Arrow object) {
		if (a == null) {
			a = object;
			return true;
		}
		else {
			return a.stack(object);
		}
	}

	@Override
	public Arrow split(int number) {
		if (number > 0 && stackSize() > number) {
			if (a == null) {
				return this;
			}
			else {
				Arrow prev = null; // the arrow before the current one (initially null)
				Arrow curr = this; // the current arrows

				int qty = 0;       // the total quantity of arrows, how we know if we have split off enough

				while (qty < number) {
					prev = curr;
					curr = curr.a;
					qty++;
				}

				prev.a = null;

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
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";          // database reference number
		output[1] = this.getName();                      // name
		output[2] = this.getFlagsAsString();                     // flags
		output[3] = this.getDesc();                      // description
		output[4] = this.getLocation() + "";       // location
		output[5] = this.item_type.ordinal() + ""; // item type
		output[6] = this.stackSize() + "";         // how many arrows are stacked together
		output[7] = "*";                                 // blank
		
		/*
		 * recording stacks of these is messy
		 * 
		 * 241#Flaming Arrow#I#A flaming arrow.#8#1#5#*
		 * 
		 * should description be dynamic, and indicate the number of arrows
		 */
		
		return Utils.join(output, "#");
	}
}