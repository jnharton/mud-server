package mud;

import java.util.*;

import mud.objects.*;
import mud.objects.items.Arrow;
import mud.objects.items.Container;
import mud.utils.Utils;
import mud.game.PClass;
import mud.interfaces.ODBI;
import mud.net.Client;

/*
 * Copyright (c) 2012 Jeremy N. Harton, joshgit?
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * 
 * This replaces ArrayList(s) for object types in MUDServer
 * 
 * @author joshgit, jnharton
 *
 */

public final class ObjectDB implements ODBI {

	private static int nextId = 0;

	// TreeMap allows instant retrieval by id and by name.
	final private TreeMap<String, MUDObject>  objsByName = new TreeMap<String, MUDObject>();
	final private TreeMap<Integer, MUDObject> objsById = new TreeMap<Integer, MUDObject>();

	// holds unused database references, that exist due to "recycled" objects
	private Stack<Integer> unusedDBNs = new Stack<Integer>();
	private List<Integer> reservedDBNs = new LinkedList<Integer>();

	// Hashtable is used here because it does not permit null values
	//private Hashtable<Client, LinkedList<Integer>> reservationTable = new Hashtable<Client, LinkedList<Integer>>();

	/**
	 * As long as we construct objects and insert them into this db afterwards as a separate step,
	 * getting the next id is somewhat of a hack. We could easily construct objects with an invalid id
	 * or never add them to this db.
	 */
	public int peekNextId() {
		if( !unusedDBNs.empty() ) { return unusedDBNs.peek(); }
		else { return nextId; }
	}

	// do not use, yet
	/**
	 * Reserve a database reference number (or ID) for future use.
	 * 
	 * Takes a new dbref number ands adds it to the reserved list,
	 * unless there is an unused number already, in which case it
	 * reserves that one.
	 */
	/*public Integer reserveID(final Client client) {
    	final Integer id;

    	if( !unusedDBNs.empty() ) { id = unusedDBNs.pop(); }
    	else { id = nextId++; }                              // grab the next it, before incrementing it by one

    	reservedDBNs.add(id);

    	if( reservationTable.containsKey(client) ) {
    		reservationTable.get(client).add(id); // mark who reserved it
    	}
    	else reservationTable.put(client, new LinkedList<Integer>() { { add(id); } });

    	return id;
    }

    public List<Integer> reserveIDs(final int numIds, final Client client) {
    	final List<Integer> ids = new LinkedList<Integer>();

    	int index = 0;

    	while(index < numIds) {
    		if( !unusedDBNs.empty() ) { ids.add( unusedDBNs.pop() ); }
        	else { ids.add( nextId++ ); }

    		reservedDBNs.add( ids.get(index) );

    		index++;
    	}

    	System.out.println( ids );

    	return ids;
    }

    public void useID(final Integer id, final Client client) {
    	if( client != null ) {
    		List<Integer> ids = reservationTable.get(client);
    		if( id != null ) {
    			ids.remove(id);
    			reservedDBNs.remove(id);
    		}
    	}
    }

    public void freeID(final Integer id, final Client client) {
    	if( client != null ) {
    		List<Integer> ids = reservationTable.get(client);
    		if( id != null ) {
    			ids.remove(id);
    			reservedDBNs.remove(id);
    			// re-add id to unused?
    		}
    	}
    }

    public void freeIDs(final List<Integer> ids, final Client client) {
    	if( client != null ) {
    		if( reservationTable.containsKey(client) ) {
    			if( ids != null ) {
    				List<Integer> ids1 = reservationTable.get(client);
    				ids1.removeAll( ids );
    				reservedDBNs.removeAll( ids );
    				// re-add ids to unused?
    			}
    		}
    	}
    }*/

	/**
	 * Add an unused database reference number to the list
	 * of numbers to reuse, but only if it is not reserved.
	 * 
	 * @param unusedId
	 */
	private void addUnused(int unusedId) {
		MUDObject mobj = get(unusedId);
		int next = peekNextId();{

			if( mobj instanceof NullObject ) {
				NullObject no = (NullObject) mobj;

				if( !no.isLocked() ) {
					System.out.println("dbref isn't in use");
					unusedDBNs.push(unusedId);
				}
				else {
					System.out.println("NullObject is locked");
				}
			}
			else if( reservedDBNs.contains(unusedId) ) {
				System.out.println("That id is reserved!");
			}
			else if(unusedDBNs.empty() && unusedId == next - 1) { // we just made a new one, but it's the most recent one in the db, just go back one
				nextId--;
			}
			else {
				System.out.println("Something is already using that id!");
			}
		}
	}

