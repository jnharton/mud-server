package mud.objects;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;

import mud.Effect;
import mud.ObjectFlag;
import mud.Coins;
import mud.TypeFlag;

import mud.MUDObject;
import mud.SlotType;
import mud.magic.Spell;
import mud.objects.items.Attribute;
import mud.utils.Utils;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Item extends MUDObject {
	public boolean equippable = false; // is the item equippable?
	public boolean equipped = false;   // is the item equipped?
	
	boolean canAuction = true;

    private Coins baseCost = Coins.gold(1);
	public boolean drinkable = false;        // drinkable? (0 = no, 1 = yes)
	protected double weight = 0;             // the weight in whatever units are used of the equippable object

	// original idea was a multiplying factor for weight when wet such as
	// 1.0 - normal, 1.25 - damp, 1.50 - soaked, 2.00 - saturated, etc ("feels" x times as heavy)
	
	public double reduction_factor = 1.0; // amount of weight reduction (none by default, so 100% == 1)
	
	public boolean isAbsorb = true;       // does this item absorb water? (default: true)
	public boolean isWet = false;         // defines whether the item is wet or not (default: false)
	public double wet = 1.0;              // degree of water absorbed
	
	public int durability = 100;          // how durable the material is (100 is a test value)
	public int wear = 0;                  // how much wear and tear the item has been subject to
	
	public ItemType equip_type;           // equip type - armor, shield, jewelry, weapon
	protected ItemType item_type;         // item type - what type of item is this (supersede equip_type?)
	
	protected SlotType st;                // the type of slot this fits in (if any)
	protected int mod = 0;                // modifier - +0, +2, +3, +4, ... and so on
	
	protected BitSet attributes;          // item attributes: rusty, glowing, etc
	protected Attribute a;                // conflicting implementation with the above?
	
	protected List<Spell> spells;         // spells the item has, which can be cast from it
	
	/**
	 * Only for creating test items and then setting their properties/attributes
	 */
	public Item() {
		this.setFlags( EnumSet.of(ObjectFlag.ITEM) );
	}
	
	/**
	 * Only for sub-classes, so they can set a database reference number
	 * @param tempDBREF
	 */
	public Item(int tempDBREF) {
		super(tempDBREF);
		this.setFlags( EnumSet.of(ObjectFlag.ITEM) );
	}
	
	/**
	 * Create a new item with the same attributes as a template object
	 * 
	 * @param template
	 */
	public Item(Item template) {
		super(template.getDBRef());
		
		this.name = template.name;
		this.flags = template.flags;
		this.desc = template.desc;
		this.location = template.location;
	}

	public Item(int tempDBREF, String tempName, final EnumSet<ObjectFlag> tempFlags, String tempDesc, int tempLoc)
	{
		super(tempDBREF, tempName, tempFlags, tempDesc, tempLoc);
	}
	
	public ItemType getItemType() {
		return this.item_type;
	}
	
	public void setItemType(ItemType newType) {
		this.item_type = newType;
	}

	public Coins getCost() {
		return baseCost;
	}
	
	public int getWear() {
		return wear;
	}
	
	/**
	 * Calculate weight (in lbs?) as a double. This
	 * takes into account the weight of the water absorbed
	 * if the item is both absorbent and wet. Metal armor for
	 * instance should return it's weight, period, since it doesn't
	 * absorb water and therefore whether it is wet or not is mostly
	 * irrelevant to weight.
	 * 
	 * @return
	 */
	public Double getWeight() {
		if(isAbsorb) {
			if (isWet) {
				return (this.weight * reduction_factor) * wet;
			}
			else {
				return this.weight * reduction_factor;
			}
		}
		else {
			return this.weight * reduction_factor;
		}
	}
	
	public void setWeight(Double newWeight) {
		this.weight = newWeight;
	}
	
	public int getMod() {
		return this.mod;
	}
	
	public void setMod(int newMod) {
		this.mod = newMod;
	}
	
	public void setAttribute(Attribute newAttribute) {
		this.a = newAttribute;
	}
	
	public Attribute getAttribute(Attribute newAttribute) {
		return this.a;
	}
	
	/**
	 * Get Effective Item Level (EIL)
	 * @return
	 */
	public int getEIL() {
		return this.mod * 2;
	}
	
	public Spell getSpell() {
		return null;
	}

	public List<Spell> getSpells() {
		return this.spells;
	}
	
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";          // database reference number
		output[1] = this.getName();                // name
		output[2] = this.getFlagsAsString();       // flags
		output[3] = this.getDesc();                // description
		output[4] = this.getLocation() + "";       // location
		output[5] = this.item_type.ordinal() + ""; // item type
		output[6] = "*";                           // blank
		output[7] = "*";                           // blank
		return Utils.join(output, "#");
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}
