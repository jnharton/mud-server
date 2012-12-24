package mud.objects.items;

public enum ClothingType {
	BELT("Belt"),
	BOOTS("Boots"),
	CLOAK("Cloak"),
	GLOVES("Gloves"),
	PANTS("Pants"),	
	SHIRT("Shirt"),
	NONE("None");
	
	private String name;
	
	private ClothingType(String name) {
		this.name = name;
	}
	
	public String toString() {
		return this.name.toUpperCase();
	}	
}