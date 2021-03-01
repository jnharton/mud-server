package mud.modules;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mud.MUDObject;
import mud.game.Faction;
import mud.interfaces.Ruleset;
import mud.misc.SlotType;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;
import mud.objects.Thing;
import mud.objects.ThingType;

public abstract class GameModule {
	private static final ItemType NOTHING1 = new ItemType("Nothing", -1);
	private static final SlotType NOTHING2 = new SlotType("Nothing", -1);
	private static final ThingType NOTHING3 = new ThingType("Nothing", -1);
	
	
	public abstract String getName();
	public abstract String getShortName();
	public abstract Integer getVersion();
	
	public abstract Ruleset getRuleset();
	
	public boolean hasClasses() {
		return false;
	}
	
	public List<Faction> getFactions() {
		return Collections.emptyList();
	}
	
	public abstract void init();                      // initialize the module
	public abstract void init(final String dataDir);  // initialize the module
	public abstract void init2(final List<Faction> mFactions, Hashtable<String, ItemType> mItemTypes);
	public abstract void PCInit(final Player player); // initialize a new player for this game
	
	public ItemType getItemType(final Integer typeId) {
		return NOTHING1;
	}
	
	public ItemType getItemType(final String typeName) {
		return NOTHING1;
	}
	
	public SlotType getSlotType(final Integer typeId) {
		return NOTHING2;
	}
	
	public SlotType getSlotType(final String typeName) {
		return NOTHING2;
	}
	
	public ThingType getThingType(final Integer typeId) {
		return NOTHING3;
	}
	
	public ThingType getThingType(final String typeName) {
		return NOTHING3;
	}
	
	public Map<String, Item> getItemPrototypes() {
		return Collections.emptyMap();
	}
	public Map<String, Thing> getThingPrototypes() {
		return Collections.emptyMap();
	}
	
	public abstract Item loadItem(final String itemData);
	public abstract Thing loadThing(final String itemData);
	
	public abstract void run();                                       // stuff that the module wants to happen in the main loop...
	public abstract void op(final String input, final Player player); // status bound handling
	public abstract boolean use(final Player p, final MUDObject m);
	
	public abstract void test();                                      // do some test setup
	
	public abstract void levelup(final Player player);
}