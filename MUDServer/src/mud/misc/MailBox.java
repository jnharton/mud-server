package mud.misc;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
	
	public void clear() {
		this.mailbox.clear();
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