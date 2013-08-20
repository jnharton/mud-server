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
					client.write(check(line) + "\r\n");
				}
			}
			
			//client.write("ALIASES: " + parent.aliases.get(arg));
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
	
	private String check(final String in) {
		boolean doEval = false;
		
		StringBuilder result = new StringBuilder();
		StringBuilder work = new StringBuilder();
		
		char ch;
		
		for( int c = 0; c < in.length(); c++ ) {
			ch = in.charAt(c);
			
			switch(ch) {
			case '{':
				if( !doEval ) {
					doEval = true;
					work.append(ch);
				}
				else {
					work.delete(0, work.length());
					doEval = true;
					work.append(ch);
				}
				break;
			case '}':
				if( doEval ) {
					doEval = false;
					work.append(ch);
					result.append( evaluate( work.toString() ) );
				}
				break;
			default:
				if( doEval ) {
					work.append(ch);
				}
				else {
					result.append(ch);
				}
				break;
			}
		}
		
		return result.toString();
	}
	
	private String evaluate(String test) {
		return parent.getProgInt().interpret(test);
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}