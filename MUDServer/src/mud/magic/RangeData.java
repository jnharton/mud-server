package mud.magic;

import java.util.List;

public class RangeData {
	private final RangeType type;
	private final RangeClass cl;
	
	public RangeData(final RangeType rType, final RangeClass rClass) {
		this.type = rType;
		this.cl = rClass;
	}
	
	public int getRange(int casterLevel) {
		if( type.casterLevels == 0 || type.casterLevels != -1 ) {
			return type.range;
		}
		else {
			return type.range + (casterLevel * type.rangeIncrement);
		}
	}
	
	public boolean isType(final RangeType tt) {
		return this.type == tt;
	}
	
	public RangeType getType() {
		return this.type;
	}
	
	public RangeClass getRClass() {
		return this.cl;
	}
}