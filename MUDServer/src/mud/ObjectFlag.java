package mud;

import java.util.EnumSet;

public enum ObjectFlag
{
    ADMIN("ADMIN"),
    BUILDER("BUILDER"),
    COMPLEX("COMPLEX"),
    DARK("DARK"),
    EXIT("EXIT"),
    GUEST("GUEST"),
    HOUSE("HOUSE"),
    ITEM("ITEM"),
    MAINTENANCE("MAINTENANCE"),
    NPC("NPC"),
    PLAYER("PLAYER"),
    ROOM("ROOM"),
    SILENT("SILENT"),
    THING("THING"),
    WIZARD("WIZARD"),
    VENDOR("VENDOR"),
    ZONE("ZONE");
    
    private String name;
    
    ObjectFlag(String flagName) {
    	this.name = flagName;
    }

    static public ObjectFlag fromLetter(final char c) {
        switch (c) {
        case 'A':    return ADMIN;
        case 'B':    return BUILDER;
        case 'C':    return COMPLEX; // added from Creature, so could actually be CREATURE instead, maybe
        case 'D':    return DARK;
        case 'E':    return EXIT;
        case 'G':    return GUEST;
        case 'H':    return HOUSE;
        case 'I':    return ITEM;
        case 'M':    return MAINTENANCE;
        case 'N':    return NPC;
        case 'P':    return PLAYER;
        case 'R':    return ROOM;
        case 'S':    return SILENT;
        case 'T':    return THING;
        case 'W':    return WIZARD;
        case 'V':    return VENDOR;
        case 'Z':    return ZONE;
        default:    throw new IllegalArgumentException("Invalid ObjectFlag letter: " + c);    
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
        final StringBuilder buf = new StringBuilder();
        for (final ObjectFlag flag : set) {
            buf.append(" ").append(flag.toString());
        }
        return buf.length() < 1 ? buf.toString() : buf.toString().substring(1);
    }

    static public String firstInit(final EnumSet<ObjectFlag> set) {
        return set.isEmpty() ? "" : set.iterator().next().toString();
    }
}