package mud.commands;

import mud.MUDServer;
import mud.net.Client;
import mud.objects.Player;
import mud.utils.Pager;


public class HelpCommand extends Command {
	
	public HelpCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		if(arg.equals("@reload") == true)
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

		if( arg.equals("") ) { arg = "help"; }

		if( parent.helpmap.containsKey(arg) )
		{
			String[] helpfile = parent.getHelpFile(arg);

			if(helpfile.length > 25) {
				Player player = parent.getPlayer(client);
				player.setPager( new Pager(helpfile) );
				player.setStatus("VIEW");
				
				parent.op_pager("", client);
			}
			else {
				for(int i = 1; i < helpfile.length; i++)
				{
					client.write(helpfile[i] + "\r\n");
				}
			}
		}
		/*else if( parent.topics.containsKey(arg) ) {
		}*/
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