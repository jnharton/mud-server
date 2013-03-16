package mud.commands;

import mud.MUDObject;
import mud.MUDServer;
import mud.net.Client;
import mud.objects.Player;
import mud.objects.Room;
import mud.utils.Utils;

public class TeleportCommand extends Command {
	
	// Syntax:
	// @tel <dbref> -> '@tel 4'
	// @tel <object>=<dbref> -> '@tel Iridan=4'
	
	public TeleportCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		final Player player = getPlayer(client);

		String[] args = arg.split("=");

		final int dbref;
		MUDObject target = null;
		
		if(args.length > 1) {
			target = parent.getObject(args[0]);
			
			dbref = Utils.toInt(args[1], -1);
		}
		else {
			dbref = Utils.toInt(arg, -1);
		}
		
		boolean success = false;

		// try to find the room, by dbref or by name
		Room room = (dbref != -1) ? getRoom(dbref) : getRoom(arg);

		if (room != null) {
			success = true;
		}

		// if we found the room, send the player there
		if ( success ) {

			if( target == null) {
				getRoom(client).removeListener(player); // remove listener

				send("Teleporting to " + room.getName() + "... ", client);
				player.setLocation(room.getDBRef());
				player.setCoordinates(0, 0);
				send("Done.", client);
				room = getRoom(client);
				parent.look(room, client);

				room.addListener(player); // add listener
			}
			else {
				send("Teleporting " + target.getName() + " to " + room.getName() + "... ", client);
				target.setLocation(room.getDBRef());
				target.setCoordinates(0, 0);
				send("Done.", client);
			}
		}
		else {
			send("Teleport failed.", client);
		}
	}

	@Override
	public int getAccessLevel() {
		return WIZARD;
	}

}