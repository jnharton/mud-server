package mud.objects.items;

public enum ArmorType {

	/* Type, Name, Cost, Armor Bonus, Dexterity Bonus, Armor Check, Speed Factor, Weight */
	/*               Type         Name               Cost        P  G     S  C     AB DB  AC SPF   WHT */
	PADDED(          Type.LIGHT,  "Padded",          new int[] { 0, 5,    0, 0 },  1, 8,  0, 0.05, 10), // 0 
	LEATHER(         Type.LIGHT,  "Leather",         new int[] { 0, 10,   0, 0 },  2, 6,  0, 0.10, 15), // 1
	STUDDED_LEATHER( Type.LIGHT,  "Studded Leather", new int[] { 0, 25,   0, 0 },  3, 5, -1, 0.15, 20), // 2
	CHAIN_SHIRT(     Type.LIGHT,  "Chain Shirt",     new int[] { 0, 100,  0, 0 },  4, 4, -2, 0.20, 25), // 3
	
	HIDE(            Type.MEDIUM, "Hide",            new int[] { 0, 15,   0, 0 },  3, 4, -3, 0.20, 25), // 4
	SCALE_MAIL(      Type.MEDIUM, "Scale Mail",      new int[] { 0, 50,   0, 0 },  4, 3, -4, 0.25, 30), // 5
	CHAIN_MAIL(      Type.MEDIUM, "Chain Mail",      new int[] { 0, 150,  0, 0},   5, 2, -5, 0.30, 40), // 6
	BREAST_PLATE(    Type.MEDIUM, "Breastplate",     new int[] { 0, 200,  0, 0 },  5, 3, -4, 0.25, 30), // 7
	
	SPLINT_MAIL(     Type.HEAVY,  "Splint Mail",     new int[] { 0, 200,  0, 0 },  6, 0, -7, 0.40, 45), // 8
	BANDED_MAIL(     Type.HEAVY,  "Banded Mail",     new int[] { 0, 250,  0, 0 },  6, 1, -6, 0.35, 35), // 9
	HALF_PLATE(      Type.HEAVY,  "Half Plate",      new int[] { 0, 600,  0, 0 },  7, 0, -7, 0.40, 50), // 10
	FULL_PLATE(      Type.HEAVY,  "Full Plate",      new int[] { 0, 1500, 0, 0 },  8, 1, -6, 0.35, 50), // 11
	NONE(            Type.LIGHT,  "None",            new int[] { 0, 0,    0, 0 },  0, 0,  0, 0.00, 0 ); // 12

	public static enum Type { LIGHT, MEDIUM, HEAVY };
	
	private Type type;
	private String name;
	private int[] cost;
	private int armBonus;
	private int dexBonus;
	private int ac;
	private double spellFailure; // percentage: 0.95 = 95%
	private double weight;

	ArmorType(Type type, String name, int[] cost, int armBonus, int dexBonus, int ac, double spellFailure, double weight) {
		this.type = type;
		this.name = name;
		this.cost = cost;
		this.armBonus = armBonus;
		this.dexBonus = dexBonus;
		this.ac = ac;
		this.spellFailure = spellFailure;
		this.weight = weight;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int[] getCost() {
		return this.cost;
	}
	
	public int getArmorBonus() {
		return this.armBonus;
	}
	
	public int getDexBonus() {
		return this.dexBonus;
	}
	
	public int getAC() {
		return this.ac;
	}
	
	public double getSpellFailure() {
		return this.spellFailure;
	}

	public double getWeight() {
		return this.weight;
	}
	
	public String toString() {
		return this.name;
	}
}
