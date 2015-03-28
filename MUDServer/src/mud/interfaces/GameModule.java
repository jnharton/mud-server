package mud.interfaces;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mud.misc.Faction;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;
import mud.objects.Thing;

public interface GameModule {
	
	public String getName();
	public Integer getVersion();
	public Ruleset getRuleset();
	
	public void init();                     // initialize the module
	public void init_player(Player player); // initialize a new player for this game
	
	public void PCInit(Player player);
	
	public List<Faction> getFactions();
	
	public Hashtable<String, ItemType> getItemTypes();
	
	public Map<String, Item> getItemPrototypes();
	public Map<String, Thing> getThingPrototypes();
	
	public Item loadItem(final String itemData);
}