package mud.db;

import java.util.Arrays;

public class TableEntry implements Comparable<TableEntry>{
	public Integer id;
	int cols;
	public Object[] columns;
	
	public TableEntry(Integer nId, Object...objects) {
		this.id = nId;
		this.cols = objects.length;
		this.columns = objects;
	}

	@Override
	public int compareTo(TableEntry arg0) {
		if(this.id > arg0.id) {
			return 1;
		}
		else if(this.id == arg0.id) {
			return 0;
		}
		else { // implied that this.id < arg0.id
			return -1;
		}
	}
	
	@Override
	public String toString() {
		return this.id + " " + Arrays.asList(this.columns);
	}
}