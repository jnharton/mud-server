package mud;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public enum Editor {
	AREA("Area Editor"),
	DESC("Desc Editor"),
	CHARGEN("Character Generation"),
	HELP("Help Editor"),
	INTCAST("Interactive Casting"),
	ITEM("Item Editor"),
	LIST("List Editor"),
	MAIL("Mail Editor"),
	QUEST("Quest Editor"),
	ROOM("Room Editor"),
	SKILL("Skill Editor"),
	NONE("None");
	
	private String name;
	
	private Editor(String editorName) {
		this.name = editorName;
	}
	
	public String getName() {
		return this.name;
	}
}