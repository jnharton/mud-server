package mud.objects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.Abilities;
import mud.Classes;
import mud.Coins;
import mud.Currency;
import mud.MUDServer;
import mud.Races;
import mud.interfaces.*;
import mud.net.Client;
import mud.objects.items.Armor;

public class Innkeeper extends NPC implements InteractiveI, Vendor {

	private MUDServer parent;
	public ArrayList<Item> stock;

	public Innkeeper(final MUDServer mudServer, final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, 
                    final String tempDesc, final String tempTitle, final String tempPStatus, final int tempLoc, final Coins tempMoney) {
		super(tempDBRef, tempName, null, tempFlags, tempDesc, tempTitle, tempPStatus, tempLoc, tempMoney);

		this.parent = mudServer;
		this.stock = new ArrayList<Item>();

		this.access = 0;

		this.stats = new LinkedHashMap<Abilities, Integer>(6, 0.75f);

		this.stats.put(Abilities.STRENGTH, 12);
		this.stats.put(Abilities.DEXTERITY, 12);
		this.stats.put(Abilities.CONSTITUTION, 12);
		this.stats.put(Abilities.INTELLIGENCE, 12);
		this.stats.put(Abilities.WISDOM, 12);
		this.stats.put(Abilities.CHARISMA, 12);

		this.race = Races.HUMAN;
		this.pclass = Classes.COMMONER;
	}

	public ArrayList<Item> list() {
		return this.stock;
	}

	public Item buy(String name) {
		Item bought = null;

		for (Item item : this.stock) {
			if (item.getName().equals(name)) {

				bought = item;

				if ( this.stock.remove(item) ) {
					return bought;
				}
			}
		}

		return bought;
	}

	public void sell(Item item) {
		// decide if we'll buy it or not
		// then?
	}

	public boolean hasItem(String name) {
		for (Item item : this.stock) {
			if (item.getName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	public Item getItem(String name) {
		for (Item item : this.stock) {
			if (item.getName().equals(name)) {
				return item;
			}
		}

		return null;
	}

	@Override
	public void interact(Client client) {
		parent.send(this.getName(), client);
		parent.send("-----< Stock >--------------------", client);
		for (Item item : this.stock) {
			if (item instanceof Armor) {
				final Armor a = (Armor) item;
				parent.send(parent.colors("+" + a.getMod() + " " + a.getName() + " " + a.getDesc() + " (" + a.armor.getWeight() + ") Cost: " + a.getCost(), "yellow"), client);
			}
			else {
				parent.send("?", client);
			}
		}
		parent.send("----------------------------------", client);
	}
}
