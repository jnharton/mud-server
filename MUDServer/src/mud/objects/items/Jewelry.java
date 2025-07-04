package mud.objects.items;

import java.util.EnumSet;
import java.util.List;

import mud.ObjectFlag;

import mud.interfaces.MagicItem;

import mud.magic.Spell;

import mud.misc.Effect;
import mud.misc.SlotTypes;

import mud.net.Client;

import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;

import mud.utils.Utils;

public class Jewelry extends Item implements MagicItem {
	// types - necklace, bracelet, ring, earring, tiara
	
	// ex. new Jewelry(ItemType.RING, "Ring of Invisibility", "A medium-sized gold ring with a smooth, unmarked surface.", new Effect("invisibility"))
	// ex. new Jewelry("Ring of Invisibility", "A medium-sized gold ring with a smooth, unmarked surface.", ItemType.RING, new Effect("invisibility"))

	public Effect effect;
	
	public Jewelry(final ItemType jType, final String jName, final String jDesc, final Effect jEffect) {
		//super(-1);
		super(-1, jName, jDesc);
		
		this.item_type = jType;

		this.equippable = true;

		this.effect = jEffect;
		this.effect.setPermanent(true);
		
		this.weight = 1.0;
	}

	public Jewelry(final ItemType jType, final String jName, final String jDesc, final String jEffectString) {
		this(jType, jName, jDesc, new Effect(jEffectString));
	}
	
	/**
	 * <b>Object Loading Constructor</b><br/><br/>
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other two constructors
	 * that have parameters.
	 * 
	 * @param dbref
	 * @param name
	 * @param flags
	 * @param description
	 * @param location
	 */
	public Jewelry(final int dbref, final String name, final EnumSet<ObjectFlag> flags, final String description, final int location)
	{
		super(dbref, name, flags, description, location);
		
		// TODO we'll just make ring the only kind of jewelry for now
		this.item_type = ItemTypes.RING;
		this.slot_type = SlotTypes.FINGER;
		
		this.equippable = true;
	}
	
	public void use(String arg, Client client) {
		/*System.out.println("[Jewelry->Use]");
		if (this.equip_type == ItemTypes.RING) {
			Player player = parent.getPlayer(client);
			parent.debug(player);
			System.out.println(parent.applyEffect(player, effect));
		}*/
	}
	
	public Spell getSpell() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<Spell> getSpells() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Effect getEffect() {
		return this.effect;
	}
	
	@Override
	public List<Effect> getEffects() {
		return Utils.mkList( this.effect );
	}
	
	public void setEffect(final Effect newEffect) {
		this.effect = newEffect;
	}
	
	@Override
	public String toDB() {
		//final String[] output = new String[10];
		//return Utils.join(output, "#");
		
		return super.toDB();
	}

	public String toString() {
		return this.getName();
	}
}