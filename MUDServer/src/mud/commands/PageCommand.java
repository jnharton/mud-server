package mud.commands;

import mud.Command;
import mud.Constants;
import mud.net.Client;
import mud.objects.Player;
import mud.utils.Message;
import mud.utils.Utils;

public class PageCommand extends Command {
	public PageCommand() {
		super("send a private message");
	}
	
	public PageCommand(String description) {
		super(description);
	}

	@Override
	public void execute(String arg, Client client) {
		// ARG: <recipients>=<message>/nathan,admin=test message
		String[] in = arg.split("=");

		if (in.length > 1) {
			final String[] recipients = in[0].split(",");

			String msg = "";

			if (in.length == 2) {
				msg = in[1];

				send("You page, " + "\"" + Utils.trim(msg) + "\" to " + in[0] + ".", client);

				for (final String recipName : recipients) {
					final Player targetPlayer = getPlayer(recipName);
					final Client recipClient = targetPlayer.getClient();

					if (recipClient != null) {
						// TODO decide if I need to do things this way
						// mesage with a player sender, text to send, and the player to send it to

						Message message = new Message(getPlayer(client), targetPlayer, msg);

						addMessage(message);
					}
				}
			}
		}

	}
	
	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}