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

public enum ItemType {
	ARMOR("Armor"),         // 0 
	ARROW("Arrow"),         // 1
	BOOK("Book"),           // 2
	CLOTHING("Clothing"),   // 3
	FOOD("Food"),           // 4
	HELMET("Helmet"),       // 5
	NECKLACE("Necklace"),   // 6
	NONE("None"),           // 7
	POTION("Potion"),       // 8
	RING("Ring"),           // 9
	SHIELD("Shield"),       // 10
	WAND("Wand"),           // 11
	WEAPON("Weapon"),       // 12
	CONTAINER("Container"); // 13
	
	private String name;
	
	private ItemType(String name) {
		this.name = name;
	}
	
	public static ItemType getType(String typeName) {
		if (typeName.toUpperCase().equals("ARMOR")) { return ARMOR; }
		else if (typeName.toUpperCase().equals("BOOK")) { return BOOK; }
		else if (typeName.toUpperCase().equals("CLOTHING")) { return CLOTHING; }
		else if (typeName.toUpperCase().equals("HELMET")) { return HELMET; }
		else if (typeName.toUpperCase().equals("NECKLACE")) { return NECKLACE; }
		else if (typeName.toUpperCase().equals("NONE")) { return NONE; }
		else if (typeName.toUpperCase().equals("RING")) { return RING; }
		else if (typeName.toUpperCase().equals("SHIELD")) { return SHIELD; }
		else if (typeName.toUpperCase().equals("WEAPON")) { return WEAPON; }
		else if (typeName.toUpperCase().equals(CONTAINER)) { return CONTAINER; }
		else { return NONE; }
	}
	
	public String toString() {
		return this.name.toUpperCase();
	}	
}