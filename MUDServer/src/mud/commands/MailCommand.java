package mud.commands;

import java.util.ArrayList;

import mud.Editor;
import mud.MUDServer;
import mud.objects.Player;
import mud.utils.EditList;
import mud.utils.Mail;
import mud.utils.MailBox;
import mud.utils.Utils;
import mud.net.Client;

public class MailCommand extends Command {

	private final int shortSUB = 10;
	private final int shortMSG = 32;
	
	public MailCommand(MUDServer mParent) {
		super(mParent);
	}

	@Override
	public void execute(String arg, Client client) {
		
		final Player player = parent.getPlayer(client);
		
		final String[] args = arg.split(" ");
		
		if (!arg.equals("")) {
			if (args[0].equals("#delete")) {
				client.write("#delete function entry\n");
			}
			else if (args[0].equals("#list")) {
				// kinda dependent on mail message objects and mailbox
				// basically we get the mailbox object and then give information for each
				// piece of mail
				// should these box headers be configurable either on the server end or the client end?
				
				client.write("#list function entry\n");
				client.write("+--------------------------------------------------------------------+\n");
				client.write("| Mailbox                                                            |\n");
				client.write("+--------------------------------------------------------------------+\n");

                int i = 0;
				for (final Mail mail : player.getMailBox()) {
					client.write("| ");
					client.write(Utils.padLeft(i + "", 5).substring(0, 5));
					client.write(" ");
					client.write(Utils.padRight(mail.getSubject(), shortSUB).substring(0, shortSUB));
					client.write(" ");
					client.write(Utils.padRight(mail.getMessage(), shortMSG).substring(0, shortMSG));
					client.write(" ");
					client.write("5/5/2011 12:31PM");
					client.write(" |");
					client.write("\n");
				}
				
				client.write("+--------------------------------------------------------------------+\n");
			}
			else if (args[0].equals("#write")) {
				/* Functionality is not complete */
				
				if (arg.indexOf("+") != -1) {
					String[] args1 = arg.substring(arg.indexOf("+")).split("=");
					
					player.setStatus("EDT");
					player.setEditor(Editor.LIST);
					
					// need to somehow flag as editing mail, so I can
					// conditionally change some of the list editor's behavior
					// or maybe I could add another comand 'mail #send' that
					// would look for the write listname in my lists, use it
					// to construct a mail message, and then remove it?
					
                    player.startEditing("mailmsg");
					client.write("Mail Editor v0.0b\n");
                    final EditList list = player.getEditList();
					String header = "< List: " + list.name + " Lines: " + list.getLines() + " >";

					client.write(header);
					
					client.write(">> Please type your message below and type '.end' when done.");
					client.write(">> NOTE: you are using the normal line editor for this, so some commands" +
							"may produce unexpected results");
				}
				else {
				}
			}
			else {
				final int msg = Utils.toInt(args[0], -1);

				if (msg > -1 && msg < player.getMailBox().size()) {
					client.write("Checking Mail..." + msg + "\n");
					
					Mail mail = player.getMailBox().get(msg);

					client.write("Message #: " + msg + "\n");
					client.write("To: " + mail.getRecipient() + "\n");
					client.write("Subject: " + mail.getSubject() + "\n");
					client.write(mail.getMessage() + "\n");

					if (mail.isUnread()) {
						mail.markRead();
						client.write("< mail marked as read >\n");
					}
				}
				else {
					client.write("No such existing message!\n");
				}
			}
		}
		else {
			client.write("Checking for unread messages...\n");

            final int messages = player.getMailBox().numUnreadMessages();

			if (messages == 0) { client.write("You have no unread messages.\n"); }
			else { client.write("You have " + String.valueOf(messages) + " unread messages.\n"); }
		}
	}

	@Override
	public int getAccessLevel() {
		return USER;
	}
}