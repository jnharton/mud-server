package mud.utils;

/*
 * Copyright (c) 2018 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Triple<E> {
	public final E one;
	public final E two;
	public final E three;

	public Triple(final E first, final E second, final E third) {
		this.one = first;
		this.two = second;
		this.three = third;
	}

	public String toString() {
		return String.format("(%s, %s, %s)", one, two, three);
	}
}