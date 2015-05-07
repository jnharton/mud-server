package mud.misc.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import mud.utils.Utils;

public class Table implements Iterable<TableEntry> {
	enum CType { INTEGER, STRING };
	
	private String name; 
	
	private List<Column> columns;
	private List<TableEntry> rows;
	
	public Table(final String tableName, final String tableFormat, final String...names) {
		this.name = tableName;
		this.columns = new LinkedList<Column>();
		this.rows = new ArrayList<TableEntry>();

		String[] formatData = tableFormat.split(" ");

		if( formatData.length == names.length ) {
			int index = 0;
			
			for(String s : formatData) {
				if( s.startsWith("%") ) {
					switch( s.charAt(1) ) {
					case 'i':
						columns.add( new Column(names[index], CType.INTEGER) );
						break;
					case 's':
						columns.add( new Column(names[index], CType.STRING) );
						break;
					default:
						break;
					}
					
					index++;
				}
			}
		}
	}
	
	public void add( Object...data ) {
		TableEntry te = new TableEntry(0, Arrays.copyOfRange(data, 0, columns.size()));
		rows.add( te );
	}
	
	public void add(TableEntry te) {
		// if it exists, change it, else add it
		if( this.rows.contains(te) ) {
			int index = this.rows.indexOf(te);
			this.rows.set(index, te);
		}
		else {
			this.rows.add(te);
		}
	}
	
	public TableEntry get(int index) {
		return this.rows.get(index);
	}
	
	// numerical sort
		public void sort() {
			for(int i = 0; i < this.rows.size(); i += 2) {
				TableEntry te = this.rows.get(i);
				TableEntry te1 = this.rows.get(i + 1);
				if(te.compareTo(te1) == 1) {
					this.rows.set(i + 1, te);
					this.rows.set(i, te1);
				}
				else if(te.compareTo(te1) == 0) {
				}
				else if(te.compareTo(te1) == -1) {
					this.rows.set(i, te1);
					this.rows.set(i + 1, te);
				}
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
			return (index < rows.size() - 1);
		}

		@Override
		public TableEntry next() {
			if (hasNext()){
				index++;
				return rows.get(index);
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
	
	public static void main(String[] args) {
		Table table = new Table( "Treasure Values per Encounter", "%i %s",
				"Encounter Level", "Treasure per Encounter");
		table.add( new TableEntry(0, 1, "300 gp"));
		table.add( new TableEntry(0, 2, "600 gp"));
		table.add( new TableEntry(0, 3, "900 gp"));
		table.add( new TableEntry(0, 4, "1200 gp"));
		table.add( new TableEntry(0, 5, "1600 gp"));
		
		
		System.out.println( Utils.padRight(table.columns.get(0).name, ' ', 16) + Utils.padRight(table.columns.get(1).name, ' ', 16) );
		
		for(TableEntry te : table.rows) {
			System.out.print( Utils.padRight( ((Integer) te.data[0]) + "", ' ', 16) );
			System.out.print( Utils.padRight( (String) te.data[1], ' ', 16) );
			System.out.print( '\n' );
			System.out.println( te );
		}
	}
	
	private class Column {
		private String name;
		private CType type;
		
		public Column(String cName, final CType cType) {
			this.name = cName;
			this.type = cType;
		}
	}
}