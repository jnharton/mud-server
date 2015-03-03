package mud.misc;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * An enumeration of ANSI colors
 * 
 * NOTE: not used, I have a hashmap instead
 * 
 * @author Jeremy
 *
 */
public enum Colors {
	
	BLACK(30),
	RED(31),
	GREEN(32),
	YELLOW(33),
	BLUE(34),
	MAGENTA(35),
	CYAN(36),
	WHITE(37);
	
	private static boolean bright = true;
	
	private final String prefix = "\33[";
	private final String suffix = "m";
	
	private int num;
	
	private Colors(int num) {
		this.num = num;
	}
	
	public static void setBright(boolean bright) {
		Colors.bright = bright;
	}
	
	public String toString() {
		if( Colors.bright ) return prefix + this.num + ";1" + suffix;
		else return prefix + this.num + ";0" + suffix;
	}
}