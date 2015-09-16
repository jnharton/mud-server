package mud.objects;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.MUDObject;
import mud.interfaces.Stackable;
import mud.magic.Spell;
import mud.misc.Coins;
import mud.misc.Effect;
import mud.misc.Script;
import mud.misc.Slot;
import mud.misc.SlotType;
import mud.misc.SlotTypes;
import mud.misc.Trigger;
import mud.misc.TriggerType;
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

public class Item extends MUDObject implements Cloneable {
	protected ItemType item_type = ItemTypes.NONE;  // item type - what type of item is this (supersede equip_type?)
	protected ItemType equip_type = ItemTypes.NONE; // equip type - armor, shield, jewelry, weapon
	protected SlotType slot_type = SlotTypes.NONE;  // the type of slot this fits in (if any)
	
	public Coins baseValue = Coins.gold(1);         // should be 'protected'?
	
	protected double weight = 0;                    // the weight in whatever units are used of the equippable object
	
	// game/system level "rules"
	protected boolean equippable = false;     // is the item equippable? (default: false)
	protected boolean equipped = false;       // is the item equipped? (default: false)
	
	protected boolean canAuction = true;      // allows/disallows auctioning this item (default: true)
    
	protected boolean drinkable = false;      // drinkable? (0 = no, 1 = yes) -- implies DRINK
    protected boolean edible = false;         // edible? -- implies FOOD
	
    protected boolean unique = false;         // is this item Unique (only one of them, cannot be copied)
	
    // environment "rules"
	protected boolean isAbsorb = true;        // does this item absorb water? (default: true)
	protected boolean reducesWeight = false;  // does this item reduce the weight of it's contents (default: false)
	protected boolean isWet = false;          // defines whether the item is wet or not (default: false)
	
	public double reduction_factor = 1.0;     // amount of weight reduction (none by default, so 100% == 1) -- should be 'protected'?
	
	// original idea was a multiplying factor for weight when wet such as
	// 1.0 - normal, 1.25 - damp, 1.50 - soaked, 2.00 - saturated, etc ("feels" x times as heavy)
	public double wet = 1.0;                  // degree of water absorbed -- should be 'protected'?
	
	public int wear = 0;                      // how much wear and tear the item has been subject to
	public int durability = 100;              // how durable the material is (100 is a test value) -- should be 'protected'?
	
	//protected BitSet attributes;              // item attributes: rusty, glowing, etc
	protected Attribute a;                    // conflicting implementation with the above?
	
	protected List<Effect> effects;           // effects
	protected List<Spell> spells;             // spells the item has, which can be cast from it
	
	//protected Map<String, Slot> slots = null; // handles objects which hold specific things, like sheaths for swords
	
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
	
	public Item(int tempDBREF, final String name, final String description) {
		super(tempDBREF);
		
		this.type = TypeFlag.ITEM;
		
		this.name = name;
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		this.desc = description;
		this.location = -1; // ? 
	}
	
	/**
	 * Copy Constructor
	 * 
	 * Create a new item with the same attributes as a template object
	 * 
	 * @param template
	 */
	protected Item(Item template) {
		super( template );
		
		this.type = TypeFlag.ITEM;
		
		this.item_type = template.item_type;
		this.equip_type = template.equip_type;
		
		this.slot_type = template.slot_type;
		
		this.equipped = false;
		this.equippable = template.equippable;
		this.drinkable = template.drinkable;
		
		this.weight = template.weight;
		
		System.out.println("Cloning... " + template.getName());
		
		if( template.onUse != null ) {
			this.onUse = new Trigger( template.onUse.getScript().getText() );
			System.out.println("Item (onUse Script): " + this.getScript(TriggerType.onUse).getText());
		}
		else {
			System.out.println("Item (onUse Script): No Trigger/No Script!");
		}
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Since Item is a subclass of object, this constructor mainly just sets the appropriate
	 * TypeFlag and passes the rest of the argument's to the superclass's object loading
	 * constructor
	 * 
	 * @param tempDBREF
	 * @param tempName
	 * @param tempFlags
	 * @param tempDesc
	 * @param tempLoc
	 */
	public Item(final int tempDBREF, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc)
	{
		super(tempDBREF, tempName, tempFlags, tempDesc, tempLoc);
		this.type = TypeFlag.ITEM;
	}
	
	/*public void setItemType(ItemType newType) {
		this.item_type = newType;
	}*/
	
	public ItemType getItemType() {
		return this.item_type;
	}
	
	public void setEquipType(ItemType newType) {
		this.equip_type = newType;
	}
	
	public ItemType getEquipType() {
		return this.item_type;
	}
	
	public void setSlotType(SlotType newType) {
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
	
	// TODO not setter for wear
	public int getWear() {
		return this.wear;
	}
	
	public void setWeight(Double newWeight) {
		this.weight = newWeight;
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
	
	public void setAbsorb(boolean absorb) {
	}
	
	public boolean isAbsorb() {
		return this.isAbsorb;
	}
	
	public boolean isWet() {
		return this.isWet;
	}
	
	public void setAuctionable(boolean canAuction) {
		this.canAuction = canAuction;
	}
	
	public boolean isAuctionable() {
		return canAuction;
	}
	
	public void setEquippable(boolean canEquip) {
		this.equippable = canEquip;
	}
	
	public boolean isEquippable() {
		return this.equippable;
	}
	
	public void setEquipped(boolean equipped) {
		this.equipped = equipped;
	}
	
	public boolean isEquipped() {
		return this.equipped;
	}
	
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	
	public boolean isUnique() {
		return this.unique;
	}
	
	public boolean isEdible() {
		return this.edible;
	}
	
	/**
	 * @param drinkable the drinkable to set
	 */
	public void setDrinkable(boolean drinkable) {
		this.drinkable = drinkable;
	}

	public boolean isDrinkable() {
		return this.drinkable;
	}
	
	// TODO decide if I should have a single or fixed set of attributes and whether to store it in a bitset or an array or ?
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
		// TODO fix this kludge
		return 0;
		//return this.mod * 2;
	}
	
	public Spell getSpell() {
		if( this.spells.size() >= 1 ) {
			return this.spells.get(0);
		}
		
		return null;
	}

	public List<Spell> getSpells() {
		return this.spells;
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
		String[] output = new String[10];
		
		output[0] = this.getDBRef() + "";           // database reference number
		output[1] = this.getName();                 // name
		output[2] = TypeFlag.asLetter(type) + "";   // flags
		output[2] = output[2] + getFlagsAsString();
		output[3] = this.getDesc();                 // description
		output[4] = this.getLocation() + "";        // location
		
		output[5] = this.item_type.getId() + "";    // item type
		output[6] = this.equip_type.getId() + "";   // equip type
		output[7] = this.slot_type.getId() + "";    // slot type
		
		output[8] = "*";                            // blank
		output[9] = "*";                            // blank
		
		return Utils.join(output, "#");
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MUDObject fromJSON() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Item clone() {
		return new Item(this);
	}
}