package mud.objects;

/**
 * Describes the type of room that this is, with relation
 * to whether it is affected by the outdoors and to what degree.
 * 
 * @author Jeremy
 *
 */
public enum RoomType {
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
	
	public String getString(char flag) {
		switch(flag) {
		case 'E':
			return EXPOSED.stringRep;
		case 'I':
			return INSIDE.stringRep;
		case 'O':
			return OUTSIDE.stringRep;
		case 'N':
			return NONE.stringRep;
		default:
			return ""; 
		}
	}
	
	@Override
	public String toString() {
		return this.stringRep;
	}
}
