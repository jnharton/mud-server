package mud.commands;

import mud.Effect;
import mud.MUDObject;
import mud.MUDServer;

import mud.magic.Spell;
import mud.net.Client;

import mud.objects.Player;

import mud.utils.Message;

public class CastCommand extends Command {
	
	public CastCommand(final MUDServer mParent) {
		super(mParent);
	}
	
	@Override
	public void execute(final String spellName, final Client client) {

        final Player player = parent.getPlayer(client);
		if (!player.isCaster()) {
			send("What do you think you are anyway? Some kind of wizard? That's just mumbo-jumbo to you!", client);
            return;
		}

		final Spell spell = parent.getSpell(spellName);

        if (spell == null) {
            send("You move your fingers and mumble, but nothing happens. Must have been the wrong words.", client);
            debug("CastCommand spellName: " + spellName);
            return;
        }

        // shoudl this be reversed???
        if (player.getLevel() < spell.getLevel()) {
            // add reagents check!

            // target check, if no target then auto-target self, etc, dependent on spell
            if (player.getTarget() == null) {
                player.setTarget(player); // auto-target to self
            }

            final MUDObject target = player.getTarget();

            // reduce mana (placeholder), really needs to check the spell's actual cost
            player.setMana(-spell.getManaCost());

            // cast spell
            send(spell.castMsg.replace("&target", player.getTarget().getName()), client);

            // if our target is a player tell them otherwise don't bother
            if (target instanceof Player) {
            	parent.addMessage(new Message(player, player.getName() + " cast " + spell.name + " on you." , (Player) target));
            }

            // apply effects to the target
            for (final Effect e : spell.effects) {
            	parent.applyEffect(target, e);
            	if(target instanceof Player) {
            		parent.applyEffect((Player) target, e); // apply the effect to the target
            	}
            	else {
            	}
            }	
        }
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}

}
