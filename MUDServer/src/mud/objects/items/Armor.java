package mud.objects.items;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.combat.ArmorType;
import mud.combat.ArmorTypes;
import mud.game.Coins;
import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.utils.Utils;

/**
 * 
 * @author Jeremy
 *
 * @param aMod Armor bonus modifier (integer)
 * @param aGroup err, no idea.
 */
public class Armor extends Item {
	/**
	 * Flag: I
	 * ItemType: Armor
	 */

	// weight - light, medium, heavy
	//private String group = "";
	private ArmorType armorType;
	
	protected int modifier = 0;                    // modifier - +0, +2, +3, +4, ... and so on
	
	// TODO what kind of armor is it? armortype being null is a problem
	public Armor(final String name, final String desc) {
		super(-1, name, desc);
		
		this.item_type = ItemTypes.ARMOR;
		
		this.equippable = true;
		
		this.modifier = 0;
		
		this.armorType = ArmorTypes.NONE;
		this.weight = 5;
	}
	
	public Armor(int aMod, ArmorType armor) {
		super(-1, armor.getName().toLowerCase(), "armor");
		
		this.item_type = ItemTypes.ARMOR;
		
		this.equippable = true;
		
		this.modifier = aMod;
		
		this.armorType = armor;
		this.weight = armor.getWeight();
	}
	
	/**
	 * Armor - Copy Constructor
	 * 
	 * @see "Use this to make an identical copy of another armor. Just
	 * remember to change the database reference, etc accordingly"
	 * 
	 * @param template the armor to copy
	 */
	protected Armor( Armor template ) {
		super( template );
		
		this.armorType = template.armorType;
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other two constructors
	 * that have parameters.
	 * @param tempName
	 * @param tempDesc
	 * @param tempLoc
	 * @param tempDBREF
	 */
	public Armor(final int aDBREF, final String aName, final EnumSet<ObjectFlag> aFlags, final String aDesc, final int aLoc, final ArmorType armor)
	{
		super(aDBREF, aName, aFlags, aDesc, aLoc);
		
		this.item_type = ItemTypes.ARMOR;
		
		this.equippable = true;
		
		//this.mod = aMod;
		this.modifier = 0;
		
		this.armorType = armor;
		
		this.weight = armor.getWeight();
	}
	
	// TODO decide if this is really needed... especially since names are important
	/*
	@Override
	public String getName() {
		if( this.armorType != null ) return this.armorType.getName() + " Armor";
		else                         return this.name;
	}*/
	
	public int getArmorBonus() {
		return this.armorType.getArmorBonus();
	}
	
	public int getDexBonus() {
		return this.armorType.getDexBonus();
	}
	
	public double getSpellFailure() {
		return this.armorType.getSpellFailure();
	}
	
	@Override
	public Coins getValue() {
		return Coins.fromArray(armorType.getCost());
		//return new Coins(armor_type.getCost());
	}
	
	public int getModifier() {
		return this.modifier;
	}
	
	public void setModifier(int newMod) {
		this.modifier = newMod;
	}
	
	@Override
	public Armor getCopy() {
		return new Armor(this);
	}
	
	@Override
	public String toDB() {
		final String[] output = new String[2];
		
		output[0] = ArmorTypes.getId(this.armorType.getName()) + ""; // armor type
		output[1] = this.modifier + "";                              // modifier
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		if (this.modifier > 0)      return "+" + this.modifier + " " + this.getName();
		else if (this.modifier < 0) return this.modifier + " " + this.getName();
		else                   return this.getName();
	}
}