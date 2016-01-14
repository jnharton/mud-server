package mud.commands;

import mud.Command;
import mud.Constants;
import mud.MUDServer;
import mud.objects.Player;
import mud.utils.EditorData;
import mud.utils.Mail;
import mud.utils.Utils;
import mud.misc.Editors;
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

public class MailCommand extends Command {
	private final int shortSUB = 10;
	private final int shortMSG = 32;
	
	@Override
	public void execute(String arg, Client client) {

		final Player player = getPlayer(client);

		final String[] args = arg.split(" ");

		final String param;

		if( args.length > 1 ) {
			param = args[0];
		}
		else param = "";

		if (!param.equals("")) {
			if (param.equals("#delete")) {
				if( args.length > 1 ) {
					int msg = Utils.toInt(args[1], -1);

					if( msg > -1 ) {
						player.getMailBox().remove(msg);
						client.writeln("Deleted message #" + msg);
					}
					else client.writeln("No mail with that id.");
				}
				else client.writeln("no message to delete indicated.");
			}
		}
		else if (arg.equals("#list")) {
			// kinda dependent on mail message objects and mailbox
			// basically we get the mailbox object and then give information for each
			// piece of mail
			// should these box headers be configurable either on the server end or the client end?
			
			// TODO should I convert to using writeln and building my strings before sending
			
			client.write("+---------------------------------------------------------------------------+\n");
			client.write("| Mailbox                                                                   |\n");
			client.write("+-------+------+------------+----------------------------------+------------+\n");
			client.write("| ID    | Flag | Subject    | Brief                            | Date       |\n");
			client.write("+-------+------+------------+----------------------------------+------------+\n");
			
			for (final Mail mail : player.getMailBox()) {
				client.write("| ");
				client.write(Utils.padLeft(mail.getId() + "", 5).substring(0, 5));
				client.write(" | ");
				client.write(Utils.padLeft(mail.getFlag() + "", 4).substring(0, 4));
				client.write(" | ");
				client.write(Utils.padRight(mail.getSubject(), shortSUB).substring(0, shortSUB));
				client.write(" | ");
				client.write(Utils.padRight(mail.getMessage(), shortMSG).substring(0, shortMSG));
				client.write(" | ");
				client.write(mail.getDate().toString());
				client.write(" |");
				client.write("\n");
			}

			client.write("+---------------------------------------------------------------------------+\n");
		}
		else if (arg.equals("#write")) {
			String old_status = player.getStatus();

			player.setStatus("EDT");       // set the 'edit' status flag
			player.setEditor(Editors.MAIL); // mail

			EditorData newEDD = new EditorData();
			
			// record prior player status
			newEDD.addObject("pstatus", old_status);
			
			newEDD.addObject("recipient", "");
			newEDD.addObject("subject", "");
			newEDD.addObject("message", "");
			newEDD.addObject("step", 0);
			
			player.setEditorData(newEDD);
			
			handle_mail("", client);
		}
		else if(!arg.equals("")) {
			final int msg = Utils.toInt(arg, -1);

			if (msg > -1 && msg < player.getMailBox().numMessages()) {
				send("Checking Mail... " + msg, client);

				Mail mail = player.getMailBox().get(msg);

				send("Message #: " + msg, client);
				send("To:        " + mail.getRecipient(), client);
				send("Subject:   " + mail.getSubject(), client);
				send(" ", client);
				send(mail.getMessage(), client);
				send(" ", client);

				if (mail.isUnread()) {
					mail.markRead();
					send("< mail marked as read >", client);
				}
			}
			else {
				send("No such existing message!", client);
			}
		}
		else {
			//send("Checking for unread messages...", client);

			final int messages = player.getMailBox().numUnreadMessages();

			if (messages == 0)  send("You have no unread messages.", client);
			else                send("You have " + String.valueOf(messages) + " unread messages.", client);
		}
	}

	@Override
	public int getAccessLevel() {
		return Constants.USER;
	}
}