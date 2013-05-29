package mud.commands;

import mud.MUDServer;
import mud.net.Client;
import mud.objects.Player;
import mud.utils.Pager;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class HelpCommand extends Command {
	
	public HelpCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		if (arg.equals("@reload"))
		{
			parent.help_reload();
			client.write("Game> Help Files Reloaded!\n");
			return;
		}
		
		/*
		 * really should add a topics system and multi-page help files (need a "pager");
		 * it'd be awesome to have some kind of virtual page up/page down functionality
		 * - maybe that would be workable if I had a real terminal emulator on the other
		 * end. if i code this feature, I could enable it if I could identify a full
		 * terminal emulation on the other end (telnet negotiation? or maybe just asking via
		 * the game for a response)
		 */

		if ( arg.equals("") ) {
            arg = "help";
        }

        final String[] helpfile = parent.getHelpFile(arg);
        final String[] topicfile = parent.getTopicFile(arg);
        
		if (helpfile != null)
		{

			if (helpfile.length > 25) {
				final Player player = getPlayer(client);
				player.setPager( new Pager(helpfile) );
				player.setStatus("VIEW");
				
				parent.op_pager("view", client);
			}
			else {
				for (final String line : helpfile) {
					client.write(line + "\r\n");
				}
			}
		}
		else if (topicfile != null) {
			for (final String line : topicfile) {
				client.write(line + "\r\n");
			}
		}
		else
		{
			client.write("No such help file!\r\n");
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}