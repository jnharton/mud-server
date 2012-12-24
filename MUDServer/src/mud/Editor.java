package mud;

public enum Editor {
	AREA("Area Editor"),
	DESC("Desc Editor"),
	CHARGEN("Character Generation"),
	HELP("Help Editor"),
	INTCAST("Interactive Casting"),
	ITEM("Item Editor"),
	LIST("List Editor"),
	MAIL("Mail Editor"),
	ROOM("Room Editor"),
	NONE("None");
	
	private String name;
	
	private Editor(String editorName) {
		this.name = editorName;
	}
	
	public String getName() {
		return this.name;
	}
}