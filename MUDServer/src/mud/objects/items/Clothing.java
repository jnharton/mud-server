package mud.objects.items;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;

import mud.Effect;
import mud.ObjectFlag;

import mud.interfaces.Equippable;
import mud.interfaces.Usable;
import mud.interfaces.Wearable;

import mud.magic.Spell;
import mud.net.Client;

import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;

import mud.utils.Utils;

public class Clothing extends Item implements Equippable<Clothing>, Usable<Clothing>, Wearable<Clothing>
{
	/**
	 * Additional stuff that I need to figure into the persistence:
	 * - modifiers
	 * - type
	 * - some kind of prototype object id (so I can just say, go find that object and use
	 * it's properties)
	 * 	- this would mean I'd need prototype objects either elsewhere or that are normally loaded
	 *  - possibly I could add a static array/table of some kind that I will modify on startup
	 *  to include all possible prototypes (i.e. the ones that actually exist)
	 */

	// type - cloak, boots, pants, shirt, undergarment
	public ClothingType clothing;
	
	private Effect effect = null;
	
	/**
	 * default constructor for subclassing?
	 */
	public Clothing() {
	}

	public Clothing(String name, int cMod, ClothingType cType, double cWeight)
	{
		super(-1, name, EnumSet.of(ObjectFlag.ITEM), "<TESTING>", 8);
		this.equippable = true;
		this.equip_type = ItemType.CLOTHING; // the type of equipment it is
		this.item_type = ItemType.CLOTHING;
		this.mod = cMod;
		this.clothing = cType;               // the actual type of clothing
		this.weight = cWeight;               // the weight of the clothing
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other constructors
	 * that has parameters.
	 * @param tempDBREF
	 * @param tempName
	 * @param tempDesc
	 * @param tempLoc
	 * @param tCharges
	 * @param spellName
	 */
	public Clothing(int tempDBREF, String tempName, String tempDesc, int tempLoc, int cMod, ClothingType cType) {
		super(tempDBREF, tempName, EnumSet.of(ObjectFlag.ITEM), tempDesc, tempLoc);
		this.equippable = true;
		this.equip_type = ItemType.CLOTHING;
		this.item_type = ItemType.CLOTHING;
		
		this.clothing = cType;
	}

	@Override
	public void equip() {
	}

	@Override
	public void equip(Player p) {
	}

	@Override
	public Clothing unequip() {
		return null;
	}
	
	public void wear(String arg, Client client) {
		// TODO Auto-generated method stub
	}
	
	public Effect getEffect() {
		return this.effect;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";          // clothing database reference number
		output[1] = this.getName();                // clothing name
		output[2] = this.getFlagsAsString();       // clothing flags
		output[3] = this.getDesc();                // clothing description
		output[4] = this.getLocation() + "";       // clothing location (a.k.a parent)
		output[5] = this.item_type.ordinal() + ""; // item type
		output[6] = this.clothing.ordinal() + "";  // clothing type
		output[7] = this.mod + "";                 // modifier
		return Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		//return this.mod + " " + this.name;
		return getName();
	}
}