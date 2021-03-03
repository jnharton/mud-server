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
import java.util.Collections;
import java.util.List;

import mud.objects.Player;

/**
 * optional class that defines an accessible collection of in-game bulletin board messages
 * which are aggregated and can be added to, removed from, or simply viewed.
 * 
 * @author Jeremy
 *
 */
public class BulletinBoard {
	private String name;        // the name of the board
	private String shortname;   // a shorter name for the board?
	private String filename;    // the filename we'll save this board's messages to
	private Integer lastId = 0; // the id of the last message written to this board
	
	private ArrayList<BBEntry> entries;
	
	// TODO does this make sense?
	private Player owner;

	public BulletinBoard(final String name) {
		this.name = name;
		this.filename = "";
		this.entries = new ArrayList<BBEntry>();
	}
	
	public BulletinBoard(final String name, final String filename) {
		this.name = name;
		this.filename = filename;
		this.entries = new ArrayList<BBEntry>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(final String newName) {
		this.name = newName;
	}
	
	public String getShortName() {
		return this.shortname;
	}
	
	public void setShortName(final String newShortName) {
		this.shortname = newShortName;
	}
	
	public String getFilename() {
		if( !filename.equals("") ) {
			return this.filename;
		}
		else {
			return this.name + ".txt";
		}
	}
	
	public void setFilename(final String filename) {
		this.filename = filename;
	}
	
	public Player getOwner() {
		return this.owner;
	}
	
	public void setOwner(final Player newOwner) {
		this.owner = newOwner;
	}
	
	public BBEntry getEntry(final int messageNum) {
		BBEntry message = null;
		
		try {
			message = this.entries.get(messageNum);
		}
		catch(final IndexOutOfBoundsException ioobe) {
			ioobe.printStackTrace();
		}
		
		return message;
	}
	
	public List<BBEntry> getEntries() {
		return Collections.unmodifiableList(this.entries);
	}

	public void addEntry(final BBEntry entry) {
		this.entries.add(entry);
	}
	
	public void loadEntry(final BBEntry entry) {
		this.addEntry(entry);
		this.lastId = entry.getId();
	}

	public void removeEntry(int index) {
		this.entries.remove(index);
	}

	public void write(final String message) {
		write("", "", message);
	}
	
	public void write(final String subject, final String message) {
		write("", subject, message);
	}
	
	public void write(final String author, final String subject, final String message) {
		this.addEntry( new BBEntry(++lastId, author, subject, message) );
	}
	
	/**
	 * renumber
	 * 
	 * renumbers the messages (changes their IDs) for a delete action,
	 * essentially just slides them all back one (5 would become 4).
	 * 
	 * @param start where to start the renumber process
	 */
	public void renumber(int start) {
		for(int i = start; i < entries.size(); i++) {
			final BBEntry entry = entries.get(i);
			
			entry.setId( entry.getId() - 1);
		}
	}

	public void renumber() {
		renumber(0);
	}
	
	public void setInitialCapacity(int capacity) {
		this.entries.ensureCapacity(capacity);
	}
	
	public int getNumMessages() {
		return this.entries.size();
	}
}