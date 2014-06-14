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
	/*
	 * ItemType(s) have a rough correspondence to
	 * subclasses of Item, although not all of them
	 * are represented. 
	 */
	
	ARMOR("Armor"),         // 0 (exists)
	ARROW("Arrow"),         // 1 (exists)
	BOOK("Book"),           // 2 (exists)
	CIRCLET("Circlet"),     // 3
	CLOTHING("Clothing"),   // 4 (exists)
	CONTAINER("Container"), // 5 (exists)
	EAR_RING("Earring"),    // 6
	FOOD("Food"),           // 7
	HELMET("Helmet"),       // 8
	NECKLACE("Necklace"),   // 9
	NONE("None"),           // 10 (exists, basic Item)
	POTION("Potion"),       // 11 (exists)
	RING("Ring"),           // 12
	SHIELD("Shield"),       // 13 (exists)
	WAND("Wand"),           // 14 (exists)
	WEAPON("Weapon");       // 15 (exists)
	
	private String name;
	
	private ItemType(String name) {
		this.name = name;
	}
	
	public static ItemType getType(String typeName) {
		switch(typeName.toUpperCase()) {
		case "ARMOR":     return ARMOR;
		case "ARROW":     return ARROW;
		case "BOOK":      return BOOK;
		case "CIRCLET":   return CIRCLET;
		case "CLOTHING":  return CLOTHING;
		case "CONTAINER": return CONTAINER;
		case "EAR_RING":  return EAR_RING;
		case "FOOD":      return FOOD;
		case "HELMET":    return HELMET;
		case "NECKLACE":  return NECKLACE;
		case "NONE":      return NONE;
		case "POTION":    return POTION;
		case "RING":      return RING;
		case "SHIELD":    return SHIELD;
		case "WAND":      return WAND;
		case "WEAPON":    return WEAPON;
		default:          return NONE;
		}
	}
	
	public String toString() {
		return this.name.toUpperCase();
	}	
}