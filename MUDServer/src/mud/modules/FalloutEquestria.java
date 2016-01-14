package mud.modules;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mud.Command;
import mud.foe.*;
import mud.foe.items.*;
import mud.game.Faction;
import mud.game.Race;
import mud.interfaces.ExtraCommands;
import mud.interfaces.GameModule;
import mud.misc.Currency;
import mud.misc.Slot;
import mud.misc.SlotType;
import mud.misc.SlotTypes;
import mud.misc.TriggerType;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;
import mud.objects.Player;
import mud.objects.Thing;
import mud.objects.items.Weapon;
import mud.rulesets.foe.FOESpecial;
import mud.rulesets.special.SpecialRuleset;
import mud.utils.Utils;

/* this class is marked final because there shouldn't ever be a subclass */
public final class FalloutEquestria implements GameModule, ExtraCommands {
	/* the class members are static because this is a class where
	 * there is only supposed to be a single instance, ever.	
	 */
	private static Map<String, Command> commands;
	private static Map<String, Item> prototypes;
	private static Map<String, Thing> prototypes1;

	private static List<Faction> factions;
	
	private static Hashtable<String, ItemType> itemTypes;
	private static Hashtable<String, SlotType> slotTypes;
	
	public static final Currency BOTTLE_CAPS = new Currency("bottle cap", "bc", null, 1.0);

	// static block initializing the above stuff
	{
		// Factions
		// TODO I'd like to make this data loaded from a file and not a static block
		factions = new LinkedList<Faction>();

		factions.add( new Faction("Dashites") );
		factions.add( new Faction("Grand Pegasus Enclave") );
		factions.add( new Faction("Raiders") );
		factions.add( new Faction("Steel Rangers") );
		factions.add( new Faction("Talons") );
		factions.add( new Faction("Wastelanders") );
		factions.add( new Faction("Stable Dwellers") );

		// Item Types
		itemTypes = new Hashtable<String, ItemType>();
		
		// Slot Types
		slotTypes = new Hashtable<String, SlotType>();
	}
	
	public FalloutEquestria() {
		//init();
	}

	@Override
	public String getName() {
		return "Fallout Equestria";
	}

	@Override
	public Integer getVersion() {
		return 0;
	}

	@Override
	public SpecialRuleset getRuleset() {
		return FOESpecial.getInstance();
	}
	
	@Override
	public boolean hasClasses() {
		return false;
	}

