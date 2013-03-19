package mud.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;



public class Table implements Iterable<TableEntry> {
	private ArrayList<TableEntry> entries;
	
	public Table() {
		this.entries = new ArrayList<TableEntry>();
	}
	
	public Table(int size) {
		this.entries = new ArrayList<TableEntry>(size);
	}
	
	// numerical sort
	public void sort() {
		for(int i = 0; i < this.entries.size(); i += 2) {
			TableEntry te = this.entries.get(i);
			TableEntry te1 = this.entries.get(i + 1);
			if(te.compareTo(te1) == 1) {
				this.entries.set(i + 1, te);
				this.entries.set(i, te1);
			}
			else if(te.compareTo(te1) == 0) {
			}
			else if(te.compareTo(te1) == -1) {
				this.entries.set(i, te1);
				this.entries.set(i + 1, te);
			}
		}
	}
	
	public void add(TableEntry te) {
		// if it exists, change it, else add it
		if( this.entries.contains(te) ) {
			int index = this.entries.indexOf(te);
			this.entries.set(index, te);
		}
		else {
			this.entries.add(te);
		}
	}

	@Override
	public Iterator<TableEntry> iterator() {
		return new TableIterator();
	}
	
	public class TableIterator implements Iterator<TableEntry> {
		int index = -1;

		@Override
		public boolean hasNext() {
			return (index < entries.size() - 1);
		}

		@Override
		public TableEntry next() {
			if (hasNext()){
				index++;
				return entries.get(index);
			}
			else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}