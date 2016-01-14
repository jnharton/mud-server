package mud.commands;

import java.util.Map;

import mud.Command;
import mud.Constants;
import mud.MUDServer;
import mud.interfaces.ExtraCommands;
import mud.misc.Slot;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.Player;
import mud.utils.Utils;

public final class EquipCommand extends Command {
	@Override
	public void execute(String arg, Client client) {
		final Player player = getPlayer(client);
		
		// get the integer value, if there is one, as the argument
		final int dbref = Utils.toInt(arg, -1);
		
		//Slot slot = null;

		if (arg.equals("") ) {
			send("Equip what?", client);
		}
		else {
			Item item = null;
			
			// look for specified item in the player's inventory
			if( dbref != -1 )  item = findItem(player.getInventory(), dbref);
			if( item == null ) item = findItem(player.getInventory(), arg);

			if ( item != null ) {
				if ( item.isEquippable() ) {
					send("Equip Type: " + item.getEquipType(), client);

					// equips the item in the first available slot
					//for (final String s : player.getSlots().keySet()) {
					for(final Slot slot : player.getSlots( item.getEquipType().toString() ) ) {
						if ( slot.isType( item.getItemType() ) && slot.getSlotType() == item.getSlotType() ) {
							if ( !slot.isFull() ) {
								/*
								 * handle any OnEquip effects/events
								 */

								debug("Equip Type " + item.getEquipType() + " matches " + slot.getItemType());

								slot.insert(item);                  // put item in the slot
								player.getInventory().remove(item); // remove it from the inventory
								item.setEquipped(true);             // set item's equipped "flag" to true (equipped)

								if(item instanceof ExtraCommands) {
									debug("Item has extra commands.");
									for(Map.Entry<String, Command> cmd : ((ExtraCommands) item).getCommands().entrySet()) {
										player.commandMap.put( cmd.getKey(), cmd.getValue() );
										debug("Added " + cmd.getKey() + " to player's command map from " + item.getName());
									}
								}

								item = null;                        // set item reference to null

								send(slot.getItem().getName() + " equipped (" + slot.getItem().getEquipType() + ")", client);

								break; // break the for loop, so we don't try to insert a now null object somewhere else
							}
							else {
								// are these alternative messages?
								send("You can't equip that. (Equip Slot Full)", client);
								send("Where are you going to put that? It's not like you have a spare...", client);
							}
						}
						else {
							send("You can't equip that. (Equip Type Incorrect)", client); //only useful if I force specifics of equipment?
							debug("Equip Type " + item.getEquipType() + " does not match " + slot.getItemType());
						}
					}
				}
				else {
					send("You can't equip that. (Not Equippable)", client);
					return;
				}
			}
			else send("Equip what?", client);
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}