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

import java.util.EnumSet;

/**
 * 
 * @author jeremy, joshgit
 *
 */
public enum ObjectFlag
{
	BANK("BANK"),
	DARK("DARK"),
    FORGE("FORGE", TypeFlag.ROOM),
    GUEST("GUEST"),
    HOUSE("HOUSE"),
    MERCHANT("MERCHANT"),
    NO_TELEPORT("NO_TELEPORT"), // prevent teleporting to the location (for rooms) or moving the object (for everything else)
    NO_ENTER("NO_ENTER", TypeFlag.ROOM),
    NO_LEAVE("NO_LEAVE", TypeFlag.ROOM),
    QUIET("QUIET"),
    SHOP("SHOP", TypeFlag.ROOM),
    SILENT("SILENT"),
    VENDOR("VENDOR"),
	VIRTUAL("VIRTUAL");
    
    private String name;   // name of the flag
    private TypeFlag type; // type of object this flag can be set on
    
    ObjectFlag(String flagName) {
    	this.name = flagName;
    }
    
    ObjectFlag(String flagName, TypeFlag type) {
    	this.name = flagName;
    	this.type = type;
    }

    static public ObjectFlag fromLetter(final char c) {
        switch (c) {
        case 'B':    return BANK;
        case 'D':    return DARK;
        case 'G':    return GUEST;
        case 'F':    return FORGE;
        case 'H':    return HOUSE;
        case 'M':	 return MERCHANT;
        case 'S':    return SILENT;
        case 'Q':    return QUIET;
        case 'V':    return VENDOR;
        default:    throw new IllegalArgumentException("Invalid ObjectFlag letter: " + c);    
        }
    }
    
    static public ObjectFlag fromString(final String s) {
    	switch(s.toUpperCase()) {
    	case "NO_ENTER":	return NO_ENTER;
    	case "NO_LEAVE":	return NO_LEAVE;
    	default:			throw new IllegalArgumentException("Invalid ObjectFlag string: " + s);
    	}
    }

    static public EnumSet<ObjectFlag> getFlagsFromString(final String str) {
        final EnumSet<ObjectFlag> flags = EnumSet.noneOf(ObjectFlag.class);
        for (final Character ch : str.toCharArray()) {
            if (Character.isLetter(ch)) {
                flags.add(ObjectFlag.fromLetter(ch));
            }
        }
        return flags;
    }

    static public String toInitString(final EnumSet<ObjectFlag> set) {
    	if( set != null ) {
    		final StringBuilder buf = new StringBuilder();
    		
    		for (final ObjectFlag flag : set) {
    			buf.append(" ").append(flag.toString());
    		}
    		
    		return buf.length() < 1 ? buf.toString() : buf.toString().substring(1);
    	}
    	
    	return "";
    }

    static public String firstInit(final EnumSet<ObjectFlag> set) {
        return set.isEmpty() ? "" : set.iterator().next().toString();
    }
}