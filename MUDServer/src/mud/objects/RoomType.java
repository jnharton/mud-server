package mud.objects;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * Describes the type of room that this is, with relation
 * to whether it is affected by the outdoors and to what degree.
 * 
 * @author Jeremy
 *
 */
public enum RoomType {
    PLAYER('P', "PLAYER"),
	OUTSIDE('O', "OUTSIDE"), // Affected by all weather
	EXPOSED('E', "EXPOSED"), // Exposed to some weather
	INSIDE('I', "INSIDE"),   // Not affected by any weather
	NONE('N', "NONE");       // Same effect as inside for weather purposes
	
	private char flag;
	private String stringRep;
	
	RoomType(char flag, String stringRep) {
		this.flag = flag;
		this.stringRep = stringRep;
	}
	
	static public RoomType fromLetter(char flag) {
		switch(flag) {
		case 'E':
			return EXPOSED;
		case 'I':
			return INSIDE;
		case 'O':
			return OUTSIDE;
		case 'N':
			return NONE;
		default:
			return NONE;
		}
	}
	
	@Override
	public String toString() {
		return this.stringRep;
	}
}
