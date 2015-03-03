package mud.objects.items;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mud.TypeFlag;
import mud.interfaces.Usable;
import mud.interfaces.Wearable;
import mud.magic.Spell;
import mud.misc.Effect;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;
import mud.utils.Utils;
import mud.ObjectFlag;

public class Jewelry extends Item implements Usable<Jewelry>, Wearable<Jewelry>
{

	// type - necklace, bracelet, ring, earring, tiara
	//String type = "";

	Effect effect;

	public Jewelry() {
	}

	// ex. new Jewelry(ItemType.RING, "Ring of Invisibility", "A medium-sized gold ring with a smooth, unmarked surface.", new Effect("invisibility"))
	public Jewelry(final ItemType jType, final String jName, final String jDesc, final Effect jEffect) {
		super(-1);
		this.type = TypeFlag.ITEM;
		this.name = jName;
		this.desc = jDesc;
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		this.location = 8;

		this.equippable = true;

		this.effect = jEffect;
		this.effect.setPermanent(true);
	}

	public Jewelry(final ItemType jType, final String jName, final String jDesc, final String jEffectString) {
		this(jType, jName, jDesc, new Effect(jEffectString));
	}

	public Jewelry(String jType, double jWeight)
	{
		super(-1);
		this.type = TypeFlag.ITEM;
		this.name = "Ring of Invisibility";
		this.desc = "A medium-sized gold ring with a smooth, unmarked surface.";
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		this.location = 8;

		this.equippable = true;
		this.equip_type = ItemType.RING; // the type of equipment it is
		//this.type = jType;               // the actual type of jewelry
		this.weight = jWeight;           // the weight of the jewelry
	}

	public void use(String arg, Client client) {
		System.out.println("[Jewelry->Use]");
		if (this.equip_type == ItemType.RING) {
			Player player = parent.getPlayer(client);
			parent.debug(player);
			System.out.println(parent.applyEffect(player, effect));
		}
	}

	@Override
	public String getName() {
		return this.name;
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
		output[0] = this.getDBRef() + "";             // jewelry database reference number
		output[1] = this.getName();                   // jewelry name
		output[2] = this.getFlagsAsString();          // jewelry flags
		output[3] = this.getDesc();                   // jewelry description
		output[4] = this.getLocation() + "";          // jewelry location
		output[5] = this.item_type.ordinal() + "";    // item type
		output[6] = "*";                              // nothing (placeholder)
		output[7] = "*";                              // nothing (placeholder)
		return Utils.join(output, "#");
	}

	public String toString() {
		return this.getName();
	}
}