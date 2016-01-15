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

import mud.misc.Coins;
import mud.objects.Player;

public class Bid {
	private Player bidder;
	private Coins bid;
	private Coins max_bid;
	
	public Bid(Player bidder, Coins bid) {
		this.bidder = bidder;
		this.bid = bid;
	}
	
	public Bid(Player bidder, Coins bid, Coins max_bid) {
		this(bidder, bid);
		this.max_bid = max_bid;
	}
	
	public Player getBidder() {
		return this.bidder;
	}
	
	public Coins getAmount() {
		return this.bid;
	}
	
	public Coins getMax() {
		return this.bid;
	}
}