package mud.commands;

import mud.Command;
import mud.Constants;
import mud.net.Client;
import mud.utils.Message;

public class SayCommand extends Command {
	public SayCommand() {
		super("say things out loud");
	}
	
	@Override
	public void execute(String arg, Client client) {
		send("You say, \"" + arg + "\"", client);
		Message msg = new Message(getPlayer(client), arg);
		addMessage(msg);
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}