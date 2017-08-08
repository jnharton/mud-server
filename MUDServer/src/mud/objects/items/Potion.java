package mud.objects.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.EnumSet;

import mud.ObjectFlag;

import mud.interfaces.Drinkable;
import mud.interfaces.MagicItem;
import mud.interfaces.Stackable;

import mud.magic.Spell;
import mud.misc.Effect;
import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.utils.Utils;

public class Potion extends Item implements Drinkable, MagicItem, Stackable<Potion> {
	/**
	 * Flag: I
	 * ItemType: Potion
	 */

	/* minor problem in that it is silly to have a potion or a spell or both,
	 * really I should just have one. The point is that you can drink a potion
	 * to get a result. I suppose a potion of say polymorph should not allow
	 * you to choose, but that should be decided by the potion makers such
	 * that it is a potion of 'polymorph to cat' for instance.
	 */
	
	public Potion p;         // nested copy of itself, for implementing stacks
	
	private Spell spell;    // a spell the potion will cast on you
	private Effect effect;  // an effect the potion will give you

	public Potion() {
		super(-1, "Potion", "An empty glass potion bottle.");
		
		this.item_type = ItemTypes.POTION;
		
		this.drinkable = true;
		
		this.p = null;
		
		this.spell = null;
		this.effect = null;
	}
	
	public Potion(final Effect effect) {
		super(-1, "Potion", "A potion of " + effect.getName());
		
		this.item_type = ItemTypes.POTION;
		
		this.drinkable = true;
		
		this.p = null;
		
		this.spell = null;
		this.effect = effect;
	}

	public Potion(final Spell spell) {
		super(-1, "Potion", "A potion of " + spell.getName());
		
		this.item_type = ItemTypes.POTION;
		
		this.drinkable = true;
		
		this.p = null;
		
		this.spell = spell;
		this.effect = null;
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other two constructors
	 * that have parameters.
	 *
	 * 
	 * @param name
	 * @param description
	 * @param location
	 * @param tempDBREF
	 * @param stack_size
	 * @param spellName
	 */
	public Potion(int dbref, String name, final EnumSet<ObjectFlag> flags, String description, int location) {
		super(dbref, name, flags, description, location);

		this.item_type = ItemTypes.POTION;
		
		this.drinkable = true;

		this.p = null;

		this.spell = null;
		this.effect = null;
	}
	
	@Override
	public Double getWeight() {
		return 0.5;
	}
	
	@Override
	public void setWeight(Double newWeight) {
		// do nothing
	}
	
	@Override
	public List<Effect> getEffects() {
		return Collections.unmodifiableList(new ArrayList<Effect>());
	}
	
	public int size() {
		return stackSize();
	}

	@Override
	public int stackSize() {
		if (this.p != null) {
			return 1 + p.stackSize();
		}
		else {
			return 1;
		}
	}

	@Override
	public boolean stack(Potion object) {
		boolean result = false;

		if( this.getName().equals( object.getName() ) ) { // name equality is treated as Item equality
			if( stackSize() < Stackable.maxDepth ) {
				if ( p == null ) {
					object.setLocation( this.getDBRef() ); // the new location of the arrow in question is the old arrow
					p = object;

					result = true;
				}
				else result = p.stack(object);
			}
		}

		return result;
	}

	@Override
	public Potion split(int number) {
		if (number > 0 && stackSize() > number) {
			if (p == null) {
				return this;
			}
			else {
				Potion prev = null;
				Potion curr = this;

				int qty = 0;

				while (qty < stackSize() - number) {
					prev = curr;
					curr = curr.p;
					qty++;
				}

				prev.p = null;

				return curr;
			}
		}
		else {
			return null;
		}
	}
	
	@Override
	public Spell getSpell() {
		return this.spell;
	}

	@Override
	public List<Spell> getSpells() {
		return new ArrayList<Spell>();
	}

	@Override
	public Effect getEffect() {
		return this.effect;
	}
	
	@Override
	public String toDB() {
		final String[] output = new String[2];
		
		output[0] = this.stackSize() + "";          // how many potion are stacked together
		
		if (this.effect == null) {
			if (this.spell != null) output[1] = this.spell.getName(); // spell	
			else                    output[1] = "null";
		}
		else {
			output[1] = this.effect.getName();
		}
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		return this.name + " (" + this.stackSize() + ")";
	}
}