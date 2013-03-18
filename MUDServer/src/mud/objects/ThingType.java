package mud.objects;

public enum ThingType {
	NONE("None"),   // 0
	CHEST("Chest"); // 1
	
	private String name;
	
	private ThingType(String name) {
		this.name = name;
	}
	
	public static ThingType getType(String typeName) {
		if (typeName.toUpperCase().equals("CHEST")) { return CHEST; }
		//else if (typeName.toUpperCase().equals("NONE")) { return NONE; }
		else { return NONE; }
	}
	
	public String toString() {
		return this.name.toUpperCase();
	}	
}