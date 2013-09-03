package mud.commands;

import mud.MUDObject;
import mud.MUDServer;
import mud.MUDServer.PlayerMode;

import mud.net.Client;

import mud.objects.Creature;
import mud.objects.NPC;
import mud.objects.Player;

import mud.objects.items.WeaponType;
import mud.utils.Utils;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

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
								
								player.setMode(PlayerMode.COMBAT); // we are now in combat mode

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
								MUDObject target = player.getTarget();
								if(target instanceof Player) {
									((Player) target).setHP(-damage);
									((Player) target).updateCurrentState();
								}
								else if(target instanceof Creature) {
									((Creature) target).setHP(-damage);
								}

								// tell us what 
								parent.send("A solid hit! (" + damage + " damage )", client);
								
								if(target instanceof Player) {
									if( ((Player) target).getHP() <= 0 ) {
										parent.send("You killed " + ((Player) target).getName() + ".", client);
										parent.handleDeath( (Player) target );
									}
								}
								else {
									debug("Creature HP: " + ((Creature) target).getHP());
									if( ((Creature) target).getHP() <= 0 ) {
										parent.send("You killed " + ((Creature) target).getName() + ".", client);
										parent.handleDeath( (Creature) target );
									}
								}
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
		if( target.getState() != Player.State.DEAD ) return true;
		else return false;
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