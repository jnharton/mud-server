package mud.objects.items;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.misc.Coins;
import mud.objects.Item;
import mud.objects.ItemType;
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
	private ArmorType armor_type;
	
	protected int mod = 0;                    // modifier - +0, +2, +3, +4, ... and so on
	
	// TODO what kind of armor is it? armortype being null is a problem
	public Armor(final String name, final String desc) {
		super(-1, name, desc);
		
		this.item_type = ItemTypes.ARMOR;
		
		this.equippable = true;
		
		this.mod = 0;
		
		this.armor_type = ArmorType.NONE;
		this.weight = 5;
	}
	
	public Armor(int aMod, ArmorType armor) {
		super(-1, armor.getName().toLowerCase(), "armor");
		
		this.item_type = ItemTypes.ARMOR;
		
		this.equippable = true;
		
		this.mod = aMod;
		
		this.armor_type = armor;
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
		
		this.armor_type = template.armor_type;
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
	public Armor(int wDBREF, String wName, EnumSet<ObjectFlag> wFlags, String wDesc, int wLoc, ArmorType armor)
	{
		super(wDBREF, wName, wFlags, wDesc, wLoc);
		
		this.item_type = ItemTypes.ARMOR;
		
		this.equippable = true;
		
		//this.mod = aMod;
		this.mod = 0;
		
		this.armor_type = armor;
		
		this.weight = armor.getWeight();
	}
	
	@Override
	public String getName() {
		if( this.armor_type != null ) return this.armor_type.getName() + " Armor";
		else                          return this.name;
	}
	
	public int getArmorBonus() {
		return this.armor_type.getArmorBonus();
	}
	
	public int getDexBonus() {
		return this.armor_type.getDexBonus();
	}
	
	public double getSpellFailure() {
		return this.armor_type.getSpellFailure();
	}
	
	@Override
	public Coins getValue() {
		return Coins.fromArray(armor_type.getCost());
		//return new Coins(armor_type.getCost());
	}
	
	public int getMod() {
		return this.mod;
	}
	
	public void setMod(int newMod) {
		this.mod = newMod;
	}
	
	@Override
	public String toDB() {
		final String[] output = new String[2];
		
		output[0] = this.armor_type.ordinal() + ""; // armor type
		output[1] = this.mod + "";                  // modifier
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		int modifier = 0;
		
		if (this.mod > 0)      return "+" + this.mod + " " + this.getName();
		else if (this.mod < 0) return this.mod + " " + this.getName();
		else                   return this.getName();
	}
	
	@Override
	public Armor getCopy() {
		return new Armor(this);
	}
}