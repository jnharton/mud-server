package mud.combat;

public final class ArmorTypes {
	/* Fields - Type, Name, Cost, ? (P), ? (G), ? (S), ? (C) , Armor Bonus (AB), Dexterity Bonus (DB),
	 * Armor Check (AC), Speed Factor (SPF), Weight (WGHT) */
	/*               Type         Name               Cost        P  G     S  C     AB DB  AC SPF   WGHT */
	public static final ArmorType PADDED          = new ArmorType( ArmorType.WeightClass.LIGHT,  "Padded",          new int[] { 0, 5,    0, 0 },  1, 8,  0, 0.05, 10); // 0 
	public static final ArmorType LEATHER         = new ArmorType( ArmorType.WeightClass.LIGHT,  "Leather",         new int[] { 0, 10,   0, 0 },  2, 6,  0, 0.10, 15); // 1
	public static final ArmorType STUDDED_LEATHER = new ArmorType( ArmorType.WeightClass.LIGHT,  "Studded Leather", new int[] { 0, 25,   0, 0 },  3, 5, -1, 0.15, 20); // 2
	public static final ArmorType CHAIN_SHIRT     = new ArmorType( ArmorType.WeightClass.LIGHT,  "Chain Shirt",     new int[] { 0, 100,  0, 0 },  4, 4, -2, 0.20, 25); // 3
	public static final ArmorType HIDE            = new ArmorType( ArmorType.WeightClass.MEDIUM, "Hide",            new int[] { 0, 15,   0, 0 },  3, 4, -3, 0.20, 25); // 4
	public static final ArmorType SCALE_MAIL      = new ArmorType( ArmorType.WeightClass.MEDIUM, "Scale Mail",      new int[] { 0, 50,   0, 0 },  4, 3, -4, 0.25, 30); // 5
	public static final ArmorType CHAIN_MAIL      = new ArmorType( ArmorType.WeightClass.MEDIUM, "Chain Mail",      new int[] { 0, 150,  0, 0},   5, 2, -5, 0.30, 40); // 6
	public static final ArmorType BREAST_PLATE    = new ArmorType( ArmorType.WeightClass.MEDIUM, "Breastplate",     new int[] { 0, 200,  0, 0 },  5, 3, -4, 0.25, 30); // 7
	public static final ArmorType SPLINT_MAIL     = new ArmorType( ArmorType.WeightClass.HEAVY,  "Splint Mail",     new int[] { 0, 200,  0, 0 },  6, 0, -7, 0.40, 45); // 8
	public static final ArmorType BANDED_MAIL     = new ArmorType( ArmorType.WeightClass.HEAVY,  "Banded Mail",     new int[] { 0, 250,  0, 0 },  6, 1, -6, 0.35, 35); // 9
	public static final ArmorType HALF_PLATE      = new ArmorType( ArmorType.WeightClass.HEAVY,  "Half Plate",      new int[] { 0, 600,  0, 0 },  7, 0, -7, 0.40, 50); // 10
	public static final ArmorType FULL_PLATE      = new ArmorType( ArmorType.WeightClass.HEAVY,  "Full Plate",      new int[] { 0, 1500, 0, 0 },  8, 1, -6, 0.35, 50); // 11
	public static final ArmorType NONE            = new ArmorType( ArmorType.WeightClass.LIGHT,  "None",            new int[] { 0, 0,    0, 0 },  0, 0,  0, 0.00, 0 ); // 12

	public static final ArmorType getArmorType(int id) {
		switch(id) {
		case 0:  return PADDED;
		case 1:  return LEATHER;
		case 2:  return STUDDED_LEATHER;
		case 3:  return CHAIN_SHIRT;
		case 4:  return HIDE;
		case 5:  return SCALE_MAIL;
		case 6:  return CHAIN_MAIL;
		case 7:  return BREAST_PLATE;
		case 8:  return SPLINT_MAIL;
		case 9:  return BANDED_MAIL;
		case 10: return HALF_PLATE;
		case 11: return FULL_PLATE;
		case 12: return NONE;
		default: return NONE;
		}
	}
	
	// TODO figure out a better way to do this?
	public static final int getId(final String name) {
		int id = -1;
		
		switch(name) {
		case "Padded":          id = 0;  break;
		case "Leather":         id = 1;  break;
		case "Studded Leather": id = 2;  break;
		case "Chain Shirt":     id = 3;  break;
		case "Hide":            id = 4;  break;
		case "Scale Mail":      id = 5;  break;
		case "Chain Mail":      id = 6;  break;
		case "Breastplate":     id = 7;  break;
		case "Splint Mail":     id = 8;  break;
		case "Banded Mail":     id = 9;  break;
		case "Half Plate":      id = 10; break;
		case "Full Plate":      id = 11; break;
		case "None":            id = 12; break;
		default:                id = -1; break;
		}
		
		return id;
	}
}