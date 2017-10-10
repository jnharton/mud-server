package mud;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * These "flags" are solely for typing, not for modification
 * of behavior, visibility, or other checks.
 * 
 * @author Jeremy
 *
 */
public enum TypeFlag
{
	CREATURE("CREATURE", 'C'),
	EXIT("EXIT", 'E'),
	ITEM("ITEM", 'I'),
	NOTHING("NOTHING", 'X'),
	NPC("NPC", 'N'),
	OBJECT("OBJECT", 'O'),
    PLAYER("PLAYER", 'P'),
	ROOM("ROOM", 'R'),
	THING("THING", 'T'),
	ZONE("ZONE", 'Z');
	
	private String name;
	private char flag;
    
    TypeFlag(final String flagName, final char flagChar) {
    	this.name = flagName;
    	this.flag = flagChar;
    }
    
    public String getName() {
    	return this.name;
    }
    
    static public TypeFlag fromLetter(final char c) {
    	switch(c) {
    	case 'C':	return CREATURE;
    	case 'E':	return EXIT;
    	case 'I':	return ITEM;
    	case 'N':	return NPC;
    	case 'P':	return PLAYER;
    	case 'R':	return ROOM;
    	case 'T':	return THING;
    	case 'Z':    return ZONE;
    	default:    throw new IllegalArgumentException("Invalid TypeFlag letter: " + c);    
    	}
    }
    
    static public char asLetter(final TypeFlag flag) {
    	return flag.flag;
    }
    
    public String toString() {
    	return "" + this.flag;
    }
}