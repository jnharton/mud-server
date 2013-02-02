package mud;

import java.util.*;

import mud.objects.*;
import mud.objects.items.*;
import mud.quest.*;

import mud.MUDObject;
import mud.objects.Item;
import mud.utils.Utils;
import mud.net.Client;

public class ObjectLoader {

	static public void loadObjects(final List<String> in, final LoggerI log, final ObjectDB objectDB, 
            final MUDServer parent)
	{
		for (final String oInfo : in)
		{	
			if ( oInfo == null || oInfo.charAt(0) == '&' ) { // means to ignore that line
				log.debug("`loadObjects` ignoring line: " + oInfo);
				continue;
			}

			Integer oDBRef = 0, oLocation = 0;
			String oName = "", oFlags = "", oDesc = "";

            try {
                String[] attr = oInfo.split("#");
                oDBRef = Integer.parseInt(attr[0]);
                oName = attr[1];
                oFlags = attr[2];
                oDesc = attr[3];
                oLocation = Integer.parseInt(attr[4]);

                /*log.debug("Database Reference Number: " + oDBRef);
                log.debug("Name: " + oName);
                log.debug("Flags: " + oFlags);
                log.debug("Description: " + oDesc);
                log.debug("Location: " + oLocation);*/

                if (oFlags.indexOf("C") == 0) {
                    final Creature cre = new Creature(oDBRef, oName, oFlags, oDesc, oLocation);

                    // set race
                    try {
                        cre.race = Races.getRace(Integer.parseInt(attr[9]));    // 9 - race number (enum ordinal)
                    }
                    catch(NumberFormatException nfe) {
                        nfe.printStackTrace();
                        cre.race = Races.NONE;
                    }

                    // add the creature to the in-memory database and to the list of creatures
                    objectDB.add(cre);
                    objectDB.addCreature(cre);
                }
                else if (oFlags.indexOf("P") != -1) {
                    
                    final String oPassword = attr[5];       // 5 - password
                    // int access;                       // 8 - permissions
                    final Integer[] oStats = Utils.stringsToIntegers(attr[6].split(",")); // 6 - stats
                    final Integer[] oMoney = Utils.stringsToIntegers(attr[7].split(",")); // 7 - money

                    final Player player = new Player(oDBRef, oName, oFlags, oDesc, oLocation, "", oPassword, "IC", oStats, oMoney);
                    final int USER = 0; // stole this constant from MUDServer.
                    player.setAccess(Utils.toInt(attr[8], USER));

                    // set race
                    try {
                        player.setPlayerRace(Races.getRace(Integer.parseInt(attr[9])));   // 9 - race number (enum ordinal)
                    }
                    catch(NumberFormatException nfe) {
                        nfe.printStackTrace();
                        player.setPlayerRace(Races.NONE);
                    }

                    // set class
                    try {
                        player.setPClass(Classes.getClass(Integer.parseInt(attr[10]))); // 10 - class number (enum ordinal)
                    }
                    catch(NumberFormatException nfe) {
                        nfe.printStackTrace();
                        player.setPClass(Classes.NONE);
                    }

                    //log.debug("log.debug (db entry): " + player.toDB(), 2);

                    objectDB.add(player);
                    objectDB.addPlayer(player);
                }
                else if (oFlags.equals("WMV")) {
                    WeaponMerchant wm = new WeaponMerchant(parent, oDBRef, oName, oFlags, "A weapon merchant.", "Merchant", "VEN", 161, new String[]{"1000", "1000", "1000", "1000"} );

                    log.debug("log.debug (db entry): " + wm.toDB(), 2);
                    log.debug("Weapon Merchant", 2);

                    objectDB.add(wm);
                    objectDB.addNPC(wm);
                }
                else if (oFlags.equals("AMV")) {
                    ArmorMerchant am = new ArmorMerchant(parent, oDBRef, oName, oFlags, "An armor merchant.", "Merchant", "VEN", 161, new String[]{"1000", "1000", "1000", "1000"} );

                    log.debug("log.debug (db entry): " + am.toDB(), 2);
                    log.debug("Armor Merchant", 2);

                    objectDB.add(am);
                    objectDB.addNPC(am);
                }
                else if (oFlags.equals("IKV") ) {
                    Innkeeper ik = new Innkeeper(parent, oDBRef, oName, oFlags, oDesc, "Merchant", "VEN", oLocation, new String[]{"1000", "1000", "1000", "1000"} );

                    log.debug("log.debug (db entry): " + ik.toDB(), 2);
                    log.debug("Innkeeper", 2);

                    objectDB.add(ik);
                    objectDB.addNPC(ik);
                }
                //Exit(String tempName, String tempFlags, String tempDesc, int tempLoc, int tempDBREF, int tempDestination)
                else if (oFlags.equals("E"))
                {
                    int eType = Integer.parseInt(attr[6]);

                    ExitType et = ExitType.values()[eType];

                    if (et == ExitType.STD) {
                        int oDest = Integer.parseInt(attr[5]);

                        Exit exit = new Exit(oDBRef, oName, oFlags, oDesc, oLocation, oDest);
                        exit.setExitType(et);

                        log.debug("log.debug (db entry): " + exit.toDB(), 2);

                        objectDB.add(exit);
                        objectDB.addExit(exit);

                    }
                    else if (et == ExitType.PORTAL) {
                        Portal portal;

                        String oPortalType = attr[6];

                        // here we assume a typed but unkeyed portal
                        //public Portal(PortalType pType, int pOrigin, int[] pDestinations) 
                        if (oPortalType.equals("S")) { // Standard Portal
                            int oDestination = Integer.parseInt(attr[5]);

                            portal = new Portal(PortalType.STD, oLocation, oDestination);
                            portal.setExitType(et);
                            
                            portal.name = attr[1];            // name
                            portal.coord.setX(1);             // x coordinate
                            portal.coord.setY(1);             // y coordinate
                            
                            objectDB.add(portal);
                            objectDB.addExit(portal);
                        }
                        else if (oPortalType.equals("R")) { // Random Portal
                            int[] oDestinations = Utils.stringsToInts(attr[5].split(",")); 

                            portal = new Portal(PortalType.RANDOM, oLocation, oDestinations);
                            portal.setExitType(et);
                            
                            portal.name = attr[1];            // name
                            portal.coord.setX(1);             // x coordinate
                            portal.coord.setY(1);             // y coordinate

                            objectDB.add(portal);
                            objectDB.addExit(portal);
                        }
                    }
                }
                // NPC(int tempDBRef, String tempName, String tempDesc, int tempLoc, String tempTitle)
                else if (oFlags.equals("N")) {
                    //NPC npc = new NPC(oDBRef, oName, oDesc, oLocation, "npc");
                    NPC npc = loadNPC(oInfo);
                    npc.setCName("npc");

                    npc.setQuestList();
                    npc.addQuest(new Quest("Test", "Test", new Task("Test")));

                    log.debug("log.debug (db entry): " + npc.toDB(), 2);

                    objectDB.add(npc);
                    objectDB.addNPC(npc);
                }
                //Room(String tempName, String tempFlags, String tempDesc, int tempParent, int tempDBREF)
                else if (oFlags.indexOf("R") == 0)
                {
                    String roomType = attr[5];

                    Room room;
                    room = new Room(oDBRef, oName, oFlags, oDesc, oLocation);


                    log.debug("log.debug (db entry): " + room.toDB(), 2);

                    room.setRoomType(roomType);

                    if (room.getRoomType().equals("O")) {
                        room.getProps().put("sky", "The sky is clear and flecked with stars.");
                    }

                    objectDB.add(room);
                    objectDB.addRoom(room);
                }
                //Thing(String tempName, String tempFlags, String tempDesc, int tempLoc, int tempDBREF)
                else if (oFlags.indexOf("T") != -1)
                {
                    final Thing thing = new Thing(oDBRef, oName, oFlags, oDesc, oLocation);

                    log.debug("log.debug (db entry): " + thing.toDB(), 2);

                    objectDB.add(thing);
                    objectDB.addThing(thing);
                }
                else if (oFlags.indexOf("I") == 0) { // 
                    int itemType = Integer.parseInt(attr[5]); // get the type of item it should be
                    ItemType it = ItemType.values()[itemType];

                    if ( it == ItemType.CLOTHING ) { // Clothing
                        int clothingType = Integer.parseInt(attr[6]);
                        int mod = Integer.parseInt(attr[7]);

                        Clothing clothing = new Clothing(oName, oDesc, oLocation, oDBRef, mod, ClothingType.values()[clothingType]);
                        clothing.item_type = it;

                        objectDB.add(clothing);
                        objectDB.addItem(clothing);

                        log.debug("log.debug (db entry): " + clothing.toDB(), 2);
                    }

                    if ( it == ItemType.WAND ) { // Wand
                        String spellName = attr[6];
                        int charges = Integer.parseInt(attr[7]);

                        Wand wand = new Wand(oName, oDesc, oLocation, oDBRef, ItemType.values()[itemType], charges, spellName);
                        //wand.item_type = it; // unnecessary

                        objectDB.add(wand);
                        objectDB.addItem(wand);

                        log.debug("log.debug (db entry): " + wand.toDB(), 2);
                    }

                    if ( it == ItemType.WEAPON ) { // Weapon Merchant
                        int weaponType = Integer.parseInt(attr[6]);
                        int mod = Integer.parseInt(attr[7]);

                        Weapon weapon = new Weapon(oName, oDesc, oLocation, oDBRef, mod, Handed.ONE, WeaponType.values()[weaponType], 15.0);
                        weapon.item_type = it;

                        objectDB.add(weapon);
                        objectDB.addItem(weapon);
                        
                        log.debug("log.debug (db entry): " + weapon.toDB(), 2);
                    }

                    else if ( it == ItemType.ARMOR ) { // Armor Merchant
                        int armorType = Integer.parseInt(attr[6]);
                        int mod = Integer.parseInt(attr[7]);

                        Armor armor = new Armor(oName, oDesc, (int) oLocation, (int) oDBRef, mod , ArmorType.values()[armorType], ItemType.values()[itemType]);
                        armor.item_type = it;

                        objectDB.add(armor);
                        objectDB.addItem(armor);

                        log.debug("log.debug (db entry): " + armor.toDB(), 2);
                    }

                    if ( it == ItemType.ARROW ) { // Arrow
                        int stackID = Integer.parseInt(attr[7]);

                        Arrow arrow = new Arrow(oDBRef, oName, oDesc, oLocation);
                        arrow.item_type = it;

                        objectDB.add(arrow);
                        objectDB.addItem(arrow);

                        log.debug("log.debug (db entry): " + arrow.toDB(), 2);
                    }

                    if ( it == ItemType.BOOK ) { // Book
                        String author = attr[6];
                        String title = attr[7];
                        int pages = Integer.parseInt(attr[8]);

                        Book book = new Book(oName, oDesc, oLocation, oDBRef);
                        book.item_type = it;

                        book.setAuthor(author);
                        book.setTitle(title);
                        book.setPageNum(0);

                        objectDB.add(book);
                        objectDB.addItem(book);

                        log.debug("log.debug (db entry): " + book.toDB(), 2);
                    }

                    if ( it == ItemType.POTION ) {
                        int stack_size = Integer.parseInt(attr[6]);
                        String sn = attr[7];

                        /*
                         * whatever I do here needs to recreate the entirety
                         * of a stack of potions correctly
                         */

                        Potion potion = new Potion(oDBRef, oName, "I", oDesc, oLocation, sn);

                        for (int i = 1; i < stack_size; i++) {
                            Potion potion1 = new Potion(oDBRef, oName, "I", oDesc, oLocation, sn);
                            potion.item_type = ItemType.POTION;

                            potion.stack(potion1);
                        }

                        objectDB.add(potion);
                        objectDB.addItem(potion);

                        log.debug("log.debug (db entry): " + potion.toDB(), 2);
                    }
                }
                else if (oFlags.equals("null")) {

                    NullObject Null = new NullObject(oDBRef);

                    //log.debug("Found NULLObject");
                    log.debug("log.debug (db entry): " + Null.toDB() + " [Found NULLObject]", 2);

                    objectDB.add(new NullObject(oDBRef));
                }
            }
            catch(ConcurrentModificationException cme) {
                cme.printStackTrace();
            }
            catch(ArrayIndexOutOfBoundsException aioobe) {
                aioobe.printStackTrace();
            }
		}
	}

