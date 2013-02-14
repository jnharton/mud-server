package mud.commands;

import java.util.ArrayList;

import mud.MUDServer;

import mud.net.Client;

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