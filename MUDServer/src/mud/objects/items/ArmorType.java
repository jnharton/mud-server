package mud.objects.items;

public class ArmorType {
	public static enum WeightClass { LIGHT, MEDIUM, HEAVY }
	
	private WeightClass type;
	private String name;
	private int[] cost;
	private int armBonus;
	private int dexBonus;
	private int ac;
	private double spellFailure; // percentage: 0.95 = 95%
	private double weight;

	ArmorType(WeightClass type, String name, int[] cost, int armBonus, int dexBonus, int ac, double spellFailure, double weight) {
		this.type = type;
		this.name = name;
		this.cost = cost;
		this.armBonus = armBonus;
		this.dexBonus = dexBonus;
		this.ac = ac;
		this.spellFailure = spellFailure;
		this.weight = weight;
	}
	
	public WeightClass getType() {
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
