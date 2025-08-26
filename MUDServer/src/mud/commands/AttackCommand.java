package mud.commands;

import java.util.List;

import mud.Command;
import mud.Constants;
import mud.MUDObject;
import mud.combat.CombatManager;
import mud.combat.WeaponTypes;
import mud.misc.PlayerMode;
import mud.net.Client;
import mud.objects.Creature;
import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Thing;
import mud.objects.items.Weapon;
import mud.utils.CombatUtils;
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
	public AttackCommand() {
		super("");
	}
	
	@Override
	public void execute(String arg, Client client) {
		final Player player = getPlayer(client);
		
		final String playerName = player.getName();

		try {
			if ( !arg.equals("") ) {
				MUDObject mobj = null;
				
				/*final List<Creature> creatures = getCreaturesByRoom( getRoom( player.getLocation() ) );

				for(final Creature c : creatures) {
					if( c.getName().equalsIgnoreCase(arg) ) {
						mobj = c;
					}
				}*/
				
				final List<MUDObject> objects = getByRoom( getRoom( player.getLocation() ) );
				
				for(final MUDObject o : objects) {
					final String objName = o.getName();
					
					System.out.println("ATTACK");
					System.out.println(arg);
					System.out.println(arg.replace("_", " "));
					System.out.println("");
					System.out.println(objName);
							
					if( objName.equalsIgnoreCase(arg) || objName.equalsIgnoreCase( arg.replace("_", " ") ) ) {
						mobj = o;
					}
				}
				

				player.setTarget(mobj);
			}

			// if we have a target
			if (player.getTarget() != null) {
				final MUDObject target = player.getTarget();
				
				// can we attack them?
				boolean attack = CombatUtils.canAttack( target );

				if (attack) {
					debug(playerName + ": Can attack");
					debug(playerName + " attacks " + target.getName() + "");

					// start attacking

					// get weapon
					Weapon weapon = CombatUtils.getWeapon(player);
					
					// TODO resolve this kludge, somehow
					if( weapon == null ) weapon = new Weapon( WeaponTypes.NONE);

					// check range (assuming all weapons can reach enemies for now)
					boolean inRange = true;

					if (inRange) { // are they in range of our weapon?
						debug(playerName + ": In range");

						boolean hit = CombatUtils.canHit( target );

						if (hit) { // did we hit?
							debug(playerName + ": Can hit");

							if( player.getMode() != PlayerMode.COMBAT ) {
								player.setMode(PlayerMode.COMBAT); // we are now in combat mode (allows us to limit command set?)
							}
							
							// figure out damage (should have way to tell if weapon can hit critically)
							
							// roll for a critical hit
							int criticalCheckRoll = Utils.roll(1, 20);
							
							// determine if we got a critical hit
							boolean criticalHit = Utils.inRange(criticalCheckRoll, weapon.getCritMin(), weapon.getCritMax());
							
							// determine what the resulting damage was
							int damage = CombatUtils.calculateDamage(weapon, criticalHit);
							
							debug(playerName + ": hits " + player.getTarget() + " for " + damage + " damage");
							
							// tell us what 
							if( damage <= 1 ) {
								send("Pff. You practically missed them anyway (" + damage + " damage )", client);
							}
							else if ( Utils.inRange(damage, 2,  5) ) {
								send("A solid hit! (" + damage + " damage )", client);
							}
							else {
								send("Ouch! That had to hurt (" + damage + " damage )", client);
							}
							
							/*
							 * TODO revise this to handle different states, if we're still
							 * attacking a dead creature tell us that it's dead already, otherwise
							 * indicate if we merely incapacitated it or it's really dead. We may
							 * need to record player state beforehand so we can check against that
							 * in case a state change occurred. Or maybe, we should be recording elsewhere
							 * whether a call to updateCurrentState() actually resulted in a state change.
							 */

							// damage the target
							if(target instanceof Player) {
								final Player p = (Player) target;
								
								p.setHP(-damage);
								p.updateCurrentState();
								
								if( p.getState() == Player.State.DEAD ) {
									send("You killed them!" + p.getName() + ".", client);
									handleDeath( p );
									player.setTarget(null);
								}
							}
							else if(target instanceof Creature) {
								final Creature c = (Creature) target;
								
								c.setHP(-damage);

								debug("Creature HP: " + c.getHP());
								
								if( c.getHP() <= 0 ) {
									send("You killed " + c.getName() + ".", client);
									debug(playerName + " killed " + c.getName() + " (#" + c.getDBRef() + ")");
									
									handleDeath( c, player );
									
									removeHostile( c );
									
									player.setTarget(null);
								}
								else {
									c.target = player;
									c.isHostile = true;
									
									addHostile( c );
									
									// when we hit them and they don't die
									// problem here because combat manage only takes players
									//new CombatManager();
								}
							}
							else if(target instanceof Item) {
								final Item item = (Item) target;
								
								int resistance = 5;
								
								item.modifyWear( damage - resistance ); 
							}
							else if(target instanceof Thing) {
								final Thing thing = (Thing) target;
								
								if( thing.durability - damage < 0 ) {
									// destroy thing
								}
							}
							else {
								// ?
							}
						}
						else {
							// reason we didn't hit? (this should determine our message)
							send("Wow, that was terrible, you barely touched them with that.", client);
						}
					}
					else { // else
						// trying to provided different messages for different weapons
						/*switch(wt.getName().toUpperCase()) {
						case "LONGSWORD": send("Really? You aren't even close enough to hit!", client);         break;
						case "BOW":       send("Well, that was a waste of effort -AND- a good arrow!", client); break;
						default:          break;
						}*/
						
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