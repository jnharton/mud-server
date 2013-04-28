package mud.colors;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public enum ANSI {
	BLACK(30),
	RED(31),
	GREEN(32),
	YELLOW(33),
	BLUE(34),
	MAGENTA(35),
	CYAN(36),
	WHITE(37);
	
	private final String prefix = "\33[";
	private final String suffix = "m";
	
	private int num;
	
	private ANSI(int num) {
		this.num = num;
	}
	
	public String toString() {
		return prefix + this.num + suffix;
	}
}