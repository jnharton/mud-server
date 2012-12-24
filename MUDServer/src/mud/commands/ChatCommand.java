package mud.commands;

import java.util.ArrayList;

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
		if( arg.toLowerCase().equals("#channels") ) {
			client.write("Chat Channels\n");
			client.write("--------------------------------\n");
			for(ChatChannel cc : parent.getChatChannels()) {
				client.write(Utils.padRight(cc.getName(), 8));
				client.write(" ");
				if(cc.isListener(parent.getPlayer(client)) == true) {
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
		else if(args.length > 1) {
			String test = args[0];
			String msg = arg.replace(test + " ", "");
			// argument: show listeners on a specific channel
			if( args[1].toLowerCase().equals("#listeners") ) {
				for(ChatChannel cc : parent.getChatChannels()) {
					if( cc.getName().toLowerCase().equals(test) ) {
						client.write("Listeners on Chat Channel: " + cc.getName().toUpperCase() + "\n");
						client.write("------------------------------\n");
						ArrayList<Player> listeners = cc.getListeners();
						for(Player p : listeners) {
							client.write(p.getName() + "\n");
						}
						client.write("------------------------------\n");
						break;
					}
				}
			}
			else if( args[1].toLowerCase().equals("#messages") ) {
				for(ChatChannel cc : parent.getChatChannels()) {
					if( cc.getName().toLowerCase().equals(test) ) {
						client.write("Messages on Chat Channel: " + cc.getName().toUpperCase() + "\n");
						client.write("------------------------------\n");
						ArrayList<Message> messages = cc.getMessages();
						for(Message m : messages) {
							client.write(m.getSender() + " " + m.getRecipient() + " " + m.getMessage() + "\n");
						}
						client.write("------------------------------\n");
						break;
					}
				}
			}
			else {
				// if the channel name is that specified, write the message to the channel
				for(ChatChannel cc : parent.getChatChannels()) {
					if( cc.getName().toLowerCase().equals(test) ) {
						cc.write(client, msg);
						client.write("wrote " + msg + " to " + cc.getName() + " channel.\n");
						return;
					}
				}
				client.write("Game> No such chat channel.");
			}
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}