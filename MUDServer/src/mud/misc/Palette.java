package mud.misc;

import java.util.LinkedList;

import mud.colors.XTERM256;

public class Palette {
	private LinkedList<XTERM256> colors;
	
	public Palette() {
		this.colors = new LinkedList<XTERM256>();
	}
	
	public Palette(final String name, final XTERM256...colors) {
		this.colors = new LinkedList<XTERM256>();
		
		for(final XTERM256 color : colors) {
			this.colors.add( color );
		}
	}
	
	public XTERM256 getColor(final Integer colorIndex) {
		return this.colors.get(colorIndex);
	}
}