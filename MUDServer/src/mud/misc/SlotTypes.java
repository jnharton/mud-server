package mud.misc;

import mud.objects.ItemType;

public class SlotTypes {
	public static SlotType NONE   = new SlotType("None",    0);
	public static SlotType HEAD   = new SlotType("Head",    1);
	public static SlotType NECK   = new SlotType("Neck",    2);
	public static SlotType BODY   = new SlotType("Body",    3);
	public static SlotType CHEST  = new SlotType("Chest",   4);
	public static SlotType BACK   = new SlotType("Back",    5);
	public static SlotType ARMS   = new SlotType("Arms",    6);
	public static SlotType LHAND  = new SlotType("LHand",   7);
	public static SlotType RHAND  = new SlotType("RHand",   8);
	public static SlotType HANDS  = new SlotType("Hands",   9);
	public static SlotType FINGER = new SlotType("Finger", 10);
	public static SlotType WAIST  = new SlotType("Waist",  11);
	public static SlotType LEGS   = new SlotType("Legs",   12);
	public static SlotType FEET   = new SlotType("Feet",   13);
	
	private SlotTypes() {}
	
	private static SlotType[] types = {
		NONE,  HEAD,  NECK,   BODY,  CHEST, BACK, ARMS,
		LHAND, RHAND, HANDS, FINGER, WAIST, LEGS,  FEET
	};
	
	public static SlotType getType(Integer typeId) {
		// TODO fix this? does it need fixing? is it kludgy?
		if( typeId <= types.length - 1 ) {
			return types[typeId];
		}
		else return null;
	}
	
	public static SlotType getType(String typeName) {
		switch(typeName.toUpperCase()) {
		case "NONE":      return NONE;
		case "HEAD":      return HEAD;
		case "NECK":      return NECK;
		case "BODY":      return BODY;
		case "CHEST":     return CHEST;
		case "BACK":      return BACK;
		case "ARMS":      return ARMS;
		case "LHAND":     return LHAND;
		case "RHAND":     return RHAND;
		case "HANDS":     return HANDS;
		case "FINGER":    return FINGER;
		case "WAIST":     return WAIST;
		case "LEGS":      return LEGS;
		case "FEET":      return FEET;
		default:          return NONE;
		}
	}
}