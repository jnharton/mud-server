package mud;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

// MUD Libraries
//mud.colors
import mud.api.MUDServerAPI;
import mud.auction.Auction;
import mud.auction.AuctionTimer;
import mud.auction.Bid;
//import mud.colors.XTERM256;
import mud.commands.*;
import mud.foe.Terminal;
import mud.game.Abilities;
import mud.game.Ability;
import mud.game.Alignments;
import mud.game.Classes;
import mud.game.Feat;
import mud.game.PClass;
import mud.game.Race;
import mud.game.Races;
import mud.game.Skill;
import mud.game.Skills;
import mud.interfaces.*;
import mud.magic.*;
import mud.misc.Building;
import mud.misc.ClientData;
import mud.misc.Edge;
import mud.misc.House;
import mud.misc.Zone;
import mud.net.*;
import mud.objects.*; //ones I don't need: Banker
import mud.objects.Room.Terrain;
import mud.objects.items.*; // ones I don't need: Attribute, Pack
import mud.objects.npcs.Merchant;
import mud.objects.things.*;
// mud.protocols
import mud.protocols.MSP;
import mud.protocols.Telnet;
import mud.quest.*;
import mud.utils.*; //ones I don't need here: AreaConverter, Bank, BankAccount, HelpFile
import mud.utils.Console;
import mud.utils.Account.Status;
import mud.weather.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.xml.ws.Response;

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
 * @Last Work: database and flags (contributions by joshgit from github.com)
 * @minor version number should increase by 1: when 5 or more commands are added or modified, significant problem is fixed, etc?
 * Last Worked On: 2.4.2013
 **/

public class MUDServer implements MUDServerI, LoggerI, MUDServerAPI {
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
	private final static String program = "JavaMUD"; // the server program name
	private final static String version = "0.9.2";   // the server version number
	private String computer = "Stardust";            // the name of the server computer
	private String serverName = "Server";            // the name of the server (obtained from theme definition)

	// server configuration settings
	private int port = 4000;            // the port on which to listen for client connections
	private int max_log_size = 5000;    // max length of a log file (in lines)
	private int max_levels = 20;        // maximum player level
	private int max_players = 15;       // maximum number of players (adustable, but exceeding 100/1000 may cause other code to break down catastrophically)
	private int max_list_length = 1000; // maximum list length in lines
	private String motd = "motd.txt";   // Message of The Day file

	// server state settings
	private int multiplay = 1;               // (0=only one character per account is allowed, 1=infinite connects allowed)
	private int guest_users = 1;             // (0=guests disallowed, 1=guests allowed)
	private int debug = 1;                   // (0=off,1=on) Debug: server sends debug messages to the console
	private int debugLevel = 5;              // (1=debug,2=extra debug,3=verbose) priority of debugging information recorded
	private boolean logging = false;         // logging? (true=yes,false=no)
	private int logLevel = 3;                // (higher is more) priority of log information recorded 
	private boolean prompt_enabled = false;  // show player information bar
	
	// server state
	private GameMode mode = GameMode.NORMAL; // (0=normal: player connect, 1=wizard: wizard connect only, 2=maintenance: maintenance mode)
	private int guests = 0;         // the number of guests currently connected

	// Protocols
	/*
	 * This section is badly designed. In theory it represents whether support for something is enabled,
	 * but in the case of colors only ANSI -or- XTERM should be possible (one color system).
	 * 
	 * Incidentally, the color code generator functions check to see if color is on, if not they return
	 * the original, unaltered argument.
	 */
	private int color = Constants.XTERM; // (0=off,1=ansi,2=xterm) Color ON (ANSI/XTERM)/OFF
	private int msp = 0;                 // (0=off,1=on) MUD Sound Protocol on/off, default: off
	private int telnet = 1;              // (0=no telnet: mud clients, 1=telnet: telnet, 2=telnet: telnet and mud clients)

	// Language/Localization
	// en for English (US), fr for French (France?) are currently "supported",
	// but, only some of the error messages are currently converted to french
	private final String lang = "en";

	// directories to use (existence will be checked when setup() is run)
	private final String MAIN_DIR = new File("").getAbsolutePath() + "\\"; // Program Directory
	private final String DATA_DIR = MAIN_DIR + "data\\";                   // Data Directory

	private final String ACCOUNT_DIR = DATA_DIR + "accounts" + "\\";       // Account Directory
	private final String BACKUP_DIR = DATA_DIR + "backup" + "\\";          // Backup Directory
	private final String BOARD_DIR = DATA_DIR + "boards" + "\\";           // Boards Directory
	private final String CONFIG_DIR = DATA_DIR + "config" + "\\";          // Config Directory
	private final String HELP_DIR = DATA_DIR + "help" + "\\";              // Help Directory
	private final String TOPIC_DIR = HELP_DIR + "topics" + "\\";           // Topic Directory
	private final String MAP_DIR = DATA_DIR + "maps" + "\\";               // MAP Directory
	private final String MOTD_DIR = DATA_DIR + "motd" + "\\";              // MOTD Directory
	private final String SPELL_DIR = DATA_DIR + "spells" + "\\";           // Spell Directory
	private final String THEME_DIR = DATA_DIR + "theme" + "\\";            // Help Directory
	private final String LOG_DIR = DATA_DIR + "logs" + "\\";               // Log Directory
	private final String SESSION_DIR = DATA_DIR + "sessions" + "\\";       // Session Directory
	private final String WORLD_DIR = DATA_DIR + "worlds" + "\\";           // World Directory
	
	/* filename variables used to be final -- i'd like to be able to reload or change them while the game is running though */

	// files to use (existence will be checked when setup() is run)
	private String mainDB = DATA_DIR + "db.txt";                    // database file (ALL) -- will replace all 3 or supersede them
	private String errorDB = DATA_DIR + "errors_" + lang + ".txt";  // messages file (errors) [localized?]
	private String spellDB = DATA_DIR + "spells.txt";               // database file (spells) -- contains spell names, messages, and more
	
	private String ALIASES_FILE = CONFIG_DIR + "aliases.conf";       // config file (aliases)   -- command aliases
	private String BANLIST_FILE = CONFIG_DIR + "banlist.txt";       // config file (banlist)   -- banned ip addresses (IPv4)
	private String CHANNELS_FILE = CONFIG_DIR + "channels.txt";     // config file (channels)  -- preset chat channels to load
	private String CONFIG_FILE = CONFIG_DIR + "config.txt";         // config file (config)    -- configuration options file
	private String FORBIDDEN_FILE = CONFIG_DIR + "forbidden.txt";   // config file (forbidden) -- forbidden names/words
	
	private String THEME_FILE = THEME_DIR + "default.thm";          // theme file to load
	
	// ???
	private Integer start_room = 9;                         // default starting room

	// Objects (used throughout program in lieu of function scope variables) -- being phased out (April 2012
	// these must be global variable, so that mud can have top-level control over them in the program
	private Server s;          // The server object
	private Log log;           // A log file to keep track of user actions
	private Log debugLog;      // A log file to keep track of debugging messages
	private Log chatLog;       // A log file to keep track of chat messages
	public TimeLoop game_time; // TimeLoop Object

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
	private static final Map<String, String> ansi_colors = new HashMap<String, String>() {               // HashMap to store ansi/vt100 escape codes for outputting color (static)
		{
			put("black",  "\033[30;1m"); put("red",   "\033[31;1m"); put("green",   "\033[32;1m");
			put("yellow", "\033[33;1m"); put("blue",  "\033[34;1m"); put("magenta", "\033[35;1m");
			put("cyan",   "\033[36;1m"); put("white", "\033[37;1m");
		}
	};
	
	private static final Map<String, String> xterm_colors = new HashMap<String, String>() {              // HashMap to store xterm codes for outputting color (static)
		{
			put("red",   "\033[38;5;009m");  put("green",   "\033[38;5;010m"); put("yellow", "\033[38;5;011m");
			put("blue",  "\033[38;5;012m");  put("magenta", "\033[38;5;013m"); put("cyan",   "\033[38;5;014m");
			put("white", "\033[38;5;015m");  put("purple",  "\033[38;5;055m"); put("purple2", "\033[38;5;092m");
			put("orange", "\033[38;5;208m"); put("other", "\033[38;5;082m");   put("pink",    "\033[38;5;161m");
			put("pink2",  "\033[38;5;163m"); put("pink3",  "\033[38;5;212m");
		}
	};
	
	private final Map<String, Pair<String>> displayColors = new HashMap<String, Pair<String>>(8, 0.75f); // HashMap specifying particular colors for parts of text (dynamic)

	public final Map<String, String> aliases = new LinkedHashMap<String, String>(20, 0.75f);             // HashMap to store command aliases (static)
	private final Map<Integer, String> Errors = new HashMap<Integer, String>(5, 0.75f);                  // HashMap to store error messages for easy retrieval (static)

	private Map<String, Date> holidays = new HashMap<String, Date>(10, 0.75f);                           // HashMap that holds an in-game date for a "holiday" name string
	private Map<Integer, String> years = new HashMap<Integer, String>(50, 0.75f);                        // HashMap that holds year names for game themes that supply them (static)

	private final Map<String, Command> commandMap = new HashMap<String, Command>(20, 0.75f);             // HashMap that holds an instance of each command currently (dynamic)
	protected final Map<Zone, Integer> zones = new LinkedHashMap<Zone, Integer>(1, 0.75f);               // HashMap that tracks currently "loaded" zones (dynamic)
	private final HashMap<Client, Player> sclients = new HashMap<Client, Player>();
	private final PlayerControlMap playerControlMap = new PlayerControlMap();                      // HashMap that stores control data for Players controlling NPCs (dynamic)

	//private Map<String, String> config = new LinkedHashMap<String, String>(11, 0.75f); // track current config

	private HashMap<Player, Session> sessionMap = new HashMap<Player, Session>(1, 0.75f);                // player to session mapping

	// Databases/Data
	private ObjectDB objectDB = new ObjectDB();

	private ArrayList<Player> players;       // ArrayList of Player Objects currently in use

	private HashMap<String, Spell> spells2 = new HashMap<String, Spell>();  // HashMap to lookup spells by index using name as key (static)

	// Help Files stored as string arrays, indexed by name
	private Hashtable<String, String[]> helpTable = new Hashtable<String, String[]>();
	private Hashtable<String, String[]> topicTable = new Hashtable<String, String[]>();

	// "Security" Stuff
	private ArrayList<String> banlist;        // ArrayList of banned IP addresses
	private ArrayList<String> forbiddenNames; // ArrayList of forbidden names for players/characters

	// Other
	private ArrayList<Effect> effectTable;    // ArrayList of existing effects (can be reused many places)
	private ArrayList<Account> accounts;      // ArrayList of Accounts (UNUSED)
	private ArrayList<Auction> auctions;

	// Time & Date - General Variables
	private final static String[] suffix = { "st", "nd", "rd", "th" }; // day number suffix

	private int day = 30, month = 7, year = 1332;

	private int game_hour = 5;    // 5am
	private int game_minute = 58; // 58m past 5am

	//Theme Related Variables
	public static int[] DAYS = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 }; // days in each month
	public static int MONTHS = 12;                                                 // months in a year
	public static String[] MONTH_NAMES = new String[MONTHS];                       // month_names
	private Season season = Seasons.SUMMER; // Possible - Spring, Summer, Autumn, Winter

	private String month_name;
	private String year_name;
	private String reckoning;

	private Theme theme;

	private Map<String, BulletinBoard> boards = new Hashtable<String, BulletinBoard>();
	
	Ruleset rules = null;       // ruleset reference for defining stats, skills, etc
	boolean rs_special = false; // are we using a S.P.E.C.I.A.L based ruleset?

	// Testing
	private BulletinBoard bb;
	private ArrayList<Portal> portals = new ArrayList<Portal>();

	private ArrayList<Party> parties = new ArrayList<Party>(); // groups of players

	// for use with telnet clients
	private byte[] byteBuffer = new byte[1024]; // a byte buffer to deal with telnet (characters are sent to the server as they are typed, so need to buffer input until a particular key is pressed
	private byte linefeed = 10;                 // line-feed character

	public Hashtable<Client, ArrayList<Character>> inputBuffers = new Hashtable<Client, ArrayList<Character>>(max_players);
	// end telnet

	private ProgramInterpreter pgm; // "Script" Interpreter

	public ArrayList<Mobile> moving = new ArrayList<Mobile>(); // list of players who are currently moving

	public HashMap<Room, List<Player>> listenersLists; // possibly replace per room listener lists? (UNUSED)

	public Timer timer = new Timer(); // Timer object with thread for executing TimerTask(s) for Spells, Effects

	// timer maps
	private HashMap<Player, List<SpellTimer>> spellTimers = new HashMap<Player, List<SpellTimer>>();       // spell cooldown timers
	private HashMap<Player, List<EffectTimer>> effectTimers = new HashMap<Player, List<EffectTimer>>();    // effect timers (effects end when timer ends)
	private HashMap<Player, List<AuctionTimer>> auctionTimers = new HashMap<Player, List<AuctionTimer>>(); // auctions timers (auctions end when timer ends)

	private HashMap<Player, Party> partyInvites = new HashMap<Player, Party>(); // party invitations

	private HashMap<String, Bank> banks = new HashMap<String, Bank>();          // banks

	// global nameref table
	private HashMap<String, Integer> nameRef = new HashMap<String, Integer>(); // store global name references to dbref numbers (i.e. $this->49)

	// global quest table
	private ArrayList<Quest> quests = new ArrayList<Quest>();                       // global quest table -- all root quest objects should be loaded here
	private Map<Zone, List<Quest>> questsByZone = new HashMap<Zone, List<Quest>>(); // mapping of zones to a list of quests within them
	
	private Map<String, Item> prototypes = new Hashtable<String, Item>();
	private Map<String, Thing> prototypes1 = new Hashtable<String, Thing>();

	private boolean firstRun = false;   // is the first time the software been's run (if so, we need to some basic file creation and setup)
	
	private boolean test = false;
	private boolean weather = false;
	private boolean use_accounts = true;
	private boolean int_login = false;

	private boolean input_hold = false; // used for automatic backups to tell the server to not accept new input while running backup
	
	private Hashtable<Client, String> clientState = new Hashtable<Client, String>();
	private HashMap<Client, loginData> pLoginData = new HashMap<Client, loginData>();
	private Hashtable<Client, ClientData> clientData = new Hashtable<Client, ClientData>();

	// cmd lists
	private static final String[] user_cmds = new String[] {
			"ask", "attack", "auction", "auctions",
			"balance", "bash", "bid", "boards", "buy",
			"calendar", "cast", "chargen", "cls", "colors", "commands", "condition",
			"date", "dedit", "describe", "drink", "drop",
			"effects", "equip", "exchange", "exits", "exp",
			"feats",
			"go", "get", "greet",
			"help", "home", "housing",
			"inspect", "interact", "inventory",
			"levelup", "list", "lock", "look", "lookat",
			"mail", "map", "money", "motd", "move", "msp",
			"page", "party", "passwd", "pconfig", "pinfo", "push", "put", "prompt",
			"quests", "quit",
			"roll", "run",
			"say", "score", "sell", "sheathe", "skillcheck", "spellinfo", "spells", "stats", "staff",
			"talk", "take", "target", "tell", "time", "trade", "travel",
			"unequip", "unlock", "use",
			"version", "vitals",
			"walk", "where", "who", "write"
	};

	private static final String[] build_cmds = new String[] {
			"@check", "@cedit",                       // @check check to see what exit props aren't set
			"@describe", "@dig", "@door", "@dungeon", // @describe describe an object, @dig dig a new room, @door create an arbitrary exit @dungeon dig a new dungeon
			"@examine",
			"@fail", "@flags",                        // @fail set exit fail message, @flags see flags on an object
			"@iedit",                                 // @iedit edit an item
			"@jump",                                  // @jump jump to a different room
			"@lsedit",                                // @lsedit edit a list
			"@makehouse",                             // @makehouse make a house
			"nameref",
			"@ofail", "@open",                        // @ofail set exit ofail message, @open open a new exit (1-way)
			"@recycle", "@redit",                     // @recycle recycle objects
			"@osucc", "@success"                      // @osucc set exit osuccess message, @success set exit success message
	};

	private static final String[] admin_cmds = new String[] {
		    "@alias",                                                         // @alias setup command aliases
			"@ban", "@bb",                                                    // @ban ban player, @bb use bulletin board
			"@config", "@control",                                            // @config change server configuration options, @control control an NPC
			"@debug",                                                         // @debug show debug information
			"@hash", "@hedit",                                                // @hash see hash of a string, @hedit edit help files
			"@listprops",                                                     // @listprops list properties on an object          
			"@nextdb",                                                        // @nextdb get the next unused dbref number
			"@pgm",                                                           // @pgm interpret a "script" program
			"@set", "@setskill", "@sethp", "@setlevel", "@setmana", "@setxp", // @set set properties on objects, @setskill set player skill values
			"@zones", "@zoneinfo"                                             // @zones setup,configure,modify zones
	};

	private static final String[] wiz_cmds = new String[] {
			"@backup",
			"@flag", "@flush",
			"@setmode", "@start",
			"@teleport"
	};

	private static final String[] god_cmds = new String[] {
			"@access",
			"@broadcast",
			"@load",
			"@sethour", "@setminute", "@shutdown",
			"@unload"
	};
	
	private Map<Client, Console> consoles = new HashMap<Client, Console>();
	
	private Hashtable<Client, Account> caTable = new Hashtable<Client, Account>(); // Client/Account Table
	
	private AccountManager acctMgr = new AccountManager(this); // Account Manager, holds and tracks Account objects
	
	// necessary so that cNames can properly map to player objects
	private Hashtable<String, Player> cNames = new Hashtable<String, Player>();
	private Hashtable<PClass, Integer> numPlayersOnlinePerClass = new Hashtable<PClass, Integer>();
	
	//List<Feat> feats = (ArrayList<Feat>) Utils.mkList(Feat.ap_light, Feat.ap_medium, Feat.ap_heavy);
	
	private Map<Client, Tuple<Editors, String>> interactMap = new Hashtable<Client, Tuple<Editors, String>>();
	
	mud.foe.Terminal term = null;
	private Map<Player, mud.foe.Terminal> terminals = new HashMap<Player, mud.foe.Terminal>(1, 0.75f);
	
	// map of trades in progress (between players), basically for each player there is a table that maps another player
	// they are trading with and the trade object describing that trade
	private Map<Player, Hashtable<Player, Trade>> trades = new Hashtable<Player, Hashtable<Player, Trade>>();
	
	// methods called for a particular type of MUDObject when we try to 'use' it (see cmd_use(...))
	Map<Class, Method> useMethods = new Hashtable<Class, Method>();
	
	// currency data (TEST)
	public static Currency COPPER = new Currency("Copper", "cp", null, 1);
	public static Currency SILVER = new Currency("Silver", "sp", COPPER, 100);
	public static Currency GOLD = new Currency("Gold", "gp", COPPER, 10000);
	public static Currency PLATINUM = new Currency("Platinum", "pp", COPPER, 1000000);
	
	public static Currency BOTTLE_CAPS = new Currency("bottle cap", "bc", null, 1);
	
	List<Race> races = new ArrayList<Race>();
	
	/*
	 * The idea here is to replace the use (in here) of calling the static item types
	 * in ItemType with referencing the name of the ItemType (in lowercase) from
	 * this map/table.
	 * 
	 * Additionally, this should make it a bit easier to work with "end-user" item types
	 * defined in supplemental code.
	 */
	Hashtable<String, ItemType> itemTypes = new Hashtable<String, ItemType>() {
		{
			put("armor",     ItemType.ARMOR);
			put("arrow",     ItemType.ARROW);
			put("book",      ItemType.BOOK);
			put("clothing",  ItemType.CLOTHING);
			put("container", ItemType.CONTAINER);
			put("ear_ring",  ItemType.EAR_RING);
			put("food",      ItemType.FOOD);
			put("helmet",    ItemType.HELMET);
			put("none",      ItemType.NONE);
			put("potion",    ItemType.POTION);
			put("ring",      ItemType.RING);
			put("weapon",    ItemType.WEAPON);
		}
	};
	
	private GameModule module;
	
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
			System.exit(-1);
		}

		try {
			server = new MUDServer(); // create server
			//server = new MUDServer("localhost", 4201); // create server

			System.out.println( Arrays.asList(args) );

			// process command line parameters
			for (int a = 0; a < args.length; a++) {
				final String s = args[a];

				if ( s.startsWith("--") ) {
					final String param =  s.substring(2, s.length());

					if ( param.equals("port") ) {
						server.port = Utils.toInt(args[a+1], 4201);
						System.out.println("Using port " + server.port);
					}
					else if ( param.equals("debug") ) {
						server.debug = 1;
						System.out.println("Debugging Enabled.");
					}
					else if ( param.equals("enable-logging") ) {
						server.logging = true;
						System.out.println("Logging Enabled.");
					}
					else if ( param.equals("db") ) {
						server.mainDB = server.DATA_DIR + "databases\\" + args[a + 1];
						System.out.println("Using database " + args[a + 1]);
					}
					else if ( param.equals("dir") ) {
						//server.MAIN_DIR = "";
						//server.DATA_DIR = "";
					}
					else if ( param.equals("theme") ) {
						server.THEME_FILE = server.THEME_DIR + args[a + 1];
						System.out.println("Using theme " + args[a+1]);
					}
					else if ( param.equals("setup") ) {
						server.firstRun = true;
					}
					else if ( param.equals("telnet") ) {
						server.telnet = Utils.toInt(args[a + 1], 0);
					}
					else if ( param.equals("test") ) {
						server.test = true;
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

		System.out.println("");

		// Tell Us where the program is running from
		System.out.println("Current Working Directory: " + new File("").getAbsolutePath());
		System.out.println("MAIN_DIR: " + MAIN_DIR);

		System.out.println("");

		System.out.println("Important!: The two above should be the same if the top is where you have the program AND it's data");

		System.out.println("");

		// search for needed directories and files (using predefined names and locations)
		ArrayList<File> directories = new ArrayList<File>();
		ArrayList<File> files = new ArrayList<File>();

		// directories
		directories.add(new File(MAIN_DIR));
		directories.add(new File(DATA_DIR));
		
		directories.add(new File(BACKUP_DIR));
		directories.add(new File(BOARD_DIR));
		directories.add(new File(CONFIG_DIR));
		directories.add(new File(HELP_DIR));
		directories.add(new File(MAP_DIR));
		directories.add(new File(MOTD_DIR));
		directories.add(new File(SPELL_DIR));
		directories.add(new File(THEME_DIR));
		directories.add(new File(LOG_DIR));
		directories.add(new File(SESSION_DIR));
		directories.add(new File(WORLD_DIR));

		// check that the directories exist, if not create them
		for (final File dir : directories) {
			if ( !dir.exists() ) {
				boolean success = dir.mkdir();

				if (success) {
					System.out.println("Directory: " + dir.getAbsolutePath() + " created");
				}  
			}
			else {
				System.out.println("Directory: " + dir.getAbsolutePath() + " exists.");
			}
		}
		
		/*files.add(new File(ALIASES_FILE));
		files.add(new File(BANLIST_FILE));
		files.add(new File(CHANNELS_FILE));
		files.add(new File(CONFIG_FILE));
		files.add(new File(FORBIDDEN_FILE));

		// check that the files exist, if not create them
		for (final File file : directories) {
			boolean success = false;

			try {
				success = file.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (success) {
				System.out.println("File: " + file.getAbsolutePath() + " created");
			}  
			else {
				System.out.println("file: " + file.getAbsolutePath() + " exists.");
			}
		}*/

		System.out.println("");

		// if this is the first run (as indicated by setup parameter)
		if( firstRun ) {
			System.out.print("Running initial setup...");
			create_data();
			System.out.println("Done");
		}

		// Logging
		if ( logging ) { // if logging is enabled, create a log object and open it
			// instantiate log objects (using no max length, no buffer -- for now)
			this.log = new Log("log", false, -1);        // main log - character actions, etc
			this.debugLog = new Log("debug", false, -1); // debug log - any and all debugging
			this.chatLog = new Log("chat", false, -1);   // chat log - all chat messages

			// open log files for writing
			this.log.openLog();
			this.debugLog.openLog();
			this.chatLog.openLog();
			
			// tell us it's enabled.
			debug("Logging Enabled.");
			//
			//debug("Logs Initialized.");
		}
		else {
			// tell us it's disabled
			debug("Logging Disabled.");
		}

		debug(""); // formatting

		// Theme Loading
		this.loadTheme(THEME_FILE);
		
		day = theme.getDay();
		month = theme.getMonth();
		year = theme.getYear();
		
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

		// load spells
		loadSpells(Utils.loadStrings(spellDB));
		System.out.println("Spells Loaded!");

		System.out.println("");
		
		// Load Zones (only doing this here, because Rooms may be in a zone, and so
		// by loading Zones first then rooms can be placed in them by the ObjectLoader
		loadZones(WORLD_DIR + theme.world + "\\zones.txt");
		
		loadRaces();
		
		if( mainDB.endsWith("foe.txt") ) rules = mud.foe.FOESpecial.getInstance();
		else rules = D20.getInstance();
		
		Player.ruleset = rules; // set static Ruleset reference in Player
		
		// Load everything from databases by flag
		ObjectLoader.loadObjects(loadListDatabase(mainDB), this, objectDB, this);
		System.out.println("Database Loaded!");

		System.out.println("");

		// Post-Room Loading
		loadExits();              // load exits
		placeThingsInRooms();     // load thing
		loadItems();              // load items
		//stackItems();           // put items inside of the objects they belong 

		objectDB.stackItems();

		System.out.println("");

		//
		fillShops();

		// TODO FIX THIS
		// make sure npcs are added to listeners
		for (NPC npc : objectDB.getNPCs()) {
			if( npc != null ) {
				debug("NPC(" + npc.getName() + "): " + npc.getLocation());
				getRoom(npc.getLocation()).addListener(npc);
			}
			else debug("NULL NPC?");
		}

		// instantiate banned ip list
		banlist = loadListDatabase(BANLIST_FILE);

		// instantiate forbidden names list
		forbiddenNames = loadListDatabase(FORBIDDEN_FILE);

		// load configuration data (file -- default.config)
		//loadConfiguration(CONFIG_DIR + "config.txt", configs); ?
		/*for (final String s : loadListDatabase(CONFIG_DIR + "config.txt")) {
			final String[] configInfo = s.split(":");
			String name = Utils.trim(configInfo[0]);
			String value = Utils.trim(configInfo[1]);
			value = value.substring(0, value.indexOf('#'));
			config.put(name, value);
		}*/

		// print out config map
		//debug(config.entrySet());

		System.out.println("");

		// help file loading
		System.out.println("Loading Help Files... ");
		
		for (final String helpFileName : generateHelpFileIndex())
		{
			debug(helpFileName);
			
			final String[] helpfile = Utils.loadStrings(this.HELP_DIR + helpFileName);
			helpTable.put(helpfile[0], helpfile);
			
			if( helpTable.containsKey( helpfile[0] ) ) {
				//debug("HelpFile Loaded!");
			}
			else debug("Error!");
		}
		
		System.out.println("Help Files Loaded!");

		System.out.println("");

		// topic file loading
		System.out.println("Loading Topic Files... ");
		
		for (final String topicFileName : generateTopicFileIndex())
		{
			debug(topicFileName);
			
			final String[] topicfile = Utils.loadStrings(this.TOPIC_DIR + topicFileName);
			topicTable.put(topicfile[0], topicfile);
			
			if( topicTable.containsKey( topicfile[0] ) ) {
				//debug("Topic Loaded!");
			}
			else debug("Error!");
		}
		
		System.out.println("Topic Files Loaded!");

		System.out.println("");

		debug("Colors (ANSI): " + ansi_colors.keySet()); // DEBUG
		debug("Colors (XTERM): " + xterm_colors.keySet()); // DEBUG

		// set up display colors (ANSI, XTERM256)
		setDisplayColor("exit",     "green",   "green");
		setDisplayColor("player",   "magenta", "orange");
		setDisplayColor("npc",      "cyan",    "cyan");
		setDisplayColor("thing",    "yellow",  "yellow");
		setDisplayColor("room",     "green",   "other");
		setDisplayColor("item",     "yellow",  "yellow");
		setDisplayColor("creature", "cyan",    "cyan");
		setDisplayColor("quest"   , "cyan",    "cyan");

		debug("Object Colors: " + displayColors.entrySet()); // DEBUG

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
		this.commandMap.put("@access", new AccessCommand(this));     //
		//this.commandMap.put("@alias", new AliasCommand(this));       //
		this.commandMap.put("attack", new AttackCommand(this));      //
		this.commandMap.put("cast", new CastCommand(this));          //
		this.commandMap.put("drop", new DropCommand(this));          //
		this.commandMap.put("@examine", new ExamineCommand(this));   //
		this.commandMap.put("greet", new GreetCommand(this));        //
		this.commandMap.put("mail", new MailCommand(this));          //
		//this.commandMap.put("@teleport", new TeleportCommand(this)); //
		this.commandMap.put("where", new WhereCommand(this));        //

		debug("Mapped Commands: " + commandMap.keySet());            // Print out all the command mappings (DEBUG)
		
		/* Set up Command Aliases */
		loadAliases(this.ALIASES_FILE);

		for(String alias : aliases.keySet()) {
			String command = aliases.get(alias);
			debug(alias + " -> " + command);
		}

		/* bulletin board(s) */
		this.bb = new BulletinBoard(serverName + " board");

		final ArrayList<String> entries = loadListDatabase(BOARD_DIR + "bboard.txt");
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
				System.out.println("--- Stack Trace ---");
				nfe.printStackTrace();
			}
		}

		// store the board with a shortname
		this.boards.put("board", bb);

		System.out.println("");

		/* auctions ? */
		auctions = new ArrayList<Auction>();

		/* Server Initialization */

		// almost everything that needs to be loaded should be done before here
		System.out.println("Creating server on port " + port);
		this.s = new Server(this, port);

		System.out.println("");

		// Time Loop
		// cpu: -for now, appears marginal-
		game_time = new TimeLoop(this, DAYS, year, month, day, game_hour, game_minute);
		new Thread(game_time, "time").start();
		System.out.println("Time (Thread) Started!");

		// Weather Loop
		// cpu: ~20%
		//System.out.println("Weather (Thread) Started!");

		System.out.println("");
		System.out.println("");

		if( test ) {
			weather_test(); // set up basic weather system of sorts (should generally be okay on a new database all though it may not be desired)
			world_test();   // set up some testing (probably not valid on your database)
		}

		System.out.println("");

		loadChannels(CHANNELS_FILE); // load chat channels

		System.out.println("");

		accounts = new ArrayList<Account>();

		if( use_accounts ) {
			System.out.println("Loading Accounts...");

			loadAccounts(ACCOUNT_DIR); // load Player Accounts

			System.out.println("Done");

			System.out.println("");
		}

		if (acctMgr.numAccounts() == 0) {
			debug("No accounts.");
		}
		
		// Initialize Program Interpreter
		pgm = new ProgramInterpreter(this);
		
		/**
		 * TODO fix this kludge by finding an appropriate spot for it (it's kind of theme specific...)
		 */
		numPlayersOnlinePerClass.put( Classes.BARBARIAN, 0 );
		numPlayersOnlinePerClass.put( Classes.BARD,      0 );
		numPlayersOnlinePerClass.put( Classes.CLERIC,    0 );
		numPlayersOnlinePerClass.put( Classes.DRUID,     0 );
		numPlayersOnlinePerClass.put( Classes.FIGHTER,   0 );
		numPlayersOnlinePerClass.put( Classes.MONK,      0 );
		numPlayersOnlinePerClass.put( Classes.NONE,      0 );
		numPlayersOnlinePerClass.put( Classes.PALADIN,   0 );
		numPlayersOnlinePerClass.put( Classes.RANGER,    0 );
		numPlayersOnlinePerClass.put( Classes.ROGUE,     0 );
		numPlayersOnlinePerClass.put( Classes.SORCERER,  0 );
		numPlayersOnlinePerClass.put( Classes.WIZARD,    0 );

		System.out.println("");

		System.out.println("Server> Setup Done.");

		System.out.println("Next Database Reference: " + objectDB.peekNextId() );
		
		System.out.println( checkMem() );
	}

	private void create_data() {		
		// generate blank motd
		String[] motdData = new String[] {
				"*** Welcome to:", "",
				"<insert mud name or initial graphic here>", "",
				"<other info>", "",
				"<connection details>", "",
				"To connect to your character use 'connect <playername> <password>'",
				"To create a character use 'create <playername> <password>'"
		};

		Utils.saveStrings(MOTD_DIR + "motd.txt", motdData);

		// generate blank/basic config files
		Utils.saveStrings(CONFIG_DIR + "aliases.conf", new String[] {
				"# Command Aliases File",
				"alias north:n", "alias northeast:ne", "alias northwest:nw",
				"alias south:s", "alias southeast:se", "alias southwest:sw",
				"alias east:e",
				"alias west:w",
				"alias inventory:inv,i",
				"alias look:l",
				"alias pconfig:pconf",
				"alias quit:QUIT"
		} );
		Utils.saveStrings(CONFIG_DIR + "banlist.txt", new String[] { "# Banlist" } );
		Utils.saveStrings(CONFIG_DIR + "channels.txt", new String[] { "Support,0", "Testing,0" } );
		Utils.saveStrings(CONFIG_DIR + "forbidden.txt", new String[] { "# Forbidden names list" } );

		// generate an single, default message for the mud-wide bulletin board
		Utils.saveStrings(BOARD_DIR + "bboard.txt", new String[] { "0#admin#Welcome#Test Message" } );
		
		String[] theme = new String[] {
				"[theme]",
				"name = MUD",
				"mud_name = DefaultMUD",
				"motd_file = motd.txt",
				"start_room = 0",
				"world = world",
				"db = db.txt",
				"[/theme]",
				"",
				"[calendar]",
				"day = 1",
				"month = 1",
				"year = 0",
				"season = summer",
				"[/calendar]",
				"",
				"// number = name",
				"[months]",
				"[/months]",
				"",
				"[months_alt]",
				"[/months_alt]",
				"",
				"// month,day = name",
				"[holidays]",
				"[/holidays]",
				"",
				"[years]",
				"[/years]"
		};
		
		Utils.saveStrings(THEME_DIR + "default.thm", theme);
		
		
		// generate an empty world directory
		final File temp = new File(WORLD_DIR + "world");
		
		if( !temp.exists() ) {
			boolean success = temp.mkdir();

			if (success) {
				System.out.println("Directory: " + temp.getAbsolutePath() + " created");
			}  
		}
		else {
			System.out.println("Directory: " + temp.getAbsolutePath() + " exists.");
		}
		
		String[] zones = new String[] {
				"# Zones"
		};
		
		Utils.saveStrings(WORLD_DIR + "world\\zones.txt", zones);
		
		// create plain, mostly empty database (contains a single room and an admin player)
		/** TODO: rework this so I can simply create a new room that's not in a database and ask for it's
		 * string equivalent...
		 */ 
		String[] dbData = new String[] {
				"0#Main_Environment_Room#RS#You see nothing.#0#N#10,10,10#-1#-1",
				"1#admin#P#admin character#0#" + Utils.hash("password") + "#0,0,0,0,0,0#0,0,0,0#4#0#10#OOC#0"
		};

		Utils.saveStrings(mainDB, dbData);

		// Error Message Localization
		Utils.saveStrings(DATA_DIR + "errors_en.txt", new String[] { "1:Invalid Syntax!", "2:NaN Not a Number!" });
		Utils.saveStrings(DATA_DIR + "errors_fr.txt", new String[] { "1:Syntaxe Invalide!", "2:NaN N'est pas un nombre!" });
		
		// create topics directory
		final File temp1 = new File(TOPIC_DIR);
		
		if( !temp1.exists() ) {
			boolean success = temp1.mkdir();

			if (success) {
				System.out.println("Directory: " + temp1.getAbsolutePath() + " created");
			}  
		}
		else {
			System.out.println("Directory: " + temp1.getAbsolutePath() + " exists.");
		}
	}
	
	private void weather_test() {
		/* Weather Testing -- DB Safe */
		
		weather = true;

		// create some weather states
		WeatherState ws1 = new WeatherState("Clear Skies", 1, false, false, false, false);
		ws1.description = "The sky is clear{DAY? and blue}{NIGHT? and flecked with stars.  Moonlight faintly illuminates your surroundings}.";
		ws1.transUpText = "Your surroundings brighten a little as the {DAY?sun}{NIGHT?moon} peeks through thinning clouds.";

		WeatherState ws2 = new WeatherState("Cloudy", 0.5, false, false, true, false);
		ws2.description = "The air is dry for now, but clouds cover the sky.  It might rain soon.";
		ws2.transDownText = "It's getting cloudy.";
		ws2.transUpText = "The rain seems to have stopped for now.";

		WeatherState ws3 = new WeatherState("Rain", 0.25, true, false, true, false);
		ws3.description = "Above the pouring rain hangs a gray and solemn sky.";
		ws3.transDownText = "Rain begins to spot your surroundings.";
		ws3.transUpText = "The flashes of lightning taper off as the thunder goes quiet, the sound fading till all that can be heard it the pouring rain.";

		WeatherState ws4 = new WeatherState("Thunderstorm", 0, true, true, true, true);
		ws4.description = "Thunder and lightning light up the sky, punctuating the sound of heavy rain.";
		ws4.transDownText = "You hear a the boom of thunder and catch a glimpse of a sudden flash in the distance.";

		WeatherState ws5 = new WeatherState("Winter Storm", 0, false, false, false, true);
		ws5.description = "Blinded by a tempest of snow swirling above, you assume the sky looms black.";
		ws5.transDownText = "Occasional gusts become a river of frigid air, the snow a blinding swirl of gray.";

		// create a seasonal weather profile, and hand it the states we created
		Season spring = new Season("Spring", ws1, ws2, ws3);
		Season summer = new Season("Summer", ws1, ws2, ws3, ws4);
		Season fall = new Season("Fall", ws1, ws2, ws3);
		Season winter = new Season("Winter", ws2, ws3, ws5);

		// create a weather object, handing it our current season and a starting weather state
		Weather weather = new Weather(summer, ws4);

		// apply our new weather object to each "outside" room
		for (final Room room1 : objectDB.getWeatherRooms()) {
			room1.setWeather(weather);
		}
	}

	private void world_test() {
		// ex. ansi, true
		// ex. prompt, "< %mode %h/%H %m/%M %state >"
		// ex. prompt_enabled, true
		
		/**
		 * World/Game/Setting Test Cases
		 * 
		 * Fallout Equestria (foe.txt) [Fallout]
		 * Forgotten Realms  (db.txt)  [Dungeons and Dragons 3E?]
		 * 
		 * NOTE: It is strongly advised to use a database name other than the ones
		 * noted above to avoid triggering the following special setup intended
		 * just for those, which also expects classes not provided on GitHub
		 * with the rest of the code.
		 */
		
		/* FOE Items Testing -- NOT DB Safe */
		if( mainDB.endsWith("foe.txt") ) { // Fallout Equestria testing database (this code uses stuff from a package not included on github)
			//rules = mud.foe.FOESpecial.getInstance();
						
			try {
				registerUseMethod(mud.foe.Terminal.class, mud.foe.FalloutEquestria.class.getMethod("use_terminal", mud.foe.Terminal.class, Client.class));
			} catch (NoSuchMethodException e) {
				debug("No such method " + e.getMessage() + " in ");
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Abilities: " + Arrays.asList(rules.getAbilities()));
			
			// pull in prototypes
			prototypes.putAll( new mud.foe.FalloutEquestria().getItemPrototypes() );
			
			/* Buildings Test */
			Edge[] edges = new Edge[8];
			edges[0] = new Edge(new Point(10,10), new Point(20,10));
			edges[1] = new Edge(new Point(20,10), new Point(22,12));
			edges[2] = new Edge(new Point(22,12), new Point(22,14));
			edges[3] = new Edge(new Point(22,14), new Point(20,16));
			edges[4] = new Edge(new Point(20,16), new Point(10,16));
			edges[5] = new Edge(new Point(10,16), new Point(8,14));
			edges[6] = new Edge(new Point(8,14), new Point(8,12));
			edges[7] = new Edge(new Point(8,12), new Point(10,10));
			
			getNPC("Life Bloom").setGreeting("Welcome to Tenpony tower.");
			getNPC("Ditzy Doo").setGreeting("Ditzy Doo smiles at you, which is oddly nice and also looks gross.");
			
			//Building building = new Building("Ministry of Arcane Sciences", "MAS", edges);

			Room atrium = getRoom("Atrium");
			Room atrium2 = getRoom("Atrium2");
			Room it = getRoom("IT Center");
			
			atrium2.setProperty("/visuals/grass", "On a closer inspection, the \"grass\" is made of an unknown substance,probably inorganic. Looks pretty good, still you probably wouldn't want to eat it.");
			
			// Safe
			Box box = new Box("Safe", "A heavy metal safe");
			
			box.lock();
			
			initCreatedThing(box);
			box.setLocation(atrium2.getDBRef());
			atrium2.addThing(box);
			
			/*Item wsg = new Item(-1);
			wsg.setName("The Wasteland Survival Guide");
			wsg.setItemType(ItemType.NONE);
			wsg.setDesc("A modestly thick, black book with a white equine skull on the cover.");
			
			wsg.setAuctionable(false);
			
			initCreatedItem(wsg);
			wsg.setLocation(atrium2.getDBRef());
			atrium2.addItem(wsg);*/
			
			// PipBuck (foe version of PipBoy from Bethesda's Fallout video games)
			mud.foe.PipBuck pb = new mud.foe.PipBuck("PipBuck 3000");
			
			initCreatedItem(pb);
			//pb.setLocation(box.getDBRef());
			pb.setLocation(atrium2.getDBRef());
			//box.insert(pb);
			atrium2.addItem(pb);
			
			// StealthBuck - PipBuck Stealth/Invis Module
			mud.foe.StealthBuck sb = new mud.foe.StealthBuck();
			
			initCreatedItem(sb);
			//sb.setLocation(box.getDBRef());
			sb.setLocation(atrium2.getDBRef());
			//box.insert(sb);
			atrium2.addItem(sb);
			
			// Disruptor - PipBuck Disruptor Module
			mud.foe.Disruptor dr = new mud.foe.Disruptor();
			
			initCreatedItem(dr);
			//dr.setLocation(box.getDBRef());
			dr.setLocation(atrium2.getDBRef());
			//box.insert(dr);
			atrium2.addItem(dr);

			// Sparkle Cola Vending Machine
			Thing vending_machine = new Thing("Vending Machine", "What stand before you is a grime-coated relic of it's former glory. The once glorious purple and gold Sparkle Cola ad has " +
					"long since faded, though a few splotches of color remain to remind you of it's former state. In several spots the paint has begun peeling off the metal " +
					"and rust peeks out from beneath it.");
			
			initCreatedThing(vending_machine);
			
			vending_machine.setProperty("thingtype", "machine");
			vending_machine.setProperty("machinetype", "vending_machine");
			vending_machine.setProperty("inventory/sparkle_cola", 10);
			vending_machine.setProperty("inventory/sparkle_cola_rad", 10);
			vending_machine.setProperty("selection/0", "sparkle_cola");
			vending_machine.setProperty("selection/1", "sparkle_cola_rad");
			vending_machine.setProperty("money", 0);
			
			//{if:{eq:{prop:money,291}, 1},{give:{&player},{create_item:mud.foe.sparkle_cola}},Insufficient Money!}
			//{tell:{&player},{&this} dispenses a bottle of Sparkle-Cola}

			vending_machine.setProperty("money", 3);
			
			// IF has money && sparkle_cola are valid drink/exist && sparkle_colas > 0 THEN create new sparkle_cola and give to player and decrease money by 3 bits and decrease
			// count of remaining sparkle_cola by 1
			
			// TODO resolve issue with scripting system that makes it not work when you want to execute
			// three functions in succession
			vending_machine.setScriptOnTrigger(TriggerType.onUse,
					"{if:{ge:{prop:money,{&this}},1},{if:{ge:{prop:inventory/sparkle_cola,{&this}},1},{do:{rainbow:Enough Money!},{set:money,{&this},{sub:{prop:money,{&this}},1}},{set:inventory/sparkle_cola,{&this},{sub:{prop:inventory/sparkle_cola,{&this}},1}},{tell:PCHING! A bottle of sparkle cola!,{&player}},{give:{&player},{create_item:mud.foe.sparkle_cola}}},{tell:Sold Out!,{&player}}},{tell:Insufficient Funds!,{&player}}}"
			);
			
			System.out.println("# of Sparkle Cola(s) left: " + vending_machine.getProperty("inventory/sparkle_cola", Integer.class));
			System.out.println("# of Sparkle Cola Rad(s) left: " + vending_machine.getProperty("inventory/sparkle_cola_rad", Integer.class));

			vending_machine.setLocation(atrium2.getDBRef());
			atrium2.addThing(vending_machine);
			
			Item notebook = new Item(-1);
			notebook.setName("Notebook");
			notebook.setItemType(ItemType.NONE);
			notebook.setDesc("A blank paper notebook, a remnant of pre-war Equestria.");
			
			initCreatedItem(notebook);
			notebook.setLocation(atrium2.getDBRef());
			atrium2.addItem(notebook);
			
			Book book = new Book("Wasteland Survival Guide", "Ditzy Doo", 250);
			book.setItemType(ItemType.BOOK);
			book.setDesc("A modestly thick, black book with a white equine skull on the cover.");
			
			book.setAuctionable(false);
			
			book.addPage(0, 
					Utils.mkList(
							"Chapter 1: Basics of Pony Biology",
							"",
							"The things that everypony should know:",
							"- water, I need it, you need it, the raiders need it. Absolutely Essential.",
							"- food, same as with water, easier to find though. see chapter 3"
							));
			book.addPage(1,
					Utils.mkList(
							"Chapter 2: Dangers of the Wasteland",
							"You want dangers, we've got them all, right here for you."
							));
			book.addPage(2,
					Utils.mkList(
							"Raiders",
							"",
							"A bunch of nasty ponies if there ever was such a thing. They want your stuff and they'll happily",
							"end your life to get it. And that's just if you're lucky. Be an unlucky pony and you'll end up enslaved.",
							"Thing is, that wouldn't be so terrible, maybe, except that they'll put an explosive collar on you.",
							"Move two steps out of line and *BOOM* no more head. Yeah, loss of your head is fatal."
							));
			book.addPage(3,
					Utils.mkList(
							"Yao Guai",
							"",
							"Ah, for the golden days of Old Equestria. Once just ordinary Ursas,",
							"until your favorite friend, magical radiation, did a stellar job of",
							"creating an A-1 menace."
							));
			
			initCreatedItem(book);
			book.setLocation(atrium2.getDBRef());
			atrium2.addItem(book);
			
			Thing pipbuck_machine = new Thing("Pipbuck Machine", "A well-preserved and clean, but slight rusty machine. There is an"
					+ "inactive Stable-tec terminal embedded in it and below that a circular receptacle. Above the circular hole"
					+ "there is a hoof shape engraved into the metal plate that serves as the front of the machine. Flecks of black"
					+ "stuff seem to suggest that perhaps it was once filled in with paint for more contrast. Above the symbol the"
					+ "words \"Insert Hoof Here\" are engraved.");
			
			pipbuck_machine.setProperty("thingtype", "machine");
			pipbuck_machine.setProperty("machinetype", "pipbuck_machine");
			pipbuck_machine.setProperty("contents/pipbuck", 1000);
			
			pipbuck_machine.setScriptOnTrigger(
					TriggerType.onUse,
					"{if:{gt:{prop:contents/pipbuck,"+pipbuck_machine.getDBRef()+"},0},{give:{&player},{create_item:mud.foe.pipbuck}},{tell:Insufficient Pipbucks Available!,{&player}}}"
					);
			
			initCreatedThing(pipbuck_machine);
			pipbuck_machine.setLocation(it.getDBRef());
			it.addThing(pipbuck_machine);
			
			System.out.println("# of PipBuck(s) left: " + pipbuck_machine.getProperty("contents/pipbuck", Integer.class));
			
			//mud.foe.Terminal terminal = new mud.foe.Terminal("Terminal");
			//terminal.setName("Terminal");
			//terminal.setDesc("A Stable-Tec terminal, old pre-war technology whose durability is plain to see. On the screen, passively glowing green text indicates that it awaits input.");
			//terminal.setPowerState(mud.foe.Terminal.pwr_states.POWER_ON);
			
			mud.foe.Terminal terminal = new mud.foe.Terminal(
					"Terminal",
					"A Stable-Tec terminal, old pre-war technology whose durability is plain to see. On the screen, passively glowing green text indicates that it awaits input.",
					mud.foe.Terminal.pwr_states.POWER_ON
					);
			
			initCreatedThing(terminal);
			terminal.setLocation(atrium2.getDBRef());
			atrium2.addThing(terminal);
			
			//Thread t = new Thread( terminal, "terminal" );
			//t.start();
			
			// Spark Generator
			Thing spark_generator = new Thing("Spark Generator", "This advanced piece of magitech produces near limitless electric power via magic");
			
			Thing SparkGenerator = new Thing(-1);
			SparkGenerator.setName("Spark Generator");
			SparkGenerator.setDesc("This advanced piece of magitech produces near limitless electric power via magic");
			SparkGenerator.setLocation(-1);
			
			spark_generator.setProperty("thingtype", "spark_generator");
			spark_generator.setProperty("power", 10);
			
			prototypes1.put("mud.foe.spark_generator", SparkGenerator); 
			
			initCreatedThing(spark_generator);
			spark_generator.setLocation(it.getDBRef());
			it.addThing(spark_generator);
			
			// prototype - memory orb
			Item MemoryOrb = new Item(-1);
			MemoryOrb.setName("Memory Orb");
			MemoryOrb.setItemType(ItemType.NONE);
			MemoryOrb.setDesc(
					"A sphere of some crystalline substance. Beneath it's surface a misty "
					+ "substance swirls in ever-changing patterns. It emits gently pulsing "
					+ "light which shifts through the whole spectrum of visible colors.");
			MemoryOrb.setLocation(-1);
			MemoryOrb.setEquipType(ItemType.NONE);
			MemoryOrb.equippable = false;
			MemoryOrb.equipped = false;
			
			prototypes.put("mud.foe.memory_orb", MemoryOrb);
			
			// prototype - Sparkle Cola soda
			Item SparkleCola = new Item(-1);
			SparkleCola.setName("Sparkle Cola");
			SparkleCola.setItemType(ItemType.NONE);
			SparkleCola.setDesc(
					"A bottle of ancient, lukewarm Sparkle Cola. Probably just as good as it ever was.\n\n" +
					"Your Choice.\nThe Best in Equestria.\nSparkle Cola\nSoar into the sky."); 
			SparkleCola.setLocation(-1);
			SparkleCola.setDrinkable(true);
			SparkleCola.setEquipType(ItemType.NONE);
			SparkleCola.equippable = false;
			SparkleCola.equipped = false;
			
			SparkleCola.setScriptOnTrigger(TriggerType.onUse,
					"{do:{give:{&player},{create_item:mud.foe.bottlecap_sc}},{tell:You toss the bottle, keeping just the bottlecap.,{&player}}}"
			);
			
			System.out.println("SparkleCola(onUse Script): " + SparkleCola.getScript(TriggerType.onUse).getText());

			prototypes.put("mud.foe.sparkle_cola", SparkleCola);
			
			// prototype - bottle cap
			Item BottleCap = new Item(-1);
			BottleCap.setName("Bottle Cap");
			BottleCap.setItemType(ItemType.NONE);
			BottleCap.setDesc("A slightly bent metal bottle cap. Once a common waste product of the drinking habits of the Equestrian nation,"
					+ "now a valuable currency in the Equestrian Wasteland");
			BottleCap.setLocation(-1);
			BottleCap.setEquipType(ItemType.NONE);
			BottleCap.equippable = false;
			BottleCap.equipped = false;
			
			BottleCap.setProperty("type", "sc"); // Sparkle Cola bottle cap
			BottleCap.setProperty("value", 1);
			
			prototypes.put("mud.foe.bottlecap_sc", BottleCap);
			
			Item bc = createItem("mud.foe.bottlecap_sc", false);
			
			initCreatedItem(bc);
			bc.setLocation(atrium2.getDBRef());
			atrium2.addItem(bc);
			
			Item memory_orb = createItem("mud.foe.memory_orb", false);
			
			initCreatedItem(memory_orb);
			memory_orb.setLocation(box.getDBRef());
			box.insert(memory_orb);
			
			// prototype - weapon
			Weapon pgun = new Weapon();
			pgun.setName("10mm Pistol");
			pgun.setItemType(ItemType.WEAPON);
			pgun.setEquipType(ItemType.WEAPON);
			pgun.equippable = true;
			pgun.equipped = false;
			pgun.setDesc("A basic pistol, developed for the security forces of what used to be Equestria.");
			
			prototypes.put("mud.foe.weapons.pistol", pgun);
			
			Item pgun1 = createItem("mud.foe.weapons.pistol", false);
			
			initCreatedItem(pgun1);
			pgun1.setLocation(atrium2.getDBRef());
			atrium2.addItem(pgun1);
			
			// particular item
			//Weapon gun = (Weapon) new Item(-1);
			//Item gun = new Item(-1);
			Weapon gun = new Weapon();
			gun.setName("Gun");
			gun.setItemType(ItemType.WEAPON);
			//gun.setWeaponType(WeaponType.REVOLVER);
			gun.setEquipType(ItemType.WEAPON);
			gun.equippable = true;
			gun.equipped = false;
			gun.setDesc("A sturdy revolver with a mouth grip, clearly of earth pony make or at least designed for one.");

			gun.setProperty("name", "Little Macintosh");
			gun.setProperty("visual/engraving/number", "IF-18");
			gun.setProperty("visual/engraving/script", "Little Macintosh");
			gun.setProperty("weapon/maker", "Ironshod Firearms");
			gun.setProperty("weapon/model", "IF-18");
			gun.setProperty("weapon/type", "revolver");
			gun.setProperty("damage", 5);
			gun.setProperty("ammo_size", 0.44);
			gun.setProperty("ammo_type", "magnum");
			gun.setProperty("magazine", false);
			gun.setProperty("chambers", 6);

			initCreatedItem(gun);
			gun.setLocation(box.getDBRef());
			//gun.setLocation(atrium2.getDBRef());
			box.insert(gun);
			//atrium2.addItem(gun);
			
			Item laser_rifle = new Item(-1);
			laser_rifle.setName("Laser Rifle");
			laser_rifle.setItemType(ItemType.NONE);
			laser_rifle.setEquipType(ItemType.WEAPON);
			laser_rifle.equippable = true;
			laser_rifle.equipped = false;
			laser_rifle.setDesc("");
			
			laser_rifle.setProperty("damage",  10);
			laser_rifle.setProperty("ammo_type", "energy");
			
			initCreatedItem(laser_rifle);
			laser_rifle.setLocation(atrium2.getDBRef());
			atrium2.addItem(laser_rifle);
			
			Weapon laser_rifle1 = new Weapon("Laser Rifle", "An energy weapon modeled after a basic rifle.", 0.0);
			
			laser_rifle1.setItemType(ItemType.WEAPON);
			laser_rifle1.setEquipType(ItemType.WEAPON);
			laser_rifle1.equippable = true;
			laser_rifle1.equipped = false;
			laser_rifle1.setDesc("");
			
			laser_rifle1.setProperty("damage",  10);
			laser_rifle1.setProperty("ammo_type", "energy");
			
			initCreatedItem(laser_rifle1);
			laser_rifle1.setLocation(atrium2.getDBRef());
			atrium2.addItem(laser_rifle1);
			
			Item battle_saddle = new Item(-1);
			battle_saddle.setName("Battle Saddle");
			battle_saddle.setItemType(ItemType.NONE);
			battle_saddle.setEquipType(ItemType.ARMOR);
			battle_saddle.equippable = true;
			battle_saddle.equipped = false;
			battle_saddle.setDesc("");
			
			battle_saddle.setProperty("gun1", -1);
			battle_saddle.setProperty("gun2", -1);
			
			initCreatedItem(battle_saddle);
			battle_saddle.setLocation(atrium2.getDBRef());
			atrium2.addItem(battle_saddle);
			
			//laser_rifle.setLocation(battle_saddle.getDBRef());
			//laser_rifle1.setLocation(battle_saddle.getDBRef());
			//battle_saddle.setProperty("gun1", laser_rifle.getDBRef());
			//battle_saddle.setProperty("gun2", laser_rifle1.getDBRef());
		}
		
		/* Item/Creature/Quest Testing -- NOT DB Safe (expects a room to exist, etc) */
		//if( mainDB.endsWith("db.txt") ) { -- this is the right one but it triggers a mess
		// with a brand new db file (called db.txt) that doesn't contain the expected data
		if( mainDB.endsWith("db.txt") ) {			
			/* Arrow Testing -- Not DB Safe */
			Arrow a = new Arrow();
			initCreatedItem(a);
			a.setLocation( 8 );
			getRoom( 8 ).addItem(a);

			Arrow b;

			for(int i = 0; i < 15; i++) {
				b = new Arrow();
				initCreatedItem(b);
				a.stack(b);
			}
			
			/* Item Testing -- Not DB Safe */
			Jewelry ring = new Jewelry(ItemType.RING, "Ring of Invisibility", "A medium-sized gold ring with a smooth, unmarked surface.", new Effect("invisibility"));
			ring.setItemType(ItemType.RING);
			ring.setEquipType(ItemType.RING); // the type of equipment it is
			debug("Item Type: " + ring.getItemType() + " Equip Type: " + ring.getEquipType() );
			initCreatedItem(ring);
			ring.setLocation( 0 );
			getRoom(0).getItems().add(ring);

			Jewelry circlet = new Jewelry(ItemType.NONE, "Copper Circlet", "", new Effect("none"));
			circlet.setItemType(ItemType.RING);

			/* Item properties testing -- Not DB Safe */ 
			Item item = new Item();
			item.setItemType(ItemType.NONE);
			item.setName("Copper Ore");
			item.setDesc("A chunk of copper ore. Veins of copper swirl through the baser rock surrounding them.");
			item.setProperty("material", "copper");
			item.setProperty("purity", "0.90");
			initCreatedItem(item);
			item.setLocation(8);

			getRoom(8).addItem(item);
			
			Zone rdi = getZone("Red Dragon Inn");
			//Zone rdi = new Zone("Red Dragon Inn", null);
			//zones.put(rdi, 0);
			
			Room inn = getRoom(4); // Red Dragon Inn
			//rdi.addRoom( inn );
			
			Room basement = createRoom("Basement", -1);
			objectDB.addAsNew(basement);
			objectDB.addRoom(basement);
			rdi.addRoom( basement );
			
			int basement_dbref = basement.getDBRef();
			
			// add code for exits to and from basement -- 1/6/2014
			Exit down = new Exit("down", inn.getDBRef(), basement.getDBRef());
			Exit up = new Exit("up", basement.getDBRef(), inn.getDBRef());
			
			objectDB.addAsNew(down);
			objectDB.addExit(down);
			objectDB.addAsNew(up);
			objectDB.addExit(up);
			
			inn.getExits().add(down);
			basement.getExits().add(up);
			
			Creature c = new Creature("Mangy Rat", "A large, scruffy gray rat with red beady eyes and pointy teeth.");
			c.setMaxHP(15);
			c.setLocation(basement_dbref);
			c.setRace(Races.NONE);
			
			Creature c1 = new Creature( c );
			Creature c2 = new Creature( c );
			Creature c3 = new Creature( c );
			Creature c4 = new Creature( c );
			
			objectDB.addAsNew(c);
			objectDB.addCreature(c);
			objectDB.addAsNew(c1);
			objectDB.addCreature(c1);
			objectDB.addAsNew(c2);
			objectDB.addCreature(c2);
			objectDB.addAsNew(c3);
			objectDB.addCreature(c3);
			objectDB.addAsNew(c4);
			objectDB.addCreature(c4);

			Quest quest = new Quest("Help the Innkeeper", "The inn's basement is full of rats. Help the innkeeper out by killing a few.",
					rdi, new Task("Kill 5 rats", TaskType.KILL, basement, 5, c) );

			NPC npc = getNPC("Iridan");

			if( npc != null ) {
				System.out.println(npc.getName()); //
				npc.setQuestGiver(true);
				npc.addQuest( quest );             // assign the quest to this NPC
			}
			else { System.out.println("NPC is null!"); }

			quests.add(quest); // add to main quest table
			questsByZone.put(quest.getLocation(), Utils.mkList(quest));
			
			Merchant m1 = (Merchant) getNPC("Aran");
			Merchant m2 = (Merchant) getNPC("Terys");
			
			if( m1 != null ) { m1.setType("armor"); }
			if( m2 != null ) { m2.setType("weapon"); }
		}

		/* Bank Test -- DB Safe */
		Bank bank = new Bank("test");
		BankAccount acct = new BankAccount(0, Coins.platinum(1000));
		bank.addAcount(0, acct);
		banks.put(bank.getName(), bank);
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
			if ( checkAccess( getPlayer(client), newCmd.getPermissions() ) )
			{
				cmd(command, client);
			}
			else {
				System.out.println("Insufficient Access Permissions");
			}
		}
		catch(Exception e) {
			System.out.println("--- Stack Trace ---");
			e.printStackTrace();
		}

		if ( loginCheck( client ) ) {
			System.out.println("Client associated with login");
			prompt(client);
		}
	}

	private void runHelper(final Client client) {
		
		final String cs = getClientState(client);
		
		//System.out.println("cs: \"" + cs + "\"");
		
		if( cs != null && cs.equals("BUSY") ) {
			return;
		}
		
		if( cs != null && cs.equals("interactive_login") ) {
			final String temp = client.getResponse();
			//System.out.println("temp: \"" + temp + "\"");
			
			if( !temp.equals("") ) {
				client.setResponseExpected(false);
				
				echo(temp, client);
				
				// get client data
				final ClientData cd = getClientData(client);
				
				if( cd != null ) {
					if( cd.loginstate.equals("NAME") ) {
						cd.name = temp;
						cd.loginstate = "PASS";
					}
					else if( cd.loginstate.equals("PASS") ) {
						cd.pass = temp;
						cd.loginstate = "LOGIN";
					}
					
					System.out.println("NAME:  " + cd.name);
					System.out.println("PASS:  " + cd.pass);
					System.out.println("STATE: " + cd.loginstate);
					
					// enter next stage
					interactive_login(client, cd.loginstate);
				}
				else {
					System.out.println("CLIENT DATA is NULL.");
				}
			}
			
			return;
		}
		
		String whatClientSaid = client.getInput();

		// telnet negotiation
		/*if (client.tn) { // if the client is using telnet
			final byte[] clientBytes = whatClientSaid.getBytes();

			Telnet.send("IAC WILL MCCP", client);

			if (clientBytes[0] == 255) { // Telnet.IAC
				String telnetString = Telnet.translate(Arrays.copyOfRange(clientBytes, 0, 2));
				System.out.println("Telnet: " + telnetString);
				//processTelnetCommand(clientBytes);

			}

			//System.out.println( new String(clientBytes) );

			//if (clientBytes.length >= 3)    client.tn = false;
		}*/

		// If the client is not null and has something to say
		try {
			//if (whatClientSaid == null || "".equals(whatClientSaid)) {
			if (whatClientSaid == null) {
				final Player p = getPlayer(client);
				
				if( p != null ) {
					if( !p.isIdle() ) {
						p.setIdle(true);
					}
				}
				
				return;
			}
			// all the rest
			else {
				final Player p = getPlayer(client);

				if( p != null ) {
					if( p.isIdle() ) {
						p.setIdle(false);
						p.idle = 0;
					}
				}
				
				echo(whatClientSaid, client);
				cmd(whatClientSaid.trim(), client);
				
				//if( Arrays.asList(user_cmds).contains(whatClientSaid.trim() )
				//processCMD(new CMD(whatClientSaid.trim(), sclients.get(client), client, -1));

				/*if (!whatClientSaid.trim().equals("")) {  // blocks blank input
					//System.out.print("Putting comand in command queue...");
					processCMD(new CMD(whatClientSaid.trim(), sclients.get(client), client, -1));
					// put the command in the queue
					//cmd(whatClientSaid.trim(), c);
					//System.out.println("Done.");
				}*/

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

			s.write("Game> Fatal Exception! Shutting down...");

			// restart?
			//MUDServer.main(new String[] {"--port=4202", "--debug"} );
		}
	}

	// main loop
	private void run()
	{
		debug("Entering main program loop...");
		debug("Running? " + this.running);           // tell us whether the MUD server is running or not
		debug("Server? " + s.isRunning());           // tell us whether the underlying socket server is running

		while (running) {
			if( !input_hold ) {
				for (final Client client : s.getClients_alt()) {
					runHelper(client);
				}

				// chat messages
				Client client = null;
				Message msg = null;

				// for each ChatChannel
				for(ChatChannel cc : chan.getChatChannels()) {
					//debug("CHANNEL: " + cc.getName(), 4);

					String chan_name = cc.getName(), chan_color = cc.getChanColor(), text_color = cc.getTextColor();

					// get the next message
					msg = cc.getNextMessage();

					// if msg is null, continue
					if( msg == null ) {
						//debug(chan_name + ": No Messages", 4);
						continue;
					}

					//debug("Next Message (" + cc.getName() + "): " + msg.getMessage());
					
					/*
					 * for each listener of this channel, send the message
					 */
					for(Player player : cc.getListeners()) {
						debug("PLAYER: " + player.getName(), 4);

						try {
							client = player.getClient();

							if( player.getConfig().get("tagged-chat") ) {
								client.write("CHAT (" + colors(chan_name, chan_color) + ") " + "<" + msg.getSender().getName() + "> " + colors(msg.getMessage(), text_color) + "\r\n"); // send the message	
							}
							else {
								client.write("(" + colors(chan_name, chan_color) + ") " + "<" + msg.getSender().getName() + "> " + colors(msg.getMessage(), text_color) + "\r\n"); // send the message
							}

							chatLog.writeln("(" + chan_name + ") " + "<" + msg.getSender().getName() + "> " + msg.getMessage() + "\n");

							debug("chat message sent successfully", 4);
						}
						catch(NullPointerException npe) {
							debug("Game [chat channel: " + chan_name + "] > Null Message.");
							npe.printStackTrace();
						}
					}
				}
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
	 * @param _input client input (String)
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

		if (!inputList.isEmpty()) { // if there was any input
			cmd = inputList.remove(0);                  // grab the first element (which should be the command)

			debug("Command: \"" + cmd + "\"", 2);       // print command (pre-trim so we see any extra junk)
			cmd = cmd.trim();                           // trim the command (remove funny characters)
			debug("Command(trimmed): \"" + cmd + "\""); // print trimmed command
			//cmd = cmd.toLowerCase();                    // lowercase the command

			// if there were any arguments then get the argument, which is everything else
			arg = Utils.join(inputList, " ");  // get the arguments from the input list
			
			// if the command has arguments
			if (inputList.size() > 1) {
				// don't display/log the arguments to connect or create, since they contain sensitive user information,
				// including their name and password
				if (!cmd.toLowerCase().equals("connect") && !cmd.toLowerCase().equals("create")) {
					debug("Arguments: \"" + arg + "\"", 2);       // print arguments
					arg = Utils.trim(arg);                        // trim arguments
					debug("Arguments(trimmed): \"" + arg + "\""); // print trimmed arguments
				}
			}
		}

		debug("");

		// check for command alias (so we know there is one for later)
		boolean aliasExists = false;
		
		final String alias = aliases.get(cmd);
		
		if (alias != null) aliasExists = true;

		// restrict commands based on whether or not the connection has a logged-in player
		// if not logged-in
		// all of this should have configurable messages and maybe a time duration to
		// show for when the mode will return to normal
		if (!loginCheck(client))
		{
			if ( client.isConsole() ) {
				debug("CONSOLE");
				
				final Console console = consoles.get(client);
				
				if( console != null ) console.processInput(input, client);
				else send("Console not initialized?", client);
				
				return;
			}
			
			// if the client is marked somehow for registration or account login
			// then send the input to the respective handlers
			
			final String cs = getClientState(client);
			
			if ( cs != null ) {
				System.out.println("Client State (CS): " + "\'" + cs + "\'");
				
				/*if ( cs.equals("input") ) {
				}
				else if ( cs.equals("register") ) {
					handle_registration(arg, client);
					return;
				}
				else if( cs.equals("account_login") ) {
					handle_account_login(cmd, client);
					return;
				}
				else if( cs.equals("account_menu") ) {
					handle_account_menu(cmd, client);
					return;
				}
				else {
					setClientState(client, null); // clear any unintended client states
				}*/
				
				switch(cs) {
				case "input":
					break;
				case "register":
					handle_registration(arg, client);
					break;
				case "account_login":
					handle_account_login(cmd, client);
					break;
				case "account_menu":
					handle_account_menu(cmd, client);
					break;
				default:
					setClientState(client, null); // clear any unintended client states
					break;
				}
				
				return;
			}

			if (mode == GameMode.NORMAL) // Normal Running Mode (a.k.a. Mode 0)
			{	
				if(this.players.size() > this.max_players) {
					if( cmd.equals("connect") || cmd.equals("create") ) {
						send("Sorry. Maximum number of players are connected. Please try back later.", client);
						return;
					}
				}
				
				if ( cmd.equals("connect") || ( aliasExists && alias.equals("connect") ) ) {
					cmd_connect(arg, client); // pass arguments to the player connect function
				}
				else if ( cmd.equals("console") ) {
					cmd_console(arg, client);
				}
				else if ( cmd.equals("create") || (aliasExists && alias.equals("create") ) ) {
					cmd_create(arg, client); // pass arguments to the player creation function
				}
				else if( cmd.equals("help") ) {
					send("Available Commands: connect, console, create, help, register, quit, who", client);
				}
				else if( cmd.equals("register") ) {
					send("REGISTER", client);
					cmd_register(arg, client);
				}
				else if ( cmd.equals("quit") || (aliasExists && alias.equals("quit") ) ) {
					s.disconnect(client); // just kill the client?
				}
				else if ( cmd.equals("who") || ( aliasExists && alias.equals("who") ) ) {
					cmd_who(arg, client); // run the who function
				}
				else {
					send("That is not a known command.", client);
					debug("Command> Unknown Command");
				}
			}
			else if (mode == GameMode.WIZARD) // Wizard-Only Mode (a.k.a. Mode 1)
			{
				send("System is in Wizard-Only Mode.", client);
				if( cmd.equals("connect") || ( aliasExists && alias.equals("connect") ) )
				{
					cmd_connect(arg, client); // handles wizflag checking itself (since that's player dependent)
				}
				else if ( cmd.equals("console") ) {
					cmd_console(arg, client);
				}
				else if( cmd.equals("create") || ( aliasExists && alias.equals("create") ) )
				{
					send("Sorry, only Wizards are allowed to login at this time.", client);
				}
				else if( cmd.equals("help") ) {
					send("Available Commands: connect, console, create, help, quit, who", client);
				}
				else if( cmd.equals("quit") || ( aliasExists && alias.equals("quit") ) )
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
				s.disconnect(client); // just kill the client
			}
			else { // ? (any other mode number -- may indicate some kind of failure)
				send("System may be malfunctioning.", client);
				send("No Logins allowed. Booting Client...", client);
				s.disconnect(client); // just kill the client
			}
		}

		// if logged-in
		else if ( loginCheck(client) )
		{
			// get a hold of the player (reduce redundant getPlayer(client) calls)
			// also, replaced temp with player as far as @flag
			Player player = getPlayer(client);
			Room room = getRoom(player);
			
			// if the user is editing a list, pass their input to the list editor
			if ( player.getStatus().equals(Constants.ST_EDIT) ) {	
				final Editors editor = player.getEditor();

				switch(editor) {
				case AREA:     break;
				case CHARGEN:  op_chargen(input, client);      break;
				case CREATURE: op_creatureedit(input, client); break;
				case DESC:     op_dedit(input, client);        break;
				case HELP:     op_helpedit(input, client);     break;
				case INPUT:    op_input(input, client);        break;
				case INTCAST:  op_cast(input, client);         break;
				case ITEM:     op_itemedit(input, client);     break;
				case LIST:     op_listedit(input, client);     break;
				case MAIL:     handleMail(input, client);      break;
				case QUEST:    op_questedit(input, client);    break;
				case ROOM:     op_roomedit(input, client);     break;
				case SKILL:    op_skilledit(input, client);    break;
				case ZONE:     op_zoneedit(input, client);     break;
				case NONE:
					send("Exiting " + editor.getName(), client);
					break;
				default:
					break;
				}
			}
			else if ( player.getStatus().equals("CNVS") ) { // Conversation
				// TODO call conversation handler?
			}
			else if ( player.getStatus().equals("VIEW") ) { // viewing help files
				op_pager(input, client);
			}
			else if ( player.getStatus().equals("TERM") ) {
				debug("Using Terminal");
				
				debug("Terminal Input: \'" + input + "\'");
				
				int code = term.processInput( input );
				
				if( code == 0 ) {
					player.setStatus("IC");
					notify(player, "You quit using the terminal.");
				}
			}
			// else pass their input to command parsing
			else
			{
				// TODO there's a check for cmd == "" here
				//if( "".equals(cmd) ) return;
				
				// see if there are any namrefs, and if so evaluate them
				arg = nameref_eval(arg, client);

				debug("Argument(evaluated): \"" + arg + "\""); // print the trimmed argument

				/* Command Logging */

				if( logging ) {
					// Log all commands after login
					System.out.println("Command being logged...");
					
					log( Utils.trim(input), player );
					
					System.out.println("Command Logged!");
				}

				/* Grab a possible chat command */
				if(cmd.charAt(0) == '#') {
					final String channelName = cmd.substring(1);
					
					// chat channel invocation
					chatHandler(channelName, arg, client);

					return;
				}

				/* Command Evaluation */

				boolean buildCmd = false;
				boolean adminCmd = false;
				boolean wizCmd = false;
				boolean godCmd = false;

				debug("Entering god command loop...", 4);

				// stash god commands inside here
				if (player.getAccess() >= Constants.GOD) {
					// pass arguments to the access function
					if ( cmd.equals("@access") || ( aliasExists && alias.equals("@access") ) ) {
						godCmd = true;
						//cmd_access(arg, client);
						getCommand("@access").execute(arg, client);
					}
					else if ( cmd.equals("@broadcast") ) {
						godCmd = true;
						s.write("Game> " + getPlayer(client).getName() + " says, " + arg);
					}
					else if( cmd.equals("@load") ) {
						godCmd = true;

						send("Game> Command Not Implemented!", client);

						/*boolean success = cmd_loadc(arg, client);

						if( success ) {
							send("Game> Loaded " + commandMap.get(arg), client);
						}
						else {
							String[] args = arg.split("=");
							send("Game> Failed to load " + args[1] + " with command name " + args[0], client);
						}*/
					}
					else if( cmd.equals("@unload") ) {
						godCmd = true;

						send("Game> Command Not Implemented!", client);

						/*boolean success = cmd_unloadc(arg, client);

						if( success ) {
							send("Game> Unloaded " + arg + ".", client);
						}
						else {
							String[] args = arg.split("=");
							send("Game> Failed to unload " + args[1] + " with command name " + args[0], client);
						}*/
					}
					else if ( cmd.equals("@reload") || ( aliasExists && alias.equals("@shutdown") ) ) {
						if( arg.equals("") ) {
							sys_reload();
						}
						else {
							sys_reload(arg);
						}
					}
					// pass arguments to the shutdown function
					else if ( cmd.equals("@shutdown") || ( aliasExists && alias.equals("@shutdown") ) )
					{
						godCmd = true;
						cmd_shutdown(arg, client);
					}
					else if ( cmd.equals("@sethour") ) {
						godCmd = true;
						cmd_sethour(arg, client);
					}
					else if ( cmd.equals("@setminute") ) {
						godCmd = true;
						cmd_setminute(arg, client);
					}
					else if ( cmd.equals("@setweather") ) {
						godCmd = true;
						cmd_setweather(arg, client);
					}

					if (godCmd) {
						return;
					}
				}

				debug("Exited god command loop.", 4);

				debug("Entering build command loop...", 4);

				// stash build commands inside here
				if (player.getAccess() >= Constants.BUILD) {
					// pass arguments to the check function
					if ( cmd.equals("@check") || ( aliasExists && alias.equals("@check") ) )
					{
						buildCmd = true;
						// run the check function
						cmd_check(arg, client);
					}
					else if ( cmd.equals("@cedit") || ( aliasExists && alias.equals("@cedit") ) )
					{
						buildCmd = true;

						// indicate what is parsing commands
						debug("Command Parser: @cedit");

						// launch the item editor
						cmd_creatureedit(arg, client);
					}
					else if ( cmd.equals("@create_npc") || ( aliasExists && alias.equals("@create_npc") )) {
						buildCmd = true;
						createNPC(arg, getPlayer(client).getLocation());
					}
					else if ( cmd.equals("@dig") || ( aliasExists && alias.equals("@dig") ) )
					{
						buildCmd = true;
						cmd_dig(arg, client);
					}
					else if ( cmd.equals("@describe") || ( aliasExists && alias.equals("@describe") ) )
					{
						buildCmd = true;
						// run the describe function
						cmd_describe(arg, client);
					}
					else if ( cmd.equals("@door") || ( aliasExists && alias.equals("@door") ) )
					{
						buildCmd = true;
						// run the door function
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
					else if ( cmd.equals("@examine") || (aliasExists && alias.equals("@examine") ) )
					{
						buildCmd = true;
						// run the examine function
						//cmd_examine(arg, client);
						getCommand("@examine").execute(arg, client);
					}
					//
					else if ( cmd.equals("@fail") || (aliasExists && alias.equals("@fail") ) )
					{
						buildCmd = true;
						cmd_fail(arg, client);
					}
					else if ( cmd.equals("@iedit") || ( aliasExists && alias.equals("@iedit") ) )
					{
						buildCmd = true;

						// indicate what is parsing commands
						debug("Command Parser: @iedit");

						// launch the item editor
						cmd_itemedit(arg, client);
					}
					else if ( cmd.equals("@jump") || (aliasExists && alias.equals("@jump") ) )
					{
						buildCmd = true;
						cmd_jump(arg, client);
					}
					else if ( cmd.equals("@lock") ) {
						buildCmd = true;
						send("@lock: Command not Implemented!", client);
					}
					else if ( cmd.equals("@lsedit") )
					{
						buildCmd = true;
						cmd_lsedit(arg, client); // run the list editor
					}
					else if ( cmd.equals("nameref") || ( aliasExists && alias.equals("nameref") ) )
					{
						buildCmd = true;
						cmd_nameref(arg, client);
					}
					else if ( cmd.equals("@ofail") || ( aliasExists && alias.equals("@ofail") ) )
					{
						buildCmd = true;
						cmd_ofail(arg, client); // set an ofail message
					}
					else if ( cmd.equals("@open") || ( aliasExists && alias.equals("@open") ) )
					{
						buildCmd = true;
						// run the open function
						cmd_open(arg, client);
					}
					else if ( cmd.equals("@osuccess") || ( aliasExists && alias.equals("@osuccess") ) )
					{
						buildCmd = true;
						//
						cmd_osuccess(arg, client);
					}
					else if ( cmd.equals("@qedit") || ( aliasExists && alias.equals("@qedit") ) )
					{
						buildCmd = true;

						//
						debug("Command Parser: @qedit");

						// run the list editor
						cmd_questedit(arg, client);
					}
					else if ( cmd.equals("@redit") || ( aliasExists && alias.equals("@redit") ) )
					{
						buildCmd = true;

						//
						debug("Command Parser: @redit");

						// run the list editor
						cmd_roomedit(arg, client);
					}
					else if ( cmd.equals("@skilledit") || ( aliasExists && alias.equals("@skilledit") ) )
					{
						buildCmd = true;

						//
						debug("Command Parser: @skilledit");

						// run the skill editor
						cmd_skilledit(arg, client);
					}
					//
					else if ( cmd.equals("@success") || ( aliasExists && alias.equals("@success") ) ) {
						buildCmd = true;
						cmd_success(arg, client); // set an exit success message
					}
					else if ( cmd.equals("@zoneedit") || ( aliasExists && alias.equals("@zoneedit") )) {
						buildCmd = true;
						cmd_zoneedit(arg, client);
					}


					if (buildCmd) {
						return;
					}
				}

				debug("Exited build command loop.", 4);

				debug("Entering admin command loop...", 4);

				// stash admin commands inside here
				if (player.getAccess() >= Constants.ADMIN) {
					if ( cmd.equals("@accounts") || ( aliasExists && alias.equals("@accounts") ) ) {
						adminCmd = true;
						cmd_accounts(arg, client);
					}
					else if ( cmd.equals("@alias") || ( aliasExists && alias.equals("@alias") ) ) {
						adminCmd = true;
						getCommand("@alias").execute(arg, client);
					}
					// pass arguments to the backup function
					else if ( cmd.equals("@backup") || ( aliasExists  && alias.equals("@backup") ) )
					{
						adminCmd = true;
						// run the backup function
						cmd_backup(arg, client);
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
						adminCmd = true;
						cmd_config(arg, client);
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
					else if ( cmd.equals("@kick") || ( aliasExists && alias.equals("@kick") ) ) {
						adminCmd = true;
						// handle args and pass appropriate parameters to kick function
						kick(player.getClient());
					}
					else if ( cmd.equals("@listprops") || ( aliasExists && alias.equals("@listprops") ) )
					{
						adminCmd = true;
						cmd_listprops(arg, client);
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
					// pass arguments to the pgm function
					else if ( cmd.equals("@pgm") || ( aliasExists && alias.equals("@pgm") ) )
					{
						adminCmd = true;
						
						ProgramInterpreter newInterp = new ProgramInterpreter(this);

						// invoke the program interpreter and pass it the argument
						send("-Result: " + newInterp.interpret(arg, player), client);
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
						cmd_session(arg, client);
					}
					else if ( cmd.equals("@setskill") || ( aliasExists && alias.equals("@setskill") ) )
					{
						adminCmd = true;
						cmd_setskill(arg, client);
					}
					else if ( cmd.equals("@sethp") || (aliasExists && alias.equals("@sethp") ) )
					{
						adminCmd = true;
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
								System.out.println("--- Stack Trace ---");
								nfe.printStackTrace();
							}
						}
					}
					else if ( cmd.equals("@setmana") || (aliasExists && alias.equals("@setmana") ) )
					{
						adminCmd = true;
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
					else if ( cmd.equals("@setlevel") || (aliasExists && alias.equals("@setlevel") ) )
					{
						adminCmd = true;
						// DM/Debug Command
						cmd_setlevel(arg, client);
					}
					else if ( cmd.equals("@setxp") || (aliasExists && alias.equals("@setxp") ) )
					{
						adminCmd = true;
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
					else if ( cmd.equals("@tune") || ( aliasExists && alias.equals("@tune") ) )
					{
						adminCmd = true;

						String[] args = arg.split(" ");

						if ( args[0].equals("cmdDelay") ) {
							final int delay = Utils.toInt(args[1], -1);
						}
					}
					else if ( cmd.equals("@zones") || ( aliasExists && alias.equals("@zones") ) )
					{
						adminCmd = true;
						cmd_zones(arg, client);
					}
					else if ( cmd.equals("@zoneinfo") || ( aliasExists && alias.equals("@zoneinfo") ) )
					{
						adminCmd = true;
						cmd_zoneinfo(arg, client);
					}
					if (adminCmd) {
						return;
					}
				}

				debug("Exited admin command loop.", 4);

				debug("Entered wizard command loop.", 4);

				// stash wizard commands inside here
				if (player.getAccess() >= Constants.WIZARD) {
					if ( cmd.equals("@flag") || ( aliasExists && alias.equals("@flag") ) )
					{
						wizCmd = true;
						cmd_flag(arg, client);
					}
					else if ( cmd.equals("@flush") || ( aliasExists && alias.equals("@flush") ) )
					{
						wizCmd = true;
						flush();
					}
					// pass arguments to the recycle function
					else if ( cmd.equals("@recycle") || (aliasExists && alias.equals("@recycle") ) )
					{
						wizCmd = true;
						cmd_recycle(arg, client);
					}
					else if ( cmd.equals("@setmode") || ( aliasExists && alias.equals("@setmode") ) )
					{
						wizCmd = true;
						cmd_setmode(arg, client);
					}
					else if ( cmd.equals("@spawn") ) {
						wizCmd = true;
						
						Class c;

						try {
							c = Class.forName("mud.objects.creatures." + arg);
							
							Creature cre = (Creature) c.getConstructor().newInstance();
							
							//cre.setName(arg);
							cre.setLocation( getPlayer(client).getLocation() );
							// set virtual flag?
							
							objectDB.addAsNew(cre);
							objectDB.addCreature(cre);
							
							send("Spawned " + arg, client);
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if ( cmd.equals("@teleport") || ( aliasExists && alias.equals("@teleport") ) )
					{
						wizCmd = true;
						getCommand("@teleport").execute(arg, client);
					}

					if (wizCmd) {
						return;
					}
				}

				debug("Exited wizard command loop.", 4);

				debug("Entering user commmand loop...", 4);

				// stash user commands inside here
				if (player.getAccess() >= Constants.USER) {
					/*if( cmd.equals("aconfig") || (aliasExists && alias.equals("aconfig") ) ) {
						cmd_aconfig(arg, client);
					}*/
					if ( cmd.equals("ask") || (aliasExists && alias.equals("ask")) ) {
						cmd_ask(arg, client);
					}
					else if ( cmd.equals("attack") || (aliasExists && alias.equals("attack")) ) {
						getCommand(cmd).execute(arg, client);
					}
					else if( cmd.equals("auction")  || (aliasExists && alias.equals("auction")) ) {
						cmd_auction(arg, client);
					}
					else if( cmd.equals("auctions") || (aliasExists && alias.equals("auctions")) ) {
						cmd_auctions(arg, client);
					}
					else if ( cmd.equals("balance") || (aliasExists && alias.equals("balance")) ) {
						cmd_balance(arg, client);
					}
					else if ( cmd.equals("bash") || (aliasExists && alias.equals("bash")) ) {
						cmd_bash(arg, client);
					}
					else if ( cmd.equals("bid") || (aliasExists && alias.equals("bid")) ) {
						cmd_bid(arg, client);
					}
					else if ( cmd.equals("boards") || (aliasExists && alias.equals("boards")) ) {
						cmd_boards(arg, client);
					}
					else if ( cmd.equals("buy") || (aliasExists && alias.equals("buy") ) ) {
						cmd_buy(arg, client);
					}
					else if ( cmd.equals("calendar") || (aliasExists && alias.equals("calendar")) ){
						cmd_calendar("", client);
					}
					else if ( cmd.equals("cast")  || (aliasExists && alias.equals("cast")) ) {
						/* This stuff here places you in the interactive spell editor, it should
						 * not do so normally, or for every spell. the editor should only be launched
						 * under certain conditions, such as:
						 * 
						 * 
						 */
						//player.setStatus(Constants.ST_EDIT);
						//player.setEditor(Editors.INTCAST); // Editor set to -intcast-

						// run the cast function
						//cmd_cast(arg, client);
						getCommand("cast").execute(arg, client);
					}
					else if ( cmd.equals("chargen") || (aliasExists && alias.equals("chargen")) ) {
						cmd_chargen(arg, client);
					}
					else if ( cmd.equals("chat") || (aliasExists && alias.equals("chat")) ) {
						cmd_chat(arg, client);
					}
					else if ( cmd.equals("cls") || (aliasExists && alias.equals("cls")) ) {
						cmd_cls(arg, client);
					}
					else if ( cmd.equals("colors") ) {
						for(String s : displayColors.keySet()) {
							send(s + ": " + colors(s, getDisplayColor(s)), client);
						}
					}
					else if ( cmd.equals("commands") || (aliasExists && alias.equals("commands") ) )
					{
						cmd_commands(arg, client);
					}
					else if ( cmd.equals("condition") || (aliasExists && alias.equals("condition") ) ) {
						cmd_condition(arg, client);
					}
					else if ( cmd.equals("date") || (aliasExists && alias.equals("date")) ) {
						send(gameDate(), client);
					}
					else if ( cmd.equals("dedit") || (aliasExists && alias.equals("dedit")) ) {
						player.setStatus(Constants.ST_EDIT);
						player.setEditor(Editors.DESC);
					}
					else if ( cmd.equals("drink") || (aliasExists && alias.equals("drink")) ) {
						cmd_drink(arg, client);
					}
					else if (cmd.equals("drop"))
					{
						// run the drop function
						cmd_drop(arg, client);
						//getCommand("drop").execute(arg, client);
					}
					else if ( cmd.equals("effects") || (aliasExists && alias.equals("effect") ) ) {
						// run the effects function
						cmd_effects(arg, client);
					}
					else if ( cmd.equals("equip") || (aliasExists && alias.equals("equip") ) ) {
						// run the equip function
						cmd_equip(arg, client);
					}
					else if ( cmd.equals("exchange") || (aliasExists && alias.equals("exchange")) ) {
						cmd_exchange(arg, client);
					}
					else if ( cmd.equals("exp") ) {
						send(player.getLevel() + " [ "+ player.getXP() + " / " + player.getXPToLevel() + " ] " + (player.getLevel() + 1) , client);
					}
					else if ( cmd.equals("exits") ) {
						cmd_exits(arg, client);
					}
					else if ( cmd.equals("feats") ) {
						cmd_feats(arg, client);
					}
					else if ( cmd.equals("fly") ) {
						cmd_fly(arg, client);
					}
					else if ( cmd.equals("get") ) {
						cmd_get(arg, client);
					}
					else if ( cmd.equals("go") ) {
						cmd_go(arg, client);
					}
					else if ( cmd.equals("greet") ) {
						//cmd_greet(arg, client);
						getCommand("greet").execute(arg, client);
					}
					else if (cmd.equals("help") || (aliasExists && alias.equals("help")))
					{
						// run the help function
						cmd_help(arg, client);
					}
					else if (cmd.equals("home") || (aliasExists && alias.equals("home")))
					{
						try {
							boolean globalNameRefs = false;

							if( player.getConfig() != null)
							{
								if( player.getConfig().get("global-nameref-table") ) {
									globalNameRefs = true;
								}
							}
							
							int destination = -1;

							if( !globalNameRefs ) {
								destination = this.getNameRef("home");
							}
							else {
								destination = player.getNameRef("home");
							}
							
							cmd_jump(String.valueOf(destination), client);
						}
						catch(NumberFormatException nfe) {
							send("Exception (CMD:HOME): Invalid Destination!", client);
						}
					}
					else if (cmd.equals("housing") || (aliasExists && alias.equals("housing"))) {
						// player housing information
						cmd_housing(arg, client);
					}
					else if ( cmd.equals("inspect") || (aliasExists && alias.equals("inspect")) ) {
						cmd_inspect(arg, client);
					}
					else if ( cmd.equals("interact") || (aliasExists && alias.equals("interact")) ) {
						cmd_interact(arg, client);
					}
					else if ( cmd.equals("inventory") || (aliasExists && alias.equals("inventory") ) ) {
						// run the inventory function
						cmd_inventory(arg, client);
					}
					else if ( cmd.equals("land") ) {
						cmd_land(arg, client);
					}
					else if ( cmd.equals("levelup") ) {
						if ( hasValidRace(player) && hasValidClass(player) ) {
							if( player.isLevelUp() ) {
								player.changeLevelBy(1);
								send("You leveled up to level " + player.getLevel() + "!", client); 
							}
							else {
								send("You are not currently ready to level-up.", client);
							}
						}
						else {
							send("Your character is invalid.", client);
						}
					}
					else if ( cmd.equals("list") || (aliasExists && alias.equals("list") ) ) {
						cmd_list(arg, client);
					}
					else if ( cmd.equals("lock") || (aliasExists && alias.equals("lock") ) ) {
						cmd_lock(arg, client);
					}
					else if ( cmd.equals("look") || (aliasExists && alias.equals("look") ) ) {
						cmd_look(arg, client);
					}
					else if ( cmd.equals("lookat") || (aliasExists && alias.equals("lookat") ) ) {
						cmd_lookat(arg, client);
					}
					else if ( cmd.equals("mail") || (aliasExists && alias.equals("mail") ) ) {
						//cmd_mail(arg, client);
						getCommand("mail").execute(arg, client);
					}
					else if ( cmd.equals("map") || (aliasExists && alias.equals("map") ) ) {
						cmd_map(arg, client);
					}
					else if ( cmd.equals("money") || ( aliasExists && alias.equals("money") ) ) {
						send("You have " + player.getMoney() + ".", client);
					}
					else if ( cmd.equals("motd") || ( aliasExists && alias.equals("motd") ) ) {
						// run the motd function
						send(MOTD(), client);
					}
					else if ( cmd.equals("move") || ( aliasExists && alias.equals("move") ) ) {
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
					else if ( cmd.equals("ooc") ) {
						getPlayer(client).setStatus("OOC");
						send("Game> Status set to Out-of-Character (OOC)", client);
					}
					else if ( cmd.equals("open") ) {
						List<MUDObject> objects = objectDB.getByRoom( room );
						
						MUDObject object = null;
						
						for(MUDObject obj : objects) {
							if( obj.getName().equals(arg) ) {
								object = obj;
							}
						}
						
						if( object != null ) {
							if( object instanceof Exit ) {
								Exit exit = (Exit) object;
								
								if( exit.getExitType() == ExitType.DOOR ) {
									Door door = (Door) exit;
									
									if( door.isLocked() ) {
										if( door.unlock() ) { // key check
											send("You unlock the door and open it.", client);
										}
										else {
											send("That door is locked!", client);
										}
									}
									else {
										send("You find the door to be unlocked and open it.", client);
									}
								}
							}
							else if( object instanceof Lockable<?> ) {
								Lockable<Item> l;
								
								if( object instanceof Item ) {
									l = (Lockable<Item>) object;
								}
								else if( object instanceof Thing ) {
									l = (Lockable<Item>) object;
								}
							}
						}
					}
					else if ( cmd.equals("page") || ( aliasExists && alias.equals("page") ) ) {
						// run the page function
						cmd_page(arg, client);
					}
					else if ( cmd.equals("party") ) {
						cmd_party(arg, client);
					}
					else if ( cmd.equals("passwd") || ( aliasExists && alias.equals("passwd") ) ) {
						// run the password change function
						cmd_passwd(arg, client);
					}
					else if( cmd.equals("pconfig") || (aliasExists && alias.equals("pconfig") ) ) {
						cmd_pconfig(arg, client);
					}
					else if ( cmd.equals("pinfo") || ( aliasExists && alias.equals("pinfo") ) ) {
						cmd_pinfo(arg, client);
					}
					else if ( cmd.equals("push") ) {
						Thing t = getThing(arg, getRoom( player )); 

						push(t, client);
					}
					else if ( cmd.equals("put") ) {
						cmd_put(arg, client);
					}
					else if ( cmd.equals("prompt") ) {
						prompt(client);
					}
					else if ( cmd.equals("quests") || (aliasExists && alias.equals("quests") ) ) {
						cmd_quests(arg, client);
					}
					else if ( cmd.equals("quit") || (aliasExists && alias.equals("QUIT") ) ) {
						// run the quit function
						cmd_quit(arg, client);
						//return;
					}
					else if ( cmd.equals("read") ) {
						cmd_read(arg, client);
					}
					else if ( cmd.equals("roll") || (aliasExists && alias.equals("roll") ) ) {
						String[] args = arg.split(",");
						try {
							int number = Integer.parseInt(args[0]);
							int sides = Integer.parseInt(args[1]);

							send("Rolling " + number + "d" + sides, client);

							send( Utils.roll(number, sides), client );
						}
						catch(NumberFormatException nfe) {
							System.out.println("--- Stack Trace ---");
							nfe.printStackTrace();
						}
					}
					else if ( cmd.equals("run") ) {
						player.setSpeed(Constants.RUN);
						send("You get ready to run.", client);
					}
					else if ( cmd.equals("say") || (aliasExists && alias.equals("say") ) ) {
						cmd_say(arg, client);
					}
					else if ( cmd.equals("score") ) {
						cmd_score(arg, client);
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
					else if ( cmd.equals("spellinfo") || (aliasExists && alias.equals("spellinfo") ) ) {
						cmd_spellinfo(arg, client);
					}
					else if ( cmd.equals("spells") || (aliasExists && alias.equals("spells") ) ) {
						cmd_spells(arg, client);
					}
					else if ( cmd.equals("staff") ) {
						cmd_staff(arg, client);
					}
					// pass arguments to the stats function
					else if ( cmd.equals("stats") || (aliasExists && alias.equals("stats") ) ) {
						// run the stats function
						cmd_stats(arg, client);
					}
					else if ( cmd.equals("status") ) {
						cmd_status(arg, client);
					}
					else if ( cmd.equals("talk") ) {
						cmd_talk(arg, client);
					}
					else if ( cmd.equals("take") )
					{
						// run the take function
						cmd_take(arg, client);
					}
					else if ( cmd.equals("target") ) {
						cmd_target(arg, client);
					}
					else if ( cmd.equals("tell") ) {
						cmd_tell(arg, client);
					}
					else if ( cmd.equals("test") ) {
						cmd_test(arg, client);
					}
					else if ( cmd.equals("time") ) {
						send(gameTime(), client);
					}
					else if ( cmd.equals("trade") || (aliasExists && alias.equals("trade") ) ) {
						cmd_trade(arg, client);
					}
					else if ( cmd.equals("travel") || (aliasExists && alias.equals("travel") ) ) {
						cmd_travel(arg, client);
					}
					else if ( cmd.equals("unequip") || (aliasExists && alias.equals("unequip") ) ) {
						// run the drop function
						cmd_unequip(arg, client);
					}
					else if ( cmd.equals("unlock") || (aliasExists && alias.equals("unlock") ) ) {
						cmd_unlock(arg, client);
					}
					else if ( cmd.equals("use") || (aliasExists && alias.equals("use") ) ) {
						cmd_use(arg, client);
					}
					else if ( cmd.equals("version") ) {
						//send(program + " " + version, client);
						send(getName() + " " + version, client);
					}
					else if ( cmd.equals("vitals") || (aliasExists && alias.equals("vitals") ) ) {
						cmd_vitals(arg, client);
					}
					else if ( cmd.equals("where") || (aliasExists && alias.equals("where") ) ) {
						// run the where function
						cmd_where(arg, client);
						//getCommand("where");
					}
					else if ( cmd.equals("who") || (aliasExists && alias.equals("who") ) ) {
						// run the where function
						cmd_who(arg, client);
					}
					else if ( cmd.equals("walk") ) {
						player.setSpeed(Constants.WALK);
						send("You slow down to a walking speed.", client);
					}
					else if ( cmd.equals("write") ) {
						// check for something to write in/on (writable things -- paper, books, scrolls?)
						// check for writing tool and ink
						// get stuff and "write" it down
					}
					else if( commandMap.containsKey(cmd)  ) { // general case for non-explicitly checked mapped commands
						Command command = getCommand(cmd);

						if( command != null ) {
							// if player has appropriate permissions, ...
							if( player.getAccess() >= command.getAccessLevel() ) {
								command.execute(arg,  client);
							}
							else send("You may not use that command.", client);
						}
					}
					else if( player.commandMap.containsKey(cmd) ) {
						Command command = player.commandMap.get(cmd);

						if( command != null ) {
							// if player has appropriate permissions, ...
							if( player.getAccess() >= command.getAccessLevel() ) {
								command.execute(arg, client);
							}
							else send("You may not use that command. (Insufficient Access Level!)", client);
						}
					}
					else
					{
						debug("Exit? " + cmd);
						// exit handling
						if (cmd.matches("[a-zA-Z_-]+")) // no nums
						{
							// This will print to the console.
							debug("Found a match in '" + cmd + "'");

							// has the user given an action/exit that is linked to something for which no similarly named command exists,
							// if so, execute link or move user in the direction/to the room specified by the action/exit
							// handle the command as an exit
							if (!exitHandler(cmd, client)) {
								send("Huh? That is not a known command.", client);
								debug("Command> Unknown Command");
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

				debug("Exited user commmand loop.", 4);
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
		// syntax: access <player>=<access level> (access level denoted by integer -- see CONSTANTS)
		final String[] args = arg.split("=");
		if (args.length > 0) {
			Player player = getPlayer(args[0]);

			if (player != null) {
				if (args.length > 1) {
					try {
						player.setAccess(Integer.parseInt(args[1]));
						send(player.getName() + "'s access level set to " + player.getAccess(), client);
					}
					catch(NumberFormatException nfe) {
						System.out.println("--- Stack Trace ---");
						send("Invalid access level!", client);
					}
				}
				else {
					send(player.getName() + "'s access level is " + player.getAccess(), client);
				}
			}
			else {
				send("No such player!", client);
			}
		}
		else {
			send(gameError("@access", ErrorCodes.INVALID_SYNTAX), client); // Invalid Syntax Error
		}
	}

	/**
	 * Command: @accounts
	 * 
	 * List details of existing accounts. Also permits adding a new account,
	 * specifiying the name and password
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_accounts(final String arg, final Client client) {
		if ( arg.equals("") ) {
			send("Accounts (Online)", client);
			send("--------------------------------------------------------------------------------", client);
			send("Name     ID     Player                        Online Created            Age", client);
			send("--------------------------------------------------------------------------------", client);
			//   "Test     000001 Nathan                        No     02-11-2011 02:36AM 365 days"

			if (this.acctMgr != null) {
				for(Account a : acctMgr.getAccounts()) {
					//send(a.display(), client);
					final String username = Utils.padRight(a.getUsername(), 8);
					final String id = Utils.padRight(String.valueOf(a.getId()), 6);

					final Player player = a.getPlayer();
					final String name;

					if (player != null) { name = Utils.padRight(player.getName(), 29); }
					else { name = Utils.padRight("- No Player -", 29); };

					final String state = Utils.padRight(a.isOnline(), 6);
					final String creationDate = Utils.padRight(a.getCreated().toString(), 10);

					send(username + " " + id + " " + name + " " + state + " " + creationDate, client);
				}
			}

			send("--------------------------------------------------------------------------------", client);
		}
		else {
			String[] args = arg.split(" ");

			if (args.length == 3) {
				System.out.printf("@accounts: %s %s %s\n", args[0], args[1], args[2]);
				if (args[0].equals("+add")) {
					if (this.acctMgr != null) {
						client.write("Adding new account (+add)");
						
						/*
						account.linkCharacter(getPlayer(client)); // link current character to account
						account.setPlayer(getPlayer(client));     // mark it as the active player
						account.setClient(client);                // mark current client as active client
						account.setOnline(true);                  // mark us as being online
						*/
						
						//account = new Account(this.accounts.size(), args[1], args[2], 5);
						acctMgr.addAccount( args[1], args[2], 5 ); // add the account to the account manager
					}
				}
				else if (args[0].equals("+link")) { // @accounts +link 3 Nathan
					if (this.acctMgr != null) {
						Account account = acctMgr.getAccount(Utils.toInt(args[1], -1));
						Player player = getPlayer(args[2]);
						
						if( account != null ) {
							if( player != null ) account.linkCharacter(player);
							else send("No such Player Exists!", client);
						}
						else {
							send("No Such Account Exists!", client);
							//client.write("No Such Account Exists!");
						}
					}
				}
				else if (args[0].equals("+info")) {
					if (this.acctMgr != null) {
						Account account = acctMgr.getAccount(args[1], args[2]);

						if( account != null ) {
							send("Account - " + account.getUsername() + " (" + account.getId() + ")", client);
							send("Online:     " + account.isOnline(), client);
							//send("Character:  " + account.getPlayer().getName(), client);
							send("Characters: ", client);
							int index = 1;
							for(Player p : account.getCharacters()) {
								send(index + ") " + p.getName() + "( " +  p.getPClass().getAbrv() + " )", client);
							}
						}
						else {
							send("No Such Account Exists!", client);
							//client.write("No Such Account Exists!");
						}
					}
				}
			}
		}
	}
	
	/**
	 * Provides account configuration to the player if they are logged in
	 * with an account.
	 * 
	 * @param arg
	 * @param client
	 */
	/*private void cmd_aconfig(String arg, Client client) {
	}*/

	private void cmd_add(String arg, Client client) {
	}
	
	/**
	 * Command: ask
	 * 
	 * Ask an npc something
	 * 
	 * NOTE: Serves as a general npc interaction tool for acquiring information
	 * and getting quests
	 * 
	 * Syntax: ask <npc name> <keyword> <additional data>
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_ask(final String arg, final Client client) {
		final String[] args = arg.split(" ");

		if (args.length < 2) {
			return;
		}

		final Player player = getPlayer(client);           // get the player
		final NPC npc = getNPC(args[0].replace('_', ' ')); // get the npc we're referring to

		final String keyword = args[1]; // get the keyword;

		if( npc != null ) {
			if ( keyword.equals("quests") ) {
				if ( !npc.isQuestgiver() ) {
					npc.tell(player, "Impertinent scoundrel! It's not my job to find something for you to do.");
					return;
				}
				
				final List<Quest> quests = npc.getQuestsFor(player);

				send("Available Quests", client);
				send("================================================================================", client);

				for (final Quest quest : quests) {
					if (!quest.isComplete()) {
						client.write( quests.indexOf(quest) + ") " + quest.toDisplay( color == Constants.ANSI || color == Constants.XTERM ));
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
					final Quest quest = npc.getQuestsFor(player).get( Utils.toInt(args[2], -1) );

					if (quest != null) {
						if( !player.hasQuest( quest ) ) {
							if( quest.getName().equals("Help the Innkeeper") ) {
								npc.tell(player, "We've been having problems with rats getting into our stores lately. It'd be much appreciated if you could dispatch them for us.");
							}
							player.getQuests().add( quest.clone() );
							send("New Quest - " + quest.getName(), client);
							send("Quest Added!", client);
						}
						else { send("You already have that quest!", client); }
					}
					else {
						send("No such Quest!", client);
					}
				}
			}
			else if ( keyword.equals("about") ) {
				if ( args.length == 3 ) {
					// quests (by name?), conversation topics
				}
			}
			else if ( keyword.equals("complete") ) {
				// ask <npc> complete <quest id?>
				if ( args.length == 3 ) {
					final Quest quest = npc.getQuestsFor(player).get( Utils.toInt(args[2], -1) );

					if (quest != null) {
						if( player.hasQuest(quest) ) {
							// get the quest
							final Quest q1 = player.getQuest( quest.getId() );
							
							if (q1 != null) {
								if( q1.isComplete() ) {
									npc.tell(player, "Thanks a lot. Now we can quit worrying about our stock being consumed by vermin!");;
									send("Quest Completed!");
									
									npc.tell(player, "Here's a reward for your efforts");
									
									// give player a reward
									player.setMoney(Coins.gold(5));
									
									notify(player, "You receive 5 gold.");
								}
								else {
									npc.tell(player, "Oh yeah? I can still the rats scuttling around down there!");
								}
							}
							else { send("You don't have that quest!", client); }
						}
						else { send("You haven't done anything of the sort!"); }
					}
					else {
						send("No such Quest!", client);
					}
				}
			}
		}
		else {
			send("Ask who?", client);
		}
	}

	/**
	 * Command: Auction
	 * 
	 * auction an item
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_auction(final String arg, final Client client) {
		// ideas:
		// auction <item> <time> <price> <buyout>
		// ex. auction longsword 2d 50gp 100gp
		// ex. auction longsword for 2d at 50gp
		Player player = getPlayer(client);

		String[] args = arg.split(" ");

		if( args.length >= 2 ) {
			if( args[0].equals("#cancel") ) {
				Auction auction = getAuction(args[1]);

				if( auction != null ) {
					if( auction.hasBids() ) {
						send("You cannot cancel an auction once someone has bid on it.", client);
						return;
					}

					Item item = auction.getItem();
					
					auctions.remove(auction);
					
					send("You canceled your auction of " + item.getName(), client);
					
					player.getInventory().add( item );
					
					return;
				}
				else send("No such auction!", client);
			}
			else {
				Item item = getItem(args[0], player);

				if(item != null) {
					if( !item.isAuctionable() ) {
						send("You cannot auction that item.", client);
						return;
					}

					Coins price = Coins.copper(Utils.toInt(args[1], -1));

					// create auction and add it to the list
					Auction auction = createAuction(player, item, price);
					auctions.add( auction );

					// schedule the timer and stash it away
					AuctionTimer aTimer = new AuctionTimer( auction, 21600 );
					getAuctionTimers(player).add(aTimer);
					timer.scheduleAtFixedRate(aTimer, 0, 1000);

					send(item.getName() + " put up for auction at " + price.toString(true));
				}
				else {
					send("What do you want to auction?", client);
				}
			}
		}
	}

	private void cmd_auctions(final String arg, final Client client) {
		//ex. | +2 Long Sword | 2 days, 17 hours | 150gp | 250gp |
		
		List<String> output = new LinkedList<String>();
		
		output.add("+---------------------+------------------+---------------------+--------------------+");
		output.add("|      Item Name      |     Duration     |    current price    |    buyout price    |");
		output.add("+---------------------+------------------+---------------------+--------------------+");
		
		StringBuilder sb = new StringBuilder();
		
		Item item;
		Coins price;
		Coins buyoutprice;

		for(Auction auction : auctions) {
			item = auction.getItem();

			price = null;
			buyoutprice = null;

			if( auction.hasBids() ) price = auction.getCurrentBid().getAmount();
			else price = auction.getInitialPrice();
			
			sb.append("| ");
			sb.append(Utils.center(item.getName(), 19));
			sb.append(" | ");
			
			int rem = auction.getTimeLeft() / 60;
			int remainder = auction.getTimeLeft() % 60;
			
			if(remainder > 0) {
				sb.append(Utils.center("> " + rem + "s", 16));
			}
			else sb.append(Utils.center("" + rem + "s", 16));
			
			//sb.append(Utils.center("" + auction.getTimeLeft(), 16));
			
			sb.append(" | ");
			sb.append(Utils.center(price.toString(true), 19));
			sb.append(" | ");
			//sb.append(Utils.center(buyoutprice.toString(true), 18));
			sb.append(Utils.center("", 18));
			sb.append(" |");
			
			output.add(sb.toString()); // add the line to the output
			sb.delete(0, sb.length()); // clear the string builder
		}

		output.add("+---------------------+------------------+---------------------+--------------------+");
		
		client.write( output );
	}

	/**
	 * Command: @backup
	 * 
	 * Backup the database
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_backup(final String arg, final Client client)
	{
		backup(arg);
		send("Game> Finished backing up.", client);
	}
	
	/**
	 * Balance
	 * 
	 * Show the user's balance
	 * 
	 * System: Economy
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_balance(final String arg, final Client client) {
		final Player player = getPlayer(client);

		final Bank bank = getBank("test");
		final BankAccount acct = bank.getAccount(0);
		
		if( bank != null && acct != null ) {
			send("( " + bank.getName() + " ) Your balance is: " + acct.getBalance().toString(true), client );
		}
		else {
			send("You don't have an account.", client);
		}
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
		/*String[] args;

		if( arg.indexOf(' ') != -1 ) args = arg.split(" ");*/

		if( arg.equals("#list") ) {
			send("Banned IP Addresses", client);
			for (String s : banlist) send(s, client);
			send("Banned Players", client);
			//for(Player player : objectDB.players.values()) send(player.getName());
		}

		if (!arg.equals("")) {
			final Player player = getPlayer(arg); // player name based search (banning a player should ban his account as well

			System.out.println("Player: " + player);

			if (player != null) {
				final Client client1 = player.getClient();

				if (client1 != null) { // current DB always has players loaded, so the player will never be null
					// add the player's ip address to the banlist (IP address ban)
					banlist.add(client1.getIPAddress());

					// if they have an account, suspend the account
					Account acct = acctMgr.getAccount(player);

					if(acct != null) {
						acct.setStatus(Account.Status.SUSPENDED);
					}

					// mark the player somehow
					player.setPStatus(Player.Status.BANNED);

					cmd_page(arg + ", you have been banned.", client1);
					kick(client1);
				}
				else {
					/* Update my code so it stores the last login ip for players,
					 * so I can ban them even if they log off?
					 * Also, add a banned check to logging in so they still can't
					 * login, even if they change their ip?
					 */
					debug("That player is not connected");
					send("That player is not connected", client);
				}

				return;
			}
			else {
				if( arg.matches("[a-zA-Z]+") ) {
					send("No such player!", client);
				}
				else {
					String[] range = arg.split(",");

					System.out.println(Arrays.asList(range));

					for(final String ipa : range) {
						if( ipa.matches("^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}$") ) {
							banlist.add(ipa);
						}
						else send("That's not an ipaddress or a range of them.");
					}
				}
			}
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
		// exit if there are no arguments at all
		if( "".equals(arg) ) return;
		
		// break up argument string into individual pieces
		final String[] args = arg.split(" ");
		
		// print out individual args for debugging
		for (final String s : args) {
			debug(s);
		}
		
		//BulletinBoard board = bb;
		
		// important components of argument data
		String command;
		String board_name;
		
		command = args[0];
		
		/*if( args[1].indexOf('/') != -1 ) {
			board_name = args[1].substring(0, args[1].indexOf('/') );
		}*/

		if( args.length == 1) {
			if (command.equals("+scan")) {
				StringBuilder sb = new StringBuilder();
				for (final String s : bb.scan()) {
					sb.append(s + '\n');
					//client.write(s + '\n');
				}
				client.write(sb.toString());
			}
		}
		else if (args.length == 2) {
			if( command.equals("+delete") ) {
				int messageNum = Utils.toInt(args[1], -1); // get message number to delete

				if(messageNum > -1) {
					if( messageNum < bb.getNumMessages() ) { // check for valid message number
						ArrayList<BBEntry> entries = bb.getEntries();

						Player player = getPlayer(client);
						BBEntry entry = entries.get(messageNum);

						if (entry.getAuthor().equals(player.getName())) {
							debug("Removing Message...");
							bb.removeEntry(messageNum);
							debug("Renumbering entries...");
							bb.renumber(messageNum);
							debug("Done.");
						}

						return;
					}

					send("No such message", client);
				}

				send(gameError("@bb", ErrorCodes.INVALID_SYNTAX), client);
			}
			else if( command.equals("+read") ) {
				int messageNum = Integer.parseInt(args[1]);
				BBEntry entry = bb.getEntry(messageNum);

				if( entry != null ) {
					client.write("ID: " + entry.getId() + "\n");
					client.write("Author: " + entry.getAuthor() + "\n");
					client.write("Subject: " + entry.getSubject() + "\n");
					//client.write("Message: " + "\n");
					client.write(entry.getMessage() + "\n");
				}
				else {
					send("No such entry!", client);
				}
			}
			else if( command.equals("+scan") ) {
				BulletinBoard board = boards.get(args[1].toLowerCase());

				if (board != null) {
					StringBuilder sb = new StringBuilder();
					for (final String s : board.scan()) {
						sb.append(s + '\n');
						// client.write(s + '\n');
					}
					client.write(sb.toString());
				} else {
					send("No Such Board.", client);
				}
			}
		}

		if (args.length >= 2) {
			if (command.equals("+add")) {
				String subject;
				String message;
				
				BulletinBoard board = bb;
				
				String temp = Utils.join(Arrays.copyOfRange(args, 1, args.length), " "); // remove parameter
				
				String[] entry = temp.split("=");
				
				if (entry.length == 2) {
					if( entry[0].indexOf('/') != -1 ) {
						board = boards.get( entry[0].split("/")[0] );
						subject = entry[0].split("/")[1];
						message = entry[1];
					}
					else {
						subject = entry[0];
						message = entry[1];
					}
					
					board.write(subject, message, client);
				}
				else {
					send(gameError("@bb", ErrorCodes.INVALID_SYNTAX), client);
				}
			}
		}
		else {
			//cmd_help("@bb", client);
		}
	}

	/**
	 * Command: bid
	 * 
	 * Bid on an auctioned item.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_bid(final String arg, final Client client) {
		Player player = getPlayer(client);

		// bid <auction id/item> <money>
		// bid 'staff of the archmagi' 600pp
		// bid staff 600

		Auction auction;
		String itemName;
		String money;

		/*if( arg.indexOf('\'') != -1 ) {
			int first = arg.indexOf('\'');
			int second = arg.indexOf('\'', first);

			System.out.println("first: " + first);
			System.out.println("second: " + second);

			itemName = arg.substring(first, second);
			money = arg.substring(second, arg.length());
		}*/

		String[] args = arg.split(" ");
		
		/*if( arg.indexOf('\'') != -1 && arg.indexOf('\'', arg.indexOf('\'')) ) {
			
		}*/

		if(args.length >= 2) {
			itemName = args[0];
			money = args[1];

			System.out.println("Item name: \'" + itemName + "\'");
			System.out.println("Money: \'" + money + "\'");

			/*int end = 0;

			for(int i = 0; i < money.length(); i++) {
				if( !Character.isDigit(money.charAt(i)) ) {
					end = i;
					break;
				}
			}*/

			int value = Utils.toInt( args[1], -1 );
			//String type = arg.substring(end, arg.length());

			System.out.println("Value: " + value);
			//System.out.println("Type: " + type);

			Coins coins = null;

			/*switch(type) {
			case "pp":
				coins = Coins.platinum(value);
				break;
			case "gp":
				coins = Coins.gold(value);
				break;
			case "sp":
				coins = Coins.silver(value);
				break;
			case "cp":
				coins = Coins.copper(value);
				break;
			default:
				coins = Coins.copper(value);
				break;
			}*/

			coins = Coins.copper(value);

			Bid bid = new Bid(player, coins);
			auction = getAuction(itemName);

			if( auction != null ) {
				if( auction.placeBid(bid) ) {
					send("You successfully placed a bid of " + coins.toString(true) + " on " + itemName, client);
					return;
				}

				send("You failed to place a bid (Cause?)", client);
				return;
			}

			send("No such auction!", client);
		}
	}

	/**
	 * Command: boards
	 * 
	 * Add, Delete, List BulletinBoard(s)
	 * 
	 * +add <new board name>      add a new board
	 * +del <existing board name> delete an existing board
	 * +list                      list the existing boards
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_boards(final String arg, final Client client) {
		if( !arg.equals("") ) {
			String[] args = arg.split(" ");
			
			if( args[0].charAt(0) == '+' ) {
				final String param = args[0].substring(1);

				if( args.length == 2 ) {
					final String board_name = args[1].toLowerCase();
					
					if( param.equalsIgnoreCase("add") ) {
						this.boards.put( board_name, new BulletinBoard(board_name) );
					}
					else if( param.equalsIgnoreCase("del") ) {
						// ask for confirmation?
						this.boards.remove( board_name );
					}
				}
			}
		}
		else {
			send("Boards", client);
			send(Utils.padRight("", '-', 40), client);
			for(final Map.Entry<String, BulletinBoard> entry : this.boards.entrySet()) {
				BulletinBoard bb1 = entry.getValue();
				String shortName = entry.getKey();
				send(bb1.getName() + " (" + bb1.getNumMessages() + ") [" + shortName + "]", client);
			}
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

		if (player.getStatus().equals(Constants.ST_INTERACT)) { // interact mode
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

					if ( player.getMoney().isMoreOrEqual(item.getValue()) ) {
						item1 = v.buy(arg);

						player.setMoney(item1.getValue());

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
	 * COMMAND OBJECT EXISTS (current code reintegrated from that command -- 4/13/2013)
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_cast(final String arg, final Client client)
	{
		final String spellName = arg;
		final Player player = getPlayer(client);

		if (!player.isCaster()) {
			send("What do you think you are anyway? Some kind of wizard? That's just mumbo-jumbo to you!", client);
			return;
		}

		final Spell spell = getSpell(spellName);

		if (spell == null) {
			send("You move your fingers and mumble, but nothing happens. Must have been the wrong words.", client);
			debug("CastCommand spellName: " + spellName);
			return;
		}

		// should this be reversed???
		// ^No, but the player's level and effective caster level is not equivalent to the spell's level
		// PL -> SL: 0 -> 0, 1,2 -> 1, 3,4 -> 2, 5,6 -> 3, 7 -> 4...
		if (player.getLevel() < spell.getLevel()) {
			// add reagents check!
			if( spell.getReagents() != null ) {
				// at present Reagent is not a subclass of Item
				/*for(Reagent r : spell.getReagents().values()) {
					if( player.getInventory().contains(r) ) {
						if(r instanceof Stackable<?>) {
							Stackable<Reagent> rStack = (Stackable<Reagent>) r;
							if( !(((Stackable) r).stackSize() > 0) ) {
								send("Insufficient spell components", client);
								return;
							}
						}
					}
					else {
						send("Insufficient spell components", client);
						return;
					}
				}*/
			}

			// target check, if no target then auto-target self, etc, dependent on spell
			if (player.getTarget() == null) {
				player.setTarget(player); // auto-target to self
			}

			final MUDObject target = player.getTarget();

			// reduce mana (placeholder), really needs to check the spell's actual cost
			player.setMana(-spell.getManaCost());

			// calculate spell failure (basic, just checks armor for now)
			Armor armor = (Armor) player.getSlots().get("armor").getItem();

			double spellFailure = 0;

			if( armor != null ) {
				spellFailure = armor.getSpellFailure() * 100; // spellFailure stored as percentage
				debug("Spell Failure: " + spellFailure);
			}

			//Create random number 1 - 100
			int randNumber = (int) ((Math.random() * 100) + 1);

			debug("d100 Roll: " + randNumber);

			if( randNumber > spellFailure ) {
				// cast spell
				send(spell.getCastMessage().replace("&target", player.getTarget().getName()), client);

				// apply effects to the target
				for (final Effect e : spell.getEffects()) {
					if(target instanceof Player) {
						System.out.println("Target is Player.");

						applyEffect((Player) target, e);               // apply the effect to the target

						SpellTimer sTimer = new SpellTimer(spell, 60); // spell timer with default (60 sec) cooldown
						getSpellTimers(player).add(sTimer);
						timer.scheduleAtFixedRate(sTimer, 0, 1000);

						EffectTimer etimer = new EffectTimer(e, 30);
						getEffectTimers(player).add(etimer);
						timer.scheduleAtFixedRate(etimer, 0, 1000); // create countdown timer
					}
					else {
						System.out.println("Target is Player.");
						applyEffect(target, e);
					}
				}

				// if our target is a player tell them otherwise don't bother
				if (target instanceof Player) {
					addMessage(new Message(player, player.getName() + " cast " + spell.getName() + " on you." , (Player) target));
				}
			}
			else {
				send("A bit of magical energy sparks off you briefly, then fizzles out. Drat!", client);
			}
		}
	}
	
	private void cmd_chargen(final String arg, final Client client) {
		Player player = getPlayer(client);
		
		player.setStatus(Constants.ST_EDIT); // Player Status set to -Edit-
		player.setEditor(Editors.CHARGEN);   // set the player's editor to CHARGEN

		op_chargen("start", client); // initiate character generation sequence
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
	private void cmd_chat(final String arg, final Client client) {
		String[] args = arg.split(" ");
		// argument: show a list of available channels and whether you are on them
		if ( arg.toLowerCase().equals("#channels") ) {
			client.write("Chat Channels\n");
			client.write("--------------------------------\n");
			for (final String chanName : chan.getChannelNames()) {
				client.write(Utils.padRight(chanName, 8));
				client.write(" ");
				if (chan.isPlayerListening(chanName, getPlayer(client))) {
					//client.write(Colors.GREEN.toString() + "Enabled" + Colors.WHITE.toString() + "\n");
					client.write(colors("Enabled", "green") + "\n"); // alternate method
				}
				else {
					//client.write(Colors.RED.toString() + "Disabled" + Colors.WHITE.toString() + "\n");
					client.write(colors("Disabled", "red") + "\n"); // alternate method
				}
			}
			client.write("--------------------------------\n");
		}
		else if( args.length > 1 ) {
			Player player = getPlayer(client);
			String channelName;
			
			final String param = args[0];

			if(args[0].charAt(0) == '#') { // chat subcommand
				channelName = args[1];

				if( chan.hasChannel(channelName) ) {
					if( args[0].toLowerCase().equals("#join") ) {
						try {
							if( !chan.isPlayerListening(channelName, player) ) {
								boolean success = chan.add(player, channelName);
								if( success ) {
									send("ChatChanneler> Joined channel: " + channelName, client);
									chan.send(channelName, "ChatChanneler> " + player.getName() + " joined the channel.");
								}
								else {
									send("ChatChanneler> Failed to join. Channel is restricted!", client);
								}
							}
							else {
								send("ChatChanneler> You are already listening to that channel.", client);
							}
						}
						catch (Exception e) {
							e.printStackTrace();
							send("ChatChanneler> cannot join channel: " + channelName, client);
						}
					}
					else if( args[0].toLowerCase().equals("#leave") ) {
						try {
							if( chan.isPlayerListening(channelName, player) ) {
								chan.remove(player, channelName);
								send("ChatChanneler> Left channel: " + channelName, client);
							}
							else {
								send("ChatChanneler> You aren't listening to that channel.", client);
							}
						}
						catch (Exception e) {
							e.printStackTrace();
							send("ChatChanneler> cannot leave channel: " + channelName, client);
						}
					}
					else if( args[0].toLowerCase().equals("#listeners") ) { // argument: show listeners on a specific channel
						client.write("Listeners on Chat Channel: " + channelName.toUpperCase() + "\n");
						client.write("------------------------------\n");
						for (final Player p : chan.getListeners(channelName)) {
							client.write(p.getName() + "\n");
						}
						client.write("------------------------------\n");
					}
					else if( args[0].toLowerCase().equals("#messages") ) {
						send("Messages:", client);
						send("------------------------------", client);
						for(Message m : chan.getChatChannel(channelName).getMessages()) {
							send(m.getMessage());
						}
						send("------------------------------", client);
						send("Next Message: " + chan.getChatChannel(channelName).getMessages().peek().getMessage(), client);
						send("------------------------------", client);
					}
				}
				else {
					send("Game> No such chat channel.", client);
				}
			}
			else { // sending a chat message
				channelName = args[0];
				String msg = arg.replace(channelName + " ", "");

				if( chan.hasChannel(channelName) ) {
					chan.send(channelName, player, msg);

					debug("New message (" + channelName + "): " + msg);

					chatLog.writeln("(" + channelName + ") <" + getPlayer(client).getName() + "> " + arg);
				}
				else {
					client.write("Game> No such chat channel.");
				}
			}
		}
	}

	private void cmd_config(final String arg, final Client client) {
		// use this to replace 'ansi' and 'msp' commands?
		// or possibly alias them to it?
		// @config ansi = on, @config ansi = off
		// @config msp = on, @config msp = off

		//String[] args = arg.split("=");
		String[] args = arg.split(" ");

		if( args.length == 2 ) {
			if( args[0].equals("colors") ) {
				if( args[1].equalsIgnoreCase("ansi") ) {
					color = Constants.ANSI; // enables ansi and disables xterm
					client.write("\033[;1m"); // tell client to use bright version of ANSI Colors
					//send("> Using BRIGHT ANSI colors <", client); // indicate the use of bright ansi colors to the client
					send(rainbow("ANSI") + " colors turned on.", client);
				}
				else if( args[1].equalsIgnoreCase("xterm") ) {
					color = Constants.XTERM; // enables xterm and disables ansi
					send(rainbow("XTERM256") + " colors turned on.", client);
				}
				else if( args[1].equalsIgnoreCase("off") ) {
					color = Constants.DISABLED; // disables color
					send("Colors turned off.", client);
				}
			}
		}
		else if( args.length == 1 ) {
			if( args[0].equals("colors") ) {
				if( color == Constants.ANSI )       send("Using " + rainbow("ANSI") + " colors.", client);
				else if( color == Constants.XTERM ) send("Using " + rainbow("XTERM256") + " colors.", client);
				else                                send("Colors are disabled.", client);
			}
		}

		/*if ( args.length > 1 ) {
			if ( arg.equalsIgnoreCase("prompt_enabled") ) {
				switch( args[1] ) {
				case "true":
					prompt_enabled = true;
					send("SRV> set " + args[0] + " to TRUE.", client);
					break;
				case "false":
					prompt_enabled = false;
					send("SRV> set " + args[0] + " to FALSE.", client);
					break;
				default:
					send("SRV> Invalid config setting!", client);
					break;
				}
			}
		}*/

		/*f ( arg.contains("=") ) {
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
					send( Utils.padRight( e.getKey(), ' ', 10 ) + " : " + e.getValue(), client);
				}
			}
			else {
				send( gameError("@config", ErrorCodes.INVALID_SYNTAX), client );
			}
		}*/
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
		Thing thing = objectDB.getThing(getRoom(player).getDBRef(), arg); // ex. box, ladder, building

		// check distance from object
		if( Utils.distance( player.getPosition(), thing.getPosition() ) <= 1 ) {

			// get the check for it's difficulty (static assign for testing purposes)
			int difficultyCheck = 10;

			// check to see if we can climb it
			boolean canClimb = skill_check(player, Skills.CLIMB, "1d4+1", difficultyCheck);

			// evaluate results
			if (canClimb) {
				Integer height = Utils.toInt(thing.attributes.get("height"), 1);

				if(height > 1) {
					send("You start climbing <direction> the " + thing.getName().toLowerCase(), client);
				}
				else if(height == 1) {
					send("You climb <up/onto> the " + thing.getName().toLowerCase(), client);
					Point thingPos = thing.getPosition();
					player.setPosition( thingPos.getX(), thingPos.getY() );
					player.changePosition( 0, 0, 1 );
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
	 * "Clears" the screen by sending the specified number of blank
	 * lines.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_cls(final String arg, final Client client) {
		int numLines = Utils.toInt(arg, 25);
		int n = 0;
		
		while( n < numLines ) {
			send("", client);
			n++;
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

		debug(args.length);

		if (args.length < 1 || args.length > 2 || args[0].equals("")) {
			/*send("Enter a valid character creation or connection string\n" +
					"such as 'create <character name> <password>' or 'connect <character name> <password>'\n" +
					"To connect as a guest simply type 'connect guest'", client);*/
			send( "Enter a valid character creation or connection string", client );
			send( "such as 'create <character name> <password>' or 'connect <character name> <password>'", client );
			send( "To connect as a guest simply type 'connect guest'", client );
			send( "NOTE: Using an valid account name and password will trigger the account options menu", client );
			
			return;
		}

		final String user = Utils.trim(args[0]);
		final String pass = args.length > 1 ? Utils.trim(args[1]) : " ";

		debug("Username?: " + user);
		debug("Password?: " + Utils.padRight("", '*', pass.length()) );
		debug("");

		// Guest Players
		if ( user.toLowerCase().equals("account") ) {
			setClientState(client, "account_login");
			handle_account_login(user, client);
		}
		else if ( user.toLowerCase().equals("guest") ) {
			if (guest_users == 1) {
				Player player;

				player = getNextGuest();

				if( player == null ) {
					player = new Player(-1, "Guest" + guests, EnumSet.of(ObjectFlag.DARK, ObjectFlag.GUEST), "A guest player.", Constants.WELCOME_ROOM, "", Utils.hash("password"), "OOC", new Integer[] { 0, 0, 0, 0, 0, 0 }, Coins.copper(0));
					objectDB.addAsNew(player);
				}

				init_conn(player, client, false);
				guests++;
			}
			else {
				send("Sorry. Guest users have been disabled.", client);
			}
		}
		else {
			/*
			 * NOTE:
			 * if all players always existed, then instead of instantiating a player i'd
			 * simply assign a client to it. Otherwise I need to get the player data from
			 * somewhere so I can load it up.
			 */
			
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

			// account check
			final Account a = acctMgr.getAccount(user, pass);
			
			//debug("CONNECT: Fail");
			//send("That account does not exist or the password is incorrect!", client);
			
			if(a != null) {
				if( a.getStatus() != Account.Status.ACTIVE ) {
					switch(a.getStatus()) {
					case FROZEN:    send("That account has been frozen", client); break;
					case INACTIVE:  send("That account is inactive.", client); break;
					case LOCKED:    send("That account is locked.", client); break;
					case SUSPENDED: send("That account has been suspended.", client); break;
					case ARCHIVED:  send("That account has been archived.", client); break;
					default:        break;
					}
				}
				else {
					caTable.put(client, a);
					account_menu( a, client );
					setClientState(client, "account_menu");
				}
				
				return;
			}
			
			// character check
			final Player p = getPlayer(user);

			if (p == null) {
				debug("CONNECT: Fail");
				debug("No such player!");
				send("That player does not exist or the password is incorrect!", client);
				return;
			}
			
			if(p.getPStatus() == Player.Status.BANNED) {
				debug("CONNECT: Fail");
				debug("Player " + p.getName() + " is banned!");
				send("Player is banned.", client);
				return;
			}
			
			if(!p.getPass().equals(Utils.hash(pass))) {
				debug("PASS: Fail");
				send("That player does not exist or the password is incorrect!", client);
				return;
			}

			debug("PASS: Pass"); // report success for password check

			// remove player in case of relogin (this is the not ideal handling)
			for(Player player : players) {
				if( player.getDBRef() == p.getDBRef() && player.getName().equals( p.getName() ) ) {
					/*
					 *  won't work because client object vanishes when the player loses connection
					 */
					init_disconn( player.getClient() );
					break;
				}
			}
			
			// connect to the game

			if (mode == GameMode.NORMAL) {
				init_conn(p, client, false); // Open Mode
			}
			else if (mode == GameMode.WIZARD) {
				if ( p.getFlags().contains("W") || p.getAccess() == 4 ) {
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
	
	private void cmd_console(final String arg, final Client client) {
		String[] credentials = arg.trim().split(" ");
		
		if( credentials.length == 2 ) {
			final String user = credentials[0];
			final String pass = credentials[1];
			
			if( user.equals("admin") && pass.equals("admin") ) {
				client.setConsole(true);
				client.write("Launching Console...");
				
				final Console console = new Console(this, client);
				
				consoles.put(client,  console);
				
				client.writeln("Done.");
				client.writeln("");
				client.writeln("MUDServer Console");
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

		if (arg.toLowerCase().equals("#break")) { // break control, return controlling player to their own body (ha ha)
			final Player controller = playerControlMap.getController(player); // get the controller

			// if the player is a slave to some controller
			if( controller != null ) {
				// show control table state before change
				debug("DM Control Table:");
				debug(playerControlMap);

				// get name of slave npc
				String name = playerControlMap.getSlave(controller).getName();

				playerControlMap.stopControllingAnyone(controller); // stop controlling an npc

				// show control table state after change
				debug("DM Control Table:");
				debug(playerControlMap);

				// status message, indicate that we have stopped controlling an npc and what its names is
				send("Game> You stop controlling " + name, client);
			}
		}
		else { // should not be able to use this to control other players (at least not normally) NOTE: needs work
			final Player npc = getNPC(arg); // get the npc we want to control by name

			debug(player); // print out player as string
			debug(npc);    // print out npc as string

			if (npc instanceof NPC) {
				// show control table state before change
				debug("DM Control Table:");
				debug(playerControlMap);

				final Player oldSlave = playerControlMap.control(player, npc); // control an npc 

				player.setController(true); // mark the player as controlling an npc (commented out in PlayerControlMap)

				// if we were already controlling an npc, revoke the privilege escalation (controlled npcs have the same permissions as the controller)
				if( oldSlave != null ) {
					oldSlave.setAccess(Constants.USER);   // revoke any greater privileges granted
				}

				// show control table state after change
				debug("DM Control Table:");
				debug(playerControlMap);

				// status message, indicate that we are now controlling an npc and what its names is
				send("Game> You are now controlling " + npc.getName(), client);
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
		
		if( arg.split(" ").length != 2 ) {
			send("CREATE: insufficient arguments. Did you forget to specify a password?", client);
			return;
		}
		
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
			// create a new player object for the new player
			final Player player = new Player(-1, user, Utils.hash(pass), start_room);
			
			// TODO decide if this is okay or find a better way
			//sclients.put(client, player);
			// run character generation (should we do this here?)

			objectDB.addAsNew(player);
			objectDB.addPlayer(player); // add player to the auth table
			
			send("Welcome to the Game, " + user + ", your password is: " + pass, client);
			
			// initiate the connection
			init_conn(player, client, true);
		}
		else
		{
			// indicate the unavailability and/or unsuitability of the chosen name
			send("That name is not available, please choose another and try again.", client);
		}
	}

	/**
	 * Command: @check
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
		final Room room = getRoom( getPlayer( client ) );

		debug(room.getExits());

		for (final Exit exit : room.getExits())
		{
			send(exit.getName(), client);
			
			if( exit.getExitType() == ExitType.DOOR ) {
				send("Locked: " + (((Door) exit).isLocked() ? "Yes" : "No"), client);
			}
			
			send("\tsuccess: " + exit.getMessage("succMsg"), client);
			send("\tosuccess: " + exit.getMessage("osuccMsg"), client);
			send("\tfail: " + exit.getMessage("failMsg"), client);
			send("\tofail: " + exit.getMessage("ofailMsg"), client);
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
		final Player player = getPlayer(client);
		final int access = player.getAccess();
		
		final List<Set<String>> commandMaps = new LinkedList<Set<String>>();
		commandMaps.add( commandMap.keySet() );
		commandMaps.add( player.commandMap.keySet() );
		
		int c = 0;
		
		StringBuilder sb = new StringBuilder();
		
		for(final Set<String> s : commandMaps) {
			for (String key : s) {
				debug(key);
				if (sb.toString().equals("")) sb.append(key);
				else sb.append(", " + key);
			}
			
			switch(c) {
			case 0:
				showDesc(colors("mapped: ", "yellow") + sb.toString(), client);
				break;
			case 1:
				showDesc(colors("mapped(player): ", "yellow") + sb.toString(), client);
				break;
			default:
				break;
			}
			
			sb.delete(0, sb.length());
			
			c++;
		}

		showDesc(colors("user commands: ", "green") + Utils.join(user_cmds, ", "), client);

		if (access >= Constants.BUILD)
		{
			showDesc(colors("builder commands: ", "cyan") + Utils.join(build_cmds, ", "), client);
		}
		if (access >= Constants.ADMIN)
		{
			showDesc(colors("admin commands: ", "red") + Utils.join(admin_cmds, ", "), client);
		}
		if (access >= Constants.WIZARD)
		{
			showDesc(colors("wizard commands: ", "magenta") + Utils.join(wiz_cmds, ", "), client);
		}
		if (access >= Constants.GOD)
		{
			showDesc(colors("gods commands: ", "yellow") + Utils.join(god_cmds, ", "), client);
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
			room.getItems().add(item);
			send("Item named " + item.getName() + "(#" + item.getDBRef() + ") created. " + item.getName() + " has been placed in your location.", client);
		}
	}*/

	private void cmd_condition(final String arg, final Client client) {
		NPC npc = getNPC(arg);

		if( npc != null ) {
			send(npc.getName() + " " + "HP: " + npc.getHP() + "/" + npc.getTotalHP() + " " + npc.getState(), client);
			send("head: 100%", client);
			send("neck: 100%", client);
			send("left arm:  100% (left hand: 100%)", client);
			send("right arm: 100% (right hand: 100%)", client);
			send("chest: 100%", client);
			send("left leg:  100% (left foot: 100%)", client);
			send("right leg: 100% (right foot: 100%)", client);
		}
	}

	/**
	 * Command: @debug
	 * 
	 * Show requested debugging information, takes several arguments to show different
	 * information
	 * 
	 * on/off/client/clients/creatures/holidays/listen/position/pos/portals/seasons/timedata
	 * timers/udbnstack/<a number>
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_debug(final String arg, final Client client) {
		String[] args = arg.split(":");
		String[] args2 = arg.split(" ");

		String param = arg.toLowerCase();

		if ( param.equals("on") ) {
			debug = 1;
			send("Game> Debugging: On", client);
		}
		else if ( param.equals("off") ) {
			debug = 0;
			send("Game> Debugging: Off", client);
		}
		else if ( param.equals("client") ) {
			/* tell us about ourselves (i.e. which client object we are and our ip address) */
			send(client, client);
			send(client.getIPAddress(), client);
		}
		else if ( param.equals("clients") ) {
			int cn = 0;
			for (Client c : s.getClients()) {
				if (c != null) {
					if(c == client) {
						send(cn + " " + c.getIPAddress() + " " + c.toString() + "[YOU]", client);
					}
					else {
						send(cn + " " + c.getIPAddress() + " " + c.toString(), client);
					}
				}
				else {
					send(cn + " " + "---.---.---.--- null", client);
				}
				cn++;
			}
		}
		else if ( param.equals("colors") ) {
			if( color == Constants.XTERM ) {
				char c = 'A';

				for(int i = 0; i < 255; i++) {
					client.write("\033[38;5;" + i + "m" + c);
					if( i % 36 == 0 ) client.writeln("");
				}

				client.writeln("");
			}
			if( color == Constants.ANSI ) {
				char c = 'A';

				for(int i = 0; i < 9; i++) {
					client.write("\033[3" + i + ";m" + c);
				}

				client.writeln("");
			}
		}
		else if ( param.equals("creatures") ) {
			send("Creatures", client);
			send("--------------------------------------------------------------------------", client);
			for (final Creature c : objectDB.getCreatures()) {
				send(String.format("%s %s %s (#%s)", c.getDBRef(), c.getName(), getRoom(c.getLocation()).getName(), c.getLocation()), client);
			}
			send("--------------------------------------------------------------------------", client);
		}
		else if ( param.equals("dbdump") ) {
			/*
			 * List all of the names and dbrefs of the objects
			 * in the database their actual index in the database
			 */
			objectDB.dump(client, this);
		}
		else if ( param.equals("holidays") ) {
			/* list the holidays */
			for (Map.Entry<String, Date> entry : holidays.entrySet()) {
				debug(entry.getKey() + ": " + MONTH_NAMES[((Date) entry.getValue()).getMonth() - 1] + " " + ((Date) entry.getValue()).getDay());
				send(entry.getKey() + ": " + MONTH_NAMES[((Date) entry.getValue()).getMonth() - 1] + " " + ((Date) entry.getValue()).getDay(), client);
			}
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
		else if ( param.equals("mem") | param.equals("memory") ) {
			send(checkMem(), client);
		}
		else if ( param.equals("pos") || param.equals("position") ) {
			Player player = getPlayer(client);
			Point position = player.getPosition();

			send("X: " + position.getX(), client);
			send("Y: " + position.getY(), client);
			send("Z: " + position.getZ(), client);
			
			send("Flying: " + player.isFlying(), client);
			send("Moving: " + player.isMoving(), client);
		}
		else if( param.equals("portals") ) {
			send("--- " + Utils.padRight("Portals", '-', 68), client);
			for (final Portal portal : portals) {
				String state = "inactive";
				if( portal.isActive() ) { state = "active"; }
				send("P " + portal.getName() + " (#" + portal.getDBRef() + ") [ " + state + " ]", client);
			}
			//send("" + Utils.padRight("", '-', 78), client);
		}
		else if ( param.equals("seasons") ) {
			/*
			 * list all the seasons
			 */
			for (final Season s : Seasons.getSeasons()) {
				send(s + ": " + MONTH_NAMES[s.beginMonth - 1] + " to " + MONTH_NAMES[s.endMonth - 1], client);
			}
		}
		else if ( param.equals("timedata") ) {
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
			// 500 ms -> 1 second = 30000ms -> 1 minute (30s -> 1m)
			// 166 ms -> 1 second = 9960ms -> 1 minute ( 9.960s -> 1m)
			send("Time Scale: 1 minute game time/" + ((((double) game_time.getScale()) * 60) / 1000) + " seconds real time", client);
		}
		else if( param.equals("timers") ) {
			send("Timers", client);
			send(Utils.padRight("", '-', 40), client);
			checkTimers();
			for(EffectTimer etimer : getEffectTimers( getPlayer( client ) )) {
				send("E " + etimer.getEffect().getName() + " ( " + etimer.getTimeRemaining() + " s )", client);
			}
			for(SpellTimer stimer : getSpellTimers( getPlayer( client ) )) {
				send("S " + stimer.getSpell().getName() + " ( " + stimer.getTimeRemaining() + " s )", client);
			}
			for(AuctionTimer atimer : getAuctionTimers( getPlayer( client ) )) {
				send("A " + atimer.getAuction().getItem().getName() + " ( " + atimer.getTimeRemaining() + " s )", client);
			}
			send(Utils.padRight("", '-', 40), client);
			send("A - Auction, E - Effect, S - Spell", client);
		}
		else if( param.equals("udbnstack") ) {
			send("Functionality Removed", client);
			client.write("Stack: [ ");
			Stack<Integer> unusedDBNs = objectDB.getUnused();

			for (int i = 0; i < unusedDBNs.size(); i++) {

				if ( i < unusedDBNs.size() - 1) {
					client.write("" + unusedDBNs.get(i));
				}
				else {
					client.write(unusedDBNs.get(i) + ", ");
				}
			}

			/*for (final Integer i : unusedDBNs) {
				client.write(i + ", ");
			}*/

			client.write(" ]\n");
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
			//cmd_help("@debug", client);
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
		if (arg.indexOf("=") != -1)
		{
			String name = Utils.trim( arg.substring(0, arg.indexOf("=")) );
			int parent = Utils.toInt(arg.substring(arg.indexOf("=") + 1, arg.length()), 0);

			System.out.println("Room Name: " + name);
			System.out.println("Room Parent: " + parent);

			Room room = createRoom(name, parent);

			// add rooms to database (main)
			objectDB.addAsNew(room);
			objectDB.addRoom(room);

			// tell us about it
			send("Room '" + room.getName() + "' created as #" + room.getDBRef() + ". Parent set to " + room.getLocation() + ".", client);
			//send("Room '" + room.getName() + "' created as #" + room.getDBRef() + ". Parent set to " + room.getParent() + ".", client);
		}
		else
		{
			send(gameError("@dig", ErrorCodes.INVALID_SYNTAX), client); // Invalid Syntax Error
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
		final Room room = getRoom(player);

		MUDObject m = null;

		// get object
		// if no argument or empty argument, fail with an error
		if (arg.equals("") || arg.equals(null))
		{
			send(gameError("@describe", ErrorCodes.INVALID_SYNTAX), client); // Invalid Syntax Error
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
			final ArrayList<Item> itemList = new ArrayList<Item>();

			// get drinkable items
			for (final Item item : player.getInventory()) {
				if ( item.isDrinkable() ) { // drinkable check
					itemList.add(item);
				}
			}

			debug(itemList);

			Item item = null;
			
			/*
			 * you should type 'drink healing' instead of 'drink potion' if you want a healing potion,
			 * otherwise you might get a potion of invisibility or bull's strength
			 */

			if ( player.getMode() == PlayerMode.COMBAT ) { // if in combat
				// try healing, etc potions first if just 'drink' is typed
				ArrayList<Item> healing = new ArrayList<Item>();

				for (final Item item1 : itemList) {
					/* need to check to see if something contains a healing effect
					 * 
					 * does it need to have solely a heal effect?
					 */
					if( item1.hasEffectType( Effect.Type.HEAL ) ) {
						healing.add(item1);
					}
				}
			}
			else { // else
				// search by name for the item
				for (final Item item1 : itemList) {
					if ( item1.getName().equals(arg) || item1.getName().contains(arg) ) {
						if(item1 instanceof Stackable) {
							item = ((Stackable<Item>) item1).split(1);
						}
						item = item1;
						break;
					}
				}
			}

			if (item != null) {
				// determine what kind of drinkable item it is and apply an effects
				// or status changes accordingly
				if (item instanceof Potion) {
					Potion potion = (Potion) item;

					debug("Potion?: " + potion.toString());

					List<Effect> effects = potion.getEffects();

					debug(effects);

					/* just one effect? */
					applyEffect(player, potion.getEffect());

					/*
					 * if the drinkable item is stackable too,
					 * then I need to be sure to use only one
					 */					
				}
				else {
					send("You take a sip of your " + colors(item.getName(), getDisplayColor("item")) + ".", client);
				}
				
				//Script s = item.getScript(TriggerType.onUse);

				//if( s != null ) {
					final String result = pgm.interpret( item.getScript(TriggerType.onUse), player, item );

					if( !result.equals("") ) send( result, client );
				//}
				
				player.getInventory().remove(item); // remove from inventory
				objectDB.remove( item );            // remove from database
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
		final Room room = getRoom(player);

		// split the arguments into a string array by space characters
		final String[] args = arg.split(" ");
		// tell us how many elements the array has (debug)
		debug(args.length);

		Item item;

		ArrayList<Item> inventory = player.getInventory();

		// get the integer value, if there is one, as the argument
		final int dbref = Utils.toInt(arg, -1);

		if ( arg.equalsIgnoreCase("all") ) {
			room.addItems( player.getInventory() );

			for(final Item item1 : player.getInventory()) {
				final String itemName = item1.getName();

				debug(itemName + " true");

				item1.setLocation( room.getDBRef() );
				item1.setPosition( player.getPosition() );

				send("You dropped " + colors(itemName, "yellow") + " onto the floor.", client);
				
				//addMessage( new Message( player.getName() + " dropped " + colors(itemName, "yellow") + " on the floor.", room ) );
			}

			player.getInventory().clear();

			return;
		}
		else {
			// get the object the argument refers to: by name (if it's in the calling player's inventory), or by dbref#
			// should be done by searching the player's inventory for the object and if there is such an object, drop it on the floor.
			for (int i = 0; i < player.getInventory().size(); i++)
			{			
				item = inventory.get(i);

				final String itemName = item.getName();

				// if there is a name or dbref match from the argument in the inventory
				if ( itemName.equals(arg) || itemName.contains(arg) && !arg.equals("") || item.getDBRef() == dbref )
				{
					debug(itemName + " true");

					/*
				// remove object from player inventory
    			inventory.remove(item);

    			// move object from player inventory to ground
				item.setLocation( room.getDBRef() );
				item.setPosition( player.getPosition() );
				room.addItem( item );
				//player.getInventory().remove(item);
					 */

					drop(player, item);

					// check for silent flag to see if object's dbref name should be shown as well?
					if( !player.hasFlag(ObjectFlag.SILENT) ) {
						send("You dropped " + colors(itemName, "yellow") + "(#" + item.getDBRef() + ") on the floor.", client);
					}
					else {
						send("You dropped " + colors(itemName, "yellow") + " on the floor.", client);
					}

					// return message telling others that the player dropped the item?
					// obviously we want the players in the current room that can see something
					addMessage( new Message( player.getName() + " dropped " + colors(itemName, "yellow") + " on the floor.", room ) );

					return;
				}
			}
			send("You don't have that.", client);
		}
	}

	/**
	 * Command: edit
	 * 
	 * Edit an object. Determines what kind of object and "opens"
	 * the appropriate editor
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_edit(final String arg, final Client client) {
		Player player = getPlayer(client);
		String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT); // set the 'edit' status flag

		int dbref = Utils.toInt(arg, -1);

		if(dbref != -1) {
			MUDObject m = objectDB.get(dbref);

			if( m instanceof Room ) {
				Room room = (Room) m;
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
	 * Command: enter
	 * 
	 * Enter into some kind of special object or any Thing with ENTER_OK flag set
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_enter(final String arg, final Client client) {
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
		
		Item item = null;
		//Slot slot = null;

		if (arg.equals("") && i == -1) {
			send("Equip what?", client);
			return;
		}
		else {
			for(Item item1 : player.getInventory()) {
				if (arg.equals(item1.getName())  || i == item1.getDBRef()) {
					item = item1;
				}
			}

			if ( item != null) {
				if ( item.equippable ) {
					send("Equip Type: " + item.getEquipType(), client);

					// equips the item in the first available slot
					//for (final String s : player.getSlots().keySet()) {
					for(final Slot slot : player.getSlots( item.getEquipType().toString() ) ) {
						//debug(s);

						//slot = player.getSlots().get(s);
						
						// a slot of with ItemType.NONE can hold any item type
						if ( slot.isType(item.getEquipType()) || slot.isType(ItemType.NONE) ) {
							if ( !slot.isFull() ) {
								/*
								 * handle any OnEquip effects/events
								 */

								debug("Equip Type " + item.getEquipType() + " matches " + slot.getType());

								slot.insert(item);                  // put item in the slot
								player.getInventory().remove(item); // remove it from the inventory
								item.equipped = true;               // set item's equipped "flag" to true (equipped)

								if(item instanceof ExtraCommands) {
									debug("Item has extra commands.");
									for(Map.Entry<String, Command> cmd : ((ExtraCommands) item).getCommands().entrySet()) {
										player.commandMap.put( cmd.getKey(), cmd.getValue() );
										debug("Added " + cmd.getKey() + " to player's command map from " + item.getName());
									}
								}

								item = null;                        // set item reference to null

								send(slot.getItem().getName() + " equipped (" + slot.getItem().getEquipType() + ")", client);

								break; // break the for loop, so we don't try to insert a now null object somewhere else
							}
							else {
								// are these alternative messages?
								send("You can't equip that. (Equip Slot Full)", client);
								send("Where are you going to put that? It's not like you have a spare...", client);
							}
						}
						else {
							send("You can't equip that. (Equip Type Incorrect)", client); //only useful if I force specifics of equipment?
							debug("Equip Type " + item.getEquipType() + " does not match " + slot.getType());
						}
					}
				}
				else {
					send("You can't equip that. (Not Equippable)", client);
					return;
				}
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
			Room room = getRoom( getPlayer(client) );
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
		final String exitNames = getRoom(getPlayer(client)).getVisibleExitNames();
		
		if (exitNames != null && !exitNames.equals("")) {
			send(colors("Exits: " + exitNames, getDisplayColor("exit")), client);
		}
		else {
			send(colors("Exits:", getDisplayColor("exit")), client);
		}
	}

	private void cmd_feats(final String arg, final Client client) {
		Player player = getPlayer(client);

		List<Feat> feats = new ArrayList<Feat>();
		
		feats.add(Feat.ap_light);
		feats.add(Feat.ap_medium);
		feats.add(Feat.ap_heavy);

		send("Feats", client);
		for(Feat feat : feats) {
			send(feat.getName(), client);
		}

		send("Your Feats", client);
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
		// @flag <object> = <flags
		// ex. @flag me=D, should add the dark flag
		final String[] args = arg.split("=");

		final Player player = getPlayer(client);
		final Room room = getRoom(player);

		final ObjectFlag flag;

		if (args.length > 1) {
			MUDObject m = null;
			
			// only the first flag indicated matters if the ! symbol is present
			if (args[1].contains("!")) {				
				flag = ObjectFlag.fromLetter(args[1].charAt(1));

				if (args[0].equals("me")) {
					m = player;
					//player.removeFlag( flag );
					//send("Removed " + flag.toString() + " flag from " + player.getName(), client);
				}
				else if (args[0].equals("here")) {
					m = room;
					//room.removeFlag( flag );
					//send("Removed " + flag.toString() + " flag from " + room.getName(), client);
				}
				else {
					//MUDObject m = null;
					
					int dbref = Utils.toInt(args[0], -1);

					if(dbref != -1) {
						m = getObject(dbref);
					}
					else {
						m = getObject(args[0]);
					}

					/*if(m != null) {
						m.removeFlag( flag );
						send("Removed " + flag.toString() + " flag from " + m.getName(), client);
					}
					else send("No such object.", client);*/
				}
				
				if(m != null) {
					m.removeFlag( flag );
					send("Removed " + flag.toString() + " flag from " + m.getName(), client);
				}
				else send("No such object.", client);
			}
			else {
				send("Adding Flag(s)", client);
				
				if (args[0].equals("me")) {
					debug("New FlagString: " + args[1] + player.getFlagsAsString());
					debug("New FlagString(reversed): " + Utils.reverseString( args[1] + player.getFlagsAsString() ) );
					player.setFlags( ObjectFlag.getFlagsFromString( args[1] + player.getFlagsAsString() ) );
					send(player.getName() + " flagged " + ObjectFlag.fromLetter(args[1].charAt(0)), client);
				}
				else if (args[0].equals("here")) {
					room.setFlags(ObjectFlag.getFlagsFromString(room.getFlagsAsString() + args[1]));
					send(room.getName() + " flagged " + ObjectFlag.fromLetter(args[1].charAt(0)), client);
				}
				else {
					MUDObject m1 = null;
					
					int dbref = Utils.toInt(args[0], -1);

					if(dbref != -1) {
						m1 = getObject(dbref);
					}
					else {
						m1 = getObject(args[0]);
					}

					if(m1 != null) {
						m1.setFlag( ObjectFlag.fromLetter(args[1].charAt(0)) );
					}
					else send("No such object.", client);
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
		MUDObject m;

		if( arg.charAt(0) == '#' ) {
			m = getObject( Utils.toInt(arg.substring(1), -1) );
		}
		else if( Character.isDigit(arg.charAt(0)) ) {
			m = getObject( Utils.toInt(arg, -1) );
		}
		else {
			m = getObject(arg);
		}

		if( m != null ) {
			debug(m.getFlags());
			debug(m.getDBRef());
			client.writeln("Flags: " + ObjectFlag.toInitString(m.getFlags()));
		}
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
		// syntax: @find [<type>] <search string>
		final LinkedList<String> matches = new LinkedList<String>();

		char param = 0;
		String searchString = null;

		String args[] = arg.split(" ");

		if(args.length > 1) {
			param = args[0].charAt(1);
			searchString = args[1];
		}
		else searchString = arg;

		switch(param) {
		case 'p':
			System.out.println("Find Players");
			/*if( searchString == null ) {
				matches.addAll(objectDB.players.keySet());
			}
			else {
				for(final MUDObject m : objectDB.players.values()) {
					if( m.getName().contains(searchString) ) {
						matches.add(m.getName() + " (#" + m.getDBRef() + ")");
					}
				}
			}*/
			break;
		case 'n':
			System.out.println("Find NPCs");
			/*if( searchString == null ) {
				matches.addAll(objectDB.npcsByName.keySet());
			}
			else {
				for (final MUDObject m : objectDB.npcsByName.values()) {
					if( m.getName().contains(searchString) ) {
						matches.add(m.getName() + " (#" + m.getDBRef() + ")");
					}
				}
			}*/
			break;
		default:
			System.out.println("Find Any");
			for (final MUDObject m : objectDB.findByLower(arg)) {
				System.out.println(m.getName() + " (#" + m.getDBRef() + ")");
				matches.add(m.getName() + " (#" + m.getDBRef() + ")");
			}
			break;
		}


		for (final String s : matches) {
			send(s, client);
		}
		send("**********", client);
		send(matches.size() + " objects found.", client);
	}
	
	private void cmd_fly(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Race race = player.getRace();
		
		if( race.canFly() ) {
			player.setFlying(true);
			player.changePosition(0, 0, 1);
		}
	}
	
	private void cmd_land(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Race race = player.getRace();
		
		if( race.canFly() ) {
			if( player.isFlying() ) {
				// TODO fix kludge, we assume here that the player will land straight down to the zero coordinate
				final Point pt = player.getPosition();
				pt.setZ(0);
				
				move(player, pt);
			}
		}
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

		final Player player = getPlayer(client);
		final Room room = getRoom( player );

		if (args.length == 1) {
			if( args[0].equals("north") || args[0].equals("south") || args[0].equals("east") || args[0].equals("west") ) {
				
			}
			else {
				MUDObject m = getObject(arg, room);
				
				if (m != null) {
					move( player, m.getPosition() );
				}
			}
		}
		else if (args.length >= 2) {
			int x = Integer.parseInt(args[0]);
			int y = Integer.parseInt(args[1]);
			
			// TODO validate coordinates in current room
			
			move( player, new Point(x, y) );
		}
	}

	/**
	 * Get items from inside of things?
	 * 
	 * ex. get <x> <y>
	 * 
	 * NOTE: should only work on current room
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_get(final String arg, final Client client) {
		Player player = getPlayer(client);
		Room room = getRoom(player);
		
		// process syntax/arguments
		
		// i.e. take <item> from <container>
		//perhaps get <item> from <container> would be better
		
		// if there is no argument
		if ( arg.equals("") ) {
			send("Syntax: get <container> <item>", client);
		}
		else {
			// split the arguments into a string array by space characters
			final String[] args = arg.split(" ");
			// tell us how many elements the array has (debug)
			debug(args.length);

			// look in the player's inventory
			for(Item item : player.getInventory()) {
				if(item.getName().equalsIgnoreCase(args[1]) && item instanceof Container) {
					Container c = (Container) item;

					System.out.println("Arg: " + args[0]);

					Item item1 = null;
					
					if( c != null ) {
						item1 = c.retrieve(args[0]);
					}
					else {
						send("No such container");
						return;
					}

					if( item1 != null ) {
						System.out.println("Item: " + item1.getName());
						System.out.println("Container: " + c.getName());

						c.getContents().remove(item1);
						player.getInventory().add( item1 );
						item1.setLocation(player.getDBRef());
						System.out.println(player.getInventory());

						send("You get " + colors(item1.getName(), getDisplayColor("item")) + " from " + c.getName(), client);

						return;
					}
					else send("No such item", client);
				}
			}

			// get a list of the objects that the player can see
			List<MUDObject> foundObjects = findVisibleObjects(room);

			// look in the room
			for(final MUDObject m : foundObjects) {
				if( m instanceof Thing && m instanceof Storage ) {
					if( m.getName().equalsIgnoreCase(args[0]) ) {
						Storage s = (Storage) m;

						Item item = s.retrieve(args[1]);

						if( item != null ) {
							player.getInventory().add( item );
							item.setLocation(player.getDBRef());
							System.out.println(player.getInventory());

							send("You get " + colors(item.getName(), getDisplayColor("item")) + " from " + ((MUDObject) s).getName(), client);
							
							return;
						}
					}
				}
			}
		}
	}

	/**
	 * Command: greet
	 * 
	 * Greet another player (this tells them your name with some specificity).
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
		
		final Player player = getPlayer(client);

		if (arg.equals("@reload"))
		{
			if( checkAccess(player, Constants.BUILD) ) {
				help_reload();
				client.write("Game> Help Files Reloaded!\n");
			}
			
			return;
		}

		if ( arg.equals("") ) {
			arg = "help";
		}
		
		// config options retrieval
		final boolean pagerEnabled = player.getConfig().get("pager_enabled");
		final int lineWidth = player.getLineLimit();

		final String[] helpfile = getHelpFile(arg);
		final String[] topicfile = getTopicFile(arg);

		if (helpfile != null)
		{
			if (helpfile.length > 25 && pagerEnabled) {
				//player.setPager( new Pager( helpfile ) );
				player.setPager( new Pager( Arrays.copyOfRange(helpfile, 1, helpfile.length) ) );
				player.setStatus("VIEW");

				op_pager("view", client);
			}
			else {
				for (final String line : Arrays.copyOfRange(helpfile, 1, helpfile.length) ) {
					client.write(check(line) + "\r\n");
				}
				
				// tell us when the file was last modified/updated
				/*System.out.println(parent.HELP_DIR + arg + ".txt");
				File file = new File(parent.HELP_DIR + arg + ".txt");
				try {
					client.writeln("Last Updated: " + Utils.unixToDate( file.lastModified() ));
				}
				catch (ParseException pe) { pe.printStackTrace(); }*/
			}
			
			StringBuilder sb = new StringBuilder();
			
			if( aliases.containsKey(arg) ) {
				sb.append( aliases.get(arg) );
			}
			else if( aliases.containsValue(arg) ) {
				for(final String key : aliases.keySet()) {
					final String value = aliases.get(key);
					if( value.equals(arg) ) {
						sb.append( value + " " ); 
					}
				}
			}
			
			client.writeln("ALIASES: " + sb.toString());
		}
		else if (topicfile != null) {
			
			if (topicfile.length > 25 && pagerEnabled) {
				//player.setPager( new Pager( topicfile ) );
				player.setPager( new Pager( Arrays.copyOfRange(topicfile, 1, topicfile.length) ) );
				player.setStatus("VIEW");

				op_pager("view", client);
			}
			else {
				showDesc(Utils.join(topicfile, " "), lineWidth, client);
				/*for (final String line : topicfile) {
					client.write(line + "\r\n");
				}*/
				/*for (final String line : Arrays.copyOfRange(topicfile, 1, topicfile.length) ) {
					client.write(check(line) + "\r\n");
				}*/
				
				// tell us when the file was last modified/updated
			}
		}
		else if ( commandMap.containsKey(arg) ) {
			Command c = commandMap.get(arg);
			send(arg + " - " + c.getDescription(), client);
		}
		else if ( player.commandMap.containsKey(arg) ) {
			Command c = player.commandMap.get(arg);
			send(arg + " - " + c.getDescription(), client);
		}
		else
		{
			client.write("No such help file!\r\n");
		}
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

		player.setStatus(Constants.ST_EDIT); // set the 'edit' status flag
		player.setEditor(Editors.HELP);

		boolean exist = false;
		
		// test for existence of helpfile?
		if (helpTable.get(arg) != null) {
			exist = true;
		}

		final EditList list;

		if (!exist) { // if it doesn't exist, create a new one

			send("Game> (help editor) Error: Invalid Help File!", client);
			send("Game> (help editor) Creating new help file...", client);

			player.startEditing(arg);
			list = player.getEditList();

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

			player.loadEditList(arg, loadList(HELP_DIR + arg + ".help"));
			list = player.getEditList();

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
		send("Type '.help' or '.h' for help.\n");

		String header = "< List: " + list.name + " Line: " + list.getLineNum() + " Lines: " + list.getNumLines() + "  >";
		send(header, client);
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
		send("Tenpony Tower", client);
		send("   o " + colors(Utils.padRight("Apartment", ' ', 16) + " " + "[ 250 / 1000 ]", "green"), client);
		send("   o " + colors(Utils.padRight("Luxury Apartment", ' ', 16) + " " + "[ 5 / 50]", "yellow"), client);
		send(colors("=========================================================", "cyan"), client);
	}
	
	private void cmd_inspect(final String arg, final Client client) {
		Player player = getPlayer(client);
		
		List<Item> items = objectDB.getItemsByLoc( player.getLocation() );
		
		StringBuilder sb = new StringBuilder();
		
		for(Item item : items) {
			if( item.getName().equals(arg) ) {
				final LinkedHashMap<String, Object> visual_props = item.getVisualProperties();
				
				send("Inspecting the " + item.getName() + " you see:", client);
				
				// engraved numbers or names, rust spots, locks
				for(final String s : visual_props.keySet()) {					
					if( s.substring( s.indexOf('/') + 1 ).startsWith("engraving") ) {
						if( s.contains("number") ) {
							send(colors("an engraved number", "purple"), client);
						}
						else if( s.contains("script") ) {
							send(colors("an engraved script", "purple"), client);
						}
					}
					
					send("a " + colors(s, "purple"), client);
				}
				
				//send(sb.toString(), client);
				return;
			}
		}
	}
	
	private void cmd_interact(final String arg, final Client client) {
		Player player = getPlayer(client);
		
		player.setStatus(Constants.ST_INTERACT); // mark the player as in interaction mode
		
		NPC npc = getNPC(arg);   // get the npc by name

		if( npc != null ) {
			debug(npc.getName());

			getPlayer(client).setTarget(npc);   // "target" the npc
			
			npc.interact(player);
			
			/*if (npc.getFlags().contains("V")) {
				debug("Target is NPC.");
				debug("Target is Vendor");
				
				// differentiate between vendor types?
				
				Vendor v = (Vendor) npc;
				v.interact();
				debug("Using default Vendor interaction.");
			}
			else {
				debug("Target is NPC.");
				debug("Using default NPC interaction.");
				ArrayList<Message> msgs = npc.interact(0);
				for (Message m : msgs) { addMessage(m); }
			}*/
		}
		else send("NPC(" + arg + ") is NULL.", client);
	}

	/**
	 * Command: inventory
	 * 
	 * check player inventory
	 * 
	 * @param arg    unused
	 * @param client the client
	 */
	private void cmd_inventory(final String arg, final Client client)
	{
		final Player player = getPlayer(client);

		if (player != null) // if the player exists
		{
			debug(player.getInventory()); 
			send(player.getName() + "'s Inventory:", client);

			//if (player.getInvType() == 'S') { // simple inventory display
			//else if (player.getInvType() == 'C') { // complex inventory display

			if( player.getConfig().get("complex-inventory") == false ) {
				for (final Item item : player.getInventory())
				{
					if (item != null) {
						//send(colors(item.getName(), "yellow"), client);
						send(colors(item.toString(), "yellow"), client);
						//send(colors(item.getName(), "yellow") + " " + "(#" + item.getDBRef() + ")", client);

						if( item instanceof Container ) {
							for(Item item1 : ((Container) item).getContents()) {
								send("  " + colors(item1.getName(), "yellow") + " ( in " + item.getName() + " )", client);
							}
						}
					}
					else {
						debug("Item is null");
					}
				}
			}
			else {
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
						if (item instanceof Container) {
							if( ((Container) item).getContents().size() > 0 ) {
								displayContainer((Container) item, client);
							}
							else {
								//String itemString = colors(item.getName(), "yellow") + "(#" + item.getDBRef() + ")";
								//String itemString = item.getName() + "(#" + item.getDBRef() + ")";

								//String itemString = item.getName();
								String itemString = item.toString();

								String padded = Utils.padRight(itemString);

								StringBuffer sb = new StringBuffer(padded);

								sb.insert(0, colorCode("yellow"));
								//sb.insert(sb.indexOf("("), colorCode("white"));
								sb.append(colorCode("white"));

								//send("|" + Utils.padRight(itemString) + "|", client);
								send("|" + sb.toString() + "|", client);
							}
						}
						else {
							//String itemString = colors(item.getName(), "yellow") + "(#" + item.getDBRef() + ")";
							//String itemString = item.getName() + "(#" + item.getDBRef() + ")";

							//String itemString = item.getName();
							String itemString = item.toString();

							String padded = Utils.padRight(itemString);

							StringBuffer sb = new StringBuffer(padded);

							sb.insert(0, colorCode("yellow"));
							//sb.insert(sb.indexOf("("), colorCode("white"));
							sb.append(colorCode("white"));

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
	
	// TODO write code for cmd_creatureedit(...)
	private void cmd_creatureedit(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT);
		player.setEditor(Editors.CREATURE);

		EditorData newEDD = new EditorData();

		// create new creature if no creature to edit specified
		if ( arg.equals("") ) {
			Creature cre = createCreature();

			if ( cre.Edit_Ok ) {
				cre.Edit_Ok = false; // further edit access not permitted (only one person may access at a time)
			}
			else { // item is not editable, exit the editor
				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Creature Editor - Error: creature not editable (!Edit_Ok)", client);

				return;
			}

			// record prior player status
			newEDD.addObject("pstatus", old_status);

			// add item and it's constituent parts to the editor data
			newEDD.addObject("creature",  cre);
			newEDD.addObject("desc", cre.getDesc());
			newEDD.addObject("name", cre.getName());
			newEDD.addObject("type", cre.getCreatureType());

			player.setEditorData(newEDD);
		}
		else {
			Creature cre = null;
			boolean exist = false;

			try {
				/*int dbref = Integer.parseInt(arg);
				//item = getItem(dbref);
				
				// the below segment is accessing a NULL POINTER as it stands
				if ( cre.Edit_Ok ) {
					cre.Edit_Ok = false; // further edit access not permitted (only one person may access at a time
				}
				else { // item is not editable, exit the editor
					// reset player, and clear edit flag and editor setting
					player.setStatus(old_status);
					player.setEditor(Editors.NONE);

					// clear editor data
					player.setEditorData(null);

					send("Game> Creature Editor - Error: item not editable (!Edit_Ok)", client);

					return;
				}

				exist = true;*/
				
				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Creature Editor - Error: functionality not implemented.", client);

				return;
			}
			catch(NumberFormatException nfe) { // no item with that dbref, cannot edit (abort)
				System.out.println("--- Stack Trace ---");
				nfe.printStackTrace();

				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Creature Editor - Unexpected error caused abort (number format exception)", client);
			}
			catch(NullPointerException npe) { // null item, cannot edit (abort)
				System.out.println("--- Stack Trace ---");
				npe.printStackTrace();
				
				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Creature Editor - Unexpected error caused abort (null pointer exception)", client);
			}

			if (exist) {	// item exists
				// record prior player status
				newEDD.addObject("pstatus", old_status);

				// add item and it's constituent parts to the editor data
				//newEDD.addObject("item", item);
				//newEDD.addObject("desc", item.getDesc());
				//newEDD.addObject("name", item.getName());
				//newEDD.addObject("type", item.getItemType());
			}
			else { // item doesn't exist (abort)
				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Creature Editor - Error: creature does not exist", client);

				return;
			}

			player.setEditorData(newEDD);
		}

		op_creatureedit("show", client); // print out the info page
	}

	/**
	 * Command to launch item editor (iedit)
	 * 
	 * NOTE: editor concept borrowed from ROM, a derivative of Merc,
	 * a derivative of DIKU.
	 * 
	 * Basically you call the item editor like this (at least from inside the code):
	 * 'cmd_itemedit(<item #/item name>, client)'
	 * 
	 * And it attempts to find the item and edit it,
	 * if it can't find it, it will indicate a failure and
	 * open the editor with no item.
	 */
	private void cmd_itemedit(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT);
		player.setEditor(Editors.ITEM);

		EditorData newEDD = new EditorData();

		// create new item if no item to edit specified
		if ( arg.equals("") ) {
			Item item = createItem();

			if ( item.Edit_Ok ) {
				item.Edit_Ok = false; // further edit access not permitted (only one person may access at a time)
			}
			else { // item is not editable, exit the editor
				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

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
					player.setEditor(Editors.NONE);

					// clear editor data
					player.setEditorData(null);

					send("Game> Item Editor - Error: item not editable (!Edit_Ok)", client);

					return;
				}

				exist = true;
			}
			catch(NumberFormatException nfe) { // no item with that dbref, cannot edit (abort)
				System.out.println("--- Stack Trace ---");
				nfe.printStackTrace();

				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Item Editor - Unexpected error caused abort (number format exception)", client);
			}
			catch(NullPointerException npe) { // null item, cannot edit (abort)
				System.out.println("--- Stack Trace ---");
				npe.printStackTrace();
				
				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

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
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Item Editor - Error: item does not exist", client);

				return;
			}

			player.setEditorData(newEDD);
		}

		op_itemedit("show", client); // print out the info page
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

		final int dbref = Utils.toInt(arg, -1);

		// try to find the room, by dbref or by name
		Room room = (dbref != -1) ? getRoom(dbref) : getRoom(arg);

		// if we found the room, send the player there
		if ( room != null ) {
			getRoom(player).removeListener(player); // remove listener

			send("Jumping to " + room.getName() + "... ", client);
			player.setLocation(room.getDBRef());
			player.setPosition(0, 0);
			send("Done.", client);
			room = getRoom(player);
			look(room, client);

			getRoom(player).addListener(player); // add listener
		}
		else {
			send("Jump failed.", client);
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

		if (player.getStatus().equals(Constants.ST_INTERACT)) {
			MUDObject target = player.getTarget();

			if (target instanceof Vendor) {
				if(arg.equals("")) {
					send("-----< Stock >--------------------", client);

					for (final Item item : ((Vendor) player.getTarget()).list()) {
						if (item instanceof Weapon) {
							final Weapon w = (Weapon) item;
							//send(colors("+" + w.getMod() + " " + w.weapon.getName() + " (" + w.getWeight() + ") Cost: " + w.getCost(), "yellow"), client);
							send(colors(w.toString() + " (" + w.getWeight() + ") Cost: " + w.getValue(), "yellow"), client);
						}
						else if (item instanceof Armor) {
							final Armor a = (Armor) item;
							//send(colors("+" + a.getMod() + " " + a.getName() + " (" + a.getWeight() + ") Cost: " + a.getCost(), "yellow"), client);

							final String armorInfo = a.toString() + " (" + a.getWeight() + ") ";
							send(colors(Utils.padRight(armorInfo, ' ', 30) + " " + a.getValue(), "yellow"), client);
						}
						else {
							send(colors(item.getName() + " (" + item.getWeight() + ") Cost: " + item.getValue(), "yellow"), client);
						}
					}

					send("----------------------------------", client);
				}
			}
			else {
				send("That's not a vendor.", client);
			}
		}
		else {
			send("You're not interacting with anyone.", client);
		}
	}
	
	/**
	 * Command: load
	 * 
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_load(final String arg, final Client client) {
		// check prototype table?
	}
	
	/**
	 * Command: lock (applies to lockable things)
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_lock(final String arg, final Client client) {
		Room room = getRoom( getPlayer( client ) );
		
		List<MUDObject> objects = objectDB.getByRoom( room );

		MUDObject object = null;

		for(MUDObject obj : objects) {
			if( obj.getName().equals(arg) ) {
				object = obj;
			}
		}
		
		//Exit exit = getExit(arg);
		//if( exit != null ) {
		
		if( object != null ) {
			//if( object.isType(TypeFlag.EXIT) ) {
			if( object instanceof Exit ) {
				Exit exit = (Exit) object;

				if (exit instanceof Lockable) {
					Lockable l = (Lockable) exit;

					if (!l.isLocked()) {
						l.lock();
						send("You lock " + exit.getName() + ".", client);
						return;
					}

					send("It's already locked.", client);
					return;
				}

				send("It's a good thing no one saw you trying to lock a " + exit.getExitType().toString() + " with no lock.", client);
			}
		}
	}
	
	/**
	 * Command: look
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_look(final String arg, final Client client)
	{
		debug("Look Command");
		
		// get player, room objects to work with
		final Player player = getPlayer(client);
		final Room room = getRoom(player);

		final String[] args = arg.split(" ");
		for (final String s : args) {
			debug(s);
		}

		// if no argument or empty argument, show the room
		if ( arg.equals("") ) {
			look(room, client);
			return;
		}
		else {
			debug("Argument (String): " + arg);


			if( arg.toLowerCase().equals("here") ) {
				look(room, client);
				return;
			}
			else if ( arg.toLowerCase().equals("me") ) {
				look(player, client);
				return;
			}
			else {

				// decide what else is visible and then find the best match in there
				ArrayList<MUDObject> objectsFound = findVisibleObjects(room);
				objectsFound.addAll( player.getInventory() );

				int spec = 0;

				// supposed to handle a syntactical structure like 'sword.2'
				if ( arg.contains(".") ) {
					spec = Integer.parseInt( arg.substring( arg.indexOf('.') ) );
					debug("Specifier: " + spec);
				}
				
				debug("Objects Found: " + objectsFound);
				
				// just the items in the current room (looks here first)
				for(MUDObject m : objectsFound) {
					final String name = m.getName();
					final List<String> components = Arrays.asList(m.getName().toLowerCase().split(" "));
					
					String name_lc = name.toLowerCase();
					String arg_lc = arg.toLowerCase();
					
					debug("Argument:              " + arg);
					debug("Name:                  " + name);
					debug("Argument (Lower Case): " + arg_lc);
					debug("Name (Lower Case):     " + name_lc);
					debug("Components:            " + components);
					
					/*
					 * 1) is the name the same as ARG (ignoring case -- setting both name and arg to lowercase)
					 * 2) does the name start with ARG (ignoring case -- setting both name and arg to lowercase)
					 * 3) does the name end with ARG (ignoring case -- setting both name and arg to lowercase)
					 * 4) does the name contain ARG (ignoring case -- setting both name and arg to lowercase) 
					 * 5) is any component of the name the same as the arg (continues non-whitespace separated segments)
					 */
					
					boolean sameName = name.equalsIgnoreCase(arg);
					boolean startsWith = name_lc.startsWith(arg_lc);
					boolean endsWith = name_lc.endsWith(arg_lc);
					boolean nameContains = name_lc.contains(arg_lc);
					boolean compsContain = components.contains(arg_lc);
					
					boolean test = false;
					
					for(String s : components) { for(String s1 : Arrays.asList(arg.toLowerCase().split(" "))) { if( s.contains(s1) ) test = true; break; } }
					
					// for string in A, is A.S a substring of string name N.S
					if( sameName || startsWith || endsWith || nameContains || compsContain || test ) {
						look(m, client);
						return;
					}
				}
				
				// not sure if look should apply to dbrefs/any old name, permissions?
				final int dbref = Utils.toInt(arg, -1);
				MUDObject m = null;

				if (dbref != -1) {
					m = getObject(dbref);
				}
				else {
					if (spec == 0) {
						m = getObject(arg);
					}
					
					//else { MUDObject[] mObjs = getObjects(arg); }
				}
				
				if( m != null ) {
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
			}
		}
	}
	
	/**
	 * Command: lookat
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_lookat(final String arg, final Client client) {
		final Room room = getRoom(getPlayer(client));

		if (!arg.equals("")) {

			// get properties (I think we should have /visuals "folder" for visual properties
			// i.e. 'ceiling', 'floor', 'wall(s)'
			Object o = room.getProperty("/visuals/" + arg);

			if (o != null && o instanceof String) {
				String result = (String) o;
				//send("You look at the " + arg, client);
				//send(result, client);
				send("You look at the " + arg + ": " + result , client);
				return;
			}
			else {
				send("You look around, but don't see that.", client);
			}
		}
		else {
			send("Look at what?", client);
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

		player.setStatus(Constants.ST_EDIT); // flag us as being in EDIT mode
		player.setEditor(Editors.LIST);      // set the editor we want to use

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
	 * Command: listprops
	 * 
	 * List properties stored on a MUDObject
	 * 
	 * @param arg    an identifier/key that refers to the object (ex. name, dbref)
	 * @param client the client that sent the command
	 */
	private void cmd_listprops(final String arg, final Client client) {

		Player player = getPlayer(client);
		Room room = getRoom(player);

		debug("ARG: " + arg);
		
		final String[] args = arg.split("=");
		
		MUDObject object = null;

		// get the specified objects
		if (args[0].toLowerCase().equals("here")) {
			object = room;
		}
		else if( args[0].toLowerCase().equals("me") ) {
			object = player;
		}
		else {
			int dbref = Utils.toInt(args[0], -1); // parse string to get dbref
			
			if( dbref != -1 ) object = getObject(dbref);   // get object by dbref
			else              object = getObject(args[0]); // get object by name
		}
		
		LinkedHashMap<String, Object> props = null;

		if( object != null ) {
			props = object.getProperties();

			final String type = object.type.toString().toLowerCase();

			send(colors(object.getName(), getDisplayColor(type)) + " (#" + object.getDBRef() + ")" + colorCode("white"), client);

			if( args.length == 2 ) {
				System.out.println("ARG0: \'" + args[0] + "\'");
				System.out.println("ARG1: \'" + args[1] + "\'");
				
				for (final Object k : props.keySet()) {
					if( ((String) k).startsWith(args[1]) ) {
						send((String) k + " " + props.get(k), client);
					}
				}
			}
			else {
				for (final Object k : props.keySet()) {
					send((String) k + " " + props.get(k), client);
				}
			}
			
			//System.out.println("KEY: \'" + (String) k + "\'");
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
			System.out.println("--- Stack Trace ---");
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
				case '$': // Shop
					// set foreground yellow
					client.write("" + Colors.YELLOW);
					//client.write("\33[33m");
					client.write('$');
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
	private void cmd_move(final String arg, final Client client)
	{
		Player player = getPlayer(client);

		final String direction = arg.toLowerCase();

		if (direction.equals("north"))
		{
			if (player.getLocation() - 10 >= 0)
			{
				System.out.println("success");
				player.setLocation(player.getLocation() - 10);
			}
		}
		else if (direction.equals("south"))
		{
			player.setLocation(player.getLocation() + 10);
			System.out.println("success");
		}
		else if (direction.equals("east"))
		{
			player.setLocation(player.getLocation() + 1);
			System.out.println("success");
		}
		else if (direction.equals("west"))
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

		look(getRoom(player), client);

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

			String oldName = m.getName();
			String newName = args[1];

			if( !(m instanceof Player) ) {
				objectDB.changeName( m, newName );

				send("Game> Changed name of " + oldName + "(#" + m.getDBRef() + ") to " + newName, client);
			}
			else {
				send("Game> Cannot change name of players.", client);
			}
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

		boolean globalNameRefs = false;

		if( player.getConfig() != null)
		{
			if( player.getConfig().get("global-nameref-table") ) {
				globalNameRefs = true;
			}
		}

		if( !globalNameRefs ) {
			if (arg.toLowerCase().equals("#list")) {
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
			else if (args.length == 2) {
				if (args[0].equals("#delete")) {
					player.getNameReferences().remove(args[1]);
					send("nameRef deleted.", client);
				}
				else {
					try {
						player.setNameRef(args[0], Integer.parseInt(args[1]));
						send("nameRef allocated.", client);
						send(args[0].substring(0, args[0].length()) + " allocated to " + args[1], client);
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
			}
		}
		else {
			if (arg.toLowerCase().equals("#list")) {
				send("Name Reference Table (Global)", client);
				send("------------------------------------------------", client);
				for (String str : getNameReferences()) {
					send(str + " -> " + getNameRef(str), client);
				}
				send("------------------------------------------------", client);
			}
			else if (arg.toLowerCase().equals("#clear")) {
				clearNameRefs();
				send("Name Reference Table cleared!", client);
			}
			else if (args.length == 2) {
				if (args[0].equals("#delete")) {
					getNameReferences().remove(args[1]);
					send("nameRef deleted.", client);
				}
				else {
					try {
						setNameRef(args[0], Integer.parseInt(args[1]));
						send("nameRef allocated.", client);
						send(args[0].substring(0, args[0].length()) + " allocated to " + args[1], client);
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
			}
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
		
		Room room;
		
		try {
			// get the source room
			room = getRoom(getPlayer(client));
			
			source = room.getDBRef();
			
			if (args.length == 2) { // simple form - name=destination
				// destination defaults to an invalid room dbref
				if (args[1].equals("")) { destination = -1; }
				else { destination = Integer.parseInt(args[1]); }
			}
			else {
				send( "open : " + gameError("@open", ErrorCodes.INVALID_SYNTAX), client);
				return;
			}
		}
		catch(NumberFormatException nfe) {
			send( "@open : destination dbref invalid (number format)\nExit creation failed", client );
			//nfe.printStackTrace();
			return;
		}
		catch(NullPointerException npe) {
			send( "@open : source Room invalid (null pointer)\nExit creation failed", client );
			//npe.printStackTrace();
			return;
		}
		
		if( !validExitName(name) ) {
			send( "@open : Invalid exit name (contained disallowed characters)\nExit creation failed", client );
			return;
		}

		// create the exit
		Exit exit = new Exit(name, source, destination);

		objectDB.addAsNew(exit);
		objectDB.addExit(exit);

		// add the exit to the source room
		room.getExits().add(exit);

		// tell us that we succeeded in creating the exit
		send("You open an exit called " + exit.getName() + "(#" + exit.getDBRef() + ")" + " from #" + exit.getLocation() + " to #" + exit.getDestination() + ".", client);
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

		Room room = getRoom(getPlayer(client));

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
				send( "@door : " + gameError("@open", ErrorCodes.INVALID_SYNTAX), client);
				return;
			}
		}
		catch(NumberFormatException nfe) {
			send( "open : source or destination dbref invalid, exit creation failed", client );
			return;
		}

		// create the exit
		Exit exit = new Exit(name, source, destination);

		objectDB.addAsNew(exit);
		objectDB.addExit(exit);
		
		// get the source roo
		room = getRoom(source);

		// add the exit to the source room
		room.getExits().add(exit);

		// tell us that we succeeded in creating the exit
		send("You open an exit called " + exit.getName() + "(#" + exit.getDBRef() + ")" + " from #" + exit.getLocation() + " to #" + exit.getDestination() + ".", client);
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
	
	/**
	 * Command: fail
	 * 
	 * FOR BUILDERS
	 * 
	 * @param arg
	 * @param client
	 */
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
	
	/**
	 * Command: page
	 * 
	 * Function to send player messages
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_page(final String arg, final Client client)
	{
		// ARG: <recipients>=<message>/nathan,admin=test message
		String[] in = arg.split("=");

		if (in.length > 1) {
			final String[] recipients = in[0].split(",");
			String ms = "";

			if (in.length == 2) {
				ms = in[1];

				send("You page, " + "\"" + Utils.trim(ms) + "\" to " + in[0] + ".", client);

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
	 * Command: party
	 * 
	 * handles party management
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_party(final String arg, final Client client) {
		// party: list the members of your party (if you are in one)
		// party #create: create a new party with you as the leader (if you're not in a party)
		// party #delete: delete an existing party that you are the leader of
		// party #invite <player name> ...: invite a player or players to the party
		// party #join: join the party you were most recently invited to
		// party #kick <player name> ...: kick a player out of the party
		// party #leave: leave your current party

		Player player = getPlayer(client);
		Party party = getPartyContainingPlayer( player );

		if( arg.equals("") ) {
			if( party != null ) {
				send("- Party -------- ", client);

				for( Player player1 : party.getPlayers() ) {
					String extra = "";

					if( party.isLeader(player1) ) { extra = "L"; }

					// refresh player state info
					player1.updateCurrentState();

					// indicate whether we're alive or not
					switch( player.getState() ) {
					case ALIVE:
						send( colors(player1.getName(), "green") + " " + extra, client);
						//client.write(colors("ALIVE", "green") + '\n');
						break;
					case INCAPACITATED:
						send( colors(player1.getName(), "yellow") + " " + extra, client);
						//client.write(colors("INCAPACITATED", "yellow") + '\n');
						break;
					case DEAD:
						send( colors(player1.getName(), "red") + " " + extra, client);
						//client.write(colors("DEAD", "red") + '\n');
						break;
					default:
						break;
					}
				}
			}
			else {
				send("You are not in a party.", client);
			}
		}
		else if( arg.charAt(0) == '#' ) {
			String arg1;
			String[] params = null;

			if( arg.indexOf(' ') == -1 ) { arg1 = arg.substring(1); }
			else {
				arg1 = arg.substring(1, arg.indexOf(' ') );
				params = arg.substring(arg.indexOf(' ')).split(" ");
			}

			debug(arg1);

			if( arg1.equalsIgnoreCase("create") ) {
				// create a new party where you are the leader
				if( party != null) {
					party.removePlayer( player );
				}
				parties.add( new Party( player ) );
			}
			else if( arg1.equalsIgnoreCase("delete") ) {
				// deletes the party if you have one and are the leader
				if( party != null && party.isLeader(player) ) {
					parties.remove(party);
				}
			}
			else if( arg1.equalsIgnoreCase("invite") ) {
				// send an invite to join a party to one or more players (if you are the leader)
				if( party.isLeader(player) ) {
					if( params.length > 0 ) {
						for(String playerName : params) {
							Player player1 = getPlayer( playerName );

							if( player1 != null ) {
								// send invite to named player (overwrites any previous invite)
								partyInvites.put(player1, getPartyContainingPlayer( player ));
								String message = player.getName() + " has invited you to their party!";
								//addMessage( new Message( player, message, player1 ) );
								notify(player1, message);
							}
						}
					}
					else {
						send("No players to invite specified.", client);
					}
				}
				else {
					send("Only the party leader may invite players!", client);
				}
			}
			else if( arg1.equalsIgnoreCase("join") ) {
				// accept a standing invite to join a party
				Party party1 = partyInvites.get(player);

				if( party1 != null ) {
					// need to check if we're in a party and either remove us from that party or require us
					// to leave it before joining another
					if( party != null ) {
						party.removePlayer( player );
					}

					party1.addPlayer(player);
					send("You joined " + party1.getLeader().getName() + "'s party.", client);
					//addMessage( new Message(player, player.getName() + " accepted your party invitation!", party1.getLeader() ) );
					//addMessage( new Message(player, player.getName() + " joined your party.", party1.getLeader() ) );
					notify(party1.getLeader(), player.getName() + " accepted your party invitation!");
					notify(party1.getLeader(), player.getName() + " joined your party.");
				}

				partyInvites.remove(player);
			}
			else if( arg1.equalsIgnoreCase("kick") ) {
				if( party.isLeader(player) ) {
					if( params.length > 0 ) {
						for(String playerName : params) {
							Player player1 = getPlayer( playerName );

							if( player1 != null ) {
								// kick named player
								party.removePlayer( player1 );
								send("You kick " + player1.getName() + " from the party.", client);
								//addMessage( new Message(player, player.getName() + " kicked you from the party.", player1) );
								notify(player1, player.getName() + " kicked you from the party.");
							}
						}
					}
					else {
						send("No players to kick specified.", client);
					}
				}
				else {
					send("Only the party leader may kick party members!", client);
					//notfiy(party.getLeader(), player.getName() + " tried to kick " + player1.getName() + "!");
				}
			}
			else if( arg1.equalsIgnoreCase("leave") ) {
				// leave your current party, if you're in one
				if( party != null ) { 
					Player leader = party.getLeader();
					party.removePlayer( player );
					send("You left " + leader.getName() + "'s party.", client);
					addMessage( new Message(player, player.getName() + " left your party.", leader) );
				}
			}
		}
	}

	/**
	 * Command: passwd
	 * 
	 * Allows the player to change their password themselves.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_passwd(final String arg, final Client client)
	{
		// Syntax: passwd test / change your password to test (user)
		Player player = getPlayer(client); // get the current player

		player.setPass(arg);
		
		send("Your password has been changed to: '" + arg + "'", client);
		send("CAUTION: Exercise care in remembering this password, as the admins cannot do anything for you if you forget it.", client);
	}
	
	/**
	 * Command: pconfig
	 * 
	 * Configure player options
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_pconfig(final String arg, final Client client) {
		Player player = getPlayer(client);

		String[] args = arg.split("=");
		
		if( !arg.equals("") ) {
			if( arg.charAt(0) == '#' ) {
				String param = arg.substring(1);

				if( param.equals("options") || param.equals("opt") ) {
					for(Entry<String, Boolean> e : player.getConfig().entrySet()) {
						send(e.getKey() + ": " + e.getValue(), client);
					}
				}
				else if( param.equals("help") || param.equals("h") ) {
					String[] args1 = arg.split(" ");
					
					if( args1.length == 2 ) {
						switch(args1[1]) {
						case "global-nameref-table": send("Use the global nameref table for storing your name references.", client);     break;
						case "pinfo-brief":          send("Show the short form of the output of the pinfo command by default.", client); break;
						case "prompt_enabled":       break;
						case "msp_enabled":          break;
						case "complex-inventory":    break;
						case "pager_enabled":        break;
						case "show-weather":         break;
						case "tagged-chat":          break;
						case "compact-editor":       break;
						case "hud_enabled":          break;
						case "notfiy_newmail":       break;
						default:                     break;
						}
					}
				}
			}
			else {
				if( args.length > 1 ) {
					if( !player.getConfig().containsKey( args[0] ) ) {
						send("No such config option! (see 'pconf #opt')", client);
						return; 
					}

					switch( args[1] ) {
					case "true":
						player.setConfigOption(args[0], true);
						send("set " + args[0] + " to TRUE.", client);
						break;
					case "false":
						player.setConfigOption(args[0], false);
						send("set " + args[0] + " to FALSE.", client);
						break;
					default:
						send("Invalid config setting! (use 'true' or 'false')", client);
						break;
					}
				}
				else {
					send(args[0] + " = " + getPlayer(client).getConfig().get(args[0]), client);
				}
			}
		}
		else {
			send("No such config option! (see 'pconf #opt')", client);
		}
	}
	
	/**
	 * Command: pinfo
	 * 
	 * Prints out player info
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_pinfo(final String arg, final Client client) {
		final Player player = getPlayer(client);

		send("------------------------------[ Sheet ]------------------------------", client);
		send("Character Name: " + Utils.padRight(player.getName(), 16) + " Player Name: " + Utils.padRight("", 8), client);
		send("Race: " + player.getRace().getName(), client);
		send("Class: " + player.getPClass().getName(), client);
		send("Level: " + player.getLevel(), client);

		send("", client);

		if ( player.isLevelUp() ) {
			//client.write(Colors.GREEN + "Ready to Level-Up!" + Colors.WHITE + '\n');
			send(colors("Ready to Level-Up!", "green"), client);
			send("", client);
		}

		send("XP: " + Utils.padRight("" + player.getXP(), 7) + " XP to next Level: " + Utils.padRight("" + (player.getXPToLevel() - player.getXP()), 7), client);

		send("", client);

		/*Ability[] abilities = new Ability[] {
				Abilities.STRENGTH,
				Abilities.DEXTERITY,
				Abilities.CONSTITUTION,
				Abilities.INTELLIGENCE,
				Abilities.CHARISMA,
				Abilities.WISDOM
		};*/
		
		// TODO testing
		Ability[] abilities = rules.getAbilities();

		/*send(Utils.padRight("Strength: ", ' ', 14) + player.getAbility(Abilities.STRENGTH), client);*/

		/*send(Utils.padRight(abilities[0].getName() + ": ", ' ', 14) + abStrings[0], client);*/
		
		String abilityString = "";
		
		for( Ability ability : abilities ) {
			int ab = player.getAbility(ability);     // get base ability stat
			int abm = player.getAbilityMod(ability); // get ability stat modifier
			
			abilityString = (ab > ab - abm) ? colors("" + ab, "green") : (ab < ab - abm) ? colors("" + ab, "red") : "" + ab;
			
			send(Utils.padRight(ability.getName() + ": ", ' ', 14) + abilityString, client);
		}

		send("", client);

		send("AC: " + player.getAC(), client);

		// Brew Potion (0) Craft Magic Arms And Armor (1) Craft Rod  (2) Craft Staff  (3)
		// Craft Wand  (4) Craft Wondrous Item        (5) Forge Ring (6) Scribe Scroll(7)
		String[] item_creation_feats = { "Brew Potion", "Craft Magic Arms and Armor", "Craft Rod",
				"Craft Staff", "Craft Wand", "Craft Wondrous Item", "Forge Ring", "Scribe Scroll" };
		send("Item Creation Feats", client);
		for(int i = 0; i < 8; i++) {
			send(item_creation_feats[i] + ": " + (player.item_creation_feats.get(i) ? "Yes" : "No"), client);
		}

		if( player.getConfig().get("pinfo-brief") == false || arg.equals("#skills")  ) {

			int si = 1;      // skill index
			int columns = 2; // how many columns wide the skill printout should be

			StringBuilder row = new StringBuilder();

			// This outputs skill info in a varying number of columns
			// CHANGE:
			// if possible make this configurable by the end user
			// let them decided how many columns and how they are sorted
			// ie 1,2,3 down then 4,5,6 or 1,2,3 across then 4,5,6 across
			// 1, 4, 7 or 1, 2, 3
			// 2, 5, 8    4, 5, 6
			// 3. 6, 9    7, 8, 9

			send("------------------------------[ Skills ]------------------------------", client);
			for (final Skill s : player.getSkills().keySet()) {

				String skill = s.toString();
				Integer value = player.getSkills().get(s);
				Integer mod = player.getSkillMod(s);

				String color = "";
				String output = "";

				if (value == -1)     color = "red";
				else if (value == 0) color = "yellow";
				else if (value > 0)  color = "green";

				//output = Utils.padRight( colors(skill, color), 35 ) + " : " + Utils.padLeft("" + value, ' ', 2) + " (" + mod + ")";
				output = colors( Utils.padRight(skill, ' ', 18), color) + " : " + Utils.padLeft("" + value, ' ', 2) + " (" + mod + ")";

				if (si > columns) {
					client.write(row.toString() + '\n');
					row.delete(0, row.length());
					si = 1;
				}

				row.append( Utils.padRight( output, 46 ) );

				si++;
			}
			client.write('\n');
		}
	}
	
	/**
	 * Command: put
	 * 
	 * Syntax:
	 * put <x> in <y>
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_put(final String arg, final Client client) {
		Player player = getPlayer(client);
		Room room = getRoom(player);

		List<MUDObject> foundObjects = findVisibleObjects(room);

		// split the arguments into a string array by space characters and make a list
		final List<String> args = Arrays.asList(arg.split(" "));
		//final List<String> args1 = Utils.mkList(arg.split(" "));
		// tell us how many elements the array has (debug)
		debug(args.size());
		
		// check for "in" and return if we don't find it
		int index = -1;
		for(final String s : args) if( s.equals("in") ) index = args.indexOf(s);
		if( index == -1 ) return;

		Container container = null;
		Item item = null;
		String param = null;
		
		// look in the player's inventory
		for(Item item1 : player.getInventory()) {
			param = Utils.join(args.subList(0, index), " ");
			if(item1.getName().equalsIgnoreCase(param)) {
				item = item1;
			}
		}

		for(Item item1 : player.getInventory()) {
			param = Utils.join(args.subList(index + 1, args.size()), "");
			if(item1.getName().equalsIgnoreCase(param) && item1 instanceof Container) {
				container = (Container) item1;
			}
		}

		if( container != null && item != null ) {
			System.out.println("Item: " + item.getName());
			System.out.println("Container: " + container.getName());

			container.insert(item);
			player.getInventory().remove(item);
			item.setLocation(container.getDBRef());
			System.out.println(container.getContents());

			send("You put " + colors(item.getName(), "yellow") + " in " + colors(container.getName(), "yellow"), client);
		}
	}
	
	/**
	 * qedit - quest editor
	 * 
	 * qedit <quest_name>, qedit <quest_id>, qedit
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_questedit(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT);
		player.setEditor(Editors.QUEST);

		EditorData newEDD = new EditorData();

		Quest quest = null;
		boolean exist = false;

		if ( arg.equals("new") ) {
			quest = new Quest("New Quest", "A new, blank quest.");

			// add new quest to global table
			//quests.add( quest );

			exist = true;
		}
		else {
			try {
				int id = Utils.toInt(arg, -1);
				quest = getQuest(id);
			}
			catch(NumberFormatException nfe) { // no item with that dbref, cannot edit (abort)
				System.out.println("--- Stack Trace ---");
				nfe.printStackTrace();

				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Quest Editor - Unexpected error caused abort (number format exception)", client);
			}
			catch(NullPointerException npe) {
				System.out.println("--- Stack Trace ---");
				npe.printStackTrace();
				
				// null quest, cannot edit (abort)
				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Quest Editor - Unexpected error caused abort (null pointer exception)", client);
			}

			if ( quest != null ) {
				exist = true;
			}
		}

		// quest exists
		if (exist) {
			if( quest.Edit_Ok ) {
				quest.Edit_Ok = false;
			}
			else { // quest is not editable, exit the editor
				abortEditor("Game> Quest Editor - Error: quest not editable (!Edit_Ok)", old_status, client);
				return;
			}

			// record prior player status
			newEDD.addObject("pstatus", old_status);

			// add quest and it's constituent parts to the editor data
			newEDD.addObject("quest", quest);
			newEDD.addObject("name", quest.getName());
			newEDD.addObject("location", -1);
			newEDD.addObject("desc", "");

			player.setEditorData(newEDD);
		}
		else { // quest doesn't exist (abort)
			// reset player, and clear edit flag and editor setting
			player.setStatus(old_status);
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			send("Game> Quest Editor - Error: quest does not exist", client);

			return;
		}

		// show the current state of the quest
		op_questedit("show", client);
	}
	
	/**
	 * Command: quests
	 * 
	 * Handles quest management for the player. Allows you to
	 * abandon, ignore quests and share them with other players.
	 * Also, will allow you to list your current quests
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_quests(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if( !arg.equals("") ) {
			String[] args = arg.split(" ");
			
			if( arg.charAt(0) == '#' ) {
				String param = args[0];
				
				Quest quest = null;
				
				if( arg.indexOf(" ") != -1 ) {
					param = arg.substring(1, arg.indexOf(" "));
				}
				else {
					param = arg.substring(1);
				}

				if( args.length == 2 ) {
					int qId = Utils.toInt(args[1], -1);

					if( qId != -1 ) {
						quest = player.getQuests().get(qId);
					}

					/*if( quest == null ) {
						send("No such quest!", client);
						return;
					}*/

					if( quest != null ) {
						if( param.equals("abandon") || param.equals("a") ) {
							send("You abandon " + quest.getName() + ".", client);
							player.getQuests().remove(qId);
						}
						else if( param.equals("ignore") ) {
							send("You ignore " + colors(quest.getName(), getDisplayColor("quest")) + ".", client);
							quest.setIgnore( true );
						}
						else if( param.equals("info") ) {
							// will be a little like examine, just here to show changes
							send(Utils.padRight("", '-', 80), client);
							//send("----------------------------------------------------", client);
							send("Quest ID#: " + quest.getId(), client);
							send("Name: " + quest.getName(), client);
							send("Location: " + quest.getLocation(), client);
							send("Description: ", client);
							showDesc(quest.getDescription(), 80, client);
							//send("----------------------------------------------------", client);
							send(Utils.padRight("", '-', 80), client);
							int i = 0;
							for(Task t : quest.getTasks()) {
								send(" " + i + ") " + t.getDescription(), client);
								i++;
							}
							send(Utils.padRight("", '-', 80), client);
						}
						else if( param.equals("remember") ) {
							send("You remember " + colors(quest.getName(), getDisplayColor("quest")) + ".", client);
							quest.setIgnore( false );
						}
					}
				}
				else if( args.length == 3 ) {
					Player player1 = getPlayer(args[1]);
					int qId = Utils.toInt(args[2], -1);
					
					if( param.equals("share") || param.equals("s") ) {
						if( qId != -1 ) {
							quest = player.getQuests().get(qId);
							// no way here for the player to deny a share...
							player1.getQuests().add( quest.clone() );
							addMessage( new Message(player, player.getName() + " shared " + quest.getName() + " with you.", player1) );
						}
					}
				}
				else {
					if( ( param.equals("list") || param.equals("l") ) && player.getAccess() >= Constants.BUILD ) {
						int index = 0;

						for( Quest q : quests ) {
							System.out.println( index );
							send( q.getId() + ": " + q.getName() + " ( " + q.getLocation().getName() + " )", client );
							index++;
						}
					}
				}
			}

			return;
		}

		int index = 0;

		send("Quests", client);
		send("================================================================================", client);
		for (Quest quest : player.getQuests()) {
			if ( !quest.isComplete() && !quest.isIgnored() ) {
				client.write(index + ") ");
				client.write(Colors.YELLOW + "   o " + quest.getName());
				client.writeln(Colors.MAGENTA + " ( " + quest.getLocation().getName() + " ) " + Colors.CYAN);
				client.write('\n');
				client.write("" + Colors.YELLOW);
				showDesc(quest.getDescription(), client);
				client.write("" + Colors.CYAN);
				for (Task task : quest.getTasks()) {
					if ( task.isComplete() ) {
						// should be greyed out if task is complete
						client.write(Colors.GREEN + "      o " + task.getDescription());
						if ( task.getType().equals(TaskType.KILL) ) {
							client.write(" [ " + task.kills + " / " + task.toKill + " ]");
						}
					}
					else {
						client.write(Colors.CYAN + "      o " + task.getDescription());
						if ( task.getType().equals(TaskType.KILL) ) {
							client.write(" [ " + task.kills + " / " + task.toKill + " ]");
						}
					}
					client.write(Colors.MAGENTA + " ( " + task.getLocation().getName() + " ) " + Colors.CYAN);
					client.write('\n');
				}
			}
			else {
			}

			index++;
		}
		client.write("" + Colors.WHITE);
		send("================================================================================", client);
		send("* ignored quests are not show here", client);
	}

	// Function to disconnect player
	private void cmd_quit(final String arg, final Client client) {
		init_disconn(client);
	}
	
	private void cmd_ride(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player );
		
		List<Creature> creatures = objectDB.getCreaturesByRoom( room.getLocation() );
		
		for(final Creature c : creatures) {
			if( c.getName().equals(arg) ) {
				if( c instanceof Ridable ) {
					Ridable r = (Ridable) c;
					
					if( r.isLargeEnough(null) ) {
						mount( r, player );
					}
				}
			}
		}
	}

	// Object/Room Recycling Function
	/**
	 * Command: recycle
	 * 
	 * Object/Room Recycling Function
	 * 
	 * ADMIN?
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_recycle(final String arg, final Client client) {
		final Player player = getPlayer(client);

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
			object = getObject(arg);
		}

		if (object != null && !(object instanceof Player)) { // if we got an object and we have a valid player

			boolean success = false;

			String name = object.getName();
			int num = object.getDBRef();

			if (object instanceof Thing) {
				Thing thing = (Thing) object;
				Room room = getRoom(thing.getLocation());

				// remove thing from room
				room.removeThing(thing);     // remove the thing from the room
				objectDB.removeThing(thing); // recycle the thing (remove from database -- only partial)

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
				
				objectDB.remove( object ); // clear the database entry (object)

				send(msg, client);
			}
		}
	}

	
	
	private void cmd_read(final String arg, final Client client) {
		// get the specified item
		final List<Item> items = objectDB.getItemsByLoc( getPlayer(client).getLocation() );
		
		Item item = null;
		
		for(final Item i : items) {
			debug("Item Name: " + i.getName());
			debug("Item Type: " + i.getItemType().toString());
			
			if( i.getName().equals(arg) ) {
				item = i;
				break;
			}
		}
		
		if( item != null ) {
			// check to see if it's a book (or readable?)
			if( item.getItemType() == ItemType.BOOK ) {
				final Book book = (Book) item;

				if( book.getSize() > 0 ) {
					// show us what we're looking at
					final List<String> page = book.getPage( book.getPageNum() );

					if( page.size() > 0 ) {
						client.write(page);	
					}
					else if( page.size() == 0 ) {
						client.writeln("The page is blank...");
					}
				}
			}
		}
		else {
			send("No such book.", client); return;
		}
	}
	
	/**
	 * Register an account for the game
	 * 
	 * Not sure if this will be interactive or simply have
	 * a name/password parameter set.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_register(final String arg, final Client client) {
		// TODO make this interactive instead of requiring a name and password up front
		// TODO set client state to handle interactive input somehow
		System.out.println("arg: " + arg);
		
		String[] args = arg.split(" ");
		
		if( args.length == 2 ) {
			System.out.println("args[0]: " + args[0]);
			System.out.println("args[1]: " + args[1]);
			
			String username = args[0];
			String password = args[1];
			
			//Account account = new Account(acctMgr.nextId(), username, password, 3);
			
			acctMgr.addAccount( username, password, 3 );
			//caTable.put(client, account);
			
			Account account = acctMgr.getAccount(username, password);
			
			caTable.put(client, account);
			
			send("Account Registered!", client);
			send("Username: \'" + account.getUsername() + "\'", client);
			send("Password: \'" + account.getPassword() + "\'", client);
			
			// I'd really like this to log the account in and present it with the account menu
			//setClientState(client, "account_menu");
			
			send("You may now login using your account credentials as you would those for an ordinary player.", client);
		}
		else {
			send("register: Invalid data!", client);
		}
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

		player.setStatus(Constants.ST_EDIT); // set the 'edit' status flag
		player.setEditor(Editors.ROOM);      // room editor

		EditorData newEDD = new EditorData();

		Room room = null;
		boolean exist = false;

		// get a room
		if ( arg.equals("") || arg.toLowerCase().equals("here") ) { // edit current room
			room = getRoom(player.getLocation());

			exist = true;
		}
		else if ( arg.equals("new") ) { // create new room if no room to edit specified
			room = createRoom("New Room", 0);

			// add new room to database
			objectDB.addAsNew(room);
			objectDB.addRoom(room);

			// do not use, yet
			//room.setDBRef(objectDB.peekNextId());
			//objectDB.reserveID(); // hold onto the id just in case we decide to keep the room, but don't let anyone else use it

			exist = true;
		}
		else {
			final int dbref = Utils.toInt(arg, -1);

			if (dbref != -1) {
				room = getRoom(dbref); // get room by dbref (Integer)
			}
			else {
				room = getRoom(arg); // get room by name (String)
			}

			if( room != null ) {
				exist = true;
			}
		}

		if( exist ) {
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
			newEDD.addObject("name", room.getName());
			newEDD.addObject("flags", EnumSet.copyOf( room.getFlags() ));
			newEDD.addObject("desc", room.getDesc());
			newEDD.addObject("location", room.getLocation());
			newEDD.addObject("zone", room.getZone());
			newEDD.addObject("x", room.x);
			newEDD.addObject("y", room.y);
			newEDD.addObject("z", room.z);
			for(Exit exit : room.getExits()) {
				newEDD.addObject("e|" + exit.getName(), exit);
			}
			for(Item item : room.getItems()) {
				newEDD.addObject("i|" + item.getName(), item);
			}

			player.setEditorData(newEDD);

			op_roomedit("show", client); // print out the info page
		}
		else send("No such room!", client);
	}

	// 'say' function
	private void cmd_say(final String arg, final Client client)
	{
		send("You say, \"" + arg + "\"", client);
		Message msg = new Message(getPlayer(client), arg);
		addMessage(msg);
	}
	
	/**
	 * Set properties on objects.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_set(final String arg, final Client client) {
		final Player player = getPlayer(client);

		MUDObject mobj;

		// here=header:======================
		// here,head:=================
		String[] args = arg.split("=", 2);

		if (args.length > 1) { // if there is a property to be set or removed
			String target = args[0];

			// head:================= -> head,=================
			final String[] temp = Utils.trim(args[1]).split(":", 2);

			if(temp.length == 2) {
				String key = temp[0];
				String value = temp[1];

				// get the object
				if (target.toLowerCase().equals("me")) mobj = player;
				else if (target.toLowerCase().equals("here")) mobj = getRoom(player);
				else mobj = objectDB.getByName(target);

				// if the object isn't null, modify it's properties
				if( mobj != null ) {
					if( !value.equals("") ) {
						mobj.setProperty(key, value);
						send("Property \'" + Utils.trim(key) + "\' with value of \'" + Utils.trim(value) + "\' set on " + mobj.getName(), client);
					}
					else {
						mobj.getProperties().remove(key);
						send("Property \'" + Utils.trim(key) + "\' removed from " + mobj.getName(), client);
					}
				}
			}
		}
	}

	private void cmd_setmode(final String arg, final Client client) {
		if( arg == null ) {
			mode = GameMode.NORMAL;
		}
		else {
			final char m = arg.toLowerCase().charAt(0);

			if (!GameMode.isValidString(m)) {
				mode = GameMode.NORMAL;
				send("Invalid GameMode, using Normal instead.", client);
			}
			else mode = GameMode.fromChar(m);
		}

		send("Game> setting GameMode to -" + mode + "-", client);
	}
	
	private void cmd_sethour(final String arg, final Client client) {
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
			System.out.println("--- Stack Trace ---");
			nfe.printStackTrace();
			send("Game> Invalid hour", client);
		}
	}

	private void cmd_setminute(final String arg, final Client client) {
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
			System.out.println("--- Stack Trace ---");
			nfe.printStackTrace();
			send("Game> Invalid minute", client);
		}
	}
	
	private void cmd_setlevel(final String arg, final Client client) {
		Player player = getPlayer(client);

		if (!arg.equals("")) {
			try {
				debug("ARG: " + arg);

				final int changeLevel = Integer.parseInt(arg);

				debug("INTERPRETED VALUE: " + changeLevel);
				debug("SIGN: " + Integer.signum(changeLevel));

				final int newLevel = player.getLevel() + changeLevel;

				if( newLevel <= max_levels) {
					player.changeLevelBy(changeLevel);
					send("Game> Gave " + player.getName() + " " + changeLevel + " levels (levels).", client);
				}
				else {
					send("Game> Greater than maximum level used, no changes made.", client);
				}
			}
			catch(NumberFormatException nfe) {
				System.out.println("--- Stack Trace ---");
				nfe.printStackTrace();
			}
		}
		else {
			send("Game> No amount specified, no experience (xp) change has been made.", client);
		}
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

		Player player = getPlayer(client);

		if (args.length > 1) {
			String skillName = args[0];
			Integer skillValue = Integer.parseInt(args[1].replaceAll(" ", ""));

			Skill skill = getSkill(skillName);

			if( skill != null ) {
				System.out.println(skillName);
				System.out.println(skill.toString());

				if (skillValue < Constants.MAX_SKILL) {
					send("Set " + skill.toString() + " skill to " + skillValue, client);
					player.setSkill(skill, skillValue);
				}
				else {
					send("Setting exceeds maximum skill value, change aborted.", client);
				}
			}
			else {
				send("No such skill!", client);
			}
		}
		else {
			String skillName = args[0];

			Skill skill = getSkill(skillName);

			if( skill != null ) {
				send(skill.getName() + " = " + player.getSkill(skill));
			}
		}
	}
	
	// TODO make this work
	private void cmd_setweather(final String arg, final Client client) {
		final Player player = getPlayer(client);
		Zone zone = getRoom( player.getLocation() ).getZone();
		
		List<WeatherState> wsl = season.weatherPattern.weather_states;
		WeatherState w = null;
		
		for(WeatherState ws : wsl ) {
			if( ws.name.equals(arg) ) {
				w = ws;
				break;
			}
		}
		
		if( w == null ) return;
		
		// if the zone exists, change the weather of all the rooms in that zone to
		// be the specified weather
		if( zone != null ) {
		}
		else { // otherwise, change all the weather
			Room room;
			
			for(int id = 0; id < objectDB.peekNextId(); id++ ) {
				if( (room = objectDB.getRoomById(id)) != null ) {
					room.getWeather().ws = w;
				}
			}
		}
	}

	private void cmd_score(final String arg, final Client client) {
		final Player player = getPlayer(client);

		send("You are " + player.getName() + ". " + player.getTitle() + ", level " + player.getLevel(), client);
		send("Race: " + player.getRace().getName() + " Sex: " + player.getGender().toString() + " Class: " + player.getPClass().getName(), client);
		send("Hit points: " + player.getHP() + "(" + player.getTotalHP() + ")", client);
		
		double exp_prog = ((float) player.getXP() / (float) player.getXPToLevel()) * 100;
		send("Experience Progress: " + new DecimalFormat("#0.0").format(exp_prog) + " %", client);
		
		send("Money: " + player.getMoney().toString() + ".", client);

		// Toril Mud 'score' output below
		/*Level: 2   Race: Human   Class: Warrior     
		Hit points: 48(48)  Moves: 160(160)
		Experience Progress: 4 % 
		Coins carried:    0 platinum     8 gold     3 silver   158 copper
		Coins in bank:    0 platinum    10 gold     0 silver     0 copper
		Prestige: 760   Acheron Kill Count: 0
		Outcast from: Leuthilspar
		Playing time: 0 days / 11 hours / 15 minutes
		Title: 
		Status:  Standing.*/

		/*send("Level: " + Utils.padRight("" + player.getLevel(), ' ', (max_levels / 9)) + " Race: " + player.getPRace().getName() + " Class: " + player.getPClass().getName(), client);
		send("Hit points: " + player.getHP() + "(" + player.getTotalHP() + ")", client);
		double exp_prog = ((float) player.getXP() / (float) player.getXPToLevel()) * 100;
		System.out.println(exp_prog);
		send("Experience Progress: " + exp_prog + " %", client); */
	}
	
	/**
	 * Command: sell
	 * 
	 * Syntax:
	 * sell <item>
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_sell(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if (player.getStatus().equals(Constants.ST_INTERACT)) {
			if (player.getTarget() instanceof NPC) {
				NPC npc = (NPC) player.getTarget();

				//npc.interact(0);
			}
		}

		/*Player player = getPlayer(client);

		if (player.getStatus().equals(Constants.ST_INTERACT)) { // interact mode
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

	private void cmd_session(final String arg, final Client client) {
		Player p = getPlayer(arg);
		if (p != null) {
			Session s = sessionMap.get(p);
			send("Connected: " + s.connected, client);
			send("Connect Time: " + s.connect, client);
			send("Connected for: ", client);
			//send("Disconnect Time: " + s.disconnect, client);
			send("Player: " + s.getPlayer().getName(), client);
			send("Client (IP): " + s.getClient().getIPAddress(), client);
		}
		else {
			// retrieve last session for player
			// Session s = loadSessionData( player );
		}
	}

	private void cmd_skilledit(final String arg, final Client client) {
		Player player = getPlayer(client);
		String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT); // set the 'edit' status flag
		player.setEditor(Editors.SKILL);     // skill editor

		EditorData newEDD = new EditorData();

		Skill skill = null;

		if( arg.equals("new") ) {
			skill = new Skill();
		}
		else {
			// get skill
			skill = getSkill(arg);
		}

		if( skill != null ) {
			// record prior player status
			newEDD.addObject("pstatus", old_status);

			//
			newEDD.addObject("skill", skill);
			newEDD.addObject("name", skill.getName());
			newEDD.addObject("stat", skill.getAbility());
			newEDD.addObject("abbrev", "");
			newEDD.addObject("classes", skill.getClasses());

			//
			player.setEditorData(newEDD);

			//newEDD
			op_skilledit("show", client);
		}
		else {
			abortEditor("no such skill", old_status, client);
		}
	}
	
	private void cmd_spellinfo(final String arg, final Client client) {
		final Spell spell = getSpell(arg);
		send("Name:  " + spell.getName());
		send("Level: " + spell.getLevel());

		send("Targets: " + Spell.decodeTargets(spell), client);
	}

	private void cmd_spells(final String arg, final Client client) {
		Player player = getPlayer(client);
		client.write("Spell List\n");
		client.write("-----------------------------------------------------------------\n");
		if ( arg.equals("#all") && player.getAccess() >= Constants.WIZARD) {
			// list all the spells, by level?
			for (final Spell spell : this.spells2.values()) {
				client.write(spell.getSchool().toString() + " " + spell.toString() + "\n");
			}
		}
		else {
			// list your spells, by level?
			for(int l = 0; l < player.getLevel(); l++) {
				List<Spell> spells = player.getSpellBook().getSpells(l);
				client.write("Level " + l + "\n");
				client.write("--------\n");
				for(Spell spell : spells) {
					client.write(spell.getName() + "\n");
				}

			}
		}
		client.write("-----------------------------------------------------------------\n");
		System.out.println(spells2.entrySet());
	}

	private void cmd_staff(final String arg, final Client client) {
		send("Staff", client);
		send("----------------------------------------", client);
		for(Player p : players) {
			final String playerName = Utils.padRight( Utils.truncate(p.getName(), 16), 16);

			switch(p.getAccess()) {
			case Constants.GOD:    send(playerName + "[GOD]", client); break;
			case Constants.WIZARD: send(playerName + "[WIZARD]", client); break;
			case Constants.ADMIN:  send(playerName + "[ADMIN]", client); break;
			case Constants.BUILD:  send(playerName + "[BUILD]", client); break;
			default: break;
			}
		}
	}

	private void cmd_success(final String arg, final Client client) {
		final String[] args = arg.split("=");
		//List<Exit> exits = objectDB.getExitsByRoom(getPlayer(client).getLocation());
		final Exit exit = getExit(args[0]);

		if (args.length > 1) {
			debug(exit.getName() + "(" + exit.getDBRef() + ")");
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
		//sys_reload(); //
		running = true;
		//loop();
	}

	// Server Shutdown function
	private void cmd_shutdown(final String arg, final Client client)
	{
		// shutdown
		// shutdown now
		// shutdown -h 5

		if( arg.equals("now") ) {
			debug("SHUTDOWN TYPE: IMMEDIATE");
			s.write("Server going down immediately.");

			shutdown();
		}
		else {
			String[] args = arg.split(" ");

			if( args.length > 0 ) {			
				if( args.length == 1 ) {
					if( arg.equals("now") )
					{
						debug("SHUTDOWN TYPE: IMMEDIATE");
						s.write("Server going down immediately.");

						shutdown();
					}
				}
				// if the type of shutdown is null and some kind of time was specified
				else if( args.length == 2 ) {
					if ( args[0].equals("-h") )
					{
						debug("SHUTDOWN TYPE: TIMED");
						//s.write("Server going down for reboot in " + secs / 60 + "m" + secs % 60 + "s");
						s.write("Server going down for reboot in " + args[1] + "s");

						shutdown( Utils.toInt(args[1], 300) );
					}
				}
			}
			else {
				debug("SHUTDOWN TYPE: NORMAL");
				// Tell people the server is going down, so they know what happened when they get kicked off.
				s.write("Server going down for reboot in 5 minutes.");

				shutdown(300); // 5 min. = 300 secs
			}
		}
	}

	// Function to list stats
	private void cmd_stats(final String arg, final Client client)
	{
		System.out.println(objectDB.getSize());

		final int[] counts = objectDB.getFlagCounts(new String[]{ "P", "N", "E", "R", "I", "T" });
		final int usersCount = counts[0];
		final int npcsCount = counts[1];
		final int exitsCount = counts[2];
		final int roomsCount = counts[3];
		final int itemsCount = counts[4];
		final int thingsCount = counts[5];

		int total = usersCount + npcsCount + exitsCount + roomsCount + itemsCount + thingsCount;

		send(serverName + " Statistics", client);
		send("----------------------", client);
		send(String.format("Players: %-4s %5.2f%%", usersCount,  usersCount  * 100.0 / total), client);
		send(String.format("NPCS:    %-4s %5.2f%%", npcsCount,   npcsCount   * 100.0 / total), client);
		send(String.format("Exits:   %-4s %5.2f%%", exitsCount,  exitsCount  * 100.0 / total), client);
		send(String.format("Rooms:   %-4s %5.2f%%", roomsCount,  roomsCount  * 100.0 / total), client);
		send(String.format("Items:   %-4s %5.2f%%", itemsCount,  itemsCount  * 100.0 / total), client);
		send(String.format("Things:  %-4s %5.2f%%", thingsCount, thingsCount * 100.0 / total), client);
		send("Total:   " + total, client);
		send("----------------------", client);
	}
	
	// output format borrowed from TorilMUd status command
	private void cmd_status(final String arg, final Client client) {
		send("Effects", client);
		send(Utils.padRight("", '-', 75), client);
		
		int index = 1;
		StringBuilder sb = new StringBuilder();
		
		for(final Effect effect : getPlayer(client).getEffects()) {
			if( index % 3 == 0 ) {
				send(sb.toString(), client);
				sb.delete(0, sb.length());
			}
			else {
				sb.append( Utils.padRight(effect.getName(), ' ', 25) );
			}
			
			index++;
		}
		
		if( sb.length() != 0 ) {
			send(sb.toString(), client);
			sb.delete(0, sb.length());
		}
		
		send("", client);
		
		send("Spells", client);
		send(Utils.padRight("", '-', 75), client);
	}
	
	private void cmd_talk(final String arg, final Client client) {
		// TODO improve talk command
		Player player = getPlayer(client);
		NPC npc = getNPC(arg);
		
		// player should be not null, but...
		if( npc != null ) {
			// send npc greeting
			//npc.greet( getPlayer(client) );
			
			send(colors(npc.getName(), getDisplayColor("npc")) + ": " + npc.greeting, client);
			
			// send list of player conversation options
			final List<String> convOpts = Utils.mkList(
					"-- Conversation (" + npc.getName() + ")",
					colors("1) What's up?", "green"),
					colors("2) I'd like to buy something.", "green"),
					colors("3) Bye. (Ends Conversation)", "green")
					);
			send(convOpts, client);
			send("* this is test output", client);
			
			// set player status to conversation (CNVS)
			//player.setStatus("CNVS");
		}
	}

	// Function to take objects in a room
	@SuppressWarnings("unchecked")
	private void cmd_take(final String arg, final Client client)
	{
		// get player, room objects to work with
		final Player player = getPlayer(client);
		final Room room = getRoom(player);

		// split the arguments into a string array by space characters
		final String[] args = arg.split(" ");
		// tell us how many elements the array has (debug)
		debug(args.length);

		// if there is no argument
		if ( arg.equals("") ) {
			send("Syntax: take <item>", client);
		}
		// if there are three arguments, implying the following syntax: TAKE <thing> FROM <container>
		else if ( arg.equalsIgnoreCase("all") ) {
			// all implies stuff on the ground
			// since all the stuff on the ground is in the room, we should evaluate the room to get it's stuff

			// basically we want to evalutate all the items, then take the one with the largest value, one at a time
			// the evaluation scheme needs to take what's usable and what's not as well monetary value into account
			// if we have room for everything, then just take it all
			// - an item is usable if, given restrictions, you meet all requirements (class, race, level, skill)
			ArrayList<Item> items = (ArrayList<Item>) room.getItems();
			//ArrayList<Item> usable = new ArrayList<Item>();

			player.getInventory().addAll( items );
			
			String itemName;
			
			for(Item item : items) {
				itemName = item.getName();
				item.setLocation( player.getDBRef() );
				send("You picked " + colors(itemName, "yellow") + " up off the floor.", client);
			}
			
			room.getItems().clear();
		}
		else { // assuming one argument
			// get the object the argument refers to: by name (if it's in the room), or by dbref#
			// should be done by searching the room's contents for the object and if there is such an object, put it in the player's inventory
			Item item = null;

			for (final Item item1 : room.getItems())
			{
				final int dbref = Utils.toInt(arg, -1);
				
				// if there is a name or dbref match from the argument in the inventory
				// if the item name exactly equals the arguments or the name contains the argument (both case-sensitive), or if the dbref is correct
				
				if( dbref != -1 && item1.getDBRef() == dbref ) {
					item = item1;
					break;
				}
				else {
					final String itemName = item1.getName();
					
					// i.e. for 'long sword' either 'long sword', 'long', or 'sword' should do
					if ( itemName.equalsIgnoreCase(arg) || itemName.toLowerCase().contains(arg.toLowerCase()) ) {
						item = item1;
						break;
					}
					else if( itemName.toLowerCase().startsWith(arg.toLowerCase()) ) {
						item = item1;
						break;
					}
					
					/*String[] nameParts = arg.split(" ");
					else if( nameParts.length > 1 ) {
					}*/
				}
			}
			
			if( item == null ) {
				Thing thing = getThing(arg, room);
				
				if( thing != null ) {
					send("You can't pick that up.", client);
				}
				else {
					send("You can't find that.", client);
					//send(arg + "?", client);
					//send("Did you mean, " + item.getName() + " - " + item.getDBRef(), client);
				}
				
				return;
			}
			
			final String itemName = item.getName();
			
			debug(itemName + " true");
			
			// move object from it's present location to player inventory
			// it would be good to just replace this with a function, since it will need to test for a standard location to put it
			// see if there is a generic storage container to put it in
			if ( hasGenericStorageContainer( player, item ) ) { // if you have a container for this item type, put it there
				/*debug(item.getName() + " container");
					Container<Item> c = getGenericStorageContainer( player, item );
					item.setLocation(c.getDBRef());
					c.add( item );
					send("You picked " + colors(item.getName(), "yellow") + " up off the floor and put it in " + c.getName(), client);*/
			}
			else { // else just stick it in inventory
				debug(itemName + " inventory");

				// if there is an existing, not full stack of that item trying to add these to it
				if (item instanceof Stackable) {
					Stackable<Item> sItem = (Stackable<Item>) item;
					
					List<Item> item_stacks = getItems(itemName, player);
					
					boolean foundItemStack = false;
					
					for(Item item_stack : item_stacks) {
						debug("stackable - have a stack already");
						Stackable sItem1 = (Stackable) getItem(item.getName(), player);
						
						if (sItem1.stackSize() < Constants.MAX_STACK_SIZE) {
							debug("stackable - added to existing stack");
							sItem1.stack(sItem);
							foundItemStack = true;
							break;
						}
						
						debug(player.getInventory().contains(item));
					}
					
					if( !foundItemStack ) {
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

				send("You picked " + colors(itemName, "yellow") + " up off the floor.", client);
			}

			// remove from the room
			room.getItems().remove(item);

			// check for silent flag to see if object's dbref name should be shown as well?
			// return message telling the player that they picked up the object
			// return message telling others that the player picked up the item
			// needs to be placed in the message queue for just the room somehow, not sent to the current player
		}
	}

	private void cmd_target(final String arg, final Client client) {
		Player player = getPlayer(client);
		Room room = getRoom( player );
		MUDObject target = getObject( arg, room );


		// if we currently have a target, tell us what it is
		if (player.getTarget() != null) {
			debug(player.getTarget());
			debug(arg);
		}
		else if( target != null ) {
			debug("Getting target..." + target.getName());

			player.setTarget(target);

			// tell us what we are targetting now
			debug(player.getTarget());
			debug(arg);
			send("Target set to: " + player.getTarget().getName(), client);
		}
		else {
			send("You don't see that.", client);
		}
	}
	
	// tell <player> <message>
	private void cmd_tell(final String arg, final Client client) {
		final Player player;
		final NPC npc;
		
		player = getPlayer(client);
		
		if( player == null ) {
			npc = getNPC(arg);
			
			if( npc == null) {
				return;
			}
			else {
				
			}
		}
		else {
			if( player.getStatus().equals("OOC") ) {
				if( arg.indexOf(" ") != -1 ) {
					final String pName = arg.substring(0, arg.indexOf(" "));
					final String message = arg.substring(arg.indexOf(" "), arg.length());

					final Player player1 = getPlayer(pName);

					final Message msg = new Message(player, message, player1);
					addMessage(msg);
				}
				else send("invalid player", client);
			}
			else send("you cannot use that command while in-character", client);
		}
	}
	
	private void cmd_test(final String arg, final Client client) {
		//requestInput("Name? ", client);
	}
	
	/**
	 * TRADE
	 *  
	 * @param arg
	 * @param client
	 */
	private void cmd_trade(final String arg, final Client client) {
		// trade <player>
		// trade #add <player> -- add something to the trade
		// trade #rem <player> -- remove something from the trade
		// trade #accept <player> -- accept a trade in progress
		// trade #cancel <player> -- cancel an in progress trade
		
		// NOTE: you should only be able to have one trade per other player at a time and it should
		// probably be canceled if they exit the room
		
		final String[] args = arg.split(" ");
		
		Player player = getPlayer(client); // get the current player, the one who executed the command
		
		if( args.length == 1 && !args[0].startsWith("#") ) {
			// try to find the specified player and initiate trade with them
			Player player1 = objectDB.getPlayer(args[0]);

			// if such a player exists and they are in the same room as you
			if( player1 != null ) {
				if( player.getLocation() == player1.getLocation() ) {
					// create new Trade object and store it somewhere
					Trade trade = new Trade(player, player1);

					trades.get(player).put( player1, trade);
				}
				else {
					send("That player is not present", client);
				}

				/*List<Player> players = objectDB.getPlayersByRoom( player.getLocation() );

							if( players.contains( player1 ) ) {
								// create new Trade object and store it somewhere
								Trade trade = new Trade(player, player1);

								//trades.put( player, trade );
							}*/
			}
			else {
				send("No such player.", client);
			}
		}
		else {
			if( args[0].equals("#cancel") ) {
				Player player1 = objectDB.getPlayer(args[0]);
				
				if( player1 != null ) {
					trades.get(player).remove(player1);
					send("You cancel your trade with " + player1.getName(), client);
				}
			}
			else {
			}
		}
	}
	
	/**
	 * TRAVEL
	 * 
	 * travel <landmark -- known location>
	 * 
	 * Note: credit for basic idea behind design to Ryan Hamshire
	 * (http://textgaming.blogspot.com/2011/01/updating-navigation.html)
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_travel(final String arg, final Client client) {
		/**
		 * travel <landmark> - travel to a known landmark
		 * travel stop - pause your travel
		 * travel resume - resume your travel
		 * travel #record <name> - start recording the steps you take as being a route
		 * to a landmark called <name>
		 * travel #end - end a recording
		 */
		String[] args = arg.split(" ");

		Player player = getPlayer(client);

		// get landmarks
		// if no arg, then list our landmarks (within x walking distance?)
		if( arg.isEmpty() ) {
			if( player.landmarks.size() > 0 ) {
				// list landmarks
				send("Landmarks", client);
				send(Utils.padRight("", '-', 79), client);
				for(Landmark landmark : player.landmarks.values()) {
					send(Utils.padRight(landmark.getName(), ' ', 20), client);
					for(String route : landmark.getRoutes()) {
						send(Utils.padRight("", ' ', 25) + route); 
					}
				}
			}
			else {
				send("No recorded landmark routes.", client);
			}
		}
		else if( arg.equals("stop") ) {

		}
		else if( arg.equals("resume") ) {

		}
		else {
			if( args.length >= 1 ) {
				if( args[0].charAt(0) == '#' ) {
					String param = args[0].substring(1);

					if( param.equalsIgnoreCase("record") ) {

					}
					else if( param.equalsIgnoreCase("pause") ) {

					}
					else if( param.equalsIgnoreCase("end") ) {

					}
				}
				else {
					// check to see if we have whatever was suggested
					if( player.landmarks.containsKey( arg.toLowerCase() ) ) {
						String route = null;

						// if so, either calculate a route , or use a pre-specified one
						for(String route1 : player.landmarks.get(arg.toLowerCase()).getRoutes()) {
							String[] t = route1.split(":");
							String[] u = t[1].split(",");

							// if the origin is our current location
							if( player.getLocation() == 0 ) {
								route = route1;
								break;
							}
						}

						if( route != null ); // follow route
						else {
							send("No route found!", client);
							// calculate a route?
						}
					}
					else {
						send("Travel where?", client);
					}
				}
			}
		}
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

						send(item.getName() + " un-equipped (" + item.getEquipType() + ")", client);
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
		Exit exit = getExit(arg);
		
		if( exit != null ) {
			if (exit instanceof Lockable<?>) {
				Lockable<?> l = (Lockable<?>) exit;

				if ( l.isLocked() ) {
					l.unlock();
					send("You unlocked " + exit.getName() + ".", client);
					return;
				}

				send("It's already unlocked.", client);
				return;
			}

			send("It's a good thing no one saw you trying to lock a " + exit.getExitType().toString() + " with no lock.", client);
		}
		else {
			send("Unlock what?", client);
		}
	}
	
	/**
	 * TODO write description
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_use(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		MUDObject m = getObject(arg);

		// FOE
		if( m instanceof mud.foe.Terminal ) {
			term = (mud.foe.Terminal) m;

			if( term.getPowerState() == Terminal.pwr_states.POWER_ON ) {
				term.setInput( client.getInputStream() );
				term.setOutput( client.getOutputStream() );

				player.setStatus("TERM"); // tell the game proper to ignore my input
				
				//term.handle_login();
			}
			else {
				send("The terminal is powered off, perhaps you could 'turn terminal on'?", client);
			}
		}
		else {
			if( useMethods.containsKey(m.getClass()) ) {
				try {
					useMethods.get(m.getClass()).invoke(null);
				}
				catch (IllegalAccessException e) { e.printStackTrace(); }
				catch (IllegalArgumentException e) { e.printStackTrace(); }
				catch (InvocationTargetException e) { e.printStackTrace(); }
			}
			
			// look at the player first
			if (arg.equals("") ) {
				debug("Game> Arguments?");
				
				// Player Equipment
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
			else {
				debug("Game> Arguments Received.");
				
				// Inventory [Item(s)]
				Item item = null;
				
				for(final Item e : player.getInventory()) {
					if( e.getName().equals( arg ) ) {
						item = e;
						break;
					}
				}

				if( item != null ) {
					try {
						if (item instanceof Potion) { use_potion( (Potion) item, client); }      // potion handling
						else if (item instanceof Wand) { use_wand( (Wand) item , client); }      // wand handling
					}
					catch(NullPointerException npe) {
						System.out.println("--- Stack Trace ---");
						npe.printStackTrace();
						System.out.println("Arguments: \'" + arg + "\'");
					}
					
					return;
				}
				
				// Room [Thing(s)]
				final List<Thing> things = objectDB.getThingsForRoom( room.getDBRef() );
				Thing t = null;
				
				for(final Thing e : things) {
					if( e.getName().equals( arg ) )  {
						t = e;
						break;
					}
				}

				if( t != null ) {
					try {
						final String result = pgm.interpret( t.getScript(TriggerType.onUse), player, t );
						if( !result.equals("") ) send( result, client );
					}
					catch(NullPointerException npe) {
						System.out.println("--- Stack Trace ---");
						npe.printStackTrace();
						System.out.println("Arguments: \'" + arg + "\'");
					}
					
					return;
				}
				
				send("No such object.", client);

				// ?
				/*MUDObject m1;
				
				try {
					m1 = getObject(arg);

					System.out.println("MUDObject: " + m1.getName());

					if (m1 instanceof Potion) { use_potion( (Potion) m1, client); }      // potion handling
					else if (m1 instanceof Portal) { use_portal( (Portal) m1, client); } // portal handling
					else if (m1 instanceof Wand) { use_wand( (Wand) m1 , client); }      // wand handling
					else if (m1 instanceof Thing) {
						final String result = pgm.interpret( ((Thing) m1).getScript(TriggerType.onUse), player, m1 );
						
						if( !result.equals("") ) send( result, client );
					}
				}
				catch(NullPointerException npe) {
					System.out.println("--- Stack Trace ---");
					npe.printStackTrace();
					System.out.println("Arguments: \'" + arg + "\'");
					return;
				}*/
			}
		}
	}

	/**
	 * TODO write description
	 * 
	 * @param arg
	 * @param client
	 */
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

	private void cmd_wear(final String arg, final Client client) {
		Player player = getPlayer(client);
		// get the item in question
		List<Item> items = player.getInventory();

		for(Item item : items) {
			if(item instanceof Wearable && item.getName().equals( arg )) {
				player.wear( (Wearable) item );
				return;
			}
		}
	}

	private void cmd_wield(final String arg, final Client client) {}

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
		List<String> output = new LinkedList<String>();
		
		int n = 0;

		//send("Player     Class     S Race      Idle Status Location", client);
		//send("Player     Class     S Race      Idle Location", client);
		output.add("Player     Class     S Race      Idle Location");
		// 10+1+9+1+(1)+1+9+1+4+1+6+1+24 = 69
		//send(Utils.padRight("", '-', 69), client);
		// 10+1+9+1+(1)+1+9+1+4+1+24 = 62
		send(Utils.padRight("", '-', 74), client);
		output.add(Utils.padRight("", '-', 74));
		
		for (Player player : players)
		{
			try {
				String name = player.getName(); // need to limit name to 10 characters
				String cname = player.getCName();
				//String title = player.getTitle(); // need to limit title to 8 characters
				String playerClass = player.getPClass().getName();
				String playerGender = player.getGender().toString();
				String race = player.getRace().toString();
				//String ustatus = player.getStatus(); // need to limit status to 3 characters
				int location = player.getLocation(); // set room # limit to 5 characters (max. 99999)
				String roomName = getRoom(location).getName(); // truncate to 24 characters?
				String locString = "";

				if (player.hasEffect("invisibility")) { locString = "INVISIBLE"; }
				else {
					if ( !getRoom(player.getLocation()).hasFlag(ObjectFlag.DARK) ) {
						locString = roomName + " (#" + location + ")";
					}
					else {
						//Zone zone = getZone( getRoom( location ) );
						Zone zone = getRoom( location ).getZone();
						
						if( zone != null ) {
							locString = zone.getName();
						}
						else { locString = roomName; }
					}
				}

				String idle = player.getIdleString();

				Player current = getPlayer(client);

				if (current.getNames().contains(name) || current.getName().equals(name)) {
					//send(Utils.padRight(name, 10) + " " + Utils.padRight(playerClass, 9) + " " + Utils.padRight(playerGender, 1) + " " + Utils.padRight(race, 9) + " " + Utils.padRight(idle, 4) + " " + locString, client);
					output.add(Utils.padRight(name, 10) + " " + Utils.padRight(playerClass, 9) + " " + Utils.padRight(playerGender, 1) + " " + Utils.padRight(race, 9) + " " + Utils.padRight(idle, 4) + " " + locString);
				}
				else {
					//send(Utils.padRight(cname, 10) + " " + Utils.padRight(playerClass, 9) + " " +  Utils.padRight(playerGender, 1) + " " + Utils.padRight(race, 9) + " " + Utils.padRight(idle, 4) + " " + locString, client);
					output.add(Utils.padRight(cname, 10) + " " + Utils.padRight(playerClass, 9) + " " +  Utils.padRight(playerGender, 1) + " " + Utils.padRight(race, 9) + " " + Utils.padRight(idle, 4) + " " + locString);
				}

				n++;
			}
			catch(NullPointerException npe) {
				System.out.println("--- Stack Trace ---");
				npe.printStackTrace();
			}
		}
		//send(Utils.padRight("", '-', 69), client);
		//send(Utils.padRight("", '-', 74), client);
		output.add(Utils.padRight("", '-', 74));
		//send(n + " players currently online.", client);
		output.add(n + " players currently online.");
		
		client.write( output );
	}

	//Function to list player locations
	private void cmd_who(final String arg, final Client client)
	{
		if( !arg.equals("") && loginCheck(client) &&checkAccess(getPlayer(client), Constants.ADMIN) ) {
			Player player = getPlayer(arg);
			
			if( player != null ) {
				send(player.getName() + " Located: " + player.getLocation(), client);
				return;
			}
			else return;
		}
		
		int n = 0;

		for (final Player player : players)
		{
			try {
				String name = player.getName();                  // need to limit name to 10 characters
				String cname = player.getCName();
				String title = player.getTitle();                // need to limit title to 8 characters
				String race = player.getRace().toString();

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
					sb.append(cname);
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
	// @zones +new [zone name]
	// @zones +add [room name]=[zone name]
	//
	// DEBUG: need to debug this code and make sure there aren't any logical or coding errors
	private void cmd_zones(final String arg, final Client client) {
		final String[] params = arg.split(" ");

		debug("# Params: " + params.length);

		/*if (!arg.equals("") && params.length == 1) {
			if (params[0].equals("+new")) {
				Zone zone = new Zone(params[0], null);
				zones.put(zone, 10); // store a new zone object 
				send("New Zone Established!", client); // tell us that it succeeded.
			}
		}*/
		if (params.length >= 2) {
			String[] args = params[1].split("=");
			System.out.println(params[0]);
			if (params[0].equals("+new")) {
				System.out.println("params: " + params[0] + " " + params[1]);
				Zone zone = new Zone(params[1], null);
				zones.put(zone, 10); // store a new zone object 
				send("New Zone Established!", client); // tell us that it succeeded.
			}
			else if (params[0].equals("+add")) {
				if (args != null) {
					if (args.length > 1) {
						try {
							Room room = getRoom(Utils.toInt(args[0], -1));
							Zone zone = getZone(args[1]);
							zone.addRoom(room);
							room.setZone(zone);
							send(room.getName() + " added to " + zone.getName(), client);
						}
						catch(NumberFormatException nfe) { send(gameError("@zones", ErrorCodes.NOT_A_NUMBER), client); }
						catch(NullPointerException npe) { send("One or more invalid rooms given.", client); }
					}
				}
			}
			else if(params[0].equals("+info")) {
					try {
						// need to fix this so that the portion of this kind of argument '+info Red Dragon Inn' gets
						// zoined back into a single string
						String s = Utils.join( Arrays.copyOfRange(params, 1, params.length), " " );
						Zone zone = getZone( s );
						
						//send("Zone - " + zone.getName() + "(" + zone.getId() + ")", client);
						
						//send("" + zone.getName() + " ( " + zone.getRooms().size() + " Rooms )", client);
						send("" + colors(zone.getName(), "purple") + " ( " + zone.getRooms().size() + " Rooms )", client);
						
						for(final Room room : zone.getRooms()) {
							send("- " + room.getName() + "(#" + room.getDBRef() + ")", client);
						}
						
						send("Quests:", client);
						
						for(final Quest quest : getQuestsByZone(zone)) {
							send(quest.getId() + ": " + quest.getName(), client);
						}
					}
					catch(NullPointerException npe) {
						System.out.println("--- Stack Trace ---");
						npe.printStackTrace();
					}
			}
		}
		else {
			send("Zones:", client);
			debug(zones.entrySet());
			for (Zone zone : zones.keySet()) {
				//send("" + zone.getName() + " ( " + zone.getRooms().size() + " Rooms )", client);
				send("" + colors(zone.getName(), "purple") + " ( " + zone.getRooms().size() + " Rooms )", client);
				for(final Room room : zone.getRooms()) {
					send("- " + room.getName() + "(#" + room.getDBRef() + ")", client);
				}
			}
		}
	}
	
	/**
	 * Command: offer
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_offer(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if (player.getStatus().equals(Constants.ST_INTERACT)) {
			if (player.getTarget() instanceof NPC) {
				NPC npc = (NPC) player.getTarget();

				//npc.interact(0);
			}
		}
	}

	private void cmd_value(final String arg, final Client client) {
	}

	private void cmd_zoneedit(final String arg, final Client client) {
		Player player = getPlayer(client);
		String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT); // set the 'edit' status flag
		player.setEditor(Editors.ZONE);      // zone editor

		EditorData newEDD = new EditorData();

		Zone zone = null;

		if( arg.equals("new") ) {
			zone = new Zone("New Zone", null);
		}
		else {
			if( arg.charAt(0) == '#' ) {
			}
			else {
				zone = getZone(arg);
			}
		}

		if( zone != null ) {
			// record prior player status
			newEDD.addObject("pstatus", old_status);

			// editable zone data
			newEDD.addObject("zone", zone);
			newEDD.addObject("name", zone.getName());

			//
			player.setEditorData(newEDD);

			//newEDD
			op_zoneedit("show", client);
		}
	}
	
	/**
	 * 
	 * List zone information for the current zone
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_zoneinfo(final String arg, final Client client) {
		try {
			Zone zone = getZone( getPlayer(client) );
			// Zone - Test Zone (4)
			//send("Zone - " + zone.getName() + "(" + zone.getId() + ")", client);
			
			// Test Zone ( 10 Rooms )
			//send("" + zone.getName() + " ( " + zone.getRooms().size() + " Rooms )", client);
			
			//send("" + colors(zone.getName(), "purple") + " ( " + zone.getRooms().size() + " Rooms )", client);
			
			final Integer zoneID = zone.getId();
			final String zoneName = zone.getName();
			
			//send(colors(zoneID + " - " + zoneName, "purple") + " ( " + zone.getRooms().size() + " Rooms )", client);
			
			// [ 4 ] Test Zone ( 10 Rooms )
			send(colors("[" + zoneID + "] " + zoneName, "magenta") + " ( " + zone.getRooms().size() + " Rooms )", client);
			
			for(final Room room : zone.getRooms()) {
				send("- " + room.getName() + "(#" + room.getDBRef() + ")", client);
			}
			
			send("Quests:", client);
			
			for(final Quest quest : getQuestsByZone(zone)) {
				send(quest.getId() + ": " + quest.getName(), client);
			}
		}
		catch(NullPointerException npe) {
			System.out.println("--- Stack Trace ---");
			npe.printStackTrace();
		}
		
	}

	// used for help files
	/**
	 * Check
	 * 
	 * Evaluates any scripting in the specified text. Useful
	 * for handling places where you'd like color
	 * 
	 * NOTES: Not particularly robust, used for help files
	 * 
	 * @param in
	 * @return
	 */
	private String check(final String in) {
		boolean doEval = false;

		StringBuilder result = new StringBuilder();
		StringBuilder work = new StringBuilder();

		char ch;

		for( int c = 0; c < in.length(); c++ ) {
			ch = in.charAt(c);

			switch(ch) {
			case '{':
				if( !doEval ) {
					doEval = true;
					work.append(ch);
				}
				else {
					work.delete(0, work.length());
					doEval = true;
					work.append(ch);
				}
				break;
			case '}':
				if( doEval ) {
					doEval = false;
					work.append(ch);
					result.append( evaluate( work.toString() ) );
				}
				break;
			default:
				if( doEval ) {
					work.append(ch);
				}
				else {
					result.append(ch);
				}
				break;
			}
		}

		return result.toString();
	}

	// used for help files
	private String evaluate(String test) {
		return getProgInt().interpret(test, null);
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
		if ( prompt_enabled && getPlayer(client).getConfig().get("prompt_enabled") ) {
			prompt("< %mode %h/%H %m/%M %state >", client);
		}
	}
	
	// Handlers
	
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

		String playerMode = player.getMode().toString();

		output = output.replace("%mode", playerMode);

		output = output.replace( "%h", hp );
		output = output.replace( "%H", max_hp );

		output = output.replace( "%m", mana );
		output = output.replace( "%M", max_mana );

		String playerState = player.getState().toString();

		output = output.replace( "%state", playerState );

		send(output, client);
		//addMessage(new Message(output, player));
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
		//ChatChannel cc = chan.getChatChannel(channelName); // get ChatChannel by name
		//cc.write(getPlayer(client), arg);                  // add message to ChatChannel message queue

		//client.write("wrote " + arg + " to " + channelName + " channel.\n");
		final Player player = getPlayer(client);

		chan.send( channelName, player, arg );                                      // send chat message
		chatLog.writeln("(" + channelName + ") <" + player.getName() + "> " + arg); // log the sent chat message

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
		Room room = getRoom(player);             // get current room

		debug("Entering exit handler...");

		for (final Exit exit : room.getExits())
		{	
			// for doors
			final String[] doorNames = exit.getName().split("/");
			
			// checks for doorness and at the same time whether the given command is a valid exit name for the door
			boolean door = (doorNames.length == 2) ? ((doorNames[0].equals(cmd) || doorNames[1].equals(cmd)) ? true : false) : false;
			boolean door_alias = (doorNames.length == 2) ? (exit.hasAlias(doorNames[0] + "|" + cmd) || exit.hasAlias(doorNames[1] + "|" + cmd)) : false;
			
			debug("Command: " + cmd);
			debug("door [B]: " + door);
			debug("door_alias [B]: " + door_alias);
			
			if( exit.getExitType() == ExitType.DOOR ) {
				debug("\'" + doorNames[0] + "|" + cmd + "\'");
				debug("\'" + doorNames[1] + "|" + cmd + "\'");
			}
			
			debug("Aliases: " + exit.getAliases());
			
			if (exit.getName().equals(cmd) || exit.getName().equals(aliases.get(cmd)) || exit.getAliases().contains(cmd) || door || door_alias)
			{
				boolean canUse = false;

				if( exit.getExitType() == ExitType.DOOR && exit instanceof Door ) {
					final Door d = (Door) exit;
					
					if( !d.isLocked() ) { canUse = true; }
					else {
						send("The door is locked.", client);
					}
				}
				else if ( exit.getExitType() == ExitType.PORTAL && exit instanceof Portal ) {
					final Portal p = (Portal) exit;
					
					if( p.isActive() ) { canUse = true; }
					else {
						if( p.requiresKey() && p.hasKey(player) ) {
							// activate portal
							canUse = true;
						}
					}
				}
				else {
					canUse = true;
				}

				if ( canUse ) { // exit lock check?
					debug("success");

					// send the success message
					if (!exit.getMessage("succMsg").equals("")) {
						//Message msg = new Message(exit.getMessage("succMsg"), player);
						//addMessage(msg);
						send(exit.getMessage("succMsg"), client);
						send("You leave the room.", client); // TODO fix kludge
					}

					// execute leave triggers
					for(Trigger trigger : room.getTriggers(TriggerType.onLeave)) {
						System.out.println(trigger);
						execTrigger(trigger, client);
					}
					
					// send other exit properties
					send("Exit Type: " + exit.getExitType().getName(), client);
					
					// set player's location
					if( exit.getExitType() == ExitType.DOOR ) {
						Door d = (Door) exit;
						
						int dbref = room.getDBRef();
						
						int orig = d.getLocation();
						int dest = d.getDestination();
						
						if( dbref == orig )      player.setLocation( dest );
						else if( dbref == dest ) player.setLocation( orig );
					}
					else {
						player.setLocation( exit.getDestination() );
					}
					
					// remove listener from room
					room.removeListener( player );

					// send the osuccess message
					if (!exit.getMessage("osuccMsg").equals("")) {
						Message msg = new Message(exit.getMessage("osuccMsg"), room);
						addMessage(msg);
						addMessage( new Message(player.getName() + " left the room.") ); // TODO fix kludge
					}

					// get new room object
					room = getRoom(player);

					// add listener to room
					room.addListener(player);

					// execute enter triggers
					for(Trigger trigger : room.getTriggers(TriggerType.onEnter)) {
						System.out.println(trigger);
						execTrigger(trigger, client);
					}

					// call msp to play a tune that is the theme for a type of room
					if (msp == 1 && player.getConfig().get("msp_enabled")) { // MSP is enabled
						if (room.getRoomType().equals(RoomType.INSIDE)) { // if inside play the room's music
							playMusic("tranquil.wav", client);
						}
						else if (room.getRoomType().equals(RoomType.OUTSIDE)) { // if outside, play appropriate weather sounds?
							// perhaps simply setting a pattern of some kind would be good?
							// in case we wish to have an ambient background (rain, wind) and an effect sound for lightning (thunder)
							// ASIDE: some clients only support one sound, so an effect sound should be handled
							// as the sound, and then in the ambient background

							// get weather, then play related sound
							switch(room.getWeather().ws.name) {
							case "Rain":
								playMusic("rain.wav", client);
								break;
							case "Cloudy":
								playMusic("rain.wav", client);
								break;
							case "Clear Skies":
								playMusic("rain.wav", client);
								break;
							default:
								break;
							}
						}
					}

					// show the description
					look(room, client);

					// tell us we are leaving the exit handler
					debug("Exiting exit handler...");
				}

				return true;
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
			
			String scmd = "";
			String sarg = "";
			
			if (input.indexOf(" ") != -1) { // Arguments
				scmd = input.substring(input.indexOf(".") + 1, input.indexOf(" "));
				sarg = input.substring(input.indexOf(" ") + 1, input.length());
			}
			else { // No Arguments
				scmd = input.substring(input.indexOf(".") + 1, input.length());
			}

			System.out.println("scmd: " + scmd);
			System.out.println("sarg: " + sarg);

			if (scmd.equals("select")) {
				/* select takes a spell name as an argument
				 * 
				 */
				if( sarg.equals("") ) {
					send("Please specify the spell you'd like to cast.", client);
				}
				else {
					getPlayer(client).getSpellQueue().push(spells2.get(sarg));
				}
			}
			else if (scmd.equals("queue")) {
				send("Queue", client);
				send("---------------------", client);
				for (Spell spell : getPlayer(client).getSpellQueue()) {
					send(spell.getName(), client);
				}
				send("---------------------", client);
			}
			else if (scmd.equals("spells")) {
				SpellBook sb = getPlayer(client).getSpellBook();
				send("Spellbook", client);

				if( sb != null ) {
					for(int level = 0; level <= getPlayer(client).getLevel(); level++) {
						List<Spell> spells =  sb.getSpells(level);

						if( spells != null ) {
							send("-----------------", client);
							send("Level: " + level, client);
							for(Spell spell : spells) {
								send(" " + spell.getName(), client); // space for indenting purposes
							}
							send("-----------------", client);
						}
					}
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
				getPlayer(client).getSpellQueue().clear();
				
				// reset editor
				getPlayer(client).setEditor(Editors.NONE);
				
				// reset status
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
	 * @param input input to the editor
	 * @param client the client to which output will be sent
	 */
	public void op_chargen(final String input, final Client client) {
		final Player player = getPlayer(client);

		debug("STATUS: -" + player.getStatus() + "-");
		
		// if the player isn't a guest character and the player is new
		if( !player.getName().startsWith("Guest") && (player.isNew() || 1 == 0) ) {
			cgData cgd = player.getCGData();
			
			if (input.equals("start")) { // if the input indicates we are to start character generation
				cgd = new cgData(0, 1, 0, false); // create new character generation data (cgd) object and populate with values
				
				debug("T: " + cgd.t + " Step: " + cgd.step + " Answer: " + cgd.answer);

				player.setCGData( op_chargen("", client, cgd) );
			}
			else {
				player.setCGData( op_chargen(Utils.trim(input), client, player.getCGData()) );
			}
			
			// check to see if the chargen data is null
			if (cgd != null) { // if not null , pass the data on to an overloaded version of this function
				player.setCGData( op_chargen("", client, player.getCGData()) );
			}
		}
		else {
			send("Game> You have already completed and finalized character generation.", client);

			player.setStatus("IC");
			player.setEditor(Editors.NONE);
		}
	}
	
	/**
	 * TODO write nicer messages and maybe find a way to make them customizable
	 * instead of hardcoded.
	 * 
	 * @param input
	 * @param client
	 * @param cgd
	 * @return
	 */
	public cgData op_chargen(final String input, final Client client, final cgData cgd) {
		final Player player = getPlayer(client);

		debug("T: " + cgd.t + " Step: " + cgd.step + " Answer: " + cgd.answer);

		int t = cgd.t;
		int step = cgd.step;     // the step of chargen we are on
		int answer = cgd.answer; // the player's answer to the current input request
		boolean edit = cgd.edit;

		debug("Start: T is now " + t);

		if (t == 0) {
			//send("Step: " + step, client);
			debug("Step: " + step);

			switch(step) {
			case 1:
				if( edit ) send("Player Race: " + player.getRace(), client);
				send("Please choose a race:", client);
				
				int si = 0;
				int n = 0;
				
				final StringBuilder sb = new StringBuilder();
				
				for(final Race race : races) {
					if( si < 2 ) {
						sb.append("" + n + ")" + Utils.padRight(race.getName(), 8) + " ");
						
						si++;
					}
					else if( si < 3 ) {
						sb.append("" + n + ")" + Utils.padRight(race.getName(), 8));
						
						si++;
					}
					else {
						send(sb.toString(), client);
						sb.delete(0, sb.length());
						si = 0;
						
						sb.append("" + n + ")" + Utils.padRight(race.getName(), 8) + " ");
					}
					
					n++;
				}
				
				//send("1) " + Utils.padRight("" + Races.ELF, 6) +  " 2) " + Utils.padRight("" + Races.HUMAN, 6) + " 3) " + Utils.padRight("" + Races.DWARF, 6), client);
				//send("4) " + Utils.padRight("" + Races.GNOME, 6) + " 5) " + Utils.padRight("" + Races.ORC, 6) + " 6) " + Utils.padRight("" + Races.HALF_ELF, 6), client);
				
				break;
			case 2:
				if( edit ) send("Player Gender: " + player.getGender(), client);
				send("Please choose a gender:", client);
				send("1) Female 2) Male 3) Other 4) Neuter (no gender)", client);
				break;
			case 3:
				if( edit ) send("Player Class: " + player.getPClass(), client);
				send("Please choose a class:", client);
				
				// TODO Fix Hardcoded class options?
				/*StringBuilder sb = new StringBuilder();
				
				int c = 1;
				int n = 0;
				
				for(PClass pclass : Classes.getClasses()) {
					if( n < 3 ) {
						if( n < 2 ) sb.append(" " + c + ") " + Utils.padRight("" + pclass, 12) + " ");
						else        sb.append(" " + c + ") " + Utils.padRight("" + pclass, 12));
					}
					
					c++;
					n++;
					
					if( n == 3 ) {
						send( sb.toString(), client );
						sb.delete(0, sb.length());
						n = 0;
					}
				}*/
				
				send(" 1) " + Utils.padRight("" + Classes.BARBARIAN, 12) + " 2) " + Utils.padRight("" + Classes.BARD, 12) + " 3) " + Utils.padRight("" + Classes.CLERIC, 12), client);
				send(" 4) " + Utils.padRight("" + Classes.DRUID, 12) + " 5) " + Utils.padRight("" + Classes.FIGHTER, 12) + " 6) " + Utils.padRight("" + Classes.MONK, 12), client);
				send(" 7) " + Utils.padRight("" + Classes.PALADIN, 12) + " 8) " + Utils.padRight("" + Classes.RANGER, 12) + " 9) " + Utils.padRight("" + Classes.ROGUE, 12), client);
				send("10) " + Utils.padRight("" + Classes.SORCERER, 12) + "11) " + Utils.padRight("" + Classes.WIZARD, 12) + " 0) " + Utils.padRight("" + Classes.NONE, 12), client);
				
				break;
			case 4:
				if( edit ) send("Player Alignment: " + player.getAlignment(), client);
				send("Please select an alignment:", client);
				for(int i = 1; i < 9; i = i + 3) {
					send("" + i + ") " + Utils.padRight("" + Alignments.values()[i], ' ', 14) + " " + (i+1) + ") " + Utils.padRight("" + Alignments.values()[i+1], ' ', 14) + " " + (i+2) + ") " + Utils.padRight("" + Alignments.values()[i+2], ' ', 14), client);
				}
				break;
			case 5:
				send(Utils.padRight("Race: ", ' ', 8) + player.getRace(), client);
				send(Utils.padRight("Gender: ", ' ', 8)  + player.getGender(), client);
				send(Utils.padRight("Class: ", ' ', 8)  + player.getPClass(), client);
				send(Utils.padRight("Align: ", ' ', 8)  + player.getAlignment(), client);
				send("", client);
				send("Options:", client);
				send(" 1) Reset 2) Edit 3) Done", client);
				break;
			case 6:
				send("Edit What:", client);
				send("1) Race 2) Gender 3) Class 4) Alignment 5) Abort", client);
				break;
			default:
				break;
			}
			
			client.write("> ");

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

				//send("Answer: " + answer, client);
				debug("Answer: " + answer);

				//send("Entering Step " + step, client);
				debug("Entering Step " + step);

				if (step == 1) {
					//player.setRace(Races.getRace(answer));
					player.setRace(races.get(answer));
					
					send("You have chosen to be an " + player.getRace(), client);
					send("Player Race set to: " + player.getRace(), client);

					/*Ability[] ab = new Ability[] {
							Abilities.STRENGTH, Abilities.DEXTERITY, Abilities.CONSTITUTION,
							Abilities.INTELLIGENCE, Abilities.WISDOM, Abilities.CHARISMA
					};*/
					
					Ability[] ab = rules.getAbilities();

					int index = 0;

					// apply permanent stat adjustments according to race
					for(int value : (Races.getRace(answer)).getStatAdjust()) {
						player.setAbility(ab[index], player.getAbility(ab[index]) + value);
						index++;
					}

					send("", client);

					step++;

					if( edit ) { edit = false; step = 5; }
				}
				else if (step == 2) {
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
					
					// You are female/You are male/You have chosen to be genderles/You have no gender
					send("Player Gender set to: " + player.getGender(), client);
					send("", client);

					step++;

					if( edit ) { edit = false; step = 5; }
				}
				else if (step == 3) {
					player.setPClass(Classes.getClass(answer));
					
					send("You have chosen to pursue being a " + player.getClass(), client);
					send("Player Class set to: " + player.getPClass(), client);
					send("", client);

					step++;

					if( edit ) { edit = false; step = 5; }
				}
				else if(step == 4) {

					if( answer >= 1 && answer <= 9 ) {
						player.setAlignment(Alignments.values()[answer]);

						send("Player Alignment set to: " + player.getAlignment(), client);
						send("", client);

						step++;

						if( edit ) { edit = false; step = 5; }
					}
					else {
						send("Invalid Alignment Selection. Try Again.", client);
						send("", client);
					}
				}
				else if (step == 5) {
					if (answer == 1) {      // Reset
						player.setRace(Races.NONE);
						player.setGender('N');
						player.setPClass(Classes.NONE);
						player.setAlignment(Alignments.NONE);

						reset_character(player); // reset character data to defaults

						client.write("Resetting...");
						client.writeln("Done");

						step = 1;
					}
					else if (answer == 2) { // Edit
						step = 6;
					}
					else if (answer == 3) { // Exit
						// not sure whether I should do the above steps on the spot
						// or in this function below, by passing it the appropriate classes
						// I suppose either is doable
						
						generate_character(player); // generate basic character data based on choices

						step = 0;
						answer = 0;

						player.setEditor(Editors.NONE);

						send("Game> Editor Reset", client);

						player.setStatus("OOC");

						send("Game> Status Reset", client);

						send("Exiting...", client);

						return new cgData(-1, -1, -1, false);
					}
				}
				else if (step == 6) {
					switch(answer) {
					case 1:
						edit = true;
						step = 1;
						break;
					case 2:
						edit = true;
						step = 2;
						break;
					case 3:
						edit = true;
						step = 3;
						break;
					case 4:
						edit = true;
						step = 4;
						break;
					case 5:
						client.write("Abort Edit\n");
						edit = false;
						step = 5;
					default:
						break;
					}
				}

				t = 0;
				debug("T is now " + t);
			}
			catch(NumberFormatException npe) {
				System.out.println("--- Stack Trace ---");
				npe.printStackTrace();
			}
		}

		return new cgData(t, step, answer, edit);
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
	public void op_helpedit(final String input, final Client client) {
		final Player player = getPlayer(client);    // get Player
		final EditList list = player.getEditList(); // get EditList

		debug("input: " + input);

		String hcmd = "";
		String harg = "";

		if (input.indexOf(".") == 0) // Is it an editor commans?
		{
			if (input.indexOf(" ") != -1) { // Arguments
				hcmd = input.substring(input.indexOf(".") + 1, input.indexOf(" "));
				harg = input.substring(input.indexOf(" ") + 1, input.length());
			}
			else { // No Arguments
				hcmd = input.substring(input.indexOf(".") + 1, input.length());
			}

			debug("HEDIT CMD");
			debug("hcmd: " + hcmd);

			if ( hcmd.equals("abort") || hcmd.equals("a") )
			{
				player.abortEditing();

				send("< List Aborted. >", client);
				send("< Exiting... >", client);

				player.setEditor(Editors.NONE);
				player.setStatus("OOC");
			}
			else if ( hcmd.equals("del") || hcmd.equals("d") )
			{
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
				if (list != null) {
					list.setLineNum(Utils.toInt(harg, 0));
					debug("Line changed to: " + Utils.toInt(harg, 0));
				}
			}
			else if ( hcmd.equals("list") || hcmd.equals("l") )
			{
				if (list != null) {
					int i = 0;
					for (String s : list.getLines())
					{
						System.out.println(Utils.padRight(i + ": ",' ', 5) + s);
						send(Utils.padRight(i + ": ",' ', 5) + s, client);
						i++;
					}
				}
			}
			else if ( hcmd.equals("print") || hcmd.equals("p") )
			{
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
				op_helpedit(".save", client);

				send("< Exiting... >", client);

				player.setEditor(Editors.NONE);
				player.setStatus("OOC");
			}
			else if ( hcmd.equals("replace") || hcmd.equals("repl") ) {
				// .repl start_line end_line=/old text/new text
				// .repl line=new text
			}
			else if ( hcmd.equals("save") || hcmd.equals("s") )
			{
				if (list != null) {
					// convert the list to a string array
					this.helpTable.put(list.name, list.getLines().toArray(new String[0]));

					send("< Help File Written Out! >", client);
					send("< Help File Saved. >", client);
				}
			}
			else if ( hcmd.equals("stat") || hcmd.equals("st") ) {
				if (list != null) {
					String header = "< Help File: " + list.name + ".txt" + " Line: " + list.getLineNum() + " Lines: " + list.getNumLines() + "  >";
					send(header, client);
				}
			}
		}
		else {
			if (list != null) {
				debug("Line: " + list.getLineNum() + " Input: " + input);
				if( list.getLineNum() < list.getNumLines() ) {
					list.setLine(list.getLineNum(), input);
				}
				else { list.addLine(input); }
				debug(list.getLineNum() + ": \"" + list.getCurrentLine() + "\"");
			}
		}
	}
	
	public void op_input(final String input, final Client client) {
		Tuple<Editors, String> temp = interactMap.get(client);
		
		if( temp != null ) {
			temp.two = input;
			send("Input was: \'" + temp.two + "\'", client);
		}
		
		final Player player = getPlayer( client );
		
		player.setEditor(temp.one);
		player.setStatus("OOC");
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
	 * @param input
	 * @param client
	 */
	public void op_listedit(final String input, final Client client)
	{
		final Player player = getPlayer(client);
		final EditList list = player.getEditList();

		debug("input: " + input);

		String lcmd = "";
		String larg = "";

		if (input.indexOf(".") == 0)
		{
			if (input.indexOf(" ") != -1) { // Arguments
				lcmd = input.substring(input.indexOf(".") + 1, input.indexOf(" "));
				larg = input.substring(input.indexOf(" ") + 1, input.length());
			}
			else { // No Arguments
				lcmd = input.substring(input.indexOf(".") + 1, input.length());
			}

			debug("LSEDIT CMD");
			debug("lcmd: " + lcmd);
			debug("larg: " + larg);

			if ( lcmd.equals("abort") || lcmd.equals("a") ) // exit the editor, aborting any changes made
			{
				player.abortEditing();

				send("< List Aborted. >", client);
				send("< Exiting... >", client);

				player.setEditor(Editors.NONE);
				player.setStatus("OOC");
			}
			else if ( lcmd.equals("help") || lcmd.equals("h") )
			{
				send("<List Editor Help>", client);
				send(".abort(.a)  - throw out the current list and exit", client);
				send(".help(.h)   - display this help", client);
				send(".insert(.i) - move insertion point to line specified by arg", client);
				send(".list(.l)   - print out list w/line numbers", client);
				send(".print(.p)  - print out list w/o line numbers", client);
				send(".quit(.q)   - save and exit list", client);
				send(".save(.s)   - save list", client);
				send(".stat(.st)  - display current status of the list", client);
				send("< End List Editor Help >", client);
			}
			else if ( lcmd.equals("insert") || lcmd.equals("i") )
			{
				if (list != null) {
					int lineNum = Utils.toInt(larg, list.getLineNum());
					list.setLineNum( lineNum );
				}
			}
			else if ( lcmd.equals("list") || lcmd.equals("l") ) // send the list to the client, line by line (with line numbers)
			{
				int i = 0;
				if (list != null) {
					for (String s : list.getLines())
					{
						System.out.println(i + ": " + s);
						send(i + ": " + s, client);
						i++;
					}
				}
			}
			else if ( lcmd.equals("print") || lcmd.equals("p") ) // send the list to the client, line by line
			{
				if (list != null) {
					for (String s : list.getLines())
					{
						System.out.println(s);
						send(s, client);
					}
				}
			}
			else if ( lcmd.equals("quit") || lcmd.equals("q") )
			{
				// save the help file?
				op_listedit(".save", client);

				send("< Exiting... >", client);

				player.setEditor(Editors.NONE);
				player.setStatus("OOC");
			}
			else if ( lcmd.equals("save") || lcmd.equals("s") ) // save the current list
			{
				player.saveCurrentEdit();

				send("< List Written Out! >", client);
				send("< List Saved. >", client);
			}
			else if ( lcmd.equals("stat") || lcmd.equals("st") ) // tell us about the current list (current line we're editing, number of lines)
			{
				if (list != null) {
					String header = "< List: " + list.name + " Line: " + list.getLineNum() + " Lines: " + list.getNumLines() + " >";
					send(header, client);
				}
			}
			System.out.println(getPlayer(client).getStatus());
		}
		else // add the line you typed to the list at the current line
		{ 
			if (list != null) { // if we have a valid list
				list.addLine(input);
				debug(list.getNumLines() + ": " + input);
			}
		}
	}

	public Mail handleMail(final String input, final Client client) {
		// TODO resolve the issue of where to send the new mail object since I can't return it because this is a handler
		final int START = 0;
		final int SUBJECT = 1;
		final int RECIPIENT = 2;
		final int MESSAGE = 3;
		final int DONE = 4;

		final Player player = getPlayer(client);

		final EditorData data = player.getEditorData();

		final int step = (Integer) data.getObject("step");

		System.out.println("MAIL: " + step);

		// done
		if( input.equals(".") ) { // send the mail
			String sender = (String) data.getObject("sender");
			String recipient = (String) data.getObject("recipient");
			String subject = (String) data.getObject("subject");
			String message = (String) data.getObject("message");

			Mail mail = new Mail(-1, sender, recipient, subject, message, 'U');

			// exit
			send("< Exiting... >", client);

			// reset editor and player status
			player.setStatus( (String) data.getObject("pstatus") );
			player.setEditor(Editors.NONE);
			
			sendMail( mail, player );
			
			return mail;
		}
		else if( input.equals("~") ) { // abort sending
			// exit
			send("< Exiting... >", client);

			// reset editor and player status
			player.setStatus( (String) data.getObject("pstatus") );
			player.setEditor(Editors.NONE);
			
			return null;
		}
		else {
			switch(step) {
			case START:
				client.writeln("Writing a new mail message...");
				data.setObject("step", SUBJECT);
				send("Subject?", client);
				break;
			case SUBJECT:
				data.setObject("subject", input);
				echo(input, true, client);
				data.setObject("step", RECIPIENT);
				send("Recipient?", client);
				break;
			case RECIPIENT:
				data.setObject("recipient", input);
				echo(input, true, client);
				data.setObject("step", MESSAGE);
				send("Message:", client);
				break;
			case MESSAGE:
				data.setObject("message", input);
				echo(input, true, client);
				data.setObject("step", DONE);
				send("Done.", client);
				break;
			case DONE:
				break;
			default:
				break;
			}
		}
		
		return null;
	}

	/* Editors - OLC (OnLine Creation) Tools */
	
	// TODO write code for op_creatureedit
	public void op_creatureedit(final String input, final Client client) {
	}

	/**
	 * Room Editor
	 * 
	 * Whenever a change is made to the data about the room, if no other
	 * messages is output and the change was made successfully, the "editor"
	 * should send "Ok." to the client.
	 * 
	 * @param input
	 * @param client
	 */
	@SuppressWarnings("unchecked")
	public void op_roomedit(final String input, final Client client) {
		final Player player = getPlayer(client);

		String rcmd = "";
		String rarg = "";

		EditorData data = player.getEditorData();

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
			player.setEditor(Editors.NONE);
		}
		else if ( rcmd.equals("addexit") ) {
			// addexit <name> <destination dbref>
			String[] args = rarg.split(" ");

			// if 
			if (args.length > 1 ) {
				final int destination = Integer.parseInt(args[1]);
				final Room r = objectDB.getRoomById(destination);

				if ( r != null ) {
					Room r1 = (Room) data.getObject("room");
					data.addObject("e|" + args[0], new Exit( args[0], r1.getDBRef(), destination ));
					send("Ok.", client);
				}
			}
		}
		else if ( rcmd.equals("addthing") ) {
			// addthing <thing name/dbref>

			// result:
			// key: add or addthing
			// value: object/dbref of object
			// ex. add, 4
			// ex. addthing, 4

			int dbref = Utils.toInt(rarg, -1);

			final MUDObject m;

			if( dbref != -1 ) m = objectDB.get(dbref);
			else m = objectDB.getByName(rarg);

			if( m != null ) {
				if( m instanceof Thing) {
					final Thing thing = (Thing) m;

					Room room = (Room) data.getObject("room");
					Room room1 = objectDB.getRoomById( thing.getLocation() );

					room1.removeThing( thing );
					room.addThing( thing );
					thing.setLocation( room.getDBRef() );

					send("Ok.", client);
				}
			}
			else { // create new thing and set the name to rarg's value if it's not a number?
				Room room = (Room) data.getObject("room");

				Thing thing = new Thing(rarg);
				room.addThing(thing);
				thing.setLocation( room.getDBRef() );

				objectDB.add(thing);
				send("Ok.", client);
			}
		}
		else if ( rcmd.equals("additem") ) {
			// TODO Fix this, since it shouldn't actually create the item until the room is saved
			Room room = (Room) data.getObject("room");
			
			if( prototypes.containsKey(rarg) ) {
				Item item = createItem(rarg, false);
				
				item.setLocation( room.getDBRef() );
				
				// TODO fix getObjects somehow
				//final String suffix = "" + data.getObjects("i|" + item.getName()).size();
				
				//debug("REDIT (suffix): " + suffix);
				
				/*if( suffix.equals("0") ) {
					data.addObject("i|" + item.getName(), item);
				}
				else {
					data.addObject("i|" + item.getName() + suffix, item);
				}*/
				
				// TODO fix this kludge later
				boolean test = false;
				int suffix = 0;
				
				while(!test) {
					if( suffix == 0 ) test = data.addObject("i|" + item.getName(), item);
					else              test = data.addObject("i|" + item.getName() + suffix, item);
					
					suffix++;
				}
				
				send("Ok.", client);
			}
		}
		else if ( rcmd.equals("desc") ) {
			String[] rargs = rarg.split(" ");
			
			if( rargs.length > 1 ) {
				if( rargs[0].equalsIgnoreCase("-f") ) {
					// ?
					data.setObject("desc", rarg);
				}
				else {
					data.setObject("desc", rarg);
				}
			}
			else {
				data.setObject("desc", rarg);
			}
			
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
					System.out.println("--- Stack Trace ---");
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
			player.setEditor(Editors.NONE);
		}
		else if ( rcmd.equals("help") ) {
			if ( rarg.equals("") ) {
				// output help information
				final List<String> output = (List<String>) Utils.mkList(
						"Room Editor -- Help",
						Utils.padRight("", '-', 74),
						"abort                          abort the editor (no changes will be kept)",
						"addexit <name> <destination>   creates a new exit",
						"additem <prototype key>        creates a new instance of the indicated",
						"                               prototype",
						"addthing <name/dbref>          move an existing object to the room (not",
						"                               sure how it should work)",
						"desc <param> <new description> change/set the room description",
						"dim <dimension> <size>         change a dimension of the room (x/y/z)",
						"done                           finish editing (save & exit)",
						"help                           shows this help information",
						"items                          list available item prototypes",
						"npcs",
						"layout                         display a 2D layout visualization",
						"name <new name>                change/set the room name",
						"rooms                          list the other rooms that are in the same",
						"                               zone as this one",
						"save                           save changes to the room",
						"setlocation                    change the location (deprecated?)",
						"setzone                        set the zone that this room belongs to",
						"show                           show basic information about the room",
						"trigger <type> <data>          setup a trigger of the specified type with",
						"                               the specified data",
						"zones                          list the zones that exist",
						Utils.padRight("", '-', 74)
				);
				client.write(output);
			}
			else {
				// output help information specific to the command name given
			}
		}
		else if ( rcmd.equals("items") ) {
			final List<String> output = new LinkedList<String>();
			
			for(final String s : prototypes.keySet()) {
				output.add(s);
			}
			
			client.write(output);
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
		else if ( rcmd.equals("rooms") ) {
			Zone z = (Zone) data.getObject("zone");
			
			if( z != null ) {
				for(final Room rm : z.getRooms()) {
					send(rm.getName() + " (#" + rm.getDBRef() + ")", client);
				}
			}
		}
		else if ( rcmd.equals("save") ) {
			Room room = (Room) data.getObject("room");
			
			room.setName((String) data.getObject("name"));
			room.setFlags((EnumSet<ObjectFlag>) data.getObject("flags"));
			room.setDesc((String) data.getObject("desc"));
			room.setLocation((Integer) data.getObject("location"));
			
			room.x = (Integer) data.getObject("x");
			room.y = (Integer) data.getObject("y");
			room.z = (Integer) data.getObject("z");
			
			room.getZone().removeRoom(room);
			room.setZone((Zone) data.getObject("zone"));
			room.getZone().addRoom(room);
			
			for ( String s : data.getObjects().keySet() ) {
				if ( s.startsWith("e|") ) {
					Exit e = (Exit) data.getObject(s);

					if( !(room.getExits().contains(e)) ) {
						objectDB.add(e);
						objectDB.addAsNew(e);
						objectDB.addExit(e);
						room.getExits().add(e);
					}
				}
				
				if( s.startsWith("i|") ) {
					Item item = (Item) data.getObject(s);
					
					if( !(room.getItems().contains(item)) ) {
						initCreatedItem(item);
						room.addItem(item);
					}
				}
			}
			if( data.getObject("onEnter") != null ) {
				for( Trigger t : (LinkedList<Trigger>) data.getObject("onEnter") ) {
					room.setTrigger(TriggerType.onEnter, t);	
				}
			}
			if( data.getObject("onLeave") != null ) {
				for( Trigger t : (LinkedList<Trigger>) data.getObject("onLeave") ) {
					room.setTrigger(TriggerType.onLeave, t);
				}
			}
			send("Room saved.", client);
		}
		else if ( rcmd.equals("setflag") ) {
			EnumSet<ObjectFlag> flags = (EnumSet<ObjectFlag>) data.getObject("flags");
			if( rarg.startsWith("!") ) {
				flags.remove( ObjectFlag.fromLetter( rarg.charAt(1) ) );
				send( ObjectFlag.fromLetter( rarg.charAt(1) ).name() + " flag removed.", client);
			}
			else {
				try {
					ObjectFlag flag = ObjectFlag.fromLetter( rarg.charAt(0) );
					flags.add( flag );
					send( flag.name() + " flag set.", client);
				}
				catch(IllegalArgumentException iae) {
					System.out.println("--- Stack Trace ---");
					iae.printStackTrace();
					send("No such flag.", client);
				}
			}
			data.setObject("flags", flags);
			send("Ok.", client);
		}
		else if ( rcmd.equals("setlocation") ) {
			data.setObject("location", Utils.toInt(rarg, -1));
			send("Ok.", client);
		}
		else if ( rcmd.equals("setzone") ) {
			//data.setObject("zone", getZone(Utils.toInt(rarg, -1)));
			data.setObject("zone", getZone(rarg));
			send("Ok.", client);
		}
		else if ( rcmd.equals("show") ) {
			Room room = (Room) data.getObject("room");

			// will be a little like examine, just here to show changes
			send("--- Room Editor " + Utils.padRight("", '-', 80 - 16), client);
			//send(Utils.padRight("", '-', 80), client);
			send("DB Reference #: " + room.getDBRef(), client);
			send("Name: " + data.getObject("name"), client);
			send("Flags: " + ((EnumSet<ObjectFlag>) data.getObject("flags")).toString(), client);
			send("Dimensions:", client);
			send("    X: " + (Integer) data.getObject("x"), client);
			send("    Y: " + (Integer) data.getObject("y"), client);
			send("    Z: " + (Integer) data.getObject("z"), client);
			send("Description:", client);
			showDesc((String) data.getObject("desc"), 80, client);
			send("Location: " + (Integer) data.getObject("location"), client);
			send("Zone: " + colors(((Zone) data.getObject("zone")).getName(), "purple2"), client);

			if( player.getConfig().get("compact-editor") ) {
				StringBuilder sb = new StringBuilder();
				List<String> exitStrings = new LinkedList<String>();
				List<String> itemStrings = new LinkedList<String>();

				send( colors(Utils.padRight("Exits:", ' ', 35), getDisplayColor("exit")) + " " + colors(Utils.padRight("Items:", ' ', 35), getDisplayColor("item")),client);
				for ( final String s : data.getObjects().keySet() ) {
					System.out.println(s);
					if ( s.startsWith("i|") ) {
						System.out.println("Item Found");
						final Item item = (Item) data.getObject(s);
						if( item.getDBRef() == -1 ) itemStrings.add(colors(item.getName() + "(#" + item.getDBRef() + ")", "green"));
						else itemStrings.add(item.getName() + "(#" + item.getDBRef() + ")");
					}
					else if ( s.startsWith("e|") ) {
						System.out.println("Exit Found");
						final Exit exit = (Exit) data.getObject(s);
						exitStrings.add(exit.getName() + "(#" + exit.getDBRef() + ") - Source: " + exit.getLocation() + " Dest: " + exit.getDestination());
					}
				}

				// the below will cause null pointer exceptions if one empties before the other
				while( !itemStrings.isEmpty() || !exitStrings.isEmpty() ) {
					String exitString = "", itemString = "";
					
					if( !exitStrings.isEmpty() ) exitString = exitStrings.remove(0);
					if( !itemStrings.isEmpty() ) itemString = itemStrings.remove(0);

					sb.append( Utils.padRight(exitString, ' ', 35) + " " + Utils.padRight(itemString, ' ', 35));

					send( sb.toString(), client );

					sb.delete(0, sb.length());
				}
			}
			else {
				send(colors("Exits:", getDisplayColor("exit")), client);
				for ( final String s : data.getObjects().keySet() ) {
					System.out.println(s);
					if ( s.startsWith("e|") ) {
						System.out.println("Exit Found");
						final Exit exit = (Exit) data.getObject(s);
						send( exit.getName() + "(#" + exit.getDBRef() + ") - Source: " + exit.getLocation() + " Dest: " + exit.getDestination(), client );
					}
				}
				send(colors("Items:", getDisplayColor("item")), client);
				for ( final String s : data.getObjects().keySet() ) {
					System.out.println(s);
					if ( s.startsWith("i|") ) {
						System.out.println("Item Found");
						final Item item = (Item) data.getObject(s);
						send( item.getName() + "(#" + item.getDBRef() + ")", client );
					}
				}
			}

			send(Utils.padRight("", '-', 80), client);
		}
		else if ( rcmd.equals("trigger") ) {
			String[] rargs = rarg.split(" ");

			if( rargs.length >= 2 ) {
				int type = Utils.toInt(rargs[0], -1);

				if( type == 0 ) {
					if( data.getObject("onEnter") == null ) {
						data.addObject("onEnter", new LinkedList<Trigger>());
					}
					
					((LinkedList<Trigger>) data.getObject("onEnter")).add(new Trigger(rargs[1]));
					
					send("Ok.", client);
				}
				else if( type == 1 ) {
					if( data.getObject("onLeave") == null ) {
						data.addObject("onLeave", new LinkedList<Trigger>());
					}
					
					((LinkedList<Trigger>) data.getObject("onLeave")).add(new Trigger(rargs[1]));
					
					send("Ok.", client);
				}
				else {
					send("trigger: Bad trigger type.", client);
				}
			}
			else {
				send("trigger: Bad arguments.", client);
			}
		}
		else if ( rcmd.equals("zones") ) {
			send("Zones", client);
			send(Utils.padRight("", '-', 40), client);

			if( "".equals(rarg) ) {
				for (Zone zone : zones.keySet()) {
					send("" + zone.getName() + " ( " + zone.getRooms().size() + " Rooms )", client);
				}
			}
			else {
				for (Zone zone : zones.keySet()) {
					if( zone.getName().toLowerCase().startsWith(rarg.toLowerCase()) ) {
						send("" + zone.getName() + " ( " + zone.getRooms().size() + " Rooms )", client);
					}
				}
			}
		}
		else {
			send("No such command.");
		}
	}

	/**
	 * Item Editor
	 * 
	 * @param input
	 * @param client
	 */
	public void op_itemedit(final String input, final Client client) {
		final Player player = getPlayer(client);

		String icmd = "";
		String iarg = "";

		EditorData data = player.getEditorData();

		if (input.indexOf(" ") != -1) {
			icmd = input.substring(0, input.indexOf(" ")).toLowerCase();
			iarg = input.substring(input.indexOf(" ") + 1, input.length());
		}
		else {
			icmd = input.substring(0, input.length()).toLowerCase();
		}

		debug("IEDIT CMD");
		debug("icmd: \"" + icmd + "\"");
		debug("iarg: \"" + iarg + "\"");

		if ( icmd.equals("abort") ) {
			/// clear edit flag
			((Item) data.getObject("item")).Edit_Ok = true;

			// reset editor and player status
			player.setStatus( (String) data.getObject("pstatus") );
			player.setEditor(Editors.NONE);
			
			// clear editor data
			player.setEditorData(null);
			
			// exit
			send("< Exiting... >", client);
		}
		else if ( icmd.equals("desc") ) {
			data.setObject("desc", iarg);
			send("Ok.", client);
		}
		else if ( icmd.equals("done") ) {
			// save changes
			op_itemedit("save", client);

			// clear edit flag
			((Item) data.getObject("item")).Edit_Ok = true;

			// reset editor and player status
			player.setStatus( (String) data.getObject("pstatus") );
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if ( icmd.equals("help") ) {
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
		else if ( icmd.equals("name") ) {
			data.setObject("name", iarg);
			send("Ok.", client);
		}
		else if ( icmd.equals("save") ) {
			final Item item = (Item) data.getObject("item");

			item.setName((String) data.getObject("name"));
			item.setDesc((String) data.getObject("desc"));
			item.setItemType((ItemType) data.getObject("type"));

			if ( (ItemType) data.getObject("type") == ItemType.CLOTHING ) {
				((Clothing) item).clothing = (ClothingType) data.getObject("subtype");
			}

			send("Item saved.", client);
		}
		else if ( icmd.equals("set") ) {
			// general purpose command
			// ex: set drinkable true
			
			String[] args = iarg.split(" ");

			if(args.length == 2) {
				if( args[0].equals("drinkable") ) {
					if( args[1].equalsIgnoreCase("true") ) {
						data.setObject("drinkable", true);
					}
					else if(args[1].equalsIgnoreCase("false") ) {
						data.setObject("drinkable", false);
					}
				}
			}

			send("Command not implemented.", client);
		}
		else if ( icmd.equals("show") ) {
			final Item item = (Item) data.getObject("item");

			// will be a little like examine, just here to show changes
			send(Utils.padRight("", '-', 80), client);
			//send("----------------------------------------------------", client);
			send("DB Reference #: " + item.getDBRef(), client);
			send("Name: " + data.getObject("name"), client);
			send("Item Type: " + ((ItemType) data.getObject("type")).toString(), client);
			send("Description:", client);
			showDesc((String) data.getObject("desc"), 80, client);
			
			switch( (ItemType) data.getObject("type") ) {
			case WEAPON:
				break;
			default:
				break;
			}
			
			//send("----------------------------------------------------", client);
			send(Utils.padRight("", '-', 80), client);

		}
		else if ( icmd.equals("type") ) {
			data.setObject("type", ItemType.getType(iarg));
			
			send("Ok.", client);
		}
		else {
			// currently causes a loop effect, where the command gets funneled back
			// into op_iedit regardless
			//cmdQueue.add(new CMD(input, client, 0));
		}
	}

	/**
	 * Quest Editor
	 * 
	 * @param input
	 * @param client
	 */
	public void op_questedit(final String input, final Client client) {
		final Player player = getPlayer(client);             // we shouldn't be modifying the player in here
		final EditorData data = player.getEditorData();      // we won't need to make a new instance of EditorData
		final Quest quest = (Quest) data.getObject("quest"); // no need for a new quest, since we should always have a valid one

		String qcmd = "";
		String qarg = "";

		if (input.indexOf(" ") != -1) {
			qcmd = input.substring(0, input.indexOf(" ")).toLowerCase();
			qarg = input.substring(input.indexOf(" ") + 1, input.length());
		}
		else {
			qcmd = input.substring(0, input.length()).toLowerCase();
		}

		debug("QEDIT CMD");
		debug("qcmd: \"" + qcmd + "\"");
		debug("qarg: \"" + qarg + "\"");

		if( qcmd.equals("abort") ) {
			send("< Aborting Changes... >", client);
			
			// clear edit flag
			quest.Edit_Ok = true;

			// reset editor and player status
			player.setStatus( (String) data.getObject("pstatus") );
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if ( qcmd.equals("done") ) {
			send("< Saving Changes... >", client);
			
			// save changes
			op_questedit("save", client);
			
			send("< Done >", client);

			// clear edit flag
			quest.Edit_Ok = true;

			// reset editor and player status
			player.setStatus( (String) data.getObject("pstatus") );
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if( qcmd.equals("help") ) {
			send("Quest Editor -- Help", client);
			send(Utils.padRight("", '-', 40), client);
			send("abort", client);
			//send("desc <new description>", client);
			send("done", client);
			send("help", client);
			send("name <new name>", client);
			send("save", client);
			send("setloc", client);
			send("show", client);
			send("zones", client);
		}
		else if( qcmd.equals("name") ) {
			data.setObject("name", qarg);
			send("Ok.", client);
		}
		else if( qcmd.equals("save") ) {
			quest.init(); // ensure that we have a valid quest id

			quest.setName( (String) data.getObject("name") );
			quest.setDescription( (String) data.getObject("desc") );

			// save tasks (add new ones, update existing ones, remove deleted ones)

			if( !quests.contains( quest) ) {
				quests.add( quest ); // add quest to global quest table
			}
			else {
			}
		}
		else if( qcmd.equals("setloc") ) {
			data.setObject("location", Utils.toInt(qarg, -1));
			send("Ok.", client);
		}
		else if( qcmd.equals("show") ) {
			// will be a little like examine, just here to show changes
			send(Utils.padRight("", '-', 80), client);
			
			send("Quest ID#: " + quest.getId(), client);
			send("Name: " + (String) data.getObject("name"), client);
			send("Location: " + (Integer) data.getObject("location"), client);
			send("Description: ", client);
			showDesc((String) data.getObject("desc"), 80, client);
			
			send(Utils.padRight("", '-', 80), client);
			
			int i = 0;
			
			for(Task t : quest.getTasks()) {
				send(" " + i + ") " + t.getDescription(), client);
				i++;
			}
			
			send(Utils.padRight("", '-', 80), client);

		}
		else if( qcmd.equals("zones") ) {
			for(Entry<Zone, Integer> zoneData : zones.entrySet()) {
				Zone zone = zoneData.getKey();
				send(zone.getInstanceId() + " - " + zone.getName(), client);
			}
		}
		else {
		}
	}

	@SuppressWarnings("unchecked")
	public void op_skilledit(final String input, final Client client) {
		final Player player = getPlayer(client);             // we shouldn't be modifying the player in here
		final EditorData data = player.getEditorData();      // we won't need to make a new instance of EditorData
		final Skill skill = (Skill) data.getObject("skill"); // no need for a new skill, since we should always have a valid one

		String scmd = "";
		String sarg = "";

		if (input.indexOf(" ") != -1) {
			scmd = input.substring(0, input.indexOf(" ")).toLowerCase();
			sarg = input.substring(input.indexOf(" ") + 1, input.length());
		}
		else {
			scmd = input.substring(0, input.length()).toLowerCase();
		}

		debug("SKEDIT CMD");
		debug("srcmd: \"" + scmd + "\"");
		debug("sarg: \"" + sarg + "\"");

		if( scmd.equals("abort") ) {
			// reset editor and player status
			player.setStatus( (String) data.getObject("pstatus") );
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if( scmd.equals("abrv") ) {
			data.setObject("abbrev", sarg);
			send("Ok.", client);
		}
		else if( scmd.equals("abbrevs") ) {
		}
		else if( scmd.equals("class") ) {
			if(sarg != null) {
				String[] args = sarg.split(" ");

				if( args.length > 0 ) {					
					for(String s : args ) {
						final String className = s.substring(1);
						final PClass newClass = Classes.getClass(className);

						if(data.getObject("classes") instanceof List<?>) {
							List<PClass> classes = (List<PClass>) data.getObject("classes");
							
							if( s.charAt(0) == '+' ) { 
								classes.addAll(Utils.mkList(newClass));
								send("Added " + newClass + " to classes for " + (String) data.getObject("name"), client);
							}
							else if( s.charAt(0) == '-') {
								classes.remove(newClass);
								send("Removed " + newClass + " from classes for " + (String) data.getObject("name"), client);
							}
						}
					}
				}
			}
		}
		else if( scmd.equals("classes") ) {
			// send list of valid class names
			send("NONE, BARBARIAN, BARD, CLERIC, DRUID, FIGHTER, MONK, PALADIN, RANGER, ROGUE, SORCERER,"
					+ "WIZARD, ADEPT, ARISTOCRAT, COMMONER, EXPERT, WARRIOR", client);
		}
		else if( scmd.equals("name") ) {
			data.setObject("name", sarg);
			send("Ok.", client);
		}
		else if( scmd.equals("quit") ) {
		}
		else if( scmd.equals("save") ) {
		}
		else if( scmd.equals("show") ) {
			send(Utils.padRight("", '-', 80), client);
			send("   Name: " + (String) data.getObject("name"), client);
			send("     ID: " + skill.getId(), client);
			send("   Stat: " + (Ability) data.getObject("stat"), client);
			send("Classes: " + (List<PClass>) data.getObject("classes"), client);
			send(" Abbrev: " + (String) data.getObject("abbrev"), client);
			send(Utils.padRight("", '-', 80), client);
		}
		else if( scmd.equals("skills") ) {
		}
		else if( scmd.equals("stat") ) {
			// change the stat a skill is associated with
			/* TODO this command should take the short/long form of the
			 * stat name and set that stat as the primary stat for
			 * this skill
			 */
		}
		else if( scmd.equals("stats") ) {
			final Ability[] abilities = rules.getAbilities();
			
			StringBuilder sb = new StringBuilder();
			int count = 0;
			
			for(Ability ab : abilities) {
				sb.append( ab.getName() );
				
				count++;
				
				if( count < abilities.length ) {
					sb.append(", ");
				}
			}
			
			send( "Stats: " + sb.toString(), client );
			//send( "Abilities: " + sb.toString(), client );
		}
	}

	/**
	 * Zone Editor Input Handler
	 * 
	 * @param input
	 * @param client
	 */
	public void op_zoneedit(final String input, final Client client) {
		final Player player = getPlayer(client);             // we shouldn't be modifying the player in here
		final EditorData data = player.getEditorData();      // we won't need to make a new instance of EditorData
		final Zone zone = (Zone) data.getObject("zone");     // no need for a new zone, since we should always have a valid one

		String zcmd = "";
		String zarg = "";

		if (input.indexOf(" ") != -1) {
			zcmd = input.substring(0, input.indexOf(" ")).toLowerCase();
			zarg = input.substring(input.indexOf(" ") + 1, input.length());
		}
		else {
			zcmd = input.substring(0, input.length()).toLowerCase();
		}

		debug("ZEDIT CMD");
		debug("zcmd: \"" + zcmd + "\"");
		debug("zarg: \"" + zarg + "\"");

		if( zcmd.equals("abort") ) {
			// reset editor and player status
			player.setStatus( (String) data.getObject("pstatus") );
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if( zcmd.equals("addroom") ) {
		}
		else if( zcmd.equals("remroom") ) {
		}
		else if( zcmd.equals("setparent") ) {
		}
		else if( zcmd.equals("show") ) {
			send("--- Zone Editor " + Utils.padRight("", '-', 80 - 16), client);
			//send(Utils.padRight("", '-', 80), client);
			send("   Name: " + (String) data.getObject("name"), client);
			send("     ID: " + zone.getId(), client);
			send(Utils.padRight("", '-', 80), client);
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

			client.write("< lines " + top + "-" + bottom + ", " + rem + " lines remaining | 'up'/'down' | 'done' to finish >\r\n");
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
	
	private void handle_registration(final String arg, final Client client) {
	}
	
	private void handle_account_login(final String arg, final Client client) {
		System.out.println("handle_account_login");
		
		Player player = getPlayer(client);

		loginData data = pLoginData.get(client);

		// state indicator: is the server responding to our input or spitting
		// data about what to input next
		// what is our input affecting at the moment
		// we need store transitory data: state, username, password

		if( data != null ) {
			if( data != pLoginData.get(client) ) send("Object in map is not the same as the stored retrieved result???", client);
			
			int state = data.state;
			
			System.out.println("state: " + state);
			System.out.println("username: " + data.username);
			System.out.println("password: " + data.password);

			if( state == Constants.USERNAME ) {
				data.username = arg;
				data.state = Constants.PASSWORD;

				send("Password? ", client);
			}
			else if( state == Constants.PASSWORD ) {
				data.password = arg;
				data.state = Constants.AUTHENTICATE;
				
				handle_account_login("", client);
			}
			else if( state == Constants.AUTHENTICATE ) {
				pLoginData.remove(client);

				// TODO create the account handler
				final Account account1 = acctMgr.getAccount(data.username, data.password);

				if (account1 != null) {
					if( account1.getPlayer() == null || multiplay == 1 ) {
						caTable.put(client, account1);
						
						account_menu( client );
						
						setClientState(client, "account_menu");
					}
				}
				else {
					send("No Such Account!", client);
					setClientState(client, null);
				}
			}
		}
		else {
			if( arg.equals("account") ) {
				pLoginData.put(client, new loginData( Constants.USERNAME ));
				
				send("Username? ", client);
			}
			else {
				send("loginData is NULL", client);
			}
		}
	}
	
	private void handle_account_menu(final String input, final Client client) {
		final Account account = caTable.get(client);

		if( account != null ) {
			int character = Utils.toInt(input, -1);

			if( character != -1 ) {
				Player player = account.getCharacters().get(character);

				if( player != null ) {
					if (mode == GameMode.NORMAL) {
						init_conn(player, client, false); // Open Mode
					}
					else if (mode == GameMode.WIZARD) {
						if ( player.getAccess() == Constants.WIZARD ) {
							init_conn(player, client, false); // Wizard-Only Mode
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
				else {
					send("No such player!", client);
				}
			}
			else {
				// try to find a character by name
				Player player1 = null;

				for(final Player p : account.getCharacters()) {
					if( p.getName().equalsIgnoreCase(input) ) {
						player1 = p;
						break;
					}
				}

				if( player1 != null ) {
					if (mode == GameMode.NORMAL) {
						init_conn(player1, client, false); // Open Mode
					}
					else if (mode == GameMode.WIZARD) {
						if ( player1.getAccess() == Constants.WIZARD ) {
							init_conn(player1, client, false); // Wizard-Only Mode
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
				else {
					send("No such player!", client);

					char ch = input.charAt(0);

					switch(ch) {
					case 'N': // New Character
						send("Testing -- this code only produces a user with the name 'user' and the password 'pass'.");
						send("* this will likely fail if attempted a second time");
						
						String user = "user";
						String pass = "pass";

						// get a name and password

						// create new character / player object
						final Player player = new Player(-1, user, Utils.hash(pass), start_room);

						// handle login stuff
						init_conn(player, client, true);

						// drop the new player into chargen
						cmd_chargen("", client);

						//send("Account Action -> New Character (Not Implemented)", client);
						break;
					case 'L': // Link Character
						//requestInput("Character Name? ", client);
						send("Account Action -> Link Character (Not Implemented)", client);
						break;
					case 'U': // Unlink Characters
						send("Account Action -> Unlink Character (Not Implemented)", client);
						break;
					case 'R': // Reorder Characters
						send("Account Action -> Reorder Characters (Not Implemented)", client);
						break;
					case 'E': // Enter Description
						send("Account Action -> Enter Description (Not Implemented)", client);
						break;
					case 'D': // Delete Character
						send("Account Action -> Delete Character (Not Implemented)", client);
						break;
					case 'C': // Change Password
						send("Account Action -> Change Password (Not Implemented)", client);
						break;
					case 'Q': // Quit
						send("Goodbye.", client);
						setClientState(client, null); // clear client state
						init_disconn(client);         // disconnect client
						break;
					default:  // Don't do anything, return to menu
						break;
					}
				}
			}
		}
		else {
			send("No such account!", client);
		}
	}

	// logged-in player check
	public boolean loginCheck(final Client client) {
		if( client == null ) {
			return false;
		}
		
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
	/*public MUDObject getObject(final String objectName, final Client client) {
		MUDObject object = getExit(objectName);
		if (object != null) return object;

		object = getRoom(objectName);
		if (object != null) return object;

		return getThing(objectName, getRoom( getPlayer(client) ));
	}*/

	/**
	 * get object specified by name
	 * 
	 * @param objectDBRef
	 * @return
	 */
	public MUDObject getObject(final String name) {
		return objectDB.getByName(name);
	}
	
	public MUDObject getObject(final String name, final Room room) {
		List<MUDObject> objects = objectDB.getByRoom( room );
		
		debug(room.getName() + ": " + objects);
		
		for(MUDObject obj : objects) {
			debug("getObject: " + obj.getName());
			if( obj.getName().equals(name) ) {
				return obj;
			}
		}
		
		return null;
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

		return null;
	}
	
	public List<Item> getItems(final String name, final Player player) {
		List<Item> items = new LinkedList<Item>();
		
		for (final Item item : player.getInventory()) {
			if (item.getName().equals(name)) {
				items.add(item);
			}
		}
		
		return items;
	}

	/**
	 * 
	 * @param dbref
	 * @param player
	 * @return
	 */
	public Item getItem(final Integer dbref, final Player player) {
		for (final Item item : player.getInventory()) {
			if (item.getDBRef() == dbref) {
				return item;
			}
		}

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
		return objectDB.getItem(dbref);
	}
	
	// TODO resolve the issue surrounding this, which is that there might be "identical items"
	/*public Item getItem(final String name) {
		return objectDB.getItem(name);
	}*/

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

	public Exit getExit(String exitName, Client client) {
		Room room = getRoom(getPlayer(client));
		Exit exit;

		//ArrayList<Integer> eNums = new ArrayList<Integer>();

		// look through the present room's exits
		for (int e = 0; e < room.getExits().size(); e++) {
			exit = (Exit) room.getExits().get(e);
			if (exit.getName().toLowerCase().equals(exitName)) {
				return exit;
			}
			/*else {
				eNums.add(e);
			}*/
		}

		// look through all the exits (would be great if this could ignore previously searched exits
		// perhaps by dbref (since that's much shorter than holding object references, etc
		/*for (int e = 0; e < exits1.size(); e++) {
			if (!eNums.contains(e)) {
				exit = (Exit) exits1.get(e);

				if (exit.getName().toLowerCase().equals(exitName)) {
					return exit;
				}
			}
		}*/

		return null;
	}
	public Exit getExit(final Integer dbref) {
		return objectDB.getExit(dbref);
	}
	
	public Exit getExit(final Integer dbref, final Client client) {

		// look through the present room's exits first
		for (final Exit e : getRoom(getPlayer(client)).getExits()) {
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
		//debug("Searching for player by client...", 3);
		//debug("\"" + client  + "\"", 3);

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

		/*for (final Player player : players) {
			if (player.getName().equals(name) || player.getCName().equals(name)) {
				//debug(name);
				//debug(player.getName());
				//debug(player.getCName());
				return player;
			}
		}

		return null;*/
		
		return objectDB.getPlayer(name);
	}

	/**
	 * Get a Player (player character) object by it's dbref (Integer).
	 * 
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
	/*public Room getRoom(final Client client)
	{
		Player player = getPlayer(client);

		if (player != null) {
			return getRoom( player.getLocation() );
		}

		return null;
	}*/

	public Room getRoom(Player player) {
		return objectDB.getRoomById( player.getLocation() );
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
		return objectDB.getRoomById( dbref );
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
	public Thing getThing( String name, Room room ) {
		return objectDB.getThing( room.getDBRef(), name );
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

	public Bank getBank(final String bankName) {
		return this.banks.get( bankName );
	}

	/* Saving Objects */

	/*
	 * Persistence Routines
	 */
	
	/* Data Saving Functions */
	
	public void saveAccounts() {
		try {
			for (final Account a : acctMgr.getAccounts()) {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACCOUNT_DIR + a.getUsername() + ".acct"));
				oos.writeObject(a);
				oos.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save Database (calls save method on instance of ObjectDB)
	 */
	public void saveDB() {
		// save databases to disk, modifies 'real' files
		objectDB.save(mainDB);
		send("Done");
	}
	
	public void saveDB(final String filename) {
		objectDB.save(BACKUP_DIR + filename);
	}

	public void saveHelpFiles() {
		synchronized (this.helpTable) {
			for (final Entry<String, String[]> hme : this.helpTable.entrySet()) {
				debug("Saving " + HELP_DIR + hme.getKey() + ".help" + "... ");
				Utils.saveStrings(HELP_DIR + hme.getKey() + ".help", hme.getValue());
			}
		}
	}
	
	public void saveTopicFiles() {
		synchronized(this.topicTable) {
			for(final Entry<String, String[]> tme : this.topicTable.entrySet()) {
				debug("Saving " + TOPIC_DIR + tme.getKey() + ".topic" + "... ");
				Utils.saveStrings(TOPIC_DIR + tme.getKey() + ".topic", tme.getValue());
			}
		}
	}

	/**
	 * Generally speaking the purpose of this would be to save
	 * any changes made to spells and any new spells created
	 * within the game.
	 */
	public void saveSpells() {
		System.out.println("Not Implemented!");
	}

	public void saveSession(final Player player) {
		final Session session = sessionMap.get(player);
		
		// convert session to JSON
		
		// store session
	}
	
	public void saveBoards() {
		for(final BulletinBoard board : boards.values()) {
			saveBoard(board);
		}
	}
	
	public void saveBoard(final BulletinBoard board) {
		File file;
		
		file = new File(BOARD_DIR + board.getName());
		
		RandomAccessFile raf;
	}

	/* Data Loading Functions */
	public void loadAccounts(String account_dir) {
		// TODO find a more effective way to load account data
		final File dir = new File(account_dir);
		
		System.out.println("ACCOUNT_DIR: " + account_dir);
		
		Account account = null;
		
		ObjectInputStream ois;

		if(!dir.isDirectory()) {
			System.out.println("Invalid Account Directory!");
			return;
		}
		
		System.out.println(dir.listFiles());
		
		for (final File file : dir.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".acct")) {
				System.out.println("Account File Found: " + file.getName());

				try {
					ois = new ObjectInputStream(new FileInputStream(file));
					
					account = (Account) ois.readObject();
					
					System.out.println("ID: " + account.getId() );
					System.out.println("Username: " + account.getUsername() );
					
					final int aId = account.getId();
					Account.Status aStatus = account.getStatus();
					
					final Date aCreated = account.getCreated();
					Date aModified = account.getModified();
					
					String aUsername = account.getUsername();
					String aPassword = account.getPassword();
					
					final int aCharLimit = account.getCharLimit();
					
					Account account2 = new Account(aId, aStatus, aCreated, aModified, aUsername, aPassword, aCharLimit, new Player[0] );
					
					// connect players to their respective account
					Player player = getPlayer(account.getUsername());
					
					if( player != null ) account2.linkCharacter(player);
					
					acctMgr.addAccount( account2 );
					
					/*while( ois.available() > 0 ) {
						account = (Account) ois.readObject();
						
						for(Player player : players) {
							// TODO figure out way connect players and the accounts they belong to
						}
						
						acctMgr.addAccount( account );
					}*/
					
					ois.close();
				}
				catch (FileNotFoundException e) {
					System.out.println("--- Stack Trace ---");
					e.printStackTrace();
				}
				catch (ClassNotFoundException e) {
					System.out.println("--- Stack Trace ---");
					e.printStackTrace();
				}
				catch (IOException e) {
					System.out.println("--- Stack Trace ---");
					e.printStackTrace();
				}
			}
		}
		
		/*LinkedList<Integer> ids = new LinkedList<Integer>();
		
		for(Integer i : iamap.keySet()) {
			System.out.println(i);
			ids.add(i);
		}
		
		if( !ids.isEmpty() ) {
			Collections.sort(ids);
			System.out.println(ids);
			last_account_id = ids.getLast();
		}
		else last_account_id = 0;*/
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
				//debug(alias + " -> " + command);
			}
		}
		debug("Aliases loaded.");
		debug("");
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

	/**
	 * Go through all the items that exist in the database
	 * and place them in the respective rooms they are located in
	 */
	public void loadItems() {
		objectDB.addItemsToRooms();
		objectDB.addItemsToContainers();

		for (final Entry<Item, Player> entry : objectDB.getItemsHeld().entrySet()) {

			final Item item = entry.getKey();
			final Player npc = entry.getValue();

			debug(item.getDBRef() + " " + item.getName());

			debug(item.getLocation() + " " + npc.getName(), 2);

			if (npc instanceof NPC) {
				if (npc instanceof Merchant) {
					final Merchant merchant = (Merchant) npc;
					debug("Merchant (" + merchant.getName() + ") " + item.getName(), 2);
					merchant.stock.add(item);
				}
				else {
					debug(npc.getName() + ": Not a merchant", 2);
				}
			}
			else {
				((Player) npc).getInventory().add(item);
			}

			debug("Item Loaded", 2);
		}
	}
	
	public void loadExits() {
		objectDB.loadExits(this);
	}

	public ArrayList<String> loadListDatabase(String filename) {
		String[] string_array;     // create string array
		ArrayList<String> strings; // create arraylist of strings

		string_array = Utils.loadStrings(filename);

		strings = new ArrayList<String>(string_array.length);

		for (int line = 0; line < string_array.length; line++) {
			// if not commented out
			if (string_array[line].charAt(0) != '#') strings.add(string_array[line]);
			// else
			else debug("-- Skip - Line Commented Out --", 2);
		}

		return strings;
	}

	// for the list editor
	public ArrayList<String> loadList(String filename) {
		ArrayList<String> strings = new ArrayList<String>();
		
		for(String line : Utils.loadStrings(filename)) {
			strings.add(line);
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
			// name#cast message#duration#effects#reagents#targets
			//eagles_splendor#You cast Eagle's Splendor#instant#cha+4#none#self,friend
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

				final Spell newSpell = new Spell(tName, School.ENCHANTMENT, tCastMsg, SpellType.ARCANE, tEffects, tReagents);

				final String[] tTargets = args[5].split(",");

				newSpell.target = Spell.encodeTargets(tTargets);

				System.out.println(newSpell.getName() + " " + newSpell.target);

				spells2.put(tName, newSpell);
				debug(tName + " " + tCastMsg + " " + tType + tEffects);
			}
		}
		catch(NullPointerException npe) {
			System.out.println("--- Stack Trace ---");
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

		theme = new Theme();
		String section = "";

		for (final String line : Utils.loadStrings(themeFile)) {
			if ("".equals(line) || line.trim().startsWith("//")) {  // skip blank lines
				continue;
			}			

			if (line.startsWith("[/")) { // end section
				if (!line.substring(2, line.length() - 1).equals(section)) { // if end tag doesn't match current section
					throw new IllegalStateException("Theme section is " + section + " but ending tag is " + line);
				}
				else {
					section = ""; // clear section
					debug("Leaving " + section);
				}
			}
			else if (line.startsWith("[")) { // start section
				section = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
				debug("Entering " + section);
			}
			else if (line.indexOf(" = ") == -1) { // start section
				throw new IllegalStateException("Theme line is not section name, but missing \" = \": " + line);
			}
			else {
				final String[] parts = line.split(" = ", 2);
				if ( section.equals("theme") ) {
					if (parts[0].equals("mud_name") ) {
						theme.setName(parts[1]);
						debug(line);
					}
					else if ( parts[0].equals("motd_file") ) {
						//debug("MOTD File NOT set to " + motd);
						motd = parts[1];
					}
					else if ( parts[0].equals("start_room") ) {
						start_room = Utils.toInt(parts[1], 0);
					}
					else if ( parts[0].equals("world") ) {
						theme.world = parts[1];
					}
				}
				else if ( section.equals("calendar") ) {
					debug(line);
					if ( parts[0].equals("day") ) {
						theme.setDay(Utils.toInt(parts[1], 0));
					}
					else if ( parts[0].equals("month") ) {
						theme.setMonth(Utils.toInt(parts[1], 0));
					}
					else if ( parts[0].equals("year") ) {
						theme.setYear(Utils.toInt(parts[1], 0));
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
				else if (section.equals("months")) {
					final int monthIndex = Utils.toInt(parts[0], -1) - 1;
					MONTH_NAMES[monthIndex] = parts[1];
					debug("Month " + monthIndex + " set to \"" + parts[1] + "\"");
				}
				else if (section.equals("months_alt")) {
				}
				else if (section.equals("holidays")) {
					debug(line, 2);
					// day, month = holiday name/day name
					// dateline is day,month part
					// E.g.: 9,21 = Autumn Equinox
					final String[] dateline = parts[0].split(",");
					holidays.put(Utils.trim(parts[1]), new Date(Integer.parseInt(Utils.trim(dateline[0])), Integer.parseInt(Utils.trim(dateline[1]))));
					// multi-day holidays not handled very well at all, only one day recorded for now
					//holidays.put(new Date(Integer.parseInt(trim(dateline[1])), Integer.parseInt(trim(dateline[0]))), trim(line[1]));
				}
				else if (section.equals("years")) {
					debug(line, 2);
					years.put(Integer.parseInt(Utils.trim(parts[0])), Utils.trim(parts[1]));
				}
			}
		}

		debug("");
		debug("Theme Loaded.");
	}

	public void loadChannels(final String filename) {
		try {
			final FileReader fr = new FileReader(new File(filename));
			final BufferedReader br = new BufferedReader(fr);

			String line;

			while ((line = br.readLine()) != null) {
				String[] channelData = line.split(",");

				final String channelName = channelData[0].toLowerCase();
				final int channel_perm = Utils.toInt(channelData[1], Constants.USER);

				chan.makeChannel(channelName);
				chan.getChatChannel(channelName).setRestrict(channel_perm);

				debug("Channel Added: " + channelName);
			}

			br.close();
			fr.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public void loadSessionData(Player p) {
		
	}
	
	/**
	 * For each npc, every one that is either a WeaponMerchant
	 * or an ArmorMerchant will be stocked with a default set of merchandise
	 * if they have NO stock.
	 */
	public void fillShops() {
		debug("Filling shops with merchandise!");
		for (final NPC npc : objectDB.getNPCs()) {
			// Merchants
			if (npc instanceof Merchant) {
				Merchant m = (Merchant) npc;
				if (m.stock.size() == 0) { // no merchandise
					if( m.getType().equals("armor") ) {
						m.stock = createItems(new Armor(0, 0, ArmorType.CHAIN_MAIL), 10);
					}
					else if( m.getType().equals("weapon") ) {
						m.stock = createItems(new Weapon(0, Handed.ONE, WeaponTypes.LONGSWORD, 15), 10);
					}
					
					//System.out.println("Armor Merchant's (" + m.getName() + ") store has " + m.stock.size() + " items.");
					
					for (final Item item : m.stock) {
						int l = item.getLocation();
						item.setLocation(m.getDBRef());
						System.out.println("Item #" + item.getDBRef() + " had Location #" + l + " and is now at location #" + item.getLocation());
					}
				}
			}
			/*else if (npc instanceof Innkeeper) {
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
			}*/
		}
	}
	
	/**
	 * Go through all the things that exist in the database
	 * and place them in the respective rooms they are located in
	 */
	public void placeThingsInRooms() {
		objectDB.placeThingsInRooms(this);
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
		props = player.getProperties();
		
		// get connection properties from user properties array  
		for (String key : props.keySet()) {
			
			if (key.contains("_connect")) {
				String prop = (String) props.get(key);

				if (prop != null) {
					int initial = prop.indexOf("/");
					String test = prop.substring(initial, prop.indexOf("/", initial));
					System.out.println(test);
					
					if ( test.equals("_connect") ) {
						debug("Connect Property Found!");
						send(pgm.interpret(prop, player), player.getClient());
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
		props = player.getProperties();
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
						send(pgm.interpret(prop, player), player.getClient());
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
		if( player == null ) {
			debug("ERROR!!!: Player Object is NULL!");
		}
		
		// generate generic name for unknown players based on their class and the number of players with the same class presently on
		// logged on of a given class
		System.out.println("Generating generic name for player...");
		
		int temp = numPlayersOnlinePerClass.get( player.getPClass() );
		//int temp = objectDB.getNumPlayers( player.getPClass() );

		player.setCName(player.getPClass().toString() + temp);
		objectDB.addName( player, player.getCName() );
		
		numPlayersOnlinePerClass.put( player.getPClass(), ++temp);
		
		cNames.put( player.getCName(), player );
		
		System.out.println("Generated Name: " + player.getName());

		System.out.println("Done");

		debug("Number of current connected players that share this player's class: " + objectDB.getNumPlayers(player.getPClass()));

		// NOTE: I should probably add a mapping here somewhere that ties the player to their account, if they have one

		if ( newCharacter ) { // if new, do some setup
			// send a welcome mail to them
			//sendMail("System", player, "Welcome", "Welcome to " + getName());
			sendMail("System", player.getName(), "Welcome", "Welcome to " + getName()); // kludge for System message

			// give basic equipment (testing purposes)
			// so far, this means: leather armor, long sword,
			// future? class-based starter equipment?
			
			/*final Armor armor = new Armor("Leather Armor", "A brand new set of leather armor, nice and smooth, but a bit stiff still.", -1, -1, 0, ArmorType.LEATHER);
			final Weapon sword = new Weapon("Long Sword", "A perfectly ordinary longsword.", 0, Handed.ONE, WeaponTypes.LONGSWORD, 15.0);

			objectDB.addAsNew(armor);
			objectDB.addAsNew(sword);

			armor.setLocation(player.getDBRef());
			sword.setLocation(player.getDBRef());

			player.getInventory().add(armor);
			player.getInventory().add(sword);*/

			// starting money?
			//player.setMoney(Coins.platinum(10).add(Coins.gold(50)).add(Coins.silver(50)));
		}

		// get the time
		final Time time = getTime();

		// account login state
		Account account = acctMgr.getAccount(player);

		if( account != null ) {
			account.setLastIPAddress( client.getIPAddress() ); // maybe this should be inside of setClient(...)
			account.setClient(client);
			account.setPlayer(player);
			account.setOnline(true);
		}
		
		player.setClient(client); // need this set so I can ask for it in the call to logConnect
		
		logConnect( player, time );
		
		// open a new session
		Session session = new Session(client, player);
		session.connect = time;
		session.connected = true;

		sessionMap.put(player, session);
		
		/* */
		player.setClient(client);
		sclients.put(client, player);
		
		// tell the player that their connection was successful
		debug("\nConnected!\n");
		//send(Colors.YELLOW + "Connected!" + Colors.WHITE, client);
		send(colors("Connected!", "yellow"), client);
		//send(Colors.YELLOW + "Connected to " + name + " as " + player.getName() + Colors.WHITE, client);
		send(colors("Connected to " + serverName + " as " + player.getName(), "yellow"), client);

		/* load the player's mailbox */
		loadMail(player);

		// indicate to the player how much mail/unread mail they have
		client.writeln("Checking for unread messages...");

		int messages = player.getMailBox().numUnreadMessages();

		if (messages == 0) {
			client.writeln("You have no unread messages.");
		}
		else {
			client.writeln("You have " + String.valueOf(messages) + " unread messages.");
		}
		
		// list the items in the player's inventory (located "in" the player)
		for (final Item item : player.getInventory()) {
			debug("Item -> " + item.getName() + " (#" + item.getDBRef() + ") @" + item.getLocation());
		}
		
		// load the player's inventory

		// go through objects array and put references to objects that are located in/on the player in their inventory
		/*for (final Item item : objectDB.getItemsByLoc(player.getDBRef())) {
			debug("Item -> " + item.getName() + " (#" + item.getDBRef() + ") @" + item.getLocation());
			//player.getInventory().add(item);
			//inventory.add(item);
		}*/

		/*Map<Item, Player> itemsHeld = objectDB.getItemsHeld();

		for(Item item : itemsHeld.keySet()) {
			if(item.getLocation() == player.getDBRef()) {
				debug("Item -> " + item.getName() + " (#" + item.getDBRef() + ") @" + item.getLocation());	
			}
		}*/

		debug("");

		/* ChatChannel Setup */
		try {
			chan.add(player,  OOC_CHANNEL);
		} catch (Exception e) {
			System.out.println("No ooc channel exists!!!");
		}

		// add player to the STAFF ChatChannel (testing), if they are staff
		if (player.getAccess() > Constants.USER) {
			try {
				chan.add(player, STAFF_CHANNEL);
			} catch (Exception e) {
				System.out.println("No staff channel exists!!!");
			}
		}

		/* add the player to the game */
		players.add(player);

		/* create timer lists for the player */
		effectTimers.put(player, new LinkedList<EffectTimer>());
		spellTimers.put(player, new LinkedList<SpellTimer>());
		auctionTimers.put(player, new LinkedList<AuctionTimer>());

		/* run any connect properties specified by the player */
		//cProps(player);

		/* look at the current room */
		final Room current = getRoom(player);   // determine the room they are in
		look(current, client);                  // show the room
		current.addListener(player);            // add them to the listeners group for the room
	}

	/**
	 * De-Initialize Connection (Disconnect)
	 * 
	 * @param player
	 * @param client
	 */
	public void init_disconn(final Client client)
	{
		debug("init_disconn(" + client.getIPAddress() + ")");
		
		// get the player associated with the client
		final Player player = getPlayer(client);
		
		// if such a player does not exist, then just disconnect the client
		if (player == null) {
			debug("Player not found for client: " + client);
			s.disconnect(client);
			return;
		}
		
		final String playerName = player.getName();

		// break any current control of npcs
		cmd_control("#break", client);

		// remove listener
		getRoom(player).removeListener(player);

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
					inventory.add(slot.remove());
				}
			}
		}

		send("Equipment un-equipped!", client);

		// remove from chat channels
		chan.remove(player, STAFF_CHANNEL);
		chan.remove(player, OOC_CHANNEL);
		
		player.setStatus("ZZZ");
		
		// get time
		Time time = getTime();

		// account login state
		Account account = acctMgr.getAccount(player);

		if( account != null ) {
			account.setClient(null);
			account.setPlayer(null);
			account.setOnline(false);
		}
		
		logDisconnect( player, time );

		// get session
		Session toRemove = sessionMap.get(player);

		// record disconnect time
		toRemove.disconnect = time;

		// store the session info on disk

		// clear session
		sessionMap.remove(player);

		// if player is a guest
		if (player.hasFlag(ObjectFlag.GUEST)) {
			// remove from database
			objectDB.remove( player ); // replace db entry with NULLObjet
		}
		else {
			send("Saving mail...", player.getClient());

			// save mail
			saveMail(player);
		}
		
		// cName handling
		int temp = numPlayersOnlinePerClass.get( player.getPClass() );
		//int temp = objectDB.getNumPlayers( player.getPClass() );
		
		numPlayersOnlinePerClass.put( player.getPClass(), temp--);
		
		cNames.remove( player.getCName() );
		
		// synchronized to deal with the possibility that someone invoked 'who' and needs to finish iterating through the list
		synchronized(players) {
			players.remove(player);  // Remove the player object for the disconnecting player
		}

		// DEBUG: Tell us which character was disconnected
		debug(playerName + " removed from play!");
		send("Disconnected from " + serverName + "!", client);
		s.disconnect(client);
	}

	public void telnetNegotiation(Client client) {
		//client.telnet = true; // mark as client as being negotiated with

		int s = 0;  // current sub-negotiation? (0=incomplete,1=complete)

		ArrayList<String> options = new ArrayList<String>();

		//options.add("IAC WILL MCCP");
		options.add("IAC IAC DO TERMINAL-TYPE");

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
		// IAC SB  MCCP1 WONT SE
		// 255 250 85    252  250

		// IAC DO  TERMINAL-TYPE
		// 255 253 24
		// -- if --
		// IAC WILL TERMINAL-TYPE
		// 255 251  24
		// -- then -- 

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
		//client.telnet = true;
	}

	// EVENT Section
	//

	// event triggered on client connection
	public void clientConnected(final Client someClient)
	{		
		send("Connecting from " + someClient.getIPAddress(), someClient);
		// decide if a player (or in this case, IP address) will be allowed to continue connecting
		if (banlist.contains(someClient.getIPAddress())) {
			send("Server> Your IP is banned.", someClient);
			send("Server> Booting client...", someClient);
			someClient.stopRunning();
		}
		
		// check to see if ansi colors are enabled for the server
		if ( color == Constants.ANSI ) {
			someClient.write("\033[;1m"); // tell client to use bright version of ANSI Colors
			send("> Using BRIGHT ANSI colors <", someClient); // indicate the use of bright ansi colors to the client
		}

		// send data about the server
		send(colors(program, "yellow") + colors(" " + version, "yellow") + colors(" -- Running on " + computer, "green"), someClient);
		send(colors(serverName, "red"), someClient);
		send(colors(MOTD(),"cyan"), someClient);

		// reset color
		//send(colors("Color Reset to Default!", "white"));

		// indicate game mode
		send("Mode: " + mode, someClient);
		
		if( int_login ) {
			interactive_login(someClient, "NAME");
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
		try {
			for (final String helpFileName : generateHelpFileIndex())
			{
				String helpLines[] = Utils.loadStrings(HELP_DIR + helpFileName);
				this.helpTable.put(helpLines[0], helpLines);
			}
			//System.out.println("Finished");
		}
		catch(NullPointerException npe) {
			System.out.println("NullPointerException in helpfile loading.");
			System.out.println("--- Stack Trace ---");
			npe.printStackTrace();
		}
	}
	
	/*
	 * 
	 * DO NOT USE SYS_RELOAD()! IT WILL RESULT IN A NON-CLEAN WORLD STATE!
	 * 
	 * NOTE: Among other things, a successful reload will likely cause GC errors
	 * and heap error and other weird stuff to happen.
	 * 
	 * This should probably do some of the things live backup does to reset certain
	 * parts of the server. It would also likely to be wise to remove players from the world
	 * while retaining client <-> player mappings for later restoration. Setting input hold
	 * and stopping/pausing the time loop is recommended. In addition certain post db loading
	 * steps should be done again as well.
	 * 
	 */
	public void sys_reload() {
		// tell us that the database is being loaded (supply custom message?)
		send("Game> Loading Database!");

		// clear database, etc
		objectDB.clear();
		
		// load objects from databases
		ObjectLoader.loadObjects(loadListDatabase(mainDB), this, objectDB, this);

		// tell us that loading is done (supply custom message?)
		send("Game> Done.");
	}
	
	// highly dangerous test function below
	public void sys_reload(final String filename) {
		// tell us that the database is being loaded (supply custom message?)
		send("Game> Loading Database!");

		// clear database, etc
		objectDB.clear();

		// load objects from databases
		ObjectLoader.loadObjects(loadListDatabase(filename), this, objectDB, this);

		// tell us that loading is done (supply custom message?)
		send("Game> Done.");
	}
	
	/**
	 * Backup files, either to the specified file name, or to the default
	 * one.
	 * 
	 * @param filename
	 */
	public void backup(final String filename) {
		// tell us that the database is being backed up (supply custom message?)

		// Accounts
		/*log("Game> Backing up Accounts...");

		synchronized(accounts) {
			saveAccounts();
		}

		log("Done.");*/

		// Database
		log("Game> Backing up Database...");
		
		// NOTE: real file modification occurs here
		if( "".equals(filename) ) {
			saveDB();
		}
		else {
			saveDB(filename);
		}

		log("Done.");

		// Spells
		log("Game> Backing up Spells...");

		//saveSpells();
		
		log("Done.");

		// Help Files
		/*log("Game> Backing up Help Files...");

		saveHelpFiles();
		
		log("Done.");*/
		
		// Topic Files
		/*log("Game> Backing up Topic Files...");
		
		saveTopicFiles();

		log("Done.");*/

		// tell us that backing up is done (supply custom message?)
		log("Database Backup - Done.");
	}

	// non-existent player "flush" function
	public void flush()
	{
		for (final Player player : players) {
			final Player slave = playerControlMap.getSlave(player);
			if( player.getClient() == null ) {
				/*if (slave == null) {
					//players.remove(player);
					//debug("Player removed.");
					return;
				}
				else {
					debug("Player \"idle\", but controlling an npc.");
				}*/
				players.remove(player);
			}
		}
	}

	private void shutdown(int secs) {
		timer.schedule(new TimerTask() { public void run() { shutdown(); } }, secs * 1000);
	}

	private void shutdown() {
		s.write("Server Shutdown!\n");

		mode = GameMode.MAINTENANCE;    // prevent any new connections

		// disconnect any connected clients
		for (final Client client1 : s.getClients()) {
			init_disconn(client1);
		}

		System.out.print("Stopping main game... ");
		// indicate that the MUD server  is no longer running, should cause the main loop to exit
		running = false;
		System.out.println("Done");

		// stop the server (the network server, the part handling sockets)
		System.out.print("Stopping server... ");
		s.stopRunning();
		System.out.println("Done");

		// run the backup (run before closing logs so any backup problems get logged)
		System.out.println("Running backup... ");
		backup("");

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

	public String gameError(String funcName, ErrorCodes errorCode) {
		return gameError(funcName, errorCode.ordinal());
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
	 * wraps any cases of a println and and a server write into one function, also
	 * makes it easy to disable printing to standard out for most debugging and
	 * status messages
	 * 
	 * NOTE: an overloaded version of the function that takes only strings, instead of
	 * any kind of object
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
			for(char c : data.toCharArray()) {
				s.write(c);
			}
			s.write("\r\n");
		}
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
			//int lineLimit = 80;

			/*if ( loginCheck(client) ) {
				lineLimit = getPlayer(client).getLineLimit();
			}*/

			/*// if the data to be sent exceeds the line limit
			if (data.length() > lineLimit) {
				String newData = data.substring(0, lineLimit - 1); // choose a chunk of data that does not exceed the limit

				if (telnet == 0) // no telnet
				{
					client.write(newData + "\r\n");
				}
				else if (telnet == 1 || telnet == 2) {
					// telnet and mud clients
					for (int c = 0; c < data.length(); c++)
					{
						client.write(newData.charAt(c));
					}
					client.write("\r\n");
				}

				send(data.substring(lineLimit - 1, data.length()), client); // recursively call the function with the remaining data

				return;
			}*/


			if (telnet == 0) // no telnet
			{
				client.write(data + "\r\n");
			}
			else if (telnet == 1 || telnet == 2) {
				// telnet and mud clients
				for(char c : data.toCharArray()) {
					client.write(c);
				}
				client.write("\r\n");
			}
		}
		else {
			System.out.println("Error: Client is inactive (maybe disconnected), message not sent");
			System.out.println(data);
		}
	}
	
	private void send(List<String> data, Client client) {
		client.write(data);
	}

	/**
	 * A wrapper function for System.out.println that can be "disabled" by setting an integer.
	 * Used to turn "on"/"off" printing debug messages to the console.
	 * 
	 * Each debug level includes the levels below it
	 * 
	 * e.g.
	 * debug level 3 includes levels 3, 2, 1
	 * debug level 2 includes levels 2, 1
	 * debug level 1 includes levels 1
	 * 
	 * Uses an Object parameter and a call to toString so that I can pass objects to it
	 * 
	 * @param data
	 */
	public void debug(final Object data, final int tDebugLevel)
	{
		if (debug == 1) // debug enabled
		{
			if (debugLevel >= tDebugLevel) { // current debug level is to the specified one
				System.out.println(data);
				if ( logging ) {
					if( debugLog != null ) debugLog.writeln(("" + data).trim()); // convert to string and strip extra whitespace
					else System.out.println("Error: Debug Log Not Open!");
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
	
	public void log(final String string) {
		if( logging ) {
			log.writeln(string);
		}
	}
	
	public void log(final String cmd, final Player player) {
		if( logging ) {
			// get variables to log
			String playerName = player.getName();
			int playerLoc = player.getLocation();

			// log command
			log.writeln(playerName, playerLoc, cmd);
		}
	}
	
	public void logConnect(final Player player, final Time time) {
		// get variables to log
		final String playerName = player.getName();
		final int playerLoc = player.getLocation();
		final Room room = getRoom( playerLoc );
		
		final String loginTime = time.hour() + ":" + time.minute() + ":" + time.second();
		
		debug("-- Login");
		debug("Name: " + playerName);
		debug("Location: " + room.getName() + "(#" + playerLoc + ")");
		debug("Login Time: " + loginTime);
		
		// log their login
		if( logging ) {
			log.writeln(playerName, playerLoc, "Logged in at " + loginTime + " from " + player.getClient().getIPAddress()); // log the login
		}
	}
	
	public void logDisconnect(final Player player, final Time time) {
		// get variables to log
		final String playerName = player.getName();
		final int playerLoc = player.getLocation();
		final Room room = getRoom( playerLoc );
		
		final String logoutTime = time.hour() + ":" + time.minute() + ":" + time.second();
		
		debug("-- Logout");
		debug("Name: " + playerName);
		debug("Location: " + room.getName() + "(#" + playerLoc + ")");
		debug("Logout Time: " + logoutTime);

		// log the disconnect
		if( logging ) {
			log.writeln(playerName, playerLoc, "Logged out at " + logoutTime + " from " + player.getClient().getIPAddress());
		}
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
			output = "It is " + tod.timeOfDay + ", the " + game_time.getMoonPhase() + " is " + tod.bodyLoc + ".";
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
		return ( weight / 16 ); // 16 oz = 1 lb
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
	private Container getGenericStorageContainer(final Player player, final Item item) {
		return null;
	}

	// AI routines
	
	// Line of Sight
	protected void lineOfSight(Point origin, Player target) {
		final Room room = getRoom( target.getLocation() );
		lineOfSight(room, origin, target.getPosition());
	}

	protected void lineOfSight(Room room, Point origin, Point goal) {
		int x_pos = origin.getX(); // get origin X coord
		int y_pos = origin.getY(); // get origin Y coord

		int d_x_pos = goal.getX(); // get goal X coord
		int d_y_pos = goal.getY(); // get goal Y coord
		
		List<MUDObject> objects = objectDB.getByRoom(room);
		
		while (x_pos < d_x_pos) {
			while (y_pos < d_y_pos) {
				for(final MUDObject m : objects) {
					
				}
				// find out if there's anything at that intersection of x and y
				
				// increment y
			}
			
			// increment x
		}
	}


	// Random Movement
	protected void randomMovement() {
		// determine possible moves
		// randomly select among them
	}
	
	public void account_menu(final Client client) {
		Account account = caTable.get(client);
		
		account_menu(account, client);
	}
	
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
			final String divider = "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";

			send(colors(divider, "green"), client);
			
			send(colors(Utils.center("Welcome to Fallout Equestria", 80), "pink2"), client);
			//send("Characters:", client);

			send(colors(divider, "green"), client);

			final List<Player> characters = account.getCharacters();
			
			int n = 0;
			
			// for characters in account
			for(Player p : characters) {
				send(n + ") " + p.getName(), client);
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
		final Player sender = msg.getSender();
		final Player recip = msg.getRecipient();
		final Integer loc = msg.getLocation();
		
		Room room;

		debug( msg.getType().name(), 4 );

		switch(msg.getType()) {
		case BROADCAST:
			for(Player player : players) {
				if( getRoom( player ).getRoomType() == RoomType.OUTSIDE && player.getEditor() == Editors.NONE ) {
					send(msg.getMessage(), player.getClient());
				}
			}
			msg.markSent();
			break;
		case BROADCAST_LOCAL:
			room = getRoom( msg.getLocation() );

			if( room != null ) { // just in case the location somehow isn't a room
				
				// send a message to all of the room's listeners (i.e. those who are online and in the same room)
				for(Player player : room.getListeners()) {
					if( true ) { // placeholder test condition for awareness of surroundings
						final String message = msg.getMessage();

						if( !message.contains( player.getName() ) && !message.contains( player.getCName() ) ) {
							send( message, player.getClient() );
						}
						//send( msg.getMessage(), player.getClient() );
					}
				}
			}

			msg.markSent();
			break;
		case BROADCAST_PLAYER:
			room = getRoom( msg.getLocation() );

			if( room != null ) {
				room.fireEvent( msg.getMessage() );
				
				/*for (final Player bystander : players) {
					if (bystander.getLocation() == msg.getLocation())
					{
						send(msg.getSender().getName() + " says, \"" + msg.getMessage() + "\".", bystander.getClient());
						msg.markSent();
					}
				}*/

				for( final Player bystander : room.getListeners() ) {
					if( msg.getSender() != bystander && !(bystander instanceof NPC) ) {
						
						send(msg.getSender().getName() + " says, \"" + msg.getMessage() + "\".", bystander.getClient());
						msg.markSent();
						
					}
				}
			}

			break;
		case NORMAL:
			// set up by default for "tells"
			String color = null;
			
			// TODO kludging here for color name
			if( msg.getSender() instanceof NPC ) {
				color = getDisplayColor("npc");
			}
			
			if( color != null ) {
				send(colors(msg.getSender().getName(), color) + " tells you, \"" + msg.getMessage() + ".\"", recip.getClient());
			}
			else {
				send(msg.getSender() + " tells you, \"" + msg.getMessage() + ".\"", recip.getClient());
			}
			
			msg.markSent();
			
			debug("addMessage, sent message");
			break;
		case SYSTEM:
			send( msg.getMessage(), msg.getRecipient().getClient() );
			break;
		default:
			debug("Message Type Unknown");
			if( sender != null ) debug("Sender: " + msg.getSender().getName());
			if( recip != null ) debug("Recipient: " + msg.getRecipient().getName());
			debug("Message: " + msg);
			break;
		}

		if( msg.sent() ) debug("sent message", 4);
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
	 * A general purpose method that sends information about the object
	 * being examined to the player for any object.
	 * 
	 * @param m
	 * @param client
	 */
	public void examine(final MUDObject m, final Client client) {
		if ( !(m instanceof NullObject) && m != null) {
			send(colors(m.getName() + "(#" + m.getDBRef() + ")", getDisplayColor(m.type.toString().toLowerCase())), client);
			
			//send("Type: " + ObjectFlag.firstInit(m.getFlags()) + " Flags: " + ObjectFlag.toInitString(m.getFlags()), client);
			send("Type: " + m.type + " Flags: " + ObjectFlag.toInitString(m.getFlags()), client);

			if (m instanceof Exit) {
				send("Exit Type: " + ((Exit) m).getExitType().getName(), client);
				
				if(((Exit) m).getExitType() == ExitType.DOOR) {
					send("Locked: " + ((Door) m).isLocked(), client);
				}
				
				if(m instanceof Portal) send("Portal Type: " + ((Portal) m).getPortalType(), client);
			}
			else if (m instanceof Item) {
				send("Item Type: " + ((Item) m).getItemType().toString(), client);
			}
			else if (m instanceof Player) {
				send("Race: " + ((Player) m).getRace().getName(), client);
				send("Class: " + ((Player) m).getPClass().getName(), client);
			}
			else if (m instanceof Thing) {
				send("Thing Type: " + ((Thing) m).thing_type.toString(), client);
			}
			
			MUDObject owner = getObject( m.getOwner() );
			
			if(owner != null) send("Owner: " + colors(owner.getName(), getDisplayColor(owner.getTypeName().toLowerCase())) + " (#" + owner.getDBRef() + ")", client);			send("Description: " + m.getDesc(), client);
			
			final MUDObject m1 = getObject(m.getLocation());
			
			if( m1 != null) {
				send("Location: " + colors(m1.getName(),getDisplayColor(m1.getTypeName().toLowerCase())) + "(#" + m1.getDBRef() + ")", client);
			}
			else {
				send("Location: null", client);
			}
			
			if (m instanceof Exit) {
				if( ((Exit) m).getExitType() == ExitType.PORTAL ) {
					switch( ((Portal) m).getPortalType() ) {
					case RANDOM:
						send("Destinations: Uncertain", client);
						break;
					case STD:
						send("Destination: " + getRoom(((Portal) m).getDestination()).getName(), client);
						break;
					default:
						break;
					}
				}
				else {
					send("Destination: " + ((Exit) m).getDestination(), client);
				}
			}
			
			send("Coordinates:", client);
			
			Point position = m.getPosition();
			send("X: " + position.getX(), client);
			send("Y: " + position.getY(), client);
			send("Z: " + position.getZ(), client);
			
			//send("Coordinates: (" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")", client);
			
			if (m instanceof Container) {
				send("Contains: ", client);
				for(Item item : ((Container) m).getContents()) {
					send(item.getName(), client);
				}
			}
			
			if (m instanceof Player) {
				Player player = (Player) m;
				// helmet, necklace, armor, cloak, rings, gloves, weapons, belt, boots

				/*debug("RING1: " + player.getSlots().get("ring1").getItem() +
						"\t" + "RING2: " + player.getSlots().get("ring2").getItem());
				debug("RING3: " + player.getSlots().get("ring3").getItem() +
						"\t" + "RING4: " + player.getSlots().get("ring4").getItem());
				debug("RING5: " + player.getSlots().get("ring5").getItem() +
						"\t" + "RING6: " + player.getSlots().get("ring6").getItem());*/

				// TODO fix all of this kludging, this depends far too heavily on certain named slots existing
				for(int i = 1; i < 6; i = i + 2) {
					String color = getDisplayColor("thing");
					String r1 = colors("RING" + i, color);
					String r2 = colors("RING" + (i + 1), color);

					//send("RING" + i + ": " + player.getSlots().get("ring" + i).getItem() + "\t" + "RING" + (i + 1) + ": " + player.getSlots().get("ring" + (i + 1)).getItem(), client);
					if( player.getSlot("ring" + i) != null && player.getSlot("ring" + (i + 1)) != null ) {
						send(r1 + ": " + player.getSlot("ring" + i).getItem() + "\t" + r2 + ": " + player.getSlot("ring" + (i + 1)).getItem(), client);
					}
				}

				for (Slot slot : player.getSlots().values()) {
					String tmp;

					if (slot.getType() == ItemType.CLOTHING) { tmp = slot.getCType().toString(); }
					else { tmp = slot.getType().toString(); }

					tmp = tmp.toUpperCase();

					Item item = slot.getItem();

					if (!tmp.contains("RING")) {
						if (item != null) {
							send(colors(tmp, getDisplayColor("thing"))
									+ " : " + item + " *" + item.getWeight()
									+ "lbs.", client);
						} else {
							send(colors(tmp, getDisplayColor("thing"))
									+ " : null", client);
						}
					}
				}
			}
			
			if(m instanceof Portal) {
				Portal portal = (Portal) m;
			}
		}
		else {
			final String lockState;

			if( ((NullObject) m).isLocked() ) lockState = "Locked";
			else lockState = "unLocked";

			send("-- NullObject -- (#" + m.getDBRef() + ") [" + lockState + "]", client);
		}
	}
	
	public void examine(final Room room, final Client client) {
		send(colors(room.getName() + "(#" + room.getDBRef() + ")", getDisplayColor(room.type.toString().toLowerCase())), client);
		
		//send("Type: " + ObjectFlag.firstInit(m.getFlags()) + " Flags: " + ObjectFlag.toInitString(m.getFlags()), client);
		send("Type: " + room.type + " Flags: " + ObjectFlag.toInitString(room.getFlags()), client);
		
		send("Room Type: " + room.getRoomType().toString(), client);

		send("Description: " + room.getDesc(), client);

		final MUDObject m1 = getObject(room.getLocation());

		if( m1 != null) {
			send("Location: " + m1.getName() + "(#" + m1.getDBRef() + ")", client);
		}
		else {
			send("Location: null", client);
		}

		final Zone zone = room.getZone();

		if( zone != null ) {
			send("Zone: " + zone.getName(), client);
		}
		else send("Zone: null", client);

		send("Sub-Rooms:", client);
		for (final Room room1 : objectDB.getRoomsByLocation(room.getDBRef())) {
			send(room1.getName() + "(#" + room1.getDBRef() + ")", client);
		}
		send("Contents:", client);
		final List<Thing> roomThings = objectDB.getThingsForRoom(room.getDBRef());
		for (final Thing thing : roomThings) {
			send( colors(thing.getName(), "yellow") + "(#" + thing.getDBRef() + ")", client);
		}
		send("Items (contents1):", client);
		final List<Item> roomItems = objectDB.getItemsByLoc(room.getDBRef());
		for (final Item item : roomItems) {
			send( colors(item.getName(), "yellow") + "(#" + item.getDBRef() + ")", client);
		}
		send("Creatures:", client);
		for (final Creature creep : objectDB.getCreaturesByRoom(room.getDBRef())) {
			send( colors(creep.getName(), "cyan") + "(#" + creep.getDBRef() + ")", client );
		}
	}

	/**
	 * Look (MUDObject)
	 * 
	 * Look at any MUDObject (basically anything), this is a stopgap to prevent anything being
	 * unlookable.
	 * 
	 * For players: Looking at a player, should show a description (based on what they're wearing and
	 * what parts of them are visible).
	 * 
	 * NOTE: I shouldn't be able to see the dagger or swords that are hidden under a cloak
	 * 
	 * @param mo
	 * @param client
	 */
	public void look(final MUDObject mo, final Client client) {
		String objectType = mo.type.toString().toLowerCase();
		
		System.out.println("LOOK (" + objectType + ")");
		
		if( getPlayer(client).getAccess() >= Constants.BUILD ) {
			if(!mo.hasFlag(ObjectFlag.SILENT) && !getPlayer(client).hasFlag(ObjectFlag.SILENT)) {
				send(colors(mo.getName() + " (#" + mo.getDBRef() + ")", getDisplayColor(objectType)), client);
			}
			else {
				send(colors(mo.getName(), getDisplayColor(objectType)), client);
			}
		}
		else { send(colors(mo.getName(), getDisplayColor(objectType)), client); }

		//send(mo.getDesc(),  client);
		showDesc(mo.getDesc(),  client);

		if( mo instanceof Player) {
			StringBuilder output = new StringBuilder();

			for (Entry<String, Slot> e : ((Player) mo).getSlots().entrySet()) {
				Slot slot = e.getValue();

				if( slot.isFull() && slot.getType() == ItemType.CLOTHING ) {
					Item item = slot.getItem();
					output.append( item.getName() + ", " );
				}
			}

			send( "Wearing (visible): " + output.toString(), client );
		}
		
		if( mo instanceof Storage<?> ) {
			send("Contents:", client);
			
			Storage<?> storage = (Storage<?>) mo;
			
			for(Item item : storage.getContents()) {
				send(colors(item.getName(), getDisplayColor("item")), client);
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
		Player player = getPlayer(client);
		
		if (player == null) {
			send("Game> Player is NULL? (this should be impossible here, ignoring bugs)", client);
		}

		if (room == null) {
			send("Game> Invalid Room?", client);
			return;
		}
		
		int line_limit = player.getLineLimit(); /* Make the description conform to a column limit */
		
		// TODO make this get it's header data from somewhere else to make it customizable
		if( getPlayer(client).getConfig().get("hud_enabled") ) {
			send(getHeader("--| %r |%s[ %z ]--", room), client);
		}
		else {
			if (!room.getFlags().contains(ObjectFlag.SILENT)) {
				send(colors(room.getName() + " (#" + room.getDBRef() + ")", (String) getDisplayColor("room")), client);
			}
			else {
				send(colors(room.getName(), (String) getDisplayColor("room")), client);
			}
			
			send(Utils.padRight("", '-', line_limit), client);
		}

		/* Start Description */

		send("", client);
		
		TimeOfDay[] night = { TimeOfDay.DUSK, TimeOfDay.MIDNIGHT, TimeOfDay.NIGHT, TimeOfDay.BEFORE_DAWN };
		
		if( room.getRoomType() == RoomType.OUTSIDE && Arrays.asList(night).contains( game_time.getTimeOfDay() ) ) {
			send("It's too dark to be able to see anything.", client);
		}
		else {
			final String description = parse(room.getDesc(), room.timeOfDay);
			
			showDesc(description, line_limit, client);
		}

		send("", client);
		
		/* End Description */

		/* presumably some sort of config would allow you to disable date and time reporting here,
		 * maybe even turn off the weather data
		 */
		if ( room.getRoomType().equals(RoomType.OUTSIDE) && player.getConfig().get("show-weather") ) {
			final Weather weather = room.getWeather();

			//send("*** " + "<weather>: " + parse(room.getWeather().ws.description, room.timeOfDay), client);
			if( weather != null ) {
				//send("*** " + weather.ws.name + ": " + weather.ws.description, client);
				
				send("*** " + colors(weather.ws.name, "purple") + ": " + weather.ws.description, client);
				
				//showDesc("*** " + colors(weather.ws.name, "purple") + ": " + weather.ws.description, client);
				
				send("", client);
			}

			//send(gameTime(), client); // the in-game time of day

			//send("", client);
		}

		//send(gameDate(), client); // the actual date of the in-game year
		//send("", client);
		
		// TODO make this get it's footer data from somewhere else to make it customizable
		if( getPlayer(client).getConfig().get("hud_enabled") ) {
			send(getFooter("--[%S]%s[ %Tam ]--[ %D ]--"), client);
		}
		else send(Utils.padRight("", '-', line_limit), client);
		
		if (room.getThings().size() > 0)
		{	
			StringBuilder sb = new StringBuilder();
			
			for (final Thing thing : room.getThings())
			{
				if (!thing.getFlags().contains(ObjectFlag.DARK)) { // only shown non-Dark things
					if (!room.getFlags().contains(ObjectFlag.SILENT)) {
						//send(colors(thing.getName() + "(#" + thing.getDBRef() + ")", "yellow"), client);
						sb.append(colors(thing.getName() + "(#" + thing.getDBRef() + ")", "yellow") + ", ");
					}
					else {
						//send(colors(thing.getName(), "yellow"), client);
						sb.append(colors(thing.getName(), "yellow") + ", ");
					}
				}
			}
			send( sb.toString().substring(0, sb.length() - 2), client ); // dropping the last two characters clips the ending ", "
		}
		
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
			send(colors("Exits: " + exitNames, getDisplayColor("exit")), client);
		}
		else {
			send(colors("Exits:", getDisplayColor("exit")), client);
		}

		send("Contents:", client);
		
		//StringBuilder sb = new StringBuilder();

		if (room.getItems().size() > 0)
		{
			for (final Item item : room.getItems())
			{
				if (!room.getFlags().contains(ObjectFlag.SILENT)) {
					send(colors(item.getName() + "(#" + item.getDBRef() + ")", "yellow"), client);
					//sb.append(colors(item.getName() + "(#" + item.getDBRef() + ")", "yellow") + ", ");
				}
				else {
					send(colors(item.getName(), "yellow"), client);
					//sb.append(colors(item.getName(), "yellow") + ", ");
				}
			}
		}
		
		//send("Contents: " + sb.toString(), client);

		send("With:", client);

		for (final NPC npc : objectDB.getNPCsByRoom(room.getDBRef())) {
			if (!room.getFlags().contains(ObjectFlag.SILENT)) {
				send(colors("[" + npc.getStatus() + "] "+ npc.getName() + "(#" + npc.getDBRef() + ")", "cyan"), client);
			}
			else {
				send(colors("[" + npc.getStatus() + "] "+ npc.getName(), "cyan"), client);
			}
		}

		for (final Creature creep : objectDB.getCreaturesByRoom(room.getDBRef())) {
			if (!room.getFlags().contains(ObjectFlag.SILENT)) {
				send( colors( creep.getName() + "(#" + creep.getDBRef() + ")", "cyan" ), client );
			}
			else {
				send( colors( creep.getName(), "cyan" ), client );
			}
		}

		//for (final Player player : objectDB.getPlayersByRoom( room.getDBRef() ))
		//for (final Player player : players) // conveniently only the logged-in, online players
		for (final Player player1 : objectDB.getPlayersByRoom( room.getDBRef() ))
		{
			if( !players.contains(player1) ) continue; // temporary kludge to hide offline players
			
			String dcolor = getDisplayColor("player");
			
			if( player1 == player && player1.hasEffect("invisibility") ) {
				send(colors("[" + player1.getStatus() + "] "+ player1.getName() + " (invisible)", dcolor), client);
				continue;
			}
			
			if (player1.getLocation() == room.getDBRef())
			{
				if (!player1.hasEffect("invisibility") || player.hasEffect("see_invisibility")) { // if player is not invisible
					boolean sdesc = false; // short descriptions (true=yes,false=no)
					
					if ( sdesc ) { // if using short descriptions
						send( evaluate( player, player1 ), client );
					}
					else { // otherwise
						if (player.getNames().contains(player1.getName()) || player.getName().equals(player1.getName())) {
							if( player.mount != null ) {
								send(colors("[" + player1.getStatus() + "] "+ player1.getName() + "( riding a " + player.mount.getName() + " )", dcolor), client);
							}
							else {
								send(colors("[" + player1.getStatus() + "] "+ player1.getName(), dcolor), client);
							}
						}
						else {
							
							if( player.mount != null ) {
								send(colors("[" + player1.getStatus() + "] "+ player1.getCName() + "( riding a " + player.mount.getName() + " )", dcolor), client);
							}
							else {
								send(colors("[" + player1.getStatus() + "] "+ player1.getCName(), dcolor), client);
							}
						}
					}
				}
			}
		}

		final ArrayList<Portal> tempPortals = new ArrayList<Portal>(5);

		for (final Portal portal : portals) {
			/*final Point currentPos = current.getPosition();
			final Point portalPos = portal.getPosition();
			
			final boolean playerAtPortal = ( portalPos.getX() == currentPos.getX() && portalPos.getY() == currentPos.getY() );*/
			
			final boolean playerAtPortal = player.getPosition().equals( portal.getPosition() );
			
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
	 * DEBUG: 2
	 * 
	 * @param toParse    the description string to parse
	 * @param CtimeOfDay the current time of day
	 * @return
	 */
	public String parse(final String toParse, final String CtimeOfDay) {
		debug("start desc parsing", 2);

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

			debug("end desc parsing", 2);

			return output;
		}
		else {
			debug("end desc parsing", 2);

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
		if(m instanceof Player) {
			return applyEffect((Player) m, effect);
		}
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
			// remove effect if ! is prefixed to it
			if (effect.getName().contains("!")) {
				// covers dispel case for now
				// will need serious work later
				if ( effect.getName().equals("!any") ) {
					/*
					 * if I mean to use this for different kinds
					 * of dispelling I need a rule
					 */
					player.clearEffects();
					send("All Effects removed!", client);
				}

				String effectName = effect.getName().substring(effect.getName().indexOf("!") + 1, effect.getName().length());
				player.removeEffect(effectName);
				send(effectName + " effect removed!", client);
			}
			else {
				if ( effect.getName().startsWith("heal") ) {
					Integer amount;
					String temp = effect.getName().substring(effect.getName().indexOf("+") + 1, effect.getName().length());

					try {
						amount = Integer.parseInt(temp);
						debug("Amount of Healing: " + amount);
						debug("Hitpoints: " + player.getHP());
						debug("Hitpoints (total): " + player.getTotalHP());

						// if max is 10 and have 10, then no healing
						int diff = player.getTotalHP() - player.getHP();

						if (diff >= amount) {
							player.setHP(amount);
							send("Healing (+" + amount + ") effect applied!\nYou gained " + amount + " hit points.", client);
						}
						else if (diff < amount) {
							player.setHP(diff);
							if (diff < amount) {
								send("Healing " + "(+" + amount + ") effect applied!\nYou gained "  + diff + " hit points.", client);
							}
						}
						/*else if (diff == amount) {
							player.setHP(amount);
							send("Healing (+" + amount + ") effect applied!\nYou gained " + amount + " hit points.", client);
						}*/
					}
					catch( NumberFormatException nfe ) {
						debug(nfe.getMessage()); // send a debug message
						amount = 0;
					}
				}
				else if (effect.getName().startsWith("dam")) {
					Integer damage;
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
				else {
					// detect whether this is a positive or negative afffect
					int firstPlus = effect.getName().indexOf("+");
					int firstMinus = effect.getName().indexOf("-");
					int plusMinus = (firstPlus > 0 && firstPlus <= 3) ? 1 : (firstMinus > 0 && firstMinus <= 3) ? -1 : 0;

					if( plusMinus != 0 ) {
						final String effectName = effect.getName();
						final String test = effectName.substring(0, 3);

						Integer boost;
						String temp = "";

						if( plusMinus == 1 ) temp = effectName.substring(effectName.indexOf('+'), effectName.length());       // positive (plusMinus == 1)
						else if( plusMinus == -1 ) temp = effectName.substring(effectName.indexOf('-'), effectName.length()); // negative (plusMinus == -1)

						try {
							boost = Integer.parseInt(temp);
						}
						catch( NumberFormatException nfe ) {
							debug(nfe.getMessage()); // send a debug message
							boost = 0;
						}
						
						/** TODO: resolve this for results with differing stats **/
						switch(test) {
						case "str":
							debug("Strength Bonus: " + boost);
							player.setAbilityMod(Abilities.STRENGTH, boost);
							send("Strength increased by " + boost + " to " + player.getAbility(Abilities.STRENGTH), client);
							break;
						case "con":
							debug("Constitution Bonus: " + boost);
							player.setAbilityMod(Abilities.CONSTITUTION, boost);
							send("Constitution increased by " + boost + " to " + player.getAbility(Abilities.CONSTITUTION), client);
							break;
						case "dex":
							debug("Dexterity Bonus: " + boost);
							player.setAbilityMod(Abilities.DEXTERITY, boost);
							send("Dexterity increased by " + boost + " to " + player.getAbility(Abilities.DEXTERITY), client);
							break;
						case "int":
							debug("Intelligence Bonus: " + boost);
							player.setAbilityMod(Abilities.INTELLIGENCE, boost);
							send("Intelligence increased by " + boost + " to " + player.getAbility(Abilities.INTELLIGENCE), client);
							break;
						case "cha":
							debug("Charisma Bonus: " + boost);
							player.setAbilityMod(Abilities.CHARISMA, boost);
							send("Charisma increased by " + boost + " to " + player.getAbility(Abilities.CHARISMA), client);
							break;
						case "wis":
							debug("Wisdom Bonus: " + boost);
							player.setAbilityMod(Abilities.WISDOM, boost);
							send("Wisdom increased by " + boost + " to " + player.getAbility(Abilities.WISDOM), client);
							break;
						default:
							break;
						}
					}
					// add effects
					else {
						String effectName = effect.getName();
						if( player.hasEffect( effectName ) ) {
							player.removeEffect( effectName );
						}
						//player.addEffect( effectName );
						player.addEffect( effect );
						send(effectName + " Effect applied to " + player.getName(), client);
						debug("Game> " + "added " + effectName + " to " + player.getName() + ".");
					}
				}
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
		init_disconn(c);
	}

	/**
	 * getTime
	 * 
	 * get a brand new time object that holds the current time
	 * 
	 * NOTES:
	 * - includes hours, minutes, and seconds
	 * - only holds the exact time when called, does not do any counting or do anything else
	 * - this is the "real world" time not the GAME time.
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
	 * @param DC         the DC(difficulty) check you are comparing your skill against
	 * @return           true (succeeded in passing DC), false (failed to pass DC)
	 */
	public boolean skill_check(final Player p, final Skill s, final String diceRoll, final int DC) {
		return skill_check(s, diceRoll, p.getSkill(s), p.getAbility(s.getAbility()), DC);
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
	public boolean skill_check(final Skill s, final String diceRoll, final int skillValue, final int skillMod, final int DC) {
		// ex. 10 skill + 4 mod (via STR) > 25 ?: false (14 < 25)

		final String skillName = s.getName();

		final int skill = skillValue + skillMod;

		debug(skillName + ": " + skill + " [ " + skillValue + "(skill) " + skillMod + "(modifier)" + " ]");

		final int roll = Utils.roll(diceRoll);

		System.out.println(diceRoll + " -> " + roll); // tell us what we rolled

		// Report the result of our rull
		debug("Difficulty Check: " + DC);

		if ( skill + roll >= DC ) {
			debug("Success");
			return true;
		}
		else {
			debug("Failure");
			return false;
		}
	}

	public void broadcastWeather() {
		// old WeatherLoop code, which only ran once per "minute" anyway
		// loop through all the rooms and broadcast weather messages accordingly
		for (final Room room : objectDB.getWeatherRooms()) {
			if( objectDB.getPlayersByRoom(room.getDBRef()).size() == 0 ) continue;
			
			debug(room.getDBRef(), 4);
			String msg = null;

			switch( room.getWeather().ws.name ) {
			case "Clear Skies":
				msg = "The sun shines down brightly from the cloudless blue sky.";
				break;
			case "Cloudy":
				msg = "The cloud layer blots out the sun, leaving the world to be lit by the dim gray light that diffuses through it.";
				break;
			case "Rain":
				msg = "The rain continues to pour down from above.";
				break;
			default:
				break;
			}
			
			if( msg != null ) {
				//broadcast(msg, r);
				addMessage( new Message( msg, room ) );
			}
		}
		// end old WeatherLoop code
	}

	/**
	 * Loop through the players, make sure we are the only one poking at the current one,
	 * then make single increment adjustments to their location
	 * 
	 * NOTE: seems to explode where x != y for the destination
	 */
	public void handleMovement() {
		synchronized(this.moving) { // we don't want anyone to modify this list while we're manipulating things
			for (final Mobile mobile : this.moving) {
				//synchronized(player) {
				// if the player is moving (something else could change this)
				if (mobile.isMoving()) {

					Point position = mobile.getPosition();       // current player position
					Point destination = mobile.getDestination(); // current player destination

					if (position.getX() != destination.getX() && position.getY() != destination.getY()) {
						//Message msg = new Message("Current Location: " + position.getX() + ", " + position.getY(), player);
						//addMessage(msg);

						// move diagonally to reach the destination
						// NOTE: not the best way, but it'll have to til I can implement some kind of pathfinding
						if (position.getX() < destination.getX()) {
							mobile.changePosition(1, 0, 0);
						}
						else if (position.getX() > destination.getX()) {
							mobile.changePosition(-1, 0, 0);
						}

						if (position.getY() < destination.getY()) {
							mobile.changePosition(0, 1, 0);
						}
						else if (position.getY() > destination.getY()) {
							mobile.changePosition(0, -1, 0);
						}

						// tell us about our new location
						//Message msg1 = new Message("New Location: " + position.getX() + ", " + position.getY(), player);
						//addMessage(msg1);
						//Message msg2 = new Message("Destination Location: " + destination.getX() + ", " + destination.getY(), player);
						//addMessage(msg2);

						// tell us if we reached the destination this time
						if (position.getX() == destination.getX() && position.getY() == destination.getY()) {
							mobile.setMoving(false);
							
							// if the mobile is a Player (or NPC) and flys to the ground they will be marked as not flying
							if(mobile instanceof Player) {
								final Player player = (Player) mobile;
								final Race race = player.getRace();
								
								if( race.canFly() ) {
									if( player.isFlying() ) {
										final Point pt = player.getPosition();
										
										if( pt.getZ() == 0 ) {
											player.setFlying(false);
										}
									}
								}
							}
							
							moving.remove(mobile);
							//msg = new Message("You have reached your destination", player);
							//addMessage(msg);
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
	public void push(final Thing thing, final Client client) {
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
		String[] args = arg.split("=");

		if( args.length >= 2 ) {

			Command command = null;

			ClassLoader cl = ClassLoader.getSystemClassLoader();

			try {
				Class<?> newClass = cl.loadClass("mud.commands." + args[1]);

				Class<? extends Command> c2 = (Class<? extends Command>) newClass;

				Object c2Object;

				try {
					c2Object = c2.newInstance();

					command = Command.class.cast(c2Object);
					commandMap.put(args[0], command);

					return true;
				}
				catch (InstantiationException e) {
					e.printStackTrace();
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				//send("(Success) Specified Class is of the type Command or a sub-class of it!", client);
				//send("(Error) Specified Class is not of the type Command or a sub-class of it!", client);
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			return false;
		}

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
	
	/**
	 * gives you a reference to the player list from which you cannot
	 * remove or add players, or clear the list
	 */
	public ArrayList<Player> getPlayers() {
		return (ArrayList<Player>) Collections.unmodifiableList(this.players);
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
	 * based on the calling player's ability to "observe"
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
		player.setAccess(Utils.toInt(attr[8], Constants.USER));

		/* Set Player Race */
		try {
			raceNum = Integer.parseInt(attr[9]);
			player.setRace(Races.getRace(raceNum));
		}
		catch(NumberFormatException nfe) {
			System.out.println("--- Stack Trace ---");
			nfe.printStackTrace();	
		}
		finally { player.setRace(Races.NONE); }

		/* Set Player Class */
		try {
			classNum = Integer.parseInt(attr[10]);
			player.setPClass(Classes.getClass(classNum));
		}
		catch(NumberFormatException nfe) {
			System.out.println("--- Stack Trace ---");
			nfe.printStackTrace();
		}
		finally { player.setPClass(Classes.NONE); }

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
		final Room room = new Room(-1, roomName, EnumSet.of(ObjectFlag.SILENT), "You see nothing.", roomParent);

		/* for use of room editor */
		return room;
	}

	/**
	 * create a new basic, untyped Item for us to modify and work on
	 * 
	 * @return
	 */
	private Item createItem() {
		final Item item = new Item();

		item.setFlags(EnumSet.noneOf(ObjectFlag.class));
		item.setLocation(Constants.VOID);

		item.setItemType(ItemType.NONE);

		objectDB.addAsNew(item);
		objectDB.addItem(item);

		((Room) objectDB.get(item.getLocation())).addItem(item);

		return item;
	}
	
	public Item createItem( ItemType type ) {
		return null;
	}
	
	public Item createItem( String prototype ) {
		return createItem( prototype, true );
	}
	
	public Item createItem( String prototype, boolean init ) {
		final Item template = prototypes.get( prototype );
		
		if( template != null ) {
			//final Item newItem = new Item( template );
			final Item newItem = template.clone();
			
			newItem.setFlags(EnumSet.noneOf(ObjectFlag.class));
			
			if( init ) {
				objectDB.addAsNew(newItem);
				objectDB.addItem(newItem);
			}
			
			return newItem;
		}
		
		return null;
	}
	
	public Item createItem(Item template) {
		if( !template.isUnique() ) {
			//Item newItem = new Item();
			//return null;
		}
		
		return null;
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
		if( !template.isUnique() ) {
			final Item item = createItems(template, 1).get(0);
			item.setLocation(Constants.WELCOME_ROOM);
			return item;
		}
		return null;
	}

	private Item createItem(Book template) {
		if( !template.isUnique() ) {
			final Item item = createItems(template, 1).get(0);
			item.setLocation(Constants.WELCOME_ROOM);
			return item;
		}

		return null;
	}

	private Item createItem(Armor template) {
		if( !template.isUnique() ) {
			final Item item = createItems(template, 1).get(0);
			item.setLocation(Constants.WELCOME_ROOM);
			return item;
		}

		return null;
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
			final Weapon item = template.clone();
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
			final Armor armor = template.clone();
			items.add(armor);
			initCreatedItem(armor);
		}

		return items;
	}

	private void initCreatedItem(final Item item) {
		objectDB.addAsNew(item);
		//item.setLocation(0);
		objectDB.addItem(item);
	}

	private void initCreatedThing(final Thing thing) {
		objectDB.addAsNew(thing);
		//thing.setLocation(0);
		objectDB.addThing(thing);
	}
	
	private void initCreatedNPC(final NPC npc) {
		objectDB.addAsNew(npc);
		
		objectDB.addNPC(npc);
	}

	public NPC createNPC(String name, int location) {
		NPC npc = new NPC(name);
		npc.setLocation(location);
		
		initCreatedNPC( npc );
		
		return npc;
	}
	
	/*
	 * private Item createItem() {
		final Item item = new Item();

		item.setFlags(EnumSet.noneOf(ObjectFlag.class));
		item.setLocation(Constants.VOID);

		item.setItemType(ItemType.NONE);

		objectDB.addAsNew(item);
		objectDB.addItem(item);

		((Room) objectDB.get(item.getLocation())).addItem(item);

		return item;
	}
	 */

	/*public Creature createCreature(String race, String name, String desc) {
		Creature creature = new Creature(getNextDB(), race, name, desc);
		creatures.add(creature);
		return creature;
	}*/
	
	public Creature createCreature() {
		final Creature cre = new Creature();

		cre.setFlags(EnumSet.noneOf(ObjectFlag.class));
		cre.setLocation(Constants.WELCOME_ROOM);
		
		cre.setCreatureType(CreatureType.NONE);
		
		objectDB.addAsNew( cre );
		objectDB.addCreature( cre );

		//((Room) objectDB.get(item.getLocation())).addItem(item);

		return cre;
	}

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
	 */
	/*public ArrayList<Exit> filter(ArrayList<Exit> exits, Filter...filters) {
		ArrayList<Exit> results = new ArrayList<Exit>();
		
		results.addAll( exits );
		
		for(Filter f : filters) {
			f.filter(results);
		}
		
		return null;
	}*/
	
	// What is this for?
	public List<MUDObject> filterByLocation(List<MUDObject> objects, String filter) {
		List<MUDObject> list = new LinkedList<MUDObject>();
		
		for(final MUDObject m : objects) {
		}
		
		return list;
	}

	/**
	 * Run a weather update.
	 * 
	 * This should go through the rooms that "need" updating and cause
	 * them to proceed from the current weather state to a new weather
	 * state based on probability.
	 */
	public void updateWeather() {
		if( !weather ) return;
		
		for (final Room room : objectDB.getWeatherRooms()) {

			room.getWeather().nextState();

			final WeatherState ws = room.getWeather().ws;
			
			if (ws.upDown != 1 && ws.upDown != -1) {
				return;
			}

			String changeText = ws.upDown == 1 ? ws.transUpText : ws.transDownText;
			
			if (changeText != null) {
				addMessage( new Message(changeText, room) );
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
		//System.out.println("Help File? " + name)
		
		if( helpTable.containsKey(name) ) {
			//System.out.println("Help File Exists!");
			return helpTable.get(name);
		}
		else {
			final String temp = aliases.get(name);
			
			if( temp != null ) {
				if( helpTable.containsKey(aliases.get(name)) ) {
					//System.out.println("Help File Exists!");
					return helpTable.get(aliases.get(name));
				}
				else return null;
			}
			else return null;
		}
	}

	public String[] getTopicFile(String name) {
		return topicTable.containsKey(name) ? topicTable.get(name) : null;
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
		String temp;
		
		boolean nl_begin = false;
		boolean nl_middle = false;
		boolean nl_end = false;
		
		// TODO fix checking and handling of a newline marker (maybe make it #n or #nl/$n or $nl)
		for (final String word : description.split(" ")) { //^[a-zA-Z]$ ^\\b$
			// TODO find a way to make sure this usually doesn't show up in debug
			//debug("result: " + result, 4);
			//debug("result (length): " + result.length(), 4);
			//debug("next: " + word, 4);
			//debug("next (length): " + word.length(), 4);
			
			// newline check
			nl_begin = word.startsWith("$n");
			nl_middle = word.contains("$n");
			nl_end = word.endsWith("$n");

			//debug("Newline? " + nl_middle, 4);
			
			// need to integrate the following three cases into the three primary cases somehow
			// converting the array above into a list so that I break words containing \n up
			// and insert the other part before the following word might help
			
			if(word.startsWith("$n")) {
				//debug("send", 4);
				send(result, client);                        // send the current contents of the buffer 
				result.delete(0, result.length());           // clear the buffer
				temp = word.substring( word.indexOf("$n") );
				result.append(temp);                         // append the word after the newline
			}
			
			if(word.endsWith("$n")) {
				temp = word.substring(0, word.indexOf("$n"));
				result.append(" ").append( temp );
				//debug("send", 4);
				send(result, client);
				result.delete(0, result.length());
				continue;
			}
			
			// append the first part (before the \n)
			if(word.contains("$n")) {
				temp = word.substring(0, word.indexOf("$n"));
				result.append(" ").append(temp);
				//debug("send", 4);
				send(result, client);
				result.delete(0, result.length());
				result.append(word.substring(word.indexOf("$n") + 1, word.length()));
				continue;
			}

			if (result.length() < 1) { // append current word if empty
				result.append(word);
			}
			else if (result.length() + word.length() + 1 < line_limit) { // append current word if it won't overflow
				//debug("add", 4);
				result.append(" ").append(word);
			}
			/*// interesting idea, but not very good results
			else if (result.length() + word.length() + 1 > line_limit) {
				// split the word so it fits
				int max = line_limit - result.length() - 1;
				debug("add", 4);
				if( max < word.length() ) {
					result.append(" ").append(word.substring(0, max - 1)).append("-");
				}
			}*/
			else { // if it will overflow, send and clear, and append current word
				//debug("send", 4);
				send(result, client);
				result.delete(0, result.length());
				result.append(word);
			}
		}

		// make sure we send the last word if there was only one left
		if( result.length() > 0 ) {
			//debug("send", 4);
			send(result, client);
			result.delete(0, result.length());
		}
	}
	
	// Use Methods

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
		final Room room = getRoom(player);
		final int portalOrigin = portal.getOrigin();
		final int portalDest = portal.getDestination();
		
		final boolean playerAtPortal = player.getPosition().equals( portal.getPosition() );
		
		final boolean missingRequiredKey = portal.requiresKey() && !portal.hasKey( player );

		boolean success = false;

		System.out.println("Portal: " + portal.getName());

		debug("Old Location: " + player.getLocation());

		// if the portal is keyed and is some kind of thing/item, then I need to check before permitting use
		if (!playerAtPortal || missingRequiredKey) {
			return;
		}

		if (portalOrigin == room.getDBRef()) {
			player.setLocation(portalDest);

			debug("Portal( " + player.getName() + ", " + portalOrigin + ", " + portalDest + " ): success");

			success = true;
		}
		else if (portalDest == room.getDBRef()) {
			player.setLocation(portalOrigin);

			debug("Portal( " + player.getName() + ", " + portalDest + ", " + portalOrigin + " ): success");

			success = true;
		}

		if( success ) {
			send("You use the portal.");
			// tell us about the new location
			int location = getPlayer(client).getLocation();
			debug("New Location: " + location);

			Room room1 = getRoom(location);
			look(room1, client);
		}
		else {
			send("This portal isn't active?", client);
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
			send("You use your Wand of " + wand.spell.getName() + " to cast " + wand.spell.getName() + " on yourself.", client);

			debug("Game> Casting..." + wand.spell.getName());

			try {
				//cmd_cast(wand.spell.name, client);
				//getCommand("cast").execute(wand.spell.name, client);
				cmd("cast " + wand.spell.getName(), client);
			}
			catch(Exception e) {
				System.out.println("--- Stack Trace ---");
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
			send("You use a Potion of " + potion.getSpell().getName() + " on yourself.", client);
			player.setTarget(player); // target yourself
			cmd_cast(potion.getSpell().getName(), client);
		}
		else if (potion.getEffect() != null) {
			send("You use a Potion of " + potion.getEffect().getName() + " on yourself.", client);
			applyEffect(player, potion.getEffect());
		}

		// destroy the "used" item
		player.getInventory().remove(potion); // remove from player inventory
		objectDB.remove(potion);              // remove from "live" database (replace with NullObject)
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
	public boolean checkAccess(final Player player, final int accessLevel) {
		int check = Constants.USER;

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
		player.setEditor(Editors.NONE);
		player.setEditorData(null);

		// tell us what went wrong
		send(errorMessage, client);
	}

	/**
	 * Load an area into the game from a text file
	 * 
	 * NOTE: really more like creating an area, since the rooms will exist next time round and we won't run this on the same area twice?
	 */
	/*public void loadArea(final String filename) {
		final String[] file = Utils.loadStrings(filename);

		int step = 0; // 0=AREA, 1=ROOM

		Area area = new Area();

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
					area.setName(value);
				}
				else if ( key.equals("registered") ) {
					if (Utils.toInt(value, -1) == 1) { // is this area "registered"
						step = 1;
					}
				}
				else if ( key.equals("rooms") ) {
					area.setSize( Utils.toInt(value, 0) );
					if( area.getSize() > 0 ) step = 1;
				}
				break;
			case 1: // room
				//basically, for a brand new area, we just create each room as it's specification popsup

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

				area.addRoom(room);
				break;
			default:
				break;
			}
		}
	}*/

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
	else if ( cmd.equals("@create_creature") ) {
		String[] args = arg.split("=");
		createCreature(args[0], args[1], args[2]);
	}
	 */

	public boolean isRunning() {
		return this.running;
	}

	/**
	 * Takes an input string and generates a new one where each letter is prefixed by
	 * the ansi code for the colors of the rainbows in order from Red to Violet (ROYGBIV).
	 * The colors are repeated until we've run out of the input string. The whole thing
	 * is then capped off with the white color code as a sort of reset.
	 * 
	 * NOTE: since normal ansi colors don't cover the whole rainbow, a few have been omitted
	 */
	public String rainbow(final String input) {
		// red, orange, yellow, green blue, indigo, violet
		String[] rainbow = null;
		
		if( color == Constants.ANSI ) {
			rainbow = new String[] { "red", "yellow", "green", "blue" };
		}
		else if( color == Constants.XTERM ) {
			rainbow = new String[]{ "red" ,"orange", "yellow", "green", "blue", "purple" };
		}
		
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

	public void displayContainer(Container c, Client client) {
		String top = c.getTop(), side = c.getSide(), bottom = c.getBottom();
		int displayWidth = c.getDisplayWidth();

		String head = c.getName() + "(#" + c.getDBRef() + ")";

		send(side + top + side + Utils.padRight("", ' ', 39) + side, client);

		//send(side + Utils.padRight(colors(head, "yellow"), ' ', 30 + 10) + side + Utils.padRight("", ' ', 38) + side, client)
		send(side + colors(Utils.padRight(head, ' ', 30), "yellow") + side + Utils.padRight("", ' ', 39) + side, client);

		send(side + top + side + Utils.padRight("", ' ', 39) + side, client);

		for (final Item item : c.getContents()) { 
			send(side + colors(Utils.padRight(item.getName(), ' ', 30), "yellow") + side + Utils.padRight("", ' ', 39) + side, client);
		}

		send(side + bottom + side + Utils.padRight("", ' ', 39) + side, client);
	}

	/*public Account getAccount(Player player) {
		for (final Account account : accounts) {
			if( account.getCharacters().contains(player) ) {
				return account;
			}
		}

		return null;
	}

	public Account getAccount(final String name, final String pass) {
		for (final Account account : accounts) {
			if (account.getUsername().equals(name) && account.getPassword().equals(pass)) {
				return account;
			}
		}

		return null;
	}*/
	
	/* Time Methods */
	
	public void onSecondIncrement() {
		for(final Player p : players) {
			if( p.isIdle() ) p.idle++;
		}
	}
	
	/**
	 * anything that needs to execute once per in-game minute should go here
	 */
	public void onMinuteIncrement() {
		int hour = game_time.getHours();
		int minute = game_time.getMinutes();

		debug("Time loop: " + hour + ":" + minute);

		try {
			handleMovement();
		}
		catch(ConcurrentModificationException cme) {
			cme.printStackTrace();
			debug(cme.getMessage());
		}

		int weather_update_interval = game_time.getWeatherUpdateInterval();

		if( (minute % weather_update_interval) == 0 ) {
			broadcastWeather();
		}
	}

	/**
	 * anything that needs to execute once per in-game hour should go here
	 */
	public void onHourIncrement() {
		fillShops();
		updateWeather();
	}

	/**
	 * anything that needs to execute once per in-game day should go here
	 */
	public void onDayIncrement() {
		//live_backup();
	}

	public void live_backup() {
		send("Pausing game!");

		// Pause all combat
		input_hold = true;           // Halt Player Input
		game_time.pauseLoop();       // Pause the time tracking
		
		GameMode old_mode = mode;
		mode = GameMode.MAINTENANCE; // Put the game into Maintenance mode (no new logins, except Wizards)
		send("Entering " + mode.toString() + " Mode.");

		send("Backing up game...");

		backup("");

		send("Finished backing up");

		send("Unpausing game!");

		game_time.unpauseLoop();    // Resume tracking time
		input_hold = false;         // Resume Player Input
		// Unpause all combat

		mode = old_mode;
		send("Entering " + mode.toString() + " Mode.");
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

		Character ch = null; // the character we'll pull out of the input stringbuffer
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

					if( index == sb.length() - 1) {
						end = index;
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
				String reference = refString.toString();

				debug("");
				debug("Game> (argument eval) reference: " + reference);
				
				// try to get number from nameref table
				if( client != null ) {
					if( getPlayer(client).getConfig().get("global-nameref-table") ) {
						refNum = getNameRef(reference);
					}
					else { refNum = getPlayer(client).getNameRef(reference); }
				}
				else {
					refNum = getNameRef(reference);
				}

				debug("refNum: " + refNum);
				debug("DB size: " + objectDB.getSize());

				// modify string, if we got a valid reference (i.e. could be in database, a NULLObject is a valid reference
				if(refNum != null && refNum < objectDB.getSize()) {
					debug("Game> (argument eval) tempI: " + refNum); // report number

					debug("");
					debug("Begin: " + begin + " End: " + end + " Original: " + sb.substring(begin, end + 1) + " Replacement: " + refNum.toString());
					debug("");

					sb.replace(begin, end + 1, refNum.toString());

					debug("BUFFER: " + sb.toString());
					debug("");
				}
				else { // modify string to remove potential name reference if not valid
					sb.replace(begin, end + 1, "");
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
	 * Gets and returns the name of the 
	 * program, which is a static constant.
	 * 
	 * @return
	 */
	public static String getName() {
		return program;
	}

	public static String getVersion() {
		return version;
	}

	public String getServerName() {
		return serverName;
	}

	public String[] generateHelpFileIndex() {
		// Directory path here
		String path = HELP_DIR;

		List<String> fileList = new ArrayList<String>();

		System.out.println("Help File Index");
		System.out.println("----------------------------------------");

		for( File file : Arrays.asList( new File(path).listFiles() ) ) {
			if( file.isFile() ) {
				String filename = file.getName();

				if( filename.endsWith(".help") || filename.endsWith(".HELP") )
				{
					//System.out.println( filename );
					fileList.add( filename );
				}
			}
		}

		return Utils.listToStringArray(fileList);
	}

	public String[] generateTopicFileIndex() {
		// Directory path here
		String path = TOPIC_DIR;

		List<String> fileList = new ArrayList<String>();

		System.out.println("Topic File Index");
		System.out.println("----------------------------------------");

		for( File file : Arrays.asList( new File(path).listFiles() ) ) {
			if( file.isFile() ) {
				String filename = file.getName();

				if( filename.endsWith(".topic") || filename.endsWith(".TOPIC") )
				{
					//System.out.println( filename );
					fileList.add( filename );
				}
			}
		}

		return Utils.listToStringArray(fileList);
	}

	public ArrayList<MUDObject> findVisibleObjects(Room room) {
		ArrayList<MUDObject> objectsFound = new ArrayList<MUDObject>();

		for( Thing thing : room.getThings()) {
			if( !thing.hasFlag(ObjectFlag.DARK) ) {
				objectsFound.add(thing);
			}
		}

		for( Item item : room.getItems() ) {
			debug(item.getName());
			/*if( !item.hasFlag(ObjectFlag.DARK) ) {
				objectsFound.add(item);
			}*/
			objectsFound.add(item);
		}

		// are Players and NPCs really objects for this consideration?
		/*for( Player player : objectDB.getPlayersByRoom(room.getDBRef()) ) {
			if( !player.hasFlag(ObjectFlag.DARK) ) {
				objectsFound.add(player);
			}
		}

		for( NPC npc : objectDB.getNPCsByRoom(room.getDBRef()) ) {
			if( !npc.hasFlag(ObjectFlag.DARK) ) {
				objectsFound.add(npc);
			}
		}*/

		return objectsFound;
	}

	/**
	 * Load a player's mail file into the mailbox structure.
	 * 
	 * This consists of dividing the file into "messages", creating
	 * message objects and putting them in a "mailbox".
	 * 
	 * NOTE: Should we return a mailbox to point the player's mailbox reference
	 * to or continue as is, where we just modify the player's mailbox.
	 * 
	 * @param player the player who's mail file we wish to load
	 */
	private void loadMail(Player player) {
		int msg = 0;
		
		String mailBox = WORLD_DIR + theme.world + "\\" + "mail\\mail-" + player.getName() + ".txt";
		String lines[] = null;
		
		/*
		 * check for existence of mail file, and abort if it doesn't exist
		 * (yet? no file for brand new players until save)
		 */
		File file = new File(mailBox);
		
		if( !file.exists() ) {
			debug("No such mail file!");
			return;
		}
		
		lines = Utils.loadStrings(mailBox); // load the file into a string array
		
		if( lines == null ) {
			debug("Could not find mail file for user: " + player.getName());
			send("Could not find mail file for user: " + player.getName(), player.getClient());
			return;
		}

		MailBox mb = player.getMailBox();
		
		final int SENDER = 1;
		final int RECIP = 2;
		final int SUBJECT = 3;
		final int MSG = 4;
		final int FLAG = 5;
		final int MARK = 6;
		final int END = 7;
		
		String sender = "";
		String recipient = "";
		String subject = "";
		String message = "";
		char flag = ' ';
		char mark = ' ';
		
		int part = SENDER;
		
		System.out.println("MSG: " + msg);
		
		for(String line : lines) {
			System.out.println("Line: " + line);
			
			switch(part) {
			case SENDER:  sender = line;         part = RECIP;   break;
			case RECIP:   recipient = line;      part = SUBJECT; break;
			case SUBJECT: subject = line;        part = MSG;     break;
			case MSG:     message = line;        part = FLAG;    break;
			case FLAG:    flag = line.charAt(0); part = MARK;    break;
			case MARK:    mark = line.charAt(0); part = END;     break;
			default:      break;
			}

			if( part == END ) {
				if( !sender.equals("") && !recipient.equals("") && !subject.equals("") && !message.equals("") && flag != ' ' && mark == '~') {
					// create mail object
					Mail mail = new Mail(msg, sender, recipient, subject, message, flag);

					// add the mail object to the mailbox
					mb.add(mail);
					
					msg++;
				}
				else {
					// Invalid Message ...
					debug("Invalid Message!");
					
					break; // if we found an invalid message we can't depend on the rest of the file to be intact
				}
				
				sender = ""; recipient = ""; subject = ""; message = ""; flag = ' '; mark = ' ';
				
				part = SENDER;
			}
		}
	}
	
	/**
	 * saveMail
	 * 
	 * @param player
	 */
	private void saveMail(Player player) {
		MailBox mb = player.getMailBox();

		send("Saving mail...", player.getClient());

		try {
			PrintWriter pw = new PrintWriter(WORLD_DIR + theme.world + "\\mail\\mail-" + player.getName() + ".txt");

			for (Mail mail : mb) {
				// Recipient
				pw.println(mail.getRecipient());
				// Subject
				pw.println(mail.getSubject());
				// Message
				pw.println(mail.getMessage());
				// Flag (Read/Unread)
				pw.println(mail.getFlag());
				// Mark
				pw.println('~');
			}

			pw.flush();
			pw.close();
		}
		catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	}

	public Item compare(Item item1, Item item2) {
		// comparisons:

		// item type -- if not same type, return null as they are not comparable?
		//if( item1.getItemType() == item2.getItemType() )	

		// item value -- compare value in terms of money, copper pieces specifically
		if( item1.getValue().numOfCopper() > item2.getValue().numOfCopper() ) {
			// item wear
			if( ( item1.getDurability() - item1.getWear() ) <  ( item2.getDurability() - item2.getWear() ) ) {
				return item1;
			}
			else {
				return item2;
			}
		}
		else if( item1.getValue().numOfCopper() == item2.getValue().numOfCopper() ) {
			return item1;
		}
		else { return item2; }
	}

	public Item compare(Item item1, Item item2, Object...criteria) {
		// comparisons:
		// item type
		// item wear
		// item value
		return null;
	}

	private void execTrigger(Trigger trig, Client client) {
		String script = trig.getScript().getText();
		
		final int numLeftBrace = Utils.countNumOfChar(script, '{');
		final int numRightBrace = Utils.countNumOfChar(script, '}');
		
		if( numLeftBrace != 0 && numRightBrace != 0 && numLeftBrace == numRightBrace ) {
			send(pgm.interpret( trig.getScript().getText(), getPlayer(client)), client);
		}
		else { send(trig.exec(), client); }
	}

	// Timers

	public List<SpellTimer> getSpellTimers(Player player) {
		return spellTimers.get(player);
	}

	public List<EffectTimer> getEffectTimers(Player player) {
		return effectTimers.get(player);
	}

	public List<AuctionTimer> getAuctionTimers(Player player) {
		return auctionTimers.get(player);
	}

	/**
	 * check for expired timers and clear them (player)
	 *  
	 * @param player
	 */
	public void checkTimers(Player player) {
		List<EffectTimer> etl = getEffectTimers(player);

		List<EffectTimer> eff_timers = new LinkedList<EffectTimer>();

		for(EffectTimer etimer : etl) {
			if( etimer.getTimeRemaining() <= 0 && !etimer.getEffect().isPermanent() ) {
				eff_timers.add(etimer);
			}
		}

		for(EffectTimer etimer : eff_timers) {
			player.removeEffect(etimer.getEffect().getName());
			send(etimer.getEffect().getName() + " effect removed.", player.getClient());
			etl.remove(etimer);
		}

		eff_timers.clear();
	}

	/**
	 * check for expired timers and clear them
	 */
	public void checkTimers() {
		// Effect Timers (when they expire, effects are removed)
		List<EffectTimer> eff_timers = new LinkedList<EffectTimer>();
		
		// each player has their own list of effect timers
		for(Entry<Player, List<EffectTimer>> entry : effectTimers.entrySet()) {
			Player player = entry.getKey();
			List<EffectTimer> etl = entry.getValue(); // player's list of effect timers
			
			// add to eff the timers that are expired
			for(EffectTimer etimer : etl) {
				if( etimer.getTimeRemaining() <= 0 ) {
					eff_timers.add(etimer);
				}
			}
			
			// remove each timer in eff from the player's list of effect timers
			for(EffectTimer etimer : eff_timers) {
				player.removeEffect(etimer.getEffect().getName());
				
				String effectName = etimer.getEffect().getName();
				char[] temp = effectName.toCharArray();
				Character.toUpperCase(temp[0]);
				effectName = String.copyValueOf(temp);
				
				send(etimer.getEffect().getName() + " effect removed.", player.getClient());
				etl.remove(etimer);
			}

			eff_timers.clear();
		}

		List<AuctionTimer> auc_timers = new LinkedList<AuctionTimer>();

		for(Entry<Player, List<AuctionTimer>> entry : auctionTimers.entrySet()) {
			Player player = entry.getKey();
			List<AuctionTimer> atl = entry.getValue();

			for(AuctionTimer atimer : atl) {
				if( atimer.getTimeRemaining() <= 0 ) {
					auc_timers.add(atimer);
				}
			}

			for(AuctionTimer atimer : auc_timers) {
				// remove completed auction? marked it ended?
				send("Your auction of " + atimer.getAuction().getItem().getName() + " ended!", player.getClient());
				atl.remove(atimer);
			}

			auc_timers.clear();
		}
	}
	
	/**
	 * Schedule Task (Immediate)
	 * 
	 * @param task
	 */
	public void scheduleTask(TimerTask task) {
		this.timer.schedule(task, 0L); // immediate scheduling
	}

	// global nameref access functions

	/**
	 * Get a value for a Name Reference from the global table.
	 * 
	 * @param key
	 * @return
	 */
	private Integer getNameRef(String key) {
		return this.nameRef.get(key);
	}

	/**
	 * Get the whole set of Name Reference(s) from the global table
	 * 
	 * @return
	 */
	private Set<String> getNameReferences() {
		return this.nameRef.keySet();
	}

	/**
	 * Set a Name Reference in the global table.
	 * 
	 * @param key
	 * @param value
	 */
	private void setNameRef(String key, Integer value) {
		this.nameRef.put(key, value);
	}

	/**
	 * Clear the global Name Reference Table
	 */
	private void clearNameRefs() {
		this.nameRef.clear();
	}

	private void processTelnetCommand() {
	}

	// need to check and see if sound filename isn't empty
	//debug("MSP", 2);
	//debug("Filename: " + MSP.fileName, 2);
	//debug("Filetype: " + MSP.fileType, 2);
	
	/**
	 * Ask the client to play a sound on it's end through MSP<br><br>
	 * 
	 * NOTES:<br>
	 * do not call when msp is not enabled.<br>
	 * this is for the special case of the type SOUND<br>
	 * 
	 * 
	 * @param soundFile the name of the sound file to play
	 * @param client    the client to send the message to
	 */
	private void playSound(String soundFile, Client client) {
		MSP.play(soundFile, "sound", 25, -1);
		String mspMsg = MSP.generate();       // generate MSP message
		debug(mspMsg, 2);
		send(mspMsg, client);                 // send the message
		MSP.reset();
	}
	
	/**
	 * Ask the client to play music on it's end through MSP<br><br>
	 * 
	 * NOTES:<br>
	 * do not call when msp is not enabled.<br>
	 * this is for the special case of the type MUSIC<br>
	 * 
	 * @param musicFile
	 * @param client
	 */
	private void playMusic(String musicFile, Client client) {
		MSP.play(musicFile, "music", 25, -1);
		String mspMsg = MSP.generate();       // generate MSP message
		debug(mspMsg, 2);
		send(mspMsg, client);                 // send the message
		MSP.reset();
	}

	// presumably this would utilize a loot table or similar construct
	private List<Item> generateLoot( Creature creature ) {
		List<Item> loot = new LinkedList<Item>();

		Weapon weapon = new Weapon();

		loot.add( weapon );

		return loot;
	}

	// dummy function right now, since it should only return a quest if the global quest table exists and has one with that id/index
	private Quest getQuest(int questId) {
		return quests.get(questId);
		//return new Quest();
	}

	public ProgramInterpreter getProgInt() {
		return this.pgm;
	}

	public List<Portal> getPortals() {
		return this.portals;
	}

	/* death event handlers */

	public void handleDeath(Player player) {
		if( player.getState() == Player.State.DEAD ) { // Player/NPC Death
			Room room = getRoom( player.getLocation() );
			Thing corpse = new Thing();
			room.addThing( corpse );
			//Ghost ghost = new Ghost( player ); // ghosts?
			players.remove(player);
		}
	}

	public void handleDeath(Creature creature, Player player) {
		if( creature == null ) {
			send("Error!", player.getClient());
			return;
		}
		
		if( creature.getHP() <= 0 ) {
			//Room room = getRoom( creature.getLocation() );
			creature.setLocation(-1);
			debug("Creature: \"" + creature.getName() + " Location: " + creature.getLocation());
			
			//List<Item> loot = generateLoot(creature);
			//room.addItems(loot);

			// check my quests, and then pass an QuestUpdate if I advanced it
			final Quest q = player.active_quest;
			
			if( q != null ) {
				if( q.getLocation() == getZone(player) ) {
					// check each task
					for( Task task : q.getTasks(true) ) { // get incomplete tasks
						if( task.isType( TaskType.KILL ) ) {
							// is the thing we killed the thing this quest asks us to
							if( task.objective == creature || creature.getRace() == task.objective.getRace() ) {
								task.kills++;
								task.update(null); // dummy update
								if( task.isComplete() ) send("You completed the task ( " + task.getDescription() + " )" , player.getClient());
								
								final QuestUpdate qu = new QuestUpdate( q.getId() );
								final TaskUpdate tu = new TaskUpdate( 0 );
								
								qu.taskUpdates.add(tu);
								
								q.update( qu );
								
								if( q.isComplete() ) send("You completed the quest ("+ q.getName() + ")", player.getClient());
								else { send("Quest not completed!", player.getClient()); }
								
								return;
							}
						}
					}
				}
			}
			
			// for each of the player's quests
			for(Quest quest : player.getQuests()) {
				// if we're in the right zone
				if( quest.getLocation() == getZone(player) ) {
					// check each task
					for( Task task : quest.getTasks(true) ) { // get incomplete tasks
						if( task.isType( TaskType.KILL ) ) {
							// is the thing we killed the thing this quest asks us to
							if( task.objective == creature || creature.getRace() == task.objective.getRace() ) {
								task.kills++;
								task.update(null); // dummy update
								if( task.isComplete() ) send("You completed the task ( " + task.getDescription() + " )" , player.getClient());
								return;
							}
						}
					}
				}
			}
		}
	}

	/* Auctions */

	/**
	 * Creates a new auction and a timer for it, then schedules the
	 * timer for a 1 second tick and returns the auction object.
	 * 
	 * @param item
	 * @param price
	 * @return
	 */
	private Auction createAuction(Player seller, Item item, Coins price) {

		Auction auction = new Auction( seller, item, price, -1 );

		return auction;
	}

	public Auction getAuction(int auctionId) {
		return null;
	}

	public Auction getAuction(Item item) {
		for(Auction auction : this.auctions) {
			if( auction.getItem() == item ) {
				return auction;
			}
		}

		return null;
	}

	public Auction getAuction(String itemName) {
		for(Auction auction : this.auctions) {
			if( auction.getItem().getName().equals(itemName) ) {
				return auction;
			}
		}

		return null;
	}
	
	public List<Auction> getAuctions(Player player) {
		List<Auction> auctions = new LinkedList<Auction>();
		
		for(Auction auction : this.auctions) {
			if( auction.getSeller() == player ) {
				auctions.add( auction );
			}
		}
		
		return auctions;
	}

	/* Party */
	
	/**
	 * Get the party that the specified player is in,
	 * if they are in a party.
	 * 
	 * @param player
	 * @return
	 */
	private Party getPartyContainingPlayer( final Player player ) {
		for( Party party : parties ) {
			if( party.hasPlayer(player) ) {
				return party;
			}
		}

		return null;
	}

	/* Guest Players */

	/**
	 * Get the next pre-existing guest player for the purpose of guest logins
	 * @return
	 */
	private Player getNextGuest() {
		Player temp;

		for(int i = 0; i < guests; i++) {
			temp = getPlayer("Guest" + i);

			if( temp != null && !sclients.values().contains(temp) ) {
				return temp;
			}
		}

		return null;
	}

	/* CHARacter GENeration (Chargen) */

	/**
	 * Determine if the specified Player has a valid race set. Checks
	 * to see if you have a race other than Races.NONE and which is
	 * not a restricted race. Also checks for null (which would be
	 * equally invalid).
	 * 
	 * @param player
	 * @return
	 */
	private boolean hasValidRace(Player player) {
		Race race = player.getRace();

		if( race != null && ( race != Races.NONE || races.contains(race) ) && !race.isRestricted() ) {
			return true;
		}

		return false;
	}

	/**
	 * Determine if the specified Player has a valid class set. Checks
	 * to see if you have a class other than Classes.NONE and which is
	 * not an NPC class. Also checks for null (which would be equally
	 * invalid).
	 * 
	 * @param player
	 * @return
	 */
	private static boolean hasValidClass(Player player) {
		final PClass pcl = player.getPClass();

		if( pcl != null && pcl != Classes.NONE && !pcl.isNPC() ) {
			return true;
		}

		return false;
	}

	private void reset_character(Player player) {
		// Reset ability scores (default is 0)
		final Ability[] abilities = rules.getAbilities();

		for(int index = 0; index < abilities.length; index++) {
			player.setAbility(abilities[index], 0);
		}

		// Reset skills (default is -1?)
		for(final Skill skill : player.getSkills().keySet()) {
			player.setSkill( skill,  -1 );
		}
	}

	private void generate_character(Player player) {
		player.getClient().writeln("Generating player stats, etc...");
		
		player.getClient().writeln("");
		
		// TODO this state should come from somewhere else
		int temp = Constants.ASSIGN;
		
		// calculate hp, etc using stats
		/*Ability[] ab = new Ability[] {
				Abilities.STRENGTH, Abilities.DEXTERITY, Abilities.CONSTITUTION,
				Abilities.INTELLIGENCE, Abilities.WISDOM, Abilities.CHARISMA
		};*/

		Ability[] ab = rules.getAbilities();
		
		int ability_score;
		int index;
		
		switch( temp ) {
		case Constants.ROLL:
			index = 0;
			
			// roll for abilities
			for(final Ability ability : ab) {
				ability_score = 0;
				
				// roll 3d6
				while( ability_score < 8 ) ability_score = Utils.roll(3, 6);

				// add racial ability modifiers
				ability_score += player.getRace().getStatAdjust()[index]; 

				// set ability score
				player.setAbility(ab[index], ability_score);
				
				send( Utils.padRight(ability.getName() + ":", ' ', 15) + ability_score + " (" + player.getRace().getStatAdjust()[index] + ")", player.getClient() );
				
				index++;
			}
			break;
		case Constants.ASSIGN:
			index = 0;
			
			for(final Ability ability : ab) {
				// 5 is used as a placeholder for base value
				ability_score = 5;
				
				// add racial ability modifiers
				ability_score += player.getRace().getStatAdjust()[index];
				
				// set ability score
				player.setAbility(ability, ability_score);
				
				send( Utils.padRight(ability.getName() + ":", ' ', 15) + ability_score + " (" + player.getRace().getStatAdjust()[index] + ")", player.getClient() );
				
				index++;
			}
			break;
		default:
			break;
		}
		
		player.getClient().writeln("");
		
		player.getClient().writeln("Done.");
	}

	private void giveSpells(final Player player, final Spell...newSpells) {
		if( player.isCaster() ) {
			for(Spell s : newSpells) player.getSpellBook().addSpell(s);
		}
	}

	/*int value(Item item) {
		if( canUse( null, item ) ) { // check "can use"
			// determine value in copper/weight
			if( getSkill(Skills.KNOWLEDGE) ) { // really this should be some kind of trading knowledge
				int copperValue = item.getCost().numOfCopper();
				int weight = (int) Math.round( item.getWeight() );
				return copperValue / weight;
			}

			return 1;
		}

		return 0;
	}*/
	
	/**
	 * Determine if a player can use an item. E.g. if you
	 * are trying to use a wand, do you have sufficient
	 * skill in USE_MAGIC_DEVICE or are you a magic using class.
	 * If neither is true, the wand might be useless.
	 * 
	 * @param player
	 * @param item
	 * @return
	 */
	boolean canUse( Player player, Item item ) {
		return false;
	}

	/**
	 * Determine if a player has a particular feat. This is necessary
	 * for certain kinds of checks. For example, if you are selecting
	 * new feats and the one you wish to choose has prequisite feats we
	 * need to be able to check. Also, in case of such feats as proficiencies
	 * for armor and weapons we must be able to tell if you are proficient and
	 * if not to assign the negatives for attempting to use it anyway.
	 * 
	 * @param player
	 * @param featName
	 * @return
	 */
	public boolean hasFeat(Player player, String featName) {
		return false;
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
	public String colors(final String arg, final String cc)
	{
		if ( color == Constants.ANSI ) {
			// ex. \33[5m;Test String\33[0m;
			return ansi_colors.get(cc) + arg + ansi_colors.get("white");
		}
		else if( color == Constants.XTERM ) {
			return xterm_colors.get(cc) + arg + xterm_colors.get("white");
		}
		else {
			return arg;
		}
	}

	public String colorCode(String cc) {
		if ( color == Constants.ANSI ) {
			return ansi_colors.get(cc);
		}
		else if (color == Constants.XTERM) {
			return xterm_colors.get(cc);
		}
		else {
			return "";
		}
	}
	
	/**
	 * Set colors for displaying the names of MUDObject when the
	 * game shows object names (and the color output is enabled).
	 * 
	 * NOTE: this is a shorter form for when you want to set an ANSI color,
	 * but not an xterm256 color
	 * 
	 * @param displayType
	 * @param ANSIColor
	 */
	public void setDisplayColor(final String displayType, final String ANSIColor ) {
		this.displayColors.put( displayType, new Pair<String>(ANSIColor, "") );	
	}

	/**
	 * Set colors for displaying the names of MUDObjects when the 
	 * game shows object names (and color output is enabled).
	 * 
	 * @param displayType
	 * @param ANSIColor   a named ansi color
	 * @param XTERMColor  a named xterm256 color
	 */
	private void setDisplayColor(final String displayType, final String ANSIColor, final String XTERMColor ) {
		this.displayColors.put( displayType, new Pair<String>(ANSIColor, XTERMColor) );
	}
	
	/**
	 * Get color for the displaying the name of a MUDObject
	 * of the type specified (when color output is enabled).
	 * 
	 * @param displayType
	 * @return
	 */
	private String getDisplayColor(final String displayType) {
		Pair<String> colors = this.displayColors.get(displayType);

		if( colors != null ) {
			if ( color == Constants.ANSI ) return colors.one;
			else if ( color == Constants.XTERM ) return colors.two;
			else return "white";
		}
		else return "white";
	}

	public String checkMem() {
		Runtime r = Runtime.getRuntime();

		double in_use = r.totalMemory() / 1000000; // MB
		double max = r.maxMemory() / 1000000;      // MB
		
		return "Memory: " + in_use + " MB / " + max + " MB";
	}

	private Zone getZone(String zoneName) {
		for(Zone zone : zones.keySet()) {
			if( zone.getName().toLowerCase().equals(zoneName.toLowerCase()) ) {
				return zone;
			}
		}

		return null;
	}
	
	// TODO fix this! even when a room has it's zone field set this won't return a zone
	// since it uses a zone's list of room
	private Zone getZone(Room room) {
		Zone z = null;

		/*for(Zone zone : zones.keySet()) {
			if( zone.hasRoom(room) ) {
				z = zone;
				return zone;
			}
		}

		return null; */

		if( z != null ) return z;
		//else return getZone( getRoom( room.location ) );
		else return room.getZone();
	}

	private Zone getZone(Player player) {
		return getZone( getRoom( player.getLocation() ) );
	}
	
	public Zone getZone(int id) {
		for(Zone z : zones.keySet()) {
			//System.out.println("Zone ID: " + z.getId());
			if( z.getId() == id ) {
				return z;
			}
		}
		
		return null;
	}

	/* player to unknown player (i.e. they only have the name) */
	private void sendMail(Player sender, String recipient, String subject, String message) {
		sendMail( sender, getPlayer( recipient ), subject, message );
	}
	
	private void sendMail(String sender, String recipient, String subject, String message) {
		sendMail( getPlayer( sender ), getPlayer( recipient ), subject, message );
	}
	
	private void sendMail(String sender, Player recipient, String subject, String message) {
		sendMail( getPlayer( sender ), recipient, subject, message );
	}

	private void sendMail(Player sender, Player recipient, String subject, String message) {
		MailBox mb = recipient.getMailBox();
		
		// also kludged for System messages
		if( sender == null ) {
			Mail mail = new Mail(mb.numMessages() + 1, "System", recipient.getName(), subject, message, 'U');
			mb.add(mail);
		}
		else {
			Mail mail = new Mail(mb.numMessages() + 1, sender.getName(), recipient.getName(), subject, message, 'U');
			mb.add(mail);
		}
	}

	private void sendMail(final Mail mail, final Player player) {
		if( player != null ) {
			player.getMailBox().add(mail);
			notify(player, Messages.NEW_MAIL);
		}
	}

	public Map<String, String> getAliases() {
		return this.aliases;
	}
	
	/**
	 * Get the header to use for the specified room
	 * 
	 * Takes in a format string and replaces certain markers
	 * with game data
	 * 
	 * %r - the current room
	 * %s - space, fills the remaining space up to the line_limit with some character
	 * %z - the zone the current room belongs to
	 * 
	 * @param formatString
	 * @param client
	 * @return
	 */
	private final String getHeader(final String formatString, final Room room) {
		final StringBuilder sb = new StringBuilder();

		sb.append(formatString);
		
		String temp = sb.toString();

		//final Zone z = getZone( room );
		final Zone z = room.getZone();

		temp = temp.replace("%r", room.getName());
		temp = (z != null) ? temp.replace("%z", z.getName()) : temp.replace("%z", "???");
		//temp = (z != null) ? (z.getParent() != null) ? temp.replace("%z", z.getName() + ", " + z.getParent().getName()) : temp.replace("%z", z.getName()) : temp.replace("%z", "???"); 				
		temp = temp.replace("%s", Utils.padRight("", '-', (80 - (temp.length() - 2) )));
		
		temp = temp.replaceFirst(room.getName(), colors(room.getName(), getDisplayColor("room")));

		return temp;
	}
	
	/**
	 * 
	 * %s - space, fills the remaining space up to the line_limit with some character
	 * %S - current status information
	 * %T - current time
	 * %D - current date
	 * 
	 * @param formatString
	 * @return
	 */
	private final String getFooter(final String formatString) {
		final StringBuilder sb = new StringBuilder();

		sb.append(formatString);
		
		String temp = sb.toString();
		
		String current_status = "H: 0, types:";
		String current_time = game_time.getHours() + ":" + game_time.getMinutes();
		String current_date = day + " " + MONTH_NAMES[month - 1] + ", " + year + " " + reckoning;
		
		temp = temp.replace("%S", current_status);
		temp = temp.replace("%T", current_time);
		temp = temp.replace("%D", current_date);
		
		temp = temp.replace("%s", Utils.padRight("", '-', (80 - (temp.length() - 2) )));
		
		return temp;
	}
	
	/**
	 * Determine if the string specified is a valid name for an exit.
	 * 
	 * alphabetical (lower/upper case), underscore, and dashes permitted.
	 * @param exitName
	 * @return
	 */
	private boolean validExitName(String exitName) {
		return exitName.matches("[a-zA-Z_-]+");
	}
	
	/**
	 * getQuestsByZone
	 * 
	 * WARN: could return null...
	 * 
	 * @param zone the zone to get quests from
	 * @return a list of Quest(s) that are in the specified zone
	 */
	private List<Quest> getQuestsByZone(final Zone zone) {
		List<Quest> quests = new LinkedList<Quest>();
		
		for(final Quest quest : this.quests) {
			if( quest.getLocation() == zone ) {
				quests.add(quest);
			}
		}
		
		return quests;
	}
	
	/*public static Skill getSkill(final String skillName) {
		return skillMap.get(skillName);
	}*/
	
	public Skill getSkill(final String skillName) {
		Skill skill;	

		skill = Skills.skillMap.get(skillName);

		if( skill != null ) return skill;

		return null;
	}
	
	public static int getSkillId(final Skill s) {
		return s.getId();
	}
	
	private void loadZones(final String filename) {
		String[] data = Utils.loadStrings(filename); // file data
		
		final int ID = 0;
		final int NAME = 1;   // name of the zone on it's own line
		final int PARENT = 2; // reference to a parent zone, on it's own line
		final int ERROR = 3;  // error state, a third line that isn't "~"
		
		int step = 0;
		
		// input data storage
		int id = -1;
		String name = null;
		Zone parent = null;
				
		Zone temp = null; // temporary reference for the zone objects we're creating.
		
		// for each line in the file.
		for( String str : data ) {
			//System.out.println("LINE: " + str);
			//System.out.println("STEP: " + step);
			
			if( str.charAt(0) == '#' ) continue;
			else if( str.charAt(0) == '~' ) {
				temp = new Zone(name, parent);
				temp.setId(id);
				zones.put(temp, 0);
				
				System.out.println("New Zone");
				System.out.println(temp.getId() + " = " + temp.getName());
				
				temp = null;
				
				id = -1;
				name = null;
				parent = null;
				
				step = ID;
				continue;
			}
			else {
				switch(step) {
				case ID:
					id = Utils.toInt(str, -1);
					step = NAME;
					break;
				case NAME:
					name = str;
					setNameRef(name.toLowerCase(), id);
					//System.out.println("$" + name.toLowerCase() + " -> " + getNameRef(name.toLowerCase()));
					step = PARENT;
					break;
				case PARENT:
					if( !str.equals("$NULL") ) {
						parent = getZone( getNameRef(str.substring(1)) );
					}
					step = ERROR;
					break;
				case ERROR:
					System.out.println("ERROR! Zone Loading aborted!");
					return;
				default: break;
				}
			}
		}
		
		//System.out.println( getNameReferences() );
		//System.out.println( zones.keySet() );
	}
	
	// kludges for modifying client state
	public void setClientState(final Client client, final String newState) {
		if( !("".equals(newState) || newState == null) ) {
			clientState.put(client, newState);
		}
		else clientState.remove(client);
	}
	
	public String getClientState(final Client client) {
		return clientState.get(client);
	}
	
	public void setClientData(final Client client, final ClientData newData) {
		if( !(newData == null) ) {
			clientData.put(client, newData);
		}
		else clientData.remove(client);
	}
	
	public ClientData getClientData(final Client client) {
		return clientData.get(client);
	}
	
	/**
	 * Move the specified Mobile to the Point specified
	 * @param mob
	 * @param destination
	 */
	public void move(Mobile mob, Point destination) {
		mob.setMoving(true);
		mob.setDestination( destination );
		
		synchronized(this.moving) {
			this.moving.add( mob );
		}
	}
	
	/**
	 * Output the specified text to the client only if we are supposed to echo.
	 * 
	 * @param string
	 * @param client
	 */
	public void echo(final String string, boolean newline, final Client client) {
		/*if( newline ) {
			client.writeln(string);
		}
		else client.write(string);*/
	}
	
	/**
	 * Load Protoypes from a file
	 * 
	 * NOT USED!
	 */
	public void loadProtoypes(final String fileName) {
		File file;
	}
	
	/* Get input from the player (asynchronously?) */
	
	// unused
	
	/*public void requestInput( Client client ) {
		Player player = getPlayer( client );

		interactMap.put( client, new Tuple<Editors, String>(player.getEditor(), "") );

		player.setStatus(Constants.ST_EDIT);
		player.setEditor(Editors.INPUT);
	}*/

	public void requestInput(final String string, final Client client) {
		client.write(string);
		client.setResponseExpected(true);
	}
	
	/*public String getInput( Client client ) {
		Tuple<Editors, String> temp = interactMap.get(client);
		
		if( temp != null ) {
			return temp.two;
		}
		
		return "";
	}*/
	
	// to be used for immediately notifying the player of something
	public void notify(final Player player, final String message) {
		if( player != null ) {
			/*
			 * it'd be nice to have a queue so we don't have to do the sending here
			 * and can hold the notifications if the player is in a state, like
			 * editing or combat where they don't want to see if someone sent them mail
			 */

			send(message, player.getClient());
		}
	}
	
	private boolean registerUseMethod(final Class cl, final Method useMethod) {
		if( !useMethods.containsKey(cl) ) {
			useMethods.put( cl, useMethod );
			return true;
		}
		else {
			return false;
		}
	}
	
	private void mount(final Ridable r, final Player p) {
		p.mount = r;
	}
	
	private void unmount(final Ridable r, final Player p) {
		p.mount = null;
	}
	
	private boolean validateEmailAddress(final String emailAddress) {
		return emailAddress.matches("([\\w-+]+(?:\\.[\\w-+]+)*@(?:[\\w-]+\\.)+[a-zA-Z]{2,7})");
	}
	
	final public List<Client> getClients() {
		return Collections.unmodifiableList( s.getClients() );
	}
	
	/**
	 * Load Race data from a json file and store in a "global" variable.
	 * 
	 * Uses Google-Gson
	 */
	public void loadRaces() {
		com.google.gson.GsonBuilder gb = new com.google.gson.GsonBuilder();
		
		gb.registerTypeAdapter(Race.class, new mud.json.RaceAdapter());
		
		com.google.gson.Gson gson = gb.create();
		
		String path = WORLD_DIR + theme.world + "\\";
		
		try {
			String[] temp = Utils.loadStrings(path + "races.json");
			String json = Utils.join(temp, " ");
			
			Race[] racesArr = gson.fromJson(json, Race[].class);

			races.addAll(Arrays.asList(racesArr));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		for(Race r : races) {
			debug("Race");
			debug("Name: " + r.getName());
			debug("Id: " + r.getId());
			debug("Stat Adjust: " + Arrays.asList(r.getStatAdjust()));
			debug("Restricted?: " + r.isRestricted());
			debug("Can Fly?: " + r.canFly());
		}

		System.out.println( races );
	}
	
	public void addCurrency(final Currency newCurrency) {
	}
	
	public void removeCurrency(final Currency currency) {
	}
	
	public void getCurrencies() {
	}
	
	/**
	 * Change the "root" directory where the server looks for it's data
	 * 
	 * this is, by definition, temporary and must be run before anything else to ensure
	 * that the server will look in the specified directory to find the data it wants
	 *  
	 * @return
	 */
	public void chroot() {
		// NOT IMPLEMENTED!!!
	}
	
	public List<Creature> getCreaturesByRoom(final Room room) {
		return objectDB.getCreaturesByRoom( room.getDBRef() );
	}
	
	public Race getRace(int num) {
		return races.get(num);
	}
	
	/**
	 * Get the Command object from the command map that corresponds to the
	 * given string. Also check any aliases that are defined.
	 * 
	 * @param command
	 * @return
	 */
	private Command getCommand(final String command) {
		if( commandMap.containsKey( command ) ) {
			return commandMap.get( command );
		}
		else {
			if( aliases.containsKey( command ) ) {
				return commandMap.get( aliases.get( command ) );
			}
			else return null;
		}
	}
	
	private void loadModule(final String moduleName) {
		// we're going to classload the specified class and
		// then pull some method references
	}
	
	private void echo(final String input, final Client client) {
		send(input, client);
	}
	
	private void interactive_login(final Client client, final String stage) {
		setClientState(client, "interactive_login");
		
		if( stage.equals("NAME") ) {
			final ClientData cd = new ClientData();
			
			cd.loginstate = "NAME";
			
			setClientData(client, cd);
			
			requestInput("Name? ", client);
		}
		if( stage.equals("PASS") )  requestInput("Password? ", client);
		if( stage.equals("LOGIN") ) {
			final ClientData cd = getClientData(client);
			
			cmd_connect( cd.name + " " + cd.pass, client );
			
			setClientData(client, null);
		}
	}
	
	// testing?
	
	private void drop(final Player player, final Item item) {
	    final Room room = getRoom( player.getLocation() );
	    
	    if( room != null && player.getInventory().contains(item) ) {
	        player.getInventory().remove(item);
	        
	        item.setLocation( room.getDBRef() );
	        item.setPosition( player.getPosition() );
	        room.addItem( item );
	    }
	}
	
	private boolean isAllowed(ObjectFlag of, TypeFlag tf) {
		if( of.getAllowedType() == tf ) {
			return true;
		}
		else return false;
	}
}