package mud.utils;

public class Position {
	private final String name;
	private final String description;
	
	public Position(final String name, final String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
}