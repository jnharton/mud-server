package mud.objects.items;

public enum ClothingType {
	BELT("Belt"),     // 0
	BOOTS("Boots"),   // 1
	CLOAK("Cloak"),   // 2
	GLOVES("Gloves"), // 3
	PANTS("Pants"),	  // 4
	SHIRT("Shirt"),   // 5
	NONE("None");     // 6
	
	private String name;
	
	private ClothingType(String name) {
		this.name = name;
	}
	
	public String toString() {
		return this.name.toUpperCase();
	}	
}