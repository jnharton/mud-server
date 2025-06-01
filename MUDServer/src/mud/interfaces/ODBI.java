package mud.interfaces;

import java.util.List;
import java.util.Map;

import mud.MUDObject;
import mud.TypeFlag;
import mud.objects.Creature;
import mud.objects.Exit;
import mud.objects.Item;
import mud.objects.NPC;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.RoomType;
import mud.objects.Thing;

/**
 * ObjectDB Interface
 * 
 * Provides an interface to the database that completely masks any
 * additional operation.
 * 
 * @author Jeremy
 *
 */
public interface ODBI {
	// General
	public int peekNextId();
	public List<Integer> getUnused();
	public int getSize();
	
	public List<MUDObject> findByLower(final String name);
	public int[] getFlagCounts(final String[] letters);
	
	public void addName(final MUDObject object, final String name);
	public boolean hasName(final String name);
	public boolean hasName(final String name, final TypeFlag flag);
	public void updateName(final MUDObject object, final String oldName);
	
	// MUDObject
	public MUDObject getById(final int n);
	public MUDObject getByName(final String name);
	
	// TODO this seems like it should be removed...
	public List<MUDObject> getByRoom(final Room room);
	public List<MUDObject> getObjects();
	
	public void add(final MUDObject object);
	public void addAsNew(final MUDObject object);
	public void remove(final MUDObject item);

	// Room
	public Room getRoomById(final int id);
	public Room getRoomByName(final String name);

	public List<Room> getRooms();
	public List<Room> getRoomsByLocation(final int loc);
	public List<Room> getRoomsByType(final RoomType type);

	// Exit
	public Exit getExit(final int dbref);
	public Exit getExit(final String name);

	public List<Exit> getExits();
	public List<Exit> getExitsByRoom(final Room room);

	// Creature
	public List<Creature> getCreatures();
	public List<Creature> getCreaturesByRoom(final Room room);

	// NPC
	public NPC getNPC(final int dbref);
	public NPC getNPC(final String name);

	public List<NPC> getNPCs();
	public List<NPC> getNPCsByLocation(final Integer location);
	public List<NPC> getNPCsByRoom(final Room room);

	// Item
	public Item getItem(final int dbref);
	public Item getItem(final String name);

	public List<Item> getItems();
	public Map<Item, Player> getItemsHeld();
	public List<Item> getItemsByLocation(final int loc);

	// Thing
	public Thing getThing(final int dbref);
	public Thing getThing(final String name);

	public List<Thing> getThings();
	public List<Thing> getThingsForRoom(final Room room);

	// Player
	public Player getPlayer(final int dbref);
	public Player getPlayer(final String name);
	
	public List<Player> getPlayers();
	public List<Player> getPlayersByRoom(final Room room);
}