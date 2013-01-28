package mud.utils;

/**
 * class that defines a single bulletin board entry
 * 
 * @author Jeremy
 *
 */
public class BBEntry {
	private Integer id;
	private String author;
	private String subject;
	private String message;

	public BBEntry() {
	}
	
	public BBEntry(Integer id, String tempSubject, String tempMessage) {
		this(id, "", tempSubject, tempMessage);
	}

	// for reloading existing entries
	public BBEntry(Integer id, String author, String tempSubject, String message) {
		this.id = id;
		this.author = author;
		this.subject = tempSubject;
		this.message = message;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int newId) {
		this.id = newId;
	}
	
	public String getAuthor() {
		return this.author;
	}
	
	public void setAuthor(String newAuthor) {
		this.author = newAuthor;
	}

	public String toDB() {
		return this.id + "#" + this.author + "#" + this.message;
	}

	public String toView() {
		/*return "| " + Utils.padLeft(this.id + "", 3) + " | " + Utils.padRight(this.author, 8) + " | "
				+ Utils.padRight(this.subject, 8) + " | " + Utils.padRight(this.message, 20) + " |";*/
		return Utils.padLeft(this.id + "", 3) + ") " + Utils.padRight(this.subject, 20) + "("
				+ this.author + ")";
	}

	public String toString() {
		return this.id + " " + this.author + " " + this.message;
	}
}