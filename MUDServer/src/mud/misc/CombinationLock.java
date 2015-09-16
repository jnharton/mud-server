package mud.misc;

import java.util.Hashtable;
import java.util.Map;

public class CombinationLock extends Lock {
	public static final int LEFT = -1;
	public static final int RIGHT = 1;
	
	// combination
	Combination combo;
	
	Map<Integer, Boolean> tumblers;
	
	int startPos;
	int maxPos;
	int currentPos;
	
	boolean locked;
	
	public CombinationLock(int c1, int c2, int c3, boolean locked) {
	}
	
	public CombinationLock(int c1, int c2, int c3, int currentPos, boolean locked) {
		this.combo = new Combination(c1, c2, c3);
		
		this.tumblers = new Hashtable<Integer, Boolean>() {
			{ put(1, false); put(2, false); put(3, false); }
		};
		
		this.currentPos = currentPos;
		
		this.locked = locked;
	}
	
	public void rotate(int direction, int amount) {
		int newPos;
		switch(direction) {
		case RIGHT:
			newPos = currentPos + amount;
			while(currentPos < newPos) {
				currentPos++;
				check();
			}
			break;
		case LEFT:
			newPos = currentPos - amount;
			while(currentPos > newPos) {
				currentPos--;
				check();
			}
			break;
		default:    break;
		}
	}
	
	public void check() {
		
	}
	
	private class Combination {
		int num1, num2, num3;
		
		public Combination(int c1, int c2, int c3) {
			this.num1 = c1;
			this.num2 = c2;
			this.num3 = c3;
		}
		
		public int getNum(int num) {
			switch(num) {
			case 1:  return this.num1;
			case 2:  return this.num2;
			case 3:  return this.num3;
			default: return -1;
			}
		}
	}
}