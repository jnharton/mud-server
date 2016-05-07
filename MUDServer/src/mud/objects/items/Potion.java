package mud.objects.items;

import java.util.ArrayList;
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
	
	public Potion p = null; // nested copy of itself, for implementing stacks
	
	private Spell spell;    // a spell the potion will cast on you
	private Effect effect;  // an effect the potion will give you

	public Potion() {
		super(-1, "Potion", EnumSet.noneOf(ObjectFlag.class), "An empty glass potion bottle.", 8);
		
		this.item_type = ItemTypes.POTION;
		
		this.drinkable = true;
		
		this.spell = null;
		this.effect = null;
		this.weight = 0.5;
		
		this.effects = new ArrayList<Effect>();
	}

	/*public Potion(String name, String desc, int location, int dbref) {
		super(dbref, name, "I", desc, location);
		this.item_type = ItemType.POTION;
		this.drinkable = 1;
		
		this.effects = new ArrayList<Effect>();
	}*/
	
	public Potion(Effect effect) {
		super(-1, "Potion", EnumSet.noneOf(ObjectFlag.class), "A potion of " + effect.getName(), 8);
		
		this.item_type = ItemTypes.POTION;
		
		this.drinkable = true;
		
		this.effect = effect;
		this.weight = 0.5;
		
		this.effects = new ArrayList<Effect>();
		this.effects.add(effect);
	}

	public Potion(Spell spell) {
		super(-1, "Potion", EnumSet.noneOf(ObjectFlag.class), "A potion of " + spell.getName(), 8);
		this.item_type = ItemTypes.POTION;
		
		this.drinkable = true;
		
		this.spell = spell;
		this.weight = 0.5;
		
		this.effects = new ArrayList<Effect>();
		
		/*for (Effect e : this.spell.getEffects()) {
			this.effects.add(e);
		}*/
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other two constructors
	 * that have parameters.
	 *
	 * 
	 * @param tempName
	 * @param tempDesc
	 * @param tempLoc
	 * @param tempDBREF
	 * @param stack_size
	 * @param spellName
	 */
	public Potion(int tempDBRef, String tempName, final EnumSet<ObjectFlag> tempFlags, String tempDesc, int tempLoc, String s) {
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc);
		
		this.item_type = ItemTypes.POTION;
		
		this.drinkable = true;
		
		/*String spellName = s;
		this.spell = parent.getSpell(spellName);*/

		String effectName = s;
		this.effect = new Effect(effectName);
		
		this.effects = new ArrayList<Effect>();
		this.effects.add(this.effect);
	}
	
	@Override
	public Double getWeight() {
		return 0.5;
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
		if (p == null) {
			p = object;
			return true;
		}
		else {
			return p.stack(object);
		}
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
	public String getName() {
		return this.name;
	}
	
	@Override
	public Spell getSpell() {
		// TODO Auto-generated method stub
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
		else { output[1] = this.effect.getName(); } // effect
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		return this.name + " (" + this.stackSize() + ")";
	}
}