package mud.utils;

import java.util.ArrayList;

import mud.utils.Mail;

/**
 * Class that defines a 'mailbox' for the player
 * should have methods for adding and removing mail (reciept and deletion) as well
 * as a way to get how many messages there are, how many are unread, the next unread one, etc
 * @author Jeremy
 */
public class MailBox extends ArrayList<Mail> {

	public int numUnreadMessages() {
        int num = 0;
		for (final Mail m : this) {
			if (m.isUnread()) {
				num++;
			}
		}
		return num;
	}

	public Mail getFirstUnreadMail() {
		for (final Mail m : this) {
			if (m.isUnread()) {
				return m;
			}
		}
		return null;
	}

}