	public Stack<Integer> getUnused() {
		return this.unusedDBNs;
	}

	/*public List<Integer> getUnused() {
    	List<Integer> result = new LinkedList<Integer>();

    	for(Integer i : unusedDBNs) {
    		result.add(i);
    	}

    	return result;
    }*/

	public int getSize() {
		return objsById.size();
	}

	public void addAsNew(final MUDObject item) {
		if( !unusedDBNs.empty() ) {
			item.setDBRef( unusedDBNs.pop() );
		}
		else {
			item.setDBRef( nextId );
		}

		add(item);
	}

	/**
	 * mostly used for adding objects in file to in-memory database, all of
	 * which have, of course, valid dbrefs that follow each other sequentially.
	 * 
	 * @param item
	 */
	public void add(final MUDObject item) {
		//System.out.println("");
		//System.out.println("ObjectDB " + nextId + ": " + item.getDBRef());

		/* If the object's dbref doesn't match the next id (e.g. there's no
		 * database entry and the db skips over a number) then insert a NullObject,
		 * then the object that didn't match the id.
		 * 
		 * NOTE: This has been setup as a while loop to catch multiple such objects.
		 * 
		 * WARNING: The absence of entries for multiple consecutive dbrefs may indicate
		 * some kind of database corruption or data loss. To handle that possibility,
		 * the code below will lock the NullObject (prevent editing) so that the errors
		 * are easier to find in the database later?
		 */
		while( item.getDBRef() != nextId && get(item.getDBRef()) == null ) {
			// TODO fix this code, seriously! it will fill up the database with nullobjects if it hits ONE problem!
			System.out.println("nextId: " + nextId);
			System.out.println("dbref:  " + item.getDBRef());

			NullObject no = new NullObject(nextId);
			no.lock();

			objsById.put(no.getDBRef(), no);

			System.out.println("Inserted NullObject!");

			nextId++;
			continue;
		}

		System.out.println("");
		System.out.println("ObjectDB " + nextId + ": " + item.getDBRef());

		System.out.println(item.getDBRef() + ": " + item.getName());
		System.out.println("");

		if(item instanceof NullObject) {
			NullObject no = (NullObject) item;

			objsById.put(no.getDBRef(), no);

			System.out.println("Inserted NullObject!");

			if( !no.isLocked() ) {
				addUnused(item.getDBRef());
			}

			nextId++;
		}
		else {
			// kludge for doors here (give one object two different name entries)
			if( item.getName().contains("/") ) {
				String[] temp = item.getName().split("/");

				objsByName.put(temp[0], item);
				objsByName.put(temp[1], item);
			}
			else {
				objsByName.put(item.getName(), item);
			}

			objsById.put(item.getDBRef(), item);

			nextId++;
		}
	}

	/*public void allocate(final int n) {
        // Until we travel back to 1970 and convert this codebase into C, we probably don't need to pre-allocate things.
        // Unless users are creating rooms faster that the JVM can allocate objects...
    }*/

	// Send all objects
	public void dump(final Client client, final MUDServer server) {
		int i = 0;
		//for (final MUDObject obj : objsByName.values()) {
		for (final MUDObject obj : objsById.values()) {
			server.send(String.format("%s: %s (#%s)", i, obj.getName(), obj.getDBRef()), client);
			i += 1;
		}
	}

	public void dump() {
		int i = 0;
		//for (final MUDObject obj : objsByName.values()) {
		for (final MUDObject obj : objsById.values()) {
			System.out.println(String.format("%s: %s (#%s)", i, obj.getName(), obj.getDBRef()));
			i += 1;
		}
	}

	// remove object from DB, but insert a NullObject placeholder
	public void remove(final MUDObject item) {    	
		objsById.put(item.getDBRef(), new NullObject(item.getDBRef()));
		objsByName.remove(item.getName());
	}

	// Ensure object is in both maps, overwriting any object in the id map.
	/*public void set(final int n, final MUDObject item) {
        objsById.put(n, item);
        objsByName.put(item.getName(), item);
    }*/

