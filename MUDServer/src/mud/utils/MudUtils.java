package mud.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import mud.Constants;
import mud.MUDObject;
import mud.ObjectFlag;
import mud.TypeFlag;
import mud.combat.WeaponType;
import mud.game.Coins;
import mud.magic.Spell;
import mud.misc.BBEntry;
import mud.misc.BulletinBoard;
import mud.misc.Direction;
import mud.misc.Effect;
import mud.misc.Slot;
import mud.misc.Zone;
import mud.objects.Exit;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.exits.Portal;
import mud.objects.exits.PortalType;
import mud.objects.items.Container;
import mud.objects.items.Weapon;

public final class MudUtils {
	public static double calculateWeight(final Coins money) {
		double weight = 0.0;

		final int[] coins = money.toArray();

		weight += coins[0] * ( 0.0625 );  // copper (1/16 oz./coin)
		weight += coins[1] * ( 0.1250 );  // silver (1/8 oz./coin)
		weight += coins[2] * ( 0.2500 );  // gold (1/4 oz./coin)
		weight += coins[3] * ( 0.5000 );  // platinum (1/2 oz./coin)

		return ( weight / 16 ); // 16 oz = 1 lb

		// in order to set weight of a coin I need to establish values and relative values
		// i.e. 100 copper = 1 silver, 100 silver = 1 gold, 100 gold = 1 platinum
		// need to establish density of each material, and then calculate weight
		// weight of coin: amount of material (g) -> amount of material (oz).
		// copper coin = 4.5g
		// silver coin = 4.5g
		// gold coin = 4.5g
		// platinum coin = 4.5g
		//send(gramsToOunces(4.5));

		/*final int[] coins = player.getMoney().toArray();
				weight += coins[0] * ( 0.0625 );  // copper (1/16 oz./coin)
				weight += coins[1] * ( 0.1250 );  // silver (1/8 oz./coin)
				weight += coins[2] * ( 0.2500 );  // gold (1/4 oz./coin)
				weight += coins[3] * ( 0.5000 );  // platinum (1/2 oz./coin)
				return ( weight / 16 ); // 16 oz = 1 lb*/
	}

	public static double calculateWeight(final Player player) {
		double weight = 0.0;

		for (final Item item : player.getInventory())
		{
			// if it's a container, ask it how heavy it is
			// (this is calculated by the bag -- hence bags of holding)
			if (item != null) {
				weight += item.getWeight();
			}
		}

		return weight;
	}

	public static String getTypeName(final TypeFlag tFlag) {
		return tFlag.name();
	}

	public static String flagsToString(final EnumSet<ObjectFlag> flags) {
		final StringBuilder buf = new StringBuilder();

		for (final ObjectFlag f : flags) {
			buf.append( f.toString().charAt(0) );
		}

		return buf.toString();
	}

	public static final String listEffects(final MUDObject object) {
		String effectList = "Effects: ";
		String sep = ", ";

		Integer count = 0;

		// assumes that nothing is changed while i'm iterating...
		final Integer numEffects = object.getEffects().size();

		for (final Effect effect : object.getEffects()) {
			if (numEffects - 1 == count) sep = "";

			effectList += effect + sep;

			count++;
		}

		return effectList;
	}
	
	public static Direction getRandomDirection(final Direction lastDir) {
		final List<Direction> directions = Utils.mkList(
				Direction.NORTH,     Direction.SOUTH,
				Direction.EAST,      Direction.WEST,
				Direction.NORTHEAST, Direction.NORTHWEST,
				Direction.SOUTHEAST, Direction.SOUTHWEST);

		final Random rDir = new Random();		

		int n = rDir.nextInt( directions.size() );

		final Direction temp = directions.get(n);

		if( temp == lastDir ) {
			return getRandomDirection(lastDir);
		}
		else {
			return temp;
		}
	}

	/**
	 * Determine if the specified ObjectFlag can be applied
	 * to the specified object type (indicated by TypeFlag).
	 * <br><br>
	 * 
	 * pulls a list of allowed types from the flag itself.
	 * 
	 * @param of ObjectFlag
	 * @param tf TypeFlag
	 * @return
	 */
	public static boolean isAllowed(final ObjectFlag of, final TypeFlag tf) {
		final List<TypeFlag> flags = of.getAllowedTypes();
		
		if( flags.contains(tf) || flags.contains(TypeFlag.OBJECT) ) return true;
		else                                                        return false;
	}

