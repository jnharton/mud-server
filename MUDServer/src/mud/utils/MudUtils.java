package mud.utils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import mud.Constants;
import mud.MUDObject;
import mud.ObjectFlag;
import mud.TypeFlag;
import mud.misc.Coins;
import mud.misc.Direction;
import mud.misc.Effect;
import mud.misc.Slot;
import mud.misc.Zone;
import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.items.Container;
import mud.objects.items.Weapon;
import mud.objects.items.WeaponType;

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

	public static int calculateDamage(final Weapon weapon, final boolean critical) {
		int damage = 0;
		
		if( weapon == null ) {
			// TODO this really should return the character's ability to cause damage by themselves
			if( critical ) damage = 2;
			else           damage = 1;
		}
		else {
			final WeaponType wtype = weapon.getWeaponType();

			if(wtype != null ) {
				final String damageRoll = wtype.getDamage();

				if (critical) {
					damage = Utils.roll(damageRoll) * wtype.getCritical();
				}
				else {
					damage = Utils.roll(damageRoll);
				}
			}
			else damage = weapon.damage;
		}
		
		return damage;
	}

	public static boolean canAttack(final MUDObject target) {
		if (target instanceof Player) {
			return canAttack( (Player) target );
		}

		return true;
	}

	public static boolean canAttack(final Player target) {
		return ( target.getState() != Player.State.DEAD );
	}

	public static boolean canHit(MUDObject Target) {
		int roll = Utils.roll(1, 20);

		if (roll == 1) { // Natural 1 (guaranteed miss)
			return false;
		}
		else if (roll == 20) { // Natural 20 (guaranteed hit)
			return true;
		}
		else { // compare to AC of target
			return roll > 5;
		}
	}

	public static Weapon getWeapon(final Player player) {
		Weapon weapon = null;

		/*if( weapon == null ) {
			weapon = (Weapon) player.getSlots().get("weapon").getItem();
		}

		if( weapon == null ) {
			weapon = (Weapon) player.getSlots().get("weapon1").getItem();
		}

		if( weapon == null ) {
			weapon = (Weapon) player.getSlots().get("weapon2").getItem();
		}*/

		if( weapon == null ) {
			// skim through slots and find first weapon
			for(final String k : player.getSlots().keySet()) {
				Slot s = player.getSlot(k);

				if( s.isType(ItemTypes.WEAPON) ) {
					if( s.isFull() ) {
						weapon = (Weapon) s.getItem();
						break;
					}
				}
			}
		}

		return weapon;
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
	 * isAllowed
	 * 
	 * Determine if the specified ObjectFlag can be applied
	 * to the specified object type (indicated by TypeFlag).
	 * 
	 * @param of
	 * @param tf
	 * @return
	 */
	public static boolean isAllowed(final ObjectFlag of, final TypeFlag tf) {
		if( of.getAllowedType() == tf ) {
			return true;
		}
		else return false;
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
			out = new ArrayList<String>();

			out.add(bb.getName());

			out.add("+------------------------------------------------------------------------------+");

			for (final BBEntry entry : bb.getEntries()) {
				out.add("| " + Utils.padRight(entry.toView(), 76) + " |");
			}

			out.add("+------------------------------------------------------------------------------+");
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
}