package mud.interfaces;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mud.MUDObject;
import mud.game.Faction;
import mud.misc.SlotType;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;
import mud.objects.Thing;
import mud.objects.ThingType;

public interface GameModule {
	public String getName();
	public String getShortName();
	public Integer getVersion();
	
	public Ruleset getRuleset();
	
	public boolean hasClasses();
	
	public List<Faction> getFactions();
	
	public void init();                      // initialize the module
	public void init(final String dataDir);  // initialize the module
	public void init2(final List<Faction> mFactions, Hashtable<String, ItemType> mItemTypes);
	public void PCInit(final Player player); // initialize a new player for this game
	
	public ItemType getItemType(final Integer typeId);
	public ItemType getItemType(final String typeName);
	
	public SlotType getSlotType(final Integer typeId);
	public SlotType getSlotType(final String typeName);
	
	public ThingType getThingType(final Integer typeId);
	public ThingType getThingType(final String typeName);
	
	public Map<String, Item> getItemPrototypes();
	public Map<String, Thing> getThingPrototypes();
	
	public Item loadItem(final String itemData);
	public Thing loadThing(final String itemData);
	
	public void run();                                       // stuff that the module wants to happen in the main loop...
	public void op(final String input, final Player player); // status bound handling
	public boolean use(final Player p, final MUDObject m);
	
	public void test();                                      // do some test setup
	
	public void levelup(final Player player);
}