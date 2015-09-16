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
	
	public static int getDirection(final String direction) {
		int result = NONE.value;
		
		switch(direction.toLowerCase()) {
		case "north":     result = NORTH.value;     break;
		case "northeast": result = NORTHEAST.value; break;
		case "northwest": result = NORTHWEST.value; break;
		case "south":     result = SOUTH.value;     break;
		case "southeast": result = SOUTHEAST.value; break;
		case "southwest": result = SOUTHWEST.value; break;
		case "east":      result = EAST.value;      break;
		case "west":      result = WEST.value;      break;
		default:          result = NONE.value;      break;
		}
		
		return result;
	}
}