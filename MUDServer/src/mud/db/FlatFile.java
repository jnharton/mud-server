package mud.db;

import java.util.ArrayList;

import mud.MUDObject;
import mud.interfaces.Database;

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

	String filename;
	
	ArrayList<String> db = new ArrayList<String>();

	public FlatFile() {
	}

	public FlatFile(String filename) {
		this.filename = filename;
		load(filename);
	}

	@Override
	public void backup() {
		// save file to some generated name text file
	}

	@Override
	public void backup(String filename) {
		// save data to same file as it was loaded from
	}

	@Override
	public void open() {		
	}

	@Override
	public void load() {
	}

	@Override
	public void load(String fileToLoad) {
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
		this.db.set(m.getDBRef(), m.toDB());
	}

	@Override
	public void close() {
	}
}