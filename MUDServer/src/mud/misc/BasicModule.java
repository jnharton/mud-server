package mud.misc;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mud.game.Faction;
import mud.interfaces.GameModule;
import mud.interfaces.Ruleset;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;
import mud.objects.Thing;

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
	public void init_player(Player player) {
		// TODO Auto-generated method stub

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
	public Hashtable<String, ItemType> getItemTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Item> getItemPrototypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Thing> getThingPrototypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item loadItem(String itemData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Hashtable<String, SlotType> getSlotTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemType getItemType(Integer typeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SlotType getSlotType(Integer typeId) {
		// TODO Auto-generated method stub
		return null;
	}
}