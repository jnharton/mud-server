package mud.db;

import mud.interfaces.Database;


public class XMLFile implements Database {
	
	String filename;
	
	public XMLFile() {
	}
	
	public XMLFile(String filename) {
	}

	@Override
	public void backup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void open() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void load() {
		// TODO Auto-generated method stub

	}

	@Override
	public void read() {
		// TODO Auto-generated method stub

	}

	@Override
	public String readString(int location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object readObject(int location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write() {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(Object o) {
		// TODO Auto-generated method stub

	}

}
