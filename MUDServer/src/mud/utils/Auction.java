package mud.utils;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

import java.util.LinkedList;
import java.util.List;

import mud.Coins;
import mud.objects.Item;
import mud.objects.Player;

public class Auction {
	int duration;
	int remaining = 100;
	private Player seller;
	private Item item;
	private Coins initial_price;
	private Coins buyout_price;
	
	private Bid currentBid;
	
	private LinkedList<Bid> bids;
	
	/**
	 * create an auction for the specified items, setting
	 * the initial price
	 * 
	 * @param auctionItem
	 * @param initial
	 */
	/**
	 * @param auctionItem
	 * @param initial
	 */
	public Auction(Player seller, Item auctionItem, Coins initial) {
		this.seller = seller;
		this.item = auctionItem;
		this.initial_price = initial;
		
		this.currentBid = null;
		
		bids = new LinkedList<Bid>();
	}
	
	/**
	 * create an auction for the specified items, setting
	 * the initial price and a buyout price
	 * 
	 * @param auctionItem
	 * @param initial
	 * @param buyout
	 */
	public Auction(Player seller, Item auctionItem, Coins initial, Coins buyout) {
		this(seller, auctionItem, initial);
		
		this.buyout_price = buyout;
	}
	
	/**
	 * Get the initial price of the auction as a Coins object.
	 * 
	 * @return a "copy" of the Coins object 'initial_price' ( so the actual price can't be modified )
	 */
	public Coins getInitialPrice() {
		return Coins.copper( initial_price.numOfCopper() );
	}
	
	/**
	 * Get the set buyout price (the price at which you can buy the thing outright) as a Coins object.
	 * 
	 * @return a "copy" of the Coins object 'buyout_price' ( so the actual price can't be modified )
	 */
	public Coins getBuyoutPrice() {
		return Coins.copper( buyout_price.numOfCopper() );
	}
	
	public boolean hasBids() {
		if( bids.size() > 0 ) { return true; }
		else { return false; }
	}
	
	public Player getSeller() {
		return this.seller;
	}
	
	public Item getItem() {
		return this.item;
	}
	
	public List<Bid> getBids() {
		return this.bids;		
	}
	
	public boolean placeBid(Bid newBid) {
		
		boolean success = false;

		if( getTimeLeft() > 0 ) {
			if( currentBid != null) {
				if( newBid.getAmount().numOfCopper() > currentBid.getAmount().numOfCopper() ) {
					currentBid = newBid;
					this.bids.add(newBid);
					success = true;
				}
			}
			else {
				currentBid = newBid;
				this.bids.add(newBid);
				success = true;
			}
		}

		return success;
	}
	
	public boolean retractBid(Bid oldBid) {
		if( currentBid != oldBid ) {
			return bids.remove(oldBid);
		}
		
		return false;
	}
	
	public Bid getCurrentBid() {
		return this.currentBid;
	}
	
	public int getTimeLeft() {
		return this.remaining;
	}
}