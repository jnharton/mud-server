package mud.objects.items;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.misc.Coins;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;
import mud.utils.Utils;

public class Shield extends Item
{	
	private ShieldType shield_type;
	// weight - light, medium, heavy
	String size = "";
	//type - buckler, small shield, medium shield, large shield, tower shield
	//String type = "";
	Handed handed = Handed.ONE;
	
	protected int mod = 0;                    // modifier - +0, +2, +3, +4, ... and so on

	public Shield(int sMod, String sType, double sWeight)
	{
		super(-1);
		
		this.equippable = true;
		this.equip_type = ItemTypes.SHIELD; // the type of equipment it is
		this.mod = sMod;
		//this.type = sType;                 // the actual type of shield
		this.weight = sWeight;             // the weight of the shield
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
	public Shield(String sName, String sDesc, int sLoc, int sDBREF, int sMod, ItemType item, ShieldType shield) {
		super(sDBREF, sName, EnumSet.noneOf(ObjectFlag.class), sDesc, sLoc);
		this.type = TypeFlag.ITEM;
		this.equippable = true;
		this.equip_type = ItemTypes.SHIELD;
		
		this.mod = sMod;
		this.item_type = item;
		this.shield_type = shield;
		
		this.weight = 0;
	}
	
	protected Shield(Shield template) {
		super(-1);
	}

	public int getShieldBonus() {
		return this.shield_type.getShieldBonus();
	}
	
	@Override
	public Coins getValue() {
		return new Coins(shield_type.getCost());
	}
	
	public int getMod() {
		return this.mod;
	}
	
	public void setMod(int newMod) {
		this.mod = newMod;
	}
	
	@Override
	public String toString() {
		return this.mod + " " + this.getName();
	}
	
	@Override
	public String toDB() {
		String[] output = new String[10];
		
		output[0] = this.getDBRef() + "";            // database reference number
		output[1] = this.getName();                  // name
		output[2] = this.getFlagsAsString();         // flags
		output[3] = this.getDesc();                  // description
		output[4] = this.getLocation() + "";         // location
		output[5] = this.item_type.getId() + "";     // item type
		output[6] = this.equip_type.getId() + "";    // equip type
		output[7] = this.slot_type.getId() + "";     // slot type
		
		output[8] = this.shield_type.ordinal() + ""; // shield type
		output[9] = this.mod + "";                   // modifier
		
		return Utils.join(output, "#");
	}

	@Override
	public String getName() {
		if( this.mod > 0 ) { return "+" + this.mod + " " + this.shield_type.getName() + " Wooden Shield"; }
		else { return this.shield_type.getName() + " Wooden Shield"; }
	}
	
	@Override
	public Shield getCopy() {
		return new Shield(this);
	}
}