	/**
	 * Get an object by it's Database Reference Number (DBRef)
	 * which is unique in the database
	 * 
	 * @param n the DBRef of the object you wish to get
	 * @return the object with that DBRef (if it exists)
	 */
	public MUDObject get(final int n) {
		return objsById.get(n);
	}

	public List<MUDObject> getObjects() {
		return new ArrayList<MUDObject>(objsById.values());
	}

	// TODO kludgy, used for/with cnames
	public void addName(MUDObject object, String name) {
		if( this.getByName(name) == null ) {
			objsByName.put( name, object );

			if( object instanceof Player ) {
			}
		}
	}

	/**
	 * Does the database contain an object by the name
	 * passed in? If so, return true
	 * 
	 * @param name the name of an object to look for
	 * @return true if there is object with the specified name
	 */
	public boolean hasName(final String name) {
		return objsByName.containsKey(name);
	}

	/**
	 * Does the database contain an object by the name
	 * passed in? If so, return that object.
	 * 
	 * @param name the name of an object to look for
	 * @return the object with the specified name (if there is one)
	 */
	public MUDObject getByName(final String name) {
		return objsByName.get(name);
	}

	/**
	 * Gets a List of the object in or attached to the specified
	 * room.
	 * 
	 * @param room
	 * @return
	 */
	public List<MUDObject> getByRoom(final Room room) {
		List<MUDObject> objects = new ArrayList<MUDObject>(100);

		objects.addAll( getExitsByRoom( room.getDBRef() )   );
		objects.addAll( getThingsForRoom( room.getDBRef() ) );
		objects.addAll( getItemsByLoc( room.getDBRef() )    );
		objects.addAll( getNPCsByRoom( room.getDBRef() )    );
		objects.addAll( getPlayersByRoom( room.getDBRef() ) );

		return objects;
	}

	public void changeName(final MUDObject object, final String newName) {
		object.setName(newName);

		objsByName.remove( object.getName() );
		objsByName.put( newName, object );

		if(object instanceof NPC) {
			final NPC npc = (NPC) object; 
			npcsByName.remove( npc.getName() );
			npcsByName.put( newName, npc );
		}
		else if(object instanceof Room) {
			final Room room = (Room) object;
			roomsByName.remove( room.getName() );
			roomsByName.put( newName, room );
		}
		else if(object instanceof Exit) {
			final Exit exit = (Exit) object;
			exitsByName.remove( exit.getName() );
			exitsByName.put( newName, exit );
		}
	}

	public void updateName(final MUDObject object) {
		final String newName = object.getName();

		objsByName.remove( object.getName() );
		objsByName.put( newName, object );

		if(object instanceof NPC) {
			final NPC npc = (NPC) object; 
			npcsByName.remove( npc.getName() );
			npcsByName.put( newName, npc );
		}
		else if(object instanceof Room) {
			final Room room = (Room) object;
			roomsByName.remove( room.getName() );
			roomsByName.put( newName, room );
		}
		else if(object instanceof Exit) {
			final Exit exit = (Exit) object;
			exitsByName.remove( exit.getName() );
			exitsByName.put( newName, exit );
		}
	}

	/**
	 * Find objects in the database whose names start with the
	 * specified search term. Use the lowercase of the object's
	 * name and the search term for comparison
	 * 
	 * @param name
	 * @return
	 */
	public List<MUDObject> findByLower(final String name) {
		final LinkedList<MUDObject> acc = new LinkedList<MUDObject>();
		for (final MUDObject obj : objsByName.values()) {
			if(obj.getName().toLowerCase().startsWith(name.toLowerCase())) {
				acc.add(obj);
			}
		}
		return acc;
	}

	/**
	 * Get the number of objects with a particular flag set. This
	 * is chiefly intended for getting a quick count of say, the number
	 * of (P)layers, (R)ooms, (E)xits, etc. However it could also be used
	 * to count objects that are flagged (D)ark for instance.
	 * 
	 * @param letters the flags to search for
	 * @return
	 */
	public int[] getFlagCounts(final String[] letters) {
		final int[] counts = new int[letters.length];
		Arrays.fill(counts, 0);

		// objsById returns more objects than objsByName
		for (final MUDObject obj : objsById.values()) {
			for (int i = 0; i < letters.length; i++) {
				if (obj.type.toString().startsWith(letters[i])) {
					counts[i] += 1;
				}
			}
		}

		return counts;
	}

