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

/**
 * Class that defines a unit of mail, a single message with several attributes:
 * id - the order of the letter in the recipient's inbox
 * recipient - the user/player that the mail is addressed too (maybe not terribly important on loading/saving, but
 * useful when constructing new Mail to place in a player's MailBox)
 * message - the content of the message itself
 * flag - a flag indicating the status of the message [flags: U (unread), R (read), D (delete)]
 * @author Jeremy
 *
 */
public class Mail {
	private Integer id;
	private String recipient;
	private String subject;
	private String message;
	private Character flag;
	
	private boolean isUnread;
	
	/**
	 * Mail message
	 * 
	 * @param tId
	 * @param tRecipient
	 * @param tSubject
	 * @param tMessage
	 * @param tFlag
	 */
	public Mail(int tId, String tRecipient, String tSubject, String tMessage, Character tFlag) {
		this.id = tId;
		this.recipient = tRecipient;
		this.subject = tSubject;
		this.message = tMessage;
		this.flag = tFlag;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public String getRecipient() {
		return this.recipient;
	}
	
	public String getSubject() {
		return this.subject;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public Character getFlag() {
		return this.flag;
	}
	
	public void markRead() {
		this.flag = 'R';
		this.isUnread = false;
	}
	
	public void markUnread() {
		this.flag = 'U';
		this.isUnread = true;
	}
	
	public boolean isUnread() {
		return this.isUnread;
	}
	
	public String[] toStorage() {
		String[] out = new String[4];
		
		out[0] = recipient;
		out[1] = subject;
		out[2] = message;
		out[3] = flag.toString();
		
		return out;
	}
	
	public String toString() {
		return "MAIL " + id + " " + recipient + " " + subject + " " + message + " " + flag;
	}
}