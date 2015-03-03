package mud.misc;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public enum Editors {
	AREA("Area Editor"),
	DESC("Desc Editor"),
	CHARGEN("Character Generation"),
	CREATURE("Creature Editor"),
	HELP("Help Editor"),
	INTCAST("Interactive Casting"),
	INPUT("Interactive Input"),
	ITEM("Item Editor"),
	LIST("List Editor"),
	MAIL("Mail Editor"),
	QUEST("Quest Editor"),
	ROOM("Room Editor"),
	SKILL("Skill Editor"),
	ZONE("Zone Editor"),
	NONE("None");
	
	private String name;
	
	private Editors(String editorName) {
		this.name = editorName;
	}
	
	public String getName() {
		return this.name;
	}
}