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
	
	// standard database backup
	public void backup();
	
	// backup database to specific filename
	public void backup(String filename);
	
	// open database file
	public void open();
	
	// close database file
	public void close();
	
	// standard database load
	public void load();
	
	// load database from specific filename
	public void load(String fileToLoad);
	
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