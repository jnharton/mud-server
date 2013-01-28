package mud.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import mud.utils.Mail;

/**
 * Class that defines a 'mailbox' for the player
 * should have methods for adding and removing mail (reciept and deletion) as well
 * as a way to get how many messages there are, how many are unread, the next unread one, etc
 * @author Jeremy
 */
public class MailBox implements Iterable<Mail> {

    final private ArrayList<Mail> mailbox;

	public MailBox() {
		this.mailbox = new ArrayList<Mail>();
	}
	
	public void add(Mail mail) {
        this.mailbox.add(mail);
	}
	
	public Mail get(int index) {
		return this.mailbox.get(index);
	}
	
	public Mail remove(int index) {
		return mailbox.remove(index);
	}

	public int numMessages() {
		return mailbox.size();
	}

	public int numUnreadMessages() {
        int num = 0;
		for (final Mail m : mailbox) {
			if (m.isUnread()) {
				num++;
			}
		}
		return num;
	}

	// allow moving directly to next message
	public Mail getNextUnreadMail() {
		for (final Mail m : mailbox) {
			if (m.isUnread()) {
				return m;
			}
		}
		
		return null;
	}

	public Iterator<Mail> iterator() {
		return new MailBoxIterator();
	}
	
	private class MailBoxIterator implements Iterator<Mail> {
		private int index = 0;

		public boolean hasNext() {  
			return index < mailbox.size();  
		}

		public Mail next() {  
			if (hasNext()) {
				final Mail m = mailbox.get(index);
				this.index++;
				return m;
			}
			else { 
				throw new NoSuchElementException();
			}
		}

		public void remove() {  
			throw new UnsupportedOperationException();
		}  
	}
}