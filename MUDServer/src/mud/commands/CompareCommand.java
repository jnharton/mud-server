package mud.commands;

import mud.MUDServer;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;

public class CompareCommand extends Command {
	
	public CompareCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		// get player, room objects to work with
		Player player = getPlayer(client);
		Room room = getRoom(client);
		Item item1;
		Item item2;
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}

}
