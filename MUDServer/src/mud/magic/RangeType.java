package mud.magic;

public enum RangeType {
	STD("Standard", 0, -1, -1),         // ?
	PERSONAL("Personal", 0, 0, -1),     // yourself only, no increment, no caster level
	TOUCH("Touch", 0, 0, 0),            // requires touch (0ft.), no increment, no caster level
	CLOSE("Close", 25, 5, 2),           // close range (25ft.), increment of 5ft./2 caster levels
	MEDIUM("Medium", 100, 10, 1),       // medium range (100ft.), increment of 10ft./caster level
	LONG("Long", 400, 40, 1),           // long range (400ft.), increment of 40ft./caster level
	UNLIMITED("Unlimited", -1, -1, -1); // no limit on range, no increment, not affected by caster level
	
	public String typeName;
	public int range, rangeIncrement, casterLevels;
	
	RangeType(String typename, int range, int rInc, int casterlevels) {
		this.typeName = typename;
		this.range = range;
		this.rangeIncrement = rInc;
		this.casterLevels = casterlevels;
	}
}