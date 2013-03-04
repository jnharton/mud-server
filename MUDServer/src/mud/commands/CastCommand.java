package mud.commands;

import mud.Effect;
import mud.MUDObject;
import mud.MUDServer;

import mud.magic.Spell;
import mud.net.Client;

import mud.objects.Player;
import mud.objects.items.Armor;

import mud.utils.Message;

public class CastCommand extends Command {

	public CastCommand(final MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(final String spellName, final Client client) {

		final Player player = getPlayer(client);
		
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

		// should this be reversed???
		// ^No, but the player's level and effective caster level is not equivalent to the spell's level
		// PL -> SL: 0 -> 0, 1,2 -> 1, 3,4 -> 2, 5,6 -> 3, 7 -> 4...
		if (player.getLevel() < spell.getLevel()) {
			// add reagents check!

			// target check, if no target then auto-target self, etc, dependent on spell
			if (player.getTarget() == null) {
				player.setTarget(player); // auto-target to self
			}

			final MUDObject target = player.getTarget();

			// reduce mana (placeholder), really needs to check the spell's actual cost
			player.setMana(-spell.getManaCost());
			
			// calculate spell failure (basic, just checks armor for now)
			Armor armor = (Armor) player.getSlots().get("armor").getItem();

			double spellFailure = armor.armor.getSpellFailure();

			//Create random number 1 - 100
			int randNumber = (int) ((Math.random() * 100) + 1);

			if( randNumber > spellFailure) {
				// cast spell
				send(spell.castMsg.replace("&target", player.getTarget().getName()), client);

				// apply effects to the target
				for (final Effect e : spell.effects) {
					if(target instanceof Player) {
						parent.applyEffect((Player) target, e); // apply the effect to the target
					}
					else {
						parent.applyEffect(target, e);
					}
				}

				// if our target is a player tell them otherwise don't bother
				if (target instanceof Player) {
					parent.addMessage(new Message(player, player.getName() + " cast " + spell.name + " on you." , (Player) target));
				}
			}
			else {
				send("A bit of magical energy sparks off you briefly, then fizzles out. Drat!", client);
			}
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}

}