	// Serialize all objects via `toDB` and save array to file.
	// TODO fix save method, this one depends on saving over the old database
	public void save(final String filename) {
		final String[] old = Utils.loadStrings(filename);    // old (current in file) database
		final String[] toSave = new String[objsById.size()]; // new (save to file) database

		int index = 0;

		if( old != null ) {
			for (final MUDObject obj : objsById.values()) {
				if(obj instanceof NullObject) {
					NullObject no = (NullObject) obj;

					// a locked NullObject probably means a line whose data we are ignoring (but want to keep)
					if( no.isLocked() ) {
						System.out.println(index + " Old Data: " + old[index] + " Keeping...");
						toSave[index] = old[index]; // keep old data
					}
					else {
						System.out.println(index + " No Previous Data, Overwriting...");
						toSave[index] = obj.toDB(); // just save the null object
					}
				}
				else if(obj instanceof Player) {
					Player player = (Player) obj;

					if( !player.isNew() ) { // if the player is not new, save it
						toSave[index] = obj.toDB();
					}
					else { // otherwise, store a NullObject
						toSave[index] = new NullObject(obj.getDBRef()).toDB();
					}
				}
				else {
					System.out.println(obj.getName());

					toSave[index] = obj.toDB();
				}

				index++;
			}
		}
		else {
			// TODO figure out if this part of save(...) should just go elsewhere. This is essentially
			// breaking the notion of save since we aren't saving over an existing database
			for (final MUDObject obj : objsById.values()) {
				System.out.println(obj.getName());

				toSave[index] = obj.toDB();

				System.out.println(toSave[index]);

				index++;
			}
		}

		Utils.saveStrings(filename, toSave);
	}

	////////////// ROOMS
	final private Map<Integer, Room> roomsById   = new HashMap<Integer, Room>();
	final private Map<String, Room>  roomsByName = new HashMap<String, Room>();

	// must add room to both maps
	public void addRoom(final Room room) {
		roomsByName.put(room.getName(), room);
		roomsById.put(room.getDBRef(), room);
	}

	// must remove room from both maps
	public void removeRoom(final Room r) {
		roomsByName.values().remove(r);
		roomsById.values().remove(r);
	}

	public Room getRoomByName(final String name) {
		return roomsByName.get(name);
	}

	public Room getRoomById(final int id) {
		return roomsById.get(id);
	}

	public List<Room> getRoomsByType(final RoomType type) {
		final List<Room> acc = new LinkedList<Room>();
		for (final Room r : roomsById.values()) {
			if (r.getRoomType() == type) {
				acc.add(r);
			}
		}
		return acc;
	}

	public List<Room> getWeatherRooms() {
		return getRoomsByType(RoomType.OUTSIDE);
	}

	public List<Room> getRoomsByParentLocation(final int loc) {
		final List<Room> acc = new LinkedList<Room>();
		for (final Room r : roomsById.values()) {
			if (r.getParent() == loc) {
				acc.add(r);
			}
		}
		return acc;
	}

	public List<Room> getRoomsByLocation(final int loc) {
		final List<Room> acc = new LinkedList<Room>();
		for (final Room r : roomsById.values()) {
			if (r.getLocation() == loc) {
				acc.add(r);
			}
		}
		return acc;
	}

	/////////////// EXITS
	private final Map<Integer, Exit> exitsById   = new HashMap<Integer, Exit>();
	private final Map<String, Exit>  exitsByName = new HashMap<String, Exit>();

	public Exit getExit(final String name) {
		return exitsByName.get(name);
	}

	public Exit getExit(final int dbref) {
		return exitsById.get(dbref);
	}

	public List<Exit> getExitsByRoom(final int loc) {
		final List<Exit> acc = new LinkedList<Exit>();
		for (final Exit e : exitsById.values()) {
			if (e.getLocation() == loc) {
				acc.add(e);
			}
		}
		return acc;
	}

	public void addExit(final Exit exit) {
		if( exit.getExitType() == ExitType.DOOR ) {
			String[] names = exit.getName().split("/");

			exitsByName.put(names[0], exit);
			exitsByName.put(names[1], exit);
		}
		else { exitsByName.put(exit.getName(), exit); }

		exitsById.put(exit.getDBRef(), exit);
	}

