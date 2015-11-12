package mud.foe;

import mud.misc.SlotType;

public class FOESlotTypes {
	public static SlotType NONE   = new SlotType("None",   0);
	public static SlotType HEAD   = new SlotType("Head",   1);
	public static SlotType EARS   = new SlotType("Ears",   2);
	public static SlotType EYES   = new SlotType("Eyes",   3);
	public static SlotType BODY   = new SlotType("Body",   4);
	public static SlotType BACK   = new SlotType("Back",   5);
	public static SlotType HOOVES = new SlotType("Hooves", 6);
	
	public static SlotType RFHOOF  = new SlotType("RForeHoof", 6);
	public static SlotType LFHOOF  = new SlotType("LForeHoof", 6);
	//public static SlotType RBHOOF  = new SlotType("RBackHoof");
	//public static SlotType LBHOOF  = new SlotType("LBackHoof");
	
	public static SlotType WINGS  = new SlotType("Wings",  7);
	public static SlotType HORN   = new SlotType("Horn",   8);
	
	private FOESlotTypes() {}

	private static SlotType[] types = {
		NONE, HEAD, EARS, EYES, BODY, BACK, HOOVES, RFHOOF, LFHOOF, WINGS, HORN
	};

	public static SlotType getType(final Integer typeId) {
		return types[typeId];
	}

	public static SlotType getType(final String typeName) {
		switch(typeName.toUpperCase()) {
		case "NONE":         return NONE;
		case "HEAD":         return HEAD;
		case "EARS":         return EARS;
		case "EYES":         return EYES;
		case "BODY":         return BODY;
		case "BACK":         return BACK;
		case "HOOVES":       return HOOVES;
		case "RFHOOF":       return RFHOOF;
		case "LFHOOF":       return LFHOOF;
		case "WINGS":        return WINGS;
		case "HORN":         return HORN;
		default:             return NONE;
		}
	}
	
	public static SlotType[] getSlotTypes() {
		return types;
	}
}