	// utility function
	public static void addRoomToZone(final Zone zone, final Room room) {
		if( zone != null && room != null ) {
			zone.addRoom( room );
			room.setZone( zone );
		}
	}

	/*public static void addItemToRoom(final Room room, final Item item) {
		if( room != null && item != null ) {
			item.setLocation(room.getDBRef());
			room.addItem(item);
		}
	}*/

	public static List<String> scan(final BulletinBoard bb) {
		List<String> out = null;

		if(bb != null) {
			out = new ArrayList<String>(bb.getNumMessages() + 2);

			for (final BBEntry entry : bb.getEntries()) {
				//out.add("| " + entry.toView() + " |");
				out.add( String.format("| %s |", entry.toView()) );
			}
		}
		
		return out;
	}

	public static boolean validateEmailAddress(final String emailAddress) {
		// matches against *@*.* | ex: test@gmail.com
		return emailAddress.matches("([\\w-+]+(?:\\.[\\w-+]+)*@(?:[\\w-]+\\.)+[a-zA-Z]{2,7})");
	}
	
	// container stuff
	public static String getTop(final Container c) {
		return Utils.padRight("", Container.top, c.getDisplayWidth());
	}
	
	public static String getSide() {
		return "" + Container.side;
	}
	
	public static String getBottom(final Container c) {
		return Utils.padRight("", Container.bottom, c.getDisplayWidth());
	}
	
	/**
	 * Checks access/permissions level against the queried access and returns
	 * true or false depending on whether they meet/exceed the specified access
	 * level or not. If the client is associated with a player, then the
	 * player's access is checked, otherwise it's checked against 0 (basic user
	 * permissions).
	 * 
	 * @param client
	 * @param accessLevel
	 * 
	 * @return
	 */
	public static boolean checkAccess(final Player player, final int accessLevel) {
		int check = Constants.USER;

		if (player != null) {
			check = player.getAccess();
		}

		return check >= accessLevel;
	}

	public static String getIdleString(int idle_time) {
		String idle_string = "";

		if ( idle_time > 0 ) {
			if ( idle_time > 60 ) idle_string = (idle_time / 60) + "m" + (idle_time % 60) + "s";
			else                  idle_string = idle_time + "s";
		}
		else idle_string = "----";

		return idle_string;
	}
	
	/**
	 * Find the Item with the specified name in the list of Items provided, if
	 * it's there.
	 * 
	 * @param itemName
	 * @param items
	 * 
	 * @return
	 */
	public static Item findItem(final String itemName, final List<Item> items) {
		return findItem(itemName, items, false);
	}
	
	/**
	 * Find the Item with the specified name in the list of Items provided, if
	 * it's there.
	 * 
	 * @param itemName
	 * @param items
	 * 
	 * @return
	 */
	public static Item findItem(final String itemName, final List<Item> items, final boolean exact) {
		/** Extracted from MUDServer **/
		final String arg = itemName;
		final String arg_lc = arg.toLowerCase();
		
		Item item = null;
		
		for (final Item item1 : items) {
			// if there is a name or dbref match from the argument in the inventory
			// if the item name exactly equals the arguments or the name
			// contains the argument (both case-sensitive), or
			// if the dbref is correct

			final String name = item1.getName();
			final String name_lc = name.toLowerCase();
			
			final List<String> components = Arrays.asList(name_lc.split(" "));
			
			System.out.println("Argument:              " + arg);
			System.out.println("Name:                  " + name);
			System.out.println("Argument (Lower Case): " + arg_lc);
			System.out.println("Name (Lower Case):     " + name_lc);
			System.out.println("Components:            " + components);

			/*
			 * 1) is the name the same as ARG (ignoring case -- setting both name and arg to lowercase)
			 * 2) does the name start with ARG (ignoring case -- setting both name and arg to lowercase)
			 * 3) does the name end with ARG (ignoring case -- setting both name and arg to lowercase)
			 * 4) does the name contain ARG (ignoring case -- setting both name and arg to lowercase)
			 * 5) is any component of the name the same as the arg (continues non-whitespace separated segments)
			 */

			boolean sameName = name.equalsIgnoreCase(arg) || name_lc.equals(arg_lc);
			boolean startsWith = name_lc.startsWith(arg_lc);
			boolean endsWith = name_lc.endsWith(arg_lc);
			boolean nameContains = name_lc.contains(arg_lc);
			boolean compsContain = exact ? false : components.contains(arg_lc);

			boolean test = false;

			for (final String s : components) {
				for (final String s1 : Arrays.asList(arg.toLowerCase().split(" "))) {
					if (s.contains(s1) && !exact) test = true;
					break;
				}
			}
			
			// for string in A, is A.S a substring of string name N.S
			if ( sameName || startsWith || endsWith || nameContains || compsContain || test ) {
				item = item1;
				
				break;
			}
		}

		return item;
	}
	
