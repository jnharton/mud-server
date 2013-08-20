package mud;

import java.util.*;

import mud.objects.*;
import mud.objects.items.*;
import mud.objects.things.Chest;
import mud.quest.*;

import mud.Coins;
import mud.MUDObject;
import mud.objects.Item;
import mud.utils.Utils;
import mud.magic.Spell;
import mud.misc.Zone;
import mud.net.Client;

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

	static public void loadObjects(final List<String> in, final LoggerI log, final ObjectDB objectDB, 
            final MUDServer parent)
	{
		for (final String oInfo : in)
		{	
			Integer oDBRef = 0, oLocation = 0;
			String oName = "", oFlags = "", oDesc = "";
			
			if ( oInfo.charAt(0) == '&' ) { // means to ignore that line
				log.debug("`loadObjects` ignoring line: " + oInfo);
				oDBRef = Integer.parseInt(oInfo.split("#")[0].replace('&', ' ').trim());
				NullObject no = new NullObject(oDBRef);
				no.lock(); // lock the NullObject
				objectDB.add(no);
				continue;
			}

            try {
                String[] attr = oInfo.split("#");
                oDBRef = Integer.parseInt(attr[0]);
                oName = attr[1];
                oFlags = attr[2];
                oDesc = attr[3];
                oLocation = Integer.parseInt(attr[4]);

                log.debug("Database Reference Number: " + oDBRef);
                log.debug("Name: " + oName);
                log.debug("Flags: " + oFlags);
                log.debug("Description: " + oDesc);
                log.debug("Location: " + oLocation);

                if (oFlags.indexOf("C") == 0) {
                    final Creature cre = new Creature(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);

                    // set race
                    try {
                        cre.setRace( Races.getRace(Integer.parseInt(attr[9])) );    // 9 - race number (enum ordinal)
                    }
                    catch(NumberFormatException nfe) {
                        nfe.printStackTrace();
                        cre.setRace( Races.NONE );
                    }

                    // add the creature to the in-memory database and to the list of creatures
                    objectDB.add(cre);
                    objectDB.addCreature(cre);
                }
                else if (oFlags.indexOf("P") == 0) {
                	Player player = loadPlayer( oInfo );
                	
                	//log.debug("log.debug (db entry): " + player.toDB(), 2);

                    objectDB.add(player);
                    objectDB.addPlayer(player);
                }
                else if (oFlags.equals("WMV")) {
                    WeaponMerchant wm = new WeaponMerchant(parent, oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), "A weapon merchant.", "Merchant", "VEN", 161, Coins.fromArray(new int[] {1000, 1000, 1000, 1000}));

                    log.debug("log.debug (db entry): " + wm.toDB(), 2);
                    log.debug("Weapon Merchant", 2);

                    objectDB.add(wm);
                    objectDB.addNPC(wm);
                }
                else if (oFlags.equals("AMV")) {
                    ArmorMerchant am = new ArmorMerchant(parent, oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), "An armor merchant.", "Merchant", "VEN", 161, Coins.fromArray(new int[] {1000, 1000, 1000, 1000}));

                    log.debug("log.debug (db entry): " + am.toDB(), 2);
                    log.debug("Armor Merchant", 2);

                    objectDB.add(am);
                    objectDB.addNPC(am);
                }
                else if (oFlags.equals("IKV") ) {
                    Innkeeper ik = new Innkeeper(parent, oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, "Merchant", "VEN", oLocation, Coins.fromArray(new int[] {1000, 1000, 1000, 1000}));

                    log.debug("log.debug (db entry): " + ik.toDB(), 2);
                    log.debug("Innkeeper", 2);

                    objectDB.add(ik);
                    objectDB.addNPC(ik);
                }
                //Exit(String tempName, String tempFlags, String tempDesc, int tempLoc, int tempDBREF, int tempDestination)
                else if (oFlags.indexOf("E") == 0)
                {
                    int eType = Integer.parseInt(attr[6]);

                    ExitType et = ExitType.values()[eType];

                    if (et == ExitType.STD) {
                        int oDest = Integer.parseInt(attr[5]);

                        Exit exit = new Exit(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, oDest);
                        exit.setExitType(et);

                        log.debug("log.debug (db entry): " + exit.toDB(), 2);

                        objectDB.add(exit);
                        objectDB.addExit(exit);

                    }
                    else if( et == ExitType.DOOR ) {
                    	int oDest = Integer.parseInt(attr[5]);
                    	int lockState = Integer.parseInt(attr[6]);

                        Exit exit = new Exit(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, oDest);
                        exit.setExitType(et);
                        
                        if( lockState == 1 ) { exit.lock(); }
                        else { exit.unlock(); }

                        log.debug("log.debug (db entry): " + exit.toDB(), 2);

                        objectDB.add(exit);
                        objectDB.addExit(exit);
                    }
                    else if (et == ExitType.PORTAL) {
                        Portal portal;
                        
                        int pType = Utils.toInt(attr[7], -1);
                        PortalType oPortalType = PortalType.values()[pType];

                        // here we assume a typed but unkeyed portal
                        //public Portal(PortalType pType, int pOrigin, int[] pDestinations) 
                        if (oPortalType == PortalType.STD) { // Standard Portal
                            int oDestination = Integer.parseInt(attr[5]);
                            
                            portal = new Portal(PortalType.STD, oLocation, oDestination);
                            portal.setDBRef(oDBRef);          // NOTE: ought to handle this in the constructor
                            portal.setExitType(et);
                            portal.setDesc(oDesc);
                            
                            portal.name = attr[1];            // name
                            portal.coord.setX(0);             // x coordinate
                            portal.coord.setY(0);             // y coordinate
                            
                            log.debug("log.debug (db entry): " + portal.toDB(), 2);
                            
                            portal.setKey("test");
                            
                            parent.getPortals().add(portal);
                            parent.getRoom(portal.getOrigin()).addSayEventListener(portal);
                            parent.getRoom(portal.getDestination()).addSayEventListener(portal);
                            
                            objectDB.add(portal);
                            objectDB.addExit(portal);
                        }
                        else if (oPortalType == PortalType.RANDOM) { // Random Portal
                            int[] oDestinations = Utils.stringsToInts(attr[5].split(",")); 

                            portal = new Portal(PortalType.RANDOM, oLocation, oDestinations);
                            portal.setDBRef(oDBRef);          // NOTE: ought to handle this in the constructor
                            portal.setExitType(et);
                            
                            portal.name = attr[1];            // name
                            portal.coord.setX(0);             // x coordinate
                            portal.coord.setY(0);             // y coordinate
                            
                            portal.setKey("test");
                            
                            log.debug("log.debug (db entry): " + portal.toDB(), 2);
                            
                            parent.getPortals().add(portal);
                            parent.getRoom(portal.getOrigin()).addSayEventListener(portal);
                            
                            objectDB.add(portal);
                            objectDB.addExit(portal);
                        }
                        else {
                        	log.debug("log.debug (error): Problem with object #" + oDBRef +  " - invalid PortalType", 2);
                        }
                    }
                    else {
                    	log.debug("log.debug (error): Problem with object #" + oDBRef, 2);
                    }
                }
                // NPC(int tempDBRef, String tempName, String tempDesc, int tempLoc, String tempTitle)
                else if (oFlags.indexOf("N") == 0) {
                    //NPC npc = new NPC(oDBRef, oName, oDesc, oLocation, "npc");
                    NPC npc = loadNPC(oInfo);
                    npc.setCName("npc");

                    //npc.addQuest(new Quest("Test", "Test", new Task("Test")));

                    log.debug("log.debug (db entry): " + npc.toDB(), 2);

                    objectDB.add(npc);
                    objectDB.addNPC(npc);
                }
                //Room(String tempName, String tempFlags, String tempDesc, int tempParent, int tempDBREF)
                else if (oFlags.indexOf("R") == 0)
                {
                    String roomType = attr[5];

                    final Room room = new Room(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);

                    log.debug("log.debug (db entry): " + room.toDB(), 2);

                    room.setRoomType(RoomType.fromLetter(roomType.charAt(0)));
                    
                    // set room dimensions
                    int[] dimensions = Utils.stringsToInts(attr[6].split(","));
                    
                    room.x = dimensions[0];
                    room.y = dimensions[1];
                    room.z = dimensions[2];
                    
                    // create zones where flags exist
                    if( oFlags.contains("Z") ) {
                    	parent.zones.put(new Zone("zone", room), 1);
                    }

                    if (room.getRoomType().equals(RoomType.OUTSIDE)) {
                        room.getProps().put("sky", "The sky is clear and flecked with stars.");
                    }

                    objectDB.add(room);
                    objectDB.addRoom(room);
                }
                //Thing(String tempName, String tempFlags, String tempDesc, int tempLoc, int tempDBREF)
                else if (oFlags.indexOf("T") == 0)
                {
                	int tType = Utils.toInt(attr[5], 0); // find a type, else TYPE 0 (NONE)
                    ThingType tt = ThingType.values()[tType];
                    
                    final Thing thing;
                    
                    if( tt == ThingType.CHEST ) {
                    	thing = new Chest(oDBRef, oName, oDesc, oLocation);
                    	//thing.thing_type = ThingType.CHEST; // shouldn't be needed
                    }
                    else {
                    	thing = new Thing(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);
                    }

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

                        final Clothing clothing = new Clothing(oDBRef, oName, oDesc, oLocation, mod, ClothingType.values()[clothingType]);
                        clothing.setItemType(it);

                        objectDB.add(clothing);
                        objectDB.addItem(clothing);

                        log.debug("log.debug (db entry): " + clothing.toDB(), 2);
                    }

                    if ( it == ItemType.WAND ) { // Wand
                        String spellName = attr[6];
                        int charges = Integer.parseInt(attr[7]);
                        
                        Spell spell = parent.getSpell(spellName);

                        Wand wand = new Wand(oName, oDesc, oLocation, oDBRef, ItemType.values()[itemType], charges, spell);
                        //wand.item_type = it; // unnecessary

                        objectDB.add(wand);
                        objectDB.addItem(wand);

                        log.debug("log.debug (db entry): " + wand.toDB(), 2);
                    }

                    if ( it == ItemType.WEAPON ) { // Weapon Merchant
                        int weaponType = Integer.parseInt(attr[6]);
                        int mod = Integer.parseInt(attr[7]);

                        Weapon weapon = new Weapon(oName, oDesc, oLocation, oDBRef, mod, Handed.ONE, WeaponType.values()[weaponType], 15.0);
                        weapon.setItemType(it);

                        objectDB.add(weapon);
                        objectDB.addItem(weapon);
                        
                        log.debug("log.debug (db entry): " + weapon.toDB(), 2);
                    }

                    else if ( it == ItemType.ARMOR ) { // Armor Merchant
                        int armorType = Integer.parseInt(attr[6]);
                        int mod = Integer.parseInt(attr[7]);

                        Armor armor = new Armor(oName, oDesc, (int) oLocation, (int) oDBRef, mod , ArmorType.values()[armorType], ItemType.values()[itemType]);
                        armor.setItemType(it);

                        objectDB.add(armor);
                        objectDB.addItem(armor);

                        log.debug("log.debug (db entry): " + armor.toDB(), 2);
                    }

                    if ( it == ItemType.ARROW ) { // Arrow
                        int stackID = Integer.parseInt(attr[7]);

                        Arrow arrow = new Arrow(oDBRef, oName, oDesc, oLocation);
                        arrow.setItemType(it);

                        objectDB.add(arrow);
                        objectDB.addItem(arrow);

                        log.debug("log.debug (db entry): " + arrow.toDB(), 2);
                    }

                    if ( it == ItemType.BOOK ) { // Book
                        String author = attr[6];
                        String title = attr[7];
                        int pages = Integer.parseInt(attr[8]);

                        Book book = new Book(oName, oDesc, oLocation, oDBRef);
                        book.setItemType(it);

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

                        Potion potion = new Potion(oDBRef, oName, EnumSet.of(ObjectFlag.ITEM), oDesc, oLocation, sn);

                        for (int i = 1; i < stack_size; i++) {
                            Potion potion1 = new Potion(oDBRef, oName, EnumSet.of(ObjectFlag.ITEM), oDesc, oLocation, sn);
                            potion.setItemType(ItemType.POTION);

                            potion.stack(potion1);
                        }

                        objectDB.add(potion);
                        objectDB.addItem(potion);

                        log.debug("log.debug (db entry): " + potion.toDB(), 2);
                    }
                    
                    else if ( it == ItemType.SHIELD ) { // Armor Merchant
                        int shieldType = Integer.parseInt(attr[6]);
                        int mod = Integer.parseInt(attr[7]);
                        
                        Shield shield = new Shield(oName, oDesc, oLocation, oDBRef, mod, it, ShieldType.values()[shieldType]);
                        
                        objectDB.add(shield);
                        objectDB.addItem(shield);

                        log.debug("log.debug (db entry): " + shield.toDB(), 2);
                    }
                    
                    else if ( it == ItemType.RING ) {
                    	
                    	Item ring = new Item(oDBRef, oName, EnumSet.of(ObjectFlag.ITEM), oDesc, oLocation);
                    	
                    	ring.setItemType( ItemType.RING );
                    	
                    	objectDB.add(ring);
                    	objectDB.addItem(ring);
                    }
                }
                else if (oFlags.contains("null")) {

                    NullObject Null = new NullObject(oDBRef);
                    
                    objectDB.add(Null);

                    log.debug("log.debug (db entry): " + Null.toDB() + " [Found NULLObject]", 2);
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

	/* loadPlayer is probably redundant with loadNPC to some extent and doesn't seem to be used, but just moving
	 * it to here for now.
	 * 
	 * ~jnharton
	 */
	
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
		String oFlags = attr[2];
		String oDesc = attr[3];
		Integer oLocation = Utils.toInt(attr[4], Constants.WELCOME_ROOM);

		/*debug("Database Reference Number: " + oDBRef);
		debug("Name: " + oName);
		debug("Flags: " + oFlags);
		debug("Description: " + oDesc);
		debug("Location: " + oLocation);*/
		
		String oPassword = attr[5];

		Integer[] oStats = Utils.stringsToIntegers( attr[6].split(",") );
		int[] oMoney = Utils.stringsToInts( attr[7].split(",") );

		Player player = new Player(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, "", oPassword, "IC", oStats, Coins.fromArray(oMoney));
		
		int access, raceNum, classNum, player_status;
		
		/* Set Player Permissions */
		access = Utils.toInt(attr[8], Constants.USER);
		player.setAccess(access);

		/* Set Player Race */
		raceNum = Utils.toInt(attr[9], 9);
		player.setPlayerRace(Races.getRace(raceNum));

		/* Set Player Class */
		classNum = Utils.toInt(attr[10], 0);
		player.setPClass(Classes.getClass(classNum));
		
		/* Set Status */
        player.setStatus(attr[11]);
        
        /* Set Player Status */
        player_status = Utils.toInt(attr[12], Player.Status.LOCKED.ordinal());
        player.setPStatus(Player.Status.values()[player_status]);

		return player;
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

		int oDBRef = 0, oLocation = 0;
		String oName = "", oFlags = "", oDesc = "";
		String[] os, om;

		oDBRef = Integer.parseInt(attr[0]);    // 0 - npc database reference number
		oName = attr[1];                       // 1 - npc name
		oFlags = attr[2].substring(1);         // 2 - npc flags
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
		int[] oMoney = Utils.stringsToInts(om);		

        NPC npc = new NPC(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, "", "IC", oStats, Coins.fromArray(oMoney));

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