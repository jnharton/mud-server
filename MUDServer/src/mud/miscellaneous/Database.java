package mud.miscellaneous;

import mud.db.Table;

public class Database<T> {
	Table storage; // Main Storage
	
	Database() {
	}
	
	Database(int size) {
		this.storage = new Table(size);
	}
	
	
	public void add(T newObject) {
	}
	
	public T get(int index) {
		return null;
	}
	
	public T remove(int index) {
		return null;
	}
	
	public T remove(T object) {
		return null;
	}
}