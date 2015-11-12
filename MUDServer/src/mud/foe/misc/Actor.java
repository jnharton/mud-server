package mud.foe.misc;

public class Actor {
	private String name;
	private String description;
	
	public Actor(final String name, final String description) {
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