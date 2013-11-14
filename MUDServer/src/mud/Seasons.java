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

/**
 * Enumeration of Seasons with the months
 * that they start and end
 * 
 * @author Jeremy
 *
 */
public enum Seasons
{
	SPRING("Spring", 3, 6),
	SUMMER("Summer", 6, 9),
	AUTUMN("Autumn", 9, 12),
	WINTER("Winter", 12, 3);

	private String name;
	public int beginMonth;
	public int endMonth;

	private Seasons(String name, int beginMonth, int endMonth) {
		this.name = name;
		this.beginMonth = beginMonth;
		this.endMonth = endMonth;
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name;
	}
    
    static public Seasons fromStringLower(final String str) {
        if ( str.equals("spring") ) {       return SPRING; }
        else if ( str.equals("summer") ) {  return SUMMER; }
        else if ( str.equals("autumn") ) {  return AUTUMN; }
        else if ( str.equals("winter") ) {  return WINTER; }
        else return null;
    }

}