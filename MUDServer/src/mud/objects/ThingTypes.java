package mud.objects;

public final class ThingTypes {
	public static final ThingType NONE      = new ThingType("None", 0);      // 0
	public static final ThingType CONTAINER = new ThingType("Container", 1); // 1
	
	private ThingTypes() {}
	
	private static ThingType[] types = {
			NONE, CONTAINER
	};
	
	public static ThingType getType(final Integer typeId) {
		// TODO fix this? does it need fixing? is it kludgy?
		if( typeId <= types.length - 1 ) {
			return types[typeId];
		}
		else return null;
	}
	
	public static ThingType getType(final String typeName) {
		ThingType tt;
		
		switch(typeName.toUpperCase()) {
		case "CONTAINER": tt = CONTAINER;
		default:          tt = NONE;
		}
		
		return tt;
	}
}