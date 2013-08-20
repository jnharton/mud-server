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

import java.util.EnumSet;

/**
 * These "flags" are solely for typing, not for modification
 * of behavior, visibility, or other checks.
 * 
 * @author Jeremy
 *
 */
public enum TypeFlag
{
	EXIT("EXIT"),
	ITEM("ITEM"),
	NPC("NPC"),
	NONE("NONE"),
    PLAYER("PLAYER"),
	ROOM("ROOM"),
	THING("THING");
	
	private String name;
    
    TypeFlag(String flagName) {
    	this.name = flagName;
    }
    
    static public TypeFlag fromLetter(final char c) {
    	switch(c) {
    	case 'E':	return EXIT;
    	case 'I':	return ITEM;
    	case 'N':	return NPC;
    	case 'P':	return PLAYER;
    	case 'R':	return ROOM;
    	case 'T':	return THING;
    	default:    throw new IllegalArgumentException("Invalid TypeFlag letter: " + c);    
    	}
    }
    
    static public EnumSet<TypeFlag> getFlagsFromString(final String str) {
        final EnumSet<TypeFlag> flags = EnumSet.noneOf(TypeFlag.class);
        for (final Character ch : str.toCharArray()) {
            if (Character.isLetter(ch)) {
                flags.add(TypeFlag.fromLetter(ch));
            }
        }
        return flags;
    }

    static public String toInitString(final EnumSet<TypeFlag> set) {
        final StringBuilder buf = new StringBuilder();
        for (final TypeFlag flag : set) {
            buf.append(" ").append(flag.toString());
        }
        return buf.length() < 1 ? buf.toString() : buf.toString().substring(1);
    }

    static public String firstInit(final EnumSet<TypeFlag> set) {
        return set.isEmpty() ? "" : set.iterator().next().toString();
    }
}