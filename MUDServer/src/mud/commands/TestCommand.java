/**
 * 
 */
package mud.commands;

import mud.Command;
import mud.Constants;
import mud.MUDServer;
import mud.net.Client;

/**
 * @author Jeremy
 *
 */
public class TestCommand extends Command {
	/* (non-Javadoc)
	 * @see mud.commands.Command#execute(java.lang.String, mud.net.Client)
	 */
	@Override
	public void execute(String arg, Client client) {
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}