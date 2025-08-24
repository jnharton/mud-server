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
	private static final String prefix = "\033[";
	private static final String suffix = "m";
	
	public static enum Intensity { NORMAL, BRIGHT };
	
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
	
	static int state = 3;
	private int option = 38; // this numbers matter, but what does it mean?
	
	private int color = -1;
	private Intensity intensity = null;
	
	private int num;
	
	public XTERM256(final Integer tColor, final Intensity tIntensity) {
		this.color = tColor;
		this.intensity = tIntensity;
		
		this.num = this.color + ( this.intensity.ordinal() * 8 );
		
		System.out.println(color);
	}
	
	public XTERM256 parseRGB(int red, int blue, int green) {
		int number = 0;
		
		if( Utils.range(red, 0, 5) && Utils.range(green, 0, 5) && Utils.range(blue, 0, 5) ) {
			number = (red * 36) + (green * 6) + blue;
		}
		else {
			number = 15;
		}
		
		return new XTERM256(number, Intensity.BRIGHT);
	}
	
	public String toString() {
		//return XTERM256.prefix + this.option + ";5;" + Utils.padLeft("" + this.num, '0', 3) + XTERM256.suffix;
		return XTERM256.prefix + this.option + ";5;" + this.num + XTERM256.suffix;
	}
	
	public Integer getNumber() {
		return this.num;
	}
	
	public Intensity getIntensity() {
		return this.intensity;
	}
}