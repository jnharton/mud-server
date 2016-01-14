package mud.misc;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mud.objects.Exit;
import mud.objects.Room;

public class Building {
	private String name;
	private String shortName;
	
	private Room parent;
	
	private Map<String, Exit> entrances; // places where you can enter the building (ex. doors, windows, holes in the walls)
	private Map<String, Exit> exits;     // places where you can leave the building (ex. doors, windows, holes in the walls)
	
	private List<Edge> sides;
	
	public Building(Room parent, String name, String shortName, Edge...edges) {
		this.parent = parent;
		this.name = name;
		this.shortName = shortName;
		this.sides = new LinkedList<Edge>( Arrays.asList( edges ) );	
	}
	
	public Map<String, Exit> getEntrances() {
		return Collections.unmodifiableMap( this.entrances );
	}
	
	public Map<String, Exit> getExits() {
		return Collections.unmodifiableMap( this.exits );
	}
	
	/**
	 * Get a List of the Edges (or sides) of the building that
	 * is unmodifiable for the purposes of determining where the
	 * character can walk.
	 * 
	 * NOTE:
	 * Edges consist of two points and the length of the straight line connecting
	 * them.
	 * 
	 * @return
	 */
	public List<Edge> getSides() {
		return Collections.unmodifiableList( sides );
	}
}