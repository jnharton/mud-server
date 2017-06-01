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
	
	private Integer topic = 0;
	
	public BBEntry(final Integer id, final String author, final String tempSubject, final String message) {
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
	
	public String getSubject() {
		return this.subject;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	/**
	 * output formatting when we want to list the messages, shows
	 * just enough information to have a sense for what it's about
	 * @return
	 */
	public String toView() {
		return toView(76);
	}
	
	public String toView(int width) {
		// left(id, 3) | right(author, 8) | right(subject, 8) | right(message, 20) |
		StringBuilder sb = new StringBuilder();
		
		sb.append( Utils.padLeft(this.id + "", 5) );
		sb.append( " " );
		sb.append( Utils.padRight(this.subject, 20) );
		sb.append( "(" );
		sb.append( this.author );
		sb.append( ")" );
		
		return Utils.padRight( sb.toString(), width );
	}
	
	/**
	 * toString
	 * 
	 * string representation for the purpose of storing the messages
	 */
	public String toString() {
		// <id>#<author>#<subject>#<message>
		return Utils.join(Utils.mkList("" + this.id, this.author, this.subject, this.message), "#");
	}
}