	public void removeExit(final Exit e) {
		if( e.getExitType() == ExitType.DOOR ) {
			String[] temp = e.getName().split(";");
			String[] names = temp[0].split("/");

			exitsByName.values().remove(names[0]);
			exitsByName.values().remove(names[1]);
		}
		else { exitsByName.remove(e.getName()); }

		exitsById.values().remove(e.getDBRef());
	}

	/**
	 * Go through all the exits that exist in the database and
	 * place/attach them in/to the respective rooms they are part of
	 */
	public void loadExits(final MUDServer parent) {
		parent.debug("Loading exits:", 2);
		for (final Exit e : exitsById.values()) {
			if( e.getExitType() == ExitType.DOOR ) {
				final Room room = getRoomById(e.getLocation());

				if (room != null) {
					room.getExits().add(e);
					parent.debug("Exit (Door)" + e.getDBRef() + " added to room " + room.getDBRef() + ".", 2);
				}

				final Room room1 = getRoomById(e.getDestination());

				if (room1 != null) {
					room1.getExits().add(e);
					parent.debug("Exit (Door)" + e.getDBRef() + " added to room " + room1.getDBRef() + ".", 2);
				}
			}
			else {
				final Room room = getRoomById(e.getLocation());

				if (room != null) {
					room.getExits().add(e);
					parent.debug("Exit " + e.getDBRef() + " added to room " + room.getDBRef() + ".", 2);
				}
			}
		}
		parent.debug("Done loading exits:", 2);
	}

	//////////////////// CREATURES
	// Should rooms store their own creatures?
	final private ArrayList<Creature> creeps = new ArrayList<Creature>();

	public void addCreature(final Creature c) {
		creeps.add(c);
	}

	public List<Creature> getCreatures() {
		return new ArrayList<Creature>(creeps);
	}

	public List<Creature> getCreaturesByRoom(final int loc) {
		final List<Creature> acc = new LinkedList<Creature>();
		for (final Creature c : creeps) {
			if (c.getLocation() == loc) {
				acc.add(c);
			}
		}
		return acc;
	}

	////////////////// NPCs
	private final Map<Integer, NPC> npcsById   = new HashMap<Integer, NPC>();
	private final Map<String, NPC>  npcsByName = new HashMap<String, NPC>();

	public void addNPC(final NPC npc) {
		npcsById.put(npc.getDBRef(), npc);
		npcsByName.put(npc.getName(), npc);
		npcsByName.put(npc.getCName(), npc);
	}

	/**
	 * Get the NPC that has the specified name.
	 * 
	 * NOTE: As a special case, an empty string returns null.
	 * 
	 * @param name
	 * @return
	 */
	public NPC getNPC(final String name) {
		if( name.equals("") ) return null;
		return npcsByName.get(name);
	}

	public NPC getNPC(final int dbref) {
		if( dbref < 0 ) return null;
		return npcsById.get(dbref);
	}

	public List<NPC> getNPCs() {
		return new ArrayList<NPC>(npcsById.values());
	}

	public List<NPC> getNPCsByRoom(final int loc) {
		final List<NPC> acc = new LinkedList<NPC>();
		for (final NPC n : npcsById.values()) {
			if (n.getLocation() == loc) {
				acc.add(n);
			}
		}
		return acc;
	}

	////////////////// ITEMS
	private final ArrayList<Item> items = new ArrayList<Item>();

	// these are not presently loaded with anything by the code (8-23-2013)
	private final Map<Integer, Item> itemsById   = new HashMap<Integer, Item>();

	public void addItem(final Item item) {
		items.add(item);
		itemsById.put(item.getDBRef(), item);
	}

	public void removeItem(final Item item) {
		items.remove(item);
		itemsById.remove(item.getDBRef());
	}

	public Item getItem(final int dbref) {
		return itemsById.get(dbref);
	}

	// somewhat pointless, since items are more likely to have the same name than most other objects
	public Item getItem(final int roomId, final String name) {
		for (final Item item : items) {
			if (item.getLocation() == roomId && item.getName().equals(name)) return item;
		}
		
		return null;
	}

	public List<Item> getItems() {
		return new ArrayList<Item>(itemsById.values());
	}

	public void addItemsToRooms() {
		for (final Item item : items) {
			final Room r = getRoomById(item.getLocation());
			
			if (r != null) {
				r.addItem(item);
			}
		}
	}

	public void addItemsToContainers() {
		for (final Item item : items) {
			if( item instanceof Container ) {
				Container c = (Container) item;
				
				List<Item> items1 = getItemsByLoc(item.getDBRef());
				
				for(Item item2 : items1) {
					c.insert(item2);
				}
			}
		}
	}

