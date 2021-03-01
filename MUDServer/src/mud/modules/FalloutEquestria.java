package mud.modules;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mud.Command;
import mud.MUDObject;
import mud.MUDServer;
import mud.ObjectFlag;
import mud.TypeFlag;
import mud.foe.*;
import mud.foe.items.*;
import mud.game.Faction;
import mud.game.Race;
import mud.interfaces.ExtraCommands;
import mud.misc.Currency;
import mud.misc.Script;
import mud.misc.Slot;
import mud.misc.SlotType;
import mud.misc.TriggerType;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;
import mud.objects.Player;
import mud.objects.Thing;
import mud.objects.ThingType;
import mud.objects.items.Drink;
import mud.objects.items.Weapon;
import mud.objects.items.Weapon.DamageType;
import mud.rulesets.foe.FOESpecial;
import mud.rulesets.foe.Races;
import mud.rulesets.special.SpecialRuleset;
import mud.utils.Utils;

/*
 * a module to implement some Fallout Equestria (original fanfiction by Kkat) stuff
 */

/* this class is marked final because there shouldn't ever be a subclass */
public final class FalloutEquestria extends GameModule implements ExtraCommands {
	private String DATA_DIR;
	
	/* the class members are static because this is a class where
	 * there is only supposed to be a single instance, ever.	
	 */
	
	// class
	private static Map<String, Command> commands;
	private static Map<String, Item> prototypes;
	private static Map<String, Thing> prototypes1;

	private static List<Faction> factions;
	
	public static Faction DASHITES;
	
	public static final Currency EQUESTRIAN_BIT = new Currency("Bit", "eb", null, 1.0);
	public static final Currency BOTTLE_CAPS = new Currency("bottle cap", "bc", null, 1.0);
	
	// instance
	private Map<Player, mud.foe.Terminal> terminals = new HashMap<Player, mud.foe.Terminal>(1, 0.75f);
	
	public FalloutEquestria() {
		//init();
	}

	@Override
	public String getName() {
		return "Fallout Equestria";
	}
	
	@Override
	public String getShortName() {
		return "FOE";
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
	}

