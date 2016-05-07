package mud.commands;

import mud.Command;
import mud.Constants;
import mud.MUDObject;
import mud.objects.Player;
import mud.objects.Room;
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
	public ExamineCommand() {
		super("");
	}
	
	@Override
	public void execute(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );
		
		if ( arg.equals("") || arg.equals("here") ) {
			examine(room, client);
		}
		else if (arg.equals("me")) {
			examine(player, client);
		}
		else {
			if( arg.charAt(0) == '#' ) {
				final int dbref = Utils.toInt(arg.substring(1), -1);
				
				if (dbref != -1) {
					MUDObject mobj = getObject(dbref);

					if (mobj != null) {
						examine(mobj, client);
					}
					else {
						send("That doesn't exist.", client);
					}
				}
				else {
					send("That doesn't exist.", client);
				}
			}
			else {
				// get by string/name
				MUDObject mobj = getObject(arg);
				
				if( mobj != null ) {
					/** TODO: fix the following kludge **/
					if( mobj.getLocation() != player.getLocation() ) {
						send("That doesn't exist.", client);
						return;
					}
					
					examine(mobj, client);
				}
				else {
					send("That doesn't exist.", client);
				}
			}
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.BUILD;
	}
}