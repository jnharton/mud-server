package mud.misc;

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

	private Column[] columns;
	private List<TableEntry> rows;

	public Table(final String tableName, final String tableFormat, final String...names) {
		this.name = tableName;
		this.columns = new Column[names.length];
		this.rows = new LinkedList<TableEntry>();

		String[] formatData = tableFormat.split(" ");

		if( formatData.length == names.length ) {
			int index = 0;

			for(final String s : formatData) {
				if( s.startsWith("%") ) {
					switch( s.charAt(1) ) {
					case 'i':
						columns[index] = new Column(names[index], CType.INTEGER);
						break;
					case 's':
						columns[index] = new Column(names[index], CType.STRING);
						break;
					default:
						break;
					}

					index++;
				}
			}
		}
	}
	
	public void add(final TableEntry te) {
		// if it exists, change it, else add it
		if( this.rows.contains(te) ) {
			int index = this.rows.indexOf(te);
			this.rows.set(index, te);
		}
		else {
			this.rows.add(te);
		}
	}
	
	public void add(final Object...data ) {
		//TableEntry te = new TableEntry(0, Arrays.copyOfRange(data, 0, columns.length));
		this.rows.add( new TableEntry(0, Arrays.copyOfRange(data, 0, columns.length)) );
	}
	
	public String getName() {
		return this.name;
	}

	public TableEntry get(final int index) {
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
	
	public static void printTable(final Table table) {
		StringBuilder sb = new StringBuilder();

		int numColumns = 0;

		for(final Column c : table.columns) {
			sb.append( Utils.padRight(c.name, ' ', 16) );

			if( numColumns < table.columns.length ) sb.append(' ');
		}

		System.out.println( sb.toString() );

		sb.delete(0, sb.length());

		for(final TableEntry te : table.rows) {
			for(final Object obj : te.getData()) {
				System.out.print( Utils.padRight(obj.toString(), ' ', 16) );
				System.out.print(' ');
			}

			System.out.print( '\n' );

			//System.out.println( te );
		}
	}

	public static void main(final String[] args) {
		Table table = new Table("Treasure Values per Encounter", "%i %s", "Encounter Level", "Treasure per Encounter");
		
		table.add( new TableEntry(0, 1, "300 gp"));
		table.add( new TableEntry(0, 2, "600 gp"));
		table.add( new TableEntry(0, 3, "900 gp"));
		table.add( new TableEntry(0, 4, "1200 gp"));
		table.add( new TableEntry(0, 5, "1600 gp"));
		
		printTable(table);
		
		Table table1 = new Table("stats", "%i %i %i %i %i %i", "Strength", "Dexterity", "Constitution", "Charisma", "Intelligence", "Wisdom");
		
		table1.add( new TableEntry(0, 10, 10, 10, 10, 10, 10) );
		table1.add( new TableEntry(0, 12, 14, 10, 16, 12, 13) );
		table1.add( new TableEntry(0, 18, 18, 18, 18, 18, 18) );
		
		printTable(table1);
	}
}