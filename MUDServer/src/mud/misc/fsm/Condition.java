package mud.misc.fsm;

final class Condition {
	// >, <, =, >=, <=
	// gt, lt, eq, ge, le
	
	public static final String GT = ">";
	public static final String LT = "<";
	public static final String EQ = "=";
	public static final String GE = ">=";
	public static final String LE = "<=";
	
	private String comparison;
	
	private Integer value1;
	private Integer value2;
	
	public Condition(final String comparison, final  Integer value1, final  Integer value2) {
		this.comparison = comparison;
		this.value1 = value1;
		this.value2 = value2;
	}
	
	public boolean check() {
		return check(comparison, value1, value2);
	}
	
	private static boolean check(String comparison, Integer value1, Integer value2) {
		boolean result = false;
		
		switch(comparison) {
		case GT:
			if( value1 > value2 ) result = true;
			break;
		case LT:
			if( value1 < value2 ) result = true;
			break;
		case EQ:
			if( value1 == value2) result = true;
			break;
		case GE:
			if( value1 >= value2 ) result = true;
			break;
		case LE:
			if( value1 <= value2 ) result = true;
			break;
		default:
			break;
		}
		
		return result;
	}
}