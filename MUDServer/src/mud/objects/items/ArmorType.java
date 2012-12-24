package mud.objects.items;

public enum ArmorType {

	/* Type, Name, Cost, Armor Bonus, Dexterity Bonus, Armor Check, Weight */
	/*               Type         Name               Cost                        AB DB  AC   SPF WHT */
	PADDED(          Type.LIGHT,  "Padded",          new int[] { 0, 0, 5, 0 },    1, 8,  0, 0.05, 10), 
	LEATHER(         Type.LIGHT,  "Leather",         new int[] { 0, 0, 10, 0 },   2, 6,  0, 0.10, 15), 
	STUDDED_LEATHER( Type.LIGHT,  "Studded Leather", new int[] { 0, 0, 25, 0},    3, 5, -1, 0.15, 20),
	CHAIN_SHIRT(     Type.LIGHT,  "Chain Shirt",     new int[] { 0, 0, 100, 0 },  4, 4, -2, 0.20, 25),
	
	HIDE(            Type.MEDIUM, "Hide",            new int[] { 0, 0, 15, 0 },   3, 4, -3, 0.20, 25),
	SCALE_MAIL(      Type.MEDIUM, "Scale Mail",      new int[] { 0, 0, 50, 0 },   4, 3, -4, 0.25, 30),
	CHAIN_MAIL(      Type.MEDIUM, "Chain Mail",      new int[] { 0, 0, 150, 0},   5, 2, -5, 0.30, 40),
	BREAST_PLATE(    Type.MEDIUM, "Breastplate",     new int[] { 0, 0, 200, 0 },  5, 3, -4, 0.25, 30),
	
	SPLINT_MAIL(     Type.HEAVY,  "Splint Mail",     new int[] { 0, 0, 200, 0 },  6, 0, -7, 0.40, 45),
	BANDED_MAIL(     Type.HEAVY,  "Banded Mail",     new int[] { 0, 0, 250, 0 },  6, 1, -6, 0.35, 35),
	HALF_PLATE(      Type.HEAVY,  "Half Plate",      new int[] { 0, 0, 600, 0 },  7, 0, -7, 0.40, 50),
	FULL_PLATE(      Type.HEAVY,  "Full Plate",      new int[] { 0, 0, 1500, 0 }, 8, 1, -6, 0.35, 50);

	public static enum Type { LIGHT, MEDIUM, HEAVY };
	
	private Type type;
	private String name;
	private int armBonus;
	private int dexBonus;
	private int ac;
	private double spellFailure; // percentage: 0.95 = 95%
	private double weight;

	ArmorType(Type type, String name, int[] cost, int armBonus, int dexBonus, int ac, double spellFailure, double weight) {
		this.type = type;
		this.name = name;
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
	
	public int getArmBonus() {
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