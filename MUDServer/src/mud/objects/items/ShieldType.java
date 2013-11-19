package mud.objects.items;

public enum ShieldType {
	/* Type, Name, Cost, Shield Bonus, Weight */
	/*       Type         Name       Cost        P  G  S   C   SB WHT */
	BUCKLER( Type.LIGHT,  "Buckler", new int[] { 0, 0, 5,  0}, 1, 5),  // 0
	LIGHT(   Type.LIGHT,  "Light",   new int[] { 0, 0, 10, 0}, 3, 10), // 1
	MEDIUM(  Type.MEDIUM, "Medium",  new int[] { 0, 0, 20, 0}, 5, 20), // 2
	Heavy(   Type.HEAVY,  "Heavy",   new int[] { 0, 0, 40, 0}, 7, 30), // 3
	TOWER(   Type.TOWER,  "Tower",   new int[] { 0, 0, 80, 0}, 9, 40); // 4
	
	public static enum Type { LIGHT, MEDIUM, HEAVY, TOWER };
	
	private Type type;
	private String name;
	private int[] cost;
	private int shdBonus;
	private double weight;
	
	private ShieldType(Type type, String name, int[] cost, int sb, int weight) {
		this.type = type;
		this.name = name;
		this.cost = cost;
		this.shdBonus = sb;
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
	
	public int getShieldBonus() {
		return shdBonus;
	}
	
	public double getWeight() {
		return this.weight;
	}
	
	public String toString() {
		return this.name;
	}
}