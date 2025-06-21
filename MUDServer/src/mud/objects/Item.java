package mud.objects;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.game.Coins;
import mud.MUDObject;
import mud.magic.Enchantment;
import mud.misc.Script;
import mud.misc.SlotType;
import mud.misc.SlotTypes;
import mud.misc.Trigger;
import mud.misc.TriggerType;
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
	protected ItemType item_type = ItemTypes.NONE;  // item type - what type of item is this
	protected SlotType slot_type = SlotTypes.NONE;  // the type of slot this fits in (if any) -- used for Equippable
	
	protected Coins baseValue = Coins.gold(1);      // should be 'protected'?
	
	// game/system level "rules"
	protected boolean canAuction = true;      // allows/disallows auctioning this item (default: true)
	protected boolean canSell = true;         // allows/disallows selling this item (default: true)
	
    protected boolean unique = false;         // is this item Unique (only one of them, cannot be copied)
	
    // environment "rules"
	protected boolean isAbsorb = false;       // does this item absorb water? (default: false)
	protected boolean isWet = false;          // defines whether the item is wet or not (default: false)
	
	protected boolean reducesWeight = false;  // does this item reduce the weight of it's contents (default: false)
	protected double reduction_factor = 1.0;  // weight reduction multiplier (default: 1 = 100% of original weight)
	
	// properties?
	protected boolean drinkable = false;      // drinkable -- implies DRINK
    protected boolean edible = false;         // edible -- implies FOOD
    protected boolean equippable = false;     // equippable -- implies Equippable (default: false)
	
	// original idea was a multiplying factor for weight when wet such as
	// 1.0 - normal, 1.25 - damp, 1.50 - soaked, 2.00 - saturated, etc ("feels" x times as heavy)
	protected double wet_factor = 1.0;        // degree of water absorbed (default: 1 = 100% of original weight)
	
	protected int wear = 0;                   // how much wear and tear the item has been subject to
	protected int durability = 100;           // how durable the material is (100 is a test value) -- should be 'protected'?
	
	Enchantment enchant = null;
	
	protected boolean isEnchanted = false;
	
	// item attributes: rusty, glowing, etc ?
	
	//protected Hashtable<String, Integer> skill_buffs = new Hashtable<String, Integer>();
	
	public Trigger onUse = null;
	
	/**
	 * Only for sub-classes, so they can set a database reference number
	 * @param tempDBREF
	 */
	public Item(int tempDBREF) {
		super(tempDBREF);
		
		this.type = TypeFlag.ITEM;
	}
	
	public Item(final String name, final String description) {
		this(-1, name, description);
	}
	
	protected Item(int tempDBREF, final String name, final String description) {
		super(tempDBREF, name, description);
		
		this.type = TypeFlag.ITEM;
	}
	
	/**
	 * Copy Constructor
	 * 
	 * Create a new item with the same attributes as a template object
	 * 
	 * @param template
	 */
	protected Item(final Item template) {
		super(template);
		
		this.type = TypeFlag.ITEM;
		
		this.item_type = template.item_type;
		this.slot_type = template.slot_type;
		
		this.drinkable = template.drinkable;
		this.edible = template.edible;
		this.equippable = template.equippable;
		
		this.weight = template.weight;
		
		if( template.onUse != null ) {
			this.onUse = new Trigger( template.onUse.getScript().getText() );
		}
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Since Item is a subclass of object, this constructor mainly just sets the appropriate
	 * TypeFlag and passes the rest of the argument's to the superclass's object loading
	 * constructor
	 * 
	 * @param dbref
	 * @param name
	 * @param flags
	 * @param description
	 * @param location
	 */
	public Item(final int dbref, final String name, final EnumSet<ObjectFlag> flags, final String description, final int location)
	{
		super(dbref, name, flags, description, location);
		
		this.type = TypeFlag.ITEM;
	}
	
	public void setItemType(ItemType newType) {
		this.item_type = newType;
	}
	
	public ItemType getItemType() {
		return this.item_type;
	}
	
	public void setSlotType(final SlotType newType) {
		this.slot_type = newType;
	}
	
	public SlotType getSlotType() {
		return this.slot_type;
	}
	
	// TODO not setter for value
	public Coins getValue() {
		return this.baseValue;
	}
	
	// TODO not setter for durability
	public int getDurability() {
		return this.durability;
	}
	
	public void modifyWear(int change) {
		this.wear += change;
	}
	
	// TODO not setter for wear
	public int getWear() {
		return this.wear;
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
				return (this.weight * reduction_factor) * wet_factor;
			}
			else {
				return this.weight * reduction_factor;
			}
		}
		else {
			return this.weight * reduction_factor;
		}
	}
	
	public void setAbsorb(boolean absorb) {
	}
	
	public boolean isAbsorb() {
		return this.isAbsorb;
	}
	
	public boolean isWet() {
		return this.isWet;
	}
	
	public boolean isAuctionable() {
		return this.canAuction;
	}
	
	public void setAuctionable(boolean canAuction) {
		// boolean auctionable ?
		this.canAuction = canAuction;
	}
	
	public boolean isEquippable() {
		return this.equippable;
	}
	
	public boolean isDrinkable() {
		return this.drinkable;
	}
	
	/*public void setDrinkable(boolean drinkable) {
		this.drinkable = drinkable;
	}*/
	
	public boolean isEdible() {
		return this.edible;
	}
	
	public boolean isUnique() {
		return this.unique;
	}
	
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	
	public boolean isEnchanted() {
		//return this.isEnchanted;
		return ( enchant != null ) ? true : false;
	}
	
	/**
	 * Get Effective Item Level (EIL)
	 * @return
	 */
	public int getEIL() {
		// TODO fix this kludge
		return 0;
		//return this.mod * 2;
	}
	
	/* Scripting stuff */
	public void setScriptOnTrigger(TriggerType type, String script) {
		switch(type) {
		case onUse:
			onUse = new Trigger(script);
			break;
		default:
			break;
		}
	}
	
	public Script getScript(TriggerType type) {
		switch(type) {
		case onUse:
			return this.onUse.getScript();
		default:
			return null;
		}
	}
	
	public String toDB() {
		final String[] output = new String[7];
		
		output[0] = this.getDBRef() + "";        // database reference number
		output[1] = this.getName();              // name
		output[2] = type + getFlagsAsString();   // flags
		output[3] = this.getDesc();              // description
		output[4] = this.getLocation() + "";     // location
		
		output[5] = this.item_type.getId() + ""; // item type
		output[6] = this.slot_type.getId() + ""; // slot type
		
		return Utils.join(output, "#");
	}
	
	public Item getCopy() {
		return new Item(this);
	}
}