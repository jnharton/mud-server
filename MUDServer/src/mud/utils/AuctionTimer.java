package mud.utils;

import java.util.TimerTask;

import mud.Effect;
import mud.utils.Auction;

public class AuctionTimer extends TimerTask {
	private Auction auction;
	private int remaining;
	
	public AuctionTimer(Auction auction, int duration) {
		super();
		this.auction = auction;
		this.remaining = duration;
	}
	
	public Auction getAuction() {
		return this.auction;
	}
	
	public int getTimeRemaining() {
		return this.remaining;
	}

	@Override
	public void run() {
		if( this.remaining > 0 ) {
			// do task
			// increment counter
			this.remaining--; // count down a "second"
		}
		else { cancel(); }
	}
}