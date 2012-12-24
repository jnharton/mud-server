package mud.magic;

public class Reagent {
	private String name;
	
	public Reagent(String tName) {
		this.name = tName;
	}
	
	public void setName(String tName) {
		this.name = tName;
	}
	
	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name;
	}
}