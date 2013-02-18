package mud.interfaces;

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

public interface Database {
	
	/**
	 * Open a database file/connection
	 */
	public void open();
	
	/**
	 * Close a database file/connection
	 */
	public void close();
	
	/**
	 * standard database load
	 * 
	 * Perform a full load of data/objects stored in the database
	 * into memory
	 */
	public void load();
	
	/**
	 * standard database backup
	 * 
	 * Perform a full translation of in-memory data/objects over
	 * to a database file or other mechanism (e.g. MySQL)
	 */
	public void backup();
	
	// read whatever is in the file at the present index
	public void read();
	
	// database read by integer line location, convert to string (?)
	public String readString(int location);
	
	// database read by integer line location, convert to objects (?)
	public Object readObject(int location);
	
	// database write (write an empty line, maybe?)
	public void write();
	
	// database write (string)
	public void write(String s);
	
	// database write (object)
	public void write(Object o);
}