package mud;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Ability {
	private String name; // ability name
	private String abrv; // ability abbreviation
	private int id;      // ability id
	
	/* name is stored in all caps */
	/* ex. new Ability("strength", 5);, using getName() would get you "STRENGTH" */
	public Ability(String name, String abrv, int id) {
		this.name = name.toUpperCase();
		this.abrv = abrv.toUpperCase();
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getAbrv() {
		return this.abrv;
	}
	
	public int getId() {
		return this.id;
	}
	
	
	public String toString() {
		return this.getName();
	}
}