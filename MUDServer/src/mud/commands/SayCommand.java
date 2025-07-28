package mud.commands;

import mud.Command;
import mud.Constants;
import mud.net.Client;
import mud.objects.Player;
import mud.utils.Message;

public class SayCommand extends Command {
	public SayCommand() {
		super("say things out loud");
	}
	
	@Override
	public void execute(String arg, Client client) {
		send("You say, \"" + arg + "\"", client);
		Player player = getPlayer(client);
		//Message msg = new Message(getPlayer(client), null, arg);
		Message msg = new Message(player, null, arg, getRoom(player.getLocation()));
		addMessage(msg);
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}