package mud.commands;

import mud.MUDObject;
import mud.MUDServer;
import mud.objects.Exit;
import mud.objects.Item;
import mud.objects.Room;
import mud.objects.Thing;

import mud.net.Client;
import mud.utils.Utils;

import mud.objects.Player;

public class ExamineCommand extends Command {

	public ExamineCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		if ( arg.equals("") || arg.equals("here") ) {
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
					if (mobj instanceof Player) {
						Player player = (Player) mobj;
						parent.examine(player, client);
					}

					else if (mobj instanceof Room) {
						Room room = (Room) mobj;
						parent.examine(room, client);
					}

					else if (mobj instanceof Exit) {
						Exit exit = (Exit) mobj;
						parent.examine(exit, client);
					}
					
					else if (mobj instanceof Thing) {
						Thing thing = (Thing) mobj;
						parent.examine(thing, client);
					}

					else if (mobj instanceof Item) {
						Item item = (Item) mobj;
						parent.examine(item, client);
					}

					else {
						parent.examine(mobj, client);
					}
				}
			}
			else {
				
				/*MUDObject mobj = parent.getObject(dbref);

				if (mobj != null) {
					if (mobj instanceof Player) {
						Player player = (Player) mobj;
						parent.examine(player, client);
					}

					else if (mobj instanceof Room) {
						Room room = (Room) mobj;
						parent.examine(room, client);
					}

					else if (mobj instanceof Exit) {
						Exit exit = (Exit) mobj;
						parent.examine(exit, client);
					}
					
					else if (mobj instanceof Thing) {
						Thing thing = (Thing) mobj;
						parent.examine(thing, client);
					}

					else if (mobj instanceof Item) {
						Item item = (Item) mobj;
						parent.examine(item, client);
					}

					else {
						parent.examine(mobj, client);
					}
				}*/
				
				// get by string/name
				final Room room = getRoom(arg);
				
				if (room != null) {
					parent.examine(room, client);
					return;
				}
				
				final Player player = getPlayer(arg);
				
				if (player != null) {
					parent.examine(player, client);
					return;
				}
				
				final Exit exit = parent.getExit(arg);
				
				if (exit != null) {
					parent.examine(exit, client);
					return;
				}
				
			}
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}