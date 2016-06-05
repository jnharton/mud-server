package mud.colors;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import mud.utils.Utils;

public class Palette {
	// Color Options
	public static final int DISABLED = 0;
	public static final int ANSI = 1;
	public static final int XTERM = 2;

	private String name;
	private Map<String, String> colors;

	private final int type;

	public Palette(final String name) {
		this(name, DISABLED);
	}

	public Palette(final String name, final int colorType) {
		this.name = name;
		this.colors = new Hashtable<>();
		this.type = colorType;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getType() {
		return this.type;
	}
	
	public Map<String, String> getColors() {
		return Collections.unmodifiableMap(this.colors);
	}

	public void addColor(final String name, final Integer number) {
		if( this.type == ANSI ) {
			this.colors.put(name, "\033[" + number + ";1m");
		}
		else if( this.type == XTERM ) {
			this.colors.put(name, "\033[38;5;" + Utils.padLeft("" + number, '0', 3) + "m");
		}
		else {
			this.colors.put(name, "" + number);
		}
	}
	
	public String getColor(final String name) {
		return this.colors.get(name);
	}
	
	public void removeColor(final String name) {
		this.colors.remove(name);
	}
}