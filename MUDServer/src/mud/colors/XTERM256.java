package mud.colors;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

public enum XTERM256 {
	BLACK(0, Intensity.NORMAL),
	RED(1, Intensity.NORMAL),
	GREEN(2, Intensity.NORMAL),
	YELLOW(3, Intensity.NORMAL),
	BLUE(4, Intensity.NORMAL),
	PURPLE(5, Intensity.NORMAL),
	CYAN(6, Intensity.NORMAL),
	GRAY(7, Intensity.NORMAL),
	
	DARK_GRAY(0, Intensity.BRIGHT),
	BRIGHT_RED(1, Intensity.BRIGHT),
	BRIGHT_GREEN(2, Intensity.BRIGHT),
	BRIGHT_YELLOW(3, Intensity.BRIGHT),
	BRIGHT_BLUE(4, Intensity.BRIGHT),
	PINK(5, Intensity.BRIGHT),
	BRIGHT_CYAN(6, Intensity.BRIGHT),
	WHITE(7, Intensity.BRIGHT),
	
	TEST(201);
	
	static enum Intensity { NORMAL, BRIGHT };
	static int state = 3;
	private int option = 38;
	
	private Intensity intensity = null;
	private int color = -1;
	
	private final String prefix = "\\e[";
	private final String suffix = "m";
	
	private int num;
	
	XTERM256(int tColor, Intensity tIntensity) {
		this.color = tColor;
		this.intensity = tIntensity;
		this.num = this.color + ( this.intensity.ordinal() * 8 );
		
		System.out.println(color);
	}

	XTERM256(int red, int blue, int green) {
		if ( ( red >= 0 && red <= 5 ) && ( blue >= 0 && blue <= 5 ) && ( green >= 0 && green <= 5) ) {
			this.num = (red * 36) + (green * 6) + blue;
		}
		else {
			this.num = 15;
		}
	}
	
	// test (does direct RGB with a number between 0-255)
	XTERM256(int rgb) {
		this.num = rgb;
	}
	
	public String toString() {
		return this.prefix + this.option + ";5;" + this.num + suffix;
	}
}