package mud.misc;

import java.util.Arrays;

public class TableEntry implements Comparable<TableEntry>{
	private Integer id;
	private Integer cols;
	
	private Object[] data;
	
	public TableEntry(final Integer nId, final Object...objects) {
		this.id = nId;
		this.cols = objects.length;
		
		this.data = objects;
	}
	
	public TableEntry(final Integer nId, final Column[] columns, final Object...objects) {
		this.id = nId;
		this.cols = objects.length;
		
		this.data = objects;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public Integer getNumColumns() {
		return this.cols;
	}
	
	public Object[] getData() {
		return this.data;
	}

	@Override
	public int compareTo(final TableEntry arg0) {
		int nId = arg0.id;
		
		if(this.id > nId)       return 1;
		else if(this.id == nId) return 0;
		else                    return -1;
	}
	
	@Override
	public String toString() {
		//StringBuilder sb = new StringBuilder();
		//sb.append(this.id);
		
		return this.id + " " + Arrays.asList(this.data);
		//return sb.toString();
	}
}