	@Override
	public void init() {
		prototypes  = new Hashtable<String, Item>();
		prototypes1 = new Hashtable<String, Thing>();
		
		/* Item Prototypes */
		
		// prototype - pipbuck
		
		// PipBuck (foe version of PipBoy from Bethesda's Fallout video games)
		PipBuck pipbuck = new mud.foe.items.PipBuck("PipBuck 3000");
		
		pipbuck.setLocation(-1);
		
		prototypes.put("mud.foe.pipbuck", pipbuck);
		
		// prototype - memory orb
		mud.foe.items.MemoryOrb MemoryOrb = new mud.foe.items.MemoryOrb();

		MemoryOrb.setName("Memory Orb");
		MemoryOrb.setDesc(
				"A sphere of some crystalline substance. Beneath it's surface a misty "
						+ "substance swirls in ever-changing patterns. It emits gently pulsing "
						+ "light which shifts through the whole spectrum of visible colors.");
		MemoryOrb.setLocation(-1);
		MemoryOrb.setEquippable(false);
		MemoryOrb.setEquipped(false);

		prototypes.put("mud.foe.memory_orb", MemoryOrb);

		// prototype - Sparkle Cola soda
		Item SparkleCola = new Item(-1);

		SparkleCola.setName("Sparkle Cola");
		SparkleCola.setDesc(
				"A bottle of ancient, lukewarm Sparkle Cola. Probably just as good as it ever was.\n\n" +
				"Your Choice.\nThe Best in Equestria.\nSparkle Cola\nSoar into the sky."); 
		SparkleCola.setLocation(-1);
		SparkleCola.setDrinkable(true);
		SparkleCola.setEquipType(ItemTypes.NONE);
		SparkleCola.setEquippable(false);
		SparkleCola.setEquipped(false);

		SparkleCola.setScriptOnTrigger(TriggerType.onUse,
				"{do:{give:{&player},{create_item:mud.foe.bottlecap_sc}},{tell:You toss the bottle, keeping just the bottlecap.,{&player}}}"
				);

		System.out.println("SparkleCola(onUse Script): " + SparkleCola.getScript(TriggerType.onUse).getText());

		prototypes.put("mud.foe.sparkle_cola", SparkleCola);

		// prototype - bottle cap
		BottleCap BottleCap = new BottleCap();

		BottleCap.setName("Bottle Cap");
		BottleCap.setDesc("A slightly bent metal bottle cap. Once a common waste product of the drinking habits of the Equestrian nation,"
				+ "now a valuable currency in the Equestrian Wasteland");
		BottleCap.setLocation(-1);
		
		BottleCap.setEquipType(ItemTypes.NONE);
		
		BottleCap.setEquippable(false);
		BottleCap.setEquipped(false);

		BottleCap.setProperty("type", "sc"); // Sparkle Cola bottle cap
		BottleCap.setProperty("value", 1);

		prototypes.put("mud.foe.bottlecap_sc", BottleCap);
		
		/* Weapons */
		
		// prototype - pistol
		final Weapon pgun = new Weapon("10mm Pistol", "A basic pistol, developed for the security forces of what used to be Equestria.");

		prototypes.put("mud.foe.weapons.pistol", pgun);
		
		// prototype - laser rifle
		final Weapon laser_rifle = new Weapon("Laser Rifle", "An energy weapon modeled after a basic rifle.");
		
		laser_rifle.setWeight(0.0);
		
		// TODO need a better slottype management, since another species could hold it
		laser_rifle.setSlotType(FOESlotTypes.HOOVES);
		
		laser_rifle.damage = 5;

		laser_rifle.setProperty("damage",  10);
		laser_rifle.setProperty("ammo_type", "energy");
		
		prototypes.put("mud.foe.laser_rifle", laser_rifle);
		
		// prototype - wing blades
		final Weapon wing_blades = new Weapon("Wing Blades", "Sharp, curved metal blades designed to be strapped onto a pegasus' wings.");
		
		wing_blades.setWeight(0.0);
		
		wing_blades.setSlotType(FOESlotTypes.WINGS);
		
		prototypes.put("mud.foe.wing_blades", wing_blades);

		/* Thing Prototypes */

		// prototype - Spark Generator
		Thing spark_generator = new Thing("Spark Generator", "This advanced piece of magitech produces near limitless electric power via magic");

		Thing SparkGenerator = new Thing(-1);

		SparkGenerator.setName("Spark Generator");
		SparkGenerator.setDesc("This advanced piece of magitech produces near limitless electric power via magic");
		SparkGenerator.setLocation(-1);

		spark_generator.setProperty("thingtype", "spark_generator");
		spark_generator.setProperty("power", 10);

		prototypes1.put("mud.foe.spark_generator", SparkGenerator);

		// prototype - terminal
		mud.foe.Terminal terminal = new mud.foe.Terminal(
				"Terminal",
				"A Stable-Tec terminal, old pre-war technology whose durability is plain to see. On the screen, passively glowing green text indicates that it awaits input.",
				mud.foe.Terminal.Power.POWER_ON, null, null);

		prototypes1.put("mud.foe.terminal", terminal);

		// TODO do I want name indexing or id indexing?
		for(final ItemType it : FOEItemTypes.getItemTypes()) {
			itemTypes.put( it.getName(), it ); 
		}
		
		for(final SlotType st : FOESlotTypes.getSlotTypes()) {
			slotTypes.put( st.getName(), st );
		}
	}

	/**
	 * PCInit
	 * 
	 * handle basic player initialization for all the game specific stuff
	 * 
	 * This will be called once on any new player created when this game module is
	 * being used. Otherwise it is assumed that these things will have been saved
	 * and restored on server loading for existing things
	 * 
	 * @author Jeremy
	 */
	public void PCInit(Player player) {
		// check to see if Player has had this init done before and for what ruleset

		// if it's never been done, do it here
		final SpecialRuleset rules = FOESpecial.getInstance();

		//Race EARTH = new Race(rules, "Earth", 0, false);
		Race PEGASUS = new Race(rules, "Pegasus", 1, false);
		Race UNICORN = new Race(rules, "Unicorn", 2, false);
		
		//Race GRIFFIN;
		//Race ZEBRA;
		
		// goggles, glasses? (FOESlotTypes.HEAD, ItemTypes.NONE)
		
		player.addSlot("helmet",        new Slot(FOESlotTypes.HEAD, ItemTypes.HELMET));
		player.addSlot("clothes_head",  new Slot(FOESlotTypes.HEAD, ItemTypes.CLOTHING));
		player.addSlot("eyes",          new Slot(FOESlotTypes.EYES, ItemTypes.NONE));

		if( player.getRace().equals( UNICORN ) ) {
			player.addSlot("horn",      new Slot(FOESlotTypes.HORN, ItemTypes.NONE));
		}

		player.addSlot("clothes_body",  new Slot(FOESlotTypes.BODY, ItemTypes.CLOTHING));
		player.addSlot("barding",       new Slot(FOESlotTypes.BODY, ItemTypes.CLOTHING));

		player.addSlot("battle saddle", new Slot(FOESlotTypes.BACK, ItemTypes.CLOTHING));
		player.addSlot("saddlebags",    new Slot(FOESlotTypes.BACK, ItemTypes.CLOTHING));

		if( player.getRace().equals( PEGASUS ) ) {
			player.addSlot("wings",     new Slot(FOESlotTypes.WINGS, ItemTypes.WEAPON));
		}
		
		player.addSlot("anklets",       new Slot(FOESlotTypes.HOOVES, ItemTypes.ARMOR));
		player.addSlot("boots",         new Slot(FOESlotTypes.HOOVES, ItemTypes.ARMOR));
		player.addSlot("armor",         new Slot(FOESlotTypes.HOOVES, ItemTypes.ARMOR));
		player.addSlot("hooves",        new Slot(FOESlotTypes.HOOVES, ItemTypes.WEAPON));
		player.addSlot("weapon2",       new Slot(FOESlotTypes.HOOVES, ItemTypes.WEAPON));
		
		player.addSlot("special",       new Slot(FOESlotTypes.LFHOOF, FOEItemTypes.PIPBUCK));
		player.addSlot("special2",      new Slot(FOESlotTypes.RFHOOF, FOEItemTypes.PIPBUCK));
		
		// initialize faction reputation
		for(final Faction faction : getFactions()) {
			player.setReputation(faction, 0);
		}
	}

