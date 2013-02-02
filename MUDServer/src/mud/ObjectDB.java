package mud;

import java.util.*;

import mud.MUDObject;
import mud.objects.*;
import mud.utils.Utils;
import mud.net.Client;

public class ObjectDB {

    private static int nextId = 0;

    // TreeMap allows instant retrieval by id and by name.
    final private TreeMap<String, MUDObject>  objsByName = new TreeMap<String, MUDObject>();
    final private TreeMap<Integer, MUDObject> objsById = new TreeMap<Integer, MUDObject>();

    /** As long as we construct objects and insert them into this db afterwards as a separate step, getting the next id is somewhat of a hack.
     *  We could easily construct objects with an invalid id or never add them to this db.
     */
    public int peekNextId() {
        return nextId;
    }

    public int getSize() {
        return objsByName.size();
    }

    public void addAsNew(final MUDObject item) {
        item.setDBRef(nextId++);
        add(item);
    }

    public void add(final MUDObject item) {
        objsByName.put(item.getName(), item);
        objsById.put(item.getDBRef(), item);
    }

    public void allocate(final int n) {
        // Until we travel back to 1970 and convert this codebase into C, we probably don't need to pre-allocate things.
        // Unless users are creating rooms faster that the JVM can allocate objects...
    }

    // Send all objects
    public void dump(final Client client, final MUDServer server) {
        int i = 0;
        for (final MUDObject obj : objsByName.values()) {
            server.send(String.format("%s: %s (#%s)", i, obj.getName(), obj.getDBRef()), client);
            i += 1;
        }
    }

    // Ensure object is in both maps, overwriting any object in the id map.
    public void set(final int n, final MUDObject item) {
        objsById.put(n, item);
        objsByName.put(item.getName(), item);
    }

    public boolean hasName(final String name) {
        return objsByName.containsKey(name);
    }

    public MUDObject getByName(final String name) {
        return objsByName.get(name);
    }

    public MUDObject get(final int n) {
        return objsById.get(n);
    }

    public List<MUDObject> findByLower(final String name) {
        final LinkedList<MUDObject> acc = new LinkedList<MUDObject>();
        for (final MUDObject obj : objsByName.values()) {
            if (obj.getName().toLowerCase().contains(name.toLowerCase())) {
                acc.add(obj);
            }
        }
        return acc;
    }

    public int[] getFlagCounts(final String[] letters) {
        final int[] counts = new int[letters.length];
        Arrays.fill(counts, 0);

        for (final MUDObject obj : objsByName.values()) {
            final String flags = obj.getFlags();
            for (int i = 0; i < letters.length; i++) {
                if (flags.contains(letters[i])) {
                    counts[i] += 1;
                }
            }
        }
        return counts;
    }

    // Serialize all objects via `toDB` and save array to file.
    public void save(final String filename) {
		final String[] toSave = new String[objsByName.size()];
		int index = 0;
        for (final MUDObject obj : objsByName.values()) {
            toSave[index] = obj.toDB();
        }
		Utils.saveStrings(filename, toSave);
    }

    ///////////// THINGS
    final private ArrayList<Thing> things = new ArrayList<Thing>();

    public List<Thing> getThingsForRoom(final int roomId) {
        final List<Thing> acc = new LinkedList<Thing>();
		for (final Thing t : things) {
			if (t.getLocation() == roomId) {
                acc.add(t);
			}
		}
        return acc;
    }

    public Thing getThing(final int roomId, final String name) {
		for (final Thing t : things) {
			if (t.getLocation() == roomId && t.getName().equals(name))
            {
                return t;
			}
		}
        return null;
    }

    public void addThing(final Thing t) {
        things.add(t);
    }

    public void removeThing(final Thing t) {
        things.remove(t);
    }

	public void placeThingsInRooms(final MUDServer parent) {
		for (final Thing t : things) {
			if (t != null) {
                final Room room = parent.getRoom(t.getLocation());
                if (room != null) {
                    room.contents.add(t);
                }
			}
		}
	}

    ////////////// ROOMS
    final private Map<Integer, Room> roomsById   = new HashMap<Integer, Room>();
    final private Map<String, Room>  roomsByName = new HashMap<String, Room>();

    // must add room to both maps
    public void addRoom(final Room r) {
        roomsByName.put(r.getName(), r);
        roomsById.put(r.getDBRef(), r);
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

    public List<Room> getRoomsByType(final String type) {
        final List<Room> acc = new LinkedList<Room>();
        for (final Room r : roomsById.values()) {
            if (r.getRoomType().equals(type)) {
                acc.add(r);
            }
        }
        return acc;
    }

    public List<Room> getWeatherRooms() {
        return getRoomsByType("O");
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
    final Map<Integer, Exit> exitsById   = new HashMap<Integer, Exit>();
    final Map<String, Exit>  exitsByName = new HashMap<String, Exit>();

    public Exit getExit(final int dbref) {
        return exitsById.get(dbref);
    }

    public Exit getExit(final String name) {
        return exitsByName.get(name);
    }

    public void addExit(final Exit e) {
        exitsByName.put(e.getName(), e);
        exitsById.put(e.getDBRef(), e);
    }

    public void removeExit(final Exit e) {
        exitsByName.values().remove(e);
        exitsById.values().remove(e);
    }

	/**
	 * Go through all the exits that exist in the database and
	 * place/attach them in/to the respective rooms they are part of
	 */
	public void loadExits(final MUDServer parent) {
		for (final Exit e : exitsById.values()) {
            final Room room = getRoomById(e.getLocation());
            if (room != null) {
                room.getExits().add(e);
                parent.debug("Exit Loaded", 2);
                parent.debug(room.getDBRef() + " " + e.getDBRef(), 2);
            }
		}
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

    public List<Creature> getCreatureByRoom(final int loc) {
        final List<Creature> acc = new LinkedList<Creature>();
        for (final Creature c : creeps) {
            if (c.getLocation() == loc) {
                acc.add(c);
            }
        }
        return acc;
    }

    ////////////////// NPCs
    final Map<Integer, NPC> npcsById   = new HashMap<Integer, NPC>();
    final Map<String, NPC>  npcsByName = new HashMap<String, NPC>();

    public void addNPC(final NPC npc) {
        npcsById.put(npc.getDBRef(), npc);
        npcsByName.put(npc.getName(), npc);
        npcsByName.put(npc.getCName(), npc);
    }

    public NPC getNPC(final String name) {
        return npcsByName.get(name);
    }

    public NPC getNPC(final int dbref) {
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
    private ArrayList<Item> items = new ArrayList<Item>();

    public void addItem(final Item i) {
        items.add(i);
    }

    public void addItemsToRooms() {
		for (final Item item : items) {
			final Room r = getRoomById(item.getLocation());
            if (r != null) {
                r.contents1.add(item);
            }
        }
    }
    
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

    public List<Item> getItemsByLoc(final int loc) {
        final List<Item> acc = new LinkedList<Item>();
		for (final Item item : items) {
            if (item.getLocation() == loc) {
                acc.add(item);
            }
        }
        return acc;
    }

}
