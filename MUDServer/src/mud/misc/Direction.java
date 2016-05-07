package mud.misc;

public class Direction {
	public static final Direction NONE      = new Direction(0);
	public static final Direction NORTH     = new Direction(1);
	public static final Direction NORTHEAST = new Direction(3);
	public static final Direction NORTHWEST = new Direction(4);
	public static final Direction SOUTH     = new Direction(5);
	public static final Direction SOUTHEAST = new Direction(6);
	public static final Direction SOUTHWEST = new Direction(7);
	public static final Direction EAST      = new Direction(8);
	public static final Direction WEST      = new Direction(9);
	
	private Integer value;
	
	public Direction(final Integer direction) {
		this.value = direction;
	}
	
	public Integer getValue() {
		return this.value;
	}
	
	public static Direction getDirection(final String direction) {
		Direction dir = Direction.NONE;
		
		switch(direction.toLowerCase()) {
		case "north":     dir = Direction.NORTH;     break;
		case "northeast": dir = Direction.NORTHEAST; break;
		case "northwest": dir = Direction.NORTHWEST; break;
		case "south":     dir = Direction.SOUTH;     break;
		case "southeast": dir = Direction.SOUTHEAST; break;
		case "southwest": dir = Direction.SOUTHWEST; break;
		case "east":      dir = Direction.EAST;      break;
		case "west":      dir = Direction.WEST;      break;
		default:          dir = Direction.NONE;      break;
		}
		
		return dir;
	}
}