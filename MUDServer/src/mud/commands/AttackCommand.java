package mud.commands;

import mud.MUDObject;
import mud.MUDServer;

import mud.net.Client;

import mud.objects.NPC;
import mud.objects.Player;

import mud.objects.items.WeaponType;
import mud.utils.Utils;

public class AttackCommand extends Command {

	public AttackCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		Player player = getPlayer(client);

		try {
			if (!arg.equals("")) {
				// here we want to try and get whatever was targeted

				MUDObject mobj = parent.getObject(arg);

				player.setTarget(mobj);

				if (player.getTarget() != null) {
					// start attacking

					// get our weapon type
					WeaponType wt = WeaponType.LONGSWORD;

					// can we attack them?
					boolean attack = canAttack(player.getTarget());

					if (attack) {
						parent.send("Can attack.", client);
						parent.send("You attack " + player.getTarget().getName() + ".", client);

						boolean inRange = true;

						if (inRange) { // are they in range of our weapon?
							parent.send("In range.", client);

							boolean hit = canHit(player.getTarget());

							if (hit) { // did we hit?

								boolean criticalHit = false;							
								int criticalCheckRoll = Utils.roll(1, 20);

								int damage = 0;

								if (criticalCheckRoll > 10) { /*dummy hit check, if c > 10 we hit them and it's a critical */
									damage = calculateDamage(wt, true); // need to figure out how to get whatever weapon they used
								}
								else {
									damage = calculateDamage(wt, false); // need to figure out how to get whatever weapon they used
								}

								// damage the target

								// tell us what happened
								parent.send("A solid hit! (" + damage + " damage )", client);
							}
							else {
								// reason we didn't hit? (this should determine our message)
								parent.send("Wow, that was terrible, you barely touched them with that.", client);

							}
						}
						else { // else
							switch(wt) {
							case LONGSWORD:
								parent.send("Really? You aren't even close enough to hit!", client);
								break;
							default:
								break;
							}
							parent.send("Your attack was ineffectual, since you couldn't reach your target.", client);
						}
					}
					else {
						parent.send("Hey! You can't attack that.", client);
					}
				}
				else {
					parent.send("Your attempt to kill nothing looked really pitiful.", client);
				}
			}
		}
		catch(NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}

	public int calculateDamage(final WeaponType wType, final boolean critical) {
		String damageRoll = wType.getDamage();

		if (critical) {
			return Utils.roll(damageRoll) * wType.getCritical();
		}
		else {
			return Utils.roll(damageRoll);
		}		
	}

	public boolean canAttack(MUDObject target) {
		if (target instanceof Player) {
			return canAttack( (Player) target );
		}
		return true;
	}

	public boolean canAttack(Player target) {
		return true;
	}

	public boolean canHit(MUDObject Target) {
		int roll = Utils.roll(1, 20);

		if (roll == 1) { // Natural 1 (guaranteed miss)
			return false;
		}
		else if (roll == 20) { // Natural 20 (guaranteed hit)
			return true;
		}
		else { // compare to AC of target
			return roll > 10;
		}
	}
}