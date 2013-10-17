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
	CIRCLET("Circlet"),     // 3
	CLOTHING("Clothing"),   // 4
	CONTAINER("Container"), // 5
	EAR_RING("Earring"),    // 6
	FOOD("Food"),           // 7
	HELMET("Helmet"),       // 8
	NECKLACE("Necklace"),   // 9
	NONE("None"),           // 10
	POTION("Potion"),       // 11
	RING("Ring"),           // 12
	SHIELD("Shield"),       // 13
	WAND("Wand"),           // 14
	WEAPON("Weapon");       // 15
	
	private String name;
	
	private ItemType(String name) {
		this.name = name;
	}
	
	public static ItemType getType(String typeName) {
		if (typeName.toUpperCase().equals("ARMOR"))          return ARMOR;
		else if (typeName.toUpperCase().equals("BOOK"))      return BOOK;
		else if (typeName.toUpperCase().equals("CIRCLET"))   return CIRCLET;
		else if (typeName.toUpperCase().equals("CLOTHING"))  return CLOTHING;
		else if (typeName.toUpperCase().equals("CONTAINER")) return CONTAINER;
		else if (typeName.toUpperCase().equals("EAR_RING"))  return EAR_RING;
		else if (typeName.toUpperCase().equals("FOOD"))      return FOOD;
		else if (typeName.toUpperCase().equals("HELMET"))    return HELMET;
		else if (typeName.toUpperCase().equals("NECKLACE"))  return NECKLACE;
		else if (typeName.toUpperCase().equals("NONE"))      return NONE;
		else if (typeName.toUpperCase().equals("POTION"))    return POTION;
		else if (typeName.toUpperCase().equals("RING"))      return RING;
		else if (typeName.toUpperCase().equals("SHIELD"))    return SHIELD;
		else if (typeName.toUpperCase().equals("WEAPON"))    return WEAPON;
		else if (typeName.toUpperCase().equals(CONTAINER))   return CONTAINER;
		else { return NONE; }
	}
	
	public String toString() {
		return this.name.toUpperCase();
	}	
}