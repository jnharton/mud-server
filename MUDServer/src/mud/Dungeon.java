package mud;

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

import java.util.EnumSet;

import mud.interfaces.Instance;
import mud.objects.Room;
import mud.utils.Utils;

/**
 * Class for an Instanced Dungeon
 * 
 * To some extent this intends a square x by x grid of rooms that make up
 * a "dungeon"
 * 
 * @author Jeremy
 *
 */
public class Dungeon implements Instance
{	
	private Dungeon parent = null;      // holds a reference to the primary on a non-primary
	private boolean isInstance = false; // true, unless primary
	private int instance_id = 0;        // this instance's id (-1/0 on the primary one)
	private int instances = 0;          // number of instances (used on the primary one)
	private String name = "";           // name of the dungeon
	private String instance_name = "";  // name of the dungeon instance (probably redundant)
	private int dimX = 0;               // number of rooms wide
	private int dimY = 0;               // number of rooms long
	public Room[][] dRooms;
	public int[] roomIds;
	private int counter = 0; // index to roomIds;

	public Dungeon(String name, int x, int y)
	{
		dimX = x;
		dimY = y;
		roomIds = new int[dimX * dimY];
		instance_id = -1;
		instance_name = name;
		dRooms = new Room[dimX][dimY];

		//debug("DimX: " + dimX);
		//debug("DimY: " + dimY);

		for (int i = 0; i < dimX; i++)
		{
			for (int j = 0; j < dimY; j++)
			{
				int id = 0;
				//int id = nextDB();
				Room room = new Room(id, instance_name + " Room (" + i + ", " + j + ")", EnumSet.of(ObjectFlag.ROOM, ObjectFlag.DARK), "You see nothing.", 0);
				// flags are created statically here, because I need to take in more variables and in no case should create anything other than a standard room which has 'RD' for a flag
				//main.add(room.toDB());
				//rooms1.add(room);
				//send("Room '" + room.getName() + "' created as #" + id + ". Parent set to " + Utils.trim("0") + "."); // room creation message
				//nextDB("use");
				//debug("Counter: " + counter);
				roomIds[counter] = room.getDBRef();
				counter = counter + 1;
				dRooms[i][j] = room;
				//debug(dRooms[i][j].getName() + " " + dRooms[i][j].getDBRef());
			}
		}
	}

	// work in progress, basically needs to duplicate the dungeon instance passed to it
	public Dungeon(Dungeon template) {
		if (!template.isInstance()) { // use the template
			parent = template;    // set child's parent
		}
		else { // use the template's parent
			parent = template.parent;    // set child's parent
		}

		// set child values
		isInstance = true;
		instance_id = parent.instances++;
		instance_name = parent.instance_name + "(" + instance_id + ")";

		// copy the dungeon
		dimX = parent.dimX;
		dimY = parent.dimY;
		roomIds = new int[dimX * dimY];
		dRooms = new Room[dimX][dimY];

		//debug("DimX: " + dimX);
		//debug("DimY: " + dimY);

		for (int i = 0; i < dimX; i++)
		{
			for (int j = 0; j < dimY; j++)
			{
				int id = 0;
				
				//int id = nextDB(); // get the next free database reference

				// flags are created statically here, because I need to take in more variables and in no case should create anything other than a standard room which has 'RD' for a flag
				Room room = new Room(id, instance_name + " Room (" + i + ", " + j + ")", EnumSet.of(ObjectFlag.ROOM, ObjectFlag.DARK), "You see nothing.", 0);

				//main.add(room.toDB());
				//rooms1.add(room);

				//send("Room '" + room.getName() + "' created as #" + id + ". Parent set to " + Utils.trim("0") + ".");

				//nextDB("use");

				//debug("Counter: " + counter);

				roomIds[counter] = room.getDBRef();

				counter = counter + 1;

				dRooms[i][j] = room;

				//debug(dRooms[i][j].getName() + " " + dRooms[i][j].getDBRef());
			}
		}
	}

	@Override
	public int getInstanceId() {
		return this.instance_id;
	}

	@Override
	public boolean isInstance() {
		return this.isInstance;
	}
}