package mud.objects.items;

import java.util.EnumSet;

import mud.Coins;
import mud.ObjectFlag;
import mud.TypeFlag;
import mud.game.Skill;
import mud.interfaces.Equippable;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;
import mud.objects.items.Handed;
import mud.utils.Tuple;
import mud.utils.Utils;

public class Weapon extends Item implements Cloneable
{
	private WeaponType weapon_type = null;
	Handed handed;
	
	protected Skill required_skill;
	protected int req_skill_value;

	public Weapon() {
		super(-1, "Sword", EnumSet.noneOf(ObjectFlag.class), "A nice, shiny steel longsword.", 8);
		this.type = TypeFlag.ITEM;
		this.equippable = true;
		this.equip_type = ItemType.WEAPON;       // the type of equipment it is
		this.item_type = ItemType.WEAPON;
		this.mod = 0;
		this.handed = Handed.ONE;
		this.weapon_type = WeaponTypes.LONGSWORD; // weapon type
		this.weight = 7.0;                       // the weight of the weapon (lbs)
	}
	
	public Weapon(int wMod, Handed handed, WeaponType wType)
	{
		super(-1, wType.getName(), EnumSet.noneOf(ObjectFlag.class), "A nice, shiny steel longsword.", 8);
		this.type = TypeFlag.ITEM;
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
		super(-1);
		
		this.name = "Sword";
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		this.desc = "A nice, shiny steel longsword.";
		this.location = 8;
		
		this.type = TypeFlag.ITEM;
		this.equippable = true;
		this.equip_type = ItemType.WEAPON; // the type of equipment it is
		this.item_type = ItemType.WEAPON;
		this.mod = wMod;
		this.handed = handed;
		this.weapon_type = wType;   // the actual type of weapon
		this.weight = wWeight; // the weight of the weapon
	}
	
	public Weapon(String wName, String wDesc, double wWeight) {
		super(-1);
		
		this.type = TypeFlag.ITEM;
		this.name = wName;
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		this.desc = wDesc;
		this.location = -1;
		
		this.equippable = true;
		this.equip_type = ItemType.WEAPON; // the type of equipment it is
		this.item_type = ItemType.WEAPON;
		this.weight = wWeight;
	}
	
	public Weapon(String wName, String wDesc, int wMod, Handed handed, WeaponType wType, double wWeight)
	{
		this(wName, wDesc, wWeight);
		
		this.mod = wMod;
		this.handed = handed;
		this.weapon_type = wType;   // the actual type of weapon
	}
	
	/**
	 * Copy Constructor
	 * 
	 * @param template
	 */
	protected Weapon( Weapon template ) {
		//super(-1, template.name, template.flags, template.desc, template.location);
		super( template );
		this.type = TypeFlag.ITEM;
		this.equippable = true;
		this.equip_type = template.equip_type;
		this.item_type = template.item_type;
		this.mod = template.mod;
		this.handed = template.handed;
		this.weapon_type = template.weapon_type;
		this.weight = template.weight;
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other two constructors
	 * that have parameters.
	 *
	 * 
	 * @param wName
	 * @param wDesc
	 * @param wLoc
	 * @param wDBREF
	 */
	public Weapon(String wName, String wDesc, int wLoc, int wDBREF, int wMod, Handed handed, WeaponType wType, double wWeight)
	{
		super(wDBREF, wName, EnumSet.noneOf(ObjectFlag.class), wDesc, wLoc);
		this.item_type = ItemType.WEAPON;
		
		this.equippable = true;
		this.equip_type = ItemType.WEAPON; // the type of equipment it is
		
		this.mod = wMod;
		this.handed = handed;
		this.weapon_type = wType; // the actual type of weapon
		this.weight = wWeight;    // the weight of the weapon
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public Coins getValue() {
		return new Coins(weapon_type.getCost());
	}
	
	public void setRequiredSkill(Skill required, int value) {
		this.required_skill = required;
		this.req_skill_value = value;
	}
	
	public Tuple<Skill, Integer> getRequiredSkill() {
		return new Tuple<Skill, Integer>(this.required_skill, this.req_skill_value);
	}
	
	public boolean isSkillRequired() {
		if( required_skill == null ) return false;
		else return true;
	}
	
	public void setWeaponType(WeaponType wType) {
		this.weapon_type = wType;
	}
	
	public WeaponType getWeaponType() {
		return this.weapon_type;
	}
	
	@Override
	public Weapon clone() {
		return new Weapon(this);
	}
	
	@Override
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";              // database reference number
		output[1] = this.getName();                    // name
		output[2] = TypeFlag.asLetter(this.type) + ""; // flags
		output[2] = output[2] + getFlagsAsString();
		output[3] = this.getDesc();                    // description
		output[4] = this.getLocation() + "";           // location
		output[5] = this.item_type.ordinal() + "";     // item type
		//output[6] = this.weapon_type.getId() + "";     // weapon type
		output[6] = "" + 0;
		output[7] = this.mod + "";                     // modifier
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