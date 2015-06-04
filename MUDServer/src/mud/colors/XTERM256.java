package mud.colors;

import mud.utils.Utils;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class XTERM256 {
	public static final XTERM256 BLACK  = new XTERM256(0, Intensity.NORMAL);
	public static final XTERM256 RED    = new XTERM256(1, Intensity.NORMAL);
	public static final XTERM256 GREEN  = new XTERM256(2, Intensity.NORMAL);
	public static final XTERM256 YELLOW = new XTERM256(3, Intensity.NORMAL);
	public static final XTERM256 BLUE   = new XTERM256(4, Intensity.NORMAL);
	public static final XTERM256 PURPLE = new XTERM256(5, Intensity.NORMAL);
	public static final XTERM256 CYAN   = new XTERM256(6, Intensity.NORMAL);
	public static final XTERM256 GRAY   = new XTERM256(7, Intensity.NORMAL);
	
	public static final XTERM256 DARK_GRAY     = new XTERM256(0, Intensity.BRIGHT);
	public static final XTERM256 BRIGHT_RED    = new XTERM256(1, Intensity.BRIGHT);
	public static final XTERM256 BRIGHT_GREEN  = new XTERM256(2, Intensity.BRIGHT);
	public static final XTERM256 BRIGHT_YELLOW = new XTERM256(3, Intensity.BRIGHT);
	public static final XTERM256 BRIGHT_BLUE   = new XTERM256(4, Intensity.BRIGHT);
	public static final XTERM256 PINK          = new XTERM256(5, Intensity.BRIGHT);
	public static final XTERM256 BRIGHT_CYAN   = new XTERM256(6, Intensity.BRIGHT);
	public static final XTERM256 WHITE         = new XTERM256(7, Intensity.BRIGHT);
	
	static enum Intensity { NORMAL, BRIGHT };
	
	static int state = 3;
	private int option = 38;
	
	private Intensity intensity = null;
	private int color = -1;
	
	private final String prefix = "\033[";
	private final String suffix = "m";
	
	private int num;
	
	public XTERM256(int tColor, Intensity tIntensity) {
		this.color = tColor;
		this.intensity = tIntensity;
		this.num = this.color + ( this.intensity.ordinal() * 8 );
		
		System.out.println(color);
	}

	public XTERM256(int red, int blue, int green) {
		if( Utils.range(red, 0, 5) && Utils.range(green, 0, 5) && Utils.range(blue, 0, 5) ) {
			this.num = (red * 36) + (green * 6) + blue;
		}
		else {
			this.num = 15;
		}
	}
	
	// test (does direct RGB with a number between 0-255)
	public XTERM256(int rgb) {
		this.num = rgb;
	}
	
	public String toString() {
		return this.prefix + this.option + ";5;" + Utils.padLeft("" + this.num, '0', 3) + this.suffix;
	}
	
	public Integer getNumber() {
		return this.num;
	}
}