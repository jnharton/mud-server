package mud.objects.items;
/*
 * longsword: 15g
 * rapier: 20g
 * scimitar: 15g 
 */

public class WeaponType {
	
	public enum DamageType { PIERCING, SLASHING };
	
	private int id;
	private String name;
	private int[] cost;            // weapon base cost
	private DamageType dType;      // damage type
	private String damage;         // damage roll
	
	private boolean critical_hits;
	private int critMin;           // minimum roll for a critical hit
	private int critMax;           // maximum roll for a critical hit
	private int critical;          // damage multiplier for a critical hit
	private double weight;         // base weapon weight
	
	WeaponType(int id, String name, DamageType dType, String damage, int critMin, int critMax, int critical, double weight ) {
		this.name = name;
		this.cost = new int[] { 0, 0, 10, 0 };
		this.dType = dType;
		this.damage = damage;
		this.critMin = critMin;
		this.critMax = critMax;
		this.critical = critical;
		this.weight = weight;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public DamageType getDamageType() {
		return this.dType;
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