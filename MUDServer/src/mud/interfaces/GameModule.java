package mud.interfaces;

import java.util.List;
import java.util.Map;

import mud.misc.Faction;
import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Thing;

public interface GameModule {
	
	public String getName();	
	public Integer getVersion();
	
	public void init(); // initialize the module
	public void init_player(Player player);
	
	public List<Faction> getFactions();
	
	public Ruleset getRuleset();
	
	public Map<String, Item> getItemPrototypes();
	public Map<String, Thing> getThingPrototypes();
}