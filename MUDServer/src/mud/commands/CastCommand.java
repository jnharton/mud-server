package mud.commands;

import java.util.Arrays;
import java.util.List;

import mud.Command;
import mud.Constants;
import mud.MUDObject;
import mud.MUDServer;
import mud.magic.Reagent;
import mud.magic.Spell;
import mud.misc.Effect;
import mud.net.Client;
import mud.objects.Player;
import mud.objects.items.Armor;
import mud.utils.EffectTimer;
import mud.utils.Message;
import mud.utils.SpellTimer;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class CastCommand extends Command {
	@Override
	public void execute(final String spellName, final Client client) {
		final Player player = getPlayer(client);
		
		if (!player.isCaster()) {
			send("What do you think you are anyway? Some kind of wizard? That's just mumbo-jumbo to you!", client);
			return;
		}

		final Spell spell = getSpell(spellName);

		if (spell == null) {
			send("You move your fingers and mumble, but nothing happens. Must have been the wrong words.", client);
			debug("CastCommand spellName: " + spellName);
			return;
		}

		// should this be reversed???
		// ^No, but the player's level and effective caster level is not equivalent to the spell's level
		// PL -> SL: 0 -> 0, 1,2 -> 1, 3,4 -> 2, 5,6 -> 3, 7 -> 4...
		if (player.getLevel() < spell.getLevel()) {
			// add reagents check!
			if( spell.getReagents() != null ) {
				/*for(Reagent r : spell.getReagents().values()) {
					System.out.println(r.getName());
					if( player.getInventory().contains(r) ) {
					}
					else {
						send("Insufficient spell components", client);
						return;
					}
				}*/
			}

			// target check, if no target then auto-target self, etc, dependent on spell
			if (player.getTarget() == null) {
				int range = spell.getRange(player.getLevel());
				if( range == 0 ) player.setTarget(player); // auto-target to self, if spell takes self
			}

			final MUDObject target = player.getTarget();
			
			// check validity of target for this spell
			if( !validTarget( target, spell, player ) ) {
				send("Game> Invalid Target for Spell!", client);
				return;
			}

			// expend mana
			player.setMana(-spell.getManaCost());
			
			// calculate spell failure (basic, just checks armor for now)
			Armor armor = (Armor) player.getSlots().get("armor").getItem();
			
			double spellFailure = 0;
			
			if( armor != null ) {
				spellFailure = armor.getSpellFailure() * 100; // spellFailure stored as percentage
				debug("Spell Failure: " + spellFailure);
			}

			//Create random number 1 - 100
			int randNumber = (int) ((Math.random() * 100) + 1);
			
			debug("d100 Roll: " + randNumber);
			
			if( randNumber > spellFailure ) {
				// cast spell
				send(spell.getCastMessage().replace("&target", player.getTarget().getName()), client);

				// apply effects to the target
				for (final Effect e : spell.getEffects()) {
					applyEffect(target, e); // apply the effect to the target
					
					// is there ever a case where the caster wouldn't be a player here?
					SpellTimer sTimer = new SpellTimer(spell, 60);     // spell timer with default (60 sec) cooldown
					addSpellTimer(player, sTimer);
					scheduleAtFixedRate(sTimer, 0, 1000);
					
					EffectTimer etimer = new EffectTimer(e, 30);
					addEffectTimer(player, etimer);
					scheduleAtFixedRate(etimer, 0, 1000); // create countdown timer
					
					// if our target is a player set timers for us and tell them, otherwise don't bother
					if(target instanceof Player) {
						debug("Target is Player.");
						addMessage(new Message(player, player.getName() + " cast " + spell.getName() + " on you." , (Player) target));
					}
				}
			}
			else {
				send("A bit of magical energy sparks off you briefly, then fizzles out. Drat!", client);
			}
			
			player.setLastSpell(spell);
		}
	}
	
	// is TARGET a valid target for SPELL cast by PLAYER
	private boolean validTarget( MUDObject target, Spell spell, Player player) {
		// if the spell is an area affect spell, then not having a target is valid, as is having a target,
		// having no target means the spell hits a general area somewhere in front of you, whereas a target
		// means the spell hits and radiates out from the target
		
		List<String> targets = Arrays.asList( Spell.decodeTargets(spell).split(",") );
		
		System.out.println("Targets: " + targets);
		
		if( target instanceof Player ) {
			Player player1 = (Player) target;
			
			// determine whether the target player is hostile or friendly with regard to
			// the caster
			
			// I'd like a better method that uses numerical equivalents
			if( targets.contains("self") ) {
				if( player == player1 ) return true;
				
				return false;
			}
			else if( targets.contains("enemy") ) {
				if( player != player1 ) return false;
				else {
					return false;
				}
			}
			else if( targets.contains("friend") ) {
				if( player != player1 ) return false;
				else {
					return false;
				}
			}
			else return false;
		}
		else {
			return true; // assuming that the spell can target any non-player object, regardless
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}

}