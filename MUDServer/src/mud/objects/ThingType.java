package mud.objects;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public enum ThingType {
	NONE("None"),   // 0
	CHEST("Chest"); // 1
	
	private String name;
	
	private ThingType(String name) {
		this.name = name;
	}
	
	public static ThingType getType(String typeName) {
		if (typeName.toUpperCase().equals("CHEST")) { return CHEST; }
		//else if (typeName.toUpperCase().equals("NONE")) { return NONE; }
		else { return NONE; }
	}
	
	public String toString() {
		return this.name.toUpperCase();
	}	
}