package mud.misc;

import mud.misc.Table.CType;

class Column {
	String name;
	private CType type;

	public Column(final String cName, final CType cType) {
		this.name = cName;
		this.type = cType;
	}
}