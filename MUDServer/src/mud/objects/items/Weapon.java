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
	public enum DamageType { ENERGY, PIERCING, SLASHING, MAGICAL };
	
	public static final int MELEE = 0;
	public static final int RANGED = 1;
	
	private Skill required_skill;
	private int req_skill_value;
	
	private DamageType dType;      // damage type
	
	private int damage = 1;
	private int mod = 0;           // modifier - +0, +2, +3, +4, ... and so on
	
	private int critMin;           // minimum roll for a critical hit
	private int critMax;           // maximum roll for a critical hit
	private int critical;          // damage multiplier for a critical hit
	
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
		
		this.weight = wType.getWeight(); // the weight of the weapon
		
		this.dType = wType.getDamageType();
		
		this.damage = 1;
		//this.damage = wType.getDamage();
		
		//this.mod = wMod;
		
		this.critMin = wType.getCritMin();
		this.critMax = wType.getCritMax();
		this.critical = wType.getCritical();
	}
	
	/**
	 * Copy Constructor
	 * 
	 * @param template
	 */
	protected Weapon(final Weapon template) {
		super( template );
		
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
	public Weapon(final int wDBREF, final String wName, EnumSet<ObjectFlag> wFlags, final String wDesc, final int wLoc)
	{
		super(wDBREF, wName, wFlags, wDesc, wLoc);
		
		this.item_type = ItemTypes.WEAPON;
		this.slot_type = SlotTypes.NONE;
		
		this.equippable = true;
	}
	
	@Override
	public Coins getValue() {
		return Coins.gold(5);
	}
	
	@Override
	public Double getWeight() {
		return this.weight;
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
	
	public DamageType getDamageType() {
		return this.dType;
	}
	public void setDamageType(final DamageType newType) {
		this.dType = newType;
	}
	
	public void setDamage(int newDamage) {
		this.damage = newDamage;
	}
	
	public int getDamage() {
		return this.damage;
	}
	
	public void setModifier(int newMod) {
		this.mod = newMod;
	}
	
	public int getModifer() {
		return this.mod;
	}
	
	public int getCritMin() {
		return this.critMin;
	}
	
	public int getCritMax() {
		return this.critMax;
	}
	
	public int getCritical() {
		return this.critical;
	}
	
	public void setCritical(boolean crits, int critMin, int critMax) {
		if( crits ) {
		}
	}
	
	@Override
	public Weapon getCopy() {
		return new Weapon(this);
	}

	@Override
	public String toDB() {
		final String[] output = new String[2];
		
		output[0] = -1 + "";       // weapon type ?
		output[1] = this.mod + ""; // modifier
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		// e.g. Rapier +1
		if( this.mod > 0 ) return this.getName() + "+" + this.mod;
		else               return this.getName();
	}
}