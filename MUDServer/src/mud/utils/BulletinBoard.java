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
import mud.net.Client;

/**
 * optional class that defines an accessible collection of in-game bulletin board messages
 * which are aggregated and can be added to, removed from, or simply viewed.
 * 
 * @author Jeremy
 *
 */
public class BulletinBoard {

	/**
	 * 
	 */
	private final String name;
	private String filename;
	private int lastId = 0;
	
	private ArrayList<BBEntry> entries;

	public BulletinBoard(String name) {
		this.name = name;
		this.filename = "";
		this.entries = new ArrayList<BBEntry>();
	}
	
	public BulletinBoard(String name, String filename) {
		this.name = name;
		this.filename = filename;
		this.entries = new ArrayList<BBEntry>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setFilename(final String filename) {
		this.filename = filename;
	}
	
	public String getFilename() {
		if( !filename.equals("") ) {
			return this.filename;
		}
		else {
			return this.name + ".txt";
		}
	}
	
	public BBEntry getEntry(int messageNum) {
		return this.entries.get(messageNum);
	}

	public void addEntry(BBEntry entry) {
		this.entries.add(entry);
	}

	public void removeEntry(int index) {
		this.entries.remove(index);
	}
	
	public ArrayList<BBEntry> getEntries() {
		return this.entries;
	}

	/**
	 * if class extracted, revise to send a
	 * string array or arraylist of strings
	 * instead of directly using send?
	 */
	public ArrayList<String> scan() {
		ArrayList<String> out = new ArrayList<String>();
		
		out.add(name);
		out.add("+------------------------------------------------------------------------------+");
		for (BBEntry entry : this.entries) {
			//out.add("| " + Utils.padRight(entry.toView(), 78) + " |");
			out.add("| " + Utils.padRight(entry.toView(), 76) + " |");
		}
		out.add("+------------------------------------------------------------------------------+");
		
		return out;
	}

	public void write(String message) {
		BBEntry entry = new BBEntry(++lastId, "", message);
		this.addEntry(entry);
	}

	public void write(String subject, String message) {
		BBEntry entry = new BBEntry(++lastId, subject, message);
		this.addEntry(entry);
	}

	public void write(String author, String message, Client client) {
		BBEntry entry = new BBEntry(++lastId, author, message);
		this.addEntry(entry);
	}

	public void write(String author, String subject, String message, Client client) {
		BBEntry entry = new BBEntry(++lastId, author, subject, message);
		this.addEntry(entry);
	}

	public void renumber(int start) {
		for(int i = start; i < entries.size(); i++) {
			final BBEntry entry = entries.get(i);
			
			int tid = entry.getId() - 1;
			System.out.println("Old Entry Id: " + entry.getId());
			entry.setId(tid);
			System.out.println("New Entry Id: " + entry.getId());
		}
	}

	public void renumber() { renumber(0); }
	
	public void ensureCapacity(int capacity) {
		this.entries.ensureCapacity(capacity);
	}
	
	public int getNumMessages() {
		return this.entries.size();
	}
}