package mud;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public final class Constants {
	// Configuration Values
	public static final int MAX_SKILL = 50;
	public static final int MAX_STACK_SIZE = 25; // generic maximum for all Stackable items (should this be in the stackable interface?)
	
	// Movement Speed (move to another constants class - Speed?)
	public static final int WALK = 1;
	public static final int RUN = 3;
	
	// Permissions (move to another constants class - Perms?)
	public static final int USER = 0;   // limited permissions, no @commands at all
	public static final int BUILD = 1;  // building
	public static final int ADMIN = 2;  // account administration?
	public static final int WIZARD = 3; // Most permissions
	public static final int GOD = 4;    // Pff, such arrogant idiots we are! (anyway, max permissions)
	
	// Channel Names/IDs? (move to another constants class - ChanID?)
	public static final String OOC_CHANNEL = "ooc";
	public static final String STAFF_CHANNEL = "staff";
}