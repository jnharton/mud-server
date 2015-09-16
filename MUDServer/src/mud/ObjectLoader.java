package mud;

import java.util.*;

import mud.objects.*;
import mud.objects.creatures.Horse;
import mud.objects.exits.Door;
import mud.objects.exits.Portal;
import mud.objects.exits.PortalType;
import mud.objects.items.*;
import mud.objects.npcs.Innkeeper;
import mud.objects.npcs.Merchant;
import mud.objects.things.Box;
import mud.objects.Item;
import mud.rulesets.d20.Classes;
import mud.rulesets.d20.Races;
import mud.utils.Utils;
import mud.interfaces.GameModule;
import mud.interfaces.Ruleset;
import mud.magic.Spell;
import mud.misc.Coins;
import mud.misc.SlotType;
import mud.misc.SlotTypes;
import mud.misc.Zone;

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
	static public MUDServer parent;

	static public void loadObjects(final List<String> in, final LoggerI log, final ObjectDB objectDB, final MUDServer parent) {
		/** TODO: resolve this kludge somehow **/
		ObjectLoader.parent = parent;
		
		for (final String oInfo : in) {
			Integer oDBRef = 0, oLocation = 0;
			String oName = "", oFlags = "", oDesc = "";
			char oTypeFlag;

			if (oInfo.charAt(0) == '&') { // means to ignore that line
				log.debug("`loadObjects` ignoring line: " + oInfo);
				
				// grab the dbref number and remove the prefixing & character
				oDBRef = Integer.parseInt(oInfo.split("#")[0].replace('&', ' ').trim());

				NullObject no = new NullObject(oDBRef);
				no.lock(); // lock the NullObject

				System.out.println("NULLObject (" + oDBRef + ") Locked?: " + no.isLocked()); // print out the lock state

				objectDB.add(no);
				continue;
			}

			try {
				String[] attr = oInfo.split("#");
				
				oDBRef = Integer.parseInt(attr[0]);
				
				System.out.println(oDBRef);
				
				oName = attr[1];
				oTypeFlag = attr[2].charAt(0);
				oFlags = attr[2].substring(1, attr[2].length());
				oDesc = attr[3];
				oLocation = Integer.parseInt(attr[4]);

				log.debug("Database Reference Number: " + oDBRef);
				log.debug("Name: " + oName);
				log.debug("Flags: " + oFlags);
				log.debug("Description: " + oDesc);
				log.debug("Location: " + oLocation);

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
					Player player = loadPlayer(oInfo);

					// log.debug("log.debug (db entry): " + player.toDB(), 2);

					objectDB.add(player);
					objectDB.addPlayer(player);
				}
				
				else if (oFlags.equals("IKV")) {
					Innkeeper ik = new Innkeeper( parent, oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, "Merchant", "VEN", oLocation,
							Coins.fromArray(new int[] { 1000, 1000, 1000, 1000 }));

					log.debug("log.debug (db entry): " + ik.toDB(), 2);
					log.debug("Innkeeper", 2);

					objectDB.add(ik);
					objectDB.addNPC(ik);
				}
				
				// Exit(String tempName, String tempFlags, String tempDesc, int
				// tempLoc, int tempDBREF, int tempDestination)
				else if (oTypeFlag == 'E') {
					int eType = Integer.parseInt(attr[6]);

					ExitType et = ExitType.values()[eType];

					if (et == ExitType.STD) {
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
						exit.setExitType(et);
						
						if( !oAlias.equals("") ) exit.addAlias(oAlias);

						log.debug("log.debug (db entry): " + exit.toDB(), 2);

						objectDB.add(exit);
						objectDB.addExit(exit);
					}
					else if (et == ExitType.DOOR) {
						int oDest = Integer.parseInt(attr[5]);
						
						// all lock states other than 0 or 1 are invalid
						int lockState = Utils.toInt(attr[7], 0);

						String[] temp = oName.split(";");
						System.out.println(Arrays.asList(temp));
						oName = temp[0];

						Door door = new Door(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, oDest);
						door.setExitType(et);

						if (temp.length == 2) {
							String[] names = temp[0].split("/");
							System.out.println(Arrays.asList(names));
							String[] aliases = temp[1].split("/");
							System.out.println(Arrays.asList(aliases));

							if (aliases.length > 0) {
								for (String a : aliases[0].split(",")) {
									door.addAlias(names[0] + "|" + a);
								}

								if (aliases.length == 2) {
									for (String a : aliases[1].split(",")) {
										door.addAlias(names[1] + "|" + a);
									}
								}
							}
						}

						// default is unlocked
						if (lockState == 1) {
							door.lock();
						}

						log.debug("log.debug (db entry): " + door.toDB(), 2);

						objectDB.add(door);
						objectDB.addExit(door);
					}
					else if (et == ExitType.PORTAL) {
						Portal portal;

						int pType = Utils.toInt(attr[7], -1);
						PortalType oPortalType = PortalType.values()[pType];

						// here we assume a typed but unkeyed portal
						// public Portal(PortalType pType, int pOrigin, int[]
						// pDestinations)
						if (oPortalType == PortalType.STD) { // Standard Portal
							int oDestination = Integer.parseInt(attr[5]);

							portal = new Portal(PortalType.STD, oLocation, oDestination);
							portal.setDBRef(oDBRef); // NOTE: ought to handle this in the constructor
							portal.setExitType(et);
							portal.setDesc(oDesc);

							portal.name = attr[1]; // name
							portal.setPosition(0, 0); // set x and y coordinate of position

							log.debug("log.debug (db entry): " + portal.toDB(), 2);

							portal.setKey("test");

							parent.getPortals().add(portal);
							
							parent.getRoom(portal.getOrigin()).addSayEventListener(portal);
							parent.getRoom(portal.getDestination()).addSayEventListener(portal);

							objectDB.add(portal);
							objectDB.addExit(portal);
						}
						else if (oPortalType == PortalType.RANDOM) { // Random
							// Portal
							int[] oDestinations = Utils.stringsToInts(attr[5].split(","));

							portal = new Portal(PortalType.RANDOM, oLocation, oDestinations);
							portal.setDBRef(oDBRef); // NOTE: ought to handle this in the constructor
							portal.setExitType(et);

							portal.name = attr[1]; // name
							portal.setPosition(0, 0); // set x and y coordinate of position

							portal.setKey("test");

							log.debug("log.debug (db entry): " + portal.toDB(), 2);

							parent.getPortals().add(portal);
							parent.getRoom(portal.getOrigin()).addSayEventListener(portal);

							objectDB.add(portal);
							objectDB.addExit(portal);
						}
						else {
							log.debug("log.debug (error): Problem with object #" + oDBRef + " - invalid PortalType", 2);
						}
					}
					else {
						log.debug("log.debug (error): Problem with object #" + oDBRef, 2);
					}
				}
				
				// NPC(int tempDBRef, String tempName, String tempDesc, int
				// tempLoc, String tempTitle)
				else if (oTypeFlag == 'N') {
					if (oFlags.contains("M")) {
						Merchant merchant = new Merchant(parent, oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), "A merchant.", "Merchant", "VEN", oLocation,
								Coins.fromArray(new int[] { 1000, 1000, 1000, 1000 }));

						log.debug("log.debug (db entry): " + merchant.toDB(), 2);
						log.debug("Merchant", 2);

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

						log.debug("log.debug (db entry): " + npc.toDB(), 2);

						objectDB.add(npc);
						objectDB.addNPC(npc);
					}
				}
				
				// Room(String tempName, String tempFlags, String tempDesc, int
				// tempParent, int tempDBREF)
				else if (oTypeFlag == 'R') {
					String roomType = attr[5];
					int[] dimensions = Utils.stringsToInts(attr[6].split(","));
					int zoneId = Utils.toInt(attr[8], -1);

					final Room room = new Room(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);

					log.debug("log.debug (db entry): " + room.toDB(), 2);

					room.setRoomType(RoomType.fromLetter(roomType.charAt(0)));

					// set room dimensions (x, y, z)
					room.setDimensions(dimensions[0], dimensions[1], dimensions[2]);

					// set zone
					if (zoneId != -1) {
						System.out.println("Zone ID: " + zoneId);

						final Zone zone = parent.getZone(zoneId);

						System.out.println((zone == null));

						room.setZone(parent.getZone(zoneId));
						parent.getZone(zoneId).addRoom(room);
					}

					if (room.getRoomType().equals(RoomType.OUTSIDE)) {
						room.getProperties().put("sky", "The sky is clear and flecked with stars.");
					}

					objectDB.add(room);
					objectDB.addRoom(room);
				}
				
				// Thing(String tempName, String tempFlags, String tempDesc, int
				// tempLoc, int tempDBREF)
				// Thing(s)
				else if (oTypeFlag == 'T') {
					int tType = Utils.toInt(attr[5], 0); // find a type, else
					// TYPE 0 (NONE)
					ThingType tt = ThingType.values()[tType];

					final Thing thing;

					if (tt == ThingType.CONTAINER) {
						thing = new Box(oDBRef, oName, oDesc, oLocation);
					} else {
						thing = new Thing(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);
					}

					log.debug("log.debug (db entry): " + thing.toDB(), 2);

					objectDB.add(thing);
					objectDB.addThing(thing);
				}
				
				// Item(s)
				else if (oTypeFlag == 'I') {
					final Item item = loadItem(oInfo);

					objectDB.add(item);
					objectDB.addItem(item);

					log.debug("log.debug (db entry): " + item.toDB(), 2);
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
				
				else if (oFlags.contains("null")) {
					NullObject Null = new NullObject(oDBRef);

					objectDB.add(Null);

					log.debug("log.debug (db entry): " + Null.toDB() + " [Found NULLObject]", 2);
				}
			}
			catch (ConcurrentModificationException cme)   { cme.printStackTrace();    }
			catch (ArrayIndexOutOfBoundsException aioobe) { aioobe.printStackTrace(); }
		}
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
	public static Player loadPlayer(String playerData) {

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
		// 12 - player state (ALIVE/DEAD/INCAPACITATED/etc)

		Integer oDBRef = Utils.toInt(attr[0], -1);
		String oName = attr[1];
		char oTypeFlag = attr[2].charAt(0);
		String oFlags = attr[2].substring(1, attr[2].length());
		String oDesc = attr[3];
		Integer oLocation = Utils.toInt(attr[4], Constants.WELCOME_ROOM);

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

		/* Set Player State */
		int state = Utils.toInt(attr[12], -1);
		player.setState(Player.State.values()[state]);

		/**/
		// player_status = Utils.toInt(attr[13], -1);
		// player.setPStatus(Player.Status.values()[player_status]);
		
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
	static private NPC loadNPC(String npcData) {

		String[] attr = npcData.split("#");

		int oDBRef = 0, oLocation = 0;
		String oName = "", oFlags = "", oDesc = "";
		String[] os, om;

		int len = attr[2].length();

		oDBRef = Integer.parseInt(attr[0]); // 0 - npc database reference number
		oName = attr[1]; // 1 - npc name
		oFlags = attr[2].substring(1, len); // 2 - npc flags
		oDesc = attr[3]; // 3 - npc description
		oLocation = Integer.parseInt(attr[4]); // 4 - npc location

		// 5 - npc doesn't have a password
		os = attr[6].split(","); // 6 - npc stats
		om = attr[7].split(","); // 7 - npc money
		int access; // 8 - npc permissions
		int raceNum; // 9 - npc race number (enum ordinal)
		int classNum; // 10 - npc class number (enum ordinal)

		/*
		 * debug("Database Reference Number: " + oDBRef); debug("Name: " +
		 * oName); debug("Flags: " + oFlags); debug("Description: " + oDesc);
		 * debug("Location: " + oLocation);
		 */

		Integer[] oStats = Utils.stringsToIntegers(os);
		int[] oMoney = Utils.stringsToInts(om);

		NPC npc = new NPC(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags),
				oDesc, oLocation, "", "IC", oStats, Coins.fromArray(oMoney));

		// Set NPC Race
		try {
			raceNum = Integer.parseInt(attr[9]);
			//npc.setRace(Races.getRace(raceNum));
			npc.setRace(parent.getRace(raceNum));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			npc.setRace(Races.NONE);
		}

		// Set NPC Class
		try {
			classNum = Integer.parseInt(attr[10]);
			npc.setPClass(Classes.getClass(classNum));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			npc.setPClass(Classes.NONE);
		}
		
		// mark ownership
		npc.setOwner( npc );

		return npc;
	}
	
	final static private Item loadItem(final String itemData) {
		String[] attr = itemData.split("#");
		
		System.out.println("");
		System.out.println("debug(itemData): " + itemData);
		System.out.println("");
		
		Integer oDBRef = Integer.parseInt(attr[0]);
		String oName = attr[1];
		char oTypeFlag = attr[2].charAt(0);
		String oFlags = attr[2].substring(1, attr[2].length());
		String oDesc = attr[3];
		Integer oLocation = Integer.parseInt(attr[4]);
		
		// TODO equipType, slotType are new, work them in (3-17-2015)
		int itemType  = Utils.toInt(attr[5], 0); // get the type of item it should be
		int equipType = Utils.toInt(attr[6], 0);
		int slotType  = Utils.toInt(attr[7], 0);
		
		// TODO fix getting an item type in the loader to use module/etc item types as well
		//ItemType it = ItemTypes.getType(itemType);
		ItemType it = getItemType(itemType);
		
		SlotType st = getSlotType(slotType);

		/*ItemType itemType = ItemTypes.getType(typeId);

		// if it's not a default item type, try the game module's set of item types
		if( itemType == null ) {
			for(final ItemType it : parent.getGameModule().getItemTypes().values()) {
				if( it.getId() == typeId ) {
					itemType = it;
					break;
				}
			}
		}

		return itemType;*/
		
		System.out.println("ItemType ID: " + itemType);
		System.out.println("SlotType ID: " + slotType);
		
		if( it.getId() >= 16 || it == null ) {
			System.out.println("ItemType ID: " + itemType);
			System.out.println("");
			
			final Item item = parent.getGameModule().loadItem(itemData);
			
			System.out.println("Item DBRef: " + item.getDBRef());
			System.out.println("");
			
			return item;
		}
		
		if( st != null ) {
			System.out.println("SlotType: " + st.getName());
			System.out.println("");
		}

		if (it == ItemTypes.CLOTHING) { // Clothing
			int clothingType = Integer.parseInt(attr[8]);
			int mod = Integer.parseInt(attr[9]);

			final Clothing clothing = new Clothing(oDBRef, oName, oDesc, oLocation, mod, ClothingType.values()[clothingType]);

			//clothing.setEquipType(ItemTypes.getType(equipType);
			clothing.setSlotType(st);

			return clothing;
		}
		else if (it == ItemTypes.WAND) { // Wand
			String spellName = attr[8];
			int charges = Integer.parseInt(attr[9]);

			Spell spell = parent.getSpell(spellName);

			final Wand wand = new Wand(oName, oDesc, oLocation, oDBRef, ItemTypes.getType(itemType), charges, spell);
			
			return wand;
		}
		else if (it == ItemTypes.WEAPON) { // Weapon Merchant
			int weaponType = Integer.parseInt(attr[8]);
			int mod = Integer.parseInt(attr[9]);

			final Weapon weapon = new Weapon(oDBRef, oName, oDesc, oLocation, mod, Handed.ONE, WeaponTypes.getWeaponType(weaponType), 15.0);
			
			weapon.setSlotType(st);
			
			return weapon;
		}
		else if (it == ItemTypes.ARMOR) { // Armor Merchant
			int armorType = Integer.parseInt(attr[8]);
			int mod = Integer.parseInt(attr[9]);

			final Armor armor = new Armor(oName, oDesc, (int) oLocation, (int) oDBRef, mod, ArmorType.values()[armorType]);
			
			armor.setSlotType(st);
			
			return armor;
		}
		else if (it == ItemTypes.ARROW) { // Arrow
			final Arrow arrow = new Arrow(oDBRef, oName, oDesc, oLocation);
			
			return arrow;
		}
		else if (it == ItemTypes.BOOK) { // Book
			String author = attr[8];
			String title = attr[9];
			int pages = Integer.parseInt(attr[10]);

			final Book book = new Book(oName, oDesc, oLocation, oDBRef);

			book.setAuthor(author);
			book.setTitle(title);
			book.setPageNum(0);
			
			return book;
		}
		else if (it == ItemTypes.CONTAINER) { // Container
			final Container container = new Container(oName);

			container.setDesc(oDesc);
			container.setLocation(oLocation);
			container.setDBRef(oDBRef);
			
			return container;
		}
		else if (it == ItemTypes.POTION) {
			int stack_size = Integer.parseInt(attr[8]);
			String sn = attr[9];
			
			/*
			 * whatever I do here needs to recreate the entirety of
			 * a stack of potions correctly
			 */
			
			Potion potion = new Potion(oDBRef, oName, EnumSet.noneOf(ObjectFlag.class), oDesc, oLocation, sn);

			for (int i = 1; i < stack_size; i++) {
				Potion potion1 = new Potion(oDBRef, oName, EnumSet.noneOf(ObjectFlag.class), oDesc, oLocation, sn);

				potion.stack(potion1);
			}
			
			return potion;
		}
		else if (it == ItemTypes.SHIELD) { // Armor Merchant
			int shieldType = Integer.parseInt(attr[8]);
			int mod = Integer.parseInt(attr[9]);

			Shield shield = new Shield(oName, oDesc, oLocation, oDBRef, mod, it, ShieldType.values()[shieldType]);
			
			return shield;
		}
		else if (it == ItemTypes.RING) {
			//Item ring = new Item(oDBRef, oName, null, oDesc, oLocation);
			final Item ring = new Item(oDBRef, oName, EnumSet.noneOf(ObjectFlag.class), oDesc, oLocation);
			
			// TODO this is special, an 'item type' without an associated class
			//ring.setItemType(ItemTypes.RING);
			
			return ring;
		}
		else {
			// TODO resolve issue of loading item types not listed in here (3-20-2015)
			//Item item = new Item(oDBRef, oName, null, oDesc, oLocation);
			
			final Item item = new Item(oDBRef, oName, EnumSet.noneOf(ObjectFlag.class), oDesc, oLocation);
			
			return item;
		}
	}
	
	private static ItemType getItemType(final int typeId) {
		final GameModule module = parent.getGameModule();
		
		if( module != null && typeId >= 16 ) {
			return module.getItemType(typeId);
		}
		else {
			return ItemTypes.getType(typeId);
		}
	}
	
	private static SlotType getSlotType(final int typeId) {
		final GameModule module = parent.getGameModule();
		
		if( module != null) {
			return module.getSlotType(typeId);
		}
		else {
			return SlotTypes.getType(typeId);
		}
	}
}