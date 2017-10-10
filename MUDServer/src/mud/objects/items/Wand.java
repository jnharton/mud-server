package mud.objects.items;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.interfaces.MagicItem;
import mud.magic.Spell;
import mud.misc.Effect;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;
import mud.utils.Utils;

public class Wand extends Item implements MagicItem {
	// wand should really have the itemType WAND, but it needs weapon right now, so that it can
	// fit in the slot it needs to be in

	public int charges;
	public Spell spell;
	
	// TODO is there a such a thing as a wand with no spell/charges or one with a generic spell/num charges?
	
	public Wand(final Spell spell, final int tCharges) {
		super(-1, "Wand", "A magic wand.");
		
		//super(-1, "Wand", EnumSet.noneOf(ObjectFlag.class), "A magic wand.", 8);
		this.item_type = ItemTypes.WAND;
		
		this.equippable = true;
		
		this.charges = tCharges;
		this.spell = spell;

		//this.name = "Wand of " + this.spell.getName();
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other two constructors
	 * that have parameters.
	 * @param dbref
	 * @param name
	 * @param description
	 * @param flags TODO
	 * @param location
	 * @param tCharges
	 * @param spell
	 */
	public Wand(int dbref, String name, String description, EnumSet<ObjectFlag> flags, int location, ItemType itemType, int tCharges, Spell spell) {
		super(dbref, name, EnumSet.noneOf(ObjectFlag.class), description, location);
		
		this.item_type = ItemTypes.WAND;
		
		this.equippable = true;
		
		this.charges = tCharges;
		this.spell = spell;
		
		//this.name = "Wand of " + this.spell.getName();
	}
	
	protected Wand(final Wand template) {
		super(template);
		
		this.item_type = ItemTypes.WAND;
		
		this.equippable = true;
		
		this.charges = template.charges;
		this.spell = template.getSpell();

		//this.name = "Wand of " + this.spell.getName();
	}
	
	// TODO this kind of data should be accessible somehow
	/*
	@Override
	public String getDesc() {
		final String chargeState;
		
		if(this.charges >= 50) {
			chargeState = "fully charged";
		}
		else if(this.charges >= 12 && this.charges <= 25) {
			chargeState = "about half charged";
		}
		else if(this.charges < 12) {
			chargeState = "about a quarter charged";
		}
		else {
			chargeState = "dead, kaput, useless";
		}
		
		return this.desc + "\nThis wand is " + chargeState;
	}
	*/
	
	@Override
	public Spell getSpell() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Spell> getSpells() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*public Effect getEffect() {
		// TODO Auto-generated method stub
		this.spell.getEffects()
	}*/

	public String toDB() {
		final String[] output = new String[2];
		
		if (this.spell != null) output[0] = this.spell.getName(); // spell name	
		else                    output[0] = "null";
		
		//output[0] = this.spell.getName(); // spell name
		output[1] = this.charges + "";    // spell charges
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		// TODO fix this nonsense, since a wand might not have a spell set...
		if (this.spell != null) return "Wand of " + this.spell.getName();
		else                    return "Wand of NULL";
		// return "Wand of " + this.spell.getName();
	}
	
	@Override
	public Wand getCopy() {
		return new Wand(this);
	}

	@Override
	public Effect getEffect() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Effect> getEffects() {
		return this.spell.getEffects();
	}
}