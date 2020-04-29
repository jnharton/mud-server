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
 * 
 * http://opensource.org/licenses/MIT
 */

// Java Libraries
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.DecimalFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.regex.Matcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mud.auction.*;
import mud.chat.*;
import mud.colors.*;
import mud.commands.*;
import mud.crafting.Recipe;
import mud.game.*;
import mud.interfaces.*;
import mud.interfaces.Readable;
import mud.magic.*;
import mud.misc.*;
import mud.misc.json.*;
import mud.net.*;
import mud.objects.*;
import mud.objects.exits.*;
import mud.objects.items.*;
import mud.objects.items.Weapon.DamageType;
import mud.objects.npcs.*;
import mud.objects.things.*;
import mud.protocols.*;
import mud.quest.*;
import mud.rulesets.d20.*;
import mud.utils.*;
import mud.utils.Message.*;
import mud.weather.*;

/**
 * @author Jeremy N. Harton
 * 
 * @version 0.9.2
 * 
 * @see "aSimpleMU Java MUD Server Version 0.9.2 ( 2.4.2013 )
 * 
 * - uses Java SDK 1.7
 * - need to remove MU* stuff and focus on MUD related code
 * 
 * >> Copyright 2010 - Eternity Jeremy N. Harton <<
 * 
 * - v0.6.5b retired ( 4.15.2010 )
 * - revision bumped to 0.6.9b ( 9.26.2010)
 * - revision bumped to 0.7b ( 11.1.2010 )
 * - revision bumped to 0.7.2b (11.18.2010 )
 * - forked to 0.7.2bF0 for theme change ( 3.4.2011 )
 * - forked to 0.7.2.bF1 for being more MUD than MU*
 * - revision bumped to 0.8bF1 for major work towards being a MUD server ( 6.4.2011 )
 * - revision bumped to 0.9bF1 for incalculable change since last version ( 3.31.2012 )
 * - revision bumped to 0.9.1bF1 for slow change in last six months (11.2.2012 )
 * - revision bumped to 0.9.2bF1 for recent changes, especially to database design ( 2.4.2013 )
 * - fork nomenclature dropped 0.9.2bF1 -> 0.9.2
 *
 * @Last Work: database and flags (contributions by joshgit from github.com)
 * @minor version number should increase by 1: when 5 or more commands are added
 *        or modified, significant problem is fixed, etc? Last Worked On:
 *        2.4.2013
 **/
public final class MUDServer implements MUDServerI, MUDServerAPI {
	/*
	 * config options --------------
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
	 * _ mode -> mode: <some numbered1mode>
	 * _ telnet -> telnet: <number representing configured telnet support>
	 * A override -> allow overriding of one login per ip address rule
	 * _ max levels (maximum player levels) -> max levels: <some number>
	 * _ max players (maximum concurrent logins) -> max players: <some number>
	 * 
	 * P = Player Configurable, A = Admin Configurable, _ = Initial Server
	 * Configuration Only
	 * 
	 * Some of these ought not to be player configurable except for those who
	 * have sufficient permissions. Player permissions ought to override system
	 * ones, so that MSP can be turned on by default, but a player can disable
	 * it for themselves.
	 * 
	 * However, the permissions need to be set so that a player can only be more
	 * restrictive, not less so (i.e. not 'more permissive') than the server.
	 */

	/* NOTE: "Theme" related */
	private String mud_name;
	private String motd_file;
	private String world;

	// program information
	private final static String program = "JavaMUD"; // the server program name
	private final static String version = "0.9.2";   // the server version number

	// server information
	private String computer = "Stardust";   // the name of the server computer
	private String serverName = "Server";   // the name of the server (obtained from theme definition)

	// server configuration settings
	private int port = 4000;                // the port on which to listen for client connections
	
	private int max_levels = 20;            // maximum player level
	private int max_players = 25;           // maximum number of players (adjustable, but upper limit untested)
	private int max_list_length = 1000;     // maximum list length in lines
	
	// TODO resolve this kludge
	//private String motd = "motd.txt";       // Message of The Day file

	// server state settings
	private int multiplay = 1;              // (0=only one character per account is allowed, 1=infinite connects allowed)
	private int guest_users = 1;            // (0=guests disallowed, 1=guests allowed)
	
	private boolean debug = true;           // (false=off,true=on) Debug: server sends debug messages to the console
	private int debugLevel = 5;             // (1=debug,2=extra debug,3=verbose) priority of debugging information recorded
	
	private boolean logging = false;        // logging? (true=yes,false=no)
	
	private boolean prompt_enabled = false; // show player information bar
	
	private boolean log_chat = true;        // log chat messages? (true=yes,false=no)
	private boolean log_debug = true;       // log debug messages? (true=yes,false=no)
	
	// server state
	private boolean running = true;
	private GameMode mode = GameMode.NORMAL; // see mud.utils.GameMode
	private int guests = 0;                  // the number of guests currently connected

	// Protocols
	/*
	 * This section is badly designed. In theory it represents whether support
	 * for something is enabled, but in the case of colors only ANSI -or- XTERM
	 * should be possible (one color system).
	 * 
	 * Incidentally, the color code generator functions check to see if color is
	 * on, if not they return the original, unaltered argument.
	 */
	private int color = Constants.XTERM; // (0=off,1=ansi,2=xterm) Color ON (ANSI/XTERM)/OFF
	private int msp = 0;                 // (0=off,1=on) MUD Sound Protocol on/off, default: off
	private int telnet = 1;              // (0=no telnet: mud clients, 1=telnet: telnet, 2=telnet: telnet and mud clients)

	// Language/Localization
	// en for English (US), fr for French (France?) are currently "supported",
	// but, only some of the error messages are currently converted to french
	private final String lang = "en";

	
	
	// directories to use (existence will be checked when setup() is run) -- these are pathnames..
	private final String MAIN_DIR = resolvePath("");               // Program Directory
	private final String DATA_DIR = resolvePath(MAIN_DIR, "data"); // Data Directory
	
	private final String ACCOUNT_DIR = resolvePath(DATA_DIR, "accounts");     // Account Directory
	private final String BACKUP_DIR = resolvePath(DATA_DIR, "backup");        // Backup Directory
	private final String BOARD_DIR = resolvePath(DATA_DIR, "boards");         // Boards Directory
	private final String CONFIG_DIR = resolvePath(DATA_DIR, "config");        // Config Directory
	private final String LOG_DIR = resolvePath(DATA_DIR, "logs");             // Log Directory
	private final String MAIL_DIR = resolvePath(DATA_DIR, "mail");            // MAIL Directory
	private final String MAP_DIR = resolvePath(DATA_DIR, "maps");             // MAP Directory
	private final String MOTD_DIR = resolvePath(DATA_DIR, "motd");            // MOTD Directory
	private final String SESSION_DIR = resolvePath(DATA_DIR, "sessions");     // Session Directory
	private final String SPELL_DIR = resolvePath(DATA_DIR, "spells");         // Spell Directory
	private final String THEME_DIR = resolvePath(DATA_DIR, "theme");          // Theme Directory
	private final String WORLD_DIR = resolvePath(DATA_DIR, "worlds");         // World Directory
	
	// help and topic files pertinent to the server
	private final String HELP_DIR = resolvePath(DATA_DIR, "help");            // Help Directory
	private final String TOPIC_DIR = resolvePath(DATA_DIR, "help", "topics"); // Topic Directory
	
	// server storage of default music/sounds for client download?
	private final String MUSIC_DIR = resolvePath(DATA_DIR, "music");
	private final String SOUND_DIR = resolvePath(DATA_DIR, "sound");

	/*
	 * accounts, backup, boards, config?, mail, map, motd, log, session, spell
	 * 
	 * these directories might be within a world folder so unless you have no world folder
	 * they really ought not to be top level dirs
	 * 
	 * help and topics should be generally the same per game
	 */

	/*
	 * filename variables used to be final -- i'd like to be able to reload or
	 * change them while the game is running though
	 */

	// TODO consider fixing filenames everywhere so that we don't have to
	// reference the dirs every time

	// files to use (existence will be checked when setup() is run)
	private String DB_FILE = resolvePath(DATA_DIR,          "db.txt");                  // database file (ALL) -- will replace all 3 or supersede them
	private String ERRORS_FILE = resolvePath(DATA_DIR,      "errors_" + lang + ".txt"); // messages file (errors) [localized?]
	private String SPELLS_FILE = resolvePath(DATA_DIR,      "spells.txt");              // database file (spells) -- contains spell names, messages, and more
	private String ALIASES_FILE = resolvePath(CONFIG_DIR,   "aliases.conf");            // aliases.conf -- command aliases
	private String BANLIST_FILE = resolvePath(CONFIG_DIR,   "banlist.txt");             // banlist.txt -- banned ip addresses (IPv4)
	private String CHANNELS_FILE = resolvePath(CONFIG_DIR,  "channels.txt");            // channels.txt -- preset chat channels to load
	private String CONFIG_FILE = resolvePath(CONFIG_DIR,    "config.txt");              // config.txt -- configuration options file
	private String FORBIDDEN_FILE = resolvePath(CONFIG_DIR, "forbidden.txt");           // forbidden.txt -- forbidden names/words
	private String MOTD_FILE = resolvePath(MOTD_DIR,        "motd.txt");                // motd.txt -- message of the day/intro screen
	private String THEME_FILE = resolvePath(THEME_DIR,      "default.thm");             // theme file to load

	// ???
	private Integer start_room = 9; // default starting room

	// Primary Objects
	private Server s;      // The network server object
	
	private Logger logger;
	
	public TimeLoop game_time;   // TimeLoop Object
	private CommandExec cmdExec; //

	private ConcurrentLinkedQueue<CMD> cmdQueue;
	
	final private ChatChanneler chan = new ChatChanneler();

	// HashMaps
	// dynamic - the contents of the hashmap may change while the server is running and in some cases that is very likely
	// static - the contents of the hashmap are currently loaded once at startup and not modified thereafter
	// class static - identical for every instances of the class
	// pna - per name association?
	
	// TODO consider reworking ANSI and XTERM256 /classes/ so that they can be used to export strings
	public static final Palette ANSI = new Palette("ansi", Constants.ANSI);
	public static final Palette XTERM256 = new Palette("xterm256", Constants.XTERM);
	
	{
		ANSI.addColor("black", 30);
		ANSI.addColor("red", 31);
		ANSI.addColor("green",  32);
		ANSI.addColor("yellow", 33);
		ANSI.addColor("blue", 34);
		ANSI.addColor("magenta", 35);
		ANSI.addColor("cyan", 36);
		ANSI.addColor("white", 37);
	}
	
	{
		XTERM256.addColor("red", 9);
		XTERM256.addColor("green", 10);
		XTERM256.addColor("yellow", 11);
		XTERM256.addColor("blue", 12);
		XTERM256.addColor("magenta", 13);
		XTERM256.addColor("cyan", 14);
		XTERM256.addColor("white", 15);
		XTERM256.addColor("purple", 55);
		XTERM256.addColor("purple2", 92);
		XTERM256.addColor("orange", 208);
		XTERM256.addColor("other", 82);
		XTERM256.addColor("pink", 161);
		XTERM256.addColor("pink2", 163);
		XTERM256.addColor("pink3", 212);
	}
	
	private Map<String, Thread> threads = new Hashtable<String, Thread>();

	private final Map<String, Pair<String>> displayColors = new HashMap<String, Pair<String>>(8, 0.75f); // HashMap specifying particular colors for parts of text (dynamic)

	private final Map<String, String> aliases = new LinkedHashMap<String, String>(20, 0.75f); // HashMap to store command aliases (static)
	private final Map<Integer, String> Errors = new HashMap<Integer, String>(5, 0.75f);      // HashMap to store error messages for easy retrieval (static)
	private final Map<String, Date> holidays = new HashMap<String, Date>(10, 0.75f);               // HashMap that holds an in-game date for a "holiday" name string
	private final Map<Integer, String> years = new HashMap<Integer, String>(50, 0.75f);            // HashMap that holds year names for game themes that supply them (static)
	private final Map<String, Command> commandMap = new HashMap<String, Command>(20, 0.75f); // HashMap that holds an instance of each command currently (dynamic)
	private final Map<Zone, Integer> zones = new LinkedHashMap<Zone, Integer>(1, 0.75f);   // HashMap that tracks currently "loaded" zones (dynamic)
	
	private final Map<Client, Player> sclients = new HashMap<Client, Player>();
	
	private final PlayerControlMap playerControlMap = new PlayerControlMap();                // HashMap that stores control data for Players controlling NPCs (dynamic)
	
	private HashMap<Player, Session> sessionMap = new HashMap<Player, Session>(1, 0.75f); // player to session mapping

	// Databases/Data
	//private ODBI objectDB = new ObjectDB(); // TODO consider access modifier
	private ObjectDB objectDB = new ObjectDB(); // TODO consider access modifier

	private List<Player> players;          // ArrayList of Player Objects currently in use

	private Map<String, Spell> spells2 = new HashMap<String, Spell>(); // HashMap to lookup spells by index using name as key (static)

	// Help Files stored as string arrays, indexed by name
	private Map<String, String[]> helpTable = new Hashtable<String, String[]>();
	private Map<String, String[]> topicTable = new Hashtable<String, String[]>();

	// "Security" Stuff
	private List<String> banlist;        // ArrayList of banned IP addresses
	private List<String> forbiddenNames; // ArrayList of forbidden names for players/characters

	// Other
	private List<Effect> effectTable; // ArrayList of existing effects (could be reused many places)
	private List<Auction> auctions;

	/* I need to decide how this data should be split between here and timeloop */

	// Time & Date - General Variables
	private final static String[] suffix = { "st", "nd", "rd", "th" }; // day number suffix

	// NOTE: at the moment, the date and time is only held here until we can put it into timeloop
	private int day = 30;
	private int month = 7;
	private int year = 1332;

	private int game_hour = 5;    // 5am
	private int game_minute = 58; // 58m past 5am

	// Theme Related Variables
	public static int[] DAYS = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 }; // days in each month
	public static int MONTHS = 12;                                                 // months in a year
	public static String[] MONTH_NAMES = new String[MONTHS];                       // month_names
	private Season season = Seasons.SUMMER;                                        // Possible - Spring, Summer, Autumn, Winter

	//private String month_name;
	//private String year_name;
	private String reckoning;

	private Map<String, BulletinBoard> boards = new Hashtable<String, BulletinBoard>();

	Ruleset rules = null;       // ruleset reference for defining stats, skills, etc
	boolean rs_special = false; // are we using a S.P.E.C.I.A.L based ruleset?

	// Testing
	//private BulletinBoard default_board;
	private ArrayList<Portal> portals = new ArrayList<Portal>();

	private ArrayList<Party> parties = new ArrayList<Party>(); // groups of players
	
	// TODO there is no reason to track more than one trade per player at a time (11-18-2015)
	private Map<Player, Trade> trades = new Hashtable<Player, Trade>(); // ongoing trades

	// for use with telnet clients
	private byte[] byteBuffer = new byte[1024]; // a byte buffer to deal with telnet (characters are sent to the server as they are typed, so need to buffer input until a particular key is pressed
	private byte linefeed = 10;                 // line-feed character

	private Map<Client, ArrayList<Character>> inputBuffers = new Hashtable<Client, ArrayList<Character>>( max_players);
	// end telnet

	private ProgramInterpreter pgm; // "Script" Interpreter

	/* global */

	protected ArrayList<Mobile> moving = new ArrayList<Mobile>();  // list of mobiles which are currently moving
	protected ArrayList<Player> outside = new ArrayList<Player>(); // list of players who are currently outside

	// public HashMap<Room, List<Player>> listenersLists; // possibly replace per room listener lists? (UNUSED)

	protected Timer timer = new Timer(); // Timer object with thread for executing TimerTask(s) for Spells, Effects

	private Hashtable<String, Bank> banks = new Hashtable<String, Bank>(); // banks

	// nameref table
	private Hashtable<String, Integer> nameRef = new Hashtable<String, Integer>();  // store global name references to dbref numbers (i.e. $this->49)

	// quest table
	private ArrayList<Quest> quests = new ArrayList<Quest>();                       // global quest table -- all root quest objects should be loaded here
	private Map<Zone, List<Quest>> questsByZone = new HashMap<Zone, List<Quest>>(); // mapping of zones to a list of quests within them

	// prototypes
	private Map<String, Item> prototypes = new Hashtable<String, Item>();
	private Map<String, Thing> prototypes1 = new Hashtable<String, Thing>();

	/* player data */

	// timer maps
	private Map<Player, List<SpellTimer>> spellTimers = new HashMap<Player, List<SpellTimer>>();       // spell cooldown timers
	private Map<Player, List<EffectTimer>> effectTimers = new HashMap<Player, List<EffectTimer>>();    // effect timers (effects end when timer ends)
	private Map<Player, List<AuctionTimer>> auctionTimers = new HashMap<Player, List<AuctionTimer>>(); // auctions timers auctions end when timer ends)

	// server state/configuration
	private boolean firstRun = false;        // is the first time the software been's run                       [default: false]
	private boolean testing = false;         // enable the execution of bits of random code                     [default: false]
	private boolean use_weather = false;     // indicate whether the weather should be updated regularly        [default: false]
	private boolean use_accounts = false;    // enable the use of accounts/the account system                   [default: false]
	private boolean use_cnames = false;      // will generate names for players you don't know use classes      [default: false]
	private boolean int_login = false;       // interactive login?                                              [default: false]
	private boolean magic = false;           // use the coded magic system?                                     [default: false]
	private boolean notify_immediate = true; // notifications using notify(...) are sent immediately            [default: true]
	private boolean input_hold = false;      // don't accept new input during automatic backups are running     [default: false]   
	private boolean queued_commands = false; // should we queue commands and process them in order of arrival?  [default: true]
	private boolean soft_commands = true;    // will we look at a command map for Command object based commands [default: true]
	private boolean console_enabled = true;  // is the admin console available                                  [default: true]
	private boolean echo_enabled = true;     // does the server do echoing ???
	
	// TODO resolve the issue behind enabling queued_commands which results in fairly high CPU usage
	
	/*
	 * 'weather' is auto set to true when weather is setup/enabled
	 * if 'notify_immediate' is false, notifications are queued up and the queue is
	 * cycled through in main loop
	 * setting firstRun to true will cause the software to do some basic file creation and data generation
	 */

	// TODO config settings for client/server echoing?

	// state data for various client/server interactions
	private Map<Client, String> clientState = new Hashtable<Client, String>();
	
	private Map<Client, LoginData> loginData = new Hashtable<Client, LoginData>();    // holding values for login
	private Map<Client, ClientData> clientData = new Hashtable<Client, ClientData>(); // used for?
	
	// holds instances of interactive terminal to game that is outside of the game world itself
	private ConsoleMonitor cmon;

	// Client/Account Table (maps clients to accounts, used for account login/menu)
	private Map<Client, Account> caTable = new Hashtable<Client, Account>();

	private AccountManager acctMgr = new AccountManager(); // Account Manager, holds and tracks Account objects

	// necessary so that cNames can properly map to player objects
	private Map<String, Player> cNames = new Hashtable<String, Player>();
	private Map<PClass, Integer> numPlayersOnlinePerClass = new Hashtable<PClass, Integer>();

	// List<Feat> feats = (ArrayList<Feat>) Utils.mkList(Feat.ap_light,
	// Feat.ap_medium, Feat.ap_heavy);
	
	private Map<Player, Party> partyInvites = new HashMap<Player, Party>(); // party invitations
	private Map<Player, Player> tradeInvites = new HashMap<Player, Player>(); // trade invitations

	List<Race> races = new ArrayList<Race>();
	
	// loot tables (lists?) for creatures?
	Map<String, List<Item>> lootTables = new Hashtable<String, List<Item>>();
	//Map<String, Table> lootTables2 = new Hashtable<String, Table>(); // map some kind of id to a table

	/*
	 * The idea here is to replace the use (in here) of calling the static item
	 * types in ItemType with referencing the name of the ItemType (in
	 * lowercase) from this map/table.
	 * 
	 * Additionally, this should make it a bit easier to work with "end-user"
	 * item types defined in supplemental code.
	 */
	/*Hashtable<String, ItemType> itemTypes = new Hashtable<String, ItemType>() {
		{
			put("armor", ItemTypes.ARMOR);
			put("arrow", ItemTypes.ARROW);
			put("book", ItemTypes.BOOK);
			put("clothing", ItemTypes.CLOTHING);
			put("container", ItemTypes.CONTAINER);
			put("ear_ring", ItemTypes.EAR_RING);
			put("food", ItemTypes.FOOD);
			put("helmet", ItemTypes.HELMET);
			put("none", ItemTypes.NONE);
			put("potion", ItemTypes.POTION);
			put("ring", ItemTypes.RING);
			put("weapon", ItemTypes.WEAPON);
		}
	};*/

	private GameModule module = null;
	
	// per player message queue
	private Hashtable<Player, LinkedList<String>> messageQueues = new Hashtable<Player, LinkedList<String>>();

	private Hashtable<Player, cgData> cg_data = new Hashtable<Player, cgData>();
	
	// list of currently hostile creatures
	public List<Creature> hostiles = new LinkedList<Creature>();

	// command lists
	private static final String[] user_cmds = new String[] {
			"ask", "attack", "auction", "auctions",
			"balance", "bash", "bid", "boards", "buy",
			"calendar", "cast", "chargen", "climb",
			"cls", "colors", "commands", "condition",
			"describe", "drink", "drop",
			"effects", "equip", "exits", "exp",
			"feats",
			"go", "get", "greet",
			"help", "home", "housing",
			"inspect", "interact", "inventory",
			"levelup", "list", "lock", "look",
			"lookat",
			"mail", "map", "money", "motd", "move",
			"page", "party", "passwd", "pconfig", "pinfo",
			"push", "put", "prompt",
			"quests", "quit",
			"roll", "run",
			"say", "score", "sell", "sheathe", "skillcheck",
			"spellinfo", "spells", "stats", "staff",
			"talk", "take", "target", "tell", "time",
			"trade", "travel",
			"unequip", "unlock", "use",
			"version", "vitals",
			"walk", "where", "who", "write"
	};

	private static final String[] build_cmds = new String[] { "@check",
			"@cedit",                      // @check check to see what exit props aren't set
			"@describe", "@dig", "@door",  // @describe describe an object, @dig dig a new room, @door create an arbitrary exit
			"@dungeon",                    // @dungeon dig a new dungeon
			"@examine", "@fail", "@flags", // @fail set exit fail message, @flags see flags on an object
			"@iedit",                      // @iedit edit an item
			"@jump",                       // @jump jump to a different room
			"@lsedit",                     // @lsedit edit a list
			"@makehouse",                  // @makehouse make a house
			"nameref", "@ofail", "@open",  // @ofail set exit ofail message, @open open a new exit (1-way)
			"@recycle", "@redit",          // @recycle recycle objects
			"@osucc", "@success"           // @osucc set exit osuccess message, @success set exit success message
	};

	private static final String[] admin_cmds = new String[] {
			"@alias",                                                         // @alias setup command aliases
			"@bb",                                                            // @bb use bulletin board
			"@config", "@control",                                            // @config change server configuration options, @control control an NPC
			"@debug",                                                         // @debug show debug information
			"@hash", "@hedit",                                                // @hash see hash of a string, @hedit edit help files
			"@give", "@kick", "@listprops",                                   // @listprops list properties on an object
			"@nextdb", "@npcs",                                               // @nextdb get the next unused dbref number
			"@pgm",                                                           // @pgm interpret a "script" program
			"@set", "setcolor", "@setskill",                                  // @set set properties on objects, @setskill set player skill values
			"@sethp", "@setlevel", "@setmana",
			"@setxp",
			"@viewlog", "@zones", "@zoneinfo"                                 // @zones setup,configure,modify zones
	};

	private static final String[] wiz_cmds = new String[] {
			"@backup", "@ban",     // @ban ban player
			"@flag", "@flush",
			"@setmode", "@start",
			"@teleport"
	};

	private static final String[] superuser_cmds = new String[] {
			"@access",
			"@broadcast",
			"@load",
			"@sethour", "@setminute", "@shutdown",
			"@unload"
	};

	private static final int DEFAULT_PORT = 4000;
	
	// on-going conversation tracking
	Map<Player, Tuple<NPC, CNode>> conversations = new Hashtable<Player, Tuple<NPC, CNode>>();
	
	/*
	public int WELCOME_ROOM = 0; // welcome room
	public int VOID = -1;        //
	public int LIMBO = -2;       // ?
	*/
	
	/*public static final Ore iron = new Ore("Iron");
	public static final Ore tin = new Ore("Tin");
	
	public static final Ore copper = new Ore("Copper");
	public static final Ore silver = new Ore("Silver");
	public static final Ore gold = new Ore("Gold");*/
	
	// TODO implement new debug controls -- use string keys to decide if that debug data is currently wanted
	private Set<String> debugKeys = new HashSet<String>();
	
	private Map<String, Recipe> recipes = new Hashtable<String, Recipe>();
	
	Weather weather = null;
	
	// resource nodes
	List<Node> nodeList = new LinkedList<Node>();
	
	String[] serverArgs;
	
	public MUDServer() {
	}

	public MUDServer(final String address, int port) {
		this.port = port;
	}
	
	// TODO try to clean up server.X references...
	public static void main(final String args[]) {
		/* options: <port>, --port=<port>, --debug */
		MUDServer server = null;

		if (args.length < 1) {
			System.out.println("No port number specified. Exiting...");
			System.exit(-1);
		}

		try {
			server = new MUDServer(); // create server
			
			server.serverArgs = Arrays.copyOfRange(args, 0, args.length);

			System.out.println(Arrays.asList(args));

			// process command line parameters
			for (int a = 0; a < args.length; a++) {
				final String s = args[a];

				if (s.startsWith("--")) {
					final String param = s.substring(2, s.length());

					if (param.equals("port")) {
						server.port = Utils.toInt(args[a + 1], DEFAULT_PORT);
						System.out.println("Using port " + server.port);
						System.out.println("");
					}
					else if (param.equals("config-file")) {
						server.readConfigFile(args[a + 1]);
					}
					else if (param.equals("db")) {
						server.DB_FILE = server.resolvePath(server.DATA_DIR, "databases", args[a + 1]);
						System.out.println("Using database " + args[a + 1]);
						// System.out.println("");
					}
					else if (param.equals("debug")) {
						server.debug = true;
						System.out.println("Debugging Enabled.");
						System.out.println("");
					}
					else if (param.equals("enable-logging")) {
						server.logging = true;
						System.out.println("Logging Enabled.");
						System.out.println("");
					}
					else if (param.equals("enable-testing")) {
						server.testing = true;
						System.out.println("Testing Enabled!");
					}
					else if (param.equals("int-login")) {
						server.int_login = true;
					}
					else if( param.equals("magic") ) {
						server.magic = true;
					}
					else if (param.equals("module")) {
						String moduleName = args[a + 1];
						
						// TODO fix these explicit checks
						if (moduleName.equals("basic") ) server.module = new mud.modules.BasicModule("Basic", null);
						if (moduleName.equals("foe"))    server.module = new mud.modules.FalloutEquestria();
						if (moduleName.equals("dnd-fr")) server.module = new mud.modules.DND35();
						else                             ; //load and initialize a GameModule subclass?
					}
					else if (param.equals("setup")) {
						server.firstRun = true;
					}
					else if (param.equals("telnet")) {
						server.telnet = Utils.toInt(args[a + 1], 0);
					}
					else if (param.equals("theme")) {
						server.THEME_FILE = server.resolvePath(server.THEME_DIR, args[a + 1]);
						System.out.println("Using theme " + args[a + 1]);
						// System.out.println("");
					}
					else if (param.equals("use-accounts")) {
						server.use_accounts = true;
					}
				}
			}
		}
		catch (final Exception e) {
			server.debug("Exception(MAIN): " + e.getMessage());
			
			server.debug( e );
			
			// TODO is a more orderly shutdown possible?
			System.exit(-1);
		}

		server.init();
	}

	/**
	 * Gets and returns the name of the program, which is a static constant.
	 * 
	 * @return
	 */
	public static String getName() {
		return program;
	}

	/**
	 * Gets and returns the version (number) of the program, which is a static
	 * constant.
	 * 
	 * @return
	 */
	public static String getVersion() {
		return version;
	}

	public String getServerName() {
		return serverName;
	}

	public boolean isRunning() {
		return this.running;
	}

	/**
	 * Initialize Server
	 * 
	 * Performs the basics tasks to get the server running setup - loads
	 * configuration, preferences, the database, game theme, etc run - starts
	 * the main program loop -> If an unhandled exception (NullPointerExceptions
	 * in particular) occurs anyway in the init or run methods it will be caught
	 * here and result in the program being terminated
	 * 
	 */
	public void init() {
		try {
			setup();
			run(); // run main loop
		}
		catch (final Exception e) {
			debug("Exception in MUDServer.init()");
			
			debug( e );
			
			System.exit(-1);
		}
	}

	private void setup() {
		// snag this machine's real IP address
		try {
			// Get the address and hostname, so you can report it
			InetAddress addr = InetAddress.getLocalHost(); // Get IP Address
			this.computer = addr.getHostName(); // get computer name
			System.out.println("");
			System.out.println(this.computer); // print computer name
			System.out.println("");
		}
		catch (final UnknownHostException e) {
			debug( e );
		}

		// Tell Us where the program is running from
		System.out.println("Working: " + MAIN_DIR);
		System.out.println("Data:    " + DATA_DIR);
		
		System.out.println("");

		System.out.println("Important!: The two above should be the same if the top is where you have the program AND it's data");

		System.out.println("");

		// search for needed directories and files (using predefined names and
		// locations)
		ArrayList<File> directories = new ArrayList<File>();

		// directories
		directories.add(new File(MAIN_DIR));
		directories.add(new File(DATA_DIR));

		directories.add(new File(BACKUP_DIR));
		directories.add(new File(BOARD_DIR));
		directories.add(new File(CONFIG_DIR));
		directories.add(new File(HELP_DIR));
		directories.add(new File(MAP_DIR));
		//directories.add(new File(MOTD_DIR));
		directories.add(new File(SPELL_DIR));
		directories.add(new File(THEME_DIR));
		directories.add(new File(LOG_DIR));
		directories.add(new File(SESSION_DIR));
		directories.add(new File(WORLD_DIR));

		// check that the directories exist, if not create them
		for (final File dir : directories) {
			if ( dir.exists() ) {
				System.out.println("Directory \'" + dir + "\' exists.");
			}
			else {
				boolean success = dir.mkdir();

				if (success) {
					System.out.println("Directory \'" + dir + "\' created");
				}
				else {
					System.out.println("Error: could not create directory (" + dir.getName() + ")");
				}
			}
		}
		
		/*ArrayList<File> files = new ArrayList<File>();

		files.add(new File(ALIASES_FILE));
		files.add(new File(BANLIST_FILE));
		files.add(new File(CHANNELS_FILE)); 
		files.add(new File(CONFIG_FILE));
		files.add(new File(FORBIDDEN_FILE));

		// check that the files exist, if not create them for (final File
		for(File file : files) {
			boolean success = false;

			try {
				success = file.createNewFile();
			}
			catch (IOException e1) {
				//TODO Auto-generated catch block e1.printStackTrace(); }
			}
			
			if (success) {
				System.out.println("File \'" + file.getAbsolutePath() + "\' created");
			}
			else {
				System.out.println("File \'" + file.getAbsolutePath() + "\' exists."); 
			}
		}*/
		
		System.out.println("");
		
		// TODO should module be another theme file entry?
		// TODO this is a kludge to set the ruleset based on the module
		if (module != null) {
			if (module.getName().equals("Fallout Equestria")) {
				rules = mud.rulesets.foe.FOESpecial.getInstance();
				rs_special = true;
			}
			else rules = D20.getInstance();
		}
		else rules = D20.getInstance();

		Player.ruleset = rules; // set static Ruleset reference in Player

		// if this is the first run (as indicated by setup parameter)
		if (firstRun) {
			System.out.print("Running initial setup...");
			create_data();
			System.out.println("Done");
		}

		// Logging
		if ( logging ) {
			Logger logger = new Logger();
			
			logger.setDirectory( LOG_DIR );
			
			logger.addConfigOption("debug", debug);
			logger.addConfigOption("log_chat", log_chat);
			logger.addConfigOption("log_debug", log_debug);
			
			// instantiate log objects (using no max length, no buffer -- for now)
			
			logger.register( new Log("log", false) );   // character actions, etc
			logger.register( new Log("error", false) ); // any software errors that occur
			
			if( log_debug ) logger.register( new Log("debug", false) ); // any and all debugging
			if( log_chat )  logger.register( new Log("chat", false) );  // all chat messages
			
			attach( logger );
			
			// open log files for writing
			this.logger.start();
			
			debug("Logs Initialized.");
		}
		else {
			// tell us it's disabled
			debug("Logging Disabled.");
		}

		debug(""); // formatting

		if (module != null) {
			debug("Module: " + module.getName() + " v" + module.getVersion());
		}

		debug(""); // formatting

		// TODO should it always load a theme or only when specified?

		// Theme Loading
		this.loadTheme(THEME_FILE);
		
		// TODO resolve this somehow, perhaps those aren't appropriate pieces of a theme
		// NOTE: going to set these in loadTheme for now (8-8-2015)
		/*day = 0;
		month = 0;
		year = 0;*/

		//this.month_name = MONTH_NAMES[month - 1];
		//this.year_name = years.get(year);

		debug(""); // formatting

		debug("Using database " + this.DB_FILE);

		debug(""); // formatting

		// Load Databases/Persistent Data into memory
		//
		// Note: Does it make any sense to replace loading all this into memory
		// using a buffered reader and loading objects as it goes? It would be
		// really
		// wise to implement configurable auto db-saving here
		//

		// error message hashmap loading
		final String[] errors = Utils.loadStrings(ERRORS_FILE);
		for (final String e : errors) {
			final String[] working = e.split(":");

			if (working.length >= 2) {
				debug("Error(number): " + working[0]);
				debug("Error(message): " + working[1]);
				
				this.Errors.put(Integer.parseInt(working[0]), working[1]);
			}
		}
		
		debug("");
		
		// load spells
		if (magic) {
			loadSpells(Utils.loadStrings(SPELLS_FILE));
			debug("Spells Loaded!");
		}
		else debug("Magic Disabled!");
		
		debug("");
		
		debug("Loading Zones...");
		
		debug("");
		
		// TODO be nice to do all these resolutions in one place..
		// Load Zones (only doing this here, because Rooms may be in a zone, and
		// so by loading Zones first then rooms can be placed in them by the ObjectLoader
		//loadZones(WORLD_DIR + world + "\\zones.txt");
		loadZones( resolvePath(WORLD_DIR, world, "zones.txt") );
		

		debug("");

		debug("Loading Races...");
		
		debug("");

		// TODO get rid of this kludge and deal with JSON stuff
		boolean test = false;

		if (!test && !firstRun) {
			loadRaces();
		}
		else {
			for (int i = 0; i < 8; i++) {
				races.add( Races.getRace(i) );
			}
		}
		
		debug("");
		
		debug("Module Setup...");
		
		// TODO deal with kludgy module issues
		if ( module != null ) {
			debug("Module: " + module.getName());
			
			// initialize module
			module.init(DATA_DIR);
			
			// "install" ruleset
			final Ruleset modRS = module.getRuleset();
		}
		
		debug("");

		debug("Loading Database...");

		final ObjectLoader loader = new ObjectLoader(this, objectDB);

		// Load everything from databases by flag
		loader.loadObjects(loadListDatabase(DB_FILE), logger);

		debug("Database Loaded!");
		debug("");
		
		debug("Unused DBRefs: " + objectDB.getUnused() );
		
		// TODO what do these functions output?
		
		// Post-Room Loading (i.e. place within the game world)
		loadExits();          // load exits
		placeThingsInRooms(); // load thing
		loadItems();          // load items
		// stackItems(); // put items inside of the objects they belong

		// objectDB.stackItems(); // put items inside of the objects they belong

		debug("");

		debug("Filling shops with merchandise!");
		
		fillShops(); // put stuff in merchant's shops (deprecated?)
		
		debug("");
		
		debug("Adding Listeners to Rooms");
		
		// make sure npcs are added to listeners
		for (final NPC npc : getNPCList()) {
			if (npc != null) {
				debug("NPC(" + npc.getName() + "): " + npc.getLocation());
				
				final Room room = getRoom(npc.getLocation());
				
				if( room != null ) room.addListener(npc);
				else               debug("Missing ROOM (#" + npc.getLocation() + ")");
			}
			else debug("NULL NPC?");
		}
		
		conversation_test();
		
		// instantiate banned ip list
		banlist = loadListDatabase(BANLIST_FILE);

		// instantiate forbidden names list
		forbiddenNames = loadListDatabase(FORBIDDEN_FILE);

		// load configuration data (file -- default.config)
		// loadConfiguration(CONFIG_DIR + "config.txt", configs); ?
		/*
		 * for (final String s : loadListDatabase(CONFIG_DIR + "config.txt")) {
		 * final String[] configInfo = s.split(":"); String name =
		 * Utils.trim(configInfo[0]); String value = Utils.trim(configInfo[1]);
		 * value = value.substring(0, value.indexOf('#')); config.put(name,
		 * value); }
		 */

		// print out config map
		// debug(config.entrySet());

		debug("");
		
		// for help/topic file loading
		final StringBuilder sb = new StringBuilder();
		Counter c = new Counter(0, 0, 3);
		
		// help file loading
		debug("Loading Help Files... ");
		System.out.println("");
		
		System.out.println("Help Files");
		//System.out.println("----------------------------------------");
		System.out.println(Utils.padLeft("", '-', 65));
		
		for (final String helpFileName : generateHelpFileIndex()) {
			if( c.isMax() ) {
				System.out.println( sb.toString().trim() );
				sb.delete(0,  sb.length());
				c.reset();
			}
			else {
				sb.append(Utils.padRight(helpFileName, ' ', 25)).append(" ");
				c.increment();
			}
			
			// TODO path kludging AGAIN! this vs vague reference to globals?
			//final String[] helpfile = Utils.loadStrings(this.HELP_DIR + helpFileName);
			final String[] helpfile = Utils.loadStrings( resolvePath(this.HELP_DIR, helpFileName) );
			
			final String key = helpfile[0];
			
			helpTable.put(key, helpfile);

			// HelpFile hf = new HelpFile(helpfile[0], helpfile);

			// TODO consider a config option for this
			// check to see if the mapping made it into the table
			/*if( helpTable.containsKey(key) && helpTable.get(key) != null ) {
				debug(Utils.padRight(helpFileName, ' ', 20) + " -- HelpFile Loaded!");
			}
			else {
				
			}*/
		}

		// cleanup
		System.out.println( sb.toString().trim() );
		sb.delete(0,  sb.length());
		c.reset();
		
		System.out.println("");
		debug("Help Files Loaded!");

		debug("");

		// topic file loading
		debug("Loading Topic Files... ");
		System.out.println("");
		
		System.out.println("Topic Files");
		//System.out.println("----------------------------------------");
		System.out.println(Utils.padLeft("", '-', 65));
		
		for (final String topicFileName : generateTopicFileIndex()) {
			if( c.isMax() ) {
				System.out.println( sb.toString().trim() );
				sb.delete(0,  sb.length());
				c.reset();
			}
			else {
				sb.append(Utils.padRight(topicFileName, ' ', 25)).append(" ");
				c.increment();
			}

			//final String[] topicfile = Utils.loadStrings(this.TOPIC_DIR + topicFileName);
			final String[] topicfile = Utils.loadStrings( resolvePath(this.TOPIC_DIR, topicFileName) );

			topicTable.put(topicfile[0], topicfile);

			// TODO consider a config option for this
			// check to see if the mapping made it into the table
			/*
			 * if( topicTable.containsKey( topicfile[0] ) ) {
			 * debug("Topic Loaded!"); } else debug("Error!");
			 */
		}
		
		// cleanup
		System.out.println( sb.toString().trim() );
		sb.delete(0,  sb.length());
		c.reset();
		
		System.out.println("");
		debug("Topic Files Loaded!");

		debug("");

		debug("Colors (ANSI): " + ANSI.getColors().keySet()); // DEBUG
		debug("Colors (XTERM): " + XTERM256.getColors().keySet()); // DEBUG

		// set up display colors (ANSI, XTERM256)
		setDisplayColor("exit", "green", "green");
		setDisplayColor("player", "magenta", "orange");
		setDisplayColor("npc", "cyan", "cyan");
		setDisplayColor("thing", "yellow", "yellow");
		setDisplayColor("room", "green", "other");
		setDisplayColor("item", "yellow", "yellow");
		setDisplayColor("creature", "cyan", "cyan");
		setDisplayColor("quest", "cyan", "cyan");

		debug("Object Colors: " + displayColors.entrySet()); // DEBUG
		
		debug("");
		
		/*
		 * Command Mapping
		 * 
		 * Ideally this would be done for all commands and the cmd parser would
		 * simply check aliases, get the actual name, and call
		 * commandMap.get(name).execute(arg, client)
		 * 
		 * I really need to be careful with access permissions, or I need to
		 * facilitate some means to get just a basic interface
		 * 
		 * NOTE: these need to be kept up to date with the implementation in the
		 * functions cmd_x, the plan is to eventually remove those and the cmd
		 * interpreter to a major extent so that I am mostly just doing this:
		 * 
		 * commandMap.get(cmd).execute(arg, client)
		 * 
		 * for each command. The point being that I can reduce the amount of
		 * work I need to add a new command to virtually nil if I use existing
		 * features. In fact that bit just above doesn't need any changing.
		 * Although I might need to explicity handle aliases better.
		 */
		addCommand("@access",   new AccessCommand());   //
		addCommand("attack",    new AttackCommand());   //
		//addCommand("cast",      new CastCommand());     //
		addCommand("greet",     new GreetCommand());    //
		addCommand("mail",      new MailCommand());     //
		addCommand("say",       new SayCommand());      //
		addCommand("staff",     new StaffCommand());    //
		addCommand("@teleport", new TeleportCommand()); //
		
		// TODO not sure the best way to store/access aliases for Command objects
		addAlias("attack", "kill");
		
		debug("Mapped Commands: " + commandMap.keySet()); // Print out all the command mappings (DEBUG)

		/* Set up Command Aliases */
		loadAliases(this.ALIASES_FILE);
		
		for (final String alias : aliases.keySet()) {
			String command = aliases.get(alias);
			
			if( c.isMax() ) {
				System.out.println( sb.toString().trim() );
				sb.delete(0,  sb.length());
				c.reset();
			}
			else {
				sb.append(Utils.padRight(alias + " -> " + command, ' ', 26)).append(" ");
				c.increment();
			}
		}

		// cleanup
		System.out.println( sb.toString().trim() );
		sb.delete(0,  sb.length());
		c.reset();

		debug("Aliases: " + aliases.keySet());

		// TODO rewrite board loading as state based system and change format of files

		/* bulletin board(s) */
		loadBoards();
		
		/* auctions ? */
		auctions = new ArrayList<Auction>();

		debug("");
		
		debug("");
		debug("");
		
		chan.makeChannel(Constants.STAFF_CHANNEL);
		chan.makeChannel(Constants.OOC_CHANNEL);
		
		loadChannels(CHANNELS_FILE); // load chat channels
		
		debug("Loaded chat channels.");
		
		debug("");

		if ( use_accounts ) {
			debug("Loading Accounts...");

			loadAccounts(ACCOUNT_DIR); // load Player Accounts

			debug("Done");

			debug("");
			
			if (acctMgr.numAccounts() == 0) {
				debug("No accounts.");
			}
		}

		// Initialize Program Interpreter
		this.pgm = new ProgramInterpreter(this, true);

		// Coins.debug();

		debug("");

		// TODO resolve the problems that make this not work (which are?)
		if ( module != null ) {
			debug("Module: " + module.getName());
			
			// initialize module
			module.init(DATA_DIR);
			
			// "install" ruleset
			final Ruleset modRS = module.getRuleset();

			// just going to be extra careful here...
			if (modRS != null) {
				debug("Ruleset: " + modRS.getName());

				if( module.hasClasses() ) {
					// TODO debug?
					System.out.println(Arrays.asList(modRS.getClasses()));

					for (final PClass pcl : modRS.getClasses()) {
						numPlayersOnlinePerClass.put(pcl, 0);
					}
				}
			}
			
			debug("");
		}

		/* Testing */
		if( testing ) {
			// TODO is this necessary
			/*do {
				System.out.println("Waiting...");
			}
			while( !loader.isLoaded() );*/

			synchronized(objectDB) {
				// world specific testing and pre-load
				// probably not valid on your database
				world_test();

				// set up basic weather system of sorts
				// should generally be okay on a new database
				weather_test();

				// spawns a bunch of creatures randomly
				//spawn_test();

				// add some arbitrary scripted commands
				scripted_command_test();
				
				debug("Mapped Commands (+ scripted cmd test): " + commandMap.keySet()); // Print out all the command mappings (DEBUG)
			}
		}

		// almost everything that needs to be loaded should be done before here
		debug("Creating server on port " + port);
		
		// TODO where does this belong
		// initialize player array
		this.players = new ArrayList<Player>(max_players);
		
		// start threads
		// Time Loop
		// cpu: -for now, appears marginal-
		final Date startDate = new Date(month, day, year);
		final Time startTime = new Time(game_hour, game_minute, 0);

		game_time = new TimeLoop(this, DAYS, startDate, startTime);

		startThread( game_time, "time" );

		debug("Time (Thread) Started!");

		// Weather Loop
		// cpu: ~20%
		// System.out.println("Weather (Thread) Started!");

		// NOTE: if commands are unqueued then an error may hang the game
		if (queued_commands) {
			cmdQueue = new ConcurrentLinkedQueue<CMD>();
			cmdExec = new CommandExec(this, cmdQueue);

			startThread( cmdExec, "command_exec" );

			debug("Command Execution (Thread) Started!");
		}
		
		/* Server Initialization (network level) */

		// TODO convert printlns to debug calls
		
		// TODO it'd be good to caught server launch failures in here, maybe?
		this.s = new Server(this, port);
		
		startThread( this.s, "server" );
		
		// wait for the server to start, 1 second at a time
		/*do {
			synchronized(this) {
				try                            { wait(1000); }
				catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
		while( !s.isRunning() );*/
		
		debug("Server (Thread) Started!");
		
		debug("");

		debug("Server> Setup Done.");

		debug("Next Database Reference: " + objectDB.peekNextId());

		debug(Utils.checkMem());

		debug("");
		
		// Initialize Console Monitor?
		if( console_enabled ) {
			cmon = new ConsoleMonitor(this);
		}
		
		loot_test();
	}

	// main loop
	private void run() {
		debug("Entering main program loop...");
		debug("Running? " + this.running); // tell us whether the MUD server is running or not
		//debug("Server? " + s.isRunning()); // tell us whether the underlying socket server is running
		
		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		while( !s.isRunning() );
		
		debug("Server? " + s.isRunning()); // tell us whether the underlying socket server is running

		while (running) {
			if (!input_hold) {
				// try to reduce usage of cpu time
				try {
					Thread.sleep(500);
				}
				catch(final InterruptedException ie) {
					debug( ie );
				}
				
				for (final Client client : s.getClients()) {
					runHelper(client);
				}
				
				// execute any relevant main code from the module
				module.run();

				/* Notification Messages */

				// if we aren't delivering immediate notifications, then they'll be handled here
				if ( !notify_immediate ) {
					// grab and send one notification per player this loop iteration (if they have any)
					for (final Player player : messageQueues.keySet()) {
						final String message = messageQueues.get(player).poll();

						if (message != null) {
							send(message, player.getClient());
						}
					}
				}

				// TODO resolve major inefficiencies around delivering chat messages that amount to substantial cpu usage

				/* Chat Messages */
				Client client = null;
				Message msg = null;

				String chan_name = "";
				String chan_color = "";
				String sender_color = "";
				String text_color = "";

				// for each ChatChannel
				for (final ChatChannel cc : chan.getChatChannels()) {
					// debug("CHANNEL: " + cc.getName(), 4);

					chan_name = cc.getName();
					chan_color = cc.getChanColor();
					sender_color = cc.getSenderColor();
					text_color = cc.getTextColor();
					
					msg = cc.getNextMessage();
					
					if (msg == null) {
						//debug(chan_name + ": No Messages", 4);
						continue;
					}
					
					Triple<String> colors = new Triple<String>(chan_color, sender_color, text_color);
					
					final String cm = buildChatMessage(chan_name, colors , msg, true, false);
					final String cm_tag = buildChatMessage(chan_name, colors, msg, true, true);
					final String cm_log = buildChatMessage(chan_name, colors, msg, false, false); // no color for logs
					
					// for each listener of this channel, send the message
					for (final Player player : cc.getListeners()) {
						try {
							client = player.getClient();

							if (player.getConfigOption("tagged-chat")) client.write(cm_tag);
							else                                       client.write(cm);
							
							logChat( cm_log );

							debug("chat message sent successfully", 4);
						}
						catch (final NullPointerException npe) {
							debug("Game [chat channel: " + chan_name + "] > Null Message.");
							debug( npe );
						}
					}
				}
			}
		}
	}
	
	private void runHelper(final Client client) {
		final String client_state = getClientState(client);
		
		// telnet command handling?
		Byte[] msg = client.getTelnetMessage();

		if( msg.length > 0 ) {
			byte[] ba = new byte[msg.length];

			int index = 0;

			for (Byte b : msg) {
				ba[index] = b;
				index++;
			}

			String message = Telnet.translate(ba);

			System.out.println("] " + message);
			
			System.out.println("TELNET Response");
			
			if( message.equals("IAC WILL NAWS") ) {
				client.NAWS = true;
				client.write( Telnet.translate("IAC DONT NAWS") );
			}
			
			/*if( message.equals("IAC SB NAWS") ) {
				client.tn_cmd = false;
				client.tn_subneg = true;
			}
			
			if( message.equals("IAC SE") ) {
				client.tn_cmd = true;
				client.tn_subneg = false;
				
				client.tn_neg_seq = false;
			}*/
		}
		

		// TODO fix this right here (5-10-2015), prevents my other stuff from working
		if (client_state != null) {
			if (client_state.equals("interactive_login")) {
				final String temp = client.getResponse();

				if (temp != null && !temp.equals("")) {
					System.out.println("Interactive Login, temp: " + temp);
					
					client.setResponseExpected(false);

					// TODO figure out why I echo this here
					// TODO resolve the problem that when I fail to echo here
					// that I need newlines for formatting purposes
					echo(temp, client);

					// get client data
					final LoginData ld = getLoginData(client);

					if (ld != null) {
						System.out.println("Interactive Login, STATE: " + ld.state);
						
						if ( ld.state.equals("USERNAME") ) {
							ld.username = temp;
							ld.state = "PASSWORD";
							
							// enter next stage
							handle_interactive_login(client);
						}
						else if ( ld.state.equals("PASSWORD") ) {
							ld.password = temp;
							ld.state = "LOGIN";
							
							// enter next stage
							handle_interactive_login(client);
						}
						else if( ld.state.equals("LOGIN") ) {
							// TODO need to be careful about printing out password in plain text
							System.out.println("NAME:  " + ld.username);
							System.out.println("PASS:  " + Utils.padRight("", '*', ld.password.length()));
							System.out.println("STATE: " + ld.state);
							
							// enter next stage
							handle_interactive_login(client);
						}
					}
					else {
						System.out.println("CLIENT DATA is NULL.");
					}
				}
			}
		}

		final String whatClientSaid = client.getInput();
		
		try {
			// TODO should idling be managed on a timer/tick?
			final Player player = getPlayer(client);

			if (player != null) {
				handle_idle_player(player, whatClientSaid);
			}
			
			// If the client is not null and has something to say
			if (whatClientSaid != null) {
				System.out.println("what client said: \'" + whatClientSaid + "\'");
				
				// TODO fix this, there may not be a player yet and so we can't check their config options
				/*if( echo_enabled && player.getConfigOption("server_echo") ) {
					echo(whatClientSaid, client);
				}*/
				
				if( console_enabled ) {
					// check to see if we are a console user
					if( cmon.hasConsole(client) ) {
						debug("CONSOLE");

						final Console console = cmon.getConsole(client);

						if (console != null) console.processInput(whatClientSaid.trim());
						else                 send("Console not initialized?", client);

						return;
					}
				}
				
				if ( queued_commands ) {
					debug("Putting comand in command queue.");

					CMD cmd = new CMD(whatClientSaid.trim(), player, client);

					cmdQueue.add(cmd);
				}
				else {
					debug("Evaluating command.");

					try {
						cmd(whatClientSaid.trim(), client);
					}
					catch (final NullPointerException npe) {
						debug("runHelper(): command processing issue (exception) [!queued_commands]");
						// usurp the junk below and catch any exceptions here
						debug( npe );
					}
				}
			}

			// flush players -- clean up broken connections;
			//flush(); // not sure what the point of this is...

			Thread.sleep(500); // wait 5/10 of a second
		}
		catch (final InterruptedException ie) {
			debug("runHelper(): thread interrupted? (interrupted exception)");
		}
		catch (final Exception e) {
			/*
			 * until I resolve the issue of the possibility that any command
			 * that goes wrong catastrophically and unresolvably could crash the
			 * server, we should assume that any exception is a fatal exception
			 * and reboot, either exiting or reloading from the last save
			 * automatically.
			 * 
			 * hence this code here should report the error, then exit or wait a
			 * pre-determined amount of time and then attempt a reboot.
			 * 
			 * NOTE: I really need a separate thread or means for executing
			 * stuff so that bugs in a command only cause a real problem for the
			 * player who tried to use it. probably the quick solution should be
			 * booting the player and having them lose all their progress since
			 * the last save. Tentatively, if I can find a way to verify that
			 * the error is not going to effect them, I can save their
			 * character, tell them to reconnect in a little while and kick
			 * them. A live character restore system would be really awesome but
			 * I'm sure how it would integrate with any system fitting my
			 * description above.
			 */

			debug("Exception(RUN): " + e.getMessage());
			
			debug( e );

			send("Game> Fatal Exception! Halting...");

			/*
			 * - halt command interpretation (if it isn't mangled already)
			 * - clear command queue
			 * - send a message to everyone (if possible)
			 * - disconnect players (saving if possible)
			 * - backup session data
			 * - stop threads
			 * - restart server
			 * - start threads
			 */

			send("Pausing game!");

			input_hold = true; // Halt Player Input
			game_time.pauseLoop(); // Pause the time tracking
			
			// Put the game into Maintenance mode (no new logins, except Wizards)
			changeMode(GameMode.MAINTENANCE);
			
			send("Entering " + mode.toString() + " Mode.");
			
			// do cleanup
			
			// need to use some scratch space to retain data to copyover so I can reinstantiate the server

			//restart?
			// MUDServer.main(new String[] {"--port=4202", "--debug"} );
		}
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
			if (checkAccess(getPlayer(client), newCmd.getPermissions())) {
				cmd(command, client);
			}
			else {
				System.out.println("Insufficient Access Permissions");
			}
		}
		catch (final Exception e) {
			debug("processCMD(): ? (exception)");
			debug( e );
		}

		if (loginCheck(client)) {
			System.out.println("Client associated with login");
			prompt(client);
		}
	}
	
	private String buildChatMessage(String channelName, Triple<String> colorData, Message msg, Boolean color, Boolean tagged) {
		final StringBuilder sb = new StringBuilder();
		
		final String chanColor = colorData.one;
		final String senderColor = colorData.two;
		final String textColor = colorData.three;
		
		// TODO is there a better way to handle a null sender?
		
		if( tagged ) sb.append("CHAT ");
		
		if( color ) sb.append("(" + colors(channelName, chanColor) + ")");
		else        sb.append("(" + channelName + ")");
		
		sb.append(" ");
		sb.append("<" + colors((msg.getSender() != null ? msg.getSender().getName() : "System"), senderColor) + ">");
		sb.append(" ");
		
		if( color ) sb.append(colors(msg.getMessage(), textColor));
		else        sb.append(msg.getMessage());
		
		sb.append("\r\n");
		
		return sb.toString();
	}
	
	/**
	 * Create the very basic data files needed for a new game.
	 * 
	 * NOTE: commented out stuff to keep from blowing manually generated data away
	 */
	private void create_data() {
		// Error Message Localization
		final File errors_en = new File( resolvePath(DATA_DIR, "errors_en.txt") );
		final File errors_fr = new File( resolvePath(DATA_DIR, "errors_fr.txt") );
		
		if( !errors_en.exists() ) {
			Utils.saveStrings( resolvePath(DATA_DIR, "errors_en.txt"), new String[] { "1:Invalid Syntax!", "2:NaN Not a Number!" } );
		}
		if( !errors_fr.exists() ) {
			Utils.saveStrings( resolvePath(DATA_DIR, "errors_fr.txt"), new String[] { "1:Syntaxe Invalide!", "2:NaN N'est pas un nombre!" } );
		}
		
		// generate blank/basic config files
		final File aliases =   new File( resolvePath(CONFIG_DIR, "aliases.conf") );
		final File banlist =   new File( resolvePath(CONFIG_DIR, "banlist.txt") );
		final File channels =  new File( resolvePath(CONFIG_DIR, "channels.txt") );
		final File forbidden = new File( resolvePath(CONFIG_DIR, "forbidden.txt") );
		final File bboard =    new File( resolvePath(BOARD_DIR, "bboard.txt") );

		if( !aliases.exists() ) {
			Utils.saveStrings(
					resolvePath(CONFIG_DIR, "aliases.conf"),
					new String[] {
							"# Command Aliases File",
							"alias north:n", "alias northeast:ne", "alias northwest:nw",
							"alias south:s", "alias southeast:se", "alias southwest:sw",
							"alias east:e", "alias west:w",
							"alias inventory:inv,i", "alias look:l", "alias pconfig:pconf", "alias quit:QUIT"
					}
					);
		}
		
		if( !banlist.exists() ) {
			Utils.saveStrings( resolvePath(CONFIG_DIR, "banlist.txt"), new String[] { "# Banlist", "0.0.0.0" });
		}
		
		if( !channels.exists() ) {
			Utils.saveStrings( resolvePath(CONFIG_DIR, "channels.txt"), new String[] { "Support,0", "Testing,0" });
		}
		
		if( !forbidden.exists() ) {
			Utils.saveStrings( resolvePath(CONFIG_DIR, "forbidden.txt"), new String[] { "# Forbidden names list", "shit" });
		}

		// generate an single, default message for the mud-wide bulletin board
		if( !bboard.exists() ) {
			Utils.saveStrings( resolvePath(BOARD_DIR, "bboard.txt"), new String[] { "0#admin#Welcome#Test Message" });
		}

		final String[] theme = new String[] {
				"[theme]",
				"name = MUD",
				"mud_name = NewMUD",
				"motd_file = motd.txt",
				"start_room = 0",
				"world = new-world",
				"db = db.txt",
				"[/theme]",
				"",
				"[calendar]",
				"day = 1", "month = 1", "year = 0", "season = summer",
				"[/calendar]",
				"",
				"// number = name",
				"[months]",
				"1 = January", "2 = February", "3 = March", "4 = April", "5 = May", "6 = June",
				"7 = July", "8 = August", "9 = September", "10 = October", "11 = November", "12 = December",
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
		
		Utils.saveStrings( resolvePath(THEME_DIR, "new.thm"), theme);
		
		/* Create new world folder and files */
		
		// generate an empty world directory and necessary sub directories
		final File world_d = new File( resolvePath(WORLD_DIR, "new-world") );
		final File mail_d = new File( resolvePath(WORLD_DIR, "new-world", "mail") );
		final File motd_d = new File( resolvePath(WORLD_DIR, "new-world", "motd") );

		// check that the directories exist, if not create them
		for (final File dir : Utils.mkList(world_d, mail_d, motd_d)) {
			if (!dir.exists()) {
				boolean success = dir.mkdir();

				if (success) System.out.println("Directory: " + dir + " created");
			}
			else {
				System.out.println("Directory: " + dir.getAbsolutePath() + " exists.");
			}
		}

		// generate blank motd
		String[] motdData = new String[] {
				"*** Welcome to:",
				"",
				"<insert mud name or initial graphic here>",
				"",
				"<other info>",
				"",
				"<connection details>",
				"",
				"To connect to your character use 'connect <playername> <password>'",
				"To create a character use 'create <playername> <password>'"
		};

		Utils.saveStrings( resolvePath(WORLD_DIR, "new-world", "motd", "motd.txt"), motdData);

		// create plain, mostly empty database (contains a single room and an admin player)
		final Room room = createRoom("An Empty Room", "You see nothing.", 0);
		room.setDBRef(0);

		final Player admin = new Player(1, "admin", Utils.hash("password"), 0);
		
		admin.setDesc("admin character");
		admin.setAccess(Constants.SUPERUSER);
		admin.setRace(Races.DRAGON); // 0 in Races.java
		admin.setPClass(Classes.NONE);
		
		String[] dbData = new String[] {
				room.toDB(),
				admin.toDB()
		};
		//String[] dbData = new String[2];
		//dbData[0] = room.toDB();
		//dbData[1] = admin.toDB();

		Utils.saveStrings( resolvePath(WORLD_DIR, "new-world", "db.txt"), dbData);
		
		// creates races file
		String[] races = new String[] {
				"[",
				"{",
				"\"name\": \"Human\",",
				"\"subrace\": null,",
				"\"id\": 0,",
				"\"statAdj\": [ 0, 0, 0, 0, 0, 0 ],",
				"\"restricted\": false,",
				"\"canFly\": false",
				"},",
				"{",
				"\"name\": \"None\",",
				"\"subrace\": null,",
				"\"id\": 8,",
				"\"statAdj\": [ 0, 0, 0, 0, 0, 0 ],",
				"\"restricted\": true,",
				"\"canFly\": false",
				"}",
				"]"
		};
		
		Utils.saveStrings( resolvePath(WORLD_DIR, "new-world", "races.json"), races );
		
		// create an empty zones file
		String[] zones = new String[] { "# Zones" };

		Utils.saveStrings( resolvePath(WORLD_DIR, "new-world", "zones.txt"), zones );
		
		/*
		// create topics directory
		final File temp1 = new File(TOPIC_DIR);

		if (!temp1.exists()) {
			boolean success = temp1.mkdir();

			if (success) {
				System.out.println("Directory: " + temp1.getAbsolutePath() + " created");
			}
		}
		else {
			System.out.println("Directory: " + temp1.getAbsolutePath() + " exists.");
		}
		 */
	}

	private void conversation_test() {
		// TODO remove later
		Script script = new Script("{test:5}");
		
		CNode opt1 = new CNode(1, "What's up?", "Nothing. Much");
		CNode opt2 = new CNode(2, "I'd like to buy something", "Sure.", null, true, script);
		CNode opt3 = new CNode(3, "Bye.", "See ya.", null, true);

		opt1.addOptions(opt1, opt2, opt3);

		// add conversations?
		for (final NPC npc : getNPCList()) {
			if (npc != null) {
				// TODO rip this out later, it doesn't really belong here

				// generate and add conversations
				CNode opt0 = new CNode(0, "", npc.greeting, Utils.mkList(opt1, opt2, opt3));

				npc.setConversation(opt0);
			}
		}
	}

	private void weather_test() {
		/* Weather Testing -- DB Safe */

		use_weather = true;

		// create some weather states
		WeatherState ws1 = new WeatherState("Clear Skies", 1, false, false, false, false);
		ws1.setDescription("The sky is clear{DAY? and blue}{NIGHT? and flecked with stars. Moonlight faintly illuminates your surroundings}.");
		ws1.transUpText = "Your surroundings brighten a little as the {DAY?sun}{NIGHT?moon} peeks through thinning clouds.";

		WeatherState ws2 = new WeatherState("Cloudy", 0.5, false, false, true, false);
		ws2.setDescription("The air is dry for now, but clouds cover the sky.  It might rain soon.");
		ws2.transDownText = "It's getting cloudy.";
		ws2.transUpText = "The rain seems to have stopped for now.";

		WeatherState ws3 = new WeatherState("Rain", 0.25, true, false, true, false);
		ws3.setDescription("Above the pouring rain hangs a gray and solemn sky.");
		ws3.transDownText = "Rain begins to spot your surroundings.";
		ws3.transUpText = "The flashes of lightning taper off as the thunder goes quiet, the sound fading till all that can be heard it the pouring rain.";

		WeatherState ws4 = new WeatherState("Thunderstorm", 0, true, true, true, true);
		ws4.setDescription("Thunder and lightning light up the sky, punctuating the sound of heavy rain.");
		ws4.transDownText = "You hear a the boom of thunder and catch a glimpse of a sudden flash in the distance.";

		WeatherState ws5 = new WeatherState("Winter Storm", 0, false, false, false, true);
		ws5.setDescription("Blinded by a tempest of snow swirling above, you assume the sky looms black.");
		ws5.transDownText = "Occasional gusts become a river of frigid air, the snow a blinding swirl of gray.";

		// create a seasonal weather profile, and hand it the states we created
		Season spring = new Season("Spring", 1, 3, ws1, ws2, ws3);      // 0 - 2
		Season summer = new Season("Summer", 4, 7, ws1, ws2, ws3, ws4); // 3 - 6
		Season fall = new Season("Fall", 8, 10, ws1, ws2, ws3);         // 7 - 9
		Season winter = new Season("Winter", 11, 13, ws2, ws3, ws5);    // 10 - 12

		// create a weather object, handing it our current season and a starting
		// weather state
		weather = new Weather(summer, ws4);

		// apply our new weather object to each "outside" room
		for (final Room room1 : getWeatherRooms()) {
			room1.setWeather(weather);
		}
	}
	
	private void spawn_test() {
		// Randomly spawn some creatures
		Random random = new Random( System.nanoTime() );

		List<Room> outside_rooms = objectDB.getRoomsByType(RoomType.OUTSIDE);

		for(int n = 0; n < 100; n++) {
			int index = random.nextInt( outside_rooms.size() );

			final Room target = outside_rooms.get(index);

			Creature cre = new Creature("Rabbit", "A fluffy bunny rabbit.");

			objectDB.addAsNew( cre );
			objectDB.addCreature( cre );

			//World.add

			cre.setLocation( target.getDBRef() );
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
		 * NOTE: It is strongly advised to use a database name other than the
		 * ones noted above to avoid triggering the following special setup
		 * intended just for those, which also expects classes not provided on
		 * GitHub with the rest of the code.
		 */

		/* FOE Items Testing -- NOT DB Safe */
		if ( moduleLoaded("FOE") && DB_FILE.endsWith("foe.txt")) { // Fallout Equestria testing database (this code uses stuff from a package not included on github)
			debug("Fallout Equestria (FOE) Item Testing setup");
			
			// TODO need to prototype all these things or something and then just make an instance in the test database

			// rules = mud.foe.FOESpecial.getInstance();

			System.out.println("Abilities: " + Arrays.asList(rules.getAbilities()));

			// pull in prototypes
			prototypes.putAll( module.getItemPrototypes() );
			prototypes1.putAll( module.getThingPrototypes() );
			
			// TODO greetings really should be persisted with npcs
			
			/* Set NPC Greetings (bit of a hack, really) */
			getNPC("Life Bloom").setGreeting("Welcome to Tenpony tower.");
			getNPC("Ditzy Doo").setGreeting("Ditzy Doo smiles at you, which is oddly nice and also looks gross.");

			Room atrium = getRoom("Atrium");
			Room atrium2 = getRoom("Atrium2");
			Room it = getRoom("IT Center");

			atrium2.setProperty(
					"/visuals/grass",
					"On a closer inspection, the \"grass\" is made of an unknown substance,probably inorganic. Looks pretty good, still you probably wouldn't want to eat it.");

			// Safe
			Box box = new Box("Safe", "A heavy metal safe");

			box.lock();

			initCreatedThing(box);
			box.setLocation(atrium2.getDBRef());
			atrium2.addThing(box);
			
			// PipBuck (foe version of PipBoy from Bethesda's Fallout video games)
			// Item pb = createItem("mud.foe.pipbuck", false);

			// initCreatedItem(pb);

			// pb.setLocation(box.getDBRef());
			// pb.setLocation( atrium2.getDBRef() );
			// box.insert(pb);
			// atrium2.addItem( pb );

			// StealthBuck - PipBuck Stealth/Invis Module
			mud.foe.items.StealthBuck sb = new mud.foe.items.StealthBuck();

			initCreatedItem(sb);

			// sb.setLocation(box.getDBRef());
			sb.setLocation(atrium2.getDBRef());
			// box.insert(sb);
			atrium2.addItem(sb);

			// Disruptor - PipBuck Disruptor Module
			mud.foe.items.Disruptor dr = new mud.foe.items.Disruptor();

			initCreatedItem(dr);
			// dr.setLocation(box.getDBRef());
			dr.setLocation(atrium2.getDBRef());
			// box.insert(dr);
			atrium2.addItem(dr);

			// Sparkle Cola Vending Machine
			Thing vending_machine = new Thing(
					"Vending Machine",
					"What stand before you is a grime-coated relic of it's former glory. The once glorious purple and gold Sparkle Cola ad has "
							+ "long since faded, though a few splotches of color remain to remind you of it's former state. In several spots the paint has begun peeling off the metal "
							+ "and rust peeks out from beneath it.");

			initCreatedThing(vending_machine);

			vending_machine.setProperty("thingtype", "machine");
			vending_machine.setProperty("machinetype", "vending_machine");
			vending_machine.setProperty("inventory/sparkle_cola", 10);
			vending_machine.setProperty("inventory/sparkle_cola_rad", 10);
			vending_machine.setProperty("selection/0", "sparkle_cola");
			vending_machine.setProperty("selection/1", "sparkle_cola_rad");
			vending_machine.setProperty("money", 0);

			// {if:{eq:{prop:money,291},
			// 1},{give:{&player},{create_item:mud.foe.sparkle_cola}},Insufficient
			// Money!}
			// {tell:{&player},{&this} dispenses a bottle of Sparkle-Cola}

			vending_machine.setProperty("money", 20);

			// IF has money && sparkle_cola are valid drink/exist &&
			// sparkle_colas > 0 THEN create new sparkle_cola and give to player
			// and decrease money by 3 bits and decrease
			// count of remaining sparkle_cola by 1

			// TODO resolve issue with scripting system that makes it not work
			// when you want to execute
			// three functions in succession
			/*vending_machine.setScriptOnTrigger( TriggerType.onUse,
					"{if:{ge:{prop:money,{&this}},1},{if:{ge:{prop:inventory/sparkle_cola,{&this}},1},{do:{tell:{colors:green,Enough Money!},{&player}},{store:money,{&this},{sub:{prop:money,{&this}},1}},{store:inventory/sparkle_cola,{&this},{sub:{prop:inventory/sparkle_cola,{&this}},1}},{tell:PCHING! A bottle of sparkle cola!,{&player}},{give:{&player},{create_item:mud.foe.sparkle_cola}}},{tell:Sold Out!,{&player}}},{tell:{colors:red,Insufficient Funds!},{&player}}}");
			*/
			vending_machine.setScriptOnTrigger(TriggerType.onUse, ""
					+ "{do:{set:money,{prop:money,{&this}}},{set:stock,{prop:inventory/sparkle_cola,{&this}}},{if:{ge:{&money},1},{if:{ge:{&stock},1},{do:{tell:{colors:green,Enough Money!},{&player}},{set:money,{sub:{&money},1}},{store:money,{&this},{&money}},{set:stock,{sub:{&stock},1}},{store:inventory/sparkle_cola,{&this},{&stock}},{tell:PCHING! A bottle of sparkle cola!,{&player}},{give:{&player},{create_item:mud.foe.sparkle_cola}}},{tell:Sold Out!,{&player}}},{tell:{colors:red,Insufficient Funds!},{&player}}}}");

			System.out.println("# of Sparkle Cola(s) left: " + vending_machine.getProperty("inventory/sparkle_cola", Integer.class));
			System.out.println("# of Sparkle Cola Rad(s) left: " + vending_machine.getProperty("inventory/sparkle_cola_rad", Integer.class));

			vending_machine.setLocation(atrium2.getDBRef());
			atrium2.addThing(vending_machine);

			// createItem("Notebook", ItemTypes.NONE,
			// "A blank paper notebook, a remnant of pre-war Equestria.");

			Book notebook = new Book();
			notebook.setName("Notebook");
			notebook.setDesc("A blank paper notebook, a remnant of pre-war Equestria.");
			
			notebook.setTitle("Notebook");

			initCreatedItem(notebook);
			
			notebook.setLocation(atrium2.getDBRef());
			atrium2.addItem(notebook);
			
			Book wsg = new Book("Wasteland Survival Guide", "Ditzy Doo", 250);
			wsg.setName("The Wasteland Survival Guide");
			wsg.setDesc("A modestly thick, black book with a white equine skull on the cover.");
			
			wsg.setAuctionable(false);

			wsg.addPage(
					Utils.mkList(
							"Chapter 1: Basics of Pony Biology",
							"",
							"The things that everypony should know:",
							"- water, I need it, you need it, the raiders need it. Absolutely Essential.",
							"- food, same as with water, easier to find though. see chapter 3"));
			wsg.addPage(
					Utils.mkList("Chapter 2: Dangers of the Wasteland",
							"You want dangers, we've got them all, right here for you."));
			wsg.addPage(
					Utils.mkList(
							"Raiders",
							"",
							"A bunch of nasty ponies if there ever was such a thing. They want your stuff and they'll happily",
							"end your life to get it. And that's just if you're lucky. Be an unlucky pony and you'll end up enslaved.",
							"Thing is, that wouldn't be so terrible, maybe, except that they'll put an explosive collar on you.",
							"Move two steps out of line and *BOOM* no more head. Yeah, loss of your head is fatal."));
			wsg.addPage(
					Utils.mkList(
							"Yao Guai",
							"",
							"Ah, for the golden days of Old Equestria. Once just ordinary Ursas,",
							"until your favorite friend, magical radiation, did a stellar job of",
							"creating an A-1 menace."));
			
			wsg.addPage(
					Utils.mkList("Chapter 3: Food",
							"We're very lucky that pre-war Equestrians we're so diligent about preparing for"
							+ "the apocalypse. They died too quickly to care about their wasted effort, but"
							+ "thanks to them us few Wastelanders can survive for a very long time on their stockpiles"
							+ "Sure, it's hardly the most nutritious stuff compared to fresh food (I would know), but"
							+ "it's tastier than radroaches and less irradiated, most of the time."));

			initCreatedItem(wsg);
			wsg.setLocation(atrium2.getDBRef());
			atrium2.addItem(wsg);

			Thing pipbuck_machine = new Thing(
					"Pipbuck Machine",
					"A well-preserved and clean, but slight rusty machine. There is an"
							+ "inactive Stable-tec terminal embedded in it and below that a circular receptacle. Above the circular hole"
							+ "there is a hoof shape engraved into the metal plate that serves as the front of the machine. Flecks of black"
							+ "stuff seem to suggest that perhaps it was once filled in with paint for more contrast. Above the symbol the"
							+ "words \"Insert Hoof Here\" are engraved.");

			pipbuck_machine.setProperty("thingtype", "machine");
			pipbuck_machine.setProperty("machinetype", "pipbuck_machine");
			pipbuck_machine.setProperty("contents/pipbuck", 1000);

			// pipbuck_machine.getDBRef()
			pipbuck_machine.setScriptOnTrigger(TriggerType.onUse,
					"{if:{gt:{prop:contents/pipbuck,{&this}},0},{do:{give:{&player},{create_item:mud.foe.pipbuck}},{tell:You cautiously stick your hoof into the hole.,{&player}}},{tell:Insufficient Pipbucks Available!,{&player}}}");

			initCreatedThing(pipbuck_machine);
			
			pipbuck_machine.setLocation(it.getDBRef());
			it.addThing(pipbuck_machine);

			System.out.println("# of PipBuck(s) left: " + pipbuck_machine.getProperty("contents/pipbuck", Integer.class));

			/*
			 * mud.foe.Terminal terminal = new mud.foe.Terminal("Terminal");
			 * terminal.setName("Terminal"); terminal.setDesc(
			 * "A Stable-Tec terminal, old pre-war technology whose durability is plain to see. On the screen, passively glowing green text indicates that it awaits input."
			 * ); terminal.setPowerState(mud.foe.Terminal.pwr_states.POWER_ON);
			 */

			mud.foe.Terminal terminal = new mud.foe.Terminal(
					"Terminal",
					"A Stable-Tec terminal, old pre-war technology whose durability is plain to see."
					);
			
			terminal.setProperty("/visuals/screen", "passively glowing green text indicates that it awaits input");

			terminal.init();
			System.out.println( terminal.powerOn() );

			initCreatedThing(terminal);
			
			terminal.setLocation(atrium2.getDBRef());
			atrium2.addThing(terminal);

			// Spark Generator
			Thing spark_generator = new Thing("Spark Generator", "This advanced piece of magitech produces near limitless electric power via magic");

			initCreatedThing(spark_generator);
			spark_generator.setLocation(it.getDBRef());
			it.addThing(spark_generator);

			// TODO convert the above ^ to a prototype creation call

			Item bc = createItem("mud.foe.bottlecap_sc", false);

			initCreatedItem(bc);
			bc.setLocation(atrium2.getDBRef());
			atrium2.addItem(bc);

			Item memory_orb = createItem("mud.foe.memory_orb", false);

			initCreatedItem(memory_orb);
			memory_orb.setLocation(box.getDBRef());
			box.insert(memory_orb);

			Item pgun1 = createItem("mud.foe.weapons.pistol", false);

			initCreatedItem(pgun1);
			pgun1.setLocation(atrium2.getDBRef());
			atrium2.addItem(pgun1);

			// particular item
			// Weapon gun = (Weapon) new Item(-1);
			// Item gun = new Item(-1);
			Weapon gun = new Weapon(
					"Gun",
					"A sturdy revolver with a mouth grip, clearly of earth pony make or at least designed for one."
					);

			// gun.setWeaponType(WeaponType.REVOLVER);
			
			gun.setProperty("name", "Little Macintosh");
			gun.setProperty("visual/engraving/number", "IF-18");
			gun.setProperty("visual/engraving/script", "Little Macintosh");
			gun.setProperty("weapon/maker", "Ironshod Firearms");
			gun.setProperty("weapon/model", "IF-18");
			gun.setProperty("weapon/size", "small");
			gun.setProperty("weapon/type", "revolver");
			gun.setProperty("damage", 5);
			gun.setProperty("ammo_size", 0.44);
			gun.setProperty("ammo_type", "magnum");
			gun.setProperty("magazine", false);
			gun.setProperty("chambers", 6);

			initCreatedItem(gun);
			gun.setLocation(box.getDBRef());
			box.insert(gun);

			Weapon laser_rifle = (Weapon) createItem("mud.foe.laser_rifle", false);

			initCreatedItem(laser_rifle);
			laser_rifle.setLocation(atrium2.getDBRef());
			atrium2.addItem(laser_rifle);

			Weapon laser_rifle1 = (Weapon) createItem("mud.foe.laser_rifle", false);

			initCreatedItem(laser_rifle1);
			laser_rifle1.setLocation(atrium2.getDBRef());
			atrium2.addItem(laser_rifle1);
			
			Armor battle_saddle = new Armor("Battle Saddle", "");

			battle_saddle.setProperty("gun1", -1);
			battle_saddle.setProperty("gun2", -1);

			initCreatedItem(battle_saddle);
			battle_saddle.setLocation(atrium2.getDBRef());
			atrium2.addItem(battle_saddle);

			// laser_rifle.setLocation(battle_saddle.getDBRef());
			// laser_rifle1.setLocation(battle_saddle.getDBRef());
			// battle_saddle.setProperty("gun1", laser_rifle.getDBRef());
			// battle_saddle.setProperty("gun2", laser_rifle1.getDBRef());

			Weapon wing_blades = (Weapon) createItem("mud.foe.wing_blades", false);

			initCreatedItem(wing_blades);

			wing_blades.setLocation(atrium2.getDBRef());
			atrium2.addItem(wing_blades);
			
			// Bank Test -- DB Safe
			Bank bank = new Bank("Bank of Equestria");
			
			banks.put(bank.getName(), bank);
			
			BankAccount acct = new BankAccount(0, Coins.platinum(1000));
			
			bank.addAcount(0, acct);
		}

		/* Item/Creature/Quest Testing -- NOT DB Safe (expects a room to exist, etc) */

		// if( mainDB.endsWith("db.txt") ) { -- this is the right one but it
		// triggers a mess with a brand new db file (called db.txt) that doesn't contain the expected data
		if ( moduleLoaded("DND35") && world.equals("forgotten_realms") && ( DB_FILE.endsWith("db.txt") || DB_FILE.endsWith("new.txt") ) ) {
			/*
			 * // Arrow Testing -- Not DB Safe Arrow a = new Arrow();
			 * initCreatedItem(a); a.setLocation( 25 ); getRoom( 25
			 * ).addItem(a);
			 * 
			 * for(int i = 0; i < 5; i++) { Arrow b = new Arrow();
			 * initCreatedItem(b); b.setLocation( 25 ); getRoom( 25
			 * ).addItem(b); }
			 * 
			 * // Item Testing -- Not DB Safe Jewelry ring = new
			 * Jewelry(ItemTypes.RING, "Ring of Invisibility",
			 * "A medium-sized gold ring with a smooth, unmarked surface.", new
			 * Effect("invisibility")); ring.setItemType(ItemTypes.RING);
			 * ring.setEquipType(ItemTypes.RING); // the type of equipment it is
			 * debug("Item Type: " + ring.getItemType() + " Equip Type: " +
			 * ring.getEquipType() ); initCreatedItem(ring); ring.setLocation( 0
			 * ); getRoom(0).getItems().add(ring);
			 * 
			 * Jewelry circlet = new Jewelry(ItemTypes.NONE, "Copper Circlet",
			 * "", new Effect("none")); circlet.setItemType(ItemTypes.RING);
			 */

			// Item properties testing -- Not DB Safe Item item =
			Item ore = createItem(
					"Copper Ore",
					"A chunk of copper ore. Veins of copper swirl through the baser rock surrounding them.",
					start_room
					);
			
			Item ore2 = createItem(
					"Iron Ore",
					"A chunk of iron ore. Bands of reddish brown are intertwined with darker gray spots.",
					start_room
					);
			
			Room ore_loc = null;
			int n = 0;
			
			ore_loc = getRoom( ore.getLocation() );
			
			n = 5;
			
			while( n > 0 ) {
				Item oreC = ore.getCopy();
				
				oreC.setProperty("type", "ore");
				oreC.setProperty("material", "copper");
				oreC.setProperty("purity", "0.90");
				
				initCreatedItem(oreC);
				
				oreC.setLocation( ore_loc.getDBRef() );
				ore_loc.addItem( oreC );
				
				n--;
			}
			
			n = 5;
			ore_loc = getRoom( ore2.getLocation() );
			
			while( n > 0 ) {
				Item oreC = ore2.getCopy();
				
				oreC.setProperty("type", "ore");
				oreC.setProperty("material", "iron");
				oreC.setProperty("purity", "0.90");
				
				initCreatedItem(oreC);
				
				oreC.setLocation( ore_loc.getDBRef() );
				ore_loc.addItem( oreC );
				
				n--;
			}
			
			//initCreatedItem(ore);
			//getRoom( ore.getLocation() ).addItem(ore);
			
			recipes.put("iron ingot", new Recipe("IRON_BAR", "iron ore"));
			recipes.put("copper ingot", new Recipe("IRON_BAR", "copper ore"));
			recipes.put("iron dagger", new Recipe("IRON_DAGGER", "iron ingot"));
			
			//Item IronIngot = createItem("Iron Ingot", "", -1);
			//prototypes.put("mud.iron_ingot", IronIngot);
			
			prototypes.put("mud.iron_ingot", createItem("Iron Ingot", "", -1));
			
			//Item CopperIngot = createItem("Copper Ingot", "", -1);
			//prototypes.put("mud.iron_ingot", IronIngot);

			prototypes.put("mud.copper_ingot", createItem("Copper Ingot", "", -1));
			
			Weapon IronDagger = new Weapon("Iron Dagger", "A sharp dagger made of iron", 0.5);
			
			IronDagger.setDamage(3);
			IronDagger.setDamageType(DamageType.SLASHING);
			
			IronDagger.setSlotType(SlotTypes.RHAND);
			
			prototypes.put("mud.iron_dagger", IronDagger);
			
			nodeList.add( new Node( new Ore("Iron", true) ) );
			
			// Bow
			
			Weapon w = new Weapon(WeaponTypes.BOW);

			w.setSlotType(SlotTypes.HANDS);

			initCreatedItem(w);
			w.setLocation( start_room );
			getRoom(start_room).addItem(w);

			Zone rdi = getZone("Red Dragon Inn");
			// Zone rdi = new Zone("Red Dragon Inn", null);
			// zones.put(rdi, 0);

			Room inn = getRoom(4); // Red Dragon Inn
			// rdi.addRoom( inn );

			Room basement = createRoom("Basement", -1);
			objectDB.addAsNew(basement);
			objectDB.addRoom(basement);

			basement.setZone(rdi);
			rdi.addRoom(basement);

			int basement_dbref = basement.getDBRef();

			// add code for exits to and from basement -- 1/6/2014
			Exit down = new Exit("down", inn.getDBRef(), basement.getDBRef());
			Exit up = new Exit("up", basement.getDBRef(), inn.getDBRef());

			objectDB.addAsNew(down);
			objectDB.addExit(down);
			objectDB.addAsNew(up);
			objectDB.addExit(up);

			inn.addExit(down);
			basement.addExit(up);

			Creature c = new Creature("Mangy Rat", "A large, scruffy gray rat with red beady eyes and pointy teeth.");
			
			c.setMaxHP(5);
			c.setLocation(basement_dbref);
			c.setRace(Races.NONE);

			Creature c1 = c.getCopy();
			Creature c2 = c.getCopy();
			Creature c3 = c.getCopy();
			Creature c4 = c.getCopy();

			for (final Creature cre : Utils.mkList(c, c1, c2, c3, c4)) {
				objectDB.addAsNew(cre);
				objectDB.addCreature(cre);
			}

			// NAME: Help the Innkeeper
			// DESC: The inn's basement is full of rats. Help the innkeeper out
			// by killing a few.
			// LOCATION: Basement
			// ZONE: Red Dragon Inn
			// TASK: KILL 5 RAT
			// REWARD: Bread, 5 gold

			Room room = getRoom("Basement");

			Data objectiveData = new Data();

			objectiveData.addObject("toKill", 5);
			objectiveData.addObject("target", c);

			Quest quest = new Quest(
					"Help the Innkeeper",
					"The inn's basement is full of rats. Help the innkeeper out by killing a few.",
					rdi,
					new KillTask("Kill 5 rats", room, objectiveData)
					);
			
			Item bread = new Item("Bread", "A tasty looking loaf of yellow bread.");
			
			quest.setReward( new Reward(Coins.gold(5), bread) );

			if (quest.getTasks().get(0).getObjective() == null) {
				debug("ERROR! Objective Data is NULL?");
			}
			else {
				for (Entry<String, Object> entry : quest.getTasks().get(0).getObjective().getObjects().entrySet()) {
					debug(entry.getKey());
					//debug(entry.getValue());
				}
			}

			NPC npc = getNPC("Iridan");

			if (npc != null) {
				System.out.println(npc.getName()); //
				npc.setQuestGiver(true);
				npc.addQuest(quest); // assign the quest to this NPC
			}
			else {
				System.out.println("NPC is null!");
			}

			quests.add(quest); // add to main quest table
			questsByZone.put(quest.getLocation(), Utils.mkList(quest));

			// Bank Test -- DB Safe
			Bank bank = new Bank("Bank of Faerun");

			banks.put(bank.getName(), bank);

			BankAccount acct = new BankAccount(0, Coins.platinum(1000));

			bank.addAcount(0, acct);

			inn.setProperty("_game/isBank", "true");
			inn.setProperty("_game/bank/name", "Bank of Faerun");
		}
	}

	private void loot_test() {
		if( world.equals("forgotten_realms") ) {
			// initialize a loot table for a creature
			// hand it concrete, but unitialized items pulled from prototypes
			lootTables.put( "mangy rat", Utils.mkList(
					createItem("mud.iron_dagger", false),
					createItem("mud.iron_dagger", false),
					createItem("mud.iron_dagger", false),
					createItem("mud.iron_dagger", false),
					createItem("mud.iron_dagger", false)
					));
		}
	}

	private void scripted_command_test() {
		// scripted commands
		addCommand("@dbref",
				new ScriptedCommand("get object dbref", pgm, new Script("{dbref:{&arg}}")));
		addCommand("@list",
				new ScriptedCommand("", pgm, new Script("{list:{&arg},{&this}}")));
	}

	// Command Parser
	// needs more general alias support

	private boolean moduleLoaded(final String modName) {
		return module != null && ( module.getName().equals(modName) || module.getShortName().equals(modName) );
	}

	/**
	 * <b>Command Parser</b>
	 * 
	 * <br />
	 * <br />
	 * 
	 * Takes in the client object and the input from the client, and looks for a
	 * command/exit invocation.
	 * 
	 * @param _input  client input (String)
	 * @param client  client (Client)
	 */
	public void cmd(final String _input, final Client client) throws NullPointerException {
		String arg = "";
		String cmd = "";

		final String input = _input.trim();

		debug("");

		// cut the input into an array of strings separated by spaces
		final LinkedList<String> inputList = new LinkedList<String>(Arrays.asList(input.split(" ")));

		if ( !inputList.isEmpty() ) { // if there was any input
			cmd = inputList.remove(0); // grab the first element (which should be the command)

			debug("Command: \"" + cmd + "\"", 2);       // print command (pre-trim so we see any extra junk)
			cmd = cmd.trim();                           // trim the command (remove funny characters)
			debug("Command(trimmed): \"" + cmd + "\""); // print trimmed command


			arg = Utils.join(inputList, " "); // get the arguments if there are any

			// if the command has arguments
			if (inputList.size() > 1) {
				/* we don't display/log the arguments for:
				 * connect, create, or passwd 
				 * since these they contain sensitive user information, including their name and password
				 * */
				String cmd_lc = cmd.toLowerCase(); 
				
				//if (!cmd.toLowerCase().equals("connect") && !cmd.toLowerCase().equals("create") && !cmd.toLowerCase().equals("passwd")) 
				if (!cmd_lc.equals("connect") && !cmd_lc.equals("create") && !cmd_lc.equals("passwd")) {
					debug("Arguments: \"" + arg + "\"", 2);       // print arguments
					arg = Utils.trim(arg);                        // trim arguments
					debug("Arguments(trimmed): \"" + arg + "\""); // print trimmed arguments
				}
			}
		}

		debug("");

		// check if the client is logged in
		final boolean logged_in = loginCheck(client);

		// restrict commands based on whether or not the connection has a logged-in player
		// if not logged-in all of this should have configurable messages and maybe a time
		// duration to show for when the mode will return to normal
		if ( !logged_in ) {
			final String cs = getClientState(client);

			if (cs != null) {
				System.out.println("Client State (CS): " + "\'" + cs + "\'");

				switch (cs) {
				case "register":
					handle_registration(cmd, client);
					break;
				case "account_login":
					handle_account_login(cmd, client);
					break;
				case "account_menu":
					handle_account_menu(cmd, client);
					break;
				case "recover_password":
					handle_recovery(cmd, client);
					break;
				case "interactive_login":
					break;
				default:
					setClientState(client, ""); // clear any unintended client states
					break;
				}

				return;
			}
			else debug("CS is NULL!");

			if (mode == GameMode.NORMAL) // Normal Running Mode (a.k.a. Mode 0)
			{
				if (this.players.size() > this.max_players) {
					if (cmd.equals("connect") || cmd.equals("create")) {
						send("Sorry. Maximum number of players are connected. Please try back later.", client);
						return;
					}
				}

				if ( cmdIs(cmd, "connect") ) {
					cmd_connect(arg, client); // pass arguments to the player connect function
				}
				else if ( cmdIs(cmd, "console") ) {
					cmd_console(arg, client);
				}
				else if ( cmdIs(cmd, "create") ) {
					cmd_create(arg, client); // pass arguments to the player creation function
				}
				else if ( cmdIs(cmd, "help") ) {
					send("Available Commands: connect, console, create, help, register, quit, who", client);
				}
				else if ( cmdIs(cmd, "recover") ) {
					cmd_recover(arg, client);
				}
				else if ( cmdIs(cmd, "register") ) {
					send("REGISTER", client);
					cmd_register(arg, client);
				}
				else if ( cmdIs(cmd, "quit") ) {
					disconnect(client); // just kill the client?
				}
				else if ( cmdIs(cmd, "who") ) {
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

				if ( cmdIs(cmd, "connect") ) {
					cmd_connect(arg, client); // handles wizflag checking itself (since that's player dependent)
				}
				else if ( cmdIs(cmd, "console") ) {
					cmd_console(arg, client);
				}
				else if ( cmdIs(cmd, "create") ) {
					send("Sorry, only Wizards are allowed to login at this time.", client);
				}
				else if (cmd.equals("help")) {
					send("Available Commands: connect, console, create, help, quit, who", client);
				}
				else if ( cmdIs(cmd, "quit") ) {
					disconnect(client); // just kill the client?
				}
				else {
					send("Huh? That is not a known command.", client);
					debug("Command> Unknown Command");
				}
			}
			else if (mode == GameMode.MAINTENANCE) // Maintenance Mode (a.k.a. Mode 2)
			{
				send("System is in Maintenance Mode.", client); // >configurable message<
				send("No Logins allowed. Booting Client...", client);
				disconnect(client); // just kill the client
			}
			else {
				// ? (any other mode number -- may indicate some kind of failure)
				send("System may be malfunctioning.", client);
				send("No Logins allowed. Booting Client...", client);
				disconnect(client); // just kill the client
			}
		}

		// if logged-in
		else if (logged_in) {
			final Player player = getPlayer(client);
			final Room room = getRoom( player.getLocation() );

			// if the user is editing something, pass their input to the editor
			if (player.getStatus().equals(Constants.ST_EDIT)) {
				final Editors editor = player.getEditor();

				switch (editor) {
				case AREA:     break;
				case CHARGEN:  op_chargen(input, client);      break;
				case CREATURE: op_creatureedit(input, client); break;
				case HELP:     op_helpedit(input, client);     break;
				case INTCAST:  op_cast(input, client);         break;
				case ITEM:     op_itemedit(input, client);     break;
				case LIST:     op_listedit(input, client);     break;
				case MAIL:     handle_mail(input, client);     break;
				case QUEST:    op_questedit(input, client);    break;
				case ROOM:     op_roomedit(input, client);     break;
				case SKILL:    op_skilledit(input, client);    break;
				case ZONE:     op_zoneedit(input, client);     break;
				case NONE:
					send("Exiting " + editor.getName(), client);
					break;
				default:       break;
				}
			}
			else if (player.getStatus().equals("CNVS")) { // Conversation
				debug(">> In a conversation");

				if (input.equalsIgnoreCase("leave") || input.equalsIgnoreCase("bye")) {
					// TODO this is a kludge to allow escaping a broken conversation
					send("You leave the conversation.", client);
					
					player.setStatus("IC");

					look( getRoom( player.getLocation() ), player.getClient() );
				}
				else {
					// TODO make sure we know what npc the player is talking to
					// and what the player type
					
					final NPC npc = conversations.get(player).one;
					
					// TODO call conversation handler?
					handle_conversation(npc, player, Utils.toInt(input, -1));
				}
			}
			else if (player.getStatus().equals("VIEW")) { // viewing help files
				op_pager(input, client);
			}
			// TODO fix this. (what's broken?)
			else if( module != null && !Utils.mkList("IC", "OOC", "INT").contains( player.getStatus() ) ) {
				System.out.println("Module Processing input");
				module.op(input, player);
			}
			// else pass their input to command parsing
			else {
				// TODO there's a check for cmd == "" here
				// if( "".equals(cmd) ) return;

				// evaluate any named references (namerefs) in the arguments
				arg = nameref_eval(arg, client);

				debug("Argument(evaluated): \"" + arg + "\""); // print the trimmed argument

				/* Command Logging */

				if ( logging ) {
					// Log all commands after login
					System.out.println("Command being logged...");
					
					logAction(player.getName(), player.getLocation(), Utils.trim(input));

					System.out.println("Command Logged!");
				}
				
				// KLUDGE
				// TODO figure out how to handle this better, I may need to be able to type an empty line
				if( cmd.equals("") ) return;

				/* Grab a possible chat command */
				if (cmd.charAt(0) == '#') {
					final String temp = cmd.substring(1);
					final String test = chan.resolveShortName(temp);
					
					System.out.println("CMD? " + cmd);
					System.out.println("Temp? " + temp);
					System.out.println("Test? " + test);
					
					// "" means no channel, a result means a full channel name
					
					final String channelName = test.equals("") ? temp : test;
					
					System.out.println("Channel? " + channelName);

					// chat channel invocation
					chatHandler(channelName, arg, client);

					return;
				}

				/* Command Evaluation */

				boolean buildCmd = false;
				boolean adminCmd = false;
				boolean wizCmd = false;
				boolean superuserCmd = false;

				debug("Entering superuser command loop...", 4);
				
				/* SuperUser Commands */

				if (player.getAccess() >= Constants.SUPERUSER) {
					superuserCmd = true; // begin by assuming we have a superuser command

					/*if( cmdIs(cmd, "@access") ) {
						getCommand("@access").execute(arg, client);
					}*/
					if( cmdIs(cmd, "@alias") ) {
						cmd_alias(arg, client);
					}
					else if( cmdIs(cmd, "@broadcast") ) {
						write("Game> " + player.getName() + " says, " + arg);
					}
					else if( cmdIs(cmd, "@jsonify") ) {
						cmd_jsonify(arg, client);
					}
					else if( cmdIs(cmd, "@load") ) {
						send("Game> Command Not Implemented!", client);

						boolean success = cmd_unloadc(arg, client);

						if( success ) {
							send("Game> Loaded " + commandMap.get(arg) + ".", client);
						}
						else {
							String[] args = arg.split("=");

							send("Game> Failed to load " + args[1] + " with command name " + args[0], client);
						}
					}
					else if( cmdIs(cmd, "@unload") ) {
						send("Game> Command Not Implemented!", client);

						boolean success = cmd_unloadc(arg, client);

						if( success ) {
							send("Game> Unloaded " + arg + ".", client);
						}
						else {
							String[] args = arg.split("=");

							send("Game> Failed to unload " + args[1] + " with command name " + args[0], client);
						}
					}
					else if( cmdIs(cmd, "@reload") ) {
						if (arg.equals("")) sys_reload();
						else                sys_reload(arg);
					}
					else if( cmdIs(cmd, "@shutdown") )   cmd_shutdown(arg, client);
					else if( cmdIs(cmd, "@sethour") )    cmd_sethour(arg, client);
					else if( cmdIs(cmd, "@setminute") )  cmd_setminute(arg, client);
					else if( cmdIs(cmd, "@setweather") ) cmd_setweather(arg, client);
					else {
						superuserCmd = false; // if our initial assumption was wrong, indicate that
					}

					if (superuserCmd) {
						return;
					}
				}

				debug("Exited superuser command loop.", 4);

				debug("Entering build command loop...", 4);

				/* Builder Commands */

				if (player.getAccess() >= Constants.BUILD) {
					buildCmd = true; // begin by assuming we have a build command

					if ( cmdIs(cmd, "@check") ) {
						// run the check function
						cmd_check(arg, client);
					}
					else if ( cmdIs(cmd, "@cedit") ) {
						// indicate what is parsing commands
						debug("Command Parser: @cedit");

						// launch the item editor
						cmd_creatureedit(arg, client);
					}
					else if ( cmdIs(cmd, "@create_npc") ) {
						final String[] args = arg.split(" ");

						for (final String s : args) {
							System.out.println(s);
						}

						final String npcName = args[0];
						
						//getRace checks equality, ignoring case
						//final Race npcRace = getRace(args[1]);
						final Race npcRace = Races.NONE;
						//final Integer[] stats = Utils.stringsToIntegers(args[2].split(","));
						final Integer[] stats = Utils.stringsToIntegers("7,7,7,7,7,7,7".split(","));
						
						final Room location = getRoom(getPlayer(client).getLocation());

						createNPC(npcName, npcRace, stats, location);
					}
					else if ( cmdIs(cmd, "@dig") ) {
						cmd_dig(arg, client);
					}
					else if ( cmdIs(cmd, "@describe") ) {
						// run the describe function
						cmd_describe(arg, client);
					}
					else if ( cmdIs(cmd, "@door") ) {
						// run the door function
						cmd_door(arg, client);
					}
					else if ( cmdIs(cmd, "@dungeon") ) {
						// run the dungeon function
						cmd_dungeon(arg, client);
					}
					else if ( cmdIs(cmd, "@edit") ) {
						cmd_edit(arg, client);
					}
					else if ( cmdIs(cmd, "@examine") ) {
						// run the examine function
						cmd_examine(arg, client);
					}
					//
					else if ( cmdIs(cmd, "@fail") ) {
						cmd_fail(arg, client);
					}
					else if ( cmdIs(cmd, "@iedit") ) {
						// indicate what is parsing commands
						debug("Command Parser: @iedit");

						// launch the item editor
						cmd_itemedit(arg, client);
					}
					else if ( cmdIs(cmd, "@jump") ) {
						cmd_jump(arg, client);
					}
					else if ( cmdIs(cmd, "@link") ) {
						cmd_link(arg, client);
					}
					else if ( cmdIs(cmd, "@lsedit") ) {
						cmd_lsedit(arg, client); // run the list editor
					}
					else if ( cmdIs(cmd, "@nameref") ) {
						cmd_nameref(arg, client);
					}
					else if ( cmdIs(cmd, "@ofail") ) {
						cmd_ofail(arg, client); // set an ofail message
					}
					else if ( cmdIs(cmd, "@open") ) {
						// run the open function
						cmd_open(arg, client);
					}
					else if ( cmdIs(cmd, "@osuccess") ) {
						//
						cmd_osuccess(arg, client);
					}
					else if ( cmdIs(cmd, "@qedit") ) {
						//
						debug("Command Parser: @qedit");

						// run the list editor
						cmd_questedit(arg, client);
					}
					else if ( cmdIs(cmd, "@redit") ) {
						//
						debug("Command Parser: @redit");

						// run the list editor
						cmd_roomedit(arg, client);
					}
					else if ( cmdIs(cmd, "@skedit") ) {
						//
						debug("Command Parser: @skilledit");

						// run the skill editor
						cmd_skilledit(arg, client);
					}
					//
					else if ( cmdIs(cmd, "@success") ) {
						cmd_success(arg, client); // set an exit success message
					}
					else if ( cmdIs(cmd, "@unlink") ) {
						cmd_unlink(arg, client);
					}
					else if ( cmdIs(cmd, "@zedit") ) {
						cmd_zoneedit(arg, client);
					}
					else {
						buildCmd = false; // if our initial assumption was wrong, indicate that
					}

					if (buildCmd) {
						return;
					}
				}

				debug("Exited build command loop.", 4);

				debug("Entering admin command loop...", 4);

				/* Admin Commands */

				if (player.getAccess() >= Constants.ADMIN) {
					adminCmd = true; // begin by assuming we have a admin command

					if ( cmdIs(cmd, "@accounts") ) {
						cmd_accounts(arg, client);
					}
					else if ( cmdIs(cmd, "@alias") ) {
						cmd_alias(arg, client);
					}
					else if ( cmdIs(cmd, "@backup") ) {
						cmd_backup(arg, client);
						// send("Game> Backup Functionality Broken. Please stick to manual file saves.");
					} 
					else if ( cmdIs(cmd, "@bb") ) {
						cmd_bb(arg, client);
					}
					else if ( cmdIs(cmd, "@config") ) {
						cmd_config(arg, client);
					}
					else if ( cmdIs(cmd, "@control") ) {
						// run the NPC control/takeover function?
						cmd_control(arg, client);
					}
					else if ( cmdIs(cmd, "@debug") ) {
						cmd_debug(arg, client);
					}
					else if ( cmdIs(cmd, "@find") ) {
						cmd_find(arg, client);
					}
					else if ( cmdIs(cmd, "@flags") ) {
						cmd_flags(arg, client);
					}
					else if ( cmdIs(cmd, "@give") ) {
						cmd_give(arg, client);
					}
					else if ( cmdIs(cmd, "@hash") ) {
						client.write("Hash of argument: '" + arg + "' is hash: '" + Utils.hash(arg) + "'.");
					}
					else if ( cmdIs(cmd, "@hedit") ) {
						cmd_helpedit(arg, client);
					}
					else if ( cmdIs(cmd, "@initialize") ) {
						cmd_initialize(arg, client);
					}
					else if ( cmdIs(cmd, "@kick") ) {
						cmd_kick(arg, client);
					}
					else if ( cmdIs(cmd, "@listprops") ) {
						cmd_listprops(arg, client);
					}
					else if ( cmdIs(cmd, "@makehouse") ) {
						makeHouse( getPlayer(client) );
					}
					else if ( cmdIs(cmd, "@name") ) {
						cmd_name(arg, client);
					}
					else if ( cmdIs(cmd, "@nextdb") ) {
						if (arg.equals("")) {
							send("Next Database Reference Number (DBRef/DBRN): " + objectDB.peekNextId(), client);
						}
					}
					else if ( cmdIs(cmd, "@npcs") ) {
						cmd_npcs("", client);
					}
					else if ( cmdIs(cmd, "@lex") ) {
						final List<String> result = ProgramInterpreter.lex(arg); 
						
						send(result, client);
					}
					else if ( cmdIs(cmd, "@pgm") ) {
						// we'll just create a new and temporary instance (debugging enabled)
						ProgramInterpreter newInterp = new ProgramInterpreter(this, true);
						
						newInterp.lex(arg);
						
						Script script = new Script(arg);
						
						// invoke the program interpreter and pass it the argument
						send("-Result: " + newInterp.interpret(script, player, null), client);

						newInterp = null;
					}
					else if ( cmdIs(cmd, "@set") ) {
						cmd_set(arg, client);
					}
					else if ( cmdIs(cmd, "@session") ) {
						/*
						 * notionally this should give data on the current or
						 * last of the specified player
						 */
						cmd_session(arg, client);
					}
					else if ( cmdIs(cmd, "@sethp") ) {
						cmd_sethp(arg, client); // DM/Debug Command
					}
					else if ( cmdIs(cmd, "@setlevel") ) {
						cmd_setlevel(arg, client); // DM/Debug Command
					}
					else if ( cmdIs(cmd, "@setmana") ) {
						cmd_setmana(arg, client); // DM/Debug Command
					}
					else if ( cmdIs(cmd, "@setskill") ) {
						cmd_setskill(arg, client); // DM/Debug Command
					}
					else if ( cmdIs(cmd, "@setxp") ) {
						cmd_setxp(arg, client); // DM/Debug Command
					}
					else if ( cmdIs(cmd, "@viewlog") ) {
						cmd_viewlog(arg, client);
					}
					else if ( cmdIs(cmd, "@tune") ) {
						String[] args = arg.split(" ");

						if (args[0].equals("cmdDelay")) {
							final int delay = Utils.toInt(args[1], -1);
						}
					}
					else if ( cmdIs(cmd, "@zones") ) {
						cmd_zones(arg, client);
					}
					else if ( cmdIs(cmd, "@zoneinfo") ) {
						cmd_zoneinfo(arg, client);
					}
					else {
						adminCmd = false; // if our initial assumption was wrong, indicate that
					}

					if (adminCmd) {
						return;
					}
				}

				debug("Exited admin command loop.", 4);

				debug("Entered wizard command loop.", 4);

				/* Wizard Commands */

				if (player.getAccess() >= Constants.WIZARD) {
					wizCmd = true; // begin by assuming we have a wizard command

					if ( cmdIs(cmd, "@ban") ) {
						cmd_ban(arg, client);
					}
					else if ( cmdIs(cmd, "@flag") ) {
						cmd_flag(arg, client);
					}
					else if ( cmdIs(cmd, "@flush") ) {
						flush();
						send("Dead Connections Flushed!");
					}
					else if ( cmdIs(cmd, "@recycle") ) {
						cmd_recycle(arg, client);
					}
					else if ( cmdIs(cmd, "@setcolor") ) {
						cmd_setcolor(arg, client);
					}
					else if ( cmdIs(cmd, "@setmode") ) {
						cmd_setmode(arg, client);
					}
					else if ( cmdIs(cmd, "@spawn") ) {
						cmd_spawn(arg, client);
					}
					else if ( cmdIs(cmd, "@teleport") ) {
						getCommand("@teleport").execute(arg, client);
					}
					else {
						wizCmd = false; // if our initial assumption was wrong, indicate that
					}

					if (wizCmd) {
						return;
					}
				}

				debug("Exited wizard command loop.", 4);

				debug("Entering user commmand loop...", 4);

				/* User Commands */

				// TODO convert player command checks to use CmdIs(...)

				if (player.getAccess() >= Constants.USER) {
					if ( cmdIs(cmd, "ask") )             cmd_ask(arg, client);
					// attack (a soft command)
					else if ( cmdIs(cmd, "aliases") )    cmd_aliases(arg, client);
					else if ( cmdIs(cmd, "auction") )    cmd_auction(arg, client);
					else if ( cmdIs(cmd, "auctions") )   cmd_auctions(arg, client);
					else if ( cmdIs(cmd, "balance") )    cmd_balance(arg, client);
					else if ( cmdIs(cmd, "bash") )       cmd_bash(arg, client);
					else if ( cmdIs(cmd, "bid") )        cmd_bid(arg, client);
					else if ( cmdIs(cmd, "boards") )     cmd_boards(arg, client);
					else if ( cmdIs(cmd, "buy") )        cmd_buy(arg, client);
					else if ( cmdIs(cmd, "calendar") )   cmd_calendar(arg, client);
					else if ( cmdIs(cmd, "cast") )       cmd_cast(arg, client);     // cast (a soft command)?
					else if ( cmdIs(cmd, "chargen") )    cmd_chargen(arg, client);
					else if ( cmdIs(cmd, "chat") )       cmd_chat(arg, client);
					else if ( cmdIs(cmd, "climb") )      cmd_climb(arg, client);
					else if ( cmdIs(cmd, "cls") )        cmd_cls(arg, client);
					else if ( cmdIs(cmd, "colors") )     cmd_colors(arg, client);
					else if ( cmdIs(cmd, "commands") )   cmd_commands(arg, client);
					else if ( cmdIs(cmd, "consider") )   cmd_consider(arg, client);
					else if ( cmdIs(cmd, "condition") )  cmd_condition(arg, client);
					else if ( cmdIs(cmd, "craft") )      cmd_craft(arg, client);
					else if ( cmdIs(cmd, "deposit") )    cmd_deposit(arg, client);
					else if ( cmdIs(cmd, "drink") )      cmd_drink(arg, client);
					else if ( cmdIs(cmd, "drop") )       cmd_drop(arg, client); 
					else if ( cmdIs(cmd, "effects") )    cmd_effects(arg, client);
					else if ( cmdIs(cmd, "equip") )      cmd_equip(arg, client);
					else if ( cmdIs(cmd, "exp") )        cmd_exp(arg, client); // TODO CMD 5
					else if ( cmdIs(cmd, "exits") )      cmd_exits(arg, client);
					else if ( cmdIs(cmd, "feats") )      cmd_feats(arg, client);
					else if ( cmdIs(cmd, "fly") )        cmd_fly(arg, client); // TODO CMD 6
					else if ( cmdIs(cmd, "get") )        cmd_get(arg, client);
					else if ( cmdIs(cmd, "go") )         cmd_go(arg, client);
					// greet (a soft command)
					else if ( cmdIs(cmd, "help") )       cmd_help(arg, client);
					else if ( cmdIs(cmd, "hold") )       cmd_hold(arg, client);
					else if ( cmdIs(cmd, "home") )       cmd_home(arg, client);
					else if ( cmdIs(cmd, "housing") )    cmd_housing(arg, client);
					else if ( cmdIs(cmd, "inspect") )    cmd_inspect(arg, client);
					else if ( cmdIs(cmd, "interact") )   cmd_interact(arg, client);
					else if ( cmdIs(cmd, "inventory") )  cmd_inventory(arg, client);
					else if ( cmdIs(cmd, "land") )       cmd_land(arg, client);    // TODO CMD
					else if ( cmdIs(cmd, "levelup") )    cmd_levelup(arg, client); // TODO CMD 8 fix this kludge
					else if ( cmdIs(cmd, "list") )       cmd_list(arg, client);
					else if ( cmdIs(cmd, "lock") )       cmd_lock(arg, client);
					else if ( cmdIs(cmd, "logout") )     cmd_logout(arg, client);
					else if ( cmdIs(cmd, "look") )       cmd_look(arg, client);
					else if ( cmdIs(cmd, "lookat") )     cmd_lookat(arg, client);
					// mail (a soft command)
					else if ( cmdIs(cmd, "map") )        cmd_map(arg, client);
					else if ( cmdIs(cmd, "mine") )       cmd_mine(arg, client);
					else if ( cmdIs(cmd, "money") )      cmd_money(arg, client);
					
					else if ( cmdIs(cmd, "motd") )       send(messageOfTheDay(), client); // TODO CMD 11
					
					else if ( cmdIs(cmd, "move") )       cmd_move(arg, client);
					else if ( cmdIs(cmd, "ooc") )        player.setStatus("OOC"); // TODO CMD 12 fix this kludge
					else if ( cmdIs(cmd, "ic") )         player.setStatus("IC");  // TODO CMD 13 fix this kludge
					
					else if ( cmdIs(cmd, "open") )       cmd_open2(arg, client);
					else if ( cmdIs(cmd, "page") )       cmd_page(arg, client);
					else if ( cmdIs(cmd, "pose") )       cmd_pose(arg, client);
					else if ( cmdIs(cmd, "party") )      cmd_party(arg, client);
					else if ( cmdIs(cmd, "passwd") )     cmd_passwd(arg, client);
					else if ( cmdIs(cmd, "pconfig") )    cmd_pconfig(arg, client);
					else if ( cmdIs(cmd, "pinfo") )      cmd_pinfo(arg, client);
					else if ( cmdIs(cmd, "push") )       cmd_push(arg, client);
					else if ( cmdIs(cmd, "put") )        cmd_put(arg, client);
					
					else if ( cmdIs(cmd, "prompt") )     prompt(client);
					
					else if ( cmdIs(cmd, "quests") )     cmd_quests(arg, client);
					else if ( cmdIs(cmd, "quit") )       cmd_quit(arg, client);
					else if ( cmdIs(cmd, "read") )       cmd_read(arg, client);
					else if ( cmdIs(cmd, "roll") )       cmd_roll(arg, client); // TODO CMD 14
					else if ( cmdIs(cmd, "run") ) {
						(new Command("") {
							@Override
							public void execute(final String arg, final Client client) {
								final Player player = getPlayer(client);
								player.setSpeed(Constants.RUN);
								send("You get ready to run.", client);
							}
						}).execute(arg, client);
					}
					else if ( cmdIs(cmd, "score") )      cmd_score(arg, client);
					else if ( cmdIs(cmd, "sell") )       cmd_sell(arg, client);
					else if ( cmdIs(cmd, "sheathe") )    cmd_sheathe(arg, client);
					else if ( cmdIs(cmd, "skillcheck") ) cmd_skillcheck(arg, client);
					else if ( cmdIs(cmd, "spellinfo") )  cmd_spellinfo(arg, client);
					else if ( cmdIs(cmd, "spells") )     cmd_spells(arg, client);
					// staff (a soft command)
					else if ( cmdIs(cmd, "stats") )      cmd_stats(arg, client);
					else if ( cmdIs(cmd, "status") )     cmd_status(arg, client);
					else if ( cmdIs(cmd, "talk") )       cmd_talk(arg, client);
					else if ( cmdIs(cmd, "take") )       cmd_take(arg, client);
					else if ( cmdIs(cmd, "target") )     cmd_target(arg, client);
					else if ( cmdIs(cmd, "tell") )       cmd_tell(arg, client);
					else if ( cmdIs(cmd, "time") )       cmd_time(arg, client);
					else if ( cmdIs(cmd, "trade") )      cmd_trade(arg, client);
					else if ( cmdIs(cmd, "travel") )     cmd_travel(arg, client);
					else if ( cmdIs(cmd, "unequip") )    cmd_unequip(arg, client);
					else if ( cmdIs(cmd, "unlock") )     cmd_unlock(arg, client);
					else if ( cmdIs(cmd, "use") )        cmd_use(arg, client);
					else if ( cmdIs(cmd, "value") )      cmd_value(arg, client);
					else if ( cmdIs(cmd, "version") )    cmd_version(arg, client);					
					else if ( cmdIs(cmd, "vitals") )     cmd_vitals(arg, client);
					else if ( cmdIs(cmd, "where") )      cmd_where(arg, client);
					else if ( cmdIs(cmd, "who") )        cmd_who(arg, client);
					else if ( cmdIs(cmd, "withdraw") )   cmd_withdraw(arg, client);
					else if ( cmdIs(cmd, "walk") ) {
						(new Command("") {
							@Override
							public void execute(final String arg, final Client client) {
								final Player player = getPlayer(client);
								
								if (player.getSpeed() > Constants.WALK) {
									player.setSpeed(Constants.WALK);
									send("You slow down to a walking speed.", client);
								}
							}
						}).execute(arg, client);
					}
					else if ( cmdIs(cmd, "write") ) { // TODO CMD 21
						// check for something to write in/on (writable things
						// -- paper, books, scrolls?)
						// check for writing tool and ink
						// get stuff and "write" it down
					}
					else {
						// TODO exits first, then soft commands...
						debug("Exit? \'" + cmd + "\'");
						
						boolean nothing = false;

						//if ( cmd.matches("[a-zA-Z_-]+") ) {
						if ( cmd.matches("(@)?[a-zA-Z]+") ) {
							// exit handling
							// Has the user given an action/exit that is linked to something for which no similarly named command exists?
							// If so, execute link or move user in the direction/to the room specified by the action/exit

							// handle the command as an exit
							if ( exitHandler(cmd, client) ) {
								debug("It was an exit");
							}
							// general case for non-explicitly checked mapped commands
							else if (soft_commands) { // "soft" commands enabled?
								debug("Look in CommandMap");
								final Command command = (player.hasCommand(cmd) ? player.getCommand(cmd) : getCommand(cmd));

								if (command != null) {
									if ( checkAccess(player, command.getAccessLevel()) ) {
										System.out.println("Entered ScriptedCommand...");
										command.execute(arg, client);
										System.out.println("Exited ScriptedCommand...");
									}
									else send("You may not use that command. (Insufficient Access Level!)", client);
								}
								else send("Command is NULL.", client);
							}
							else nothing = true;
						}
						else {
							debug("No match found in '" + cmd + "'");
							nothing = true;
						}
						
						if( nothing ) {
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
	 * Command parameter shall in all cases conform to containing no more than,
	 * but maybe less than a String arg and a Client client as parameters.
	 * 
	 * function name syntax 'cmd_<name>(String arg, Client client)'
	 * 
	 * should functions check access permissions, or the command loop? note:
	 * currently it might be being checked in the command loop
	 */

	private void makeHouse(final Player player) {
		if( player != null ) {
			final House h = createHouse(player, 5);
			
			send(Arrays.asList( h.getInfo() ), player.getClient());
		}
	}
	
	private House createHouse(final Player player, final Integer size) {
		return null;
	}

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
		// syntax: access <player>=<access level> (access level denoted by
		// integer -- see CONSTANTS)
		final String[] args = arg.split("=");

		if (args.length > 0) {
			// NOTE: access the DB directly because we want to be able to modify
			// logged out players as well
			Player player = objectDB.getPlayer(args[0]);

			if (player != null) {
				if (args.length > 1) {
					try {
						player.setAccess(Integer.parseInt(args[1]));
						send(player.getName() + "'s access level set to " + player.getAccess(), client);
					}
					catch (final NumberFormatException nfe) {
						// TODO is this "debug" message adequate? should I tell
						// the player or write to the debug log?
						send("Invalid access level!", client);
						
						// TODO should this be a game error?
						
						debug( nfe );
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
		if (this.acctMgr != null) {
			if ( arg.equals("") ) {
				final String border = Utils.padRight("", '-', 80);
				final String header = "Name     ID     Player                        Online Created    Age";

				final List<String> output = new LinkedList<String>();

				output.add("Accounts (Online)");

				output.add(border);

				output.add(header);

				output.add(border);

				for (final Account acct : acctMgr.getAccounts()) {
					final String username = Utils.padRight(acct.getUsername(), 8);
					final String id = Utils.padRight(String.valueOf(acct.getId()), 6);

					final String name;

					final Player player = acct.getPlayer();

					// 29 vs 40?
					if (player != null) name = Utils.padRight(player.getName(), 29);
					else                name = Utils.padRight("- No Player -", 29);

					final String state = Utils.padRight((acct.isOnline() ? "Yes" : "No"), 6);
					final String creationDate = Utils.padRight(acct.getCreated().toString(), 10);

					output.add(username + " " + id + " " + name + " " + state + " " + creationDate);
				}

				output.add(border);

				send(output, client);
			}
			else {
				String[] args = arg.split(" ");

				if (args.length == 3) {
					System.out.printf("@accounts: %s %s %s\n", args[0], args[1], args[2]);

					if ( args[0].equals("+add") ) {
						client.write("Adding new account (+add)");

						/*
						 * account.linkCharacter(getPlayer(client)); // link
						 * current character to account
						 * account.setPlayer(getPlayer(client)); // mark it as
						 * the active player account.setClient(client); // mark
						 * current client as active client
						 * account.setOnline(true); // mark us as being online
						 */

						// account = new Account(this.accounts.size(), args[1],
						// args[2], 5);
						acctMgr.addAccount(args[1], args[2], 5); // add the account to the account manager
					}
					else if ( args[0].equals("+link") ) { // @accounts +link 3 Nathan
						final Account account = acctMgr.getAccount(Utils.toInt(args[1], -1));
						final Player player = objectDB.getPlayer(args[2]);
						// NOTE: ^ use DB access above so we can link logged out
						// players too

						if (account != null) {
							if (player != null) account.linkCharacter(player);
							else                send("No such Player Exists!", client);
						}
						else send("No Such Account Exists!", client);
					}
					else if ( args[0].equals("+info") ) {
						final Account account = acctMgr.getAccount(args[1], args[2]);

						if (account != null) {
							send("Account - " + account.getUsername() + " (" + account.getId() + ")", client);
							send("Online:     " + account.isOnline(), client);

							// send("Character:  " +
							// account.getPlayer().getName(), client);

							send("Characters: ", client);

							int index = 1;

							for (Player p : account.getCharacters()) {
								send(index + ") " + p.getName() + "( " + p.getPClass().getAbrv() + " )", client);
							}
						}
						else send("No Such Account Exists!", client);
					}
				}
				else send("@accounts: insufficient arguments", client);
			}
		}
		else send("Accounts Disabled or Account Manager is NULL!", client);
	}

	/**
	 * Provides account configuration to the player if they are logged in with
	 * an account.
	 * 
	 * @param arg
	 * @param client
	 */
	/*
	 * private void cmd_aconfig(String arg, Client client) { }
	 */
	
	private void cmd_alias(final String arg, final Client client) {
		// @alias command=alias string
		final String[] args = arg.split("=");

		if( args.length == 2 ) {
			final String command = args[0];
			final String alias = args[1];

			// should we be making sure the specified command is valid
			addAlias(command, alias);
		}
		else {
			// if no set of things to alias to, just show existing aliases for it if any
		}
	}

	private void cmd_aliases(final String arg, final Client client) {
		send("Aliases", client);
		
		send("-------------------------------------------", client);
		
		for (final Entry<String, String> e : getAliases().entrySet()) {
			send(e.getKey() + " : " + e.getValue(), client);
		}
		
		send("-------------------------------------------", client);
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
			send("Syntax: ask <npc name> <keyword> <additional data>", client);
			return;
		}

		final Player player = getPlayer(client);           // get the player
		final NPC npc = getNPC(args[0].replace('_', ' ')); // get the npc we're referring to

		if (npc != null) {
			final String keyword = args[1]; // get the keyword;

			if (keyword.equals("quests")) {
				if( npc.isQuestgiver() ) {
					final List<Quest> quests = npc.getQuestsFor(player);

					send("Available Quests", client);
					send("================================================================================", client);

					boolean use_color = (color == Constants.ANSI || color == Constants.XTERM);

					for (final Quest quest : quests) {
						if (!quest.isComplete()) {
							// index of quest in the list or it's id
							client.write(quests.indexOf(quest) + ") " + quest.toDisplay(use_color));
						}

						client.write("" + Colors.WHITE);
					}

					send("================================================================================", client);
					send("* To accept a quest, type 'ask <npc> accept <quest identifier>')",                 client);
					send("* If you don't see a quest here, you are currently on it / already completed it.", client);
					send("* Also, finished quests are greyed out.",                                          client);
					send("** For the moment, the quest identifier is the quest's index in the list (0-?)",   client);
					// TODO the above hints should be reduced/removed since the problem may eventually be eliminated
				}
				else {
					final String str = "Impertinent scoundrel! It's not my job to find something for you to do.";
					final Message msg = npc.tell(player, str);

					addMessage(msg);
				}
			}
			else if (keyword.equals("accept")) {
				if (args.length == 3) {
					if( npc.isQuestgiver() ) {
						final Quest quest = npc.getQuestsFor(player).get(Utils.toInt(args[2], -1));

						if (quest != null) {
							if (!player.hasQuest(quest)) {
								// TODO fix this kludge, this should be more generic and not done this way
								if (quest.getName().equals("Help the Innkeeper")) {
									final String str = "We've been having problems with rats getting into our stores lately. It'd be much appreciated if you could dispatch them for us.";
									final Message msg = npc.tell(player, str);

									addMessage(msg);
								}

								final Quest new_quest = quest.getCopy();

								player.addQuest(new_quest);

								player.setActiveQuest(new_quest);
								
								send(colors("New Quest", "green") + " - " + colors(quest.getName(), "yellow"), client);

								//send("New Quest - " + quest.getName(), client);
								send("Quest Added!", client);
							}
							else send("You already have that quest!", client);
						}
						else send("No such Quest!", client);
					}
					else  {
						String g = npc.getGender();
						String pn = (g.equals("M") ? "He" : (g.equals("F") ? "She" : "It"));

						send(pn + " looks at you irritatedly.", client);

						final String str = "What the heck do you want?! I haven't the faintest clue what you're going on about.";
						final Message msg = npc.tell(player, str);

						addMessage(msg);
					}
				}
			}
			else if (keyword.equals("about")) {
				if (args.length == 3) {
					// quests (by name?), conversation topics
				}
			}
			else if (keyword.equals("complete")) {
				// ask <npc> complete
				// ask <npc> complete <quest num>

				// the quest num here is (for now), simply the index of the quest in the player list of current quests

				if ( npc.isQuestgiver() ) {
					if (args.length >= 2) {	
						int questNum;

						Quest quest = null;

						// if there's an arg, get that quest, otherwise pick the
						// first of the questgiver's quests that the player currently has?

						// currently, we'll take the quest at the specified index
						if (args.length == 3) {
							questNum = Utils.toInt(args[2], -1);

							final List<Quest> quests = player.getQuests();

							if( questNum >= 0 && questNum < quests.size() ) {
								quest = player.getQuests().get(questNum);
							}
							else {
								send("Invalid Quest!", client);
								return;
							}
						}
						else {
							quest = player.getActiveQuest();
						}

						if (quest != null) {
							if ( npc.hasQuest(quest) ) {
								if( quest.isComplete() ) {
									// TODO these messages should come from quest data
									Message msg = null;

									msg = npc.tell(player, "Thanks a lot. Now we can quit worrying about our stock being consumed by vermin!");

									addMessage(msg);

									debug("Quest Completed!");

									msg = npc.tell(player, "Here's a reward for your efforts");

									addMessage(msg);

									final Reward r = quest.getReward();

									if( r != null ) {
										if( r.getCoins() != null && r.getCoins().numOfCopper() > 0 ) {
											// give player a reward
											giveMoney(player, Coins.gold(5));
										}
										else System.out.println("ASK: Quest Reward - Coins is NULL.");

										if( r.getItems() != null ) {
											//final List<Item> inv = player.getInventory();

											for(final Item item : r.getItems()) {
												final Item item1 = item.getCopy();

												initCreatedItem( item1 );

												item1.setLocation( player.getDBRef() );
												
												giveItem(player, item1);
											}
										}
										else System.out.println("ASK: Quest Reward - Items is NULL.");
									}
									else System.out.println("ASK: Quest Reward is NULL.");

									// TODO do something so the quest can't be considered again without redoing it
									player.removeQuest( quest );
								}
								else {
									Message msg = npc.tell(player, "Oh yeah? I can still hear the rats scuttling around down there!");
									addMessage(msg);
								}
							}
							else {
								send("Wrong questgiver!", client);
							}
						}
						else {
							send("No such Quest!", client);
						}
					}
				}
				else {
					final String str = "Beggars! Bunch of lazy bums, the lot of them. I'm not offering any reward for whatever it is you did!";
					final Message msg = npc.tell(player, str);

					addMessage(msg);
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
		// TODO should auctioning something remove it from your inventory?
		// TODO if I don't remove the item I need to mark it so it can't be auctioned more than once
		// ideas:
		// auction <item> <time> <price> <buyout>
		// ex. auction longsword 2d 50gp 100gp
		// ex. auction longsword for 2d at 50gp
		final Player player = getPlayer(client);

		String[] args = arg.split(" ");

		if (args.length >= 2) {
			if( args[0].charAt(0) == '#' ) {
				final String param = args[0].substring(1);
				
				if ( param.equalsIgnoreCase("search") || param.equalsIgnoreCase("s") ) {
					
				}
				else if (param.equalsIgnoreCase("cancel") || param.equalsIgnoreCase("c")) {
					final Auction auction = getAuction(player, args[1]);

					if (auction != null) {
						if ( auction.hasBids() ) {
							send("You cannot cancel an auction once someone has bid on it.", client);
						}
						else {
							final Item item = auction.getItem();

							synchronized(auctions) {
								auctions.remove(auction);
							}

							send("You canceled your auction of " + item.getName(), client);

							player.getInventory().add(item);
						}
					}
					else send("No such auction!", client);
				}
			}
			else {
				final Item item = getItem(args[0], player);

				if ( item != null ) {
					if ( item.isAuctionable() ) {
						Coins price = Coins.copper(Utils.toInt(args[1], -1));

						// create auction and add it to the list
						Auction auction = createAuction(player, item, price, 21600);

						auctions.add(auction);

						// create and store a timer for the auction
						addAuctionTimer( new AuctionTimer(auction), player);

						send(item.getName() + " put up for auction at " + price.toString(true));
					}
					else {
						send("You cannot auction that item.", client);
					}
				}
				else {
					send("What do you want to auction?", client);
				}
			}
		}
	}
	
	/**
	 * Command: auctions
	 * 
	 * View current auctions and search them.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_auctions(final String arg, final Client client) {
		if( !arg.equals("") ) {
			// search <item type>
		}
		else {
			// examples
			// | +2 Long Sword            | 2 days, 17 hours | 150gp   | 250gp  |
			// | Staff of the Archmagi    | 2d 13h           | 32sp    | 2gp5sp |
			
			List<String> output = new LinkedList<String>();

			output.add("+----------------------+-----------------+------------------+------------------+");
			output.add("|      Item Name       |    Duration     |  current price   |   buyout price   |");
			output.add("+----------------------+-----------------+------------------+------------------+");

			StringBuilder sb = new StringBuilder();

			Item item;
			Coins price;
			Coins buyoutprice;

			for (final Auction auction : auctions) {
				item = auction.getItem();

				price = null;
				buyoutprice = null;

				if ( auction.hasBids() ) price = auction.getCurrentBid().getAmount();
				else                     price = auction.getInitialPrice();

				sb.append("| ");
				sb.append(Utils.center(item.getName(), 20));
				sb.append(" | ");

				int rem = auction.getTimeLeft() / 60;
				int remainder = auction.getTimeLeft() % 60;

				if (remainder > 0) {
					sb.append(Utils.center("> " + rem + "s", 15));
				}
				else sb.append(Utils.center("" + rem + "s", 15));

				// sb.append(Utils.center("" + auction.getTimeLeft(), 16));

				sb.append(" | ");
				sb.append(Utils.center(price.toString(true), 16));
				sb.append(" | ");
				// sb.append(Utils.center(buyoutprice.toString(true), 18));
				sb.append(Utils.center("", 16));
				sb.append(" |");

				output.add(sb.toString()); // add the line to the output
				sb.delete(0, sb.length()); // clear the string builder
			}

			output.add("+----------------------+-----------------+------------------+------------------+");

			client.write(output);
		}
	}

	/**
	 * Command: @backup
	 * 
	 * Backup the database
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_backup(final String arg, final Client client) {
		backup(arg);
		send("Finished backing up.", client);
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
		final Room room = getRoom( player.getLocation() );
		
		// TODO clearly this was necessary, but doesn't seem like it should be 
		/*String str = null;
		
		try {
			str = room.getProperty("_game/isBank");
			
			System.out.println("_game/isBank: " + str);
		}
		catch(final ClassCastException cce) {
			System.out.println("--- Stack Trace ---");
			cce.printStackTrace();
			str = "false";
		}*/
		
		// TODO this is a place where a result class would be useful, kinda like channels
		// results: BANK, NO_BANK -- ACCOUNT, NO_ACCOUNT
		
		String str = room.getProperty("_game/isBank");
		
		final Boolean isBank = str.equals("true") ? true : false;
		
		if( isBank ) {
			final Bank bank = getBank( room.getProperty("_game/bank/name") );
			final BankAccount acct = bank.getAccount(0);

			if ( bank != null ) {
				if( acct != null ) {
					send("( " + bank.getName() + " ) Your balance is: " + acct.getBalance().toString(true), client);
				}
				else {
					send("You don't have an account.", client);
				}
			}
			else {
				send("No such bank?!", client);
			}
		}
		else send("You aren't in a bank.", client);
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
		if(arg.equals("")) {
			send("Command> '@ban' : No valid arguments.", client);
		}
		else {
			final String[] args = arg.split(" ");

			// @ban #list -- list bans
			// @ban #ip <addresss> -- ban an IP address
			// @ban <player name(s)?> -- ban a player
			// @ban +ip <player name> -- ban a player and their current IP address

			if( args.length >= 1 ) {
				if( args[0].startsWith("#") || args[0].startsWith("+") ) {
					final String param1 = args[0].substring(1);

					if (args[0].equals("#list")) {
						// List Banned IPs
						send("Banned IP Addresses", client);

						for (final String s : banlist) send(s, client);

						// List Banned Players
						send("Banned Players", client);

						for(final Player player : objectDB.getPlayers()) {
							if( player.getPStatus() == Player.Status.BANNED ) {
								send(player.getName(), client);
							}
						}
					}
					else if (args.length > 1) {
						if (args[0].equals("#ip")) {
							final String ip_addr = args[1];

							// add IP address to banlist

							// TODO test ip address for format errors
							// ^ need to check IPv4 and IPv6
							boolean valid_address = false;

							// IPv4
							if( ip_addr.matches("^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}$") ) {
								valid_address = true;
							}

							if( valid_address ) banlist.add(args[1]);
						}
						else if(args[0].equals("+ip")) {

						}
					}
				}
				else {
					// NOTE: use DB so we can ban logged off players too
					final Player player = objectDB.getPlayer(arg); // player name based search (banning a player should ban his account as well

					System.out.println("Player: " + player);

					if (player != null) {
						final Client client1 = player.getClient();

						if (client1 != null) { // current DB always has players loaded, so the player will never be null
							// TODO this should be a configurable option
							// add the player's ip address to the banlist (IP address ban)
							// banlist.add(client1.getIPAddress());

							// if they have an account, suspend the account
							final Account acct = acctMgr.getAccount(player);

							if (acct != null) {
								acct.setStatus(Account.Status.SUSPENDED);
							}
							else {

							}

							// mark the player somehow
							player.setPStatus(Player.Status.BANNED);

							// inform the player
							cmd_page(arg + ", you have been banned.", client1);

							// remove them (unceremoniously) from the server/game
							kick(client1);
						}
						else {
							/*
							 * Update my code so it stores the last login ip for
							 * players, so I can ban them even if they log off? Also,
							 * add a banned check to logging in so they still can't
							 * login, even if they change their ip?
							 */
							debug("That player is not connected");
							send("That player is not connected", client);
						}

						return;
					}
					else {
						if (arg.matches("[a-zA-Z]+")) {
							send("No such player!", client);
						}
						else {
							String[] range = arg.split(",");

							System.out.println(Arrays.asList(range));

							for (final String ipa : range) {
								if (ipa.matches("^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}$")) {
									banlist.add(ipa);
								}
								else send("That's not an ipaddress or a range of them.");
							}
						}
					}
				}
			}
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
		getPlayer(client).setMode(PlayerMode.COMBAT); // set the play mode to COMBAT
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
		final Player player = getPlayer(client);

		// TODO fix null pointer generation which crashes this if an incorrect argument occurs

		// exit if there are no arguments at all
		if ( "".equals(arg) ) {
			send("invalid syntax. see 'help @bb'", client);
		}
		else {
			// break up argument string into individual pieces
			final String[] args = arg.split(" ");

			debug("# of args: " + args.length);

			// initialize this to the main system board
			BulletinBoard board = null; //boards.get("board");
			Integer messageNum = -1;

			// TODO change this to get the appropriate bulletin board

			// important components of argument data
			String command = "";
			String param = "";

			// @bb <command> <params>
			// args[0]: <command>
			// args[1...?]: <params>

			if (args.length >= 2) {
				command = args[0];
				param = args[1];

				int dotIndex = param.indexOf('.');

				if ( dotIndex != -1 ) {
					board = getBoard( param.substring(0, dotIndex) );
					
					messageNum = Utils.toInt(param.substring(dotIndex + 1, param.length()), -1);

					System.out.println("    board: " + board.getName());
					System.out.println("message #: " + messageNum);
				}
				else {
					board = getBoard(param);

					final List<String> params = (List<String>) Arrays.asList(args);
					
					param = Utils.join( params.subList(2, params.size()), " ");
					
				}
			}

			debug("command: " + command);
			debug("  param: " + param);
			
			if ( command.equals("+scan") | command.equals("+s") ) {
				debug("bb:scan");

				final List<String> out = new LinkedList<String>();

				for (final String s : MudUtils.scan(board)) out.add(s);

				send(out, client);
			}
			else if ( command.equals("+delete") || command.equals("+d") ) {
				debug("bb:delete");

				if (messageNum > -1) {
					if (messageNum < board.getNumMessages()) { // check for valid message number
						final List<BBEntry> entries = board.getEntries();

						final BBEntry entry = entries.get(messageNum);

						// TODO equating players and their names might be bad...
						if ( entry.getAuthor().equals( player.getName() ) ) {
							debug("Removing Message...");
							board.removeEntry(messageNum);
							
							debug("Renumbering entries...");
							board.renumber(messageNum);
							
							debug("Done.");
						}
						else send("BB: You may only delete your own posts.", client);
					}
					else send("No such message", client);
				}
				else send(gameError("@bb", ErrorCodes.INVALID_SYNTAX), client); // invalid message num
			}
			else if ( command.equals("+read")  ) {
				debug("bb:read");

				// should this be handled elsewhere (index/bounds issue)
				if (board.getNumMessages() <= 0 || messageNum >= board.getNumMessages()) {
					send( String.format("%s.%d: No such Message", board.getName(), messageNum), client);
				}
				else {
					final BBEntry entry = board.getEntry(messageNum);

					if (entry != null) {						
						send( Utils.mkList(
								"ID: " + entry.getId(),
								"Author: " + entry.getAuthor(),
								"Subject: " + entry.getSubject(),
								"",
								entry.getMessage()
								), client);
					}
					else send("No such entry!", client);
				}
			}
			else if ( command.equals("+reply") ) {
				// @bb +reply board_name.message_num message
				if (board.getNumMessages() <= 0 || messageNum >= board.getNumMessages()) {
					System.out.println("No Such Message.");
				}
				else {
					final BBEntry entry = board.getEntry(messageNum);

					if (entry != null) {
						final String author = player.getName();
						
						final String subject = ( entry.getSubject().startsWith("RE:") ) ? entry.getSubject() : "RE: " + entry.getSubject();
						final String message = param;
						
						board.write(author, subject, message);
						
						send("Wrote Reply (" + board.getName() + ")", client);
					}
					else send("No such entry!", client);
				}
			}
			else if (command.equals("+write") || command.equals("+w")) {
				// @bb +write board_name subject=message
				String[] entry = param.split("=");

				if (entry.length == 2) {
					final String author = player.getName();
					
					final String subject = entry[0];
					final String message = entry[1];

					board.write(author, subject, message);
					
					send("Wrote Message (" + board.getName() + ")", client);
				}
				else send(gameError("@bb", ErrorCodes.INVALID_SYNTAX), client);
			}
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
		// TODO fix this command to be able to specify a particular auction
		final Player player = getPlayer(client);

		// bid <auction id/item> <money - in copper pieces>
		
		// -- ideas
		// bid 'staff of the archmagi' 600pp
		// bid staff 600
		// bid 5 600

		Integer itemId = -1;
		String itemName = "";
		Integer money = -1;

		final String[] args = arg.split(" ");

		if( args.length == 2 ) {
			itemId = Utils.toInt(args[0], -1);

			if( itemId == -1 ) itemName = args[0];

			money = Utils.toInt(args[0], -1);
		}

		if ( money != -1 ) {
			Auction auction = null;

			if ( itemId != -1 ) {
				System.out.println("Item ID: \'" + itemId + "\'");
				
				auction = getAuction(itemId);
			}
			else {
				System.out.println("Item name: \'" + itemName + "\'");
				
				//auction = getAuction(itemName);
			}

			System.out.println("Money: \'" + money + "\'");

			/*
			 * switch(type) { case "pp": coins = Coins.platinum(value); break;
			 * case "gp": coins = Coins.gold(value); break; case "sp": coins =
			 * Coins.silver(value); break; case "cp": coins =
			 * Coins.copper(value); break; default: coins = Coins.copper(value);
			 * break; }
			 */

			if( auction != null ) {
				Coins coins = Coins.copper(money);

				Bid bid = new Bid(player, coins);

				if ( auction.placeBid(bid) ) {
					send("You successfully placed a bid of " + coins.toString(true) + " on " + itemName, client);
					
					// TODO handle max bid check?
				}
				else send("You failed to place a bid (Cause?)", client);
			}
			else send("No such auction!", client);
		}
	}
	
	/**
	 * Command: boards
	 * 
	 * Add, Delete, List BulletinBoard(s)
	 * 
	 * +add <new board name> add a new board +del <existing board name> delete
	 * an existing board +list list the existing boards
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_boards(final String arg, final Client client) {
		if (!arg.equals("")) {
			final String[] args = arg.split(" ");

			if (args[0].charAt(0) == '+') {
				final String param = args[0].substring(1);
				final String board_name = args[1].toLowerCase();

				if (args.length == 2) {
					if (param.equalsIgnoreCase("add")) {
						final BulletinBoard board = new BulletinBoard(board_name);
						final Player player = getPlayer(client);
						
						board.setOwner( player );
						
						this.boards.put(board_name, new BulletinBoard(board_name));
						
						send("Board Added - " + board_name, client);
					}
					else if (param.equalsIgnoreCase("del")) {
						final BulletinBoard board = getBoard(board_name);

						if( board != null ) {
							final Player player = getPlayer(client);

							if( board.getOwner() == player || checkAccess(player, Constants.ADMIN) ) {
								// TODO implement delete confirmation
								/*String msg = String.format("Do you want to delete the board '%s'?", board_name);
								requestInput(msg, client);*/
								
								// TODO ask for confirmation?
								this.boards.remove(board_name);
								
								send("Board Removed - " + board_name, client);
							}
							else {
								send("ERROR: You do not own that board!", client);
							}
						}
					}
					else if(param.equalsIgnoreCase("info")) {
						final BulletinBoard board = getBoard(board_name);

						if( board != null ) {
							final List<String> data = Utils.mkList(
									Utils.padRight("", '-', 75),
									"    Name: " + board.getName(),
									"   Owner: " + ((board.getOwner() != null) ? board.getOwner().getName() : "none"),
									"Messages: " + board.getNumMessages(),
									Utils.padRight("", '-', 75)
									);
							send(data, client);
						}
					}
				}
				else if(args.length == 3) {
					// +transfer board new_owner
					if(param.equalsIgnoreCase("transfer")) {
						final BulletinBoard board = getBoard(board_name);

						if( board != null ) {
							final Player player = getPlayer(client);
							final Player player1 = getPlayer(args[2]);

							if( board.getOwner() == player || checkAccess(player, Constants.ADMIN) ) {
								if( player1 != null ) {
									board.setOwner(player1);
								}
							}
						}
					}
				}
			}
		}
		else {
			send("Boards", client);
			
			send(Utils.padRight("", '-', 40), client);
			
			for (final Map.Entry<String, BulletinBoard> entry : this.boards.entrySet()) {
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

		if ( player.getStatus().equals(Constants.ST_INTERACT) ) { // interact mode
			NPC npc = (NPC) player.getTarget();
			
			// TODO null check?
			
			send(npc.getName(), client); // tell us his/her name

			debug("Target is NPC.");

			if ( npc.hasFlag(ObjectFlag.VENDOR) ) { // if the npc is a vendor
				debug("Target is Vendor");
				
				Vendor v = null;
				
				v = (Vendor) npc; // Cast npc as a vendor into a vendor reference	
				
				/*try {
					v = (Vendor) npc; // Cast npc as a vendor into a vendor reference	
				}
				catch(final Exception e) {
					System.out.println("--- Stack Trace ---");
					e.printStackTrace();
				}*/
				
				if ( v == null ) return;

				if (v.hasItem(arg)) // if the vendor had that item
				{
					final Coins value = v.getItem(arg).getValue();
					
					if( player.getMoney().isMoreOrEqual(value) ) {
						//final Coins payment = player.getMoney().subtract(value);
						final Item item1 = v.buy(arg, value);
						
						// TODO clean this up somehow
						debug("Player Funds (cp): " + player.getMoney().numOfCopper());
						debug("  Item Value (cp): " + value.numOfCopper());
						//debug("        Cost (cp): " + temp.numOfCopper());
						
						player.setMoney( player.getMoney().subtract(value) );
						
						debug("");
						debug("Player Funds (cp): " + player.getMoney().numOfCopper());

						send("Item Bought!", client); // tell us that we bought the item

						send(colors(npc.getName(), getDisplayColor("npc")) + " takes your money and gives you a " + colors(item1.getName(), getDisplayColor("item")), client); // response

						item1.setLocation(player.getDBRef()); // change item's location

						player.getInventory().add(item1); // add the item to the player's inventory
						
						addMessage( npc.say("There you go.") ); // npc response 
					}
					else send("I'm sorry, you can't afford that.", client);
				}
				else send("I'm sorry, we don't sell that.", client);
			}
		}
		else debug("Target is not npc.");
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
		
		final String month_name = MONTH_NAMES[month - 1];

		String start;
		String end;
		
		client.write(month_name);
		client.write('\n');
		
		// the month starts on the first day
		for (int i = 1; i <= daysOfMonth; ++i) {
			if (i < 10) start = "|  ";
			else        start = "| ";

			debug("" + i + ": " + (i % 7));

			if (i % 7 > 0)             end = " ";
			else if (i != daysOfMonth) end = " |\n";
			else                       end = " |";
			
			if(i == day) client.write(start + colors(i + "", "green") + end);
			else         client.write(start + i + end);
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
	 * COMMAND OBJECT EXISTS (current code reintegrated from that command --
	 * 4/13/2013)
	 * 
	 * @param arg
	 * @param client
	 */
	/*private void cmd_cast(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if ( !player.isCaster() ) {
			send("What do you think you are anyway? Some kind of wizard? That's just mumbo-jumbo to you!", client);
			return;
		}
		
		final String spellName = arg;

		final Spell spell = getSpell(spellName);

		if (spell == null) {
			send("You move your fingers and mumble, but nothing happens. Must have been the wrong words.", client);
			debug("CastCommand spellName: " + spellName);
			return;
		}
		
		// level, spell check -- can the player cast this level spell...
		// should this be reversed???
		// ^No, but the player's level and effective caster level is not
		// equivalent to the spell's level
		// PL -> SL: 0 -> 0, 1,2 -> 1, 3,4 -> 2, 5,6 -> 3, 7 -> 4...
		//if (player.getLevel() < spell.getLevel()) {
		if( true ) {
			// add reagents check!
			if( !hasReagents(player, spell) ) {
				send("Insufficient spell components", client);
				return;
			}
			
			if( player.getMana() < spell.getManaCost() ) {
				send("Insufficient Mana!", client);
				return;
			}

			// reduce mana by spell's mana cost
			player.setMana( -spell.getManaCost() );
			
			// TODO unreliable armor getting call
			// calculate spell failure (basic, just checks armor for now)
			Armor armor = (Armor) player.getSlots().get("armor").getItem();
			
			// TODO spell failure should be balance by something
			double spellFailure = 0;

			if (armor != null) {
				spellFailure = armor.getSpellFailure() * 100; // spellFailure stored as percentage
				debug("Spell Failure: " + spellFailure);
			}

			// Create random number 1 - 100
			int randNumber = (int) ((Math.random() * 100) + 1);

			debug("d100 Roll: " + randNumber);
			
			// success percentage > 50
			if (randNumber - spellFailure > 50 ) {
				final RangeData rd = spell.getRangeData();
				
				if( rd.isType( RangeType.PERSONAL ) || rd.isType(RangeType.TOUCH) ) {
				}
				
				// TODO need an unambiguous way to identify a target..
				// target check
				if (player.getTarget() == null) {
					// TODO what about multi-target?
					// if no target then auto-target self, etc, dependent on spell
					// auto-target to self
					player.setTarget(player);
				}

				final MUDObject target = player.getTarget();
				
				// cast spell 
				String message = spell.getCastMessage();
				
				message = message.replace("&target", player.getTarget().getName());
				
				send(message, client);
				
				// TODO no such method yet
				//applyEffects(target, spell.getEffects());
				
				// apply effects to the target
				for (final Effect e : spell.getEffects()) {
					if ( target.isType(TypeFlag.PLAYER) ) {
						System.out.println("Target is Player.");
						final Player ptarget = (Player) target;

						applyEffect(ptarget, e); // apply the effect to the target

						// spell timer with default (60 sec) cooldown
						final SpellTimer sTimer = new SpellTimer(spell, 60);
						
						getSpellTimers(player).add(sTimer);
						
						timer.scheduleAtFixedRate(sTimer, 0, 1000);
						
						// effect timer with default (30 sec) cooldown
						final EffectTimer etimer = new EffectTimer(e, 30);
						
						getEffectTimers(player).add(etimer);
						
						timer.scheduleAtFixedRate(etimer, 0, 1000); // create countdown timer
					}
					else {
						System.out.println("Target is not Player.");
						applyEffect(target, e);
					}
				}

				// if our target is a player tell them otherwise don't bother
				if ( target.isType(TypeFlag.PLAYER) ) {
					debug("Target is Player.");
					
					//Message msg = new Message(player, (Player) target, player.getName() + " cast " + spell.getName() + " on you.");
					Message msg = new Message(null, (Player) target, player.getName() + " cast " + spell.getName() + " on you.");
					
					addMessage(msg);
				}
				
				player.setLastSpell(spell);
			}
			else {
				send("A bit of magical energy sparks off you briefly, then fizzles out. Drat!", client);
			}
		}
	}*/
	

	private void cmd_cast(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if ( !player.isCaster() ) {
			send("What do you think you are anyway? Some kind of wizard? That's just mumbo-jumbo to you!", client);
			return;
		}

		final String spellName = arg;

		final Spell spell = getSpell(spellName);

		if (spell == null) {
			send("You move your fingers and mumble, but nothing happens. Must have been the wrong words.", client);
			debug("CastCommand spellName: " + spellName);
			return;
		}

		// level, spell check -- can the player cast this level spell...
		// should this be reversed???
		// ^No, but the player's level and effective caster level is not
		// equivalent to the spell's level
		// PL -> SL: 0 -> 0, 1,2 -> 1, 3,4 -> 2, 5,6 -> 3, 7 -> 4...
		if (player.getLevel() >= spell.getLevel()) {
			// add reagents check!
			if( spell.getReagents() != null ) {
				/*for(Reagent r : spell.getReagents().values()) {
						System.out.println(r.getName());
						if( player.getInventory().contains(r) ) {
						}
						else {
							send("Insufficient spell components", client);
							return;
						}
					}*/
			}

			// add reagents check!
			if( !hasReagents(player, spell) ) {
				send("Insufficient spell components", client);
				return;
			}

			if( player.getMana() < spell.getManaCost() ) {
				send("Insufficient Mana!", client);
				return;
			}

			// target check, if no target then auto-target self, etc, dependent on spell
			if (player.getTarget() == null) {
				final RangeData rd = spell.getRangeData();

				int range = rd.getRange(player.getLevel());
				if( range == 0 ) player.setTarget(player); // auto-target to self, if spell takes self
			}

			final MUDObject target = player.getTarget();

			// check validity of target for this spell
			if( !validTarget( target, spell, player ) ) {
				send("Game> Invalid Target for Spell!", client);
				return;
			}

			// expend mana
			// reduce mana by spell's mana cost
			player.setMana( -spell.getManaCost() );

			// TODO unreliable armor getting call
			// calculate spell failure (basic, just checks armor for now)
			Armor armor = (Armor) player.getSlots().get("armor").getItem();

			// TODO spell failure should be balance by something
			double spellFailure = 0;

			if (armor != null) {
				spellFailure = armor.getSpellFailure() * 100; // spellFailure stored as percentage
				debug("Spell Failure: " + spellFailure);
			}

			// Create random number 1 - 100
			int randNumber = (int) ((Math.random() * 100) + 1);

			debug("d100 Roll: " + randNumber);

			// success percentage > 50
			if (randNumber - spellFailure > 50 ) {
				// cast spell
				send(spell.getCastMessage().replace("&target", player.getTarget().getName()), client);

				// apply effects to the target
				for (final Effect e : spell.getEffects()) {
					applyEffect(target, e); // apply the effect to the target

					// is there ever a case where the caster wouldn't be a player here?
					SpellTimer sTimer = new SpellTimer(spell, 60);     // spell timer with default (60 sec) cooldown
					addSpellTimer(player, sTimer);
					scheduleAtFixedRate(sTimer, 0, 1000);

					EffectTimer etimer = new EffectTimer(e, 30);
					addEffectTimer(player, etimer);
					scheduleAtFixedRate(etimer, 0, 1000); // create countdown timer

					// if our target is a player set timers for us and tell them, otherwise don't bother
					if(target instanceof Player) {
						debug("Target is Player.");
						
						if( target != player ) {
							Message msg = new Message(player, (Player) target, player.getName() + " cast " + spell.getName() + " on you.");
							
							addMessage(msg);
						}
					}
				}
			}
			else {
				send("A bit of magical energy sparks off you briefly, then fizzles out. Drat!", client);
			}

			player.setLastSpell(spell);
		}
		else send("Insufficient spell mastery (level too low).", client);
	}

	private void cmd_chargen(final String arg, final Client client) {
		Player player = getPlayer(client);

		player.setStatus(Constants.ST_EDIT); // Player Status set to -Edit-
		player.setEditor(Editors.CHARGEN); // set the player's editor to CHARGEN

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
		final Player player = getPlayer(client);

		String[] args = arg.split(" ");

		// TODO don't receive proof or confirmation of a channel's creation/deletion...

		// show a list of available channels and whether you are on them
		if (arg.equalsIgnoreCase("#channels")) {
			final List<String> sendList = new LinkedList<String>();

			sendList.add("Chat Channels");
			sendList.add("--------------------------------");

			final StringBuilder sb = new StringBuilder();

			for (final ChatChannel channel : chan.getChatChannels()) {
				if( !channel.isHidden() || checkAccess(player, Constants.WIZARD) || channel.isListener(player) ) {
					sb.append(Utils.padRight(channel.getName(), 8));
					sb.append(" ");
					sb.append(Utils.padRight("[" + Utils.padRight(channel.getShortName().toUpperCase(), '_', 4)+ "]", 6));
					sb.append(" ");

					if ( channel.isListener( player ) ) sb.append(colors("Enabled", "green"));
					else                                sb.append(colors("Disabled", "red"));

					sendList.add(sb.toString());

					sb.delete(0, sb.length());
				}
			}

			sendList.add("--------------------------------");

			send(sendList, client);
		}
		else if (args.length > 1) {
			// party #<param> <channel_name>
			final String param = args[0];

			if (param.charAt(0) == '#') { // chat subcommand
				final String channelName = args[1];
				final String password = ( args.length == 3 ) ? args[2] : ""; // TODO kludgy...

				if (param.equalsIgnoreCase("#join")) {
					final Result result = chan.add(player, channelName, password);

					String message = "";

					switch(result) {
					case JOIN:        message = "Joined channel: " + channelName;                  break;
					case WRONG_PASS:  message = "Error: Failed to join. (Wrong Password)";         break;
					case RESTRICTED:  message = "Error: Failed to join. (Channel is restricted!)"; break;
					case CURR_LISTEN: message = "Error: Failed to join. (already listening).";     break;
					case NO_CHANNEL:  message = "Error: Failed to join. (No Such Channel)";        break;
					default: break;
					}
					
					send("Chat> " + message, client);
					
					// kludge
					if( result == Result.JOIN ) chan.send(channelName, player.getName() + " joined the channel.");
				}
				else if ( param.equalsIgnoreCase("#leave") ) {
					final Result result = chan.remove(player, channelName);
					
					String message = "";
					
					switch(result) {
					case LEAVE:         message = "Chat> Left channel: " + channelName;              break;
					case CURR_NOLISTEN: message = "Chat> Error: Failed to leave. (Not Listening).";  break;
					case NO_CHANNEL:    message = "Chat> Error: Failed to leave. (No Such Channel)"; break;
					default: break;
					}
					
					send(message, client);
					
					// kludge
					if( result == Result.LEAVE ) chan.send(channelName, player.getName() + " left the channel.");
				}
				else if (param.equalsIgnoreCase("#listeners")) { // argument: show listeners on a specific channel
					final List<String> sendList = new LinkedList<String>();

					if( checkAccess(player, Constants.WIZARD) ) {
						sendList.add("Listeners on Chat Channel: " + channelName.toUpperCase());
						sendList.add("------------------------------");

						for (final Player p : chan.getListeners(channelName)) {
							sendList.add(p.getName() + "\n");
						}

						sendList.add("------------------------------");

						send(sendList, client);
					}
					else send("Chat> Error: param '#listeners' requires Admin permissions.", client);
				}
				else if (param.equalsIgnoreCase("#restrict")) {
					if( checkAccess(player, Constants.ADMIN) ) {
						final int restrict = Utils.toInt(args[2], 0);
						
						final Result result = chan.modifyRestriction(channelName, restrict);
						
						switch(result) {
						case MODIFY_OK:
							send("Chat> " + channelName + " restriction changed to " + restrict, client);
							break;
						case MODIFY_NOK:
							send("Chat>  " + channelName + " invalid restriction.", client);
							break;
						case NO_CHANNEL:
							send("No such channel.", client);
							break;
						default:
							break;
						}
					}
					else send("Chat> Error: param '#restrict' requires Admin permissions.", client); 
				}
				else if (param.equalsIgnoreCase("#destroy")) {
					// test for permissions
					if( checkAccess(player, Constants.ADMIN) ) {
						chan.destroyChannel(channelName);
						send("Chat> Destroyed channel: " + channelName + ".", client);
					}
					else send("Chat> Error: param '#destroy' requires Admin permissions.", client);
				}
				else if (param.equalsIgnoreCase("#create")) {
					// test for permissions
					if( checkAccess(player, Constants.ADMIN) ) {
						chan.makeChannel(channelName);
						send("Chat> Created new channel: " + channelName + ".", client);
					}
					else send("Chat> Error: param '#create' requires Admin permissions.", client);
				}
				/*else {
					send("No such chat channel.", client);
				}*/
			}
		}
		else { // sending a chat message
			final String test = chan.resolveShortName(args[0]);

			final String channelName = test.equals("") ? args[0] : test;
			final String msg = arg.replace(channelName, "").trim();

			if (chan.hasChannel(channelName)) {
				chan.send(channelName, player, msg);

				debug("New message (" + channelName + "): " + msg);

				logChat("(" + channelName + ") <" + player.getName() + "> " + arg);
			}
			else {
				send("Chat> No such chat channel.", client);
			}
		}
	}

	private void cmd_colors(final String arg, final Client client) {
		if (arg.equals("")) {
			for (final String s : displayColors.keySet()) {
				send(s + ": " + colors(s, getDisplayColor(s)), client);
			}
		}
	}

	private void cmd_config(final String arg, final Client client) {
		// use this to replace 'ansi' and 'msp' commands?
		// or possibly alias them to it?
		// @config ansi = on, @config ansi = off
		// @config msp = on, @config msp = off

		// String[] args = arg.split("=");
		String[] args = arg.split(" ");

		if (args.length == 2) {
			if (args[0].equals("color")) {
				if (args[1].equalsIgnoreCase("off")) {
					color = Constants.DISABLED; // disables color
					send("Colors turned off.", client);
				}
				else if (args[1].equalsIgnoreCase("on")) {
					color = Constants.ANSI; // disables color
					send("Colors turned on.", client);
				}
			}
			else if (args[0].equals("colors")) {
				if( color != Constants.DISABLED ) {
					if (args[1].equalsIgnoreCase("ansi")) {
						color = Constants.ANSI; // enables ansi and disables xterm
						client.write("\033[;1m"); // tell client to use bright version of ANSI Colors
						// send("> Using BRIGHT ANSI colors <", client); // indicate the use of bright ansi colors to the client
						send(Utils.rainbow("ANSI", ANSI) + " colors turned on.", client);
					}
					else if (args[1].equalsIgnoreCase("xterm")) {
						color = Constants.XTERM; // enables xterm and disables ansi
						send(Utils.rainbow("XTERM256", XTERM256) + " colors turned on.", client);
					}
					else {
						send("Colors are disabled.", client);
					}
				}
			}
			else if (args[0].equals("linelimit")) {
				// default line limit is?
				int line_limit = Utils.toInt(args[1], 80);

				// TODO validate input for a given range?
				getPlayer(client).setLineLimit(line_limit);

				send("Line Limit set to " + line_limit, client);
			}
			else if (args[0].equals("msp")) {
				if (args[1].equals("on")) {
					msp = 1;
				}
				else if (args[1].equals("off")) {
					msp = 0;
				}

				send("MSP turned " + ((msp == 0) ? "off" : "on"), client);
			}
		}
		else if (args.length == 1) {
			if (args[0].equals("color")) {
				if( color != Constants.DISABLED ) send("Colors are on.", client);
				else                              send("Colors are off.", client);
			}
			else if (args[0].equals("colors")) {
				if (color == Constants.ANSI)       send("Using " + Utils.rainbow("ANSI", ANSI) + " colors.", client);
				else if (color == Constants.XTERM) send("Using " + Utils.rainbow("XTERM256", XTERM256) + " colors.", client);
				else                               send("Colors are disabled.", client);
			}
			else if (args[0].equals("msp")) {
				switch(msp) {
				case 0:  send("MSP: off", client);       break;
				case 1:  send("MSP: on", client);        break;
				default: send("MSP: -invalid-", client); break;
				}
				
				//if (msp == 0)      send("MSP: off", client);
				//else if (msp == 1) send("MSP: on", client);
			}

			if (args[0].equals("linelimit")) {
				send("Line Limit is " + getPlayer(client).getLineLimit() + ".", client);
			}
		}

		/*
		 * if ( args.length > 1 ) { if ( arg.equalsIgnoreCase("prompt_enabled")
		 * ) { switch( args[1] ) { case "true": prompt_enabled = true;
		 * send("SRV> set " + args[0] + " to TRUE.", client); break; case
		 * "false": prompt_enabled = false; send("SRV> set " + args[0] +
		 * " to FALSE.", client); break; default:
		 * send("SRV> Invalid config setting!", client); break; } } }
		 */

		/*
		 * f ( arg.contains("=") ) { String[] args = arg.split("=");
		 * 
		 * if ( config.containsKey( Utils.trim( args[0] ) ) ) {
		 * debug("Config> Setting '" + Utils.trim( args[0] ) + "' to '" +
		 * Utils.trim( args[1] )); config.put( Utils.trim( args[0] ),
		 * Utils.trim( args[1] ) ); send("Game [config]> " + Utils.trim( args[0]
		 * ) + ": " + Utils.trim( args[1] ), client); } else {
		 * debug("Game [config]> no such configurable setting exists.");
		 * send("Game [config]> no such configurable setting exists.", client);
		 * } } else { if ( arg.equals("list") ) { send("Configuration Options",
		 * client); for (Entry<String, String> e : config.entrySet()) { send(
		 * Utils.padRight( e.getKey(), ' ', 10 ) + " : " + e.getValue(),
		 * client); } } else { send( gameError("@config",
		 * ErrorCodes.INVALID_SYNTAX), client ); } }
		 */
	}

	/**
	 * Command: climb
	 * 
	 * Climb something
	 * 
	 * NOTE: this an invocation of skill usage, should I deviate the naming
	 * convention or other things to make note of this? Can generalize the input
	 * somehow so that using the command name can invoke the skill regardless of
	 * an independent command name check for each skill
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_climb(final String arg, final Client client) {
		final Player player = getPlayer(client);

		// identify the thing to be climbed, if it's possible
		Thing thing = getThing(arg, getRoom( player.getLocation() )); // ex. box, ladder, building

		if (thing == null) {
			send("Climb what?", client);
			return;
		}

		// check distance from object
		if (Utils.distance(player.getPosition(), thing.getPosition()) <= 1) {

			// get the check for it's difficulty (static assign for testing
			// purposes)
			int difficultyCheck = 10;

			// check to see if we can climb it
			boolean canClimb = skill_check(player, Skills.CLIMB, "1d4+1", difficultyCheck);

			// evaluate results
			if (canClimb) {
				Integer height = Utils.toInt(thing.attributes.get("height"), 1);

				if (height > 1) {
					send("You start climbing <direction> the " + thing.getName().toLowerCase(), client);
				}
				else if (height == 1) {
					send("You climb <up/onto> the " + thing.getName().toLowerCase(), client);
					Point thingPos = thing.getPosition();
					player.setPosition(thingPos.getX(), thingPos.getY());
					player.changePosition(0, 0, 1);
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
	 * "Clears" the screen by sending the specified number of blank lines.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_cls(final String arg, final Client client) {
		int numLines = Utils.toInt(arg, 25);
		int n = 0;

		while (n < numLines) {
			send("", client);
			n++;
		}
	}

	/**
	 * Command: connect
	 * 
	 * Connect to the game via a player or an account and then a player
	 * 
	 * NOTE: if I add all the usernames and passwords to a hashmap I could
	 * immensely simplify this, although some of the structure would change
	 * quite a bit I would check for a username (key) in the hashmap by
	 * searching for the username supplied then attempt to get it and test the
	 * password (value) against the password supplied
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_connect(final String arg, final Client client) {
		// Trim the argument of any additional whitespace
		final String[] args = Utils.trim(arg).split(" ");

		debug("" + args.length);
		
		// if the arguments are invalid somehow, tell the user what the correct options are
		if (args.length < 1 || args.length > 2 || args[0].equals("")) {
			send("Enter a valid character creation or connection string",                                 client);
			//send("such as 'create <character name> <password>' or 'connect <character name> <password>'", client);
			//send("To connect as a guest simply type 'connect guest'",                                     client);
			//send("NOTE: Using an valid account name and password will trigger the account options menu",  client);
			return;
		}

		final String user = Utils.trim(args[0]);
		final String pass = args.length > 1 ? Utils.trim(args[1]) : " ";

		debug("Username?: " + user);
		debug("Password?: " + Utils.padRight("", '*', pass.length()));
		debug("");
		
		Player player = null;
		
		// Guest Players
		if ( user.equalsIgnoreCase("account") ) {
			setClientState(client, "account_login");
			handle_account_login(user, client);
		}
		else if ( user.equalsIgnoreCase("guest") ) {
			if (guest_users == 1) {
				player = getNextGuest();

				if (player == null) {
					final Player guest = createGuestPlayer();
					
					// if we don't have sufficient existing guest players, make a new one
					objectDB.addAsNew( guest );
				}

				init_conn(player, client, false);

				guests++;
			}
			else send("Sorry. Guest users have been disabled.", client);
		}
		else {
			/*
			 * NOTE: if all players always existed, then instead of
			 * instantiating a player i'd simply assign a client to it.
			 * Otherwise I need to get the player data from somewhere so I can
			 * load it up.
			 */

			/*
			 * I don't want account names to conflict with characters, so
			 * perhaps I will insert a stopgap measure where you must indicate
			 * an account like this:
			 * 
			 * connect account
			 * 
			 * If the user input is 'account' we will assume you want to connect
			 * to an account and will do an interactive login for you.
			 * 
			 * Other we will look for a character by the name given.
			 */

			if (use_accounts) {
				// account check
				final Account a = acctMgr.getAccount(user, pass);

				// debug("CONNECT: Fail");
				// send("That account does not exist or the password is incorrect!",
				// client);

				if (a != null) {
					if (a.getStatus() == Account.Status.ACTIVE) {
						this.caTable.put(client, a);
						account_menu(a, client);
						setClientState(client, "account_menu");
					}
					else {
						final String message = Account.msgmap.get( a.getStatus() );
						
						if( message != null ) send(message, client);
					}

					return;
				}
			}

			// character check
			// NOTE: use DB here too
			final Player p = objectDB.getPlayer(user);

			if (p == null) {
				debug("CONNECT: Fail");
				debug("No such player!");
				send("That player does not exist or the password is incorrect!", client);
				return;
			}

			if (p.getPStatus() == Player.Status.BANNED) {
				debug("CONNECT: Fail");
				debug("Player " + p.getName() + " is banned!");
				send("Player is banned.", client);
				return;
			}
			
			final String password = p.getPass();
			final String password_hash = Utils.hash(pass);

			//if ( !p.getPass().equals()) {
			if( !password.equals(password_hash) ) {
				debug("PASS: Fail");
				send("That player does not exist or the password is incorrect!", client);
				return;
			}

			debug("PASS: Pass"); // report success for password check
			
			// TODO how do I handle what amounts to a second login after some kind of pseudo-disconnect 
			
			// remove player in case of relogin (this is the not ideal handling)
			final int playerDBRef = p.getDBRef();
			final String playerName = p.getName();
			
			for (final Player player1 : this.players) {
				if ( player1.getDBRef() == playerDBRef && player1.getName().equals(playerName) ) {
					/* won't work because client object vanishes when the player loses connection */
					init_disconn(player1.getClient(), false);
					break;
				}
			}
			
			// connect to the game
			if (mode == GameMode.NORMAL) {
				init_conn(p, client, false); // Open Mode
			}
			else if (mode == GameMode.WIZARD) {
				if (p.getFlags().contains("W") || p.getAccess() == Constants.WIZARD) {
					init_conn(p, client, false); // Wizard-Only Mode
				}
				else send("Sorry, only Wizards are allowed to login at this time.", client);
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

		if( console_enabled ) {
			if (credentials.length == 2) {
				final String user = credentials[0];
				final String pass = credentials[1];
				
				// TODO make a more secure arrangement here
				if (user.equals("admin") && pass.equals("admin")) {
					client.write("Launching Console...");
					
					cmon.addConsole(client);
					
					client.writeln("Done.");
					client.writeln("");
					client.writeln("MUDServer Console");
				}
			}
		}
		else {
			client.writeln("Console Disabled.");
		}
	}
	
	private void ctl_break(final Client client) {
		final Player player = getPlayer(client);
		final Player controller = playerControlMap.getController(player);
		
		if (controller != null) playerControlMap.stopControllingAnyone(controller);
	}

	/**
	 * Command: control
	 * 
	 * Allow someone with sufficient permissions to control an npc
	 * 
	 * NOTE: needs fixing to be sure no one can end up in control of another
	 * player NOTE: should npcs have some kind of controllable measure so that a
	 * person can control some npcs but not others?
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_control(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if (arg.toLowerCase().equals("#break")) { // break control, return controlling player to their own body (ha ha)
			final Player controller = playerControlMap.getController(player); // get the controller

			// if the player is a slave to some controller
			if (controller != null) {
				// show control table state before change
				debug("DM Control Table:");
				debug(playerControlMap.toString());

				// get name of slave npc
				String name = playerControlMap.getSlave(controller).getName();

				playerControlMap.stopControllingAnyone(controller); // stop controlling an npc

				// show control table state after change
				debug("DM Control Table:");
				debug(playerControlMap.toString());

				// status message, indicate that we have stopped controlling an npc and what its name is
				send("Game> You stop controlling " + name, client);
			}
		}
		else { // should not be able to use this to control other players (at least not normally) NOTE: needs work
			final Player npc = getNPC(arg); // get the npc we want to control by name
			
			// TODO what is the point here?
			//debug(player); // print out player as string
			//debug(npc); // print out npc as string

			if ( npc.isType(TypeFlag.NPC) ) {
				// show control table state before change
				debug("DM Control Table:");
				debug(playerControlMap.toString());

				final Player oldSlave;

				// if the player is really an NPC at the moment (consequence of one level control)
				if ( player.isType(TypeFlag.NPC) ) {
					oldSlave = playerControlMap.control(playerControlMap.getController(player), npc);
				}
				else {
					oldSlave = playerControlMap.control(player, npc); // control an npc
				}

				player.setController(true); // mark the player as controlling an npc (commented out in PlayerControlMap)

				// if we were already controlling an npc, revoke the privilege
				// escalation (controlled npcs have the same permissions as the
				// controller)
				if (oldSlave != null) {
					oldSlave.setAccess(Constants.USER); // revoke any greater privileges granted
				}

				// show control table state after change
				debug("DM Control Table:");
				debug(playerControlMap.toString());

				// status message, indicate that we are now controlling an npc and what its name is
				send("Game> You are now controlling " + npc.getName(), client);
			}
			else {
				send("Players are not controllable, only NPCs", client);
			}
		}
	}
	
	/**
	 * Command: craft
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_craft(final String arg, final Client client) {
		final Player player = getPlayer(client);

		debug( String.format("CRAFT: '%s'", arg) );

		boolean haveMaterials = false;

		if( arg != null ) {
			if( arg.equals("recipes" ) ) {
				final List<String> output = new ArrayList<String>(recipes.size());
				
				List<String> recipeData;
				
				for(final String recipeName : recipes.keySet()) {
					recipeData = Arrays.asList( recipes.get(recipeName).getComponents() );
					
					output.add( String.format("%s - %s", recipeName, recipeData) );
				}
				
				send(output, client);
			}
			else {
				final Recipe recipe = recipes.get( arg );

				if( recipe != null ) {
					debug( String.format("RECIPE: %s", recipe.getName()) );

					final List<Item> materials = new LinkedList<Item>();   // list of actual items to use
					final List<String> missing = new LinkedList<String>(); // list of missing materials/items

					// for each specified component...
					for(final String component : recipe.getComponents()) {
						System.out.println("Component: " + component);
						
						// TODO this should never return similar items only exact ones
						final Item item = MudUtils.findItem(component, player.getInventory());

						if( item != null ) materials.add(item);
						else               missing.add( component );
					}

					haveMaterials = (missing.size() == 0); // no missing materials

					// look at player inventory

					// do crafting?
					if( haveMaterials ) {
						System.out.println("Materials: " + materials);
						
						final List<Item> inventory = player.getInventory();
						
						// remove materials from inventory
						for(final Item item : materials) {
							inventory.remove( item );
						}

						materials.clear();

						final Item item = createItem("mud." + arg.replace(" ", "_"), true);

						if( item != null ) {
							// TODO need to not destroy input material in case of code bugs?
							// also destroy by removing from db)
							for(final Item item1 : materials) {
								objectDB.remove( item1 );
								objectDB.removeItem( item1 );
							}
							
							send("You craft " + colors(item.getName(), getDisplayColor(TypeFlag.ITEM)), client);

							inventory.add( item );
							item.setLocation( player.getDBRef() );
						}
						else {
							send("crafting failed.", client);
							debug("createItem: NULL");
						}
					}
					else send(recipe.getName() + ": missing materials (" + missing + ")", client);
				}
				else {
					debug("RECIPE: -NONE-");
					send("Craft what?", client);
				}
			}
		}
	}

	/**
	 * Command: create
	 * 
	 * Create a new player
	 * 
	 * NOTE: would possibly need to be revised if a multi-player account was
	 * what was logged into, rather than a single player. Namely, how do I
	 * handler character creation from the account menu?
	 * 
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_create(final String arg, final Client client) {
		String user; // user name
		String pass; // password
		
		String[] args = arg.split(" ");
		
		// test number of arguments
		if ( args.length != 2 ) {
			send("CREATE: insufficient arguments. Did you forget to specify a password?", client);
			return;
		}

		// get the username and password from the command arguments
		user = Utils.trim( args[0] ); // new user name (trimmed)
		pass = Utils.trim( args[1] ); // new user password (trimmed)

		// check for existing player by that name,
		// if exists report that the name is already used, if not continue on
		// if (!objectDB.hasName(user) && validateName(user)) {
		if ( objectDB.hasName(user, TypeFlag.PLAYER) || !validateName(user) ) {
			// indicate the unavailability and/or unsuitability of the chosen name
			send("That name is not available, please choose another and try again.", client);
		}
		else {
			// create a new player object for the new player
			final Player player = new Player(-1, user, Utils.hash(pass), start_room);

			// TODO decide if this is okay or find a better way
			// sclients.put(client, player);
			// run character generation (should we do this here?)

			debug("");
			debug("\'" + module.getName() + "\'");
			debug("");

			// does this belong here or in init_conn
			if (module != null) {
				//
				module.PCInit(player);
				
				// TODO how to print this out?
				//debug(player.getSlots().keySet());

				// give them starting equipment?
				
				//
				final Item helpNote = createItem("Note", "Read this note for help with 'read'.", player.getDBRef());

				helpNote.setProperty("_game/readable", true);
				helpNote.setProperty("_game/text","Welcome.\nTo get started you should read the help/lore files and then go through character generation.");

				initCreatedItem(helpNote);

				player.getInventory().add( helpNote );
			}
			
			if( use_accounts ) {
				//acctMgr.getAccount(user, pass).linkCharacter(player);
				// TODO figure out how to make sure this account is created before any queries
				final Account a = acctMgr.addAccount(user, pass, 4);
				
				a.linkCharacter(player);
			}

			objectDB.addAsNew(player);
			objectDB.addPlayer(player); // add player to the auth table

			send("Welcome to the Game, " + colors(user, getDisplayColor("player")) + ". Your password is: " + colors(pass, "yellow"), client);

			// initiate the connection
			init_conn(player, client, true);
		}
	}

	// TODO write code for cmd_creatureedit(...)
	private void cmd_creatureedit(final String arg, final Client client) {
		final Player player = getPlayer(client);

		Creature creature = null;
		boolean exist = false;

		// get a room
		if (arg.equals("new")) { // create new room if no room to edit specified
			creature = createCreature();

			// add new creature to database
			// objectDB.addAsNew(creature);
			// objectDB.addCreature(creature);

			exist = true;
		}
		else {
			final int dbref = Utils.toInt(arg, -1);

			if (dbref != -1) {
				creature = null; // get creature by dbref? (Integer)

				for (final Creature c : objectDB.getCreatures()) {
					if (c.getDBRef() == dbref) {
						creature = c;
						break;
					}
				}
			}
			else {
				creature = null; // get creature by name (String)

				for (final Creature c : objectDB.getCreatures()) {
					if (c.getName().equalsIgnoreCase(c.getName())) {
						creature = c;
						break;
					}
				}
			}

			if (creature != null) {
				exist = true;
			}
		}

		if (exist) {
			editCreature(player, creature);
		}
	}

	/**
	 * Command: @check
	 * 
	 * Check for and lists the exits on a room, some visibility and state data
	 * about them would be useful (default locking state, type of lock, etc).
	 * Such information would help with troubleshooting any future problems.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_check(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		debug( room.getExits().toString() );

		for (final Exit exit : room.getExits()) {
			send(exit.getName(), client);

			if (exit.getDestination() == -1) send("\t" + colors("Unlinked", "red"), client);
			else                             send("\t" + colors("Linked", "green"), client);

			send("", client);

			if (exit.getExitType() == ExitType.DOOR) {
				send("\tLocked: " + (((Door) exit).isLocked() ? colors("Yes", "red") : colors("No", "yellow")), client);
				send("", client);
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
	private void cmd_commands(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final int access = player.getAccess();
		
		/*final List<Set<String>> commandMaps = new LinkedList<Set<String>>();
		
		commandMaps.add( commandMap.keySet() );
		commandMaps.add( player.getCommands().keySet() );

		int c = 0;

		StringBuilder sb = new StringBuilder();

		for (final Set<String> s : commandMaps) {
			for (String key : s) {
				System.out.println(key);
				debug(key);
				if (sb.toString().equals("")) sb.append(key);
				else                          sb.append(", " + key);
			}

			switch (c) {
			case 0:
				//System.out.println("mapped cmd");
				showDesc(colors("mapped: ", "yellow") + sb.toString(), client);
				break;
			case 1:
				//System.out.println("mapped player cmd");
				showDesc(colors("mapped(player): ", "yellow") + sb.toString(), client);
				break;
			default:
				break;
			}

			sb.delete(0, sb.length());

			c++;
		}*/
		
		send(colors("user commands: ", "green") + Utils.join(user_cmds, ", "), client);

		if (access >= Constants.BUILD) {
			System.out.println( Arrays.asList(build_cmds) );
			send(colors("builder commands: ", "cyan") + Utils.join(build_cmds, ", "), client);
		}

		if (access >= Constants.ADMIN) {
			System.out.println( Arrays.asList(admin_cmds) );
			send(colors("admin commands: ", "red") + Utils.join(admin_cmds, ", "), client);
		}

		if (access >= Constants.WIZARD) {
			System.out.println( Arrays.asList(wiz_cmds) );
			send(colors("wizard commands: ", "magenta") + Utils.join(wiz_cmds, ", "), client);
		}

		if (access >= Constants.SUPERUSER) {
			System.out.println( Arrays.asList(superuser_cmds) );
			send( colors("superuser commands: ", "yellow") + Utils.join(superuser_cmds, ", "), client);
		}
	}

	/**
	 * Command: Create Item Permissions: Admin?
	 * 
	 * Arbitrarily create an item with the specified name
	 * 
	 * @param arg
	 * @param client
	 */
	/*
	 * private void cmd_createItem(String arg, Client client) { Player player =
	 * getPlayer(client); Room room = getRoom(client);
	 * 
	 * //Item item = new Clothing(arg, 0, "cloak",1.0); // use the object
	 * loading constructor for testing purposes
	 * 
	 * int location = getPlayer(client).getLocation(); int dbref = getNextDB();
	 * 
	 * Item item = new Clothing(arg, "A new piece of clothing.", location,
	 * dbref, 0, ClothingType.SHIRT);
	 * 
	 * if (dbref == main.size()) { main.add(item.toDB()); main1.add(item); }
	 * else { main.set(item.getDBRef(), item.toDB()); main1.set(item.getDBRef(),
	 * item); }
	 * 
	 * int temp = 1;
	 * 
	 * if (temp == 1) { // test to see if it fits in the inventory
	 * player.getInventory().add(item); send("Item named " + item.getName() +
	 * "(#" + item.getDBRef() + ") created. " + item.getName() +
	 * " has been placed in your inventory.", client); } else {
	 * room.getItems().add(item); send("Item named " + item.getName() + "(#" +
	 * item.getDBRef() + ") created. " + item.getName() +
	 * " has been placed in your location.", client); } }
	 */
	
	/**
	 * Provide a way to get a basic answer to whether the player will
	 * have success killing/defeating/destroying the enemy/target. A measure
	 * of successful destruction without giving yes/no or numbers.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_consider(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		if( !arg.equals("") ) {
			// find the specified object in the room
			MUDObject obj = getObject(arg);
			
			// if it's a creature, npc, player, etc decide how hard it would be to kill
			if( obj.isType(TypeFlag.CREATURE) ) {
				final Creature c = (Creature) obj;
				
				// simulate combat
				
				// how likely are we to be able to kill them with our current loadout?
				
				// simple model that assumes we have more HP and could do up to 2x damage as it can
				if ( c.getHP() <= 2 * player.getHP() ) {
					send("You might be up to that.", client);
				}
			}
			else if( obj.isType(TypeFlag.NPC) ) {
				send("Hey! What did they ever do to you?", client);
			}
			else if( obj.isType(TypeFlag.PLAYER) ) {
				send("That doesn't seem wise...", client);
			}
		}
		else send("Consider what?", client);
	}
	
	/**
	 * assessment of character status, condition (health wise)
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_condition(final String arg, final Client client) {
		if ( !arg.equals("") ) {
			final NPC npc = getNPC(arg);

			if (npc != null) {
				send(npc.getName() + " " + "HP: " + npc.getHP() + "/" + npc.getTotalHP() + " " + npc.getState(), client);
				
				// TODO need to somehow represents the actual limbs/bodyparts a player has
				/*send("head: 100%", client);
				send("neck: 100%", client);
				send("left arm:  100% (left hand: 100%)", client);
				send("right arm: 100% (right hand: 100%)", client);
				send("chest: 100%", client);
				send("left leg:  100% (left foot: 100%)", client);
				send("right leg: 100% (right foot: 100%)", client);
				*/
			}
		}
		else {
			final Player player = getPlayer(client);

			if (player != null) {
				send(player.getName() + " " + "HP: " + player.getHP() + "/" + player.getTotalHP() + " " + player.getState(), client);

				/*send("head: 100%", client);
				send("neck: 100%", client);
				send("left arm:  100% (left hand: 100%)", client);
				send("right arm: 100% (right hand: 100%)", client);
				send("chest: 100%", client);
				send("left leg:  100% (left foot: 100%)", client);
				send("right leg: 100% (right foot: 100%)", client);*/
			}
		}
	}

	/**
	 * Command: @debug
	 * 
	 * Show requested debugging information, takes several arguments to show
	 * different information
	 * 
	 * on/off/client/clients/colors/creatures/dbdump/holidays/listen/memory/
	 * position/pos/ portals/seasons/timedata/timers/udbnstack/<a number>
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_debug(final String arg, final Client client) {
		String[] args = arg.split(":");
		String[] args2 = arg.split(" ");

		String param = arg.toLowerCase();

		if (param.equals("on")) {
			debug = true;
			send("Game> Debugging: On", client);
		}
		else if (param.equals("off")) {
			debug = false;
			send("Game> Debugging: Off", client);
		}
		else if (param.equals("client")) {
			/*
			 * tell us about ourselves (i.e. which client object we are and our
			 * ip address)
			 */
			send("" + client, client);
			send(client.getIPAddress(), client);
		}
		else if (param.equals("clients")) {
			int cn = 0;

			send( Utils.padRight("#", ' ', 4) + " " + Utils.padRight("IP Address", ' ', 15) + " " + Utils.padRight("Client", ' ', 23) + " " + Utils.padRight("Me?", ' ', 3) + " " + Utils.padRight("Running?", ' ', 8) + " " + Utils.padRight("Socket?", ' ', 7) + " " + Utils.padRight("In/Out", ' ', 6), client);
			send( Utils.padLeft("", '-', 72), client);
			
			String msg;
			
			for (final Client c : s.getClients()) {
				if (c != null) {
					if (c == client) {
						msg = String.format("%-4d %-15s %-23s %3s %-8s %-7s %-3s %-3s",
								cn, c.getIPAddress(), c.toString(), "Yes", c.isRunning(), c.isAlive() ? "closed" : "open", c.getSocket().isInputShutdown() ? "Y" : "N", c.getSocket().isOutputShutdown() ? "Y" : "N"
								);
					}
					else {
						msg = String.format("%-4d %-15s %-23s %3s %-8s %-3s %-3s %-3s",
								cn, c.getIPAddress(), c.toString(), "", c.isRunning(), c.isAlive() ? "closed" : "open", c.getSocket().isInputShutdown() ? "Y" : "N", c.getSocket().isOutputShutdown() ? "Y" : "N"
								);
					}
					
					send(msg, client);

					//if (c == client) send(cn + " " + c.getIPAddress() + " " + c.toString() + "[YOU]" + " " + c.isRunning(), client);
					//else             send(cn + " " + c.getIPAddress() + " " + c.toString() + " " + "" + c.isRunning(), client);
				}
				else {
					send(cn + " " + "---.---.---.--- null", client);
				}

				cn++;
			}
		}
		else if (param.equals("colors")) {
			if (color == Constants.ANSI) {
				char c = 'A';

				for (int i = 0; i < 9; i++) {
					client.write("\033[3" + i + ";m" + c);
				}

				client.writeln("");
			}
			else if (color == Constants.XTERM) {
				char c = 'A';

				for (int i = 0; i < 255; i++) {
					client.write("\033[38;5;" + i + "m" + c);
					if (i % 36 == 0) client.writeln("");
				}

				client.writeln("");
			}
		}
		else if (param.equals("commands")) {
			send("Commands", client);
			send("--------------------------------------------------------------------------", client);
			for(final Map.Entry<String, Command> e : commandMap.entrySet()) {
				send(String.format("%s : %s", e.getKey(), e.getValue().getDescription()), client);
			}
			send("--------------------------------------------------------------------------", client);
		}
		else if (param.equals("creatures")) {
			send("Creatures", client);
			send("--------------------------------------------------------------------------", client);
			for (final Creature c : objectDB.getCreatures()) {
				send(String.format("%s %s %s (#%s)", c.getDBRef(), c.getName(), getRoom(c.getLocation()).getName(), c.getLocation()), client);
			}
			send("--------------------------------------------------------------------------", client);
		}
		else if (param.equals("dbdump")) {
			/*
			 * List all of the names and dbrefs of the objects in the database
			 * their actual index in the database
			 */
			send(dumpDatabase(), client);
		} 
		else if (param.equals("holidays")) {
			/* list the holidays */
			for (Map.Entry<String, Date> entry : holidays.entrySet()) {
				debug(entry.getKey() + ": " + MONTH_NAMES[((Date) entry.getValue()).getMonth() - 1] + " " + ((Date) entry.getValue()).getDay());
				send(entry.getKey() + ": " + MONTH_NAMES[((Date) entry.getValue()).getMonth() - 1] + " " + ((Date) entry.getValue()).getDay(), client);
			}
		}
		else if (args2[0].toLowerCase().equals("instanceof")) {
			// @debug instanceof Laser_Rifle Weapon
			if (args2.length >= 3) {
				final String objectName = args2[1].replace('_', ' ');
				final String className = args2[2];
				
				MUDObject obj = objectDB.getById( Utils.toInt(objectName, -1) );

				if (obj != null && className != null) {
					Class cl = null;

					try {
						cl = Class.forName(className);
						
						debug( cl.getName() );
					}
					catch (final ClassNotFoundException cnfe) {
						send("false", client);
						System.out.println("--- Stack Trace ---");
						cnfe.printStackTrace();
					}
					
					if( cl != null ) {
						send("" + cl.isInstance(obj), client);
					}
				}
			}
		}
		else if (args2[0].toLowerCase().equals("listen")) {
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
		else if (param.equals("match")) {
			send(String.format("'%s' matches \"%s\": %s", "@access", "[a-zA-Z_-]+", "@access".matches("[a-zA-Z_-]+")), client);
			send(String.format("'%s' matches \"%s\": %s", "@dbref",  "[a-zA-Z_-]+", "@dbref".matches("[a-zA-Z_-]+")), client);
			
		}
		else if (param.equals("mem")) {
			send(Utils.checkMem(), client);
		}
		else if (param.equals("pos")) {
			Player player = getPlayer(client);
			Point position = player.getPosition();

			send("X: " + position.getX(), client);
			send("Y: " + position.getY(), client);
			send("Z: " + position.getZ(), client);

			send("Flying: " + player.isFlying(), client);
			send("Moving: " + player.isMoving(), client);
		}
		else if (param.equals("portals")) {
			send("--- " + Utils.padRight("Portals", '-', 68), client);

			for (final Portal portal : portals) {
				String state = "inactive";
				
				if (portal.isActive()) {
					state = "active";
				}
				
				send("P " + portal.getName() + " (#" + portal.getDBRef() + ") [ " + state + " ]", client);
			}

			// send("" + Utils.padRight("", '-', 78), client);
		}
		else if (param.equals("seasons")) {
			/*
			 * list all the seasons
			 */
			for (final Season s : Seasons.getSeasons()) {
				send(s + ": " + MONTH_NAMES[s.beginMonth - 1] + " to " + MONTH_NAMES[s.endMonth - 1], client);
			}
		}
		else if (param.equals("telnet")) {
			Byte[] msg = client.getTelnetMessage();

			while( msg.length > 0 ) {
				byte[] ba = new byte[msg.length];

				int index = 0;

				for (Byte b : msg) {
					ba[index] = b;
					index++;
				}

				String message = Telnet.translate(ba);

				System.out.println("] " + message);

				msg = client.getTelnetMessage();
			}
		}
		else if (param.equals("timedata")) {
			// get current data
			Calendar rightNow = Calendar.getInstance();

			// Real World Time (or at least whatever time zone the server is in)
			String real_time = "" + rightNow.get(Calendar.HOUR);

			if (rightNow.get(Calendar.HOUR) < 10) {
				real_time = " " + real_time;
			}

			if (rightNow.get(Calendar.MINUTE) < 10) {
				real_time = real_time + ":0" + rightNow.get(Calendar.MINUTE);
			}
			else {
				real_time = real_time + ":" + rightNow.get(Calendar.MINUTE);
			}

			send("Real Time: " + real_time, client);

			// In-game Time
			String gameTime = "" + game_time.getHours();

			if (game_time.getHours() < 10) {
				gameTime = " " + gameTime;
			}

			if (game_time.getMinutes() < 10) {
				gameTime = gameTime + ":0" + game_time.getMinutes();
			}
			else {
				gameTime = gameTime + ":" + game_time.getMinutes();
			}

			send("Game Time: " + gameTime, client);

			// Time Scale (the relative number of seconds to an-game minute)
			// 500 ms -> 1 second = 30000ms -> 1 minute (30s -> 1m)
			// 166 ms -> 1 second = 9960ms -> 1 minute ( 9.960s -> 1m)
			send("Time Scale: 1 minute game time/" + ((((double) game_time.getScale()) * 60) / 1000) + " seconds real time", client);
		}
		else if (param.equals("timers")) {
			send("Timers", client);

			send(Utils.padRight("", '-', 40), client);
			
			
			// TODO check on this, why do I do this here? to make sure their up to date? TimeLoop does this already
			checkTimers();

			// Effect Timers
			for (EffectTimer etimer : getEffectTimers(getPlayer(client))) {
				send("E " + etimer.getEffect().getName() + " ( " + etimer.getTimeRemaining() + " s )", client);
			}

			// Spell Timers
			for (SpellTimer stimer : getSpellTimers(getPlayer(client))) {
				send("S " + stimer.getSpell().getName() + " ( " + stimer.getTimeRemaining() + " s )", client);
			}

			// Auction Timers
			for (final AuctionTimer atimer : getAuctionTimers(getPlayer(client))) {
				final Auction auc = atimer.getAuction();
				//send("A " + auc.getItem().getName() + " ( " + auc.getTimeLeft() + " s )", client);
				send("A " + auc.getItem().getName() + " ( " + atimer.getTimeRemaining() + " s )", client);
			}

			send(Utils.padRight("", '-', 40), client);

			send("A - Auction, E - Effect, S - Spell", client);
		}
		else if (param.equals("udbnstack")) {
			send("Functionality Removed", client);
			client.write("Stack: [ ");
			
			List<Integer> unusedDBNs = objectDB.getUnused();

			for (int i = 0; i < unusedDBNs.size(); i++) {

				if (i < unusedDBNs.size() - 1) {
					client.write("" + unusedDBNs.get(i));
				}
				else {
					client.write(unusedDBNs.get(i) + ", ");
				}
			}

			/*
			 * for (final Integer i : unusedDBNs) { client.write(i + ", "); }
			 */

			client.write(" ]\n");
		}
		else if( param.equals("weather") ) {
			final Player player = getPlayer(client);
			final Room room = getRoom( player.getLocation() );
			
			if( room != null ) {
				Weather weather = room.getWeather();
				
				if( weather != null ) {
					Season s = weather.getSeason();
					WeatherState ws = weather.getState();
					
					List<String> msg = Utils.mkList(
							"-- Season",
							String.format("%s: %s to %s", s, MONTH_NAMES[s.beginMonth - 1], MONTH_NAMES[s.endMonth - 1]),
							"",
							"-- State",
							ws.getName(),
							ws.getDescription(),
							"",
							String.format("Trans. Up:   %s (%f)", ws.transUpText, 1.0 - ws.TransitionDownProbability),
							String.format("Trans. Down: %s (%f)", ws.transDownText, ws.TransitionDownProbability),
							"",
							String.format("WINDS: %-3s CLOUDS: %-3s STORM: %-3s", ws.Wind ? "Yes" : "No", ws.Clouds ? "Yes" : "No", ws.Storm ? "Yes" : "No")
							);
					
					send(msg, client);
					
				}
			}
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
			// cmd_help("@debug", client);
		}
	}

	/**
	 * Command: @dig
	 * 
	 * Create a room manually
	 * 
	 * NOTE: need to handle the roomName and roomParent string getting inside to
	 * make the command parameter input more uniform with the other commands [is
	 * this done?]
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_dig(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		if (arg.indexOf("=") != -1) {
			final String[] args = arg.split("=");

			final String name = Utils.trim(args[0]);
			final int parent = Utils.toInt(args[1], 0);

			System.out.println("Room Name:   " + name);
			System.out.println("Room Parent: " + parent);

			final Room room1 = createRoom(name, parent);

			// add rooms to database (main)
			objectDB.addAsNew(room1);
			objectDB.addRoom(room1);
			
			final String roomName = room1.getName();
			final Integer roomDBRef = room1.getDBRef();
			final Integer rLocation = room1.getLocation();

			// tell us about it (getLocation() vs getParent()?)
			send("Room '" + roomName + "' created as #" + roomDBRef + ". Parent set to " + rLocation + ".", client);
		}
		else {
			final List<String> directions = Utils.mkList("north", "northeast",
					"northwest", "south", "southeast", "southwest", "east",
					"west", "up", "down");

			if (directions.contains(arg.toLowerCase())) {
				final String rName = "new room";
				final int rParent = room.getDBRef();

				System.out.println("Room Name:   " + rName);
				System.out.println("Room Parent: " + rParent);

				final Room room1 = createRoom(rName, rParent);

				// add rooms to database (main)
				objectDB.addAsNew(room);
				objectDB.addRoom(room);

				final String roomName = room1.getName();
				final Integer roomDBRef = room1.getDBRef();
				final Integer rLocation = room1.getLocation();

				// tell us about it (getLocation() vs getParent()?)
				send("Room '" + roomName + "' created as #" + roomDBRef + ". Parent set to " + rLocation + ".", client);

				// create the directional exit
				final Exit exit = new Exit(arg.toLowerCase(), room.getDBRef(), room1.getDBRef());

				objectDB.addAsNew(exit);
				objectDB.addExit(exit);

				// add the exit to the source room
				room.addExit(exit);

				// tell us that we succeeded in creating the exit
				final String exitName = exit.getName();
				final Integer exitDBRef = exit.getDBRef();
				final Integer eLocation = exit.getLocation();
				final Integer destination = exit.getDestination();

				send("You open an exit called " + exitName + "(#" + exitDBRef + ")" + " from #" + eLocation + " to #" + destination + ".", client);

				// move player to room and show room?
			}
			else {
				send(gameError("@dig", ErrorCodes.INVALID_SYNTAX), client); // Invalid Syntax Error
			}
		}
	}
	
	private void cmd_deposit(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		/*String str = null;

		try {
			str = room.getProperty("_game/isBank");
		}
		catch(final ClassCastException cce) {
			System.out.println("--- Stack Trace ---");
			cce.printStackTrace();
			str = "false";
		}*/

		String str = room.getProperty("_game/isBank");

		final Boolean isBank = str.equals("true") ? true : false;

		if( isBank ) {
			final Bank bank = getBank( room.getProperty("_game/bank/name") );
			final BankAccount acct = bank.getAccount(0);

			if ( bank != null ) {
				if( acct != null ) {
					int amount = Utils.toInt(arg, 0);

					if( amount > 0 ) {
						if( player.getMoney().numOfGold() >= amount ) {
							player.setMoney( player.getMoney().subtractGold(amount) );
							acct.deposit( Coins.gold(amount) );

							send("You deposit " + amount + " gold.", client);
						}
						else send("You don't have that much.", client);
					}
				}
				else {
					send("You don't have an account.", client);
				}
			}
			else {
				send("No such bank?!", client);
			}
		}
		else send("You aren't in a bank.", client);
	}

	/**
	 * Command: describe
	 * 
	 * Change the description of objects: rooms, exits, etc
	 * 
	 * NOTE: only handles player and room descriptions, not those of other
	 * objects, yet
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_describe(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );
		
		// @desc <object name/object dbref/here/me>=<description>
		final String[] args = arg.split("=");

		final String ref = args[0];
		final String description = args[1];

		MUDObject m = null;

		// get object
		// if no argument or empty argument, fail with an error
		if (arg.equals("") || arg.equals(null)) {
			send(gameError("@describe", ErrorCodes.INVALID_SYNTAX), client); // Invalid Syntax Error
		}
		else {
			int dbref = Utils.toInt(ref, -1);
			
			if (ref.equalsIgnoreCase("here") || room.getDBRef() == dbref) {
				m = room;
			}
			else if (ref.equalsIgnoreCase("me") || player.getDBRef() == dbref) {
				m = player;
			}
			else {
				// try to get the specified object
				m = getObject(dbref);
			}
		}

		// attempt to change description
		if (m != null) {
			// check to see if the object is okay to edit (no one else is editing it)
			if (m.Edit_Ok) {
				m.setDesc(description);
				
				send("Description Changed.", client);
				
				if( m != player ) {
					send("You changed the description of " + m.getName() + " to:", client);
					
					send(parseDesc(m.getDesc(), 80), client);
				}
			}
			else send("object not editable (!Edit_Ok)", client);
		}
		else send("No such object!", client);
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

		Room room = getRoom( getPlayer(client).getLocation() );

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
				send("@door : " + gameError("@open", ErrorCodes.INVALID_SYNTAX), client);
				return;
			}
		}
		catch (final NumberFormatException nfe) {
			// TODO is this debug message adequate?
			send("open : source or destination dbref invalid, exit creation failed", client);
			return;
		}

		// create the exit
		Exit exit = new Exit(name, source, destination);

		objectDB.addAsNew(exit);
		objectDB.addExit(exit);

		// get the source room
		room = getRoom(source);

		// add the exit to the source room
		room.addExit(exit);

		// tell us that we succeeded in creating the exit
		send("You open an exit called " + exit.getName() + "(#" + exit.getDBRef() + ")" + " from #" + exit.getLocation() + " to #" + exit.getDestination() + ".", client);
	}
	
	private void cmd_dungeon(final String arg, final Client client) {
		int dX = 2;
		int dY = 2;

		Dungeon d = new Dungeon(arg, dX, dY);

		cmd_jump(d.dRooms[0][0].getDBRef() + "", client);

		cmd_look("", client);

		int dXc = 0;
		int dYc = 0;

		System.out.println(d.dRooms.length);

		for (int r = 0; r < dX * dY; r++) {
			if (dXc == 0) {
				System.out.println("Xexits" + ", " + r + ", " + "(" + dXc + ", " + dYc + ")");

				//cmd_open("east=" + d.roomIds[r] + "=" + (d.roomIds[r] + 1), client);
				cmd_open( String.format("east=%d=%d", d.roomIds[r], d.roomIds[r] + 1), client );
				
				//cmd_open("west=" + (d.roomIds[r] + 1) + "=" + d.roomIds[r], client);
				cmd_open( String.format("west=%d=%d", d.roomIds[r] + 1, d.roomIds[r]), client );
				
			}

			if (dYc == 0) {
				System.out.println("Yexits" + ", " + r + ", " + "(" + dXc + ", " + dYc + ")");
				cmd_open("south=" + d.roomIds[r] + "=" + (d.roomIds[r] + dX), client);
				cmd_open("north=" + (d.roomIds[r] + dX) + "=" + d.roomIds[r], client);
			}

			if (r == dX - 1) {
				System.out.println("Increase in Y"); dYc = dYc + 1; dXc = 0; }
			else {
				System.out.println("Increase in X");
				dXc = dXc + 1;
			}
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
				if (item.isDrinkable()) { // drinkable check
					itemList.add(item);
				}
			}

			debug( itemList.toString() );

			Item item = null;

			/*
			 * you should type 'drink healing' instead of 'drink potion' if you
			 * want a healing potion, otherwise you might get a potion of
			 * invisibility or bull's strength
			 */

			if (player.getMode() == PlayerMode.COMBAT) { // if in combat
				// try healing, etc potions first if just 'drink' is typed
				ArrayList<Item> healing = new ArrayList<Item>();

				for (final Item item1 : itemList) {
					/*
					 * need to check to see if something contains a healing
					 * effect
					 * 
					 * does it need to have solely a heal effect?
					 */
					if (item1.hasEffectType(Effect.Type.HEAL)) {
						healing.add(item1);
					}
				}
			}
			else { // else search by name for the item
				for (final Item item1 : itemList) {
					if (item1.getName().equals(arg) || item1.getName().contains(arg)) {
						if (item1 instanceof Stackable) {
							item = ((Stackable<Item>) item1).split(1);
						}

						item = item1;

						break;
					}
				}
			}

			if (item != null) {
				// determine what kind of drinkable item it is and apply an
				// effects
				// or status changes accordingly
				if (item instanceof Potion) {
					Potion potion = (Potion) item;

					debug("Potion?: " + potion.toString());

					List<Effect> effects = potion.getEffects();

					debug( effects.toString() );

					/* just one effect? */
					applyEffect(player, potion.getEffect());

					/*
					 * if the drinkable item is stackable too, then I need to be
					 * sure to use only one
					 */
				}
				else {
					send("You take a sip of your " + colors(item.getName(), getDisplayColor("item")) + ".", client);
				}

				// Script s = item.getScript(TriggerType.onUse);

				// if( s != null ) {
				final String result = pgm.interpret(item.getScript(TriggerType.onUse), player, item);

				if (!result.equals("")) send(result, client);
				// }

				player.getInventory().remove(item); // remove from inventory
				objectDB.remove(item);              // remove from database
			}
			else {
				send("You don't have one of those to drink.", client);
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
	private void cmd_drop(final String arg, final Client client) {
		// get player, room objects to work with
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		// split the arguments into a string array by space characters
		final String[] args = arg.split(" ");

		// tell us how many elements the array has (debug)
		debug("" + args.length);

		// get the object the argument refers to: by name (if it's in the
		// calling player's inventory), or by dbref#
		// should be done by searching the player's inventory for the object and
		// if there is such an object, drop it on the floor.

		if (arg.equals("")) {
			send("Syntax: drop <item>", client);
		}
		else if (arg.equalsIgnoreCase("all")) {
			room.addItems(player.getInventory());

			String itemName;

			for (final Item item1 : player.getInventory()) {
				itemName = item1.getName();

				debug(itemName + " true");

				// drop(player, room, item1);

				item1.setLocation(room.getDBRef());

				// check for silent flag to see if object's dbref name should be
				// shown as well?
				if (!player.hasFlag(ObjectFlag.SILENT)) {
					send("You dropped " + colors(itemName, "yellow") + " (#" + item1.getDBRef() + ") on the floor.", client);
				}
				else {
					send("You dropped " + colors(itemName, "yellow") + " on the floor.", client);
				}

				addMessage(new Message(player.getName() + " dropped " + colors(itemName, "yellow") + " on the floor.", room));
			}

			player.getInventory().clear();
		}
		else {
			Item item = null;

			// get the integer value, if there is one, as the argument
			final int dbref = Utils.toInt(arg, -1);

			// look for specified item in the player's inventory
			if (dbref != -1)  item = MudUtils.findItem(dbref, player.getInventory());
			if (item == null) item = MudUtils.findItem(arg, player.getInventory());

			if (item != null) {
				final String itemName = item.getName();

				drop(player, room, item);

				// check for silent flag to see if object's dbref name should be shown as well?
				if (!player.hasFlag(ObjectFlag.SILENT)) {
					send("You dropped " + colors(itemName, "yellow") + " (#" + item.getDBRef() + ") on the floor.", client);
				}
				else {
					send("You dropped " + colors(itemName, "yellow") + " on the floor.", client);
				}

				// return message telling others that the player dropped the item?
				// obviously we want the players in the current room that can see something
				addMessage(new Message(player.getName() + " dropped " + colors(itemName, "yellow") + " on the floor.", room));
			}
			else send("You don't have that.", client);
		}
	}

	/**
	 * Command: edit
	 * 
	 * Edit an object. Determines what kind of object and "opens" the
	 * appropriate editor
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_edit(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if (arg.equalsIgnoreCase("new")) {
			send("EDIT> This command only works with existing objects.", client);
			return;
		}

		final Integer dbref = Utils.toInt(arg, -1);

		MUDObject m = null;

		if (dbref != -1) {
			m = getObject(dbref);
		}
		else {
			m = getObject(arg);
		}

		if (m != null) {
			final TypeFlag type = m.getType();

			switch (type) {
			case CREATURE:
				editCreature(player, (Creature) m);
				break;
			case EXIT:
				break;
			case ITEM:
				break;
			case NPC:
				break;
			case ROOM:
				editRoom(player, (Room) m);
				break;
			case THING:
				break;
			default:
				break;
			}
		}
		else {
			send("No Such MUDObject", client);
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
	private void cmd_effects(final String arg, final Client client) {
		client.writeln(MudUtils.listEffects(getPlayer(client)));
	}

	/**
	 * Command: enter
	 * 
	 * Enter into some kind of special object or any Thing with ENTER_OK flag
	 * set
	 * 
	 * NOTE: this is intended to supply similar behavior to some MUSHes and is
	 * not a complex, integrated system.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_enter(final String arg, final Client client) {
		// TODO determine how to resolve this and the implied 'leave' command
		// against similarly named exits
		final Player player = getPlayer(client);
		final MUDObject mobj = getObject(arg);

		if (player != null && mobj != null) {
			if (mobj.hasFlag(ObjectFlag.ENTER_OK)) {
				// TODO figure out what other state changes, etc should be
				// happening here
				player.setLocation(mobj.getDBRef());
			}
		}
	}

	/**
	 * Command: equip
	 * 
	 * Equip an item to a player, likely in the first slot that is both
	 * available and compatible unless one was specified.
	 * 
	 * NOTE: should I have a hold and wield command instead or should they tie
	 * into this? 'equip bastard sword' or 'equip rhand bastard sword' or 'wield
	 * bastard sword'
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_equip(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		if ( arg.equals("") ) {
			String temp;
			String display;

			for (final String key : player.getSlots().keySet()) {
				final Slot slot = player.getSlot(key);
				
				temp = key.toUpperCase();

				final Item item = slot.getItem();
				
				if (item != null) {
					//display = String.format("%s : %s *%f lbs", colors(temp, getDisplayColor("item")), item, item.getWeight());
					display = String.format("%s : %s", colors(String.format("%14s", temp), getDisplayColor("item")), item, item.getWeight());
					
				}
				else {
					//display = String.format("%s : %s", colors(temp, getDisplayColor("item")), "null");
					display = String.format("%s : %s", colors(String.format("%14s", temp), getDisplayColor("item")), "null");
				}
				
				send(display, client);
			}
		}
		else {
			// get the integer value, if there is one, as the argument
			final int dbref = Utils.toInt(arg, -1);
			
			Item item = null;

			// look for specified item in the player's inventory
			if (dbref != -1)  item = MudUtils.findItem(dbref, player.getInventory());
			if (item == null) item = MudUtils.findItem(arg, player.getInventory());

			if (item != null) {
				if ( item.isEquippable() ) {
					// equips the item in the first available slot
					// TODO possible revise getSlots(...) method and reconsider how we decide what slots we have
					final Slot slot = player.getSlot(item);

					if (slot == null) {
						send("No Valid Slot!", client);
						return;
					}
					
					final ItemType type = item.getItemType();
					
					// TODO these should be under debug, since we don't need to see this normally
					send("Item Type", client);
					send("\tItem: " + item.getItemType(), client);
					send("\tSlot: " + slot.getItemType(), client);
					
					send("Slot Type", client);
					send("\tItem: " + item.getSlotType(), client);
					send("\tSlot: " + slot.getSlotType(), client);
					
					if ( slot.isType(type) && slot.getSlotType() == item.getSlotType() ) {
						if ( slot.isEmpty() ) {
							/*
							 * handle any OnEquip effects/events
							 */

							slot.insert(item);                  // put item in the slot
							player.getInventory().remove(item); // remove it from the inventory

							if (item instanceof ExtraCommands) {
								debug("Item has extra commands.");

								for (Map.Entry<String, Command> cmdE : ((ExtraCommands) item).getCommands().entrySet()) {
									final String cmdName = cmdE.getKey();
									final Command command = cmdE.getValue();
									
									command.init(this);

									player.addCommand(cmdName, command);

									debug("Added " + cmdName + " to player's command map from " + item.getName());
								}
							}
							
							send(colors(item.getName(), "yellow") + " equipped (" + slot.getItem().getItemType() + ")", client);

							item = null; // set item reference to null
						}
						else {
							// are these alternative messages?
							send("You can't equip that. (Equip Slot Full)", client);
							send("Where are you going to put that? It's not like you have a spare...", client);
						}
					}
					else send("You can't equip that. (Item Type or Slot Type is Incorrect)", client);
				}
				else send("You can't equip that. (Not Equippable)", client);
			}
			else send("Equip what?", client);
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
	private void cmd_examine(final String arg, final Client client) {
		if ( arg.equals("") || arg.equals("here") ) {
			Room room = getRoom( getPlayer( client ).getLocation() );
			examine(room, client);
		}
		else if (arg.equals("me")) {
			Player player = getPlayer(client);
			examine(player, client);
		}
		else {
			if( arg.charAt(0) == '#' ) {
				final int dbref = Utils.toInt(arg.substring(1), -1);
				
				if (dbref != -1) {
					MUDObject mobj = getObject(dbref);

					if (mobj != null) {
						switch(mobj.getType()) {
						case EXIT:   examine((Exit) mobj, client);   break;
						case PLAYER: examine((Player) mobj, client); break;
						case ROOM:   examine((Room) mobj, client);   break;
						default:     examine(mobj, client);          break;
						}
					}
					else send("That doesn't exist.", client);
				}
				else send("That doesn't exist.", client);
			}
			else {
				// TODO fix finding stuff... needs exact case name as is
				
				// get by string/name
				MUDObject mobj = getObject(arg);
				
				//MUDObject mobj = null;// findObject()
				
				if( mobj != null ) {
					/** TODO: fix the following kludge **/
					/*if( mobj.getLocation() != getPlayer(client).getLocation() ) {
						send("That doesn't exist.", client);
						return;
					}*/
					
					switch(mobj.getType()) {
					case EXIT:   examine((Exit) mobj, client);   break;
					case PLAYER: examine((Player) mobj, client); break;
					case ROOM:   examine((Room) mobj, client);   break;
					default:     examine(mobj, client);          break;
					}
				}
				else {
					send("That doesn't exist.", client);
				}
			}
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
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );
		
		final String exitNames = room.getVisibleExitNames();

		if (exitNames != null && !exitNames.equals("")) {
			send(colors("Exits: " + exitNames, getDisplayColor("exit")), client);
		}
		else {
			send(colors("Exits:", getDisplayColor("exit")), client);
		}
	}
	
	private void cmd_exp(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		final Integer level = player.getLevel();
		final Integer XP = player.getXP();
		final Integer xpToLevel = player.getXPToLevel();
		
		send(level + " [ " + XP + " / " + xpToLevel + " ] " + (level + 1), client);
	}

	private void cmd_feats(final String arg, final Client client) {
		Player player = getPlayer(client);

		final List<Feat> feats = new ArrayList<Feat>();

		feats.add(Feat.ap_light);
		feats.add(Feat.ap_medium);
		feats.add(Feat.ap_heavy);

		send("Feats", client);

		final List<String> featNames = new LinkedList<String>();

		for (final Feat feat : feats) {
			featNames.add( feat.getName() );
		}

		send(featNames, client);

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
		final Room room = getRoom( player.getLocation() );

		final ObjectFlag flag;

		if (args.length > 1) {
			MUDObject m = null;
			
			if ( args[0].equals("me") )        m = player;
			else if ( args[0].equals("here") ) m = room;
			else {
				int dbref = Utils.toInt(args[0], -1);

				if (dbref != -1) m = getObject(dbref);
				else             m = getObject(args[0]);
			}
			
			// TODO fix explosion if char doesn't indicate a flag ObjectFlag.fromLetter(...) throws an exception
			
			// only the first flag indicated matters if the ! symbol is present
			if (args[1].contains("!")) {
				flag = ObjectFlag.fromLetter(args[1].charAt(1));

				if (m != null) {
					m.removeFlag(flag);
					send("Removed " + flag.toString() + " flag from " + m.getName(), client);
				}
				else send("No such object.", client);
			}
			else {
				send("Adding Flag(s)", client);
				
				for(final Character c : args[1].toCharArray()) {
					try {
						ObjectFlag of = ObjectFlag.fromLetter(c);

						if (m != null) {
							if( !MudUtils.isAllowed(of, m.getType()) ) {
								send("You may not set " + of.name() + " on a " + m.getType().getName(), client);
								continue;
							}

							m.setFlag(of);

							send(m.getName() + " flagged as " + of, client);
						}
						else send("No such object.", client);
					}
					catch(final IllegalArgumentException iae) {
						send("Invalid Flag: " + c, client);
					}
				}
				
				/*if (m != null) {
					m.setFlags( ObjectFlag.getFlagsFromString(args[1] + player.getFlagsAsString()) );

					send(m.getName() + " flagged " + ObjectFlag.fromLetter(args[1].charAt(0)), client);
				}
				else send("No such object.", client);*/
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

		if (arg.charAt(0) == '#') {
			m = getObject(Utils.toInt(arg.substring(1), -1));
		}
		else if (Character.isDigit(arg.charAt(0))) {
			m = getObject(Utils.toInt(arg, -1));
		}
		else {
			m = getObject(arg);
		}

		if (m != null) {
			debug( ObjectFlag.toInitString( m.getFlags() ) );
			debug("" + m.getDBRef());
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
		// syntax: @find <search string>
		//         @find [<type>] <search string>
		final LinkedList<String> matches = new LinkedList<String>();

		String args[] = arg.split(" ");

		if( args.length > 1 ) {
			if (args[0].charAt(0) == '-') {
				String param = args[0].substring(1);
				String searchString = args[1];

				/*
				 * if( param.equals("p") ) { System.out.println("Find Players");
				 * send("Find Players - using \'" + searchString + "\'");
				 * 
				 * // needs to work even with offline players if( searchString
				 * == null ) { matches.addAll(objectDB.players.keySet()); } else
				 * { for(final MUDObject m : objectDB.players.values()) { if(
				 * m.getName().contains(searchString) ) {
				 * matches.add(m.getName() + " (#" + m.getDBRef() + ")"); } } }
				 * }
				 */
				if ( param.equals("i") ) {

				}
				else if (param.equals("n")) {
					System.out.println("Find NPCs");
					send("Find NPCs - using \'" + searchString + "\'");

					if (searchString == null) {
						for (final NPC npc : getNPCList()) {
							matches.add(npc.getName() + " (#" + npc.getDBRef() + ")");
						}
					}
					else {
						for (final NPC npc : getNPCList()) {
							if (npc.getName().toLowerCase().startsWith(searchString)) {
								matches.add(npc.getName() + " (#" + npc.getDBRef() + ")");
							}
						}
					}
				}
				else if (param.equals("nx")) {
				}
				else if ( param.equals("p") ) {
					if (searchString == null) {
						for (final Player pc : objectDB.getPlayers()) {
							matches.add(pc.getName() + " (#" + pc.getDBRef() + ")");
						}
					}
					else {
						for (final Player pc : objectDB.getPlayers()) {
							if (pc.getName().toLowerCase().startsWith(searchString)) {
								matches.add(pc.getName() + " (#" + pc.getDBRef() + ")");
							}
						}
					}

				}
				else if ( param.equals("px") ) {
					// player, exclusive -- find only the first match?
					System.out.println("Find Player");

					Player player = objectDB.getPlayer(searchString);

					matches.add(player.getName() + " (#" + player.getDBRef() + ")");
				}
				else if ( param.equals("r") ) {
					System.out.println("Find Rooms");
					send("Find Rooms - using \'" + searchString + "\'");

					String rName;

					if( searchString.equals("*") ) {
						for(final Room room : objectDB.getRooms()) {
							matches.add( "" + room );
						}
					}
					else {
						for(final Room room : objectDB.getRooms()) {
							rName = room.getName();

							boolean equals = rName.equals(searchString);
							boolean contains = rName.contains(searchString);
							boolean startsWith = rName.startsWith(searchString);
							boolean endsWith = rName.endsWith(searchString);

							if( Utils.or(equals, contains, startsWith, endsWith) ) {
								matches.add( "" + room );
							}
						}
					}

				}
				else if ( param.equals("rx") ) {
					// room, exclusive -- find only the first match?
				}
			}
			else {
				// TODO use gameError stuff here?
				send("Invalid Parameter.", client);
			}
		}
		else {
			String searchString = arg;

			if (searchString.equals("") || searchString == null) {
				System.out.println("Find Any");

				// TODO implement this, it should just return everything
				for (final MUDObject m : objectDB.getObjects()) {
					matches.add(m.getName() + " (#" + m.getDBRef() + ")");
				}
			}
			else {
				System.out.println("Find Named");
				send("Find Any - using \'" + searchString + "\'", client);

				for (final MUDObject m : objectDB.findByLower(arg)) {
					matches.add(m.getName() + " (#" + m.getDBRef() + ")");
				}
			}
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

		if (race.canFly()) {
			send("With a strong flap of your wings, you push yourself upward into the air.");

			player.setFlying(true);
			player.changePosition(0, 0, 1);
		}
		else send("You don't have wings, you silly person.", client);
	}

	private void cmd_give(final String arg, final Client client) {
		/*
		 * @give <player> item <id>
		 * @give <player> money <value>
		 * 
		 * e.g.
		 * give Iridan money 5
		 * give Iridan item sword
		 */
		
		if ( !arg.equals("") ) {
			final String[] args = arg.split(" ");
			
			if (args.length == 2) {
				debug("args (1): " + args[0]);
				debug("args (2): " + args[1]);

				final Player player = getPlayer(args[0]);

				if (player != null) {
					final String[] temp = args[1].split(":");

					if (temp.length == 2) {
						debug("temp (1): " + temp[0]);
						debug("temp (2): " + temp[1]);

						if (temp[0].equalsIgnoreCase("money")) {
							int c = 0;

							try {
								c = Integer.parseInt(temp[1]);
							}
							catch (final NumberFormatException nfe) {
								debug("cmd_give(): money value (number format exception)");
								System.out.println("--- Stack Trace ---");
								nfe.printStackTrace();
								c = 0;
							}

							debug("" + c);
							
							player.setMoney( player.getMoney().add( Coins.copper(c) ) );

							send("You give " + player.getName() + " " + Coins.copper(c).toString(true));
						}
					}
				}
				else send("No such player.", client);
			}
			else {
				send("@give: invalid arguments", client);
			}
		}
	}

	/**
	 * Command: go
	 * 
	 * Move towards an object or a specific point in the cartesian plane
	 * 
	 * go <north/northeast/northwest/south/southeast/southwest/east/west>
	 * go <object in room>
	 * go <x> <y>
	 * go <x> <y> [z]
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_go(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		String[] args = arg.split(" ");

		if (args.length == 1) {
			final List<String> cardinal_directions = Utils.mkList("north",
					"northeast", "northwest", "south", "southeast",
					"southwest", "east", "west");

			if ( cardinal_directions.contains(args[0].toLowerCase()) ) {
				debug("Go: Valid Direction!");

				// check for alternate room systems...
				final Direction d = Direction.getDirection(args[0]);
				final Exit exit = room.getExit(d);

				if (exit != null) {
					debug("Go: Valid Exit!");

					exitHandler(exit, client);
				}
				else {
					send("You can't go that way.", client);
				}
			}
			else {
				MUDObject m = getObject(arg, room);

				if (m != null) {
					move(player, m.getPosition());
				}
			}
		}
		else if (args.length == 2) {
			try {
				int x = Integer.parseInt(args[0]);
				int y = Integer.parseInt(args[1]);

				if (x < room.getDimension("x") && y < room.getDimension("y")) {
					move(player, new Point(x, y));
				}
			}
			catch (final NumberFormatException nfe) {
				// TODO is this debug message adequate?
				send("Invalid Point!", client);
				debug( nfe );
			}
		}
		else if (args.length == 3) {
			try {
				int x = Integer.parseInt(args[0]);
				int y = Integer.parseInt(args[1]);
				int z = Integer.parseInt(args[2]);
				
				// Utils.range(x, 0, room.getDimension("x")); // ?

				if (x < room.getDimension("x") && y < room.getDimension("y") && z < room.getDimension("z")) {
					move(player, new Point(x, y, z));
				}
			}
			catch (final NumberFormatException nfe) {
				// TODO is this debug message adequate?
				send("Invalid Point!", client);
				System.out.println("--- Stack Trace ---");
				nfe.printStackTrace();
			}
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
		Room room = getRoom( player.getLocation() );

		// process syntax/arguments
		// get (from) <container> <item>

		// if there is no argument
		if ( arg.equals("") ) {
			send("Syntax: get [from] <container> <item>", client);
		}
		else {
			// split the arguments into a string array by space characters
			final String[] args = arg.split(" ");

			// tell us how many elements the array has (debug)
			debug("" + args.length);

			if (args.length == 2) {
				// allow for multi word items to be referred to with _ instead of the spaces that exist
				final String itemName = args[1].replace('_', ' ');

				System.out.println("Arg: " + args[0]);

				// find the container
				// TODO need a item finding function that can evaluate specified criteria
				// TODO I think maybe I should glom the items and things found into a containers list and iterate over that, stopping when I successfully find something
				
				boolean success = false;
				
				Storage<Item> s = null;
				
				final List<MUDObject> objects = new LinkedList<MUDObject>();
				
				objects.addAll( findItems(player.getInventory(), args[0]) );
				objects.addAll( findItems(room.getItems(),       args[0]) );
				objects.addAll( room.getThings() ); // TODO should be getting only things with the right name
				
				for (final MUDObject object : objects) {
					if( object.isType(TypeFlag.ITEM) ) {
						final Item item = (Item) object;
						
						if ( item.getItemType() == ItemTypes.CONTAINER) {
							s = (Container) item;

							success = get(player, s, itemName);
						}	
					}
					else if( object.isType(TypeFlag.THING) ) {
						final Thing thing = (Thing) object;
						
						if( thing.thing_type == ThingTypes.CONTAINER ) {
							s = (Box) thing;

							success = get(player, s, itemName);
						}
					}
					
					if( success ) break;
				}
				
				if( !success ) send("No such item", client);
				
				/*System.out.println("ITEMS");

				final List<Item> items = new LinkedList<Item>();

				items.addAll( findItems(player.getInventory(), args[0]) );
				items.addAll( findItems(room.getItems(),       args[0]) );

				for (final Item item : items) {
					if (item.getItemType() == ItemTypes.CONTAINER) {
						s = (Container) item;

						success = get(player, s, itemName);

						break;
					}
				}

				if( !success ) {
					System.out.println("THINGS");

					final List<Thing> things = new LinkedList<Thing>();

					things.addAll( room.getThings() );

					for (final Thing thing : things) {	
						if( thing.thing_type == ThingTypes.CONTAINER ) {
							s = (Box) thing;

							success = get(player, s, itemName);
							break;
						}
					}
				}

				if( !success ) send("No such item", client);*/
			}

			/*
			 * 
			 * // get a list of the objects that the player can see
			 * List<MUDObject> foundObjects = findVisibleObjects(room);
			 * 
			 * // look in the room for(final MUDObject m : foundObjects) { if( m
			 * instanceof Thing && m instanceof Storage ) { if(
			 * m.getName().equalsIgnoreCase(args[0]) ) { Storage s = (Storage)
			 * m;
			 * 
			 * Item item = s.retrieve(itemName);
			 * 
			 * if( item != null ) { player.getInventory().add( item );
			 * item.setLocation(player.getDBRef());
			 * System.out.println(player.getInventory());
			 * 
			 * send("You get " + colors(item.getName(), getDisplayColor("item"))
			 * + " from " + ((MUDObject) s).getName(), client);
			 * 
			 * return; } } } }
			 */
		}
	}

	/**
	 * Command: help
	 * 
	 * Provide access to help files about the use of other commands
	 * 
	 * TODO arg is final so we can change it, but that's a bad way of doing this
	 * 
	 * @param arg    the name of the help file to access
	 * @param client the client
	 */
	private void cmd_help(String arg, final Client client) {
		/*
		 * really should add a topics system and multi-page help files (need a
		 * "pager"); it'd be awesome to have some kind of virtual page up/page
		 * down functionality - maybe that would be workable if I had a real
		 * terminal emulator on the other end. if i code this feature, I could
		 * enable it if I could identify a full terminal emulation on the other
		 * end (telnet negotiation? or maybe just asking via the game for a
		 * response)
		 */

		final Player player = getPlayer(client);

		if ( arg.equals("@reload") ) {
			if (checkAccess(player, Constants.BUILD)) {
				help_reload();
				notify(player, "Game> Help Files Reloaded!\n");
			}
		}
		else {
			if ( arg.equals("") ) arg = "help";

			// config options retrieval
			final boolean pagerEnabled = player.getConfigOption("pager_enabled");
			final int lineWidth = player.getLineLimit();

			boolean isHelpFile = false;

			String[] file = null;

			file = getHelpFile(arg);

			if (file == null) file = getTopicFile(arg);
			else              isHelpFile = true;

			if( file != null ) {
				final String[] temp = Arrays.copyOfRange(file, 1, file.length);

				if( file.length > 25 && pagerEnabled) {
					player.setPager( new Pager(temp) );
					player.setStatus("VIEW");

					op_pager("view", client);
				}
				else {
					final List<String> output = new LinkedList<String>();

					output.add( colors(file[1], "green") + "\r\n" );

					output.addAll( Arrays.asList(temp).subList(2, temp.length) );

					/*for (final String line : Arrays.copyOfRange(file, 2, file.length)) {
					// client.write(check(line) + "\r\n");
					// client.write(line + "\r\n");

					output.add( line );

					// TODO figure out why showDesc ignores the line width if
					// used here, perhaps check(...) is causing the problem?
					// showDesc(check(line), lineWidth, client);
				}*/

					// tell us when the file was last modified/updated?

					send(output, client);
				}

				if( isHelpFile ) {
					final StringBuilder sb = new StringBuilder();

					for(final String key : aliases.keySet()) {
						if( aliases.get(key).equals(arg) ) sb.append(key + " ");
					}

					send("ALIASES: " + sb.toString().trim());
				}
			}
			else {
				Command c = null;

				if ( commandMap.containsKey(arg) ) c = commandMap.get(arg);
				else if ( player.hasCommand(arg) ) c = player.getCommand(arg);

				if( c != null ) send(arg + " - " + c.getDescription(), client);
				else            client.write("No such help file!\r\n");
			}
		}
	}

	private void cmd_hold(final String arg, final Client client) {
		final Player player = getPlayer(client);

		final int DBREF = Utils.toInt(arg, -1);

		Item item = null;

		// determine whether you have the item/thing? in question
		if (DBREF != -1)  item = MudUtils.findItem(DBREF, player.getInventory());
		else              item = MudUtils.findItem(arg, player.getInventory());

		if (item != null) {
			// can you hold it?
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
			// header: @command
			// shows the naming, so it's easy to index
			// @COMMAND
			// printed out as the name of the command when viewing help

			list.addLine(arg);               // add name of command, lowercase (header)
			list.addLine(arg.toUpperCase()); // add name of command, uppercase (header)

			send("Game> (help editor) Helpfile created.", client);
		}
		else { // if it does, load it
			System.out.println(HELP_DIR + arg + ".txt");

			// probably ought to somehow prevent editing of the header
			// load the help file with an offset so I can avoid borking
			// the two header data lines ?
			final List<String> lines = Utils.loadList( resolvePath(HELP_DIR, arg + ".help") );
			
			player.setEditList( new EditList(arg, lines) );
			
			list = player.getEditList();

			// if loading fails, create it
			if (list.getNumLines() == 0) {
				send("Game> (help editor) Error: Invalid Help File!", client);
				send("Game> (help editor) Creating new help file...", client);

				player.startEditing(arg);

				// need to generate header of help file without including it ineditable space
				// header: @command
				// shows the naming, so it's easy to index
				// @COMMAND
				// printed out as the name of the command when viewing help

				final EditList newlist = player.getEditList();

				newlist.addLine(arg);               // add name of command, lowercase (header)
				newlist.addLine(arg.toUpperCase()); // add name of command, uppercase (header)

				send("Game> (help editor) Helpfile created.", client);
			}
		}

		send("Help Editor v0.0b\n", client);
		send("Type '.help' or '.h' for help.\n");

		String header = "< List: " + list.getName() + " Line: " + list.getLineNum() + " Lines: " + list.getNumLines() + "  >";

		send(header, client);
	}

	/**
	 * Command: housing
	 * 
	 * Shows housing information and availability for the current
	 * area/region/whatnot
	 * 
	 * NOTE: dummy test information at the moment
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_housing(final String arg, final Client client) {
		debug("Test Data Only");

		send(colors("Player Housing - Details", "cyan"), client);
		send(colors("=========================================================", "cyan"), client);

		if (mud_name.equals("WaterdeepMUD")) {
			send("Waterdeep - Castle Ward (South)", client);
			send("   o " + colors("Winding Way Apartments [ 0 / 10 ]", "green"), client);
			send("   o " + colors("Sea Villas [ 10 / 10 ]", "red"), client);
			send("   o " + colors("Cymbril's Walk [ 7 / 10 ]", "yellow"), client);
		}
		else if (mud_name.equals("FalloutEquestria")) {
			send("Tenpony Tower", client);
			send("   o " + colors(Utils.padRight("Apartment", ' ', 16) + " " + "[ 250 / 1000 ]", "green"), client);
			send("   o " + colors(Utils.padRight("Luxury Apartment", ' ', 16) + " " + "[ 5 / 50]", "yellow"), client);
		}

		send(colors("=========================================================", "cyan"), client);
	}
	
	private void cmd_home(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		try {
			boolean globalNameRefs = false;

			if (player.getConfig() != null) {
				if ( player.getConfigOption("global-nameref-table") ) {
					globalNameRefs = true;
				}
			}

			int destination = -1;

			if ( globalNameRefs ) {
				destination = this.getNameRef("home");
			}
			else {
				destination = player.getNameRef("home");
			}

			cmd_jump(String.valueOf(destination), client);
		}
		catch (final NumberFormatException nfe) {
			// TODO is this debug message adequate?
			send("Exception (CMD:HOME): Invalid Destination!", client);
			System.out.println("--- Stack Trace ---");
			nfe.printStackTrace();
		}
	}

	/**
	 * Command: inspect
	 * 
	 * Take a close look at an object. (shows the visual properties, stuff from
	 * _visual)
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_inspect(final String arg, final Client client) {
		final Player player = getPlayer(client);

		final StringBuilder sb = new StringBuilder();
		
		int loc = player.getLocation();

		final Item item = MudUtils.findItem(arg, objectDB.getItemsByLoc(loc));

		final Map<String, String> visual_props = item.getVisualProperties();

		send("Inspecting the " + item.getName() + " you see:", client);

		// TODO need to find a way to mesh multiple of these into a nice
		// description

		// engraved numbers or names, rust spots, locks
		for (final String s : visual_props.keySet()) {
			//int temp = s.lastIndexOf('/') + 1;
			
			if (s.substring(s.indexOf('/') + 1).startsWith("engraving")) {
				if (s.contains("number")) {
					send(colors("an engraved number", "purple"), client);
				}
				else if (s.contains("script")) {
					send(colors("an engraved script", "purple"), client);
				}
			}

			send("a " + colors(s, "purple"), client);
		}

		// send(sb.toString(), client);
		return;
	}

	private void cmd_initialize(final String arg, final Client client) {
		final Player player = getPlayer(arg);

		if (module != null) module.PCInit(player);
		else                send("No GameModule configured!", client);
	}

	private void cmd_interact(final String arg, final Client client) {
		Player player = getPlayer(client);
		
		// mark the player as in interaction mode
		player.setStatus(Constants.ST_INTERACT);

		NPC npc = getNPC(arg); // get the npc by name

		if (npc != null) {
			debug(npc.getName());

			getPlayer(client).setTarget(npc); // "target" the npc

			//npc.interact(player);

			// below is essentially the contents of the default npc interact
			if( player != null ) {
				if( npc.knowsName( player.getName() ) ) {
					addMessage( npc.tell(player, "Hello, " + player.getName() + ".") );
				}
				else {
					addMessage( npc.tell(player, npc.greeting) );
				}
			}
		}
		else send("NPC(" + arg + ") is NULL.", client);
	}

	/**
	 * Command: inventory
	 * 
	 * check player inventory
	 * 
	 * @param arg
	 *            unused
	 * @param client
	 *            the client
	 */
	private void cmd_inventory(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		if (player != null) // if the player exists
		{
			debug( player.getInventory().toString() );
			send(player.getName() + "'s Inventory:", client);

			// if (player.getInvType() == 'S') { // simple inventory display
			// else if (player.getInvType() == 'C') { // complex inventory
			// display

			// if( player.getConfigOption("complex-inventory") == false ) {
			// }

			boolean in_shop = room.hasFlag(ObjectFlag.SHOP);

			debug("IN SHOP: " + in_shop);

			final StringBuilder sb = new StringBuilder();

			for (final Item item : player.getInventory()) {
				sb.delete(0, sb.length());

				if (item != null) {
					// getName or toString ?
					// send(colors(item.getName(), "yellow") + " " + "(#" +
					// item.getDBRef() + ")", client);

					if (in_shop) {
						sb.append(colors(Utils.padRight(item.toString(), ' ', 24), "yellow"));
						sb.append(" ");
						sb.append(item.getValue().toString(true));

						send(sb.toString(), client);
					}
					else send(colors(item.toString(), "yellow"), client);

					if (item instanceof Container) {
						for (Item item1 : ((Container) item).getContents()) {
							sb.delete(0, sb.length());

							final String space = Utils.padRight("", ' ', 2);

							if (in_shop) {
								sb.append(space);
								sb.append(colors(item.toString(), "yellow"));
								sb.append(" ");
								sb.append(" ( in ").append(item.getName()).append(" )");
								sb.append(Utils.padRight("", ' ', 24 - sb.length() + space.length()));
								sb.append(space);
								sb.append(item.getValue().toString(true));

								send(sb.toString(), client);
							}
							else send(space + colors(item1.getName(), "yellow") + " ( in " + item.getName() + " )", client);
						}
					}
				}
				else {
					debug("Item is null");
				}
			}

			/*
			 * else { // WORK: need to redo this, and not use a for loop this
			 * way, the // whole inventory should be shown in the way the
			 * container is send("/" + Utils.padRight("", '-', 70) + "\\",
			 * client); //send("|" + Utils.padRight(colors("Pack", "yellow")) +
			 * "|", client);
			 * 
			 * String padded1 = Utils.padRight("Pack", 70); StringBuffer sb1 =
			 * new StringBuffer(padded1);
			 * 
			 * sb1.insert(0, colorCode("yellow")); //sb1.insert(sb1.indexOf("k")
			 * + 1, colorCode("white")); sb1.append(colorCode("white"));
			 * 
			 * send("|" + sb1.toString() + "|", client); send("|" +
			 * Utils.padRight("", '-', 70) + "|", client); for (final Item item
			 * : player.getInventory()) { if (item != null) { if (item
			 * instanceof Container) { if( ((Container)
			 * item).getContents().size() > 0 ) { displayContainer((Container)
			 * item, client); } else { //String itemString =
			 * colors(item.getName(), "yellow") + "(#" + item.getDBRef() + ")";
			 * //String itemString = item.getName() + "(#" + item.getDBRef() +
			 * ")";
			 * 
			 * //String itemString = item.getName(); String itemString =
			 * item.toString();
			 * 
			 * String padded = Utils.padRight(itemString, 70);
			 * 
			 * StringBuffer sb = new StringBuffer(padded);
			 * 
			 * sb.insert(0, colorCode("yellow")); //sb.insert(sb.indexOf("("),
			 * colorCode("white")); sb.append(colorCode("white"));
			 * 
			 * //send("|" + Utils.padRight(itemString) + "|", client); send("|"
			 * + sb.toString() + "|", client); } } else { //String itemString =
			 * colors(item.getName(), "yellow") + "(#" + item.getDBRef() + ")";
			 * //String itemString = item.getName() + "(#" + item.getDBRef() +
			 * ")";
			 * 
			 * //String itemString = item.getName(); String itemString =
			 * item.toString();
			 * 
			 * String padded = Utils.padRight(itemString, 70);
			 * 
			 * StringBuffer sb = new StringBuffer(padded);
			 * 
			 * sb.insert(0, colorCode("yellow")); //sb.insert(sb.indexOf("("),
			 * colorCode("white")); sb.append(colorCode("white"));
			 * 
			 * //send("|" + Utils.padRight(itemString) + "|", client); send("|"
			 * + sb.toString() + "|", client); } } } send("\\" +
			 * Utils.padRight("", '-', 70) + "/", client); }
			 */
			
			double weight = MudUtils.calculateWeight(player) + MudUtils.calculateWeight(player.getMoney());

			//send("Weight: " + weight + "/" + player.getCapacity() + " lbs.", client);
			send(String.format("Weight: %f / %d lbs.", weight, player.getCapacity()), client);
		}
	}

	/**
	 * Command to launch item editor (iedit)
	 * 
	 * NOTE: editor concept borrowed from ROM, a derivative of Merc, a
	 * derivative of DIKU.
	 * 
	 * Basically you call the item editor like this (at least from inside the
	 * code): 'cmd_itemedit(<item #/item name>, client)'
	 * 
	 * And it attempts to find the item and edit it, if it can't find it, it
	 * will indicate a failure and open the editor with no item.
	 */
	private void cmd_itemedit(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT);
		player.setEditor(Editors.ITEM);

		EditorData newEDD = new EditorData();

		// create new item if no item to edit specified
		if (arg.equals("")) {
			Item item = createItem();

			if (item.Edit_Ok) {
				item.Edit_Ok = false; // further edit access not permitted (only one person may access at a time)
			}
			else {
				abortEdit("item not editable (!Edit_Ok)", old_status, client);

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

				if (item.Edit_Ok) {
					item.Edit_Ok = false; // further edit access not permitted (only one person may access at a time
				}
				else {
					abortEdit("item not editable (!Edit_Ok)", old_status, client);

					return;
				}

				exist = true;
			}
			catch (final NumberFormatException nfe) {
				// no item with that dbref, cannot edit (abort)

				System.out.println("--- Stack Trace ---");
				nfe.printStackTrace();

				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Item Editor - Unexpected error caused abort (number format exception)", client);
			}
			catch (final NullPointerException npe) {
				// null item, cannot edit (abort)

				System.out.println("--- Stack Trace ---");
				npe.printStackTrace();

				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Item Editor - Unexpected error caused abort (null pointer exception)", client);
			}

			if (exist) {
				// item exists

				// record prior player status
				newEDD.addObject("pstatus", old_status);

				// add item and it's constituent parts to the editor data
				newEDD.addObject("item", item);
				newEDD.addObject("desc", item.getDesc());
				newEDD.addObject("name", item.getName());
				newEDD.addObject("type", item.getItemType());
			}
			else {
				// item doesn't exist (abort) reset player, and clear edit flag and editor setting

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
	private void cmd_jump(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		Room room = null; // the room to jump to

		if( arg.indexOf('*') != -1 ) {
			int zoneId = Utils.toInt(arg.substring(1), -1);
			
			Zone zone = null;
			
			if( zoneId != -1 ) {
				zone = getZone(zoneId);
			}
			else {
				zone = getZone(arg.substring(1));
			}
			
			if(zone != null) room = zone.getEntryRoom();
			else             send("Invalid Zone.", client);
		}
		else {
			final int dbref = Utils.toInt(arg, -1); 
			
			debug("DBREF: " + dbref);

			// try to find the room, by dbref or by name
			if( dbref != -1 ) {
				room = getRoom(dbref);
			}
			else {
				room = getRoom(arg);
			}
			
			//room = (dbref != -1) ? getRoom(dbref) : getRoom(arg);
		}

		// if we found the room, send the player there
		if (room != null) {
			getRoom( player.getLocation() ).removeListener(player); // remove listener

			send("Jumping to " + room.getName() + "... ", client);

			player.setLocation(room.getDBRef());
			player.setPosition(0, 0);

			send("Done.", client);
			
			look(room, client);

			getRoom( player.getLocation() ).addListener(player); // add listener
		}
		else send("Jump failed.", client);
	}
	
	private void cmd_jsonify(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );
		
		if( testing ) {
			GsonBuilder builder = new GsonBuilder(); // Or use new GsonBuilder().create();

			builder.registerTypeAdapter(Spell.class, new SpellAdapter());
			builder.registerTypeAdapter(Race.class, new RaceAdapter());
			builder.setPrettyPrinting();

			Gson gson = builder.create();

			String[] args = arg.split(" ");

			if( args.length == 2 ) {
				if( args[0].startsWith("#") ) {
					String param = args[0].substring(1);

					if( param.equals("s") ) {
						final Spell spell = getSpell(args[1]);
						
						send(gson.toJson(spell), client);
					}
				}
			}
			else {
				if( arg.equals("me") )        send(gson.toJson(player), client);
				else if( arg.equals("here") ) send(gson.toJson(room), client);
				else {
					final MUDObject obj = getObject(arg);
					if( obj != null ) {
						send(gson.toJson(obj), client);
					}
				}
			}
		}
		else send("Testing: DISABLED", client);
	}
	
	private void cmd_kick(final String arg, final Client client) {
		final Player player = getPlayer(arg);
		
		if( player != null ) {
			kick( player.getClient() );
		}
		else send("KICK: No such Player!", client);
	}
	
	private void cmd_land(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Race race = player.getRace();

		if (race.canFly()) {
			if (player.isFlying()) {
				// TODO fix kludge, we assume here that the player will land
				// straight down to the zero coordinate
				final Point pt = player.getPosition();

				pt.setZ(0);

				move(player, pt);
			}
		}
	}

	private void cmd_levelup(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if ( hasValidRace(player) && hasValidClass(player) ) {
			if ( player.isLevelUp() ) {
				if( player.getLevel() + 1 < max_levels ) {
					player.changeLevelBy(1);
					
					send("You leveled up to level " + player.getLevel() + "!", client);
					
					// TODO call game module function here
					if ( module != null ) module.levelup( player );
				}
				send("You are already at max level?!", client);
			}
			else {
				send("You are not currently ready to level-up.", client);
			}
		}
		else {
			send("Your character is invalid.", client);
		}
	}

	private void cmd_link(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		final String[] args = arg.trim().split("=");

		if( args.length == 2 ) {
			// TODO cmd_link -- to consider
			// is a string or int is better to search by?
			// must the exit be in the same room as we are?
			final Exit exit = getExit(args[0]);

			if( exit != null ) {
				final int newDestination = Utils.toInt(args[1], -1); // integers for now
				
				final Room newDest = getRoom(newDestination);
				
				if( exit.getDestination() == -1 ) {
					if( newDest != null ) {
						final String ndName = newDest.getName();
						final int ndDBRef = newDest.getDBRef();

						exit.setDestination(newDestination);
						
						send("Linked " + exit.getName() + " to " + ndName + " (#" + ndDBRef + ")", client);
					}
					else send("Error: Invalid Destination!", client);
				}
				else send("Error: That exit is already linked to somewhere.", client);
			}
		}
		else send("Error: incorrect number of arguments", client);
	}
	
	/**
	 * List what is available for sale from a vendor/merchant
	 * 
	 * currently no argument is accepted, but perhaps it could be utilized to
	 * take a keyword for a specific category of items the vendor/merchant
	 * sells. I.e. we know they sell weapons, but can directly request a list of
	 * 'swords' or 'axes'.
	 * 
	 * @param arg    not used
	 * @param client the client sending this command
	 */
	private void cmd_list(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if (player.getStatus().equals(Constants.ST_INTERACT)) {
			final MUDObject target = player.getTarget();
			
			// TODO a Vendor flag isn't sufficient here, because only Merchant implements Vendor
			if( target.isType(TypeFlag.NPC) && target.hasFlag(ObjectFlag.MERCHANT) && target.hasFlag(ObjectFlag.VENDOR) ) {
				final Vendor vendor = (Vendor) ( (Merchant) player.getTarget() );

				if (arg.equals("")) {
					// TODO maybe this should be in the vendor interface/npc class
					
					// build list of stuff for sale
					final Map<String, Tuple<Item, Integer>> sl = new LinkedHashMap<String, Tuple<Item, Integer>>();

					String stringRep;

					for (final Item item : vendor.getStock()) {
						/*
						if (item instanceof Weapon) {
							final Weapon w = (Weapon) item;
							// send(colors("+" + w.getMod() + " " + w.weapon.getName() + " (" + w.getWeight() + ") Cost: " + w.getCost(), "yellow"), client);

							// using this one
							//send(colors(w.toString() + " (" + w.getWeight() + ") Cost: " + w.getValue(), "yellow"), client);

							stringRep = w.toString() + " (" + w.getWeight() + ")";
						}
						else if (item instanceof Armor) {
							final Armor a = (Armor) item;
							// send(colors("+" + a.getMod() + " " + a.getName() + " (" + a.getWeight() + ") Cost: " + a.getCost(), "yellow"), client);

							// using this one
							//final String armorInfo = a.toString() + " (" + a.getWeight() + ") ";
							//send(colors(Utils.padRight(armorInfo, ' ', 30) + " " + a.getValue(), "yellow"), client);

							stringRep = a.toString() + " (" + a.getWeight() + ")";
						}
						else {
							// using this one
							//send(colors(item.getName() + " (" + item.getWeight() + ") Cost: " + item.getValue(), "yellow"), client);
							stringRep = item.getName() + " (" + item.getWeight() + ")";
						}
						*/
						
						//stringRep = Utils.padRight(item.getName(), ' ', 20) + Utils.padRight("" + item.getWeight(), '8') + Utils.padRight("lbs.", ' ', 4);

						//
						if( sl.containsKey( item.getName() ) ) sl.get( item.getName() ).two++;
						else                                   sl.put(item.getName(), new Tuple<Item, Integer>(item, 1));
					}

					final List<String> output = new LinkedList<String>();
					
					final StringBuilder sb = new StringBuilder();
					
					output.add("-----< Stock >----------------------");
					
					for(final Tuple<Item, Integer> t : sl.values()) {
						final Item item = t.one;
						final Integer quantity = t.two;
						final Coins price = t.one.getValue();
						
						sb.append( Utils.padRight(item.getName(), ' ', 20) );
						sb.append( Utils.padRight("" + item.getWeight(), 6) );
						sb.append(price);
						sb.append( " (" );
						sb.append( "x" );
						sb.append( Utils.padLeft("" + quantity, ' ', 3) );
						sb.append( ") " );
						
						output.add( sb.toString() );
						
						sb.delete(0, sb.length());
					}
					
					output.add("------------------------------------");
					
					send(output, client);
				}
			}
			else send("That's not a vendor.", client);
		}
		else {
			if (player.getConfigOption("silly_messages")) send("You ask the air around you what it has for sale, but receive no response...", client);
			else                                          send("You're not interacting with anyone.", client);
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
		debug("Command: lock");

		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		debug("Player: " + player.getName());
		debug("Room:   " + room.getName());
		
		Exit exit = null;	
		
		boolean found_exit = false;
		
		for(final Exit e : objectDB.getExitsByRoom(room)) {
			if( e.getExitType() == ExitType.DOOR ) {
				final Door d = (Door) e;
				
				System.out.println("lock: \'" + d.getName( room.getDBRef() ) + "\'");
				
				if( d.getName( room.getDBRef() ).equals(arg) ) {
					exit = d;
					found_exit = true;
				}
				
			}
			else if( e.getName().equals(arg) ) {
				exit = e;
				found_exit = true;
			}
			
			if( found_exit ) break;
		}
		
		if ( exit != null ) {
			final String exitName = exit.getName();
			
			if( exit instanceof Lockable ) {
				final Lockable<Item> l = (Lockable<Item>) exit;

				if ( !l.isLocked() ) {
					if( l.requiresKey() ) {
						boolean hasKey = hasKey(player, l);
						
						if( hasKey ) {
							l.lock();
							
							final String keyName = l.getKey().getName();
							
							send("You lock " + exitName + " with " + colors(keyName, getDisplayColor("item")) + ".", client);
						}
					}
					else {
						l.lock();
						send("You lock " + exitName + ".", client);
					}
				}
				else send("That's already locked.", client);
			}
			else send(exitName + " not lockable.", client);
			
			//send("It's a good thing no one saw you trying to lock a " + exit.getExitType().toString() + " with no lock.", client);
		}
		else send("No such object", client);
	}

	private void cmd_logout(final String arg, final Client client) {
		if (use_accounts) {
			// remove the player from the game
			init_disconn(client, true);

			final Account account = caTable.get(client);

			// if there is an account associated with the client
			if (account != null) {
				account_menu(account, client);
				setClientState(client, "account_menu");
			}
		}
		else {
			init_disconn(client, false);
		}
	}

	/**
	 * Command: look
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_look(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );
		
		debug("Look Command");

		final String[] args = arg.split(" ");
		
		for (final String s : args) {
			debug(s);
		}

		// if no argument or empty argument, show the room
		if (arg.equals("")) {
			player.setTarget(room);
			
			look(room, client);
			
			return;
		}
		else {
			debug("Argument (String): " + arg);

			if (arg.toLowerCase().equals("here")) {
				player.setTarget(room);
				look(room, client);
				return;
			}
			else if (arg.toLowerCase().equals("me")) {
				player.setTarget(player);
				look(player, client);
				return;
			}
			else {
				// decide what else is visible and then find the best match in there
				ArrayList<MUDObject> objectsFound = findVisibleObjects(room);
				objectsFound.addAll(player.getInventory());

				int spec = 0;

				// supposed to handle a syntactical structure like 'sword.2'
				if (arg.contains(".")) {
					spec = Integer.parseInt(arg.substring(arg.indexOf('.')));
					debug("Specifier: " + spec);
				}

				debug("Objects Found: " + objectsFound);

				// just the items in the current room (looks here first)
				for (MUDObject m : objectsFound) {
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
					 * 1) is the name the same as ARG (ignoring case -- setting
					 * both name and arg to lowercase) 2) does the name start
					 * with ARG (ignoring case -- setting both name and arg to
					 * lowercase) 3) does the name end with ARG (ignoring case
					 * -- setting both name and arg to lowercase) 4) does the
					 * name contain ARG (ignoring case -- setting both name and
					 * arg to lowercase) 5) is any component of the name the
					 * same as the arg (continues non-whitespace separated
					 * segments)
					 */

					boolean sameName = name.equalsIgnoreCase(arg);
					boolean startsWith = name_lc.startsWith(arg_lc);
					boolean endsWith = name_lc.endsWith(arg_lc);
					boolean nameContains = name_lc.contains(arg_lc);
					boolean compsContain = components.contains(arg_lc);

					boolean test = false;

					for (String s : components) {
						for (String s1 : Arrays.asList(arg.toLowerCase().split(" "))) {
							if (s.contains(s1)) test = true;
							break;
						}
					}

					// for string in A, is A.S a substring of string name N.S
					if (sameName || startsWith || endsWith || nameContains || compsContain || test) {
						player.setTarget(m);
						look(m, client);
						return;
					}
				}

				// not sure if look should apply to dbrefs/any old name,
				// permissions?
				final int dbref = Utils.toInt(arg, -1);
				MUDObject m = null;

				if (dbref != -1) {
					m = getObject(dbref);
				}
				else {
					if (spec == 0) {
						m = getObject(arg);
					}

					// else { MUDObject[] mObjs = getObjects(arg); }
				}

				if (m != null) {
					debug("MUDObject : " + m.getDBRef() + " " + m.getName());
					
					player.setTarget(m);

					if (m instanceof Player)    look((Player) m, client);
					else if (m instanceof Room) look((Room) m, client);
					else                        look(m, client);
				}
			}
		}
	}

	/**
	 * Command: lookat
	 * 
	 * kind of like inspect, but used for the room rather than an arbitray
	 * object
	 * 
	 * perhaps inspect/lookat should be behavior swapped? (3/7/2015)
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_lookat(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		if ( !arg.equals("") ) {
			final MUDObject obj = player.getTarget();
			
			String s;
			
			// get properties (I think we should have /visuals "folder" for visual properties
			// i.e. 'ceiling', 'floor', 'wall(s)'
			if( obj != null ) {
				s = obj.getProperty("/visuals/" + arg);
			}
			else {
				s = room.getProperty("/visuals/" + arg);
			}
			
			if( s.equals(Constants.NONE) ) send("You look at the " + arg + ": " + s, client);
			else                           send("You look around, but don't see that.", client);
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
	private void cmd_lsedit(final String arg, final Client client) {
		final Player player = getPlayer(client);

		player.setStatus(Constants.ST_EDIT); // flag us as being in EDIT mode
		player.setEditor(Editors.LIST);      // set the editor we want to use
		
		if (!player.hasEditor(arg)) player.startEditing(arg);
		else                        player.loadEditList(arg);

		final EditList list = player.getEditList();
		
		final String listName = list.getName();
		final Integer currLine = list.getLineNum();
		final Integer lines = list.getNumLines();
		
		send("List Editor v0.0b\n", client);
		send("< List: " + listName + " Line: " + currLine + " Lines: " + lines + " >", client);
	}

	/**
	 * Command: listprops
	 * 
	 * List properties stored on a MUDObject
	 * 
	 * @param arg
	 *            an identifier/key that refers to the object (ex. name, dbref)
	 * @param client
	 *            the client that sent the command
	 */
	private void cmd_listprops(final String arg, final Client client) {
		Player player = getPlayer(client);
		Room room = getRoom( player.getLocation() );

		debug("ARG: " + arg);

		final String[] args = arg.split("=");

		MUDObject object = null;

		// get the specified objects
		if (args[0].toLowerCase().equals("here")) {
			object = room;
		}
		else if (args[0].toLowerCase().equals("me")) {
			object = player;
		}
		else {
			int dbref = Utils.toInt(args[0], -1); // parse string to get dbref

			if (dbref != -1) object = getObject(dbref);   // get object by dbref
			else             object = getObject(args[0]); // get object by name
		}

		Map<String, String> props = null;

		if (object != null) {
			props = object.getProperties();
			
			send(colors(object.getName(), getDisplayColor(object.type)) + " (#" + object.getDBRef() + ")" + colorCode("white"), client);

			if (args.length == 2) {
				System.out.println("ARG0: \'" + args[0] + "\'");
				System.out.println("ARG1: \'" + args[1] + "\'");

				for (final String key : props.keySet()) {
					if (key.startsWith(args[1])) {
						send(key + " " + props.get(key), client);
					}
				}
			}
			else {
				for (final String key : props.keySet()) {
					send(key + " " + props.get(key), client);
				}
			}

			// System.out.println("KEY: \'" + key + "\'");
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
	
	private void cmd_mine(final String arg, final Client client) {
		final Player player = getPlayer( client );
		final Room room = getRoom( player.getLocation() );
		
		// mine <resource> ? mine <node> ? copper.1 ? copper.rich? rich copper vein
		
		// search for minable nodes
		List<Node> minable_nodes = getMinableNodes(room, Resource.ResType.ORE);
		
		//  list them (plus short id strings)
		if( arg.equals("") ) {
			List<String> out = Utils.mkList("-- Minable Nodes");
			
			for(final Node n : minable_nodes) out.add( n.getName() );
			
			send( out, client );
		}
		else {
			// try to mine the specified node of material
			Node mine = null; 
			
			for(final Node n : minable_nodes) {
				String nodeName = n.getName().toLowerCase();
				
				if( nodeName.startsWith(arg) || nodeName.equals(arg) ) {
					send("Found Node!", client);
					mine = n;
					break;
				}
			}
			
			// if we found something to mine, generate real items to give to the user
			if( mine != null ) {
				final Resource res = mine.getResource();
				
				String resourceName = res.getName();
				String resourceType = res.getType().name();
				
				Item ore = createItem(resourceName, "", -1);
				
				ore.setProperty("type",     resourceType.toLowerCase());
				ore.setProperty("material", resourceName.toLowerCase());
				ore.setProperty("purity",   "1.00");
				
				objectDB.addAsNew( ore );
				objectDB.addItem( ore );
				
				
				ore.setLocation( player.getLocation() );
				player.getInventory().add( ore );
			}
			
		}
	}
	
	public Item getResource() {
		Item ore = createItem(
				"Iron Ore",
				"A chunk of iron ore. Bands of reddish brown are intertwined with darker gray spots.",
				-1
				);
		
		ore.setProperty("type", "ore");
		ore.setProperty("material", "iron");
		ore.setProperty("purity", "0.90");
		
		return ore;
	}
	
	/**
	 * Command: map
	 * 
	 * Display/Render a map on the screen for the player
	 * 
	 * testing, ideally I would either have stored room maps, stored
	 * maps/players of rooms or just generate the map on the fly should I store
	 * maps inside the code? (don't like that idea much) or per area in wherever
	 * I keep the data files?
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_map(final String arg, final Client client) {
		debug(MAP_DIR + "map.txt");

		String mapFile = resolvePath(MAP_DIR, "map.txt");
		String[] test1 = new String[1];

		try {
			// TODO should this exception be handled in the utility function?
			test1 = Utils.loadStrings(mapFile);

			if ( !(test1 instanceof String[]) ) {
				throw new FileNotFoundException("Invalid File!");
			}
		}
		catch (final FileNotFoundException fnfe) {
			System.out.println("--- Stack Trace ---");
			fnfe.printStackTrace();
		}

		// TODO if the above doesn't load, I think the function should probably abort

		client.write("Legend: ");
		client.write(colors("B", "green") + " - Bank ");
		client.write(colors("H", "red") + " - House ");
		client.write(colors("I", "magenta") + " - Inn ");
		client.write(colors("S", "yellow") + " - Shop ");
		client.write('\n');

		for (final String str : test1) {
			for (int i = 0; i < str.length(); i++) {
				switch (str.charAt(i)) {
				case '#':
					//client.write("" + Colors.WHITE); // set foreground white
					// client.write("\33[37m");
					//client.write(' '); // draw symbol
					client.write( colors(" ", "white") );
					break;
				case '*':
					//client.write("\33[47m"); // set background white
					//client.write(' '); // draw symbol
					//client.write("\33[40m"); // reset background (black)
					client.write( colors("o", "white") );
					break;
				case 'B': // Bank
					//client.write("\33[32m"); // set foreground green
					//client.write('B'); // draw symbol
					client.write( colors("B", "green") );
					break;
				case 'H': // House
					//client.write("\33[31m"); // set foreground red
					//client.write('H'); // draw symbol
					client.write( colors("H", "red") );
					break;
				case 'I': // Inn
					//client.write("\33[35m"); // set foreground green
					//client.write('I'); // draw symbol
					client.write( colors(" ", "magenta") );
					break;
				case 'S': // Shop
					// set foreground yellow
					//client.write("" + Colors.YELLOW);
					// client.write("\33[33m");
					//client.write('S');
					client.write( colors("S", "yellow") );
					break;
				case '$': // Shop
					// set foreground yellow
					//client.write("" + Colors.YELLOW);
					// client.write("\33[33m");
					//client.write('$');
					client.write( colors("$", "yellow") );
					break;
				default:
					break;
				}

				// reset foreground (set to white)
				//client.write("" + Colors.WHITE);
				client.write( colors("", "white") );
				
			}

			// reset background (black)
			//client.write("\33[40m\n");
			client.write( colors("", "black") + "\n");
		}
	}
	
	private void cmd_money(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		send("You have " + player.getMoney().toString() + ".", client);
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
	private void cmd_move(final String arg, final Client client) {
		final Player player = getPlayer(client);

		final String direction = arg.toLowerCase();

		if (direction.equals("north")) {
			if (player.getLocation() - 10 >= 0) {
				System.out.println("success");
				player.setLocation(player.getLocation() - 10);
			}
		}
		else if (direction.equals("south")) {
			player.setLocation(player.getLocation() + 10);
			System.out.println("success");
		}
		else if (direction.equals("east")) {
			player.setLocation(player.getLocation() + 1);
			System.out.println("success");
		}
		else if (direction.equals("west")) {
			if (player.getLocation() - 1 >= 0) {
				player.setLocation(player.getLocation() - 1);
				System.out.println("success");
			}
		}
		else {
			send("Invalid Movement!", client);
			return;
		}

		look(getRoom( player.getLocation() ), client);

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
			final MUDObject m = getObject(args[0]);

			if (m != null) {
				final String oldName = m.getName();
				final String newName = args[1];

				// check to make sure it's okay to edit this (make sure no one is editing it)
				if (m.Edit_Ok) {
					// we're not allowed to change player names at the moment
					// NOTE: Player overrides setname, so this is just a
					// protection against changing names in the database
					boolean nameChanged = m.setName(newName);
					
					if( nameChanged ) {
						objectDB.updateName(m, oldName); // revise the lookup tables
						
						send("Game> Changed name of " + oldName + "(#" + m.getDBRef() + ") to " + m.getName(), client);
					}
					else {
						send("Game> Cannot change the name of (#" + m.getDBRef() + ")", client);
						
						if( m.isType(TypeFlag.PLAYER) ) {
							send("Game> Changing Player names is not allows", client);
						}
					}
				}
				else send("Game> Object - Error: object not editable (!Edit_Ok)", client);
			}
			else send("No such object!", client);
		}
	}

	/**
	 * Command: nameref
	 * 
	 * Store a personal string to be used to refer to a number, useful for
	 * people who have a hard time remembering database reference numbers, but
	 * not names.
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

		final String[] args = arg.split(" ");

		boolean globalNameRefs = false;

		if (player.getConfig() != null) {
			if (player.getConfigOption("global-nameref-table")) {
				globalNameRefs = true;
			}
		}
		
		final String arg_lc = arg.toLowerCase();

		if (!globalNameRefs) {
			if ( arg_lc.equals("#list") || arg_lc.equals("#l") ) {
				send("Name Reference Table", client);
				send("------------------------------------------------", client);

				for (String str : player.getNameReferences()) {
					send(str + " -> " + player.getNameRef(str), client);
				}

				send("------------------------------------------------", client);
			}
			else if ( arg_lc.equals("#clear") ) {
				player.clearNameRefs();
				
				send("Name Reference Table cleared!", client);
			}
			else if (args.length == 2) {
				if ( args[0].equals("#delete") || args[0].equals("#d") ) {
					player.getNameReferences().remove(args[1]);
					
					send("nameRef deleted.", client);
				}
				else {
					try {
						player.setNameRef(args[0], Integer.parseInt(args[1]));
						
						send("nameRef allocated.", client);
						send(args[0].substring(0, args[0].length()) + " allocated to " + args[1], client);
					}
					catch (final NumberFormatException nfe) {
						System.out.println("--- Stack Trace ---");
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
					catch (final NumberFormatException nfe) {
						System.out.println("--- Stack Trace ---");
						nfe.printStackTrace();
					}
				}
			}
		}
	}

	private void cmd_npcs(final String arg, final Client client) {
		final List<String> output = new LinkedList<String>();
		
		// TODO should I use room names for location information?
		output.add("Race         Name             Loc   Ctrld Controller");
		output.add("------------ ---------------- ----- ----- ----------------");

		for (final NPC npc : getNPCList()) {
			final String race = Utils.padRight(npc.getRace().getName(), ' ', 12);
			final String name = Utils.padRight(npc.getName(), ' ', 16);
			final String location = Utils.padLeft("" + npc.getLocation(), ' ', 5);

			String controlled = "";
			String controller = "";

			final Player cPlayer = playerControlMap.getController(npc);

			if (cPlayer != null) {
				controlled = "Yes";
				controller = Utils.padRight(cPlayer.getName(), ' ', 16);
			}
			else controlled = "No";
			
			output.add(race + " " + name + " " + location + " " + Utils.padRight(controlled, ' ', 5) + " " + controller);
		}
		
		send(output, client);

		// Pegasus Ditzy Doo 34 Yes Stormy
	}

	/**
	 * Command to launch object editor (oedit) Permission: Builder
	 * 
	 * NOTE: concept borrowed from ROM, a derivative of Merc, a derivative of DIKU.
	 * 
	 * Basically you call the object editor like this: 'cmd_objectedit(<object #/object name>, client)'
	 * 
	 * And it attempts to find the object and edit it, if it can't find it, it
	 * will indicate a failure and open the editor with no object.
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
	private void cmd_open(final String arg, final Client client) {
		final String[] args = arg.split("=");

		String name = args[0];
		int source = 0, destination = 0;

		Room room;

		try {
			// get the source room
			room = getRoom( getPlayer(client).getLocation() );

			source = room.getDBRef();

			if (args.length == 2) { // simple form - name=destination
				// destination defaults to an invalid room dbref
				if (args[1].equals("")) {
					destination = -1;
				}
				else {
					destination = Integer.parseInt(args[1]);
				}
			}
			else {
				send("open : " + gameError("@open", ErrorCodes.INVALID_SYNTAX), client);
				return;
			}
		}
		catch (final NumberFormatException nfe) {
			// TODO is this debug message adequate?
			send("@open : destination dbref invalid (number format)\nExit creation failed", client);
			// System.out.println("--- Stack Trace ---");
			// nfe.printStackTrace();
			return;
		}
		catch (final NullPointerException npe) {
			// TODO is this debug message adequate?
			send("@open : source Room invalid (null pointer)\nExit creation failed", client);
			System.out.println("--- Stack Trace ---");
			npe.printStackTrace();
			return;
		}

		if (!validExitName(name)) {
			send("@open : Invalid exit name (contained disallowed characters)\nExit creation failed", client);
			return;
		}

		// create the exit
		Exit exit = new Exit(name, source, destination);

		objectDB.addAsNew(exit);
		objectDB.addExit(exit);

		// add the exit to the source room
		room.addExit(exit);

		// tell us that we succeeded in creating the exit
		send("You open an exit called " + exit.getName() + "(#" + exit.getDBRef() + ")" + " from #" + exit.getLocation() + " to #" + exit.getDestination() + ".", client);
	}
	
	private void cmd_open2(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );
		
		final List<MUDObject> objects = objectDB.getByRoom(room);

		MUDObject object = null;

		for (final MUDObject obj : objects) {
			if ( obj.getName().equals(arg) ) {
				object = obj;
			}
		}

		if (object != null) {
			if ( object.isType(TypeFlag.EXIT) ) {
				Exit exit = (Exit) object;

				if (exit.getExitType() == ExitType.DOOR) {
					Door door = (Door) exit;

					if (door.isLocked()) {
						if (door.unlock()) { // key check
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
			else if (object instanceof Lockable<?>) {
				Lockable<Item> l;

				if (object instanceof Item) {
					l = (Lockable<Item>) object;
				}
				else if (object instanceof Thing) {
					l = (Lockable<Item>) object;
				}
			}
		}
	}

	/**
	 * Command: osuccess
	 * 
	 * Sets a message that tells other players about the successful action
	 * another player did
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
	 * Sets a message that tells other players about a player's failure to
	 * complete an action
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
	private void cmd_page(final String arg, final Client client) {
		// ARG: <recipients>=<message>/nathan,admin=test message
		String[] in = arg.split("=");

		if (in.length > 1) {
			final String[] recipients = in[0].split(",");
			
			String msg = "";

			if (in.length == 2) {
				msg = in[1];

				send("You page, " + "\"" + Utils.trim(msg) + "\" to " + in[0] + ".", client);

				for (final String recipName : recipients) {
					final Player targetPlayer = getPlayer(recipName);
					final Client recipClient = targetPlayer.getClient();

					if (recipClient != null) {
						// TODO decide if I need to do things this way
						// mesage with a player sender, text to send, and the player to send it to
						
						Message message = new Message(getPlayer(client), targetPlayer, msg); 
						
						addMessage(message);
						//sendMessage(message, recipClient);
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

		final Player player = getPlayer(client);
		final Party party = getPartyContainingPlayer(player);

		if (arg.equals("")) {
			if (party != null) {
				// show the players in the party and their current vital status
				final List<String> output = new LinkedList<String>();
				
				output.add("- Party -------- ");
				
				for (final Player player1 : party.getPlayers()) {
					String extra = "";
					String color = getDisplayColor("player");

					if ( party.isLeader(player1) ) extra = "L";

					// refresh player state info
					player1.updateCurrentState();

					// indicate whether we're alive or not
					switch (player.getState()) {
					case ALIVE:         color = "green";  break;
					case INCAPACITATED: color = "yellow"; break;
					case DEAD:          color = "red";    break;
					default:            color = "white";  break;
					}
					
					output.add(colors(player1.getName(), color) + " " + colors(extra, "green"));
				}
				
				send(output, client);
			}
			else {
				send("You are not in a party.", client);
			}
		}
		else if (arg.charAt(0) == '#') {
			String arg1;
			String[] params = new String[0];

			if (arg.indexOf(' ') == -1) {
				arg1 = arg.substring(1).toLowerCase();
			}
			else {
				arg1 = arg.substring(1, arg.indexOf(' ')).toLowerCase();
				params = arg.substring(arg.indexOf(' ')).split(" ");
			}

			debug(arg1);
			
			if ( arg1.equals("create") || arg1.equals("c") ) {
				// create a new party with you as the leader (if you're not in a party)
				if (party != null) send("You are already in a party.", client);
				else               this.parties.add( createParty(player) );
			}
			else if ( arg1.equals("delete") || arg1.equals("d") ) {
				// deletes the party if you have one and are the leader
				if (party != null && party.isLeader(player)) {
					for(final Player player1 : party.getPlayers()) {
						party.removePlayer(player1);
					}
					
					this.chan.destroyChannel( party.getChannel().getName() );
					
					party.setChannel(null);
					
					this.parties.remove(party);
				}
				else send("Only the party leader may delete the party.", client);
			}
			else if ( arg1.equals("invite") || arg1.equals("i")) {
				// send an invite to join a party to one or more players (if you are the leader)
				if ( party != null ) {
					if ( party.isLeader(player) ) {
						if( params.length == 0 ) {
							send("No players to invite specified.", client);
							return;
						}
						
						for (final String playerName : params) {
							final Player player1 = getPlayer(playerName);

							// TODO establish difference between players that don't exist and ones that aren't logged in
							// ^ we shouldn't be able to invite players who aren't logged in/don't exist

							if (player1 != null) {
								// is that player logged in
								if ( sclients.containsValue(player1) ) {
									// send invite (overwrites previous invites)
									this.partyInvites.put(player1, getPartyContainingPlayer(player));

									notify(player1, player.getName() + " has invited you to their party!");
								}
								else send("That player is not logged-in.", client);
							}
							else send("No such player.", client);
						}
					}
					else send("Only the party leader may invite players!", client);
				}
			}
			else if ( arg1.equals("join") || arg1.equals("j") ) {
				// accept a standing invite to join a party
				if( party != null ) {
					send("You're already in a party. Leave that one first.", client);
				}
				else {
					final Party party1 = partyInvites.get(player);
					
					if (party1 != null) {
						party1.addPlayer(player);
						
						this.partyInvites.remove(player);

						send("You joined " + party1.getLeader().getName() + "'s party.", client);

						notify(party1.getLeader(), player.getName() + " accepted your party invitation!");
						notify(party1.getLeader(), player.getName() + " joined your party.");
					}
				}
			}
			else if ( arg1.equals("kick") || arg1.equals("k") ) {
				// kick named player from the party
				if ( party != null ) {
					if ( party.isLeader(player) ) {
						if( params.length == 0 ) {
							send("No players to kick specified.", client);
							return;
						}

						for (final String playerName : params) {
							final Player player1 = getPlayer(playerName);
							
							if (player1 != null) {
								party.removePlayer(player1);

								send("You kick " + player1.getName() + " from the party.", client);
								notify(player1, player.getName() + " kicked you from the party.");
							}
						}
					}
					else {
						send("Only the party leader may kick party members!", client);
					}
				}
				else send("You are not in a party.", client);
			}
			else if ( arg1.equals("leave") || arg1.equals("l") ) {
				// leave your current party, if you're in one
				if (party != null) {
					final Player leader = party.getLeader();

					party.removePlayer(player);

					send("You left " + leader.getName() + "'s party.", client);
					
					notify(leader, player.getName() + " left your party.");
				}
				else send("You are not in a party.", client);
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
	private void cmd_passwd(final String arg, final Client client) {
		// Syntax: passwd test / change your password to test (user)
		final Player player = getPlayer(client); // get the current player

		if ( !arg.equals("") ) {
			player.setPass(arg);

			send("Your password has been changed to: '" + arg + "'", client);
			send("CAUTION: Exercise care in remembering this password, as the admins cannot do anything for you if you forget it.", client);
			
			debug( String.format("EVENT> %s changed their password.", player.getName() ) );
		}
		else {
			send("Syntax: passwd <new password>", client);
		}
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
		// pconf #opt
		// pconf <option>=<true/false>
		final Player player = getPlayer(client);

		if ( !arg.equals("") ) {
			if (arg.charAt(0) == '#') {
				final String[] args = arg.split(" ");
				
				final String param = args[0].substring(1);

				if ( param.equals("options") || param.equals("opt") ) {
					// TODO why treat the current player's options as the whole set?
					final List<String> output = new LinkedList<String>();
					
					output.add("Configuration Options (player specific)");
					output.add( Utils.padLeft("", '-', 40) );
					
					for (final String key : player.getConfig().keySet()) {
						//send(key, client);
						output.add(key);
					}
					
					send(output, client);
				}
				else if ( param.equals("help") ) {
					if (args.length == 2) {
						switch (args[1]) {
						case "global-nameref-table":
							send("Use the global nameref table for storing your name references.", client);
							break;
						case "pinfo-brief":
							send("Show the short form of the output of the pinfo command by default.", client);
							break;
						case "prompt_enabled":
							break;
						case "msp_enabled":
							send("Indicate if you'd like the server to send you MSP messages.", client);
							break;
						case "complex-inventory":
							break;
						case "pager_enabled":
							break;
						case "show-weather":
							break;
						case "tagged-chat":
							send("Prefix all chat with 'CHAT' for easier client detection", client);
							break;
						case "compact-editor":
							break;
						case "hud_enabled":
							break;
						case "notify_newmail":
							send("Should the game notify you when you receive in-game OOC mail?", client);
							break;
						default:
							break;
						}
					}
					else {
						send("option not specified", client);
					}
				}
			}
			else {
				final String[] args = arg.split("=");
				
				final String option = args[0].trim();
				final String value = args[1].trim();
				
				if (args.length > 1) {
					if ( !player.hasConfigOption( option ) ) {
						send("No such config option! (see 'pconf #opt')", client);
						return;
					}

					switch (value) {
					case "true":
						player.setConfigOption(option, true);
						send("set " + option + " to TRUE.", client);
						break;
					case "false":
						player.setConfigOption(option, false);
						send("set " + option + " to FALSE.", client);
						break;
					default:
						send("Invalid config setting! (use 'true' or 'false')", client);
						break;
					}
				}
				else {
					send(option + " = " + getPlayer(client).getConfigOption(option), client);
				}
			}
		}
		else {
			// NOTE: output config data with color tagging
			for (final Entry<String, Boolean> e : player.getConfig().entrySet()) {
				String key = e.getKey();
				Boolean value = e.getValue();
				
				String c = (value) ? "green" : "red"; // color
				
				//String.format("%s: %s", Utils.padLeft(key, ' ', 20), colors("" + value, c));
				
				send(Utils.padLeft(key, ' ', 20) + ": " + colors("" + value, c), client);
			}
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

		send("-------------------------[ Character Sheet ]-------------------------", client);
		send("Name: " + Utils.padRight(player.getName(), 16), client);
		send("Race:  " + player.getRace().getName(), client);
		send("Class: " + player.getPClass().getName(), client);
		send("Level: " + player.getLevel(), client);

		send("", client);

		if (player.isLevelUp()) {
			// client.write(Colors.GREEN + "Ready to Level-Up!" + Colors.WHITE +
			// '\n');
			send(colors("Ready to Level-Up!", "green"), client);
			send("", client);
		}

		send("XP: " + Utils.padRight("" + player.getXP(), 7) + " XP to next Level: " + Utils.padRight("" + (player.getXPToLevel() - player.getXP()), 7), client);

		send("", client);

		// TODO testing
		final Ability[] abilities = rules.getAbilities();
		
		String abilityString = "";

		for (final Ability ability : abilities) {
			int ab = player.getAbility(ability);      // get base ability stat
			int abm = player.getAbilityMod(ability); // get ability stat modifier

			// ab would represent the current ability score, this code is meant
			// to visually render whether the overall modifier adds or subtracts
			// from inherent
			abilityString = (ab > ab - abm) ? colors("" + ab, "green") : (ab < ab - abm) ? colors("" + ab, "red") : "" + ab;

			send(Utils.padRight(ability.getName() + ": ", ' ', 14) + abilityString, client);
		}

		send("", client);

		send("AC: " + player.getAC(), client);
		
		/*
		// Brew Potion (0) Craft Magic Arms And Armor (1)           Craft Rod (2)
		// Craft Staff (3)                 Craft Wand (4) Craft Wondrous Item (5)
		//  Forge Ring (6)               Scribe Scroll(7)
		String[] item_creation_feats = { "Brew Potion", "Craft Magic Arms and Armor", "Craft Rod", "Craft Staff", "Craft Wand", "Craft Wondrous Item", "Forge Ring", "Scribe Scroll" };

		send("Item Creation Feats", client);

		for (int i = 0; i < 8; i++) {
			send(item_creation_feats[i] + ": " + (player.item_creation_feats.get(i) ? "Yes" : "No"), client);
		}
		*/

		if (player.getConfigOption("pinfo-brief") == false || arg.equals("#skills")) {
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
			
			int count = 0;
			int size = player.getSkills().size();

			send("------------------------------[ Skills ]------------------------------", client);
			for (final Skill s : player.getSkills().keySet()) {
				String skill = s.toString();
				Integer value = player.getSkills().get(s);
				Integer mod = player.getSkillMod(s);
				
				debug( skill + ": " + value + " (" + mod + ")");

				String color = "";
				String output = "";

				if (value == -1)     color = "red";
				else if (value == 0) color = "yellow";
				else if (value > 0)  color = "green";

				output = colors(Utils.padRight(skill, ' ', 18), color) + " : " + Utils.padLeft("" + value, ' ', 2) + " (" + mod + ")";
				
				row.append( Utils.padRight(output, 46) );
				
				if( si == columns || count == size - 1 ) {
					debug("SI: " + si);

					client.write(row.toString() + '\n');
					row.delete(0, row.length());
					
					si = 1;
				}
				else {
					si++;
				}
				
				count++;
			}

			client.write('\n');
		}
	}
	
	/**
	 * Command: pose
	 * 
	 * Syntax: pose hops up and down, while holding their foot.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_pose(final String arg, final Client client) {
		Player player = getPlayer(client);
		Room room = getRoom( player.getLocation() );
		
		//addMessage( new Message(player.getName() + " " + arg, room) );
		
		for (final Player player1 : room.getListeners()) {
			send(colors(player.getName(), getDisplayColor(TypeFlag.PLAYER)) + " " + arg, player1.getClient());
		}
	}

	/**
	 * Command: put
	 * 
	 * Syntax: put <x> in <y>
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_put(final String arg, final Client client) {
		Player player = getPlayer(client);
		Room room = getRoom( player.getLocation() );

		// process syntax/arguments
		// get (from) <container> <item>

		// if there is no argument
		if ( arg.equals("") ) {
			send("Syntax: put [in] <container> <item>", client);
		}
		else {
			// split the arguments into a string array by space characters
			final String[] args = arg.split(" ");

			// tell us how many elements the array has (debug)
			debug("" + args.length);

			if (args.length == 2) {
				// allow for multi word items to be referred to with _ instead of the spaces that exist
				final String itemName = args[1].replace('_', ' ');

				System.out.println("Arg: " + args[0]);

				// find the container
				// TODO need a item finding function that can evaluate specified criteria
				// TODO I think maybe I should glom the items and things found into a containers list and iterate over that, stopping when I successfully find something

				boolean success = false;

				boolean found_item = false;
				boolean found_container = false;

				Item item = null;
				Storage<Item> s = null;

				final List<MUDObject> objects = new LinkedList<MUDObject>();

				objects.addAll( findItems(player.getInventory(), args[0]) );
				objects.addAll( findItems(room.getItems(),       args[0]) );
				objects.addAll( room.getThings() ); // TODO should be getting only things with the right name

				for (final MUDObject object : objects) {
					if( object.isType(TypeFlag.ITEM) ) {
						final Item temp = (Item) object;

						if( !found_item ) {
							if( temp.getName().equals(itemName) || temp.getName().startsWith(itemName) || temp.getName().endsWith(itemName) ) {
								item = temp;
								found_item = true;
								
								System.out.println("Item: " + temp.getName() );
								
								continue;
							}
						}

						if( !found_container) {
							if( temp.getName().equals(args[0]) || temp.getName().startsWith(args[0]) || temp.getName().endsWith(args[0]) ) {
								if ( temp.getItemType() == ItemTypes.CONTAINER) {
									s = (Container) temp;
									found_container = true;
									
									System.out.println("Container: " +  temp.getName() );
									
									continue;
								}
							}
						}
					}
					else if( object.isType(TypeFlag.THING) ) {
						final Thing temp = (Thing) object;

						if( !found_container) {
							if( temp.getName().equals(args[0]) || temp.getName().startsWith(args[0]) || temp.getName().endsWith(args[0]) ) {
								if( temp.thing_type == ThingTypes.CONTAINER ) {
									s = (Box) temp;
									found_container = true;
									
									System.out.println("Container: " + temp.getName() );
									
									continue;
								}
							}
						}


					}

					if( found_item && found_container ) success = put(player, s, item);
					
					if( success ) break;
				}

				if( !success ) send("No such item", client);
			}
		}
		
		/*
		List<MUDObject> foundObjects = findVisibleObjects(room);

		// split the arguments into a string array by space characters and make a list
		final List<String> args = Arrays.asList(arg.split(" "));
		// final List<String> args1 = Utils.mkList(arg.split(" "));

		// tell us how many elements the array has (debug)
		debug("" + args.size());

		// check for "in" and return if we don't find it
		int index = -1;

		for (final String s : args) {
			if (s.equals("in")) index = args.indexOf(s);
		}

		if (index == -1) return;
		else {
			System.out.println("PUT: " + args.get(0) + " " + args.get(2));
		}

		Container container = null;
		Item item = null;
		String param = null;
		String param1 = null;

		boolean found_item = false;
		boolean found_container = false;

		param = Utils.join(args.subList(0, index), " ");
		param1 = Utils.join(args.subList(index + 1, args.size()), "");

		// look in the player's inventory
		for (final Item item1 : player.getInventory()) {
			// find item
			if (!found_item) {
				if (item1.getName().equalsIgnoreCase(param)) {
					item = item1;
					found_item = true;
				}
			}

			// find container
			if (!found_container) {
				if (item1.getName().equalsIgnoreCase(param1) && item1 instanceof Container) {
					container = (Container) item1;
					found_container = true;
				}
			}
		}

		if (container != null && item != null) {
			System.out.println("Item: " + item.getName());
			System.out.println("Container: " + container.getName());

			if( container.insert(item) ) {
				player.getInventory().remove(item);
				item.setLocation( container.getDBRef() );
				System.out.println( container.getContents() );

				send("You put " + colors(item.getName(), "yellow") + " in " + colors(container.getName(), "yellow"), client);
			}
		}
		*/
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

		if (arg.equals("new")) {
			// the below uses a form of the constructor that doesn't assign an id
			quest = new Quest("New Quest", "A new, blank quest.");

			// add new quest to global table
			// quests.add( quest );

			exist = true;
		}
		else {
			// TODO if I could avoid calling the exact same code each time here,
			// that would be best. should I be calling abortEditor?
			
			// TODO work on improving this code.. (quest edit)
			int id = Utils.toInt(arg, -1); // catchs the number format exception
			
			try {
				quest = getQuest(id);
			}
			catch(final IndexOutOfBoundsException ioobe) {
				// invalid quest id/list index, cannot edit (abort)
				System.out.println("--- Stack Trace ---");
				ioobe.printStackTrace();

				// reset player, and clear edit flag and editor setting
				player.setStatus(old_status);
				player.setEditor(Editors.NONE);

				// clear editor data
				player.setEditorData(null);

				send("Game> Quest Editor - Unexpected error caused abort (bounds exception)", client);
			}

			if (quest != null) {
				exist = true;
			}
		}

		// quest exists
		if (exist) {
			if (quest.Edit_Ok) {
				quest.Edit_Ok = false;
			}
			else {
				// quest is not editable, exit the editor
				abortEdit("quest not editable (!Edit_Ok)", old_status, client);
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
		else {
			// quest doesn't exist (abort) reset player, and clear edit flag and editor setting

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
	 * Handles quest management for the player. Allows you to abandon, ignore
	 * quests and share them with other players. Also, will allow you to list
	 * your current quests
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_quests(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if (!arg.equals("")) {
			String[] args = arg.split(" ");

			if (arg.charAt(0) == '#') {
				String param = args[0];

				if (arg.indexOf(" ") != -1) param = arg.substring(1, arg.indexOf(" "));
				else                        param = arg.substring(1);
				
				if (args.length == 2) {
					Quest quest = null;
					
					int qNum = Utils.toInt(args[1], -1);

					if (qNum != -1) {
						// TODO more elegant fix?
						// bounds check
						if( qNum < player.getQuests().size() ) {
							quest = player.getQuests().get(qNum);
						}
					}

					/*
					 * if( quest == null ) { send("No such quest!", client);
					 * return; }
					 */

					if (quest != null) {
						if (param.equals("abandon") || param.equals("a")) {
							// #abandon or #a
							send("You abandon " + quest.getName() + ".", client);
							player.removeQuest(quest);
						}
						else if (param.equals("ignore") || param.equals("i")) {
							// #ignore or #i
							send("You ignore " + colors(quest.getName(), getDisplayColor("quest")) + ".", client);
							quest.setIgnore(true);
						}
						else if (param.equals("info")) {
							// #info
							send(Utils.padRight("", '-', 80), client);
							// send("----------------------------------------------------", client);
							send("Quest ID#: " + quest.getId(), client);
							send("Name: " + quest.getName(), client);
							send("Location: " + quest.getLocation().getName(), client);
							send("Description: ", client);
							send(parseDesc(quest.getDescription(), 80), client);
							// send("----------------------------------------------------", client);
							send(Utils.padRight("", '-', 80), client);

							int i = 0;

							for (final Task t : quest.getTasks()) {
								send(" " + i + ") " + t.getDescription() + "[ " + t.getProgress() + " ]", client);
								i++;
							}

							send(Utils.padRight("", '-', 80), client);
						}
						else if (param.equals("remember") || param.equals("r")) {
							// #remember or #r
							send("You remember " + colors(quest.getName(), getDisplayColor("quest")) + ".", client);
							quest.setIgnore(false);
						}
					}
				}
				else if (args.length == 3) {
					Player player1 = getPlayer(args[1]);
					
					int qNum = Utils.toInt(args[2], -1);

					if (param.equals("share") || param.equals("s")) {
						if (qNum != -1) {
							// TODO fix this ugly bounds check too (quests)
							if( qNum < player.getQuests().size() ) {
								final Quest quest = player.getQuests().get(qNum);

								// no way here for the player to deny a share...
								player1.addQuest( quest.getCopy() );

								final String msg = player.getName() + " shared " + quest.getName() + " with you.";

								addMessage( new Message(player, player1, msg) );
							}
						}
					}
				}
				else {
					if (param.equals("ignored")) {
						int index = 0;

						client.write("" + Colors.WHITE);

						send("Quests (Ignored)", client);
						send("================================================================================", client);

						for (final Quest quest1 : player.getQuests()) {
							if (quest1.isIgnored()) {
								client.write(index + ") ");
								client.write(Colors.YELLOW + "   o " + quest1.getName());
								client.writeln(Colors.MAGENTA + " ( " + quest1.getLocation().getName() + " ) " + Colors.CYAN);
								client.write('\n');
								
								client.write("" + Colors.YELLOW);
								send(parseDesc(quest1.getDescription(), 80), client);
								client.write("" + Colors.CYAN);

								for (final Task task : quest1.getTasks()) {
									if (task.isComplete()) {
										// should be greyed out if task is
										// complete
										client.write(Colors.GREEN + "      o " + task.getDescription());

										if (task.getType().equals(TaskType.KILL)) {
											final KillTask kt = (KillTask) task;

											client.write(" [ " + kt.kills + " / " + kt.toKill + " ]");
										}
									}
									else {
										client.write(Colors.CYAN + "      o " + task.getDescription());

										if (task.getType().equals(TaskType.KILL)) {
											final KillTask kt = (KillTask) task;

											client.write(" [ " + kt.kills + " / " + kt.toKill + " ]");
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
					}
					else if ((param.equals("list") || param.equals("l")) && checkAccess(player, Constants.BUILD)) {
						int index = 0;

						for (Quest q : quests) {
							System.out.println(index);
							send(q.getId() + ": " + q.getName() + " ( " + q.getLocation().getName() + " )", client);
							index++;
						}
					}
				}
			}
		}
		else {
			int index = 0;

			send("Quests", client);
			send("================================================================================", client);

			for (final Quest quest : player.getQuests()) {
				if ( !quest.isIgnored() ) {
					if ( quest.isComplete() ) {
						client.write(index + ")    o " + quest.getName() + " ( " + quest.getLocation().getName() + " ) [" + quest.getId() + "]\n");
						continue;
					}

					client.write(index + ") ");
					client.write(Colors.YELLOW + "   o " + quest.getName());
					client.write(Colors.MAGENTA + " ( " + quest.getLocation().getName() + " ) " + Colors.CYAN);
					client.writeln("[" + quest.getId() + "]");
					client.write('\n');
					client.write("" + Colors.YELLOW);
					
					send(parseDesc(quest.getDescription(), 80), client);

					client.write("" + Colors.CYAN);

					for (Task task : quest.getTasks()) {
						if ( task.isComplete() ) {
							// should be greyed out if task is complete
							client.write(Colors.GREEN + "      o " + task.getDescription());

							if (task.getType().equals(TaskType.KILL)) {
								final KillTask kt = (KillTask) task;

								client.write(" [ " + kt.kills + " / "  + kt.toKill + " ]");
							}
						}
						else {
							client.write(Colors.CYAN + "      o " + task.getDescription());

							if (task.getType().equals(TaskType.KILL)) {
								final KillTask kt = (KillTask) task;
								client.write(" [ " + kt.kills + " / " + kt.toKill + " ]");
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
	}

	// Function to disconnect player
	private void cmd_quit(final String arg, final Client client) {
		init_disconn(client, false);
	}

	// TODO ride command?
	/*
	 * private void cmd_ride(final String arg, final Client client) { final
	 * Player player = getPlayer(client); final Room room = getRoom( player );
	 * 
	 * List<Creature> creatures = objectDB.getCreaturesByRoom(
	 * room.getLocation() );
	 * 
	 * for(final Creature c : creatures) { if( c.getName().equals(arg) ) { if( c
	 * instanceof Ridable ) { Ridable r = (Ridable) c;
	 * 
	 * if( r.isLargeEnough(null) ) { mount( r, player ); } } } } }
	 */

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
		// need to find object whose name is arg and pass that object to
		// cmd_recycle, food for thought here -- 4.15.2010

		MUDObject object = null;
		final int dbref = Utils.toInt(arg, -1);

		if (dbref != -1) { // if we found one
			try {
				object = getObject(dbref);
			}
			catch (final NullPointerException npe) {
				System.out.println("--- Stack Trace ---");
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

				room.removeExit(exit); // remove exit from room
				objectDB.removeExit(exit); // remove exit from db

				success = true;
			}
			else if (object instanceof Room) {
				send("Recycle: Room recycling broken, needs to do a better job of cleaning up the room.", client);

				Room room = (Room) object;

				// destroy exits from this room
				for(final Exit exit : room.getExits()) {
					if( exit.getExitType() == ExitType.STD ) {
						// does it remove names?
						objectDB.removeExit(exit);
					}
				}

				objectDB.removeRoom(room); // recycle the room

				success = true;
			}
			else {
				send("Recycle: Cannot recycle that. (" + object.getClass().getName() + ")", client);
			}

			if (success) {
				String msg = name + "(#" + num + "): Recycled."; // i(#127728): Recycled.

				objectDB.remove(object); // clear the database entry (object)

				send(msg, client);
			}
		}
	}

	private void cmd_read(final String arg, final Client client) {
		final Player player = getPlayer(client);

		// get the specified item
		final List<Item> items = new LinkedList<Item>();

		items.addAll( objectDB.getItemsByLoc( player.getLocation() ) ); // in room
		items.addAll( player.getInventory() );                          // carried by player
		
		//objectDB.getThingsForRoom( getRoom( player.getLocation() ) );
		
		final Item item = MudUtils.findItem(arg, items);
		
		// use findItem! (11-9-2015)
		if (item != null) {
			debug("Item Name: " + item.getName());
			debug("Item Type: " + item.getItemType().toString());
			
			// check to see if it's a book (or readable?)
			if ( item.getItemType() == ItemTypes.BOOK ) {
				final Book book = (Book) item;

				if (book.numPages() > 0) {
					// show us what we're looking at
					final List<String> page = book.getPage( book.getPageNum() );

					if (page.size() > 0)       client.write(page);
					else if (page.size() == 0) client.writeln("The page is blank...");
				}
				else client.writeln("Odd, a totally empty book, devoid of pages.");
			}
			else if ( item instanceof Readable ) {
				String text = ((Readable) item).read();
				send(parseDesc(text, 80), client);
			}
			else if( item.getProperty("_game/readable", Boolean.class) ) {
				final String text = item.getProperty("_game/text");

				send(parseDesc(text, 80), client);
			}
		}
		else {
			send("No such item.", client);
		}
	}
	
	/**
	 * Recover your account? character? password
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_recover(final String arg, final Client client) {
		final ClientData cd = new ClientData();
		
		cd.state = Constants.QUERY;
		
		setClientState(client, "recover_password");
		//setClientData(client, new ClientData());
		setClientData(client, cd);
		
		// initial call recovery stuff
		op_recovery(client);
	}

	/**
	 * Register an account for the game
	 * 
	 * Not sure if this will be interactive or simply have a name/password
	 * parameter set.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_register(final String arg, final Client client) {
		setClientState(client, "register");
		setLoginData(client, new LoginData(Constants.USERNAME));
		
		// initial call registration stuff
		op_registration(client);

		/*
		// TODO make this interactive instead of requiring a name and password up front
		// TODO set client state to handle interactive input somehow
		System.out.println("arg: " + arg);

		String[] args = arg.split(" ");

		if (args.length == 2) {
			System.out.println("args[0]: " + args[0]);
			System.out.println("args[1]: " + args[1]);

			final String username = args[0];
			final String password = args[1];

			acctMgr.addAccount(username, password, 3);

			final Account account = acctMgr.getAccount(username, password);

			caTable.put(client, account);

			send("Account Registered!", client);
			send("Username: \'" + account.getUsername() + "\'", client);
			send("Password: \'" + account.getPassword() + "\'", client);

			// I'd really like this to log the account in and present it with the account menu
			// setClientState(client, "account_menu");

			send("You may now login using your account credentials as you would those for an ordinary player.", client);
		}
		else {
			send("register: Invalid data!", client);
		}
		 */
	}

	private void cmd_roll(final String arg, final Client client) {
		String[] args = arg.split(",");

		try {
			int number = Integer.parseInt(args[0]);
			int sides = Integer.parseInt(args[1]);

			send("Rolling " + number + "d" + sides, client);

			send("" + Utils.roll(number, sides), client);
		}
		catch (final NumberFormatException nfe) {
			debug("cmd(): 'roll' (number format exception)");
			System.out.println("--- Stack Trace ---");
			nfe.printStackTrace();
		}
	}

	/**
	 * Command to launch room editor (redit)
	 * 
	 * NOTE: concept borrowed from ROM, a derivative of Merc, a derivative of
	 * DIKU.
	 * 
	 * Basically you call the room editor like this: 'cmd_roomedit(<room #/room
	 * name>, client)'
	 * 
	 * And it attempts to find the room and edit it, if it can't find it, it
	 * will indicate a failure and open the editor with no room.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_roomedit(final String arg, final Client client) {
		final Player player = getPlayer(client);

		Room room = null;
		boolean exist = false;

		// get a room
		if (arg.equals("") || arg.toLowerCase().equals("here")) { // edit current room
			room = getRoom(player.getLocation());

			exist = true;
		}
		else if (arg.equals("new")) { // create new room if no room to edit specified
			room = createRoom("New Room", 0);

			// add new room to database
			objectDB.addAsNew(room);
			objectDB.addRoom(room);

			// do not use, yet
			// room.setDBRef(objectDB.peekNextId());
			// objectDB.reserveID(); // hold onto the id just in case we decide
			// to keep the room, but don't let anyone else use it

			exist = true;
		}
		else {
			final int dbref = Utils.toInt(arg, -1);

			if (dbref != -1)  room = getRoom(dbref); // get room by dbref (Integer)
			else              room = getRoom(arg); // get room by name (String)

			if (room != null) {
				exist = true;
			}
		}

		if (exist) {
			editRoom(player, room);
		}
	}

	// COMMAND OBJECT (5-31-2015)
	// 'say' function
	/*
	 * private void cmd_say(final String arg, final Client client) {
	 * send("You say, \"" + arg + "\"", client); Message msg = new
	 * Message(getPlayer(client), arg); addMessage(msg); }
	 */

	/**
	 * Set properties on objects.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_set(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom(player.getLocation());
		
		MUDObject mobj;

		// ex. @set here=header:======================
		
		debug("SET: " + arg);
		
		String[] args = arg.split("=", 2);

		// if there is a property to be set or removed
		if (args.length > 1) {
			final String target = args[0];
			final String other = args[1];
			
			debug("SET (target): " + target);
			debug("SET (other): " + other);
			
			// get the property and it's value from the arguments
			final String[] temp = Utils.trim(other).split(":", 2);
			
			// if we have a separate key and value to set
			if (temp.length == 2) {
				final String key = Utils.trim(temp[0]);
				final String value = Utils.trim(temp[1]);

				debug("SET (property): " + key);
				debug("SET (value): " + value);
				
				// get the object
				switch(target) {
				case "me":   mobj = player; break;
				case "here": mobj = room; break;
				default:     mobj = getObject(target); break;
				}
				
				debug("MOBJ IS NULL? " + (mobj == null));

				// if the object isn't null and is neart the player, modify it's properties
				if (mobj != null) {
					int dbref = mobj.getDBRef();
					int location = mobj.getLocation();

					debug("Object Loc:   " + location);
					debug("");
					debug("Player DBRef: " + player.getDBRef());
					debug("Player Loc:   " + player.getLocation());
					
					boolean roomItself = (dbref == player.getLocation());
					boolean sameRoom = (location == player.getLocation());
					boolean holding = (location == player.getDBRef());
					
					if( roomItself || holding || sameRoom ) {
						if ( !value.equals("") ) {
							mobj.setProperty(key, value);
							send("Property \'" + key + "\' with value of \'" + value + "\' set on " + mobj.getName(), client);
						}
						else {
							mobj.getProperties().remove(key);
							send("Property \'" + key + "\' removed from " + mobj.getName(), client);
						}
					}
				}
			}
			else {
				send("SET: Invalid key-value pair!", client);
			}
		}
		else {
			send("SET: No property specified!", client);
		}
	}

	/**
	 * setmode <n/d/w/m>
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_setmode(final String arg, final Client client) {
		if (arg == null) {
			changeMode(GameMode.NORMAL);
			
			send("Entering " + mode.toString() + " Mode.");
		}
		else {
			final char ch = arg.toLowerCase().charAt(0);
			
			// TODO this is kind of a kludge, since I'd have to muck with it if I added a GameMode
			if ( "ndwm".indexOf(ch) != -1 ) {
				changeMode( GameMode.fromChar(ch) );
				
				send("Entering " + mode.toString() + " Mode.");
			}
			else send("Invalid GameMode.", client);
		}
	}
	
	private void cmd_setcolor(final String arg, final Client client) {
		final String[] args = arg.split(" ");

		if (args.length > 1) {
			if (args[0].charAt(0) == '#') {
				final String param = args[0].substring(1);

				final List<String> validParams = Utils.mkList("npc", "player", "creature", "item", "quest", "thing", "room");

				if (validParams.contains(param)) {
					send(param + ": " + colors(param, getDisplayColor(param)), client);

					if (color == Constants.ANSI) {
						setDisplayColor(param, args[1], this.displayColors.get(param).two);
					}
					else if (color == Constants.XTERM) {
						setDisplayColor(param, this.displayColors.get(param).one, args[1]);
					}
				}
			}
		}
	}

	/* Time */

	/**
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_sethour(final String arg, final Client client) {
		int hour = Utils.toInt(arg, -1);

		if (hour >= 0 && hour <= 23) {
			game_time.setHours(hour);

			send("Game> Hour set to " + hour, client);
		}
		else {
			send("Game> Invalid hour", client);
		}
	}

	/**
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_setminute(final String arg, final Client client) {
		int minute = Utils.toInt(arg, -1);

		if (minute >= 0 && minute <= 59) {
			game_time.setMinutes(minute);

			send("Game> Minute set to " + minute, client);
		}
		else {
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

				if (newLevel <= max_levels) {
					player.changeLevelBy(changeLevel);
					send("Game> Gave " + player.getName() + " " + changeLevel
							+ " levels (levels).", client);
				} else {
					send("Game> Greater than maximum level used, no changes made.",
							client);
				}
			}
			catch (final NumberFormatException nfe) {
				debug("cmd_setlevel(): not a valid level since it isn't a number (number format exception)");
				System.out.println("--- Stack Trace ---");
				nfe.printStackTrace();
			}
		} else {
			send("Game> No amount specified, no experience (xp) change has been made.",
					client);
		}
	}

	/**
	 * Set Skill (setskill):
	 * 
	 * Increases the specified player skill by the amount given, negative or
	 * positive, if the input is positive it is a skill point gain, otherwise it
	 * is a skill point loss.
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

			if (skill != null) {
				System.out.println(skillName);
				System.out.println(skill.toString());

				if (skillValue < Constants.MAX_SKILL) {
					send("Set " + skill.toString() + " skill to " + skillValue,
							client);
					player.setSkill(skill, skillValue);
				} else {
					send("Setting exceeds maximum skill value, change aborted.",
							client);
				}
			} else {
				send("No such skill!", client);
			}
		} else {
			String skillName = args[0];

			Skill skill = getSkill(skillName);

			if (skill != null) {
				send(skill.getName() + " = " + player.getSkill(skill));
			}
		}
	}

	private void cmd_sethp(final String arg, final Client client) {
		final Player player = getPlayer(client);

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
			catch (final NumberFormatException nfe) {
				debug("cmd(): '@sethp' (number format exception)");
				System.out.println("--- Stack Trace ---");
				nfe.printStackTrace();
			}
		}
	}

	private void cmd_setmana(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if (!arg.equals("")) {
			System.out.println("ARG: " + arg);

			final int changeMANA = Utils.toInt(arg, 0);
			final int changeSign = Integer.signum(changeMANA);

			System.out.println("INTERPRETED VALUE: " + changeMANA);
			System.out.println("SIGN: " + changeSign);

			if (changeMANA != 0) {
				player.setMana(changeMANA);

				if (changeSign > 0) {
					send("Game> Gave " + player.getName() + " " + changeMANA + " mana (mana).", client);
				} else if (changeSign < 0) {
					send("Game> Took " + changeMANA + " mana (mana) from " + player.getName(), client);
				}

				player.updateCurrentState();
			} 
			else {
				send("Game> No amount specified, no mana (mana) change has been made.", client);
			}
		}
	}

	private void cmd_setxp(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if (!arg.equals("")) {
			System.out.println("ARG: " + arg);

			final int changeXP = Utils.toInt(arg, 0);

			System.out.println("INTERPRETED VALUE: " + changeXP);
			System.out.println("SIGN: "+ Integer.signum(changeXP));

			player.setXP(changeXP);
			send("Game> Gave " + player.getName() + " " + changeXP + " experience (xp).", client);
		}
		else {
			send("Game> No amount specified, no experience (xp) change has been made.", client);
		}
	}

	// TODO make this work
	private void cmd_setweather(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );
		final Zone zone = getZone( room );
		
		final Season s = this.season;
		
		List<WeatherState> wsl = s.getPattern().getStates();
		
		WeatherState w = null;

		for (final WeatherState ws : wsl) {
			if ( ws.getName().equals(arg) ) {
				w = ws;
				break;
			}
		}

		if (w == null) return;
		
		List<Room> rooms = null;
		
		// if the zone exists,
		// change each room in the zone to the specified weather..
		if (zone != null) rooms = zone.getRooms();
		else              rooms = objectDB.getRooms();
		
		for(final Room room1 : rooms) {
			room1.getWeather().setState(w);
		}
	}

	private void cmd_score(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		send( String.format("Level: %d   Race: %s   Class: %s", player.getLevel(), player.getRace().getName(), player.getPClass().getName()), client);
		
		send("Race: " + player.getRace().getName() + " Sex: " + player.getGender() + " Class: " + player.getPClass().getName(), client);
		//send("You are " + player.getName() + ". " + player.getTitle() + ", level " + player.getLevel(), client);
		//send("Race: " + player.getRace().getName() + " Sex: " + player.getGender() + " Class: " + player.getPClass().getName(), client);
		send("Hit points: " + player.getHP() + "(" + player.getTotalHP() + ")", client);
		
		int exp = player.getXP();
		int exp_need = player.getXPToLevel();

		double exp_prog = ((float) player.getXP() / (float) player.getXPToLevel()) * 100;
		//double exp_prog = ((float) exp / (float) exp_need) * 100;
		
		send("Experience Progress: " + new DecimalFormat("#0.0").format(exp_prog) + " %", client);
		//send(String.format("Experience Progress: %d / %d [%s] ", exp, exp_need, new DecimalFormat("#0.0").format(exp_prog) + " %"), client);

		//send("Money: " + player.getMoney().toString() + ".", client);
		send("Coins: " + player.getMoney().toString(true) + ".", client);

		// Toril Mud 'score' output below
		/*
		 * Level: 2 Race: Human Class: Warrior Hit points: 48(48) Moves:
		 * 160(160) Experience Progress: 4 % Coins carried: 0 platinum 8 gold 3 silver 158 copper Coins in bank: 0 platinum 10 gold 0 silver 0 copper
		 * Prestige: 760 Acheron Kill Count: 0 Outcast from: Leuthilspar Playing
		 * time: 0 days / 11 hours / 15 minutes Title: Status: Standing.
		 */
	}

	/**
	 * Command: sell
	 * 
	 * Syntax: sell <item>
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_sell(final String arg, final Client client) {
		final Player player = getPlayer(client);

		// TODO at the moment a player has to be interacting with the NPC to sell things
		if (player.getStatus().equals(Constants.ST_INTERACT)) {
			if (player.getTarget() instanceof NPC) {
				final NPC npc = (NPC) player.getTarget();

				// we can only sell to vendors
				if( npc.hasFlag(ObjectFlag.VENDOR) ) {
					final Vendor v = (Vendor) npc;
					
					// get the object to sell
					final Item item = MudUtils.findItem(arg, player.getInventory());
					
					final Coins payment = v.sell(item);

					// assume that you can sell anything to any vendor
					send(colors(npc.getName(), getDisplayColor("npc")) + " takes a " + colors(item.getName(), getDisplayColor("item")) + " and gives you " + payment.toString(true));
					
					player.getInventory().remove(item);
					player.setMoney( player.getMoney().add(payment) );
				}
			}
		}
	}
	
	private void cmd_select(final String arg, final Client client) {
		if( Utils.mkList("friend", "f", "ally", "a", "enemy", "e").contains( arg.toLowerCase() ) ) {
			// search through the room and find the creatures/NPCs/Players that fit that list
		}
		else if( arg.equalsIgnoreCase("all") ) {
			
		}
		else {
			// try to find the specified target
		}
	}
	
	private void cmd_session(final String arg, final Client client) {
		Player p = getPlayer(arg);

		if (p != null) {
			Session s = sessionMap.get(p);
			send("Connected: " + s.connected, client);
			send("Connect Time: " + s.connect, client);

			// getting time spent connected is a trick here, because the time
			// object doesn't track dates, and so the comparison would only be meaningful for
			// 12-24 hours at which point we don't have any idea how long they've been connected
			// anymore
			//send("Connected for: ", client);

			//send("Disconnect Time: " + s.disconnect, client);
			send("", client);
			send("Player: " + s.getPlayer().getName(), client);
			send("Client (IP): " + s.getClient().getIPAddress(), client);
		}
		else {
			// retrieve last session for player
			// Session s = loadSessionData( player );
		}
	}
	
	private void cmd_skillcheck(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		final String args[] = arg.split(" ");

		// TODO: fix code to deal with players who are
		// invalid/not setup and have no skills to check
		// TODO: add a means to indicate which skills to test
		// TODO: move to another command category?
		Player player1 = null;

		String skillName = "";
		String dice = "";
		Integer DC = 0;

		Skill sk;
		Integer value = 0;
		Integer mod = 0;

		if (args.length == 3) {
			skillName = args[0];
			dice = args[1];
			DC = Integer.parseInt(args[2]);

			player1 = player;
		}
		else if (args.length == 4) {
			final String playerName = args[0];

			skillName = args[1];
			dice = args[2];
			DC = Integer.parseInt(args[3]);

			player1 = getPlayer(playerName);
		}

		sk = rules.getSkill(skillName);

		value = player.getSkill(sk);
		mod = player.getSkillMod(sk);

		send(skill_check(player1, sk, dice, DC) ? "true" : "false", client);
	}

	private void cmd_skilledit(final String arg, final Client client) {
		Player player = getPlayer(client);
		String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT); // set the 'edit' status flag
		player.setEditor(Editors.SKILL); // skill editor

		EditorData newEDD = new EditorData();

		Skill skill = null;

		if (arg.equals("new")) {
			skill = new Skill();
		} else {
			// get skill
			skill = getSkill(arg);
		}

		if (skill != null) {
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

			// newEDD
			op_skilledit("show", client);
		} else {
			abortEdit("Invalid Skill!", old_status, client);
		}
	}

	private void cmd_spellinfo(final String arg, final Client client) {
		final Spell spell = getSpell(arg);
		
		final List<String> output = new LinkedList<String>();
		
		output.add( String.format("Name:    %s", spell.getName()) );
		output.add( String.format("Level:   %s", spell.getLevel()) );
		output.add( "" );
		output.add( String.format("Cost:    %s", spell.getManaCost()) );
		output.add( "" );
		output.add( String.format("Targets: %s", Spell.decodeTargets(spell)) );
		
		send( output, client );
	}

	private void cmd_spells(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		send("Spell List", client);
		send("-----------------------------------------------------------------", client);
		
		if (arg.equals("#all") && checkAccess(player, Constants.WIZARD)) {
			for (final Spell spell : this.spells2.values()) {
				client.write(spell.getSchool() + " " + spell.getName() + "\n");
			}
		}
		else {
			// list your spells, by level?
			final SpellBook sb = player.getSpellBook();
			
			for (int level = 0; level < player.getLevel(); level++) {
				final List<Spell> spells = sb.getSpells(level);
				
				send("Level " + level, client);
				send("--------", client);
				
				for (final Spell spell : spells) send(spell.getName(), client);
			}
		}
		
		send("-----------------------------------------------------------------", client);
	}

	// COMMAND OBJECT (5-31-2015)
	
	/**
	 * Command: staff
	 * 
	 * COMMAND OBJECT EXISTS
	 * 
	 * List staff,
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_staff(final String arg, final Client client) {
		send("Staff", client);
		send("----------------------------------------", client);

		for(final Player p : players) {
			final String playerName = Utils.padRight( Utils.truncate(p.getName(), 16), 16);

			switch(p.getAccess()) {
			case Constants.SUPERUSER:
				send(playerName + "[SUPERUSER]", client);
				break;
			case Constants.WIZARD:
				send(playerName + "[WIZARD]", client);
				break;
			case Constants.ADMIN:
				send(playerName + "[ADMIN]", client);
				break;
			case Constants.BUILD:
				send(playerName + "[BUILD]", client); break; default: break; 
			} 
		}
	}

	private void cmd_success(final String arg, final Client client) {
		final String[] args = arg.split("=");
		// List<Exit> exits =
		// objectDB.getExitsByRoom(getPlayer(client).getLocation());
		final Exit exit = getExit(args[0]);

		if (args.length > 1) {
			debug(exit.getName() + "(" + exit.getDBRef() + ")");
			exit.setMessage("succMsg", args[1]);
			send(exit.getName() + "'s success message set to: " + args[1],
					client);
		}
	}
	
	private void cmd_sheathe(final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		if (player.getSlots().get("weapon").isFull()) {
			final Weapon w = (Weapon) player.getSlots().get("weapon").remove();
			// temp.inventory.add(temp.getSlots().get("weapon").remove());
			send("You put away your " + w.getName(), client);
		}
		else if (player.getSlots().get("weapon1").isFull()) {
			final Weapon w = (Weapon) player.getSlots().get("weapon").remove();
			// temp.inventory.add(temp.getSlots().get("weapon").remove());
			send("You put away your " + w.getName(), client);
		}
	}
	
	// TODO combat oriented
	private void cmd_shoot(final String arg, final String client) {
		Player player = getPlayer(client);
		
		// get weapon
		final Weapon w = player.getWeapon(true);
		
		// is weapon a gun? / ranged
		boolean ranged = true;
		
		if( ranged ) {
			// get target
			final MUDObject m = player.getTarget();
			
			/*if( m.isType(TypeFlag.PLAYER) ) {			
			}
			else {
			}*/
			
			float distance = Utils.distance( player.getPosition(), m.getPosition() );
			
			// are we in range (shooting at things we can't hit is pointles
			boolean in_range = true;
			
			if( in_range ) {
				// determine distance
				
				// decide if the shot hits
				
				// apply damage
			}
		}
	}

	// Server Start Function (usually used to start again after manual shutdown)
	private void cmd_restart(final String arg, final Client client) {
		restart();
	}

	// Server Shutdown function
	private void cmd_shutdown(final String arg, final Client client) {
		// shutdown [ now /-h <secs> ]
		// shutdown -h 5
		
		final String[] args = arg.split(" ");

		if (args.length == 1) {
			final String param = args[0];

			if ( param.equals("now") ) {
				debug("SHUTDOWN TYPE: IMMEDIATE");
				
				// tell people the server is going down
				write("Server going down immediately.");

				shutdown();
			}
			else if ( param.equals("-h")) {
				debug("SHUTDOWN TYPE: TIMED");

				int time = 0;

				if (args.length == 2) {
					time = Utils.toInt(args[1], 300);
				}
				
				String time_str = (time / 60) + "m" + " " + (time % 60) + "s";
				
				// tell people the server is going down
				write("Server going down for reboot in " + time_str);

				shutdown(time);
			}
		}
		else {
			debug("SHUTDOWN TYPE: NORMAL");
			
			// tell people the server is going down
			write("Server going down for reboot in 10 minutes.");

			shutdown(600); // 10 m * 60 s/m = 600 s
		}
	}
	
	// TODO what the heck is this for
	private String getPackagePath(final String packageName) {
		return "" + resolvePath("", packageName.split("."));
	}

	/**
	 * Spawn an instance of a pre-defined creature
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_spawn(final String arg, final Client client) {
		final Player player = getPlayer(client);

		Creature c = new Creature(arg, "a spawned creature");

		if( c != null ) {
			c.setLocation( player.getLocation() );

			// set virtual flag?

			objectDB.addAsNew(c);
			objectDB.addCreature(c);

			send("Spawned " + c.getName(), client);
		}

		if( arg.startsWith("#") ) {
			final String param = arg.substring(1);

			if( param.equals("list") ) {
				send("-- Creatures", client);

				final String path = getPackagePath("mud.objects.creatures");
				File file = new File(path);

				if( file.isDirectory() ) {
					for(final String fileName : file.list()) {
						send(fileName, client);
					}
				}
			}

		}
		else {
			Class<?> cla = getClass("mud.objects.creatures." + arg);

			Creature cre = null;

			if( cla != null ) {
				try {
					cre = (Creature) cla.getConstructor().newInstance();
				}
				catch (final InstantiationException e)    { e.printStackTrace(); }
				catch (final IllegalAccessException e)    { e.printStackTrace(); }
				catch (final IllegalArgumentException e)  { e.printStackTrace(); }
				catch (final InvocationTargetException e) { e.printStackTrace(); }
				catch (final NoSuchMethodException e)     { e.printStackTrace(); }
				catch (final SecurityException e)         { e.printStackTrace(); }

				if( cre != null ) {
					cre.setLocation(getPlayer(client).getLocation());

					// TODO set virtual flag?
					objectDB.addAsNew(cre);
					objectDB.addCreature(cre);

					send("Spawned " + arg, client);
				}
			}
			else {
				// TODO kludge land, fix this...
				cre = new Creature(arg, "");

				if( cre != null ) {
					cre.setLocation(getPlayer(client).getLocation());

					// set virtual flag?

					objectDB.addAsNew(cre);
					objectDB.addCreature(cre);

					send("Spawned " + arg, client);
				}

				send("No such Creature exists.", client);
			}
		}
	}
	
	/**
	 * List object statistics for the world.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_stats(final String arg, final Client client) {
		System.out.println(objectDB.getSize());

		final int[] counts = objectDB.getFlagCounts(new String[] { "P", "N", "E", "R", "I", "T" });
		final int usersCount = counts[0];
		final int npcsCount = counts[1];
		final int exitsCount = counts[2];
		final int roomsCount = counts[3];
		final int itemsCount = counts[4];
		final int thingsCount = counts[5];

		int total = usersCount + npcsCount + exitsCount + roomsCount
				+ itemsCount + thingsCount;

		send(serverName + " Statistics", client);
		send("----------------------", client);
		send(String.format("Players: %-4s %5.2f%%", usersCount, usersCount * 100.0 / total), client);
		send(String.format("NPCS:    %-4s %5.2f%%", npcsCount, npcsCount * 100.0 / total), client);
		send(String.format("Exits:   %-4s %5.2f%%", exitsCount, exitsCount * 100.0 / total), client);
		send(String.format("Rooms:   %-4s %5.2f%%", roomsCount, roomsCount * 100.0 / total), client);
		send(String.format("Items:   %-4s %5.2f%%", itemsCount, itemsCount * 100.0 / total), client);
		send(String.format("Things:  %-4s %5.2f%%", thingsCount, thingsCount * 100.0 / total), client);
		send("Total:   " + total, client);
		send("----------------------", client);
	}

	// output format borrowed from TorilMud status command
	private void cmd_status(final String arg, final Client client) {
		send("Effects", client);
		send(Utils.padRight("", '-', 75), client);

		int index = 1;
		StringBuilder sb = new StringBuilder();

		for (final Effect effect : getPlayer(client).getEffects()) {
			if (index % 3 == 0) {
				send(sb.toString(), client);
				sb.delete(0, sb.length());
			}
			else {
				sb.append(Utils.padRight(effect.getName(), ' ', 25));
			}

			index++;
		}

		if (sb.length() != 0) {
			send(sb.toString(), client);
			sb.delete(0, sb.length());
		}

		send("", client);

		send("Spells", client);
		send(Utils.padRight("", '-', 75), client);
	}
	
	private static List<String> getNames(final List<? extends MUDObject> list) {
		List<String> l = new ArrayList<String>(list.size());
		
		for(final MUDObject m : list) {
			l.add( m.getName() );
		}
		
		return l;
	}
	
	private void cmd_talk(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final NPC npc = getNPC(arg);
		
		// TODO improve talk command
		
		/*
		 * I need a pretty way to handle different names..
		 * 
		 * 'talk Star' is fairly unambiguous, but what if there are two NPC,
		 * Star and Star Nova or just one NPC with the latter name? I think
		 * we should get the exact match if there is one, a less exact match
		 * if there is only one similar option, and a list of NPCs in the room
		 * otherwise
		 * 
		 * However this is a mess that boils down to what should getNPC(...) be doing.
		 * 
		 * some sort of result block might be useful, but turning getNPC(...) into a complex
		 * function is not ideal...
		 */
		
		/*
		NPC npc = null;
		
		// TODO better resolution? I don't want to 
		if( arg.contains(" ") ) {
			List<NPC> npcList = objectDB.getNPCsByLocation( player.getLocation() );
			
			if     ( npcList.isEmpty()   ) npc = null;
			else if( npcList.size() == 1 ) npc = npcList.get(0);
			else {
				npc = null;
				
				send("Did you mean?", client);
				send( MUDServer.getNames(npcList), client);
			}
		}*/

		// player should be not null, but...
		if (player != null && npc != null) {
			final CNode cnvs = npc.getConversation();
			
			// send npc greeting
			// npc.greet( getPlayer(client) );
			//send(colors(npc.getName(), getDisplayColor("npc")) + ": " + npc.greeting, client);
			send(colors(npc.getName(), getDisplayColor("npc")) + ": " + cnvs.getResponse(), client);
			send("", client);

			// send list of player conversation options			
			send("-- Conversation (" + npc.getName() + ")", client);
			
			int n = 1;
			
			final List<String> convOpts = new LinkedList<String>();
			
			for(final CNode cn : cnvs.getOptions()) {
				if( cn.ends ) convOpts.add(n + " ) " + colors(cn.getText() + " (Ends Conversation)", "green") );
				else          convOpts.add(n + " ) " + colors(cn.getText(), "green") );
				
				n++;
			}
			
			send(convOpts, client);
			
			send("", client);
			
			conversations.put(player, new Tuple<NPC, CNode>(npc, cnvs));
			
			// set player status to conversation (CNVS)
			player.setStatus("CNVS");
		}
	}

	// Function to take objects in a room
	private void cmd_take(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		final String[] args = arg.split(" ");

		debug("" + args.length);

		// if there is no argument, report command syntax
		if (arg.equals("")) {
			send("Syntax: take <item>", client);
		}

		// if there are three arguments, implying the following syntax:
		// TAKE <thing> FROM <container>
		else if ( arg.matches("(take)(\\s+)((?:[a-z][a-z]+))(\\s+)(from)(\\s+)((?:[a-z][a-z]+))") ) {
			debug("take from");

			String itemName = args[1];
			String container = args[3];

			final Item item = MudUtils.findItem(itemName, room.getItems()); // this probably won't work...
		}
		else if (arg.equalsIgnoreCase("all")) {
			// TODO problem if getItems() returns an unmodifiable list?

			// all implies stuff on the ground. since all the stuff on the ground is in the room,
			// we should evaluate the room to get it's stuff

			// basically we want to evaluate all the items, then take the one with the largest value,
			// one at a time. the evaluation scheme needs to take what's usable and what's not
			// as well monetary value into account
			// if we have room for everything, then just take it all
			// - an item is usable if, given restrictions, you meet all
			// the requirements (class, race, level, skill)

			final List<Item> items = new ArrayList<Item>( room.getItems() );

			for (final Item item : items) {
				take(player, room, item);
			}
		}
		else {
			// assuming one argument
			Item item = null;

			// get the integer value, if there is one, as the argument
			final int dbref = Utils.toInt(arg, -1);

			// look for specified item in the player's current room
			if (dbref != -1)  item = MudUtils.findItem(dbref, room.getItems());
			if (item == null) item = MudUtils.findItem(arg, room.getItems());
			
			debug("DBRef: " + dbref);
			debug("Item:  " + item);

			if (item != null) {
				debug("Item is " + item.getName());
				take(player, room, item);
			}
			else {
				Thing thing = getThing(arg, room);

				if (thing != null) {
					final String msg = thing.getProperty("_game/msgs/take-fail");

					if ( !msg.equals(Constants.NONE) ) send(msg, client);

					send("You can't pick that up.", client);
				}
				else {
					send("You can't find that.", client);
				}
			}
		}
	}

	private void cmd_target(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );
		
		MUDObject target = player.getTarget();
		
		if( arg.equals("") ) {
			if( target != null ) {
				send("target: " + colors(target.getName(), getDisplayColor(target.type)), client);
			}
			else {
				send("target: none", client);
			}
		}
		else {
			target = getObject(arg, room);

			if (target != null) {
				debug("Getting target..." + target.getName());

				player.setTarget(target);

				// tell us what we are targetting now
				debug( player.getTarget().getName() );
				debug(arg);
				send("Target set to: " + player.getTarget().getName(), client);
			}
			else {
				send("You don't see that.", client);
			}
		}
	}

	// tell <player> <message>
	private void cmd_tell(final String arg, final Client client) {
		final Player player;
		final NPC npc;

		player = getPlayer(client);

		if (player == null) {
			npc = getNPC(arg);

			if (npc == null) {
				return;
			}
			else {
			}
		}
		else {
			if (player.getStatus().equals("OOC")) {
				if (arg.indexOf(" ") != -1) {
					final String pName = arg.substring(0, arg.indexOf(" "));
					final String message = arg.substring(arg.indexOf(" "), arg.length());

					final Player player1 = getPlayer(pName);

					final Message msg = new Message(player, player1, message);
					
					addMessage(msg);
				}
				else send("invalid player", client);
			}
			else send("you cannot use that command while in-character", client);
		}
	}
	
	private void cmd_time(final String arg, final Client client) {
		send(gameTime(), client);
		send(gameDate(), client);
	}
	
	/**
	 * TRADE
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_trade(final String arg, final Client client) {
		// trade <player>    -- offer to trade with other player
		// trade #accept     -- accept offer to trade
		// trade #add <item> -- add something to the trade
		// trade #rem <item> -- remove something from the trade
		// trade #confirm    -- finalize a trade in progress
		// trade #cancel     -- cancel an in progress trade
		
		// -- P1 trades with P2
		// trade P2
		// trade #accept P1
		// trade #add P1_item / P2_item OR trade #rem P1_item / P2_item
		// trade #confirm OR trade #cancel

		// NOTES:
		// each player may be only trade with one other player and no
		// player may be involved in more than one trade at a time
		// if either player exits the room? then the trade will be canceled

		final Player player = getPlayer(client); // get the current player

		final String[] args = arg.split(" ");

		if (args.length >= 1) {
			if ( args[0].startsWith("#") ) {				
				final String param = args[0].substring(1).toLowerCase();
				final String arg2 = (args.length == 2) ? args[1] : "";
				
				final Trade trade = trades.get(player);

				if ( trade != null ) {
					final Player player1 = ((player == trade.p1) ? trade.p2 : trade.p1);

					if ( param.equals("add") || param.equals("a") ) {
						final Item item = MudUtils.findItem(arg2, player.getInventory());

						if( item != null ) {
							trade.addItem(player, item);

							//send("You add " + colors(item.getName(), getDisplayColor(TypeFlag.ITEM)) + " to the trade.", client);
							notify(player, "You add " + colors(item.getName(), getDisplayColor(TypeFlag.ITEM)) + " to the trade.");
							notify(player1, player.getName() + " has added " + colors(item.getName(), getDisplayColor(TypeFlag.ITEM)) + " to the trade.");
						}
					}
					else if ( param.equals("remove") || param.equals("rem") || param.equals("r") ) {
						Item item = null;

						// NOTE: we're assuming trade items are removed from inventory, which they aren't...
						if( player == trade.p1 )      item = MudUtils.findItem(arg2, trade.p1_items);
						else if( player == trade.p2 ) item = MudUtils.findItem(arg2, trade.p2_items);

						if( item != null ) {
							trade.removeItem(player, item);

							send("You remove " + colors(item.getName(), getDisplayColor(TypeFlag.ITEM)) + "from the trade.", client);
							notify(player1, player.getName() + " has removed " + colors(item.getName(), getDisplayColor(TypeFlag.ITEM)) + " from the trade,");
						}
					}
					else if ( param.equals("cancel") ) {
						notify(player, "You cancel your trade with " + player1.getName() + ".");
						notify(player1, player.getName() + " has canceled their trade with you.");
						
						trades.remove( player );
						trades.remove( player1 );
					}
					else if ( param.equals("confirm") ) {
						if( player == trade.p1 && trade.p2_confirm || player == trade.p2 && trade.p1_confirm ) {
							// finish the trade and transfer the items
							final List<Item> itemsP1 = trade.p1_items;
							final List<Item> itemsP2 = trade.p2_items;

							trade.p1.getInventory().removeAll(itemsP1);
							trade.p2.getInventory().addAll(itemsP1);

							trade.p2.getInventory().removeAll(itemsP2);
							trade.p1.getInventory().addAll(itemsP2);

							trade.p1_items.clear();
							trade.p2_items.clear();

							trades.remove(player);
							trades.remove(player1);

							notify(player, "You successfully conclude your trade with " + player1.getName());
							notify(player1, "You successfully conclude your trade with " + player.getName());
						}
						else {
							notify(player, "You confirm the trade. (use 'trade #rescind' if you've changed your mind)");
							notify(player, "Waiting for " + player1.getName() + " to confirm the trade.");
							
							notify(player1, player.getName() + " has confirmed the trade. (use 'trade #confirm' if you wish to complete it)");
						}
					}
					else if ( param.equals("rescind") || param.equals("resc") ) {
						if ( player == trade.p1 ) {
							trade.p1_confirm = false;
							
							notify(player, "You rescind the trade.");
							notify(player1, player.getName() + " has rescinded the trade.");
						}
						else if ( player == trade.p2 ) {
							trade.p2_confirm = false;
							
							notify(player, "You rescind the trade.");
							notify(player1, player.getName() + " has rescinded the trade.");
						}
					}
					else if ( param.equals("list") || param.equals("l") ) {
						List<Item> items;
						List<String> output = new ArrayList<String>();
						
						//send("-- Trade", client);
						output.add("-- Trade");
						
						//send("- You", client);
						output.add("- You");
						
						items = ((player == trade.p1) ? trade.p1_items : trade.p2_items);
						
						for(final Item item : items){
							//send(colors(item.getName(), getDisplayColor(TypeFlag.ITEM)), client);
							output.add( colors(item.getName(), getDisplayColor(TypeFlag.ITEM)) );
						}
						
						//send("- Them", client);
						output.add("- Them");
						
						items = ((player == trade.p1) ? trade.p2_items : trade.p1_items);
						
						for(final Item item : items){
							//send(colors(item.getName(), getDisplayColor(TypeFlag.ITEM)), client);
							output.add( colors(item.getName(), getDisplayColor(TypeFlag.ITEM)) );
						}
						
						send( output, client );
					}
					else {
					}
				}
				else {
					if ( param.equals("accept") ) {
						final Player partner = tradeInvites.get(player);

						if( partner != null ) {
							//create new Trade object and store it somewhere
							Trade newTrade = new Trade(partner, player);

							trades.put(player, newTrade);
							trades.put(partner, newTrade);

							notify(partner, player.getName() + " has accepted your trade request.");

							tradeInvites.remove(player);
						}
						else {
							debug("trade error");
						}
					}
					else {
						send("You aren't currently trading with anyone.");
					}
				}
			}
			else {
				// try to find the specified player and initiate trade with them
				final Player player1 = objectDB.getPlayer(args[0]);

				// if such a player exists and they are in the same room as you
				if (player1 != null) {
					if (player.getLocation() == player1.getLocation()) {
						tradeInvites.put(player1, player);
						
						notify(player1, player.getName() + " wants to trade with you.");
						notify(player1, "Do you want to trade with them? (start trading with 'trade #accept')");
					}
					else send("That player is not present", client);
				}
				else send("No such player.", client);
			}
		}
	}

	/**
	 * TRAVEL
	 * 
	 * travel <landmark -- known location>
	 * 
	 * Note: credit for ideas behind design to Ryan Hamshire
	 * (http://textgaming.blogspot.com/2011/01/updating-navigation.html)
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_travel(final String arg, final Client client) {
		/**
		 * travel               - list known landmarks
		 * travel <landmark>    - travel to a known landmark
		 * travel record <name> - start recording the steps you take as being a route to a landmark called <name>
		 * travel pause         - pause travel and/or recording
		 * travel resume        - resume travel and/or recording
		 * travel end           - end travel and/or recording
		 */
		final Player player = getPlayer(client);

		final String[] args = arg.split(" ");

		// get landmarks: if no arg, then list our landmarks (within x walking distance?)
		if ( !arg.equals("") ) {
			if( args.length == 1 ) {
				if ( arg.equals("pause") ) {}
				else if ( arg.equals("resume") ) {}
				else if ( arg.equals("end") ) {}
			}
			else if (args.length == 2) {
				final String param = args[0].toLowerCase();
				final String name = args[1];

				if ( param.equals("record") ) {
				}
			}
			else {
				final Map<String, Landmark> landmarks = player.getLandmarks();
				
				final String arg_lc = arg.toLowerCase();

				// check to see if we have whatever was suggested
				if ( landmarks.containsKey(arg_lc) ) {
					String route = null;

					// if so, either calculate a route , or use a pre-specified one
					final Landmark lm = landmarks.get(arg_lc);

					for (final String r : lm.getRoutes()) {
						String[] t = r.split(":");
						String[] steps = t[1].split(",");

						// if the origin is our current location
						if (player.getLocation() == 0) {
							route = r;
							break;
						}
					}

					if (route != null) ; // follow route
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
		else {
			final Map<String, Landmark> landmarks = player.getLandmarks();

			if (landmarks.size() > 0) {
				// list landmarks
				send("Landmarks", client);

				send(Utils.padRight("", '-', 79), client);

				for (final Landmark landmark : landmarks.values()) {
					send(Utils.padRight(landmark.getName(), ' ', 20), client);

					for (final String route : landmark.getRoutes()) {
						send(Utils.padRight("", ' ', 25) + route);
					}
				}
			}
			else send("No recorded landmark routes.", client);
		}
	}

	/**
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_unequip(final String arg, final Client client) {
		final Player player = getPlayer(client);

		if ( arg.equals("") ) {
			send("Unequip what?", client);
		}
		else {
			// TODO alternate method than scanning all the slots?
			Slot slot = null;
			
			// TODO fix these nasty kludges here simply to allow me to do a better search on slots
			List<Item> test = new ArrayList<Item>();
			
			for (final Slot slot1 : player.getSlots().values()) {
				if ( slot1.isFull() ) {
					test.add( slot1.getItem() );
				}
			}
			
			Item item1 = MudUtils.findItem(arg, test);
			
			for (final Slot slot1 : player.getSlots().values()) {
				if ( slot1.isFull() ) {
					if( slot1.getItem() == item1 ) {
						slot = slot1;
					}
				}
			}
			
			if( slot != null ) {
				final Item item = slot.remove();
				
				player.getInventory().add( item );    

				send(item.getName() + " un-equipped (" + item.getItemType() + ")", client);
			}
		}
	}

	// unlock command (applies to lockable things)
	private void cmd_unlock(final String arg, final Client client) {
		debug("Command: unlock");

		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		debug("Player: " + player.getName());
		debug("Room:   " + room.getName());

		// only want to find exits in the same room, should this limit to visible exits too?
		Exit exit = null;
		
		boolean found_exit = false;
		
		for(final Exit e : objectDB.getExitsByRoom(room)) {
			debug("    Exit: " + e.getName());
			
			if( e.getExitType() == ExitType.DOOR ) {
				final Door d = (Door) e;
				
				System.out.println("lock: \'" + d.getName( room.getDBRef() ) + "\'");
				
				if( d.getName( room.getDBRef() ).equals(arg) ) {
					exit = d;
					found_exit = true;
				}
			}
			else if( e.getName().equals(arg) ) {
				exit = e;
				found_exit = true;
			}
			
			if( found_exit ) break;
		}
		
		if (exit != null) {
			if( exit instanceof Lockable ) {
				final Lockable<Item> l = (Lockable<Item>) exit;

				if ( l.isLocked() ) {
					if( l.requiresKey() ) {
						boolean hasKey = hasKey(player, l);
						
						if( hasKey ) {
							l.unlock();
							
							send("You unlock " + exit.getName() + " with " + l.getKey().getName() + ".", client);
							//send("You unlocked " + exit.getName() + ".", client);
						}
					}
					else {
						l.unlock();
						send("You unlock " + exit.getName() + ".", client);
						//send("You unlocked " + exit.getName() + ".", client);
					}
				}
				else send("It's already unlocked.", client);
				//else send("That's already unlocked.", client);
			}
			else {
				//send(obj.getName() + "not lockable.", client);
				send("It's a good thing no one saw you trying to lock a " + exit.getExitType().toString() + " with no lock.", client);
			}
		}
		else send("Unlock what?", client);
	}

	/**
	 * 
	 * use only with plain old exits (STD)
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_unlink(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		// really we should consider whether a string or int is better to search by and if the exit must be in
		// the same room as we are
		final Exit exit = getExit(arg);

		if( exit != null ) {
			final int oldDestination = exit.getDestination();

			final Room oldDest = getRoom(oldDestination);
			final String odName = oldDest.getName();
			final int odDBRef = oldDest.getDBRef();

			exit.setDestination(-1);
			send("Unlinked " + exit.getName() + " from " + odName + " (#" + odDBRef + ")", client); 
		}
		else send("Error: No such exit.", client);
	}

	/**
	 * TODO write description
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_use(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom(player.getLocation());

		MUDObject m = getObject(arg);
		
		// assume that no argument implies something I am wearing..
		if ( arg.equals("") ) {
			debug("Game> Arguments?");

			// since there weren't any arguments, look through the player's equipment

			// Player Equipment
			for (Entry<String, Slot> eq_entry : player.getSlots().entrySet()) {
				final Slot slot = eq_entry.getValue();

				// check that the slot is valid and there is something in it
				if ( slot != null && slot.isFull() ) {
					// TODO is this the right way?
					debug("Slot (" + slot.getItemType().getName() + "): " + slot.isFull());
					
					// if the slot holds rings
					if ( slot.isType(ItemTypes.RING) ) {
						final Item item = slot.getItem();

						//item.isType(TypeFlag)
						if (item instanceof Jewelry) {
							debug("Item is Jewelry");

							final Jewelry j = (Jewelry) item;

							//j.use("", client);
							
							// TODO some kind of check here before applying effect?
							System.out.println( applyEffect( player, j.getEffect() ) );
						}
					}
				}

			}
		}
		else {
			debug("Game> Arguments Received.");
			
			if( module != null) {
				if( module.use( player, m ) ) return;
			}

			// Inventory [Item(s)]
			Item item = null;

			for (final Item e : player.getInventory()) {
				if (e.getName().equals(arg)) {
					item = e;
					break;
				}
			}

			if (item != null) {
				try {
					if (item instanceof Potion) {
						// potion handling
						use_potion((Potion) item, client);
					}
					else if (item instanceof Wand) {
						// wand handling
						use_wand((Wand) item, client);
					}
				}
				catch (final NullPointerException npe) {
					System.out.println("--- Stack Trace ---");
					npe.printStackTrace();
				}

				return;
			}

			// Room [Thing(s)]
			final List<Thing> things = objectDB.getThingsForRoom( room );
			Thing t = null;

			for (final Thing e : things) {
				if (e.getName().equals(arg)) {
					t = e;
					break;
				}
			}

			if (t != null) {
				try {
					final String result = pgm.interpret(t.getScript(TriggerType.onUse), player, t);
					
					if (!result.equals("")) send(result, client);
				}
				catch (final NullPointerException npe) {
					//TODO should the below (not stack trace) be a debug call?
					System.out.println("Arguments: \'" + arg + "\'");
					System.out.println("--- Stack Trace ---");
					npe.printStackTrace();
				}

				return;
			}

			send("No such object.", client);
		}
	}
	
	private void cmd_version(final String arg, final Client client) {
		send(getName() + " " + getVersion(), client);
	}

	/**
	 * TODO write description
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_vitals(final String arg, final Client client) {
		Player player = getPlayer(client);
		
		final StringBuilder sb = new StringBuilder();
		
		// tell us how many hitpoints and how much mana we have
		sb.append("HP: " + player.getHP() + "/" + player.getTotalHP()).append(" ");
		sb.append("MANA: " + player.getMana() + "/" + player.getTotalMana()).append(" ");
		
		/*
		sb.append(colors("HP", "red")).append(":").append(" ");
		sb.append(player.getHP() + "/" + player.getTotalHP()).append(" ");
		sb.append(colors("MANA", "blue")).append(":").append(" ");
		sb.append(player.getMana() + "/" + player.getTotalMana()).append(" ");
		*/
		
		// refresh player state info
		player.updateCurrentState();

		// indicate whether we're alive or not
		switch (player.getState()) {
		case ALIVE:         sb.append(colors("ALIVE", "green"));          break;
		case INCAPACITATED: sb.append(colors("INCAPACITATED", "yellow")); break;
		case DEAD:          sb.append(colors("DEAD", "red"));             break;
		default:            break;
		}
		
		// FULL, HIGH, MED, LOW, DEPLETED
		
		send(sb.toString(), client);
		
		sb.delete(0,  sb.length());
	}

	/*
	 * private void cmd_wear(final String arg, final Client client) { Player
	 * player = getPlayer(client); // get the item in question List<Item> items
	 * = player.getInventory();
	 * 
	 * for(Item item : items) { if(item instanceof Wearable &&
	 * item.getName().equals( arg )) { player.wear( (Wearable) item ); return; }
	 * } }
	 */
	
	// TODO for my own sake, please, please make a utility function to load log files... resolvePath(...) out the wazoo
	/**
	 * Allow you to ask to have an arbitrary part of the current log file be
	 * printed out to the screen for viewing purposes.
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_viewlog(final String arg, final Client client) {
		// parse arguments
		final List<String> args = Arrays.asList(arg.split(" "));
		
		System.out.println(args);

		String command = "";
		String[] params = new String[0];

		boolean validCNP = false;

		try {
			command = args.get(0);
			params = Utils.listToStringArray(args.subList(1, args.size()));

			validCNP = true;
		}
		catch(final IndexOutOfBoundsException ioobe) {
			ioobe.printStackTrace();
		}

		if( validCNP ) {
			// TODO this is a kludge that restricts log viewing to the main log...
			final Log log = logger.getLog("log");
			
			if ( command.startsWith("+") ) {
				command = command.substring(1);

				if (command.equals("info")) {
					send("Log File: " + resolvePath(LOG_DIR, log.getFileName()), client);
					send("Log Size: " + log.getLinesWritten(), client);
				}
				else if (command.equals("lines")) {
					List<String> lines;

					if (params.length >= 2) {
						final int start = Utils.toInt(params[0], 0);
						final int end = Utils.toInt(params[1], log.getLinesWritten());

						lines = Utils.loadList(resolvePath(LOG_DIR, log.getFileName()), start, end);
					}
					else {
						lines = Utils.loadList( resolvePath(LOG_DIR, log.getFileName()) );
					}

					send(lines, client);
				}
				else if ( command.equals("size") || command.equals("sz") ) {
					send("Log Size: " + log.getLinesWritten(), client);
				}
				else if ( command.equals("since") || command.equals("sc") ) {
					List<String> lines;

					if (params.length >= 1) {
						final Time time = Time.fromString(params[0]);
						
						lines = Utils.loadList( resolvePath(LOG_DIR, log.getFileName()) );

						int startIndex = 0;

						int index = -1;
						
						// find line to start at by iterating until the timestamp is later than the specified time
						for (final String line : lines) {
							index++;
							
							String temp = line.split(" ")[0];
							
							String timeString = temp.substring(1, temp.length() - 1); // trim [] off
							
							Time time1 = Time.fromString(timeString);

							if (time1.hour >= time.hour && time1.minute >= time.minute && time1.second >= time.second) {
								startIndex = index;
								break;
							}
						}
						
						send(lines.subList(startIndex, lines.size()), client);
					}
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

				// npc.interact(0);
			}
		}
	}

	private void cmd_value(final String arg, final Client client) {
		final Player player = getPlayer(client);

		final Item item = MudUtils.findItem(arg, player.getInventory());

		send(item.getName() + " " + item.getValue(), client);
	}

	// TODO 'wield' command?

	/**
	 * list player locations
	 * 
	 * COMMAND OBJECT EXISTS, not in use though
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_where(final String arg, final Client client) {
		// send("Player     Class     S Race      Idle Status Location", client);
		// send("Player     Class     S Race      Idle Location", client);

		List<String> output = new LinkedList<String>();

		StringBuilder sb = new StringBuilder();

		int n = 0;

		output.add(Utils.padRight("", '-', 78));
		output.add("Player     Class     S Race      Idle     Location");
		output.add(Utils.padRight("", '-', 78));

		for (final Player player : players) {
			try {
				String name = player.getName();                    // (char limit: 10 ?)
				String cname = player.getCName();                  //
				String playerClass = player.getPClass().getName(); //
				String playerGender = "" + player.getGender().charAt(0);
				String race = player.getRace().toString();         //
				
				int location = player.getLocation();               // set room # limit to 5 characters (max. 99999)
				
				String locString = "";
				
				final Room room = getRoom(location);

				if ( player.hasEffect("invisibility") ) {
					locString = "INVISIBLE";
				}
				else {
					if ( room.hasFlag(ObjectFlag.DARK) ) {
						Zone zone = room.getZone();

						if (zone != null) locString = zone.getName();
						else              locString = room.getName();
					}
					else {
						locString = room.getName() + " (#" + location + ")";
					}
				}

				String idle = MudUtils.getIdleString( player.getIdleTime() );

				Player current = getPlayer(client);

				if (current.getNames().contains(name) || current.getName().equals(name)) {
					sb.append(Utils.padRight(name, 10)).append(" ");
					sb.append(Utils.padRight(playerClass, 9)).append(" ");
					sb.append(Utils.padRight(playerGender, 1)).append(" ");
					sb.append(Utils.padRight(race, 9)).append(" ");
					sb.append(Utils.padRight(idle, 8)).append(" ");
					sb.append(locString);

					output.add(sb.toString());
				}
				else {
					sb.append(Utils.padRight(cname, 10)).append(" ");
					sb.append(Utils.padRight(playerClass, 9)).append(" ");
					sb.append(Utils.padRight(playerGender, 1)).append(" ");
					sb.append(Utils.padRight(race, 9)).append(" ");
					sb.append(Utils.padRight(idle, 8)).append(" ");
					sb.append(locString);

					output.add(sb.toString());
				}

				n++;
			}
			catch (final NullPointerException npe) {
				debug("cmd_where(): one of the pieces of player information is null... (null pointer exception)");
				System.out.println("--- Stack Trace ---");
				npe.printStackTrace();
			}

			sb.delete(0, sb.length());
		}

		output.add(Utils.padRight("", '-', 78));
		output.add(n + " players currently online.");

		send(output, client);
	}

	// Function to list player locations
	private void cmd_who(final String arg, final Client client) {
		// TODO Is there any reason for the WHO command to care whether you know someone's name?
		if (!arg.equals("") ) {

			if (loginCheck(client) && checkAccess(getPlayer(client), Constants.ADMIN)) {
				final Player player = getPlayer(arg);

				String name = player.getName(); // need to limit name to 10 characters
				String cname = player.getCName();

				if (player != null) {
					if (player.getNames().contains(name) || getPlayer(client).getName().equals(name)) {
						send(player.getName() + " Located: " + player.getLocation(), client);
					}
					else {
						send(cname + " Located: " + player.getLocation(),client);
					}
				}
			}
		}
		else {
			int n = 0;

			for (final Player player : players) {
				try {
					String name = player.getName();   // limit name to 10 characters?
					String cname = player.getCName();
					String title = player.getTitle(); // limit title to 8 characters
					String race = player.getRace().toString();

					StringBuilder sb = new StringBuilder();

					// [ level class ] name - specialty/prestige class - group/guild
					// (race)
					sb.append(colors("[", "blue"));
					sb.append(player.getLevel() + "");
					sb.append(' ');
					sb.append(colors(player.getPClass().getAbrv(), player.getPClass().getColor()));
					sb.append(colors("]", "blue"));
					sb.append(' ');

					// name
					if (loginCheck(client)) {
						if (player.getNames().contains(name) || getPlayer(client).getName().equals(name)) {
							sb.append(name);
						}
						else {
							if( use_cnames )  sb.append(cname);
							else              sb.append( Utils.padRight("", '?', 3) );
						}
					}
					else {
						if( use_cnames )  sb.append(cname);
						else              sb.append( Utils.padRight("", '?', 3) );
					}

					// title
					if (!title.equals("")) { // if title isn't empty
						sb.append(' ');
						sb.append("\'" + title + "\'");
					}

					// race
					sb.append(' ');
					sb.append("(" + colors(race, "magenta") + ")");
					sb.append("\r");
					sb.append('\n');

					client.write(sb.toString());

					// count players
					n++;
				}
				catch (final NullPointerException npe) {
					debug("cmd_who(): some piece of player information is null (null pointer exception)");
					System.out.println("--- Stack Trace ---");
					npe.printStackTrace();
				}
			}

			send(n + " players currently online.", client);
		}
	}
	
	private void cmd_withdraw(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		/*String str = null;

		try {
			str = room.getProperty("_game/isBank");
		}
		catch(final ClassCastException cce) {
			cce.printStackTrace();
			str = "false";
		}*/

		String str = room.getProperty("_game/isBank");

		final Boolean isBank = str.equals("true") ? true : false;

		if( isBank ) {
			final Bank bank = getBank( room.getProperty("_game/bank/name") );
			final BankAccount acct = bank.getAccount(0);

			if ( bank != null ) {
				if( acct != null ) {
					int amount = Utils.toInt(arg, 0);

					if( amount > 0 ) {
						if( acct.getBalance().numOfCopper() >= amount ) {
							final Coins withdrawal = acct.withdraw( Coins.gold(amount) );

							player.setMoney( player.getMoney().add(withdrawal) );

							send("You withdraw " + amount + " gold.", client);
						}
						else send("You don't have that much in the bank.", client);
					}
					else send("I don't think we accept that kind of currency.", client);

				}
				else {
					send("You don't have an account.", client);
				}
			}
			else {
				send("No such bank?!", client);
			}
		}
		else send("You aren't in a bank.", client);;
	}

	private void cmd_zoneedit(final String arg, final Client client) {
		Player player = getPlayer(client);
		String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT); // set the 'edit' status flag
		player.setEditor(Editors.ZONE); // zone editor

		EditorData newEDD = new EditorData();

		Zone zone = null;

		if ( arg.equals("new") ) {
			zone = new Zone("New Zone", null);
		}
		else {
			if (arg.charAt(0) == '#') {
			}
			else {
				zone = getZone(arg);
			}
		}

		if (zone != null) {
			// record prior player status
			newEDD.addObject("pstatus", old_status);

			// editable zone data
			newEDD.addObject("zone", zone);
			newEDD.addObject("name", zone.getName());

			//
			player.setEditorData(newEDD);

			// newEDD
			op_zoneedit("show", client);
		}
		else {
			send("No such zone!");
			// abort editing..
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
		final Zone zone = getZone(getPlayer(client));

		if (zone != null) {
			final Integer zoneID = zone.getId();
			final String zoneName = zone.getName();

			// [ 4 ] Test Zone ( 10 Rooms )
			send(colors("[" + zoneID + "] " + zoneName, "magenta") + " ( " + zone.getRooms().size() + " Rooms )", client);

			for (final Room room : zone.getRooms()) {
				send("- " + room.getName() + " (#" + room.getDBRef() + ")", client);
			}

			send("Quests:", client);

			for (final Quest quest : getQuestsByZone(zone)) {
				send(quest.getId() + ": " + quest.getName(), client);
			}

			final StringBuilder subzones = new StringBuilder();

			int zc = 0;

			for (final Zone z : zones.keySet()) {
				if (zone.getParent() == z) {
					subzones.append(zone.getName());

					if (zc < zones.size()) {
						subzones.append(", ");
					}
				}

				zc++;
			}

			send("Subzones: " + subzones.toString());
		}
		else send("No such zone!", client);
	}

	// @zones +new [zone name]
	// @zones +add [room name]=[zone name]
	/**
	 * Create a zone, add a room to a zone, list all zones, etc
	 * 
	 * NOTE: no room may be added to a zone if it exceeds the max zone size
	 * though it may if the zone is less than the max, at which point it's size
	 * will be increased
	 * 
	 * syntax: @zones +new [zone name]=<zone parent>, @zones +add [room to
	 * zone]=<zone parent>
	 * 
	 * <zone parent> will be either a specified parent by dbref, a default
	 * parent, one the player has set beforehand for themselves or a default
	 * zone in the case that it is not specified
	 * 
	 * DEBUG: need to debug this code and make sure there aren't any logical or
	 * coding errors
	 * 
	 * @param arg
	 * @param client
	 */
	private void cmd_zones(final String arg, final Client client) {
		final String[] params = arg.split(" ");

		debug("# Params: " + params.length);

		/*
		 * if (!arg.equals("") && params.length == 1) { if
		 * (params[0].equals("+new")) { Zone zone = new Zone(params[0], null);
		 * zones.put(zone, 10); // store a new zone object
		 * send("New Zone Established!", client); // tell us that it succeeded.
		 * } }
		 */
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
						int dbref = Utils.toInt(args[0], -1);

						if (dbref == -1) {
							send(gameError("@zones", ErrorCodes.NOT_A_NUMBER), client);
						}

						final Room room = getRoom(dbref);
						final Zone zone = getZone(args[1]);

						if (room != null) {
							if (zone != null) {
								MudUtils.addRoomToZone(zone, room);

								send(room.getName() + " added to " + zone.getName(), client);
							}
							else send("No such zone!", client);
						}
						else send("Invalid Room!", client);
					}
				}
			}
			else if (params[0].equals("+info")) {
				// TODO this parameter is redundant with the zoneinfo command

				// need to fix this so that the portion of this kind of argument
				// '+info Red Dragon Inn' gets
				// zoined back into a single string
				String s = Utils.join(Arrays.copyOfRange(params, 1, params.length), " ");

				final Zone zone = getZone(s);

				if (zone != null) {
					final Integer zoneID = zone.getId();
					final String zoneName = zone.getName();

					final Integer zoneSize = zone.getRooms().size();

					// send("Zone - " + zone.getName() + "(" + zone.getId() + ")", client);

					// send("" + zone.getName() + " ( " + zone.getRooms().size() + " Rooms )", client);
					send("" + colors(zoneName + " [" + zoneID + "] ", "purple") + " ( " + zoneSize + " Rooms )", client);

					for (final Room room : zone.getRooms()) {
						send("- " + room.getName() + " (#" + room.getDBRef() + ")", client);
					}

					send("Quests:", client);

					for (final Quest quest : getQuestsByZone(zone)) {
						send(quest.getId() + ": " + quest.getName(), client);
					}

					final StringBuilder subzones = new StringBuilder();

					int zc = 0;

					for (final Zone zone1 : zones.keySet()) {
						if (zone1.getParent() == zone) {
							subzones.append(zone1.getName());

							if (zc < zones.size()) {
								subzones.append(", ");
							}
						}

						zc++;
					}

					send("Subzones: " + subzones.toString());
				}
			}
		}
		else {
			send("Zones:", client);
			//debug(zones.entrySet());

			for (Zone zone : zones.keySet()) {
				final Integer zoneID = zone.getId();
				final String zoneName = zone.getName();

				send("" + colors(zoneName + " [" + zoneID + "] ", "purple") + " ( " + zone.getRooms().size() + " Rooms )", client);

				for (final Room room : zone.getRooms()) {
					send("- " + room.getName() + " (#" + room.getDBRef() + ")", client);
				}
			}
		}
	}

	// used for help files
	/**
	 * Check
	 * 
	 * Evaluates any scripting in the specified text. Useful for handling places
	 * where you'd like color
	 * 
	 * NOTES: Not particularly robust, used for help files
	 * 
	 * @param in
	 * @return
	 */
	private String check(final String in) {
		boolean doEval = false;
		
		final ProgramInterpreter pgmi = getProgramInterpreter();

		StringBuilder result = new StringBuilder();
		StringBuilder work = new StringBuilder();

		char ch;

		for (int c = 0; c < in.length(); c++) {
			ch = in.charAt(c);

			switch (ch) {
			case '{':
				if (!doEval) {
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
				if (doEval) {
					doEval = false;
					work.append(ch);
					
					final Script script = new Script( work.toString() );

					result.append( pgmi.interpret(script, null, null) );
				}
				break;
			default:
				if (doEval) work.append(ch);
				else        result.append(ch);

				break;
			}
		}

		return result.toString();
	}

	// Inline Status bar
	/**
	 * Draws a status bar every time on calling the function shows the players
	 * current hitpoints and mana. Maybe I could strap this to a timing loop to
	 * get it to show repeatedly.
	 * 
	 * @param client
	 */
	public void prompt(final Client client) {
		if (prompt_enabled && getPlayer(client).getConfigOption("prompt_enabled")) {
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

		final Player player = getPlayer(client);

		String output = pattern;

		final String playerMode = player.getMode().toString();

		final String hp = ((Integer) player.getHP()).toString();
		final String max_hp = ((Integer) player.getTotalHP()).toString();

		final String mana = ((Integer) player.getMana()).toString();
		final String max_mana = ((Integer) player.getTotalMana()).toString();

		final String playerState = player.getState().toString();

		output = output.replace("%mode", playerMode);

		output = output.replace("%h", hp);
		output = output.replace("%H", max_hp);

		output = output.replace("%m", mana);
		output = output.replace("%M", max_mana);

		output = output.replace("%state", playerState);

		send(output, client);
		// addMessage(new Message(output, player));
	}

	// Command Support Functions

	/**
	 * Chat Handler
	 * 
	 * @param channel the name of the channel we are writing to
	 * @param arg     the message we are writing
	 * @param client  who is writing
	 */
	public void chatHandler(final String channelName, final String arg, final Client client) {
		final Player player = getPlayer(client);
		
		if( chan.hasChannel(channelName) ) {
			boolean writeSuccess = chan.send(channelName, player, arg); // send chat message
			
			if( writeSuccess ) {
				logChat("(" + channelName + ") <" + player.getName() + "> " + arg); // log the sent chat message
			}
			else {
				send("Chat> Error: you don't have access to that channel.", client);
			}
		}
		else if( channelName.equals("party") ) { // special case: #party
			String temp = "";
			
			// try to find a party channel that the player belongs to
			for(final ChatChannel channel : chan.getChatChannels()) {
				final String chanName = channel.getName();
				
				if( chanName.startsWith("party_") ) {
					if( channel.isListener(player) ) {
						temp = chanName;
						break;
					}
				}
			}
			
			// could also just grab their party and pull that specific channel...
			/*final Party party = getPartyContainingPlayer(player);
			
			if( party != null ) {
				party.getChannel().write("");
			}*/
			
			boolean writeSuccess = chan.send(temp, player, arg); // send chat message
			
			if( writeSuccess ) {
				logChat("(" + temp + ") <" + player.getName() + "> " + arg); // log the sent chat message
			}
		}
		else {
			send("Chat> No such channel.", client);
		}
	}
	
	/**
	 * Exit Handler
	 * 
	 * TODO unkludge ?
	 * 
	 * This version of the handler handles any exit name resolution before
	 * passing the appropriate exit name to the actual handler
	 * 
	 * @param cmd
	 * @param client
	 * @return
	 */
	public boolean exitHandler(final String cmd, final Client client) {
		final Player player = getPlayer(client);           // get current player
		final Room room = getRoom( player.getLocation() ); // get current room

		debug("Entering exit handler (String)...");

		for (final Exit exit : room.getExits()) {
			// for doors
			final String[] doorNames = exit.getName().split("/");

			// checks for doorness and at the same time whether the given command is a valid exit name for the door
			//boolean door = (doorNames.length == 2) ? ((doorNames[0].equals(cmd) || doorNames[1].equals(cmd)) ? true : false) : false;
			//boolean door_alias = (doorNames.length == 2) ? (exit.hasAlias(doorNames[0] + "|" + cmd) || exit.hasAlias(doorNames[1] + "|" + cmd)) : false;
			
			boolean door = false;
			boolean door_alias = false;
			
			if ( doorNames.length == 2 ) {
				// door
				if ( doorNames[0].equals(cmd) || doorNames[1].equals(cmd) ) door = true;
				
				// door_alias
				if ( exit.hasAlias(doorNames[0] + "|" + cmd) || exit.hasAlias(doorNames[1] + "|" + cmd) ) door_alias = true;
			}

			debug("Command: " + cmd);
			debug("door [B]: " + door);
			debug("door_alias [B]: " + door_alias);

			if (exit.getExitType() == ExitType.DOOR) {
				debug("\'" + doorNames[0] + "|" + cmd + "\'");
				debug("\'" + doorNames[1] + "|" + cmd + "\'");
			}

			debug("Aliases: " + exit.getAliases());
			
			final String exitName = exit.getName();

			if (exitName.equals(cmd) || exitName.equals( aliases.get(cmd) ) || exit.getAliases().contains(cmd) || door || door_alias) {
				System.out.println("Exit: " + exit.getName() + " (#" + exit.getDBRef() + ")");
				return exitHandler(exit, client);
			}
		}

		// tell us we are leaving the exit handler
		debug("Exiting exit handler...");

		return false;
	}

	/**
	 * kind of a kludge
	 * 
	 * NOTE: for now, we should make sure not to pass NULL exits to this... (4/24/2015)
	 * NOTE: this is used for directional exits (6/4/2015)
	 */
	public boolean exitHandler(final Exit exit, final Client client) {
		final Player player = getPlayer(client);           // get current player
		final Room room = getRoom( player.getLocation() ); // get current room

		debug("Entering exit handler (Exit)...");

		boolean canUse = false;

		if (exit.getExitType() == ExitType.DOOR && exit instanceof Door) {
			final Door d = (Door) exit;

			if ( !d.isLocked() ) canUse = true;
			else                 send("The door is locked.", client);
		}
		else if (exit.getExitType() == ExitType.PORTAL && exit instanceof Portal) {
			final Portal p = (Portal) exit;

			if ( p.isActive() ) {
				canUse = true;
			}
			else {
				if ( p.requiresKey() && p.hasKey(player) ) {
					// TODO should we activate the portal here
					// activate portal
					canUse = true;
				}
			}
		}
		else {
			canUse = true;
		}
		
		// TODO need to resolve the possibility of an invalid destination here
		
		if (canUse) { // exit lock check?
			debug("success");

			// send the success message
			if (!exit.getMessage("succMsg").equals("")) {
				// TODO check where these messages end up getting sent to
				addMessage( new Message(null, player, exit.getMessage("succMsg")) );
				addMessage( new Message(null, player, "You leave the room.") );
			}

			// execute leave triggers
			for (final Trigger trigger : room.getTriggers(TriggerType.onLeave)) {
				System.out.println(trigger);
				
				// TODO need new way to send text here if the trigger result isn't a script
				// execTrigger(trigger, room, client);
				send(pgm.interpret(trigger.getScript(), getPlayer(client), room), client);
			}

			// send other exit properties
			//send("Exit Type: " + exit.getExitType().getName(), client);
			debug("Exit Type: " + exit.getExitType().getName());

			// set player's location
			if (exit.getExitType() == ExitType.DOOR) {
				Door d = (Door) exit;

				int dbref = room.getDBRef();

				int orig = d.getLocation();
				int dest = d.getDestination();

				if (dbref == orig)      player.setLocation(dest);
				else if (dbref == dest) player.setLocation(orig);
				
				System.out.print("Player location (NEW): " + player.getLocation());
			}
			else {
				player.setLocation( exit.getDestination() );
				System.out.print("Player location (NEW): " + exit.getDestination()); 
			}

			// remove listener from room
			room.removeListener(player);

			// send the osuccess message
			if (!exit.getMessage("osuccMsg").equals("")) {
				// TODO check where these messages end up getting sent to
				addMessage( new Message(exit.getMessage("osuccMsg"), room) );
				addMessage( new Message(player.getName() + " left the room.") ); // TODO fix kludge
			}
			
			// cancel this player's current trade, if they are in one
			final Trade trade = trades.get(player);

			if( trade != null ) {
				trades.remove( trade.p1 );
				trades.remove( trade.p2 );

				notify(trade.p1, "Your trade with " + trade.p2.getName() + " was canceled.");
				notify(trade.p2, "Your trade with " + trade.p1.getName() + " was canceled.");
			}

			// get new room object
			final Room room1 = getRoom( player.getLocation() );

			// add listener to room
			room1.addListener(player);
			
			// TODO what is the purpose of the 'outside' list
            if (room.getRoomType() == RoomType.OUTSIDE && !outside.contains(player)) {
                outside.add(player);
            }
            else outside.remove(player);
            
			// execute enter triggers
			for (final Trigger trigger : room1.getTriggers(TriggerType.onEnter)) {
				System.out.println(trigger);
				
				// TODO need new way to send text here if the trigger result isn't a script
				// execTrigger(trigger, room, client);
				send(pgm.interpret(trigger.getScript(), getPlayer(client), room1), client);
			}

			// call msp to play a tune that is the theme for a type of room
			// MSP is turned on (server) and enabled (player)
			if (msp == 1 && player.getConfigOption("msp_enabled")) {
				// if inside play the room's music
				if (room.getRoomType().equals(RoomType.INSIDE)) {
					playMusic("tranquil.wav", client);
				}
				// if outside, play appropriate weather sounds?
				else if (room1.getRoomType().equals(RoomType.OUTSIDE)) {
					// perhaps simply setting a pattern of some kind would be good?
					// in case we wish to have an ambient background (rain, wind) and an effect sound for lightning (thunder)

					// ASIDE: some clients only support one sound, so an effect
					// sound should be handled as the sound, and then in the ambient background
					
					final String wsName = room1.getWeather().getState().getName();
					
					// get weather, then play related sound
					switch (wsName) {
					case "Rain":        playMusic("rain.wav", client); break;
					case "Cloudy":      playMusic("rain.wav", client); break;
					case "Clear Skies": playMusic("rain.wav", client); break;
					default:            break;
					}
				}
			}

			// show the description
			look(room1, client);

			// tell us we are leaving the exit handler
			debug("Exiting exit handler...");
		}

		return true;
	}

	/* Editors */

	/**
	 * Interactive Casting "Editor"
	 * 
	 * a.k.a 'Interactive Spell Mode'
	 * 
	 * a list editor like system that allows you to choose a spell from your
	 * spellbook/ memorized spells (flag memorized ones with color or some other
	 * way to indicate availablity). you can also choose a target and indicate
	 * any special criteria
	 * 
	 * will allow you to set up a series of spells to cast sequentially so you
	 * don't have to set them up or allow you to construct a more complicated
	 * spell with parameters without having to 'say' them to the game.
	 * 
	 * @param input
	 * @param client
	 */
	public void op_cast(final String input, final Client client) {
		if (input.indexOf(".") != -1) {
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
				/*
				 * select takes a spell name as an argument
				 */
				if (sarg.equals("")) {
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

				if (sb != null) {
					for (int level = 0; level <= getPlayer(client).getLevel(); level++) {
						List<Spell> spells = sb.getSpells(level);

						if (spells != null) {
							send("-----------------", client);
							
							send("Level: " + level, client);
							
							for (final Spell spell : spells) {
								send(" " + spell.getName(), client); // space
								// for
								// indenting
								// purposes
							}
							
							send("-----------------", client);
						}
					}
				}
				// send("-----------------", client);
				// send("Level 1:", client);
				// send(" dispel", client);
				// send(" fireball", client);
				// send(" invisibility", client);
				// send("-----------------", client);
			}
			else if (scmd.equals("finalize")) {
				/*
				 * finalize and init spell casting, slot into current
				 * "round"/present time or next available "round"/time
				 * automatically
				 */
				send("Interactive Spell Mode> Finalizing...", client);
			}
			else if (scmd.equals("cancel")) {
				// tell us
				send("Interactive Spell Mode Canceled.", client);

				// clear queue
				getPlayer(client).getSpellQueue().clear();

				// reset editor
				getPlayer(client).setEditor(Editors.NONE);

				// reset status
				getPlayer(client).setStatus("OOC");
			}
			else if (scmd.equals("quit")) {}
			else if (scmd.equals("target")) {}
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
		if (!player.getName().startsWith("Guest") && player.isNew()) {
			cgData cgd = null;

			if (input.equals("start")) { // if the input indicates we are to start character generation
				// create new character generation data (cgd) object and populate with values
				cgd = new cgData(0, 1, 0, false);

				debug("T: " + cgd.t + " Step: " + cgd.step + " Answer: "+ cgd.answer);

				cg_data.put( player, op_chargen("", client, cgd) );
			}
			else {
				// basically if we aren't just starting call op_chargen(...) with input and existing data
				cg_data.put( player, op_chargen(Utils.trim(input), client, cg_data.get( player )) );
			}

			// make sure we advance to the answer phase provided that the data in question exists?
			if (cgd != null) {
				cg_data.put( player, op_chargen("", client, cg_data.get( player )) );
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

		int t = cgd.t;           // question or answer?
		int step = cgd.step;     // the step of chargen we are on
		int answer = cgd.answer; // the player's answer to the current input request
		boolean edit = cgd.edit; // are we editing (i.e. coming back after first run through and changing things)

		debug("Start: T is now " + t);

		if (t == 0) {
			// send("Step: " + step, client);
			debug("Step: " + step);

			switch (step) {
			case 1:
				if (edit) send("Player Race: " + player.getRace(), client);

				send("Please choose a race:", client);

				int si = 0;
				int n = 0;

				final StringBuilder sb = new StringBuilder();

				for (final Race race : races) {
					if (si < 2) {
						sb.append("" + n + ")" + Utils.padRight(race.getName(), 8) + " ");

						si++;
					}
					else if (si < 3) {
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

				// send("1) " + Utils.padRight("" + Races.ELF, 6) + " 2) " +
				// Utils.padRight("" + Races.HUMAN, 6) + " 3) " +
				// Utils.padRight("" + Races.DWARF, 6), client);
				// send("4) " + Utils.padRight("" + Races.GNOME, 6) + " 5) " +
				// Utils.padRight("" + Races.ORC, 6) + " 6) " +
				// Utils.padRight("" + Races.HALF_ELF, 6), client);

				break;
			case 2:
				if (edit) send("Player Gender: " + player.getGender(), client);

				send("Please choose a gender:", client);
				send("1) Female 2) Male 3) Other 4) Neuter (no gender)", client);

				break;
			case 3:
				if (edit) send("Player Class: " + player.getPClass(), client);

				send("Please choose a class:", client);

				// TODO Fix Hardcoded class options?

				if (module != null && module.hasClasses()) {
					final PClass[] classes = module.getRuleset().getClasses();

					final StringBuilder sb1 = new StringBuilder();

					int choice = 1;
					int count = 0;

					int index = 1;

					// deliberately skipping first element of array (index = 0)
					// because it should be NONE which isn't a valid choice
					while (index < classes.length) {
						PClass pcl = classes[index];

						// 1,2,3, 4!
						if (count < 3) {
							if (count < 2) sb1.append(" " + choice + ") " + Utils.padRight("" + pcl.getName(), 12));
							else           sb1.append(choice + ") " + Utils.padRight("" + pcl.getName(), 12));

							choice++;
							count++;

							index++;
						}
						else {
							send(sb1.toString(), client);
							sb1.delete(0, sb1.length());

							count = 0;
						}
					}
				}
				else {
					send(" 1) " + Utils.padRight("" + Classes.BARBARIAN, 12) + " 2) " + Utils.padRight("" + Classes.BARD, 12) + " 3) " + Utils.padRight("" + Classes.CLERIC, 12), client);
					send(" 4) " + Utils.padRight("" + Classes.DRUID, 12) + " 5) " + Utils.padRight("" + Classes.FIGHTER, 12) + " 6) " + Utils.padRight("" + Classes.MONK, 12), client);
					send(" 7) " + Utils.padRight("" + Classes.PALADIN, 12) + " 8) " + Utils.padRight("" + Classes.RANGER, 12) + " 9) " + Utils.padRight("" + Classes.ROGUE, 12), client);
					send("10) " + Utils.padRight("" + Classes.SORCERER, 12) + "11) " + Utils.padRight("" + Classes.WIZARD, 12) + " 0) " + Utils.padRight("" + Classes.NONE, 12), client);
				}

				break;
			case 4:
				if (edit) send("Player Alignment: " + player.getAlignment(), client);

				send("Please select an alignment:", client);

				for (int i = 1; i < 9; i = i + 3) {
					send("" + i + ") " + Utils.padRight("" + Alignments.values()[i], ' ', 14) + " " + (i + 1) + ") " + Utils.padRight("" + Alignments.values()[i + 1], ' ', 14) + " " + (i + 2) + ") " + Utils.padRight("" + Alignments.values()[i + 2], ' ', 14), client);
				}

				break;
			case 5:
				send(Utils.padRight("Race: ", ' ', 8) + player.getRace(), client);
				send(Utils.padRight("Gender: ", ' ', 8) + player.getGender(), client);
				send(Utils.padRight("Class: ", ' ', 8) + player.getPClass(), client);
				send(Utils.padRight("Align: ", ' ', 8) + player.getAlignment(), client);
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
				// if there is input
				if (!input.equals("")) answer = Integer.parseInt(input);
				// return the data unchanged
				else                   return cgd;

				// send("Answer: " + answer, client);
				debug("Answer: " + answer);

				// send("Entering Step " + step, client);
				debug("Entering Step " + step);

				if (step == 1) {
					// player.setRace(Races.getRace(answer));
					player.setRace(races.get(answer));

					send("You have chosen to be an " + player.getRace(), client);
					send("Player Race set to: " + player.getRace(), client);

					/*
					 * Ability[] ab = new Ability[] { Abilities.STRENGTH,
					 * Abilities.DEXTERITY, Abilities.CONSTITUTION,
					 * Abilities.INTELLIGENCE, Abilities.WISDOM,
					 * Abilities.CHARISMA };
					 */

					Ability[] ab = rules.getAbilities();

					int index = 0;

					// apply permanent stat adjustments according to race
					for (int value : (Races.getRace(answer)).getStatAdjust()) {
						player.setAbility(ab[index], player.getAbility(ab[index]) + value);
						index++;
					}

					send("", client);

					step++;

					if (edit) {
						edit = false;
						step = 5;
					}
				}
				else if (step == 2) {
					switch (answer) {
					case 1:
						player.setGender("Female");
						break;
					case 2:
						player.setGender("Male");
						break;
					case 3:
						player.setGender("Other");
						break;
					case 4:
						player.setGender("None");
						break;
					default:
						player.setGender("None");
						break;
					}

					// You are female/You are male/You have chosen to be
					// genderles/You have no gender
					send("Player Gender set to: " + player.getGender(), client);

					send("", client);

					step++;

					if (edit) {
						edit = false;
						step = 5;
					}
				}
				else if (step == 3) {
					player.setPClass(Classes.getClass(answer));

					send("You have chosen to pursue being a " + player.getClass(), client);
					send("Player Class set to: " + player.getPClass(), client);
					send("", client);

					step++;

					if (edit) {
						edit = false;
						step = 5;
					}
				}
				else if (step == 4) {

					if (answer >= 1 && answer <= 9) {
						player.setAlignment(Alignments.values()[answer]);

						send("Player Alignment set to: " + player.getAlignment(), client);
						send("", client);

						step++;

						if (edit) {
							edit = false;
							step = 5;
						}	
					}
					else {
						send("Invalid Alignment Selection. Try Again.", client);
						send("", client);
					}
				}
				else if (step == 5) {
					if (answer == 1) { // Reset
						player.setRace(Races.NONE);
						player.setGender("None");
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
						// not sure whether I should do the above steps on the spot or in this function below,
						// by passing it the appropriate classes. I suppose either is doable

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
					switch (answer) {
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
			catch (final NumberFormatException npe) {
				debug("op_chargen(): invalid step number (number format exception)");
				System.out.println("--- Stack Trace ---");
				npe.printStackTrace();
			}
		}

		return new cgData(t, step, answer, edit);
	}
	
	/**
	 * Help File Editor command parser
	 * 
	 * @param input
	 * @param client
	 */
	public void op_helpedit(final String input, final Client client) {
		final Player player = getPlayer(client); // get Player
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

			if (hcmd.equals("abort") || hcmd.equals("a")) {
				player.abortEditing();

				send("< List Aborted. >", client);
				send("< Exiting... >", client);

				player.setEditor(Editors.NONE);
				player.setStatus("OOC");
			}
			else if (hcmd.equals("del") || hcmd.equals("d")) {
				if (list != null) {
					final int toDelete = Utils.toInt(harg, -1);
					list.removeLine(toDelete);
					send("< Line " + toDelete + " deleted. >", client);
				}
			}
			else if (hcmd.equals("help") || hcmd.equals("h") || hcmd.equals("?")) {
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
			else if (hcmd.equals("insert") || hcmd.equals("i")) {
				if (list != null) {
					list.setLineNum(Utils.toInt(harg, 0));
					debug("Line changed to: " + Utils.toInt(harg, 0));
				}
			}
			else if (hcmd.equals("list") || hcmd.equals("l")) {
				if (list != null) {
					int i = 0;
					for (String s : list.getLines()) {
						System.out.println(Utils.padRight(i + ": ", ' ', 5) + s);
						send(Utils.padRight(i + ": ", ' ', 5) + s, client);
						i++;
					}
				}
			}
			else if (hcmd.equals("print") || hcmd.equals("p")) {
				if (list != null) {
					for (String s : list.getLines()) {
						System.out.println(s);
						send(s, client);
					}
				}
			}
			else if (hcmd.equals("quit") || hcmd.equals("q")) {
				// save the help file?
				op_helpedit(".save", client);

				send("< Exiting... >", client);

				player.setEditor(Editors.NONE);
				player.setStatus("OOC");
			}
			else if (hcmd.equals("replace") || hcmd.equals("repl")) {
				// .replace start_line:end_line=line\line\...
				// .replace 5 7 this;is;a
				// .replace line=new text
				if( harg.matches("^(\\d+)( )(\\d+)( )((\\w+)([;]))*(\\w+)$") ) {
					String[] args = harg.split(" ");
					
					int start = Utils.toInt(args[0], -1);
					int end = Utils.toInt(args[1], -1);
					
					String[] data = args[2].split(";");
					
					// TODO doesn't test that the lines fall within the list...
					if( start != -1 && end != -1 && end > start ) {
						int n = 0;
						
						for(int l = start; start < end; l++) {
							if( n < data.length ) {
								list.setLine(l, data[n]);
								n++;
							}
							else break;
						}
					}
				}
				else send("replace: invalid arguments", client);
			}
			else if (hcmd.equals("save") || hcmd.equals("s")) {
				if (list != null) {
					// convert the list to a string array
					this.helpTable.put(list.getName(), list.getLines().toArray(new String[0]));

					send("< Help File Written Out! >", client);
					send("< Help File Saved. >", client);
				}
			}
			else if (hcmd.equals("stat") || hcmd.equals("st")) {
				if (list != null) {
					String header = "< Help File: " + list.getName() + ".txt" + " Line: " + list.getLineNum() + " Lines: " + list.getNumLines() + "  >";
					send(header, client);
				}
			}
		}
		else {
			if (list != null) {
				debug("Line: " + list.getLineNum() + " Input: " + input);

				if (list.getLineNum() < list.getNumLines()) {
					list.setLine(list.getLineNum(), input);
				} 
				else list.addLine(input);

				debug(list.getLineNum() + ": \"" + list.getCurrentLine() + "\"");
			}
		}
	}

	/**
	 * List Editor
	 * 
	 * status: buggy, it doesn't save file to disk, but does hold onto it
	 * transiently
	 * 
	 * NOTE: sort of saves lists, temporarily, but doesn't save to any kind of
	 * file. This has a long way to go before it bears any real resemblance to
	 * the functionality available on TinyMU* or NamelessMUCK.
	 * 
	 * @param input
	 * @param client
	 */
	public void op_listedit(final String input, final Client client) {
		final Player player = getPlayer(client);
		final EditList list = player.getEditList();

		debug("input: " + input);

		String lcmd = "";
		String larg = "";

		if (input.indexOf(".") == 0) {
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

			if (lcmd.equals("abort") || lcmd.equals("a"))
			{
				// exit the editor, aborting any changes made
				player.abortEditing();

				send("< List Aborted. >", client);
				send("< Exiting... >", client);

				player.setEditor(Editors.NONE);
				player.setStatus("OOC");
			}
			else if (lcmd.equals("help") || lcmd.equals("h") || lcmd.equals("?")) {
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
			else if (lcmd.equals("insert") || lcmd.equals("i")) {
				if (list != null) {
					int lineNum = Utils.toInt(larg, list.getLineNum());
					list.setLineNum(lineNum);
				}
			}
			else if (lcmd.equals("list") || lcmd.equals("l"))
			{
				// send the list to the client, line by line (with line numbers)
				int i = 0;

				if (list != null) {
					for (String s : list.getLines()) {
						System.out.println(i + ": " + s);
						send(i + ": " + s, client);
						i++;
					}
				}
			}
			else if (lcmd.equals("print") || lcmd.equals("p"))
			{
				// send the list to the client, line by line
				if (list != null) {
					for (String s : list.getLines()) {
						System.out.println(s);
						send(s, client);
					}
				}
			}
			else if (lcmd.equals("quit") || lcmd.equals("q")) {
				// save the help file?
				op_listedit(".save", client);

				send("< Exiting... >", client);

				player.setEditor(Editors.NONE);
				player.setStatus("OOC");
			}
			else if (lcmd.equals("replace") || lcmd.equals("repl")) {
				// .replace start_line end_line line;line;line
				String re1 = "(\\d+)( )(\\d+)( )";
				String re2 = "((( )?(\\w+)( )?)*([;]))*";
				String re3 = "(( )?(\\w+)( )?)*";
				
				//if( larg.matches("^(\\d+)( )(\\d+)( )((( )?(\\w+)( )?)*([;]))*(( )?(\\w+)( )?)*$") ) {
				if( larg.matches("^"+re1+re2+re3+"$") ) {
					String[] args = larg.split(" ");
					
					int start = Utils.toInt(args[0], -1);
					int end = Utils.toInt(args[1], -1);
					
					String[] data = args[2].split(";");
					
					// TODO doesn't test that the lines fall within the list...
					if( start != -1 && end != -1 && end >= start ) {
						int n = 0;
						
						for(int l = start; start <= end; l++) {
							if( n < data.length ) {
								list.setLine(l, data[n]);
								n++;
							}
							else break;
						}
					}
				}
				else send("replace: invalid arguments", client);
			}
			else if (lcmd.equals("save") || lcmd.equals("s"))
			{
				// save the current list
				player.saveCurrentEdit();

				send("< List Written Out! >", client);
				send("< List Saved. >", client);
			}
			else if (lcmd.equals("stat") || lcmd.equals("st"))
			{
				// tell us about the current list (current line we're editing, number of lines)
				if (list != null) {
					String header = "< List: " + list.getName() + " Line: " + list.getLineNum() + " Lines: " + list.getNumLines() + " >";
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

	/* Editors - OLC (OnLine Creation) Tools */

	// TODO write code for op_creatureedit
	public void op_creatureedit(final String input, final Client client) {
		final Player player = getPlayer(client);

		String ccmd = "";
		String carg = "";

		final EditorData data = player.getEditorData();

		if (input.indexOf(" ") != -1) {
			ccmd = input.substring(0, input.indexOf(" ")).toLowerCase();
			carg = input.substring(input.indexOf(" ") + 1, input.length());
		} else {
			ccmd = input.substring(0, input.length()).toLowerCase();
		}

		debug("CEDIT CMD");
		debug("ccmd: \"" + ccmd + "\"");
		debug("carg: \"" + carg + "\"");

		if (ccmd.equals("abort")) {
			// clear edit flag
			((Creature) data.getObject("creature")).Edit_Ok = true;

			// exit
			send("< Exiting... >", client);

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);
		}
		else if (ccmd.equals("desc")) {
			String[] cargs = carg.split(" ");

			if (cargs.length > 1) {
				if (cargs[0].equalsIgnoreCase("-f")) {
					// ?
					data.setObject("desc", carg);
				}
				else data.setObject("desc", carg);
			}
			else data.setObject("desc", carg);

			send("Ok.", client);
		}
		else if (ccmd.equals("done")) {
			// save changes
			op_creatureedit("save", client);

			// clear edit flag
			((Creature) data.getObject("creature")).Edit_Ok = true;

			// exit
			send("< Exiting... >", client);

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);
		}
		else if (ccmd.equals("help") || ccmd.equals("?")) {
			if (carg.equals("")) {
				// output help information
				final List<String> output = (List<String>) Utils.mkList(
						"Creature Editor -- Help",
						Utils.padRight("", '-', 74),
						"abort                           abort the editor (no changes will be kept)",
						"desc <new description>          change/set the creature description",
						//"desc <param> <new description>  change/set the room description",
						"done                            finish editing (save & exit)",
						"help                            shows this help information",
						// "items                           list available item prototypes",
						"name <new name>                 change/set the creature name",
						"save                            save changes to the creature",
						"setlocation                     change the location (deprecated?)",
						"setrace                         set the creature's race",
						"setzone                         set the zone that this room belongs to",
						"show                            show basic information about the room",
						"zones                           list the zones that exist",
						Utils.padRight("", '-', 74)
						);
				client.write(output);
			}
			else {
				// output help information specific to the command name given
			}
		}
		else if (ccmd.equals("name")) {
			data.setObject("name", carg);
			send("Ok.", client);
		}
		else if (ccmd.equals("races")) {
			final List<String> raceList = new LinkedList<String>();

			for (final Race race : races) {
				raceList.add(race.getName());
			}

			send(raceList, client);
		}
		else if (ccmd.equals("save")) {
			// TODO consider using a bitset/flags or something to set only
			// changed data (REDIT)
			final Creature creature = (Creature) data.getObject("creature");

			// save room data
			creature.setName((String) data.getObject("name"));
			creature.setFlags((EnumSet<ObjectFlag>) data.getObject("flags"));
			creature.setDesc((String) data.getObject("desc"));
			creature.setLocation((Integer) data.getObject("location"));

			// room.getZone().removeRoom(room);
			// room.setZone((Zone) data.getObject("zone"));
			// room.getZone().addRoom(room);

			send("Creature saved.", client);
		}
		else if (ccmd.equals("setflag")) {
			final EnumSet<ObjectFlag> flags = (EnumSet<ObjectFlag>) data.getObject("flags");

			if (carg.startsWith("!")) {
				flags.remove(ObjectFlag.fromLetter(carg.charAt(1)));
				send(ObjectFlag.fromLetter(carg.charAt(1)).name() + " flag removed.", client);
			}
			else {
				try {
					final ObjectFlag flag = ObjectFlag.fromLetter(carg.charAt(0));

					flags.add(flag);
					send(flag.name() + " flag set.", client);
				}
				catch (final IllegalArgumentException iae) {
					// TODO is this debug message adequate?
					send("No such flag.", client);
					System.out.println("--- Stack Trace ---");
					iae.printStackTrace();
				}
			}

			data.setObject("flags", flags);
			send("Ok.", client);
		}
		else if (ccmd.equals("setlocation")) {
			data.setObject("location", Utils.toInt(carg, -1));
			send("Ok.", client);
		}
		else if (ccmd.equals("setrace")) {
			data.setObject("race", getRace(carg));
			send("Ok.", client);
		}
		/*
		 * else if ( ccmd.equals("setzone") ) { //data.setObject("zone",
		 * getZone(Utils.toInt(rarg, -1))); data.setObject("zone",
		 * getZone(carg)); send("Ok.", client); }
		 */
		else if (ccmd.equals("show")) {
			final Creature creature = (Creature) data.getObject("creature");

			final String editor = "-- Creature Editor";

			// will be a little like examine, just here to show changes
			send(editor + Utils.padRight("", '-', 80 - editor.length()), client);

			// send(Utils.padRight("", '-', 80), client);

			send("DB Reference #: " + creature.getDBRef(), client);
			send("Name: " + data.getObject("name"), client);
			send("Flags: " + ((EnumSet<ObjectFlag>) data.getObject("flags")).toString(), client);
			send("Description:", client);
			parseDesc((String) data.getObject("desc"), 80);
			send("Location: " + (Integer) data.getObject("location"), client);
			
			// send("Zone: " + colors(((Zone) data.getObject("zone")).getName(),
			// "purple2"), client);

			send("Race: " + (Race) data.getObject("race"), client);

			send(Utils.padRight("", '-', 80), client);
		}
		else if (ccmd.equals("zones")) {
			send("Zones", client);
			send(Utils.padRight("", '-', 40), client);

			if ("".equals(carg)) {
				for (Zone zone : zones.keySet()) {
					send("" + zone.getName() + " ( " + zone.getRooms().size()
							+ " Rooms )", client);
				}
			}
			else {
				for (Zone zone : zones.keySet()) {
					if (zone.getName().toLowerCase()
							.startsWith(carg.toLowerCase())) {
						send("" + zone.getName() + " ( "
								+ zone.getRooms().size() + " Rooms )", client);
					}
				}
			}
		} else {
			send("No such command.");
		}
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

		if (rcmd.equals("abort")) {
			// clear edit flag
			((Room) data.getObject("room")).Edit_Ok = true;

			// exit
			send("< Exiting... >", client);

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);
		}
		else if (rcmd.equals("addexit")) {
			// addexit <name> <destination dbref>
			String[] args = rarg.split(" ");

			// if
			if (args.length > 1) {
				final int destination = Integer.parseInt(args[1]);
				final Room r = objectDB.getRoomById(destination);

				if (r != null) {
					Room r1 = (Room) data.getObject("room");
					data.addObject("e|" + args[0], new Exit(args[0], r1.getDBRef(), destination));
					send("Ok.", client);
				}
			}
		}
		else if (rcmd.equals("addthing")) {
			// addthing <thing name/dbref>

			// result:
			// key: add or addthing
			// value: object/dbref of object
			// ex. add, 4
			// ex. addthing, 4

			int dbref = Utils.toInt(rarg, -1);

			final MUDObject m;

			if (dbref != -1) m = objectDB.getById(dbref);
			else             m = objectDB.getByName(rarg);

			if (m != null) {
				if (m instanceof Thing) {
					final Thing thing = (Thing) m;

					Room room = (Room) data.getObject("room");
					Room room1 = objectDB.getRoomById(thing.getLocation());

					room1.removeThing(thing);
					room.addThing(thing);
					thing.setLocation(room.getDBRef());

					send("Ok.", client);
				}
			}
			else { // create new thing and set the name to rarg's value if it's not a number?
				Room room = (Room) data.getObject("room");

				Thing thing = new Thing(rarg);
				room.addThing(thing);
				thing.setLocation(room.getDBRef());

				objectDB.add(thing);
				send("Ok.", client);
			}
		}
		else if (rcmd.equals("additem")) {
			// TODO Fix this, since it shouldn't actually create the item until
			// the room is saved
			Room room = (Room) data.getObject("room");

			if ( prototypes.containsKey(rarg) ) {
				// create item but don't initialize it yet
				final Item item = createItem(rarg, false);

				item.setLocation( room.getDBRef() );

				// TODO fix getObjects somehow
				// final String suffix = "" + data.getObjects("i|" +
				// item.getName()).size();

				// debug("REDIT (suffix): " + suffix);

				/*
				 * if( suffix.equals("0") ) { data.addObject("i|" +
				 * item.getName(), item); } else { data.addObject("i|" +
				 * item.getName() + suffix, item); }
				 */

				// TODO fix this kludge later
				boolean test = false;
				int suffix = 0;

				while (!test) {
					if (suffix == 0) test = data.addObject("i|" + item.getName(), item);
					else             test = data.addObject("i|" + item.getName() + suffix, item);

					suffix++;
				}

				send("Ok.", client);
			}
		}
		else if (rcmd.equals("desc")) {
			String[] rargs = rarg.split(" ");

			if (rargs.length > 1) {
				if (rargs[0].equalsIgnoreCase("-f")) {
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
		else if (rcmd.equals("dim")) {
			String[] args = rarg.split(" ");

			if (args.length > 1) {
				try {
					System.out.println("Args: \"" + args[0] + "\" \"" + args[1] + "\"");

					Integer dim = Integer.parseInt(args[1]);

					System.out.println("key: " + args[0] + " value: " + dim);

					if (args[0].toLowerCase().equals("x")) {
						data.setObject("x", dim);
						send("Ok.", client);
					}
					else if (args[0].toLowerCase().equals("y")) {
						data.setObject("y", dim);
						send("Ok.", client);
					}
					else if (args[0].toLowerCase().equals("z")) {
						data.setObject("z", dim);
						send("Ok.", client);
					}
					else {
						send("Invalid Dimension.", client);
					}
				}
				catch (final NumberFormatException nfe) {
					debug("op_roomedit(): the specified dimension size is invalid (number format exception)");
					System.out.println("--- Stack Trace ---");
					nfe.printStackTrace();
				}
			}
		}
		else if (rcmd.equals("dirset")) {
			// dirset <direction> <exit dbref>
			// ex. dirset NORTH 28
			// needs to be a valid direction and a valid exit
			String[] args = rarg.split(" ");

			if (args.length == 2) {
				final List<String> cardinal_directions = Utils.mkList("north",
						"northeast", "northwest", "south", "southeast",
						"southwest", "east", "west");

				debug("REDIT: direction = " + args[0]);
				debug("REDIT: exit dbref = " + args[1]);

				// direction validation
				if (cardinal_directions.contains(args[0].toLowerCase())) {
					Integer dbref = Utils.toInt(args[1], -1);

					if (dbref != -1) {
						final Room room = (Room) data.getObject("room");
						final Exit exit = getExit(dbref);

						if (room.getExits().contains(exit)) {
							data.addObject("dirset:" + args[0].toLowerCase(), dbref);
							send("Ok.", client);
						}
						else send("Valid Exit, but it's not attached to this room.", client);
					}
					else send("Invalid Exit!", client);
				}
				else send("Invalid Argument - non-cardinal direction", client);
			}
			else send("Invalid Arguments - incorrect number of argments", client);
		}
		else if (rcmd.equals("done")) {
			// save changes
			op_roomedit("save", client);

			// clear edit flag
			((Room) data.getObject("room")).Edit_Ok = true;

			// exit
			send("< Exiting... >", client);

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);
		}
		else if (rcmd.equals("flags")) {
			final ObjectFlag[] flags = new ObjectFlag[] {
					ObjectFlag.BANK,     ObjectFlag.DARK,     ObjectFlag.ENTER_OK,
					ObjectFlag.FORGE,    ObjectFlag.GUEST,    ObjectFlag.HOUSE,
					ObjectFlag.MERCHANT, ObjectFlag.NO_ENTER, ObjectFlag.NO_LEAVE,
					ObjectFlag.NO_TELEPORT
			};

			for (final ObjectFlag flag : flags) {
				if (MudUtils.isAllowed(flag, TypeFlag.ROOM)) {
					send(flag.getName(), client);
				}
			}
		}
		else if (rcmd.equals("help") || rcmd.equals("?")) {
			if (rarg.equals("")) {
				// output help information
				final List<String> output = (List<String>) Utils.mkList("Room Editor -- Help",
						Utils.padRight("", '-', 74),
						"abort                           abort the editor (no changes will be kept)",
						"addexit <name> <destination>    creates a new exit",
						"additem <prototype key>         creates a new instance of the indicated",
						"                                prototype",
						"addthing <name/dbref>           move an existing object to the room (not",
						"                                sure how it should work)",
						"desc <param> <new description>  change/set the room description",
						"dim <dimension> <size>          change a dimension of the room (x/y/z)",
						"dirset <direction> <exit dbref> associate an exit with a cardinal direction",
						"done                            finish editing (save & exit)",
						"help                            shows this help information",
						"items                           list available item prototypes",
						"npcs",
						"layout                          display a 2D layout visualization",
						"modify                          change layout",
						"name <new name>                 change/set the room name",
						"rooms                           list the other rooms that are in the same",
						"                                zone as this one",
						"save                            save changes to the room",
						"setlocation                     change the location (deprecated?)",
						"setzone <zone name>             set the zone that this room belongs to",
						"show                            show basic information about the room",
						"trigger <type> <data>           setup a trigger of the specified type with",
						"                                the specified data",
						"zones                           list the zones that exist",
						Utils.padRight("", '-', 74)
						);

				client.write(output);
			}
			else {
				// output help information specific to the command name given
			}
		}
		else if (rcmd.equals("items")) {
			final List<String> output = new LinkedList<String>();

			for (final String s : prototypes.keySet()) {
				output.add(s);
			}

			client.write(output);
		}
		else if (rcmd.equals("layout")) {
			int width = (Integer) data.getObject("x");
			int length = (Integer) data.getObject("y");

			char[] tiles = ((Room) data.getObject("room")).tiles;
			
			final List<String> sendList = new LinkedList<String>();
			final StringBuilder sb = new StringBuilder();
			
			sendList.add("_ : open space, X : wall, / : door");

			for (int y = 0; y < length; y++) {
				for (int x = 0; x < width; x++) {
					if (x < (width - 1)) sb.append("|" + tiles[y * width + x]);
					else                  sb.append("|" + tiles[y * width + x] + "|");
				}
				
				sendList.add(sb.toString());
				sb.delete(0, sb.length());
			}
			
			send(sendList, client);
			send("Ok.", client);
		}
		else if( rcmd.equals("modify") ) {
			// modify layout 0,0 [5,1] X

			//String expr1 = "(modify)(\\s+)(layout)(\\s+)(\\d+)(,)(\\d+)(\\s+)([_X])";
			//String expr2 = "(modify)(\\s+)(layout)(\\s+)(\\d+)(,)(\\d+)(\\s+)(\\d+)(,)(\\d+)(\\s+)([_X])";

			// "(modify)(\s+)(layout)(\s+)(\d+)(,)(\d+)(\s+)(\d+)(,)(\d+)(\s+)([_\\X])"
			//if( rarg.matches(expr1) || rarg.matches(expr2) ) {
			final String[] args = rarg.split(" ");

			int width = (Integer) data.getObject("x");
			int length = (Integer) data.getObject("y");

			char[] tiles = ((Room) data.getObject("room")).tiles;

			if( args[0].equalsIgnoreCase("layout") ) {
				if( args.length == 3 ) {
					String[] coord = args[1].split(",");
					
					int x = Utils.toInt(coord[0], -1);
					int y = Utils.toInt(coord[1], -1);

					char c = args[2].charAt(0);
					
					if( Utils.range(x, 0, width - 1) && Utils.range(y, 0, length -1) ) {
						tiles[y * width + x] = c;

						send("Ok.", client);
					}
					else {
						send("Incorrect Coordinates", client);
					}

					// width: 5 (x), length: 8 (y)
					// X Y -> 5 x 8 -> 40, 0-39
					// 4, 6
				}
				else if( args.length == 4 ) {
					String[] coord = args[1].split(",");
					String[] dimension = args[2].split(",");
					
					int x = Utils.toInt(coord[0], -1);
					int y = Utils.toInt(coord[1], -1);

					int w = Utils.toInt(dimension[0], 0);
					int l = Utils.toInt(dimension[1], 0);

					char c = args[3].charAt(0);

					// Bounds Checking
					if( Utils.range(x, 0, width - 1) && Utils.range(y, 0, length - 1) ) {
						if( Utils.range(w, x, width) && Utils.range(l, y, length)) {
							debug("Initial index value: " + ((y * width) + x));

							for(int n = 0; n < l; n++) {
								for(int p = 0; p < w; p++) {
									tiles[(y + n) * width + (x + p)] = c;
								}
							}

							send("Ok.", client);
						}

						send("Wrong Bounds", client);
					}

					send("Incorrect Coordinates", client);
				}
				else {
					send("Incorrect Parameters,", client);
				}
			}
			else {
				send("Syntax Error.", client);
			}
		}
		else if (rcmd.equals("name")) {
			data.setObject("name", rarg);
			send("Ok.", client);
		}
		else if (rcmd.equals("rooms")) {
			final Zone z = (Zone) data.getObject("zone");

			if (z != null) {
				final List<String> sendList = Utils.mkList();
				
				for (final Room rm : z.getRooms()) {
					sendList.add(rm.getName() + " (#" + rm.getDBRef() + ")");
				}
				
				send(sendList, client);
			}
			else send("Invalid Zone!", client);
		}
		// TODO improve this
		// TODO consider using a bitset/flags or something to set only changed data
		// TODO consider making editor objects and moving this kind of code into them
		else if (rcmd.equals("save")) {
			//rsave(data);
			Room room = (Room) data.getObject("room");
			
			// NOTE: no bitset is created in editing a room, yet (4-5-2016)
			/*BitSet b = ((BitSet) data.getObject("dirty"));
			
			int n = 0;
			
			final int NAME = 0;
			final int FLAGS = 1;
			final int DESC = 2;
			final int LOC = 3; 

			while(n < 4) {
				boolean dirty = b.get(n);
				
				switch(n) {
				case NAME:  if( dirty ) room.setName((String) data.getObject("name")); break;
				case FLAGS: if( dirty ) room.setFlags((EnumSet<ObjectFlag>) data.getObject("flags")); break;
				case DESC:  if( dirty ) room.setDesc((String) data.getObject("desc")); break;
				case LOC:   if( dirty ) room.setLocation((Integer) data.getObject("location")); break;
				default:    break;
				}
				
				n++;
			}*/
			
			// save room data
			room.setName((String) data.getObject("name"));
			room.setFlags((EnumSet<ObjectFlag>) data.getObject("flags"));
			room.setDesc((String) data.getObject("desc"));
			room.setLocation((Integer) data.getObject("location"));

			room.setDimension("x", (Integer) data.getObject("x"));
			room.setDimension("y", (Integer) data.getObject("y"));
			room.setDimension("z", (Integer) data.getObject("z"));

			if (room.getZone() != null) {
				room.getZone().removeRoom(room);
			}

			room.setZone((Zone) data.getObject("zone"));

			if (room.getZone() != null) {
				room.getZone().addRoom(room);
			}

			// save exits
			for (final String s : data.getKeysByPrefix("e|")) {
				final Exit e = (Exit) data.getObject(s);

				if ( !(room.getExits().contains(e)) ) {
					objectDB.add(e);
					objectDB.addAsNew(e);
					objectDB.addExit(e);

					room.addExit(e);
				}
			}

			// save items
			for (final String s : data.getKeysByPrefix("i|")) {
				final Item item = (Item) data.getObject(s);

				if ( !(room.getItems().contains(item)) ) {
					initCreatedItem(item);
					
					room.addItem(item);
				}
			}

			// save direction settings
			for (final String s : data.getKeysByPrefix("dirset:")) {
				final Integer dbref = (Integer) data.getObject(s);
				final Exit exit = getExit(dbref);
				
				int temp = s.indexOf(":");
				
				// TODO check to see if temp is -1?
				final Direction dir = Direction.getDirection(s.substring(temp + 1, s.length()));
				
				room.setDirectionExit(dir.getValue(), exit);
			}

			// Save Triggers
			if (data.getObject("onEnter") != null) {
				for (final Trigger t : (LinkedList<Trigger>) data.getObject("onEnter")) {
					room.setTrigger(TriggerType.onEnter, t);
				}
			}

			if (data.getObject("onLeave") != null) {
				for (final Trigger t : (LinkedList<Trigger>) data.getObject("onLeave")) {
					room.setTrigger(TriggerType.onLeave, t);
				}
			}

			send("Room saved.", client);
		}
		else if (rcmd.equals("setflag")) {
			final EnumSet<ObjectFlag> flags = (EnumSet<ObjectFlag>) data.getObject("flags");

			ObjectFlag flag = null;

			if (rarg.startsWith("!")) {
				if (flag == null) flag = ObjectFlag.fromString(rarg.substring(1));
				if (flag == null) flag = ObjectFlag.fromLetter(rarg.charAt(1));
			}
			else {
				if (flag == null) flag = ObjectFlag.fromString(rarg);
				if (flag == null) flag = ObjectFlag.fromLetter(rarg.charAt(0));
			}

			if (flag == null) {
				send("Invalid Flag!", client);
				return;
			}

			if (rarg.startsWith("!")) {
				flags.remove(flag);
				send(flag.name() + " flag removed.", client);
			}
			else {
				flags.add(flag);
				send(flag.name() + " flag set.", client);
			}

			data.setObject("flags", flags);
			send("Ok.", client);
		}
		else if (rcmd.equals("setlocation")) {
			data.setObject("location", Utils.toInt(rarg, -1));
			send("Ok.", client);
		}
		else if (rcmd.equals("setzone")) {
			// data.setObject("zone", getZone(Utils.toInt(rarg, -1)));
			data.setObject("zone", getZone(rarg));
			send("Ok.", client);
		}
		else if (rcmd.equals("show")) {
			Room room = (Room) data.getObject("room");

			// will be a little like examine, just here to show changes
			send("--- Room Editor " + Utils.padRight("", '-', 80 - 16), client);
			// send(Utils.padRight("", '-', 80), client);
			//send("DB Reference #: " + room.getDBRef(), client);
			send("DB Ref #: " + room.getDBRef(), client);
			send("Name: " + data.getObject("name"), client);
			send("Flags: " + ((EnumSet<ObjectFlag>) data.getObject("flags")).toString(), client);
			send("Dimensions:", client);
			send("    X: " + (Integer) data.getObject("x"), client);
			send("    Y: " + (Integer) data.getObject("y"), client);
			send("    Z: " + (Integer) data.getObject("z"), client);
			send("Description:", client);
			
			parseDesc((String) data.getObject("desc"), 80);
			
			send("Location: " + (Integer) data.getObject("location"), client);

			final Zone zone = (Zone) data.getObject("zone");

			if (zone != null) send("Zone: " + colors(zone.getName(), "purple2"), client);
			else              send("Zone: null");

			if (player.getConfigOption("compact-editor")) {
				StringBuilder sb = new StringBuilder();

				List<String> thingStrings = new LinkedList<String>();
				List<String> itemStrings = new LinkedList<String>();
				
				List<String> exitStrings = new LinkedList<String>();

				send(colors(Utils.padRight("Things:", ' ', 35), getDisplayColor("thing")) + " " + colors(Utils.padRight("Items:", ' ', 35), getDisplayColor("item")), client);
				
				for (final String s : data.getKeysByPrefix("t|")) {
					System.out.println(s);
					System.out.println("Thing Found");

					final Thing thing = (Thing) data.getObject(s);

					if (thing.getDBRef() == -1) thingStrings.add(colors(thing.getName() + "(#" + thing.getDBRef() + ")", "green"));
					else                        thingStrings.add(thing.getName() + "(#" + thing.getDBRef() + ")");
				}
				
				for (final String s : data.getKeysByPrefix("i|")) {
					System.out.println(s);
					System.out.println("Item Found");

					final Item item = (Item) data.getObject(s);

					if (item.getDBRef() == -1) itemStrings.add(colors(item.getName() + "(#" + item.getDBRef() + ")", "green"));
					else                       itemStrings.add(item.getName() + "(#" + item.getDBRef() + ")");
				}

				// the below will cause null pointer exceptions if one empties
				// before the other
				while (!itemStrings.isEmpty() || !exitStrings.isEmpty()) {
					String thingString = "", itemString = "";

					if (!thingStrings.isEmpty()) thingString = thingStrings.remove(0);
					if (!itemStrings.isEmpty())  itemString = itemStrings.remove(0);

					sb.append(Utils.padRight(thingString, ' ', 35) + " " + Utils.padRight(itemString, ' ', 35));

					send(sb.toString(), client);

					sb.delete(0, sb.length());
				}
				
				send(colors(Utils.padRight("Exits:", ' ', 35), getDisplayColor("exit")), client);
				
				for (final String s : data.getKeysByPrefix("e|")) {
					System.out.println(s);
					System.out.println("Exit Found");

					final Exit exit = (Exit) data.getObject(s);

					exitStrings.add(exit.getName() + "(#" + exit.getDBRef() + ") - Source: " + exit.getLocation() + " Dest: " + exit.getDestination());
				}
				
				while (!exitStrings.isEmpty()) {
					String exitString = "";

					if (!exitStrings.isEmpty()) exitString = exitStrings.remove(0);

					//sb.append(Utils.padRight(exitString, ' ', 35) + " " + Utils.padRight(itemString, ' ', 35));
					sb.append(Utils.padRight(exitString, ' ', 70));

					send(sb.toString(), client);

					sb.delete(0, sb.length());
				}
			}
			else {
				send(colors("Exits:", getDisplayColor("exit")), client);

				for (final String s : data.getKeysByPrefix("e|")) {
					System.out.println(s);
					System.out.println("Exit Found");

					final Exit exit = (Exit) data.getObject(s);

					send(exit.getName() + "(#" + exit.getDBRef() + ") - Source: " + exit.getLocation() + " Dest: " + exit.getDestination(), client);
				}

				send(colors("Items:", getDisplayColor("item")), client);

				for (final String s : data.getKeysByPrefix("i|")) {
					System.out.println(s);
					System.out.println("Item Found");

					final Item item = (Item) data.getObject(s);

					send(item.getName() + "(#" + item.getDBRef() + ")", client);
				}
			}

			send(Utils.padRight("", '-', 80), client);
		}
		else if (rcmd.equals("trigger")) {
			String[] rargs = rarg.split(" ");

			if (rargs.length >= 2) {
				int type = Utils.toInt(rargs[0], -1);

				if (type == 0) {
					if (data.getObject("onEnter") == null) {
						data.addObject("onEnter", new LinkedList<Trigger>());
					}

					List<String> temp = Arrays.asList(rargs);
					temp = temp.subList(1, temp.size());

					((LinkedList<Trigger>) data.getObject("onEnter")).add(new Trigger(Utils.join(temp, " ")));

					send("Ok.", client);
				}
				else if (type == 1) {
					if (data.getObject("onLeave") == null) {
						data.addObject("onLeave", new LinkedList<Trigger>());
					}

					List<String> temp = Arrays.asList(rargs);
					temp = temp.subList(1, temp.size());

					((LinkedList<Trigger>) data.getObject("onLeave")).add(new Trigger(Utils.join(rargs, " ")));

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
		else if (rcmd.equals("zones")) {
			send("Zones", client);
			send(Utils.padRight("", '-', 40), client);

			if ("".equals(rarg)) {
				for (Zone zone : zones.keySet()) {
					send("" + zone.getName() + " ( " + zone.getRooms().size() + " Rooms )", client);
				}
			}
			else {
				for (Zone zone : zones.keySet()) {
					if (zone.getName().toLowerCase().startsWith(rarg.toLowerCase())) {
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

		if (icmd.equals("abort")) {
			// / clear edit flag
			((Item) data.getObject("item")).Edit_Ok = true;

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if (icmd.equals("desc")) {
			data.setObject("desc", iarg);
			send("Ok.", client);
		}
		else if (icmd.equals("done")) {
			// save changes
			op_itemedit("save", client);

			// clear edit flag
			((Item) data.getObject("item")).Edit_Ok = true;

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if (icmd.equals("help") || icmd.equals("?")) {
			// output help information
			final List<String> output = (List<String>) Utils.mkList("Item Editor -- Help",
					Utils.padRight("", '-', 74),
					"abort                           abort the editor (no changes will be kept)",
					"desc <param> <new description>  change/set the item description",
					"done                            finish editing (save & exit)",
					"help                            shows this help information",
					"name <new name>                 change/set the item name",
					"save                            save changes to the item",
					"show                            show basic information about the item",
					"type                            change the item's type",
					"types                           list of valid item types",
					Utils.padRight("", '-', 74)
					);

			client.write(output);
		}
		else if (icmd.equals("name")) {
			data.setObject("name", iarg);
			send("Ok.", client);
		}
		else if (icmd.equals("save")) {
			final Item item = (Item) data.getObject("item");

			item.setName((String) data.getObject("name"));
			item.setDesc((String) data.getObject("desc"));

			// TODO what am I trying to accomplish here exactly, is item type a
			// changeable thing?
			// item.setItemType((ItemType) data.getObject("type"));

			send("Item saved.", client);
		}
		else if (icmd.equals("set")) {
			// general purpose command
			// ex: set drinkable true

			String[] args = iarg.split(" ");

			if (args.length == 2) {
				if (args[0].equals("drinkable")) {
					if (args[1].equalsIgnoreCase("true"))       data.setObject("drinkable", true);
					else if (args[1].equalsIgnoreCase("false")) data.setObject("drinkable", false);
				}
			}

			send("Command not implemented.", client);
		}
		else if (icmd.equals("show")) {
			final Item item = (Item) data.getObject("item");

			// will be a little like examine, just here to show changes
			send("--- Item Editor " + Utils.padRight("", '-', 80 - 16), client);
			// send("----------------------------------------------------", client);
			
			send("DB Reference #: " + item.getDBRef(), client);
			send("Name: " + data.getObject("name"), client);
			send("Item Type: " + ((ItemType) data.getObject("type")).toString(),client);
			send("Description:", client);
			
			parseDesc((String) data.getObject("desc"), 80);

			// TODO show different data depending on item type

			// send("----------------------------------------------------", client);
			send(Utils.padRight("", '-', 80), client);

		}
		else if (icmd.equals("type")) {
			data.setObject("type", ItemTypes.getType(iarg));

			send("Ok.", client);
		}
		else if (icmd.equals("types")) {
			send("Not Implemented Yet.", client);
		}
		else {
			// currently causes a loop effect, where the command gets funneled back into op_iedit regardless
			// cmdQueue.add(new CMD(input, client, 0));
		}
	}

	/**
	 * Quest Editor
	 * 
	 * @param input
	 * @param client
	 */
	public void op_questedit(final String input, final Client client) {
		final Player player = getPlayer(client);
		final EditorData data = player.getEditorData();
		final Quest quest = (Quest) data.getObject("quest");

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

		if ( qcmd.equals("abort") ) {
			send("< Aborting Changes... >", client);

			// clear edit flag
			quest.Edit_Ok = true;

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if ( qcmd.equals("desc") ) {
			if( data.setObject("desc", qarg) ) {
				send("Ok.", client);
			}
		}
		else if ( qcmd.equals("done") ) {
			send("< Saving Changes... >", client);

			// save changes
			op_questedit("save", client);

			send("< Done >", client);

			// clear edit flag
			quest.Edit_Ok = true;

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if (qcmd.equals("help") || qcmd.equals("?")) {
			send("Quest Editor -- Help", client);
			send(Utils.padRight("", '-', 40), client);
			send("abort", client);
			send("desc <new description>", client);
			send("done", client);
			send("help", client);
			send("name <new name>", client);
			send("save", client);
			send("setloc", client);
			send("show", client);
			send("zones", client);
		}
		else if (qcmd.equals("name")) {
			if( data.setObject("name", qarg) ) {
				send("Ok.", client);
			}
		}
		else if (qcmd.equals("save")) {
			quest.init(); // ensure that we have a valid quest id

			quest.setName((String) data.getObject("name"));
			quest.setDescription((String) data.getObject("desc"));

			// save tasks (add new ones, update existing ones, remove deleted
			// ones)

			if (!quests.contains(quest)) {
				quests.add(quest); // add quest to global quest table
			}
		}
		else if (qcmd.equals("setloc")) {
			if( data.setObject("location", Utils.toInt(qarg, -1)) ) {
				send("Ok.", client);
			}
		}
		else if (qcmd.equals("show")) {
			// will be a little like examine, just here to show changes
			send("--- Quest Editor " + Utils.padRight("", '-', 80 - 17), client);

			send("Quest ID#: " + quest.getId(), client);
			send("Name: " + (String) data.getObject("name"), client);
			send("Location: " + (Integer) data.getObject("location"), client);
			send("Description: ", client);
			
			parseDesc((String) data.getObject("desc"), 80);
			
			send(Utils.padRight("", '-', 80), client);
			
			send("Reward:", client);
			
			final Reward r = quest.getReward();

			if( r != null ) {
				final Coins c = r.getCoins();
				final List<Item> itemList = r.getItems();

				if( c != null ) send(c.toString(true), client);

				if( itemList != null ) {
					for(final Item item : itemList) {
						send(item.getName(), client);
					}
				}
			}
			else {
				send("-None-", client);
			}

			send(Utils.padRight("", '-', 80), client);
			
			send("Tasks:", client);

			int i = 0;

			for (final Task t : quest.getTasks()) {
				send(" " + i + ") " + t.getDescription(), client);
				i++;
			}

			send(Utils.padRight("", '-', 80), client);
		}
		else if (qcmd.equals("zones")) {
			for (final Entry<Zone, Integer> zoneData : zones.entrySet()) {
				final Zone zone = zoneData.getKey();
				
				if( zone != null ) {
					// NOTE: what was I thinking with instance id here?
					//send(zone.getInstanceId() + " - " + zone.getName(), client);
					send(zone.getId() + " - " + zone.getName(), client);
				}
			}
		}
		else {
		}
	}

	@SuppressWarnings("unchecked")
	public void op_skilledit(final String input, final Client client) {
		final Player player = getPlayer(client);
		final EditorData data = player.getEditorData(); 
		final Skill skill = (Skill) data.getObject("skill");

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

		if (scmd.equals("abort")) {
			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if (scmd.equals("abrv")) {
			data.setObject("abbrev", sarg);
			send("Ok.", client);
		}
		else if (scmd.equals("abbrevs")) {
		}
		else if (scmd.equals("class")) {
			if (sarg != null) {
				String[] args = sarg.split(" ");

				if (args.length > 0) {
					for (String s : args) {
						if( !s.isEmpty() ) {
							final String className = s.substring(1);
							final PClass newClass = Classes.getClass(className);

							if (data.getObject("classes") instanceof List<?>) {
								List<PClass> classes = (List<PClass>) data.getObject("classes");

								if (s.charAt(0) == '+') {
									classes.addAll(Utils.mkList(newClass));
									send("Added " + newClass + " to classes for " + (String) data.getObject("name"), client);
								}
								else if (s.charAt(0) == '-') {
									classes.remove(newClass);
									send("Removed " + newClass + " from classes for " + (String) data.getObject("name"), client);
								}
							}
						}
						else {
							send("Error: no such class.", client);
						}
					}
				}
			}
		}
		else if (scmd.equals("classes")) {
			// send list of valid class names
			send("NONE, BARBARIAN, BARD, CLERIC, DRUID, FIGHTER, MONK, PALADIN, RANGER, ROGUE, SORCERER, WIZARD, ADEPT, ARISTOCRAT, COMMONER, EXPERT, WARRIOR", client);
		}
		else if (scmd.equals("help") || scmd.equals("?")) {
			if (sarg.equals("")) {
				// output help information
				final List<String> output = (List<String>) Utils.mkList("Skill Editor -- Help",
						Utils.padRight("", '-', 74),
						"abort                          abort the editor (no changes will be kept)",
						"abrv                           designate abbreviated form of skill name",
						"abbrevs                        ?",
						"class                          add/remove classes that have/can use this skill",
						"classes                        list of valid class names",
						"desc <param> <new description> change/set the room description **",
						"done                           finish editing (save & exit) **",
						"help                           shows this help information",
						"name <new name>                change/set the skill name",
						"quit                           ",
						"save                           save changes to the skill (NOT IMPLEMENTED)",
						"show                           show basic information about the skill",
						"skills                         list all skills (NOT IMPLEMENTED)",
						"stat                           change the stat this skill is associated with",
						"stats                          list all stats",
						Utils.padRight("", '-', 74));
				
				client.write(output);
			}
			else {
				// output help information specific to the command name given
			}
		}
		else if (scmd.equals("name")) {
			data.setObject("name", sarg);
			send("Ok.", client);
		}
		else if (scmd.equals("quit")) {
		}
		else if (scmd.equals("save")) {
		}
		else if (scmd.equals("show") || scmd.equals("sh")) {
			/*
			send(Utils.padRight("", '-', 80), client);
			send("   Name: " + (String) data.getObject("name"), client);
			send("     ID: " + skill.getId(), client);
			send("   Stat: " + (Ability) data.getObject("stat"), client);
			send("Classes: " + (List<PClass>) data.getObject("classes"), client);
			send(" Abbrev: " + (String) data.getObject("abbrev"), client);
			send(Utils.padRight("", '-', 80), client);
			*/
			
			final List<String> output = Utils.mkList(
					Utils.padRight("", '-', 80),
					"    Name: " + (String) data.getObject("name"),
					"      ID: " + skill.getId(),
					"    Stat: " + (Ability) data.getObject("stat"),
					" Classes: " + (List<PClass>) data.getObject("classes"),
					"  Abbrev: " + (String) data.getObject("abbrev"),
					Utils.padRight("", '-', 80)
					);
			
			send(output, client);
		}
		else if (scmd.equals("skills")) {
		}
		else if (scmd.equals("stat")) {
			// change the stat a skill is associated with
			/*
			 * TODO this command should take the short/long form of the stat
			 * name and set that stat as the primary stat for this skill
			 */
		}
		else if (scmd.equals("stats")) {
			final Ability[] abilities = rules.getAbilities();
			final StringBuilder sb = new StringBuilder();
			
			int count = 0;

			for (final Ability ab : abilities) {
				sb.append(ab.getName());

				count++;

				if (count < abilities.length) sb.append(", ");
			}

			send("Stats: " + sb.toString(), client);
			// send( "Abilities: " + sb.toString(), client );
		}
		else {
			send("No such command in this editor.", client);
		}
	}

	/**
	 * Zone Editor Input Handler
	 * 
	 * @param input
	 * @param client
	 */
	public void op_zoneedit(final String input, final Client client) {
		final Player player = getPlayer(client);
		final EditorData data = player.getEditorData();
		final Zone zone = (Zone) data.getObject("zone");

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

		if (zcmd.equals("abort")) {
			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);

			// clear editor data
			player.setEditorData(null);

			// exit
			send("< Exiting... >", client);
		}
		else if (zcmd.equals("addroom")) {
		}
		else if (zcmd.equals("help") || zcmd.equals("?")) {
			if (zarg.equals("")) {
				// output help information
				final List<String> output = (List<String>) Utils.mkList("Skill Editor -- Help",
						Utils.padRight("", '-', 74),
						"abort                          abort the editor (no changes will be kept)",
						"addroom",
						"desc <param> <new description> change/set the room description **",
						"delroom                        (NOT IMPLEMENTED)",
						"remroom                        (NOT IMPLEMENTED)",
						"setparent                      (NOT IMPLEMENTED)",
						"done                           finish editing (save & exit) ** (NOT IMPLEMENTED)",
						"help                           shows this help information",
						"name <new name>                change/set the zone name (NOT IMPLEMENTED)",
						"quit                           (NOT IMPLEMENTED)",
						"save                           save changes to the zone (NOT IMPLEMENTED)",
						"show                           show basic information about the zone",
						Utils.padRight("", '-', 74));
				
				client.write(output);
			}
			else {
				// output help information specific to the command name given
			}
		}
		else if (zcmd.equals("remroom")) {
		}
		else if (zcmd.equals("setparent")) {
		}
		else if (zcmd.equals("show")) {
			send("--- Zone Editor " + Utils.padRight("", '-', 80 - 16), client);
			send("   Name: " + (String) data.getObject("name"), client);
			send("     ID: " + zone.getId(), client);
			send(Utils.padRight("", '-', 80), client);
		}
	}

	/**
	 * The input handler for a Pager, of which each Player has one that holds
	 * the contents of a file (usually help files) they are currently looking
	 * at. A pager offers the ability to scroll up and down through the file.
	 * The internal "commands" for the pager are interpreted here.
	 * 
	 * @param input
	 * @param client
	 */
	public void op_pager(final String input, final Client client) {
		final Player player = getPlayer(client);
		
		boolean cont = true;
		
		try {
			final Pager pager = player.getPager();

			String[] temp = null;

			if ( input.equals("up") )        temp = pager.scrollUp();
			else if ( input.equals("down") ) temp = pager.scrollDown();
			else if ( input.equals("view") ) temp = pager.getView();
			else if ( input.equals("done") ) cont = false;

			if( cont ) {
				for (final String s : temp) {
					client.write(s + "\r\n");
				}

				int top = pager.getTop();
				int bottom = pager.getBottom();
				int rem = pager.getContent().length - bottom;

				client.write("< lines " + top + "-" + bottom + ", " + rem + " lines remaining | 'up'/'down' | 'done' to finish >\r\n");
			}
			else {
				client.writeln("Leaving Pager");

				player.setPager(null);
				player.setStatus("OOC");

				look(getRoom( player.getLocation() ), client);
			}
		}
		catch (final NullPointerException npe) {
			System.out.println("Pager sub-system: NullPointerException caught");

			System.out.println("Reporting error:");
			System.out.println("--- Stack Trace ---");
			npe.printStackTrace();

			System.out.println("Leaving Pager");

			player.setPager(null);
			player.setStatus("OOC");
		}
	}
	
	private void op_recovery(final Client client) {
		final ClientData cd = getClientData( client );
		
		final String state = cd.state;
		
		System.out.println("STATE: " + state);
		
		// use requestinput?
		
		if( state.equals(Constants.QUERY) )    client.write("Account or Player? (A/P)? ");
		if( state.equals(Constants.ACCOUNT) )  client.write("Account Name? ");
		if( state.equals(Constants.PLAYER) )   client.write("Player Name? ");
		if( state.equals(Constants.KEY) )      client.write("Recovery Key? ");
		if( state.equals(Constants.NEWPASS) )  client.write("New Password? ");
		if( state.equals(Constants.CONFIRM) )  client.write("Re-Enter to Confirm? ");
		
		System.out.println("STATE: " + state);
	}
	
	private void handle_recovery(final String input, final Client client) {
		final ClientData cd = getClientData( client );
		
		debug("state: " + cd.state);
		debug("input: " + input);
		
		// TODO remove? falls under enabling an echo?
		if( cd.state == Constants.NEWPASS )     send( Utils.padRight("", '*', input.length()), client);
		else if( cd.state == Constants.CONFIRM) send( Utils.padRight("", '*', input.length()), client);
		else                                    send(input, client);
		
		if( cd.state.equals(Constants.QUERY) ) {
			if( input.equalsIgnoreCase("a") ) {
				send("Account Recovery selected.", client);
				
				cd.data.put("type", "account");
				
				cd.state = Constants.ACCOUNT;
			}
			else if( input.equalsIgnoreCase("p") ) {
				send("Player Recovery selected.", client);
				
				cd.data.put("type", "player");
				
				cd.state = Constants.PLAYER;
			}
			else {
				send("Invalid Input!", client);
			}
			
			op_recovery(client);
		}
		
		else if( cd.state.equals(Constants.ACCOUNT) ) {
			final Account account = acctMgr.getAccount(input);
			
			if( account != null ) {
				send("Valid Account Found!");
				
				cd.data.put("account", account);
				
				cd.state = Constants.KEY;
			}
			else {
				send("No such account!", client);
			}
			
			op_recovery(client);
		}
		
		else if( cd.state.equals(Constants.PLAYER) ) {
			final Player player = objectDB.getPlayer( input );
			
			if( player != null ) {
				send("Valid Player Found!");
				
				cd.data.put("player", player);
				
				cd.state = Constants.KEY;
			}
			else {
				send("No such player!", client);
			}
			
			op_recovery(client);
		}
		
		else if( cd.state.equals(Constants.KEY) ) {
			final String recoveryType = (String) cd.data.get("type");
			
			switch(recoveryType) {
			case "account":
				final Account account = (Account) cd.data.get("account");
				
				if( account != null ) {
					if( input.equals( account.getRecoveryKey() ) ) {
						send("Valid Key Submitted!", client);
						
						cd.state = Constants.NEWPASS;
					}
					else {
						send("Invalid Key!", client);
					}
				}
				break;
			case "player":
				final Player player = (Player) cd.data.get("player");
				
				if( player != null ) {
					if( input.equals( "overrideALPHAZETAEPSILON" ) ) {
						send("Valid Key Submitted!", client);
						
						cd.state = Constants.NEWPASS;
					}
				}
				else {
					send("Invalid Key!", client);
				}
				break;
			default:
				send("Invalid Recovery Type!", client);
				
				cd.state = Constants.QUERY;
				break;
			}
			
			op_recovery(client);
		}
		
		else if( cd.state.equals(Constants.NEWPASS) ) {
			cd.data.put("newpass", input);
			
			cd.state = Constants.CONFIRM;
			
			op_recovery(client);
		}
		
		else if( cd.state.equals(Constants.CONFIRM) ) {
			final String newpass = (String) cd.data.get("newpass");
			
			if( newpass.equals(input) ) {
				final String rType = (String) cd.data.get("type");
				
				switch( rType ) {
				case "account":
					final Account account = (Account) cd.data.get("account");
					
					account.setPassword( newpass );
					
					break;
				case "player":
					final Player player = (Player) cd.data.get("player");
					
					player.setPass( newpass );
					
					break;
				default:
					break;
				}
				
				send("Your password has been changed to: '" + newpass + "'\n", client);
				send("CAUTION: Exercise care in remembering this password, as the admins cannot do anything for you if you forget it.\n", client);
				
				cd.state = "";
				
				setClientState(client, "");
				setClientData(client, null);
			}
			else {
				send("Passwords don't match!\n");
				
				cd.state = Constants.NEWPASS;
				
				op_recovery(client);
			}
		}
	}

	private void op_registration(final Client client) {
		final LoginData ld = getLoginData(client);
		
		final String state = ld.state;
		
		System.out.println("STATE: " + state);

		if ( state.equals(Constants.USERNAME)) client.write("Name?     ");
		if ( state.equals(Constants.PASSWORD)) client.write("Password? ");
		if ( state.equals(Constants.REGISTER)) client.write("Finalize? ");
	}

	private void handle_registration(final String input, final Client client) {
		final LoginData ld = getLoginData(client);
		
		debug("state: " + ld.state);
		debug("input: " + input);
		
		if ( ld.state.equals(Constants.USERNAME) ) {
			ld.username = input;
			ld.state = Constants.PASSWORD;
			
			op_registration(client);
		}
		else if ( ld.state.equals(Constants.PASSWORD) ) {
			ld.password = input;
			ld.state = Constants.REGISTER;
			
			op_registration(client);
		}

		else if ( ld.state.equals(Constants.REGISTER) ) {
			if( Utils.mkList("y", "Y", "yes", "Yes", "YES").contains(input) ) {
				final String username = ld.username;
				final String password = ld.password;

				acctMgr.addAccount(username, password, 3);
				
				// TODO where should hashing occur? 
				final Account account = acctMgr.getAccount(username, Utils.hash(password));
				
				if( client == null ) System.out.println("CLIENT IS NULL!");
				if( account == null ) System.out.println("ACCOUNT IS NULL!");
				
				caTable.put(client, account);
				
				// TODO this seems a tad insecure
				send("Account Registered!", client);
				send("Username: \'" + username + "\'", client);
				send("Password: \'" + password + "\'", client);

				send("You may now login using your account credentials as you would those for an ordinary player.", client);
			}
			else {
				send("Registration Aborted!");
			}
			
			// exit registration regardless
			setClientState(client, "");
			setLoginData(client, null);
		}
	}
	
	private void handle_account_login(final String arg, final Client client) {
		System.out.println("handle_account_login");

		final Player player = getPlayer(client);

		final LoginData data = loginData.get(client);

		// state indicator: is the server responding to our input or spitting
		// data about what to input next
		// what is our input affecting at the moment
		// we need store transitory data: state, username, password

		if (data != null) {
			// TODO figure out if this test is necessary and why
			if (data != loginData.get(client)) {
				send("Object in map is not the same as the stored retrieved result???", client);
			}
			
			final String state = data.state;

			System.out.println("state: " + state);
			System.out.println("username: " + data.username);
			System.out.println("password: " + data.password);

			if ( state.equals(Constants.USERNAME) ) {
				data.username = arg;
				data.state = Constants.PASSWORD;

				send("Password? ", client);
			} 
			else if ( state.equals(Constants.PASSWORD) ) {
				data.password = arg;
				data.state = Constants.AUTHENTICATE;

				handle_account_login("", client);
			}
			else if ( state.equals(Constants.AUTHENTICATE) ) {
				loginData.remove(client);

				// TODO create the account handler
				//final Account account1 = acctMgr.getAccount(data.username, Utils.hash( data.password ) );
				final Account account1 = acctMgr.getAccount(data.username, data.password);

				if (account1 != null) {
					// if there is no active player or multiplay is allowed
					if (account1.getPlayer() == null || multiplay == 1) {
						caTable.put(client, account1);

						// final Account account = caTable.get(client);

						account_menu(account1, client);

						setClientState(client, "account_menu");
					}
					else {
						send("That account is already logged-in!", client);

						setClientState(client, "");
					}
				}
				else {
					send("No Such Account!", client);

					setClientState(client, "");
				}
			}
		}
		else {
			if (arg.equals("account")) {
				loginData.put(client, new LoginData("USERNAME"));

				send("Username? ", client);
			}
			else {
				send("loginData is NULL", client);
			}
		}
	}

	private void handle_account_menu(final String input, final Client client) {
		debug("HANDLE ACCOUNT MENU");
		
		// pull account data for the client
		final Account account = caTable.get(client);

		if (account != null) {
			char ch = input.charAt(0);

			// valid: NEW, LINK, UNLINK, REORDER, DELETE (12-4-2015)
			switch (ch) {
			case 'n':
			case 'N': // New Character
				setClientState(client, "account_menu_new");
				send("Account Action -> New Character (Not Implemented)", client);
				setClientState(client, "account_menu");
				break;
			case 'l':
			case 'L': // Link Character
				setClientState(client, "account_menu_link");
				send("Account Action -> Link Character (Not Implemented)", client);
				setClientState(client, "account_menu");
				
				// TODO properly implement account character linking
				//requestInput("Character Name? ", client);
				break;
			case 'u':
			case 'U': // Unlink Characters
				setClientState(client, "account_menu_unlink");
				send("Account Action -> Unlink Character (Not Implemented)", client);
				setClientState(client, "account_menu");
				break;
			case 'r':
			case 'R': // Reorder Characters
				setClientState(client, "account_menu_reorder");
				send("Account Action -> Reorder Characters (Not Implemented)", client);
				setClientState(client, "account_menu");
				break;
			case 'e':
			case 'E': // Enter Description
				send("Account Action -> Enter Description (Not Implemented)", client);
				break;
			case 'd':
			case 'D': // Delete Character
				setClientState(client, "account_menu_del");
				send("Account Action -> Delete Character (Not Implemented)", client);
				setClientState(client, "account_menu");
				break;
			case 'c':
			case 'C': // Change Password
				send("Account Action -> Change Password (Not Implemented)", client);
				break;
			case 'q':
			case 'Q': // Quit
				send("Goodbye.", client);
				setClientState(client, ""); // clear client state
				init_disconn(client, false); // disconnect client
				break;
			default: // Login the specified character
				int c = Utils.toInt(input, -1);

				if (c != -1) {
					if( handle_login(account, c, client) ) {
						setClientState(client, ""); // clear client state
					}
				}
				break;
			}
		}
		else {
			send("No such account!", client);
		}
	}

	/**
	 * Do the login steps for the selected character on the specified account.
	 * 
	 * @param account the account in question
	 * @param c       index/number to specify a character
	 * @param client  the client which is connected
	 */
	private boolean handle_login(final Account account, final Integer c, final Client client) {
		debug("LOGIN HANDLER");
		
		boolean success = false;
		boolean can_login = false;
		
		String message = "";
		
		final List<Player> characters = account.getCharacters();
		
		Player player;
		
		try {
			player = characters.get(c);
		}
		catch(final IndexOutOfBoundsException ioobe) {
			System.out.println("--- Stack Trace ---");
			ioobe.printStackTrace();
			
			player = null;
		}

		if (player != null) {
			if (account.getPlayer() != player) {
				// Open Mode
				if (mode == GameMode.NORMAL) {
					can_login = true;
				}
				// Wizard-Only Mode
				else if (mode == GameMode.WIZARD) {
					boolean isWizard = (player.getAccess() == Constants.WIZARD); 
					
					if ( isWizard ) can_login = true;
					else            message = "Sorry, only Wizards are allowed to login at this time.";
				}
				else if (mode == GameMode.MAINTENANCE) {
					message = "Sorry, the mud is currently in maintenance mode.";
				}
				else {
					message = "Sorry, you cannot connect to the mud at this time. Please try again later.";
				}
				
				if( can_login ) {
					init_conn(player, client, false);
					success = true;
				}
				else send(message, client);
			}
			else {
				send("That player is already in use!", client);
			}
		}
		else {
			send("No such player!", client);
		}
		
		return success;
	}
	
	/**
	 * 
	 * @param account
	 * @param action
	 * @param data
	 * @param client
	 */
	public void handle_account_action(final Account account, final String action, final Data data, final Client client) {
		if( account != null && data != null ) {
			if( action.equals("change_password") ) {
				final String password = (String) data.getObject("newPass");
				
				account.setPassword(password);
				
				send("Account Password Changed!", client);
			}
			else {
				// are we going to store a string for the player name or something else
				final Player player = (Player) data.getObject("player");
				final Boolean authorized = (Boolean) data.getObject("authorized");
				
				if( player != null ) {
					switch(action) {
					case "new_character":
						new_character(account, null);
						break;
					case "link_character":
						link_character(account, null);
						break;
					case "unlink_character":
						unlink_character(account, null);
						break;
					case "reorder_characters":
						break;
					case "enter_description":
						break;
					case "delete_character":
						delete_character(account, null);
						break;
					default:
						break;
					}
				}
				else {
					send("No such player.", client);
				}
			}
		}
	}
	
	/**
	 * handle_mail
	 * 
	 * Input handler for writing mail interactively.
	 * 
	 * @param input
	 * @param client
	 * @return
	 */
	public Mail handle_mail(final String input, final Client client) {
		// TODO resolve the issue of where to send the new mail object since I
		// can't return it because this is a handler
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
		if (input.equals(".")) { // send the mail
			final String sender = (String) data.getObject("sender");
			final String recipient = (String) data.getObject("recipient");
			final String subject = (String) data.getObject("subject");
			final String message = (String) data.getObject("message");
			
			final Mail mail = new Mail(-1, sender, recipient, subject, message, getDate().toString(), Mail.UNREAD);

			// exit
			send("< Exiting... >", client);

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);

			sendMail(mail, getPlayer(recipient));

			return mail;
		}
		else if (input.equals("~")) { // abort sending
			// exit
			send("< Exiting... >", client);

			// reset editor and player status
			player.setStatus((String) data.getObject("pstatus"));
			player.setEditor(Editors.NONE);

			return null;
		}
		else {
			switch (step) {
			case START:
				client.writeln("Writing a new mail message...");
				send("Send: '.' Abort '~'", client);
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
				break;
			case DONE:
				send("Done.", client);
				break;
			default:
				break;
			}
		}

		return null;
	}

	// logged-in player check
	public boolean loginCheck(final Client client) {
		if (client == null) return false;

		// we only want to go through player references of those who are logged in
		for (final Player p : players) {
			if (p.getClient().equals(client)) {
				return true;
			}
		}

		return false;
	}

	// Object "Retrieval" Functions

	/* Start - DB Retrieval */
	
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
	 * get object specified by name, which is located in the specified room
	 * 
	 * @param name
	 * @param room
	 * @return
	 */
	public MUDObject getObject(final String name, final Room room) {
		final List<MUDObject> objects = objectDB.getByRoom(room);

		debug(room.getName() + ": " + objects);

		for (final MUDObject obj : objects) {
			debug("getObject: " + obj.getName());

			// TODO is this comparison fair?
			if (obj.getName().equals(name)) {
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
	public MUDObject getObject(final Integer dbref) {
		return objectDB.getById(dbref);
	}

	/**
	 * get exit specified by name
	 * 
	 * @param exitName
	 * @param client
	 * @return
	 */
	public Exit getExit(final String exitName) {
		return this.objectDB.getExit(exitName);
	}

	public Exit getExit(final String exitName, final Room room) {
		// Exit exit;

		// ArrayList<Integer> eNums = new ArrayList<Integer>();

		// look through the present room's exits
		// for (int e = 0; e < room.getExits().size(); e++) {
		for (final Exit exit : room.getExits()) {
			// exit = (Exit) room.getExits().get(e);
			if (exit.getName().toLowerCase().equals(exitName)) {
				return exit;
			}
			/*
			 * else { eNums.add(e); }
			 */
		}

		// look through all the exits (would be great if this could ignore
		// previously searched exits
		// perhaps by dbref (since that's much shorter than holding object
		// references, etc
		/*
		 * for (int e = 0; e < exits1.size(); e++) { if (!eNums.contains(e)) {
		 * exit = (Exit) exits1.get(e);
		 * 
		 * if (exit.getName().toLowerCase().equals(exitName)) { return exit; } }
		 * }
		 */

		return null;
	}

	public Exit getExit(final Integer dbref) {
		return objectDB.getExit(dbref);
	}

	public Exit getExit(final Integer dbref, final Room room) {

		// look through the present room's exits first
		for (final Exit e : room.getExits()) {
			if (e.getDBRef() == dbref) {
				return e;
			}
		}

		// look through all the exits (would be great if this could ignore
		// previously searched exits
		// perhaps by dbref (since that's much shorter than holding object
		// references, etc
		return objectDB.getExit(dbref);
	}
	
	/**
	 * Get the named item, if it's present in the specified player's inventory
	 * 
	 * NOTE: these are kind of important for containers, but also for general examine
	 * 
	 * @param name
	 * @param player
	 * @return
	 */
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
	 * Get a list of stackable items/item stack in the specified player's inventory.
	 * 
	 * @param name   item name
	 * @param player player whose inventory we're checking
	 * @return
	 */
	public List<Item> getStackableItems(final String name, final Player player) {
		List<Item> items = new LinkedList<Item>();

		for (final Item item : player.getInventory()) {
			if (item.getName().equals(name)) {
				if (item instanceof Stackable) {
					items.add(item);
				}
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
	 * Get an Item by it's dbref number. Will fail (return null) if the object
	 * in the database is not an Item.
	 * 
	 * @param dbref
	 * @return
	 */
	public Item getItem(final Integer dbref) {
		return objectDB.getItem(dbref);
	}

	// TODO resolve the issue surrounding this, which is that there might be
	// "identical items"
	/*
	 * public Item getItem(final String name) { return objectDB.getItem(name); }
	 */

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
	
	public List<NPC> getNPCList() {
		return objectDB.getNPCs();
	}

	/**
	 * Get a Player (player character) object by name.
	 * 
	 * NOTE: only retrieves from logged-in players
	 * WARNING: never call before confirming logged in player using loginCheck()
	 * 
	 * @param name
	 * @return
	 */
	public Player getPlayer(final String name) {
		return getPlayer(name, true);
	}
	
	/**
	 * Get a Player (player character) object by name.
	 * 
	 * @param name
	 * @param online
	 * @return
	 */
	public Player getPlayer(final String name, final Boolean online) {
		debug("Searching for player by name...");
		debug("\"" + name + "\"", 2);
		
		Player player = null;
		
		if( online ) {
			for (final Player p : this.players) {
				// TODO: how to handle cnames? how does the command know if they are enabled?
				//if (player.getName().equals(name) || player.getCName().equals(name)) {
				// how important is true equality?
				if ( p.getName().equals(name) ) {
					debug("Argument:     " + name);
					debug("Player Name:  " + p.getName());
					debug("Player CName: " + p.getCName());

					player = p;
				}
			}
		}
		else {
			player = objectDB.getPlayer(name);
		}
		
		return player;
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
		return getPlayer(dbref, true);
	}
	
	public Player getPlayer(final Integer dbref, final Boolean online) {
		Player player = null;
		
		if( online ) {
			for (final Player p : this.players) {
				if (p.getDBRef() == dbref) {
					player = p;
				}
			}
		}
		else player = objectDB.getPlayer(dbref);
		
		return player;
	}
	
	

	/**
	 * Get a Player (player character) object by client.
	 * 
	 * @param client
	 * @return
	 */
	public Player getPlayer(Client client) {
		// debug("Searching for player by client...", 3);
		// debug("\"" + client + "\"", 3);

		final Player p = sclients.get(client);

		// ensure that if we are controlling someone, then that
		// is the player current tied to the client
		if (p != null) {
			if (p.isController()) {
				return playerControlMap.getSlave(p);
			}
		}

		return p;
	}

	/**
	 * function to get a room reference for the logged on player's location, if
	 * there is a logged-on player with a location.
	 * 
	 * WARNING: never call before confirming logged in player using loginCheck()
	 * 
	 * @param client
	 * @return
	 */
	/*
	 * public Room getRoom(final Client client) { Player player =
	 * getPlayer(client);
	 * 
	 * if (player != null) { return getRoom( player.getLocation() ); }
	 * 
	 * return null; }
	 */

	/**
	 * Get a room by it's name.
	 * 
	 * @param roomName
	 * @return
	 */
	public Room getRoom(final String roomName) {
		return objectDB.getRoomByName(roomName);
	}

	/**
	 * Get a room by it's database reference number.
	 * 
	 * @param objDBREF
	 * @return
	 */
	public Room getRoom(final Integer dbref) {
		return objectDB.getRoomById(dbref);
	}

	/**
	 * function to get a thing reference for the logged on player, if there is a
	 * logged-on player
	 * 
	 * WARNING: never call before confirming logged in player using loginCheck()
	 * 
	 * @param arg
	 * @param client
	 * @return
	 */
	public Thing getThing(final String name, final Room room) {
		for(final Thing thing : objectDB.getThingsForRoom(room)) {
			if( thing.getName().equals(name) ) {
				return thing;
			}
		}
		
		return null;
	}
	
	/* End - DB Retrieval */

	public Bank getBank(final String bankName) {
		return this.banks.get(bankName);
	}

	/* Saving Objects */

	/*
	 * Persistence Routines
	 */

	/* Data Saving Functions */

	public void saveAccounts() {
		/*for (final Account a : acctMgr.getAccounts()) {
			String[] temp = new String[11];

			temp[0] = "" + a.getCreated();
			temp[1] = "" + a.getModified();
			// temp[2] = "" + a.getArchived();
			temp[2] = "";

			temp[3] = "" + a.getI d();
			temp[4] = "" + a.getStatus().ordinal();
			temp[5] = a.getUsername();
			temp[6] = a.getPassword();

			temp[7] = "" + a.getCharLimit();

			temp[8] = "" + a.isDisabled();

			temp[9] = "" + a.getLastIPAddress();

			final StringBuilder sb = new StringBuilder();

			int index = 1;

			for (final Player p : a.getCharacters()) {
				sb.append(p.getName());
				
				if (index < a.getCharacters().size()) sb.append(", ");
				
				index++;
			}

			temp[10] = sb.toString();

			for (final String s : temp) {
				System.out.println(s);
			}

			System.out.println("");
		}*/

		FileOutputStream fos;
		ObjectOutputStream oos;
		
		GsonBuilder builder = new GsonBuilder();
		
		builder.setPrettyPrinting();
		
		Gson gson = builder.create();

		for (final Account acct : acctMgr.getAccounts()) {
			try {
				fos = new FileOutputStream( resolvePath(ACCOUNT_DIR, acct.getUsername() + ".acct") );
				oos = new ObjectOutputStream(fos);
				
				System.out.println( gson.toJson( acct ) );
				
				oos.writeObject(acct);

				oos.close();
			}
			catch (final FileNotFoundException fnfe) { fnfe.printStackTrace(); }
			catch (final Exception e)                { e.printStackTrace(); }
		}
	}

	/**
	 * Save Database
	 */
	public void saveDB() {
		// save databases to disk, modifies 'real' files
		save(DB_FILE);
		send("Done");
	}
	
	/**
	 * Save Database (to a specific file, possibly distinct from the original one loaded)
	 */
	public void saveDB(final String filename) {
		// save databases to disk, modifies 'real' files
		save( resolvePath(BACKUP_DIR, filename) );
		send("Done");
	}

	public void saveJSON() {
		GsonBuilder builder = new GsonBuilder(); // Or use new GsonBuilder().create();

		builder.setPrettyPrinting();

		Gson gson = builder.create();

		for(final MUDObject object : objectDB.getObjects()) {
			System.out.println(object.getDBRef() + " : " + object.getName());

			//final String fileName = DATA_DIR + "json\\" + object.getDBRef() + ".json";
			final String fileName = object.getDBRef() + ".json";;
			final String filePath = resolvePath(DATA_DIR, "json", fileName);

			Utils.saveStrings(filePath, new String[] { gson.toJson(object) });
		}
	}

	public void saveHelpFiles() {
		synchronized (this.helpTable) {
			for (final Entry<String, String[]> hme : this.helpTable.entrySet()) {
				debug("Saving " + HELP_DIR + hme.getKey() + ".help" + "... ");
				
				Utils.saveStrings( resolvePath(HELP_DIR, hme.getKey() + ".help"), hme.getValue() );
			}
		}
	}

	public void saveTopicFiles() {
		synchronized (this.topicTable) {
			for (final Entry<String, String[]> tme : this.topicTable.entrySet()) {
				debug("Saving " + TOPIC_DIR + tme.getKey() + ".topic" + "... ");
				
				Utils.saveStrings( resolvePath(TOPIC_DIR, tme.getKey() + ".topic"), tme.getValue() );
			}
		}
	}

	/**
	 * Generally speaking the purpose of this would be to save any changes made
	 * to spells and any new spells created within the game.
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
		for (final BulletinBoard board : boards.values()) {
			saveBoard(board);
		}
	}

	public void saveBoard(final BulletinBoard board) {
		/*
		 * File file; RandomAccessFile raf;
		 * 
		 * try { System.out.println(BOARD_DIR + board.getFilename());
		 * 
		 * file = new File(BOARD_DIR + board.getFilename()); raf = new
		 * RandomAccessFile(file, "rws");
		 * 
		 * String line = ""; long pos = 0;
		 * 
		 * int message = 0;
		 * 
		 * while(raf.getFilePointer() < raf.length()) { pos =
		 * raf.getFilePointer(); line = raf.readLine();
		 * 
		 * System.out.println("current position: " + pos);
		 * System.out.println(line); } } catch(Exception e) {
		 * e.printStackTrace(); }
		 */

		// I just want to overwrite the board file with the current state
		System.out.println(BOARD_DIR + board.getFilename());

		File file;
		
		// TODO resolvePath(...) is ugly as sin
		try {
			file = new File( resolvePath(BOARD_DIR, board.getFilename()) );
		}
		catch (final NullPointerException npe) {
			// TODO do I really need to catch an exception here?
			System.out.println("--- Stack Trace ---");
			npe.printStackTrace();
		}
	}

	/* Data Loading Functions */

	/**
	 * loadAccounts
	 * 
	 * Read in serialized account data from files in the account directory,
	 * create account objects, and then add them to the account manager
	 * 
	 * @param account_dir
	 */
	public void loadAccounts(String account_dir) {
		// TODO find a more effective way to load account data
		System.out.println("ACCOUNT_DIR: " + account_dir);
		
		final File dir = new File(account_dir);
		
		if ( !dir.isDirectory() ) {
			System.out.println("Invalid Account Directory!");
			return;
		}
		
		System.out.println(dir.listFiles());
		
		Account account = null;
		
		ObjectInputStream ois;
		
		final FilenameFilter af = new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".acct");
			}
		};
		
		GsonBuilder builder = new GsonBuilder(); // Or use new GsonBuilder().create();
		
		// NOTE: default representation of Date isn't bad...
		//builder.registerTypeAdapter(Account.class, new AccountAdapter());
		
		builder.setPrettyPrinting();

		Gson gson = builder.create();
		
		// TODO use a FilenameFilter to get only .acct files, what is the best listing method?
		for (final File file : dir.listFiles(af)) {
			//if (file.isFile() && file.getName().endsWith(".acct")) {
			if ( file.isFile() ) {
				System.out.println("Account File Found: " + file.getName());

				try {
					ois = new ObjectInputStream(new FileInputStream(file));

					account = (Account) ois.readObject();

					System.out.println("ID: " + account.getId());
					System.out.println("Username: " + account.getUsername());

					final int aId = account.getId();
					Account.Status aStatus = account.getStatus();

					final Date aCreated = account.getCreated();
					Date aModified = account.getModified();

					String aUsername = account.getUsername();
					String aPassword = account.getPassword();

					final int aCharLimit = account.getCharLimit();

					Account account2 = new Account(aId, aStatus, aCreated, aModified, aUsername, aPassword, aCharLimit, new Player[0]);

					// connect players to their respective account
					Player player = objectDB.getPlayer(account.getUsername());
					// NOTE: ^ use DB because players still ought to be account
					// associated even if not logged in

					if (player != null) account2.linkCharacter(player);
					
					System.out.println( gson.toJson(account2) );

					acctMgr.addAccount(account2);

					/*
					 * while( ois.available() > 0 ) { account = (Account)
					 * ois.readObject();
					 * 
					 * for(Player player : players) { // TODO figure out way
					 * connect players and the accounts they belong to }
					 * 
					 * acctMgr.addAccount( account ); }
					 */

					ois.close();
				}
				catch (final FileNotFoundException fnfe) {
					System.out.println("--- Stack Trace ---");
					fnfe.printStackTrace();
				}
				catch (final ClassNotFoundException cnfe) {
					System.out.println("--- Stack Trace ---");
					cnfe.printStackTrace();
				}
				catch (final IOException ioe) {
					System.out.println("--- Stack Trace ---");
					ioe.printStackTrace();
				}
			}
		}

		/*
		 * LinkedList<Integer> ids = new LinkedList<Integer>();
		 * 
		 * for(Integer i : iamap.keySet()) { System.out.println(i); ids.add(i);
		 * }
		 * 
		 * if( !ids.isEmpty() ) { Collections.sort(ids);
		 * System.out.println(ids); last_account_id = ids.getLast(); } else
		 * last_account_id = 0;
		 */
	}

	/**
	 * loadAliases
	 * 
	 * Load command aliases from a file
	 * 
	 * @param filename
	 */
	public void loadAliases(String filename) {
		// format: 'alias <command>:<alias list>'
		debug("Loading aliases");
		
		for (final String _line : Utils.loadStrings(filename)) {
			final String line = Utils.trim(_line);

			if ( line.startsWith("#") ) continue; // treat '#' as meaning ignore line
			
			final List<String> data = Utils.mkList(line.split(" "));
			
			if( data.size() == 2 ) {
				final String directive = data.get(0);
				final String[] params = data.get(1).split(":");
				
				if( directive.equalsIgnoreCase("alias") ) {
					final String command = params[0]; // command
					final String aliases = params[1]; // comma separated list of aliases
					
					final List<String> aliasList = Utils.mkList( aliases.split(",") ); 
					
					for (final String alias : aliasList) {
						this.aliases.put(alias, command);
					}
				}
			}
		}

		debug("Aliases loaded.");
		debug("");
	}

	/**
	 * Generate an item from it's database representation
	 * 
	 * NOTE: I should be able to use to make a new copy of a prototyped item
	 * stored on disk
	 * 
	 * @param itemData
	 * @return an item object
	 */
	public Item loadItem(String itemData) {
		String[] attr = itemData.split("#");

		Integer oDBRef = 0, oLocation = 0;
		String oName = "", oFlags = "", oDesc = "";

		oDBRef = Integer.parseInt(attr[0]); // 0 - item database reference
		// number
		oName = attr[1]; // 1 - item name
		oFlags = attr[2]; // 2 - item flags
		oDesc = attr[3]; // 3 - item description
		oLocation = Integer.parseInt(attr[4]); // 4 - item location

		ItemType itemType = ItemTypes.getType(Integer.parseInt(attr[5]));

		debug("Database Reference Number: " + oDBRef);
		debug("Name: " + oName);
		debug("Flags: " + oFlags);
		debug("Description: " + oDesc);
		debug("Location: " + oLocation);

		Item item = new Item(oDBRef, oName, ObjectFlag.getFlagsFromString(oFlags), oDesc, oLocation);

		// TODO figure out what I am trying to do here
		// item.setItemType(itemType);

		return item;
	}

	/**
	 * Go through all the items that exist in the database and place them inside the
	 * respective object they are located in (Room, Player)
	 */
	public void loadItems() {
		objectDB.addItemsToRooms();
		objectDB.addItemsToContainers();
		
		// add items to players?
		for (final Entry<Item, Player> entry : objectDB.getItemsHeld().entrySet()) {
			final Item item = entry.getKey();
			final Player player = entry.getValue();

			debug(Utils.padRight("" + item.getDBRef(), ' ', 4) + " " + item.getName());
			debug(Utils.padRight("" + item.getLocation(), ' ', 4) + " " + player.getName());

			if (player instanceof NPC) {
				final NPC npc = (NPC) player;
				
				if (npc instanceof Merchant) {
					((Merchant) npc).addToStock(item);
					
					debug("Merchant (" + npc.getName() + ")");
				}
			}
			else {
				player.getInventory().add(item);
			}

			debug("Item Loaded", 2);
		}
	}

	/**
	 * Go through all the exits that exist in the database and
	 * place/attach them in/to the respective rooms they are part of
	 */
	public void loadExits() {
		debug("Loading exits:", 2);

		for (final Exit exit : objectDB.getExits()) {
			if( exit.getExitType() == ExitType.DOOR ) {
				final Room room = objectDB.getRoomById(exit.getLocation());

				if (room != null) {
					room.addExit(exit);
					debug("Exit " + Utils.padLeft("" + exit.getDBRef(), ' ', 4) + " added to room " + room.getDBRef() + ". (Door)", 2);
				}

				final Room room1 = objectDB.getRoomById(exit.getDestination());

				if (room1 != null) {
					room1.addExit(exit);
					debug("Exit " + Utils.padLeft("" + exit.getDBRef(), ' ', 4) + " added to room " + room1.getDBRef() + ". (Door)", 2);
				}
			}
			else {
				final Room room = objectDB.getRoomById(exit.getLocation());

				if (room != null) {
					room.addExit(exit);
					debug("Exit " + Utils.padLeft("" + exit.getDBRef(), ' ', 4) + " added to room " + room.getDBRef() + ".", 2);
				}
				
				// PORTAL
				if( exit.getExitType() == ExitType.PORTAL ) {
					final Portal portal = (Portal) exit;
					
					portals.add(portal);
					
					switch(portal.getPortalType()) {
					case STD:
						getRoom(portal.getOrigin()).addSayEventListener(portal);
						getRoom(portal.getDestination()).addSayEventListener(portal);
						break;
					case RANDOM:
						getRoom(portal.getOrigin()).addSayEventListener(portal);
						break;
					default:
						break;
					}
				}
			}
		}

		debug("Done loading exits:", 2);
	}

	public ArrayList<String> loadListDatabase(String filename) {
		String[] string_array; // create string array
		ArrayList<String> strings; // create arraylist of strings

		string_array = Utils.loadStrings(filename);

		strings = new ArrayList<String>(string_array.length);

		for (int line = 0; line < string_array.length; line++) {
			// we will completely drop any empty lines, as they are irrelevant
			if( !string_array[line].isEmpty() ) {
				if (string_array[line].charAt(0) != '#')
					strings.add(string_array[line]);
				else
					debug("-- Skip - Line Commented Out --", 2);
			}
		}

		return strings;
	}

	public void loadSpells(final String[] temp) {
		// name#cast message#duration#effects#reagents#targets
		// name: eagles_splendor
		// cast message: You cast Eagle's Splendor
		// duration?: instant
		// effects: cha+4
		// reagents: none
		// targets: self,friend
		
		for (final String line : temp) {
			final String[] args = line.split("#");
			
			final String tName = args[0];
			final String tCastMsg = args[1];
			final String tType = args[2];
			
			final String[] tEffects = args[3].split(",");
			final String[] tReagents = args[4].split(",");
			final String[] tTargets = args[5].split(",");

			final List<Effect> spell_effects = new ArrayList<Effect>();

			for (final String s : tEffects) {
				spell_effects.add( new Effect(s) );
			}

			final List<Reagent> spell_reagents = new ArrayList<Reagent>();

			for (final String reagentName : tReagents) {
				if( !reagentName.equals("none") ) {
					spell_reagents.add( new Reagent(reagentName) );
				}
			}
			
			//
			final Spell newSpell = new Spell(tName, SpellType.ARCANE, School.ENCHANTMENT, 0, tCastMsg, spell_effects, spell_reagents);
			
			newSpell.setTargets( Spell.encodeTargets(tTargets) );
			
			spells2.put(tName, newSpell);
			
			debug( String.format("Spell: %s (Targets: %s)", newSpell.getName(), String.join(",", Spell.decodeTargets( newSpell ))) );
			debug("Type: " + tType);
			debug("Effects: " + tEffects);
			debug("Cast Message: " + tCastMsg);
			
			//debug(tName + " " + tCastMsg + " " + tType + tEffects);
		}
	}

	// It would be infinitely better to use JSON or the standard
	// java.util.Properties.
	public void loadTheme(final String themeFile) {
		if (themeFile == null || "".equals(themeFile)) {
			debug("Not loading theme, filename: " + themeFile);
			return;
		}

		debug("Loading theme, filename: " + themeFile);

		String section = "";

		for (final String line : Utils.loadStrings(themeFile)) {
			// skip blank lines
			if ("".equals(line) || line.trim().startsWith("//")) {
				continue;
			}

			if (line.startsWith("[/")) { // end section
				// if end tag doesn't match current section
				if (!line.substring(2, line.length() - 1).equals(section)) {
					throw new IllegalStateException("Theme section is " + section + " but ending tag is " + line);
				}
				else {
					section = ""; // clear section
					//debug("Leaving " + section);
				}
			}
			else if (line.startsWith("[")) { // start section
				section = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
				//debug("Entering " + section);
				debug("** " + section);
			}
			else if (line.indexOf(" = ") == -1) { // start section
				throw new IllegalStateException("Theme line is not section name, but missing \" = \": " + line);
			}
			else {
				final String[] parts = line.split(" = ", 2);

				if( parts.length != 2 ) continue;

				final String var = parts[0];
				final String val = parts[1];
				
				// TODO debug or not?
				//System.out.println("" + var + ": \'" + val + "\'");

				if (section.equals("theme")) {
					// TODO should db be a variable in Theme?
					// TODO more nasty path kludge... conflict between file/dir names and needing the full path..
					if(var.equals("name") )            ;
					else if (var.equals("mud_name"))   mud_name = val;
					else if (var.equals("motd_file"))  motd_file = val;
					else if (var.equals("start_room")) start_room = Utils.toInt(val, 0);
					else if (var.equals("module"))     ;
					else if (var.equals("world"))      world = val;
					//else if (var.equals("db"))         this.DB_FILE = DATA_DIR + "databases\\" + val;
					else if (var.equals("db"))         this.DB_FILE = resolvePath(DATA_DIR, "worlds", world, val);
					else {
						debug("Theme Loader (" + section + "): unknown var \'" + var + "\' with value of \'" + val + "\'.");
					}
				}
				else if (section.equals("calendar")) {
					// NOTE: could I set the day/month/year on time loop in here?
					debug(line);
					if (var.equals("day"))         day = Utils.toInt(val, 0);
					else if (var.equals("month"))  month = Utils.toInt(val, 0);
					else if (var.equals("year"))   year = Utils.toInt(val, 0);
					else if (var.equals("season")) season = Seasons.fromStringLower(val);
					else if (var.equals("reckon")) reckoning = val;
					else {
						debug("Theme Loader (" + section + "): unknown var \'" + var + "\' with value of \'" + val + "\'.");
					}
				}
				else if (section.equals("months")) {
					final int monthIndex = Utils.toInt(var, -1) - 1;
					MONTH_NAMES[monthIndex] = val;
					debug("Month " + Utils.padRight("" + monthIndex, ' ', 2) + " set to \"" + val + "\"");
				}
				else if (section.equals("months_alt")) {
				}
				else if (section.equals("holidays")) {
					/*final String[] dateInfo = var.split(",");

					final int m = Utils.toInt(dateInfo[0], 0);
					final int d = Utils.toInt(dateInfo[1], 0);

					debug(Utils.padRight(MONTH_NAMES[m], ' ', 8) + " " + Utils.padRight(d, ' ', 2) + " "   , 2);*/

					// day, month = holiday name/day name
					// E.g.: 9,21 = Autumn Equinox

					final int month = Integer.parseInt(Utils.trim(var.split(",")[0]));
					final int day = Integer.parseInt(Utils.trim(var.split(",")[1]));

					final String hname = Utils.trim(val);

					debug(Utils.padRight(MONTH_NAMES[month - 1], ' ', 9) + " " + Utils.padRight("" + day, ' ', 2) + " - " + hname, 2);

					holidays.put(hname, new Date(month, day));

					// multi-day holidays not handled very well at all, only one
					// day recorded for now
					// holidays.put(new
					// Date(Integer.parseInt(trim(dateline[1])),
					// Integer.parseInt(trim(dateline[0]))), trim(line[1]));
				}
				else if (section.equals("years")) {
					debug(line, 2);
					years.put(Integer.parseInt(Utils.trim(var)), Utils.trim(val));
				}
			}
		}

		debug("");
		debug("Theme Loaded.");
	}

	/**
	 * loadChannels
	 * 
	 * Load chat channels from the specified file
	 * 
	 * @param filename
	 */
	public void loadChannels(final String filename) {
		File f;
		FileReader fr;
		BufferedReader br;
		
		try {
			f = new File(filename);
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			
			String line = br.readLine();

			while (line != null) {
				debug("LINE: \'" + line + "\'");
				
				if( line.startsWith("#") ) continue; // skip commented out lines
				
				String[] channelData = line.split(",");
				
				if( channelData.length == 3 ) {
					final String channelName = channelData[0].toLowerCase();
					final String shortName = channelData[1].toLowerCase();
					final int channel_perm = Utils.toInt(channelData[2], Constants.USER);
					
					final ChatChannel channel = chan.makeChannel(channelName);
					
					if( channel != null ) {
						channel.setShortName(shortName);

						channel.setRestrict(channel_perm);

						channel.setChanColor("magenta");
						channel.setSenderColor("orange");
						channel.setTextColor("green");
					}
					else {
						debug("Channel is NULL!");
					}

					debug("Channel Added: " + channelName);
				}
				else {
					debug("Invalid Channel Data!");
				}
				
				line = br.readLine();
			}
			
			br.close();
			fr.close();
		}
		catch(final NullPointerException npe) {
			System.out.println("--- Stack Trace ---");
			npe.printStackTrace();
		}
		catch(final FileNotFoundException fnfe) {
			System.out.println("--- Stack Trace ---");
			fnfe.printStackTrace();
		}
		catch(final IOException ioe) {
			System.out.println("--- Stack Trace ---");
			ioe.printStackTrace();
		}
	}
	
	public void saveChannels(final String filename) {
		File f;
		FileWriter fw;
		BufferedWriter bw;
		
		try {
			f = new File(filename);
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
			
			
		}
		catch(final NullPointerException npe) {
			System.out.println("--- Stack Trace ---");
			npe.printStackTrace();
		}
		catch(final FileNotFoundException fnfe) {
			System.out.println("--- Stack Trace ---");
			fnfe.printStackTrace();
		}
		catch(final IOException ioe) {
			System.out.println("--- Stack Trace ---");
			ioe.printStackTrace();
		}
	}

	public void loadSessionData(Player p) {

	}
	
	/* Other Stuff */
	
	/**
	 * For each npc, every one that is either a WeaponMerchant or an
	 * ArmorMerchant will be stocked with a default set of merchandise if they
	 * have NO stock.
	 */
	public void fillShops() {
		for (final NPC npc : getNPCList()) {
			// Merchants
			if (npc instanceof Merchant) {
				Merchant m = (Merchant) npc;

				if (m.getStock().size() == 0) { // no merchandise
					// TODO merchant type rendered defunct, need other way to know how to restock
					/*if (m.getMerchantType().equals("armor")) {
						m.setStock( createItems(new Armor(0, ArmorType.CHAIN_MAIL), 10) );
					}
					else if (m.getMerchantType().equals("weapon")) {
						m.setStock( createItems(new Weapon(WeaponTypes.LONG_SWORD), 10) );
					}*/

					for (final Item item : m.getStock()) {
						int l = item.getLocation();
						item.setLocation(m.getDBRef());

						StringBuilder sb = new StringBuilder();

						sb.append("Item #").append( item.getDBRef() ).append(" ");
						sb.append("had Location #").append(l).append(" ");
						sb.append("and is now at location #").append( item.getLocation() );

						debug( sb.toString() );
					}
				}
			}


			/*else if (npc instanceof Innkeeper) {
				Innkeeper ik = (Innkeeper) npc;

				if (ik.stock.size() == 0) {
					// no merchandise ik.stock =
					createItems(new Book("Arcani Draconus"), 10);

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
	 * Go through all the things that exist in the database and place them in
	 * the respective rooms they are located in
	 */
	public void placeThingsInRooms() {
		objectDB.placeThingsInRooms(this);
	}

	/**
	 * MOTD - Message of The Day
	 * 
	 * Returns the messages of the day a string which converted from a byte
	 * array loaded from a file.
	 * 
	 * @return String - the message of the day
	 */
	public String messageOfTheDay() {
		// TODO even MORE path kludges
		//return new String( Utils.loadBytes(WORLD_DIR + world + "\\motd\\" + motd_file) );
		//byte[] temp = Utils.loadBytes(WORLD_DIR + world + "\\motd\\" + motd_file);
		byte[] temp = Utils.loadBytes( resolvePath(WORLD_DIR, world, "motd", motd_file) );
		
		String motd;
		
		if( temp.length > 0 ) motd = new String( temp );
		else                  motd = "No MOTD File found.";
		
		return motd;
	}

	/**
	 * On-Connect Properties Evaluation
	 * 
	 * Evaluates scripted properties/attributes on the player when they connect
	 * 
	 * @param client
	 * @return
	 */
	public void cProps(final Player player) {
		// create string array to store results of evaluated props
		String[] results = new String[0];

		// get user properties map
		final Map<String, String> props = player.getProperties();
		
		boolean connectProps = false;

		// get connection properties from user properties array
		for (final String key : props.keySet()) {
			if (key.startsWith("_connect")) {
				final String property = props.get(key);

				if (property != null) {
					if( !connectProps ) connectProps = true;
					
					int initial = property.indexOf("/");
					String test = property.substring(initial, property.indexOf("/", initial));
					System.out.println(test);

					System.out.println(property);

					debug("Connect Property Found!");
					
					final Script script = new Script(property);
					final String result = pgm.interpret(script, player, null);
					
					send(result, player.getClient());
				}
			}
		}

		if (!connectProps) {
			debug(player.getName() + ": No connection properties/scripts.");
		}
	}

	/**
	 * On-Disconnect Properties Evaluation
	 * 
	 * Evaluates scripted properties/attributes on the player when they
	 * disconnect
	 * 
	 * @param client
	 *            - a client that corresponds to a player
	 * @return String[]
	 */
	public void dProps(Player player) {
		// create string array to store results of evaluated props
		String[] results = new String[0];

		// get user properties map
		final Map<String, String> props = player.getProperties();
		
		boolean disconnectProps = false;

		// get disconnection properties from user properties array
		for (final String key : props.keySet()) {
			if (key.startsWith("_disconnect")) {
				final String prop = props.get(key);

				if (prop != null) {
					if (!disconnectProps) disconnectProps = true;
					
					int initial = prop.indexOf("/");
					String test = prop.substring(initial, prop.indexOf("/", initial));
					System.out.println(test);

					System.out.println(prop);

					debug("Disconnect Property Found!");

					final Script script = new Script(prop);
					final String result = pgm.interpret(script, player, null);

					send(result, player.getClient());
				}
			}
		}
		
		if (!disconnectProps) {
			debug(player.getName() + ": No connection properties/scripts.");
		}
	}

	/* INIT section */
	/* Connection Handling */

	/**
	 * Initialize Connection
	 * 
	 * takes a player and performs loading operations for them, as well as
	 * logging connections.
	 * 
	 * @param player the player to initialize/load into the game
	 * @param client the connecting client
	 * @param newCharacter is this a new character
	 */
	public void init_conn(final Player player, final Client client, final boolean newCharacter) {
		if (player == null) {
			debug("ERROR!!!: Player Object is NULL!");
			return;
		}

		if (use_cnames) {
			// generate generic name for unknown players based on their class
			// and the number of players with the same class presently on
			// logged on of a given class
			System.out.println("Generating generic name for player...");

			// TODO deal with number of players online of stuff
			// TODO resolve where I get the numbers from

			// int temp = numPlayersOnlinePerClass.get( player.getPClass() );
			int temp = getNumPlayers(player.getPClass());

			// TODO handle NPC control scenario that causes this to not produce
			// the desired result
			player.setCName(player.getPClass().toString() + temp);

			objectDB.addName(player, player.getCName());
			cNames.put(player.getCName(), player);

			numPlayersOnlinePerClass.put(player.getPClass(), ++temp);

			System.out.println("Generated Name: " + player.getName());

			System.out.println("Done");

			debug("Number of players that share this player's class: " + getNumPlayers(player.getPClass()));
		}

		// NOTE: I should probably add a mapping here somewhere that ties the
		// player to their account, if they have one

		if (newCharacter) { // if new, do some setup
			// send a welcome mail to them

			final int id = player.getMailBox().numMessages() + 1;
			final String msg = "Welcome to " + getName();
			final String dateString = getDate().toString();

			final Mail mail = new Mail(id, "System", player.getName(),"Welcome", msg, dateString, Mail.UNREAD);

			sendMail(mail, player);

			// TODO need an item creation factory and some method in GameModule
			// that handles starting equipment, perhaps this should be part of
			// character generation?
		}

		// get the time
		final Time time = getTime();
		final Date date = getDate();

		// account login state
		final Account account = acctMgr.getAccount(player);

		if (account != null) {
			debug("ACCOUNT EXISTS!");
			
			account.setLastIPAddress( client.getIPAddress() );
			
			account.setClient(client);
			account.setPlayer(player);
			account.setOnline(true);
		}

		/* */
		sclients.put(client, player);

		player.setClient(client);    // need this set so I can ask for it in various other places

		logConnect(player, time);

		// open a new session
		Session session = new Session(client, player);
		session.connect = new Tuple<Date, Time>(date, time);
		session.connected = true;

		sessionMap.put(player, session);

		debug("New session started!");

		// tell the player that their connection was successful
		debug("\nConnected!\n");
		send(colors("Connected!", "yellow"), client);
		//send(colors("Connected to " + serverName + " as " + player.getName(), "yellow"), client);

		/* load the player's mailbox */
		loadMail(player);

		// indicate to the player how much mail/unread mail they have
		client.writeln("Checking for unread messages...");

		int messages = player.getMailBox().numUnreadMessages();

		if (messages == 0) client.writeln("You have no unread messages.");
		else               client.writeln("You have " + String.valueOf(messages) + " unread messages.");

		// list the items in the player's inventory (located "in" the player)
		for (final Item item : player.getInventory()) {
			debug("Item -> " + item.getName() + " (#" + item.getDBRef() + ") @" + item.getLocation());
		}

		debug("");

		/* ChatChannel Setup */
		// TODO clean up the below, where we add players to channels, also we
		// should re-add them to any channels they joined before
		
		String[] chns = { Constants.OOC_CHANNEL, Constants.STAFF_CHANNEL };
		
		List<String> channelNames = Arrays.asList(chns);
		
		for(final String channelName : channelNames) {
			final Result r = chan.add(player, channelName);
			
			String message = "";
			
			switch(r) {
			case JOIN:       message = "INIT_CONN: Joined chat channel.(" + channelName +")"; break;
			case NO_CHANNEL: message = "INIT_CONN: No such chat channel.";                    break;
			case RESTRICTED: message = "INIT_CONN: That chat channel is restricted.";         break;
			case WRONG_PASS: break;
			default:         break;
			}
			
			debug(message);
			client.writeln(message);
		}

		messageQueues.put(player, new LinkedList<String>());

		/* create timer lists for the player */
		this.effectTimers.put(player, new LinkedList<EffectTimer>());
		this.spellTimers.put(player, new LinkedList<SpellTimer>());
		this.auctionTimers.put(player, new LinkedList<AuctionTimer>());

		player.setStatus("IC");
		
		// TODO is this correct placement for player init
		/* initialize the player, especially slots */
		if (module != null) module.PCInit(player);
		else                send("No GameModule configured!", client);
		
		/* add the player to the game */
		this.players.add(player);

		/* run any connect properties specified by the player */
		cProps(player); // really should check permissions..

		/* look at the current room */
		final Room current = getRoom( player.getLocation() ); // determine the room they are in
		current.addListener(player);                          // add them to the listeners group for the room
		look(current, client);                                // show the room
	}

	/**
	 * De-Initialize Connection (Disconnect)
	 * 
	 * @param player
	 * @param client
	 */
	public void init_disconn(final Client client, final Boolean logout) {
		debug("init_disconn(" + client.getIPAddress() + ")");

		// get the player associated with the client
		final Player player = getPlayer(client);

		// if such a player does not exist, then just disconnect the client
		if (player == null) {
			debug("Player not found for client: " + client);
			disconnect(client);
			return;
		}

		final String playerName = player.getName();
		
		// break any current control of npcs
		// TODO fix this kludgy use of a command invocation..
		//cmd_control("#break", client);
		ctl_break( client );

		// remove as listener from room/location
		getRoom( player.getLocation() ).removeListener(player);
		
		// remove from chat channels
		for(final ChatChannel ch : chan.getChatChannels()) {
			if( ch.isListener(player) ) ch.removeListener(player);
		}
		
		// TODO less kludgy route to removing player from channel listens?
		// remove player from joined channels
		/*for(final String channelName : chan.getChannelNames()) {
			if( chan.isPlayerListening(channelName, player) ) {
				try {
					chan.remove(player, OOC_CHANNEL);
				}
				catch(final NoSuchChannelException nsce) {
					debug("INIT_DISCONN: No such chat channel.");
				}
			}
		}*/

		/*
		 * unequip gear
		 * 
		 * If we didn't do this, stuff could get stuck in limbo, alternatively,
		 * we could just loop through the items array and put a new copy of the
		 * references in the inventory, since it's all going to end up back in
		 * the inventory anyway (or at least until I figure out how to persist
		 * information about reloading slots for a player).
		 */

		// Unequipping gear
		final List<Item> inventory = player.getInventory();

		for (final Slot slot : player.getSlots().values()) {
			if (slot.isFull()) {
				if (slot.getItem() != null) inventory.add(slot.remove());
			}
		}

		send("Equipment un-equipped!", client);

		player.setStatus("ZZZ");
		
		// handle account modifications
		if (use_accounts) {
			final Account account = acctMgr.getAccount(player);

			if (account != null) {
				debug("Account ID: " + account.getId());

				account.setPlayer(null);

				if (!logout) {
					account.setClient(null);
					account.setOnline(false);
				}
			}
		}
		
		// get time
		final Time time = getTime();
		final Date date = getDate();
		
		logDisconnect(player, time);

		// this part should only be done when a player enters or exits play/the game
		if (!use_accounts || (use_accounts && logout)) {
			// get session
			Session toRemove = sessionMap.get(player);

			// record disconnect time
			toRemove.disconnect = new Tuple<Date, Time>(date, time);

			// store the session info on disk

			// clear session
			sessionMap.remove(player);
		}

		// if player is a guest
		if (player.hasFlag(ObjectFlag.GUEST)) {
			objectDB.remove(player); // remove from database (replace db entry with NULLObject?)
		}
		else {
			send("Saving mail...", player.getClient());

			saveMail(player); // save mail
		}

		// TODO resolve where I get the numbers from

		// cName handling
		// int temp = numPlayersOnlinePerClass.get( player.getPClass() );
		int temp = getNumPlayers(player.getPClass());
		
		numPlayersOnlinePerClass.put(player.getPClass(), temp--);

		cNames.remove(player.getCName());

		// synchronized to deal with the possibility that someone invoked 'who'
		// and needs to finish iterating through the list
		synchronized (players) {
			players.remove(player); // Remove the player object for the disconnecting player
		}

		// DEBUG: Tell us which character was disconnected
		debug(playerName + " removed from play!");
		
		// if this is a complete disconnect, then disconnect the client as well
		if (!logout) {
			send("Disconnected from " + serverName + "!", client);
			disconnect(client);
		}
	}

	public void telnetNegotiation(Client client) {
		// client.telnet = true; // mark as client as being negotiated with

		int s = 0; // current sub-negotiation? (0=incomplete,1=complete)

		ArrayList<String> options = new ArrayList<String>();

		// options.add("IAC WILL MCCP");
		options.add("IAC IAC DO TERMINAL-TYPE");

		// send some telnet negotiation crap
		// IAC WILL MCCP1
		// 255 251 85
		// -- if --
		// IAC DO MCCP1
		// 255 253 85
		// -- then --
		// IAC SB MCCP1 WILL SE
		// 255 250 85 251 250
		// -- else --
		// IAC DONT MCCP1
		// 255 254 85
		// -- then --
		// IAC SB MCCP1 WONT SE
		// 255 250 85 252 250

		// IAC DO TERMINAL-TYPE
		// 255 253 24
		// -- if --
		// IAC WILL TERMINAL-TYPE
		// 255 251 24
		// -- then --

		for (final String optstr : options) { // all the things we wish to check?
			// i.e. we're going to use these if we can

			// send a message
			Telnet.send(optstr, client);

			// deal with reply
			while (s == 0) {

				// a byte buffer to hold the incoming message (hopefully it's
				// less than 10 bytes)
				byte[] byteBuffer1 = new byte[10];

				// capture the response
				// if (client.available() > 0) {
				// client.readBytes();
				// }

				System.out.println("Response Captured");

				System.out.println("Response:");

				for (byte b : byteBuffer1) {
					System.out.println("Processing...");
					int value = b;
					System.out.println(value);
				}

				// handle the response
				if (byteBuffer[0] == 255) { // if that byte is 255 (IAC - Is A
					// Command)

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

					/*
					 * if ( Telnet.translate(byteBuffer1).equals("IAC DO MCCP")
					 * ) { Telnet.send("IAC MCCP WILL SB", client); } else if (
					 * Telnet.translate(byteBuffer1).equals("IAC DONT MCCP") ) {
					 * Telnet.send("IAC MCCP WONT SB", client); }
					 */

					s = 1;
				}
			}
		}
		// client.telnet = true;
	}

	// EVENT Section
	//

	// event triggered on client connection
	public void clientConnected(final Client someClient) {
		// wait for client to get up and running
		while( !someClient.isRunning() ) ;
		
		send("Connecting from " + someClient.getIPAddress(), someClient);

		// decide if a player (or in this case, IP address) will be allowed to
		// continue connecting
		if (banlist.contains(someClient.getIPAddress())) {
			send("Server> Your IP is banned.", someClient);
			send("Server> Booting client...", someClient);
			someClient.stopRunning();
		}

		// TODO do we need this, there is no guarantee that we are using ANSI?
		// check to see if ansi colors are enabled for the server
		if (color == Constants.ANSI) {
			// tell client to use bright version of ANSI Colors
			someClient.write("\033[;1m");

			// indicate the use of bright ansi colors to the client
			send("> Using BRIGHT ANSI colors <", someClient);
		}

		// send data about the server
		send(colors(program, "yellow") + colors(" " + version, "yellow") + colors(" -- Running on " + computer, "green"), someClient);
		send(colors(serverName, "red"), someClient);
		send(colors(messageOfTheDay(), "cyan"), someClient);

		// reset color
		// send(colors("Color Reset to Default!", "white"));

		// indicate game mode
		send("Mode: " + mode, someClient);

		// TODO need a way to either exit int login for another method or to create a character in int login
		if (int_login) {
			send("Using Interactive Login", someClient);
			send("");

			start_interactive_login(someClient);
		}
	}

	// check to see that the chosen player name, conforms to the naming rules
	// NOTE: no naming rules exists nor any method for loading or checking
	// against external ones
	public boolean validateName(String testName) {
		boolean nameIsValid = true;

		Pattern validName = Pattern.compile("^\\D*$"); // all aphabetical
		// characters, no
		// numbers

		Matcher isValid = validName.matcher(testName);

		nameIsValid = isValid.matches();

		// test for forbidden names (simple check -- only matches on identical names)
		// I really should use some pattern recognition here...
		if (forbiddenNames.contains(testName)) {
			nameIsValid = false;
		}

		debug("Is \'" + testName + "\' a valid name? " + nameIsValid);

		return nameIsValid;
	}

	// System (sys) Functions

	// reload system help
	// public void sys_help_reload() throws NullPointerException
	public void help_reload() {
		for (final String helpFileName : generateHelpFileIndex()) {
			String helpLines[] = Utils.loadStrings( resolvePath(HELP_DIR, helpFileName) );
			
			this.helpTable.put(helpLines[0], helpLines);
		}
	}

	/*
	 * 
	 * DO NOT USE SYS_RELOAD()! IT WILL RESULT IN A NON-CLEAN WORLD STATE!
	 * 
	 * NOTE: Among other things, a successful reload will likely cause GC errors
	 * and heap error and other weird stuff to happen.
	 * 
	 * This should probably do some of the things live backup does to reset
	 * certain parts of the server. It would also likely to be wise to remove
	 * players from the world while retaining client <-> player mappings for
	 * later restoration. Setting input hold and stopping/pausing the time loop
	 * is recommended. In addition certain post db loading steps should be done
	 * again as well.
	 */
	public void sys_reload() {
		// tell us that the database is being loaded (supply custom message?)
		send("Game> Loading Database!");

		// clear database, etc
		objectDB.clear();

		final ObjectLoader loader = new ObjectLoader(this, objectDB);

		// load objects from databases
		loader.loadObjects(loadListDatabase(DB_FILE), this.logger);

		// tell us that loading is done (supply custom message?)
		send("Game> Done.");
	}

	// highly dangerous test function below
	public void sys_reload(final String filename) {
		// tell us that the database is being loaded (supply custom message?)
		send("Game> Loading Database!");

		// clear database, etc
		objectDB.clear();

		final ObjectLoader loader = new ObjectLoader(this, objectDB);

		// load objects from databases
		loader.loadObjects(loadListDatabase(DB_FILE), this.logger);

		// tell us that loading is done (supply custom message?)
		send("Game> Done.");
	}

	/**
	 * Backup files, either to the specified file name, or to the default one.
	 * 
	 * @param filename
	 */
	public void backup(final String filename) {
		// TODO should this be debug or the main log?

		// Accounts
		log("Backing up Accounts...");
		
		saveAccounts();
		
		log("Done.");

		// Database
		log("Backing up Database...");

		// NOTE: real file modification occurs here
		boolean using_filename = !( "".equals(filename) );
		
		if( using_filename ) {
			log("Using specified filename - \'" + filename + "\'");
			//System.out.println("Using specified filename - \'" + filename + "\'");
			saveDB(filename);
		}
		else saveDB();
		
		log("Database Backup - Done.");
		
		// TODO work on this part, maybe write some type adapters?
		
		/// JSON
		saveJSON();
		
		log("Done.");
		
		/* Spells */
		
		//log("Backing up Spells...");
		
		//saveSpells();

		//log("Done.");
		
		/* Help Files */
		
		//log("Backing up Help Files...");
		
		//saveHelpFiles();
		
		//log("Done.");
		
		/* Topic Files */
		
		//log("Backing up Topic Files...");
		
		//saveTopicFiles();
		
		//log("Done.");
	}
	
	// TODO figure out what I'm trying to accomplish here
	// non-existent player "flush" function
	public void flush() {
		// iterate over all supposedly connected players
		for (final Player player : players) {
			final Client client = player.getClient();
			
			if (player.getClient() == null) {
				// no associated client
				final Player slave = playerControlMap.getSlave(player);

				if (slave == null) {
					players.remove(player);
					debug("Player removed.");
					return;
				}
				else {
					debug("Player \"idle\", but controlling an npc.");
				}
				players.remove(player);
			}
			else {
				// we have a client
				if( !client.isRunning() ) {
					s.disconnect( client );
				}
			}
		}
	}

	private void restart(int secs) {
		final TimerTask task = new TimerTask() {
			public void run() {
				restart();
			}
		};
		
		this.timer.schedule(task, secs * 1000);
	}
	
	// TODO hot or cold restart
	private void restart() {
		/*
		this.s = new Server(this, port);         // initialize the server object
		
		new Thread(this.s, "server").start();    // start it in a thread
		
		System.out.println("Server Startup!\n"); // tell us the server has started
		
		help_reload();                           // reload the help files
		sys_reload();                            // load the database from disk
		
		this.running = true;
		*/
		
		main( serverArgs );
	}
	
	private void shutdown(int secs) {
		final TimerTask task = new TimerTask() {
			public void run() {
				shutdown();
			}
		};
		
		this.timer.schedule(task, secs * 1000);
	}

	private void shutdown() {
		write("Server Shutdown!\n");
		
		changeMode(GameMode.MAINTENANCE); // prevent any new connections
		
		write("Entering " + mode.toString() + " Mode.");

		// disconnect any connected clients
		for (final Client client1 : s.getClients()) {
			init_disconn(client1, false);
		}

		System.out.print("Stopping main game... ");
		
		// indicate that the MUD server is no longer running, should cause the main loop to exit
		running = false;
		
		// handle any currently executing commands...
		if ( true ) {
			//threads.get("command_exec").st
			//cmdExec.
		}
		
		System.out.println("Done");

		// stop the server (the network server, the part handling sockets)
		System.out.print("Stopping server... ");
		
		s.stopRunning();
		
		System.out.println("Done");

		// run the backup (run before closing logs so any backup problems get logged)
		System.out.print("Running backup... ");
		
		// TODO review backup
		//backup("");
		
		System.out.println("Done");

		// close the logs (closes the file object and saves the data to a file)
		if ( logging ) {
			System.out.print("Closing logs... ");
			
			logger.stop(); // close logs
			
			System.out.println("Done");
		}
		else {
			System.out.println("Logging not enabled.");
		}
		
		System.out.println("Exiting...");

		System.exit(0);
	}

	/**
	 * Error Handler
	 * 
	 * Generate an error message, containing the name of the function or part of
	 * the program where the error originated. Use the errorCode to get a
	 * generic error message from the default list.
	 * 
	 * @param funcName  name of the function where this is being called
	 * @param errorCode the index in the message list of the particular error message desired
	 * @return an error messag string
	 */
	public String gameError(final String funcName, final int errorCode) {
		String errorString = Errors.get(errorCode);

		if (errorString == null || errorString.length() == 0) {
			errorString = "unknown error";
		}
		
		logError(funcName + ": " + errorString);

		return "Error ( " + funcName + " ): " + errorString;
	}

	public String gameError(String funcName, ErrorCodes errorCode) {
		return gameError(funcName, errorCode.ordinal());
	}
	
	/**
	 * Send
	 * 
	 * SERVER WRITE -- send message to all connected clients
	 * 
	 * wraps any cases of a println and and a server write into one function,
	 * also makes it easy to disable printing to standard out for most debugging
	 * and status messages
	 * 
	 * NOTE: an overloaded version of the function that takes only strings,
	 * instead of any kind of object
	 * 
	 * @param data
	 */
	public void send(final String data) {
		if (telnet == 0) // no telnet
		{
			write(data + "\r\n");
		}
		if (telnet == 1 || telnet == 2) {
			// telnet and mud clients
			for (char c : data.toCharArray()) {
				write(c);
			}
			
			write('\r');
			write('\n');
			//write("\r\n");
		}
	}

	/**
	 * Send w/client specified
	 * 
	 * CLIENT WRITE -- send message only to the specified client
	 * 
	 * newish version of send w/client that tries to adhere to line limits and
	 * handle both telnet clients and non-telnet clientss
	 * 
	 * @param data
	 * @param client
	 */
	public void send(final String data, final Client client) {
		// we won't send anything if the client is stopped
		if ( client.isRunning() ) {
			// int lineLimit = 80;

			/*
			 * if ( loginCheck(client) ) { lineLimit =
			 * getPlayer(client).getLineLimit(); }
			 */

			/*
			 * // if the data to be sent exceeds the line limit if
			 * (data.length() > lineLimit) { String newData = data.substring(0,
			 * lineLimit - 1); // choose a chunk of data that does not exceed
			 * the limit
			 * 
			 * if (telnet == 0) // no telnet { client.write(newData + "\r\n"); }
			 * else if (telnet == 1 || telnet == 2) { // telnet and mud clients
			 * for (int c = 0; c < data.length(); c++) {
			 * client.write(newData.charAt(c)); } client.write("\r\n"); }
			 * 
			 * send(data.substring(lineLimit - 1, data.length()), client); //
			 * recursively call the function with the remaining data
			 * 
			 * return; }
			 */
			
			// check if socket is open, otherwise drop the connection
			final Socket s = client.getSocket();

			if( s.isClosed() ) {
				debug("Client: socket closed?! (unexpected disconnect)");
				client.stopRunning(); // TODO is this what I ought to do
				kick(client);
				return;
			}

			if (telnet == 0) // no telnet
			{
				client.write(data + "\r\n");
			}
			else if (telnet == 1 || telnet == 2) {
				// telnet and mud clients
				for (char c : data.toCharArray()) {
					client.write(c);
				}

				client.write("\r\n");
			}
		}
		else {
			// DEBUG or Error or ?
			System.out.println("Error: Client is inactive (maybe disconnected), message not sent");
			System.out.println(data);
			
			gameError("send", ErrorCodes.CLIENT_DISCONNECTED);
		}
	}
	
	/**
	 * Send a large amount of data as a list of strings.
	 * 
	 * TODO normalize line length? truncate lines to length?
	 * 
	 * @param data
	 * @param client
	 */
	private void send(final List<String> data, final Client client) {
		client.write(data);
	}
	
	/**
	 * A wrapper function for the primary debug function that ensures that I can
	 * send debugging information without a specific debugLevel and it will have
	 * a debugLevel of 1.
	 * 
	 * @param data
	 */
	public void debug(final String data) {
		debug(data, 1);
	}
	
	/**
	 * A wrapper function for System.out.println that can be "disabled" by
	 * setting an integer. Used to turn "on"/"off" printing debug messages to
	 * the console.
	 * 
	 * Each debug level includes the levels below it
	 * 
	 * e.g. debug level 3 includes levels 3, 2, 1 debug level 2 includes levels
	 * 2, 1 debug level 1 includes levels 1
	 * 
	 * Uses an Object parameter and a call to toString so that I can pass
	 * objects to it
	 * 
	 * @param data
	 */
	public void debug(final String data, final int tDebugLevel) {
		if ( debug ) // debug enabled
		{
			// current debug level is to the specified one
			if (debugLevel >= tDebugLevel) {
				System.out.println(data);

				logDebug( ("" + data).trim() );
			}
		}
	}
	
	public void debug(final String data, final Boolean test) {
		if ( debug ) {
			if ( test ) {
				System.out.println( data.trim() );
				logDebug( data.trim() );
			}
		}
	}
	
	public void debug(final Exception exception) {
		// TODO should this be shown regardless?
		System.out.println("--- Stack Trace ---");
		exception.printStackTrace();
		
		if( debug ) {
			//logDebug("--- Stack Trace ---");
			//logDebug( exception );
		}
	}
	
	/* Logging Methods */
	
	private void log(final String string) {
		log(string, Constants.LOG);
	}
	
	private void log(final String logString, final String logName) {
		if ( this.logging ) {
			this.logger.log(logName, logString);
		}
	}
	
	public void logAction(final String playerName, final Integer playerLoc, final String action) {
		log("(" + playerName + ") {Location: #" + playerLoc +  "}  " + action, Constants.LOG); 
	}
	
	private void logConnect(final Player player, final Time time) {
		final String playerName = player.getName();
		final int playerLoc = player.getLocation();
		final Room room = getRoom(playerLoc);

		final String loginTime = time.toString();
		final String ipAddress = player.getClient().getIPAddress();

		debug("-- Login");
		debug("Name: " + playerName);
		debug("Location: " + room.getName() + "(#" + playerLoc + ")");
		debug("Login Time: " + loginTime);
		
		logAction(playerName, playerLoc, "Logged in at " + loginTime + " from " + ipAddress);
	}

	private void logDisconnect(final Player player, final Time time) {
		final String playerName = player.getName();
		final int playerLoc = player.getLocation();
		final Room room = getRoom(playerLoc);

		final String logoutTime = time.toString();
		final String ipAddress = player.getClient().getIPAddress();

		debug("-- Logout");
		debug("Name: " + playerName);
		debug("Location: " + room.getName() + "(#" + playerLoc + ")");
		debug("Logout Time: " + logoutTime);
		
		logAction(playerName, playerLoc, "Logged out at " + logoutTime + " from " + ipAddress);
	}
	
	private void logError(final String errorMessage) {
		log(errorMessage, Constants.ERROR_LOG);
	}
	
	private void logChat(final String chatString) {
		if( log_chat ) log(chatString, Constants.CHAT_LOG);
	}

	private void logDebug(final String debugMessage) {
		if( log_debug ) log(debugMessage, Constants.DEBUG_LOG);
	}
	
	/**
	 * Game Time
	 * 
	 * @return a string containing a description of the time of day.
	 */
	public String gameTime() {
		String output;

		final TimeOfDay tod = game_time.getTimeOfDay();
		
		/*if (!game_time.isDaytime()) {
			output = "It is " + tod.timeOfDay + ", the " + game_time.getMoonPhase() + " is " + tod.bodyLoc + ".";
		}
		else {
			output = "It is " + tod.timeOfDay + ", the " + game_time.getCelestialBody() + " is " + tod.bodyLoc + ".";
		}*/
		
		if ( game_time.isDaytime() ) {
			output = String.format("It is %s, the %s is %s.", tod.timeOfDay, game_time.getCelestialBody(), tod.bodyLoc);
		}
		else {
			output = String.format("It is %s, the %s is %s.", tod.timeOfDay, game_time.getMoonPhase(), tod.bodyLoc);
		}

		return output;
	}

	/**
	 * Game Date
	 * 
	 * @return a string containing information about what in-game day it is
	 */
	// "st", "nd", "rd", "th"
	public String gameDate() {
		final String month_name = MONTH_NAMES[month - 1];
		final String year_name = years.get(year);
		
		String holiday = "";

		for (Map.Entry<String, Date> me : holidays.entrySet()) {
			final Date d = me.getValue();
			
			if (d.getDay() == day && d.getMonth() == month) {
				holiday = me.getKey();
				break;
			}
		}
		
		// return <general time of year> - <numerical day> day of <month>, <year> <reckoning> - <year name, if any>
		// Summer - 30th day of Flamerule, 1332 DR - The Year of the Sword and Stars ()
		//return season.getName() + " - " + day + suffix[day - 1] + " day of " + month_name + ", " + year + " " + reckoning + " - " + year_name + " (" + holiday + ")";
		
		// 30th day of Flamerule, 1332 DR (Summmer) [Shieldmeet]
		// result = day + suffix[day - 1] + " day of " + month_name + ", " + year + reckoning + " (" + season + ") [" + holiday + "]";
		
		String result = "";
		
		if (day > 0 && day <= 4) {
			result = day + suffix[day - 1] + " day of " + month_name + ", " + year + " " + reckoning + " (" + season + ")";
		}
		else if (day != 11 && ((day % 10) > 0 && (day % 10) <= 4)) {
			result = day + suffix[(day % 10) - 1] + " day of " + month_name + ", " + year + " " + reckoning + " (" + season + ")";
		}
		else {
			result = day + suffix[3] + " day of " + month_name + ", " + year + " " + reckoning + " (" + season + ")";
		}
		
		if( !holiday.equals("") ) result = result + " [" + holiday + "]";
		
		return result;
	}

	/*
	 * public static enum Telnet { SE((byte) 240), NOP((byte) 241), DM((byte)
	 * 242), BRK((byte) 243), IP((byte) 244), AO((byte) 245), AYT((byte) 246),
	 * EC((byte) 247), EL((byte) 248), GA((byte) 249), SB((byte) 250),
	 * WILL((byte) 251), WONT((byte) 252), DO((byte) 253), DONT((byte) 254),
	 * IAC((byte) 255);
	 * 
	 * public Byte b;
	 * 
	 * Telnet(Byte b) { this.b = b; }
	 * 
	 * public Byte toByte() { return this.b; } }
	 */

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

	/*
	 * Room Instancing Stuff (not complete/properly functional)
	 * 
	 * NOTE: DO NOT USE!
	 */

	/**
	 * Generate a new instance of a particular room for a specified group of
	 * players - the input syntax needs help since I won't be specifying players
	 * individually in an arguments list and I need to take in a set of
	 * rooms/zone to make an instance of. A single instance of a room doesn't
	 * make any since?
	 * 
	 * NOTE: only a non-instance can be used as a template
	 * 
	 * @param template
	 * @param group
	 * @return
	 */
	public Room new_instance(final Room template) {
		Room newRoom = null;

		// check that the template is valid (i.e. it exists and isn't an instance)
		if (template != null && !template.isInstance()) {
			// call the copy constructor
			newRoom = new Room(template);
		}
		else {
			debug("Invalid Template Room: Null or Not a Parent");
		}

		return newRoom;
	}

	public boolean move_to_instance(Instance i, Player... group) {
		boolean success = false;

		if (i instanceof Room) {
			final Room room = (Room) i;

			for (final Player player : group) {
				player.setLocation(room.getDBRef());
			}

			success = true;
		}

		return success;
	}

	/**
	 * Figure out if the player has a specific container for this item.
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
	protected boolean hasLineOfSight(Point origin, Player target) {
		final Room room = getRoom(target.getLocation());

		return hasLineOfSight(room, origin, target.getPosition());
	}

	protected boolean hasLineOfSight(Room room, Point origin, Point goal) {
		final Point temp = new Point( origin );

		/*
		 * int x_pos = origin.getX(); // get origin X coord int y_pos =
		 * origin.getY(); // get origin Y coord
		 * 
		 * int d_x_pos = goal.getX(); // get goal X coord int d_y_pos =
		 * goal.getY(); // get goal Y coord
		 */

		List<MUDObject> objects = objectDB.getByRoom(room);

		while (!temp.equals(goal)) {
			// find out if there's anything at that intersection of x and y
			for (final MUDObject m : objects) {
				if (m.getPosition().equals(temp)) {
					return false;
				}
			}
			
			temp.changeX(1); // increment x
			temp.changeY(1); // increment y
		}

		return true;
	}

	// Random Movement
	protected void randomMovement() {
		// determine possible moves
		// randomly select among them
	}

	/**
	 * Display the account menu for a specific account to the client specified
	 * 
	 * NOTE1: The design/layout for this is borrowed from the login process on
	 * TorilMUD. (torilmud.com)
	 * 
	 * @param account
	 * @param client
	 */
	public void account_menu(final Account account, final Client client) {
		if (account != null) {
			// not the place for the below, since it relates to before player connection
			// in fact, init_conn will need modification if it expects to handle accounts instead of players
			final String divider = "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-";
			
			final List<String> output = new LinkedList<String>();
			
			//send(colors(divider, "green"), client);
			output.add( colors(divider, "green") );
			
			// TODO this message and it's color should not be hardcoded to a specific one
			//send(colors(Utils.center("Welcome to Fallout Equestria", 80), "pink2"), client);
			output.add( colors(Utils.center("Welcome to " + mud_name, 80), "pink2") );
			
			//send(colors(divider, "green"), client);
			output.add( colors(divider, "green") );
			
			final List<Player> characters = account.getCharacters();

			int n = 0;

			boolean active_character = false;

			// for characters in account
			for (final Player player : characters) {
				if (player == account.getPlayer()) active_character = true;
				
				final Faction faction = player.getFaction();
				
				if (active_character) {
					//client.write("" + Colors.YELLOW);
					
					//send(n + ") " + player.getName(), client);
					//send(Utils.padLeft("", ' ', 22) + player.getRace().getName() + " [" + ((faction != null) ? faction.getName() :  "No Faction") + "]", client);
					//send(Utils.padLeft("", ' ', 22) + "Class " + player.getPClass().getName() + ", Level " + player.getLevel(), client);
					
					output.add("" + Colors.YELLOW + n + ") " + player.getName() );
					output.add( Utils.padLeft("", ' ', 22) + player.getRace().getName() + " [" + ((faction != null) ? faction.getName() :  "No Faction") + "]" );
					output.add( Utils.padLeft("", ' ', 22) + "Class " + player.getPClass().getName() + ", Level " + player.getLevel() + "" + Colors.WHITE );

					//client.write("" + Colors.WHITE);

					active_character = false;
				}
				else {
					//send(n + ") " + player.getName(), client);
					//send(Utils.padLeft("", ' ', 22) + player.getRace().getName() + " [" + ((faction != null) ? faction.getName() :  "No Faction") + "]", client);
					//send(Utils.padLeft("", ' ', 22) + "Class " + player.getPClass().getName() + ", Level " + player.getLevel(), client);
					
					output.add( n + ") " + player.getName() );
					output.add( Utils.padLeft("", ' ', 22) + player.getRace().getName() + " [" + ((faction != null) ? faction.getName() :  "No Faction") + "]" );
					output.add( Utils.padLeft("", ' ', 22) + "Class " + player.getPClass().getName() + ", Level " + player.getLevel() );
				}

				n++;
			}

			//send("", client);
			//send("Slots Remaining: " + (account.getCharLimit() - account.getCharacters().size()), client);
			
			output.add("");
			output.add( "Slots Remaining: " + (account.getCharLimit() - account.getCharacters().size()) );

			//send(colors(divider, "green"), client);
			output.add( colors(divider, "green") );

			//send("Type the # or name of a character above to login or choose an action below.", client);
			output.add("Type the # or name of a character above to login or choose an action below.");
			
			//send(colors(divider, "green"), client);
			output.add( colors(divider, "green") );
			
			//send("(N)ew character     | (L)ink character   | (U)nlink character | (R)eorder", client);
			//send("(E)nter description | (D)elete character | (C)hange password  | (Q)uit", client);
			
			output.add("(N)ew character     | (L)ink character   | (U)nlink character | (R)eorder");
			output.add("(E)nter description | (D)elete character | (C)hange password  | (Q)uit");
			
			//send(colors(divider, "green"), client);
			output.add( colors(divider, "green") );
			
			send(output, client);
		}
		else {
			send("Invalid Account!", client);
		}
	}

	/**
	 * allows adding to the message queue from external packages, classes
	 * 
	 * NOTE: may handle concurrency poorly
	 * @param newMessage
	 */
	public void addMessage(final Message msg) {
		// if this client's player is the intended recipient
		final MessageType type = msg.getType();
		
		final Player sender = msg.getSender();
		final Player recip = msg.getRecipient();
		final String message = msg.getMessage();
		final Integer location = msg.getLocation();

		Room room;

		debug(type.name(), 4);

		switch (type) {
		case BROADCAST:
			for (final Player player : players) {
				room = getRoom( player.getLocation() );
				
				if (room.getRoomType() == RoomType.OUTSIDE && player.getEditor() == Editors.NONE) {
					send(message, player.getClient());
				}
			}

			msg.markSent();
			break;
		case BROADCAST_LOCAL:
			room = getRoom(location);
			
			// just in case the location somehow isn't a room
			if (room != null) {
				// send a message to all of the room's listeners
				for (final Player player : room.getListeners()) {
					if (true) { // placeholder test condition for awareness of surroundings
						boolean name = ( message.contains( player.getName() ) );
						boolean cname = ( message.contains( player.getCName() ) );
						
						if ( !name && !cname ) {
							send(message, player.getClient());
						}
					}
				}
			}
			else {
				// TODO can't send error message here because we're not dealing directly with the player			//send("You are in a room that doesn't exist?!", 
			}

			msg.markSent();
			break;
		case BROADCAST_PLAYER:
			room = getRoom(msg.getLocation());

			if (room != null) {
				room.fireEvent(message);

				for (final Player bystander : room.getListeners()) {
					if (sender != bystander && !(bystander instanceof NPC)) {
						send(sender.getName() + " says, \"" + message + "\".", bystander.getClient());
					}
				}
			}
			
			msg.markSent();

			break;
		case NORMAL:
			// set up by default for "tells"
			String color = null;

			// TODO kludging here for color name
			if (sender instanceof NPC) {
				color = getDisplayColor("npc");
			}

			if (color != null) {
				send(colors(sender.getName(), color) + " tells you, \"" + message + ".\"", recip.getClient());
			}
			else {
				send(sender.getName() + " tells you, \"" + message + ".\"", recip.getClient());
			}

			msg.markSent();
			break;
		case SYSTEM:
			//send(msg.getMessage(), msg.getRecipient().getClient());
			send(message, recip.getClient());
			
			msg.markSent();
			break;
		default:
			debug("Message Type Unknown");
			
			if (sender != null) debug("Sender: " + msg.getSender().getName());
			if (recip != null)  debug("Recipient: " + msg.getRecipient().getName());
			
			debug("Message: " + msg);
			
			break;
		}

		if ( msg.wasSent() ) debug("addMessage, sent message", 4);
	}

	/**
	 * allows adding to the message queue from external packages, classes this
	 * version does this for multiple messages grouped together
	 * 
	 * @param newMessage
	 */
	public void addMessages(final ArrayList<Message> newMessages) {
		for (final Message m : newMessages) {
			addMessage(m);
		}
	}

	/**
	 * Examine (MUDObject)
	 * 
	 * A general purpose method that sends information about the object being
	 * examined to the player for any object.
	 * 
	 * @param m
	 * @param client
	 */
	public void examine(final MUDObject m, final Client client) {
		if( m == null ) {
			send("NULL object reference", client);
			return;
		}
		
		if( m.isType(TypeFlag.NOTHING) ) {
			final String lockState;

			if (((NullObject) m).isLocked()) lockState = "Locked";
			else                             lockState = "unLocked";

			send("-- NullObject -- (#" + m.getDBRef() + ") [" + lockState + "]", client);
		}
		else {
			send(colors(m.getName() + "(#" + m.getDBRef() + ")", getDisplayColor(m.type)), client);
			
			final String typeName = m.type.getName();
			
			send("Type: " + typeName + " Flags: " + ObjectFlag.toInitString(m.getFlags()), client);
			
			if ( m.isType(TypeFlag.ITEM) ) {
				Item item = (Item) m;

				send("Item  Type: " + item.getItemType().toString(), client);
				send("Slot  Type: " + item.getSlotType().toString(), client);
				send("Equippable: " + item.isEquippable(), client);
			}
			else if ( m.isType(TypeFlag.THING) ) {
				send("Thing Type: " + ((Thing) m).thing_type.toString(), client);
			}
			
			final Player owner = m.getOwner();

			if (owner != null) {
				send("Owner: " + colors(owner.getName(), getDisplayColor("player")) + " (#" + owner.getDBRef() + ")", client);
			}
			else {
				send("Owner: null", client);
			}

			send("Description: " + m.getDesc(), client);

			final MUDObject m1 = getObject(m.getLocation());
			
			String locInfo = "null";

			if (m1 != null) {
				locInfo = colors(m1.getName(), getDisplayColor(m1.type)) + " (#" + m1.getDBRef() + ")";
			}
			
			send("Location: " + locInfo, client);
			
			// send("Coordinates:", client);

			final Point position = m.getPosition();

			/*
			 * send("X: " + position.getX(), client); send("Y: " + position.getY(), client); send("Z: " + position.getZ(), client);
			 */

			send("Coordinates: ( " + position.getX() + ", " + position.getY() + ", " + position.getZ() + " )", client);

			if (m instanceof Container) {
				send("Contains: ", client);

				for (final Item item : ((Container) m).getContents()) {
					send(item.getName(), client);
				}
			}
			
			if(m instanceof MagicItem) {
				send("Effects: " + ((MagicItem) m).getEffects(), client);
			}
		}
	}
	
	public void examine(final Exit exit, final Client client) {
		send(colors(exit.getName() + "(#" + exit.getDBRef() + ")", getDisplayColor(exit.type)), client);

		final String typeName = exit.type.getName();

		send("Type: " + typeName + " Flags: " + ObjectFlag.toInitString(exit.getFlags()), client);
		
		send("Exit Type: " + exit.getExitType().getName(), client);

		if (exit.getExitType() == ExitType.DOOR) {
			send("Locked: " + ((Door) exit).isLocked(), client);
		}

		if (exit instanceof Portal) {
			send("Portal Type: " + ((Portal) exit).getPortalType(), client);
		}
		
		final Player owner = exit.getOwner();
		
		String own_s;
		
		if (owner != null) {
			own_s = colors(owner.getName(), getDisplayColor("player")) + " (#" + owner.getDBRef() + ")";
		}
		else own_s = "null";
		
		send("Owner: " + own_s, client);
		
		send("Description: " + exit.getDesc(), client);
		
		final Room room = getRoom( exit.getLocation() );
		
		String loc_s;
		
		if (room != null) {
			loc_s = colors(room.getName(), getDisplayColor(room.type)) + "(#" + room.getDBRef() + ")";
		}
		else loc_s = "null";
		
		send("Location: " + loc_s, client);
		
		if (exit.getExitType() == ExitType.PORTAL) {
			final Portal portal = (Portal) exit;
			
			switch (portal.getPortalType()) {
			case RANDOM: send("Destinations: Uncertain", client); break;
			case STD:    send("Destination: " + getRoom(portal.getDestination()).getName(), client); break;
			default:     break;
			}
		}
		else {
			final Room room1 = getRoom( exit.getDestination() );
			
			String dest_s;
			
			if (room1 != null) {
				dest_s = colors(room1.getName(), getDisplayColor(room1.type)) + " (#" + room1.getDBRef() + ")";
			}
			else dest_s = "null";
			
			send("Destination: " + dest_s, client);
		}

		// send("Coordinates:", client);

		final Point position = exit.getPosition();

		/*
		 * send("X: " + position.getX(), client); send("Y: " + position.getY(), client); send("Z: " + position.getZ(), client);
		 */

		send("Coordinates: ( " + position.getX() + ", " + position.getY() + ", " + position.getZ() + " )", client);
	}

	/**
	 * Examine
	 * 
	 * Shows us information about the specified player (and NPCs which are a subclass of Player)
	 * 
	 * @param player
	 * @param client
	 */
	public void examine(final Player player, final Client client) {
		send(colors(player.getName() + "(#" + player.getDBRef() + ")", getDisplayColor("player")), client);
		
		final String typeName = player.type.getName();
		
		send("Type: " + typeName + " Flags: " + ObjectFlag.toInitString(player.getFlags()), client);

		send("Race: " + player.getRace().getName(), client);
		send("Class: " + player.getPClass().getName(), client);
		
		final MUDObject owner = player.getOwner();
		
		String own_s;
		
		if (owner != null) {
			own_s = colors(owner.getName(), getDisplayColor("player")) + " (#" + owner.getDBRef() + ")";
		}
		else own_s = "null";
		
		send("Owner: " + own_s, client);

		send("Description: " + player.getDesc(), client);

		final MUDObject m1 = getObject(player.getLocation());
		
		String loc_s;
		
		if (m1 != null) {
			loc_s = colors(m1.getName(), getDisplayColor(m1.type)) + "(#" + m1.getDBRef() + ")";
		}
		else loc_s = "null";
		
		send("Location: " + loc_s, client);

		// send("Coordinates:", client);

		Point position = player.getPosition();

		/*
		 * send("X: " + position.getX(), client); send("Y: " + position.getY(),
		 * client); send("Z: " + position.getZ(), client);
		 */

		send("Coordinates: ( " + position.getX() + ", " + position.getY() + ", " + position.getZ() + " )", client);

		// helmet, necklace, armor, cloak, rings, gloves, weapons, belt, boots

		/*
		 * debug("RING1: " + player.getSlots().get("ring1").getItem() + "\t" +
		 * "RING2: " + player.getSlots().get("ring2").getItem());
		 * debug("RING3: " + player.getSlots().get("ring3").getItem() + "\t" +
		 * "RING4: " + player.getSlots().get("ring4").getItem());
		 * debug("RING5: " + player.getSlots().get("ring5").getItem() + "\t" +
		 * "RING6: " + player.getSlots().get("ring6").getItem());
		 */

		// TODO fix all of this kludging, this depends far too heavily on
		// certain named slots existing
		for (int i = 1; i < 6; i = i + 2) {
			String color = getDisplayColor("thing");
			String r1 = colors("RING" + i, color);
			String r2 = colors("RING" + (i + 1), color);

			// send("RING" + i + ": " + player.getSlots().get("ring" +
			// i).getItem() + "\t" + "RING" + (i + 1) + ": " +
			// player.getSlots().get("ring" + (i + 1)).getItem(), client);
			if (player.getSlot("ring" + i) != null && player.getSlot("ring" + (i + 1)) != null) {
				send(r1 + ": " + player.getSlot("ring" + i).getItem() + "\t" + r2 + ": " + player.getSlot("ring" + (i + 1)).getItem(), client);
			}
		}
	}
	
	/**
	 * 
	 * @param room
	 * @param client
	 */
	public void examine(final Room room, final Client client) {
		// TODO convert to send list? would need resolve send list issue that breaks color output
		send(colors(room.getName() + "(#" + room.getDBRef() + ")", getDisplayColor(room.type)), client);
		
		final String typeName = room.type.getName();
		
		// send("Type: " + ObjectFlag.firstInit(m.getFlags()) + " Flags: " + ObjectFlag.toInitString(m.getFlags()), client);
		send("Type: " + typeName + " Flags: " + ObjectFlag.toInitString(room.getFlags()), client);

		send("Room Type: " + room.getRoomType().toString(), client);

		send("Description: " + room.getDesc(), client);

		final MUDObject m1 = getObject(room.getLocation());

		if (m1 != null) send("Location: " + m1.getName() + "(#" + m1.getDBRef() + ")", client);
		else            send("Location: null", client);

		final Zone zone = room.getZone();

		if (zone != null) send("Zone: " + zone.getName(), client);
		else              send("Zone: null", client);

		send("Sub-Rooms:", client);

		for (final Room room1 : objectDB.getRoomsByLocation(room.getDBRef())) {
			send(room1.getName() + "(#" + room1.getDBRef() + ")", client);
		}
		
		send("Contents:", client);

		final List<Thing> roomThings = objectDB.getThingsForRoom( room );

		for (final Thing thing : roomThings) {
			send(colors(thing.getName(), "yellow") + "(#" + thing.getDBRef() + ")", client);
		}

		send("Items:", client);

		final List<Item> roomItems = objectDB.getItemsByLoc(room.getDBRef());

		for (final Item item : roomItems) {
			send(colors(item.getName(), "yellow") + "(#" + item.getDBRef() + ")", client);
		}

		send("Creatures:", client);
		
		for (final Creature creep : objectDB.getCreaturesByRoom( room )) {
			send(colors(creep.getName(), "cyan") + "(#" + creep.getDBRef() + ")", client);
		}
	}

	/**
	 * Look (MUDObject)
	 * 
	 * Look at any MUDObject (basically anything), this is a stopgap to prevent
	 * anything being unlookable.
	 * 
	 * For players: Looking at a player, should show a description (based on
	 * what they're wearing and what parts of them are visible).
	 * 
	 * NOTE: I shouldn't be able to see the dagger or swords that are hidden
	 * under a cloak
	 * 
	 * @param mo
	 * @param client
	 */
	public void look(final MUDObject mobj, final Client client) {
		System.out.println("LOOK (" + mobj.type.getName() + ")");

		if (getPlayer(client).getAccess() >= Constants.BUILD) {
			if (!mobj.hasFlag(ObjectFlag.SILENT) && !getPlayer(client).hasFlag(ObjectFlag.SILENT)) {
				send(colors(mobj.getName() + " (#" + mobj.getDBRef() + ")", getDisplayColor(mobj.type)), client);
			}
			else {
				send(colors(mobj.getName(), getDisplayColor(mobj.type)), client);
			}
		}
		else {
			send(colors(mobj.getName(), getDisplayColor(mobj.type)), client);
		}
		
		int line_limit = getPlayer(client).getLineLimit();
		
		send(parseDesc(mobj.getDesc(), line_limit), client);

		if (mobj instanceof Player) {
			StringBuilder output = new StringBuilder();

			for (final Entry<String, Slot> e : ((Player) mobj).getSlots().entrySet()) {
				Slot slot = e.getValue();

				if (slot.isFull() && slot.getItemType() == ItemTypes.CLOTHING) {
					Item item = slot.getItem();
					output.append(item.getName() + ", ");
				}
			}

			send("Wearing (visible): " + output.toString(), client);
		}

		if (mobj instanceof Storage<?>) {
			send("Contents:", client);

			Storage<?> storage = (Storage<?>) mobj;

			for (final Item item : storage.getContents()) {
				send(colors(item.getName(), getDisplayColor("item")), client);
			}
		}
	}
	
	public List<String> look(final MUDObject mobj, final Player player) {
		final LinkedList<String> output = new LinkedList<String>();
		
		System.out.println("LOOK (" + mobj.type.toString().toLowerCase() + ")");
		
		final String objType = MudUtils.getTypeName( mobj.getType() );
		
		// TODO small problem..., can't get display colors
		final String displayColor = getDisplayColor(objType);
		
		if ( player.getAccess() >= Constants.BUILD ) {
			if (!mobj.hasFlag(ObjectFlag.SILENT) && !player.hasFlag(ObjectFlag.SILENT)) {
				output.add( colors(mobj.getName() + " (#" + mobj.getDBRef() + ")", displayColor) );
			}
			else {
				output.add( colors(mobj.getName(), displayColor) );
			}
		}
		else {
			output.add( colors(mobj.getName(), displayColor) );
		}
		
		send(parseDesc(mobj.getDesc(), 80), player.getClient());

		if (mobj instanceof Player) {
			final StringBuilder sb = new StringBuilder();

			for (final Entry<String, Slot> e : ((Player) mobj).getSlots().entrySet()) {
				Slot slot = e.getValue();

				if (slot.isFull() && slot.getItemType() == ItemTypes.CLOTHING) {
					Item item = slot.getItem();
					
					sb.append(item.getName() + ", ");
				}
			}

			output.add( "Wearing (visible): " + sb.toString() );
		}

		if (mobj instanceof Storage<?>) {
			output.add("Contents: ");

			final Storage<?> storage = (Storage<?>) mobj;

			for (final Item item : storage.getContents()) {
				output.add( colors(item.getName(), getDisplayColor("item")) );
			}
		}
		
		return output;
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
		final Player player = getPlayer(client);

		if( player != null && room != null ) {
			final List<String> output = new LinkedList<String>();
			
			int line_limit = player.getLineLimit(); /* Make the description conform to a column limit */

			// TODO make this get it's header data from somewhere else to make it customizable
			if (player.getConfigOption("hud_enabled")) {
				output.add( getHeader("--| %z > %r |%s[ %p ]--", line_limit, room) );
			}
			else {
				if (!room.getFlags().contains(ObjectFlag.SILENT)) {
					output.add( colors(room.getName() + " (#" + room.getDBRef() + ")",  getDisplayColor("room")) );
				}
				else {
					output.add( colors(room.getName(), getDisplayColor("room")) );
				}
				
				output.add( Utils.padRight("", '-', line_limit) );
			}
			
			output.add( "" );
			
			/* Start Description */

			// TODO is this duplicated in TimeLoop?
			TimeOfDay[] night = { TimeOfDay.DUSK, TimeOfDay.MIDNIGHT, TimeOfDay.NIGHT, TimeOfDay.BEFORE_DAWN };

			if (room.getRoomType() == RoomType.OUTSIDE && Arrays.asList(night).contains(game_time.getTimeOfDay())) {
				//send("It's too dark to be able to see anything.", client);
				output.add( "It's too dark to be able to see anything." );
			}
			else {
				// TODO resolve timeOfDay issue
				//final String description = parse(room.getDesc(), room.timeOfDay);
				final String description = parse(room.getDesc(), "DAY");
				//final String description = parse(room.getDesc(), timeOfDay(room));

				debug("description (parsed): " + description);

				// TODO make sure the line below doesn't cause issues
				String tempDescription = check(description);
				
				output.addAll( parseDesc(tempDescription, line_limit) );
			}

			//send("", client);
			output.add( "" );

			/* End Description */

			/*
			 * presumably some sort of config would allow you to disable date and
			 * time reporting here, maybe even turn off the weather data
			 */
			if (room.getRoomType().equals(RoomType.OUTSIDE) && player.getConfigOption("show-weather")) {
				final Weather weather = room.getWeather();

				if (weather != null) {
					// to color or /not/ to color.., should I be using showDesc
					// send("*** " + "<weather>: " + parse(room.getWeather().ws.description, room.timeOfDay), client);
					
					final String wsName = weather.getState().getName();
					final String wsDesc = weather.getState().getDescription();
					
					//send("*** " + colors(wsName, "purple") + ": " + wsDesc, client);
					//send("", client);
					
					output.add( "*** " + colors(wsName, "purple") + ": " + wsDesc );
					output.add( "" );
				}

				// send(gameTime(), client); // the in-game time of day

				// send("", client);
			}

			// send(gameDate(), client); // the actual date of the in-game year
			// send("", client);

			// TODO make this get it's footer data from somewhere else to make it customizable
			if (player.getConfigOption("hud_enabled")) {
				//send(getFooter("--[%S]%s[ %Tam ]--[ %D ]--"), client);
				output.add( getFooter("--[%S]%s[ %Tam ]--[ %D ]--", line_limit) );
			}
			else {
				//send(Utils.padRight("", '-', line_limit), client);
				output.add( Utils.padRight("", '-', line_limit) );
			}

			if (room.getThings().size() > 0) {
				StringBuilder sb = new StringBuilder();

				for (final Thing thing : room.getThings()) {
					
					if ( !thing.hasFlag(ObjectFlag.DARK) ) { // only shown non-Dark things
						if ( !room.hasFlag(ObjectFlag.SILENT) ) {
							// send(colors(thing.getName() + "(#" + thing.getDBRef() + ")", "yellow"), client);
							sb.append(colors(thing.getName() + "(#" + thing.getDBRef() + ")", "yellow") + ", ");
						}
						else {
							// send(colors(thing.getName(), "yellow"), client);
							sb.append(colors(thing.getName(), "yellow") + ", ");
						}
					}
				}

				//send(sb.toString().substring(0, sb.length() - 2), client); // dropping the last two characters clips the ending ", "
				
				if ( sb.toString().endsWith(",") ) output.add( sb.toString().substring(0, sb.length() - 2) );
				else                               output.add( sb.toString() );
			}

			/*
			 * need to fix this code up, so that rooms whose coordinates, other
			 * location markers are null will always show up in the list but those
			 * with specific coordinates will not show up unless you can "see" them
			 * or are in the same square
			 * 
			 * part of the problem is the exitNames variable, I need it to somehow
			 * retain all of the exit names for the room ( a cached version if you
			 * will ) as long as the number of exits don't change. However, I also
			 * only want to show exits whose location is the same as mine or which
			 * don't have a specific location (i.e. you should be able to reach it
			 * no matter what if you can traverse the room safely -- hence it's okay
			 * to list it; exits such as portals/secret doors which could be absent,
			 * obscured, etc might not always show up)
			 */
			
			final String exitNames = room.getExitNames();

			// Exits:

			if ( exitNames != null && !exitNames.equals("") ) {
				//send("Obvious exits are: " + colors(exitNames, getDisplayColor("exit")), client);
				output.add( "Obvious exits are: " + colors(exitNames, getDisplayColor("exit")) );
			}

			//send("Contents:", client);
			output.add( "Contents:" );

			if ( !room.hasFlag(ObjectFlag.SILENT) ) {
				for (final Item item : room.getItems()) {
					//send(colors(item.getName() + "(#" + item.getDBRef() + ")", "yellow"), client);
					output.add( colors(item.getName() + "(#" + item.getDBRef() + ")", "yellow") );
				}
			} 
			else {
				for (final Item item : room.getItems()) {
					//send(colors(item.getName(), "yellow"), client);
					output.add( colors(item.getName(), "yellow") );
				}
			}

			//send("With:", client);
			output.add( "With:" );

			for (final NPC npc : objectDB.getNPCsByRoom( room )) {
				if ( !room.hasFlag(ObjectFlag.SILENT) ) {
					//send(colors("[" + npc.getStatus() + "] " + npc.getName() + "(#" + npc.getDBRef() + ")", "cyan"), client);
					output.add( colors("[" + npc.getStatus() + "] " + npc.getName() + "(#" + npc.getDBRef() + ")", "cyan") );
				}
				else {
					//send(colors("[" + npc.getStatus() + "] " + npc.getName(), "cyan"), client);
					output.add( colors("[" + npc.getStatus() + "] " + npc.getName(), "cyan") );
				}
			}

			for (final Creature creep : objectDB.getCreaturesByRoom( room )) {
				if ( !room.hasFlag(ObjectFlag.SILENT) ) {
					//send(colors(creep.getName() + "(#" + creep.getDBRef() + ")", "cyan"), client);
					output.add( colors(creep.getName() + "(#" + creep.getDBRef() + ")", "cyan") );
				}
				else {
					//send(colors(creep.getName(), "cyan"), client);
					output.add( colors(creep.getName(), "cyan") );
				}
			}
			
			// players (logged-in player), objectDB.getPlayersByRoom( ... )
			final List<String> names = player.getNames();
			
			for (final Player player1 : players) {
				String pName = player1.getName();
				String pCName = player1.getCName();
				String pStatus = player1.getStatus();
				
				if ( !players.contains(player1) ) continue; // temporary kludge to hide offline players, which I'd like to show

				String dcolor = getDisplayColor("player");
				
				// NOTE: invisible players can 'see' themselves
				if ( player1 == player && player1.hasEffect("invisibility") ) {
					//send(colors("[" + player1.getStatus() + "] " + player1.getName() + " (invisible)", dcolor), client);
					output.add( colors("[" + player1.getStatus() + "] " + player1.getName() + " (invisible)", dcolor) );
					continue;
				}

				if ( player1.getLocation() == room.getDBRef() ) {
					if ( !player1.hasEffect("invisibility") || player.hasEffect("see_invisibility") ) { // if player is not invisible
						// TODO this value should come from elsewhere
						boolean sdesc = false; // short descriptions (true=yes,false=no)

						if ( sdesc ) { // if using short descriptions
							//send(evaluate(player, player1), client);
							output.add( evaluate(player, player1) );
						}
						else { // otherwise
							boolean knownPlayer = ( names.contains( pName ) || player.getName().equals( pName ) );
							boolean mount = ( player1.mount != null );
							
							if( mount ) {
								if( knownPlayer ) {
									//send(colors("[" + pStatus + "] " + pName + "( riding a " + player1.mount.getName() + " )", dcolor), client);
									output.add( colors("[" + pStatus + "] " + pName + "( riding a " + player1.mount.getName() + " )", dcolor) );
								}
								else {
									//send(colors("[" + pStatus + "] " + pCName + "( riding a " + player1.mount.getName() + " )", dcolor), client);
									output.add( colors("[" + pStatus + "] " + pCName + "( riding a " + player1.mount.getName() + " )", dcolor) );
								}
							}
							else {
								if( knownPlayer ) {
									//send(colors("[" + pStatus + "] " + pName, dcolor), client);
									//output.add( colors("[" + pStatus + "] " + pName, dcolor) );
									output.add( colors("[", "blue") + colors(pStatus, "green") + colors("]", "blue") + " " + colors(pName, dcolor) );
								}
								else {
									//send(colors("[" + pStatus + "] " + pCName, dcolor), client);
									output.add( colors("[" + pStatus + "] " + pCName, dcolor) );
								}
							}
						}
					}
				}
			}
			
			// Portal
			final List<Portal> tempPortals = new ArrayList<Portal>(5);

			for (final Portal portal : portals) {
				/*
				 * final Point currentPos = current.getPosition(); final Point
				 * portalPos = portal.getPosition();
				 * 
				 * final boolean playerAtPortal = ( portalPos.getX() ==
				 * currentPos.getX() && portalPos.getY() == currentPos.getY() );
				 */

				final boolean playerAtPortal = player.getPosition().equals(portal.getPosition());

				if ( playerAtPortal && (portal.getOrigin() == room.getDBRef() || portal.getDestination() == room.getDBRef()) ) {
					tempPortals.add(portal);
				}
			}

			if (tempPortals.size() == 1)     output.add( "There is a portal here." ); //send("There is a portal here.", client);
			else if (tempPortals.size() > 1) output.add( "There are several portals here."); //send("There are several portals here.", client);
			
			send( output, client );
		}
		else if (player == null) send("Game> Player is NULL? (this should be impossible here, ignoring bugs)", client);
		else if (room == null)   send("Game> Invalid Room?", client);
	}

	/**
	 * parse
	 * 
	 * recursive description parser, needs to handle nested conditional
	 * 
	 * X ? TRUE : FALSE
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
		debug("" + input.contains("{"), 2);

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
	 * pre: the stuff before any parsing parsed content: the stuff that's been
	 * parsed post: the stuff after any parsing
	 * 
	 * ex: A long-abandoned tower dominates the sky here{RAIN?, its
	 * polished-brick facade slick with fallen rain} {CLEAR?{DAY?, gleaming in
	 * the sunlight}{NIGHT?, pale moonlight casting it in an unnatural glow}}.
	 * 
	 * pre: A long-abandoned tower dominates the sky here parsed content (NIGHT,
	 * CLEAR): , pale moonlight casting it in an unnatural glow post: .
	 * 
	 * result: A long-abandoned tower dominates the sky here, pale moonlight
	 * casting it in an unnatural glow.
	 */

	/**
	 * parse
	 * 
	 * Parse descriptions, and evaluate internal statements based on some
	 * parameters
	 * 
	 * i.e. evaluation {DAY? sunbeams cascade in through the hole in the
	 * ceiling}{NIGHT? moonlight falls gently across the stone floor}
	 * 
	 * if CtimeOfDay was day, then you'd get
	 * "sunbeams cascade in through the hole in the ceiling", otherwise this,
	 * "moonlight falls gently across the stone floor"
	 * 
	 * NOTE: non-recursive DEBUG: 2
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
		debug("" + input.contains("{"), 2);

		int begin = input.indexOf("{", index); // find the beginning of themarkup
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

				debug("Time of Day Message: " + alt, 2);

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

				debug("" + index, 2);

				begin = input.indexOf("{", index);  // find the beginning of the markup
				debug("Begin Markup: " + begin, 2);
				mid = input.indexOf("?", index);    // find the middle of the markup
				debug("Middle Markup: " + mid, 2);
				end = input.indexOf("}", index);    // find the end of the markup
				debug("End Markup: " + end, 2);

				if (end == -1) { // if there isn't a closing brace
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
	 * A mechanism to apply effects to objects. This function should
	 * differentiate between instantaneous and on-going effects.
	 * 
	 * Instaneous effects should be applied instantly and leave no trace.
	 * On-going effects should be noted somewhere
	 * 
	 * @param player
	 * @param effect
	 * @return whether or the not the effect was successfully applied
	 */
	public boolean applyEffect(final MUDObject m, final Effect effect) {
		if (m instanceof Player) {
			return applyEffect((Player) m, effect);
		}

		return false;
	}

	/**
	 * A mechanism to apply effects to players. This function should
	 * differentiate between instantaneous and on-going effects.
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

		/*
		 * WARNING: healing effects currently remove any supplementary
		 * hitpoints. this should not remove hitpoints, it should only them up
		 * to the total hitpoints of a player. To account for this behavior, the
		 * totalhp could always be adjusted to be the current max, but then i'd
		 * have to track. If I don't do that any spells/effects that temporarily
		 * raise hitpoints will make healing you at all a bad thing
		 * NOTE: another problem arises, if I had 100 totalhp and 10 hp, then I could
		 * only heal myself to 90 (9/10 full heal).
		 */

		//try {
			final String effectName = effect.getName();
			String temp;
			
			// remove effect if ! is prefixed to it
			if ( effectName.startsWith("!") ) {
				// covers dispel case for now -- will need serious work later
				if ( effectName.equals("!any") ) {
					// different dispels? -- rules?
					player.clearEffects();
					send("All Effects removed!", client);
				}
				else {
					temp = effectName.substring(effectName.indexOf("!") + 1, effectName.length());
					
					removeEffect(player, temp);
					
					//send(temp + " effect removed!", client);
				}
			}
			else {
				if ( effectName.startsWith("heal") ) {
					temp = effectName.substring(effectName.indexOf("+") + 1, effectName.length());

					try {
						Integer amount = Integer.parseInt(temp);
						
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
							
							send("Healing " + "(+" + amount + ") effect applied!\nYou gained " + diff + " hit points.", client);
						}

						/*
						 * else if (diff == amount) { player.setHP(amount);
						 * send("Healing (+" + amount +
						 * ") effect applied!\nYou gained " + amount +
						 * " hit points.", client); }
						 */
					}
					catch (NumberFormatException nfe) {
						// TODO is this debug message adequate?
						debug(nfe.getMessage()); // send a debug message
						
						debug( nfe );
					}
				}
				else if ( effectName.startsWith("dam") ) {
					temp = effectName.substring(effectName.indexOf("-") + 1, effectName.length());

					try {
						Integer damage = Integer.parseInt(temp);
						
						debug("Amount of Damage: " + damage);
						debug("Hitpoints: " + player.getHP());
						debug("Hitpoints (total): " + player.getTotalHP());

						player.setHP(damage);

						send("Damage " + "(-" + damage + ") effect applied!\nYou lost " + damage + " hit points.", client);
					}
					catch (NumberFormatException nfe) {
						// TODO is this debug message adequate?
						debug(nfe.getMessage()); // send a debug message
						
						debug( nfe );
					}
				}
				else {
					// TODO should I be doing this somewhere else? stat bonuses ought to be ongoing effects, right?
					String test = "";
					
					// detect whether this is a positive or negative effect
					int plus = effectName.indexOf('+');
					int minus = effectName.indexOf('-');
					
					Integer boost = 0;
					
					if( plus != -1 ) {
						test = effectName.substring(0, plus);
						boost = Utils.toInt(effectName.substring(plus + 1), 0);
					}

					else if( minus != -1 ) {
						test = effectName.substring(0, minus);
						boost = Utils.toInt(effectName.substring(minus + 1), 0);
					}

					if (boost != 0) {
						System.out.println("stat: " + test);
						/** TODO: resolve this for results with differing stats **/
						switch (test) {
						case "str":
							debug("Strength Bonus: " + boost);
							player.setAbilityMod(Abilities.STRENGTH, boost);
							//send("Strength increased by " + boost + " to " + player.getAbility(Abilities.STRENGTH), client);
							send("Strength increased by " + boost + " to " + player.getAbility(Player.ruleset.getAbility("STR")), client);
							break;
						case "con":
							debug("Constitution Bonus: " + boost);
							player.setAbilityMod(Abilities.CONSTITUTION, boost);
							//send("Constitution increased by " + boost + " to " + player.getAbility(Abilities.CONSTITUTION), client);
							send("Constitution increased by " + boost + " to " + player.getAbility(Player.ruleset.getAbility("CON")), client);
							break;
						case "dex":
							debug("Dexterity Bonus: " + boost);
							player.setAbilityMod(Abilities.DEXTERITY, boost);
							//send("Dexterity increased by " + boost + " to " + player.getAbility(Abilities.DEXTERITY), client);
							send("Dexterity increased by " + boost + " to " + player.getAbility(Player.ruleset.getAbility("DEX")), client);
							break;
						case "int":
							debug("Intelligence Bonus: " + boost);
							player.setAbilityMod(Abilities.INTELLIGENCE, boost);
							//send("Intelligence increased by " + boost + " to " + player.getAbility(Abilities.INTELLIGENCE), client);
							send("Intelligence increased by " + boost + " to " + player.getAbility(Player.ruleset.getAbility("INT")), client);
							break;
						case "cha":
							debug("Charisma Bonus: " + boost);
							player.setAbilityMod(Abilities.CHARISMA, boost);
							//send("Charisma increased by " + boost + " to " + player.getAbility(Abilities.CHARISMA), client);
							send("Charisma increased by " + boost + " to " + player.getAbility(Player.ruleset.getAbility("CHA")), client);
							break;
						case "wis":
							debug("Wisdom Bonus: " + boost);
							player.setAbilityMod(Abilities.WISDOM, boost);
							//send("Wisdom increased by " + boost + " to " + player.getAbility(Abilities.WISDOM), client);
							send("Wisdom increased by " + boost + " to " + player.getAbility(Player.ruleset.getAbility("WIS")), client);
							break;
						default:
							break;
						}
					}
					// add effects
					else {
						// cancel out the effect if they already had it
						// TODO should effect objects carry a stack/nostack detail?
						if ( player.hasEffect(effectName) ) {
							removeEffect(player, effectName);
						}
						
						player.addEffect(effect);
						
						send(effectName + " Effect applied to " + player.getName(), client);
						debug("Game> " + "added " + effectName + " to " + player.getName() + ".");
					}
				}
			}

			player.updateCurrentState();

			return true;
		//}
		/*catch (final Exception e) {
			// TODO why are catching just any old exception here?
			debug ( e );
			
			return false;
		}*/
	}
	
	// TODO these two methods seems somewhat redundant
	public void removeEffect(final Player player, final String effectName) {
		//player.removeEffect( new Effect(effectName) );
		player.removeEffect( effectName );
	}
	
	// remove, from player, effect
	public void removeEffect(final Player player, final Effect effect) {
		final String effectName = effect.getName();
		
		player.removeEffect(effect);
		
		send(effectName + " effect removed.", player.getClient());
	}

	/**
	 * Boot/Kick a player off the game immediately
	 * 
	 * NOTE: command ?
	 * 
	 * @param c the client to kick
	 * @return  true (succeeded), false (failed for some reason)
	 */
	public void kick(final Client c) {
		init_disconn(c, false);
	}

	/**
	 * getTime
	 * 
	 * get a brand new time object that holds the current time
	 * 
	 * NOTES: - includes hours, minutes, and seconds - only holds the exact time
	 * when called, does not do any counting or do anything else - this is the
	 * "real world" time not the GAME time.
	 * 
	 * @return
	 */
	public Time getTime() {
		return getTime(Constants.REAL);
	}

	public Time getTime(int type) {
		int hour = 0, minute = 0, second = 0;
		
		if( type == Constants.GAME ) {
			// get the hour, minute, and second
			hour = game_time.getHours();
			minute = game_time.getMinutes();
			second = game_time.getSeconds();
		}

		else if( type == Constants.REAL ) {
			// get current data
			Calendar rightNow = Calendar.getInstance();

			// get the hour, minute, and second
			hour = rightNow.get(Calendar.HOUR);
			minute = rightNow.get(Calendar.MINUTE);
			second = rightNow.get(Calendar.SECOND);
		}
		
		// create and return a new Time object with the current time
		return new Time(hour, minute, second);
	}
	
	public Date getDate() {
		return getDate(Constants.REAL);
	}

	public Date getDate(int type) {
		int month = 0, day = 0, year = 0;
		
		if( type == Constants.GAME ) {
			month = game_time.getMonth();
			day = game_time.getDay();
			year = game_time.getYear();	
		}
		else if( type == Constants.REAL ) {
			// get current data
			Calendar rightNow = Calendar.getInstance();
			
			// TODO kludgy....
			
			// get the month, day, and year
			month = rightNow.get(Calendar.MONTH) + 1;
			day = rightNow.get(Calendar.DAY_OF_MONTH);
			year = rightNow.get(Calendar.YEAR);
		}
		
		return new Date(month, day, year);
	}

	/**
	 * skill_check
	 * 
	 * perform a skill check
	 * 
	 * NOTE: checks against a player by pulling the appropriate values from the specified player
	 * 
	 * @param s        the skill "object"
	 * @param diceRoll a dice roll specified by a string (ex. '1d4' to roll a single d4 or 4-sided die)
	 * @param DC       the DC(difficulty) check you are comparing your skill against
	 * @return         true (succeeded in passing DC), false (failed to pass DC)
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

		if (skill + roll >= DC) {
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
		for (final Room room : getWeatherRooms()) {
			if (objectDB.getPlayersByRoom( room ).size() == 0) continue;

			debug("" + room.getDBRef(), 4);
			
			String msg = null;
			
			final String wsName = room.getWeather().getState().getName();
			
			switch (wsName) {
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

			if (msg != null) {
				// broadcast(msg, r);
				addMessage( new Message(msg, room) );
			}
		}
		// end old WeatherLoop code
	}

	/**
	 * Loop through the players, make sure we are the only one poking at the
	 * current one, then make single increment adjustments to their location
	 * 
	 * NOTE: seems to explode where x != y for the destination
	 */
	public void handleMovement() {
		synchronized (this.moving) { // we don't want anyone to modify this list while we're manipulating things
			for (final Mobile mobile : this.moving) {
				// synchronized(player) {
				// if the player is moving (something else could change this)
				if (mobile.isMoving()) {
					boolean isPlayer = false;

					Player player = null;

					if (mobile instanceof Player && !(mobile instanceof NPC)) {
						// final Player player = (Player) mobile;
						player = (Player) mobile;
						isPlayer = true;
					}

					final Point position = mobile.getPosition();       // current player position
					final Point destination = mobile.getDestination(); // current player destination

					if (position.getX() != destination.getX() || position.getY() != destination.getY() || position.getZ() != destination.getZ()) {
						if (isPlayer) {
							final String pos = position.getX() + ", " + position.getY();
							addMessage( new Message(null, player, "Current Location: " + pos) );
						}

						// move diagonally to reach the destination
						// NOTE: not the best way, but it'll have to til I can
						// implement some kind of pathfinding
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

						if (position.getZ() < destination.getZ()) {
							mobile.changePosition(0, 0, 1);
						}
						else if (position.getZ() > destination.getZ()) {
							mobile.changePosition(0, 0, -1);
						}

						if (isPlayer) {
							// tell us about our new location
							String loc = position.getX() + ", " + position.getY();
							String dest = destination.getX() + ", " + destination.getY();
							
							addMessage( new Message(null, player, "New Location:         " + loc) );
							addMessage( new Message(null, player, "Destination Location: " + dest) );
						}

						// tell us if we reached the destination this time
						if (position.getX() == destination.getX() && position.getY() == destination.getY() && position.getY() == destination.getY()) {
							mobile.setMoving(false);

							// if the mobile is a Player (or NPC) and flys to
							// the ground they will be marked as not flying
							if (isPlayer) {
								final Race race = player.getRace();

								if (race.canFly()) {
									if (player.isFlying()) {
										final Point pt = player.getPosition();

										if (pt.getZ() == 0) {
											player.setFlying(false);
										}
									}
								}
							}

							moving.remove(mobile);

							if (isPlayer) {
								addMessage( new Message(null, player, "You have reached your destination") );
							}
						}
					}
				}
			}
		}
	}

	public void handleWeatherEffects() {
		// for each player
		for (final Player player : players) {
			final Room room = (Room) getRoom(player.getLocation());
			final RoomType roomType = room.getRoomType();

			// if they're outside
			if (roomType == RoomType.OUTSIDE) {
				// simple view:
				// cold/hot, dry/wet, happy/sad? (character mood) as a spectrum
				// depending on the weather
				final String wsName = room.getWeather().getState().getName().toLowerCase();
				
				switch (wsName) {
				case "clear skies":
					break;
				case "cloudy":
					break;
				case "rain":
					// test if character is protected from rain or wearing
					// rain proof clothing
					boolean rainproof = false;
					boolean rainprotected = true;
					int rainprotection = 1;

					if (rainproof) {
					}
					else if (rainprotected) {
						// compare rain protection to magnitude of rain/weather
					}
					else {
					}

					// figure out how long the player has been
					final Integer i = player.getProperty("wet", Integer.class);

					if (i != null) {
						if (Utils.range(i, 0, 5)) {

						}
					}

					break;
				case "thunderstorm":
					break;
				case "winter storm":
					break;
				default:
					break;
				}
			}
		}
	}

	/*
	 * push the thing if player is able to push it (strength check) over by one
	 * space if possible (destination check) and check the triggers on the
	 * "tile" to see if there are any things that should happen. If an exit is
	 * obscured by the rock, then it should be indicated what you see behind the
	 * rock and the next look should reveal any exits there might be
	 */
	private void cmd_push(final String arg, final Client client) {
		final Player player = getPlayer(client);
		final Room room = getRoom( player.getLocation() );

		final Thing thing = getThing(arg, room);

		boolean canMove = false;

		// get weight of thing
		double weight = thing.getWeight();

		// make strength check to see if we can actually push it (str > weight / 4)
		if (player.getAbility(Abilities.STRENGTH) > weight / 4) { // able to move?
			debug(player.getAbility(Abilities.STRENGTH) + " > " + weight / 4 + "? true");
			send("Success!", client);
			canMove = true;
		}
		else {
			debug(player.getAbility(Abilities.STRENGTH) + " > " + weight / 4 + "? false");
			send("Failure.", client);
		}

		/*
		 * side note: if the rock was on some kind of sliding mechanism or
		 * aligned with a groove in the floor it might suddenly slide away, if
		 * so, one could logically be surprised and fall over a balance check
		 * might be appropriate
		 * 
		 * unfortunately, you'd probably remember this the second or third time,
		 * which would complicate modeling the sequence of events. perhaps we
		 * should have an alertness check to see how cautiously you push the
		 * boulder
		 * 
		 * a cautious person might not be caught off guard while an incautious
		 * one might lean on any rock, and perchance find a secret passage and
		 * end up tumbling in.
		 */

		if (canMove) {
		}
	}

	protected void cmd_pull(final String arg, final Client client) {
		final Player player = getPlayer(client);
	}

	/**
	 * Command to load other commands.
	 * 
	 * NOTE: This must always be loaded, or else you will not be able to load
	 * any unloaded commands.
	 * 
	 * RETURN: If the returned boolean is false, the command could not be
	 * loaded. It may be that the command is already loaded, cannot be loaded
	 * (error in the code?), or there is no such command to load (absence of the
	 * .class file?).
	 * 
	 * @param arg      the name of the command to load
	 * @param client   the client that called the command (not needed here?)
	 * @return boolean a true/false indicating whether or not the command was loaded.
	 */
	public boolean cmd_loadc(String arg, Client client) {
		String[] args = arg.split("=");

		if (args.length >= 2) {

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
				catch (final InstantiationException inse) {
					debug( inse );
				}
				catch (IllegalAccessException iae) {
					debug( iae );
				}

				// send("(Success) Specified Class is of the type Command or a sub-class of it!", client);
				// send("(Error) Specified Class is not of the type Command or a sub-class of it!", client);
			}
			catch (final ClassNotFoundException cnfe) {
				debug( cnfe );
			}

			return false;
		}

		return false;
	}

	/**
	 * Command to unload other commands.
	 * 
	 * NOTE: This must always be loaded, or else you will not be able to unload
	 * any loaded commands.
	 * 
	 * RETURN: If the returned boolean is false, the command could not be
	 * unloaded. It may be that the command has not been loaded, there is no
	 * such command to unload (absence of the .class file?), or a call to the
	 * command is still in the queue. You will not be able to unload the command
	 * until the queue contains no calls to it. At some future time, there may
	 * be a means to disable the command so it cannot be called again, although
	 * the command queue must still clear.
	 * 
	 * @param arg      the name of the command to unload
	 * @param client   the client that called the command (not needed here?)
	 * @return boolean a true/false indicating whether or not the command was unloaded.
	 */
	public boolean cmd_unloadc(String arg, Client client) {
		boolean commandUnloaded = false;

		if (commandMap.containsKey(arg)) {
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
	 * gives you a reference to the player list from which you cannot remove or
	 * add players, or clear the list
	 */
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(this.players);
	}

	/**
	 * Evaluate the player and return a string that describes their appearance,
	 * using the calling player's stats/skills/abilities to observe to decide
	 * what they could or couldn't tell from looking.
	 * 
	 * <br />
	 * <br />
	 * 
	 * <b>NOTE:</b><br />
	 * this will eventually be part of the dynamic naming engine, which will
	 * evaluate character attributes, properties, etc and come up with a name. I
	 * need both the caller and the called upon so I can figure player
	 * perception and visible target attributes in what I tell the player about
	 * the target
	 * 
	 * @param caller
	 *            the player who looks at a player
	 * @param player
	 *            the player being looked at
	 * @return a string that describes a player's appearance and proposes some
	 *         assumptions based on the calling player's ability to "observe"
	 */
	public String evaluate(Player caller, Player player) {
		return "";
	}

	/**
	 * Evaluate a list via the program parser (parse_pgm)
	 * 
	 * @param list
	 *            the text list we wish to evaluate as code
	 * @return an arraylist of strings public ArrayList<String>
	 *         evaluateList(ArrayList<String> list) { return null; }
	 */

	/* ? */

	/**
	 * Generate a player from it's database representation
	 * 
	 * NOTE: for testing purposes only now, init_conn doesn't go through
	 * loadObjects, which is pointless when you consider that I only hold onto a
	 * copy of the objects and it never goes into the player's array.
	 * 
	 * NOTE2: meant to solve a problem where I haven't copied the load code into
	 * init_conn, but want a properly initialized/loaded player for existing
	 * characters when they login
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

		oPassword = attr[5];     // 5 - player password
		os = attr[6].split(","); // 6 - player stats
		om = attr[7].split(","); // 7 - player money
		int access;              // 8 - player permissions
		int raceNum;             // 9 - player race number (enum ordinal)
		int classNum;            // 10 - player class number (enum ordinal)

		/*
		 * debug("Database Reference Number: " + oDBRef); debug("Name: " +
		 * oName); debug("Flags: " + oFlags); debug("Description: " + oDesc);
		 * debug("Location: " + oLocation);
		 */

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
		catch (final NumberFormatException nfe) {
			debug("loadPlayer(): invalid race number (number format exception)");
			
			debug( nfe );
		}
		finally {
			player.setRace(Races.NONE);
		}

		/* Set Player Class */
		try {
			classNum = Integer.parseInt(attr[10]);
			player.setPClass(Classes.getClass(classNum));
		}
		catch (final NumberFormatException nfe) {
			debug("loadPlayer(): invalid class number (number format exception)");
			
			debug( nfe );
		}
		finally {
			player.setPClass(Classes.NONE);
		}

		debug("DEBUG (db entry): " + player.toDB());

		return player;
	}

	/* Creation Functions */

	public Creature createCreature() {
		final Creature cre = new Creature();

		cre.setFlags(EnumSet.noneOf(ObjectFlag.class));
		cre.setLocation(Constants.VOID);

		objectDB.addAsNew(cre);
		objectDB.addCreature(cre);

		return cre;
	}

	/**
	 * create a new basic, untyped Item for us to modify and work on
	 * 
	 * @return
	 */
	private Item createItem() {
		final Item item = new Item(-1);

		item.setName("");
		item.setFlags(EnumSet.noneOf(ObjectFlag.class));
		item.setDesc("");
		item.setLocation(-1);

		// TODO remember to make the created items get passed through init
		// objectDB.addAsNew(item);
		// objectDB.addItem(item);

		return item;
	}

	public Item createItem(String name, String description, int location) {
		final Item item = new Item(-1);

		item.setName(name);
		item.setFlags(EnumSet.noneOf(ObjectFlag.class));
		item.setDesc(description);
		item.setLocation(location);

		return item;
	}
	
	/**
	 * Create an Item based on an existing prototype, identified by a string.
	 * 
	 * @param prototype
	 * @return
	 */
	public Item createItem(final String prototype) {
		return createItem(prototype, true);
	}
	
	/**
	 * Create an Item based on an existing prototype, identified by a string.
	 * 
	 * @param prototype id of an existing prototype
	 * @param init      should we initialize this item (add to database)
	 * @return
	 */
	public Item createItem(final String prototype, final boolean init) {
		final Item template = prototypes.get(prototype);
		
		if (template != null) {
			final Item newItem = template.getCopy();

			if (init) {
				objectDB.addAsNew(newItem);
				objectDB.addItem(newItem);
			}

			return newItem;
		}
		else {
			debug("ERROR: null template?!");
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
	 * @return         the new item we just created
	 */
	/*
	 * private Item createItem(Item template) { if( !template.isUnique() ) {
	 * final Item item = createItems(template, 1).get(0);
	 * item.setLocation(Constants.WELCOME_ROOM); return item }
	 * 
	 * return null; }
	 * 
	 * private Item createItem(Weapon template) { if( !template.isUnique() ) {
	 * final Item item = createItems(template, 1).get(0);
	 * item.setLocation(Constants.WELCOME_ROOM); return item; } return null; }
	 * 
	 * private Item createItem(Book template) { if( !template.isUnique() ) {
	 * final Item item = createItems(template, 1).get(0);
	 * item.setLocation(Constants.WELCOME_ROOM); return item; }
	 * 
	 * return null; }
	 * 
	 * private Item createItem(Armor template) { if( !template.isUnique() ) {
	 * final Item item = createItems(template, 1).get(0);
	 * item.setLocation(Constants.WELCOME_ROOM); return item; }
	 * 
	 * return null; }
	 */

	/**
	 * Create new items using an existing item as a template. More or less a
	 * means to make multiple copies of an item
	 * 
	 * <br />
	 * <br />
	 * 
	 * <b>NOTE:</b> internal use only
	 * 
	 * @param template the item to base the new ones on
	 * @param numItems how many new items to make.
	 * @return         the new items we just created
	 */
	private ArrayList<Item> createItems(final Weapon template, final Integer numItems) {
		ArrayList<Item> items = new ArrayList<Item>(numItems);

		for (int i = 0; i < numItems; i++) {
			final Weapon item = template.getCopy();
			items.add(item);
			initCreatedItem(item);
		}

		return items;
	}

	private ArrayList<Item> createItems(final Book template, final Integer numItems) {
		final ArrayList<Item> items = new ArrayList<Item>(numItems);

		for (int i = 0; i < numItems; i++) {
			final Book book = template.getCopy();
			items.add(book);
			initCreatedItem(book);
		}

		return items;
	}

	private ArrayList<Item> createItems(final Armor template, final Integer numItems) {
		final ArrayList<Item> items = new ArrayList<Item>(numItems);

		for (int i = 0; i < numItems; i++) {
			final Armor armor = template.getCopy();
			items.add(armor);
			initCreatedItem(armor);
		}

		return items;
	}

	// TODO need to deal with the ambiguity of stat values...
	public NPC createNPC(final String name, final Room location) {
		return createNPC(name, null, new Integer[] { 0, 0, 0, 0, 0, 0 }, location);
	}

	public NPC createNPC(final String name, final Race race, final Integer[] stats, final Room location) {
		NPC npc = new NPC(name);

		npc.setRace(race);

		int index = 0;

		final Ability[] ab = rules.getAbilities();

		for (final Integer i : stats) {
			if (index < ab.length) {
				npc.setAbility(ab[index], i);
				index++;
			}
		}

		npc.setLocation(location.getDBRef());

		initCreatedNPC(npc);

		final Room room = getRoom( npc.getLocation() );
		if (room != null) room.addListener(npc);

		return npc;
	}

	/**
	 * Create and return a new room object with the specified name and parent
	 * room and a unique database reference number.
	 * 
	 * @param roomName   the name for the new room
	 * @param roomParent the room that is the "parent" of this one
	 * @return           the new room object
	 */
	private Room createRoom(String roomName, int roomParent) {
		return createRoom(roomName, "You see nothing.", roomParent);
	}

	private Room createRoom(String roomName, String roomDescription, int roomParent) {
		// flags are defined statically here, because I'd need to take in more
		// variables and in no
		// case should this create anything other than a standard room which has
		// 'RS' for flags

		return new Room(-1, roomName, EnumSet.of(ObjectFlag.SILENT), roomDescription, roomParent);
	}

	// TODO check and see what the location is set to by default in MUDObject,
	// subclass constructors
	
	// INITIALIZE OBJECTS
	// creates a database entry and assigns a db ref
	
	private void initCreatedObject(final MUDObject m) {
		this.objectDB.addAsNew(m);
		
		//if( m.isType(TypeFlag.ITEM) )  
		
		switch( m.type ) {
		case OBJECT: break;
		case ITEM:   this.objectDB.addItem((Item) m);   break;
		case THING:  this.objectDB.addThing((Thing) m); break;
		case NPC:   this.objectDB.addNPC((NPC) m);     break;
		default:     break;
		}
	}
	
	/**
	 * initialize a created item (add to database)
	 * 
	 * @param item
	 */
	private void initCreatedItem(final Item item) {
		this.objectDB.addAsNew(item);
		this.objectDB.addItem(item);
	}
	
	/**
	 * initialize a created thing 
	 * @param thing
	 */
	private void initCreatedThing(final Thing thing) {
		this.objectDB.addAsNew(thing);
		this.objectDB.addThing(thing);
	}
	
	/**
	 * initialized a created NPC
	 * @param npc
	 */
	private void initCreatedNPC(final NPC npc) {
		this.objectDB.addAsNew(npc);
		this.objectDB.addNPC(npc);
	}

	/**
	 * Generate an exits list filtered by visibility based on parameters
	 * 
	 * potential parameters - current location in the room (nearness to it) -
	 * visibility (is there fog) - lighting (is it light or dark)
	 * 
	 * criteria?: flags are... flags does not contain...
	 * 
	 * @param exits   the lists of exits to filter
	 * @param filters the filters to apply
	 * @return        the filtered list of exits
	 */

	// What is this for?
	public List<MUDObject> filterByLocation(List<MUDObject> objects, String filter) {
		List<MUDObject> list = new LinkedList<MUDObject>();

		for (final MUDObject m : objects) {
		}

		return list;
	}

	/**
	 * Run a weather update.
	 * 
	 * This should go through the rooms that "need" updating and cause them to
	 * proceed from the current weather state to a new weather state based on
	 * probability.
	 */
	public void updateWeather() {
		if (!use_weather) return; // fast exit if weather is disabled

		weather.nextState();

		final WeatherState ws = weather.getState();

		// if no change occurred
		if (ws.upDown != 1 && ws.upDown != -1) return;

		String changeText = (( ws.upDown == 1 ) ? ws.transUpText : ws.transDownText);

		for (final Room room : getWeatherRooms())  {
			if (changeText != null) {
				addMessage( new Message(changeText, room) );
				debug(changeText);
			}
		}
		
		/*for (final Room room : objectDB.getWeatherRooms()) {
			room.getWeather().nextState();

			final WeatherState ws = room.getWeather().getState();
			
			// if no change occurred
			if (ws.upDown != 1 && ws.upDown != -1) return;

			String changeText = (( ws.upDown == 1 ) ? ws.transUpText : ws.transDownText);

			if (changeText != null) {
				addMessage( new Message(changeText, room) );
				debug(changeText);
			}
		}*/
	}

	/* Help, Topic Files Stuff */

	/**
	 * Get a help file by name, and return it as a string array.
	 * 
	 * @param name
	 *            the name of the help file to gt
	 * @return a string array that contains the file's contents
	 */
	public String[] getHelpFile(final String name) {
		// part below is needed so that command aliases still get you to the
		// same helpfile

		// TODO consider whether reporting the presence oof the help file is
		// important and where it should happen

		// System.out.println("Help File? " + name)

		String command = aliases.get(name);

		if (command != null) {
			if (helpTable.containsKey(command)) {
				// System.out.println("Help File Exists!");
				return helpTable.get(aliases.get(name));
			}
			else return null;
		}
		else {
			if (helpTable.containsKey(name)) {
				// System.out.println("Help File Exists!");
				return helpTable.get(name);
			}
			else return null;
		}
	}

	public String[] getTopicFile(final String name) {
		return topicTable.containsKey(name) ? topicTable.get(name) : null;
	}

	public String[] generateHelpFileIndex() {
		final String path = HELP_DIR;

		final List<String> fileList = new LinkedList<String>();

		String[] hfIndex;

		try {
			final File dir = new File(path); // could throw NullPointerException, but shouldn't

			// TODO possibly replace checks below with a FilenameFilter ?
			if( dir.isDirectory() ) {
				for (final File file : Arrays.asList( dir.listFiles() )) {
					if ( file.isFile() ) {
						final String fileName = file.getName();

						if ( fileName.endsWith(".help") || fileName.endsWith(".HELP") ) {
							fileList.add(fileName);
						}
					}
				}

				hfIndex = Utils.listToStringArray(fileList);
			}
			else hfIndex = new String[0];
		}
		catch(final NullPointerException npe) {
			System.out.println("Error: invalid help dir. (NullPointerException).");
			
			debug( npe );

			hfIndex = new String[0];
		}

		return hfIndex;
	}

	public String[] generateTopicFileIndex() {
		// Directory path here
		String path = TOPIC_DIR;

		List<String> fileList = new ArrayList<String>();

		for (File file : Arrays.asList(new File(path).listFiles())) {
			if (file.isFile()) {
				String filename = file.getName();

				if (filename.endsWith(".topic") || filename.endsWith(".TOPIC")) {
					fileList.add(filename);
				}
			}
		}

		return Utils.listToStringArray(fileList);
	}

	/**
	 * Load a newly created thing into the database.
	 * 
	 * currently unused
	 * 
	 * @param thing the thing to load
	 * @return true if the thing was successfully loaded, false otherwise
	 *         private boolean loadThing(Thing thing) { int dbref =
	 *         thing.getDBRef();
	 * 
	 *         // I want to be sure not to overwrite anything if
	 *         (main.get(dbref).split("#")[4].equals("-1")) { // need to check
	 *         to see if something is there already (dbref == -1 means a
	 *         NULLObject) main.set(dbref, thing.toDB()); // modify database
	 *         entry }
	 * 
	 *         if (main1.get(dbref) instanceof NullObject || main1.get(dbref) ==
	 *         thing) { // if main1 holds a NULLObject or the exact same thing
	 *         main1.set(dbref, thing); // modify in-memory database }
	 * 
	 *         things.add(thing); // put in the things list
	 * 
	 *         getRoom(thing.getLocation()).contents.add(thing); // put in the
	 *         room's content
	 * 
	 *         return true; }
	 */

	/**
	 * parseDesc
	 * 
	 * Breaks down a long string into a list of strings, none of whom may be
	 * more than LIMIT number of characters long.
	 * 
	 * @param description the string to wrap at LIMIT characters
	 * @param line_limit  the maximum length of a string to send
	 * @return TODO
	 */
	public List<String> parseDesc(final String description, final int line_limit) {
		final List<String> retVal = new LinkedList<String>();
		
		final StringBuilder result = new StringBuilder(line_limit);

		String temp;

		/*
		 * boolean nl_begin = false; boolean nl_middle = false; boolean nl_end =
		 * false;
		 */

		// TODO fix checking and handling of a newline marker (maybe make it #n
		// or #nl/$n or $nl)
		for (final String word : description.split(" ")) { // ^[a-zA-Z]$ ^\\b$
			// TODO find a way to make sure this usually doesn't show up in
			// debug
			// debug("result: " + result, 4);
			// debug("result (length): " + result.length(), 4);
			// debug("next: " + word, 4);
			// debug("next (length): " + word.length(), 4);

			// newline handling
			if ( word.contains("&n") ) {
				Tuple<String, String> temp1 = new Tuple<String, String>("", word);

				while ( !temp1.two.equals("") ) {
					System.out.println("WHILE (TOP)");
					System.out.println("temp1 (1): " + temp1.one);
					System.out.println("temp1 (2): " + temp1.two);
					System.out.println("result: " + result.toString());

					Utils.handle_newline_in_word(temp1.two, temp1);

					if ( temp1.one.equals("&n") ) {
						if (result.length() != 0) {
							retVal.add( result.toString() );   // add the current string to the return list
							result.delete(0, result.length()); // clear the buffer
						}
						else {
							retVal.add( "" ); // add the empty string to the return list
						}
					}
					else {
						// intelligently append word
						if (result.length() != 0) result.append(" " + temp1.one);
						else                      result.append(temp1.one);

						/*
						 * if (result.length() < 1) { // append current word if
						 * empty result.append(word); } else if (result.length()
						 * + word.length() + 1 < line_limit) { // append current
						 * word if it won't overflow
						 * result.append(" ").append(word); }
						 * 
						 * else { // if it will overflow, send and clear, and
						 * append current word send(result, client);
						 * result.delete(0, result.length());
						 * result.append(word); }
						 */
					}

					System.out.println("WHILE (BOTTOM)");
					System.out.println("temp1 (1): " + temp1.one);
					System.out.println("temp1 (2): " + temp1.two);
					System.out.println("result: " + result.toString());
				}

				// result.append(" " + temp1.one);

				//continue;
			}

			/*
			 * 
			 * // newline check nl_begin = word.startsWith("$n"); nl_middle =
			 * word.contains("$n"); nl_end = word.endsWith("$n");
			 * 
			 * //debug("Newline? " + nl_middle, 4);
			 * 
			 * // need to integrate the following three cases into the three
			 * primary cases somehow // converting the array above into a list
			 * so that I break words containing \n up // and insert the other
			 * part before the following word might help
			 * 
			 * if(nl_begin) { //debug("send", 4); send(result, client); // send
			 * the current contents of the buffer result.delete(0,
			 * result.length()); // clear the buffer temp = word.substring(
			 * word.indexOf("$n") + 2 ); // everything after '$n'
			 * result.append(temp); // append the word after the newline }
			 * 
			 * // append the first part (before the \n) if(nl_middle) { temp =
			 * word.substring(0, word.indexOf("$n"));
			 * result.append(" ").append(temp); //debug("send", 4); send(result,
			 * client); result.delete(0, result.length());
			 * result.append(word.substring(word.indexOf("$n") + 1,
			 * word.length())); continue; }
			 * 
			 * if(nl_end) { temp = word.substring(0, word.indexOf("$n")); // get
			 * everything before the newline result.append(" ").append( temp );
			 * // append everything before the newline //debug("send", 4);
			 * send(result, client); // send the current contents of the buffer
			 * result.delete(0, result.length()); // clear the buffer continue;
			 * }
			 */

			else {
				if (result.length() < 1) {
					// append current word if empty
					
					result.append(word);
				}
				else if (result.length() + word.length() + 1 < line_limit) {
					// append current word if it won't overflow, consider the added space
					
					// debug("add", 4);
					result.append(" ").append(word);
				}

				/*
				 * // interesting idea, but not very good results else if
				 * (result.length() + word.length() + 1 > line_limit) { // split
				 * the word so it fits int max = line_limit - result.length() -
				 * 1; debug("add", 4); if( max < word.length() ) {
				 * result.append(" ").append(word.substring(0, max -
				 * 1)).append("-"); } }
				 */

				else {
					// if it will overflow: add string to return list, clear buffer, and append current word
					
					retVal.add( result.toString() );
					
					result.delete(0, result.length());
					
					result.append(word);
				}
			}
		}

		// make sure we send the last word if there was only one left
		if (result.length() > 0) {
			// debug("send", 4);
			//send(result.toString(), client);
			retVal.add( result.toString() );
			
			result.delete(0, result.length());
		}
		
		return retVal;
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
		final Room room = getRoom( player.getLocation() );
		
		final int portalOrigin = portal.getOrigin();
		final int portalDest = portal.getDestination();

		final boolean playerAtPortal = player.getPosition().equals( portal.getPosition() );

		final boolean missingRequiredKey = portal.requiresKey() && !portal.hasKey(player);

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

		if (success) {
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
	 * Special use function for wands that handles applying or removing effects
	 * to/from the player, removing the charges expended, and if all charges are
	 * expended marking the wand as spent and/or empty.
	 * 
	 * @param potion the potion to use
	 * @param client the client
	 */
	private void use_wand(final Wand wand, final Client client) {
		if (wand.charges > 0) {
			Spell.decodeTargets( wand.getSpell() );

			send("You use your Wand of " + wand.spell.getName() + " to cast " + wand.spell.getName() + " on yourself.", client);

			debug("Game> Casting..." + wand.spell.getName());

			try {
				// cmd_cast(wand.spell.name, client);
				// getCommand("cast").execute(wand.spell.name, client);
				cmd("cast " + wand.spell.getName(), client);
			}
			catch (final Exception e) {
				// TODO what exception are we expecting to catch here?
				debug( e );
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
	 * Special use function for potions that handles applying or removing
	 * effects to/from the player and disposing of the potion item itself if it
	 * should disappear.
	 * 
	 * @param potion
	 *            the potion to use
	 * @param client
	 *            the client
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
		objectDB.remove(potion); // remove from "live" database (replace with NullObject)
	}

	/**
	 * Checks access/permissions level against the queried access and returns
	 * true or false depending on whether they meet/exceed the specified access
	 * level or not. If the client is associated with a player, then the
	 * player's access is checked, otherwise it's checked against 0 (basic user
	 * permissions).
	 * 
	 * @param client
	 * @param accessLevel
	 * 
	 * @return
	 */
	public boolean checkAccess(final Player player, final int accessLevel) {
		int check = Constants.USER;

		if (player != null) {
			check = player.getAccess();
		}

		return check >= accessLevel;
	}

	/**
	 * Handle aborting an error, takes an error message to print as an argument
	 */
	public void abortEdit(final String errorMessage, final String old_status, final Client client) {
		final Player player = getPlayer(client);
		
		final String msg = String.format("Game> %s - Error: %s", player.getEditor().getName(), errorMessage);
		
		// reset player status, clear edit flag and drop editor data (plus return editor setting to None)
		player.setStatus(old_status);
		player.setEditor(Editors.NONE);
		player.setEditorData(null);
		

		// tell us what went wrong
		//send(errorMessage, client);
		send(msg, client);
	}

	/**
	 * Load an area into the game from a text file
	 * 
	 * NOTE: really more like creating an area, since the rooms will exist next
	 * time round and we won't run this on the same area twice?
	 */
	/*public void importArea(final String filename) {
		final String[] file = Utils.loadStrings(filename);

		int step = 0; // 0=AREA, 1=ROOM
		
		Zone zone = new Zone("test", null);

		String name;

		for (final String str : file) {
			if ( str.equals("@AREA") ) {
				step = 0;
				continue;
			}
			else if ( str.equals("@ROOM") ) {
				step = 1; continue;
			}

			final String[] data = str.split(":");

			final String key = data[0].trim();
			final String value = data[1].trim();

			switch(step) {
			case 0:
				// area
				if ( key.equals("name") ) {
					area.setName(value);
				}
				else if ( key.equals("registered") ) {
					if(Utils.toInt(value, -1) == 1) { // is this area "registered"
						step = 1;
					}
				}
				else if ( key.equals("rooms") ) {
					area.setSize( Utils.toInt(value, 0) );

					if( area.getSize() > 0 ) step = 1;
				}
				
				break; 
			case 1:
				// room
				// basically, for a brand new area, we just create each room as it's specification popsup
				final Room room = new Room(); // creates a "blank" room with basic flags and locks, a location and desc borders

				if ( key.equals("dbref") ) {
					objectDB.addAsNew(room);
				}
				else if (key.equals("name") ) {
					room.setName(value);
				}
				else if ( key.equals("desc") ) {
					room.setDesc(value);
				}
				
				zone.addRoom(room);
				
				break;
			default: break;
			}
		}
	}*/
	
	/*
	 * // pass arguments to the object creation function else if (
	 * cmd.equals("@create") || ( aliasExists && alias.equals("@create") ) ) {
	 * adminCmd = true; // run the object creation function cmd_createItem(arg,
	 * client); } else if ( cmd.equals("@create_creature") ) { String[] args =
	 * arg.split("="); createCreature(args[0], args[1], args[2]); }
	 */

	public void displayContainer(Container c, Client client) {
		String top = MudUtils.getTop(c), side = MudUtils.getSide(), bottom = MudUtils.getBottom(c);
		
		int displayWidth = c.getDisplayWidth();

		String head = c.getName() + "(#" + c.getDBRef() + ")";

		send(side + top + side + Utils.padRight("", ' ', 39) + side, client);

		send(side + colors(Utils.padRight(head, ' ', 30), "yellow") + side + Utils.padRight("", ' ', 39) + side, client);

		send(side + top + side + Utils.padRight("", ' ', 39) + side, client);

		for (final Item item : c.getContents()) {
			send(side + colors(Utils.padRight(item.getName(), ' ', 30), "yellow") + side + Utils.padRight("", ' ', 39) + side, client);
		}

		send(side + bottom + side + Utils.padRight("", ' ', 39) + side, client);
	}

	/* Time Methods */
	public void onSecondIncrement() {
		// TODO how do I decide idleness
		checkForIdlePlayers();
		
		checkTimers();
		
		if( game_time.getSeconds() % 6 == 0 ) {
			handleCombat();
		}
	}

	/**
	 * anything that needs to execute once per in-game minute should go here
	 */
	public void onMinuteIncrement() {
		// report the time
		int hour = game_time.getHours();
		int minute = game_time.getMinutes();

		debug("Time loop: " + hour + ":" + minute);

		try {
			handleMovement();
			handleWeatherEffects();
		}
		catch (final ConcurrentModificationException cme) {
			// TODO is this debug message adequate?
			debug(cme.getMessage());
			
			debug( cme );
		}

		int weather_update_interval = game_time.getWeatherUpdateInterval();
		
		// server wide weather report?
		if ((minute % weather_update_interval) == 0) {
			broadcastWeather();
		}
	}

	/**
	 * anything that needs to execute once per in-game hour should go here
	 */
	public void onHourIncrement() {
		debug("Filling shops with merchandise!");
		fillShops();
		debug("weather update");
		updateWeather();
	}

	/**
	 * anything that needs to execute once per in-game day should go here
	 */
	public void onDayIncrement() {
		// live_backup();
	}

	public void onMonthIncrement() {
	}

	public void onYearIncrement() {
	}

	public void live_backup() {
		send("Pausing game!");

		// Pause all combat
		input_hold = true;     // Halt Player Input
		game_time.pauseLoop(); // Pause the time tracking

		final GameMode old_mode = mode;
		
		changeMode(GameMode.MAINTENANCE); // Put the game into Maintenance mode (no new logins, except Wizards)
		
		send("Entering " + mode.toString() + " Mode.");

		send("Backing up game...");

		// backs the current database up to current_database_name.bak
		//backup(BACKUP_DIR + DB_FILE.substring(DB_FILE.lastIndexOf('\'') + 1, DB_FILE.length() - 3) + ".bak");
		String current_db = ( new File(DB_FILE).getName() );
		backup( resolvePath(BACKUP_DIR, current_db.replace(".txt", ".bak")) );

		send("Finished backing up");

		send("Unpausing game!");

		game_time.unpauseLoop(); // Resume tracking time
		input_hold = false; // Resume Player Input
		// Unpause all combat
		
		changeMode(old_mode);
		
		send("Entering " + mode.toString() + " Mode.");
	}

	/**
	 * Evaluate a string and resolve name references
	 * 
	 * NOTE: name references looks like this -> '$house' and is associated with
	 * an integer database reference
	 * 
	 * @param input  an input string that potentially contains a name reference
	 * @param client the client that the string was sent by
	 * @return the original string with any name references within resolved to numbers
	 */
	public String nameref_eval(final String input, final Client client) {
		StringBuilder sb = new StringBuilder(input); // local, stringbuilder copy of original
		StringBuilder refString = new StringBuilder(); // where we'll store the ref. string as we find the characters

		Character ch = null; // the character we'll pull out of the input stringbuffer
		Integer refNum = 0;  // the retrieved number the ref. string refers to

		int index = 0; // current position in sb
		int begin = 0; // beginning of ref. string
		int end = 0;   // end of ref. string

		boolean check = false;   // have we found the beginning of a potential refrence
		boolean replace = false; // do we need to perform a replace operation
		boolean eval = false;    // are we ready to evaluate a complete reference

		while (sb.indexOf("$") != -1) {
			debug("Index: " + index);

			ch = sb.charAt(index); /* get a character */

			if (check) {
				if (Character.isLetter(ch)) {
					debug("" + ch);
					debug("Is a Letter!");

					refString.append(ch);

					if (index == sb.length() - 1) {
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
				if (ch == '$') {
					debug("found a nameref");

					begin = index;
					check = true;

					if (!replace) {
						replace = true;
					}
				}
			}

			if (!check && eval) {
				String reference = refString.toString();

				debug("");
				debug("Game> (argument eval) reference: " + reference);

				// try to get number from nameref table
				if (client != null) {
					if (getPlayer(client).getConfigOption("global-nameref-table")) {
						refNum = getNameRef(reference);
					}
					else {
						refNum = getPlayer(client).getNameRef(reference);
					}
				}
				else {
					refNum = getNameRef(reference);
				}

				debug("refNum: " + refNum);
				debug("DB size: " + objectDB.getSize());

				// modify string, if we got a valid reference (i.e. could be in
				// database, a NULLObject is a valid reference
				if (refNum != null && refNum < objectDB.getSize()) {
					debug("Game> (argument eval) tempI: " + refNum); // report number

					debug("");
					debug("Begin: " + begin + " End: " + end + " Original: " + sb.substring(begin, end + 1) + " Replacement: " + refNum.toString());
					debug("");

					sb.replace(begin, end + 1, refNum.toString());

					debug("BUFFER: " + sb.toString());
					debug("");
				} else { // modify string to remove potential name reference if not valid
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

		if (replace) {
			debug("Game> (argument eval) result: " + sb.toString());

			// change argument to reflect replacements (trim to clear extra
			// space at end)
			return sb.toString();
		}
		else {
			return input;
		}
	}

	public ArrayList<MUDObject> findVisibleObjects(Room room) {
		ArrayList<MUDObject> objectsFound = new ArrayList<MUDObject>();

		for (Thing thing : room.getThings()) {
			if (!thing.hasFlag(ObjectFlag.DARK)) {
				objectsFound.add(thing);
			}
		}

		for (Item item : room.getItems()) {
			debug(item.getName());
			/*
			 * if( !item.hasFlag(ObjectFlag.DARK) ) { objectsFound.add(item); }
			 */
			objectsFound.add(item);
		}

		// are Players and NPCs really objects for this consideration?
		/*
		 * for( Player player : objectDB.getPlayersByRoom(room.getDBRef()) ) {
		 * if( !player.hasFlag(ObjectFlag.DARK) ) { objectsFound.add(player); }
		 * }
		 * 
		 * for( NPC npc : objectDB.getNPCsByRoom(room.getDBRef()) ) { if(
		 * !npc.hasFlag(ObjectFlag.DARK) ) { objectsFound.add(npc); } }
		 */

		return objectsFound;
	}

	/**
	 * Load a board into the bulletinboard structure.
	 * 
	 * This consists of dividing the file into posts, creating bbentry objects
	 * and putting them in a "bulletinboard".
	 * 
	 * @param board the name of the board to load (filename without extension?)
	 */
	private BulletinBoard loadBoard(final String filename) {
		// TODO should derive board name from board file
		// create bulletin board object
		final BulletinBoard bb = new BulletinBoard("temp", filename);
		
		boolean name_set = false;
		
		// TODO path kludges FOR THE WIN!
		final ArrayList<String> lines = loadListDatabase( resolvePath(BOARD_DIR, bb.getFilename()) );

		bb.setInitialCapacity(lines.size());
		
		for (final String line : lines) {
			final String[] entryInfo = line.split("#");
			
			if( line.startsWith("!") ) {
				if( !name_set ) {
					final String nameLine = line.substring(1).trim();
					
					final int dot = nameLine.indexOf('.');
					
					if( dot != -1 ) {
						bb.setName( nameLine.substring(0, dot) );
						bb.setShortName( nameLine.substring(dot + 1, nameLine.length()) );
					}
					else {
						bb.setName(nameLine);
					}
					
					System.out.println("Board name set to: " + bb.getName());
					
					name_set = true;
					continue;
				}
			}

			try {
				final int id = Integer.parseInt(entryInfo[0]);
				final String author = entryInfo[1];
				final String subject = entryInfo[2];
				final String message = entryInfo[3];			
				
				bb.loadEntry( new BBEntry(id, author, subject, message) );
			}
			catch (final NumberFormatException nfe) {
				debug("setup(): loading bulletin board entries (number format exception)");
				
				debug( nfe );
			}
		}
		
		return bb;
	}
	
	private void loadBoards() {
		// WORLD_DIR + world + "\\" + "boards" + "\\" + filename + ".txt";
		final FilenameFilter f = new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".txt");
			}
		};
		
		for(final String fileName : new File(BOARD_DIR).list(f)) {
			System.out.println("board: " + fileName);

			final BulletinBoard board = loadBoard(fileName);
			
			addBoard(board);
		}
	}

	/**
	 * Load a player's mail file into the mailbox structure.
	 * 
	 * This consists of dividing the file into "messages", creating message
	 * objects and putting them in a "mailbox".
	 * 
	 * NOTE: Should we return a mailbox to point the player's mailbox reference
	 * to or continue as is, where we just modify the player's mailbox.
	 * 
	 * @param player the player whose mail file we wish to load
	 */
	private void loadMail(Player player) {
		int msg = 0;
		
		// TODO yay path resolution!
		//String mailBox = WORLD_DIR + world + "\\" + "mail\\mail-" + player.getName() + ".txt";
		String mailBox = resolvePath(WORLD_DIR, world, "mail", "mail-" + player.getName() + ".txt");
		String lines[] = null;

		/*
		 * check for existence of mail file, and abort if it doesn't exist (yet?
		 * no file for brand new players until save)
		 */
		File file = new File(mailBox);

		if (!file.exists()) {
			debug("No such mail file!");
			return;
		}

		lines = Utils.loadStrings(mailBox); // load the file into a string array

		if (lines == null) {
			debug("Could not find mail file for user: " + player.getName());
			send("Could not find mail file for user: " + player.getName(), player.getClient());
			return;
		}

		MailBox mb = player.getMailBox();
		
		// toss current data so that we don't create duplicate objects..
		mb.clear();

		final int SENDER = 0;
		final int RECIP = 1;
		final int SUBJECT = 2;
		final int MSG = 3;
		final int DATE = 4;
		final int FLAG = 5;
		final int MARK = 6;
		final int END = 8;

		String sender = "";
		String recipient = "";
		String subject = "";
		String message = "";
		String date = "";
		char flag = ' ';
		char mark = ' ';

		int part = SENDER;

		System.out.println("MSG: " + msg);

		for (final String line : lines) {
			System.out.println("Line: " + line);

			switch (part) {
			case SENDER:
				sender = line;
				part = RECIP;
				break;
			case RECIP:
				recipient = line;
				part = SUBJECT;
				break;
			case SUBJECT:
				subject = line;
				part = MSG;
				break;
			case MSG:
				message = line;
				part = DATE;
				break;
			case DATE:
				date = line;
				part = FLAG;
				break;
			case FLAG:
				flag = line.charAt(0);
				part = MARK;
				break;
			case MARK:
				mark = line.charAt(0);
				part = END;
				break;
			default:
				break;
			}

			if (part == END) {
				/*boolean validSender = !sender.equals("");
				boolean validRecipient = !recipient.equals("");
				boolean validSubject = !subject.equals("");
				boolean validMessage = !message.equals("");
				boolean validFlag = flag != ' ' && Utils.mkList("A", "R", "U").contains("" + flag);*/
				
				boolean validSdr = !sender.equals("");
				boolean validRpt = !recipient.equals("");
				boolean validSub = !subject.equals("");
				boolean validMsg = !message.equals("");
				boolean validDate = true;
				boolean validFlag = flag != ' ' && Utils.mkList("A", "R", "U").contains("" + flag);
				
				if (validSdr && validRpt && validSub && validMsg && validFlag && validDate && mark == '~') {
					// create mail object
					Mail mail = new Mail(msg, sender, recipient, subject, message, date, flag);

					// add the mail object to the mailbox
					mb.add(mail);

					msg++;
				}
				else {
					// Invalid Message ...
					debug("Invalid Message!");
					debug("Aborting Mail Loading for " + player.getName() + "...");

					return; // if we found an invalid message we can't depend on the rest of the file to be intact
				}

				sender = "";
				recipient = "";
				subject = "";
				message = "";
				flag = ' ';
				date = null;
				mark = ' ';

				part = SENDER;
			}
		}
	}

	/**
	 * saveMail
	 * 
	 * @param player
	 */
	private void saveMail(final Player player) {
		final MailBox mb = player.getMailBox();

		try {
			final String mailFile = String.format("mail-%s.txt", player.getName());
			
			PrintWriter pw = new PrintWriter( resolvePath(WORLD_DIR, world, "mail", mailFile) );

			for (final Mail mail : mb) {
				pw.println( mail.getSender() );    // Sender
				pw.println( mail.getRecipient() ); // Recipient
				pw.println( mail.getSubject() );   // Subject
				pw.println( mail.getMessage() );   // Message
				pw.println( mail.getFlag() );      // Flag (Read/Unread)
				pw.println('~');                   // Mark
			}

			pw.flush();
			pw.close();
		}
		catch (final FileNotFoundException fnfe) {
			debug( fnfe );
		}
	}
	
	/**
	 * compare
	 * 
	 * Compare two items, item1 and item2, based on value
	 * and durability
	 * 
	 * @param item1
	 * @param item2
	 * @return
	 */
	public Item compare(final Item item1, final Item item2) {
		// comparisons:

		// item type -- if not same type, return null as they are not
		// comparable?
		// if( item1.getItemType() == item2.getItemType() )

		// item value -- compare value in terms of money, copper pieces specifically
		if (item1.getValue().numOfCopper() > item2.getValue().numOfCopper()) {
			// item wear
			if ((item1.getDurability() - item1.getWear()) < (item2.getDurability() - item2.getWear())) {
				return item1;
			}
			else {
				return item2;
			}
		}
		else if (item1.getValue().numOfCopper() == item2.getValue().numOfCopper()) {
			return item1;
		}
		else {
			return item2;
		}
	}

	public Item compare(Item item1, Item item2, Object... criteria) {
		// comparisons:
		// item type
		// item wear
		// item value
		return null;
	}

	private void execTrigger(Trigger trig, MUDObject mudObject, Client client) {
		String script = trig.getScript().getText();

		final int numLeftBrace = Utils.countNumOfChar(script, '{');
		final int numRightBrace = Utils.countNumOfChar(script, '}');

		if (numLeftBrace != 0 && numRightBrace != 0 && numLeftBrace == numRightBrace) {
			String interpResult = pgm.interpret(trig.getScript(), getPlayer(client), mudObject);
			send(interpResult, client);
		}
	}

	// Timers

	public List<SpellTimer> getSpellTimers(Player player) {
		return this.spellTimers.get(player);
	}

	public List<EffectTimer> getEffectTimers(Player player) {
		return this.effectTimers.get(player);
	}

	public List<AuctionTimer> getAuctionTimers(Player player) {
		return this.auctionTimers.get(player);
	}

	/**
	 * check for expired timers and clear them (player)
	 * 
	 * @param player
	 */
	/*public void checkTimers(Player player) {
		List<EffectTimer> etl = getEffectTimers(player);
		List<EffectTimer> eff_timers = new LinkedList<EffectTimer>();

		for (final EffectTimer etimer : etl) {
			if (etimer.getTimeRemaining() <= 0 && !etimer.getEffect().isPermanent()) {
				eff_timers.add(etimer);
			}
		}

		for (final EffectTimer etimer : eff_timers) {
			removeEffect(player, etimer.getEffect());
			
			etl.remove(etimer);
		}

		eff_timers.clear();
	}*/

	/**
	 * check for expired timers and clear them
	 */
	public void checkTimers() {
		// TODO can I genericize this list to TimerTask?

		// Effect Timers (when they expire, effects are removed)
		final List<EffectTimer> expired_timers = new LinkedList<EffectTimer>();

		// each player has their own list of effect timers
		for(final Player player : effectTimers.keySet()) {
			final List<EffectTimer> etl = effectTimers.get(player);

			// add the timers that are expired to the expired list
			for (final EffectTimer etimer : etl) {
				if (etimer.getTimeRemaining() <= 0) {
					expired_timers.add(etimer);
				}
			}
			
			// remove each timer in eff from the player's list of effect timers
			for (final EffectTimer etimer : expired_timers) {
				String effectName = etimer.getEffect().getName();

				//player.removeEffect(effectName);
				removeEffect(player, effectName);

				send(etimer.getEffect().getName() + " effect removed.", player.getClient());

				etl.remove(etimer);
			}
		}

		expired_timers.clear();

		///
		
		// Effect Timers (when they expire, effects are removed)
		final List<SpellTimer> expired_timers2 = new LinkedList<SpellTimer>();

		// each player has their own list of effect timers
		for(final Player player : spellTimers.keySet()) {
			final List<SpellTimer> stl = spellTimers.get(player);

			// add the timers that are expired to the expired list
			for (final SpellTimer stimer : stl) {
				if (stimer.getTimeRemaining() <= 0) {
					expired_timers2.add(stimer);
				}
			}

			// remove each timer in eff from the player's list of effect timers
			for (final SpellTimer etimer : expired_timers2) {
				stl.remove(etimer);
			}
		}

		expired_timers2.clear();

		// TODO auction timers -- see below
		// perhaps a global spin through and decrement task of an internal count on Auctions would
		// be more practical?
		List<AuctionTimer> auc_timers = new LinkedList<AuctionTimer>();

		for(final Player player : auctionTimers.keySet()) {
			final List<AuctionTimer> atl = auctionTimers.get(player);

			for (final AuctionTimer atimer : atl) {
				if (atimer.getTimeRemaining() <= 0) {
					auc_timers.add(atimer);
				}
			}

			for (final AuctionTimer atimer : auc_timers) {
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
	// debug("MSP", 2);
	// debug("Filename: " + MSP.fileName, 2);
	// debug("Filetype: " + MSP.fileType, 2);

	/**
	 * Ask the client to play a sound on it's end through MSP<br>
	 * <br>
	 * 
	 * NOTES:<br>
	 * do not call when msp is not enabled.<br>
	 * this is for the special case of the type SOUND<br>
	 * 
	 * 
	 * @param soundFile the name of the sound file to play
	 * @param client    the client to send the message to
	 */
	private void playSound(final String soundFile, final Client client) {
		MSP.play(soundFile, "sound", 25, -1);

		String mspMsg = MSP.generate(); // generate MSP message
		debug(mspMsg, 2);
		send(mspMsg, client); // send the message

		MSP.reset();
	}

	/**
	 * Ask the client to play music on it's end through MSP<br>
	 * <br>
	 * 
	 * NOTES:<br>
	 * do not call when msp is not enabled.<br>
	 * this is for the special case of the type MUSIC<br>
	 * 
	 * @param musicFile
	 * @param client
	 */
	private void playMusic(final String musicFile, final Client client) {
		MSP.play(musicFile, "music", 25, -1);

		String mspMsg = MSP.generate(); // generate MSP message
		debug(mspMsg, 2);
		send(mspMsg, client); // send the message

		MSP.reset();
	}

	// presumably this would utilize a loot table or similar construct
	private List<Item> generateLoot(Creature creature) {
		// get loot table
		final List<Item> lootTable = getLootTable(creature);

		List<Item> loot = null;
		
		if( !lootTable.isEmpty() ) {
			int itemsToGet = 1; // number of items to return as loot
			
			loot = new ArrayList<Item>(itemsToGet);

			for(int n = 0; n < itemsToGet; n++) {
				// get a random index (1 - 10);
				int index = (int) (Math.random() * itemsToGet);

				// grab an item
				final Item item = lootTable.get(index);

				if( item != null ) {
					// create a copy and add it to the list
					final Item lootItem = item.getCopy();

					//objectDB.addAsNew( lootItem );
					//objectDB.addItem( lootItem );

					loot.add( lootItem );
				}
				// TODO this is a kludge, there should always be a valid item in the loot table
				else {
					n = n - 1;
					continue;
				}
			}
		}

		return loot;
	}

	// dummy function right now, since it should only return a quest if the
	// global quest table exists and has one with that id/index
	private Quest getQuest(final int questId) {
		return quests.get(questId);
	}

	// --------------------------------------------------------------------------------

	/**
	 * getProgInt
	 * 
	 * Gets you a reference to the internal program interpreter
	 * 
	 * @return
	 */
	protected ProgramInterpreter getProgramInterpreter() {
		return this.pgm;
	}

	/**
	 * getDBInterface
	 * 
	 * Gets you limited ("read-only") access to the database, by way of an ODBI
	 * interface reference.
	 * 
	 * TODO public?
	 * 
	 * @return
	 */
	public ODBI getDBInterface() {
		return this.objectDB;
	}

	// --------------------------------------------------------------------------------

	/**
	 * Loads an instance of the specified GameModule implementation. (fully
	 * qualified class name)
	 * 
	 * @param fileName
	 * @return
	 */
	private GameModule loadGameModule(final String fileName) {
		Class c = null;

		GameModule module = null;

		try {
			c = Class.forName(fileName);

			module = (GameModule) c.getConstructor().newInstance();
		}
		catch (final ClassNotFoundException cnfe)   { debug( cnfe ); }
		catch (final InstantiationException inse)   { debug( inse ); }
		catch (final IllegalAccessException iae)    {  debug( iae ); }
		catch (final IllegalArgumentException iae2) { debug( iae2 ); }
		catch (final InvocationTargetException ite) { debug( ite );  }
		catch (final NoSuchMethodException nsme)    { debug( nsme ); }
		catch (final SecurityException se)          { debug( se );   }

		return module;
	}

	public List<Portal> getPortals() {
		return this.portals;
	}

	/* death event handlers */

	public void handleDeath(final Player player) {
		// Player/NPC Death
		if ( player.getState() == Player.State.DEAD ) {
			final TimerTask tt = new TimerTask() {
				@Override public void run() {
					respawn(player, player.getLocation());
				}
			};

			int respawn_time = 5;

			timer.schedule(tt, respawn_time * 60000);
			
			send("You have died! (5 minutes til respawn)", player.getClient());
			
			//
			final Room room = getRoom( player.getLocation() );
			
			// remove stuff from player (they retain unique stuff?)
			List<Item> loss = new ArrayList<Item>( player.getInventory() );
			loss.removeIf( itemIsUnique() );
			room.addItems( loss );
			
			player.getInventory().removeIf( itemIsNotUnique() );
			
			// send the player to the void
			player.setLocation(Constants.VOID);

			//
			//Thing corpse = new Thing("Corpse");

			// does corpse need to be a container
			//corpse.contents = new ArrayList<Item>();

			//corpse.contents.addAll( player.getInventory() );

			//objectDB.addAsNew(corpse);
			//objectDB.addThing(corpse);

			//room.addThing(corpse);

			//Ghost ghost = new Ghost( player ); // ghosts?

			// TODO want to disable some regular player actions, but this flat out removes players from the game...
			//players.remove(player);
		}
	}
	
	// C killed by P
	public void handleDeath(final Creature creature, final Player player) {
		if (creature == null) {
			send("Error!", player.getClient());
			return;
		}

		final Room room = getRoom( creature.getLocation() );
		
		if (creature.getHP() <= 0) {
			debug("Creature: \"" + creature.getName() + "\" Location: " + room.getName() + " (#" + room.getDBRef() + ")");

			// remove/destroy creature ?
			creature.setLocation(-1);
			//destroy(creature);
			
			final List<Item> treasure = generateLoot(creature);
			
			room.addItems( treasure );

			final String cName = creature.getName();
			final TypeFlag cType = creature.getType();

			// check my quests, and then pass an QuestUpdate if I advanced it
			final Quest active_quest = player.getActiveQuest();

			if (active_quest != null) {
				debug("Active Quest: " + active_quest.getName());

				debug(active_quest.getLocation().getName());
				debug(getZone(player).getName());

				if (active_quest.getLocation() == getZone(player)) {
					debug("Player is in the quest's Zone");

					// check each task
					for (final Task task : active_quest.getTasks(true)) { // get incomplete tasks
						if ( task.isType(TaskType.KILL) ) {
							final KillTask kt = (KillTask) task;
							
							debug("Task: " + kt.getName());

							// is the thing we killed the thing this quest asks us to
							final Data objectiveData = task.getObjective();

							MUDObject objective = null;

							if (objectiveData != null) {
								objective = (MUDObject) objectiveData.getObject("target");
							}
							else {
								send("Game> Error? Objective Data is NULL.");
								break;
							}

							if (objective != null) {
								if (objective.isType(TypeFlag.CREATURE)) {
									final Creature c = (Creature) objective;

									if (c == creature || c.getRace() == creature.getRace() || c.getName() == creature.getName()) {
										kt.kills++;
									}
								}
								else if (objective.isType(TypeFlag.PLAYER)) {
								}

								task.update(null); // dummy update

								if ( task.isComplete() ) {
									// send("You completed the task ( " + task.getDescription() + " )" , player.getClient());
									final String taskDesc = colors(task.getDescription(), "green");
									send("You completed the task " + taskDesc, player.getClient());
								}
								else send("task not done yet.", player.getClient());

								//

								final QuestUpdate qu = new QuestUpdate(active_quest.getId());
								final TaskUpdate tu = new TaskUpdate(task.getId());

								qu.taskUpdates.add(tu);

								active_quest.update(qu);

								if (active_quest.isComplete()) {
									// send("You completed the quest ("+
									// active_quest.getName() + ")",
									// player.getClient());
									final String questName = colors(active_quest.getName(), getDisplayColor("quest"));
									send("You completed the quest " + questName, player.getClient());
								}
								else send("Quest not completed!", player.getClient());

								break;
							}
							else send("Task> Invalid Target!", player.getClient());
						}
					}
				}
			}
			
			// TODO this next part should basically resolve the quest where we advance an inactive quest (should we make it active at that time?)

			// for each of the player's quests
			for (Quest quest1 : player.getQuests()) {
				// SKIP active quest
				if( quest1 == player.getActiveQuest() ) continue;
				
				// if we're in the right zone
				if (quest1.getLocation() == getZone(player)) {
					// check each task
					for (Task task : quest1.getTasks(true)) { // get incomplete tasks
						if (task.isType(TaskType.KILL)) {
							final KillTask kt = (KillTask) task;

							// is the thing we killed the thing this quest asks us to
							final Data objectiveData = task.getObjective();
							final MUDObject objective = (MUDObject) objectiveData.getObject("target");

							if (objective != null) {
								if (objective.isType(TypeFlag.CREATURE)) {
									final Creature c = (Creature) objective;

									if (c == creature || c.getRace() == creature.getRace() || c.getName() == creature.getName()) {
										kt.kills++;
										task.update(null); // dummy update

										if (task.isComplete()) {
											send("You completed the task ( " + task.getDescription() + " )", player.getClient());
										}

										break;
									}
								}
								else if (objective.isType(TypeFlag.PLAYER)) {
								}
							}
						}
					}
				}
			}
		}
	}

	/* Auctions */

	/**
	 * Creates a new auction and a timer for it, then schedules the timer for a
	 * 1 second tick and returns the auction object.
	 * 
	 * @param item
	 * @param price
	 * @return
	 */
	private Auction createAuction(Player seller, Item item, Coins price) {
		return createAuction(seller, item, price, -1);
	}
	
	private Auction createAuction(Player seller, Item item, Coins price, int duration) {
		return new Auction(seller, item, price, duration);
	}

	public Auction getAuction(int auctionId) {
		// TODO actually implement this, code below is placeholder
		// NOTE: auctions don't actually have ids at the moment.
		/*
		 * for(Auction auction : this.auctions) { if( auction.getItem() == item
		 * ) { return auction; } }
		 */

		return null;
	}
	
	public Auction getAuction(final Player player, final String itemName) {
		final List<Auction> auctions = getAuctions(player);
		
		for (final Auction auction : auctions) {
			final Item auctionitem = auction.getItem();
			
			if ( auctionitem.getName().equals(itemName) ) {
				return auction;
			}
		}

		return null;
	}

	public List<Auction> getAuctions(final Player player) {
		List<Auction> auctions = new LinkedList<Auction>();

		for (final Auction auction : this.auctions) {
			if (auction.getSeller() == player) {
				auctions.add(auction);
			}
		}

		return auctions;
	}

	public List<Auction> getAuctions(final String itemName) {
		List<Auction> auctions = new LinkedList<Auction>();

		for (final Auction auction : this.auctions) {
			if (auction.getItem().getName().equals(itemName)) {
				auctions.add( auction );
			}
		}

		return auctions;
	}
	
	public List<Auction> getAuctions(final ItemType itemType) {
		List<Auction> auctions = new LinkedList<Auction>();

		for (final Auction auction : this.auctions) {
			if ( auction.getItem().getItemType() == itemType ) {
				auctions.add( auction );
			}
		}

		return auctions;
	}

	/* Party */

	/**
	 * Get the party that the specified player is in, if they are in a party.
	 * 
	 * @param player
	 * @return
	 */
	private Party getPartyContainingPlayer(final Player player) {
		for (Party party : parties) {
			if (party.hasPlayer(player)) {
				return party;
			}
		}

		return null;
	}

	/* Guest Players */

	/**
	 * Get the next pre-existing guest player for the purpose of guest logins
	 * 
	 * @return
	 */
	private Player getNextGuest() {
		Player temp;

		for (int i = 0; i < guests; i++) {
			// NOTE: use DB because the next guest obviously won't be logged in
			temp = objectDB.getPlayer("Guest" + i);

			// just double check that this is a valid guest player and not
			// currently in use
			if (temp != null && !sclients.values().contains(temp)) {
				return temp;
			}
		}

		return null;
	}

	/* CHARacter GENeration (CHARGEN) - Utility Functions*/

	/**
	 * Determine if the specified Player has a valid race set. Checks to see if
	 * you have a race other than Races.NONE and which is not a restricted race.
	 * Also checks for null (which would be equally invalid).
	 * 
	 * @param player
	 * @return
	 */
	private static boolean hasValidRace(Player player) {
		final Race race = player.getRace();

		boolean valid = false;

		if( race != null && race != Races.NONE && !race.isRestricted() ) {
			valid = true;
		}

		return valid;
	}

	/**
	 * Determine if the specified Player has a valid class set. Checks to see if
	 * you have a class other than Classes.NONE and which is not an NPC class.
	 * Also checks for null (which would be equally invalid).
	 * 
	 * @param player
	 * @return
	 */
	private static boolean hasValidClass(final Player player) {
		final PClass pcl = player.getPClass();

		boolean valid = false;

		if (pcl != null && pcl != Classes.NONE && !pcl.isNPC()) {
			valid = true;
		}
		
		return valid;
	}

	private void reset_character(final Player player) {
		// Reset ability scores (default is 0)
		final Ability[] abilities = rules.getAbilities();

		for (int index = 0; index < abilities.length; index++) {
			player.setAbility(abilities[index], 0);
		}

		// Reset skills (default is -1?)
		for (final Skill skill : player.getSkills().keySet()) {
			player.setSkill(skill, -1);
		}
	}

	private void generate_character(final Player player) {
		final Client c = player.getClient();
		
		c.writeln("Generating player stats, etc...");

		player.getClient().writeln("");

		// TODO this state should come from somewhere else
		int temp = Constants.ASSIGN;

		// calculate hp, etc using stats
		/*
		 * Ability[] ab = new Ability[] { Abilities.STRENGTH,
		 * Abilities.DEXTERITY, Abilities.CONSTITUTION, Abilities.INTELLIGENCE,
		 * Abilities.WISDOM, Abilities.CHARISMA };
		 */

		Ability[] ab = rules.getAbilities();

		int ability_score;
		int index;

		switch (temp) {
		case Constants.ROLL:
			index = 0;

			// roll for abilities
			for (final Ability ability : ab) {
				ability_score = 0;

				// roll 3d6
				while (ability_score < 8) ability_score = Utils.roll(3, 6);

				// add racial ability modifiers
				ability_score += player.getRace().getStatAdjust()[index];

				// set ability score
				player.setAbility(ab[index], ability_score);

				send(Utils.padRight(ability.getName() + ":", ' ', 15) + ability_score + " (" + player.getRace().getStatAdjust()[index] + ")", player.getClient());

				index++;
			}
			break;
		case Constants.ASSIGN:
			index = 0;

			for (final Ability ability : ab) {
				// 5 is used as a placeholder for base value
				ability_score = 5;

				// add racial ability modifiers
				ability_score += player.getRace().getStatAdjust()[index];

				// set ability score
				player.setAbility(ability, ability_score);

				send(Utils.padRight(ability.getName() + ":", ' ', 15) + ability_score + " (" + player.getRace().getStatAdjust()[index] + ")", player.getClient());

				index++;
			}

			break;
		default:
			break;
		}

		c.writeln("");

		c.writeln("Done.");
	}

	/*
	 * int value(Item item) { if( canUse( null, item ) ) { // check "can use" //
	 * determine value in copper/weight if( getSkill(Skills.KNOWLEDGE) ) { //
	 * really this should be some kind of trading knowledge int copperValue =
	 * item.getCost().numOfCopper(); int weight = (int) Math.round(
	 * item.getWeight() ); return copperValue / weight; }
	 * 
	 * return 1; }
	 * 
	 * return 0; }
	 */

	/**
	 * Determine if a player can use an item. E.g. if you are trying to use a
	 * wand, do you have sufficient skill in USE_MAGIC_DEVICE or are you a magic
	 * using class. If neither is true, the wand might be useless.
	 * 
	 * @param player
	 * @param item
	 * @return
	 */
	boolean canUse(final Player player, final Item item) {
		return false;
	}

	/**
	 * Determine if a player has a particular feat. This is necessary for
	 * certain kinds of checks. For example, if you are selecting new feats and
	 * the one you wish to choose has prequisite feats we need to be able to
	 * check. Also, in case of such feats as proficiencies for armor and weapons
	 * we must be able to tell if you are proficient and if not to assign the
	 * negatives for attempting to use it anyway.
	 * 
	 * @param player
	 * @param featName
	 * @return
	 */
	public boolean hasFeat(final Player player, final String featName) {
		return false;
	}
	
	/**
	 * Colors
	 * 
	 * Takes a string and a color and wraps the string in the appropriate coding
	 * sequences for the color specified and white (to reset back to default).
	 * 
	 * @param arg
	 * @param colorName
	 * @return
	 */
	public String colors(final String arg, final String colorName) {
		return colorCode(colorName) + arg + colorCode("white");
	}

	public String colorCode(final String colorName) {
		if (color == Constants.ANSI)       return ANSI.getColor(colorName);
		else if (color == Constants.XTERM) return XTERM256.getColor(colorName);
		else                               return "";
	}

	/**
	 * Set colors for displaying the names of MUDObject when the game shows
	 * object names (and the color output is enabled).
	 * 
	 * NOTE: this is a shorter form for when you want to set an ANSI color, but
	 * not an xterm256 color
	 * 
	 * @param displayType
	 * @param ANSIColor
	 */
	public void setDisplayColor(final String displayType, final String ANSIColor) {
		this.displayColors.put(displayType, new Pair<String>(ANSIColor, ""));
	}

	/**
	 * Set colors for displaying the names of MUDObjects when the game shows
	 * object names (and color output is enabled).
	 * 
	 * @param displayType
	 * @param ANSIColor   a named ansi color
	 * @param XTERMColor  a named xterm256 color
	 */
	private void setDisplayColor(final String displayType, final String ANSIColor, final String XTERMColor) {
		if (this.displayColors.containsKey(displayType)) {
			final Pair<String> colors = this.displayColors.get(displayType);

			if (ANSIColor == null)       this.displayColors.put(displayType, new Pair<String>(colors.one, XTERMColor));
			else if (XTERMColor == null) this.displayColors.put(displayType, new Pair<String>(ANSIColor, colors.two));
			else                         this.displayColors.put(displayType, new Pair<String>(ANSIColor, XTERMColor));
		}
		else this.displayColors.put(displayType, new Pair<String>(ANSIColor, XTERMColor));
	}
	
	private String getDisplayColor(final TypeFlag type) {
		// TODO name() or toString()
		return getDisplayColor( MudUtils.getTypeName(type).toLowerCase() );
	}
	
	/**
	 * Get color for the displaying the name of a MUDObject of the type
	 * specified (when color output is enabled).
	 * 
	 * @param displayType
	 * @return
	 */
	private String getDisplayColor(final String displayType) {
		final Pair<String> colors = this.displayColors.get(displayType);

		if (colors != null) {
			if (color == Constants.ANSI)       return colors.one;
			else if (color == Constants.XTERM) return colors.two;
			else                               return "white";
		}
		else return "white";
	}
	
	/**
	 * Get the zone whose ID (integer) is specified, if it exists.
	 * 
	 * @param zoneId
	 * @return
	 */
	protected Zone getZone(final Integer zoneId) {
		Zone z = null;
		
		for (final Zone zone : this.zones.keySet()) {
			if (zone.getId() == zoneId) {
				debug("Zone ID: " + zone.getId());
				z = zone;
				break;
			}
		}

		return z;
	}
	
	/**
	 * Get the zone whose name is specified, if it exists.
	 * 
	 * @param zoneName
	 * @return
	 */
	private Zone getZone(final String zoneName) {
		Zone z = null;
		
		for (final Zone zone : this.zones.keySet()) {
			if ( zone.getName().equalsIgnoreCase(zoneName) ) {
				debug("Zone ID: " + zone.getId());
				z = zone;
				break;
			}
		}

		return z;
	}
	
	/**
	 * Get the zone in which an object is located.
	 * 
	 * @param obj
	 * @return
	 */
	private Zone getZone(final MUDObject obj) {
		Zone zone = null;

		if (obj != null) {
			Room room = null;

			if ( obj.isType(TypeFlag.ROOM) ) room = (Room) obj;
			else                             room = getRoom(obj.getLocation());

			if (room != null) {
				zone = room.getZone();
				debug("Zone ID: " + zone.getId());
			}
		}

		return zone;
	}

	private void sendMail(final Player sender, final Player recipient, final String subject, final String message) {
		if( recipient != null ) {
			final MailBox mb = recipient.getMailBox();

			// also kludged for System messages
			if (sender == null) {
				Mail mail = new Mail(mb.numMessages() + 1, "System", recipient.getName(), subject, message, getDate().toString(), Mail.UNREAD);
				mb.add(mail);
			}
			else {
				Mail mail = new Mail(mb.numMessages() + 1, sender.getName(), recipient.getName(), subject, message, getDate().toString(), Mail.UNREAD);
				mb.add(mail);
			}

			if (recipient.getConfigOption("notify_newmail")) {
				notify(recipient, Messages.NEW_MAIL);
			}
		}
	}

	private void sendMail(final Mail mail, final Player recipient) {
		if (recipient != null) {
			final MailBox mb = recipient.getMailBox();
			
			// TODO fix this kludge?
			mail.setId(mb.numMessages() + 1);
			
			mb.add(mail);

			if (recipient.getConfigOption("notify_newmail")) {
				notify(recipient, Messages.NEW_MAIL);
			}
		}
	}

	protected Map<String, String> getAliases() {
		return this.aliases;
	}

	/**
	 * Takes in a format string and replaces certain markers with game data
	 * 
	 * %r - the current room %s - space, fills the remaining space up to the
	 * line_limit with some character %z - the zone the current room belongs to
	 * 
	 * @param formatString
	 * @param lineLimit TODO
	 * @param client
	 * @return
	 */
	private final String getHeader(final String formatString, final Integer lineLimit, final Room room) {
		final StringBuilder sb = new StringBuilder();

		sb.append(formatString);

		String temp = sb.toString();

		// final Zone z = getZone( room );
		final Zone zone = room.getZone();
		
		final Zone parent = (zone != null) ? zone.getParent() : null;

		temp = temp.replace("%r", room.getName());
		
		//temp = (zone != null) ? temp.replace("%z", zone.getName()) : temp.replace("%z", "???");
		//temp = (zone != null) ? (parent != null) ? temp.replace("%p", parent.getName()) : temp.replace("%p", "???") : temp;
		
		if( zone != null ) {
			temp = temp.replace("%z", zone.getName());
			temp = (parent != null) ? temp.replace("%p", parent.getName()) : temp.replace("%p", "???");
		}
		else {
			temp = temp.replace("%z", "???");
			temp = temp.replace("%p", "null");
		}

		// temp = (z != null) ? (z.getParent() != null) ? temp.replace("%z",
		// z.getName() + ", " + z.getParent().getName()) : temp.replace("%z",
		// z.getName()) : temp.replace("%z", "???");

		temp = temp.replace("%s", Utils.padRight("", '-', (lineLimit - (temp.length() - 2))));
		
		// TODO Deal with this. This code will hang utterly if ? and other wildcards are allowed in room names..
		temp = temp.replaceFirst(room.getName(), colors(room.getName(), getDisplayColor("room")));
		
		temp = (zone != null) ? temp.replaceFirst(zone.getName(), colors(zone.getName(), "magenta")) : temp;
		
		temp = (parent != null) ? temp.replaceFirst(parent.getName(), colors(parent.getName(), "cyan")) : temp;

		return temp;
	}

	/**
	 * Takes in a format string and replaces certain markers with game data
	 * 
	 * %s - space, fills the remaining space up to the line_limit with some
	 * character
	 * %S - current status information
	 * %T - current time
	 * %D - current date
	 * 
	 * @param formatString
	 * @param lineLimit TODO
	 * @return
	 */
	private final String getFooter(final String formatString, final Integer lineLimit) {
		final StringBuilder sb = new StringBuilder();

		sb.append(formatString);

		String temp = sb.toString();

		String current_status = "H: 0, types:";
		String current_time = game_time.getHours() + ":" + game_time.getMinutes();
		String current_date = day + " " + MONTH_NAMES[month - 1] + ", " + year + " " + reckoning;

		temp = temp.replace("%S", current_status);
		temp = temp.replace("%T", current_time);
		temp = temp.replace("%D", current_date);
		temp = temp.replace("%s", Utils.padRight("", '-', (lineLimit - (temp.length() - 2))));

		return temp;
	}

	/**
	 * Determine if the string specified is a valid name for an exit.
	 * 
	 * alphabetical (lower/upper case), underscore, and dashes permitted.
	 * 
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

		for (final Quest quest : this.quests) {
			if (quest.getLocation() == zone) {
				quests.add(quest);
			}
		}

		return quests;
	}
	
	public Skill getSkill(final String skillName) {
		Skill skill;

		skill = Skills.skillMap.get(skillName);

		if (skill != null) return skill;

		return null;
	}

	public static int getSkillId(final Skill s) {
		return s.getId();
	}

	private void loadZones(final String filename) {
		final int ID = 0; // id number for the zone
		final int NAME = 1; // name of the zone on it's own line
		final int PARENT = 2; // reference to a parent zone, on it's own line
		final int ERROR = 3; // error state, a third line that isn't "~"

		int step = 0;

		// input data storage
		int id = -1;
		String name = null;
		Zone parent = null;

		Zone temp = null; // temporary reference for the zone objects we're creating.

		final String[] data = Utils.loadStrings(filename); // file data

		// for each line in the file.
		for (final String str : data) {
			debug("LINE: " + Utils.padRight(str, ' ', 40) + "(STEP: " + step + ")");

			if (str.charAt(0) == '#') {
				continue;
			}
			else if (str.charAt(0) == '~') {
				temp = new Zone(name, parent);
				temp.setId(id);
				zones.put(temp, 0);

				debug("New Zone");
				debug(temp.getId() + " = " + temp.getName());

				id = -1;
				name = null;
				parent = null;

				temp = null;

				step = ID;
				continue;
			} 
			else {
				switch (step) {
				case ID:
					id = Utils.toInt(str, -1);
					step = NAME;
					break;
				case NAME:
					name = str;
					setNameRef(name.toLowerCase(), id);
					debug("$" + name.toLowerCase() + " -> " + getNameRef(name.toLowerCase()));
					step = PARENT;
					break;
				case PARENT:
					if (!str.equals("$NULL")) {
						parent = getZone(getNameRef(str.substring(1)));
					}
					step = ERROR;
					break;
				case ERROR:
					System.out.println("ERROR! Zone Loading aborted!");
					return;
				default:
					break;
				}
			}
		}


		// System.out.println( getNameReferences() );
		// System.out.println( zones.keySet() );
	}
	
	/* Client State Methods */

	// TODO should client state ever be non-existent or should there always be a
	// client state?
	// TODO consider adding client state constants
	/**
	 * setClientState
	 * 
	 * kludges for modifying client state
	 * 
	 * note: the empty string is a stand in for a neutral/null/empty state
	 * 
	 * @param client
	 * @param newState
	 */
	public void setClientState(final Client client, final String newState) {
		if ("".equals(newState)) clientState.remove(client);
		else                     clientState.put(client, newState);
	}

	public String getClientState(final Client client) {
		return clientState.get(client);
	}

	public void setClientData(final Client client, final ClientData newData) {
		if ( newData == null )  clientData.remove(client);
		else                    clientData.put(client, newData);
	}

	public ClientData getClientData(final Client client) {
		return clientData.get(client);
	}
	
	public LoginData getLoginData(final Client client) {
		return this.loginData.get(client);
	}
	
	public void setLoginData(final Client client, final LoginData newData) {
		if (!(newData == null)) loginData.put(client, newData);
		else                    loginData.remove(client);
	}

	/**
	 * Move the specified Mobile to the Point specified
	 * 
	 * @param mob
	 * @param destination
	 */
	public void move(final Mobile mob, final Point destination) {
		mob.setMoving(true);
		mob.setDestination(destination);

		synchronized (this.moving) {
			this.moving.add(mob);
		}
	}
	
	// TODO possibly remove the newline echo as 'send' always send a newline and carriage return
	
	/**
	 * Output the specified text to the client only if we are supposed to echo.
	 * 
	 * @param string
	 * @param client
	 */
	public void echo(final String string, boolean newline, final Client client) {
		if( echo_enabled || getPlayer(client).getConfigOption("server_echo") ) { 
			if( newline ) client.writeln(string);
			else          client.write(string);
		}
	}
	
	/**
	 * echo
	 * 
	 * A nice wrapper function that will send the provided the string to the client 
	 * if echoing is enabled. Primarily intended to provide user input echoing from the
	 * server side.
	 * 
	 * 
	 * 
	 * @param input
	 * @param client
	 */
	private void echo(final String input, final Client client) {
		if( echo_enabled ) {
			send(input, client);
		}
	}

	/**
	 * Load Protoypes from a file
	 * 
	 * NOT USED!
	 */
	public void loadProtoypes(final String fileName) {
		File file;
	}
	
	/**
	 * Get input from the player (asynchronously?)
	 * 
	 * mechanically speaking, this merely asks the client to hold the next line sent as response to the request
	 * 
	 * NOTE: you may only have one request at a time
	 * 
	 * @param string
	 * @param client
	 */
	public void requestInput(final String string, final Client client) {
		client.write(string);
		client.setResponseExpected(true);
	}
	
	// TODO rework response requests...
	
	/**
	 * Get input from the player (synchronously). Use request input, but
	 * then locks into it's own loop until it gets a response.
	 * 
	 * TODO this approach should be trashed...
	 * 
	 * @param string
	 * @param client
	 * @return
	 */
	public String getInput(final String string, final Client client) {
		requestInput(string, client);

		String response = client.getResponse();

		do {
			response = client.getResponse();
			System.out.println(response);
		}
		while (response == null);

		return response;
	}
	
	/**
	 * notify
	 * 
	 * To be used for notifying the player of something, potentially
	 * in an immediate way.
	 * 
	 * @param player
	 * @param message
	 */
	public void notify(final Player player, final String message) {
		if (player != null) {
			// under what circumstances, if any, should I hold or delay notifications?
			// e.g. editing, combat, etc
			// (esp. where we don't want to see if we received mail)
			
			if (notify_immediate) send(message, player.getClient());
			else                  this.messageQueues.get(player).push(message);
		}
	}

	// TODO riding related
	/*private void mount(final Ridable r, final Player p) {
		p.mount = r;
	}
	
	private void unmount(final Ridable r, final Player p) {
		p.mount = null;
	}*/
	
	// intended for the use of Console instances
	final public List<Client> getClients() {
		return Collections.unmodifiableList(s.getClients());
	}

	/**
	 * Load Race data from a json file and store in a "global" variable.
	 * 
	 * Uses Google-Gson
	 */
	public void loadRaces() {
		com.google.gson.GsonBuilder gb = new com.google.gson.GsonBuilder();

		gb.registerTypeAdapter(Race.class, new mud.misc.json.RaceAdapter());

		com.google.gson.Gson gson = gb.create();
		
		// TODO fix path kludges (this is just like the zones problem)
		//String path = WORLD_DIR + world + "\\";
		String races_file = resolvePath(WORLD_DIR, world, "races.json");
		
		//debug("races: " + path + "races.json");
		//debug("");
		
		debug("races: " + races_file);
		debug("");

		try {
			//String[] temp = Utils.loadStrings(path + "races.json");
			String[] temp = Utils.loadStrings(races_file);
			String json = Utils.join(temp, " ");

			Race[] racesArr = gson.fromJson(json, Race[].class);

			races.addAll(Arrays.asList(racesArr));
		}
		catch (final Exception e) {
			// TODO what exception are we catching?
			debug( e );
		}

		for (final Race r : races) {
			debug("* Race");
			debug("Name: " + r.getName());
			debug("Id: " + r.getId());
			debug("Stat Adjust: " + Arrays.asList(r.getStatAdjust()));
			debug("Restricted?: " + r.isRestricted());
			debug("Can Fly?: " + r.canFly());
			debug("");
		}

		System.out.println("Races: " + races);
	}

	public List<Creature> getCreaturesByRoom(final Room room) {
		return objectDB.getCreaturesByRoom( room );
	}

	public Race getRace(String raceName) {
		Race race = null;
		
		for (final Race r : races) {
			if( r.getName().equalsIgnoreCase(raceName) ) {
				race = r;
				break;
			}
		}

		return race;
	}

	public Race getRace(int raceId) {
		Race race = null;

		for (final Race r : races) {
			if (r.getId() == raceId) {
				race = r;
				break;
			}
		}

		return race;
	}

	/**
	 * Get the Command object from the command map that corresponds to the given
	 * string. Also check any aliases that are defined.
	 * 
	 * @param command
	 * @return
	 */
	private Command getCommand(final String command) {
		if (commandMap.containsKey(command)) {
			return commandMap.get(command);
		}
		else {
			if (aliases.containsKey(command)) {
				return commandMap.get(aliases.get(command));
			}
			else {
				final Command empty = new Command("") {
					@Override
					public void execute(String arg, Client client) {
						send(command + ": No such COMMAND", client);
					}};
					
				empty.init(this);
				
				return empty;
			}
		}
	}

	private Command getCommand(final String command, final Player player) {
		Command cmd = null;

		if (player != null) {
			if ( player.hasCommand(command) ) {
				cmd = player.getCommand(command);
			}
		}

		return cmd;
	}
	
	/**
	 * Note: TOTALLY UNUSED???
	 * 
	 * @param moduleName
	 */
	private final void loadModule(final String moduleName) {
		// we're going to classload the specified class and
		// then pull some method references
		ClassLoader classLoader = MUDServer.class.getClassLoader();

		try {
			Class aClass = classLoader.loadClass("mud." + moduleName);

			// if( aClass)
			System.out.println("aClass.getName() = " + aClass.getName());
		}
		catch (final ClassNotFoundException cnfe) {
			debug( cnfe );
		}
	}

	private void start_interactive_login(final Client client) {
		// TODO shouldn't need to set this each time, or maybe I need to split some stuff out
		setClientState(client, "interactive_login");
		setLoginData(client, new LoginData(Constants.USERNAME)); // ???
		
		handle_interactive_login(client);
	}
	
	/**
	 * handle_interactive_login
	 * 
	 * Input handler for interactive login
	 * 
	 * @param client
	 */
	private void handle_interactive_login(final Client client) {
		final LoginData ld = getLoginData(client);
		
		// indicate current state (debug info?)
		System.out.println("Interactive Login, STATE: " + ld.state);
		
		if ( ld.state.equals(Constants.USERNAME) ) {
			requestInput("Name?     ", client);
		}
		else if ( ld.state.equals(Constants.PASSWORD) ) {
			if( ld.username.equalsIgnoreCase("new") ) {
				send("Invalid Name!", client);
				
				ld.state = Constants.USERNAME;
				
				handle_interactive_login(client);
			}
			else {
				requestInput("Password? ", client);
			}
		}
		else if ( ld.state.equals(Constants.LOGIN) ) {
			// try connecting
			cmd_connect(ld.username + " " + ld.password, client);

			// TODO: FIX NEEDED, clearing client state here breaks account logins
			// TODO figure out what to do, it seems silly to try a login and then test if it worked
			
			// assume a successful connection, then check to see if it wasn't
			boolean conn_success = true;
			
			// after attempting a connection
			if (use_accounts) {
				if (!caTable.containsKey(client)) {
					// no such account
					send("No such player or account.", client);
					
					conn_success = false;
				}
			}
			else {
				if (getPlayer(client) == null) {
					// no such player
					send("No such player.", client);
					
					conn_success = false;
				}
			}
			
			if( conn_success ) {
				// we successfully logged in
				if( use_accounts ) setClientState(client, "account_menu");
				else               setClientState(client, "");             // clear state
				
				setLoginData(client, null); // clear login data
			}
			else {
				// start over and try again
				ld.state = "NAME";
				handle_interactive_login(client);
			}
		}
	}

	/**
	 * Handle the steps in dropping an item.
	 * 
	 * @param player
	 * @param room
	 * @param item
	 */
	private void drop(final Player player, final Room room, final Item item) {
		boolean validRoom = (room != null);
		boolean sameLocation = (player.getLocation() == room.getDBRef());
		boolean itemPresent = (player.getInventory().contains(item));
		
		if ( validRoom && sameLocation && itemPresent ) {
			player.getInventory().remove(item);
			
			item.setLocation(room.getDBRef());
			item.setPosition(player.getPosition());
			
			room.addItem(item);
		}
	}

	/**
	 * Handle the steps in taking an item.
	 * 
	 * @param player
	 * @param room
	 * @param item
	 */
	private void take(final Player player, final Room room, final Item item) {
		boolean validRoom = (room != null);
		boolean sameLocation = (player.getLocation() == room.getDBRef());
		boolean itemPresent = (room.getItems().contains(item));
		
		if (validRoom && sameLocation && itemPresent) {
			final String itemName = item.getName();
			
			// will need to test for a standard location to put it
			// see if there is a generic storage container to put it in

			if (hasGenericStorageContainer(player, item)) {
				// if you have a container for this item type, put it there

				/*
				 * debug(item.getName() + " container"); Container<Item> c =
				 * getGenericStorageContainer( player, item );
				 * item.setLocation(c.getDBRef()); c.add( item );
				 * send("You picked " + colors(item.getName(), "yellow") +
				 * " up off the floor and put it in " + c.getName(), client);
				 */
			}
			else {
				// else just stick it in inventory
				debug(itemName + " inventory");

				int old_loc = item.getLocation();

				final String stackable = item.getProperty("stackable");
				
				boolean stacks = ( stackable.equals("true") ) ? true : false;
				
				// if there is an existing, not full stack of that item trying
				// to add these to it
				if (item instanceof Stackable || stacks) {
					debug(itemName + " stackable");
					
					List<Item> item_stacks = getStackableItems(itemName, player);

					boolean foundItemStack = false;
					
					debug("found stack: " + foundItemStack);

					for (final Item item1 : item_stacks) {
						Stackable<Item> item_stack = (Stackable<Item>) item1;

						if (item1.getItemType() == item.getItemType()) {
							if (item_stack.stackSize() < Constants.MAX_STACK_SIZE) {
								debug("stackable - added to existing stack");
								item_stack.stack(item);
								foundItemStack = true;
								break;
							}
						}

						debug("" + player.getInventory().contains(item));
					}

					// if we didn't find an existing stack of these, just take
					// the item
					if (!foundItemStack) {
						debug("stackable - new stack");

						player.getInventory().add(item);
						item.setLocation(player.getDBRef()); // "move" item
						
						debug("added item " + player.getInventory().contains(item));
					}
				}
				else {
					debug(item.getName() + " not stackable");

					player.getInventory().add(item);
					item.setLocation(player.getDBRef()); // "move" item
					
					debug("added item " + player.getInventory().contains(item));
				}

				debug("old " + old_loc); // old location
				debug("new " + item.getLocation()); // new location

				send("You picked " + colors(itemName, "yellow") + " up off the floor.", player.getClient());
			}

			// remove from the room
			room.removeItem(item);

			// check for silent flag to see if object's dbref name should be shown as well?
			// return message telling the player that they picked up the object
			// return message telling others that the player picked up the item
			// needs to be placed in the message queue for just the room
			// somehow, not sent to the current player
		}
		else debug( String.format("Valid Room? %s -- Same Location? %s -- itemPresent? %s", validRoom, sameLocation, itemPresent) );
	}
	
	/**
	 * Handle the steps involved in getting an item that is inside
	 * another item.
	 * 
	 * NOTE: this should, hopefully, be called only on items that the players has or is near to,
	 * although it could be used more generally for some kind of aether based storage/retrieval
	 * since it cares only about the player, some kind of storage that holds items and a named item.
	 * 
	 * @param player
	 * @param storage
	 * @param itemName
	 * @return
	 */
	private boolean get(final Player player, final Storage<Item> storage, final String itemName) {
		boolean result = false; // were we able to retrieve/get the item
		
		Item item = null;

		if (storage != null) item = storage.retrieve(itemName);

		if (item != null) {
			debug("Container: " + ((MUDObject) storage).getName());
			debug("Item:      " + item.getName());

			player.getInventory().add(item);

			item.setLocation(player.getDBRef());

			debug( player.getInventory().toString() );
			
			// TODO move message out of this function?
			send("You get " + colors(item.getName(), getDisplayColor("item")) + " from " + ((MUDObject) storage).getName(), player.getClient());

			result = true;
		}

		return result;
	}
	
	private boolean put(final Player player, final Storage<Item> storage, final Item item) {
		boolean result = false; // were we able to store the item
		
		if (storage != null && item != null) {
			System.out.println("Container: " + ((MUDObject) storage).getName());
			System.out.println("Item: " + item.getName());

			if( storage.insert(item) ) {
				player.getInventory().remove(item);
				
				item.setLocation( ((MUDObject) storage).getDBRef() );
				
				//System.out.println( container.getContents() );

				send("You put " + colors(item.getName(), "yellow") + " in " + colors(((MUDObject) storage).getName(), "yellow"), player.getClient());
				
				result = true;
			}
		}

		return result;
	}
	
	private void giveMoney(final Player player, final Coins coins) {
		final Coins money = player.getMoney();
		
		//player.setMoney( player.getMoney().add(coins) );
		player.setMoney( money.add(coins) );

		// TODO fix message to adequately represent gained money
		notify(player, "You receive " + coins.toString(true));
	}
	
	private void giveItem(final Player player, final Item item) {
		final List<Item> inventory = player.getInventory();
		
		//player.getInventory().add( item );
		inventory.add( item );
		
		notify(player, "You recieve " + colors(item.getName(), getDisplayColor("item")) + ".");
	}
	
	private void giveSpells(final Player player, final Spell newSpell) {
		if( player.isCaster() ) {
			player.getSpellBook().addSpell(newSpell);
			
			notify(player, newSpell.getName() + "was added to your spellbook.");
		}
	}
	
	private void giveSpells(final Player player, final Spell... newSpells) {
		if (player.isCaster()) {
			for (final Spell s : newSpells) {
				player.getSpellBook().addSpell(s);
				
				notify(player, s.getName() + "was added to your spellbook.");
			}
		}
	}
	
	private void readConfigFile(final String filename) {
		File configFile = null;
		BufferedReader br = null;

		String temp;

		configFile = new File(filename);

		if (configFile != null) {
			try {
				br = new BufferedReader(new FileReader(configFile));
			}
			catch (final FileNotFoundException fnfe) {
				// TODO is this debug message adequate?
				System.out.println("Config File does not exist.");
				
				debug( fnfe );
			}

			if (br == null)
				return;

			try {
				String option = "";
				String value = "";
				int n = 0;

				while (br.ready()) {
					temp = br.readLine();

					if (temp != null) {
						final String[] temp1 = temp.split("=");

						option = temp1[0];

						if (temp1.length == 2) value = temp1[1];

						switch (option) {
						case "debug":
							n = Utils.toInt(value, -1);
							if (n == 1) debug = true;
							break;
						case "logging-enabled":
							if (value.equals("true")) logging = true;
						case "port":
							n = Utils.toInt(value, -1);
							if (n != 1 && (n > 1024 && n < 9999)) {
								port = n;
							}
						case "int-login":
							if (value.equals("true")) int_login = true;
							break;
						default:
							break;
						}
					}
				}

				br.close();
			}
			catch (final IOException ioe) {
				debug( ioe );
			}
		}
	}
	
	// ACCOUNT MENU actions
	// TODO actually implement the code for these
	// TODO these can't really tie up the loop, so they need to do some setup for the rest of the interaction

	private void new_character(final Account account, final Client client) {
		// NOTE: depends on getInput(...) -- 12/4/2015
		
		// TODO below is not used at the moment
		if ( getClientState(client).equals("account_menu_newchar") ) {
			//final ClientData cd = getClientData(client);
		}

		// TODO make sure this tells you about the character limit and ensure
		// that we don't create a character or link it there
		send("Testing -- this code only produces a user with the name 'user' and the password 'pass'.", client);
		send("* this will likely fail if attempted a second time", client);

		/*
		 * String user = "user"; String pass = "pass";
		 */

		// TODO implement a challenge/response bit here to get a name and pasword
		// get a name and password

		String user = "";
		String pass = "";

		user = getInput("Name? ", client);
		pass = getInput("Password? ", client);

		Player player = null;

		// check for existing player by that name, if exists report that the
		// name is already used, if not continue on
		if (!objectDB.hasName(user, TypeFlag.PLAYER) && validateName(user)) {
			// create a new player object for the new player
			player = new Player(-1, user, Utils.hash(pass), start_room);

			// TODO decide if this is okay or find a better way
			// sclients.put(client, player);
			// run character generation (should we do this here?)

			objectDB.addAsNew(player);
			objectDB.addPlayer(player); // add player to the auth table

			/*
			 * send("Welcome to the Game, " + user + ", your password is: " +
			 * pass, client);
			 * 
			 * // initiate the connection init_conn(player, client, true);
			 */
		}
		else {
			// indicate the unavailability and/or unsuitability of the chosen name
			send("That name is not available, please choose another and try again.", client);
		}

		// TODO check to see if this succeeded or not, then make a report accordingly to the client
		if ( account.linkCharacter(player) ) {
		}
		else send("", client);

		// TODO implement a challenge/response bit here to handle character generation
		// drop the new player into chargen cmd_chargen("", client);

		// TODO make a final decision as to whether we ought to just login as the new player or not

		// handle login stuff
		// init_conn(player, client, true);

		send("Created New Character, Select it in your character list to login", client);

		// Account account = caTable.get(client);

		account_menu(account, client);

		// send("Account Action -> New Character (Not Implemented)", client);
		
		// STEPS:
		// get input
		// create character
		// link character
		// send messages to user
		// return to account menu
	}
	
	// selected character, confirm?, password match?
	private void delete_character(final Account account, final Player player) {
	}
	
	private void link_character(final Account account, final Player player) {
		if( account != null ) {
			account.linkCharacter(player);
		}
	}
	
	private void unlink_character(final Account account, final Player player) {
		if( account != null ) {
			account.unlinkCharacter(player);
		}
	}
	
	// TODO finish implementation, would like to use search stuff from above and
	// would like to take criteria, like say whether the item is a container
	// NOTE this is needed still and is used to get a list of items with matching names
	/**
	 * findItems
	 * 
	 * NOTE: currently this just returns all the items that match the name in any way.
	 * PLAN: the idea here is to get back a list of items that match the specified criteria
	 * 
	 * public List<Item> findItems(final List<Item> searchList, final String parameters)
	 * 
	 * @param searchList
	 * @param itemName
	 * @return
	 */
	public List<Item> findItems(final List<Item> searchList, final String itemName) {
		final String arg = itemName;

		final List<Item> results = new LinkedList<Item>();

		if (searchList.size() > 0) {

			for (final Item item : searchList) {
				// test criteria
				// if they pass, add to the results list

				// if there is a name or dbref match from the argument in the
				// inventory
				// if the item name exactly equals the arguments or the name
				// contains the argument (both case-sensitive), or if the dbref
				// is correct

				final String name = item.getName();
				final List<String> components = Arrays.asList(item.getName().toLowerCase().split(" "));

				String name_lc = name.toLowerCase();
				String arg_lc = arg.toLowerCase();

				debug("Argument:              " + arg);
				debug("Name:                  " + name);
				debug("Argument (Lower Case): " + arg_lc);
				debug("Name (Lower Case):     " + name_lc);
				debug("Components:            " + components);

				// 1) is the name the same as ARG (ignoring case -- setting both name and arg to lowercase)
				// 2) does the name start with ARG (ignoring case -- setting both name and arg to lowercase)
				// 3) does the name end with ARG (ignoring case -- setting both name and arg to lowercase)
				// 4) does the name contain ARG (ignoring case -- setting both name and arg to lowercase)
				// 5) is any component of the name the same as the arg (continues non-whitespace separated segments)

				boolean sameName = name.equalsIgnoreCase(arg);
				boolean startsWith = name_lc.startsWith(arg_lc);
				boolean endsWith = name_lc.endsWith(arg_lc);
				boolean nameContains = name_lc.contains(arg_lc);
				boolean compsContain = components.contains(arg_lc);

				boolean test = false;

				for (String s : components) {
					for (String s1 : Arrays.asList(arg.toLowerCase().split(" "))) {
						if (s.contains(s1)) test = true;
						break;
					}
				}

				// for string in A, is A.S a substring of string name N.S
				if (sameName || startsWith || endsWith || nameContains || compsContain || test) {
					debug(itemName + " true");

					results.add(item);
				}
			}
		}

		return results;
	}
	
	/**
	 * Get the current GameModule
	 * 
	 * @return
	 */
	protected GameModule getGameModule() {
		return this.module;
	}
	
	/**
	 * Load a script as a command with the specified name and description. You must also
	 * provide an access permission value and the script itself.
	 * 
	 * @param cmdName
	 * @param cmdDescription
	 * @param access
	 * @param script
	 * @return
	 */
	private boolean loadScriptedCommand(final String cmdName, final String cmdDescription, final Integer access, final Script script) {
		boolean success = false;

		if (!commandMap.containsKey(cmdName)) {
			final ScriptedCommand newCmd = new ScriptedCommand(cmdDescription, pgm, script);

			newCmd.setAccessLevel(access);
			
			addCommand(cmdName, newCmd);

			success = true;
		}
		else {
			debug("Error> A command with that name already exists.");
		}

		return success;
	}
	
	// NOTE: is the following redundant to the immediate part above?
	/*
	public ScriptedCommand createScriptedCommand(final String name, final Script script) {
		return new ScriptedCommand(name, this.pgm, script);
	}*/

	/**
	 * check idle state and increase idle time count if the player is still idle
	 * 
	 * @param player
	 * @param input
	 */
	private void handle_idle_player(final Player player, final String input) {
		final boolean isIdle = player.isIdle();
		
		if (input != null) {
			if (isIdle) {
				player.setIdle(false);
				player.setIdleTime(0);
			}
		}
		
		// should not decide that you are idle here, since one loop without input = idle
		// by that measure
		/*if (input == null) {
			if (!isIdle) {
				player.setIdle(true);
			}
		}
		else {
			if (isIdle) {
				player.setIdle(false);
				player.idle = 0;
			}
		}*/
	}
	
	protected void addAlias(final String command, final String alias) {
		this.aliases.put(alias, command);
	}

	public void addAuctionTimer(final AuctionTimer atimer, final Player player) {
		auctionTimers.get(player).add(atimer);
		timer.scheduleAtFixedRate(atimer, 0, 1000);
	}
	
	/**
	 * General purpose. Would like to be able to use this for every object and pass it to the
	 * right editor based on object type.
	 * 
	 * @param player
	 * @param obj
	 */
	private void editObject(final Player player, final MUDObject obj) {
		final String typeName = obj.getType().toString();
		
		if( typeName.equals( MudUtils.getTypeName(TypeFlag.CREATURE) ) ) {
			editCreature(player, (Creature) obj);
		}
		else if( typeName.equals( MudUtils.getTypeName(TypeFlag.EXIT) ) ) {
			editExit(player, (Exit) obj);
		}
		else if( typeName.equals( MudUtils.getTypeName(TypeFlag.ROOM) ) ) {
			editRoom(player, (Room) obj);
		}
		else {
		}
	}
	
	/**
	 * setup the creature editor for editing the specified room
	 * 
	 * @param player
	 * @param room
	 */
	private void editCreature(final Player player, final Creature creature) {
		final Client client = player.getClient();

		String old_status = player.getStatus();

		player.setStatus(Constants.ST_EDIT); // set the 'edit' status flag
		player.setEditor(Editors.CREATURE); // creature editor

		if (creature != null) {
			if (creature.Edit_Ok) {
				creature.Edit_Ok = false;
			}
			else {
				abortEdit("creature not editable (!Edit_Ok)", old_status, client);
				return;
			}

			final EditorData newEDD = new EditorData();

			// record prior player status
			newEDD.addObject("pstatus", old_status);

			// add creature and it's constituent parts to the editor data
			newEDD.addObject("creature", creature);

			newEDD.addObject("name", creature.getName());
			newEDD.addObject("desc", creature.getDesc());
			newEDD.addObject("flags", EnumSet.copyOf(creature.getFlags()));
			newEDD.addObject("location", creature.getLocation());

			newEDD.addObject("race", creature.getRace());

			player.setEditorData(newEDD);

			op_creatureedit("show", client); // print out the info page
		}
		else {
			send("No Such Creature");
		}
	}
	
	private void editExit(final Player player, final Exit exit) {
	}

	/**
	 * setup the room editor for editing the specified room
	 * 
	 * @param player
	 * @param room
	 */
	private void editRoom(final Player player, final Room room) {
		// NOTE: by definition the player shouldn't be null in here
		final Client client = player.getClient();

		final String old_status = player.getStatus();

		if (room != null) {
			if (room.Edit_Ok) {
				room.Edit_Ok = false; // further edit access not permitted (only one person may access at a time)
			}
			else {
				// room is not editable, exit the editor
				abortEdit("room not editable (!Edit_Ok)", old_status, client);
				return;
			}

			player.setStatus(Constants.ST_EDIT); // set the 'edit' status flag
			player.setEditor(Editors.ROOM); // room editor

			final EditorData newEDD = new EditorData();

			// record prior player status
			newEDD.addObject("pstatus", old_status);

			// add room and it's constituent parts to the editor data
			newEDD.addObject("room", room);

			newEDD.addObject("name", room.getName());
			newEDD.addObject("desc", room.getDesc());
			newEDD.addObject("flags", EnumSet.copyOf(room.getFlags()));
			newEDD.addObject("location", room.getLocation());

			newEDD.addObject("zone", room.getZone());

			newEDD.addObject("x", room.getDimension("x"));
			newEDD.addObject("y", room.getDimension("y"));
			newEDD.addObject("z", room.getDimension("z"));

			// Exits
			for (final Exit exit : room.getExits()) {
				newEDD.addObject("e|" + exit.getName(), exit);
			}

			// Things
			for (final Thing thing : room.getThings()) {
				newEDD.addObject("t|" + thing.getName(), thing);
			}

			// Items
			for (final Item item : room.getItems()) {
				newEDD.addObject("i|" + item.getName(), item);
			}

			player.setEditorData(newEDD);
			
			//new RoomEditor(newEDD, client);

			op_roomedit("show", client); // print out the info page
		}
		else send("No such room!", client);
	}
	
	/**
	 * Is the specified input the given command name or an alias?
	 * 
	 * @param input
	 * @param commandName
	 * @return
	 */
	private boolean cmdIs(final String input, final String commandName) {
		boolean aliasExists = false;
		boolean aliasMatch = false;

		final String alias = this.aliases.get(input);

		if (alias != null) {
			aliasExists = true;

			if (alias.equals(commandName)) {
				aliasMatch = true;
			}
		}

		return (input.equals(commandName) || (aliasExists && aliasMatch));
	}
	
	/**
	 * not sure how great a way of doing this it is, maybe classes implementing Lockable
	 * should have an isKey method so individual instances can decide whether the object passed
	 * to them is their key?
	 * 
	 * @param p
	 * @param l
	 * @return
	 */
	private boolean hasKey(Player p, Lockable<Item> li) {
		boolean hasKey = false;

		if( p != null && li != null ) {
			final Object key = li.getKey();

			if( key != null ) {
				if(key instanceof MUDObject) {
					MUDObject k = (MUDObject) key;

					switch(k.getType()) {
					case ITEM:
						final Item item = (Item) k;

						if( p.hasItem(item) ) hasKey = true;
						break;
					default:
						break;
					}
				}
			}
		}

		return hasKey;
	}
	
	/**
	 * Create a new, temporary player object which we will set the GUEST flag on and which will
	 * not be persisted when the database is saved.
	 * 
	 * @return
	 */
	private Player createGuestPlayer() {
		final String gName = "Guest" + this.guests;
		final String gDesc = "A guest player.";
		
		final EnumSet<ObjectFlag> gFlags = EnumSet.of(ObjectFlag.GUEST);
		
		final String gPass = Utils.hash("password");
		
		return new Player(-1, gName, gFlags, gDesc, start_room, "", gPass, "OOC", new Integer[] { 0, 0, 0, 0, 0, 0 }, Coins.copper(0));
	}
	
	Class<?> getClass(final String className) {
		Class<?> c = null;
		
		try {
			c = Class.forName(className);
		}
		catch (final ClassNotFoundException cnfe) {
			debug( cnfe );
		}

		return c;
	}

	private List<Player> checkForIdlePlayers() {
		final List<Player> idle_players = new LinkedList<Player>();
		
		// increment idle time counter for players
		for (final Player p : players) {
			if ( p.isIdle() ) {
				int idle = p.getIdleTime();
				p.setIdleTime( idle++ );
			}
		}
		
		return idle_players;
	}
	
	private void kickIdlePlayers() {
		//kick();
	}
	
	// Network Server Call Wrappers
	private void disconnect(final Client client) {
		s.disconnect(client);
	}
	
	private void write(final char datum) {
		s.write(datum);
	}
	
	private void write(final String data) {
		s.write(data);
	}
	
	/**
	 * Add a Command object to the command map. This also
	 * ensures that it gets initialized.
	 * 
	 * @param text
	 * @param cmd
	 */
	private void addCommand(final String text, final Command cmd) {
		cmd.init(this);
		this.commandMap.put(text, cmd);
	}

	private void handle_conversation(final NPC npc, final Player player, final Integer option) {
		// we assume that if the player and npc are not null then things are in order...
		if (player != null && npc != null) {
				final Client client = player.getClient(); // needed for output

				boolean continue_conversation = true; // assume the conversation will continue
				
				final CNode cnvs = conversations.get(player).two;   // get the conversation itself
				final CNode node = cnvs.getOptions().get(option-1); // current node (the indicated option)
				
				conversations.get(player).two = node; // move into the specified node
				
				// send player dialog, send npc response
				send(colors(player.getName(), getDisplayColor("player")) + ": " + node.getText(), client);
				send(colors(npc.getName(), getDisplayColor("npc")) + ": " + node.getResponse(), client);
				send("", client);
				
				debug("* CNode");
				debug("ID: " + node.getId());
				debug("Text: " + node.getText());
				debug("Response: " + node.getResponse());
				debug("Options: ");
				debug("Ends: " + node.ends);
				
				// do some kind of check on the selected option (-1 is an invalid option?)
				if( node.ends ) continue_conversation = false;
				
				// if the conversation continues, list the current options
				if( continue_conversation ) {
					send("-- Conversation (" + npc.getName() + ")", client);
					
					final List<String> convOpts = new LinkedList<String>();
					
					int n = 1;
					
					for(final CNode cn : node.getOptions()) {
						if( cn.ends ) convOpts.add(n + " ) " + colors(cn.getText() + " (Ends Conversation)", "green") );
						else          convOpts.add(n + " ) " + colors(cn.getText(), "green") );
						
						n++;
					}
					
					send(convOpts, client);
					
					send("", client);
				}
				else {
					player.setStatus("IC"); // reset status: CNVS -> IC
					
					look(getRoom(player.getLocation()), client);
					
					if( node.getScript() != null ) {
						pgm.interpret(node.getScript(), player, npc);
					}
				}
		}
	}
	
	public void changeMode(final GameMode newMode) {
		this.mode = newMode;
		
		debug("Mode Changed: " + mode.toString());
	}
	
	// expects .dlg files... do we assume that the filename contains the ending?
	private CNode loadDialog(final String filename) {
		// get file as string array
		final String[] data = Utils.loadStrings(filename);
		
		// temporary data
		String[] lineData;

		Integer id;
		String text;
		String response;
		Boolean ends;
		Integer[] options;
		Script script;
		
		final List<Tuple<CNode, NTuple<Integer>>> links = new LinkedList<>();
		
		CNode root = null; // top node of the conversation/dialog
		
		for(final String line : data) {
			lineData = line.split("\t");
			
			id = Utils.toInt(lineData[0], -1);
			text = lineData[1];
			response = lineData[2];
			ends = lineData[3].equals("false") ? false : true;
			options = Utils.stringsToIntegers( lineData[4].split(",") );
			script = new Script( lineData[5] );
			
			//final CNode cn = new CNode(id, text, response);
			
			final CNode cn = new CNode(id, text, response, new ArrayList<CNode>(), ends, script);
			
			//cn.ends = ends;
			
			if( id == 1 ) root = cn;
			
			links.add( new Tuple<CNode, NTuple<Integer>>(cn, new NTuple<Integer>(options)) );
		}
		
		// assemble dialog structure based on link information
		for(int i = 0; i < links.size(); i++) {
			final Tuple<CNode, NTuple<Integer>> t = links.get(i);
			
			final CNode node = t.one;
			final NTuple<Integer> nt = t.two;
			
			// going through the options values
			for(int n = 0; n < nt.numValues(); n++) {
				int nodeId = nt.get(n);
				
				// for each node
				for(final Tuple<CNode, NTuple<Integer>> link : links) {
					final CNode node1 = link.one;
					
					// if that node is one of the options then connect it
					if( node1.getId() == nodeId) node.addOption(node1);
				}
				
			}
		}
		
		return root;
	}
	
	/* Boards */
	private void addBoard(final BulletinBoard bb) {
		final String shortname = bb.getShortName();
		
		if( shortname != null && !shortname.equals("") ) this.boards.put(shortname, bb);
		else                                             this.boards.put(bb.getName(), bb);
	}
	
	// TODO is returning a particular board actually a good solution?
	private BulletinBoard getBoard(final String boardName) {
		return this.boards.get(boardName);
	}
	
	private void handleCombat() {
		// handle combat rounds?
		//debug("Combat Round");
		
		for(final Creature c : this.hostiles) {
			debug("Creature: " + c.getName());
			debug("Location: " + c.getLocation());

			// TODO target should be more generic
			final Player p = c.target; // get it's target

			debug("Target: " + p.getName());
			debug("Location: " + p.getLocation());

			// valid player/target and they are in the same location
			if( p != null && p.getLocation().equals( c.getLocation() ) ) {
				int damage = 1;

				p.setHP(-damage);

				send(c.getName() + " damages you for " + damage + ".", p.getClient());

				p.updateCurrentState();

				// TODO maybe state shouldn't be in player
				if( p.getState() == Player.State.DEAD ) {
					send("You died!", p.getClient());
					handleDeath( p );
					p.setTarget(null);
				}
			}	
		}
	}
	
	/**
	 * createParty
	 * 
	 * Create a party with initial membership from the specified array of Player(s).
	 * 
	 * @param players
	 * @return
	 */
	private Party createParty(final Player...players) {
		final Party party = new Party(players);
		final Player leader = party.getLeader();
		
		final ChatChannel ch = chan.makeChannel("party_" + leader.getName());
		
		// make this channel hidden and password protected
		ch.setHidden(true);
		ch.setPassword( Utils.hash("acegikmoqsuwy99999999") );
		
		party.setChannel(ch);
		
		// add all party members as listeners to the channel
		for(final Player player : party.getPlayers()) {
			ch.addListener(player);
		}
		
		return party;
	}
	
	private boolean hasReagents(final Player player, final Spell spell) {
		boolean result = true;

		if ( spell.getReagents() != null ) {
			for(final Reagent r : spell.getReagents()) {
				final String reagentName = r.getName();

				final Item item = MudUtils.findItem(reagentName, player.getInventory());

				if( item != null ) {
					if(item instanceof Stackable<?>) {
						Stackable s = (Stackable<?>) item;
						
						if( s.stackSize() < 1 ) result = false;
					}
				}
				else result = false;

				if( !result ) break;
			}
		}

		return result;
	}
	
	// match("give 5 gold joe", "give %d %s %s")
	// match("give %d %s %s", "give", "5", "gold", "joe")
	private boolean match(final String pattern, final String...input) {
		if( Utils.countTokens(pattern) == input.length ) {
			if( input[0].equals("") ) {
				
			}
		}
		
		return false;
	}
	
	public void sendMessage(final Message msg, final Client client) {
    	final String sender = msg.getSender().getName();
    	final String message = msg.getMessage();
    	
    	client.writeln(sender + " says, \"" + message + "\" to you. (tell)");
    }
	
	private void respawn(final Player player, final Integer dbref) {
		player.clearEffects();               // remove effects
		player.setHP( player.getTotalHP() ); // restore health
		player.updateCurrentState();
		player.setLocation( dbref );         // return to last location
	}
	
	// TODO resurrect method?
	
	public Palette getColors() {
		switch(this.color) {
		case Constants.ANSI:  return ANSI;
		case Constants.XTERM: return XTERM256;
		default:              return null;
		}
	}
	
	public static Predicate<Item> itemIsUnique() {
	    return p -> p.isUnique();
	}
	
	public static Predicate<Item> itemIsNotUnique() {
	    return p -> !p.isUnique();
	}
	
	public void attach(final Logger l) {
		this.logger = l;
	}
	
	// standard, "always open" and one-way portal (default is active)
	public Portal createPortal(int pOrigin, int pDestination) {
		return new Portal(PortalType.STD, pOrigin, pDestination);
	}
	
	// standard, "always open" and one-way portal (default is active) -- multi destination
	public Portal createPortal(int pOrigin, int[] pDestinations) {
		return new Portal(PortalType.STD, pOrigin, pDestinations);
	}
	
	// keyed portal (default is inactive)
	/*public Portal createPortal(int pOrigin, int pDestination, final Object pKey) {
		final Portal p = new Portal(PortalType.STD, pOrigin, pDestination);

		if (pKey != null) 0
			p.key = pKey;
			p.requiresKey = true;
			
			p.deactivate(pKey);
		}
	}*/
	
	/*public Merchant createMerchant() {
		
	}*/
	
	private final String resolvePath(final String path, final String...dirs) {
		return "" + Paths.get(path, dirs).toAbsolutePath();
	}
	
	private final void startThread(final Runnable r, final String threadName) {
		Thread thread = new Thread(r, threadName);
		
		thread.start();
		
		this.threads.put(thread.getName(), thread);
	}
	
	/**
	 * Retrieve the loot table for a given creature
	 * 
	 * @param creature
	 * @return
	 */
	private List<Item> getLootTable(final Creature creature) {
		final List<Item> lt = lootTables.get( creature.getName().toLowerCase() );
		
		return (( lt != null )? lt : new ArrayList<Item>(0));
	}
	
	// is TARGET a valid target for SPELL cast by PLAYER
	private boolean validTarget(final MUDObject target, final Spell spell, final Player player) {
		// if the spell is an area affect spell, then not having a target is valid, as is having a target,
		// having no target means the spell hits a general area somewhere in front of you, whereas a target
		// means the spell hits and radiates out from the target

		List<String> targets = Arrays.asList( Spell.decodeTargets(spell).split(",") );

		System.out.println("Targets: " + targets);

		if( target instanceof Player ) {
			Player player1 = (Player) target;

			// determine whether the target player is hostile or friendly with regard to
			// the caster

			// I'd like a better method that uses numerical equivalents
			if( targets.contains("self") ) {
				if( player == player1 ) return true;

				return false;
			}
			else if( targets.contains("enemy") ) {
				if( player != player1 ) return false;
				else {
					return false;
				}
			}
			else if( targets.contains("friend") ) {
				if( player != player1 ) return false;
				else {
					return false;
				}
			}
			else return false;
		}
		else {
			return true; // assuming that the spell can target any non-player object, regardless
		}
	}
	
	private final void addSpellTimer(final Player player, final SpellTimer s) {
		getSpellTimers(player).add(s);
	}
	
	private final void addEffectTimer(final Player player, final EffectTimer e) {
		getEffectTimers(player).add(e);
	}
	
	private final void scheduleAtFixedRate(final TimerTask task, final long delay, final long period) {
		timer.scheduleAtFixedRate(task, delay, period);
	}
	
	// generate a dungeon as a rectangular grid of rooms (specific width and length
	private void generateDungeon(String entranceDir, int startX, int startY, int xRooms, int yRooms) {
		List<Room> dRooms = new ArrayList<Room>(xRooms * yRooms);
		
		// list of room/hall? types
		// array of room types
		// generate room based on room types (adding exits as necessary
		// do a second pass over generated rooms to link them together
		
		Room r = null;
		
		for(int n = 0; n < xRooms * yRooms; n++) {
			r = new Room();
			r.setName("A Room");
			
			dRooms.add( r );
		}
		
		Random rng = new Random();
		
		for(final Room room : dRooms) {
			int numExits = rng.nextInt(3) + 1; // 4 - N,S,E,W
			int countEE = 0; // exits that enter this room
			
			for(final Exit exit : room.getExits()) {
				int d = exit.getDestination();
				
				if( d != -1 ) {
					
				}
			}
		}
	}
	
	// 4.8.2018 -- utility functions ripped out of ObjectDB because they don't belong
	private List<Room> getWeatherRooms() {
		return objectDB.getRoomsByType(RoomType.OUTSIDE);
	}
	
	private int getNumPlayers(final PClass c) {
		int count = 0;

		for (final Player p : objectDB.getPlayers()) {
			if ( c.equals(p.getPClass()) ) {
				count += 1;
			}
		}

		return count;
	}
	
	// Send all objects
	public List<String> dumpDatabase() {
		int i = 0;

		final List<MUDObject> objects = objectDB.getObjects();
		final List<String> output = new ArrayList<String>(objects.size());

		for (final MUDObject obj : objects) {
			output.add( String.format("%d: %s (#%d)", i, obj.getName(), obj.getDBRef()) );
			i += 1;
		}

		return output;
	}
	
	// Serialize all objects via `toDB` and save array to file.
	// 
	
	/**
	 * save
	 * 
	 * Saves the game database to the specified file.
	 * 
	 * TODO fix save method, this one depends on saving over the old database
	 * 
	 * @param filename
	 */
	public void save(final String filename) {
		// old (current in file) database
		// toSave (save to file) database
		final String[] old = Utils.loadStrings(DB_FILE);
		final String[] toSave = new String[objectDB.getSize()];

		// TODO sometimes has issues with NullPointerException(s)

		System.out.println("Old Size: " + old.length);
		System.out.println("New Size: " + toSave.length);

		int index = 0;

		if( old != null ) {
			for (final MUDObject obj : objectDB.getObjects()) {
				if(obj instanceof NullObject) {
					NullObject no = (NullObject) obj;

					/* a locked NullObject probably means
					 * a line whose data we are ignoring (but want to keep)
					 */
					if( no.isLocked() ) {
						System.out.println(index + " Old Data: " + old[index] + " Keeping...");
						toSave[index] = old[index]; // keep old data
					}
					else {
						System.out.println(index + " No Previous Data, Overwriting...");
						toSave[index] = obj.toDB(); // just save the null object
					}
				}
				else if(obj instanceof Player) {
					Player player = (Player) obj;

					if( !player.isNew() ) { // if the player is not new, save it
						toSave[index] = obj.toDB();
					}
					else { // otherwise, store a NullObject
						toSave[index] = new NullObject(obj.getDBRef()).toDB();
					}
				}
				else {
					System.out.println(obj.getName());

					toSave[index] = obj.toDB();
				}

				index++;
			}
		}
		else {
			// TODO figure out if this part of save(...) should just go elsewhere. This is essentially
			// breaking the notion of save since we aren't saving over an existing database
			for (final MUDObject obj : objectDB.getObjects()) {
				System.out.println(obj.getName());

				toSave[index] = obj.toDB();

				System.out.println(toSave[index]);

				index++;
			}
		}

		Utils.saveStrings(filename, toSave);
	}
	
	
	
	public List<Node> getMinableNodes(final Room r, final Resource.ResType t) {
		final List<Node> minable_nodes = new ArrayList<Node>();
		
		for(final Node n : nodeList) {
			if( !n.depleted ) {
				final Resource res = n.getResource();
				
				if( res.getType() == Resource.ResType.ORE ) {
					minable_nodes.add( n );
				}
			}
		}
		
		return minable_nodes;
	}
}