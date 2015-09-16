package mud.objects.npcs;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.MUDServer;
import mud.game.Ability;
import mud.interfaces.Vendor;
import mud.misc.Coins;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.NPC;
import mud.objects.Player;
import mud.objects.items.Armor;
import mud.objects.items.Weapon;
import mud.rulesets.d20.Abilities;
import mud.rulesets.d20.Classes;
import mud.rulesets.d20.Races;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Merchant extends NPC implements Vendor {

	/**
	 * 
	 */
	final private MUDServer parent;
	public ArrayList<Item> stock = new ArrayList<Item>();
	
	public Hashtable stockTable = new Hashtable<String, Integer>();
	// sword, 15
	
	public String type = "";

	public Merchant(final MUDServer mudServer, final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, 
            final String tempDesc, final String tempTitle, final String tempPStatus, final int tempLoc, final Coins tempMoney) {
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc, tempMoney);

		this.parent = mudServer;
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
	
	@Override
	public void interact(Player player) {
		final Client client = player.getClient();
		
		parent.send(this.getName(), client);
		parent.send("-----< Stock >--------------------", client);
		for (Item item : this.stock) {
			if (item instanceof Armor) {
				Armor a = (Armor) item;
				//parent.send(parent.colors("+" + a.getMod() + " " + a.getName() + " (" + a.getWeight() + ") Cost: " + a.getCost(), "yellow"), client);
				parent.send(parent.colors(a.toString() + " (" + a.getWeight() + ") Cost: " + a.getValue(), "yellow"), client);
			}
			else if (item instanceof Weapon) {
				final Weapon w = (Weapon) item;
				//parent.send(parent.colors("+" + w.getMod() + " " + w.getName() + " (" + w.getWeight() + ") Cost: " + w.getCost(), "yellow"), client);
				parent.send(parent.colors(w.toString()  + " (" + w.getWeight() + ") Cost: " + w.getValue(), "yellow"), client);
			}
			else {
				parent.send("?", client);
			}
		}
		parent.send("----------------------------------", client);
	}

	public ArrayList<Item> list() {
		return this.stock;
	}

	public Item buy(String name) {
		Item bought = null;
		
		for (Item item : this.stock) {
			if (item.getName().equals(name)) {
				
				bought = item;
				
				if (this.stock.remove(item)) {
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
	
	// hacked in functionality for setting what sort of merchant this is
	public void setType(String type) {
		this.type = type;
	}
	
	public String getMerchantType() {
		return this.type;
	}
}