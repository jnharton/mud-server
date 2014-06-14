package mud.objects;

public enum CreatureType {
	HORSE("HORSE"),
	NONE("NONE");

	private String name;
	
	CreatureType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
}
