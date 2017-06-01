package mud.utils;

import mud.MUDObject;
import mud.misc.Slot;
import mud.objects.ItemTypes;
import mud.objects.Player;
import mud.objects.items.Weapon;

public final class CombatUtils {
	public static int calculateDamage(final Weapon weapon, final boolean critical) {
		int damage = 0;

		if( weapon == null ) {
			// TODO this really should return the character's ability to cause damage by themselves
			if( critical ) damage = 2;
			else           damage = 1;
		}
		else {

			//final String damageRoll = wtype.getDamage();

			if (critical) {
				//damage = Utils.roll(damageRoll) * wtype.getCritical();
				damage = weapon.getDamage() * weapon.getCritical();
			}
			else {
				//damage = Utils.roll(damageRoll);
				damage = weapon.getDamage();
			}
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
		else { // compare to AC of target?
			return (roll > 1);
			//return roll > 5;
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
}