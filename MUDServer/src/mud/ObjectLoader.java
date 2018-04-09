package mud;

import java.io.File;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.List;
import java.util.LinkedList;

import mud.objects.*;
import mud.objects.exits.Door;
import mud.objects.exits.Portal;
import mud.objects.exits.PortalType;
import mud.objects.items.*;
import mud.objects.npcs.Innkeeper;
import mud.objects.npcs.Merchant;
import mud.objects.things.Box;
import mud.rulesets.d20.Classes;
import mud.rulesets.d20.Races;
import mud.utils.Utils;
import mud.interfaces.GameModule;
import mud.magic.Spell;
import mud.misc.Coins;
import mud.misc.Effect;
import mud.misc.InvalidItemTypeException;
import mud.misc.InvalidThingTypeException;
import mud.misc.SlotType;
import mud.misc.SlotTypes;
import mud.misc.Zone;
import mud.misc.Effect.DurationType;

/*
 Copyright (c) 2012 Jeremy N. Harton

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * 
 * @author jeremy
 * @author joshgit
 * 
 */

public class ObjectLoader {
	// TODO get rid of parent reference if possible
	private final MUDServer parent;
	private final ObjectDB objectDB;
	
	private boolean loaded = false;
	
	public ObjectLoader(final MUDServer parent, final ObjectDB objectDB) {
		this.parent = parent;
		this.objectDB = objectDB;
	}
	
