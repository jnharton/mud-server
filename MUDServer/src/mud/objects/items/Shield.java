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
import mud.utils.Utils;

public class Shield extends Item implements Equippable<Shield>, Wieldable<Shield>
{	
	private ShieldType shield_type;
	// weight - light, medium, heavy
	String size = "";
	//type - buckler, small shield, medium shield, large shield, tower shield
	String type = "";
	Handed handed = Handed.ONE;

	public Shield() {
	}

	public Shield(int sMod, String sType, double sWeight)
	{
		this.equippable = true;
		this.equip_type = ItemType.SHIELD; // the type of equipment it is
		this.mod = sMod;
		this.type = sType;                 // the actual type of shield
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
		super(sDBREF, sName, EnumSet.of(ObjectFlag.ITEM), sDesc, sLoc);
		this.equippable = true;
		this.equip_type = ItemType.SHIELD;
		
		this.mod = sMod;
		this.item_type = item;
		this.shield_type = shield;
		
		this.weight = 0;
	}
	
	public int getShieldBonus() {
		return this.shield_type.getShieldBonus();
	}
	
	@Override
	public Coins getCost() {
		return new Coins(shield_type.getCost());
	}
	
	public void equip() {
	}

	public void equip(Player p) {
		// TODO Auto-generated method stub
	}

	public Shield unequip() {
		return null;
	}

	public void wield(String arg, Client client) {
	}
	
	public ArrayList<String> look() {
		return null;
	}
	
	@Override
	public String toString() {
		return this.mod + " " + this.getName();
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
		output[6] = this.shield_type.ordinal() + ""; // shield type
		output[7] = this.mod + "";                   // modifier
		return Utils.join(output, "#");
	}

	@Override
	public String getName() {
		if( this.mod > 0 ) { return "+" + this.mod + " " + this.shield_type.getName() + " Wooden Shield"; }
		else { return this.shield_type.getName() + " Wooden Shield"; }
	}
}