	/**
	 * Get the items held by a specific player
	 * 
	 * @return a map of with key: item, and value: player that holds references to the items the player is holding
	 */
	public Map<Item, Player> getItemsHeld() {
		final Map<Item, Player> acc = new HashMap<Item, Player>();
		for (final Item item : items) {
			final MUDObject obj = get(item.getLocation());
			if (obj instanceof Player) {
				acc.put(item, (Player) obj);
			}
		}
		return acc;
	}

	/**
	 * Get a List containing references to the items located in the
	 * object with dbref, loc.
	 * 
	 * @param loc the dbref of the object where the items are located
	 * @return list of items located in the object referred to by dbref
	 */
	public List<Item> getItemsByLoc(final int loc) {
		final List<Item> acc = new LinkedList<Item>();
		for (final Item item : items) {
			if (item.getLocation() == loc) {
				acc.add(item);
			}
		}
		return acc;
	}

	/**
	 * stack all the stackable items
	 */
	public void stackItems() {
		List<Item> items = getItems();

		List<Arrow> arrows = new LinkedList<Arrow>();

		for(Item item : items) {
			if(item instanceof Arrow) arrows.add( (Arrow) item );
		}

		for(Arrow arrow : arrows) {
			Item item = getItem( arrow.getLocation() );

			if( item != null && item instanceof Arrow ) {
				if( !(arrow == item) ) ((Arrow) item).stack(arrow);
			}
		}
	}

	///////////// THINGS
	final private ArrayList<Thing> things = new ArrayList<Thing>();
	final private Map<Integer, Thing> thingsById = new HashMap<Integer, Thing>();

	public void addThing(final Thing thing) {
		things.add(thing);
		thingsById.put(thing.getDBRef(), thing);
	}

	public void removeThing(final Thing thing) {
		things.remove(thing);
		thingsById.remove(thing.getDBRef());
	}

	public Thing getThing(int dbref) {
		return thingsById.get(dbref);
	}

	public Thing getThing(final int roomId, final String name) {
		for (final Thing thing : things) {
			if (thing.getLocation() == roomId && thing.getName().equals(name)) return thing;
		}
		
		return null;
	}

	public List<Thing> getThings() {
		return new ArrayList<Thing>(thingsById.values());
	}

	public List<Thing> getThingsForRoom(final int roomId) {
		final List<Thing> acc = new LinkedList<Thing>();
		for (final Thing t : things) {
			if (t.getLocation() == roomId) {
				acc.add(t);
			}
		}
		return acc;
	}

	public void placeThingsInRooms(final MUDServer parent) {
		for (final Thing thing : things) {
			if (thing != null) {
				final Room room = parent.getRoom(thing.getLocation());
				if (room != null) {
					room.addThing( thing );
				}
			}
		}
	}

	///////////////////////////// PLAYERS
	private final Map<String, Player> playersByName = new HashMap<String, Player>();

	public void addPlayer(final Player p) {
		playersByName.put(p.getName(), p);
	}

	public Player getPlayer(final String name) {
		// TODO fix kludginess, used with/for cnames
		Player player = playersByName.get(name);

		if( player == null ) {
			MUDObject object = getByName(name);

			if( object instanceof Player ) {
				player = (Player) object;
			}
		}

		return player;
		//return players.get(name);
	}

	public int getNumPlayers(final PClass c) {
		int count = 0;
		for (final Player p : playersByName.values()) {
			if (c.equals(p.getPClass())) {
				count += 1;
			}
		}
		return count;
	}

	public List<Player> getPlayersByRoom(final int loc) {
		final List<Player> acc = new LinkedList<Player>();
		for (final Player p : playersByName.values()) {
			if (p.getLocation() == loc) {
				acc.add(p);
			}
		}
		return acc;
	}

	/**
	 * Erase the entire database
	 */
	 public void clear() {
		 things.clear();

		 items.clear();

		 creeps.clear();

		 npcsById.clear();
		 npcsByName.clear();

		 playersByName.clear();

		 exitsById.clear();
		 exitsByName.clear();

		 roomsById.clear();
		 roomsByName.clear();

		 objsById.clear();
		 objsByName.clear();

		 unusedDBNs.clear();
		 reservedDBNs.clear();
	 }
}