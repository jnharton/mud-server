package mud.commands;

import mud.MUDObject;
import mud.MUDServer;
import mud.objects.Exit;
import mud.objects.Item;
import mud.objects.NPC;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.Thing;

import mud.net.Client;
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

public class ExamineCommand extends Command {

	public ExamineCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		if ( arg.equals("here") ) {
			Room room = getRoom(client);
			examine(room, client);
		}
		else if (arg.equals("me")) {
			Player player = getPlayer(client);
			examine(player, client);
		}
		else {
            final int dbref = Utils.toInt(arg, -1);

			if (dbref != -1) {
				MUDObject mobj = getObject(dbref);

				if (mobj != null) {

					if (mobj instanceof Room) {
						Room room = (Room) mobj;
						examine(room, client);
					}
					
					else if (mobj instanceof Item) {
						Item item = (Item) mobj;
						examine(item, client);
					}

					else if (mobj instanceof Exit) {
						Exit exit = (Exit) mobj;
						examine(exit, client);
					}
					
					else if (mobj instanceof Thing) {
						Thing thing = (Thing) mobj;
						examine(thing, client);
					}
					
					else if (mobj instanceof Player) {
						Player player = (Player) mobj;
						examine(player, client);
					}

					else {
						examine(mobj, client);
					}
				}
				else {
					send("That doesn't exist.", client);
				}
			}
			else {
				// get by string/name
				final Room room = getRoom(arg);
				
				if (room != null) {
					examine(room, client);
					return;
				}
				
				final Exit exit = getExit(arg);
				
				if (exit != null) {
					examine(exit, client);
					return;
				}
				
				
				final Player player = getPlayer(arg);
				
				if (player != null) {
					examine(player, client);
					return;
				}
				
				final NPC npc = getNPC(arg);
				
				if (npc != null) {
					examine(npc, client);
					return;
				}
				
				send("That doesn't exist.", client);
			}
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}