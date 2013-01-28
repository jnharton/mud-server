package mud.commands;

import mud.MUDServer;

import mud.net.Client;

import mud.objects.Player;

public class GreetCommand extends Command {
	
	public GreetCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		debug(arg);
		Player current = parent.getPlayer(client);
		debug("current: " + current.getName());
		Player player1 = parent.getPlayer(arg);
		Client client1 = parent.tclients.get(player1);
		debug("player1: " + player1.getName());
		if (!player1.getNames().contains(current.getName())) {
			player1.addName(current.getName());
			if (current.getNames().contains(player1.getName())) {
				send("You tell " + player1.getName() + " that your name is " + current.getName(), client);
			}
			else {
				send("You tell " + arg + " that your name is " + current.getName(), client);
			}
			send(current.getCName() + " tells you that their name is " + current.getName(), client1);
		}
		else {
			send("You've already greeted that player", client);
		}
	}
	
	@Override
	public int getAccessLevel() {
		return USER;
	}
}