	/**
	 * Find the Item with the specified dbref in the list of Items provided, if
	 * it's there.
	 * @param itemDBRef
	 * @param items
	 * 
	 * @return
	 */
	public static Item findItem(final Integer itemDBRef, final List<Item> items) {
		Item item = null;

		for (final Item item1 : items) {
			final String itemName = item1.getName();

			if ( item1.getDBRef() == itemDBRef ) {
				System.out.println(itemName + " true");

				item = item1;
				
				break;
			}
		}

		return item;
	}
	
	public static List<MUDObject> filter(final List<MUDObject> objects, final TypeFlag tf) {
		final List<MUDObject> filtered = new LinkedList<MUDObject>();

		for(final MUDObject m : objects) {
			if( m.isType(tf) ) {
				filtered.add(m);
			}
		}

		return filtered;
	}

	public static List<Item> filter(final List<Item> objects, final ItemType iType) {
		final List<Item> filtered = new LinkedList<Item>();

		for(final Item item : objects) {
			if( item.getItemType() == iType ) {
				filtered.add(item);
			}
		}

		return filtered;
	}
	
	private static List<String> getNames(final List<? extends MUDObject> list) {
		List<String> l = new ArrayList<String>(list.size());

		for (final MUDObject m : list) {
			l.add(m.getName());
		}

		return l;
	}
	
	// standard, "always open" and one-way portal (default is active)
	public Portal createPortal(int pOrigin, int pDestination) {
		return new Portal(PortalType.STD, pOrigin, pDestination);
	}

	// standard, "always open" and one-way portal (default is active) -- multi
	// destination
	public Portal createPortal(int pOrigin, int[] pDestinations) {
		return new Portal(PortalType.STD, pOrigin, pDestinations);
	}
	
	// keyed portal (default is inactive)
	public Portal createPortal(int pOrigin, int pDestination, final Object pKey) {
		if (pKey != null) {
			return new Portal(PortalType.STD, pOrigin, new int[] { pDestination }, pKey); 
		}
		else return null;
	}
	
	// generate a dungeon as a rectangular grid of rooms (specific width and length)
		private void generateDungeon(String entranceDir, int startX, int startY, int xRooms, int yRooms) {
			List<Room> dRooms = new ArrayList<Room>(xRooms * yRooms);

			// list of room/hall? types
			// array of room types
			// generate room based on room types (adding exits as necessary
			// do a second pass over generated rooms to link them together

			Room r = null;

			for (int n = 0; n < xRooms * yRooms; n++) {
				r = new Room();
				r.setName("A Room");

				dRooms.add(r);
			}

			Random rng = new Random();

			for (final Room room : dRooms) {
				int numExits = rng.nextInt(3) + 1; // 4 - N,S,E,W
				int countEE = 0; // exits that enter this room

				for (final Exit exit : room.getExits()) {
					int d = exit.getDestination();

					if (d != -1) {

					}
				}
			}
		}
		
		// is TARGET a valid target for SPELL cast by PLAYER
		public static boolean isValidTarget(final MUDObject target, final Spell spell, final Player player) {
			/*
			 * NOTES
			 * If the spell is an area affect spell, then not having a target is valid, as  is having a target.
			 * 
			 * The spell having no target means that the spell will hit a general area somewhere in front of you,
			 * you, otherwise the spell hits the target and radiates out from their position.
			 */

			List<String> targets = Arrays.asList(Spell.decodeTargets(spell).split(","));

			System.out.println("Targets: " + targets);

			if (target instanceof Player) {
				Player player1 = (Player) target;

				// determine whether the target player is hostile or friendly with regard to
				// the caster

				// I'd like a better method that uses numerical equivalents
				if (targets.contains("self")) {
					if (player == player1)
						return true;

					return false;
				}
				else if (targets.contains("enemy")) {
					if (player != player1)
						return false;
					else {
						return false;
					}
				}
				else if (targets.contains("friend")) {
					if (player != player1)
						return false;
					else {
						return false;
					}
				}
				else {
					return false;
				}
			}
			else {
				return true; // assuming that the spell can target any non-player object, regardless
			}
		}
}