	// TODO fix this so I actually us the logger
	public void loadObjects(final List<String> in, final Logger logger) {
		for (final String oInfo : in) {
			Integer oDBRef = 0, oLocation = 0;
			String oName = "", oFlags = "", oDesc = "";
			char oTypeFlag;

			if (oInfo.charAt(0) == '&') { // means to ignore that line
				debug("`loadObjects` ignoring line: ");
				debug(oInfo);
				debug("");
				
				// grab the dbref number and remove the prefixing & character
				oDBRef = Integer.parseInt(oInfo.split("#")[0].replace('&', ' ').trim());

				NullObject no = new NullObject(oDBRef);
				no.lock(); // lock the NullObject
				
				//debug("NULLObject (" + oDBRef + ") Locked?: " + no.isLocked()); // print out the lock state
				//debug("");

				objectDB.add(no);
				
				continue;
			}

			try {
				String[] attr = oInfo.split("#");
				
				oDBRef = Integer.parseInt(attr[0]);
				oName = attr[1];
				oTypeFlag = attr[2].charAt(0);
				oFlags = attr[2].substring(1, attr[2].length());
				oDesc = attr[3];
				oLocation = Integer.parseInt(attr[4]);
				
				debug("---- Database Object");
				debug("DBRef: " + oDBRef);
				debug("Name: " + oName);
				debug("Flags: " + oFlags);
				debug("Description: " + oDesc);
				debug("Location: " + oLocation);
				debug("");

				if (oTypeFlag == 'C') {
					/*
					 * int cType = Integer.parseInt(attr[6]);
					 * 
					 * CreatureType ct = CreatureType.values()[cType];
					 * 
					 * if (ct == CreatureType.HORSE) { final Horse horse = new
					 * Horse(); //final Creature cre = new Creature(oDBRef,
					 * oName, ObjectFlag.getFlagsFromString(oFlags), oDesc,
					 * oLocation); horse.setCreatureType( ct );
					 * 
					 * // add the creature to the in-memory database and to the
					 * list of creatures objectDB.add(horse);
					 * objectDB.addCreature(horse); } else { final Creature cre
					 * = new Creature(oDBRef, oName,
					 * ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);
					 * 
					 * // add the creature to the in-memory database and to the
					 * list of creatures objectDB.add(cre);
					 * objectDB.addCreature(cre); }
					 */

					final Creature cre = new Creature(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);

					// add the creature to the in-memory database and to the
					// list of creatures
					objectDB.add(cre);
					objectDB.addCreature(cre);
				}
				else if (oTypeFlag == 'P') {
					// Player
					Player player = loadPlayer(oInfo);
					
					debug("log.debug (db entry): " + player.toDB(), 2);

					objectDB.add(player);
					objectDB.addPlayer(player);
				}
				else if (oFlags.equals("IKV")) {
					// NPC - Innkeeper
					Innkeeper ik = new Innkeeper(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, "Merchant",
							"VEN", oLocation, Coins.fromArray(new int[] { 1000, 1000, 1000, 1000 }));

					debug("log.debug (db entry): " + ik.toDB(), 2);
					debug("Innkeeper", 2);

					objectDB.add(ik);
					objectDB.addNPC(ik);
				}
				
				// Exit(String tempName, String tempFlags, String tempDesc, int
				// tempLoc, int tempDBREF, int tempDestination)
				else if (oTypeFlag == 'E') {
					// Exit
					//String oDest = attr[5];
					int eType = Integer.parseInt(attr[6]);

					ExitType et = ExitType.values()[eType];

					if (et == ExitType.STD) {
						// Standard
						int oDest = Integer.parseInt(attr[5]);
						
						// [A]bsolutely [E]verything/[Leave];ae/leave
						String oAlias = "";
						
						if( oName.indexOf(';') != -1 ) {
							final String[] temp = oName.split(";");
							
							oName = oName.substring(0, oName.indexOf(';'));
							
							if( temp.length > 1 ) {
								oAlias = temp[1];
							}
						}

						Exit exit = new Exit(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, oDest);
						
						if( !oAlias.equals("") ) exit.addAlias(oAlias);

						debug("log.debug (db entry): " + exit.toDB(), 2);

						objectDB.add(exit);
						objectDB.addExit(exit);
					}
					else if (et == ExitType.DOOR) {
						// Door
						int oDest = Integer.parseInt(attr[5]);
						
						int lockState = Utils.toInt(attr[7], 0); //valid lock states are: 0, 1
						int keyDBRef = Utils.toInt(attr[8], -1);
						
						System.out.println("   Location: " + oLocation);
						System.out.println("Destination: "  + oDest);
						
						String[] temp = oName.split(";");
						
						System.out.println(Arrays.asList(temp));
						
						oName = temp[0];

						Door door = new Door(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, oDest);

						if (temp.length == 2) {
							String[] names = temp[0].split("/");
							String[] aliases = temp[1].split("/");
							
							System.out.println("  Names: " + Arrays.asList(names));
							System.out.println("Aliases: " + Arrays.asList(aliases));
							
							// set up any aliases
							if (aliases.length > 0) {
								for (final String a : aliases[0].split(",")) door.addAlias(names[0] + "|" + a);

								if (aliases.length == 2) {
									for (String a : aliases[1].split(",")) door.addAlias(names[1] + "|" + a);
								}
							}
						}
						
						if ( lockState == 1 ) door.lock();
						
						if ( keyDBRef != -1 ) {
							final Item item = objectDB.getItem(keyDBRef);
							
							if( item != null ) door.setKey(item);
						}
						
						door.init();
						
						debug( "exit (origin): " + door.getName(oLocation) );
						debug( "exit (dest): " + door.getName(oDest) );
						
						debug( door.side1.toString() );
						debug( door.side2.toString() );
						
						debug("log.debug (db entry): " + door.toDB(), 2);

						objectDB.add(door);
						objectDB.addExit(door);
					}
					else if (et == ExitType.PORTAL) {
						// Portal
						Portal portal;

						int pType = Utils.toInt(attr[7], -1);
						PortalType oPortalType = PortalType.values()[pType];

						// here we assume a typed but unkeyed portal
						if (oPortalType == PortalType.STD) {
							// Standard
							int oDestination = Integer.parseInt(attr[5]);

							portal = new Portal(PortalType.STD, oLocation, oDestination);
							
							portal.setDBRef(oDBRef); // NOTE: ought to handle this in the constructor
							portal.setDesc(oDesc);

							portal.name = attr[1]; // name
							portal.setPosition(0, 0); // set x and y coordinate of position
							
							portal.setKey("test");
							
							debug("log.debug (db entry): " + portal.toDB(), 2);
							
							objectDB.add(portal);
							objectDB.addExit(portal);
						}
						else if (oPortalType == PortalType.RANDOM) {
							// Random
							int[] oDestinations = Utils.stringsToInts(attr[5].split(","));

							portal = new Portal(PortalType.RANDOM, oLocation, oDestinations);
							
							portal.setDBRef(oDBRef); // NOTE: ought to handle this in the constructor
							portal.setDesc(oDesc);
							
							portal.name = attr[1]; // name
							portal.setPosition(0, 0); // set x and y coordinate of position

							portal.setKey("test");

							debug("log.debug (db entry): " + portal.toDB(), 2);
							
							objectDB.add(portal);
							objectDB.addExit(portal);
						}
						else {
							debug("log.debug (error): Problem with object #" + oDBRef + " - invalid PortalType", 2);
						}
					}
					else {
						debug("log.debug (error): Problem with object #" + oDBRef, 2);
					}
				}
				else if (oTypeFlag == 'N') {
					// NPC
					if (oFlags.contains("M")) {
						Merchant merchant = new Merchant(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), "A merchant.",
								"Merchant", "VEN", oLocation, Coins.fromArray(new int[] { 1000, 1000, 1000, 1000 }));

						debug("log.debug (db entry): " + merchant.toDB(), 2);
						debug("Merchant", 2);

						objectDB.add(merchant);
						objectDB.addNPC(merchant);
					}
					else {

						// NPC npc = new NPC(oDBRef, oName, oDesc, oLocation,
						// "npc");
						NPC npc = loadNPC(oInfo);
						npc.setCName("npc");

						// npc.addQuest(new Quest("Test", "Test", new
						// Task("Test")));

						debug("log.debug (db entry): " + npc.toDB(), 2);

						objectDB.add(npc);
						objectDB.addNPC(npc);
					}
				}
				else if (oTypeFlag == 'R') {
					// Room
					String roomType = attr[5];
					int[] dimensions = Utils.stringsToInts(attr[6].split(","));
					int zoneId = Utils.toInt(attr[8], -1);

					final Room room = new Room(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);

					room.setRoomType(RoomType.fromLetter(roomType.charAt(0)));

					// set room dimensions (x, y, z)
					room.setDimensions(dimensions[0], dimensions[1], dimensions[2]);

					// set zone
					if (zoneId != -1) {
						debug("Zone ID: " + zoneId);

						final Zone zone = parent.getZone(zoneId);
						
						if( zone != null ) {
							room.setZone(zone);
							zone.addRoom(room);
							
							//room.setZone(parent.getZone(zoneId));
							//parent.getZone(zoneId).addRoom(room);
						}
						
						debug((zone == null) ? "Zone is NULL." : "Zone in NOT NULL.");
					}

					if (room.getRoomType().equals(RoomType.OUTSIDE)) {
						room.getProperties().put("sky", "The sky is clear and flecked with stars.");
					}
					
					debug("log.debug (db entry): " + room.toDB(), 2);

					objectDB.add(room);
					objectDB.addRoom(room);
				}
				else if (oTypeFlag == 'T') {
					// Thing
					try {
						final Thing thing = loadThing(oInfo);
						
						debug("log.debug (db entry): " + thing.toDB(), 2);
						
						objectDB.add(thing);
						objectDB.addThing(thing);
					}
					catch (final InvalidThingTypeException itte) {
						itte.printStackTrace();
						debug("log.debug (error): " + itte.getMessage());
					}
				}
				else if (oTypeFlag == 'I') {
					// Item
					try {
						final Item item = loadItem(oInfo);
						
						debug("log.debug (db entry): " + item.toDB(), 2);
						
						objectDB.add(item);
						objectDB.addItem(item);
					}
					catch(final InvalidItemTypeException iite) {
						iite.printStackTrace();
						debug("log.debug (error): " + iite.getMessage());
					}
				}
				else if (oTypeFlag == 'Z') { // Zone
					// not sure about this bit, for some reason I made 'Z' a
					// TypeFlag
					// for a Zone, but Zone isn't presently a MUDObject and I'm
					// a little
					// uncertain as to whether it should be.
					//
					// i'd like to load them on startup, but MUDObjects chiefly
					// handle "real" objects
					// in the world rather than abstract concepts. Of course,
					// Room kind of bends
					// the boundary in that defines an abstract space and Zone
					// is kind of like a super room...
					/*
					 * Zone zone = loadZone();
					 * 
					 * objectDB.add(zone);
					 */

				}
				
				else if ( oName.equals("null") ) {
					NullObject Null = new NullObject(oDBRef);
					
					debug("log.debug (db entry): " + Null.toDB() + " [Found NULLObject]", 2);
					
					objectDB.add(Null);
				}
			}
			catch (ConcurrentModificationException cme)   { cme.printStackTrace();    }
			catch (ArrayIndexOutOfBoundsException aioobe) { aioobe.printStackTrace(); }
		}
		
