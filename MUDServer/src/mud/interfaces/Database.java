package mud.interfaces;

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