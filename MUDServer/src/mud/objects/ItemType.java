package mud.objects;

/*
 * Copyright (c) 2015 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public final class ItemType {
	private String name;
	private Integer id;
	
	public ItemType(final String name, final Integer id) {
		this.name = name;
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	@Override
	public String toString() {
		return this.name.toUpperCase();
	}
}