		loaded = true;
	}

	/*
	 * loadPlayer is probably redundant with loadNPC to some extent and doesn't
	 * seem to be used, but just moving it to here for now.
	 * 
	 * ~jnharton
	 */

	/**
	 * Generate a player from it's database representation
	 * 
	 * NOTE: for testing purposes only now, init_conn doesn't go through
	 * loadObjects, which is pointless when you consider that I only hold onto a
	 * copy of the objects and it never goes into the player's array.
	 * 
	 * NOTE2: meant to solve a problem where I haven't copied the load code into
	 * init_conn, but want a properly initialized/loaded player for existing
	 * characters when they login
	 * 
	 * @param playerData
	 * @return a player object
	 */
	public Player loadPlayer(String playerData) {

		String[] attr = playerData.split("#");

		// 0 - player database reference number
		// 1 - player name
		// 2 - player flags
		// 3 - player description
		// 4 - player location
		// 5 - player password
		// 6 - player stats
		// 7 - player money
		// 8 - player permissions
		// 9 - player race number (enum ordinal)
		// 10 - player class number (enum ordinal)
		// 11 - player status

		Integer oDBRef = Utils.toInt(attr[0], -1);
		String oName = attr[1];
		char oTypeFlag = attr[2].charAt(0);
		String oFlags = attr[2].substring(1, attr[2].length());
		String oDesc = attr[3];
		Integer oLocation = Utils.toInt(attr[4], Constants.VOID);

		/*
		 * debug("Database Reference Number: " + oDBRef); debug("Name: " +
		 * oName); debug("Flags: " + oFlags); debug("Description: " + oDesc);
		 * debug("Location: " + oLocation);
		 */

		String oPassword = attr[5];

		Integer[] oStats = Utils.stringsToIntegers(attr[6].split(","));
		int[] oMoney = Utils.stringsToInts(attr[7].split(","));

		Player player = new Player(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, "", oPassword, "IC", oStats, Coins.fromArray(oMoney));

		int access, raceNum, classNum, player_status;

		/* Set Player Permissions */
		access = Utils.toInt(attr[8], Constants.USER);
		player.setAccess(access);

		/* Set Player Race */
		raceNum = Utils.toInt(attr[9], Races.NONE.getId());
		//player.setRace(Races.getRace(raceNum));
		player.setRace(parent.getRace(raceNum));
		System.out.println("Race: " + raceNum + " ( " + parent.getRace(raceNum).getName() + " )");

		/* Set Player Class */
		classNum = Utils.toInt(attr[10], Classes.NONE.getId());
		player.setPClass(Classes.getClass(classNum));
		System.out.println("Class: " + classNum + " ( " + Classes.getClass(classNum).getName() + " )");

		/* Set Status */
		player.setStatus(attr[11]);
		
		// mark ownership
		player.setOwner( player );

		return player;
	}

	/**
	 * Generate a player from it's database representation
	 * 
	 * NOTE: for testing purposes only now, init_conn doesn't go through
	 * loadObjects, which is pointless when you consider that I only hold onto a
	 * copy of the objects and it never goes into the player's array.
	 * 
	 * NOTE2: meant to solve a problem where I haven't copied the load code into
	 * init_conn, but want a properly initialized/loaded player for existing
	 * characters when they login
	 * 
	 * @param playerData
	 * @return a player object
	 */
	private NPC loadNPC(String npcData) {
		final String[] attr = npcData.split("#");

		int oDBRef = 0, oLocation = 0;
		String oName = "", oFlags = "", oDesc = "";
		Integer[] oStats;
		int[] oMoney;

		int len = attr[2].length();

		oDBRef = Integer.parseInt(attr[0]);    // 0 - npc database reference number
		oName = attr[1];                       // 1 - npc name
		oFlags = attr[2].substring(1, len);    // 2 - npc flags
		oDesc = attr[3];                       // 3 - npc description
		oLocation = Integer.parseInt(attr[4]); // 4 - npc location
		
		// 5 - npc doesn't have a password
		
		oStats = Utils.stringsToIntegers( attr[6].split(",") ); // 6 - npc stats
		oMoney = Utils.stringsToInts( attr[7].split(",") );     // 7 - npc money
		
		/*
		debug("Database Reference Number: " + oDBRef);
		debug("Name: " + oName);
		debug("Flags: " + oFlags);
		debug("Description: " + oDesc);
		debug("Location: " + oLocation);
		*/
		
		NPC npc = new NPC(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, "", "IC", oStats, Coins.fromArray(oMoney));
		
		int access;   // 8 - npc permissions
		int raceNum;  // 9 - npc race number (enum ordinal)
		int classNum; // 10 - npc class number (enum ordinal)
		
		// Set NPC Access
		npc.setAccess(Constants.USER);

		// Set NPC Race
		//raceNum = Utils.toInt(attr[9], alt);
		
		// TODO resolve issue
		try {
			raceNum = Integer.parseInt(attr[9]);
			//npc.setRace(Races.getRace(raceNum));
			npc.setRace(parent.getRace(raceNum));
		}
		catch (final NumberFormatException nfe) {
			nfe.printStackTrace();
			npc.setRace(Races.NONE);
		}

		// Set NPC Class
		try {
			classNum = Integer.parseInt(attr[10]);
			npc.setPClass(Classes.getClass(classNum));
		}
		catch (final NumberFormatException nfe) {
			nfe.printStackTrace();
			npc.setPClass(Classes.NONE);
		}
		
		npc.setStatus(attr[11]); // 11 - npc status
		
		// mark ownership
		npc.setOwner( npc );

		return npc;
	}

	final private Item loadItem(final String itemData) throws InvalidItemTypeException {
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
		
		int itemType  = Utils.toInt(attr[5], 0); // get the type of item it should be
		int slotType  = Utils.toInt(attr[6], 0);
		
		final ItemType it = getItemType(itemType);
		final SlotType st = getSlotType(slotType);
		
		System.out.println("ItemType ID: " + itemType);
		System.out.println("SlotType ID: " + slotType);
		
		if( it != null ) {
			System.out.println("ItemType: " + it.getName());
			System.out.println("");
		}
		else throw new InvalidItemTypeException("No such ItemType (" + itemType + ")");
		
		if( st != null ) {
			System.out.println("SlotType: " + st.getName());
			System.out.println("");
		}
		
		// module level itemtype handling...
		if( it.getId() >= 16 ) {
			final Item item = parent.getGameModule().loadItem(itemData);

			System.out.println("Item DBRef: " + item.getDBRef());
			System.out.println("");

			return item;
		}

		if (it == ItemTypes.CLOTHING) { // Clothing
			final Clothing clothing = new Clothing(oDBRef, oName, flags, oDesc, oLocation);
			
			clothing.setSlotType(st);

			return clothing;
		}
		else if (it == ItemTypes.WAND) { // Wand
			String spellName = attr[7];
			int charges = Integer.parseInt(attr[8]);

			Spell spell = parent.getSpell(spellName);

			final Wand wand = new Wand(oDBRef, oName, oDesc, flags, oLocation, ItemTypes.getType(itemType), charges, spell);
			
			wand.setSlotType(st);
			
			return wand;
		}
		else if (it == ItemTypes.WEAPON) { // Weapon Merchant
			//int weaponType = Integer.parseInt(attr[7]);
			int mod = Integer.parseInt(attr[8]);

			final Weapon weapon = new Weapon(oDBRef, oName, flags, oDesc, oLocation);
			
			weapon.setSlotType(st);
			
			weapon.setModifier(mod);
			
			return weapon;
		}
		else if (it == ItemTypes.ARMOR) { // Armor Merchant
			int armorType = Integer.parseInt(attr[7]);
			int mod = Integer.parseInt(attr[8]);

			final Armor armor = new Armor(oDBRef, oName, flags, oDesc, oLocation, ArmorType.values()[armorType]);
			
			armor.setSlotType(st);
			armor.setMod(mod);
			
			return armor;
		}
		else if (it == ItemTypes.ARROW) { // Arrow
			final Arrow arrow = new Arrow(oDBRef, oName, flags, oDesc, oLocation);
			
			return arrow;
		}
		else if (it == ItemTypes.BOOK) { // Book
			String author = attr[7];
			String title = attr[8];
			int pages = Integer.parseInt(attr[9]);
			
			// TODO improve persistence issues
			final Book book = new Book(oDBRef, oName, flags, oDesc, oLocation);

			book.setAuthor(author);
			book.setTitle(title);
			book.setPageNum(0);
			
			return book;
		}
		else if (it == ItemTypes.CONTAINER) { // Container
			int size = Utils.toInt(attr[7], Container.DEFAULT_SIZE);
			
			final Container container = new Container(oDBRef, oName, flags, oDesc, oLocation, size);
			
			return container;
		}
		/*else if (it == ItemTypes.DRINK) {
			
			final Drink drink;
			
			return drink;
		}*/
		else if (it == ItemTypes.POTION) {
			int stack_size = Integer.parseInt(attr[7]);
			String spellName = attr[8];
			
			// TODO loading and storing spell and effect info?
			
			/*
			 * whatever I do here needs to recreate the entirety of
			 * a stack of potions correctly
			 */
			
			// TODO this is wonky and doesn't handle one dbref/item...
			
			final Potion potion = new Potion(oDBRef, oName, flags, oDesc, oLocation);

			for (int i = 1; i < stack_size; i++) {
				Potion potion1 = new Potion(oDBRef, oName, flags, oDesc, oLocation);

				potion.stack(potion1);
			}
			
			return potion;
		}
		else if (it == ItemTypes.SHIELD) { // Armor Merchant
			int shieldType = Utils.toInt(attr[7], 0); //Integer.parseInt(attr[7]);
			int mod = Utils.toInt(attr[8], 0); //Integer.parseInt(attr[8]);

			Shield shield = new Shield(oDBRef, oName, flags, oDesc, oLocation, it, ShieldType.values()[shieldType], mod);
			
			return shield;
		}
		else if (it == ItemTypes.RING) {
			//Item ring = new Item(oDBRef, oName, null, oDesc, oLocation);
			//final Item ring = new Item(oDBRef, oName, EnumSet.noneOf(ObjectFlag.class), oDesc, oLocation);
			
			final Jewelry ring = new Jewelry(oDBRef, oName, flags, oDesc, oLocation);
			
			//int effect = Integer.parseInt(attr[]);
			
			// TODO this is special, an 'item type' without an associated class
			//ring.setItemType(ItemTypes.RING);
			
			// TODO fix this, all rings are rings of invisibility now...
			ring.effect = new Effect("Invisibility", Effect.Type.INVIS, DurationType.PERMANENT, -1);
			
			return ring;
		}
		else if (it == ItemTypes.NONE) {
			return new Item(oDBRef, oName, flags, oDesc, oLocation);
		}
		else throw new InvalidItemTypeException("No such ItemType (" + itemType + ")");
		// TODO FIX THIS?! throwing an exception seems cool, but isn't helpful unless I have a check for every item type
	}
	
	private final Thing loadThing(final String thingData) throws InvalidThingTypeException {
		String[] attr = thingData.split("#");
		
		System.out.println("");
		System.out.println("debug(thingData): " + thingData);
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
		
		final ThingType tt = getThingType(thingType);
		
		System.out.println("ThingType ID: " + thingType);
		
		if( tt != null ) {
			System.out.println("ThingType: " + tt.getName());
			System.out.println("");
		}
		else throw new InvalidThingTypeException("No such ThingType ( " + thingType + ")");

		// module level thingtype handling...
		if( tt.getId() >= 16 ) {
			final Thing thing = parent.getGameModule().loadThing(thingData);
			
			System.out.println("Thing DBRef: " + thing.getDBRef());
			System.out.println("");
			
			return thing;
		}

		else if (tt == ThingTypes.CONTAINER) {
			final Box box = new Box(oDBRef, oName, flags, oDesc, oLocation);
			
			return box;
		}
		else {
			final Thing thing = new Thing(oDBRef, oName, flags, oDesc, oLocation);
			
			return thing;
		}
	}
	
	private Book loadBook(final String bookName) {
		//final String bookFile = DATA_DIR + "\\book\\" + bookName + ".book";
		final String bookFile = "";

		boolean header = false;
		boolean page = false;

		File file;
		Book book = null;
		
		List<String> strings;
		
		List<String> strings2 = new LinkedList<String>();
		StringBuilder sb = new StringBuilder();

		String author = "";
		String title = "";
		String desc = "";
		Integer pages = 0;
		
		// -----

		file = new File(bookFile);

		strings = Arrays.asList( Utils.loadStrings(bookFile) );

		for(final String s : strings) {
			if( s.equals("") ) {
				continue;
			}
			else if( s.equals("HEADER") ) {
				header = true;
				continue;
			}
			else if( s.equals("PAGE") ) {
				page = true;
				continue;
			}
			
			if( header ) {
				if( s.equals("/HEADER") ) header = false;
				
				if( !header ) {
					book = new Book(title, author, pages);
					
					book.setDesc(desc);
				}
				else {
					if( s.charAt(0) == '#' ) {
						String[] temp = s.substring(1).split(":");
						
						String key = temp[0];
						String val= temp[1];

						if( key.equalsIgnoreCase("author") ) author = val;
						if( key.equalsIgnoreCase("title") ) title = val;
						if( key.equalsIgnoreCase("desc") )  desc = val;
						if( key.equalsIgnoreCase("pages") ) pages = Utils.toInt(val, 0);
					}
				}
			}
			else if( page ) {
				if( s.equals("/PAGE") ) page = false;
				
				if( !page ) {
					book.addPage(strings2);
					
					sb.delete(0, sb.length());
					strings2.clear();
				}
				else {
					sb.append(s).append(" ");
					strings2.add(s);
				}
			}
		}
		
		return book;
	}
	
	private ItemType getItemType(final int typeId) {
		final GameModule module = parent.getGameModule();
		
		ItemType it = null;
		
		if( module != null ) {
			if( typeId >= 16 ) it = module.getItemType(typeId);
			else               it = ItemTypes.getType(typeId);
		}
		else it = ItemTypes.getType(typeId);
		
		return it;
	}

	private SlotType getSlotType(final int typeId) {
		final GameModule module = parent.getGameModule();
		
		SlotType st = null;
		
		if( module != null) st = module.getSlotType(typeId);
		else                st = SlotTypes.getType(typeId);
		
		return st;
	}

	private ThingType getThingType(final int typeId) {
		final GameModule module = parent.getGameModule();
		
		ThingType tt = null;
		
		if( module != null ) {
			if( typeId >= 16 ) tt = module.getThingType(typeId);
			else               tt = ThingTypes.getType(typeId);
		}
		else tt = ThingTypes.getType(typeId);
		
		return tt;
	}
	
	public boolean isLoaded() {
		return this.loaded;
	}
	
	public void debug(final String data) {
		debug(data, 1);
	}
	
	/**
	 * A wrapper function for System.out.println that can be "disabled" by
	 * setting an integer. Used to turn "on"/"off" printing debug messages to
	 * the console.
	 * 
	 * Each debug level includes the levels below it
	 * 
	 * e.g. debug level 3 includes levels 3, 2, 1 debug level 2 includes levels
	 * 2, 1 debug level 1 includes levels 1
	 * 
	 * Uses an Object parameter and a call to toString so that I can pass
	 * objects to it
	 * 
	 * @param data
	 */
	public void debug(final String data, final int tDebugLevel) {
		parent.debug(data, tDebugLevel);
	}
}