package mud.commands;

import mud.Effect;
import mud.MUDObject;
import mud.MUDServer;

import mud.magic.Spell;
import mud.net.Client;

import mud.objects.Player;

import mud.utils.Message;

public class CastCommand extends Command {
	
	public CastCommand(MUDServer mParent) {
		super(mParent);
	}
	
	@Override
	public void execute(String arg, Client client) {
		Player player = null;
		Spell spell = null;
		Effect effect = null;

		player = parent.getPlayer(client);    // get a reference to the player object

		if (player.isCaster()) {
			try {
				debug("Argument: " + arg);

				spell = parent.getSpell(arg);            // get the spell based on name
				debug("Spell Name: " + spell.name);
			}
			catch(NullPointerException npe) {
				npe.printStackTrace();
				debug("Game> Spell Casting Malfunction.");
			}

			if (spell != null) {
				// level check
				if ( player.getLevel() < spell.getLevel() ) {
					// reagents check
					if ( 1 == 1 ) { // !reagents

						// target check, if no target then auto-target self, etc, dependent on spell
						if (player.getTarget() == null) {
							player.setTarget(player); // auto-target to self
						}
						else {
							// get target
							MUDObject target = player.getTarget();

							// reduce mana (placeholder), really needs to check the spell's actual cost
							player.setMana(-spell.getManaCost());

							// cast spell
							send(spell.castMsg.replace("&target", player.getTarget().getName()), client); // send cast msg

							// if our target is a player tell them otherwise don't bother
							if (target instanceof Player) {
								Message message1 = new Message(player, player.getName() + " cast " + spell.name + " on you." , (Player) target);
								parent.addMessage(message1);
							}

							// apply effects
							for (int e = 0; e < spell.effects.size(); e++) {
								effect = spell.effects.get(e);

								parent.applyEffect(target, effect); // apply the effect to the target
							}	
						}
					}
				}
			}
			else {
				send("You move your fingers and mumble, but nothing happens. Must have been the wrong words.", client);
			}
		}
		else {
			send("What do you think you are anyway? Some kind of wizard? That's just mumbo-jumbo to you!", client);
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}