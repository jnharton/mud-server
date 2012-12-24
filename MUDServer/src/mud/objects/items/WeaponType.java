package mud.objects.items;
/*
 * longsword: 15g
 * rapier: 20g
 * scimitar: 15g 
 */

public enum WeaponType {
	LONGSWORD("Long Sword", "1d6", 19, 20, 2, 2.0, AttackType.SLASHING),
	RAPIER("Rapier", "1d4", 18, 20, 2, 2.0, AttackType.PIERCING),
	SCIMITAR("Scimitar", "1d4", 18, 20, 2, 4.0, AttackType.SLASHING);
	
	public enum AttackType { PIERCING, SLASHING; }
	
	private String name;
	private String damage;     // damage roll
	private int critMin;       // minimum roll for a critical hit
	private int critMax;       // maximum roll for a critical hit
	private int critical;      // damage multiplier for a critical hit
	private double weight;     // base weapon weight
	private AttackType aType;  // attack type
	
	WeaponType(String name, String damage, int critMin, int critMax, int critical, double weight, AttackType aType ) {
		this.name = name;
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
	
	public AttackType getAttackType() {
		return this.aType;
	}
}