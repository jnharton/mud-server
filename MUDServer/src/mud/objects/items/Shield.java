package mud.objects.items;

import java.util.ArrayList;

import mud.interfaces.Equippable;
import mud.interfaces.Wieldable;

import mud.net.Client;

import mud.objects.Item;
import mud.objects.ItemType;

import mud.objects.Player;
import mud.utils.Utils;

public class Shield extends Item implements Equippable<Shield>, Wieldable<Shield>
{
	/**
	 * Flag: ISHI
	 */
	
	ShieldType shield;
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

	@Override
	public void equip() {
	}

	@Override
	public void equip(Player p) {
		// TODO Auto-generated method stub
	}

	@Override
	public Shield unequip() {
		return null;
	}

	@Override
	public void wield(String arg, Client client) {
	}
	
	public ArrayList<String> look() {
		return null;
	}

	public String toString() {
		return this.mod + " " + this.getName();
	}

	public String toDB() {
		String[] output = new String[8];
		output[0] = Utils.str(this.getDBRef());          // shield database reference number
		output[1] = this.getName();                      // shield name
		output[2] = this.getFlags();                     // shield flags
		output[3] = this.getDesc();                      // shield description
		output[4] = Utils.str(this.getLocation());       // shield location
		output[5] = Utils.str(this.item_type.ordinal()); // shield item type
		output[6] = Utils.str(this.shield.ordinal());    // shield type
		output[7] = Utils.str(this.mod);                 // modifier
		return Utils.join(output, "#");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}