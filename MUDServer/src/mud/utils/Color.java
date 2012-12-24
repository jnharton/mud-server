package mud.utils;

/**
 * Color
 * 
 * a class to represent an rgb color and hold onto
 * the separate red, green, and blue values
 * 
 * @author Jeremy
 *
 */
public class Color {
	private String name;
	private int red;
	private int blue;
	private int green;
	
	public Color(String cName, int cRed, int cBlue, int cGreen) {
		this.name = cName;
		this.red = cRed;
		this.blue = cBlue;
		this.green = cGreen;
	}
	
	public String getName() { return this.name; }
	
	public int red() { return this.red; }
	
	public int blue() { return this.blue; }
	
	public int green() { return this.green; }
}