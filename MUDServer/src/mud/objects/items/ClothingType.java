package mud.objects.items;

public enum ClothingType {
	BELT("Belt"),       // 0
	BOOTS("Boots"),     // 1
	BRACERS("Bracers"), // 2
	CLOAK("Cloak"),     // 3
	GLOVES("Gloves"),   // 4
	PANTS("Pants"),	    // 5
	SHIRT("Shirt"),     // 6
	NONE("None");       // 7
	
	private String name;
	
	private ClothingType(String name) {
		this.name = name;
	}
	
	public String toString() {
		return this.name.toUpperCase();
	}	
}