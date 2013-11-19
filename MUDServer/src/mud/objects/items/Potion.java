package mud.objects.items;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;

import mud.Effect;
import mud.ObjectFlag;
import mud.TypeFlag;

import mud.interfaces.Stackable;
import mud.interfaces.Usable;

import mud.magic.Spell;

import mud.objects.Item;
import mud.objects.ItemType;

import mud.utils.Utils;

public class Potion extends Item implements Stackable<Potion>, Usable<Potion> {

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
	
	private Spell spell;               // a spell the potion will cast on you
	
	private Effect effect;             // an effect the potion will give you
	
	public ArrayList<Effect> effects; // multiple effects the potion will give you
	
	public Potion p = null;           // nested copy of itself, for implementing stacks
	
	//private static double weight = 0.5;

	public Potion() {
		super(-1, "Potion", EnumSet.noneOf(ObjectFlag.class), "An empty glass potion bottle.", 8);
		this.type = TypeFlag.ITEM;
		this.item_type = ItemType.POTION;
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
		this.type = TypeFlag.ITEM;
		this.item_type = ItemType.POTION;
		this.drinkable = true;
		this.effect = effect;
		this.weight = 0.5;
		
		this.effects = new ArrayList<Effect>();
		this.effects.add(effect);
	}

	public Potion(Spell spell) {
		super(-1, "Potion", EnumSet.noneOf(ObjectFlag.class), "A potion of " + spell.getName(), 8);
		this.type = TypeFlag.ITEM;
		this.item_type = ItemType.POTION;
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
		super(tempDBRef);
		
		this.name = tempName;
		this.desc = tempDesc;
		this.flags = tempFlags;
		this.location = tempLoc;

		/*String spellName = s;
		this.spell = parent.getSpell(spellName);*/

		String effectName = s;
		this.effect = new Effect(effectName);
		
		this.effects = new ArrayList<Effect>();
		this.effects.add(this.effect);

		this.item_type = ItemType.POTION;
		this.drinkable = true;
		
		this.weight = 0.5;
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

				while (qty < number) {
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
	public ArrayList<Effect> getEffects() {
		return new ArrayList<Effect>();
	}
	
	@Override
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";           // database reference number
		output[1] = this.getName();                 // name
		output[2] = this.getFlagsAsString();        // flags
		output[3] = this.getDesc();                 // description
		output[4] = this.getLocation() + "";        // location
		output[5] = this.item_type.ordinal() + "";  // item type
		output[6] = this.stackSize() + "";          // how many potion are stacked together
		if (this.effect == null) {
			if (this.spell != null) {
				output[7] = this.spell.getName();   // spell	
			}
			else {
                output[7] ="null";
            }
		}
		else { output[7] = this.effect.getName(); } // effect
		return Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		return this.name + " (" + this.stackSize() + ")";
	}
}