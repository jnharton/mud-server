package mud.modules;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mud.Command;
import mud.game.Faction;
import mud.interfaces.GameModule;
import mud.interfaces.Ruleset;
import mud.misc.Currency;
import mud.misc.Slot;
import mud.misc.SlotType;
import mud.misc.SlotTypes;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;
import mud.objects.Player;
import mud.objects.Thing;
import mud.objects.items.ClothingType;
import mud.rulesets.d20.D20;

public class DND35 implements GameModule {
	private static Map<String, Command> commands;
	private static Map<String, Item> prototypes;
	private static Map<String, Thing> prototypes1;

	private static List<Faction> factions;
	
	public static final Currency COPPER = new Currency("Copper", "cp", null, 1.0);
	public static final Currency SILVER = new Currency("Silver", "sp", COPPER, 100.0);
	public static final Currency GOLD = new Currency("Gold", "gp", SILVER, 100.0);
	public static final Currency PLATINUM = new Currency("Platinum", "pp", GOLD, 100.0);
	
	@Override
	public String getName() {
		return "Dungeons & Dragons 3.5e";
	}

	@Override
	public Integer getVersion() {
		return 0;
	}
	
	@Override
	public boolean hasClasses() {
		return true;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init_player(Player player) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void PCInit(Player player) {
		// add/initialize slots
		player.addSlot("helmet",   new Slot( SlotTypes.HEAD,   ItemTypes.HELMET));
		player.addSlot("necklace", new Slot( SlotTypes.NECK,   ItemTypes.NECKLACE));
		player.addSlot("armor",    new Slot( SlotTypes.CHEST,  ItemTypes.ARMOR));
		player.addSlot("cloak",    new Slot( SlotTypes.BACK,   ClothingType.CLOAK));
		player.addSlot("ring1",    new Slot( SlotTypes.FINGER, ItemTypes.RING));
		player.addSlot("ring2",    new Slot( SlotTypes.FINGER, ItemTypes.RING));
		player.addSlot("ring3",    new Slot( SlotTypes.FINGER, ItemTypes.RING));
		player.addSlot("ring4",    new Slot( SlotTypes.FINGER, ItemTypes.RING));
		player.addSlot("ring5",    new Slot( SlotTypes.FINGER, ItemTypes.RING));
		player.addSlot("ring6",    new Slot( SlotTypes.FINGER, ItemTypes.RING));
		player.addSlot("gloves",   new Slot( SlotTypes.HANDS,  ClothingType.GLOVES));
		player.addSlot("weapon",   new Slot( SlotTypes.RHAND,  ItemTypes.WEAPON));
		player.addSlot("weapon1",  new Slot( SlotTypes.LHAND,  ItemTypes.WEAPON));
		player.addSlot("weapon2",  new Slot( SlotTypes.HANDS,  ItemTypes.WEAPON));
		player.addSlot("belt",     new Slot( SlotTypes.WAIST,  ClothingType.BELT));
		player.addSlot("boots",    new Slot( SlotTypes.FEET,   ClothingType.BOOTS));
		player.addSlot("other",    new Slot( SlotTypes.NONE,   ItemTypes.NONE ));
	}

	@Override
	public List<Faction> getFactions() {
		// TODO write up a list of factions I can use
		return null;
	}

	@Override
	public Ruleset getRuleset() {
		return D20.getInstance();
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
		// TODO change this? we are currently just pulling the default slottypes
		return SlotTypes.getType(typeId);
	}
}