	/**
	 * Generate a player from it's database representation
	 * 
	 * NOTE: for testing purposes only now, init_conn doesn't go through
	 * loadObjects, which is pointless when you consider that I only hold onto a copy
	 * of the objects and it never goes into the player's array.
	 * 
	 * NOTE2: meant to solve a problem where I haven't copied the load code into init_conn,
	 * but want a properly initialized/loaded player for existing characters when they login
	 * 
	 * @param playerData
	 * @return a player object
	 */
	static private NPC loadNPC(String npcData) {

		String[] attr = npcData.split("#");

		Integer oDBRef = 0, oLocation = 0;
		String oName = "", oFlags = "", oDesc = "";
		String[] os, om;

		oDBRef = Integer.parseInt(attr[0]);    // 0 - npc database reference number
		oName = attr[1];                       // 1 - npc name
		oFlags = attr[2];                      // 2 - npc flags
		oDesc = attr[3];                       // 3 - npc description
		oLocation = Integer.parseInt(attr[4]); // 4 - npc location

		// 5 - npc doesn't have a password
		os = attr[6].split(",");               // 6 - npc stats
		om = attr[7].split(",");               // 7 - npc money
		int access;                            // 8 - npc permissions
		int raceNum;                           // 9 - npc race number (enum ordinal)
		int classNum;                          // 10 - npc class number (enum ordinal)

		/*debug("Database Reference Number: " + oDBRef);
		debug("Name: " + oName);
		debug("Flags: " + oFlags);
		debug("Description: " + oDesc);
		debug("Location: " + oLocation);*/

		Integer[] oStats = Utils.stringsToIntegers(os);
		Integer[] oMoney = Utils.stringsToIntegers(om);


		NPC npc = new NPC(oDBRef, oName, oFlags, oDesc, oLocation, "", "IC", oStats, oMoney); 

		// Set NPC Race
		try {
			raceNum = Integer.parseInt(attr[9]);
			npc.setPlayerRace(Races.getRace(raceNum));
		}
		catch(NumberFormatException nfe) {
			nfe.printStackTrace();
			npc.setPlayerRace(Races.NONE);
		}

		// Set NPC Class
		try {
			classNum = Integer.parseInt(attr[10]);
			npc.setPClass(Classes.getClass(classNum));
		}
		catch(NumberFormatException nfe) {
			nfe.printStackTrace();
			npc.setPClass(Classes.NONE);
		}

		return npc;
	}

}
