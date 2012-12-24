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
 *
 */
public class MailBox implements Iterable<Mail> {
	private int size;
	public int numMessages;
	public int unread;
	private ArrayList<Mail> mailbox;
	
	public MailBox() {
		this.size = 10;
		this.numMessages = 0;
		this.mailbox = new ArrayList<Mail>(this.size);
	}
	
	public void add(Mail mail) {
		if(this.numMessages < this.size) {
			this.mailbox.add(mail);
			this.numMessages++;
		}
		else {
		}
	}
	
	public Mail get(int index) {
		return this.mailbox.get(index);
	}
	
	public Mail remove(int index) {
		this.numMessages--;
		return mailbox.remove(index);
	}
	
	/*public Mail getUnreadMail() {
		return null;
	}*/
	
	// allow moving directly to next message
	public Mail getNextUnreadMail() {
		for(Mail m : this) {
			if(m.isUnread()) {
				return m;
			}
		}
		
		return null;
	}
	
	/*public Mail getNextMail() {
		return null;
	}
	
	public Mail getPreviousMail() {
		return null;
	}*/
	
	public Iterator<Mail> iterator() {
		return new MailBoxIterator();
	}
	
	private class MailBoxIterator implements Iterator<Mail> {
		private int index = 0;

		public boolean hasNext() {  
			return index < mailbox.size();  
		}
		
		/*public boolean hasPrev() {
			return index > -1;
		}*/

		public Mail next() {  
			if ( hasNext() ) {
				Mail m = mailbox.get(index);
				this.index++;
				return m;
			}
			else { 
				throw new NoSuchElementException();
			}
		}
		
		/*public Mail prev() {
			if( hasPrev() ) {
				this.index--; 
				return mailbox.get(index);
			}
			else {
				throw new NoSuchElementException();
			}
		}*/
		

		public void remove() {  
			throw new UnsupportedOperationException();
		}  
	}
}