package mud.objects.items;
/*
 * longsword: 15g
 * rapier: 20g
 * scimitar: 15g 
 */

public enum WeaponType {
	LONGSWORD("Long Sword", AttackType.SLASHING, "1d6", 19, 20, 2, 2.0),
	RAPIER("Rapier", AttackType.PIERCING, "1d4", 18, 20, 2, 2.0),
	SCIMITAR("Scimitar", AttackType.SLASHING, "1d4", 18, 20, 2, 4.0),
	REVOLVER("Revolver", null, "10", -1, -1, 1, 3.0);
	
	public enum AttackType { PIERCING, SLASHING; }
	
	private String name;
	private AttackType aType;  // attack type
	private int[] cost;        // weapon base cost
	private String damage;     // damage roll
	private int critMin;       // minimum roll for a critical hit
	private int critMax;       // maximum roll for a critical hit
	private int critical;      // damage multiplier for a critical hit
	private double weight;     // base weapon weight
	
	WeaponType(String name, AttackType aType, String damage, int critMin, int critMax, int critical, double weight ) {
		this.name = name;
		this.cost = new int[] { 0, 0, 10, 0 };
		this.damage = damage;
		this.critMin = critMin;
		this.critMax = critMax;
		this.critical = critical;
		this.weight = weight;
		this.aType = aType;
		
	}
	
	public String getName() {
		return this.name;
	}
	
	public AttackType getAttackType() {
		return this.aType;
	}
	
	public int[] getCost() {
		return this.cost;
	}
	
	public String getDamage() {
		return this.damage;
	}
	
	public int getCritMin() {
		return this.critMin;
	}
	
	public int getCritMax() {
		return this.critMax;
	}
	
	public int getCritical() {
		return this.critical;
	}
	
	public double getWeight() {
		return this.weight;
	}
}