package mud.commands;

import mud.MUDObject;
import mud.MUDServer;
import mud.objects.Exit;
import mud.objects.Item;
import mud.objects.Room;
import mud.objects.Thing;

import mud.net.Client;

import mud.objects.Player;

public class ExamineCommand extends Command {

	public ExamineCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		if( arg.equals("") || arg.equals("here") ) {
			Room room = parent.getRoom(client);
			parent.examine(room, client);
		}
		else if(arg.equals("me") == true) {
			Player player = parent.getPlayer(client);
			parent.examine(player, client);
		}
		else {
			int dbref;

			try {
				dbref = Integer.parseInt(arg);
			}
			catch(NumberFormatException nfe) {
				nfe.printStackTrace();
				dbref = -1;
			}

			if(dbref != -1) {

				MUDObject mobj = parent.getObject(dbref);

				if(mobj != null) {
					if(mobj instanceof Player) {
						Player player = (Player) mobj;
						parent.examine(player, client);
					}

					else if(mobj instanceof Room) {
						Room room = (Room) mobj;
						parent.examine(room, client);
					}

					else if(mobj instanceof Exit) {
						Exit exit = (Exit) mobj;
						parent.examine(exit, client);
					}
					
					else if(mobj instanceof Thing) {
						Thing thing = (Thing) mobj;
						parent.examine(thing, client);
					}

					else if(mobj instanceof Item) {
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

				if(mobj != null) {
					if(mobj instanceof Player) {
						Player player = (Player) mobj;
						parent.examine(player, client);
					}

					else if(mobj instanceof Room) {
						Room room = (Room) mobj;
						parent.examine(room, client);
					}

					else if(mobj instanceof Exit) {
						Exit exit = (Exit) mobj;
						parent.examine(exit, client);
					}
					
					else if(mobj instanceof Thing) {
						Thing thing = (Thing) mobj;
						parent.examine(thing, client);
					}

					else if(mobj instanceof Item) {
						Item item = (Item) mobj;
						parent.examine(item, client);
					}

					else {
						parent.examine(mobj, client);
					}
				}*/
				
				// get by string/name
				Room room = parent.getRoom(arg);
				
				if(room != null) {
					parent.examine(room, client);
					return;
				}
				
				Player player = parent.getPlayer(arg);
				
				if(player != null) {
					parent.examine(player, client);
					return;
				}
				
				Exit exit = parent.getExit(arg, client);
				
				if(exit != null) {
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