package mud.commands;

import java.util.ArrayList;

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

public class ChatCommand extends Command {

	public ChatCommand(final MUDServer server) {
		super(server);
	}

	@Override
	public void execute(final String arg, final Client client) {
        parent.cmd_chat(arg, client);
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}