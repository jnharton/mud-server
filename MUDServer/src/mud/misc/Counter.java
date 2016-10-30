package mud.misc;

import mud.utils.Utils;

public class Counter {
	private Integer value;
	
	private boolean using_bounds;
	
	private Integer min;
	private Integer max;
	
	public Counter() {
		this(0, -1, -1);
	}
	
	public Counter(final Integer initialValue) {
		this(initialValue, -1, -1);
	}
	
	public Counter(final Integer initialValue, final Integer minValue, final Integer maxValue) {
		this.value = initialValue;
		
		this.min = minValue;
		this.max = maxValue;
		
		if( this.min == -1 || this.max == -1 ) {
			this.using_bounds = false;
		}
		else this.using_bounds = true;
	}
	
	public Integer getValue() {
		return this.value;
	}
	
	public boolean isMax() {
		return (this.value == this.max);
	}
	
	public void reset() {
		if( this.using_bounds ) this.value = 0;
		else                    this.value = this.min;
	}
	
	public void setValue(final Integer value) {
		if( using_bounds ) {
			if( Utils.range(value, this.min, this.max) ) {
				this.value = value;
			}
		}
		else this.value = value;
	}
	
	public void increment() {
		if( using_bounds ) {
			if( Utils.range(value + 1, this.min, this.max) ) {
				this.value++;
			}
		}
		else this.value++;
	}
	
	public void decrement() {
		if( using_bounds ) {
			if( Utils.range(value - 1, this.min, this.max) ) {
				this.value--;
			}
		}
		else this.value--;
	}
}