package mud.utils;

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
