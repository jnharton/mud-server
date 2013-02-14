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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

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
public class Mail extends Properties
{
	public Mail(final String tRecipient, final String tSubject, final String tMessage) {
        setProperty("recipient", tRecipient);
        setProperty("subject", tSubject);
        setProperty("message", tMessage);
        setProperty("flag", "U");
	}

	public Mail(final File file) throws Exception {
        loadFromXML(new FileInputStream(file));
	}

    public String[] getLines() {
        return new String[] {
            "To: " + getProperty("recipient"),
            "Subject : " + getProperty("subject"),
            "Message: " + getProperty("message")
        };
    }

	public void markRead() {
		setProperty("flag", "R");
	}

	public void markUnread() {
		setProperty("flag", "U");
	}

	public void markDeleted() {
		setProperty("flag", "D");
	}

	public boolean isUnread() {
		return "U".equals(getProperty("flag"));
	}

	public String getMessage() {
		return getProperty("message");
	}

	public String getRecipient() {
		return getProperty("recipient");
	}

	public String getSubject() {
		return getProperty("subject");
	}

    public void saveToFile(final String filename) throws Exception {
        storeToXML(new FileOutputStream(filename), "");
	}

}
