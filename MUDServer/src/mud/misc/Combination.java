package mud.misc;

public class Combination {
	private int num1;
	private int num2;
	private int num3;

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