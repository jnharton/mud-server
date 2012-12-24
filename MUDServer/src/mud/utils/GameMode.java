package mud.utils;

public enum GameMode {
	NORMAL("Normal"),
	WIZARD("Wizard"),
	MAINTENANCE("Maintenance");
	
	private String name;
	
	GameMode(String name) {
		this.name = name;
	}
	
	public String toString() {
		return this.name;
	}
}