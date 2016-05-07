package mud.objects;

public final class ItemTypes {
	/*
	 * These ItemType(s) have a rough correspondence to
	 * subclasses of Item, although not all of them
	 * are represented. 
	 */
	public static final ItemType ARMOR     = new ItemType("Armor",     0);
	public static final ItemType ARROW     = new ItemType("Arrow",     1);
	public static final ItemType BOOK      = new ItemType("Book",      2);
	public static final ItemType CIRCLET   = new ItemType("Circlet",   3);
	public static final ItemType CLOTHING  = new ItemType("Clothing",  4);
	public static final ItemType CONTAINER = new ItemType("Container", 5);
	public static final ItemType EAR_RING  = new ItemType("Earring",   6);
	public static final ItemType FOOD      = new ItemType("Food",      7);
	public static final ItemType DRINK     = new ItemType("Drink",     8);
	//public static final ItemType HELMET    = new ItemType("Helmet",    8);
	public static final ItemType NECKLACE  = new ItemType("Necklace",  9);
	public static final ItemType NONE      = new ItemType("None",      10);
	public static final ItemType POTION    = new ItemType("Potion",    11);
	public static final ItemType RING      = new ItemType("Ring",      12);
	public static final ItemType SHIELD    = new ItemType("Shield",    13);
	public static final ItemType WAND      = new ItemType("Wand",      14);
	public static final ItemType WEAPON    = new ItemType("Weapon",    15);
	
	// TODO rework this stuff, ditch CIRCLET, EAR_RING, HELMET, NECKLACE, RING (basically subtypes)
	
	private ItemTypes() {}
	
	private static ItemType[] types = {
		ARMOR,  ARROW,    BOOK, CIRCLET, CLOTHING, CONTAINER, EAR_RING, FOOD,
		DRINK, NECKLACE, NONE, POTION,  RING,     SHIELD,    WAND,     WEAPON
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
		case "DRINK":     return DRINK;
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