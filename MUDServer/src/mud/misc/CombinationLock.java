package mud.misc;

public class CombinationLock extends Lock {
	public static final int LEFT = -1;
	public static final int RIGHT = 1;

	// combination
	private Combination combination;

	private int minPos;
	private int maxPos;
	private int currPos;
	
	private boolean tumbler1;
	private boolean tumbler2;
	private boolean tumbler3;
	
	private boolean locked;
	
	public CombinationLock(final int c1, final int c2, final int c3, int maxPos) {
		this(new Combination(c1, c2, c3), maxPos);
		this.combination = new Combination(c1, c2, c3);
	}
	
	public CombinationLock(final Combination combo, int maxPos) {
		this.combination = combo;

		this.minPos = 0;
		this.maxPos = maxPos;
		this.currPos = 0;

		this.tumbler1 = false;
		this.tumbler2 = false;
		this.tumbler3 = false;
		
		this.locked = true;
	}

	public int rotate(int direction, int amount) {
		int newPos;
		
		switch(direction) {
		case RIGHT:
			newPos = currPos + amount;
			
			if( newPos > maxPos ) currPos = (newPos - maxPos) - 1;
			
			break;
		case LEFT:
			newPos = currPos - amount;
			
			if(newPos < 0) currPos = maxPos + newPos;
			
			break;
		default:
			newPos = currPos;
			break;
		}
		
		check();
		
		return newPos;
	}

	/* decide if a tumbler is true/false */
	private void check() {
		if( this.currPos == this.combination.getNum(1) ) {
			this.tumbler1 = true;
		}
		else if( this.currPos == this.combination.getNum(2) ) {
			this.tumbler2 = true;
		}
		else if( this.currPos == this.combination.getNum(3) ) {
			this.tumbler3 = true;
		}
		
		if( tumbler1 && tumbler2 && tumbler3 ) this.locked = false;
		else                                   this.locked = true;
	}
	
	public boolean isLocked() {
		return this.locked;
	}
}