package mud.commands;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import mud.Colors;
import mud.MUDServer;

import mud.net.Client;

import mud.objects.Player;

import mud.utils.ChatChannel;
import mud.utils.Message;
import mud.utils.Utils;

public class ChatCommand extends Command {

	public ChatCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		String[] args = arg.split(" ");
		// argument: show a list of available channels and whether you are on them
		if ( arg.toLowerCase().equals("#channels") ) {
			client.write("Chat Channels\n");
			client.write("--------------------------------\n");
			for (ChatChannel cc : parent.getChatChannels()) {
				client.write(Utils.padRight(cc.getName(), 8));
				client.write(" ");
				if (cc.isListener(parent.getPlayer(client))) {
					client.write(Colors.GREEN.toString());
					client.write("Enabled");
					client.write(Colors.WHITE.toString());
					client.write("\n");
				}
				else {
					client.write(Colors.RED.toString());
					client.write("Disabled");
					client.write(Colors.WHITE.toString());
					client.write("\n");
				}
			}
			client.write("--------------------------------\n");
		}
		else if (args.length > 1) {
			String test = args[0];
			String msg = arg.replace(test + " ", "");
			final ChatChannel testChannel = parent.getChatChannel(test);
			if (testChannel == null) {
				client.write("Game> No such chat channel.");
				return;
			}

			// argument: show listeners on a specific channel
			if ( args[1].toLowerCase().equals("#listeners") ) {
				client.write("Listeners on Chat Channel: " + testChannel.getName().toUpperCase() + "\n");
				client.write("------------------------------\n");
				ArrayList<Player> listeners = testChannel.getListeners();
				for (Player p : listeners) {
					client.write(p.getName() + "\n");
				}
				client.write("------------------------------\n");
			}
			else if ( args[1].toLowerCase().equals("#messages") ) {
				client.write("Messages on Chat Channel: " + testChannel.getName().toUpperCase() + "\n");
				client.write("------------------------------\n");
				ConcurrentLinkedQueue<Message> messages = testChannel.getMessages();
				for (Message m : messages) {
					client.write(m.getSender() + " " + m.getRecipient() + " " + m.getMessage() + "\n");
				}
				client.write("------------------------------\n");
			}
			else {
				// if the channel name is that specified, write the message to the channel
				Player player = parent.getPlayer(client);
				testChannel.write(player, msg);
				client.write("wrote " + msg + " to " + testChannel.getName() + " channel.\n");
				//chatLog.writeln("(" + testChannel.getName() + ") <" + player.getName() + "> " + msg); (accessibility issue)
			}
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}