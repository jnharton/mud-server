package mud.objects.items;

public enum ShieldType {
	BUCKLER("Buckler"), // 0
	LIGHT("Light"),     // 1
	MEDIUM("Medium"),   // 2
	Heavy("Heavy"),     // 3
	TOWER("Tower");     // 4
	
	private String name;
	
	private ShieldType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}