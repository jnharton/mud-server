package mud.misc.db;

import java.util.Arrays;

public class TableEntry implements Comparable<TableEntry>{
	public Integer id;
	int cols;
	public Object[] data;
	
	public TableEntry(Integer nId, Object...objects) {
		this.id = nId;
		this.data = objects;
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
		return this.id + " " + Arrays.asList(this.data);
	}
}