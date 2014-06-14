package mud.auction;

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
	private Player seller;       // Player selling the item
	private Item item;           // Item for auction
	private Coins initial_price; // starting price
	private Coins buyout_price;  // price at which you can buy the item outright (if one is set)
	
	private int duration;        // length of the auction ( in seconds )
	protected int remaining;     // time until the auction ends ( in seconds )
	
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
	public Auction(Player seller, Item auctionItem, Coins initial, int duration) {
		this.seller = seller;
		this.item = auctionItem;
		this.initial_price = initial;
		
		if( duration > 0 ) {
			this.duration = duration;
			this.remaining = duration;
		}
		else {
			this.duration = 3600;  // default is 1 hour ( 60s/min x 60min/hr = 3600s)
			this.remaining = 3600; // starts same as duration
		}

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
	public Auction(Player seller, Item auctionItem, Coins initial, Coins buyout, int duration) {
		this(seller, auctionItem, initial, duration);
		
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