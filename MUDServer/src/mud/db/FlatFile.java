package mud.db;

import java.util.ArrayList;
import java.util.Arrays;

import mud.MUDObject;
import mud.ObjectDB;
import mud.ObjectLoader;
import mud.interfaces.Database;
import mud.utils.Utils;

/**
 * The file
 * 
 * this class should offer a version of my current database structure
 * wrapped and provided with synchronized methods to ensure that structures
 * are not modified concurrently or that such is handled
 * 
 * @author Jeremy
 *
 */
public class FlatFile implements Database {

	final String mainDB;
	
	private ObjectDB objectDB = new ObjectDB();

	public FlatFile(String filename) {
		mainDB = filename;
	}
	
	@Override
	public void open() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void load() {
		ObjectLoader.loadObjects(loadListDatabase(mainDB), null, objectDB, null);
	}

	@Override
	public void backup() {
		// save file to some generated name text file
	}

	@Override
	public void read() {
	}

	@Override
	public String readString(int location) {
		return null;
	}

	@Override
	public Object readObject(int location) {
		return null;
	}

	@Override
	public synchronized void write() {
	}

	@Override
	public synchronized void write(String s) {
	}
	
	@Override
	public void write(Object o) {
		// TODO Auto-generated method stub
		
	}

	public synchronized void write(MUDObject m) {
	}
	
	private static ArrayList<String> loadListDatabase(String filename) {		
		return (ArrayList<String>) Arrays.asList(Utils.loadStrings(filename));
	}
}