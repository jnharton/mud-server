package mud.interfaces;

import java.util.List;
import java.util.Map;

import mud.MUDObject;
import mud.objects.Creature;
import mud.objects.Exit;
import mud.objects.Item;
import mud.objects.NPC;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.Thing;

/**
 * This is kind of a weird interface/class that provides a mask of
 * ObjectDB where you can only retrieve objects. There is no functionality
 * in here to provide deletion or object renames.
 * 
 * @author Jeremy
 *
 */
public interface ODBI {
	// MUDObject
	public MUDObject get(final int n);
	public MUDObject getByName(final String name);
	
	public List<MUDObject> getByRoom(final Room room);
	
	// Room
	public Room getRoomById(final int id);
	public Room getRoomByName(final String name);
	
	// Exit
	public Exit getExit(final int dbref);
	public Exit getExit(final String name);
	
	public List<Exit> getExitsByRoom(final Room room);
	
	// Item
	public Item getItem(final int dbref);
	public Item getItem(final int roomId, final String name);
	
	public List<Item> getItems();
	public Map<Item, Player> getItemsHeld();
	public List<Item> getItemsByLoc(final int loc);
	
	// Thing
	public Thing getThing(int dbref);
	public Thing getThing(final int roomId, final String name);
	
	public List<Thing> getThings();
	public List<Thing> getThingsForRoom(final Room room);
	
	// Player
	public Player getPlayer(final String name);
	
	// NPC
	public NPC getNPC(final int dbref);
	public NPC getNPC(final String name);
	
	public List<NPC> getNPCs();
	public List<NPC> getNPCsByRoom(final Room room);
	
	// Creature
	public List<Creature> getCreatures();
	public List<Creature> getCreaturesByRoom(final Room room);
}