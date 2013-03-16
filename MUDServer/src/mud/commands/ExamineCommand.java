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

public class ExamineCommand extends Command {

	public ExamineCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		if ( arg.equals("here") ) {
			Room room = getRoom(client);
			parent.examine(room, client);
		}
		else if (arg.equals("me")) {
			Player player = getPlayer(client);
			parent.examine(player, client);
		}
		else {
            final int dbref = Utils.toInt(arg, -1);

			if (dbref != -1) {
				MUDObject mobj = parent.getObject(dbref);

				if (mobj != null) {

					if (mobj instanceof Room) {
						Room room = (Room) mobj;
						parent.examine(room, client);
					}
					
					else if (mobj instanceof Item) {
						Item item = (Item) mobj;
						parent.examine(item, client);
					}

					else if (mobj instanceof Exit) {
						Exit exit = (Exit) mobj;
						parent.examine(exit, client);
					}
					
					else if (mobj instanceof Thing) {
						Thing thing = (Thing) mobj;
						parent.examine(thing, client);
					}
					
					else if (mobj instanceof Player) {
						Player player = (Player) mobj;
						parent.examine(player, client);
					}

					else {
						parent.examine(mobj, client);
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
					parent.examine(room, client);
					return;
				}
				
				final Exit exit = parent.getExit(arg);
				
				if (exit != null) {
					parent.examine(exit, client);
					return;
				}
				
				
				final Player player = getPlayer(arg);
				
				if (player != null) {
					parent.examine(player, client);
					return;
				}
				
				final NPC npc = getNPC(arg);
				
				if (npc != null) {
					parent.examine(npc, client);
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