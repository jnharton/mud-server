package mud.objects;

public final class ItemTypes {
	/*
	 * These ItemType(s) have a rough correspondence to
	 * subclasses of Item, although not all of them
	 * are represented. 
	 */
	public static ItemType ARMOR     = new ItemType("Armor",     0);
	public static ItemType ARROW     = new ItemType("Arrow",     1);
	public static ItemType BOOK      = new ItemType("Book",      2);
	public static ItemType CIRCLET   = new ItemType("Circlet",   3);
	public static ItemType CLOTHING  = new ItemType("Clothing",  4);
	public static ItemType CONTAINER = new ItemType("Container", 5);
	public static ItemType EAR_RING  = new ItemType("Earring",   6);
	public static ItemType FOOD      = new ItemType("Food",      7);
	public static ItemType HELMET    = new ItemType("Helmet",    8);
	public static ItemType NECKLACE  = new ItemType("Necklace",  9);
	public static ItemType NONE      = new ItemType("None",      10);
	public static ItemType POTION    = new ItemType("Potion",    11);
	public static ItemType RING      = new ItemType("Ring",      12);
	public static ItemType SHIELD    = new ItemType("Shield",    13);
	public static ItemType WAND      = new ItemType("Wand",      14);
	public static ItemType WEAPON    = new ItemType("Weapon",    15);
	
	private ItemTypes() {}
	
	private static ItemType[] types = {
		ARMOR,  ARROW,    BOOK, CIRCLET, CLOTHING, CONTAINER, EAR_RING, FOOD,
		HELMET, NECKLACE, NONE, POTION,  RING,     SHIELD,    WAND,     WEAPON
	};
	
	public static ItemType getType(Integer typeId) {
		// TODO fix this? does it need fixing? is it kludgy?
		if( typeId <= types.length - 1 ) {
			return types[typeId];
		}
		else return null;
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
}