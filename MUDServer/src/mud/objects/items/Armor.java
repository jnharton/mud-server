package mud.objects.items;

import java.util.ArrayList;
import java.util.EnumSet;

import mud.ObjectFlag;

import mud.interfaces.Equippable;
import mud.interfaces.Wearable;

import mud.net.Client;

import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;
import mud.utils.Utils;

/**
 * 
 * @author Jeremy
 *
 * @param aMod Armor bonus modifier (integer)
 * @param aGroup err, no idea.
 */
public class Armor extends Item implements Equippable<Armor>, Wearable<Armor>
{
	/**
	 * Flag: I
	 * ItemType: Armor
	 */

	// weight - light, medium, heavy
	//private String group = "";
	public ArmorType armor;

	public Armor() {
	}
	
	/**
	 * Armor - Copy Constructor
	 * 
	 * @see "Use this to make an identical copy of another armor. Just
	 * remember to change the database reference, etc accordingly"
	 * 
	 * @param template the armor to copy
	 */
	public Armor( Armor template ) {
		super(-1, template.name, template.flags, template.desc, template.location);
		this.equippable = true;
		this.equip_type = template.equip_type;
		this.item_type = template.item_type;
		this.mod = template.mod;
		this.armor = template.armor;
		this.weight = template.weight;
	}
	
	public Armor(int aMod, ArmorType armor)
	{
		super(-1, armor.getName().toLowerCase(), EnumSet.of(ObjectFlag.ITEM), "armor", 8);
		this.equippable = true;
		this.equip_type = ItemType.ARMOR;
		this.item_type = ItemType.ARMOR;
		this.mod = aMod;
		this.armor = armor;
		this.weight = armor.getWeight();
	}
	
	public Armor(int dbref, int aMod, ArmorType armor)
	{
		super(dbref, armor.getName().toLowerCase(), EnumSet.of(ObjectFlag.ITEM), "armor", 8);
		this.equippable = true;
		this.equip_type = ItemType.ARMOR;
		this.item_type = ItemType.ARMOR;
		this.mod = aMod;
		this.armor = armor;
		this.weight = armor.getWeight();
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
	 */
	public Armor(String wName, String wDesc, int wLoc, int wDBREF, int aMod, ArmorType armor, ItemType item)
	{
		super(wDBREF, wName, EnumSet.of(ObjectFlag.ITEM), wDesc, wLoc);
		this.equippable = true;
		this.equip_type = ItemType.ARMOR;
		
		this.mod = aMod;
		this.armor = armor;
		this.item_type = item;
		
		this.weight = armor.getWeight();
	}

	@Override
	public void equip() {
	}

	@Override
	public void equip(Player p) {
	}

	@Override
	public Armor unequip() {
		return null;
	}
	
	public ArrayList<String> look() {
		return null;
	}

	@Override
	public void wear(String arg, Client client) {
	}
	
	@Override
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";          // database reference number
		output[1] = this.getName();                // name
		output[2] = this.getFlagsAsString();       // flags
		output[3] = this.getDesc();                // description
		output[4] = this.getLocation() + "";       // location
		output[5] = this.item_type.ordinal() + ""; // item type
		output[6] = this.armor.ordinal() + "";     // armor type
		output[7] = this.mod + "";                 // modifier
		return Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		if (this.mod > 0) {
			return "+" + this.mod + " " + this.getName();
		}
		else if (this.mod < 0) {
			return this.mod + " " + this.getName();
		}
		else {
			return this.getName();
		}
	}
}