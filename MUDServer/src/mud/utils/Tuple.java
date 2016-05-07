package mud.utils;

public class Tuple<A, B> {
	public A one;
	public B two;
	
	public Tuple(final A one, final B two) {
		this.one = one;
		this.two = two;
	}
	
	@Override
	public String toString() {
		return "(" + this.one + ", " + this.two + ")";
	}
}