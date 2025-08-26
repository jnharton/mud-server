package mud.misc;

public enum Attribute {	
	FLAMING("flaming"),
	GLOWING("glowing"),
	RUSTY("rusty");
	
	private String name;
	
	Attribute(String name) {
		this.name = name;
	}
}