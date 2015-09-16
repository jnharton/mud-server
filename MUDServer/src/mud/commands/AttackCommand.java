package mud.commands;

import java.util.List;

import mud.Command;
import mud.Constants;
import mud.MUDObject;
import mud.MUDServer;
import mud.MUDServer.PlayerMode;
import mud.misc.CombatManager;
import mud.net.Client;
import mud.objects.Creature;
import mud.objects.Item;
import mud.objects.NPC;
import mud.objects.Player;
import mud.objects.Thing;
import mud.objects.items.Weapon;
import mud.objects.items.WeaponType;
import mud.objects.items.WeaponTypes;
import mud.utils.MudUtils;
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
		final Player player = getPlayer(client);
		
		final String playerName = player.getName();

		try {
			if (!arg.equals("")) {
				// here we want to try and get whatever was targeteD
				MUDObject mobj = null;
				
				final List<Creature> creatures = getCreaturesByRoom( getRoom( player ) );

				for(final Creature c : creatures) {
					if( c.getName().equalsIgnoreCase(arg) ) {
						mobj = c;
					}
				}

				// TODO fix this kludge (designed to allow use of the target command to
				// take precedence over creature target
				if( player.getTarget() == null ) {
					player.setTarget(mobj);
				}
			}

			if (player.getTarget() != null) {

				// can we attack them?
				boolean attack = MudUtils.canAttack( player.getTarget() );

				if (attack) {
					debug(playerName + ": Can attack");
					debug(playerName + " attacks " + player.getTarget().getName() + "");

					// start attacking

					// get weapon
					//Weapon weapon = (Weapon) player.getSlots().get("weapon").getItem();
					
					Weapon weapon = MudUtils.getWeapon(player);
					
					WeaponType wt = null;

					if(weapon != null) {
						// get our weapon type
						//wt = weapon.getWeaponType();
						
						// TODO resolve this somewhere
						wt = WeaponTypes.LONGSWORD;
					}

					// check range
					boolean inRange = true;

					if (inRange) { // are they in range of our weapon?
						debug(playerName + ": In range");

						boolean hit = MudUtils.canHit(player.getTarget());

						if (hit) { // did we hit?

							if( player.getMode() != PlayerMode.COMBAT ) {
								player.setMode(PlayerMode.COMBAT); // we are now in combat mode (allows us to limit command set?)
							}

							// figure out damage
							int criticalCheckRoll = Utils.roll(1, 20);
							
							boolean criticalHit = criticalCheckRoll >= wt.getCritMin() && criticalCheckRoll <= wt.getCritMax() ? true : false;

							int damage = MudUtils.calculateDamage(weapon, criticalHit);
							
							debug(playerName + " hits " + player.getTarget() + " for " + damage + " damage");
							
							// tell us what 
							if( damage <= 1 ) {
								send("Pff. You practically missed them anyway (" + damage + " damage )", client);
							}
							else if ( Utils.range(damage, 2,  5) ) {
								send("A solid hit! (" + damage + " damage )", client);
							}
							else {
								send("Ouch! That had to hurt (" + damage + " damage )", client);
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
							else if(target instanceof Item) {
								((Item) target).wear += damage;
							}
							else if(target instanceof Thing) {
								// ?
							}
							else {
								// ?
							}

							/*
							 * TODO revise this to handle different states, if we're still
							 * attacking a dead creature tell us that it's dead already, otherwise
							 * indicate if we merely incapacitated it or it's really dead. We may
							 * need to record player state beforehand so we can check against that
							 * in case a state change occurred. Or maybe, we should be recording elsewhere
							 * whether a call to updateCurrentState() actually resulted in a state change.
							 */
							if(target instanceof Player) {
								//final Player player1 = (Player) target;

								if( ((Player) target).getHP() <= 0 ) {
									send("You killed them!" + ((Player) target).getName() + ".", client);
									handleDeath( (Player) target );
									player.setTarget(null);
								}
							}
							else {
								final Creature creature = (Creature) target;

								debug("Creature HP: " + creature.getHP());
								
								if( creature.getHP() <= 0 ) {
									send("You killed " + creature.getName() + ".", client);
									debug(playerName + " killed " + creature.getName() + " (#" + creature.getDBRef() + ")");
									
									handleDeath( creature, player );
									
									player.setTarget(null);
								}
								else {
									// when we hit them and they don't die
									// problem here because combat manage only takes players
									new CombatManager();
								}
							}
						}
						else {
							// reason we didn't hit? (this should determine our message)
							send("Wow, that was terrible, you barely touched them with that.", client);

						}
					}
					else { // else
						switch(wt.getName().toUpperCase()) {
						case "LONGSWORD":
							send("Really? You aren't even close enough to hit!", client);
							break;
						case "BOW":
							send("Well, that was a waste of effort -AND- a good arrow!", client);
							break;
						default:
							break;
						}
						send("Your attack was ineffectual, since you couldn't reach your target.", client);
					}
				}
				else {
					send("Hey! You can't attack that.", client);
				}
			}
			else {
				send("Your attempt to kill nothing looked really pitiful.", client);
			}

		}
		catch(NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}