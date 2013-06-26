package mud.objects.items;

import java.util.ArrayList;
import java.util.EnumSet;

import mud.Coins;
import mud.ObjectFlag;

import mud.interfaces.Equippable;
import mud.interfaces.Wieldable;

import mud.net.Client;

import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;

import mud.objects.items.Handed;
import mud.utils.Utils;

public class Weapon extends Item implements Equippable<Weapon>, Wieldable<Weapon>
{	
	// type - necklace, bracelet, ring, earring, tiara
	private WeaponType weapon_type;
	Handed handed;

	public Weapon() {
		super(-1, "Sword", EnumSet.of(ObjectFlag.ITEM), "A nice, shiny steel longsword.", 8);
		this.equippable = true;
		this.equip_type = ItemType.WEAPON;       // the type of equipment it is
		this.item_type = ItemType.WEAPON;
		this.mod = 0;
		this.handed = Handed.ONE;
		this.weapon_type = WeaponType.LONGSWORD; // weapon type
		this.weight = 7.0;                       // the weight of the weapon (lbs)
	}
	
	public Weapon( Weapon template ) {
		super(-1, template.name, template.flags, template.desc, template.location);
		this.equippable = true;
		this.equip_type = template.equip_type;
		this.item_type = template.item_type;
		this.mod = template.mod;
		this.handed = template.handed;
		this.weapon_type = template.weapon_type;
		this.weight = template.weight;
	}
	
	public Weapon(int wMod, Handed handed, WeaponType wType)
	{
		super(-1, wType.getName(), EnumSet.of(ObjectFlag.ITEM), "A nice, shiny steel longsword.", 8);
		this.equippable = true;
		this.equip_type = ItemType.WEAPON; // the type of equipment it is
		this.item_type = ItemType.WEAPON;
		this.mod = wMod;
		this.handed = handed;
		this.weapon_type = wType;   // the actual type of weapon
		this.weight = wType.getWeight(); // the weight of the weapon
	}

	public Weapon(int wMod, Handed handed, WeaponType wType, double wWeight)
	{
		super(-1, "Sword", EnumSet.of(ObjectFlag.ITEM), "A nice, shiny steel longsword.", 8);
		this.equippable = true;
		this.equip_type = ItemType.WEAPON; // the type of equipment it is
		this.item_type = ItemType.WEAPON;
		this.mod = wMod;
		this.handed = handed;
		this.weapon_type = wType;   // the actual type of weapon
		this.weight = wWeight; // the weight of the weapon
	}
	
	public Weapon(String wName, String wDesc, int wMod, Handed handed, WeaponType wType, double wWeight)
	{
		super(-1, wName, EnumSet.of(ObjectFlag.ITEM), wDesc, -1);
		this.equippable = true;
		this.equip_type = ItemType.WEAPON; // the type of equipment it is
		this.item_type = ItemType.WEAPON;
		this.mod = wMod;
		this.handed = handed;
		this.weapon_type = wType;   // the actual type of weapon
		this.weight = wWeight; // the weight of the weapon
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
	 * @param tCharges
	 * @param spellName
	 */
	public Weapon(String wName, String wDesc, int wLoc, int wDBREF, int wMod, Handed handed, WeaponType wType, double wWeight)
	{
		super(wDBREF, wName, EnumSet.of(ObjectFlag.ITEM), wDesc, wLoc);
		this.equippable = true;
		this.equip_type = ItemType.WEAPON; // the type of equipment it is
		this.item_type = ItemType.WEAPON;
		this.mod = wMod;
		this.handed = handed;
		this.weapon_type = wType;        // the actual type of weapon
		this.weight = wWeight;      // the weight of the weapon
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public Coins getCost() {
		return new Coins(weapon_type.getCost());
	}

	@Override
	public void equip() {
	}

	@Override
	public void equip(Player p) {
	}

	@Override
	public Weapon unequip() {
		return null;
	}

	@Override
	public void wield(String arg, Client client) {
	}
	
	public ArrayList<String> look() {
		return null;
	}
	
	public WeaponType getWeaponType() {
		return this.weapon_type;
	}
	
	@Override
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";            // database reference number
		output[1] = this.getName();                  // name
		output[2] = this.getFlagsAsString();         // flags
		output[3] = this.getDesc();                  // description
		output[4] = this.getLocation() + "";         // location
		output[5] = this.item_type.ordinal() + "";   // item type
		output[6] = this.weapon_type.ordinal() + ""; // weapon type
		output[7] = this.mod + "";                   // modifier
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