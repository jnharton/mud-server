package mud;

import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

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
	public static final int LINE_LIMIT = 80;
	public static final int MAX_SKILL = 50;
	public static final int MAX_STACK_SIZE = 25; // generic maximum for all Stackable items (should this be in the stackable interface?)
	
	// Movement Speed (move to another constants class - Speed?)
	public static final int WALK = 1;
	public static final int RUN = 3;
	
	// Permissions (move to another constants class - Perms?)
	public static final int USER = 0;      // limited permissions, no @commands at all
	public static final int BUILD = 1;     // building
	public static final int ADMIN = 2;     // account administration?
	public static final int WIZARD = 3;    // Most permissions
	public static final int SUPERUSER = 4; // Pff, such arrogant idiots we are! (anyway, max permissions)
	
	// Flags
	public static final EnumSet<ObjectFlag> default_room_flags = EnumSet.of(ObjectFlag.DARK,ObjectFlag.SILENT); // RDS, where R is a TypeFlag
	
	public static final Map<String, Integer> permissionMap = new Hashtable<String, Integer>() {
		{
			put("USER",   USER);
			put("BUILD",  BUILD);
			put("ADMIN",  ADMIN);
			put("WIZARD", WIZARD);
			put("GOD",    SUPERUSER);
		}
	};
	
	//
	public static final int VOID = -1;
	
	// Channel Names/IDs? (move to another constants class - ChanID?)
	public static final String OOC_CHANNEL = "ooc";
	public static final String STAFF_CHANNEL = "staff";
	
	// Account Login State
	public static final String USERNAME = "USERNAME";
	public static final String PASSWORD = "PASSWORD";
	public static final String AUTHENTICATE = "AUTHENTICATE";
	public static final String REGISTER = "REGISTER";
	public static final String LOGIN = "LOGIN";
	
	// Password Recovery State
	public static final String QUERY = "QUERY";
	public static final String ACCOUNT = "ACCOUNT";
	public static final String PLAYER = "PLAYER";
	public static final String KEY = "KEY";
	public static final String NEWPASS = "NEWPASS";
	public static final String CONFIRM = "CONFIRM";
	
	// Color Options
	public static final int DISABLED = 0;
	public static final int ANSI = 1;
	public static final int XTERM = 2;
	
	// ?
	public static final int CONFIG_OFF = 0;
	public static final int CONFIG_ON = 1;
	
	// game modes?
	
	// status
	public static final String ST_CNVS = "CNVS";
	public static final String ST_EDIT = "EDT";
	public static final String ST_INTERACT = "INT";
	public static final String ST_VIEW = "VIEW";
	
	// stat generation
	public static final int ROLL = 0;
	public static final int ASSIGN = 1;
	
	// backup
	public static final int FLATFILE = 0;
	public static final int JSON = 1;
	
	// time
	public static final int REAL = 0;
	public static final int GAME = 1;
	
	// logging
	public static final String LOG = "log";
	public static final String ERROR_LOG = "error";
	public static final String DEBUG_LOG = "debug";
	public static final String CHAT_LOG = "chat";
	
	// ?
	public static final String NO_PROP_VALUE = "NO-PROP";
}