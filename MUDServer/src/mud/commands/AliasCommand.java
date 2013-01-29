package mud.commands;

import java.util.Map.Entry;

import mud.MUDServer;
import mud.net.Client;

public class AliasCommand extends Command {

	public AliasCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		if ( arg.equals("#list") ) {
			parent.send("Aliases", client);
			parent.send("-------------------------------------------", client);
			for (final Entry<String, String> e : parent.aliases.entrySet()) {
				parent.send(e.getKey() + " : " + e.getValue(), client);
			}
			parent.send("-------------------------------------------", client);
		}
		else {
			// if no set of things to alias to, just show existing aliases for it if any
		}
	}

	@Override
	public int getAccessLevel() {
		return ADMIN;
	}
}
