package mud.objects.items;

import java.util.ArrayList;
import java.util.List;


import mud.Effect;
import mud.interfaces.Equippable;
import mud.interfaces.Usable;
import mud.interfaces.Wieldable;

import mud.magic.Spell;
import mud.net.Client;

import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;

import mud.utils.Utils;

public class Wand extends Item implements Equippable<Wand>, Usable<Wand>, Wieldable<Wand>
{
	// wand should really have the itemType WAND, but it needs weapon right now, so that it can
	// fit in the slot it needs to be in

	public int charges;
	public Spell spell;
	private Handed handed = Handed.ONE;

	protected Wand() {
	}

	public Wand(int tCharges, String spellName) {
		super(parent.nextDB("use"), "Wand", "", "I", 8);
		this.equippable = true;
		this.item_type = ItemType.WAND;
		this.equip_type = ItemType.WEAPON;
		this.charges = tCharges;
		this.spell = parent.getSpell(spellName);

		this.name = "Wand of " + this.spell.name;
	}

	public Wand(int tCharges, Spell tSpell) {
		super(parent.nextDB("use"), "Wand", "", "I", 8);
		this.equippable = true;
		this.item_type = ItemType.WAND;
		this.equip_type = ItemType.WEAPON;
		this.charges = tCharges;
		this.spell = tSpell;

		this.name = "Wand of " + this.spell.name;
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
	 * @param spellName
	 */
	public Wand(String tempName, String tempDesc, int tempLoc, int tempDBREF, ItemType itemType, int tCharges, String spellName) {
		super(tempDBREF, tempName, "I", tempDesc, tempLoc);
		this.equippable = true;
		this.item_type = ItemType.WAND;
		this.equip_type = ItemType.WEAPON;
		this.charges = tCharges;
		this.spell = parent.getSpell(spellName);

		this.name = "Wand of " + this.spell.name;
	}

	/*public Wand(String tempName, String tempDesc, int tempLoc, int tempDBREF, int tCharges, Spell tSpell) {
		super(tempDBREF, tempName, tempDesc, "I", tempLoc);
		this.equippable = true;
		this.equip_type = ItemType.WEAPON;
		this.charges = tCharges;
		this.spell = tSpell;

		this.name = "Wand of " + this.spell.name;
	}*/

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void equip() {
		// TODO Auto-generated method stub

	}

	@Override
	public void equip(Player p) {
		// TODO Auto-generated method stub
	}

	@Override
	public Wand unequip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void wield(String arg, Client client) {
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
		output[0] = this.getDBRef() + "";          // wand database reference number
		output[1] = this.getName();                      // wand name
		output[2] = this.getFlags();                     // wand flags
		output[3] = this.getDesc();                      // wand description
		output[4] = this.getLocation() + "";       // wand location
		output[5] = this.item_type.ordinal() + ""; // item type
		output[6] = this.spell.name;                     // wand spell name
		output[7] = this.charges + "";             // wand spell charges
		return Utils.join(output, "#");
	}

	public String toString() {
		return "Wand of " + this.spell.name;
	}
}