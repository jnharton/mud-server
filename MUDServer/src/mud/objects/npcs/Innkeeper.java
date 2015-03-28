package mud.objects.npcs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.MUDServer;
import mud.d20.Abilities;
import mud.d20.Classes;
import mud.d20.Races;
import mud.game.Ability;
import mud.interfaces.*;
import mud.misc.Coins;
import mud.misc.Currency;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.NPC;
import mud.objects.Player;
import mud.objects.items.Armor;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Innkeeper extends NPC implements Vendor {

	private MUDServer parent;
	public ArrayList<Item> stock;

	public Innkeeper(final MUDServer mudServer, final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, 
                    final String tempDesc, final String tempTitle, final String tempPStatus, final int tempLoc, final Coins tempMoney) {
		super(tempDBRef, tempName, null, tempFlags, tempDesc, tempTitle, tempPStatus, tempLoc, tempMoney);

		this.parent = mudServer;
		this.stock = new ArrayList<Item>();

		this.access = 0;

		this.stats = new LinkedHashMap<Ability, Integer>(6, 0.75f);

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
	public void interact(Player player) {
		final Client client = player.getClient();
		
		parent.send(this.getName(), client);
		parent.send("-----< Stock >--------------------", client);
		for (Item item : this.stock) {
			if (item instanceof Armor) {
				final Armor a = (Armor) item;
				//parent.send(parent.colors("+" + a.getMod() + " " + a.getName() + " " + a.getDesc() + " (" + a.getWeight() + ") Cost: " + a.getCost(), "yellow"), client);
				parent.send(parent.colors(a.toString() + " " + a.getDesc() + " (" + a.getWeight() + ") Cost: " + a.getValue(), "yellow"), client);
			}
			else {
				parent.send("?", client);
			}
		}
		parent.send("----------------------------------", client);
	}
}
