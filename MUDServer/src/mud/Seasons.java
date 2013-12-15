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

import java.util.LinkedList;

import mud.weather.Season;

/**
 * Enumeration of Seasons with the months
 * that they start and end
 * 
 * @author Jeremy
 *
 */
public class Seasons
{
	public static final Season SPRING = new Season("Spring", 3, 6);
	public static final Season SUMMER = new Season("Summer", 6, 9);
	public static final Season AUTUMN = new Season("Autumn", 9, 12);
	public static final Season WINTER = new Season("Winter", 12, 3);
	
	public static final LinkedList<Season> seasons = new LinkedList<Season>() {
		{ add(SPRING); add(SUMMER); add(AUTUMN); add(WINTER); }
	};
	
	public static LinkedList<Season> getSeasons() {
		return seasons;
	}
	
	public static Season fromStringLower(final String str) {
        if ( str.equals("spring") ) {       return SPRING; }
        else if ( str.equals("summer") ) {  return SUMMER; }
        else if ( str.equals("autumn") ) {  return AUTUMN; }
        else if ( str.equals("winter") ) {  return WINTER; }
        else return null;
    }
}