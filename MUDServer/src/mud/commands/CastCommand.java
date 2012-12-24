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
		Integer spellId = null;
		
		player = parent.getPlayer(client);    // get a reference to the player object
		
		if(player.isCaster()) {
			try {
				debug("Argument: " + arg);
				
				spellId = parent.getSpellId(arg);        // get spell id based on name
				debug("Spell Id: " + spellId);
				
				spell = parent.getSpells().get(spellId); // get the spell based on id
				debug("Spell Name: " + spell.name);
				
				spell = parent.getSpell(arg);            // get the spell based on name
				debug("Spell Name: " + spell.name);
			}
			catch(NullPointerException npe) {
				npe.printStackTrace();
				debug("Game> Spell Casting Malfunction.");
			}
			
			if(spell != null) {
				// level check
				//if( !level ) {}
				
				// reagents check
				//if( !reagents ) {}
				
				// target check, if no target then auto-target self, etc, dependent on spell
				if(player.getTarget() == null) {
					player.setTarget(player); // auto-target to self
				}
				
				// get target
				MUDObject target = player.getTarget();
				
				// reduce mana (placeholder)
				player.setMana(-5);

				// cast spell
				send(spell.castMsg.replace("&target", player.getTarget().getName()), client); // send cast msg
				
				// if our target is a player tell them otherwise don't bother
				if(target instanceof Player) {
					Message message1 = new Message(player, player.getName() + " cast " + spell.name + " on you." , (Player) target);
					parent.addMessage(message1);
				}
				
				// apply effects
				for(int e = 0; e < spell.effects.size(); e++) {
					effect = spell.effects.get(e);
					
					parent.applyEffect(target, effect); // apply the effect to the target
				}
			}
			else {
				send("You move your fingers and mumble, but nothing happens. Must have been the wrong words.", client);
			}
		}
		else {
			send("What do you think you are anyway? Some kind of wizard? That's just mumbo-jumbo to you!", client);
		}
		
		player = null;
		spellId = null;
		spell = null;
		effect = null;
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}