	@Override
	public void init(final String dataDir) {
		this.DATA_DIR = dataDir;
		
		/* factions */
		factions = new LinkedList<Faction>() {{
			add( new Faction("Dashites") );
			add( new Faction("Grand Pegasus Enclave") );
			add( new Faction("Raiders") );
			add( new Faction("Steel Rangers") );
			add( new Faction("Steel Rangers") );
			add( new Faction("Talons") );
			add( new Faction("Wastelanders") );
			add( new Faction("Stable Dwellers") );
		}};
		
		/* Items */
		prototypes  = new Hashtable<String, Item>();
		
		/************************************************************/
		/** PipBuck                                                **/
		/************************************************************/
		
		// PipBuck (foe version of PipBoy from Bethesda's Fallout video games)
		PipBuck pipbuck = new mud.foe.items.PipBuck("PipBuck 3000");
		
		//pipbuck.setLocation(-1); // TODO this should be unnecessary
		
		prototypes.put("mud.foe.pipbuck", pipbuck);
		
		/************************************************************/
		/** Memory Orb                                             **/
		/************************************************************/
		mud.foe.items.MemoryOrb MemoryOrb = new mud.foe.items.MemoryOrb();

		MemoryOrb.setName("Memory Orb");
		MemoryOrb.setDesc(
				"A sphere of some crystalline substance. Beneath it's surface a misty "
						+ "substance swirls in ever-changing patterns. It emits gently pulsing "
						+ "light which shifts through the whole spectrum of visible colors.");
		MemoryOrb.setLocation(-1);

		prototypes.put("mud.foe.memory_orb", MemoryOrb);
		
		/************************************************************/
		/** Sparkle Cola soda                                      **/
		/************************************************************/
		Drink SparkleCola = new Drink(-1); // item - drinkable

		SparkleCola.setName("Sparkle Cola");
		SparkleCola.setDesc(
				"A bottle of ancient, lukewarm Sparkle Cola. Probably just as good as it ever was.\n\n" +
				"Your Choice.\nThe Best in Equestria.\nSparkle Cola\nSoar into the sky."); 
		SparkleCola.setLocation(-1);

		SparkleCola.setScriptOnTrigger(TriggerType.onUse,
				"{do:{give:{&player},{create_item:mud.foe.bottlecap_sc}},{tell:You toss the bottle#c keeping just the bottlecap.,{&player}}}"
				);
		
		SparkleCola.setProperty("stackable", true);

		System.out.println("SparkleCola(onUse Script): " + SparkleCola.getScript(TriggerType.onUse).getText());

		prototypes.put("mud.foe.sparkle_cola", SparkleCola);
		
		/************************************************************/
		/** Sunrise Sarsaparilla soda                              **/
		/************************************************************/
		Drink SunriseSarsaparilla = new Drink(-1);
		
		SunriseSarsaparilla.setName("Sunrise Sarsaparilla");
		SunriseSarsaparilla.setDesc("");
		SunriseSarsaparilla.setLocation(-1);
		
		SunriseSarsaparilla.setScriptOnTrigger(TriggerType.onUse,
				"{do:{give:{&player},{create_item:mud.foe.bottlecap_ss}},{tell:You toss the bottle#c keeping just the bottlecap.,{&player}}}"
				);
		
		SunriseSarsaparilla.setProperty("stackable", true);
		
		prototypes.put("mud.foe.sunrise_sarsaparilla", SunriseSarsaparilla);
		
		/************************************************************/
		/** Bottle Cap (SC)                                        **/
		/************************************************************/
		BottleCap BottleCap = new BottleCap();

		BottleCap.setName("Bottle Cap");
		BottleCap.setDesc("A slightly bent metal bottle cap. Once a common waste product of the drinking habits of the Equestrian nation, "
				+ "now a valuable currency in the Equestrian Wasteland");
		BottleCap.setLocation(-1);
		
		BottleCap.setProperty("type", "sc"); // Sparkle Cola bottle cap
		BottleCap.setProperty("value", 1);
		BottleCap.setProperty("stackable", true);

		prototypes.put("mud.foe.bottlecap_sc", BottleCap);
		
		/************************************************************/
		/** Bottle Cap (SS)                                        **/
		/************************************************************/
		BottleCap BottleCap1 = new BottleCap();

		BottleCap1.setName("Bottle Cap");
		BottleCap1.setDesc("A slightly bent metal bottle cap. Once a common waste product of the drinking habits of the Equestrian nation, "
				+ "now a valuable currency in the Equestrian Wasteland");
		BottleCap1.setLocation(-1);
		
		BottleCap1.setProperty("type", "ss"); // Sparkle Cola bottle cap
		BottleCap1.setProperty("value", 1);
		BottleCap1.setProperty("stackable", true);

		prototypes.put("mud.foe.bottlecap_ss", BottleCap);
		
		/* Weapons */
		
		/************************************************************/
		/** Pistol                                                 **/
		/************************************************************/
		final Weapon pgun = new Weapon(
				"10mm Pistol",
				"A basic pistol, developed for the security forces of what used to be Equestria.");
		
		pgun.setSlotType(FOESlotTypes.HOOVES);
		
		pgun.setWeight(2.0);
		
		pgun.setDamageType(DamageType.PIERCING);		
		pgun.setDamage(5);

		prototypes.put("mud.foe.weapons.pistol", pgun);
		
		/************************************************************/
		/** Laser Rifle                                            **/
		/************************************************************/
		final Weapon laser_rifle = new Weapon(
				"Laser Rifle",
				"An energy weapon modeled after a basic rifle.");
		
		laser_rifle.setSlotType(FOESlotTypes.HOOVES);
		
		laser_rifle.setWeight(8.0);
		
		laser_rifle.setDamageType(DamageType.ENERGY);
		laser_rifle.setDamage(10);
		
		prototypes.put("mud.foe.laser_rifle", laser_rifle);
		
		/************************************************************/
		/** Wing Blades                                            **/
		/************************************************************/
		final Weapon wing_blades = new Weapon(
				"Wing Blades",
				"Sharp, curved metal blades designed to be strapped onto a pegasus' wings.");
		
		wing_blades.setSlotType(FOESlotTypes.WINGS);
		
		wing_blades.setWeight(6.0);
		
		wing_blades.setDamageType(DamageType.SLASHING);
		wing_blades.setDamage(5);
		
		prototypes.put("mud.foe.wing_blades", wing_blades);

		/* Things */
		prototypes1 = new Hashtable<String, Thing>();

		/************************************************************/
		/** Spark Generator                                        **/
		/************************************************************/
		Thing SparkGenerator = new Thing(
				"Spark Generator",
				"This advanced piece of magitech produces near limitless electric power via magic");

		SparkGenerator.setProperty("thingtype", "spark_generator");
		SparkGenerator.setProperty("power", 10);

		prototypes1.put("mud.foe.spark_generator", SparkGenerator);
		
		/************************************************************/
		/** Terminal                                               **/
		/************************************************************/
		mud.foe.Terminal terminal = new mud.foe.Terminal(
				"Terminal",
				"A Stable-Tec terminal, old pre-war technology whose durability is plain to see. On the screen, passively glowing green text indicates that it awaits input.",
				mud.foe.Terminal.Power.POWER_ON, null, null);

		prototypes1.put("mud.foe.terminal", terminal);
	}
	
