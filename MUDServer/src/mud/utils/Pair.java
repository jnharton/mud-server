package mud.utils;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Pair<E> {
	public final E one;
	public final E two;
	
	public Pair(final E first, final E second) {
		this.one = first;
		this.two = second;
	}
	
	public String toString() {
		return "(" + one.toString() + ", " + two.toString() + ")";
	}
}