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
	
	public List<Exit> getExitsByRoom(final int loc);
	
	// Item
	public Item getItem(final int dbref);
	
	public List<Item> getItems();
	public Map<Item, Player> getItemsHeld();
	public List<Item> getItemsByLoc(final int loc);
	
	// Thing
	public Thing getThing(int dbref);
	public Thing getThing(final int roomId, final String name);
	
	public List<Thing> getThings();
	public List<Thing> getThingsForRoom(final int roomId);
	
	// Player
	public Player getPlayer(final String name);
	
	// NPC
	public NPC getNPC(final int dbref);
	public NPC getNPC(final String name);
	
	public List<NPC> getNPCs();
	public List<NPC> getNPCsByRoom(final int loc);
	
	// Creature
	public List<Creature> getCreatures();
	public List<Creature> getCreaturesByRoom(final int loc);
}