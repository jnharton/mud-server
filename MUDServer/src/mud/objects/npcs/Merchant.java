package mud.objects.npcs;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.game.Ability;
import mud.interfaces.Vendor;
import mud.misc.Coins;
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

public class Merchant extends NPC implements Vendor {
	/**
	 * 
	 */
	private ArrayList<Item> stock = new ArrayList<Item>();
	private Hashtable<String, Integer> stockTable = new Hashtable<String, Integer>();
	// sword, 15
	
	private String merchantType = "";

	public Merchant(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc,
			final String tempTitle, final String tempPStatus, final int tempLoc, final Coins tempMoney) {
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc, tempMoney);

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
		
		this.title = tempTitle;
		this.status = tempPStatus;
	}
	
	@Override
	public List<Item> getStock() {
		return Collections.unmodifiableList(this.stock);
	}
	
	public void setStock(final List<Item> stock) {
		this.stock = new ArrayList<Item>(stock);
	}
	
	public void addToStock(final Item item) {
		this.stock.add(item);
	}

	public Item buy(final String name, final Coins payment) {
		Item bought = null;
		
		for (final Item item : this.stock) {
			if( item.getName().equals(name) ) {
				bought = item;
				break;
			}
		}
		
		if( bought != null ) {
			this.stock.remove(bought);
			this.setMoney(payment);
		}
		
		return bought;
	}

	public Coins sell(final Item item) {
		this.stock.add(item);
		
		return item.getValue();
	}

	public boolean hasItem(final String name) {
		boolean exactMatch = false;
		boolean partialMatch = false;
		
		for (final Item item : this.stock) {
			final String itemName = item.getName();
			
			if ( itemName.equals(name) )          exactMatch = true;
			else if ( itemName.startsWith(name) ) partialMatch = true;
			
			if ( exactMatch ) {
				
			}
		}
		
		return exactMatch || partialMatch;
	}

	public Item getItem(final String name) {
		for (final Item item : this.stock) {
			if (item.getName().equals(name)) {
				return item;
			}
		}
		
		return null;
	}
	
	// hacked in functionality for setting what sort of merchant this is
	public void setMerchantType(final String type) {
		this.merchantType = type;
	}
	
	public String getMerchantType() {
		return this.merchantType;
	}
}