	@Override
	public void init2(final List<Faction> mFactions, Hashtable<String, ItemType> mItemTypes) {
		//this.factions = mFactions;
		//this.itemTypes = mItemTypes;
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
	public void PCInit(final Player player) {
		// check to see if the player has been initialized
		if( !player.isInitialized() ) {
			final SpecialRuleset rules = FOESpecial.getInstance();

			Race EARTH = new Race(rules, "Earth", 0, false, false);
			Race PEGASUS = new Race(rules, "Pegasus", 1, true, false);
			Race UNICORN = new Race(rules, "Unicorn", 2, false, false);

			//Race GRIFFIN;
			//Race ZEBRA;
			
			// initialize slots
			player.addSlot("helmet",        new Slot(FOESlotTypes.HEAD, ItemTypes.ARMOR));
			player.addSlot("clothes_head",  new Slot(FOESlotTypes.HEAD, ItemTypes.CLOTHING));
			player.addSlot("eyes",          new Slot(FOESlotTypes.EYES, ItemTypes.NONE)); // eyewear? headwear?

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
			for(final Faction faction : getFactions()) player.setReputation(faction, 0);
		}
	}
	
	public void levelup(final Player player) {
		
	}

	public List<Faction> getFactions() {
		return FalloutEquestria.factions;
	}

	@Override
	public Map<String, Item> getItemPrototypes() {
		return FalloutEquestria.prototypes;
	}

	@Override
	public Map<String, Thing> getThingPrototypes() {
		return FalloutEquestria.prototypes1;
	}

	@Override
	public Map<String, Command> getCommands() {
		return FalloutEquestria.commands;
	}
	
	public Terminal createTerminal() {
		return null;
	}
	
	@Override
	public ItemType getItemType(final Integer typeId) {
		return FOEItemTypes.getType(typeId);
	}
	
	public ItemType getItemType(final String typeName) {
		return FOEItemTypes.getType(typeName);
	}
	
	@Override
	public SlotType getSlotType(final Integer typeId) {
		return FOESlotTypes.getType(typeId);
	}
	
	@Override
	public SlotType getSlotType(final String typeName) {
		return FOESlotTypes.getType(typeName);
	}
	
	@Override
	public ThingType getThingType(final Integer typeId) {
		return FOEThingTypes.getType(typeId);
	}
	
	@Override
	public ThingType getThingType(final String typeName) {
		return FOEThingTypes.getType(typeName);
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
		int slotType  = Utils.toInt(attr[6], 0); // get the slot type
		//int equipType = Utils.toInt(attr[6], 0);

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
	
	@Override
	public Thing loadThing(final String itemData) {
		String[] attr = itemData.split("#");
		
		System.out.println("");
		System.out.println("debug(itemData): " + itemData);
		System.out.println("");
		
		Integer oDBRef = Integer.parseInt(attr[0]);
		String oName = attr[1];
		Character oTypeFlag = attr[2].charAt(0);
		String oFlags = attr[2].substring(1, attr[2].length());
		String oDesc = attr[3];
		Integer oLocation = Integer.parseInt(attr[4]);
		
		EnumSet<ObjectFlag> flags = ObjectFlag.getFlagsFromString(oFlags);
		
		// -----
		
		int thingType = Utils.toInt(attr[5], 0); // find a type, else
		
		final ThingType tt = FOEThingTypes.getType(thingType);
		
		if( tt == FOEThingTypes.TERMINAL ) {
			// TODO need to handle dbref setting properly
			Terminal term = new Terminal(oName, "A Stable-Tec terminal, old pre-war technology whose durability is plain to see.");
			
			term.setDBRef(oDBRef);
			term.setLocation(oLocation);
			
			term.setProperty("/visuals/screen", "passively glowing green text indicates that it awaits input");
			
			term.init();
			term.powerOn();
			
			//return new Terminal( oName, oDesc );
			return term;
		}
		else if( tt == FOEThingTypes.VENDING_MACHINE ) {
			final Thing thing = new Thing(oDBRef, oName, flags, oDesc, oLocation);
			
			thing.thing_type = FOEThingTypes.VENDING_MACHINE;
			
			String[] temp = Utils.loadStrings( String.format("%s\\%s\\%s", DATA_DIR, "scripts", "pipbuck_machine.s") );
			
			String script = Utils.join(temp, "").replace(" ", "");
			
			thing.setScriptOnTrigger(TriggerType.onUse, script);
			
			return thing;
		}
		else {
			return new Thing(oDBRef, oName, flags, oDesc, oLocation);
		}
	}
	
	public static Race getRace(final int id) {
		return Races.getRace(id);
	}
	
	public static Race getRace(final String name) {
		return Races.getRace(name);
	}
	
	public void run() {
		for(final Player player : terminals.keySet()) {
			final Terminal term = terminals.get( player );
			final Client client = player.getClient();
			
			term.exec();
			
			String line = term.read();
			//String line;
			
			/*while((line = term.read()) != null) {
				client.write( line );
			}*/
			
			while(line != null) {
				System.out.println("Line NOT NULL!");
				
				client.write( line );
				
				line = term.read();
			}

			if( term.isPaused() ) {
				player.setStatus("IC");
				
				terminals.remove(player);
				
				//send("Stopped using terminal.", player.getClient());
				player.getClient().writeln("Stopped using terminal.");
			}
		}
	}
	
	public void op(final String input, final Player player) {
		if ( player.getStatus().equals("TERM") ) {
			//debug("op_terminal: " + input);
			
			op_terminal(input, player);
		}
	}
	
	void op_terminal(final String input, final Player player) {
		//debug("Using Terminal");
		System.out.println("Write to Terminal");
		
		mud.foe.Terminal term = terminals.get( player );
		
		term.write( input );

		/*if (term.getLoginState() == mud.foe.Terminal.Login.LOGGED_OUT) {
			term.handle_login(input, client);
		}
		else if (term.getLoginState() == mud.foe.Terminal.Login.LOGGED_IN) {
			int code = term.processInput(input);

			if (code == 0) {
				player.setStatus("IC");
				notify(player, "You quit using the terminal.");
			}
		}*/
	}
	
	public boolean use(final Player player, final MUDObject mobj) {
		if( mobj.isType(TypeFlag.THING) ) {
			final Thing thing = (Thing) mobj;
			
			/*switch( thing.thing_type ) {
			case FOEThingTypes.TERMINAL:
				break;
			default:
				break;
			}*/
			
			if( thing.thing_type == FOEThingTypes.TERMINAL ) {
				final Terminal term = (Terminal) thing;
				
				if ( term.checkStatus(Terminal.Power.POWER_ON, Terminal.Use.USABLE) ) {
					
				}
				
				if (term.getPowerState() == Terminal.Power.POWER_ON) {
					//debug(player.getName() + " USING TERMINAL");
					System.out.println(player.getName() + " USING TERMINAL");
					
					terminals.put(player, term);

					player.setStatus("TERM"); // tell the game proper to redirect my input to terminal i/o
					
					term.setPaused(false);
				}
				else {
					//send("The terminal is powered off, perhaps you could 'turn terminal on'?", client);
					player.getClient().writeln("The terminal is powered off, perhaps you could 'turn terminal on'?");
				}
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void test() {
		// TODO Auto-generated method stub
		
	}
	
	public void validatePlayer(final Player player) {
		
	}
	
}