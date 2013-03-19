package mud.objects;

import java.util.BitSet;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.Coins;

import mud.MUDObject;
import mud.SlotType;
import mud.utils.Utils;

public class Item extends MUDObject {
	public boolean equippable = false; // is the item equippable?
	public boolean equipped = false;   // is the item equipped?

    private Coins baseCost = Coins.gold(1);
	public int drinkable = 0; // drinkable? (0 = no, 1 = yes)
	protected double weight = 0;             // the weight in whatever units are used of the equippable object

	// original idea was a multiplying factor for weight when wet such as
	// 1.0 - normal, 1.25 - damp, 1.50 - soaked, 2.00 - saturated, etc ("feels" x times as heavy)
	
	public double reduction_factor = 1.0; // amount of weight reduction (none by default, so 100% == 1)
	
	public boolean isAbsorb = true;       // does this item absorb water? (default: true)
	public boolean isWet = false;         // defines whether the item is wet or not (default: false)
	public double wet = 1.0;              // degree of water absorbed
	public int wear = 0;                  // how much wear and tear the item has been subject to (0 or negative)
	
	public ItemType equip_type;           // equip type - armor, shield, jewelry, weapon
	public ItemType item_type;            // item type - what type of item is this (supersede equip_type?)
	
	protected SlotType st;                // the type of slot this fits in (if any)
	protected int mod = 0;                // modifier - +0, +2, +3, +4, ... and so on
	
	protected BitSet attributes;          // item attributes: rusty, glowing, etc
	
	/**
	 * Only for creating test items and then setting their properties/attributes
	 */
	public Item() {
	}
	
	/**
	 * Only for sub-classes, so they can set a database reference number
	 * @param tempDBREF
	 */
	public Item(int tempDBREF) {
		super(tempDBREF);
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

	public void examine() {
        /*
		String temp = "";
		for (int f = 1; f < this.flags.length(); f++) {
			if (f < this.flags.length() - 1) { temp = temp + Flags.get(this.flags.charAt(f)) + " "; }
			else { temp = temp + Flags.get(this.flags.charAt(f)); }
		}
        */
		//send(this.name + "(#" + this.dbref + ")");
		//send("Type: " + Flags.get(this.flags.charAt(0)) + " Flags: " + temp);
		//send(this.desc);
	}
	
	public String[] examine1() {
		String dbrefString = this.name + "(#" + getDBRef() + ")";
        // just get first one..............
		String typeInfoString = "Type: " + ObjectFlag.firstInit(flags) + " Flags: " + ObjectFlag.toInitString(flags);
		String descString = this.desc;
		return new String[] { dbrefString, typeInfoString, descString };
	}
	
	public ItemType getItemType() {
		return this.item_type;
	}
	
	public void setItemType(ItemType newType) {
		this.item_type = newType;
	}

	//public abstract String getName();

	/*@Override
	public String getName() {
		Item item = null;
		if (this instanceof Weapon) { item = (Weapon) this; }
		else if (this instanceof Armor) { item = (Armor) this; }
		else if (this instanceof Shield) { item = (Shield) this; }
		else if (this instanceof Wand) { item = (Wand) this; }
		else if (this instanceof Clothing) { item = (Clothing) this; }
		else if (this instanceof Jewelry) { item = (Jewelry) this; }
		else if (this instanceof Arrow) { item = (Arrow) this; }

		if (item != null) { return item.getName(); }
		else { return this.name; }
	}*/

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
				return (this.weight * wet) * reduction_factor;
			}
			else {
				return this.weight * reduction_factor;
			}
		}
		else {
			return this.weight;
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
	
	/**
	 * Get Effective Item Level (EIL)
	 * @return
	 */
	public int getEIL() {
		return this.mod;
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
