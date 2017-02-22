package mud.auction;

import java.util.TimerTask;

import mud.auction.Auction;
import mud.misc.Counter;

public class AuctionTimer extends TimerTask {
	private Auction auction;
	private Counter time;
	
	public AuctionTimer(final Auction auction) {
		super();
		this.auction = auction;
		this.time = new Counter( auction.duration );
	}
	
	public Auction getAuction() {
		return this.auction;
	}
	
	public int getTimeRemaining() {
		return this.time.getValue();
	}

	@Override
	public void run() {
		if( this.time.getValue() > 0 ) {
			// decrement time
			this.time.decrement(); // count down a "second"
			this.auction.remaining--;
		}
		else { cancel(); }
	}
}