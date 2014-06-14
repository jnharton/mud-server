package mud.auction;

import java.util.TimerTask;

import mud.auction.Auction;

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
			// decrement time
			this.auction.remaining--; //
			this.remaining--;         // count down a "second"
		}
		else { cancel(); }
	}
}