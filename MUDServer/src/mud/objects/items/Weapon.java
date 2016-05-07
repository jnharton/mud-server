package mud.objects.items;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.game.Skill;
import mud.misc.Coins;
import mud.misc.SlotTypes;
import mud.objects.Item;
import mud.objects.ItemTypes;

import mud.utils.Tuple;
import mud.utils.Utils;

public class Weapon extends Item {
	private WeaponType weapon_type = null;
	
	private Skill required_skill;
	private int req_skill_value;
	
	private int mod = 0;                    // modifier - +0, +2, +3, +4, ... and so on
	
	public int damage = 1;
	
	/**
	 * Default Constructor
	 */
	public Weapon() {
		this("weapon", "A generic weapon.");
	}
	
	public Weapon(final String name) {
		this(name, "A generic weapon");
	}
	
	public Weapon(final String name, final String description) {
		this(name, description, 1.0);
	}
	
	public Weapon(final String wName, final String wDesc, final double wWeight) {
		super(-1, wName, wDesc);
		
		this.item_type = ItemTypes.WEAPON;
		this.slot_type = SlotTypes.NONE;
		
		this.equippable = true;
		
		this.weight = wWeight;
	}
	
	/**
	 * Template Constructor
	 * 
	 * Create a Weapon object using the WeaponType as a template
	 * 
	 * @param wType
	 */
	public Weapon(final WeaponType wType) {
		super(-1, wType.getName(), "");
		
		this.item_type = ItemTypes.WEAPON;
		this.slot_type = SlotTypes.NONE;
		
		this.equippable = true;
		
		this.weapon_type = wType;
		
		//this.mod = wMod;
		
		this.weight = wType.getWeight(); // the weight of the weapon
	}
	
	/**
	 * Copy Constructor
	 * 
	 * @param template
	 */
	protected Weapon(final Weapon template) {
		super( template );
		
		this.weapon_type = template.weapon_type;
		
		this.required_skill = template.required_skill;
		this.req_skill_value = template.req_skill_value;
		
		this.mod = template.getModifer();
		
		this.damage = template.damage;
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other two constructors
	 * that have parameters.
	 * @param wDBREF
	 * @param wName
	 * @param wDesc
	 * @param wLoc
	 */
	public Weapon(final int wDBREF, final String wName, final String wDesc, final int wLoc, final WeaponType wType)
	{
		super(wDBREF, wName, EnumSet.noneOf(ObjectFlag.class), wDesc, wLoc);
		
		this.item_type = ItemTypes.WEAPON;
		this.slot_type = SlotTypes.NONE;
		
		this.equippable = true;
		
		this.weapon_type = wType; // the actual type of weapon
	}

	public String getName() {
		return this.name;
	}
	
	@Override
	public Coins getValue() {
		if( this.weapon_type != null ) return new Coins(weapon_type.getCost());
		else                           return Coins.gold(5);
	}
	
	public Double getWeight() {
		if( this.weapon_type != null ) return weapon_type.getWeight();
		else                           return 1.0;
	}
	
	public void setWeaponType(WeaponType wType) {
		this.weapon_type = wType;
	}
	
	public WeaponType getWeaponType() {
		return this.weapon_type;
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
	
	public void setModifier(int newMod) {
		this.mod = newMod;
	}
	
	public int getModifer() {
		return this.mod;
	}
	
	@Override
	public Weapon getCopy() {
		return new Weapon(this);
	}

	@Override
	public String toDB() {
		final String[] output = new String[2];
		
		output[0] = this.weapon_type.getId() + ""; // weapon type
		output[1] = this.mod + "";                 // modifier
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		// e.g. Rapier +1
		return this.getName() + "+" + this.mod;
	}
}