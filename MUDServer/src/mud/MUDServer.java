package mud;

/*
  Copyright (c) 2012 Jeremy N. Harton

  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
  persons to whom the Software is furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
  Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

// MUD Libraries

//mud.colors
import mud.colors.XTERM256;

import mud.commands.*;
import mud.interfaces.*;

import mud.magic.*;

import mud.miscellaneous.Atmosphere;
import mud.miscellaneous.Zone;

import mud.net.*;
import mud.objects.*;
//ones I don't need: Banker

import mud.objects.items.*;
// ones I don't need: Attribute, Pack

// mud.protocols
import mud.protocols.MSP;
import mud.protocols.Telnet;

import mud.quest.*;
import mud.utils.*;
//ones I don't need here: AreaConverter, Bank, BankAccount, HelpFile

import mud.weather.*;

// JAVA Libraries

import java.io.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Jeremy N. Harton
 * 
 * @version 0.9.2
 * 
 * @see
 * "aSimpleMU
 * Java MUD Server
 * Version 0.9.2 ( 2.4.2013 )
 * 
 * - uses Java SDK 1.7
 * 
 * -- No Theme --
 * 
 * - still need to remove theme related stuff from previous forks - 4/21/2011 (DONEish, have theming system now)
 * - need to remove MU* stuff and focus on MUD related code
 * 
 * >> Copyright 2010 - Eternity Jeremy N. Harton <<
 * 
 * - v0.6.5b retired ( 4.15.2010 )
 * - revision bumped to 0.6.9b ( 9.26.2010 )
 * - revision bumped to 0.7b ( 11.1.2010 )
 * - revision bumped to 0.7.2b ( 11.18.2010 )
 * - forked to 0.7.2bF0 for theme change ( 3.4.2011 )
 * - forked to 0.7.2.bF1 for being more MUD than MU*
 * - revision bumped to 0.8bF1 for major work towards being a MUD server ( 6.4.2011 )
 * - revision bumped to 0.9bF1 for incalculable change since last version ( 3.31.2012 )
 * - revision bumped to 0.9.1bF1 for slow change in last six months ( 11.2.2012 )
 * - revision bumped to 0.9.2bF1 for recent changes, especially to database design ( 2.4.2013 )
 * - fork nomenclature dropped 0.9.2bF1 -> 0.9.2
 * 
 * NEED concept for when to increase version number
 * 
 * Just another Java MUD
 *
 * @Last Work: database and flags (contributions by joshgit from github.com)
 * @minor version number should increase by 1: when 5 or more commands are added or modified, significant problem is fixed, etc?
 * Last Worked On: 2.4.2013
 **/

public class MUDServer implements MUDServerI, LoggerI {
	// Libraries
	// Processing Network Library (a modified version, link?)

	// Variables
	//

	/*
	 * Configuration
	 * 
	 * The value MAIN_DIR must be changed to the location of the program (other os paths may work because it's
	 * Java) which is presumably the root above the data directory (as these constants dictate the data
	 * storage location). Also, the directories referenced in DATA_DIR and BACKUP as well as any statically
	 * specified files need to exist, there must actually be a file or a crash will likely occur
	 * upon trying to load them. (should I try to fix that?). There is no guarantee that a corrupted file or
	 * a corrupted entry in a file, such as an object with an invalid location will load properly and not
	 * crash the server.
	 * 
	 * NOTE: hash updated, need to manually reassign passwords [DONE?]
	 * - writing a function to reassign them automatically would require knowing the password
	 * (i.e. decryption function) which would make the hashing inherently unsafe since someone
	 * breaking in would have an easy way to decrypt them besides writing their own
	 * - a massive password reset might be safer
	 * - I really need to find a way to get the hashing outside of the program itself so it could be
	 * supplied as a binary outside of the code
	 * **NOTE TO SELF: Need to handle these with File or IO related exceptions**
	 */

	/* config options
	 * --------------
	 * P ansi color -> ansi: <on/off>
	 * P mud sound protocol (msp) -> msp: <on/off>
	 * A debug -> debug: <on/off>
	 * _ port -> port: <some number>
	 * _ start room -> start room: <some number>
	 * _ welcome room -> welcome room: <some number>
	 * P line width -> line: <some number, default: 80>; number of characters shown per line
	 * _ logging -> logging: <true/false>
	 * _ max log size -> max log size: <some number>
	 * _ max list length -> max list length: <some number>
	 * _ mode -> mode: <some numbered mode>
	 * _ telnet -> telnet: <number representing configured telnet support>
	 * A override -> allow overriding of one login per ip address rule
	 * _ max levels (maximum player levels) -> max levels: <some number>
	 * _ max players (maximum concurrent logins) -> max players: <some number>
	 * 
	 * P = Player Configurable, A = Admin Configurable, _ = Initial Server Configuration Only
	 * 
	 * Some of these ought not to be player configurable except for those who have
	 * sufficient permissions. Player permissions ought to override system
	 * ones, so that MSP can be turned on by default, but a player can disable it for themselves.
	 * 
	 * However, the permissions need to be set so that a player can only be more restrictive, not
	 * less so (i.e. not 'more permissive') than the server.
	 */

	// server status
	private boolean running = true;

	// server information
	private final static String program = "JavaMUD";  // the server program name
	private final static String version = "0.9.2"; // the server version number
	private String computer = "Stardust";             // the name of the server computer
	private String serverName = "Server";                   // the name of the server (obtained from theme definition)

	// server configuration settings
	private int port = 4202;            // the port on which to listen for client connections
	private int WELCOME_ROOM = 8;       // welcome room
	private int max_log_size = 5000;    // max length of a log file (in lines)
	private int max_levels = 20;        // maximum player level
	private int max_players = 15;       // maximum number of players (adustable, but exceeding 100/1000 may cause other code to break down catastrophically)
	private int max_list_length = 1000; // maximum list length in lines
	private String motd = "motd.txt";   // Message of The Day file

	// server state settings
	private GameMode mode = GameMode.NORMAL; // (0=normal: player connect, 1=wizard: wizard connect only, 2=maintenance: maintenance mode)
	private int multiplay = 0;               // (0=only one character per account is allowed, 1=infinite connects allowed)
	private int guest_users = 0;             // (0=guests disallowed, 1=guests allowed)
	private int debug = 1;                   // (0=off,1=on) Debug: server sends debug messages to the console
	private int debugLevel = 3;              // (1=debug,2=extra debug,3=verbose) priority of debugging information recorded
	private boolean logging = true;          // logging? (true=yes,false=no)
	private int logLevel = 3;                // () priority of log information recorded 
	private boolean prompt_enabled = false;  // show player information bar

	// Protocols
	/*
	 * this section is badly designed. In theory it represents whether support for something is enabled,
	 * but in the case of colors only ANSI -or- XTERM should be possible (one color system).
	 */
	private int ansi = 1;        // (0=off,1=on) ANSI Color on/off, default: on [currently only dictates bright or not]
	private int xterm = 0;       // (0=off,1=on) XTERM Color on/off, default: off [not implemented]
	private int msp = 0;         // (0=off,1=on) MUD Sound Protocol on/off, default: off
	private int telnet = 0;      // (0=no telnet: mud client mode, 1=telnet: telnet client mode, 2=telnet: telnet and mud client)

	// Language/Localization
	// en for English (US), fr for French (France?) are currently "supported",
	// but, only some of the error messages are currently converted to french
	private final String lang = "en";

	// Data Storage Info
	private final String MAIN_DIR = new File("").getAbsolutePath() + "\\"; // Program Directory
	private final String DATA_DIR = MAIN_DIR + "data\\";                   // Data Directory

	private final String ACCOUNT_DIR = DATA_DIR + "accounts\\";            // Account Directory
	private final String BACKUP_DIR = DATA_DIR + "backup\\";               // Data Sub-Directory
	private final String CONFIG_DIR = DATA_DIR + "config\\";               // Config Directory
	private final String HELP_DIR = DATA_DIR + "help\\";                   // Help Directory
	private final String MAP_DIR = DATA_DIR + "maps\\";                    // MAP Directory
	private final String MOTD_DIR = DATA_DIR + "motd\\";                   // MOTD Directory
	private final String SPELL_DIR = DATA_DIR + "spells\\";                // Spell Directory
	private final String THEME_DIR = DATA_DIR + "theme\\";                 // Help Directory

	/* filename variables used to be final -- i'd like to be able to reload or change them while the game is running though */

	// files to use
	private String mainDB = DATA_DIR + "db.txt";                   // database file (ALL) -- will replace all 3 or supersede them
	private String errorDB = DATA_DIR + "errors_" + lang + ".txt"; // messages file (errors) [localized?]
	private String spellDB = DATA_DIR + "spells.txt";              // database file (spells) -- contains spell names, messages, and more
	private String helpDB = DATA_DIR + "help\\index.txt";          // index file (help)

	// Default Player Data
	private final EnumSet<ObjectFlag> startFlags = EnumSet.of(ObjectFlag.PLAYER);                       // default flag string
	private final String start_status = "NEW";                    // default status string
	private final String start_desc = "There is nothing to see."; // default desc string
	private final Integer start_room = 9;                         // default starting room
	private final Integer[] start_stats = { 0, 0, 0, 0, 0, 0 };   // default stats

	// Objects (used throughout program in lieu of function scope variables) -- being phased out (April 2012
	// these must be global variable, so that mud can have top-level control over them in the program
	private Server s;                      // The server object
	private Log log;                       // A log file to keep track of user actions
	private Log debugLog;                  // A log file to keep track of debugging messages
	private Log chatLog;                   // A log file to keep track of chat messages
	private TimeLoop game_time;            // TimeLoop Object

	static public final String OOC_CHANNEL = "ooc";
	static public final String STAFF_CHANNEL = "staff";

	final private ChatChanneler chan = new ChatChanneler(this);
	{
		chan.makeChannel(STAFF_CHANNEL);
		chan.makeChannel(OOC_CHANNEL);
	}

	// HashMaps
	// dynamic - the contents of the hashmap may change while the server is running and in some cases that is very likely
	// static  - the contents of the hashmap are currently loaded once at startup and not modified thereafter
	// class static - identical for every instances of the class
	// pna - per name association?
	private HashMap<String, Command> commandMap = new HashMap<String, Command>(20, 0.75f);       // HashMap that holds an instance of each command currently (dynamic)

	private HashMap<Client, Player> sclients = new HashMap<Client, Player>();
	private HashMap<Zone, Integer> zones = new HashMap<Zone, Integer>(1, 0.75f);                // HashMap that tracks currently "loaded" zones (dynamic)
	final private PlayerControlMap playerControlMap = new PlayerControlMap();

	private HashMap<String, String> displayColors = new HashMap<String, String>(8, 0.75f);      // HashMap specifying particular colors for parts of text (somewhat static)
	private HashMap<String, String> colors = new HashMap<String, String>(8, 0.75f);             // HashMap to store ansi/vt100 escape codes for outputting color (static)

	public HashMap<String, String> aliases = new HashMap<String, String>(20, 0.75f);             // HashMap to store command aliases (static)
	private HashMap<Integer, String> Errors = new HashMap<Integer, String>(5, 0.75f);           // HashMap to store error messages for easy retrieval (static)

	private HashMap<String, Date> holidays = new HashMap<String, Date>(10, 0.75f);               // HashMap that holds an in-game date for a "holiday" name string
	private HashMap<Integer, String> years = new HashMap<Integer, String>(50, 0.75f);            // HashMap that holds year names for game themes that supply them (static)

	/* not used much currently */
	private LinkedHashMap<String, String> config = new LinkedHashMap<String, String>(11, 0.75f); // LinkedHashMap to track current config instead of using tons of individual integers?
	//private HashTable<String, Boolean> config;

	private HashMap<Player, Session> sessionMap = new HashMap<Player, Session>(1, 0.75f);                             // player to session mapping

	private int guests = 0;         // the number of guests currently connected

	// Arrays
	private String[] help;          // string array of help file filenames

	// Databases/Data
	private ObjectDB objectDB = new ObjectDB();

	/*
	 * I don't want to generate these on the fly and they need to stay 'in sync' so to speak.
	 */
	private ArrayList<Player> players;       // ArrayList of Player Objects currently in use

	private HashMap<String, Spell> spells2 = new HashMap<String, Spell>();  // HashMap to lookup spells by index using name as key (static)

	// Help Files stored as string arrays, indexed by name
	private HashMap<String, String[]> helpMap = new HashMap<String, String[]>();

	private ArrayList<Account> accounts;     // ArrayList of Accounts (UNUSED)

	// "Security" Stuff
	private ArrayList<String> banlist;       // ArrayList of banned ip addresses

	// Other
	private ArrayList<Effect> effectTable;   // ArrayList of existing effects (can be reused many places)
	private ArrayList<String> forbiddenNames;//

	// cmd lists
	private String[] user_cmds = new String[] {
			"add", "ansi", "ask", "attack",
			"bash", "buy",
			"calendar", "cast", "chargen", "connect", "commands", "createItem",
			"date", "dedit", "describe", "drink", "drop",
			"effects", "equip", "examine", "exchange", "exits",
			"go","greet",
			"help", "home", "housing",
			"interact", "inventory", "install",
			"levelup", "list", "lock", "look",
			"mail", "map", "money", "motd", "move", "msp",
			"nameref",
			"offer",
			"page", "passwd", "pinfo", "prompt", "push",
			"quests", "quit",
			"roll",
			"say", "score", "sell", "sethp", "setlevel", "setmana", "setxp", "sheathe", "skillcheck", "spells", "stats", "status",
			"take", "target", "time",
			"unequip", "uninstall", "unlock", "use",
			"value", "version", "vitals",
			"where", "who"
	};

	private String[] build_cmds = new String[] {
			"@check",                                 // @check check to see what exit props aren't set
			"@describe", "@dig", "@door", "@dungeon", // @describe describe an object, @dig dig a new room, @door create an arbitrary exit @dungeon dig a new dungeon
			"@fail", "@flags",                        // @fail set exit fail message, @flags see flags on an object
			"@iedit",                                 // @iedit edit an item
			"@jump",                                  // @jump jump to a different room
			"@lsedit",                                // @lsedit edit a list
			"@makehouse",                             // @makehouse make a house
			"@ofail", "@open",                        // @ofail set exit ofail message, @open open a new exit (1-way)
			"@recycle", "@redit",                     // @recycle recycle objects
			"@osucc", "@success"                      // @osucc set exit osuccess message, @success set exit success message
	};

	private String[] admin_cmds = new String[] {
			"@alias", "@allocate", // @alias setup command aliases, @allocate "allocate" dbref space
			"@ban", "@bb",         // @ban ban player, @bb use bulletin board
			"@config", "@control", // @config change server configuration options, @control control an NPC
			"@debug",              // @debug show debug information
			"@hash", "@hedit",     // @hash see hash of a string, @hedit edit help files
			"@listprops",          // @listprops list properties on an object          
			"@nextdb",             // @nextdb get the next unused dbref number
			"@passwd", "@pgm",     // @passwd change passwords, @pgm interpret a "script" program
			"@set", "@setskill",   // @set set properties on objects, @setskill set player skill values
			"@zones"               // @zones setup,configure,modify zones
	};

	private String[] wiz_cmds = new String[] {
			"@access",
			"@backdb",
			"@flag", "@flush",
			"@loaddb",
			"@sethour", "@setminute", "@setmode", "@start", "@shutdown"
	};

	// Time & Date - General Variables
	private final static String[] suffix = { "st", "nd", "rd", "th" }; // day number suffix

	private int day = 2, month = 8, year = 1372;

	private int game_hour = 5;    // 5am
	private int game_minute = 58; // 55m past 5am

	//Theme Related Variables
	private String theme = THEME_DIR + "forgotten_realms.txt";                     // theme file to load

	public static int[] DAYS = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 }; // days in each month
	public static int MONTHS = 12;                                                 // months in a year
	public static String[] MONTH_NAMES = new String[MONTHS];                       // month_names
	private Seasons season = Seasons.SUMMER; // Possible - Spring, Summer, Autumn, Winter

	private String month_name;
	private String year_name;
	private String reckoning;

	private Theme theme1;

	// Testing
	private BulletinBoard bb;
	private ArrayList<Portal> portals = new ArrayList<Portal>();

	// for use with telnet clients
	private byte[] byteBuffer = new byte[1024]; // a byte buffer to deal with telnet (characters are sent to the server as they are typed, so need to buffer input until a particular key is pressed
	private byte linefeed = 10;                 // line-feed character

	public Hashtable<Client, ArrayList<Character>> inputBuffers = new Hashtable<Client, ArrayList<Character>>(max_players);

	private int done = 0; // a way of tracking if we're done with a line of telnet input

	// static values
	private static int USER = 0;   // limited permissions, no @commands at all
	private static int BUILD = 1;  // building
	private static int ADMIN = 2;  // account administration?
	private static int WIZARD = 3; // Most permissions
	private static int GOD = 4;    // Pff, such arrogant idiots we are! (anyway, max permissions)
	// Corresponding Flags: U,B,A,W,G

	private static int MAX_SKILL = 50;

	public static String admin_pass = Utils.hash("password"); // need to fix the security issues of this (unused, but for admin command

	public ArrayList<Player> moving = new ArrayList<Player>(); // list of players who are currently moving

	private static final int MAX_STACK_SIZE = 25; // generic maximum for all stackable items (should this be in the stackable interface?)

	public MUDServer() {}

	public MUDServer(final String address, int port) {
		this.port = port;
	}

	public static void main(String args[]) {
		/* options: <port>, --port=<port>, --debug */
		MUDServer server = null;

		if (args.length < 1)
		{
			System.out.println("No port number specified. Exiting...");
			/* should I have an interactive setup if no arguments are given? */
			//System.out.println("Which port do you wish to run the MUD on?");
			System.exit(-1);
		}

		try {
			server = new MUDServer(); // create server
			//server = new MUDServer("localhost", 4201); // create server

			// process command line parameters
			for (int a = 0; a < args.length; a++) {
				String s = args[a];
				
				String param = s.substring(2, s.length());

				if ( s.contains("--") ) {
					if ( param.equals("port") ) {
						server.port = Utils.toInt(args[a+1], 4201);
						System.out.println("Using port " + server.port);
					}
					else if ( param.equals("debug") ) {
						server.debug = 1;
						System.out.println("Debugging Enabled.");
					}
					else if ( param.equals("db") ) {
						// problem with assigning this variable because it is marked 'final'
						//server.mainDB = server.DATA_DIR + args[a + 1].trim() + ".txt";
					}
				}
			}
		}
		catch (Exception e) {
			server.debug("Exception(MAIN): " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		server.init();
	}

	/**
			 * Initialize Server
			 * 
			 * Performs the basics tasks to get the server running
			 * 	setup - loads configuration, preferences, the database, game theme, etc
			 * 	run   - starts the main program loop
			 * -> If an unhandled exception (NullPointerExceptions in particular) occurs
			 * anyway in the init or run methods it will be caught here and result in the
			 * program being terminated
			 * 
			 */
			public void init() {
				try {
					setup();
					run();      // run main loop
				}
				catch (Exception e) {
					debug("Exception in MUDServer.init()");
					e.printStackTrace();
					System.exit(-1);
				}
			}

			private void setup() {
				// Tell Us where the program is running from
				System.out.println("Current Working Directory: " + new File("").getAbsolutePath());
				System.out.println("MAIN_DIR: " + MAIN_DIR);
				System.out.println("Important: The two above should be the same if the top is where you have the program and it's data");

				// search for needed directories and files (using predefined names and locations)
				ArrayList<File> directories = new ArrayList<File>();
				ArrayList<File> files = new ArrayList<File>();

				// directories
				directories.add(new File(MAIN_DIR));
				directories.add(new File(DATA_DIR));
				directories.add(new File(BACKUP_DIR));
				directories.add(new File(CONFIG_DIR));
				directories.add(new File(HELP_DIR));
				directories.add(new File(MAP_DIR));
				directories.add(new File(MOTD_DIR));
				directories.add(new File(SPELL_DIR));
				directories.add(new File(THEME_DIR));

				// check that the directories exist, if not create them
				for (final File dir : directories) {
					if ( !dir.exists() ) {
						boolean success = dir.mkdir();

						if (success) {
							System.out.println("Directory: " + dir.getAbsolutePath() + " created");
						}  
					}
				}

				// snag this machine's real IP address
				try {
					// Get the address and hostname, so you can report it
					InetAddress addr = InetAddress.getLocalHost(); // Get IP Address
					this.computer = addr.getHostName();            // get computer name
					System.out.println(this.computer);             // print computer name
				} 
				catch (UnknownHostException e) {
					e.printStackTrace();
				}

				// Logging
				if ( logging ) { // if logging is enabled, create a log object and open it
					// instantiate log objects
					this.log = new Log();                      // main log - character actions, etc
					this.debugLog = new Log("debug");          // debug log - any and all debugging
					this.chatLog = new Log("chat");            // chat log - all chat messages

					// open log files for writing
					this.log.openLog();
					this.debugLog.openLog();
					this.chatLog.openLog();

					// tell us it's enabled.
					debug("Logging Enabled.");
				}
				else {
					// tell us it's disabled
					debug("Logging Disabled.");
				}

				debug(""); // formatting

				// Theme Loading
				this.loadTheme(theme);
				this.month_name = MONTH_NAMES[month - 1];
				this.year_name = years.get(year);

				debug(""); // formatting

				MUDObject.parent = this; // assign a static reference to the running server (ugly, but allows some flex)

				// initialize player array
				this.players = new ArrayList<Player>(max_players);

				System.out.println("ArrayList(s) Initialized!");

				// Load Databases/Persistent Data into memory
				//
				// Note: Does it make any sense to replace loading all this into memory
				// using a buffered reader and loading objects as it goes? It would be really
				// wise to implement configurable auto db-saving here
				//

				// error message hashmap loading
				final String[] errors = Utils.loadStrings(errorDB);
				for (final String e : errors)
				{
					final String[] working = e.split(":");
					if (working.length >= 2) {
						debug("Error(number): " + working[0]);
						debug("Error(message): " + working[1]);
						this.Errors.put(Integer.parseInt(working[0]), working[1]);
					}
				}

				loadSpells(Utils.loadStrings(spellDB));
				System.out.println("Spells Loaded!");

				// Load everything from databases by flag
				ObjectLoader.loadObjects(loadListDatabase(mainDB), this, objectDB, this);
				System.out.println("Database Loaded!");

				objectDB.loadExits(this);  // load exits (post-room loading)

				// Post-Room Loading
				//loadExits();          // load exits ( replace? moved? not sure?)
				//loadThings();       // load thing (old name)
				placeThingsInRooms(); // load things (new name)
				loadItems();          // load items

				//
				fillShops();

				// TODO FIX THIS
				// make sure npcs are added to listeners
				/*for (NPC npc : npcs1) {
			getRoom(npc.getLocation()).addListener(npc);	
		}*/

				// instantiate banned ip list
				banlist = loadListDatabase(CONFIG_DIR + "banlist.txt");

				// instantiate forbidden names list
				forbiddenNames = loadListDatabase(CONFIG_DIR + "names.txt");

				// load configuration data (file -- default.config)
				//loadConfiguration(CONFIG_DIR + "config.txt", configs); ?
				for (final String s : loadListDatabase(CONFIG_DIR + "config.txt")) {
					final String[] configInfo = s.split(":");
					String name = Utils.trim(configInfo[0]);
					String value = Utils.trim(configInfo[1]);
					value = value.substring(0, value.indexOf('#'));
					config.put(name, value);
				}

				// print out config map
				debug(config.entrySet());

				// help file loading
				System.out.print("Loading Help Files... ");
				for (final String helpFileName : Utils.loadStrings(helpDB))
				{
					final String[] helpfile = Utils.loadStrings(this.HELP_DIR + helpFileName);
					helpMap.put(helpfile[0], helpfile);
				}
				System.out.println("Help Files Loaded!");

				// Set Up Colors

				// set up the ANSI color hashmap, color name -> ansi escape code
				this.colors.put("black", "\033[30m");
				this.colors.put("red", "\033[31m");
				this.colors.put("green", "\033[32m");
				this.colors.put("yellow", "\033[33m");
				this.colors.put("blue", "\033[34m");
				this.colors.put("magenta", "\033[35m");
				this.colors.put("cyan", "\033[36m");
				this.colors.put("white", "\033[37m");
				debug("Colors: " + colors.entrySet()); // DEBUG

				// set up display colors
				this.displayColors.put("exit", "green");
				this.displayColors.put("player", "blue");
				this.displayColors.put("thing", "yellow");
				this.displayColors.put("room", "green");
				debug("Object Colors: " + displayColors.entrySet()); // DEBUG

				// TODO made redundant by ObjectFlag
				/*
				 * Command Mapping
				 * 
				 * Ideally this would be done for all commands and the cmd parser would simply
				 * check aliases, get the actual name, and call commandMap.get(name).execute(arg, client)
				 * 
				 * I really need to be careful with access permissions, or I need to facilitate some means
				 * to get just a basic interface
				 * 
				 * NOTE: these need to be kept up to date with the implementation in the functions cmd_x,
				 * the plan is to eventually remove those and the cmd interpreter to a major extent
				 * so that I am mostly just doing this:
				 * 
				 * commandMap.get(cmd).execute(arg, client)
				 * 
				 * for each command. The point being that I can reduce the amount of work I need to add a new command
				 * to virtually nil if I use existing features. In fact that bit just above doesn't need any
				 * changing. Although I might need to explicity handle aliases better.
				 */
				this.commandMap.put("@access", new AccessCommand(this));  //
				this.commandMap.put("@alias", new AliasCommand(this));    //
				this.commandMap.put("attack", new AttackCommand(this));   //
				this.commandMap.put("cast", new CastCommand(this));       //
				this.commandMap.put("chat", new ChatCommand(this));       //
				this.commandMap.put("drop", new DropCommand(this));       //
				this.commandMap.put("examine", new ExamineCommand(this)); //
				this.commandMap.put("greet", new GreetCommand(this));     //
				this.commandMap.put("help", new HelpCommand(this));       //
				this.commandMap.put("mail", new MailCommand(this));       //
				this.commandMap.put("where", new WhereCommand(this));     // Broken?

				debug("Mapped Commands: " + commandMap.entrySet());       // Print out all the command mappings (DEBUG)

				loadAliases(this.CONFIG_DIR + "aliases.txt");

				/* bulletin board(s) */
				this.bb = new BulletinBoard(serverName);

				final ArrayList<String> entries = loadListDatabase(DATA_DIR + "bboard.txt");
				this.bb.ensureCapacity(entries.size());

				for (final String e : entries) {
					String[] entryInfo = e.split("#");
					try {
						final int id = Integer.parseInt(entryInfo[0]);
						final String author = entryInfo[1];
						final String subject = entryInfo[2];
						final String message = entryInfo[3];
						final BBEntry bbe = new BBEntry(id, author, subject, message);
						bb.addEntry(bbe);
					}
					catch(NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}

				/* Server Initialization */

				// almost everything that needs to be loaded should be done before here
				System.out.println("Creating server on port " + port);
				this.s = new Server(this, port);

				// Time Loop
				// cpu: -for now, appears marginal-
				game_time = new TimeLoop(this, DAYS, month, day, game_hour, game_minute);
				new Thread(game_time, "time").start();
				System.out.println("Time (Thread) Started!");

				// Weather Loop
				// cpu: ~20%
				//System.out.println("Weather (Thread) Started!");

				test(); // set up some testing

				accounts = new ArrayList<Account>();
				loadAccounts();
				if (accounts.size() == 0) {
					debug("No accounts.");
				}

				System.out.println("Server> Setup Done.");
			}

			private void test() {
				/* Begin TESTING */

				/**
				 * Miscellaneous Items Testing
				 */

				int avar = 0;

				if (avar == 1 ) {
					Arrow a = new Arrow(-1, "Flaming Arrow", "a flaming arrow", 8);
					objectDB.addAsNew(a);
					Arrow b = new Arrow(-1, "Flaming Arrow", "a flaming arrow", 8);
					objectDB.addAsNew(b);
					a.stack(b);

					Arrow c;

					for (int i = 0; i < 30; i++) {
						c = new Arrow(-1, "Flaming Arrow", "a flaming arrow", 8);
						objectDB.addAsNew(c);
						a.stack(c);
					}


					debug(a);

					getRoom("testing").contents1.add(a.split(16));
					getRoom("testing").contents1.add(a);
				}

				/**
				 * Coordinate System Testing
				 */

				/*System.out.println("creating a thing: " + nextDB());

		Room r = getRoom(207);
		Thing thing = new Thing(-1, "slab", "TD", "a large, rectangular stone slab" , 207);

		thing.coord.x = 4;
		thing.coord.y = 6;

		r.contents.add(thing);

		main.add(thing.toDB());
		main1.add(thing);

		Thing t = getThing(256); // ladder

		t.coord.setX(4);
		t.coord.setY(6);
		t.coord.setZ(1);

		t.attributes.put("height", 10);
				 */

				/**
				 * Portal Testing
				 */

				int pvar = 0;

				if (pvar == 1) {
					System.out.println("creating a portal: " + objectDB.peekNextId());

					Portal portal = new Portal(WELCOME_ROOM, 5); // a portal connecting two rooms (#8 and #5)
					portal.name = "portal";           // generic name
					portal.coord.setX(1);             // x coordinate
					portal.coord.setY(1);             // y coordinate

					portals.add(portal);              // add to list of portals
					objectDB.addAsNew(portal);                // add to live game

					System.out.println("creating a portal: " + objectDB.peekNextId());

					Portal portal1 = new Portal(PortalType.RANDOM, WELCOME_ROOM, new int[] { 5, 182, 161, 4 });
					portal1.name = "portal1";         // generic name
					portal1.coord.setX(2);            // x coordinate
					portal1.coord.setY(2);            // y coordinate

					portals.add(portal1);             // add to list of portals
					objectDB.addAsNew(portal1);               // add to live game
				}

				/**
				 * Quest Testing
				 */
				System.out.println("NPCs: " + objectDB.getNPCs().size());

				final NPC npc = getNPC("Iridan");
				if (npc != null) {
					final Quest quest = new Quest("Clear kobold infestation", "A cave near town is infested with kobolds, " +
							"whom recently began raiding the town. Kill them all to end the infestation.",
							new Task("Kill 15 kobolds", TaskType.KILL, 15));

					npc.addQuest(quest);
				}
				else {
					debug("getNPC(\"Iridan\") returned null.");
				}

				/**
				 * Weather Testing
				 */

				// get a room, in this case we want the main environment room
				Room room = getRoom(1);

				// tell us the name of the room
				debug(room.getName());

				// create some weather states
				WeatherState ws1 = new WeatherState("Clear Skies", 1, false, false, false, false);
				ws1.description = "The sky is clear{DAY? and blue}{NIGHT? and flecked with stars.  Moonlight faintly illuminates your surroundings}.";
				ws1.transUpText = "Your surroundings brighten a little as the {DAY?sun}{NIGHT?moon} peeks through thinning clouds.";
				WeatherState ws2 = new WeatherState("Cloudy", 0.5, false, false, true, false);
				ws2.description = "The air is dry for now, but clouds cover the sky.  It might rain soon.";
				ws2.transDownText = "It's getting cloudy.";
				ws2.transUpText = "The rain seems to have stopped for now.";
				WeatherState ws3 = new WeatherState("Rain", 0, true, false, true, false);
				ws3.description = "Above the pouring rain hangs a gray and solemn sky.";
				ws3.transDownText = "Rain begins to spot your surroundings.";

				// create a seasonal weather profile, and hand it the states we created
				Season summer = new Season("Summer", ws1, ws2, ws3);
				//Season summer = new Season("Summer", new WeatherState[] { ws1, ws2, ws3 });

				// create a weather object, handing it our current season and a starting weather state
				Weather weather = new Weather(summer, ws1);

				// apply our new weather object to a room
				room.setWeather(weather);

				for (final Room room1 : objectDB.getWeatherRooms()) {
					room1.setWeather(weather);
				}

				/**
				 * End Testing
				 */
			}
			
			/**
			 * Immediate Command Processing
			 * 
			 * @param newCmd
			 */
			private void processCMD(final CMD newCmd) {
				final String command = newCmd.getCmdString();
				final Client client = newCmd.getClient();

				try {
					if ( checkAccess( client, newCmd.getPermissions() ) )
					{
						cmd(command, client);
					}
					else {
						System.out.println("Insufficient Access Permissions");
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}

				if ( loginCheck( client ) ) {
					prompt(client);
				}
			}

			private void runHelper(final Client client) {
				String whatClientSaid = client.getInput();

				// If the client is not null and has something to say
				try {
					if (whatClientSaid == null || "".equals(whatClientSaid)) {
						return;
					}
					// telnet negotiation
					if (client.tn) { // if the client is using telnet
						final byte[] clientBytes = whatClientSaid.getBytes();
						if (clientBytes.length >= 3)    client.tn = false;
					}
					// all the rest
					else {
						ArrayList<Character> input = null;

						if (telnet > 0) {
							if ( inputBuffers.containsKey(client) ) {
								input = inputBuffers.get(client);
							}
							else {
								input = new ArrayList<Character>(16384); // 16,384 characters?! really?
								inputBuffers.put(client, input);
							}
						}

						if (telnet == 0) { /* using mud client (no TELNET support) */
							// handle whatever was sent
						}
						else if (telnet == 1) { // using telnet exclusively, not concerned w/mud clients						

							if (done == 0) {
								final Character ch = whatClientSaid.charAt(0);

								System.out.println("Read: " + ch + "(" + ch.toString() + ")");

								if (ch == '\012') { // return
									StringBuffer sb = new StringBuffer();

									for (Character cha : input) { sb.append(cha); }

									input.clear();                  // clear the input buffer
									whatClientSaid = sb.toString(); // convert stringbuffer to string

									done = 1;
								}
								else if (ch == '\010') { // backspace
									// if there are still elements, remove the last character typed 
									if ( !input.isEmpty() ) {
										input.remove(input.size() - 1); 
									}
								}
								else {
									input.add(ch);
								}

								debug("current telnet input: " + input.toString()); // tell us the whole string
							}
							else {
								done = 0;
							}
						}
						else if (telnet == 2) { /* telnet and mud clients expected */
							// in order to compensate for differences,
							// we will apply the telnet style, but take all the bytes
							// once we have them, we will check for the stop character
							// and throw everything else away. this will ensure
							// that we can handle a whole string sent by a mudclient
							// acceptably quickly and still deal with character by character sending
							// status: copy and pasted plain telnet handling code
							if (done == 0) {
								Character ch;
								while (whatClientSaid.length() > 0) {
									ch = whatClientSaid.charAt(0);
									whatClientSaid = whatClientSaid.substring(1);
									if (ch != '\010' && ch != '\013') {
										System.out.println("Read: " + ch + "(" + ch.toString() + ")");
									}

									if (ch == '\012') {
										StringBuffer sb = new StringBuffer();
										for (Character cha : input) { sb.append(cha); }
										whatClientSaid = sb.toString();

										input.clear();
										done = 1;
									}
									else if (ch == '\010') { // if a backspace
										// if there are still elements, remove the last character typed 
										if ( !input.isEmpty() ) {
											input.remove(input.size() - 1); 
										}
									}
									else {
										input.add(ch);
									}

									if (ch != '\010' && ch != '\013') {
										debug("current telnet input: " + input.toString()); // tell us the whole string
									}
								}
							}
							else {
								done = 0;
							}
						}

						if (!whatClientSaid.trim().equals("")) {  // blocks blank input
							//System.out.print("Putting comand in command queue...");
							processCMD(new CMD(whatClientSaid.trim(), sclients.get(client), client, -1));
							// put the command in the queue
							//cmd(whatClientSaid.trim(), c);
							//System.out.println("Done.");
						}

						//getPlayer(c).idle_state = false; 
						//pulse(c);
					}

					// flush players -- making sure that non-existent/disconnected/broken players don't bog down the system
					flush();

					Thread.sleep(250);
				}
				catch(InterruptedException ie) {
				}
				catch(Exception e) {
					/* until I resolve the issue of the possibility that any command that goes wrong
					 * catastrophically and unresolvably could crash the server, we should assume
					 * that any exception is a fatal exception and reboot, either exiting or reloading
					 * from the last save automatically.
					 * 
					 * hence this code here should report the error, then exit or wait a pre-determined
					 * amount of time and then attempt a reboot.
					 * 
					 * NOTE: I really need a separate thread or means for executing stuff so that bugs in a
					 * command only cause a real problem for the player who tried to use it. probably
					 * the quick solution should be booting the player and having them lose all their progress
					 * since the last save. Tentatively, if I can find a way to verify that the error is not
					 * going to effect them, I can save their character, tell them to reconnect in a little while
					 * and kick them. A live character restore system would be really awesome but I'm sure
					 * how it would integrate with any system fitting my description above.
					 */
					debug("Exception(RUN): " + e.getMessage());
					e.printStackTrace();

					// do cleanup

					/*
					 * - halt command interpretation (if it isn't mangled already)
					 * - clear command queue
					 * - send a message to everyone (if possible)
					 * - disconnect players (saving if possible)
					 * - backup session data
					 * 
					 * - stop threads
					 * - restart server
					 * - start threads
					 * 
					 */

					// restart?
					MUDServer.main(new String[] {"--port=4202", "--debug"} );
				}
			}

			// main loop
			private void run()
			{
				debug("Entering main program loop...");
				debug("Running? " + this.running);           // tell us whether the MUD server is running or not
				debug("Server? " + s.isRunning());           // tell us whether the underlying socket server is running

				while (running) {
					for (final Client client : s.getClients_alt()) {
						runHelper(client);
					}
				}
			}

			// Command Parser
			// needs more general alias support

			/**
			 * <b>Command Parser</b>
			 * 
			 * <br />
			 * <br />
			 * 
			 * Takes in the client object and the input from the client, and looks
			 * for a command/exit invocation.
			 * 
			 * @param input client input (String)
			 * @param client client (Client)
			 */
			public void cmd(final String _input, final Client client) throws NullPointerException
			{	
				String arg = "";
				String cmd = "";

				final String input = _input.trim();

				debug("");

				// cut the input into an array of strings separated by spaces
				final LinkedList<String> inputList = new LinkedList<String>(Arrays.asList(input.split(" ")));
				//final ArrayList<String> inputList = Utils.stringToArrayList(input, " ");

				if (!inputList.isEmpty()) { // if there was any input
					cmd = inputList.remove(0);

					debug("Command: \"" + cmd + "\"", 2);       // print command
					cmd = cmd.trim();                           // trim the command
					debug("Command(trimmed): \"" + cmd + "\""); // print trimmed command
					debug("");

					// if there were any arguments then get the argument, which is everything else
					arg = Utils.join(inputList, " ");  // get the arguments from the input list

					if (inputList.size() > 1) {
						if (!cmd.toLowerCase().equals("connect") && !cmd.toLowerCase().equals("create")) {
							debug("Arguments: \"" + arg + "\"");          // print arguments
							arg = Utils.trim(arg);                        // trim arguments
							debug("Arguments(trimmed): \"" + arg + "\""); // print trimmed arguments
						}
					}
				}

				// check for command alias (so we know there is one for later)
				boolean aliasExists = false;
				String alias = aliases.get(cmd);
				if (alias != null) {
					aliasExists = true;
				}

				// restrict commands based on whether or not the connection has a logged-in player
				// if not logged-in
				// all of this should have configurable messages and maybe a time duration to
				// show for when the mode will return to normal
				if (!loginCheck(client))
				{
					if (mode == GameMode.NORMAL) // Normal Running Mode (a.k.a. Mode 0)
					{
						if (this.players.size() < this.max_players) { // if the maximum number of players hasn't been exceeded.
							// pass arguments to the player connect function
							if ( cmd.equals("connect") || ( aliasExists && alias.equals("connect") ) )
							{
								// pass arguments to the connect function
								cmd_connect(arg, client);
							}
							// pass arguments to the player creation function
							else if ( cmd.equals("create") || (aliasExists && alias.equals("create") ) )
							{
								// pass arguments to the player creation function
								cmd_create(arg, client);
							}
							else if ( cmd.equals("help") ) {
								commandMap.get("help").execute(arg, client);
							}
							else if ( cmd.equals("quit") || (aliasExists && alias.equals("quit") ) )
							{
								s.disconnect(client); // just kill the client?
							}
							else if ( cmd.equals("who") || ( aliasExists && alias.equals("who") ) )
							{
								// run the who function
								cmd_who(arg, client);
							}
							else
							{
								send("Huh? That is not a known command.", client);
								debug("Command> Unknown Command");
							}

						}
						else { send("Sorry. Maximum number of players are connected. Please try back later.", client); }
					}
					else if (mode == GameMode.WIZARD) // Wizard-Only Mode (a.k.a. Mode 1)
					{
						send("System is in Wizard-Only Mode.", client);
						if ( cmd.equals("connect") || ( aliasExists && alias.equals("connect") ) )
						{
							cmd_connect(arg, client); // handles wizflag checking itself
						}
						else if ( cmd.equals("create") || ( aliasExists && alias.equals("create") ) )
						{
							send("Sorry, only Wizards are allowed to login at this time.", client);
						}
						else if ( cmd.equals("quit") || ( aliasExists && alias.equals("quit") ) )
						{
							s.disconnect(client); // just kill the client?
						}
						else
						{
							send("Huh? That is not a known command.", client);
							debug("Command> Unknown Command");
						}
					}
					else if (mode == GameMode.MAINTENANCE) // Maintenance Mode (a.k.a. Mode 2)
					{
						send("System is in Maintenance Mode.", client); // >configurable message<
						send("No Logins allowed. Booting Client...", client);
						s.disconnect(client); // just kill the client?
					}
					else { // ? (any other mode number -- may indicate some kind of failure)
						send("System may be malfunctioning.", client);
						send("No Logins allowed. Booting Client...", client);
						s.disconnect(client); // just kill the client?
					}
				}

				// if logged-in
				else if ( loginCheck(client) )
				{
					// get a hold of the player (reduce redundant getPlayer(client) calls)
					// also, replaced temp with player as far as @flag
					Player player = getPlayer(client);
					Room room = getRoom(client);

					// if the user is editing a list, pass their input to the list editor
					if ( player.getStatus().equals("EDT") )
					{	
						final Editor editor = player.getEditor();

						switch(editor) {
						case AREA:
							//op_area(input, client);
							break;
						case CHARGEN:
							op_chargen(input, client);
							break;
						case DESC:
							op_dedit(input, client);
							break;
						case HELP:
							op_hedit(input, client);
							break;
						case INTCAST:
							op_cast(input, client);
							break;
						case ITEM:
							op_iedit(input, client);
							break;
						case LIST:
							op_lsedit(input, client);
							break;
						case MAIL:
							break;
						case ROOM:
							op_roomedit(input, client);
							break;
						case NONE:
							send("Aborting " + editor.getName(), client);
							player.setStatus("OOC");
						default:
							break;
						}
					}
					else if( player.getStatus().equals("INPUT") ) { // handling interactive input (ex. account logins)

					}
					else if ( player.getStatus().equals("VIEW") ) { // viewing help files
						op_pager(input, client);
					}
					// else pass their input to command parsing
					else
					{
						arg = nameref_eval(arg, client);

						debug("Argument(evaluated): \"" + arg + "\""); // print the trimmed argument

						/* Command Logging */

						// Log all commands after login
						System.out.println("Command being logged...");
						// get variables to log
						String playerName = player.getName();
						int playerLoc = player.getLocation();
						// log command
						log.writeln(playerName, playerLoc, Utils.trim(input));
						System.out.println("Command Logged!");

						/* Command Evaluation */

						boolean buildCmd = false;
						boolean adminCmd = false;
						boolean wizCmd = false;
						boolean godCmd = false;

						//debug("Entering god command loop...");

						// stash god commands inside here
						if (player.getAccess() >= GOD) {
							// pass arguments to the access function
							if ( cmd.equals("@access") || ( aliasExists && alias.equals("@access") ) ) {
								godCmd = true;
								//cmd_access(arg, client);
								commandMap.get("@access").execute(arg, client);
							}
							else if ( cmd.equals("@broadcast") ) {
								godCmd = true;
								s.write("Game> " + getPlayer(client).getName() + " says, " + arg);
							}

							if (godCmd) {
								return;
							}
						}

						//debug("Exited god command loop.");

						//debug("Entering build command loop...");

						// stash build commands inside here
						if (player.getAccess() >= BUILD) {
							// pass arguments to the check function
							if ( cmd.equals("@check") || ( aliasExists && alias.equals("@check") ) )
							{
								buildCmd = true;
								// run the check function
								cmd_check(arg, client);
							}
							// pass arguments to the dig function
							else if ( cmd.equals("@dig") || ( aliasExists && alias.equals("@dig") ) )
							{
								buildCmd = true;
								cmd_dig(arg, client);
							}
							// pass arguments to the describe function
							else if ( cmd.equals("@describe") || ( aliasExists && alias.equals("@describe") ) )
							{
								buildCmd = true;
								// run the describe function
								cmd_describe(arg, client);
							}
							// pass arguments to the open function
							else if ( cmd.equals("@door") || ( aliasExists && alias.equals("@door") ) )
							{
								buildCmd = true;
								// run the open function
								cmd_door(arg, client);
							}
							// pass arguments to the dungeon function
							// commented out on purpose
							/*else if ( cmd.equals("@dungeon") || ( aliasExists && alias.equals("@dungeon") ) )
					{
						buildCmd = true;
						int dX = 2;
						int dY = 2;
						Dungeon d = new Dungeon(arg, dX, dY);
						cmd_jump(d.dRooms[0][0].getDBRef() + "", client);
						cmd_look("", client);
						int dXc = 0;
						int dYc = 0;
						System.out.println(d.dRooms.length);
						for (int r = 0; r < dX * dY; r++)
						{
							if (dXc == 0)
							{
								System.out.println("Xexits" + ", " + r + ", " + "(" + dXc + ", " + dYc + ")");
								cmd_open("east=" + d.roomIds[r] + "=" + (d.roomIds[r] + 1), client);
								cmd_open("west=" + (d.roomIds[r] + 1) + "=" + d.roomIds[r], client);
							}
							if (dYc == 0)
							{
								System.out.println("Yexits" + ", " + r + ", " + "(" + dXc + ", " + dYc + ")");
								cmd_open("south=" + d.roomIds[r] + "=" + (d.roomIds[r] + dX), client);
								cmd_open("north=" + (d.roomIds[r] + dX) + "=" + d.roomIds[r], client);
							}
							if (r == dX - 1)
							{
								System.out.println("Increase in Y");
								dYc = dYc + 1;
								dXc = 0;
							}
							else
							{
								System.out.println("Increase in X");
								dXc = dXc + 1;
							}
						}
					}*/
							//
							else if ( cmd.equals("@fail") || (aliasExists && alias.equals("@fail") ) )
							{
								buildCmd = true;
								cmd_fail(arg, client);
							}
							// pass arguments to the itemedit function
							else if ( cmd.equals("@iedit") || ( aliasExists && alias.equals("@iedit") ) )
							{
								buildCmd = true;

								// indicate what is parsing commands
								debug("Command Parser: @iedit");

								// launch the item editor
								cmd_itemedit(arg, client);
							}
							// pass arguments to the jump function
							else if ( cmd.equals("@jump") || (aliasExists && alias.equals("@jump") ) )
							{
								buildCmd = true;
								cmd_jump(arg, client);
							}
							// pass arguments to the lsedit function
							else if ( cmd.equals("@lsedit") )
							{
								buildCmd = true;
								cmd_lsedit(arg, client); // run the list editor
							}
							else if ( cmd.equals("@ofail") || ( aliasExists && alias.equals("@ofail") ) )
							{
								buildCmd = true;
								cmd_ofail(arg, client); // set an ofail message
							}
							// pass arguments to the open function
							else if ( cmd.equals("@open") || ( aliasExists && alias.equals("@open") ) )
							{
								buildCmd = true;
								// run the open function
								cmd_open(arg, client);
							}
							//
							else if ( cmd.equals("@osuccess") || ( aliasExists && alias.equals("@osuccess") ) )
							{
								buildCmd = true;
								//
								cmd_osuccess(arg, client);
							}
							// pass arguments to the roomedit function
							else if ( cmd.equals("@redit") || ( aliasExists && alias.equals("@redit") ) )
							{
								buildCmd = true;

								//
								debug("Command Parser: @redit");

								// run the list editor
								cmd_roomedit(arg, client);
							}
							//
							else if ( cmd.equals("@success") || ( aliasExists && alias.equals("@success") ) ) {
								buildCmd = true;
								cmd_success(arg, client); // set an exit success message
							}


							if (buildCmd) {
								return;
							}
						}

						//debug("Exited build command loop.");

						//debug("Entering admin command loop...");

						// stash admin commands inside here
						if (player.getAccess() >= ADMIN) {
							if ( cmd.equals("@accounts") || ( aliasExists && alias.equals("@accounts") ) ) {
								if ( arg.equals("") ) {
									send("Accounts (Online)", client);
									send("-------------------------------------------------------------------------------------------", client);
									send("Name     ID     Player                                   Online Created            Age", client);
									send("-------------------------------------------------------------------------------------------", client);
									//send("Test     000001 Nathan                                   No     02-11-2011 02:36AM 365 days", client);
									if (this.accounts != null) {
										for (Account a : this.accounts) {
											send(a.display(), client);
										}
									}
									send("-------------------------------------------------------------------------------------------", client);
								}
								else {
									String[] args = arg.split(" ");

									if (args.length >= 3) {
										if (args[0].equals("+add")) {
											if (this.accounts != null) {
												client.write("Adding new account");

												Account account = new Account(this.accounts.size(), args[1], args[2], 5);

												account.linkCharacter(getPlayer(client));
												account.setPlayer(getPlayer(client));
												account.setClient(client);
												account.setOnline(true);

												this.accounts.add(account);
											}
										}
									}
								}
							}
							else if ( cmd.equals("@alias") || ( aliasExists && alias.equals("@alias") ) ) {
								adminCmd = true;
								commandMap.get(cmd).execute(arg, client);
							}
							// allocates space to a zone
							else if ( cmd.equals("@allocate") || ( aliasExists && alias.equals("@allocate") ) ) {
								adminCmd = true;
								cmd_allocate(arg, client);
							}
							// pass arguments to the backup function
							else if ( cmd.equals("@backdb") || ( aliasExists  && alias.equals("@backdb") ) )
							{
								adminCmd = true;
								// run the backup function
								cmd_backDB(arg, client);
								//send("Game> Backup Functionality Broken. Please stick to manual file saves.");
							}
							else if ( cmd.equals("@ban") || ( aliasExists && alias.equals("@ban") ) )
							{
								adminCmd = true;
								cmd_ban(arg, client);
							}
							else if ( cmd.equals("@bb") || ( aliasExists && alias.equals("@bb") ) )
							{
								adminCmd = true;
								cmd_bb(arg, client);
							}
							else if ( cmd.equals("@config") || ( aliasExists && alias.equals("@config") ) )
							{
								// use this to replace 'ansi' and 'msp' commands?
								// or possibly alias them to it?
								// @config ansi = on, @config ansi = off
								// @config msp = on, @config msp = off
								if ( arg.contains("=") ) {
									String[] args = arg.split("=");

									if ( config.containsKey( Utils.trim( args[0] ) ) ) {
										debug("Config> Setting '" + Utils.trim( args[0] ) + "' to '" + Utils.trim( args[1] ));
										config.put( Utils.trim( args[0] ), Utils.trim( args[1] ) );
										send("Game [config]> " + Utils.trim( args[0] ) + ": " + Utils.trim( args[1] ), client);
									}
									else {
										debug("Game [config]> no such configurable setting exists.");
										send("Game [config]> no such configurable setting exists.", client);
									}
								}
								else {
									if ( arg.equals("list") ) {
										send("Configuration Options", client);
										for (Entry<String, String> e : config.entrySet()) {
											send(e.getKey() + " : " + e.getValue(), client);
										}
									}
								}
							}
							else if ( cmd.equals("@control") || ( aliasExists && alias.equals("@control") ) )
							{
								adminCmd = true;
								// run the NPC control/takeover function?
								cmd_control(arg, client);
							}
							else if ( cmd.equals("@debug") || (aliasExists && alias.equals("@debug") ) ) {
								adminCmd = true;
								cmd_debug(arg, client);
							}
							else if ( cmd.equals("@find") || ( aliasExists && alias.equals("@find") ) ) {
								adminCmd = true;
								cmd_find(arg, client);
							}
							else if ( cmd.equals("@flags") || ( aliasExists && alias.equals("@flags") ) )
							{
								adminCmd = true;
								cmd_flags(arg, client);
							}
							else if ( cmd.equals("@hash") || ( aliasExists && alias.equals("@hash") ) ) {
								adminCmd = true;
								client.write("Hash of argument: '" + arg + "' is hash: '" + Utils.hash(arg) + "'.");
							}
							// pass arguments to the helpedit function
							else if ( cmd.equals("@hedit") || ( aliasExists && alias.equals("@hedit") ) )
							{
								adminCmd = true;
								// run the list editor
								cmd_helpedit(arg, client);
							}
							else if ( cmd.equals("@install") || (aliasExists && alias.equals("install") ) )
							{
								send("Syntax: @install <area file>", client);
								//install_area(arg);
							}
							else if ( cmd.equals("@kick") || ( aliasExists && alias.equals("@iedit") ) ) {
								adminCmd = true;
								// handle args and pass appropriate parameters to kick function
							}
							else if ( cmd.equals("@listprops") || ( aliasExists && alias.equals("@listprops") ) )
							{
								adminCmd = true;
								cmd_listprops(arg, client);
							}
							// pass arguments to the load database function
							else if ( cmd.equals("@loaddb") || (aliasExists && alias.equals("@loaddb") ) )
							{
								adminCmd = true;
								cmd_loadDB(arg, client); // load the database
							}
							else if ( cmd.equals("@makehouse") )
							{
								adminCmd = true;
								final House h = new House(getPlayer(client), 5);
								for (String s : h.getInfo()) { send(s, client); }
							}
							else if ( cmd.equals("@name") ) {
								adminCmd = true;
								cmd_name(arg, client);
							}
							else if ( cmd.equals("@nextdb") || ( aliasExists && alias.equals("@nextdb") ) )
							{
								adminCmd = true;
								if ( arg.equals("") ) {
									send("Next Database Reference Number (DBRef/DBRN): " + objectDB.peekNextId(), client);
								}
							}
							// pass arguments to the @passwd function
							else if ( cmd.equals("@passwd") || ( aliasExists && alias.equals("@passwd") ) )
							{
								adminCmd = true;
								// run the password change function
								cmd_passwd(arg, client);
							}
							// pass arguments to the pgm function
							else if ( cmd.equals("@pgm") || ( aliasExists && alias.equals("@pgm") ) )
							{
								adminCmd = true;
								// run the program interpreter
								System.out.println("PGM: <" + arg + ">");
								send(parse_pgm(arg), client);
							}
							// pass arguments to the set function
							else if ( cmd.equals("@set") || ( aliasExists && alias.equals("@set") ) )
							{
								adminCmd = true;
								cmd_set(arg, client);
							}
							else if ( cmd.equals("@session") )
							{
								/*
								 * notionally this should give data on the current or last
								 * of the specified player
								 */
								adminCmd = true;
								Player p = getPlayer(arg);
								if (p != null) {
									Session s = sessionMap.get(p);
									send("Connected: " + s.connected, client);
									send("Connect Time: " + s.connect, client);
									//send("Disconnect Time: " + s.disconnect, client);
									send("Player: " + s.getPlayer().getName(), client);
									send("Client (IP): " + s.getClient().ip(), client);
								}
							}
							else if ( cmd.equals("@setskill") || ( aliasExists && alias.equals("@setskill") ) )
							{
								adminCmd = true;
								cmd_setskill(arg, client);
							}
							else if ( cmd.equals("@tune") || ( aliasExists && alias.equals("@tune") ) ) {
								adminCmd = true;

								String[] args = arg.split(" ");

								if ( args[0].equals("cmdDelay") ) {
									final int delay = Utils.toInt(args[1], -1);

								}
							}
							else if ( cmd.equals("uninstall") || (aliasExists && alias.equals("uninstall") ) )
							{
								send("Syntax: uninstall <area file>", client);
							}
							else if ( cmd.equals("@zones") || ( aliasExists && alias.equals("@zones") ) )
							{
								adminCmd = true;
								cmd_zones(arg, client);
							}

							if (adminCmd) {
								return;
							}
						}

						//debug("Exited admin command loop.");

						//debug("Entered wizard command loop.");

						// stash wizard commands inside here
						if (player.getAccess() >= WIZARD) {
							if ( cmd.equals("@flag") || ( aliasExists && alias.equals("@flag") ) )
							{
								wizCmd = true;

								cmd_flag(arg, client);
							}
							else if ( cmd.equals("@flush") || ( aliasExists && alias.equals("@flush") ) ) {
								wizCmd = true;

								flush();
							}
							// pass arguments to the recycle function
							else if ( cmd.equals("@recycle") || (aliasExists && alias.equals("@recycle") ) )
							{
								cmd_recycle(arg, client);
							}
							else if ( cmd.equals("@sethour") ) {
								try {
									int hour = Integer.parseInt(arg);

									if (hour >= 0 && hour <= 23) {
										game_time.setHours(hour);

										send("Game> Hour set to " + hour, client);
									}
									else {
										send("Game> Invalid hour", client);
									}

									send("Game> Hour set to " + hour, client);
								}
								catch(NumberFormatException nfe) {
									nfe.printStackTrace();
									send("Game> Invalid hour", client);
								}
							}
							else if ( cmd.equals("@setminute") ) {
								try {
									int minute = Integer.parseInt(arg);

									if (minute >= 0 && minute <= 59) {
										game_time.setMinutes(minute);

										send("Game> Minute set to " + minute, client);
									}
									else {
										send("Game> Invalid minute", client);
									}

									send("Game> Minute set to " + minute, client);
								}
								catch(NumberFormatException nfe) {
									nfe.printStackTrace();
									send("Game> Invalid minute", client);
								}
							}
							// pass arguments to the shutdown function
							else if ( cmd.equals("@shutdown") || ( aliasExists && alias.equals("@shutdown") ) )
							{
								wizCmd = true;
								cmd_shutdown(arg, client);
							}
							else if ( cmd.equals("@setmode") || ( aliasExists && alias.equals("@setmode") ) )
							{
								wizCmd = true;
								cmd_setmode(arg, client);
							}

							if (wizCmd) {
								return;
							}
						}

						//debug("Exited wizard command loop.");

						//debug("Entering user commmand loop...");

						// stash user commands inside here
						if (player.getAccess() >= USER) {
							if ( cmd.equals("ansi") || (aliasExists && alias.equals("ansi") ) )
							{
								if ( arg.equals("on") ) {
									ansi = 1;
									client.write("\033[;1m"); // tell client to use bright version of ANSI Colors
									//send("> Using BRIGHT ANSI colors <", client); // indicate the use of bright ansi colors to the client
									send(rainbow("ANSI Color turned on."), client);
								}
								else if ( arg.equals("off") ) {
									ansi = 0;
									send("ANSI Color turned off.", client);
								}
								else {
									if (ansi == 0) { send("ansi: off", client); }
									else if (ansi == 1) { send("ansi: on", client); }
								}
							}
							else if ( cmd.equals("ask") ) {
								cmd_ask(arg, client);
							}
							else if ( cmd.equals("attack") || (aliasExists && alias.equals("attack") ) ) {
								commandMap.get(cmd).execute(arg, client);
							}
							else if ( cmd.equals("bash") ) {
								cmd_bash(arg, client);
							}
							else if ( cmd.equals("buy") || (aliasExists && alias.equals("buy") ) ) {
								cmd_buy(arg, client);
							}
							// pass arguments to the cast function
							else if ( cmd.equals("cast")  || (aliasExists  && alias.equals("cast") ) )
							{
								/* This stuff here places you in the interactive spell editor, it should
								 * not do so normally, or for every spell. the editor should only be launched
								 * under certain conditions, such as:
								 * 
								 * 
								 */
								//player.setStatus("EDT");
								//player.setEditor1(Editor.INTCAST); // Editor set to -intcast-

								// run the cast function
								//cmd_cast(arg, client);
								commandMap.get("cast").execute(arg, client);
							}
							else if ( cmd.equals("calendar") ) {
								cmd_calendar("", client);
							}
							else if ( cmd.equals("chargen") ) {
								player.setStatus("EDT");     // Status set to -Edit-
								player.setEditor(Editor.CHARGEN);

								op_chargen("start", client);
							}
							else if ( cmd.equals("chat") ) {
								//cmd_chat(arg, client);
								// lots of null pointer exceptions from below code (must be handling it
								// elsewhere with the internal command
								commandMap.get("chat").execute(arg, client);
								//commandMap.get(cmd).execute(arg, client);
							}
							// pass arguments to the commmands function
							else if ( cmd.equals("commands") || (aliasExists && alias.equals("commands") ) )
							{
								cmd_commands(arg, client);
							}
							else if ( cmd.equals("date") )
							{
								send(gameDate(), client);
							}
							else if ( cmd.equals("dedit") )
							{
								player.setStatus("EDT");
								player.setEditor(Editor.DESC);
							}
							else if ( cmd.equals("drink") ) {
								cmd_drink(arg, client);
							}
							// pass arguments to the drop function
							else if (cmd.equals("drop"))
							{
								// run the drop function
								//cmd_drop(arg, client);
								//commandMap.get("drop").execute(arg, client);
								commandMap.get(cmd).execute(arg, client);
							}
							// pass arguments to the equip function
							else if ( cmd.equals("equip") || (aliasExists && alias.equals("equip") ) )
							{
								// run the equip function
								cmd_equip(arg, client);
							}
							// pass arguments to the effects function
							else if ( cmd.equals("effects") || (aliasExists && alias.equals("effect") ) )
							{
								// run the effects function
								cmd_effects(arg, client);
							}
							// pass arguments to the examine function
							else if ( cmd.equals("examine") || (aliasExists && alias.equals("examine") ) )
							{
								// run the examine function
								//cmd_examine(arg, client);
								commandMap.get("examine").execute(arg, client);
								//commandMap.get(cmd).execute(arg, client);
							}
							else if ( cmd.equals("exchange") ) {
								cmd_exchange(arg, client);
							}
							else if ( cmd.equals("exits") ) {
								cmd_exits(arg, client);
							}
							else if ( cmd.equals("go") ) {
								cmd_go(arg, client);
							}
							else if ( cmd.equals("greet") ) {
								//cmd_greet(arg, client);
								//commandMap.get("greet").execute(arg, client);
								commandMap.get(cmd).execute(arg, client);
							}
							// pass arguments to the help function
							else if (cmd.equals("help") || (aliasExists && alias.equals("help")))
							{
								// run the help function
								//cmd_help(arg, client);
								//commandMap.get("help").execute(arg, client);
								commandMap.get(cmd).execute(arg, client);
							}
							else if (cmd.equals("home") || (aliasExists && alias.equals("home")))
							{
								try {
									Object o = player.getProps().get(cmd);
									String destination = o.toString();
									cmd_jump(destination, client);
								}
								catch(NumberFormatException nfe) {
									send("Exception (CMD:HOME): Invalid Destination!", client);
								}
							}
							// player housing information
							else if (cmd.equals("housing") || (aliasExists && alias.equals("housing")))
							{
								cmd_housing(arg, client);
							}
							else if ( cmd.equals("interact") ) {
								getPlayer(client).setStatus("INT"); // mark the player as in interaction mode
								NPC npc = getNPC(arg);              // get the npc by name
								send(npc.getName(), client);        // tell us his/her name
								getPlayer(client).setTarget(npc);   // "target" the npc
								/*if (npc.getFlags().contains("V")) {
							debugP("Target is NPC.");
							debugP("Target is Vendor");
							Vendor v = npc;
							if (v instanceof WeaponMerchant) {
								debugP("Target is WeaponMerchant.");
								debugP("Using default WeaponMerchant interaction.");
								((WeaponMerchant) v).interact();
							}
							else if (v instanceof ArmorMerchant) {
								debugP("Target is ArmorMerchant.");
								debugP("Using default ArmorMerchant interaction.");
								((ArmorMerchant) v).interact();
							}
							else {
								v.interact();
								debugP("Using default Vendor interaction.");
							}
						}
						else {
							debugP("Target is NPC.");
							debugP("Using default NPC interaction.");
							ArrayList<Message> msgs = npc.interact(0);
							for (Message m : msgs) { addMessage(m); }
						}*/
								//npc.interact();
							}
							// pass arguments to the inventory function (prototype)
							else if ( cmd.equals("inventory") || (aliasExists && alias.equals("inventory") ) )
							{
								// run the inventory function
								cmd_inventory(arg, client);
							}
							else if ( cmd.equals("levelup") ) {
								if ( player.isLevelUp() ) {
									player.changeLevelBy(1);
									send("You leveled up to level " + player.getLevel() + "!", client); 
								}
								else {
									send("You are not currently read to level-up.", client);
								}

							}
							else if ( cmd.equals("lock") ) {
								cmd_lock(arg, client);
							}
							// pass arguments to the look function
							else if ( cmd.equals("look") || (aliasExists && alias.equals("look") ) )
							{
								cmd_look(arg, client);
							}
							// pass arguments to the list function
							else if ( cmd.equals("list") || (aliasExists && alias.equals("list") ) )
							{
								cmd_list(arg, client);
							}
							// pass arguments to the mail function
							else if ( cmd.equals("mail") || (aliasExists && alias.equals("mail") ) )
							{
								//cmd_mail(arg, client);
								//commandMap.get("mail").execute(arg, client);
								commandMap.get(cmd).execute(arg, client);
							}
							else if ( cmd.equals("map") || (aliasExists && alias.equals("map") ) )
							{
								cmd_map(arg, client);
							}
							else if ( cmd.equals("money") || ( aliasExists && alias.equals("money") ) )
							{
								send("You have " + player.getMoney() + ".", client);
							}
							// call the motd function
							else if ( cmd.equals("motd") || ( aliasExists && alias.equals("motd") ) )
							{
								// run the motd function
								send(MOTD(), client);
							}
							// pass arguments to the move function
							else if ( cmd.equals("move") || ( aliasExists && alias.equals("move") ) )
							{
								// run the move function
								cmd_move(arg, client);
							}
							else if ( cmd.equals("msp") || ( aliasExists && alias.equals("msp") ) )
							{
								if ( arg.equals("on") ) {
									msp = 1;
									send("Game> MSP turned on.", client);
								}
								else if ( arg.equals("off") ) {
									msp = 0;
									send("Game> MSP turned off.", client);
								}
								else {
									if (msp == 0) { send("Game> MSP: off", client); }
									else if (msp == 1) { send("Game> MSP: on", client); }
								}
							}
							else if ( cmd.equals("nameref") || ( aliasExists && alias.equals("nameref") ) )
							{
								cmd_nameref(arg, client);
							}
							// pass arguments to the pgm function
							else if ( cmd.equals("page") || ( aliasExists && alias.equals("page") ) )
							{
								// run the page function
								cmd_page(arg, client);
							}
							else if ( cmd.equals("pinfo") || ( aliasExists && alias.equals("pinfo") ) )
							{
								cmd_pinfo(arg, client);
							}
							else if ( cmd.equals("push") ) {
								Thing t = getThing(arg, client); 

								push(t, client);
							}
							else if ( cmd.equals("prompt") ) {
								prompt(client);
							}
							else if ( cmd.equals("quests") || (aliasExists && alias.equals("quests") ) ) {
								cmd_quests(arg, client);
							}
							// pass arguments to the quit function
							else if ( cmd.equals("quit") || (aliasExists && alias.equals("QUIT") ) )
							{
								// run the quit function
								cmd_quit(arg, client);
								return;
							}
							//
							else if ( cmd.equals("roll") || (aliasExists && alias.equals("roll") ) )
							{
								String[] args = arg.split(",");
								try {
									int number = Integer.parseInt(args[0]);
									int sides = Integer.parseInt(args[1]);

									System.out.println("Rolling " + number + "d" + sides);

									System.out.println( Utils.roll(number, sides) );
								}
								catch(NumberFormatException nfe) {
									nfe.printStackTrace();
								}
							}
							else if ( cmd.equals("say") || (aliasExists && alias.equals("say") ) )
							{
								cmd_say(arg, client);
							}
							else if ( cmd.equals("score") ) {
								cmd_score(arg, client);
							}
							else if ( cmd.equals("sethp") || (aliasExists && alias.equals("sethp") ) )
							{
								// DM/Debug Command
								int changeHP = 0;
								if (!arg.equals("")) {
									try {
										System.out.println("ARG: " + arg);
										changeHP = Integer.parseInt(arg);
										System.out.println("INTERPRETED VALUE: " + changeHP);
										System.out.println("SIGN: " + Integer.signum(changeHP));
										if (Integer.signum(changeHP) > 0) {
											player.setHP(changeHP);
											send("Game> Gave " + player.getName() + " " + changeHP + " hitpoints (hp).", client);
										}
										else if (Integer.signum(changeHP) < 0) {
											player.setHP(changeHP);
											send("Game> Gave " + player.getName() + " " + changeHP + " hitpoints (hp).", client);
										}
										else {
											send("Game> No amount specified, no hitpoint (hp) change has been made.", client);
										}

										player.updateCurrentState();
									}
									catch(NumberFormatException nfe) {
										nfe.printStackTrace();
									}
								}
							}
							else if ( cmd.equals("setmana") || (aliasExists && alias.equals("setmana") ) )
							{
								// DM/Debug Command
								if (!arg.equals("")) {
									System.out.println("ARG: " + arg);
									final int changeMANA = Utils.toInt(arg, 0);
									System.out.println("INTERPRETED VALUE: " + changeMANA);
									System.out.println("SIGN: " + Integer.signum(changeMANA));
									if (Integer.signum(changeMANA) > 0) {
										player.setMana(changeMANA);
										send("Game> Gave " + player.getName() + " " + changeMANA + " mana (mana).", client);
									}
									else if (Integer.signum(changeMANA) < 0) {
										player.setMana(changeMANA);
										send("Game> Gave " + player.getName() + " " + changeMANA + " mana (mana).", client);
									}
									else {
										send("Game> No amount specified, no mana (mana) change has been made.", client);
									}

									player.updateCurrentState();
								}
							}
							else if ( cmd.equals("setlevel") ) {
								// DM/Debug Command
								if (!arg.equals("")) {
									try {
										System.out.println("ARG: " + arg);
										final int changeLevel = Integer.parseInt(arg);
										System.out.println("INTERPRETED VALUE: " + changeLevel);
										System.out.println("SIGN: " + Integer.signum(changeLevel));

										player.changeLevelBy(changeLevel);
										send("Game> Gave " + player.getName() + " " + changeLevel + " levels (levels).", client);
									}
									catch(NumberFormatException nfe) {
										nfe.printStackTrace();
									}
								}
								else {
									send("Game> No amount specified, no experience (xp) change has been made.", client);
								}

							}
							else if ( cmd.equals("setxp") || (aliasExists && alias.equals("setxp") ) )
							{
								// DM/Debug Command
								if (!arg.equals("")) {
									System.out.println("ARG: " + arg);
									final int changeXP = Utils.toInt(arg, 0);
									System.out.println("INTERPRETED VALUE: " + changeXP);
									System.out.println("SIGN: " + Integer.signum(changeXP));

									player.setXP(changeXP);
									send("Game> Gave " + player.getName() + " " + changeXP + " experience (xp).", client);
								}
								else {
									send("Game> No amount specified, no experience (xp) change has been made.", client);
								}
							}
							else if ( cmd.equals("sheathe") ) {
								if (player.getSlots().get("weapon").isFull() ) {
									final Weapon w = (Weapon) player.getSlots().get("weapon").remove();
									//temp.inventory.add(temp.getSlots().get("weapon").remove());
									send("You put away your " + w.getName(), client);
								}
								else if ( player.getSlots().get("weapon1").isFull()  ) {
									final Weapon w = (Weapon) player.getSlots().get("weapon").remove();
									//temp.inventory.add(temp.getSlots().get("weapon").remove());
									send("You put away your " + w.getName(), client);
								}
							}
							else if ( cmd.equals("skillcheck") ) {
								final String args[] = arg.split(" ");
								if (args.length >= 4) {
									final String dice = args[0];
									int skillValue = Integer.parseInt(args[1]);
									int skillMod = Integer.parseInt(args[2]);
									int DC = Integer.parseInt(args[3]);
									send(skill_check(Skills.BALANCE, dice, skillValue, skillMod, DC), client);
									send(skill_check(getPlayer(client), Skills.BALANCE, dice, DC), client);
								}
							}
							else if ( cmd.equals("spells") || (aliasExists && alias.equals("spells") ) )
							{
								client.write("Spell List\n");
								client.write("-----------------------------------------------------------------\n");
								if ( arg.equals("#all") ) {
									// list all the spells, by level?
									for (final Spell spell : this.spells2.values()) {
										client.write(spell.school.toString() + " " + spell.toString() + "\n");
									}
								}
								else {
									// list your spells, by level?
								}
								client.write("-----------------------------------------------------------------\n");
								System.out.println(spells2.entrySet());
							}
							// pass arguments to the stats function
							else if ( cmd.equals("stats") || (aliasExists && alias.equals("stats") ) )
							{
								// run the stats function
								cmd_stats(arg, client);
							}
							else if ( cmd.equals("status") )
							{
								cmd_status(arg, client);
							}
							// pass arguments to the take function
							else if ( cmd.equals("take") )
							{
								// run the take function
								cmd_take(arg, client);
							}
							else if ( cmd.equals("target") ) {
								cmd_target(arg, client);
							}
							else if ( cmd.equals("time") )
							{
								send(gameTime(), client);
							}
							else if ( cmd.equals("unequip") || (aliasExists && alias.equals("unequip") ) )
							{
								// run the drop function
								cmd_unequip(arg, client);
							}
							else if ( cmd.equals("unlock") ) {
								cmd_unlock(arg, client);
							}
							else if ( cmd.equals("use") || (aliasExists && alias.equals("use") ) )
							{
								cmd_use(arg, client);
							}
							else if ( cmd.equals("version") ) {
								send(serverName + " " + version, client);
							}
							else if ( cmd.equals("vitals") || (aliasExists && alias.equals("vitals") ) ) {
								cmd_vitals(arg, client);
							}
							// pass arguments to the where function
							else if ( cmd.equals("where") || (aliasExists && alias.equals("where") ) )
							{
								// run the where function
								cmd_where(arg, client);
								//commandMap.get("where");
								//commandMap.get(cmd).execute(arg, client);
							}
							// pass arguments to the who function
							else if ( cmd.equals("who") || (aliasExists && alias.equals("who") ) )
							{
								// run the where function
								cmd_who(arg, client);
							}
							// if the command doesn't exist say so
							else
							{
								debug("Exit? " + cmd);
								// exit handling
								if (cmd.matches("[a-zA-Z]+")) // no nums
								{

									// This will print to the console.
									debug("Found a match in '" + cmd + "'");

									// has the user given an action/exit that is linked to something for which no similarly named command exists,
									// if so, execute link or move user in the direction/to the room specified by the action/exit
									// handle the command as an exit
									if (!exitHandler(cmd, client)) {
										if (!chatHandler(cmd, arg, client)) {
											if ( !adminCmd && !wizCmd ) { // not an admin or wizard command
												send("Huh? That is not a known command.", client);
												debug("Command> Unknown Command");
											}
										}
									}
								}
								else
								{
									debug("No match found in '" + cmd + "'");

									send("Huh? That is not a known command.", client);
									debug("Command> Unknown Command");
								}
							}
						}

						//debug("Exited user commmand loop.");
					}
				}
			}

			/*
			 * Commands
			 * 
			 * Command parameter shall in all cases conform to containing no more than, but maybe
			 * less than a String arg and a Client client as parameters.
			 * 
			 * function name syntax 'cmd_<name>(String arg, Client client)'
			 * 
			 * should functions check access permissions, or the command loop?
			 * note: currently it might be being checked in the command loop
			 */

			/**
			 * Command: access
			 * 
			 * Alter a player's access permissions
			 * 
			 * COMMAND OBJECT EXISTS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_access(final String arg, final Client client) {
				// syntax: access <player>=<access level denoted by integer -- 0 is none/1 is admin>
				final String[] args = arg.split("=");
				if (args.length > 0) {
					Player player = getPlayer(args[0]);
					if (args.length > 1) {
						if (player != null) {
							try {
								player.setAccess(Integer.parseInt(args[1]));
								send(player.getName() + "'s access level set to " + player.getAccess(), client);
							}
							catch(NumberFormatException nfe) {
								send("Invalid access level!", client);
							}
						}
						else {
							send("No such player!", client);
						}
					}
					else {
						if (player != null) {
							try {
								send(player.getName() + "'s access level is " + player.getAccess(), client);
							}
							catch(NumberFormatException nfe) {
								send("Invalid access level!", client);
							}
						}
						else {
							send("No such player!", client);
						}
					}
				}
				else {
					send(gameError("@access", 1), client); // Invalid Syntax Error
				}
			}

			/**
			 * Command: @allocate
			 * 
			 * allocates space to a valid zone up to a max size (in rooms) reusing
			 * all unused dbrefs, and then proceeds to supply a continous block of new dbrefs
			 * until the allocation amount is reached
			 * 
			 * NOTE: I need a way to hold onto these for the account/player that requested them and guarantee
			 * safe, exclusive access to them
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_allocate(final String arg, final Client client) {
				objectDB.allocate(Utils.toInt(arg, 10));
			}

			/**
			 * Command: ask
			 * 
			 * Ask an npc something
			 * 
			 * NOTE: Serves as a general npc interaction tool for acquiring information
			 * and getting quests
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_ask(final String arg, final Client client) {
				final String[] args = arg.split(" ");

				if (args.length < 2) {
					return;
				}

				final Player player = getPlayer(client); // get the player
				final NPC npc = getNPC(args[0]);         // get the npc we're referring to

				final String keyword = args[1];          // get the keyword;

				if ( keyword.equals("quests") ) {
					final List<Quest> suitable = npc.getQuestsFor(player);

					send("Available Quests", client);
					send("================================================================================", client);

					for (final Quest quest : suitable) {
						if (!quest.isComplete()) {
							client.write(quest.toDisplay());
						}
						client.write("" + Colors.WHITE);
					}

					send("================================================================================", client);
					send("* To accept a quest, type 'ask <npc> accept <quest identifier>')", client);
					send("* If you don't see a quest here, you are currently on it / already completed it.", client);
					send("* Also, finished quests are greyed out.", client);
					send("** For the moment, the quest identifier is the quest's index in the list (0-?)", client);
				}
				else if ( keyword.equals("accept") ) {
					if ( args.length == 3 ) {
						final Quest q = npc.getQuestFor(player, Utils.toInt(args[2], -1));
						if (q != null) {
							player.getQuests().add(q);
							send("Quest Added!", client);
						}
					}
				}
			}

			/**
			 * Command: Assign
			 * 
			 * Assign a range numbers to an area or a dungeon?
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_assign(final String arg, final Client client) {
			}

			/**
			 * Command: @backdb
			 * 
			 * Backup the database
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_backDB(final String arg, final Client client)
			{
				/*
		// tell us that the database is being backed up (supply custom message?)
		send("Game> Backing up Database!", client);

		// NOTE: for these to work, I need to convert my database to one file so that the database line for an item and it's dbref number are the same
		client.write("Game> Backing up Players...");
		client.write("Done.\n");
		client.write("Game> Backing up Rooms...");
		client.write("Done.\n");
		client.write("Game> Backing up Items...");
		client.write("Done.\n");

		// save databases to disk
		int index = 0;
		String[] toSave = new String[main.size()];
		for (String s : main) {
			toSave[index] = s;
			index++;
		}
		saveStrings(mainDB, toSave);    // modifies 'real' files

		// tell us that backing up is done (supply custom message?)
		send("Game> Done.", client);
				 */
				send("Backup is buggy, therefore it is disabled", client);
				//send(backup(), client);
				//sys_backup2();
			}

			/**
			 * Command: @ban
			 * 
			 * Ban a player by IP address and/or name
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_ban(final String arg, final Client client) {
				if (!arg.equals("")) {
					final Player player1 = getPlayer(arg); // player name based search (banning a player should ban his account as well
					final Client client1 = player1.getClient();
					if (player1 != null) {
						// add the player's ip address to the banlist (IP address ban)
						banlist.add(client1.ip());
						cmd_page(arg + ", you have been banned.", client1);
						kick(client1);
					}
					else {
						// update my code so it stores the last login ip
						// for players, so I can ban them even if they log off
						// also, add a banned check to logging in so they still can't
						// login, even if they change their ip?
						debug("That player is not connected");
						send("That player is not connected", client);
					}

					/*else if (arg.equals("#list")) {
				for (String s : banlist) {
					send(s, client);
				}
			}*/
				}
				else {
					send("Command> '@ban' : No valid arguments.", client);
				}
			}

			/**
			 * Command: bash
			 * 
			 * Physically attack something
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_bash(final String arg, final Client client) {
				getPlayer(client).setMode(PlayerMode.COMBAT);  // set the play mode to COMBAT
			}

			/**
			 * Command: @bb
			 * 
			 * Show the game's main/built-in bulletin board(s)
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_bb(final String arg, final Client client) {
				final String[] args = arg.split(" ");

				for (final String s : args) {
					debug(s);
				}

				String subject;
				String message;

				if (args.length >= 2) {
					if (args[0].equals("+add")) {
						String[] entry = args[1].split("=");
						if (entry.length >= 2) {
							subject = entry[0];
							message = entry[1];
							bb.write(subject, message, client);
						}
						else {
							send(gameError("@bb", 1), client);
						}
					}
					else if (args[0].equals("+delete")) {
						int messageNum = Integer.parseInt(args[1]);
						ArrayList<BBEntry> entries = bb.getEntries();

						Player player = getPlayer(client);
						BBEntry entry = entries.get(messageNum);

						if (entry.getAuthor().equals(player.getName())) {
							bb.removeEntry(messageNum);
							debug("Renumbering entries...");
							bb.renumber(messageNum);
							debug("Done.");
						}
						// takes an index to remove?
						// in future more complicated
					}
					else if (args[0].equals("+read")) {
						for (final String s : bb.read()) {
							client.write(s + '\n');
						}
					}
				}
				else {
					cmd_help("@bb", client);
				}
			}

			/**
			 * Command: buy
			 * 
			 * Buy an item from a vendor
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_buy(final String arg, final Client client) {

				final Player player = getPlayer(client);

				if (player.getStatus().equals("INT")) { // interact mode
					NPC npc = (NPC) player.getTarget();
					send(npc.getName(), client);               // tell us his/her name

					debug("Target is NPC.");

					if ( npc.getFlags().contains("V") ) { // if the npc is a vendor
						debug("Target is Vendor");

						Vendor v = (Vendor) npc; // Cast npc as a vendor into a vendor reference

						Item item;  //
						Item item1; //

						if (v.hasItem(arg)) // if the vendor had that item
						{
							item = v.getItem(arg);

							if ( player.getMoney().isMoreOrEqual(item.getCost()) ) {
								item1 = v.buy(arg);

								player.setMoney(item1.getCost());

								send("Item Bought!", client); // tell us that we bought the item

								send(npc.getName() + " takes your money and gives you a " + item1.getName(), client); // response

								item1.setLocation(player.getDBRef()); // change item's location

								player.getInventory().add(item1);     // add the item to the player's inventory

								npc.say("Have a nice day.");          // npc response
							}
							else { send("I'm sorry, you can't afford that.", client); }
						}
						else { send("I'm sorry, we don't sell that.", client); }
					}
				}
				else {
					debug("Target is not npc.");
				}
			}

			/**
			 * Command: calendar
			 * 
			 * draw a calendar (uses ANSI colors)
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_calendar(final String arg, final Client client) {
				int daysOfMonth = DAYS[month];

				client.write(month_name);
				client.write('\n');

				String start;

				// the month starts on the first day
				for (int i = 1; i <= daysOfMonth; ++i) {

					if (i < 10) { start = "|  "; }
					else { start = "| "; }

					if (i == day) {
						client.write(start + colors(i + "", "green") + " ");
						continue;
					}

					if (i % 7 > 0) { // it's not the end of the week
						client.write(start + i + " ");
					}
					else { // it is the end of the week
						if (i != daysOfMonth) { // if it's not the last day of the month
							client.write(start + i + " |\n");
						}
						else { // if it is
							client.write(start + i + " |");
						}
					}

				}

				client.write('\n');
				client.write("Today is the " + day + " of " + month_name);
				client.write('\n');
			}

			/**
			 * Command: cast
			 * 
			 * Cast a spell
			 * 
			 * COMMAND OBJECT EXISTS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_cast(final String arg, final Client client)
			{
				String[] args = arg.split(" ");

				Player player = getPlayer(client);

				Spell spell = null;

				try {
					spell = spells2.get(args[0]);
				}
				catch (NullPointerException npe) {
					npe.printStackTrace();
				}
				//finally { spell = null; }

				if (spell != null) {
					// reagents check
					// cast spell
					// send cast msg
					send(spell.castMsg, client);
					// apply effects
					for (int e = 0; e < spell.effects.size(); e++) {
						Effect effect = spell.effects.get(e);
						// covers dispel case for now
						// will need serious work later
						if (effect.getName().contains("!any")) {
							player.removeEffects();
							send("All Effects removed!", client);
						}
						// remove effect it ! is present
						else if (effect.getName().contains("!")) {
							String effectName = effect.getName().substring(effect.getName().indexOf("!") + 1, effect.getName().length());
							player.removeEffect(effectName);
							send(effectName + " effect removed!", client);
						}
						// add effects
						else {
							applyEffect(player, effect);
							//player.addEffect(effect.getName());
							send(effect.getName() + " Effect applied to " + player.getName(), client);
							debug("Game> " + "added " + effect.getName() + " to " + serverName + ".");
						}
					}
				}
				else {
					send("You move your fingers and mumble, but nothing happens. Must have been the wrong words.", client);
				}
			}

			public void addToStaffChannel(final Player player) throws Exception {
				chan.add(player, STAFF_CHANNEL);
			}

			public void removefromStaffChannel(final Player player) {
				chan.remove(player, STAFF_CHANNEL);
			}

			/**
			 * Chat Command
			 *
			 * NOTE: probably shouldn't be directly acting on the ChatChannel object
			 * 
			 * COMMAND OBJECT EXISTS
			 * 
			 * @param arg
			 * @param client
			 */
			public void cmd_chat(final String arg, final Client client) {
				String[] args = arg.split(" ");
				// argument: show a list of available channels and whether you are on them
				if ( arg.toLowerCase().equals("#channels") ) {
					client.write("Chat Channels\n");
					client.write("--------------------------------\n");
					for (final String chanName : chan.getChannelNames()) {
						client.write(Utils.padRight(chanName, 8));
						client.write(" ");
						if (chan.isPlayerListening(chanName, getPlayer(client))) {
							client.write(Colors.GREEN.toString());
							client.write("Enabled");
							client.write(Colors.WHITE.toString());
							client.write("\n");
						}
						else {
							client.write(Colors.RED.toString());
							client.write("Disabled");
							client.write(Colors.WHITE.toString());
							client.write("\n");
						}
					}
					client.write("--------------------------------\n");
				}
				else if (args.length > 1) {
					String channelName = args[0];
					String msg = arg.replace(channelName + " ", "");
					if (!chan.hasChannel(channelName)) {
						client.write("Game> No such chat channel.");
						return;
					}

					// argument: show listeners on a specific channel
					if ( args[1].toLowerCase().equals("#listeners") ) {
						client.write("Listeners on Chat Channel: " + channelName.toUpperCase() + "\n");
						client.write("------------------------------\n");
						for (final Player p : chan.getListeners(channelName)) {
							client.write(p.getName() + "\n");
						}
						client.write("------------------------------\n");
					}
					// if the channel name is that specified, write the message to the channel
					else {
						chan.send(channelName, getPlayer(client), msg);
						client.write("wrote " + msg + " to " + channelName + " channel.\n");
					}
				}
			}

			/**
			 * Command: climb
			 * 
			 * Climb something
			 * 
			 * NOTE: this an invocation of skill usage, should I deviate the naming convention
			 * or other things to make note of this? Can generalize the input somehow so that using the command
			 * name can invoke the skill regardless of an independent command name check for each skill
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_climb(final String arg, final Client client) {
				final Player player = getPlayer(client);

				// identify the thing to be climbed, if it's possible
				Thing thing = objectDB.getThing(getRoom(client).getDBRef(), arg); // ex. box, ladder, building

				/* placeholder junk for checking to see if we are close enough to the object
				 * to act on it directly
				 */

				// check distance from object
				if( distance( player.getCoordinates(), thing.getCoordinates() ) <= 1 ) {

					// get the check for it's difficulty (static assign for testing purposes)
					int difficultyCheck = 10;

					// check to see if we can climb it
					boolean canClimb = skill_check(player, Skills.CLIMB, "1d4+1", difficultyCheck);

					// evaluate results
					if (canClimb) {
						//Integer height = (Integer) thing.attributes.get("height");
						Integer height = 1;
						if( height != null ) {
							if(height > 1) {
								send("You start climbing <direction> the " + thing.getName().toLowerCase(), client);
							}
							else if(height == 1) {
								send("You climb the " + thing.getName().toLowerCase(), client);
								player.setXCoord(thing.getXCoord());
								player.setYCoord(thing.getXCoord());
								player.coord.incZ(1);
							}
						}
					}
					// answer dependent on how badly check was failed
					else {
					}
				}
				else {
					send("That's too far away.", client);
				}
			}

			/**
			 * Command: connect
			 * 
			 * Connect to the game via a player or an account and then a player
			 * 
			 * NOTE: if I add all the usernames and passwords to a hashmap I could immensely simplify this,
			 * although some of the structure would change quite a bit
			 * I would check for a username (key) in the hashmap by searching for the username supplied then attempt to get it
			 * and test the password (value) against the password supplied
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_connect(final String arg, final Client client)
			{
				// Trim the argument of any additional whitespace
				final String[] args = Utils.trim(arg).split(" ");

				if (args.length < 1 || args.length > 2) {
					// unless I get accounts working the way I want that last line will be a lie.
					/*send("Enter a valid character creation or connection string\n" +
					"such as 'create <character name> <password>' or 'connect <character name> <password>'" +
					"NOTE: Using an valid account name and password will trigger the account options menu");*/
					send("Enter a valid character creation or connection string\n" +
							"such as 'create <character name> <password>' or 'connect <character name> <password>'", client);
					return;
				}

				final String user = Utils.trim(args[0]);
				final String pass = args.length > 1 ? Utils.trim(args[1]) : "";

				debug("User?: " + user);
				debug("Password?: " + Utils.padRight("", '*',pass.length()) );
				debug("");

				// Guest Player
				// SERIOUS: got a problem here, system does not seem to know guests are connected
				// or something like that, cannot use QUIT command for some reason
				// NOTE: my problem is related to guest not being findable somehow, all this needs a major revamp
				if ((user.toLowerCase().equals("guest")) && (pass.toLowerCase().equals("guest"))) {
					if (guest_users == 1) {
						final Player player = new Player(-1, "Guest" + guests, EnumSet.of(ObjectFlag.PLAYER, ObjectFlag.GUEST), "A guest player.", WELCOME_ROOM, "", Utils.hash("password"), "OOC", new Integer[] { 0, 0, 0, 0, 0, 0 }, Coins.copper(0));
						objectDB.addAsNew(player);
						init_conn(player, client, false);
						guests++;
					}
				}
				else if (user.toLowerCase().equals("new")) {
					final Player player = new Player(-1, "randomName", startFlags.clone(), "New player.", WELCOME_ROOM, "", Utils.hash("randomPass"), "NEW", new Integer[] { 0, 0, 0, 0, 0, 0 }, Coins.copper(0));
					objectDB.addAsNew(player);
					init_conn(player, client, false);
				}
				else if ( user.toLowerCase().equals("account") ) {
					try {
						client.write("Account Name?");
						final String aName = client.getInput();
						System.out.println("Name: " + aName);

						client.write("Account Password?");
						final String aPass = client.getInput();
						System.out.println("Password: " + aPass);

						final Account account1 = getAccount(aName, aPass);
						if (account1 != null) {
							account_menu(account1, client);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					/*
					 * NOTE:
					 * if all players always existed, then instead of instantiating a player i'd
					 * simply assign a client to it. Otherwise I need to get the player data from
					 * somewhere so I can load it up.
					 */

					// account check

					/*
					 * I don't want account names to conflict with characters, so perhaps
					 * I will insert a stopgap measure where you must indicate an account
					 * like this:
					 *
					 * connect account
					 *
					 * If the user input is 'account' we will assume you want to connect to
					 * an account and will do an interactive login for you.
					 *
					 * Other we will look for a character by the name given.
					 */

					// character check
					final Player p = objectDB.getPlayer(user);
					if (p == null || !p.getPass().equals(Utils.hash(pass))) {
						debug("CONNECT: Fail");
						send("Either that player does not exist, or has a different password.", client);
						return;
					}

					debug("PASS: Pass"); // report success for password check

					if (mode == GameMode.NORMAL) {
						init_conn(p, client, false); // Open Mode
					}
					else if (mode == GameMode.WIZARD) {
						if ( p.getFlags().contains("W") ) {
							init_conn(p, client, false); // Wizard-Only Mode
						}
						else {
							send("Sorry, only Wizards are allowed to login at this time.", client);
						}
					}
					else if (mode == GameMode.MAINTENANCE) {
						send("Sorry, the mud is currently in maintenance mode.", client);
					}
					else {
						send("Sorry, you cannot connect to the mud at this time. Please try again later.", client);
					}
				}

			}

			/**
			 * Command: control
			 * 
			 * Allow someone with sufficient permissions to control an npc
			 * 
			 * NOTE: needs fixing to be sure no one can end up in control of another player
			 * NOTE: should npcs have some kind of controllable measure so that a person can
			 * control some npcs but not others? 
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_control(final String arg, final Client client) {
				final Player player = getPlayer(client);

				if (arg.toLowerCase().equals("#break")) { // NOTE: Looks okay
					debug("DM Control Table:");
					debug(playerControlMap);

					playerControlMap.stopControllingAnyone(player);

					debug("DM Control Table:");
					debug(playerControlMap);
				}
				else { // should not be able to use this to control other players (at least not normally) NOTE: needs work
					final Player npc = getNPC(arg);

					debug(player);
					debug(npc);

					if (npc instanceof NPC) {
						debug("DM Control Table:");
						debug(playerControlMap);

						final Player oldSlave = playerControlMap.control(player, npc);
						
						player.setController(true); // mark the player as controlling an npc (commented out in PlayerControlMap)
						oldSlave.setAccess(USER);   // revoke any greater privileges granted

						debug("DM Control Table:");
						debug(playerControlMap);
					}
					else {
						send("Players are not controllable, only NPCs", client);
					}
				}
			}

			/**
			 * Command: create
			 * 
			 * Create a new player
			 * 
			 * NOTE: would possibly need to be revised if a multi-player account was what was logged into,
			 * rather than a single player. Namely, how do I handler character creation from the account menu?
			 * 
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_create(final String arg, final Client client)
			{
				String user;                                              // username
				String pass;                                              // password

				// get the username and password from the command arguments
				try {
					user = arg.substring(0, arg.indexOf(" "));            // new user name
					user = Utils.trim(user);                              // new user name (trimmed)
					pass = arg.substring(arg.indexOf(" "), arg.length()); // new user password
					pass = Utils.trim(pass);                              // new user password (trimmed)
				}
				catch (ArrayIndexOutOfBoundsException aioobe) {
					aioobe.printStackTrace();
					return;
				}

				// check for existing player by that name, if exists report that the name is already used, if not continue on
				if (!objectDB.hasName(user) && validateName(user))
				{
					// create a new player object for the new playerm the "" is an empty title, which is not currently persisted
					final Player player = new Player(-1, user, startFlags.clone(), start_desc, start_room, "", Utils.hash(pass), start_status, start_stats, Coins.copper(0));
					objectDB.addAsNew(player);

					try {
						new Mail(user, "Welcome", "Welcome to " + serverName).saveToFile(DATA_DIR + "mail\\mail-" + user + ".txt");
					} catch (Exception e) {
						e.printStackTrace();
					}

					send("Welcome to the Game, " + user + ", your password is: " + pass, client);

					// initiate the connection
					init_conn(player, client, true);

					// add player to the auth table
					objectDB.addPlayer(player);
				}
				else
				{
					// indicate the unavailability and/or unsuitability of the chosen name
					send("That name is not available, please choose another and try again.", client);
				}
			}

			/**
			 * Command: check
			 * 
			 * Check for and lists the exits on a room, some visibility and state data about them
			 * would be useful (default locking state, type of lock, etc). Such information would help
			 * with troubleshooting any future problems.
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_check(final String arg, final Client client)
			{
				final Room room = getRoom(client);

				debug(room.getExits());

				for (final Exit exit : room.getExits())
				{
					send(exit.getName(), client);
					send("\tsuccess: " + exit.succMsg, client);
					send("\tosuccess: " + exit.osuccMsg, client);
					send("\tfail: " + exit.failMsg, client);
					send("\tofail: " + exit.ofailMsg, client);
				}
			}

			/**
			 * Command: commands
			 * 
			 * List the available commands
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_commands(final String arg, final Client client)
			{
				if (arg.equals("")) {
					String out = "";
					for (String key : commandMap.keySet()) {
						debug(key);
						if (out.equals("")) { out += key; }
						else { out += ", " + key; }
					}
					send(colors("mapped: ", "yellow") + out, client);
					send(colors("user commands: ", "green") + Utils.join(user_cmds, ","), client);

					if (getPlayer(client).getAccess() >= BUILD)
					{
						send(colors("builder commands: ", "cyan") + Utils.join(build_cmds, ","), client);
					}
					if (getPlayer(client).getAccess() >= ADMIN)
					{
						send(colors("admin commands: ", "red") + Utils.join(admin_cmds, ","), client);
					}
					if (getPlayer(client).getAccess() >= WIZARD)
					{
						send(colors("admin commands: ", "magenta") + Utils.join(wiz_cmds, ","), client);
					}
				}
			}

			/**
			 * Command: Create Item
			 * Permissions: Admin?
			 * 
			 * Arbitrarily create an item with the specified name
			 * 
			 * @param arg
			 * @param client
			 */
			/*private void cmd_createItem(String arg, Client client)
	{
		Player player = getPlayer(client);
		Room room = getRoom(client);

		//Item item = new Clothing(arg, 0, "cloak",1.0);
		// use the object loading constructor for testing purposes

		int location = getPlayer(client).getLocation();
		int dbref = getNextDB();

		Item item = new Clothing(arg, "A new piece of clothing.", location, dbref, 0, ClothingType.SHIRT);

		if (dbref == main.size()) {
			main.add(item.toDB());
			main1.add(item);
		}
		else {
			main.set(item.getDBRef(), item.toDB());
			main1.set(item.getDBRef(), item);
		}

		int temp = 1;

		if (temp == 1) { // test to see if it fits in the inventory
			player.getInventory().add(item);
			send("Item named " + item.getName() + "(#" + item.getDBRef() + ") created. " + item.getName() + " has been placed in your inventory.", client);
		}
		else {
			room.contents1.add(item);
			send("Item named " + item.getName() + "(#" + item.getDBRef() + ") created. " + item.getName() + " has been placed in your location.", client);
		}
	}*/

			/**
			 * Command: @debug
			 * 
			 * Show requested debugging information, takes several arguments to show different
			 * information
			 * 
			 * on/off/cmdDelay/dbdump/timedata/seasons/holidays/chat/client/objlcache/position/roomlcache/udbnstack
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_debug(final String arg, final Client client) {
				String[] args = arg.split(":");
				String[] args2 = arg.split(" ");

				if ( arg.toLowerCase().equals("on") ) {
					debug = 1;
					send("Game> Debugging: On", client);
				}
				else if ( arg.toLowerCase().equals("off") ) {
					debug = 0;
					send("Game> Debugging: Off", client);
				}
				else if ( arg.toLowerCase().equals("clients") ) {
					int cn = 0;
					for (Client c : s.getClients()) {
						if (c != null) {
							if(c == client) {
								send(cn + " " + c.ip() + " " + c.toString() + "[YOU]", client);
							}
							else {
								send(cn + " " + c.ip() + " " + c.toString(), client);
							}
						}
						else {
							send(cn + " " + "---.---.---.--- null", client);
						}
						cn++;
					}
				}
				else if ( arg.toLowerCase().equals("creatures") ) {
					send("Creatures", client);
					send("--------------------------------------------------------------------------", client);
					for (final Creature c : objectDB.getCreatures()) {
						send(String.format("%s %s %s (#%s)", c.getDBRef(), c.getName(), getRoom(c.getLocation()).getName(), c.getLocation()), client);
					}
					send("--------------------------------------------------------------------------", client);
				}
				else if ( arg.toLowerCase().equals("dbdump") ) {
					/*
					 * List all of the names and dbrefs of the objects
					 * in the database their actual index in the database
					 */
					objectDB.dump(client, this);
				}
				else if ( arg.toLowerCase().equals("timedata") ) {
					// get current data
					Calendar rightNow = Calendar.getInstance();

					// Real World Time (or at least whatever time zone the server is in)
					String real_time = "" + rightNow.get(Calendar.HOUR);

					if (rightNow.get(Calendar.HOUR) < 10) { real_time = " " + real_time; }
					if (rightNow.get(Calendar.MINUTE) < 10) { real_time = real_time + ":0" + rightNow.get(Calendar.MINUTE); }
					else { real_time = real_time + ":" + rightNow.get(Calendar.MINUTE); }

					send("Real Time: " + real_time, client);

					// In-game Time
					String gameTime = "" + game_time.getHours();

					if (game_time.getHours() < 10) { gameTime = " " + gameTime; }
					if (game_time.getMinutes() < 10) { gameTime = gameTime + ":0" + game_time.getMinutes(); }
					else { gameTime = gameTime + ":" + game_time.getMinutes(); }

					send("Game Time: " + gameTime, client);

					// Time Scale (the relative number of seconds to an-game minute)
					send("Time Scale: 1 minute/" + (game_time.getScale() / 1000) + " seconds", client);
				}
				else if ( arg.toLowerCase().equals("seasons") ) {
					/*
					 * list all the seasons
					 */
					//return this.name + ": " + months[beginMonth - 1] + " to " + months[endMonth - 1];

					for (final Seasons s : Seasons.values()) {
						send(s + ": " + MONTH_NAMES[s.beginMonth - 1] + " to " + MONTH_NAMES[s.endMonth - 1], client);
					}
				}
				else if ( arg.toLowerCase().equals("holidays") ) {
					/* list the holidays */
					for (Map.Entry<String, Date> entry : holidays.entrySet()) {
						debug(entry.getKey() + ": " + MONTH_NAMES[((Date) entry.getValue()).getMonth() - 1] + " " + ((Date) entry.getValue()).getDay());
						send(entry.getKey() + ": " + MONTH_NAMES[((Date) entry.getValue()).getMonth() - 1] + " " + ((Date) entry.getValue()).getDay(), client);
					}
				}
				else if ( arg.toLowerCase().equals("client") ) {
					/* tell us about ourselves (i.e. which client object we are and our ip address) */
					send(client, client);
					send(client.ip(), client);
				}
				else if ( args2[0].toLowerCase().equals("listen") ) {
					final int dbref = Utils.toInt(args2[1], -1);
					final Room room = dbref != -1 ? getRoom(dbref) : getRoom(args2[1]);

					if (room != null) {
						StringBuffer listenList = new StringBuffer();

						for (final Player player : room.getListeners()) {
							listenList.append(player.getName() + ", ");
						}

						send("Listeners: " + listenList.toString(), client);
					}
				}
				else if ( arg.toLowerCase().equals("position") || arg.toLowerCase().equals("pos") ) {
					Player player = getPlayer(client);

					send("X: " + player.getXCoord(), client);
					send("Y: " + player.getYCoord(), client);
					send("Moving: " + player.isMoving(), client);
				}
				else if( arg.toLowerCase().equals("udbnstack") ) {
					send("Functionality Removed", client);
					/*client.write("Stack: [ ");
				for (int i = 0; i < unusedDBNs.size(); i++) {
					if ( i < unusedDBNs.size() - 1) {
						client.write(unusedDBNs.get(i));
					}
					else {
						client.write(unusedDBNs.get(i) + ", ");
					}
				}
				for (final Integer i : unusedDBNs) {
					client.write(i + ", ");
				}
				client.write(" ]\n");*/
				}
				else if (!arg.equals("")) {
					final int level = Utils.toInt(arg, debugLevel);
					if (debugLevel != level) {
						send("Game> Debug Level changed to: " + level, client);
					}
					debugLevel = level;
				}
				else {
					// print help information?
					cmd_help("@debug", client);
				}
			}

			/**
			 * Command: @dig
			 * 
			 * Create a room manually
			 * 
			 * NOTE: need to handle the roomName and roomParent string getting inside to make the
			 * command parameter input more uniform with the other commands [is this done?]
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_dig(final String arg, final Client client) {
				String name = "";
				int parent = 0;

				if (arg.indexOf("=") != -1)
				{
					name = arg.substring(0, arg.indexOf("="));
					name = Utils.trim(name);
					System.out.println("Room Name: " + name);
					parent = Integer.parseInt(arg.substring(arg.indexOf("=") + 1, arg.length()));
					System.out.println("Room Parent: " + parent);
					createRoom(name, parent);
				}
				else
				{
					send(gameError("@dig", 1), client); // Invalid Syntax Error
				}
			}

			/**
			 * Command: describe
			 * 
			 * Change the description of objects: rooms, exits, etc
			 * 
			 * NOTE: only handles player and room descriptions, not those of other objects, yet
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_describe(final String arg, final Client client)
			{
				String[] args = arg.split("=");
				String ref = args[0];
				String description = args[1];

				// get player, room objects to work with
				final Player player = getPlayer(client);
				Room room = getRoom(client);

				MUDObject m = null;

				// get object
				// if no argument or empty argument, fail with an error
				if (arg.equals("") || arg.equals(null))
				{
					send(gameError("@describe", 1), client); // Invalid Syntax Error
				}
				else {
					if (ref.toLowerCase().equals("here") || room.getName().equals(ref) || room.getDBRef() == Integer.parseInt(ref) ) {
						m = room;
					}
					else if (player.getName().equals(ref) || player.getDBRef() == Integer.parseInt(ref))
					{
						m = player;
					}
				}

				// attempt to change description
				if ( m != null) {
					if ( m.Edit_Ok ) {
						m.setDesc(description);
						send("Description Changed.", client);
						send("You changed the description of " + room.getName() + " to " + room.getDesc(), client);
					}
					else {
						send("Game> Object - Error: object not editable (!Edit_Ok)", client);
					}
				}
				else {
					send("No such object!", client);
				}
			}

			/**
			 * Command: drink
			 * 
			 * Drink some kind of liquid, probably a beverage or potion
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_drink(final String arg, final Client client) {
				if (!arg.equals("")) {
					final Player player = getPlayer(client);

					// create a new list to hold drinkable items
					ArrayList<Item> itemList = new ArrayList<Item>();

					// get drinkable items
					for (final Item item : player.getInventory()) {
						if (item.drinkable == 1) { // drinkable check
							itemList.add(item);
						}
					}

					debug(itemList);

					Item item = null;

					if ( player.getMode() == PlayerMode.COMBAT ) { // if in combat
						// try healing, etc potions first if just 'drink' is typed
						ArrayList<Item> healing = new ArrayList<Item>();

						for (final Item item1 : itemList) {
							/* need to check to see if something contains a healing effect
							 * 
							 * does it need to have solely a heal effect?
							 */
						}
					}
					else { // else
						// search by name for the item
						for (final Item item1 : itemList) {
							if ( item1.getName().equals(arg) || item1.getName().contains(arg) ) {
								item = item1;
								break;
							}
						}
					}

					/*
					 * you should type 'drink healing' instead of 'drink potion' if you want a healing potion,
					 * otherwise you might get a potion of invisibility or bull's strength
					 */

					if (item != null) {
						// determine what kind of drinkable item it is and apply an effects
						// or status changes accordingly
						if (item instanceof Potion) {
							Potion potion = (Potion) item;

							debug("Potion?: " + potion.toString());

							List<Effect> effects = potion.getEffects();

							debug(effects);

							/*for (Effect effect : effects) {
					debug("Effect: " + effect.getName());
					applyEffect(player, effect);
				}*/

							applyEffect(player, potion.getEffect());

							/*
							 * if the drinkable item is stackable too,
							 * then I need to be sure to use only one
							 */

							player.getInventory().remove(item);                          // remove from inventory
							objectDB.set(item.getDBRef(), new NullObject(item.getDBRef())); // remove from existence
						}
					}
				}
				else {
					send("Drink what?", client);
				}
			}

			/**
			 * Command: drop
			 * 
			 * Drop an object from your inventory onto the "floor"
			 * 
			 * NOTE: "finished" for now, converted to a Command object
			 * 
			 * CODE: extend to other surfaces, like tables?
			 * 
			 * COMMAND OBJECT EXISTS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_drop(final String arg, final Client client)
			{
				// get player, room objects to work with
				final Player player = getPlayer(client);
				Room room = getRoom(client);
				Item item;

				ArrayList<Item> inventory = player.getInventory();

				// get the integer value, if there is one, as the argument
				final int dbref = Utils.toInt(arg, -1);

				// get the object the argument refers to: by name (if it's in the calling player's inventory), or by dbref#
				// should be done by searching the player's inventory for the object and if there is such an object, drop it on the floor.
				for (int i = 0; i < player.getInventory().size(); i++)
				{			
					item = inventory.get(i);

					// if there is a name or dbref match from the argument in the inventory
					if ( item.getName().equals(arg) || item.getName().contains(arg) || item.getDBRef() == dbref )
					{
						debug(item.getName() + " true");
						// move object from player inventory to floor
						item.setLocation(room.getDBRef());
						room.contents1.add(item);
						player.getInventory().remove(item);
						// check for silent flag to see if object's dbref name should be shown as well?
						// return message telling the player that they dropped the object
						send("You dropped " + colors(item.getName(), "yellow") + " on the floor.", client);
						// return message telling others that the player dropped the item?
						break;
					}
				}
			}

			/**
			 * Command: effects
			 * 
			 * List effects set on player currently affecting a player
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_effects(final String arg, final Client client)
			{
				client.writeln( getPlayer(client).listEffects() );
			}

			/**
			 * Command: equip
			 * 
			 * Equip an item to a player, likely in the first
			 * slot that is both available and compatible
			 * unless one was specified.
			 * 
			 * NOTE: should I have a hold and wield command instead or should they
			 * tie into this?
			 * 'equip bastard sword' or
			 * 'equip rhand bastard sword' or
			 * 'wield bastard sword'
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_equip(final String arg, final Client client)
			{
				final Player player = getPlayer(client);
				final int i = Utils.toInt(arg, -1);

				/*if (arg.equals("") && i == -1) { send("Equip what?"); }
		else {
			for (int e = 0; e < player.getInventory().size(); e++) {
				Item item = player.getInventory().get(e);
				if (arg.equals(item.getName()) || i == item.getDBRef()) {
					if (item.equippable) { player.equip(item); }
					else { send("You can't equip that. (Not Equippable)"); }
					return;
				}
			}
		}*/

				Item item = null;

				if (arg.equals("") && i == -1) {
					send("Equip what?", client);
				}
				else {
					for (int e = 0; e < player.getInventory().size(); e++) {
						item = player.getInventory().get(e);

						if (arg.equals(item.getName())  || i == item.getDBRef()) {
							break;
						}
					}
				}

				if ( item != null) {
					if ( item.equippable ) {
						// equips the item in the first available slot
						for (final String s : player.getSlots().keySet()) {
							debug(s);

							Slot slot = player.getSlots().get(s);

							if ( slot.isType(item.equip_type) ) {
								if ( !slot.isFull() ) {
									if (item instanceof Equippable<?>) {
										/*
										 * handle any OnEquip effects/events
										 */

										slot.insert(item);                  // put item in the slot
										player.getInventory().remove(item); // remove it from the inventory
										item.equipped = true;               // set item's equipped "flag" to true (equipped)
										item = null;                        // set item reference to null

										send(slot.getItem().getName() + " equipped (" + slot.getItem().equip_type + ")", client);

										break; // break the for loop, so we don't try to insert a now null object somewhere else
									}
								}
								else {
									// are these alternative messages?
									send("You can't equip that. (Equip Slot Full)", client);
									send("Where are you going to put that? It's not like you have a spare...", client);
								}
							}
							else {
								send("You can't equip that. (Equip Type Incorrect)", client); //only useful if I force specifics of equipment?
								debug("Equip Type " + item.equip_type + " does not match " + slot.getType());
							}
						}
					}
					else {
						send("You can't equip that. (Not Equippable)", client);
						return;
					}
				}
			}

			/**
			 * Command: examine
			 * 
			 * Examine any type of in-game object
			 * 
			 * COMMAND OBJECT EXISTS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_examine(final String arg, final Client client)
			{	
				if ( arg.equals("") || arg.equals("here") ) {
					Room room = getRoom(client);
					examine(room, client);
				}
				else if (arg.equals("me")) {
					Player player = getPlayer(client);
					examine(player, client);
				}
				else {
					final int dbref = Utils.toInt(arg, -1);

					if (dbref != -1) {

						final MUDObject mobj = getObject(dbref);

						if (mobj != null) {
							if (mobj instanceof Player) {
								Player player = (Player) mobj;
								examine(player, client);
							}

							else if (mobj instanceof Room) {
								Room room = (Room) mobj;
								examine(room, client);
							}

							else if (mobj instanceof Exit) {
								Exit exit = (Exit) mobj;
								examine(exit, client);
							}

							else if (mobj instanceof Thing) {
								Thing thing = (Thing) mobj;
								examine(thing, client);
							}


							else if (mobj instanceof Item) {
								Item item = (Item) mobj;
								examine(item, client);
							}

							else {
								examine(mobj, client);
							}
						}
					}
					else {
						final Room room = getRoom(arg);

						if (room != null) {
							examine(room, client);
							return;
						}

						final Player player = getPlayer(arg);

						if (player != null) {
							examine(player, client);
							return;
						}
					}
				}
			}

			/**
			 * Command: exchange
			 * 
			 * Exchange currency you have for another denomination
			 * 
			 * Should get exchange rates, and and calculate the appropriate
			 * amount of the other currency to give you.
			 * 
			 * NOTE: You may specify how much of X denom. to change
			 * into Y denom, or just let it convert all of your gold
			 * to platinum, etc
			 * 
			 * CODING: consider expanding this to include money from different
			 * currency system
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_exchange(final String arg, final Client client) {

				String currency;
				String currency1;

				int amt = 0;
				int amt1 = 0;

				final Player player = getPlayer(client);

				List<String> args = Arrays.asList(arg.split(" "));

				int mid = args.indexOf("for");
				if (args.size() >= 3)
					// exchange | 4 for platinum
					// exchange | gold for 4
					// exchange | gold for platinum
					// exchange | 4 gold for platinum
					// exchange | gold for 4 platinum

					if (mid != -1 && mid > 0) {
						try {
							amt = Integer.parseInt(args.get(0));
							currency = args.get(mid - 1);
						}
						catch (NumberFormatException nfe) {
							debug("Exception(EXCHANGE): " + nfe.getMessage());
							amt = 1;
							try {
								currency = args.get(0);
							}
							catch(Exception e) {
								debug("Exception(EXCHANGE): " + nfe.getMessage());
								currency = "copper"; // default, and lowest currency
							}
						}
						try {
							amt1 = Integer.parseInt(args.get(mid + 1)); // see if it's a number
							currency1 = args.get(mid + 2);
						}
						catch (NumberFormatException nfe) {
							debug("Exception(EXCHANGE): " + nfe.getMessage());
							amt1 = 1;
							try {
								currency1 = args.get(mid + 1); // if not
							}
							catch(Exception e) {
								debug("Exception(EXCHANGE): " + e.getMessage());
								currency1 = "copper";
							}
						}
						send("Amount: " + amt, client);
						send("Currency: " + currency, client);
						send("Amount 1: " + amt1, client);
						send("Currency 1: " + currency1, client);
						// copper silver gold platinum
					}
			}

			/**
			 * Command: @flag
			 * 
			 * Sets or removes flags from an object
			 * 
			 * NOTE: should exclude type flags which probably ought to be immutable
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_flag(final String arg, final Client client) {
				final String[] args = arg.split("=");

				final Player player = getPlayer(client);
				final Room room = getRoom(client);

				if (args.length > 1) {
					if (args[1].contains("!")) {
						send("Removing sFlag(s)", client);
						if (args[0].equals("me")) {
							player.removeFlags(ObjectFlag.getFlagsFromString(args[1]));
						}
						else if (args[0].equals("here")) {
							room.removeFlags(ObjectFlag.getFlagsFromString(args[1]));
							send(room.getName() + " flagged " + ObjectFlag.fromLetter(args[1].charAt(0)), client);
						}
						else {
						}
					}
					else {
						send("Adding Flag(s)", client);
						if (args[0].equals("me")) {
							player.setFlags(ObjectFlag.getFlagsFromString(args[1]));
							send(player.getName() + " flagged " + ObjectFlag.fromLetter(args[1].charAt(0)), client);
						}
						else if (args[0].equals("here")) {
							room.setFlags(ObjectFlag.getFlagsFromString(args[1]));
							send(room.getName() + " flagged " + ObjectFlag.fromLetter(args[1].charAt(0)), client);
						}
						else {
						}
					}
				}
			}

			/**
			 * Command: flags
			 * 
			 * Shows the flags set on a particular object.
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_flags(final String arg, final Client client) {
				final MUDObject m = getObject(arg);
				client.write("Flags: ");
				client.write(ObjectFlag.toInitString(m.getFlags()));
			}

			/**
			 * Command: find
			 * 
			 * Find objects with the name given
			 * 
			 * NOTE: maybe I make do types as well or reg expressions
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_find(final String arg, final Client client) {
				final LinkedList<String> matches = new LinkedList<>();

				for (final MUDObject m : objectDB.findByLower(arg)) {
					// if ( m.getName().toLowerCase().contains( arg.toLowerCase() ) ) {
					matches.add(m.getName() + " (#" + m.getDBRef() + ")");
					// }
				}

				for (final String s : matches) {
					send(s, client);
				}
				send("**********", client);
				send(matches.size() + " objects found.", client);
			}

			/**
			 * Command: go
			 * 
			 * Move towards an object or a specific point in the cartesian plane
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_go(final String arg, final Client client) {

				String[] args = arg.split(" ");

				Player player = getPlayer(client);

				if (args.length == 1) {
					MUDObject m = getObject(arg);

					if (m != null) {
						player.setMoving(true);
						player.setDestination(new Point(m.coord.getX(), m.coord.getY()));

						moving.add(player);
					}
				}
				else if (args.length >= 2) {
					int x = Integer.parseInt(args[0]);
					int y = Integer.parseInt(args[1]);

					player.setMoving(true);
					player.setDestination(new Point(x, y));

					moving.add(player);
				}

			}

			/**
			 * Command: exits
			 * 
			 * lists exits (useful if you don't like having to look at the room again every time)
			 * 
			 * NOTE: shows non-DARK exits
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_exits(final String arg, final Client client) {
				final String exitNames = getRoom(client).getVisibleExitNames();
				if (exitNames != null && !exitNames.equals("")) {
					send(colors("Exits: " + exitNames, displayColors.get("exit")), client);
				}
				else {
					send(colors("Exits:", displayColors.get("exit")), client);
				}
			}

			/**
			 * Command: greet
			 * 
			 * Greet another player (this tells them your name with some specifity).
			 * 
			 * COMMAND OBJECT EXISTS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_greet(final String arg, final Client client) {
				debug(arg);
				final Player current = getPlayer(client);
				debug("current: " + current.getName());
				final Player player1 = getPlayer(arg);
				final Client client1 = player1.getClient();
				debug("player1: " + player1.getName());
				if (!player1.getNames().contains(current.getName())) {
					player1.addName(current.getName());
					if (current.getNames().contains(player1.getName())) {
						send("You tell " + player1.getName() + " that your name is " + current.getName(), client);
					}
					else {
						send("You tell " + arg + " that your name is " + current.getName(), client);
					}
					send(current.getCName() + " tells you that their name is " + current.getName(), client1);
				}
				else {
					send("You've already greeted that player", client);
				}
			}

			/**
			 * Command: help
			 * 
			 * Provide access to help files about the use of other commands
			 * 
			 * NOTE: COMMAND OBJECT EXISTS
			 * 
			 * @param arg    the name of the help file to access
			 * @param client the client
			 */
			private void cmd_help(String arg, final Client client)
			{
				/*
				 * really should add a topics system and multi-page help files (need a "pager");
				 * it'd be awesome to have some kind of virtual page up/page down functionality
				 * - maybe that would be workable if I had a real terminal emulator on the other
				 * end. if i code this feature, I could enable it if I could identify a full
				 * terminal emulation on the other end (telnet negotiation? or maybe just asking via
				 * the game for a response)
				 */
				if (arg.equals(""))
				{
					arg = "help";
				}

				final String[] helpLines = helpMap.get(arg);
				if (helpLines != null)
				{ 
					for (final String line : helpLines)
					{
						send(line, client);
					}
				}
				else if (arg.equals("@reload"))
				{
					help_reload();
					send("Game> Help Files Reloaded!", client);
				}
				else
				{
					send("No such help file!", client);
				}
			}

			/**
			 * Command: housing
			 * 
			 * Shows housing information and availability for the current area/region/whatnot
			 * 
			 * NOTE: dummy test information at the moment
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_housing(final String arg, final Client client) {
				send(colors("Player Housing - Details", "cyan"), client);
				send(colors("=========================================================", "cyan"), client);
				send("Waterdeep - Castle Ward (South)", client);
				send("   o " + colors("Winding Way Apartments [ 0 / 10 ]", "green"), client);
				send("   o " + colors("Sea Villas [ 10 / 10 ]", "red"), client);
				send("   o " + colors("Cymbril's Walk [ 7 / 10 ]", "yellow"), client);
				send(colors("=========================================================", "cyan"), client);
			}

			/**
			 * Command: install
			 * 
			 * Install an area file
			 * 
			 * @param arg    filename of an area file
			 * @param client the client
			 */
			private void cmd_install(final String arg, final Client client) {
				if (!arg.equals("")) {
					// search a default working directory (say a user's home or downloads
					// and load the area into the server's files somewhere
				}
			}

			/**
			 * Command: inventory
			 * 
			 * check player inventory
			 * 
			 * @param arg    unused
			 * @param client the client
			 */
			@SuppressWarnings("unchecked")
			private void cmd_inventory(final String arg, final Client client)
			{
				final Player player = getPlayer(client);

				if (player != null) // if the player exists
				{
					debug(player.getInventory()); 
					send(player.getName() + "'s Inventory:", client);

					if (player.getInvType() == 'S') { // simple inventory display
						for (final Item item : player.getInventory())
						{
							if (item != null) {
								//send(colors(item.getName(), "yellow") + "(#" + item.getDBRef() + ")", client);
								send(colors(item.toString(), "yellow") + "(#" + item.getDBRef() + ")", client);

							}
							else {
								debug("Item is null");
							}
						}
					}
					else if (player.getInvType() == 'C') { // complex inventory display
						// WORK: need to redo this, and not use a for loop this way, the
						// whole inventory should be shown in the way the container is
						send("/" + Utils.padRight("", '-', 70) + "\\", client);
						//send("|" + Utils.padRight(colors("Pack", "yellow")) + "|", client);

						String padded1 = Utils.padRight("Pack");
						StringBuffer sb1 = new StringBuffer(padded1);

						sb1.insert(0, colorCode("yellow"));
						//sb1.insert(sb1.indexOf("k") + 1, colorCode("white"));
						sb1.append(colorCode("white"));

						send("|" + sb1.toString() + "|", client);
						send("|" + Utils.padRight("", '-', 70) + "|", client);
						for (final Item item : player.getInventory())
						{
							if (item != null) {
								if (item instanceof Container<?>) {
									displayI((Container<Item>) item, client);
								}
								else {
									//String itemString = colors(item.getName(), "yellow") + "(#" + item.getDBRef() + ")";
									//String itemString = item.getName() + "(#" + item.getDBRef() + ")";
									String itemString = item.toString() + "(#" + item.getDBRef() + ")";
									String padded = Utils.padRight(itemString);

									StringBuffer sb = new StringBuffer(padded);

									sb.insert(0, colorCode("yellow"));
									sb.insert(sb.indexOf("("), colorCode("white"));

									//send("|" + Utils.padRight(itemString) + "|", client);
									send("|" + sb.toString() + "|", client);
								}
							}
						}
						send("\\" + Utils.padRight("", '-', 70) + "/", client);
					}

					send("Weight: " + calculateWeight(player) + "/" + player.getCapacity() + " lbs.", client);
					send("You have " + player.getMoney().toString() + ".", client);
				}
			}

			/**
			 * Command: jump
			 * 
			 * NOTE: non-user command, probably has a prefix
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_jump(final String arg, final Client client)
			{
				final Player player = getPlayer(client);

				getRoom(client).removeListener(player); // remove listener

				final int dbref = Utils.toInt(arg, -1);
				boolean success = false;

				// try to find the room, by dbref or by name
				Room room = (dbref != -1) ? getRoom(dbref) : getRoom(arg);

				if (room != null) {
					success = true;
				}

				// if we found the room, send the player there
				if ( success ) {
					send("Jumping to " + room.getName() + "... ", client);
					player.setLocation(room.getDBRef());
					player.setCoordinates(0, 0);
					send("Done.", client);
					room = getRoom(client);
					look(room, client);

					room.addListener(player); // add listener
				}
				else {
					send("Jump failed.", client);
				}
			}

			/**
			 * function to load database, technically only use is to reload the database
			 * while the game is running, perhaps I don't really need this?
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_loadDB(final String arg, final Client client)
			{	
				// tell us that the database is being loaded (supply custom message?)
				send("Game> Loading Database!", client);

				// load objects from databases

				// load databases from disk

				// write out databases to objects

				// tell us that loading is done (supply custom message?)
				send("Game> Done.", client);
			}

			// lock command (applies to lockable things)
			private void cmd_lock(final String arg, final Client client) {
				MUDObject m = getObject(arg);
				if (m instanceof Lockable) {
					Lockable l = (Lockable) m;

					if (!l.isLocked()) {
						l.lock();
						send(m.getName() + " locked.", client);
					}
				}
			}

			// look function
			private void cmd_look(final String arg, final Client client)
			{
				// get player, room objects to work with
				final Player player = getPlayer(client);
				final Room room = getRoom(client);

				debug("Look Command");
				final String[] args = arg.split(" ");
				for (final String s : args) {
					debug(s);
				}
				if ( arg.equals("") ) {
				}
				else {
					debug("Argument (String): " + arg);
				}

				// if no argument or empty argument, show the room
				if (arg.equals("") || arg.toLowerCase().equals("here"))
				{
					look(room, client);
				}
				else if ( arg.toLowerCase().equals("me")) {
					look(player, client);
				}
				else {
					// decide what else is visible and then find the best match in there
					//findVisibleObjects(room);
				}

				if (!arg.equals("")) {

					// get properties (I think we should have /visuals "folder" for visual properties
					// i.e. 'ceiling', 'floor', 'wall(s)'
					Object o = room.getProps().get(arg);

					if (o != null) {
						if (o instanceof String) {
							String result = (String) o;
							//send("You look at the " + arg, client);
							//send(result, client);
							send("You look at the " + arg + ": " + result , client);
						}
					}
					else {
						send("You look around, but don't see that.", client);
					}

					int spec = 0;

					if ( arg.contains(".") ) {
						spec = Integer.parseInt( arg.substring( arg.indexOf('.') ) );
						debug("Specifier: " + spec);
					}

					final int dbref = Utils.toInt(arg, -1);
					MUDObject m = null;

					if (dbref != -1) {
						try {
							m = getObject(dbref);

							debug("MUDObject : " + m.getDBRef() + " " + m.getName());

							if (m instanceof Player) {
								look((Player) m, client);
							}
							else if (m instanceof Room) {
								look((Room) m, client);
							}
							else {
								look(m, client);
							}
						}
						catch (NullPointerException npe) {
							npe.printStackTrace();
						}
					}
					else {
						try {
							if (spec == 0) {
								m = getObject(arg);
							}
							//else { MUDObject[] mObjs = getObjects(arg); }

							debug("MUDObject : " + m.getDBRef() + " " + m.getName());

							look(m, client);
						}
						catch (NullPointerException npe) {
							npe.printStackTrace();
						}
					}
				}
			}

			/**
			 * Command: lsedit
			 * 
			 * Launch List Editor
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_lsedit(final String arg, final Client client)
			{
				final Player player = getPlayer(client);

				player.setStatus("EDT");
				player.setEditor(Editor.LIST);

				if (!player.hasEditor(arg)) {// if the list doesn't exist, clear out the variables for a new one
					player.startEditing(arg);
				}
				else {// if the list does exist, load it into the list data variables
					player.loadEditList(arg);
				}

				send("List Editor v0.0b\n", client);

				final EditList list = player.getEditList();
				String header = "< List: " + list.name + " Line: " + list.getCurrentLine() + " Lines: " + list.getNumLines() + " >";

				send(header, client);
			}

			/**
			 * Command: hedit
			 * 
			 * Launch Help File Editor
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_helpedit(final String arg, final Client client) {
				final Player player = getPlayer(client);

				player.setStatus("EDT");       // set the 'edit' status flag
				player.setEditor(Editor.HELP);

				boolean exist = false;

				// test for existence of helpfile?
				if (helpMap.get(arg) != null) {
					exist = true;
				}

				if (!exist) { // if it doesn't exist, create a new one

					send("Game> (help editor) Error: Invalid Help File!", client);
					send("Game> (help editor) Creating new help file...", client);

					player.startEditing(arg);
					final EditList list = player.getEditList();

					// need to generate header of help file without including it in editable space
					// header: @command // shows the naming, so it's easy to index
					//         @COMMAND // printed out as the name of the command when viewing help
					list.addLine(arg);               // add name of command, lowercase (header)
					list.addLine(arg.toUpperCase()); // add name of command, uppercase (header)

					send("Game> (help editor) Helpfile created.", client);
				}
				else { // if it does, load it
					System.out.println(HELP_DIR + arg + ".txt");

					// probably ought to somehow prevent editing of the header
					// load the help file with an offset so I can avoid borking
					// the two header data lines ?

					player.loadEditList(arg, loadList(HELP_DIR + arg + ".txt"));
					final EditList list = player.getEditList();

					// if loading fails, create it
					if (list.getNumLines() == 0) {
						send("Game> (help editor) Error: Invalid Help File!", client);
						send("Game> (help editor) Creating new help file...", client);

						player.startEditing(arg);

						// need to generate header of help file without including it in editable space
						// header: @command // shows the naming, so it's easy to index
						//         @COMMAND // printed out as the name of the command when viewing help
						final EditList newlist = player.getEditList();
						newlist.addLine(arg);               // add name of command, lowercase (header)
						newlist.addLine(arg.toUpperCase()); // add name of command, uppercase (header)

						send("Game> (help editor) Helpfile created.", client);
					}
				}

				send("Help Editor v0.0b\n", client);

				final EditList list = player.getEditList();
				String header = "< List: " + list.name + " Line: " + list.getLineNum() + " Lines: " + list.getNumLines() + "  >";
				send(header, client);
			}

			/**
			 * Command: listprops
			 * 
			 * List properties
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_listprops(final String arg, final Client client) {

				Player player = getPlayer(client);
				Room room = getRoom(client);

				LinkedHashMap<String, Object> props;
				send("ARG: " + arg, client);
				if (arg.toLowerCase().equals("here")) {
					props = room.getProps();
					send("" + Colors.GREEN + room.getName() + " (#" + room.getDBRef() + ")" + Colors.WHITE, client);
				}
				else {
					player = getPlayer(client);
					props = player.getProps();
					send("" + Colors.GREEN + player.getName() + " (#" + player.getDBRef() + ")" + Colors.WHITE, client);
				}
				for (final Object k : props.keySet()) {
					send((String) k + " " + (String) props.get(k), client);
				}
			}

			/**
			 * Command: mail
			 * 
			 * COMMAND OBJECT EXISTS
			 * 
			 * Function to read player OOC Mail
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_mail(final String arg, final Client client) {

				final Player player = getPlayer(client);

				if (arg.equals("")) { // if no arguments
					client.write("Checking for unread messages...\n");

					int messages = player.getMailBox().numUnreadMessages();

					if (messages == 0) {
						client.write("You have no unread messages.\n");
					}
					else {
						client.write("You have " + String.valueOf(messages) + " unread messages.\n");
					}
				}
				else {
					final int msg = Utils.toInt(arg, -1);

					if (msg > -1 && msg < player.getMailBox().numUnreadMessages()) {
						final Mail mail = player.getMailBox().get(msg);

						send("Message #: " + msg, client);
						for (final String s : mail.getLines()) {
							send(s, client);
						}

						if (mail.isUnread()) {
							mail.markRead();
							send("< mail marked as read >", client);
						}
					}
					else {
						send("No such existing message!", client);
					}
				}
			}

			/**
			 * Command: map
			 * 
			 * Display/Render a map on the screen for the player
			 * 
			 * testing, ideally I would either have stored room maps, stored maps/players of rooms or just generate
			 * the map on the fly
			 * should I store maps inside the code? (don't like that idea much) or
			 * per area in wherever I keep the data files?
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_map(final String arg, final Client client) {
				debug(MAP_DIR + "map.txt");
				String mapFile = MAP_DIR + "map.txt";
				String[] test1 = new String[1];

				try {
					test1 = Utils.loadStrings(mapFile);
					if (!(test1 instanceof String[])) {
						throw new FileNotFoundException("Invalid File!");
					}
				}
				catch(FileNotFoundException fnfe) {
					fnfe.printStackTrace();
				}

				client.write("Legend: ");
				client.write(colors("B", "green") + " - Bank ");
				client.write(colors("H", "red") + " - House ");
				client.write(colors("I", "magenta") + " - Inn ");
				client.write(colors("S", "yellow") + " - Shop ");
				client.write('\n');

				for (final String str : test1) {
					for (int i = 0; i < str.length(); i++) {
						switch(str.charAt(i)) {
						case '#':
							client.write("" + Colors.WHITE); // set foreground white
							//client.write("\33[37m");
							client.write(' ');               // draw symbol
							break;
						case '*':
							client.write("\33[47m"); // set background white
							client.write(' ');       // draw symbol
							client.write("\33[40m"); // reset background (black)
							break;
						case 'B': // Bank
							client.write("\33[32m"); // set foreground green
							client.write('B');       // draw symbol
							break;
						case 'H': // House
							client.write("\33[31m"); // set foreground red
							client.write('H');       // draw symbol
							break;
						case 'I': // Inn
							client.write("\33[35m"); // set foreground green
							client.write('I');       // draw symbol
							break;
						case 'S': // Shop
							// set foreground yellow
							client.write("" + Colors.YELLOW);
							//client.write("\33[33m");
							client.write('S');
							break;
						default:
							break;
						}
						// reset foreground (set to white)
						client.write("" + Colors.WHITE);
					}
					// reset background (black)
					client.write("\33[40m\n");
				}
			}

			/**
			 * Command: move
			 * 
			 * Move in a direction
			 * 
			 * NOTE: command is defunct and unused
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_move(final String _arg, final Client client)
			{
				Player player = getPlayer(client);

				final String arg = _arg.toLowerCase();

				if (arg.equals("north"))
				{
					if (player.getLocation() - 10 >= 0)
					{
						System.out.println("success");
						player.setLocation(player.getLocation() - 10);
					}
				}
				else if (arg.equals("south"))
				{
					player.setLocation(player.getLocation() + 10);
					System.out.println("success");
				}
				else if (arg.equals("east"))
				{
					player.setLocation(player.getLocation() + 1);
					System.out.println("success");
				}
				else if (arg.equals("west"))
				{
					if (player.getLocation() - 1 >= 0)
					{
						player.setLocation(player.getLocation() - 1);
						System.out.println("success");
					}
				}
				else
				{
					send("Invalid Movement!", client);
					return;
				}

				look(getRoom(client), client);

				send(player.getName() + " Location: " + player.getLocation() + "\n", client);
			}

			/**
			 * Command: name
			 * 
			 * name an object
			 * 
			 * FOR BUILDERS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_name(final String arg, final Client client) {
				final String[] args = arg.split("=");

				if (args.length == 2) {
					MUDObject m = getObject(args[0]);

					m.setName(args[1]);

					send("Game> Changed name of #" + m.getDBRef() + " to " + m.getName(), client);
				}
			}

			/**
			 * Command: nameref
			 * 
			 * Store a personal string to be used to refer to a number, useful for people
			 * who have a hard time remembering database reference numbers, but not names.
			 * 
			 * NOTE: input of entries is manual and so could become out of date
			 * 
			 * FOR BUILDERS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_nameref(final String arg, final Client client) {
				final Player player = getPlayer(client);

				String[] args = arg.split(" ");

				if (args.length == 2) {
					try {
						player.setNameRef(args[0], Integer.parseInt(args[1]));
						send("nameRef allocated.", client);
						send(args[0].substring(0, args[0].length()) + " allocated to " + args[1], client);
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
				else if (arg.toLowerCase().equals("#list")) {
					send("Name Reference Table", client);
					send("------------------------------------------------", client);
					for (String str : player.getNameReferences()) {
						send(str + " -> " + player.getNameRef(str), client);
					}
					send("------------------------------------------------", client);
				}
				else if (arg.toLowerCase().equals("#clear")) {
					player.clearNameRefs();
					send("Name Reference Table cleared!", client);
				}
			}

			/**
			 * Command to launch object editor (oedit)
			 * Permission: Builder
			 * 
			 * NOTE: concept borrowed from ROM, a derivative of Merc,
			 * a derivative of DIKU.
			 * 
			 * Basically you call the object editor like this:
			 * 'cmd_objectedit(<object #/object name>, client)'
			 * 
			 * And it attempts to find the object and edit it,
			 * if it can't find it, it will indicate a failure and
			 * open the editor with no object.
			 * 
			 * FOR BUILDERS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_objectedit(final String arg, final Client client) {
			}

			/**
			 * Command: open
			 * 
			 * Open (create) an exit from here to another location
			 * 
			 * This command creates one way exits
			 * 
			 * FOR BUILDERS
			 * 
			 * ex. '@open name=destination'
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_open(final String arg, final Client client)
			{
				final String[] args = arg.split("=");

				String name = args[0];
				int source = 0, destination = 0;

				Room room = getRoom(client);

				try {
					if (args.length == 2) { // simple form - name=destination
						source = room.getDBRef();
						// destination defaults to an invalid room dbref
						if (args[1].equals("")) { destination = -1; }
						else { destination = Integer.parseInt(args[1]); }
					}
					else {
						send( "open : " + gameError("@open", 1), client);
						return;
					}
				}
				catch(NumberFormatException nfe) {
					send( "@open : source or destination dbref invalid, exit creation failed", client );
					return;
				}

				// get the source room
				room = getRoom(source);

				// create the exit
				Exit exit = new Exit(name, source, destination);

				objectDB.addAsNew(exit);
				objectDB.addExit(exit);

				// add the exit to the source room
				room.getExits().add(exit);

				// tell us that we succeeded in creating the exit
				send("You open an exit called " + exit.getName() + "(#" + exit.getDBRef() + ")" + " from #" + exit.getLocation() + " to #" + exit.getDest() + ".", client);
			}

			/**
			 * Command: door
			 * 
			 * Open (create) an exit from one location to another location
			 * 
			 * ex. '@door name=source=destination'
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_door(final String arg, final Client client) {
				final String[] args = arg.split("=");

				final String name = args[0];
				int source = 0, destination = 0;

				Room room = getRoom(client);

				try {
					if (args.length == 3) { // long form - name=source=destination
						// source defaults to the current room
						if (args[1].equals("")) {
							source = room.getDBRef();
						}
						else {
							source = Integer.parseInt(args[1]);
						}
						// destination defaults to an invalid room dbref
						if (args[2].equals("")) {
							destination = -1;
						}
						else {
							destination = Integer.parseInt(args[2]);
						}
					}
					else {
						send( "@door : " + gameError("@open", 1), client);
						return;
					}
				}
				catch(NumberFormatException nfe) {
					send( "open : source or destination dbref invalid, exit creation failed", client );
					return;
				}

				// get the source room
				room = getRoom(source);

				// create the exit
				Exit exit = new Exit(name, source, destination);

				objectDB.addAsNew(exit);
				objectDB.addExit(exit);

				// add the exit to the source room
				room.getExits().add(exit);

				// tell us that we succeeded in creating the exit
				send("You open an exit called " + exit.getName() + "(#" + exit.getDBRef() + ")" + " from #" + exit.getLocation() + " to #" + exit.getDest() + ".", client);
			}

			/**
			 * Command: osuccess
			 * 
			 * Sets a message that tells other players about the successful action another player did
			 * 
			 * FOR BUILDERS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_osuccess(final String arg, final Client client) {
				final String[] args = arg.split("=");
				final Exit exit = (Exit) getExit(args[0]);
				if (args.length > 1) {
					exit.setMessage("osuccMsg", args[1]);
					send(exit.getName() + "'s osuccess message set to: " + args[1], client);
				}
			}

			private void cmd_fail(final String arg, final Client client) {
				final String[] args = arg.split("=");
				final Exit exit = (Exit) getExit(args[0]);
				if (args.length > 1) {
					exit.setMessage("failMsg", args[1]);
					send(exit.getName() + "'s fail message set to: " + args[1], client);
				}
			}

			/**
			 * Command: ofail
			 * 
			 * Sets a message that tells other players about a player's failure to complete an action
			 * 
			 * FOR BUILDERS
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_ofail(final String arg, final Client client) {
				final String[] args = arg.split("=");
				final Exit exit = (Exit) getExit(args[0]);
				if (args.length > 1) {
					exit.setMessage("ofailMsg", args[1]);
					send(exit.getName() + "'s ofail message set to: " + args[1], client);
				}
			}

			// Function to send player messages
			private void cmd_page(final String arg, final Client client)
			{
				// ARG: <recipients>=<message>/nathan,admin=test message
				String[] in = arg.split("=");

				if (in.length > 1) {
					final String[] recipients = in[0].split(",");
					String ms = "";

					if (in.length == 2) {
						ms = in[1];

						Message msg = new Message("You page, " + "\"" + Utils.trim(ms) + "\" to " + in[0] + ".", getPlayer(client));
						addMessage(msg);

						for (final String recipName : recipients)
						{
							final Player targetPlayer = getPlayer(recipName);
							final Client recipClient = targetPlayer.getClient();
							if (recipClient != null) {
								// mesage with a player sender, text to send, and the player to send it to
								s.sendMessage(recipClient, new Message(getPlayer(client), Utils.trim(ms), targetPlayer));
							}
						}
					}
				}
			}

			/**
			 * Command to change passwords
			 * 
			 * NOTE: right now this is an admin command, which is problematic since
			 * I want anyone to be able to change their password, but not those
			 * for other players, unless they have admin.
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_passwd(final String arg, final Client client)
			{
				// @passwd test          change my password to test       (user)
				// @passwd @reset        reset my password                (user)
				// @passwd Nathan=test   change Nathan's password to test (wizard)
				// @passwd @reset Nathan reset Nathan's password          (wizard)
				String[] tmp = arg.split("=");  // split the arguments

				Player player = getPlayer(client);    // get the current player

				if ( arg.equals("@reset") ) {
					send("Reset Password Code", client);
					send("Game> (@passwd @reset) functionality incomplete", client);
				}
				else if (tmp.length > 1) { // if there is more than one argument

					if (player.getAccess() >= WIZARD) {
						Player player1 = getPlayer(tmp[0]); // get the player whose name was give
						if (player1 instanceof Player) {
							player1.setPass(tmp[1]);
							send(player1.getName() + "'s password has been changed to: '" + tmp[1] + "' hash: '" + player1.getPass() + "'", client);
						}
						else {
							send("Game> Invalid Player.", client);
						}
					}
					else {
						send("Game> Insufficient Permissions.", client);
					}

				}
				else { // if there is only one argument (i.e. new password for current player)
					player.setPass(arg);
					send("Your password has been changed to: '" +  tmp[0] + "' hash: " + player.getPass(), client);
				}
			}

			private void cmd_pinfo(final String arg, final Client client) {
				final Player player = getPlayer(client);

				send("------------------------------[ Sheet ]------------------------------", client);
				send("Character Name: " + Utils.padRight(player.getName(), 16) + " Player Name: " + Utils.padRight("", 8), client);
				send("Race: " + player.getPlayerRace().getName(), client);
				send("Class: " + player.getPClass().getName(), client);
				send("Level: " + player.getLevel(), client);
				if ( player.isLevelUp() ) {
					/*client.write("" + Colors.GREEN);
			client.write("Ready to Level-Up!");
			client.write("" + Colors.WHITE);
			client.write('\n');*/
					send(colors("Ready to Level-Up!", "green"), client);
				}
				send("XP: " + Utils.padRight("" + player.getXP(), 7) + " XP to next Level: " + Utils.padRight("" + (player.getXPToLevel() - player.getXP()), 7), client);
				send("Strength: " + player.getStats().get(Abilities.STRENGTH), client);
				send("Dexterity: " + player.getStats().get(Abilities.DEXTERITY), client);
				send("Constitution: " + player.getStats().get(Abilities.CONSTITUTION), client);
				send("Intelligence: " + player.getStats().get(Abilities.INTELLIGENCE), client);
				send("Charisma: " + player.getStats().get(Abilities.CHARISMA), client);
				send("Wisdom: " + player.getStats().get(Abilities.WISDOM), client);
				int si = 0;
				send("------------------------------[ Skills ]------------------------------", client);
				for (final Object o : player.getSkills().keySet()) {
					Integer value = player.getSkills().get(o);
					String color = "";
					if (value == -1) {
						color = "red";
					}
					else if (value == 0) {
						color = "yellow";
					}
					else if (value > 0) {
						color = "green";
					}

					// FIX: I would like to make the output 2 or 3 columns wide to minimize screen use
					// if possible make this configurable by the end user
					// let them decided how many columns and how they are sorted
					// ie 1,2,3 down then 3,4,5, or 1,2,3 across then 3,4,5 across
					// 1, 4, 7 or 1, 2, 3
					// 2, 5, 8    4, 5, 6
					// 3. 6, 9    7, 8, 9
					//send(((Skills) o).toString() + Colors.WHITE + " : " + (Integer) temp.skills.get(o));
					// ?? FIXED ??

					String skill = ((Skill) o).toString(); 
					String output = "";

					output = colors(skill, color) + " : " + value;

					if (si < 3) {
					}
					else {
						client.write('\n');
						si = 0;
					}

					client.write(Utils.padRight(output, 37));
					si++;
				}
				client.write('\n');
			}

			private void cmd_quests(final String arg, final Client client) {
				final Player player = getPlayer(client);

				send("Quests", client);
				send("================================================================================", client);
				for (Quest quest : player.getQuests()) {
					if ( !quest.isComplete() ) {
						client.write(Colors.YELLOW + "   o " + quest.getName());
						client.write(Colors.MAGENTA + " ( " + quest.location + " ) " + Colors.CYAN);
						client.write('\n');
						for (Task task : quest.getTasks()) {
							if ( task.isComplete() ) {
								// should be greyed out if task is complete
								client.write(Colors.GREEN + "      o " + task.getDescription());
								if ( task.getType().equals(TaskType.KILL) ) {
									client.write(" [ " + task.kills + " / " + task.toKill + " ]");
								}
								client.write(Colors.MAGENTA + " ( " + task.location + " ) " + Colors.CYAN);
								client.write('\n');
							}
							else {
								client.write(Colors.CYAN + "      o " + task.getDescription());
								if ( task.getType().equals(TaskType.KILL) ) {
									client.write(" [ " + task.kills + " / " + task.toKill + " ]");
								}
								client.write(Colors.MAGENTA + " ( " + task.location + " ) " + Colors.CYAN);
								client.write('\n');
							}
						}
					}
				}
				client.write("" + Colors.WHITE);
				send("================================================================================", client);
			}

			// Function to disconnect player
			private void cmd_quit(final String arg, final Client client) {
				initDisconn(client);
			}

			// Object/Room Recycling Function
			private void cmd_recycle(final String arg, final Client client) {
				Player player = getPlayer(client);

				// run the recycle function
				// need to find object whose name is arg and pass that object to cmd_recycle, food for thought here -- 4.15.2010
				MUDObject object = null;
				final int dbref = Utils.toInt(arg, -1);

				if (dbref != -1) { // if we found one
					try {
						object = getObject(dbref);
					}
					catch (NullPointerException npe) {
						npe.printStackTrace();
					}
				}
				else { // or, maybe not (try strings)
					object = getObject(arg, client);
				}

				if (object != null && !(object instanceof Player)) { // if we got an object and we have a valid player

					boolean success = false;

					String name = object.getName();
					int num = object.getDBRef();

					if (object instanceof Thing) {
						Thing thing = (Thing) object;
						//Room room = getRoom(thing.getLocation());

						// remove thing from room
						objectDB.removeThing(thing);          // recycle the thing

						success = true;
					}
					else if (object instanceof Exit) {
						Exit exit = (Exit) object;
						Room room = getRoom(exit.getLocation());

						room.getExits().remove(exit); // remove exit from room
						objectDB.removeExit(exit);          // remove exit from db

						success = true;
					}
					else if (object instanceof Room) {
						send("Recycle: Room recycling broken, needs to do a better job of cleaning up the room.", client);

						Room room = (Room) object;

						// destroy exits from this room
						/*if (object instanceof Exit) {
					room.getExits().remove((Exit) object);
				}
				else if (object instanceof Room) {
					// clean other stuff in the room out
				}
				else if (object instanceof Thing) {
					room.contents.remove((Thing) object);
				}*/

						objectDB.removeRoom(room);          // recycle the room

						success = true;
					}
					else {
						send("Recycle: Cannot recycle that. (" + object.getClass().getName() + ")", client);
					}

					if ( success ) {
						String msg = name + "(#" + num + "): Recycled."; // i(#127728): Recycled.

						NullObject nobj = new NullObject(num);

						objectDB.set(num, nobj);                            // clear the database entry (object)

						send(msg, client);
					}
				}
			}

			/**
			 * Retrieve items from inside of things?
			 * 
			 * NOTE: should only work on current room
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_retrieve(final String arg, final Client client) {
				Player player = getPlayer(client);
				Room room = getRoom(client);

				// split the arguments into a string array by space characters
				final String[] args = arg.split(" ");
				// tell us how many elements the array has (debug)
				debug(args.length);

				if (arg.contains("from") && args.length == 3) {
					debug("checking for stuff");
					for (final String s : args) {
						debug(s); // tell us what the elements of the array are (debug)
					}
					if (args[1].toLowerCase().equals("from")) { // if the second argument is from (should confirm that this is the intended syntax -- see above)
						// i.e. take <item> from <container>
						// perhaps get <item> from <container> would be better
						try {
							debug("looking in containers"); // tell us that we're looking in any containers the player has or that are nearby? (furniture?)

							ArrayList<Container<Item>> containers = new ArrayList<Container<Item>>(5);

							final Item i = (Item) objectDB.get(Integer.parseInt(args[0])); // get the thing to get by dbref
							if (i == null) { debug("NULL"); }
							else { debug(i); } // send us a string representation of the object (debug)

							MUDObject m = objectDB.get(Integer.parseInt(args[2]));

							/*for (Thing thing : getRoom(client).contents) { // get all the containers nearby??
						if (thing instanceof Container) { // this is not possible, since it's not an item
						}
						else { debugP(c); } // send us a string representation of the object (debug)
					}*/

							Container<Item> container;

							for (final Container<Item> con : containers)
							{
								container = con;

								if (con == null) { debug("NULL"); }
								else {
									debug(con); // send us a string representation of the object (debug)
									if ( container.contains(i) ) {
										Item item = (Item) container.remove(container.indexOf(i));
										item.setLocation(player.getLocation());
										player.getInventory().add(item);
									}
									else {
										debug(args[0] + " not found in " + args[2]);
									}
								}
							}
						}
						catch(NumberFormatException nfe) {
							debug("Exception(TAKE): " + nfe.getMessage());
						}
						catch(Exception e) {
							debug("Exception(TAKE): " + e.getMessage());
						}
					}
				}
			}

			/**
			 * Command to launch item editor (iedit)
			 * 
			 * NOTE: editor concept borrowed from ROM, a derivative of Merc,
			 * a derivative of DIKU.
			 * 
			 * Basically you call the room editor like this (at least from inside the code):
			 * 'cmd_itemedit(<room #/room name>, client)'
			 * 
			 * And it attempts to find the item and edit it,
			 * if it can't find it, it will indicate a failure and
			 * open the editor with no room.
			 */
			private void cmd_itemedit(final String arg, final Client client) {
				final Player player = getPlayer(client);
				final String old_status = player.getStatus();

				player.setStatus("EDT");
				player.setEditor(Editor.ITEM);

				edData newEDD = new edData();

				// create new item if no item to edit specified
				if ( arg.equals("") ) {
					Item item = createItem();

					if ( item.Edit_Ok ) {
						item.Edit_Ok = false; // further edit access not permitted (only one person may access at a time)
					}
					else { // item is not editable, exit the editor
						// reset player, and clear edit flag and editor setting
						player.setStatus(old_status);
						player.setEditor(Editor.NONE);

						// clear editor data
						player.setEditorData(null);

						send("Game> Item Editor - Error: item not editable (!Edit_Ok)", client);

						return;
					}

					// record prior player status
					newEDD.addObject("pstatus", old_status);

					// add item and it's constituent parts to the editor data
					newEDD.addObject("item", item);
					newEDD.addObject("desc", item.getDesc());
					newEDD.addObject("name", item.getName());
					newEDD.addObject("type", item.getItemType());

					player.setEditorData(newEDD);
				}
				else {
					Item item = null;
					boolean exist = false;

					try {
						int dbref = Integer.parseInt(arg);
						item = getItem(dbref);

						if ( item.Edit_Ok ) {
							item.Edit_Ok = false; // further edit access not permitted (only one person may access at a time
						}
						else { // item is not editable, exit the editor
							// reset player, and clear edit flag and editor setting
							player.setStatus(old_status);
							player.setEditor(Editor.NONE);

							// clear editor data
							player.setEditorData(null);

							send("Game> Item Editor - Error: item not editable (!Edit_Ok)", client);

							return;
						}

						exist = true;
					}
					catch(NumberFormatException nfe) { // no item with that dbref, cannot edit (abort)
						nfe.printStackTrace();

						// reset player, and clear edit flag and editor setting
						player.setStatus(old_status);
						player.setEditor(Editor.NONE);

						// clear editor data
						player.setEditorData(null);

						send("Game> Item Editor - Unexpected error caused abort (number format exception)", client);
					}
					catch(NullPointerException npe) { // null item, cannot edit (abort)
						// reset player, and clear edit flag and editor setting
						player.setStatus(old_status);
						player.setEditor(Editor.NONE);

						// clear editor data
						player.setEditorData(null);

						send("Game> Item Editor - Unexpected error caused abort (null pointer exception)", client);
					}

					if (exist) {	// item exists
						// record prior player status
						newEDD.addObject("pstatus", old_status);

						// add item and it's constituent parts to the editor data
						newEDD.addObject("item", item);
						newEDD.addObject("desc", item.getDesc());
						newEDD.addObject("name", item.getName());
						newEDD.addObject("type", item.getItemType());
					}
					else { // item doesn't exist (abort)
						// reset player, and clear edit flag and editor setting
						player.setStatus(old_status);
						player.setEditor(Editor.NONE);

						// clear editor data
						player.setEditorData(null);

						send("Game> Item Editor - Error: item does not exist", client);

						return;
					}

					player.setEditorData(newEDD);
				}

				op_iedit("show", client); // print out the info page
			}

			/**
			 * Command to launch room editor (redit)
			 * 
			 * NOTE: concept borrowed from ROM, a derivative of Merc,
			 * a derivative of DIKU.
			 * 
			 * Basically you call the room editor like this:
			 * 'cmd_roomedit(<room #/room name>, client)'
			 * 
			 * And it attempts to find the room and edit it,
			 * if it can't find it, it will indicate a failure and
			 * open the editor with no room.
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_roomedit(final String arg, final Client client) {
				Player player = getPlayer(client);
				String old_status = player.getStatus();

				player.setStatus("EDT");       // set the 'edit' status flag
				player.setEditor(Editor.ROOM); // room editor

				edData newEDD = new edData();

				// create new room if no room to edit specified
				if ( arg.equals("") ) {
					Room room = createRoom("name", 0);

					if ( room.Edit_Ok ) {
						room.Edit_Ok = false; // further edit access not permitted (only one person may access at a time)
					}
					else { // room is not editable, exit the editor
						abortEditor("Game> Room Editor - Error: room not editable (!Edit_Ok)", old_status, client);
						return;
					}

					// record prior player status
					newEDD.addObject("pstatus", old_status);

					// add room and it's constituent parts to the editor data
					newEDD.addObject("room", room);
					newEDD.addObject("desc", room.getDesc());
					newEDD.addObject("name", room.getName());
					newEDD.addObject("x", room.x);
					newEDD.addObject("y", room.y);
					newEDD.addObject("z", room.z);

					player.setEditorData(newEDD);
				}
				else {
					Room room = null;
					boolean exist = false;

					if ( arg.toLowerCase().equals("here") ) {
						room = getRoom(player.getLocation());

						if ( room.Edit_Ok ) {
							room.Edit_Ok = false; // further edit access not permitted (only one person may access at a time
						}
						else { // room is not editable, exit the editor
							abortEditor("Game> Room Editor - Error: room not editable (!Edit_Ok)", old_status, client);
							return;
						}

						exist = true;
					}
					else {
						final int dbref = Utils.toInt(arg, -1);
						if (dbref != -1) {
							room = getRoom(dbref);
							if (room != null) {

								if ( room.Edit_Ok ) {
									room.Edit_Ok = false; // further edit access not permitted (only one person may access at a time
								}
								else { // room is not editable, exit the editor
									abortEditor("Game> Room Editor - Error: room not editable (!Edit_Ok)", old_status, client);
									return;
								}

								exist = true;
							}
						}
					}

					if (exist) {	// room exists
						// record prior player status
						newEDD.addObject("pstatus", old_status);

						// room attributes
						newEDD.addObject("room", room);
						newEDD.addObject("desc", room.getDesc());
						newEDD.addObject("name", room.getName());
						newEDD.addObject("x", room.x);
						newEDD.addObject("y", room.y);
						newEDD.addObject("z", room.z);
					}
					else { // room doesn't exist (abort)
						abortEditor("Game> Room Editor - Error: room does not exist", old_status, client);
						return;
					}

					player.setEditorData(newEDD);
				}

				op_roomedit("show", client); // print out the info page
			}

			// 'say' function
			private void cmd_say(final String arg, final Client client)
			{
				send("You say, \"" + arg + "\"", client);
				Message msg = new Message(getPlayer(client), arg);
				addMessage(msg);
			}

			private void cmd_set(final String arg, final Client client) {
				Player player;
				Room room;

				player = getPlayer(client);
				room = getRoom(client);

				// here=header:======================
				// here,head:=================
				String[] tmp = arg.split("=", 2); 
				for (final String s : tmp) {
					System.out.println(s);
				}
				System.out.println("Length(tmp): " + tmp.length);

				if (tmp.length > 1) {

					// head:================= -> head,=================
					final String[] tmp1 = Utils.trim(tmp[1]).split(":", 2);
					for (final String s : tmp1) {
						System.out.println(s);
					}
					System.out.println("Length(tmp1): " + tmp1.length);

					if (tmp[0].toLowerCase().equals("me")) {
						player = getPlayer(client);
						if (tmp1.length > 1 && !tmp1[1].equals("")) {
							player.getProps().put(tmp1[0], tmp1[1]);
							send("Property \'" + Utils.trim(tmp1[0]) + "\' with value of \'" + Utils.trim(tmp1[1]) + "\' added to " + player.getName(), client);
						}
						else {
							player.getProps().remove(Utils.trim(tmp1[0]));
							send("Property \'" + Utils.trim(tmp1[0]) + "\' removed from " + player.getName(), client);
						}
					}
					else if (tmp[0].toLowerCase().equals("here")) {
						if ( tmp1.length > 1 && !tmp1[1].equals("")) {
							room.getProps().put(tmp1[0], tmp1[1]);
							send("Property \'" + Utils.trim(tmp1[0]) + "\' with value of \'" + tmp1[1] + "\' added to " + room.getName(), client);
						}
						else {
							room.getProps().remove(Utils.trim(tmp1[0]));
							send("Property \'" + Utils.trim(tmp1[0]) + "\' removed from " + room.getName(), client);
						}
					}
				}
			}

			private void cmd_setmode(final String arg, final Client client) {
				final char m = arg == null ? '?' : arg.toLowerCase().charAt(0);
				if (!GameMode.isValidString(m)) {
					send("Invalid GameMode, using Normal instead.", client);
				}
				mode = GameMode.fromString(m);
				send("Game> setting GameMode to -" + mode + "-", client);
			}

			/**
			 * Set Skill (setskill):
			 * 
			 * Increases the specified player skill by the amount given, negative or positive,
			 * if the input is positive it is a skill point gain, otherwise it is a skill point loss.
			 * 
			 * Syntax: setskill [player name] = [int value < max skill value]
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_setskill(final String arg, final Client client) {
				// setskill <skill name> = <int value < max skill value>
				// setskill <player>:<skill name> = <int value < max skill value>
				final String[] args = arg.split("=");
				System.out.println("@setskill args: ");
				for (final String s : args) {
					System.out.println(s);
				}
				if (args.length > 1) {
					String skillName = args[0];
					Integer skillValue = Integer.parseInt(args[1].replaceAll(" ", ""));
					Player p = getPlayer(client);

					for (final Skill skill : p.getSkills().keySet()) {
						System.out.println(skillName);
						System.out.println(skill.toString());

						if (skill.toString().toLowerCase().equals(skillName.toLowerCase())) {

							if (skillValue < MAX_SKILL) {
								send("Set " + skill.toString() + " skill to " + skillValue, client);
								System.out.println(p.getSkills().put(skill, skillValue));
							}
							else {
								send("Setting exceeds maximum skill value, change aborted.", client);
							}
						}
					}
				}
			}

			private void cmd_score(final String arg, final Client client) {
				final Player player = getPlayer(client);

				send("You are " + player.getName() + " " + player.getTitle() + ", level " + player.getLevel(), client);
				send("Race: " + player.getPlayerRace().getName() + " Sex: " + player.getGender().toString() + " Class: " + player.getPClass().getName(), client);
				send("Money: " + player.getMoney().toString() + ".", client);
			}

			private void cmd_success(final String arg, final Client client) {
				final String[] args = arg.split("=");
				final Exit exit = (Exit) getExit(args[0]);
				if (args.length > 1) {
					exit.setMessage("succMsg", args[1]);
					send(exit.getName() + "'s success message set to: " + args[1], client);
				}
			}

			// Server Start Function (usually used to start again after manual shutdown)
			private void cmd_start(final String arg, final Client client)
			{
				// initialize the server object
				s = new Server(this, port);
				// tell us the server has started
				System.out.println("Server Startup!\n");
				// reload the help files
				help_reload();
				// load the database from disk
				cmd_loadDB(arg, client);
				running = true;
				//loop();
			}

			// Server Shutdown function
			// need to handle the secs argument inside to make the command parameter input
			// more uniform with the other commands
			// replace signature with below:
			//public void cmd_shutdown(String arg, Client client) {}
			// current it does not match the command arguments format

			private void cmd_shutdown(final String arg, final Client client)
			{
				// shutdown
				// shutdown now
				// shutdown -h 5

				// if the type of shutdown is null and no time was specified
				if ( arg.equals("") )
				{
					debug("SHUTDOWN TYPE: NORMAL");
					// Tell people the server is going down, so they know what happened when they get kicked off.
					s.write("Server going down for reboot.");
				}
				// if the type of shutdown is null and some kind of time was specified
				/*else if (type.equals("null"))
		{
			debug("SHUTDOWN TYPE: TIMED");
			s.write("Server going down for reboot in " + secs / 60 + "m" + secs % 60 + "s");
		}
		else
		{
			debug("SHUTDOWN TYPE: IMMEDIATE");
			s.write("Server going down immediately.");
		}*/

				shutdown();
			}

			//private void shutdown(String type, int secs, Client client)

			// Function to list stats
			private void cmd_stats(final String arg, final Client client)
			{
				System.out.println(objectDB.getSize());

				final int[] counts = objectDB.getFlagCounts(new String[]{ "P", "N", "E", "R", "T" });
				final int usersCount = counts[0];
				final int npcsCount = counts[1];
				final int exitsCount = counts[2];
				final int roomsCount = counts[3];
				final int thingsCount = counts[4];

				int total = usersCount + npcsCount + exitsCount + roomsCount + thingsCount;

				send(serverName + " Statistics", client);
				send("-----------------------", client);
				send(String.format("Players: %s   %s%%", usersCount,  usersCount  * 100.0 / total), client);
				send(String.format("NPCS:    %s   %s%%", npcsCount,   npcsCount   * 100.0 / total), client);
				send(String.format("Exits:   %s   %s%%", exitsCount,  exitsCount  * 100.0 / total), client);
				send(String.format("Rooms:   %s   %s%%", roomsCount,  roomsCount  * 100.0 / total), client);
				send(String.format("Things:  %s   %s%%", thingsCount, thingsCount * 100.0 / total), client);
				send("Total:   " + total, client);
				send("-----------------------", client);
			}

			private void cmd_status(final String tempArg, final Client client)
			{
				/*
				 * OOC - Out-of-Character
				 * IC - In-Character
				 * EDT - Editing/Edit Mode
				 * INT - interacting/Interactive Mode
				 */
				final Player player = getPlayer(client);
				player.setStatus(tempArg.toUpperCase());
				send("Setting status: " + player.getStatus(), client);

				if (tempArg.equals("")) {
					send("Status Cleared!", client);
				}
				player.setEditor(Editor.NONE);
			}

			// Function to take objects in a room
			@SuppressWarnings("unchecked")
			private void cmd_take(final String arg, final Client client)
			{
				// get player, room objects to work with
				final Player player = getPlayer(client);
				final Room room = getRoom(client);

				// split the arguments into a string array by space characters
				final String[] args = arg.split(" ");
				// tell us how many elements the array has (debug)
				debug(args.length);

				// if there is no argument
				if ( arg.equals("") ) {
					send("Syntax: take <item>", client);
				}
				// if there are three arguments, implying the following syntax: TAKE <thing> FROM <container>
				else if (arg.toLowerCase().equals("all")) {
					// all implies stuff on the ground
					// since all the stuff on the ground is in the room, we should evaluate the room to get it's stuff
					final Room r = getRoom(client);
					// basically we want to evalutate all the items, then take the one with the largest value, one at a time
					// the evaluation scheme needs to take what's usable and what's not as well monetary value into account
					// if we have room for everything, then just take it all
					/**ArrayList<Integer> item values
			for (Item item : r.contents1) {
				evaluate(item);
			}**/
				}
				else { // assuming one argument
					// get the object the argument refers to: by name (if it's in the room), or by dbref#
					// should be done by searching the room's contents for the object and if there is such an object, put it in the player's inventory
					for (final Item item : room.contents1)
					{
						final int dbref = Utils.toInt(arg, -1);

						// if there is a name or dbref match from the argument in the inventory
						// if the item name exactly equals the arguments or the name contains the argument (both case-sensitive), or if the dbref is correct
						if ( item.getName().equals(arg) || item.getName().contains(arg) || item.getDBRef() == dbref )
						{
							debug(item.getName() + " true");
							// move object from it's present location to player inventory
							// it would be good to just replace this with a function, since it will need to test for a standard location to put it
							// see if there is a generic storage container to put it in
							if ( hasGenericStorageContainer( player, item ) ) {
								/*debug(item.getName() + " container");
						Container<Item> c = getGenericStorageContainer( player, item );
						item.setLocation(c.getDBRef());
						c.add( item );
						send("You picked " + colors(item.getName(), "yellow") + " up off the floor and put it in " + c.getName(), client);*/
							}
							// else just stick it in inventory
							else {
								debug(item.getName() + " inventory");

								// if there is an existing, not full stack of that item trying to add these to it
								if (item instanceof Stackable) {
									Stackable sItem = (Stackable) item;

									if ( getItem(item.getName(), player) != null ) {
										debug("stackable - have a stack already");
										Stackable sItem1 = (Stackable) getItem(item.getName(), player);
										if (sItem1.stackSize() < MAX_STACK_SIZE) {
											sItem1.stack(sItem);
										}
										else {
											continue;
										}
										debug(player.getInventory().contains(item));
									}
									else {
										debug("stackable - new stack");
										player.getInventory().add(item);
										debug(player.getInventory().contains(item));
									}
								}
								else {
									debug("not stackable");
									player.getInventory().add(item);
									debug(player.getInventory().contains(item));
								}

								debug(item.getLocation());           // old location
								item.setLocation(player.getDBRef()); // "move" item
								debug(item.getLocation());           // new location

								send("You picked " + colors(item.getName(), "yellow") + " up off the floor.", client);
							}

							// remove from the room
							room.contents1.remove(item);

							// check for silent flag to see if object's dbref name should be shown as well?
							// return message telling the player that they picked up the object
							// return message telling others that the player picked up the item
							// needs to be placed in the message queue for just the room somehow, not sent to the current player
							return;
						}
						else {
							//send("No such item.", client);
							send(arg + "?", client);
							send("Did you mean, " + item.getName() + " - " + item.getDBRef(), client);
						}
					}
				}
			}

			private void cmd_target(final String arg, final Client client) {
				Player player = getPlayer(client);
				Player target = getPlayer(arg);

				// if we currently have a target, tell us what it is
				if (player.getTarget() != null) {
					debug(player.getTarget());
					debug(arg);
				}

				debug("Getting target..." + target.getName());

				player.setTarget(target);

				// tell us what we are targetting now
				if (player.getTarget() != null) {
					debug(player.getTarget());
					debug(arg);
					send("Target set to: " + player.getTarget().getName(), client);
				}
			}

			private void cmd_tell(String arg, Client client) {
			}

			/**
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_unequip(final String arg, final Client client) {
				Player player = getPlayer(client);
				final int i = Utils.toInt(arg, -1);

				if (arg.equals("") && i == -1) {
					send("Unequip what?", client);
				}
				else {
					for (final String s : player.getSlots().keySet()) {
						debug(s);
						Slot slot = player.getSlots().get(s);
						if (slot.isFull()) {
							if (slot.getItem().getName() == arg || slot.getItem().getDBRef() == i) {
								Item item = slot.getItem();

								item.equipped = false; // set item's equipped "flag" to false (unequipped)
								player.unequip(slot.remove()); // mask for adding it to the inventory

								send(item.getName() + " un-equipped (" + item.equip_type + ")", client);
								break;
							}
							else {
								send("You don't have that equipped. (Equip Slot Empty)", client);
							}
						}
						else {
							send("Really? You don't have one of those equipped!", client);
						}
					}
				}
			}

			// unlock command (applies to lockable things)
			private void cmd_unlock(final String arg, final Client client) {
				MUDObject m = getObject(arg);
				if (m instanceof Lockable) {
					Lockable l = (Lockable) m;
					if (l.isLocked()) {
						l.unlock();
						send(m.getName() + " locked.", client);
					}
				}
			}

			private void cmd_use(final String arg, final Client client) {
				Player player = getPlayer(client);

				// look at the player first
				if (arg.equals("") ) {
					debug("Game> Arguments?");
					for (Entry<String, Slot> e : player.getSlots().entrySet()) {
						Slot s = e.getValue();
						debug(s);

						if (s != null && s.isFull()) {
							if (s.isType(ItemType.RING)) {
								Item item = s.getItem();
								if (item instanceof Jewelry) {
									debug("Item is Jewelry");
									Jewelry j = (Jewelry) item;
									j.use("", client);
								}
							}
						}

					}
				}
				// then check the room
				else {
					debug("Game> Arguments Received.");

					MUDObject m;

					try {
						m = getObject(arg);

						System.out.println("MUDObject: " + m.getName());

						if (m instanceof Potion) { use_potion( (Potion) m, client); }      // potion handling
						else if (m instanceof Portal) {
							use_portal( (Portal) m, client); // portal handling

							int location = getPlayer(client).getLocation();
							Room room = getRoom(location);
							look(room, client);
						}
						else if (m instanceof Wand) { use_wand( (Wand) m , client); }      // wand handling
					}
					catch(NullPointerException npe) {
						npe.printStackTrace();
						return;
					}
				}
			}

			private void cmd_vitals(final String arg, final Client client) {
				Player player = getPlayer(client);

				// tell us how many hitpoints we have
				client.write("HP: " + player.getHP() + "/" + player.getTotalHP() + " " +
						"MANA: " + player.getMana() + "/" + player.getTotalMana());

				client.write(" ");

				// refresh player state info
				player.updateCurrentState();

				// indicate whether we're alive or not
				switch( player.getState() ) {
				case ALIVE:
					client.write(colors("ALIVE", "green") + '\n');
					break;
				case INCAPACITATED:
					client.write(colors("INCAPACITATED", "yellow") + '\n');
					break;
				case DEAD:
					client.write(colors("DEAD", "red") + '\n');
					break;
				default:
					break;
				}

				// FULL, HIGH, MED, LOW, DEPLETED
			}

			/**
			 * list player locations
			 * 
			 * COMMAND OBJECT EXISTS, not in use though
			 * 
			 * @param arg
			 * @param client
			 */
			private void cmd_where(final String arg, final Client client)
			{
				int n = 0;

				send("Player     Class     S Race      Idle Status Location", client);
				// 10+1+9+1+(1)+1+9+1+4+1+6+1+24 = 69
				send(Utils.padRight("", '-', 69), client);
				for (Player player : players)
				{
					try {
						String name = player.getName(); // need to limit name to 10 characters
						String cname = player.getCName();
						//String title = player.getTitle(); // need to limit title to 8 characters
						String playerClass = player.getPClass().getName();
						String playerGender = player.getGender().toString();
						String race = player.getPlayerRace().toString();
						String ustatus = player.getStatus(); // need to limit status to 3 characters
						int location = player.getLocation(); // set room # limit to 5 characters (max. 99999)
						String roomName = getRoom(location).getName(); // truncate to 24 characters?
						String locString;

						if (player.hasEffect("invisibility")) { locString = "INVISIBLE"; }
						else {
							if (!getRoom(player.getLocation()).flags.contains("S")) {
								locString = roomName + " (#" + location + ")";
							}
							else { locString = roomName; }
						}

						String idle = player.getIdleString();

						Player current = getPlayer(client);

						if (current.getNames().contains(name) || current.getName().equals(name)) {
							send(Utils.padRight(name, 10) + " " + Utils.padRight(playerClass, 9) + " " + Utils.padRight(playerGender, 1) + " " + Utils.padRight(race, 9) + " " + Utils.padRight(idle, 4) + " " + Utils.padRight(ustatus, 6) + " " + locString, client);
						}
						else {
							send(Utils.padRight(cname, 10) + " "+ Utils.padRight(playerClass, 9) + " " +  Utils.padRight(playerGender, 1) + " " + Utils.padRight(race, 9) + " " + Utils.padRight(idle, 4) + " " + Utils.padRight(ustatus, 6) + " " + locString, client);
						}

						n++;
					}
					catch(NullPointerException npe) {
						npe.printStackTrace();
					}
				}
				send(Utils.padRight("", '-', 69), client);
				send(n + " players currently online.", client);
			}

			//Function to list player locations
			private void cmd_who(final String arg, final Client client)
			{
				int n = 0;

				for (final Player player : players)
				{
					try {
						String name = player.getName();                  // need to limit name to 10 characters
						String cname = player.getCName();
						String title = player.getTitle();                // need to limit title to 8 characters
						String race = player.getPlayerRace().toString();
						
						StringBuilder sb = new StringBuilder();

						// [ level class ] name - specialty/prestige class - group/guild (race)
						sb.append(colors("[", "blue"));
						sb.append(player.getLevel() + "");
						sb.append(' ');
						sb.append(colors(player.getPClass().getAbrv(), player.getPClass().getColor()));
						sb.append(colors("]", "blue"));
						sb.append(' ');

						// name
						if ( loginCheck( client ) ) {
							if (player.getNames().contains(name) || getPlayer(client).getName().equals(name)) {
								sb.append(name);
							}
							else {
								sb.append(cname);
							}
						}
						else {
							sb.append(name);
						}

						// title
						if (!title.equals("")) { // if title isn't empty
							sb.append(' ');
							sb.append("\'" + title + "\'");
						}

						// race
						sb.append(' ');
						sb.append("(" + race + ")");
						sb.append("\r");
						sb.append('\n');
						
						client.write(sb.toString());

						// count players
						n++;
					}
					catch(NullPointerException npe) { System.out.println(npe.getMessage()); }
				}

				send(n + " players currently online.", client);
			}

			// creates a zone or adds a room to a zone, no room may be added to a zone if it
			// exceeds the max zone size though it may if the zone is less than the max, at which point
			// it's size will be increased
			// syntax: @zones +new [zone name]=<zone parent>, @zones +add [room to zone]=<zone parent>
			// <zone parent> will be either a specified parent by dbref, a default parent, one the player
			// has set beforehand for themselves or a default zone in the case that it is not specified
			//
			// DEBUG: need to debug this code and make sure there aren't any logical or coding errors
			private void cmd_zones(final String arg, final Client client) {
				Room room;

				final String[] params = arg.split(" ");

				debug("# Params: " + params.length);

				if (params.length >= 2) {
					String[] args = params[1].split("=");
					if (params[0].equals("+new")) {
						if (args != null) {
							if (args.length > 1) {
								room = getRoom(Integer.parseInt(args[1]));
								room.setFlags(EnumSet.of(ObjectFlag.ZONE)); // set the zone flags
								Zone zone = new Zone(args[0], room);
								zones.put(zone, 10); // store a new zone object
								send("" + zone.getRoom(), client); // tell us the room is 
								send("New Zone Established!", client); // tell us that it succeeded.
							}
						}
					}
					else if (params[0].equals("+add")) {
						if (args != null) {
							if (args.length > 1) {
								room = getRoom(args[0]);
								if (room != null) {
									try {
										room.setLocation(Integer.parseInt(args[1]));
										room = getRoom(room.getLocation()); // get the current room's parent
										if (room.getFlags().contains("Z")) 
										{
											send(getRoom(args[0]).getName() + " added to zone.", client);
										}
									}
									catch(NumberFormatException nfe) { send(gameError("@zones", 2), client); }
									catch(NullPointerException npe) { send("One or more invalid rooms given.", client); }
								}
							}
						}
					}
				}
				else {
					send("Zones:", client);
					debug(zones.entrySet());
					for (Object k : zones.keySet()) {
						send("" + ((Zone) k).getName(), client);
						for (final Room r : objectDB.getRoomsByParentLocation(((Zone) k).getRoom().getLocation())) {
							send("     - " + r.getName() + "(#" + r.getLocation() + ")", client);
						}
					}
				}
			}

			/**
			 * List what is available for sale from a vendor/merchant
			 * 
			 * currently no argument is accepted, but perhaps it could be
			 * utilized to take a keyword for a specific category of items
			 * the vendor/merchant sells. I.e. we know they sell weapons,
			 * but can directly request a list of 'swords' or 'axes'. 
			 * 
			 * @param arg    not used
			 * @param client the client sending this command
			 */
			private void cmd_list(final String arg, final Client client) {
				final Player player = getPlayer(client);

				if (player.getStatus().equals("INT")) {
					if (player.getTarget() instanceof Vendor) {

						send("-----< Stock >--------------------", client);

						for (final Item item : ((Vendor) player.getTarget()).list()) {
							if (item instanceof Weapon) {
								final Weapon w = (Weapon) item;
								send(colors("+" + w.getMod() + " " + w.weapon.getName() + " " + w.getDesc() + " (" + w.getWeight() + ") Cost: " + w.getCost(), "yellow"), client);
							}
							else if (item instanceof Armor) {
								final Armor a = (Armor) item;
								send(colors("+" + a.getMod() + " " + a.armor.getName() + " " + a.getDesc() + " (" + a.getWeight() + ") Cost: " + a.getCost(), "yellow"), client);
							}
							else {
								send(colors(item.getName() + " " + item.getDesc() + " (" + item.getWeight() + ") Cost: " + item.getCost(), "yellow"), client);
							}
						}

						send("----------------------------------", client);
					}
				}

			}

			private void cmd_add(String arg, Client client) {
			}

			private void cmd_offer(final String arg, final Client client) {
				final Player player = getPlayer(client);

				if (player.getStatus().equals("INT")) {
					if (player.getTarget() instanceof NPC) {
						NPC npc = (NPC) player.getTarget();

						npc.interact(0);
					}
				}
			}

			private void cmd_sell(final String arg, final Client client) {
				final Player player = getPlayer(client);

				if (player.getStatus().equals("INT")) {
					if (player.getTarget() instanceof NPC) {
						NPC npc = (NPC) player.getTarget();

						npc.interact(0);
					}
				}

				/*Player player = getPlayer(client);

		if (player.getStatus().equals("INT")) { // interact mode
			NPC npc = (NPC) player.getTarget();
			send(npc.getName(), client);               // tell us his/her name
			if (npc.getFlags().contains("V")) {
				debugP("Target is NPC.");
				debugP("Target is Vendor");
				Vendor v = (Vendor) npc;

				Item item;
				Item item1;

				if (v.hasItem(arg))
				{
					item = v.getItem(arg);

					if (canAfford(player.getMoney(), item.getCost())) {
						item1 = v.buy(arg);

						//send("The shopkeeper takes the money and gives you a", client);
						//send("Terys takes the money and gives you a", client);
						send("Item Bought!", client);
						item1.setLocation(player.getDBRef());
						debugP("Item #" + item1.getDBRef() + " has new location which is #" + item.getLocation());
						player.getInventory().add(item1);
					}
					else {
						send("I'm sorry, you can't afford that.", client);
					}
				}
				else {
					send("I'm sorry, we won't buy that.", client);
				}
			}
			else {
				debugP("Target is NPC.");
			}
		}
		else {
			debugP("Target is not npc.");
		}*/
			}

			private void cmd_value(final String arg, final Client client) {

			}


			// Inline Status bar
			/**
			 * Draws a status bar every time on calling the function
			 * shows the players current hitpoints and mana. Maybe I could
			 * strap this to a timing loop to get it to show repeatedly.
			 * 
			 * @param client
			 */
			public void prompt(final Client client) {
				if ( prompt_enabled ) {
					prompt("< %h/%H %m/%M >", client);
				}
			}

			/**
			 * Displays a prompt.
			 * 
			 * Output is based on a supplied pattern.
			 * 
			 * @param pattern
			 */
			public void prompt(final String pattern, final Client client) {

				// private String custom_prompt = "< %h/%H  %mv/%MV %m/%M >";
				// borrowed from DIKU -> ROM, etc?
				// h - hitpoints, H - max hitpoints
				// mv - moves, MV - total moves
				// m - mana, M - total mana

				Player player = getPlayer(client);

				String output = pattern;

				String hp = ((Integer) player.getHP()).toString();
				String max_hp = ((Integer) player.getTotalHP()).toString();

				String mana = ((Integer) player.getMana()).toString();
				String max_mana = ((Integer) player.getTotalMana()).toString();

				output = output.replace( "%h", hp );
				output = output.replace( "%H", max_hp );

				output = output.replace( "%m", mana );
				output = output.replace( "%M", max_mana );

				//send(output, client);
				addMessage(new Message(output, player));
			}

			/**
			 * Function to evaluate a script/program
			 * 
			 * @param pArg
			 * @return
			 */
			public String parse_pgm(final String pArg)
			{
				System.out.println("pArg: " + pArg);

				String[] ca = new String[0];

				if (pArg.indexOf(":") != -1)
				{
					ca = pArg.split(":");

					for (final String s : ca) {
						System.out.println(s);
					}

					if (pArg.equals("{colors}") || ca[0].equals("{colors")) {
						if (ca[1] != null)
						{
							String[] params = ca[1].substring(0, ca[1].indexOf("}")).split(",");

							debug("Color: " + params[0]);

							if (params.length >= 2) {
								return "-Result: " + colorCode(params[0]) + params[1] + colorCode("white");
							}
							else { return "PGM: Error!"; }
						}
						else
						{
							return "-Result: Incomplete function statement, no parameters!";
						}
					}
					else if (pArg.equals("{rainbow}") || ca[0].equals("{rainbow")) {
						if (ca[1] != null)
						{
							String param = ca[1].substring(0, ca[1].indexOf("}"));

							if (param != null) {
								return "-Result: " + rainbow(param) + colorCode("white");
							}
							else { return "PGM: Error!"; }
						}
						else
						{
							return "-Result: Incomplete function statement, no parameters!";
						}
					}
					else if(pArg.equals("{distance}") || ca[0].equals("{distance")) {

						/*
						 * parameters:
						 * 	2d/3d
						 *  one or two points
						 */

						System.out.println("PGM -distance-");

						if( ca[1] != null ) {
							// params - two points in the form '(x,y)' separated by a ';'.
							// ex. (1,1),(4,4)

							List<String> params = new ArrayList<String>();

							StringBuffer sb = new StringBuffer();

							boolean check = true;
							int dimensions = 0;  // count points, 1 point is between your current position and there, 2 points is between the two points

							// isolate coordinate points
							for(int c = 0; c < ca[1].length(); c++) {
								char ch = ca[1].charAt(c);

								if( check ) {
									if(ch == ')') {
										debug("Current: " + sb.toString(), 3);
										debug("Added character to sb", 3);
										sb.append(ch);
										debug("Final: " + sb.toString(), 3);
										debug("finished point dec", 3);
										check = false;
										params.add(sb.toString());
										sb.delete(0, sb.length());
									}
									else {
										debug("Current: " + sb.toString(), 3);
										debug("Added character to sb", 3);
										sb.append(ch);
									}
								}
								else {
									if(ch == '(') {
										sb.delete(0, sb.length());
										debug("started point dec", 3);
										debug("Current: " + sb.toString(), 3);
										debug("Added character to sb", 3);
										sb.append(ch);
										check = true;
									}
									else if(ch == ',') {

									}
								}
							}

							if(params.size() > 0) {

								List<Point> ptList = new ArrayList<>();

								for(String param : params) {
									ptList.add(Utils.toPoint(param));
								}

								return "-Result: " + ptList;

							}
							else { return "PGM: Error!"; }
						}
						else
						{
							return "-Result: Incomplete function statement, no parameters!";
						}
					}
					else { return "PGM: Error!"; }
				}
				else if (pArg != null)
				{
					if ( pArg.equals("{name}") )
					{
						return "-Result: " + serverName;
					}
					else if ( pArg.equals("{version}") )
					{
						return "-Result: " + program + " " + version;
					}
					else if (pArg.equals("{colors}") || ca[0].equals("{colors"))
					{
						return "-Result: Incomplete function statement, no inputs!";
					}
					else if ( pArg.equals("{tell}") )
					{
						String m = "";
						String r = "";
						if (ca[1] != null)
						{
							return "-Result: You tell " + m + " to " + ca[1] + ".";
						}
						else
						{
							return "-Result: You tell " + m + " to " + r + ".";
						}
					}
					else
					{
						return "PGM: No such function! (1)";
					}
				}

				return "PGM: Error!";
			}

			// Command Support Functions

			/**
			 * Chat Handler
			 * 
			 * @param channel the channel we are writing to
			 * @param arg     the message we are writing
			 * @param client  who is writing
			 * @return did we succeed in writing to the channel? (true/false)
			 */
			public boolean chatHandler(final String channelName, final String arg, final Client client) {
				chan.send(channelName, getPlayer(client), arg);
				client.write("wrote " + arg + " to " + channelName + " channel.\n");
				chatLog.writeln("(" + channelName + ") <" + getPlayer(client).getName() + "> " + arg);
				return true;
			}

			/**
			 * Exit Handler
			 * 
			 * @param cmd
			 * @param client
			 * @return
			 */
			public boolean exitHandler(final String cmd, final Client client)
			{
				final Player player = getPlayer(client); // get current player
				Room room = getRoom(client);       // get current room

				debug("Entering exit handler...");

				for (final Exit exit : room.getExits())
				{
					if (exit.getName().equals(cmd) || exit.getAliases().contains(cmd)  || exit.getName().equals(aliases.get(cmd)))
					{
						if (true) { // exit lock check?
							debug("success");

							// send the success message
							if (!exit.succMsg.equals("")) {
								Message msg = new Message(exit.succMsg, player);
								addMessage(msg);
								//send(exit.succMsg, client);
							}

							// set player's location
							player.setLocation(exit.getDest());

							// send other exit properties

							// send the osuccess message
							if (!exit.osuccMsg.equals("")) {
								Message msg = new Message(exit.osuccMsg, room.getDBRef());
								addMessage(msg);
							}

							// remove listener from room
							room.removeListener(player);

							// get new room object
							room = getRoom(client);

							// add listener to room
							room.addListener(player);

							// call msp to play a tune that is the theme for a type of room
							if (msp == 1) { // MSP is enabled
								if (room.getRoomType().equals(RoomType.INSIDE)) { // if inside play the room's music
									// need to check and see if sound filename isn't empty
									MSP.play(room.music, "", 25, -1);
									//MSP.play(room.theme, room.theme.substring(room.theme.indexOf("."), -1), 25, -1);
									//debug("MSP", 2);
									//debug("Filename: " + MSP.fileName, 2);
									//debug("Filetype: " + MSP.fileType, 2);
									String msg = MSP.generate();
									// send the message (but only to this client)
									send(msg, client);
								}
								else if (room.getRoomType().equals(RoomType.OUTSIDE)) { // if outside, play appropriate weather sounds?
									// perhaps simply setting a pattern of some kind would be good?
									// in case we wish to have an ambient background (rain, wind) and an effect sound for lightning (thunder)
									// ASIDE: some clients only support one sound, so an effect sound should be handled
									// as the sound, and then ind1the ambient background 
								}
							}

							// show the description
							look(room, client);

							// tell us we are leaving the exit handler
							debug("Exiting exit handler...");

							return true;
						}
					}
				}

				// tell us we are leaving the exit handler
				debug("Exiting exit handler...");

				return false;
			}

			/* Editors */

			/**
			 * Interactive Casting "Editor" 
			 * 
			 * a.k.a 'Interactive Spell Mode'
			 * 
			 * a list editor like system that allows you to choose a spell from your spellbook/
			 * memorized spells (flag memorized ones with color or some other way to indicate
			 * availablity). you can also choose a target and indicate any special criteria
			 * 
			 * will allow you to set up a series of spells to cast sequentially so you don't have to
			 * set them up or allow you to construct a more complicated spell with parameters
			 * without having to 'say' them to the game.
			 * 
			 * @param input
			 * @param client
			 */
			public void op_cast(final String input, final Client client) {
				if (input.indexOf(".") != -1)
				{
					System.out.println("INTCAST CMD");
					String scmd = input.substring(input.indexOf(".") + 1, input.indexOf(""));
					String sarg = input.substring(input.indexOf("") + 1, input.length());

					System.out.println("scmd: " + scmd);
					System.out.println("sarg: " + sarg);

					if (scmd.equals("select")) {
						/* select takes a spell name as an argument
						 * 
						 */
						getPlayer(client).spellQueue.push(spells2.get(sarg));
					}
					else if (scmd.equals("queue")) {
						send("Queue", client);
						send("---------------------", client);
						for (Spell spell : getPlayer(client).getSpellQueue()) {
							send(spell.name, client);
						}
						send("---------------------", client);
					}
					else if (scmd.equals("spells")) {
						SpellBook sb = getPlayer(client).getSpellBook();
						send("Spellbook", client);
						for(int level = 0; level <= getPlayer(client).getLevel(); level++) {
							List<Spell> spells =  sb.getSpells(level);
							send("-----------------", client);
							send("Level: " + level, client);
							for(Spell spell : spells) {
								send(" " + spell.name, client); // space for indenting purposes
							}
							send("-----------------", client);

						}
						//send("-----------------", client);
						//send("Level 1:", client);
						//send(" dispel", client);
						//send(" fireball", client);
						//send(" invisibility", client);
						//send("-----------------", client);
					}
					else if (scmd.equals("finalize")) {
						/* finalize and init spell casting, slot into
						 * current "round"/present time or next available
						 * "round"/time automatically				
						 */
						send("Interactive Spell Mode> Finalizing...", client);
					}
					else if (scmd.equals("cancel"))
					{
						// tell us
						send("Interactive Spell Mode Canceled.", client);

						// clear queue

						//
						getPlayer(client).setStatus("OOC");
					}
					else if (scmd.equals("quit")) {
					}
					else if (scmd.equals("target")) {
					}
					else {
						send("Interactive Spell Mode> No such command.", client);
					}
				}
			}

			/**
			 * Character "Creator"
			 * 
			 * NOTE(?): character generation menu logic needs to be fixed, still getting number format exceptions, etc and
			 * I broke the editing to some extent
			 * 
			 * 
			 * @param input input to the editor
			 * @param client the client to which output will be sent
			 */
			public void op_chargen(final String input, final Client client) {
				final Player player = getPlayer(client);

				cgData cgd;

				cgd = player.getCGData();

				if (input.equals("start")) {
					cgd = new cgData(0, 1, 0);
					debug("T: " + cgd.t + " Step: " + cgd.step + " Answer: " + cgd.answer);

					player.setCGData( op_chargen("", client, cgd) );
				}
				else {
					player.setCGData( op_chargen(Utils.trim(input), client, player.getCGData()) );
				}

				if (cgd != null) {
					player.setCGData( op_chargen("", client, player.getCGData()) );
				}
			}

			public cgData op_chargen(final String input, final Client client, final cgData cgd) {
				final Player player = getPlayer(client);

				debug("T: " + cgd.t + " Step: " + cgd.step + " Answer: " + cgd.answer);

				int t = cgd.t;
				int step = cgd.step;
				int answer = cgd.answer;

				debug("Start: T is now " + t);

				if (t == 0) {
					send("Step: " + step, client);
					debug("Step: " + step);

					switch(step)
					{
					case 1:
						send("Please choose a race:", client);
						send("1) " + Utils.padRight("" + Races.ELF, 6) +  " 2) " + Utils.padRight("" + Races.DROW, 6) + " 3) " + Utils.padRight("" + Races.HUMAN, 6), client);
						send("4) " + Utils.padRight("" + Races.DWARF, 6) + " 5) " + Utils.padRight("" + Races.GNOME, 6) + " 6) " + Utils.padRight("" + Races.ORC, 6), client);
						send(">", client);
						break;
					case 2:
						send("Please choose a gender:", client);
						send("1) Female 2) Male 3) Other 4) Neuter (no gender)", client);
						send(">", client);
						break;
					case 3:
						send("Please choose a class:", client);
						send(" 1) " + Utils.padRight("" + Classes.BARBARIAN, 12) + " 2) " + Utils.padRight("" + Classes.BARD, 12) + " 3) " + Utils.padRight("" + Classes.CLERIC, 12), client);
						send(" 4) " + Utils.padRight("" + Classes.DRUID, 12) + " 5) " + Utils.padRight("" + Classes.FIGHTER, 12) + " 6) " + Utils.padRight("" + Classes.MONK, 12), client);
						send(" 7) " + Utils.padRight("" + Classes.PALADIN, 12) + " 8) " + Utils.padRight("" + Classes.RANGER, 12) + " 9) " + Utils.padRight("" + Classes.ROGUE, 12), client);
						send("10) " + Utils.padRight("" + Classes.SORCERER, 12) + "11) " + Utils.padRight("" + Classes.WIZARD, 12) + " 0) " + Utils.padRight("" + Classes.NONE, 12), client);
						send(">", client);
						break;
					case 4:
						send("Options:", client);
						send(" 1) Reset 2) Edit 3) Exit", client);
						break;
					case 5:
						send("Edit What:", client);
						send("1) Race 2) Gender 3) Class", client);
						break;
					default:
						break;
					}

					t = 1;

					debug("T is now " + t);
				}
				else if (t == 1) {
					try {
						if (!input.equals("")) { // if there is input
							answer = Integer.parseInt(input);
						}
						else { // return the data unchanged
							return cgd;
						}

						send("Answer: " + answer, client);
						debug("Answer: " + answer);

						if (step == 1) {
							debug("Entering Step " + step);
							player.setPlayerRace(Races.getRace(answer));
							send("Player Race set to: " + player.getPlayerRace(), client);
							send("", client);
							step++;
						}
						else if (step == 2) {
							debug("Entering Step " + step);
							switch(answer) {
							case 1:
								player.setGender('F');
								break;
							case 2:
								player.setGender('M');
								break;
							case 3:
								player.setGender('O');
								break;
							case 4:
								player.setGender('N');
								break;
							default:
								player.setGender('N');
								break;
							}
							send("Player Gender set to: " + player.getGender(), client);
							send("", client);
							step++;
						}
						else if (step == 3) {
							debug("Entering Step " + step);
							player.setPClass(Classes.getClass(answer));
							send("Player Class set to: " + player.getPClass(), client);
							send("", client);
							step++;
						}
						else if (step == 4) {
							debug("Entering Step " + step);
							if (answer == 1) {      // Reset
								player.setPlayerRace(Races.NONE);
								player.setGender('N');
								player.setPClass(Classes.NONE);

								//reset_character(player); // reset character data to defaults

								client.write("Resetting...");
								client.write("Done\n");

								step = 1;
							}
							else if (answer == 2) { // Edit
								step = 5;
							}
							else if (answer == 3) { // Exit
								// not sure whether I should do the above steps on the spot
								// or in this function below, by passing it the appropriate classes
								// I suppose either is doable

								//generate_character(player); // generate basic character data based on choices

								step = 0;
								answer = 0;

								player.setEditor(Editor.NONE);

								send("Game> Editor Reset", client);

								player.setStatus("OOC");

								send("Game> Status Reset", client);

								send("Exiting...", client);

								return new cgData(-1, -1, -1);
							}
						}
						else if (step == 5) {
							debug("Entering Step " + step);

							switch(answer) {
							case 1:
								client.write("Player Race: " + player.getPlayerRace() + "\n");
								step = 1;
								break;
							case 2:
								client.write("Player Gender: " + player.getGender() + "\n");
								step = 2;
								break;
							case 3:
								client.write("Player Class: " + player.getPClass() + "\n");
								step = 3;
								break;
							default:
								break;
							}
						}
						t = 0;
						debug("T is now " + t);
					}
					catch(NumberFormatException npe) {
						npe.printStackTrace();
					}
				}

				return new cgData(t, step, answer);
			}

			/**
			 * Description Editor
			 * command parser
			 * 
			 * @param tempString
			 * @param client
			 */
			public void op_dedit(final String tempString, final Client client) {
			}

			/**
			 * Help File Editor
			 * command parser
			 * 
			 * @param input
			 * @param client
			 */
			public void op_hedit(final String input, final Client client) {
				/*
				 * need to convert all this to work on help files
				 */

				final Player player = getPlayer(client);

				debug("input: " + input);

				String hcmd = "";
				String harg = "";

				if (input.indexOf(".") == 0)
				{
					if (input.indexOf(" ") != -1) {
						hcmd = input.substring(input.indexOf(".") + 1, input.indexOf(" "));
						harg = input.substring(input.indexOf(" ") + 1, input.length());
					}
					else {
						hcmd = input.substring(input.indexOf(".") + 1, input.length());
					}

					debug("HEDIT CMD");
					debug("hcmd: " + hcmd);

					if ( hcmd.equals("abort") || hcmd.equals("a") )
					{
						player.abortEditing();

						send("< List Aborted. >", client);
						send("< Exiting... >", client);

						player.setEditor(Editor.NONE);
						player.setStatus("OOC");
					}
					else if ( hcmd.equals("del") || hcmd.equals("d") ) {
						final EditList list = player.getEditList();
						if (list != null) {
							final int toDelete = Utils.toInt(harg, -1);
							list.removeLine(toDelete);
							send("< Line " + toDelete + " deleted. >", client);
						}
					}
					else if ( hcmd.equals("help") || hcmd.equals("h") )
					{
						send("<Help Editor Help>", client);
						send(".abort[.a]      - throw out the current help file changes and exit", client);
						send(".del[.d] <#>    - delete the indicated line", client);
						send(".help[.h]       - display this editor's help", client);
						send(".insert[.i] <#> - move input placement to the line specified", client);
						send(".list[.l]       - print out help file w/line numbers", client);
						send(".print[.p]      - print out help file w/o line numbers", client);
						send(".quit[.q]       - save and exit help file", client);
						send(".save[.s]       - save help file", client);
						send(".stat[.st]      - display current status of the help file", client);
						send("< End Help Editor Help >", client);
					}
					else if ( hcmd.equals("insert") || hcmd.equals("i") )
					{
						final EditList list = player.getEditList();
						if (list != null) {
							list.setLineNum(Utils.toInt(harg, 0));
						}
					}
					else if ( hcmd.equals("list") || hcmd.equals("l") )
					{
						final EditList list = player.getEditList();
						if (list != null) {
							int i = 0;
							for (String s : list.getLines())
							{
								System.out.println(i + ": " + s);
								send(i + ": " + s, client);
								i++;
							}
						}
					}
					else if ( hcmd.equals("print") || hcmd.equals("p") )
					{
						final EditList list = player.getEditList();
						if (list != null) {
							for (String s : list.getLines())
							{
								System.out.println(s);
								send(s, client);
							}
						}
					}
					else if ( hcmd.equals("quit") || hcmd.equals("q") )
					{
						// save the help file?
						op_hedit(".save", client);

						send("< Exiting... >", client);

						player.setEditor(Editor.NONE);
						player.setStatus("OOC");
					}

					else if ( hcmd.equals("save") || hcmd.equals("s") )
					{
						final EditList list = player.getEditList();
						if (list != null) {
							// convert the list to a string array
							this.helpMap.put(list.name, list.getLines().toArray(new String[0]));

							send("< Help File Written Out! >", client);
							send("< Help File Saved. >", client);
						}
					}
					else if ( hcmd.equals("stat") || hcmd.equals("st") ) {
						final EditList list = player.getEditList();
						if (list != null) {
							String header = "< Help File: " + list.name + ".txt" + " Line: " + list.getLineNum() + " Lines: " + list.getNumLines() + "  >";
							send(header, client);
						}
					}

					System.out.println(getPlayer(client).getStatus());
				}
				else {
					final EditList list = player.getEditList();
					if (list != null) {
						list.addLine(input);
						debug(list.getLineNum() + ": " + list.getCurrentLine());
					}
				}
			}

			/**
			 * List Editor
			 * 
			 * status: buggy, it doesn't save file to disk, but
			 * does hold onto it transiently
			 * 
			 * NOTE: sort of saves lists, temporarily, but doesn't save to any kind of file.
			 * This has a long way to go before it bears any real resemblance to the
			 * functionality available on TinyMU* or NamelessMUCK.
			 * 
			 * @param tempString
			 * @param client
			 */
			public void op_lsedit(final String input, final Client client)
			{
				final Player player = getPlayer(client);

				debug("input: " + input);

				if (input.indexOf(".") != -1)
				{
					debug("LSEDIT CMD");
					String lcmd = input.substring(input.indexOf(".") + 1, input.length());
					debug("lcmd: " + lcmd);
					debug(lcmd.equals("end"));

					if ( lcmd.equals("quit") )
					{
						// save the help file?
						op_lsedit(".save", client);

						send("< Exiting... >", client);

						player.setEditor(Editor.NONE);
						player.setStatus("OOC");
					}
					else if ( lcmd.equals("help") )
					{
						send("<List Editor Help>", client);
						send(".quit  - save and exit list", client);
						send(".help  - display this help", client);
						send(".save  - save list", client);
						send(".print - print out list w/o line numbers", client);
						send(".list  - print out list w/line numbers", client);
						send(".abort - throw out the current list and exit", client);
						send(".stat  - display current status of the list", client);
						send("< End List Editor Help >", client);
					}
					else if ( lcmd.equals("print") )
					{
						final EditList list = player.getEditList();
						if (list != null) {
							for (String s : list.getLines())
							{
								System.out.println(s);
								send(s, client);
							}
						}
					}
					else if ( lcmd.equals("list") )
					{
						int i = 0;
						final EditList list = player.getEditList();
						if (list != null) {
							for (String s : list.getLines())
							{
								System.out.println(i + ": " + s);
								send(i + ": " + s, client);
								i++;
							}
						}
					}
					else if ( lcmd.equals("save") ) {
						player.saveCurrentEditor();

						send("< List Written Out! >", client);
						send("< List Saved. >", client);
					}
					else if ( lcmd.equals("stat") ) {
						final EditList list = player.getEditList();
						if (list != null) {
							String header = "< List: " + list.name + " Line: " + list.getLineNum() + " Lines: " + list.getNumLines() + " >";
							send(header, client);
						}
					}
					else if ( lcmd.equals("abort") )
					{
						player.abortEditing();

						send("< List Aborted. >", client);
						send("< Exiting... >", client);

						player.setEditor(Editor.NONE);
						player.setStatus("OOC");
					}
					System.out.println(getPlayer(client).getStatus());
				}
				else {
					final EditList list = player.getEditList();
					if (list != null) {
						list.addLine(input);
						debug(list.getNumLines() + ": " + input);
					}
				}
			}

			/* Editors - OLC (OnLine Creation) Tools */

			/**
			 * Room Editor
			 * 
			 * @param input
			 * @param client
			 */
			public void op_roomedit(final String input, final Client client) {
				final Player player = getPlayer(client);

				String rcmd = "";
				String rarg = "";

				edData data = player.getEditorData();

				if (input.indexOf(" ") != -1) {
					rcmd = input.substring(0, input.indexOf(" ")).toLowerCase();
					rarg = input.substring(input.indexOf(" ") + 1, input.length());
				}
				else {
					rcmd = input.substring(0, input.length()).toLowerCase();
				}

				debug("REDIT CMD");
				debug("rcmd: \"" + rcmd + "\"");
				debug("rarg: \"" + rarg + "\"");

				if ( rcmd.equals("abort") ) {
					// clear edit flag
					((Room) data.getObject("room")).Edit_Ok = true;

					// exit
					send("< Exiting... >", client);

					// reset editor and player status
					player.setStatus( (String) data.getObject("pstatus") );
					player.setEditor(Editor.NONE);
				}
				else if ( rcmd.equals("addexit") ) {
					// addexit <name> <destination dbref>
					String[] args = rarg.split(" ");

					// if 
					if (args.length > 1 ) {
						final int destination = Integer.parseInt(args[1]);
						final MUDObject m = objectDB.get(destination);

						if ( m != null ) {
							if ( m instanceof Room ) {
								data.setObject("e|" + args[0], new Exit( args[0], ((Room) data.getObject("room")).getDBRef(), destination ));
								send("Ok.", client);
							}
						}

					}
				}
				else if ( rcmd.equals("desc") ) {
					data.setObject("desc", rarg);
					send("Ok.", client);
				}
				else if ( rcmd.equals("dim") ) {
					String[] args = rarg.split(" ");

					if (args.length > 1 ) {
						try {
							System.out.println("Args: \"" + args[0] + "\" \"" + args[1] + "\"");

							Integer dim = Integer.parseInt(args[1]);

							System.out.println("key: " + args[0] + " value: " + dim);

							if ( args[0].toLowerCase().equals("x") ) {
								data.setObject("x", dim);
								send("Ok.", client);
							}
							else if ( args[0].toLowerCase().equals("y") ) {
								data.setObject("y", dim);
								send("Ok.", client);
							}
							else if ( args[0].toLowerCase().equals("z") ) {
								data.setObject("z", dim);
								send("Ok.", client);
							}
							else { send("Invalid Dimension.", client); }
						}
						catch(NumberFormatException nfe) {
							nfe.printStackTrace();
						}
					}
				}
				else if ( rcmd.equals("done") ) {
					// save changes
					op_roomedit("save", client);

					// clear edit flag
					((Room) data.getObject("room")).Edit_Ok = true;

					// exit
					send("< Exiting... >", client);

					// reset editor and player status
					player.setStatus( (String) data.getObject("pstatus") );
					player.setEditor(Editor.NONE);
				}
				else if ( rcmd.equals("help") ) {
					if ( rarg.equals("") ) {
						// output help information
						send("Room Editor -- Help", client);
						send(Utils.padRight("", '-', 40), client);
						send("abort", client);
						send("desc <new description>", client);
						send("dim <dimension> <size>", client);
						send("done", client);
						send("help", client);
						send("layout", client);
						send("name <new name>", client);
						send("open <exit name> <destination>", client);
						send("save", client);
						send("show", client);
						send(Utils.padRight("", '-', 40), client);
						// test alternate output means
						/*ArrayList<Message> msgs = new ArrayList<Message>(13);
				msgs.add(new Message(client, "Room Editor -- Help"));
				msgs.add(new Message(client, Utils.padRight("", '-', 40)));
				msgs.add(new Message(client, "abort"));
				msgs.add(new Message(client, "desc <new description>"));
				msgs.add(new Message(client, "dim <dimension> <size>"));
				msgs.add(new Message(client, "done"));
				msgs.add(new Message(client, "help"));
				msgs.add(new Message(client, "layout"));
				msgs.add(new Message(client, "name <new name>"));
				msgs.add(new Message(client, "open <exit name> <destination>"));
				msgs.add(new Message(client, "save"));
				msgs.add(new Message(client, "show"));
				msgs.add(new Message(client, Utils.padRight("", '-', 40)));
				addMessages(msgs);*/
					}
					else {
						// output help information specific to the command name given
					}
				}
				else if ( rcmd.equals("layout") ) {
					int width = (Integer) data.getObject("x");
					int length = (Integer) data.getObject("y");

					for (int w = 0; w < width; w++) {
						for (int l = 0; l < length; l++) {
							if (l < length - 1) {
								client.write("|_");
							}
							else {
								client.write("|_|");
							}
						}
						client.write('\n');
					}

					send("Ok.", client);
				}
				else if ( rcmd.equals("name") ) {
					data.setObject("name", rarg);
					send("Ok.", client);
				}
				else if ( rcmd.equals("open") ) {
					send("Command Not Implemented", client);
				}
				else if ( rcmd.equals("save") ) {
					Room room = (Room) data.getObject("room");
					room.setName((String) data.getObject("name"));
					room.setDesc((String) data.getObject("desc"));
					room.x = (Integer) data.getObject("x");
					room.y = (Integer) data.getObject("y");
					room.z = (Integer) data.getObject("z");
					send("Room saved.", client);
				}
				else if ( rcmd.equals("show") ) {
					Room room = (Room) data.getObject("room");

					// will be a little like examine, just here to show changes
					send(Utils.padRight("", '-', 80), client);
					send("DB Reference #: " + room.getDBRef(), client);
					send("Name: " + data.getObject("name"), client);
					send("Dimensions:", client);
					send("    X: " + (Integer) data.getObject("x"), client);
					send("    Y: " + (Integer) data.getObject("y"), client);
					send("    Z: " + (Integer) data.getObject("z"), client);
					send("Description:", client);
					showDesc((String) data.getObject("desc"), 80, client);
					send("Exits:", client);
					for ( String s : data.getObjects().keySet() ) {
						if ( s.contains("e|") ) {
							Exit e = (Exit) data.getObject(s);
							send( e.getName() + "(#" + e.getDBRef() + ") - Source: " + e.getDBRef() + " Dest: " + e.getDest(), client );
						}
					}
					send(Utils.padRight("", '-', 80), client);

				}
				else {
					// currently causes a loop effect, where the command gets funneled back
					// into op_roomedit regardless
					//cmdQueue.add(new CMD(in, client, 0));
				}
			}

			/**
			 * Item Editor
			 * 
			 * @param input
			 * @param client
			 */
			public void op_iedit(final String input, final Client client) {
				final Player player = getPlayer(client);

				String rcmd = "";
				String rarg = "";

				edData data = player.getEditorData();

				if (input.indexOf(" ") != -1) {
					rcmd = input.substring(0, input.indexOf(" ")).toLowerCase();
					rarg = input.substring(input.indexOf(" ") + 1, input.length());
				}
				else {
					rcmd = input.substring(0, input.length()).toLowerCase();
				}

				debug("IEDIT CMD");
				debug("rcmd: \"" + rcmd + "\"");
				debug("rarg: \"" + rarg + "\"");

				if ( rcmd.equals("abort") ) {
					// exit
					send("< Exiting... >", client);

					// reset editor and player status
					player.setStatus( (String) data.getObject("pstatus") );
					player.setEditor(Editor.NONE);
				}
				else if ( rcmd.equals("desc") ) {
					data.setObject("desc", rarg);
					send("Ok.", client);
				}
				else if ( rcmd.equals("done") ) {
					// save changes
					op_iedit("save", client);

					// clear edit flag
					((Item) data.getObject("item")).Edit_Ok = true;

					// reset editor and player status
					player.setStatus( (String) data.getObject("pstatus") );
					player.setEditor(Editor.NONE);

					// clear editor data
					player.setEditorData(null);

					// exit
					send("< Exiting... >", client);
				}
				else if ( rcmd.equals("help") ) {
					send("Item Editor -- Help", client);
					send(Utils.padRight("", '-', 40), client);
					send("abort", client);
					send("desc <new description>", client);
					send("done", client);
					send("help", client);
					send("name <new name>", client);
					send("save", client);
					send("show", client);
					send("type", client);
				}
				else if ( rcmd.equals("name") ) {
					data.setObject("name", rarg);
					send("Ok.", client);
				}
				else if ( rcmd.equals("save") ) {
					final Item item = (Item) data.getObject("item");

					item.setName((String) data.getObject("name"));
					item.setDesc((String) data.getObject("desc"));
					item.setItemType((ItemType) data.getObject("type"));

					if ( (ItemType) data.getObject("type") == ItemType.CLOTHING ) {
						((Clothing) item).clothing = (ClothingType) data.getObject("subtype");
					}

					send("Item saved.", client);
				}
				else if ( rcmd.equals("show") ) {
					final Item item = (Item) data.getObject("item");

					// will be a little like examine, just here to show changes
					send(Utils.padRight("", '-', 80), client);
					//send("----------------------------------------------------", client);
					send("DB Reference #: " + item.getDBRef(), client);
					send("Name: " + data.getObject("name"), client);
					send("Item Type: " + ((ItemType) data.getObject("type")).toString(), client);
					send("Description:", client);
					showDesc((String) data.getObject("desc"), 80, client);
					//send("----------------------------------------------------", client);
					send(Utils.padRight("", '-', 80), client);

				}
				else if ( rcmd.equals("type") ) {
					final int i = Integer.parseInt(rarg);
					data.setObject("type", ItemType.values()[i]);
					//data.setObject("type", ItemType.getType(rarg));
					/*if (rarg.toUpperCase().equals("clothing") ) {
				data.setObject("subtype", ClothingType.NONE);
			}*/
					send("Ok.", client);
				}
				else {
					// currently causes a loop effect, where the command gets funneled back
					// into op_iedit regardless
					//cmdQueue.add(new CMD(input, client, 0));
				}
			}

			/**
			 * The input handler for a Pager, of which each Player has one
			 * that holds the contents of a file (usually help files) they are currently
			 * looking at. A pager offers the ability to scroll up and down through the
			 * file. The internal "commands" for the pager are interpreted here.
			 * 
			 * @param input
			 * @param client
			 */
			public void op_pager(final String input, final Client client) {
				final Player player = getPlayer(client);

				try { 
					Pager pager = player.getPager();

					if (input.equals("up")) {
						for (final String s : pager.scrollUp()) {
							client.write(s + "\r\n");
						}
					}
					else if (input.equals("down")) {
						for (final String s : pager.scrollDown()) {
							client.write(s + "\r\n");
						}
					}
					else if (input.equals("view")) {
						for (final String s : pager.getView()) {
							client.write(s + "\r\n");
						}
					}
					else if (input.equals("done")) {
						System.out.println("Leaving Pager");
						client.write("Leaving Pager");

						player.setPager( null );
						player.setStatus("OOC");
						return;
					}

					int top = pager.getTop();
					int bottom = pager.getBottom();
					int rem = pager.getContent().length - bottom;

					client.write("< lines " + top + "-" + bottom + ", " + rem + " lines remaining>\r\n");
				}
				catch (NullPointerException npe) {
					System.out.println("Pager sub-system: NullPointerException caught");

					System.out.println("Reporting error:");
					npe.printStackTrace();

					System.out.println("Leaving Pager");

					player.setPager( null );
					player.setStatus("OOC");

					return;
				}

			}

			// logged-in player check
			public boolean loginCheck(final Client client) {
				for (final Player p : players) {
					if (p.getClient().equals(client)) {
						return true;
					}
				}
				return false;
			}

			// Object "Retrieval" Functions

			/**
			 * get object specified by name
			 * 
			 * @param objectName
			 * @param client
			 * @return
			 */
			public MUDObject getObject(final String objectName, final Client client) {
				MUDObject object = getExit(objectName);
				if (object != null) return object;

				object = getRoom(objectName);
				if (object != null) return object;

				return getThing(objectName, client);
			}

			/**
			 * get object specified by name
			 * 
			 * @param objectDBRef
			 * @return
			 */
			public MUDObject getObject(final String name) {
				return objectDB.getByName(name);
			}

			/**
			 * get object specified by database reference number/id
			 * 
			 * @param objectDBRef
			 * @return
			 */
			public MUDObject getObject(Integer dbref) {
				return objectDB.get(dbref);
			}

			// these are kind of important for containers, but also for general examine
			public Item getItem(final String name, final Player player) {
				for (final Item item : player.getInventory()) {
					if (item.getName().equals(name)) {
						return item;
					}
				}

				/*for (Item item : this.items) {
			if (item.getName().equals(name)) {
				return item;
			}
		}*/

				return null;
			}

			public Item getItem(final Integer dbref, final Client client) {
				for (final Item item : getPlayer(client).getInventory()) {
					if (item.getDBRef() == dbref) {
						return item;
					}
				}

				/*for (Item item : this.items ) {
			if (item.getDBRef() == dbref) {
				return item;
			}
		}*/

				return null;
			}

			/**
			 * getItem
			 * 
			 * Get an Item by it's dbref number. Will fail (return null) if
			 * the object in the database is not an Item.
			 * 
			 * @param dbref
			 * @return
			 */
			public Item getItem(final Integer dbref) {
				final MUDObject m = getObject(dbref);

				if (m instanceof Item) {
					Item item = (Item) m;
					return item;
				}
				else {
					return null;
				}
			}


			/**
			 * get exit specified by name
			 * 
			 * @param exitName
			 * @param client
			 * @return
			 */
			public Exit getExit(final String exitName) {
				return objectDB.getExit(exitName);
			}

			/*public Exit getExit(String exitName, Client client) {
		Room room = getRoom(client);
		Exit exit;

		ArrayList<Integer> eNums = new ArrayList<Integer>();

		// look through the present room's exits first
		for (int e = 0; e < room.getExits().size(); e++) {
			exit = (Exit) room.getExits().get(e);
			if (exit.getName().toLowerCase().equals(exitName)) {
				return exit;
			}
			else {
				eNums.add(e);
			}
		}

		// look through all the exits (would be great if this could ignore previously searched exits
		// perhaps by dbref (since that's much shorter than holding object references, etc
		for (int e = 0; e < exits1.size(); e++) {
			if (!eNums.contains(e)) {
				exit = (Exit) exits1.get(e);

				if (exit.getName().toLowerCase().equals(exitName)) {
					return exit;
				}
			}
		}

		return null;
	}*/

			public Exit getExit(final Integer dbref, final Client client) {

				// look through the present room's exits first
				for (final Exit e : getRoom(client).getExits()) {
					if (e.getDBRef() == dbref) {
						return e;
					}
				}

				// look through all the exits (would be great if this could ignore previously searched exits
				// perhaps by dbref (since that's much shorter than holding object references, etc
				return objectDB.getExit(dbref);
			}

			/**
			 * Get an NPC (non-player character) object by name.
			 * 
			 * @param name
			 * @return
			 */
			public NPC getNPC(final String name) {
				return objectDB.getNPC(name);
			}

			/**
			 * Get an NPC (non-player character) object by database reference
			 * 
			 * @param name
			 * @return
			 */
			public NPC getNPC(final Integer dbref) {
				return objectDB.getNPC(dbref);
			}

			/**
			 * Get a Player (player character) object by client.
			 * 
			 * @param client
			 * @return
			 */
			public Player getPlayer(Client client)
			{
				debug("Searching for player by client...", 3);
				debug("\"" + client  + "\"", 3);
				
				Player p = sclients.get(client);
				
				if( p != null ) {
					if( p.isController() ) {
						return playerControlMap.getSlave(p);
					}
				}
				
				//return sclients.get(client);
				return p;
			}

			/**
			 * Get a Player (player character) object by name.
			 * 
			 * WARNING: never call before confirming logged in player using loginCheck()
			 * 
			 * @param name
			 * @return
			 */
			public Player getPlayer(final String name) {
				debug("Searching for player by name...");
				debug("\"" + name + "\"", 2);

				for (final Player player : players) {
					if (player.getName().equals(name) || player.getCName().equals(name)) {
						return player;
					}
				}

				return null;
			}

			/**
			 * WARNING: never call before confirming logged in player using loginCheck()
			 * 
			 * @param dbref
			 * @return
			 */
			public Player getPlayer(final Integer dbref) {
				for (final Player player : players) {
					if (player.getDBRef() == dbref) {
						return player;
					}
				}

				return null;
			}

			/**
			 * function to get a room reference for the logged on player's location, if there is a
			 * logged-on player with a location.
			 * 
			 * WARNING: never call before confirming logged in player using loginCheck()
			 * 
			 * @param client
			 * @return
			 */
			public Room getRoom(final Client client)
			{
				Player player = getPlayer(client);

				if (player != null) {
					return getRoom( player.getLocation() );
				}

				return null;
			}

			/**
			 * Get a room by it's name.
			 * 
			 * @param roomName
			 * @return
			 */
			public Room getRoom(final String roomName)
			{
				return objectDB.getRoomByName(roomName);
			}

			/**
			 * Get a room by it's database reference number.
			 * 
			 * @param objDBREF
			 * @return
			 */
			public Room getRoom(final Integer dbref)
			{
				return objectDB.getRoomById(dbref);
			}

			/**
			 * function to get a thing reference for the logged on player, if there is a logged-on player
			 * 
			 * WARNING: never call before confirming logged in player using loginCheck()
			 * 
			 * @param arg
			 * @param client
			 * @return
			 */
			public Thing getThing(String arg, Client client)
			{
				return objectDB.getThing(getRoom(client).getDBRef(), arg);
			}

			// TODO update/fix/remove
			/*public Thing getThing(int DBREF) {
			for(Thing thing : things) {
				if(thing.getDBRef() == DBREF) {
					return thing;
				}

				return null;
			}
		}*/

			/* Saving Objects */

			/*
			 * Persistence Routines
			 */

			/*
			 * Data Saving Functions
			 */

			public void saveAccounts() {
				try {
					for (final Account a : accounts) {
						ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACCOUNT_DIR + a.getUsername() + ".acct"));
						oos.writeObject(a);
						oos.close();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void saveDB() {
				// save databases to disk, modifies 'real' files
				objectDB.save(mainDB);
			}

			public void saveHelpFiles() {
				synchronized (this.helpMap) {
					for (final Entry<String, String[]> he : this.helpMap.entrySet()) {
						Utils.saveStrings(HELP_DIR + he.getKey() + ".txt", he.getValue());
					}
				}
			}

			// Data Loading Functions

			// Account Loading (one account per file) -- TESTING
			public void loadAccounts() {
				try {
					final File dirFile = new File(ACCOUNT_DIR);
					if (dirFile.isFile()) {
						for (final File file : dirFile.listFiles()) {
							if (file.isFile()) {
								ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
								accounts.add((Account) ois.readObject());
								ois.close();
							}
						}
					}
				} catch(Exception ex) {
					ex.printStackTrace();
					System.exit(11);
				}
			}

			public void loadAliases(String filename) {
				// load aliases from file
				debug("Loading aliases");
				for (final String _line : Utils.loadStrings(filename)) {
					final String line = Utils.trim(_line);
					if (line.startsWith("#")) {
						continue;
					}
					final String data = line.substring(line.indexOf(" ") + 1);  // skip "alias " prefix, "command:alias" remains.
					final String[] struct = data.split(":");
					final String command = struct[0];
					for (final String alias : struct[1].split(",")) {   // split by ',' to look for multiple aliases, e.g. "alias1,alias2"
						aliases.put(alias, command);
						debug(alias + " -> " + command);
					}
				}
				debug("Aliases loaded.");
				debug("");
			}

			/**
			 * Go through all the things that exist in the database
			 * and place them in the respective rooms they are located in
			 */
			public void placeThingsInRooms() {
				objectDB.placeThingsInRooms(this);
			}

			/**
			 * Go through all the items that exist in the database
			 * and place them in the respective rooms they are located in
			 */
			public void loadItems() {
				objectDB.addItemsToRooms();

				for (final Entry<Item, Player> entry : objectDB.getItemsHeld().entrySet()) {

					final Item item = entry.getKey();
					final Player npc = entry.getValue();

					debug(item.getDBRef() + " " + item.getName());

					debug(item.getLocation() + " " + npc.getName(), 2);
					debug("Item Loaded", 2);

					if (npc instanceof NPC) {
						if (npc instanceof ArmorMerchant) {
							final ArmorMerchant am = (ArmorMerchant) npc;
							debug("ArmorMerchant (" + am.getName() + ") " + item.getName(), 2);
							am.stock.add(item);
						}
						else if (npc instanceof WeaponMerchant) {
							final WeaponMerchant wm = (WeaponMerchant) npc;
							debug("WeaponMerchant (" + wm.getName() + ") " + item.getName(), 2);
							wm.stock.add(item);
						}
						else {
							debug(npc.getName() + ": Not a merchant", 2);
						}
					}
					else {
						((Player) npc).getInventory().add(item);
					}
				}
			}

			/**
			 * For each npc, every one that is either a WeaponMerchant
			 * or an ArmorMerchant will be stocked with a default set of merchandise
			 * if they have NO stock.
			 */
			public void fillShops() {
				for (final NPC npc : objectDB.getNPCs()) {
					// Weapon Merchants
					if (npc instanceof WeaponMerchant) {
						WeaponMerchant wm = (WeaponMerchant) npc;
						if (wm.stock.size() == 0) { // no merchandise
							wm.stock = createItems(new Weapon(0, Handed.ONE, WeaponType.LONGSWORD, 15), 10);
							System.out.println("Weapon Merchant's (" + wm.getName() + ") store has " + wm.stock.size() + " items.");
							for (final Item item : wm.stock) {
								int l = item.getLocation();
								item.setLocation(wm.getDBRef());
								System.out.println("Item #" + item.getDBRef() + " had Location #" + l + " and is now at location #" + item.getLocation());
							}
						}
					}
					// Armor Merchants
					else if (npc instanceof ArmorMerchant) {
						ArmorMerchant am = (ArmorMerchant) npc;
						if (am.stock.size() == 0) { // no merchandise
							am.stock = createItems(new Armor(0, 0, ArmorType.CHAIN_MAIL), 10);
							System.out.println("Armor Merchant's (" + am.getName() + ") store has " + am.stock.size() + " items.");
							for (final Item item : am.stock) {
								int l = item.getLocation();
								item.setLocation(am.getDBRef());
								System.out.println("Item #" + item.getDBRef() + " had Location #" + l + " and is now at location #" + item.getLocation());
							}
						}
					}
					else if (npc instanceof Innkeeper) {
						Innkeeper ik = (Innkeeper) npc;
						if (ik.stock.size() == 0) { // no merchandise
							ik.stock = createItems(new Book("Arcani Draconus"), 10);
							System.out.println("Innkeeper's (" + ik.getName() + ") store has " + ik.stock.size() + " items.");
							for (final Item item : ik.stock) {
								int l = item.getLocation();
								item.setLocation(ik.getDBRef());
								System.out.println("Item #" + item.getDBRef() + " had Location #" + l + " and is now at location #" + item.getLocation());
							}
						}
					}
				}
			}

			public ArrayList<String> loadListDatabase(String filename) {
				String[] string_array;     // create string array
				ArrayList<String> strings; // create arraylist of strings

				string_array = Utils.loadStrings(filename);

				strings = new ArrayList<String>(string_array.length);

				for (int l = 0; l < string_array.length; l++) {
					// if not commented out
					if (string_array[l].charAt(0) != '#') {
						strings.add(string_array[l]);
					}
					// else
					else {
						debug("-- Skip - Line Commented Out --", 2);
					}
				}

				return strings;
			}

			// for the list editor
			public ArrayList<String> loadList(String filename) {
				String[] string_array;
				ArrayList<String> strings;

				string_array = Utils.loadStrings(filename);

				strings = new ArrayList<String>(string_array.length);

				for (int line = 0; line < string_array.length; line++) {
					strings.add(string_array[line]);
				}

				return strings;
			}

			// for the list editor
			/**
			 * Loads an ArrayList of Strings from a file, one string per line with the given offset. The
			 * offset is the number of lines in the file to skip before loading strings. NOTE: This is used
			 * primarily to load lists for the list editor
			 * 
			 * @param filename the filename to load strings from
			 * @param offset   the number of lines to skip before loading strings (from beginning of file)
			 * @return         a list of strings
			 */
			public List<String> loadList(String filename, int offset) {
				final ArrayList<String> lines = new ArrayList<String>(Arrays.asList(Utils.loadStrings(filename)));
				return lines.size() < offset ? new ArrayList<String>() : lines.subList(offset, lines.size());
			}

			public void loadSpells(final String[] temp) {
				try {
					for (final String line : temp) {
						final String[] args = line.split("#");
						final String tName = args[0];
						final String tCastMsg = args[1];
						final String tType = args[2];
						final ArrayList<Effect> tEffects = new ArrayList<Effect>();
						
						for (final String s : args[3].split(",")) {
							tEffects.add(new Effect(s));
						}
						
						final HashMap<String, Reagent> tReagents = new HashMap<String, Reagent>();
						for (final String reagentName : args[4].split(",")) {
							try {
								tReagents.put(reagentName, new Reagent(reagentName));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
						spells2.put(tName, new Spell("Enchantment", tName, tCastMsg, tType, tEffects, tReagents));
						debug(tName + " " + tCastMsg + " " + tType + tEffects);
					}
				}
				catch(NullPointerException npe) {
					npe.printStackTrace();
				}
			}

			// It would be infinitely better to use JSON or the standard java.util.Properties.
			public void loadTheme(final String themeFile) {
				if (themeFile == null || "".equals(themeFile)) {
					debug("Not loading theme, filename: " + themeFile);
					return;
				}
				debug("Loading theme, filename: " + themeFile);

				theme1 = new Theme();
				String section = "";
				String section1 = "";

				for (final String line : Utils.loadStrings(themeFile)) {
					if ("".equals(line) || line.trim().startsWith("//")) {  // skip blank lines
						continue;
					}
					else if (line.startsWith("[/")) {   // end section
						if (!line.substring(2).equals(section.substring(1))) {
							throw new IllegalStateException("Theme section is " + section + " but ending tag is " + line);
						}
						else {
							section = "";
						}
					}
					else if (line.startsWith("[")) {    // start section
						section = line;
						section1 = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
					}
					else if (line.indexOf(" = ") == -1) {    // start section
						throw new IllegalStateException("Theme line is not section name, but missing \" = \": " + line);
					}
					else {
						final String[] parts = line.split(" = ", 2);
						if ( section1.equals("theme") ) {
							if (parts[0].equals("mud_name") ) {
								theme1.setName(parts[1]);
								debug(line);
							}
							else if ( parts[0].equals("motd_file") ) {
								debug("MOTD File NOT set to " + motd);
							}
						}
						else if ( section1.equals("calendar") ) {
							debug(line);
							if ( parts[0].equals("day") ) {
								theme1.setDay(Utils.toInt(parts[1], 0));
							}
							else if ( parts[0].equals("month") ) {
								theme1.setMonth(Utils.toInt(parts[1], 0));
							}
							else if ( parts[0].equals("year") ) {
								theme1.setYear(Utils.toInt(parts[1], 0));
							}
							else if ( parts[0].equals("season") ) {
								season = Seasons.fromStringLower(parts[1]);
							}
							else if (parts[0].equals("reckon")) {
								reckoning = parts[1];
							}
							else {
								throw new IllegalStateException("Theme calendar section, unknown line: " + line);
							}
						}
						else if (section1.equals("months")) {
							final int monthIndex = Utils.toInt(parts[0], -1) - 1;
							MONTH_NAMES[monthIndex] = parts[1];
							debug("Month " + monthIndex + " set to \"" + parts[1] + "\"");
							System.out.println("Month " + monthIndex + " set to \"" + parts[1] + "\"");
						}
						else if (section1.equals("holidays")) {
							debug(line, 2);
							// day, month = holiday name/day name
							// dateline is day,month part
							// E.g.: 9,21 = Autumn Equinox
							final String[] dateline = parts[0].split(",");
							holidays.put(Utils.trim(parts[1]), new Date(Integer.parseInt(Utils.trim(dateline[0])), Integer.parseInt(Utils.trim(dateline[1]))));
							// multi-day holidays not handled very well at all, only one day recorded for now
							//holidays.put(new Date(Integer.parseInt(trim(dateline[1])), Integer.parseInt(trim(dateline[0]))), trim(line[1]));
						}
						else if (section1.equals("years")) {
							debug(line, 2);
							years.put(Integer.parseInt(Utils.trim(parts[0])), Utils.trim(parts[1]));
						}
					}
				}
				debug("");
				debug("Theme Loaded.");
			}

			/**
			 * Generate an item from it's database representation
			 * 
			 * NOTE: I should be able to use to make a new copy of
			 * a prototyped item stored on disk
			 * 
			 * @param itemData
			 * @return an item object
			 */
			public Item loadItem(String itemData) {
				String[] attr = itemData.split("#");

				Integer oDBRef = 0, oLocation = 0;
				String oName = "", oFlags = "", oDesc = "";

				oDBRef = Integer.parseInt(attr[0]);    // 0 - item database reference number
				oName = attr[1];                       // 1 - item name
				oFlags = attr[2];                      // 2 - item flags
				oDesc = attr[3];                       // 3 - item description
				oLocation = Integer.parseInt(attr[4]); // 4 - item location

				ItemType itemType = ItemType.values()[Integer.parseInt(attr[5])];

				debug("Database Reference Number: " + oDBRef);
				debug("Name: " + oName);
				debug("Flags: " + oFlags);
				debug("Description: " + oDesc);
				debug("Location: " + oLocation);

				Item item = new Item(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);

				item.setItemType(itemType);

				return item;
			}

			public void loadChannels(final String filename) {
				try {
					final FileReader fr = new FileReader(new File(filename));
					final BufferedReader br = new BufferedReader(fr);
					String line;
					while ((line = br.readLine()) != null) { 
						// split line in file
						final String[] cInfo = line.split("#");

						// extract channel information from array of data
						final int channelId = Integer.parseInt(cInfo[0]);
						final String channelName = cInfo[1];
						chan.makeChannel(channelName);

						debug("Channel Added: " + channelName);
					}
					br.close();
					fr.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			/**
			 * MOTD - Message of The Day
			 * 
			 * Returns the messages of the day a string which
			 * converted from a byte array loaded from a file.
			 * 
			 * @return String - the message of the day
			 */
			public String MOTD()
			{
				return new String(Utils.loadBytes(MOTD_DIR + motd));
			}

			/**
			 * On-Connect Properties Evaluation
			 * 
			 * Evaluates scripted properties/attributes on the player
			 * when they connect
			 * 
			 * @param client
			 * @return
			 */
			public void cProps(final Player player)
			{
				LinkedHashMap<String, Object> props;
				// create string array to store results of evaluated props
				String[] results = new String[0];
				// get user properties array
				props = player.getProps();
				// get connection properties from user properties array  
				for (String key : props.keySet()) {
					if (key.contains("_connect")) {
						String prop = (String) props.get(key);

						if (prop != null) {
							int initial = prop.indexOf("/");
							String test = prop.substring(initial, prop.indexOf("/", initial));
							System.out.println(test);
							if ( test.equals("_connect") ) {
								System.out.println("Connect Property Found!");
								send(parse_pgm(prop), player.getClient());
							}
						}
					}
				}
			}

			/**
			 * On-Disconnect Properties Evaluation
			 * 
			 * Evaluates scripted properties/attributes on the player
			 * when they disconnect
			 * 
			 * @param client - a client that corresponds to a player
			 * @return String[]
			 */
			public void dProps(Player player)
			{
				LinkedHashMap<String, Object> props;
				// create string array to store results of evaluated props
				String[] results = new String[0];
				// get user properties array
				props = player.getProps();
				// get disconnection properties from user properties array
				for (String key : props.keySet()) {
					if (key.contains("_connect")) {
						String prop = (String) props.get(key);

						if (prop != null) {
							int initial = prop.indexOf("/");
							String test = prop.substring(initial, prop.indexOf("/", initial));
							System.out.println(test);
							if (test.equals("_disconnect")) {
								System.out.println("Disconnect Property Found!");
								send(parse_pgm(prop), player.getClient());
							}
						}
					}
				}
			}

			/* INIT section */
			/* Connection Handling */

			/**
			 * Initialize Connection
			 * 
			 * takes a player and performs loading operations for them, as well
			 * as logging connections.
			 * 
			 * @param player       the player to initialize/load into the game
			 * @param client       the connecting client
			 * @param newCharacter is this a new character
			 */
			public void init_conn(final Player player, final Client client, final boolean newCharacter)
			{
				// generate generic name for unknown players based on their class and the number of players with the same class presently on
				// logged on of a given class
				debug("Generating generic name for player...");
				debug("Done");

				player.setCName(player.getPClass().toString());

				debug("Number of current connected players that share this player's class: " + objectDB.getNumPlayers(player.getPClass()));

				// NOTE: I should probably add a mapping here somewhere that ties the player to their account, if they have one

				if ( newCharacter ) { // if new, do some setup
					player.setMoney(Coins.platinum(10).add(Coins.gold(50)).add(Coins.silver(50)));

					// give basic equipment (testing purposes)
					final Armor armor = new Armor("Leather Armor", "A brand new set of leather armor, nice and smooth, but a bit stiff still.", -1, -1, 0, ArmorType.LEATHER, ItemType.ARMOR);
					final Weapon sword = new Weapon("Long Sword", "A perfectly ordinary longsword.", 0, Handed.ONE, WeaponType.LONGSWORD, 15.0);

					objectDB.addAsNew(armor);
					objectDB.addAsNew(sword);

					armor.setLocation(player.getDBRef());

					player.getInventory().add(armor);
					player.getInventory().add(sword);
				}

				// get the time
				final Time time = getTime();

				// get variables to log
				String pname = player.getName();
				int location = player.getLocation();
				String loginTime = time.hour() + ":" + time.minute() + ":" + time.second();

				// log their login
				log.writeln(pname, location, "Logged in at " + loginTime + " from " + client.ip()); // log the login

				// open a new session
				Session session = new Session(client, player);
				session.connect = time;

				sessionMap.put(player, session);

				// tell the player that their connection was successful
				debug("Connected!");
				//send(Colors.YELLOW + "Connected!" + Colors.WHITE, client);
				send(colors("Connected!", "yellow"), client);
				//send(Colors.YELLOW + "Connected to " + name + " as " + player.getName() + Colors.WHITE, client);
				send(colors("Connected to " + serverName + " as " + player.getName(), "yellow"), client);

				try {
					player.loadMail(DATA_DIR + "mail\\mail-" + player.getName() + "\\");
				} catch (Exception e) {
					e.printStackTrace();
				}

				// indicate to the player how much mail/unread mail they have
				client.writeln("Checking for unread messages...");

				int messages = player.getMailBox().numUnreadMessages();

				if (messages == 0) {
					client.writeln("You have no unread messages.");
				}
				else {
					client.writeln("You have " + String.valueOf(messages) + " unread messages.");
				}

				// load the player's inventory

				// go through objects array and put references to objects that are located in/on the player in their inventory
				for (final Item item : objectDB.getItemsByLoc(player.getDBRef())) {
					debug("Item -> " + item.getName() + " (#" + item.getDBRef() + ") @" + item.getLocation());
					//player.getInventory().add(item);
					//inventory.add(item);
				}

				/* ChatChannel Setup */
				// add player to the STAFF ChatChannel (testing), if they are staff
				if (player.getAccess() > USER) {
					try {
						addToStaffChannel(player);
					} catch (Exception e) {
						System.out.println("No staff channel exists!!!");
					}
				}

				/* add the player to the game */
				player.setClient(client);
				sclients.put(client, player);
				players.add(player);

				/* run any connect properties specified by the player */
				//cProps(player);

				/* look at the current room */
				final Room current = getRoom(client);   // determine the room they are in
				look(current, client);                  // show the room
				current.addListener(player);
			}

			/**
			 * De-Initialize Connection (Disconnect)
			 * 
			 * @param player
			 * @param client
			 */
			public void initDisconn(final Client client)
			{
				final Player player = getPlayer(client);
				if (player == null) {
					debug("Player not found for client: " + client);
					s.disconnect(client);
					return;
				}

				// break any current control of npcs
				cmd_control("#break", client);

				// remove listener
				getRoom(client).removeListener(player);

				/*
				 * unequip gear
				 * 
				 * If we didn't do this, stuff could get stuck in limbo,
				 * alternatively, we could just loop through the items array
				 * and put a new copy of the references in the inventory,
				 * since it's all going to end up back in the inventory anyway
				 * (or at least until I figure out how to persist information
				 * about reloading slots for a player).
				 */

				// Unequipping gear
				final ArrayList<Item> inventory = player.getInventory();

				for (final Slot slot : player.getSlots().values()) {
					if (slot.isFull()) {
						if (slot.getItem() != null) {
							inventory.add(slot.getItem());
						}
					}
				}

				send("Equipment un-equipped!", client);


				debug("initDisconn(" + client.ip()+ ")");

				// get time
				Time time = getTime();

				// get variables to log
				String playerName = player.getName();
				int playerLoc = player.getLocation();

				// log the disconnect
				log.writeln(playerName, playerLoc, "Logged out at " + time.hour() + ":" + time.minute() + ":" + time.second() + " from " + client.ip());

				// get session
				Session toRemove = sessionMap.get(player);

				// record disconnect time
				toRemove.disconnect = time;

				// store the session info on disk

				// if player is a guest
				if (player.getFlags().contains(ObjectFlag.GUEST)) {
					// remove from database
					objectDB.set( player.getDBRef(), new NullObject( player.getDBRef() ) );  // replace db entry with NULLObjet
				}
				else {
					send("Saving mail...", player.getClient());
					try {
						player.saveMail(DATA_DIR + "mail\\mail-" + player.getName() + "\\");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				synchronized(players) {
					players.remove(player);  // Remove the player object for the disconnecting player
				}

				// DEBUG: Tell us which character was disconnected
				debug(playerName + " removed from play!");
				send("Disconnected from " + serverName + "!", client);
				s.disconnect(client);
			}

			public void telnetNegotiation(Client client) {
				client.tn = true; // mark as client as being negotiated with

				int s = 0;  // current sub-negotiation? (0=incomplete,1=complete)

				ArrayList<String> options = new ArrayList<String>();

				options.add("IAC WILL MCCP");

				// send some telnet negotiation crap
				// IAC WILL MCCP1
				// 255 251  85
				// -- if --
				// IAC DO  MCCP1
				// 255 253 85
				// -- then --
				// IAC SB  MCCP1 WILL SE
				// 255 250 85    251  250
				// -- else --
				// IAC DONT MCCP1
				// 255 254  85
				// -- then --
				// IAC SB  MCCP1 WONT SE/IAC SB COMPRESS WONT SE
				// 255 250 85    252  250

				for (String optstr : options) { // all the things we wish to check? (i.e. we're going to use these if we can

					// send a message
					Telnet.send(optstr, client);

					// deal with reply
					while (s == 0) {

						// a byte buffer to hold the incoming message (hopefully it's less than 10 bytes)
						byte[] byteBuffer1 = new byte[10];

						// capture the response
						// if (client.available() > 0) {
						//      client.readBytes();
						// }

						System.out.println("Response Captured");

						System.out.println("Response:");

						for (byte b : byteBuffer1) {
							System.out.println("Processing...");
							int value = b;
							System.out.println(value);
						}

						// handle the response
						if (byteBuffer[0] == 255) { // if that byte is 255 (IAC - Is A Command)

							for (byte b : byteBuffer1) {
								System.out.println("Processing...");
								int value = b;
								System.out.println(value);
							}

							System.out.println("TELNET NEGOTIATION -- BYTES");

							System.out.println("Response: " + Telnet.translate(byteBuffer1));


							// confirming MCCP1 (if told to do, respond again that I will)
							if (byteBuffer1[0] == (byte) 255 && byteBuffer1[1] == (byte) 253 && byteBuffer1[2] == (byte) 85) {
								client.write(new byte[] { (byte) 255, (byte) 85, (byte) 251, (byte) 250 });
							}
							else if (byteBuffer1[0] == (byte) 255 && byteBuffer1[1] == (byte) 254 && byteBuffer1[2] == (byte) 85) {
								client.write(new byte[] { (byte) 255, (byte) 85, (byte) 252, (byte) 250 });
							}
							else {

							}
							/*if ( Telnet.translate(byteBuffer1).equals("IAC DO MCCP") ) {
						Telnet.send("IAC MCCP WILL SB", client);
					}
					else if ( Telnet.translate(byteBuffer1).equals("IAC DONT MCCP") ) {
						Telnet.send("IAC MCCP WONT SB", client);
					}*/

							s = 1;
						}
					}	
				}
				client.tn = true;
			}

			// EVENT Section
			//

			// event triggered on client connection
			public void clientConnected(final Client someClient)
			{
				send("Connecting from " + someClient.ip(), someClient);
				// decide if a player (or in this case, IP address) will be allowed to continue connecting
				if (banlist.contains(someClient.ip())) {
					send("Server> Your IP is banned.", someClient);
					send("Server> Booting client...", someClient);
					someClient.stopRunning();
				}
				// check to see if ansi colors are enabled for the server
				if ( ansi == 1 ) {
					someClient.write("\033[;1m"); // tell client to use bright version of ANSI Colors
					send("> Using BRIGHT ANSI colors <", someClient); // indicate the use of bright ansi colors to the client
				}
				// MSP (Mud Sound Protocol) Test -- only if msp is on (redundant unless configured otherwise, default: 0 (Off)
				if (msp == 1) {
					MSP.play("intro.wav", "sound");
					String mspMsg = MSP.generate();
					send(mspMsg, someClient);
				}
				// send data about the server

				// black & white
				//send(name + " " + version + " -- Running on " + computer + "(" + ip + ")\n");
				//send(MOTD());

				// colors
				send(colors(program, "yellow") + colors(" " + version, "yellow") + colors(" -- Running on " + computer, "green"), someClient);
				send(colors(serverName, "red"), someClient);
				// send the MOTD to the client in cyan
				send(colors(MOTD(),"cyan"), someClient);
				// reset color
				//send(colors("Color Reset to Default!", "white"));
				send("Mode: " + mode, someClient);
			}

			/**
			 * Colors
			 * 
			 * Takes a string and a color and wraps the string in the ansi escape
			 * code sequences for the color specified and white (to reset back to default).
			 * 
			 * @param arg
			 * @param cc
			 * @return
			 */
			public String colors(String arg, String cc)
			{
				if ( ansi == 1 ) {
					// ex. \33[5m;Test String\33[0m;
					return colors.get(cc) + arg + colors.get("white");
				}
				else {
					return arg;
				}
			}

			public String colorCode(String cc) {
				if ( ansi == 1 ) {
					return colors.get(cc);
				}
				else if (ansi == 0 && xterm == 1) {
					return "";
				}
				else {
					return "";
				}
			}

			// check to see that the chosen player name, conforms to the naming rules
			// NOTE: no naming rules exists nor any method for loading or checking against external ones
			public boolean validateName(String testName)
			{
				boolean nameIsValid = true;

				Pattern validName = Pattern.compile("^\\D*$"); // all aphabetical characters, no numbers

				Matcher isValid = validName.matcher(testName);

				nameIsValid = isValid.matches();

				// test for forbidden names (simple check -- only matches on identical names)
				// I really should use some pattern recognition here...
				if ( forbiddenNames.contains(testName) ) {
					nameIsValid = false;
				}

				debug(nameIsValid);

				return nameIsValid;
			}

			// System (sys) Functions

			//reload system help
			//public void sys_help_reload() throws NullPointerException
			public void help_reload()
			{
				// load helpfiles (basically a duplication of the normal helpfile loading)
				this.help = Utils.loadStrings(HELP_DIR + "index.txt");       // load the index (list of files named the same as the commands
				try {
					for (final String helpFileName : help)
					{
						String helpLines[] = Utils.loadStrings(HELP_DIR + helpFileName);
						this.helpMap.put(helpLines[0], helpLines);
					}
					//System.out.println("Finished");
				}
				catch(NullPointerException npe) {
					System.out.println("NullPointerException in helpfile loading.");
					npe.printStackTrace();
				}
			}

			public String backup() {
				// tell us that the database is being backed up (supply custom message?)
				log.writeln("Game> Backing up Database!");

				// Rooms
				log.writeln("Game> Backing up Rooms...");

				log.writeln("Done.");

				// Exits
				log.writeln("Game> Backing up Exits...");

				log.writeln("Done.");

				// Players
				log.writeln("Game> Backing up Players...");

				log.writeln("Game> Backing up Non-Player Characters (NPCs)...");

				// TODO fix/remove
				// obsoleted or reworking needed?
				/*
		synchronized(npcs1) {
			saveNPCs(); // NOTE: only modifies in memory storage
		}*/
				log.writeln("Done.");

				// Accounts
				log.writeln("Game> Backing up Accounts...");

				synchronized(accounts) {
					saveAccounts();
				}

				log.writeln("Done.");

				// Things
				log.writeln("Game> Backing up Things...");

				log.writeln("Done.");

				// Items
				log.writeln("Game> Backing up Items...");

				log.writeln("Done.");

				// Database
				log.writeln("Game> Backing up Database...");

				saveDB(); // NOTE: real file modification occurs here

				log.writeln("Done.");

				log.writeln("Game> Backing up Help Files...");

				saveHelpFiles();

				log.writeln("Done.");

				// tell us that backing up is done (supply custom message?)
				log.writeln("Database Backup - Done.");

				return "Game> Finished backing up.";
			}

			// very broken, produces nulls, effectively destroying the database
			public void backup2() {
				objectDB.save(mainDB);
				send("Done");
			}

			// non-existent player "flush" function
			public void flush()
			{
				for (final Player player : players) {
					final Player slave = playerControlMap.getSlave(player);
					if (slave == null) {
						//players.remove(player);
						//debug("Player removed.");
						return;
					}
					else {
						debug("Player \"idle\", but controlling an npc.");
					}
				}
			}

			private void shutdown() {
				s.write("Server Shutdown!\n");

				mode = GameMode.MAINTENANCE;    // prevent any new connections

				// disconnect any connected clients
				for (final Client client1 : s.getClients()) {
					initDisconn(client1);
				}

				System.out.print("Stopping main game...");
				// indicate that the server is no longer running, should cause the main loop to exit
				running = false;

				// close the logs (closes the file object and saves the data to a file)
				if ( logging ) {
					System.out.print("Closing logs... ");
					log.closeLog();
					debugLog.closeLog();
					chatLog.closeLog();

					System.out.println("Done");
				}
				else {
					System.out.println("Logging not enabled.");
				}

				System.out.print("Stopping server... ");
				s.stopRunning();

				System.out.print("Running backup... ");
				backup();

				System.out.println("Done");
				System.exit(0);
			}

			/**
			 * Error Handler
			 * 
			 * Generate an error message, containing the name of the function or
			 * part of the program where the error originated. Use the errorCode
			 * to get a generic error message from the default list.
			 * 
			 * @param funcName  name of the function where this is being called
			 * @param errorCode the index in the message list of the particular error message desired
			 * @return an error messag string
			 */
			public String gameError(String funcName, int errorCode)
			{
				String errorString = Errors.get(errorCode);

				if (errorString == null || errorString.length() == 0)
				{
					errorString = "unknown error";
				}

				return "Game> Error ( " + funcName + " ): " + errorString;
			}

			/**
			 * Send
			 * 
			 * wraps any cases of a println and and a server write into one function, also
			 * makes it easy to disable printing to standard out for most debugging and
			 * status messages
			 * 
			 * overloaded version of the function that takes only strings, instead of any
			 * kind of object
			 * 
			 * @param data
			 */
			public void send(String data)
			{	
				if (telnet == 0) // no telnet
				{
					s.write(data + "\r\n");
				}
				if (telnet == 1 || telnet == 2) {
					// telnet and mud clients
					for (int c = 0; c < data.length(); c++)
					{
						s.write(data.charAt(c));
					}
					s.write("\r\n");
				}
			}

			public void send(Object data) {
				send("" + data);
			}

			/**
			 * Send w/client specified
			 * 
			 * a wrapper function for writing directly to a client, that takes an object and converts it to
			 * a string and passes it to an overloaded copy of itself that simply takes a string
			 * 
			 * @param data
			 */
			public void send(Object data, Client client) {
				send("" + data, client);
			}

			/**
			 * Send
			 * 
			 * newish version of send w/client that tries to adhere to line limits and
			 * handle both telnet clients and non-telnet clientss
			 * 
			 * @param data
			 * @param client
			 */
			public void send(final String data, final Client client)
			{
				if (client.isRunning()) {

					String newData = data;
					int lineLimit = 80;


					if ( loginCheck(client) ) {
						lineLimit = getPlayer(client).getLineLimit();
					}

					// if the data to be sent exceeds the line limit
					/*if (data.length() > lineLimit) {
				newData = newData.substring(0, lineLimit - 2); // choose a chunk of data that does not exceed the limit
			}*/


					if (telnet == 0) // no telnet
					{
						client.write(data + "\r\n");
					}
					else if (telnet == 1 || telnet == 2) {
						// telnet and mud clients
						for (int c = 0; c < data.length(); c++)
						{
							client.write(data.charAt(c));
						}
						client.write("\r\n");
					}

					/*if (data.length() > lineLimit) {
				send(data.substring(lineLimit - 2, data.length()), client); // recursively call the function with the remaining data
			}*/
				}
				else {
					System.out.println("Error: Client is inactive (maybe disconnected), message not sent");
					System.out.println(data);
				}
			}

			/**
			 * A wrapper function for System.out.println that can be "disabled" by setting an integer.
			 * Used to turn "on"/"off" printing debug messages to the console.
			 * 
			 * Uses an Object parameter and a call to toString so that I can pass objects to it
			 * 
			 * @param data
			 */
			public void debug(final Object data, final int tDebugLevel)
			{
				if (debug == 1) // debug enabled
				{
					// debug level 3 includes levels 3, 2, 1
					// debug level 2 includes levels 2, 1
					// debug level 1 includes levels 1
					if (debugLevel >= tDebugLevel) {
						System.out.println(data);
						if ( logging ) {
							debugLog.writeln("" + data);
						}
					}
				}
			}

			/**
			 * A wrapper function for the primary debug function that ensures
			 * that I can send debugging information without a specific debugLevel
			 * and it will have a debugLevel of 1.
			 * 
			 * @param data
			 */
			public void debug(final Object data)
			{
				debug(data, 1);
			}

			/**
			 * Game Time
			 * 
			 * @return a string containing a description of the time of day.
			 */
			public String gameTime() {
				String output;
				final TimeOfDay tod = game_time.getTimeOfDay();
				if (!game_time.isDaytime()) {
					output = "It is " + tod.timeOfDay + ", the " + game_time.getMoonPhase() + " " + game_time.getCelestialBody() + " is " + tod.bodyLoc + ".";
				}
				else {
					output = "It is " + tod.timeOfDay + ", the " + game_time.getCelestialBody() + " is " + tod.bodyLoc + ".";
				}
				return output;
			}

			/**
			 * Game Date
			 *  
			 * @return a string containing information about what in-game day it is
			 */
			//"st", "nd", "rd", "th"
			public String gameDate() {
				//return <general time of year> - <numerical day> day of <month>, <year> <reckoning> - <year name, if any>
				month_name = MONTH_NAMES[month - 1];
				year_name = years.get(year);

				String holiday = "";

				for (Map.Entry<String, Date> me : holidays.entrySet()) {
					final Date d = me.getValue();
					if (d.getDay() == day && d.getMonth() == month) {
						holiday = me.getKey();
					}
				}

				if (day > 0 && day <= 4) {
					return season.getName() + " - " + day + suffix[day - 1] + " day of " + month_name + ", " + year + " " + reckoning + " - " + year_name + " (" + holiday + ")";
				}
				else if (day != 11 && ((day % 10) > 0 && (day % 10) <= 4)){
					return season.getName() + " - " + day + suffix[(day % 10) - 1] + " day of " + month_name + ", " + year + " " + reckoning + " - " + year_name + " (" + holiday + ")";
				}
				else {
					return season.getName() + " - " + day + suffix[3] + " day of " + month_name + ", " + year + " " + reckoning + " - " + year_name + " (" + holiday + ")";
				}
			}

			/**
			 * Broadcast
			 * 
			 * @param message
			 * @param tRoom
			 */
			public void broadcast(String message, Room tRoom) {
				Player player;

				for (int p = 0; p < players.size(); p++) {
					player = players.get(p);
					if (player instanceof Player && player.getLocation() == tRoom.getDBRef()) {
						Message msg = new Message(Utils.trim(message), player);
						addMessage(msg);
					}
				}
			}

			public static void exit() {
				System.exit(0);
			}

			/**
                                broadcast("", r);
                                debug("message sent");
			 * Mode setting for players to indicate a state.
			 * 
			 * Normal - normal play
			 * Combat - combat (entered when in combat, game behaves a little differently)
			 * PVP    - player vs. player (when you are somewhere when you can 
			 * PK     - player kill (a mode where you are allowed to kill other players)
			 *
			 * 
			 * @see NOTE: I may need to revise definitions or change this, because
			 * I technically want player killing to always be a possiblity.
			 * 
			 * 
			 * @author Jeremy
			 *
			 */
			public static enum PlayerMode { NORMAL, COMBAT, PVP, PK };

			/*public static enum Telnet {
		SE((byte) 240),
		NOP((byte) 241),
		DM((byte) 242),
		BRK((byte) 243),
		IP((byte) 244),
		AO((byte) 245),
		AYT((byte) 246),
		EC((byte) 247),
		EL((byte) 248),
		GA((byte) 249),
		SB((byte) 250),
		WILL((byte) 251),
		WONT((byte) 252),
		DO((byte) 253),
		DONT((byte) 254),
		IAC((byte) 255);

		public Byte b;

		Telnet(Byte b) {
			this.b = b;
		}

		public Byte toByte() {
			return this.b;
		}
	}*/

			public void pulse(Client client) {
				debug("-- pulse --");
				client.write("-- pulse --" + "\n");
			}

			public double calculateWeight( Player player ) {
				double weight = 0.0;

				for (Item item : player.getInventory())
				{
					// if it's a container, ask it how heavy it is
					// (this is calculated by the bag -- hence bags of holding)
					if (item != null) {
						weight += item.getWeight();
					}
				}

				// in order to set weight of a coin I need to establish values and relative values
				// i.e. 100 copper = 1 silver, 100 silver = 1 gold, 100 gold = 1 platinum
				// need to establish density of each material, and then calculate weight
				// weight of coin: amount of material (g) -> amount of material (oz).
				// copper coin = 4.5g
				// silver coin = 4.5g
				// gold coin = 4.5g
				// platinum coin = 4.5g
				//send(gramsToOunces(4.5));
				final int[] coins = player.getMoney().toArray();
				weight += coins[0] * ( 0.0625 );  // copper (1/16 oz./coin)
				weight += coins[1] * ( 0.1250 );  // silver (1/8 oz./coin)
				weight += coins[2] * ( 0.2500 );  // gold (1/4 oz./coin)
				weight += coins[3] * ( 0.5000 );  // platinum (1/2 oz./coin)
				return weight;
			}

			public double gramsToOunces(double grams) {
				// 1 gram = 0.0352739619 ounces
				return grams * 0.0352739619;
			}

			/* Spells specific server methods */
			/**
			 * Get spell object given the name of the spell
			 * 
			 * @param name
			 * @return
			 */
			public Spell getSpell(final String name) {
				return spells2.get(name);
			}

			/**
			 * Generate a new instance of a particular room for a specified group of players
			 * - the input syntax needs help since I won't be specifying players individually
			 * in an arguments list and I need to take in a set of rooms/zone to make an instance
			 * of. A single instance of a room doesn't make any since?
			 * 
			 * NOTE: only a non-instance can be used as a template
			 * 
			 * @param template
			 * @param group
			 * @return
			 */ 
			public Room new_instance(Room template, Player...group) {
				if (template != null && template.getInstanceId() == -1)
				{
					Room newRoom = new Room(template);
					return newRoom;
				}
				else {
					debug("Invalid Template Room: Null or Not a Parent");
					return null;
				}
			}

			/**
			 * Figure out if the player has a specific container
			 * for this item.
			 * 
			 * @param player
			 * @param newItem
			 * @return
			 */
			private boolean hasGenericStorageContainer(final Player player, final Item newItem) {
				return false;
			}

			/**
			 * Get the specific container for this item if the player has one
			 * 
			 * @param player
			 * @param item
			 * @return
			 */
			private Container<?> getGenericStorageContainer(final Player player, final Item item) {
				return null;
			}

			// AI routines
			/*
	// Line of Sight
	protected void lineOfSight(Point origin, Player target) {
		Point goal = target.getCoordinates();

		int x_pos = origin.getX(); // get origin X coord
		int y_pos = origin.getY(); // get origin Y coord

		int d_x_pos = goal.getX(); // get goal X coord
		int d_y_pos = goal.getY(); // get goal Y coord

		while (x_pos < d_x_pos) {
			while (y_pos < d_y_pos) {
				// find out if there's anything at that intersection of x and y
				// increment y
			}
		}
	}

	protected void lineOfSight(Point origin, Point goal) {
	}


	// Random Movement
	protected void randomMovement() {
	}
			 */

			/**
			 * Display the account menu for a specific account to the client specified
			 * 
			 * NOTE: This is ahead of the implementation, there isn't yet an account system, but
			 * this code was written in preparation of the possible implementation of such a feature.
			 * 
			 * NOTE1: The design/layout for this is borrowed from the login process on TorilMUD.
			 * (torilmud.com)
			 * 
			 * @param account
			 * @param client
			 */
			public void account_menu(final Account account, final Client client) {
				if (account != null) {
					// not the place for the below, since it relates to before player connection
					// in fact, init_conn will need modification if it expects to handle accounts instead of players
					String divider = "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";

					send("Characters:", client);

					send(colors(divider, "green"), client);

					ArrayList<Player> characters = account.getCharacters();

					// for characters in account
					for (int p = 0; p < characters.size(); p++) {
						send(p + ") " + characters.get(p).getName(), client);
					}

					send(colors(divider, "green"), client);

					send("Type the # or name of a character above to login or choose an action below.", client);

					send(colors(divider, "green"), client);

					send("(N)ew character     | (L)ink character   | (U)nlink character | (R)eorder", client);
					send("(E)nter description | (D)elete character | (C)hange password  | (Q)uit", client);

					send(colors(divider, "green"), client);
				}
				else {
					send("Invalid Account!", client);
				}
			}

			/**
			 * allows adding to the message queue from external packages, classes
			 * 
			 * @param newMessage
			 */
			public void addMessage(final Message msg) {
				// if this client's player is the intended recipient
				final Player recip = msg.getRecipient();
				// no recipient, so we'll assume it was 'said' out loud
				if (msg.getSender() == null) {
					send(msg.getMessage());
				}
				else if (players.contains(recip)) {
					send(msg.getMessage(), recip.getClient());
					msg.markSent();
					debug("addMessage, sent message");
				}
				else {
					for (final Player bystander : players) {
						if (bystander.getLocation() == msg.getLocation())
						{
							send(msg.getSender().getName() + " says, \"" + msg.getMessage() + "\".", bystander.getClient());
						}
					}
				}
				msg.markSent();
				debug("sent message");
			}

			/**
			 * allows adding to the message queue from external packages, classes
			 * this version does this for multiple messages grouped together
			 * 
			 * @param newMessage
			 */
			public void addMessages(final ArrayList<Message> newMessages) {
				for (final Message m : newMessages) {
					addMessage(m);
				}
			}

			/*public void write(String data, Client client) {
		client.write(data);
	}*/

			/**
			 * Examine (MUDObject)
			 * 
			 * @param m
			 * @param client
			 */
			public void examine(final MUDObject m, final Client client) {
				if ( !(m instanceof NullObject) ) {
					send(m.getName() + "(#" + m.getDBRef() + ")", client);
					debug("Flags: " + m.getFlags());
					debug(ObjectFlag.firstInit(m.getFlags()));
					send("Type: " + ObjectFlag.firstInit(m.getFlags()) + " Flags: " + ObjectFlag.toInitString(m.getFlags()), client);
					if (m instanceof Item) {
						send("Item Type: " + ((Item) m).item_type.toString(), client);
					}
					send("Description: " + m.getDesc(), client);
					send("Location: " + getObject(m.getLocation()).getName() + "(#" + m.getLocation() + ")", client);
				}
				else {
					send("-- NullObject -- (#" + m.getDBRef() + ")", client);
				}
			}

			/**
			 * Examine (MUDObject -> Room)
			 * 
			 * @param room
			 * @param client
			 */
			public void examine(final Room room, final Client client) {
				send(room.getName() + "(#" + room.getDBRef() + ")", client);
				send("Type: " + ObjectFlag.firstInit(room.getFlags()) + " Flags: " + ObjectFlag.toInitString(room.getFlags()), client);
				send("Description: " + room.getDesc(), client);
				send("Location: " + getRoom(room.getLocation()).getName() + "(#" + room.getLocation() + ")", client);

				send("Sub-Rooms:", client);
				for (final Room r : objectDB.getRoomsByLocation(room.getDBRef())) {
					send(r.getName() + "(#" + r.getDBRef() + ")", client);
				}

				send("Contents:", client);
				final List<Thing> roomThings = objectDB.getThingsForRoom(room.getDBRef());
				for (final Thing t : roomThings) {
					send( colors(t.getName(), "yellow") + "(#" + t.getDBRef() + ")", client);
				}
				send("Creatures:", client);
				for (final Creature creep : objectDB.getCreatureByRoom(room.getDBRef())) {
					send( colors( creep.getName(), "cyan" ), client );
				}
			}

			/**
			 * Examine (MUDObject -> Player)
			 * 
			 * @param player
			 * @param client
			 */
			public void examine(final Player player, final Client client) {
				send(player.name + "(#" + player.getDBRef() + ")", client);
				send("Type: " + ObjectFlag.firstInit(player.getFlags()) + " Flags: " + ObjectFlag.toInitString(player.getFlags()), client);
				send("Description: " + player.getDesc(), client);
				send("Location: " + getRoom(player.getLocation()).getName() + "(#" + player.getLocation() + ")", client);

				// helmet
				// necklace
				// armor
				// cloak
				// rings
				// gloves
				// weapons
				// belt
				// boots

				debug("RING1: " + player.getSlots().get("ring1").getItem() +
						"\t" + "RING2: " + player.getSlots().get("ring2").getItem());
				debug("RING3: " + player.getSlots().get("ring3").getItem() +
						"\t" + "RING4: " + player.getSlots().get("ring4").getItem());
				debug("RING5: " + player.getSlots().get("ring5").getItem() +
						"\t" + "RING6: " + player.getSlots().get("ring6").getItem());
				debug("RING7: " + player.getSlots().get("ring7").getItem() +
						"\t" + "RING8: " + player.getSlots().get("ring8").getItem());
				debug("RING9: " + player.getSlots().get("ring9").getItem() +
						"\t" + "RING10: " + player.getSlots().get("ring10").getItem());

				for (Slot slot : player.getSlots().values()) {
					String tmp;

					if (slot.getType() == ItemType.CLOTHING) {
						tmp = slot.getCType().toString().toUpperCase();
					}
					else {
						tmp = slot.getType().toString().toUpperCase();
					}

					if (slot.getItem() != null) {
						send(colors(tmp, displayColors.get("thing")) + " : " + slot.getItem() + " *" + slot.getItem().getWeight() + "lbs.", client);
					}
					else
					{
						send(colors(tmp, displayColors.get("thing")) + " : null", client);
					}
				}
			}

			public void examine(final Exit exit, final Client client) {
				send(exit.getName() + "(#" + exit.getDBRef() + ")", client);
				send("Type: " + ObjectFlag.firstInit(exit.getFlags()) + " Flags: " + ObjectFlag.toInitString(exit.getFlags()), client);
				send(" Exit Type: " + exit.getExitType().getName(), client);
				send("Description: " + exit.getDesc(), client);
			}

			public void examine(Thing thing, Client client) {
				send(thing.name + "(#" + thing.getDBRef() + ")", client);
				send("Type: " + ObjectFlag.firstInit(thing.getFlags()) + " Flags: " + ObjectFlag.toInitString(thing.getFlags()), client);
				send("Description: " + thing.getDesc(), client);
				send("Coordinates:", client);
				send("X: " + thing.getXCoord(), client);
				send("Y: " + thing.getYCoord(), client);
				send("Z: " + thing.getZCoord(), client);
			}

			/**
			 * Look (MUDObject)
			 * 
			 * Look at any MUDObject (basically anything), this is a stopgap to prevent anything being
			 * unlookable.
			 * 
			 * @param mo
			 * @param client
			 */
			public void look(final MUDObject mo, final Client client) {
				send(mo.getName() + " (#" + mo.getDBRef() + ")", client);
				send(mo.getDesc(),  client);
			}

			/**
			 * Look (MUDObject -> Player)
			 * 
			 * Look at a player, should show a description (based on what they're wearing and what of them is visible.
			 * 
			 * NOTE: I shouldn't be able to see the dagger or swords that are hidden under a cloak
			 * 
			 * @param player player to look at
			 * @param client caller's client
			 */
			public void look(final Player player, final Client client) {
				send(colors(player.getName() + " (#" + player.getDBRef() + ")", (String) displayColors.get("player")), client);
				send(player.getDesc(), client);
				send("Wearing (visible): ", client);

				for (Entry<String, Slot> e : player.getSlots().entrySet()) {
					if (e.getValue() != null) {
						//send(e.getKey() + ": " + e.getValue().getType() + ", ", client);
						send(e.getValue().getType() + "(" + e.getKey() + ")" + ", ", client);
					}
				}
			}

			/**
			 * Look (MUDObject -> Room)
			 * 
			 * Look at a room and, hopefully ,show only what the player(client) can see.
			 * 
			 * @param room   the room to look at
			 * @param client the player that's looking/their client
			 */
			public void look(final Room room, final Client client) {
				Player current = getPlayer(client);

				if (room == null) {
					send("Game> Invalid Room?", client);
					return;
				}

				if (!room.getFlags().contains("S")) {
					send(colors(room.getName() + " (#" + room.getDBRef() + ")", (String) displayColors.get("room")), client);
				}
				else {
					send(colors(room.getName(), (String) displayColors.get("room")), client);
				}

				/* Start Description */

				/*
				 * Make the description conform to a column limit
				 */
				int line_limit = current.getLineLimit();

				send(Utils.padRight("", '-', line_limit), client);

				send("", client);

				String description = parse(room.getDesc(), room.timeOfDay);

				showDesc(description, line_limit, client);

				send("", client);

				/* presumably some sort of config would allow you to disable date and time reporting here,
				 * maybe even turn off the weather data
				 */
				if ( room.getRoomType().equals(RoomType.OUTSIDE) ) {
					final Weather weather = room.getWeather();

					//send("*** " + "<weather>: " + parse(room.getWeather().ws.description, room.timeOfDay), client);
					send("*** " + weather.ws.name + ": " + weather.ws.description, client);

					send("", client);

					send(gameTime(), client); // the in-game time of day

					send("", client);
				}

				//send(gameDate(), client); // the actual date of the in-game year
				//send("", client);

				send(Utils.padRight("", '-', line_limit), client);

				/* End Description */

				/*
				 * need to fix this code up, so that rooms whose coordinates, other location
				 * markers are null will always show up in the list but those with specific coordinates
				 * will not show up unless you can "see" them or are in the same square
				 * 
				 * part of the problem is the exitNames variable, I need it to somehow
				 * retain all of the exit names for the room ( a cached version if you will )
				 * as long as the number of exits don't change. However, I also only
				 * want to show exits whose location is the same as mine or which don't have a
				 * specific location (i.e. you should be able to reach it no matter what if
				 * you can traverse the room safely -- hence it's okay to list it; exits such
				 * as portals/secret doors which could be absent, obscured, etc might not always show up)
				 */
				final String exitNames = room.getExitNames();
				if (exitNames != null && !exitNames.equals("")) {
					send(colors("Exits: " + exitNames, displayColors.get("exit")), client);
				}
				else {
					send(colors("Exits:", displayColors.get("exit")), client);
				}

				send("Contents:", client);

				if (room.contents.size() > 0)
				{
					for (final Thing thing : room.contents)
					{
						if (!thing.getFlags().contains("D")) { // only shown non-Dark things
							if (!room.getFlags().contains("S")) {
								send(colors(thing.getName() + "(#" + thing.getDBRef() + ")", "yellow"), client);
							}
							else {
								send(colors(thing.getName(), "yellow"), client);
							}
						}
					}
				}

				if (room.contents1.size() > 0)
				{
					for (final Item item : room.contents1)
					{
						if (!room.getFlags().contains("S")) {
							send(colors(item.getName() + "(#" + item.getDBRef() + ")", "yellow"), client);
						}
						else {
							send(colors(item.getName(), "yellow"), client);
						}
					}
				}

				send("With:", client);

				for (final NPC npc : objectDB.getNPCsByRoom(room.getDBRef())) {
					if (!room.getFlags().contains("S")) {
						send(colors("[" + npc.getStatus() + "] "+ npc.getName() + "(#" + npc.getDBRef() + ")", "cyan"), client);
					}
					else {
						send(colors("[" + npc.getStatus() + "] "+ npc.getName(), "cyan"), client);
					}
				}

				for (final Creature creep : objectDB.getCreatureByRoom(room.getDBRef())) {
					if (!room.getFlags().contains("S")) {
						send( colors( creep.getName() + "(#" + creep.getDBRef() + ")", "cyan" ), client );
					}
					else {
						send( colors( creep.getName(), "cyan" ), client );
					}
				}

				for (final Player player : players)
				{
					if (player.getLocation() == room.getDBRef())
					{
						if (!player.hasEffect("invisibility")) { // if player is not invisible
							boolean sdesc = false; // short descriptions (true=yes,false=no)
							if ( sdesc ) { // if using short descriptions
								send( evaluate( current, player ), client );
							}
							else { // otherwise
								if (current.getNames().contains(player.getName()) || current.getName().equals(player.getName())) {
									send(colors("[" + player.getStatus() + "] "+ player.getName(), "magenta"), client);
								}
								else {
									send(colors("[" + player.getStatus() + "] "+ player.getCName(), "magenta"), client);
								}
							}
						}
					}
				}

				final ArrayList<Portal> tempPortals = new ArrayList<Portal>(5);

				for (final Portal portal : portals) {
					final boolean playerAtPortal = portal.coord.getX() == current.coord.getX() && portal.coord.getY() == current.coord.getY();
					if (playerAtPortal && (portal.getOrigin() == room.getDBRef() || portal.getDestination() == room.getDBRef())) {
						tempPortals.add(portal);
					}
				}

				if (tempPortals.size() == 1)        send("There is a portal here.", client);
				else if (tempPortals.size() > 1)    send("There are several portals here.", client);
			}

			/**
			 * parse
			 * 
			 * recursive description parser, needs to handle nested conditional
			 * 
			 * NOTE: re-implement with recursion
			 * 
			 * @param toParse
			 * @return
			 */
			public String parse(final String toParse) {

				int index = 0;

				final String input = toParse;
				String output = "";

				final String work = input;

				debug(input, 2);
				debug("Length (input): " + input.length(), 2);
				debug(input.contains("{"), 2);

				int begin = input.indexOf("{", index); // find the beginning of the markup
				debug("Begin Markup: " + begin, 2);
				int mid = input.indexOf("?", index);   // find the middle of the markup
				debug("Middle Markup: " + mid, 2);
				int end = input.indexOf("}", index);   // find the end of the markup
				debug("End Markup: " + end, 2);

				if (begin != -1 && mid != -1 && end != -1) { // if there is an evaluable statement inside of the current space
					return parse(work.substring(0, begin) + parse(work.substring(begin, end)) + work.substring(end, work.length())); // return the encapsulating
				}
				else { // evaluate the current space
					return "";
				}
			}

			/**
			 * pre: the stuff before any parsing
			 * parsed content: the stuff that's been parsed
			 * post: the stuff after any parsing
			 * 
			 * ex: A long-abandoned tower dominates the sky here{RAIN?, its polished-brick facade slick with fallen rain}
			 * {CLEAR?{DAY?, gleaming in the sunlight}{NIGHT?, pale moonlight casting it in an unnatural glow}}.
			 * 
			 * pre: A long-abandoned tower dominates the sky here
			 * parsed content (NIGHT, CLEAR): , pale moonlight casting it in an unnatural glow
			 * post: .
			 * 
			 * result: A long-abandoned tower dominates the sky here, pale moonlight casting it in an unnatural glow.
			 */

			/**
			 * parse
			 * 
			 * Parse descriptions, and evaluate internal statements based on some parameters
			 * 
			 * i.e. evaluation {DAY? sunbeams cascade in through the hole in the ceiling}{NIGHT? moonlight falls gently across the stone floor}
			 * 
			 * if CtimeOfDay was day, then you'd get "sunbeams cascade in through the hole in the ceiling", otherwise this,
			 * "moonlight falls gently across the stone floor"
			 * 
			 * NOTE: non-recursive
			 * 
			 * @param toParse    the description string to parse
			 * @param CtimeOfDay the current time of day
			 * @return
			 */
			public String parse(final String toParse, final String CtimeOfDay) {
				debug("start desc parsing");

				int index = 0;

				String input = toParse;
				String output = "";

				String work = input;

				debug(input, 2);
				debug("Length (input): " + input.length(), 2);
				debug(input.contains("{"), 2);

				int begin = input.indexOf("{", index); // find the beginning of the markup
				debug("Begin Markup: " + begin, 2);
				int mid = input.indexOf("?", index);   // find the middle of the markup
				debug("Middle Markup: " + mid, 2);
				int end = input.indexOf("}", index);   // find the end of the markup
				debug("End Markup: " + end, 2);

				// if there is internal markup
				if (begin != -1 && mid != -1 && end != -1) {

					output = input.substring(0, begin); // grab the stuff before the markup

					debug("Current Output: " + output, 2);

					while (input.contains("{")) { // while there is still markup

						debug(input.substring(mid + 1, end));

						String timeOfDay = input.substring(begin + 1, mid);

						debug("Time of Day (?): " + timeOfDay, 2);

						String alt = input.substring(mid + 1, end);

						debug("Time of Day Message: " +  alt, 2);

						if (timeOfDay.equals(CtimeOfDay)) {
							output = output + alt;
							debug("Current Output: " + output, 2);
						}

						debug("", 2);

						// subtract up til the end of the first markup to use as the new input
						input = input.substring(end + 1, input.length());

						debug("New Input: " + input, 2);
						debug("Length (new input): " + input.length(), 2);

						index = end;

						debug(index, 2);

						begin = input.indexOf("{", index); // find the beginning of the markup
						debug("Begin Markup: " + begin, 2);
						mid = input.indexOf("?", index);   // find the middle of the markup
						debug("Middle Markup: " + mid, 2);
						end = input.indexOf("}", index);   // find the end of the markup
						debug("End Markup: " + end, 2);

						if ( end == -1 ) { // if there isn't a closing brace
							debug("ERROR: Markup is missing an end bracket at between character " + mid + " and the end of the string");
							output = output + input.substring(mid + 1, input.length());

							debug("Current Output: " + output, 2);

							return output;
						}

					}

					output = output + work;

					debug("Current Output: " + output, 2);

					debug("end desc parsing");

					return output;
				}
				else {
					debug("end desc parsing");

					return input;
				}
			}

			/**
			 * A mechanism to apply effects to objects. This function
			 * should differentiate between instantaneous and on-going effects.
			 * 
			 * Instaneous effects should be applied instantly and leave no trace.
			 * On-going effects should be noted somewhere
			 * 
			 * @param player
			 * @param effect
			 * @return whether or the not the effect was successfully applied
			 */
			public boolean applyEffect(final MUDObject m, final Effect effect) {
				return false;
			}

			/**
			 * A mechanism to apply effects to players. This function
			 * should differentiate between instantaneous and on-going effects.
			 * 
			 * Instaneous effects should be applied instantly and leave no trace.
			 * On-going effects should be added in the player effects list, etc.
			 * 
			 * @param player
			 * @param effect
			 * @return whether or the not the effect was successfully applied
			 */
			public boolean applyEffect(final Player player, final Effect effect) {
				final Client client = player.getClient();

				/* WARNING: healing effects currently remove any supplementary hitpoints.
				 * this should not remove hitpoints, it should only them up to the total
				 * hitpoints of a player. To account for this behavior, the totalhp could always
				 * be adjusted to be the current max, but then i'd have to track. If I don't do
				 * that any spells/effects that temporarily raise hitpoints will make healing you at all a bad thing
				 * NOTE: another problem arises, if I had 100 totalhp and 10 hp, then I could only heal myself
				 * to 90 (9/10 full heal).
				 */

				try {
					if (effect.getName().contains("heal")) {
						Integer amount;
						String work = effect.getName().substring(effect.getName().indexOf("+") + 1, effect.getName().length());

						try {
							amount = Integer.parseInt(work);
							debug("Amount of Healing: " + amount);
							debug("Hitpoints: " + player.getHP());
							debug("Hitpoints (total): " + player.getTotalHP());

							// if max is 10 and have 10, then no healing
							int diff = player.getTotalHP() - player.getHP();

							if (diff > amount) {
								player.setHP(amount);
								send("Healing (+" + amount + ") effect applied!\nYou gained " + amount + " hit points.", client);
							}

							else if (diff < amount) {
								player.setHP(diff);
								if (diff < amount) {
									send("Healing " + "(+" + amount + ") effect applied!\nYou gained "  + diff + " hit points.", client);
								}
							}
							else if (diff == amount) {
								player.setHP(amount);
								send("Healing (+" + amount + ") effect applied!\nYou gained " + amount + " hit points.", client);
							}
						}
						catch( NumberFormatException nfe ) {
							debug(nfe.getMessage()); // send a debug message
							amount = 0;
						}
					}
					else if (effect.getName().contains("dam")) {
						int damage;
						String work = effect.getName().substring(effect.getName().indexOf("-") + 1, effect.getName().length());
						try {
							damage = Integer.parseInt(work);
							debug("Amount of Damage: " + damage);
							debug("Hitpoints: " + player.getHP());
							debug("Hitpoints (total): " + player.getTotalHP());

							player.setHP(damage);
							send("Damage " + "(-" + damage + ") effect applied!\nYou lost "  + damage + " hit points.", client);
						}
						catch( NumberFormatException nfe ) {
							debug(nfe.getMessage()); // send a debug message
							damage = 0;
						}
					}
					// covers dispel case for now
					// will need serious work later
					else if (effect.getName().contains("!any")) {
						/*
						 * if I mean to use this for different kinds
						 * of dispelling I need a rule
						 */
						player.removeEffects();
						send("All Effects removed!", client);
					}
					// remove effect if ! is prefixed to it
					else if (effect.getName().contains("!")) {
						String work = effect.getName();
						String effectName = work.substring(work.indexOf("!") + 1, work.length());
						//String effectName = effect.getName().substring(effect.getName().indexOf("!") + 1, effect.getName().length());
						player.removeEffect(effectName);
						send(effectName + " effect removed!", client);
					}
					// add effects
					else {
						player.addEffect(effect.getName());
						send(effect.getName() + " Effect applied to " + player.getName(), client);
						debug("Game> " + "added " + effect.getName() + " to " + player.getName() + ".");
					}

					player.updateCurrentState();

					return true;
				}
				catch(Exception e) {
					return false;
				}
			}

			/**
			 * Boot/Kick a player off the game immediately
			 * 
			 * NOTE: command ?
			 * 
			 * @param  c the client to kick
			 * @return true (succeeded), false (failed for some reason)
			 */
			public void kick(final Client c) {
				this.s.disconnect(c);
			}

			/**
			 * getTime
			 * 
			 * get a brand new time object that holds the current time
			 * 
			 * NOTE: includes hours, minutes, and seconds
			 * NOTE: only holds the exact time when called, does not count
			 * or do anything else 
			 * 
			 * @return
			 */
			public Time getTime() {
				// get current data
				Calendar rightNow = Calendar.getInstance();

				// get the hour, minute, and second
				int hour = rightNow.get(Calendar.HOUR);
				int minute = rightNow.get(Calendar.MINUTE);
				int second = rightNow.get(Calendar.SECOND);

				// return a new time object with the current time
				return new Time(hour, minute, second);
			}

			/**
			 * skill_check
			 * 
			 * perform a skill check
			 * 
			 * NOTE: doesn't check against a player, just checks against specified value
			 * 
			 * @param s          the skill "object"
			 * @param diceRoll   a dice roll specified by a string (ex. '1d4' to roll a single d4 or 4-sided die)
			 * @param skillValue your ranks in that skill
			 * @param skillMod   any modification to the skill based on stats, magical enhancement
			 * @param DC         the DC(difficulty) check you are comparing your skill against
			 * @return           true (succeeded in passing DC), false (failed to pass DC)
			 */
			public boolean skill_check(final Player p, final Skill s, final String diceRoll, final int DC) {
				return skill_check(s, diceRoll, p.getSkill(s), p.getAbility(s.getAbility()), DC);
			}

			public boolean skill_check(final Skill s, final String diceRoll, final int skillValue, final int skillMod, final int DC) {
				// ex. 10 skill + 4 mod (via STR) > 25 ?: false (14 < 25)

				final String skillName = s.getName();

				final int skill = skillValue + skillMod;

				System.out.println(skillName + ": " + skill + " [ " + skillValue + "(skill) " + skillMod + "(modifier)" + " ]");

				final int roll = Utils.roll(diceRoll);

				System.out.println(diceRoll + " -> " + roll); // tell us what we rolled

				// Report the result of our rull
				System.out.println("Difficulty Check: " + DC);

				if ( skill + roll >= DC ) {
					System.out.println("Success");
					return true;
				}
				else {
					System.out.println("Failure");
					return false;
				}
			}

			/**
			 * Loop through the players, make sure we are the only one poking at the current one,
			 * then make single increment adjustments to their location
			 * 
			 * NOTE: seems to explode where x != y for the destination
			 */
			public void handleMovement() {
				// old WeatherLoop code, which only ran once per "minute" anyway
				// loop through all the rooms and broadcast weather messages accordingly
				for (final Room r : objectDB.getWeatherRooms()) {
					broadcast("", r);
				}
				for (final Room r : objectDB.getRoomsByType(RoomType.PLAYER)) {
					broadcast("", r);
				}
				// end old WeatherLoop code

				for (final Player player : this.moving) {
					synchronized(player) {
						// if the player is moving (something else could change this)
						if (player.isMoving()) {

							//Point position = player.getCoordinates();
							//Point destination = player.getDestination();

							if (player.getCoordinates().getX() != player.getDestination().getX() && player.getCoordinates().getY() != player.getDestination().getY()) {
								Message msg = new Message("Current Location: " + player.getCoordinates().getX() + ", " + player.getCoordinates().getY(), player);
								addMessage(msg);

								// move diagonally to reach the destination
								// NOTE: not the best way, but it'll have to til I can implement some kind of pathfinding
								if (player.getCoordinates().getX() < player.getDestination().getX()) {
									player.coord.incX(1);
								}
								else if (player.getCoordinates().getX() > player.getDestination().getX()) {
									player.coord.incX(-1);
								}
								if (player.getCoordinates().getY() < player.getDestination().getY()) {
									player.coord.incY(1);
								}
								else if (player.getCoordinates().getY() > player.getDestination().getY()) {
									player.coord.incY(-1);
								}

								// tell us about our new location
								Message msg1 = new Message("New Location: " + player.getCoordinates().getX() + ", " + player.getCoordinates().getY(), player);
								addMessage(msg1);
								Message msg2 = new Message("Destination Location: " + player.getDestination().getX() + ", " + player.getDestination().getY(), player);
								addMessage(msg2);

								// tell us if we reached the destination this time
								if (player.getCoordinates().getX() == player.getDestination().getX() && player.getCoordinates().getY() == player.getDestination().getY()) {
									player.setMoving(false);
									moving.remove(player);
									msg = new Message("You have reached your destination", player);
									addMessage(msg);
								}
							}
						}
					}
				}
			}

			/*
			 * push the thing if player is able to push it (strength check) over by one
			 * space if possible (destination check) and check the triggers on the "tile" to
			 * see if there are any things that should happen. If an exit is obscured by the rock,
			 * then it should be indicated what you see behind the rock and the next look should
			 * reveal any exits there might be
			 */
			public void push(Thing thing, Client client) {
				Player player = getPlayer(client);
				Room room = getRoom(thing.getDBRef());

				boolean canMove = false;

				// get weight of thing
				double weight = thing.getWeight();

				// make strength check to see if we can actually push it (str > weight / 4)
				if ( player.getAbility(Abilities.STRENGTH) > weight / 4) { // able to move?
					debug(player.getAbility(Abilities.STRENGTH) + " > " + weight / 4 + "? true");
					send("Success!", client);
					canMove = true;
				}
				else {
					debug(player.getAbility(Abilities.STRENGTH) + " > " + weight / 4 + "? false");
					send("Failure.", client);
				}

				/*
				 * side note: if the rock was on some kind of sliding mechanism or aligned with a groove in
				 * the floor it might suddenly slide away, if so, one could logically be surprised and fall over
				 * a balance check might be appropriate
				 * 
				 * unfortunately, you'd probably remember this the second or third time, which would
				 * complicate modeling the sequence of events. perhaps we should have an alertness check to
				 * see how cautiously you push the boulder
				 * 
				 * a cautious person might not be caught off guard while an incautious one might lean on any
				 * rock, and perchance find a secret passage and end up tumbling in.
				 */

				if ( canMove ) {
				}
			}

			/**
			 * Command to load other commands.
			 * 
			 * NOTE: This must always be loaded, or else you will not be able
			 * to load any unloaded commands.
			 * 
			 * RETURN: If the returned boolean is false, the command could
			 * not be loaded. It may be that the command is already loaded,
			 * cannot be loaded (error in the code?), or there is no such
			 * command to load (absence of the .class file?).
			 * 
			 * @param arg      the name of the command to load
			 * @param client   the client that called the command (not needed here?) 
			 * @return boolean a true/false indicating whether or not the command was loaded.
			 */
			public boolean cmd_loadc(String arg, Client client) {
				return false;
			}

			/**
			 * Command to unload other commands.
			 * 
			 * NOTE: This must always be loaded, or else you will not be
			 * able to unload any loaded commands.
			 * 
			 * RETURN: If the returned boolean is false, the command could
			 * not be unloaded. It may be that the command has not been loaded,
			 * there is no such command to unload (absence of the .class file?),
			 * or a call to the command is still in the queue. You will not be
			 * able to unload the command until the queue contains no calls to it.
			 * At some future time, there may be a means to disable the command
			 * so it cannot be called again, although the command queue must still
			 * clear.
			 * 
			 * @param arg      the name of the command to unload
			 * @param client   the client that called the command (not needed here?)
			 * @return boolean a true/false indicating whether or not the command was unloaded.
			 */
			public boolean cmd_unloadc(String arg, Client client) {
				boolean commandUnloaded = false;

				if ( commandMap.containsKey(arg) ) {
					commandMap.remove(arg);
					commandUnloaded = true;

					send("Command Unloaded!", client);
				}
				else {
					send("No such command.", client);
				}

				return commandUnloaded;
			}

			public ArrayList<Player> getPlayers() {
				return this.players;
			}

			/**
			 * Evaluate the player and return a string that describes their appearance,
			 * using the calling player's stats/skills/abilities to observe to decide
			 * what they could or couldn't tell from looking.
			 * 
			 * <br />
			 * <br />
			 * 
			 * <b>NOTE:</b><br />this will eventually be part of the dynamic naming engine, which will
			 * evaluate character attributes, properties, etc and come up with a name.
			 * I need both the caller and the called upon so I can figure player
			 * perception and visible target attributes in what I tell the player
			 * about the target
			 * 
			 * @param caller the player who looks at a player
			 * @param player the player being looked at
			 * @return a string that describes a player's appearance and proposes some assumptions
			 * based on the calling player's ability to "observer"
			 */
			public String evaluate(Player caller, Player player) {
				return "";
			}

			/**
			 * Evaluate a list via the program parser (parse_pgm)
			 * 
			 * @param  list the text list we wish to evaluate as code
			 * @return an arraylist of strings
	public ArrayList<String> evaluateList(ArrayList<String> list) {
		return null;
	}
			 */

			/* ? */

			/**
			 * Generate a player from it's database representation
			 * 
			 * NOTE: for testing purposes only now, init_conn doesn't go through
			 * loadObjects, which is pointless when you consider that I only hold onto a copy
			 * of the objects and it never goes into the player's array.
			 * 
			 * NOTE2: meant to solve a problem where I haven't copied the load code into init_conn,
			 * but want a properly initialized/loaded player for existing characters when they login
			 * 
			 * @param playerData
			 * @return a player object
			 */
			public Player loadPlayer(String playerData) {

				String[] attr = playerData.split("#");

				Integer oDBRef = 0, oLocation = 0;
				String oName = "", oFlags = "", oDesc = "", oPassword = "";
				String[] os, om;

				oDBRef = Integer.parseInt(attr[0]);    // 0 - player database reference number
				oName = attr[1];                       // 1 - player name
				oFlags = attr[2];                      // 2 - player flags
				oDesc = attr[3];                       // 3 - player description
				oLocation = Integer.parseInt(attr[4]); // 4 - player location

				oPassword = attr[5];                   // 5 - player password
				os = attr[6].split(",");               // 6 - player stats
				om = attr[7].split(",");               // 7 - player money
				int access;                            // 8 - player permissions
				int raceNum;                           // 9 - player race number (enum ordinal)
				int classNum;                          // 10 - player class number (enum ordinal)

				/*debug("Database Reference Number: " + oDBRef);
		debug("Name: " + oName);
		debug("Flags: " + oFlags);
		debug("Description: " + oDesc);
		debug("Location: " + oLocation);*/

				Integer[] oStats = Utils.stringsToIntegers(os);
				int[] oMoney = Utils.stringsToInts(om);

				final Player player = new Player(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation, "", oPassword, "IC", oStats, Coins.fromArray(oMoney));

				/* Set Player Permissions */
				player.setAccess(Utils.toInt(attr[8], USER));

				/* Set Player Race */
				try {
					raceNum = Integer.parseInt(attr[9]);
					player.setPlayerRace(Races.getRace(raceNum));
				}
				catch(NumberFormatException nfe) {
					nfe.printStackTrace();
					player.setPlayerRace(Races.NONE);
				}

				/* Set Player Class */
				try {
					classNum = Integer.parseInt(attr[10]);
					player.setPClass(Classes.getClass(classNum));
				}
				catch(NumberFormatException nfe) {
					nfe.printStackTrace();
					player.setPClass(Classes.NONE);
				}

				debug("DEBUG (db entry): " + player.toDB());

				return player;
			}

			/* Creation Functions */

			/**
			 * Create and return a new room object with the specified name and parent room and
			 * a unique database reference number.
			 * 
			 * @param roomName   the name for the new room
			 * @param roomParent the room that is the "parent" of this one
			 * @return           the new room object
			 */
			public Room createRoom(String roomName, int roomParent)
			{
				// flags are defined statically here, because I'd need to take in more variables and in no
				// case should this create anything other than a standard room which has 'RS' for flags
				final Room room = new Room(-1, roomName, EnumSet.of(ObjectFlag.ROOM, ObjectFlag.SILENT), "You see nothing.", roomParent);

				/*
		// add rooms to database (main)
        objectDB.addAsNew(room);
		objectDB.addRoom(room);

		// tell us about it
		//send("Room '" + roomName + "' created as #" + id + ". Parent set to " + roomParent + ".", );

		/* for use of room editor */
				return room;
			}

			/**
			 * create a new basic, untyped item for us to modify and work on
			 * 
			 * @return
			 */
			private Item createItem() {
				final Item item = new Item();

				item.setFlags(EnumSet.of(ObjectFlag.ITEM));
				item.setLocation(WELCOME_ROOM);

				item.setItemType(ItemType.NONE);

				objectDB.addAsNew(item);
				objectDB.addItem(item);

				((Room) objectDB.get(item.getLocation())).contents1.add(item);

				return item;
			}

			/**
			 * Create a new item using an existing item as a template. More or less a
			 * means to copy an object.
			 * 
			 * NOTE: internal use only
			 * 
			 * @param template the item to base the new one on
			 * @return the new item we just created
			 */
			private Item createItem(Weapon template) {
				final Item item = createItems(template, 1).get(0);
				item.setLocation(WELCOME_ROOM);
				return item;
			}

			private Item createItem(Book template) {
				final Item item = createItems(template, 1).get(0);
				item.setLocation(WELCOME_ROOM);
				return item;
			}

			private Item createItem(Armor template) {
				final Item item = createItems(template, 1).get(0);
				item.setLocation(WELCOME_ROOM);
				return item;
			}

			/**
			 * Create new items using an existing item as a template. More or less
			 * a means to make multiple copies of an item
			 * 
			 * <br />
			 * <br />
			 * 
			 * <b>NOTE:</b> internal use only
			 * 
			 * @param  template the item to base the new ones on
			 * @param numItems how many new items to make.
			 * @return the new items we just created
			 */
			private ArrayList<Item> createItems(Weapon template, int numItems) {
				ArrayList<Item> items = new ArrayList<Item>(numItems);

				for (int i = 0; i < numItems; i++) {
					final Weapon item = new Weapon(template);
					items.add(item);
					initCreatedItem(item);
				}
				return items;
			}

			private ArrayList<Item> createItems(Book template, int numItems) {
				ArrayList<Item> items = new ArrayList<Item>(numItems);

				for (int i = 0; i < numItems; i++) {
					final Book item = new Book(template);
					items.add(item);
					initCreatedItem(item);
				}
				return items;
			}

			private ArrayList<Item> createItems(Armor template, int numItems) {
				ArrayList<Item> items = new ArrayList<Item>(numItems);

				for (int i = 0; i < numItems; i++) {
					final Armor item = new Armor(template);
					items.add(item);
					initCreatedItem(item);
				}
				return items;
			}

			private void initCreatedItem(final Item item) {
				objectDB.addAsNew(item);
				item.setLocation(0);
				objectDB.addItem(item);
			}

			/*public NPC createNPC(String name, int location) {
		NPC npc = new NPC(getNextDB(), name, null, "N", "A generic npc", "NPC", "IC", location, new String[]{ "0", "0", "0", "0" });
		npcs1.add(npc);
		return npc;
	}

	public Creature createCreature(String race, String name, String desc) {
		Creature creature = new Creature(getNextDB(), race, name, desc);
		creatures.add(creature);
		return creature;
	}*/


			/**
			 * Generate an exits list filtered by visibility based on parameters
			 * 
			 * potential parameters
			 * - current location in the room (nearness to it)
			 * - visibility (is there fog)
			 * - lighting (is it light or dark)
			 * 
			 * criteria?: 
			 * 	flags are...
			 * 	flags does not contain...
			 * 
			 * @param exits the lists of exits to filter
			 * @param filters the filters to apply
			 * @return the filtered list of exits
	public ArrayList<Exit> filter(ArrayList<Exit> exits, Filter...filters) {
		return null;
	}
			 */

			/**
			 * Run a weather update.
			 * 
			 * This should go through the rooms that "need" updating and cause
			 * them to proceed from the current weather state to a new weather
			 * state based on probability.
			 * 
			 * At the present it only updates a single room, ever.
			 */
			public void updateWeather() {
				for (final Room room : objectDB.getWeatherRooms()) {

					room.getWeather().nextState();

					final WeatherState ws = room.getWeather().ws;
					if (ws.upDown != 1 && ws.upDown != -1) {
						return;
					}

					String changeText = ws.upDown == 1 ? ws.transUpText : ws.transDownText;
					if (changeText != null) {
						addMessage(new Message(changeText, 1));
						debug(changeText);
					}
				}
			}

			/**
			 * Get a help file by name, and return it as a string
			 * array.
			 * 
			 * @param name the name of the help file to gt
			 * @return a string array that contains the file's contents
			 */
			public String[] getHelpFile(String name) {
				return helpMap.containsKey(name) ? helpMap.get(name) : null;
			}

			/**
			 * Load a newly created thing into the database.
			 * 
			 * currently unused
			 * 
			 * @param thing the thing to load
			 * @return true if the thing was successfully loaded, false otherwise
	private boolean loadThing(Thing thing) {
		int dbref = thing.getDBRef();

		// I want to be sure not to overwrite anything
		if (main.get(dbref).split("#")[4].equals("-1")) { // need to check to see if something is there already (dbref == -1 means a NULLObject)
			main.set(dbref, thing.toDB());               // modify database entry
		}

		if (main1.get(dbref) instanceof NullObject || main1.get(dbref) == thing) { // if main1 holds a NULLObject or the exact same thing
			main1.set(dbref, thing); // modify in-memory database
		}

		things.add(thing); // put in the things list

		getRoom(thing.getLocation()).contents.add(thing); // put in the room's content

		return true;
	}
			 */

			/**
			 * showDesc
			 * 
			 * A wrapper for showDesc that passes in default line wrap value.
			 */
			public void showDesc(final String description, final Client client) {
				showDesc(description, 80, client);
			}

			/**
			 * showDesc
			 * 
			 * Sends a room description or other long string to the client as a series
			 * of strings, none of whom may be more than LIMIT number of characters long
			 * 
			 * @param description the string to wrap at LIMIT characters
			 * @param line_limit       the maximum length of a string to send
			 * @param client      the client
			 */
			public void showDesc(final String description, final int line_limit, final Client client) {

				final StringBuilder result = new StringBuilder(line_limit);
				for (final String word : description.split(" ")) {
					debug("result: " + result, 3);
					debug("result (length): " + result.length(), 3);
					debug("next: " + word, 3);
					debug("next (length): " + word.length(), 3);
					if (result.length() < 1) { // append current word if empty
						result.append(word);
					}
					else if (result.length() + word.length() + 1 < line_limit) { // append current word if it won't overflow
						debug("add", 3);
						result.append(" ").append(word);
					}
					else { // if it will overflow, send and clear, and append current word
						debug("send", 3);
						send(result, client);
						result.delete(0, result.length());
						result.append(word);
					}
				}

				// make sure we send the last word if there was only one left
				if( result.length() > 0 ) {
					debug("send", 3);
					send(result, client);
					result.delete(0, result.length());
				}
			}

			/**
			 * USE_PORTAL
			 * 
			 * Handles portal usage
			 * 
			 * I'd like this to be treated as entering a room as well or using an exit
			 * 
			 */
			private void use_portal(final Portal portal, final Client client) {
				final Player player = getPlayer(client);
				final Room room = getRoom(client);
				final int portalOrigin = portal.getOrigin();
				final int portalDest = portal.getDestination();
				final boolean playerAtPortal = portal.coord.getX() == player.coord.getX() && portal.coord.getY() == player.coord.getY();
				final boolean missingRequiredKey = portal.requiresKey() && !portal.hasKey( player );

				System.out.println("Portal: " + portal.getName());

				// if the portal is keyed and is some kind of thing/item, then I need to check before permitting use
				if (!playerAtPortal || missingRequiredKey) {
					return;
				}

				if (portalOrigin == room.getDBRef()) {
					player.setLocation(portalDest);

					debug("Portal( " + player.getName() + ", " + portalOrigin + ", " + portalDest + " ): success");
				}
				else if (portalDest == room.getDBRef()) {
					player.setLocation(portalOrigin);

					debug("Portal( " + player.getName() + ", " + portalDest + ", " + portalOrigin + " ): success");
				}
			}

			/**
			 * USE_WAND
			 * 
			 * Special use function for wands that handles applying or removing effects to/from
			 * the player, removing the charges expended, and if all charges are expended marking
			 * the wand as spent and/or empty.
			 * 
			 * @param potion the potion to use
			 * @param client the client
			 */
			private void use_wand(final Wand wand, final Client client) {		
				if (wand.charges > 0) {
					send("You use your Wand of " + wand.spell.name + " to cast " + wand.spell.name + " on yourself.", client);

					debug("Game> Casting..." + wand.spell.name);

					try {
						cmd_cast(wand.spell.name, client);
					}
					catch(Exception e) {
						e.printStackTrace();
					}

					debug("Game> Spell Cast.");

					wand.charges--;
				}
				else {
					send("You wave the wand, but nothing happens. It must be drained of it's magic.", client);
				}
			}

			/**
			 * USE_POTION
			 * 
			 * Special use function for potions that handles applying or removing effects to/from
			 * the player and disposing of the potion item itself if it should disappear.
			 * 
			 * @param potion the potion to use
			 * @param client the client
			 */
			private void use_potion(final Potion potion, final Client client) {
				final Player player = getPlayer(client);

				if (potion.stackSize() > 1) {
					player.getInventory().add(potion.split(1));
				}

				if (potion.getSpell() != null) {
					send("You use a Potion of " + potion.getSpell().name + " on yourself.", client);
					player.setTarget(player); // target yourself
					cmd_cast(potion.getSpell().name, client);
				}
				else if (potion.getEffect() != null) {
					send("You use a Potion of " + potion.getEffect().getName() + " on yourself.", client);
					applyEffect(player, potion.getEffect());
				}

				// destroy the "used" item
				final int dbref = potion.getDBRef();           // get it's dbref
				final NullObject nobj = new NullObject(dbref); // create a nullobject with that dbref

				objectDB.set(dbref,  nobj);                 // remove from "live" database (replace with NullObject)
				player.getInventory().remove(potion);    // remove from player inventory
			}

			/**
			 * Checks access/permissions level against the queried
			 * access and returns true or false depending on whether they meet/exceed
			 * the specified access level or not. If the client is associated with a 
			 * player, then the player's access is checked, otherwise it's checked
			 * against 0 (basic user permissions). 
			 * 
			 * @param client
			 * @param accessLevel
			 * @return
			 */
			public boolean checkAccess(final Client client, final int accessLevel) {
				Player player = getPlayer(client);
				
				int check = 0;
				
				if(player != null) {
					check = player.getAccess();
				}

				return check >= accessLevel;
			}

			/**
			 * Handle aborting an error, takes an error message to print as an argument
			 */
			public void abortEditor(final String errorMessage, final String old_status, final Client client) {
				final Player player = getPlayer(client);

				// reset player, and clear edit flag and editor setting and editor data
				player.setStatus(old_status);
				player.setEditor(Editor.NONE);
				player.setEditorData(null);

				// tell us what went wrong
				send(errorMessage, client);
			}

			/**
			 * Load an area into the game from a text file
			 * 
			 * NOTE: really more like creating an area, since the rooms will exist next time round and we won't run this on the same area twice?
			 */
			public void loadArea(final String filename) {
				final String[] file = Utils.loadStrings(filename);

				int step = 0; // 0=AREA, 1=ROOM

				Area area = null;

				String name;

				for (final String s : file) {
					if ( s.equals("@AREA") ) {
						step = 0;
						continue;
					}
					else if ( s.equals("@ROOM") ) {
						step = 1;
						continue;
					}

					final String[] data = s.split(":");
					final String key = data[0].trim();
					final String value = data[1].trim();

					switch(step) {
					case 0: // area
						if ( key.equals("name") ) {
							name = value; // TODO: nothing is actually done with this value, AFAICT
						}
						else if ( key.equals("registered") ) {
							if (Utils.toInt(value, -1) == 1) { // is this area "registered"
								step = 1;
							}
						}
						else if ( key.equals("rooms") ) {
							area = new Area(Integer.parseInt(value));
							step = 1;
						}
						break;
					case 1: // room
						/**
						 * basically, for a brand new area, we just create each room as it's specification pops
						 * up
						 */

						final Room room = new Room(); // creates a "blank" room with basic flags and locks, a location and desc borders

						if ( key.equals("dbref") ) {
							objectDB.addAsNew(room);
						}
						else if ( key.equals("name") ) {
							room.setName(value);
						}
						else if ( key.equals("desc") ) {
							room.setDesc(value);
						}

						area.addRoom(room.getDBRef());
						break;
					default:
						break;
					}
				}
			}

			/*
	public boolean evalLock(String lockString) {
		// x && (y || z)

		// set a boolean for each variable
		// evaluate the truth of each variable
		// evaluate string in order of operations

		// return result
		return false;
	}
			 */

			/*
	// pass arguments to the object creation function
	else if ( cmd.equals("@create") || ( aliasExists && alias.equals("@create") ) )
	{
		adminCmd = true;
		// run the object creation function
		cmd_createItem(arg, client);
	}
	else if ( cmd.equals("@create_npc") || ( aliasExists && alias.equals("@create_npc") )) {
		buildCmd = true;
		createNPC(arg, getPlayer(client).getLocation());
	}
	else if ( cmd.equals("@create_creature") ) {
		String[] args = arg.split("=");
		createCreature(args[0], args[1], args[2]);
	}
			 */

			public boolean isRunning() {
				return this.running;
			}

			// TODO there is code dependent on getting a reasonable answer here, if this goes, I need to fix it
			/**
			 * Returns the size of the database, use for code external to class
			 * where I need to be sure I don't try and access an index outside
			 * of the database
			 * @return
	public int dbSize() {
		//return main1.size();
		return -1;
	}
			 */

			/**
			 * Takes an input string and generates a new one where each letter is prefixed by
			 * the ansi code for the colors of the rainbows in order from Red to Violet (ROYGBIV).
			 * The colors are repeated until we've run out of the input string. The whole thing
			 * is then capped off with the white color code as a sort of reset.
			 * 
			 * NOTE: since normal ansi colors don't cover the whole rainbow, a few have been omitted
			 */
			public String rainbow(final String input) {
				//String[] rainbow = new String[] { "red", "orange", "yellow", "green", "blue", "indigo", "violet" };
				final String[] rainbow = new String[] { "red", "yellow", "green", "blue" };
				final StringBuffer sb = new StringBuffer();

				int index = 0;

				for (final Character c : input.toCharArray()) {
					sb.append(colorCode(rainbow[index]));
					sb.append(c);
					index = (index + 1) % rainbow.length;
				}

				sb.append(colorCode("white"));

				return sb.toString();
			}

			/*	
	public void display(Container<Item> c, Client client) {
		String top = c.getTop(), side = c.getSide(), bottom = c.getBottom();
		int displayWidth = c.getDisplayWidth();

		send("/" + top + "\\", client);
		send(side + Utils.padRight(name, displayWidth) + side, client);
		send(side + top + side, client);

		Container<Item> d = (Container<Item>) c;

		for (Item item : d.getContents()) {
			send(side + Utils.padRight(item.getName(), displayWidth) + side, client);
		}
		send("\\" + bottom + "/", client);
	}
			 */

			// needs some work, need to reduce copycat functions for display, in here
			// WARNING: Seriously broken due to needing access to the player
			public void displayI(Container<Item> c, Client client) {
				String top = c.getTop(), side = c.getSide(), bottom = c.getBottom();
				int displayWidth = c.getDisplayWidth();

				send(side + Utils.padRight(colors(serverName + "(#" + c.getDBRef() + ")", "yellow"), 41) + side + Utils.padRight("", getPlayer(client).invDispWidth - displayWidth - 1) + "|", client);
				send(side + top + side + Utils.padRight("", 29) + "|", client);

				for (final Item item : c.getContents()) { 
					send(side + Utils.padRight(colors(item.getName(), "yellow"), 41) + side + Utils.padRight("", 29) + "|", client);
				}

				send("|" + bottom + "|" + Utils.padRight("", 29) + "|");
			}

			/*
	public void display(Container<Item> c, char type, Client client) {
		String top = c.getTop(), side = c.getSide(), bottom = c.getBottom();
		int displayWidth = c.getDisplayWidth();

		Container<Item> d = (Container<Item>) c;

		if (type == 'S') {
			int n = 0;
			String o = "You have ";
			for (Item item : d.getContents()) {
				if (n < c.getContents().size() - 1) {
					o = o + item.getName() + ", ";
					n++;
				}
				else {
					o = o + " and " + item.getName() + ".";
				}
				send(o, client);
			}
		}
		else if (type == 'C') {
			send("/" + top + "\\", client);
			send(side + Utils.padRight(this.name, displayWidth) + side, client);
			send(side + top + side, client);
			for (Item item : d.getContents()) {
				send(side + Utils.padRight(item.getName(), displayWidth) + side, client);
			}
			send("\\" + bottom + "/", client);
		}
	}
			 */

			public Account getAccount(final String name, final String pass) {
				for (final Account account : accounts) {
					if (account.getUsername().equals(name)) {
						return account.getPassword().equals(pass) ? account : null;
					}
				}

				return null;
			}

			public void onHourIncrement() {
				fillShops();
				updateWeather();
			}

			/**
			 * Evaluate a string and resolve name references
			 * 
			 * NOTE: name references looks like this -> '$house' and is associated with an integer database reference
			 * 
			 * @param input  an input string that potentially contains a name reference
			 * @param client the client that the string was sent by
			 * @return the original string with any name references within resolved to numbers
			 */
			public String nameref_eval(String input, Client client) {
				StringBuilder sb = new StringBuilder(input);    // local, stringbuilder copy of original
				StringBuilder refString =  new StringBuilder(); // where we'll store the ref. string as we find the characters 

				Character ch = null; // the character well pull out of the input stringbuffer
				Integer refNum = 0;  // the retrieved number the ref. string refers to

				int index = 0; // current position in sb
				int begin = 0; // beginning of ref. string
				int end = 0;   // end of ref. string

				boolean check = false;   // have we found the beginning of a potential refrence
				boolean replace = false; // do we need to perform a replace operation
				boolean eval = false;    // are we ready to evaluate a complete reference

				while( sb.indexOf("$") != -1 ) {
					debug("Index: " + index);

					ch = sb.charAt(index); /* get a character */

					if( check ) {
						if( Character.isLetter(ch) ) {
							debug(ch);
							debug("Is a Letter!");

							refString.append(ch);

							if( index == sb.length() - 1 ) {
								end = index - 1;
								check = false;
								eval = true;
							}
						}
						else {
							end = index;
							check = false;
							eval = true;
						}
					}
					else {
						if(ch == '$') {
							debug("found a nameref");

							begin = index;
							check = true;

							if( !replace ) { replace = true; }
						}
					}

					if( !check && eval ) {
						debug("");
						debug("Game> (argument eval) reference: " + refString.toString());

						// try to get number from nameref table
						refNum = getPlayer(client).getNameRef(refString.toString());

						// modify string, if we got a valid reference (i.e. could be in database, a NULLObject is a valid reference
						if(refNum != null && refNum < objectDB.getSize()) {
							debug("Game> (argument eval) tempI: " + refNum); // report number

							debug("");
							debug("Begin: " + begin + " End: " + end + " Original: " + sb.substring(begin, end) + " Replacement: " + refNum.toString());
							debug("");

							sb.replace(begin, end, refNum.toString());

							debug("BUFFER: " + sb.toString());
							debug("");
						}

						// clear variables
						refString.delete(0, refString.length());
						ch = ' ';
						refNum = 0;
						index = -1;
						begin = 0;
						end = 0;

						eval = false;
					}

					index++;
				}

				if( replace ) {
					debug("Game> (argument eval) result: " + sb.toString());

					// change argument to reflect replacements (trim to clear extra space at end)
					return sb.toString();
				}
				else {
					return input;
				}
			}

			/**
			 * Calculate straight line distance between two Point(s) on the cartesian
			 * planes.
			 * 
			 * @param start Point to start at
			 * @param end   Point to end at
			 * @return
			 */
			public static int distance(Point start, Point end) {
				if( start != null && end != null) {
					if( start != end ) {
						// use pythagorean theorem to get straight line distance
						// pythagorean theorem (simplified): a^2 + b^2 = c^2
						int rise = Math.abs( start.getY() - end.getY() ); // x (a)
						int run = Math.abs( start.getX() - end.getX() );  // y (b)

						int distance = (int) Math.sqrt( square(run) + square(rise) ); // z (c)

						return distance;
						// calculate travel distance going at right angles
					}
					else {
						return 0;
					}
				}

				return -1;
			}

			public static int square(int num) {
				return num * num;
			}
		}