	public List<Faction> getFactions() {
		return FalloutEquestria.factions;
	}

	@Override
	public Map<String, Item> getItemPrototypes() {
		return this.prototypes;
	}

	@Override
	public Map<String, Thing> getThingPrototypes() {
		return this.prototypes1;
	}

	@Override
	public Map<String, Command> getCommands() {
		return this.commands;
	}

	// Use Methods
	public String use_terminal(final Terminal term, final Player player) {
		return "";
	}

	public Terminal createTerminal() {
		return null;
	}

	@Override
	public Hashtable<String, ItemType> getItemTypes() {
		return itemTypes;
	}
	
	@Override
	public ItemType getItemType(final Integer typeId) {
		return FOEItemTypes.getType(typeId);
	}
	
	public Hashtable<String, SlotType> getSlotTypes() {
		return slotTypes;
	}
	
	@Override
	public SlotType getSlotType(final Integer typeId) {
		return FOESlotTypes.getType(typeId);
	}
	
	public Item loadItem(final String itemData) {
		String[] attr = itemData.split("#");

		Integer oDBRef = Integer.parseInt(attr[0]);
		String oName = attr[1];
		Character oTypeFlag = attr[2].charAt(0);
		String oFlags = attr[2].substring(1, attr[2].length());
		String oDesc = attr[3];
		Integer oLocation = Integer.parseInt(attr[4]);

		// TODO equipType, slotType are new, work them in (3-17-2015)
		int itemType  = Utils.toInt(attr[5], 0); // get the type of item it should be
		int equipType = Utils.toInt(attr[6], 0);
		int slotType  = Utils.toInt(attr[7], 0);

		// TODO fix kludge?
		final ItemType it = FOEItemTypes.getType(itemType);

		if( it == FOEItemTypes.BOTTLE_CAP ) {
			final BottleCap bc = new BottleCap( oDBRef );
			
			bc.setLocation(oLocation);
			
			//return new BottleCap( oDBRef );
			return bc;
		}
		else if( it == FOEItemTypes.DISRUPTOR ) {
			final Disruptor disruptor = new Disruptor( oDBRef );
			
			disruptor.setLocation(oLocation);
			
			// currently the constructor doesn't take any parameters
			//return new Disruptor( oDBRef );
			
			return disruptor;
		}
		else if( it == FOEItemTypes.MEMORY_ORB) {
			// currently the constructor doesn't take any parameters
			final MemoryOrb mo = new MemoryOrb( oDBRef );
			
			mo.setName( oName );
			mo.setDesc( oDesc );
			
			mo.setLocation(oLocation);

			return mo;
		}
		else if( it == FOEItemTypes.PIPBUCK ) {
			final PipBuck pipbuck = new PipBuck( oDBRef, oName );
			
			pipbuck.setLocation(oLocation);
			
			// currently the constructor doesn't take any parameters
			//return new PipBuck( oDBRef, oName );
			
			return pipbuck;
		}
		else if( it == FOEItemTypes.STEALTH_BUCK ) {
			final StealthBuck stealthbuck = new StealthBuck( oDBRef );
			
			stealthbuck.setLocation(oLocation);
			// currently the constructor doesn't take any parameters
			//return new StealthBuck( oDBRef );
			
			return stealthbuck;
		}
		// Terminal is a Thing
		/*else if( it == FOEItemTypes.TERMINAL ) {
			return new Terminal( oName, oDesc );
		}*/
		else {
			// TODO resolve issue of loading item types not listed in here
			// (3-20-2015)
			final Item item = new Item(oDBRef, oName, null, oDesc, oLocation);

			/*
			 * objectDB.add(item); objectDB.addItem(item);
			 */

			return item;
		}
	}
}