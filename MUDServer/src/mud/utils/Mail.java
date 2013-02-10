package mud.utils;

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
