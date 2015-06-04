package mud.utils;

import java.util.EnumSet;

import mud.MUDObject;
import mud.ObjectFlag;
import mud.TypeFlag;
import mud.misc.Coins;
import mud.misc.Effect;
import mud.objects.Item;
import mud.objects.Player;
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

		// in order to set weight of a coin I need to establish values and relative values
		// i.e. 100 copper = 1 silver, 100 silver = 1 gold, 100 gold = 1 platinum
		// need to establish density of each material, and then calculate weight
		// weight of coin: amount of material (g) -> amount of material (oz).
		// copper coin = 4.5g
		// silver coin = 4.5g
		// gold coin = 4.5g
		// platinum coin = 4.5g
		//send(gramsToOunces(4.5));
		final int[] coins = player.getMoney().toArray();
		weight += coins[0] * ( 0.0625 );  // copper (1/16 oz./coin)
		weight += coins[1] * ( 0.1250 );  // silver (1/8 oz./coin)
		weight += coins[2] * ( 0.2500 );  // gold (1/4 oz./coin)
		weight += coins[3] * ( 0.5000 );  // platinum (1/2 oz./coin)
		return ( weight / 16 ); // 16 oz = 1 lb
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
		if( weapon == null ) {
			// TODO this really should return the character's ability to cause damage by themselves
			return 1;
		}
		else {
			final WeaponType wt = weapon.getWeaponType();

			if(wt != null ) {
				String damageRoll = wt.getDamage();

				if (critical) {
					return Utils.roll(damageRoll) * wt.getCritical();
				}
				else {
					return Utils.roll(damageRoll);
				}
			}
			else return weapon.damage;
		}
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
		
		if( weapon == null ) {
			weapon = (Weapon) player.getSlots().get("weapon").getItem();
		}
		
		if( weapon == null ) {
			weapon = (Weapon) player.getSlots().get("weapon1").getItem();
		}
		
		if( weapon == null ) {
			weapon = (Weapon) player.getSlots().get("weapon2").getItem();
		}
		
		return weapon;
	}
}