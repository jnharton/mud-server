package mud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import mud.objects.*;
import mud.objects.exits.Door;
import mud.objects.items.Arrow;
import mud.objects.items.Container;
import mud.utils.Utils;

import mud.game.PClass;
import mud.interfaces.ODBI;

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
	private int nextId = 0;

	// TreeMap allows instant retrieval by id and by name.
	private final Map<String, MUDObject>  objsByName = new TreeMap<String, MUDObject>();
	private final Map<Integer, MUDObject> objsById = new TreeMap<Integer, MUDObject>();

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
		if( !unusedDBNs.empty() ) {
			return this.unusedDBNs.peek();
		}
		else {
			return this.nextId;
		}
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
		MUDObject mobj = getById(unusedId);
		
		// TODO do we really need to double check what we've presumably already vetted?
		if( mobj instanceof NullObject ) {
			NullObject no = (NullObject) mobj;

			if( !no.isLocked() ) {
				System.out.println("dbref isn't in use");
				this.unusedDBNs.push(unusedId);
			}
			else {
				System.out.println("NullObject is locked");
			}
		}
		else if( reservedDBNs.contains(unusedId) ) {
			System.out.println("That id is reserved!");
		}
		else {
			System.out.println("Something is already using that id!");
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
		return this.objsById.size();
	}

	// Send all objects
	public List<String> dump() {
		int i = 0;

		final List<String> output = new ArrayList<String>(this.objsById.size());

		for (final MUDObject obj : objsById.values()) {
			output.add( String.format("%d: %s (#%d)", i, obj.getName(), obj.getDBRef()) );
			i += 1;
		}

		return output;
	}

	// Serialize all objects via `toDB` and save array to file.
	// TODO fix save method, this one depends on saving over the old database
	public void save(final String filename) {
		final String[] old = Utils.loadStrings(filename);         // old (current in file) database
		final String[] toSave = new String[this.objsById.size()]; // new (save to file) database

		// TODO sometimes has issues with NullPointerException(s)

		System.out.println("Old Size: " + old.length);
		System.out.println("New Size: " + toSave.length);

		int index = 0;

		if( old != null ) {
			for (final MUDObject obj : this.objsById.values()) {
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
	
	/**
	 * mostly used for adding objects in file to in-memory database, all of
	 * which have, of course, valid dbrefs that follow each other sequentially.
	 * 
	 * @param object
	 */
	public void add(final MUDObject object) {
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

		// >>> fills in gaps in DBRef Ids with nullobjects and handles ignored db lines (hence locking)
		// if the item dbref doesn't match the next one AND there is no entry for that id...


		System.out.println("");
		System.out.println("ObjectDB " + nextId + ": " + object.getDBRef());

		System.out.println(object.getDBRef() + ": " + object.getName());
		System.out.println("");

		// --
		if( object.getDBRef() != nextId ) {
			// TODO This shouldn't be a problem at all, maybe... (There should always be a line in the database for every dbref that's ever been used?
			if( object.getDBRef() > nextId ) {
				// TODO the check for a non-null object is a KLUDGE so that we can use add(...) for loading and new stuff.
				while( object.getDBRef() != nextId && getById(object.getDBRef()) == null) {
					// TODO fix this code, seriously! it will fill up the database with nullobjects if it hits ONE problem!
					System.out.println("nextId: " + nextId);
					System.out.println("dbref:  " + object.getDBRef());
					System.out.println("");

					// create a NullObject AND lock it
					NullObject no = new NullObject(nextId);
					no.lock();

					// insert NullObject
					this.objsById.put(no.getDBRef(), no);

					System.out.println("Inserted NullObject!");
					System.out.println("");

					this.nextId++;

					continue;
				}

			}
		}
		
		boolean skip = false;

		this.objsById.put(object.getDBRef(), object);

		/* If there's a null object in the database, let us know.
		 * If it's just because of an ignored line it will be locked,
		 * but otherwise we should take note of the dbref for re-use
		 */
		if(object instanceof NullObject) {
			System.out.println("Inserted NullObject!");

			NullObject no = (NullObject) object;

			// if it's just an empty space from a deleted objects we can reuse the id
			if( no.isLocked() ) System.out.println(">>> Object is Locked <<<");
			else                addUnused( object.getDBRef() );

			skip = true;
		}

		this.objsByName.put(object.getName(), object);

		// TODO what type is a NullObject, does it have a type? (if it does we can avoid the boolean here...)
		if( !skip ) {
			// add additional names for exits (conveniences/game/player use)
			if( object.isType(TypeFlag.EXIT) ) {
				final Exit exit = (Exit) object; 

				if( exit.getExitType() == ExitType.DOOR ) {
					final Door d = (Door) object;

					// kludge for doors here (give one object two different name entries)
					if( d.getName().contains("/") ) {
						String[] temp = d.getName().split("/");

						this.objsByName.put(temp[0], object);
						this.objsByName.put(temp[1], object);
					}
				}
			}
		}

		this.nextId++;
	}
	
	// TODO NOTE: do not use addAsNew with NULLObjects?
	public void addAsNew(final MUDObject object) {
		if( object instanceof NullObject) {
			System.out.println("ObjectDB (addAsNew): Error");
			return;
		}
		
		if( !this.unusedDBNs.empty() ) object.setDBRef( this.unusedDBNs.pop() );
		else                           object.setDBRef( this.nextId++ );
		
		// TODO adding it is a buggy mess due to conflicting uses of add(...), load vs add new stuff
		//add(item);
		
		this.objsById.put(object.getDBRef(), object);
		this.objsByName.put(object.getName(), object);

		// add additional names for exits (conveniences/game/player use)
		if( object.isType(TypeFlag.EXIT) ) {
			final Exit exit = (Exit) object; 

			if( exit.getExitType() == ExitType.DOOR ) {
				final Door d = (Door) object;

				// kludge for doors here (give one object two different name entries)
				if( d.getName().contains("/") ) {
					String[] temp = d.getName().split("/");

					this.objsByName.put(temp[0], object);
					this.objsByName.put(temp[1], object);
				}
			}
		}
	}

	// remove object from DB, but insert a NullObject placeholder
	public void remove(final MUDObject item) {
		final int DBREF = item.getDBRef();
		
		this.objsById.remove( DBREF );
		this.objsByName.values().remove(item);
		
		final NullObject no = new NullObject( DBREF );
		
		this.objsById.put(DBREF, no);
		this.objsByName.put(no.getName(), no);
		
		addUnused( DBREF );
	}

	// Ensure object is in both maps, overwriting any object in the id map.
	/*public void set(final int n, final MUDObject item) {
        objsById.put(n, item);
        objsByName.put(item.getName(), item);
    }*/

	public List<MUDObject> getObjects() {
		return Collections.unmodifiableList( new ArrayList<MUDObject>(this.objsById.values()) );
	}

	/**
	 * Get an object by it's Database Reference Number (DBRef)
	 * which is unique in the database
	 * 
	 * @param dbref the DBRef of the object you wish to get
	 * @return the object with that DBRef (if it exists)
	 */
	public MUDObject getById(final int dbref) {
		if( dbref < 0 ) return null;

		return this.objsById.get(dbref);
	}

	/**
	 * Does the database contain an object by the name
	 * passed in? If so, return that object.
	 * 
	 * @param name the name of an object to look for
	 * @return the object with the specified name (if there is one)
	 */
	public MUDObject getByName(final String name) {
		return this.objsByName.get(name);
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

		objects.addAll( getExitsByRoom( room ) );
		objects.addAll( getThingsForRoom( room ) );
		objects.addAll( getItemsByLoc( room.getDBRef() )    );
		objects.addAll( getNPCsByRoom( room ) );
		objects.addAll( getPlayersByRoom( room ) );

		return objects;
	}

	// TODO kludgy, used for/with cnames
	public void addName(final MUDObject object, final String name) {
		if( getByName(name) == null ) {
			this.objsByName.put( name, object );

			if( object instanceof Player ) {
				this.playersByName.put(name, (Player) object);
			}
			else if( object instanceof Exit ) {
				this.exitsByName.put(name, (Exit) object);
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
		return this.objsByName.containsKey(name);
	}

	// the function just below the one below may have rendered this one unnecessary
	/*public void changeName(final MUDObject object, final String newName) {
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

		object.setName(newName);
	}*/

	// TODO test for object equality or some other so I can't accidentally pass in a similarly named object
	public void updateName(final MUDObject object, final String oldName) {
		final String newName = object.getName();

		this.objsByName.remove( oldName );
		this.objsByName.put( newName, object );
		
		if(object instanceof Player) {
			final Player player = (Player) object; 

			this.playersByName.remove( oldName );
			this.playersByName.put( newName, player );
		}
		else if(object instanceof NPC) {
			final NPC npc = (NPC) object; 

			this.npcsByName.remove( oldName );
			this.npcsByName.put( newName, npc );
		}
		else if(object instanceof Room) {
			final Room room = (Room) object;

			this.roomsByName.remove( oldName );
			this.roomsByName.put( newName, room );
		}
		else if(object instanceof Exit) {
			final Exit exit = (Exit) object;

			this.exitsByName.remove( oldName );
			this.exitsByName.put( newName, exit );
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
		
		final String testName = name.toLowerCase();
		
		for (final MUDObject obj : this.objsById.values()) {
			String objectName = obj.getName().toLowerCase();
			
			if( objectName.startsWith( testName ) ) {
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
		for (final MUDObject obj : this.objsById.values()) {
			for (int i = 0; i < letters.length; i++) {
				if (obj.type.toString().startsWith(letters[i])) {
					counts[i] += 1;
				}
			}
		}

		return counts;
	}

	////////////// ROOMS
	private final Map<Integer, Room> roomsById   = new HashMap<Integer, Room>();
	private final Map<String, Room>  roomsByName = new HashMap<String, Room>();

	// must add room to both maps
	public void addRoom(final Room room) {
		this.roomsByName.put(room.getName(), room);
		this.roomsById.put(room.getDBRef(), room);
	}

	// must remove room from both maps
	public void removeRoom(final Room r) {
		this.roomsByName.values().remove(r);
		this.roomsById.values().remove(r);
	}

	public Room getRoomByName(final String name) {
		return this.roomsByName.get(name);
	}

	/**
	 * getRoomById
	 * 
	 * NOTE: dbref #s <0 are considered invalid
	 */
	public Room getRoomById(final int id) {
		//return roomsById.get(id);
		Room room = null;

		if( id >= 0) {
			room = this.roomsById.get(id);
		}

		return room;
	}

	public List<Room> getRooms() {
		return Collections.unmodifiableList( new ArrayList<Room>(this.roomsById.values()) );
	}

	public List<Room> getRoomsByType(final RoomType type) {
		final List<Room> acc = new LinkedList<Room>();

		for (final Room r : this.roomsById.values()) {
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

		for (final Room r : this.roomsById.values()) {
			if (r.getParent() == loc) {
				acc.add(r);
			}
		}

		return acc;
	}

	public List<Room> getRoomsByLocation(final int loc) {
		final List<Room> acc = new LinkedList<Room>();

		for (final Room r : this.roomsById.values()) {
			if (r.getLocation() == loc) {
				acc.add(r);
			}
		}

		return acc;
	}

	/////////////// EXITS
	private final Map<Integer, Exit> exitsById   = new HashMap<Integer, Exit>();
	private final Map<String, Exit>  exitsByName = new HashMap<String, Exit>();

	public Exit getExit(final int dbref) {
		if( dbref < 0 ) return null;

		return this.exitsById.get(dbref);
	}

	public Exit getExit(final String name) {
		return this.exitsByName.get(name);
	}

	public List<Exit> getExits() {
		return Collections.unmodifiableList( new ArrayList<Exit>(this.exitsById.values()) );
	}

	public List<Exit> getExitsByRoom(final Room room) {
		final List<Exit> acc = new LinkedList<Exit>();

		int loc = room.getDBRef();

		for (final Exit e : this.exitsById.values()) {
			if (e.getLocation() == loc) {
				acc.add(e);
			}
		}

		return acc;
	}

	public void addExit(final Exit exit) {
		this.exitsById.put(exit.getDBRef(), exit);

		if( exit.getExitType() == ExitType.DOOR ) {
			String[] names = exit.getName().split("/");

			this.exitsByName.put(names[0], exit);
			this.exitsByName.put(names[1], exit);
		}
		else {
			this.exitsByName.put(exit.getName(), exit);
		}
	}

	public void removeExit(final Exit e) {
		this.exitsById.values().remove(e.getDBRef());

		if( e.getExitType() == ExitType.DOOR ) {
			String[] temp = e.getName().split(";");
			String[] names = temp[0].split("/");

			this.exitsByName.values().remove(names[0]);
			this.exitsByName.values().remove(names[1]);
		}
		else {
			this.exitsByName.remove(e.getName());
		}
	}

	//////////////////// CREATURES
	// Should rooms store their own creatures?
	private final List<Creature> creeps = new ArrayList<Creature>();

	public void addCreature(final Creature c) {
		this.creeps.add(c);
	}

	public List<Creature> getCreatures() {
		return new ArrayList<Creature>(this.creeps);
	}

	public List<Creature> getCreaturesByRoom(final Room room) {
		final List<Creature> acc = new LinkedList<Creature>();

		int loc = room.getDBRef();

		for (final Creature c : this.creeps) {
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
		this.npcsById.put(npc.getDBRef(), npc);
		this.npcsByName.put(npc.getName(), npc);
		this.npcsByName.put(npc.getCName(), npc);
	}

	public NPC getNPC(final int dbref) {
		if( dbref < 0 ) return null;

		return this.npcsById.get(dbref);
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

		return this.npcsByName.get(name);
	}

	public List<NPC> getNPCs() {
		return new ArrayList<NPC>(this.npcsById.values());
	}

	public List<NPC> getNPCsByRoom(final Room room) {
		final List<NPC> acc = new LinkedList<NPC>();

		int loc = room.getDBRef();

		for (final NPC n : this.npcsById.values()) {
			if (n.getLocation() == loc) {
				acc.add(n);
			}
		}

		return acc;
	}

	////////////////// ITEMS
	private final List<Item> items = new ArrayList<Item>();
	
	private final Map<Integer, Item> itemsById   = new HashMap<Integer, Item>();

	public void addItem(final Item item) {
		this.items.add(item);
		this.itemsById.put(item.getDBRef(), item);
	}

	public void removeItem(final Item item) {
		this.items.remove(item);
		this.itemsById.remove(item.getDBRef());
	}

	public void addItemsToRooms() {
		for (final Item item : this.items) {
			final Room r = getRoomById(item.getLocation());

			if (r != null) {
				r.addItem(item);
			}
		}
	}

	public void addItemsToContainers() {
		for (final Item item : this.items) {
			if( item instanceof Container ) {
				Container c = (Container) item;

				List<Item> items1 = getItemsByLoc(item.getDBRef());

				for(Item item2 : items1) {
					c.insert(item2);
				}
			}
		}
	}

	public Item getItem(final int dbref) {
		if( dbref < 0 ) return null;

		return this.itemsById.get(dbref);
	}

	// somewhat pointless, since items are more likely to have the same name than most other objects
	public Item getItem(final String name) {
		Item item = null;

		for (final Item item1 : this.items) {
			if ( item1.getName().equals(name) ) {
				item = item1;
			}
		}

		return item;
	}

	public List<Item> getItems() {
		return Collections.unmodifiableList( new ArrayList<Item>(this.itemsById.values()) );
	}

	/**
	 * Get the items held by a specific player
	 * 
	 * @return a map of with key: item, and value: player that holds references to the items the player is holding
	 */
	public Map<Item, Player> getItemsHeld() {
		final Map<Item, Player> acc = new HashMap<Item, Player>();

		for (final Item item : this.items) {
			final MUDObject obj = getById(item.getLocation());

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

		for (final Item item : this.items) {
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
	private final List<Thing> things = new ArrayList<Thing>();
	private final Map<Integer, Thing> thingsById = new HashMap<Integer, Thing>();

	public void addThing(final Thing thing) {
		this.things.add(thing);
		this.thingsById.put(thing.getDBRef(), thing);
	}

	public void removeThing(final Thing thing) {
		this.things.remove(thing);
		this.thingsById.remove(thing.getDBRef());
	}

	public Thing getThing(int dbref) {
		if( dbref < 0 ) return null;

		return this.thingsById.get(dbref);
	}

	public Thing getThing(final String name) {
		for (final Thing thing : this.things) {
			if ( thing.getName().equals(name) ) {
				return thing;
			}
		}

		return null;
	}

	public List<Thing> getThings() {
		return new ArrayList<Thing>(this.thingsById.values());
	}

	public List<Thing> getThingsForRoom(final Room room) {
		final List<Thing> acc = new LinkedList<Thing>();

		int roomId = room.getDBRef();

		for (final Thing t : this.things) {
			if (t.getLocation() == roomId) {
				acc.add(t);
			}
		}

		return acc;
	}

	public void placeThingsInRooms(final MUDServer parent) {
		for (final Thing thing : this.things) {
			if (thing != null) {
				final Room room = parent.getRoom(thing.getLocation());

				if (room != null) {
					room.addThing( thing );
				}
			}
		}
	}

	///////////////////////////// PLAYERS
	private final Map<Integer, Player> playersById = new HashMap<Integer, Player>();
	private final Map<String, Player> playersByName = new HashMap<String, Player>();

	public void addPlayer(final Player p) {
		this.playersById.put(p.getDBRef(), p);
		this.playersByName.put(p.getName(), p);
	}

	public Player getPlayer(final int dbref) {
		if( dbref < 0 ) return null;

		return this.playersById.get(dbref);

	}
	
	public Player getPlayer(final String name) {
		// TODO fix kludginess, used with/for cnames
		final Player player = this.playersByName.get(name);

		if( player == null ) {
			MUDObject object = getByName(name);

			if( object instanceof Player ) {
				//player = (Player) object;
				return (Player) object;
			}
		}

		return player;
	}

	public int getNumPlayers(final PClass c) {
		int count = 0;

		for (final Player p : this.playersByName.values()) {
			if ( c.equals(p.getPClass()) ) {
				count += 1;
			}
		}

		return count;
	}

	public List<Player> getPlayersByRoom(final Room room) {
		final List<Player> acc = new LinkedList<Player>();

		int loc = room.getDBRef();

		for (final Player p : this.playersByName.values()) {
			if (p.getLocation() == loc) {
				acc.add(p);
			}
		}

		return acc;
	}
	
	public List<Player> getPlayers() {
		return new ArrayList<Player>(this.playersById.values());
	}

	/**
	 * Erase the entire database
	 */
	public void clear() {
		this.things.clear();

		this.items.clear();

		this.creeps.clear();

		this.npcsById.clear();
		this.npcsByName.clear();

		this.playersByName.clear();

		this.exitsById.clear();
		this.exitsByName.clear();

		this.roomsById.clear();
		this.roomsByName.clear();

		this.objsById.clear();
		this.objsByName.clear();

		this.unusedDBNs.clear();
		this.reservedDBNs.clear();
	}
}