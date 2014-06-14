package mud.objects.items;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;

import mud.Effect;
import mud.ObjectFlag;
import mud.TypeFlag;

import mud.interfaces.Equippable;
import mud.interfaces.Usable;

import mud.magic.Spell;
import mud.net.Client;

import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;

import mud.utils.Utils;

public class Wand extends Item implements Usable<Wand>
{
	// wand should really have the itemType WAND, but it needs weapon right now, so that it can
	// fit in the slot it needs to be in

	public int charges;
	public Spell spell;
	private Handed handed = Handed.ONE;

	protected Wand() {
	}

	public Wand(int tCharges, String spell) {
		super(-1, "Wand", EnumSet.noneOf(ObjectFlag.class), "I", 8);
		this.equippable = true;
		this.item_type = ItemType.WAND;
		this.equip_type = ItemType.WEAPON;
		this.charges = tCharges;
		this.spell = parent.getSpell(spell);

		this.name = "Wand of " + this.spell.getName();
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
	 * @param spell
	 */
	public Wand(String tempName, String tempDesc, int tempLoc, int tempDBREF, ItemType itemType, int tCharges, Spell spell) {
		super(tempDBREF, tempName, EnumSet.noneOf(ObjectFlag.class), tempDesc, tempLoc);
		this.type = TypeFlag.ITEM;
		this.equippable = true;
		this.item_type = ItemType.WAND;
		this.equip_type = ItemType.WEAPON;
		this.charges = tCharges;
		this.spell = spell;
		
		this.name = "Wand of " + this.spell.getName();
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getDesc() {
		final String chargeState;
		
		if(this.charges >= 50) {
			chargeState = "fully charged";
		}
		else if(this.charges >= 12 && this.charges <= 25) {
			chargeState = "about half charged";
		}
		else if(this.charges < 12) {
			chargeState = "about a quarter charged";
		}
		else {
			chargeState = "dead, kaput, useless";
		}
		
		return this.desc + "\nThis wand is " + chargeState;
	}
	
	public ArrayList<String> look() {
		return null;
	}
	
	@Override
	public Spell getSpell() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Spell> getSpells() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Effect getEffect() {
		// TODO Auto-generated method stub
		return null;
	}

	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";          // database reference number
		output[1] = this.getName();                // name
		output[2] = this.getFlagsAsString();       // flags
		output[3] = this.getDesc();                // description
		output[4] = this.getLocation() + "";       // location
		output[5] = this.item_type.ordinal() + ""; // item type
		output[6] = this.spell.getName();          // spell name
		output[7] = this.charges + "";             // spell charges
		return Utils.join(output, "#");
	}

	public String toString() {
		return "Wand of " + this.spell.getName();
	}
}