package mud.objects.items;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.game.Coins;
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
	
	protected int mod = 0;                    // modifier - +0, +2, +3, +4, ... and so on

	public Shield(int sMod, String sType, double sWeight)
	{
		super(-1);
		
		this.item_type = ItemTypes.SHIELD;
		
		this.equippable = true;
		
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
	public Shield(int sDBREF, String sName, EnumSet<ObjectFlag> sFlags, String sDesc, int sLoc, ItemType item, ShieldType shield, int sMod) {
		super(sDBREF, sName, sFlags, sDesc, sLoc);
		
		this.item_type = ItemTypes.SHIELD;
		
		this.equippable = true;
		
		this.mod = sMod;
		this.shield_type = shield;
		
		this.weight = 0;
	}
	
	protected Shield(Shield template) {
		super(-1);
	}

	public int getShieldBonus() {
		return this.shield_type.getShieldBonus();
	}
	
	public ShieldType getShieldType() {
		return this.shield_type;
	}
	
	public void setShieldType(final ShieldType newType) {
		this.shield_type = newType;
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
		final String[] output = new String[2];
		
		output[0] = this.shield_type.ordinal() + ""; // shield type
		output[1] = this.mod + "";                   // modifier
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}

	@Override
	public String getName() {
		if( this.shield_type != null ) {
			if( this.mod > 0 ) {
				return "+" + this.mod + " " + this.shield_type.getName() + " Wooden Shield";
			}
			else {
				return this.shield_type.getName() + " Wooden Shield";
			}
		}
		else return this.name;
	}

	@Override
	public Shield getCopy() {
		return new Shield(this);
	}
}