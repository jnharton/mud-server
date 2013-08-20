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
	
	public String getMessage() {
		return this.message;
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