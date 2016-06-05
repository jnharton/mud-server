package mud.foe;

import mud.objects.ThingType;
import mud.objects.ThingTypes;

public final class FOEThingTypes {
	public static final ThingType VENDING_MACHINE = new ThingType("Vending Machine", 16);
	public static final ThingType TERMINAL = new ThingType("Terminal", 17);

	private FOEThingTypes() {}

	private static final ThingType[] types = {
			VENDING_MACHINE, TERMINAL
	};

	public static final ThingType getType(final Integer typeId) {
		return types[typeId - 16];
	}

	public static final ThingType getType(final String typeName) {
		switch(typeName.toUpperCase().replace(' ', '_')) {
		case "VENDING_MACHINE": return VENDING_MACHINE;
		case "TERMINAL":        return TERMINAL;
		default:                return ThingTypes.NONE;
		}
	}

	public static final ThingType[] getThingTypes() {
		return FOEThingTypes.types;
	}
}