package mud.modules;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mud.MUDObject;
import mud.game.Faction;
import mud.interfaces.GameModule;
import mud.interfaces.Ruleset;
import mud.misc.SlotType;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;
import mud.objects.Thing;
import mud.objects.ThingType;

public class BasicModule implements GameModule {
	private final String name;
	private final Ruleset rules;
	
	private List<Faction> factions;
	private Hashtable<String, ItemType> itemTypes;
	
	public BasicModule(final String moduleName, final Ruleset moduleRuleset) {
		this.name = moduleName;
		this.rules = moduleRuleset;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Integer getVersion() {
		return 0;
	}
	
	@Override
	public Ruleset getRuleset() {
		return this.rules;
	}
	
	@Override
	public boolean hasClasses() {
		return false;
	}
	
	@Override
	public void init() {
	}
	
	public void init2(final List<Faction> mFactions, Hashtable<String, ItemType> mItemTypes) {
		this.factions = mFactions;
		this.itemTypes = mItemTypes;
	}

	@Override
	public void PCInit(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Faction> getFactions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ItemType getItemType(final Integer typeId) {
		return null;
	}
	
	public ItemType getItemType(final String typeName) {
		return null;
	}
	
	@Override
	public SlotType getSlotType(final Integer typeId) {
		return null;
	}
	
	@Override
	public SlotType getSlotType(final String typeName) {
		return null;
	}
	
	@Override
	public ThingType getThingType(final Integer typeId) {
		return null;
	}
	
	@Override
	public ThingType getThingType(final String typeName) {
		return null;
	}
	
	@Override
	public Map<String, Item> getItemPrototypes() {
		return null;
	}

	@Override
	public Map<String, Thing> getThingPrototypes() {
		return null;
	}

	@Override
	public Item loadItem(final String itemData) {
		return null;
	}

	@Override
	public Thing loadThing(final String itemData) {
		return null;
	}

	@Override
	public void run() {
	}

	@Override
	public void op(String input, Player player) {
	}

	@Override
	public boolean use(Player p, MUDObject m) {
		return false;
	}
}