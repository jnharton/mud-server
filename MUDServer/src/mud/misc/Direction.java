package mud.misc;

public class Direction {
	public static final int NONE      = 0;
	public static final int NORTH     = 1;
	public static final int NORTHEAST = 3;
	public static final int NORTHWEST = 4;
	public static final int SOUTH     = 5;
	public static final int SOUTHEAST = 6;
	public static final int SOUTHWEST = 7;
	public static final int EAST      = 8;
	public static final int WEST      = 9;
	
	public static int getDirection(final String direction) {
		int result = NONE;
		
		switch(direction.toLowerCase()) {
		case "north":     result = NORTH;     break;
		case "northeast": result = NORTHEAST; break;
		case "northwest": result = NORTHWEST; break;
		case "south":     result = SOUTH;     break;
		case "southeast": result = SOUTHEAST; break;
		case "southwest": result = SOUTHWEST; break;
		case "east":      result = EAST;      break;
		case "west":      result = WEST;      break;
		default:          result = NONE;      break;
		}
		
		return result;
	}
}