package mud.objects.npcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.game.Ability;
import mud.game.Coins;
import mud.interfaces.*;
import mud.objects.Item;
import mud.objects.NPC;
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

public class Innkeeper extends NPC implements Vendor<Item> {
	private ArrayList<Item> stock;

	public Innkeeper(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc,
			final String tempTitle, final String tempPStatus, final int tempLoc, final Coins tempMoney) {
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc, tempMoney);
		
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

	public List<Item> getStock() {
		return Collections.unmodifiableList(this.stock);
	}
	
	public void setStock(List<Item> stock) {
		this.stock = new ArrayList<Item>(stock);
	}
	
	public Item buy(final String name, final Coins payment) {
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

	public Coins sell(Item item) {
		return Coins.copper(0);
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

	
}