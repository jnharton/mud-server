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
import java.util.List;

import mud.utils.Utils;

/**
 * 
 * @author jeremy, joshgit
 *
 */
public enum ObjectFlag
{
	BANK("BANK",                0, TypeFlag.ROOM),
	DARK("DARK",                1, TypeFlag.OBJECT),
	ENTER_OK("ENTER_OK",        2, TypeFlag.OBJECT),
    FORGE("FORGE",              3, TypeFlag.ROOM),
    GUEST("GUEST",              4, TypeFlag.PLAYER),
    HOUSE("HOUSE",              5, TypeFlag.ROOM),
    MERCHANT("MERCHANT",        6, TypeFlag.NPC),
    NO_TELEPORT("NO_TELEPORT",  7, TypeFlag.OBJECT), // prevent teleporting to the location (for rooms) or moving the object (for everything else)
    NO_ENTER("NO_ENTER",        8, TypeFlag.ROOM),
    NO_LEAVE("NO_LEAVE",        9, TypeFlag.ROOM),
    NIL("NIL",                 10, TypeFlag.NOTHING),
    QUIET("QUIET",             11, TypeFlag.OBJECT), 
    SHOP("SHOP",               12, TypeFlag.ROOM),
    SILENT("SILENT",           13, TypeFlag.OBJECT),
    VENDOR("VENDOR",           14, TypeFlag.NPC),
	VIRTUAL("VIRTUAL",         15, TypeFlag.OBJECT);
	
	// 13 flags    
	// 0 1 2 3 5 7 = 0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 18
	// 0000000000000
	// restrict the use of flags to certain types?
	
    private String name;          // name of the flag
    private Integer index;        //
    private List<TypeFlag> types; // types of object this flag can be set on
    
    private ObjectFlag(final String flagName, final Integer index, final TypeFlag...types) {
    	this.name = flagName;
    	this.index = index;
    	this.types = Utils.mkList(types);
    }
    
    public String getName() {
    	return this.name;
    }
    
    public List<TypeFlag> getAllowedTypes() {
    	return this.types;
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
    	case "ENTER_OK":    return ENTER_OK;
    	case "NO_TELEPORT": return NO_TELEPORT;
    	case "NO_ENTER":	return NO_ENTER;
    	case "NO_LEAVE":	return NO_LEAVE;
    	case "SHOP":        return SHOP;
    	case "VIRTUAL":     return VIRTUAL;
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