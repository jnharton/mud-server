package mud.commands;

import java.util.Map.Entry;

import mud.MUDServer;
import mud.net.Client;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

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
