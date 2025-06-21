package mud.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;

import mud.Command;
import mud.ObjectFlag;
import mud.MUDObject;
import mud.TypeFlag;
import mud.game.Ability;
import mud.game.Coins;
import mud.game.Faction;
import mud.game.PClass;
import mud.game.Profession;
import mud.game.Race;
import mud.game.Skill;
import mud.interfaces.Mobile;
import mud.interfaces.Ridable;
import mud.interfaces.Ruleset;
import mud.magic.Spell;
import mud.magic.SpellBook;
import mud.misc.Currency;
import mud.misc.Editors;
import mud.misc.MailBox;
import mud.misc.PlayerMode;
import mud.misc.Slot;
import mud.net.Client;
import mud.objects.items.Armor;
import mud.objects.items.Shield;
import mud.objects.items.Weapon;
import mud.quest.Quest;
import mud.rulesets.d20.*;
import mud.utils.EditList;
import mud.utils.Landmark;
import mud.utils.Pager;
import mud.utils.Point;
import mud.utils.Utils;
import mud.utils.EditorData;

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
 * Player Class
 * 
 * int tempDBRef, String tempName, String tempPass, String tempFlags, String tempDesc,
 * String tempTitle, String tempPStatus, int tempLoc, String[] tempMoney
 * 
 * @author Jeremy N. Harton
 *
 */
public class Player extends MUDObject implements Mobile
{
	public static enum Status { ACTIVE, BANNED };            // Player Status - Active, Banned
	public static enum State { ALIVE, INCAPACITATED, DEAD }; // Player State - ALIVE, INCAPACITATED, DEAD
	
	public static Ruleset ruleset;
	
	private static final EnumSet<ObjectFlag> _FLAGS = EnumSet.noneOf(ObjectFlag.class);
	private static final String _STATUS = "NEW";
	private static final String _DESC = "There is nothing to see.";
	private static final Coins _MONEY = new Coins(10, 50, 50, 100); // default_money (10pp, 50gp, 50sp, 100cp)
	private static final Integer[] _STATS = { 0, 0, 0, 0, 0, 0 };

	// levels: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
	//private static int[] levelXP = { 0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 66000 };
	// level[n] = level[n-1] + (n * 1000)
	// n=0: ? + (0 * 1000) = 0
	// n=1: 1000 = 0 + ( 1 * 1000) = 1000
	// n=2; 3000 = 1000 + (2 * 1000) = 3000
	// n=3: 6000 = 3000 + (3 * 1000) = 6000
	// n=4: 10000 = 6000 + (4 * 1000) = 10000
	
	/**
	 * private variable are those that are intended only for the player class
	 * protected variable are those that are intended to be inherited by sub-classes
	 */

	// SuperGame Stuff
	private int id;                                     // unique player id (for the account system) **UNUSED
	private String pass;                                // The player's password in it's hashed state (timed lockout w/ password verification?)
	private PlayerMode mode = PlayerMode.NORMAL;        // Play Mode (default: normal)
	protected int access = 0;                           // the player's access level (permissions) - 0=player,1=admin (default: 0)
	protected String status;                            // The player's status (MU)
	protected String title;                             // The player's title (for where, MU)
	
	private Status pstatus = Status.ACTIVE;             // whether or not the player has been banned
	
	private transient String cName = "";                // name that show up for players who have initiated greeting, etc
	private transient boolean idle_state = false;       // Whether or not the player is currently ide (default: false) **UNUSED
	private transient int idle = 0;                     // the amount of time that the player has been idle (MU)
	private transient String prev_status = "";
	private transient boolean isNew = true;             // is the player new? (e.g. hasn't done chargen)
	private transient boolean controller = false;       // place to indicate if we are controlling an npc (by default, we are not)
	private transient boolean initialized = false;      // has the player been initialzed as per module PCInit(...)

	private transient MailBox mailbox = new MailBox();  // player mailbox

	// TODO move preference to ACCOUNT DATA?
	// ^ does that mean I can't keep it for non-account players OR?
	
	// preferences
	private int lineLimit = 80;      // how wide the client's screen is in columns (shouldn't be in Player class)
	public int invDispWidth = 60;    // display width for the complex version of inventory display
	//private Character invType = 'C'; // display type (S = simple, C = Complex) [config option?]
	
	// h - hitpoints, H - max hitpoints, mv - moves, MV - total moves,m - mana, M - total mana
	private String custom_prompt = "< %h/%H  %mv/%MV %m/%M >"; // borrowed from DIKU -> ROM, etc?

	// utility
	private final Map<String, Boolean> config;          // player preferences for player configurable options
	private final Map<String, Integer> nameRef;         // store name references to dbref numbers (i.e. $this->49)

	protected transient MUDObject target;               // Target
	
	// Game Stuff (most set 'protected' so that an npc can basically have player characteristics
	protected Race race;                                // Race
	protected String gender;                            // Gender
	protected int age;                                  // Age
	protected PClass pclass;                            // Class
	protected Alignments alignment;                     // Alignment
	
	protected int hp;                                   // Hit Points
	protected int totalhp;                              // Total Hit Points
	protected transient State state;                    // character's "state of health" (ALIVE, INCAPACITATED, DEAD)
	
	protected int mana;                                 // Mana
	protected int totalmana;                            // Total Mana
	
	protected int speed;                                // Movement Speed
	
	protected int capacity;                             // Carrying Capacity (pounds/lbs)
	
	protected int level;                                // Level
	protected int xp;                                   // Experience
	
	protected Map<Ability, Integer> stats;              // Player Statistics (D&D, MUD)
	protected Map<Skill, Integer> skills;               // Player Skills (D&D, MUD)
	
	protected Coins money;                              // Money (D&D, MUD)
	public transient Map<Currency, Integer> money2;     //
	
	protected transient List<Item> inventory;           // Player Inventory (D&D, MUD, MU)
	protected transient Map<String, Slot> slots;        // the player's equipped gear
	
	/* Professions */
	private Profession prof1 = null;                    // Active Profession (One) **UNUSED
	private Profession prof2 = null;                    // Active Profession (Two) **UNUSED
	
	private Map<String, Profession> professions = null; // holds all your trained professions **UNUSED (use hashtable)
	
	private transient ArrayList<Quest> quests;          // the player's quests
	private transient Quest active_quest = null;        // the quest the player is currently focusing on
	
	protected Map<Faction, Integer> reputation;
	protected Faction faction;
	
	protected SpellBook spells = null;                  // spells [null if not a spellcaster]
	
	protected transient Deque<Spell> spellQueue = null; // spell queue [null if not a spellcaster]
	protected transient Spell lastSpell = null;         // last spell cast [null if not a spellcaster]

	// movement
	protected boolean moving = false;
	protected boolean flying = false;
	protected Point destination;

	// leveling up
	private int featPts;             // points available for selecting new feats (unused)
	private int skillPts;            // points available for increasing skills (unused)

	//*in some ways i'd rather not assign skill points for leveling up, but i also don't
	// really like classless systems
	//*i'm thinking that feats make sense at a level, but gaining skills ought to be by what 
	// you use the most (hence, 'acquiring' the skill)

	// BitSet to record what item creation feats this player has:
	// Brew Potion (0) Craft Magic Arms And Armor (1) Craft Rod  (2) Craft Staff  (3)
	// Craft Wand  (4) Craft Wondrous Item        (5) Forge Ring (6) Scribe Scroll(7)
	public BitSet item_creation_feats = new BitSet(8);

	// TODO transient because I should be able to recalculate temporary states
	private transient int[] statMod;  // temporary modifications to stats (i.e. stat drains, etc)
	private transient int[] skillMod; // temporary modifications to skills
	
	// TODO do I need some kind of theme specific component to keep data in?
	private int negativeLevels = 0;

	private transient Pager pager = null;             // a pager (ex. 'less' for linux)
	private transient Client client;                  //
	
	private transient Map<String, Command> commandMap; // additional commands avail. to user

	// Knowledge (protected so NPCs can have this stuff too)
	protected List<String> names;                        // names of other players
	protected Map<String, Boolean> discovered_locations; // places you've been to
	protected Map<String, Landmark> landmarks;
	
	private transient Weapon primary = null;
	private transient Weapon secondary = null;
	
	public Ridable mount = null; // TODO is this really a good place to keep mount stuff?
	
	/* End */
	
	/* Editing */
	private transient Editors editor; 
	
	private transient EditorData edd = null; // Miscellaneous Editor
	
	/* List Editor */
	private transient Map<String, EditList> editMap; // array of lists belonging to this player
	private transient EditList currentEdit;          // current list being edited (if any)

	public EditList getEditList() {
		return this.currentEdit;
	}

	public void setEditList(final EditList newList) {
		this.currentEdit = newList;
	}

	/* get an existing list to edit */
	public void loadEditList(final String name) {
		this.currentEdit = this.editMap.get(name);
	}

	public void startEditing(final String name) {
		this.currentEdit = new EditList(name);
	}

	/* save the current list */
	public void saveCurrentEdit() {
		this.editMap.put(currentEdit.getName(), currentEdit);
	}

	/* stop editing -- clears the current list in a final manner */
	public void abortEditing() {
		this.currentEdit = null;
	}
	
	/* End List Editor */
	
	/**
	 * No argument constructor for subclasses
	 * 
	 * NOTE: subclasses must initialize the members they wish to use,
	 * however they can not initialize private members of this class.
	 * 
	 */
	protected Player(final Integer tempDBREF) {
		super(tempDBREF);
		
		this.nameRef = new HashMap<String, Integer>(10, 0.75f); 
		this.config = new LinkedHashMap<String, Boolean>();
	}
	
	/**
	 * NEW PLAYER CONSTRUCTOR ?
	 * 
	 * @param tempDBREF
	 * @param tempName
	 * @param tempPass
	 * @param startingRoom
	 */
	public Player(final Integer tempDBREF, final String tempName, final String tempPass, final int startingRoom) {
		super(tempDBREF);
		
		this.name = tempName;
		this.desc = _DESC;
		this.flags = _FLAGS;
		this.location = startingRoom;
		
		this.type = TypeFlag.PLAYER;
		
		this.target = null;
		
		this.race = Races.NONE;
		this.gender = "None";
		this.pclass = Classes.NONE;
		this.alignment = Alignments.NONE;
		
		this.status = _STATUS;
		this.title = "Newbie";
		
		this.pass = tempPass;

		this.hp = 10;
		this.totalhp = 10;
		this.state = State.ALIVE;
		
		this.mana = 0;
		this.totalmana = 0;
		
		this.speed = 1;
		this.capacity = 100;
		
		this.level = 0;
		this.xp = 0;
		
		this.pass = tempPass;
		
		this.target = null;

		// instantiate slots
		this.slots = new LinkedHashMap<String, Slot>();
		
		// instantiate stats
		this.stats = new LinkedHashMap<Ability, Integer>();
		
		for(final Ability ability : ruleset.getAbilities()) {
			this.stats.put(ability, 0);
		}

		// instantiate skills
		this.skills = new LinkedHashMap<Skill, Integer>();
		
		this.money = _MONEY;
		this.inventory = new ArrayList<Item>(50);

		// instantiate quest list
		this.quests = new ArrayList<Quest>();

		// instantiate list of known names (memory - names)
		this.names = new ArrayList<String>(); // we get a new blank list this way, not a loaded state
		this.discovered_locations = new Hashtable<String, Boolean>();
		this.landmarks = new HashMap<String, Landmark>();

		// initialize list editor variables
		this.editor = Editors.NONE;
		
		this.config = new LinkedHashMap<String, Boolean>();
		
		initConfig();
		
		this.statMod = new int[ruleset.getAbilities().length];
		this.skillMod = new int[ruleset.getSkills().length];
		
		// initialize modification counters to 0
		Arrays.fill(statMod, 0);
		Arrays.fill(skillMod, 0);
		
		this.reputation = new LinkedHashMap<Faction, Integer>();
		
		this.nameRef = new HashMap<String, Integer>();    // instantiate name reference table
		this.commandMap = new HashMap<String, Command>(); // instantiate command map
		
		this.editor = Editors.NONE;
		this.editMap = new HashMap<String, EditList>();
	}

	/**
	 * Object Loading Constructor
	 * 
	 * @param tempDBRef
	 * @param tempName
	 * @param tempPass
	 * @param tempFlags
	 * @param tempDesc
	 * @param tempTitle
	 * @param tempPStatus
	 * @param tempLoc
	 * @param tempMoney
	 */

	public Player(final Integer tempDBREF, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc, 
			final String tempTitle, final String tempPass, final String tempPStatus, final Integer[] tempStats, final Coins tempMoney)
	{
		// use the MUDObject constructor to handle some of the construction?
		//super(tempDBREF, tempName, tempFlags, tempDesc, tempLoc);
		super(tempDBREF);
		
		this.name = tempName;
		this.desc = tempDesc;
		this.flags = tempFlags;
		this.location = tempLoc;
		
		this.type = TypeFlag.PLAYER;
		
		this.target = null;
		
		this.race = Races.NONE;
		this.gender = "None";
		this.pclass = Classes.NONE;
		this.alignment = Alignments.NONE;
		
		this.status = tempPStatus;
		this.title = tempTitle;
		
		// ---
		
		this.hp = 200;
		this.totalhp = 200;
		this.state = State.ALIVE;
		
		this.mana = 100;
		this.totalmana = 100;
		
		this.speed = 1;
		this.capacity = 100;
		
		this.level = 0;
		this.xp = 0;
		
		this.pass = tempPass;
		
		this.target = null;

		// instantiate slots
		this.slots = new LinkedHashMap<String, Slot>();

		this.stats = new LinkedHashMap<Ability, Integer>(ruleset.getAbilities().length, 0.75f);

		int index = 0;

		for(final Ability ability : ruleset.getAbilities()) {
			if( tempStats.length > index ) this.stats.put(ability, tempStats[index]);
			else                           this.stats.put(ability, 0);
			
			index++;
		}

		// instantiate skills
		this.skills = new LinkedHashMap<Skill, Integer>(36, 0.75f);
		
		this.money = tempMoney;
		this.inventory = new ArrayList<Item>(50);
		
		// instantiate quest list
		this.quests = new ArrayList<Quest>();

		// instantiate list of known names (memory - names)
		this.names = new ArrayList<String>(); // we get a new blank list this way, not a loaded state
		this.discovered_locations = new Hashtable<String, Boolean>();
		this.landmarks = new HashMap<String, Landmark>();

		// initialize list editor variables
		this.editor = Editors.NONE;
		
		this.config = new LinkedHashMap<String, Boolean>();
		
		initConfig();
		
		this.statMod = new int[ruleset.getAbilities().length];
		this.skillMod = new int[ruleset.getSkills().length];
		
		// initialize modification counters to 0
		Arrays.fill(statMod, 0);
		Arrays.fill(skillMod, 0);
		
		this.reputation = new LinkedHashMap<Faction, Integer>();
		// faction?
		
		this.nameRef = new HashMap<String, Integer>();    // instantiate name reference table
		this.commandMap = new HashMap<String, Command>(); // instantiate command map
		
		this.editor = Editors.NONE;
		this.editMap = new HashMap<String, EditList>();
		
		// mark player as not new
		this.isNew = false;
	}

	private void initConfig() {
		addConfigOption("global-nameref-table", false); // use the global name reference table instead of a local one (default: false)
		addConfigOption("pinfo-brief",          true);  // make your player info output brief/complete (default: true)
		addConfigOption("prompt_enabled",       false); // enable/disable the prompt (default: false)
		addConfigOption("msp_enabled",          false); // enable/disable MUD Sound Protocol, a.k.a. MSP (default: false)
		addConfigOption("complex-inventory",    false); // use/don't use complex inventory display (default: false)
		addConfigOption("pager_enabled",        false); // enabled/disable the help pager view (default: false)
		addConfigOption("show-weather",         true);  // show weather information in room descriptions (default: true)
		addConfigOption("tagged-chat",          false); // "tag" the beginning chat lines with CHAT for the purpose of triggers, etc (default: false)
		addConfigOption("compact-editor",       true);  // compact the output of editor's 'show' commands (default: true)
		addConfigOption("hud_enabled",          true);  // is the "heads-up display" that accompanies the room description enabled (default: false)
		addConfigOption("notify_newmail",       false); // notify the player on receipt of new mail? (default: false)
		addConfigOption("silly_messages",       false); // enable sillier/more humorous error messages where used (default: false)
		addConfigOption("server_echo",          false); // do we want the server to echo command if that option is enabled (default: false)
	}

	public void setClient(final Client c) {
		this.client = c;
	}

	public Client getClient() {
		return client;
	}

	/**
	 * Get Access
	 * 
	 * @return integer representing a level of permissions
	 */
	public int getAccess() {
		return this.access;
	}

	/**
	 * Set Access
	 * 
	 * @param newAccessLevel integer representing a level of permissions
	 */
	public void setAccess(final int newAccessLevel) {
		this.access = newAccessLevel;
	}

	public Race getRace() { return this.race; }

	public void setRace(final Race race) {
		this.race = race;
		setProperty("_game/race", race.getName().toLowerCase());
	}

	/**
	 * Get player gender
	 * 
	 * @return
	 */
	public String getGender() {
		return this.gender;
	}

	/**
	 * set player gender
	 * 
	 * @param newGender
	 */
	public void setGender(final String newGender) {
		this.gender = newGender;
		setProperty("_game/gender", newGender);
	}

	/**
	 * Get Player Class
	 * 
	 * @return a Classes object that represents the player's character class
	 */
	public PClass getPClass() {
		return this.pclass;
	}

	/**
	 * Set Player Class
	 * 
	 * @param playerClass the character class to set on the player
	 */
	public void setPClass(final PClass playerClass) {
		this.pclass = playerClass;
		
		setProperty("_game/class", playerClass.getName().toLowerCase());

		// do some initialization
		if( playerClass.isCaster() ) {
			this.spells = new SpellBook();
			this.spellQueue = new LinkedList<Spell>();
			this.lastSpell = null;
		}
	}

	/**
	 * Get player title
	 * 
	 * @return
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Set player title
	 * 
	 * @param newTitle
	 */
	public void setTitle(final String newTitle) {
		this.title = newTitle;
	}

	/**
	 * overrides MUDObject's setName method to prevent player names from being
	 * set
	 */
	@Override
	public boolean setName(final String newName) {
		return false;
	}

	// get the players password (BAD, BAD, BAD!!!) -- do not let this have any normal access
	// note: as of ?/?/2011 this should not be a problem since only the hashed version is ever stored
	// might not be as safe as possible, but actual password can't be lost
	public String getPass() {
		return this.pass;
	}

	/**
	 * Set player's password to a new password. Hashes the supplied new password
	 * and stores it. Result of hashing dependent on the implemented hash() function.
	 * However, the resultant hash needs to be a string
	 * 
	 * <br /><br />
	 * 
	 * <b>WARNING</b>: This makes this class dependent on the existence of a hash() function
	 * that returns a string, and having a blank hash function will result in any
	 * player with a new account or having changed their password being left
	 * totally insecure, since password are stored as hashes and any new players
	 * or those who changed their password would have an empty hash. Because hash
	 * would always return empty any password would work for those players logins.
	 * 
	 * @param newPass the new password to set on the player
	 */
	public void setPass(final String newPass) {
		this.pass = Utils.hash(newPass);
	}
	
	// TODO idling
	// doesn't seem like something that really belongs in here, may belong in some kind
	// of session, client data
	
	public boolean isIdle() {
		return this.idle_state;
	}
	
	public void setIdle(final boolean idle) {
		this.idle_state = idle;
		 
		if( idle ) {
			this.prev_status = this.status;
			setStatus("IDL");
		}
		else {
			setStatus(this.prev_status);
		}
	}
	
	public int getIdleTime() {
		return this.idle;
	}
	
	public void setIdleTime(int s) {
		this.idle = s;
	}

	// get the player's status
	public String getStatus() {
		return this.status;
	}
	
	// set the player's status
	public void setStatus(final String arg) {
		this.status = arg;
	}
	
	// player meta status - active, banned
	public Status getPStatus() {
		return this.pstatus;
	}

	public void setPStatus(final Status newStatus) {
		this.pstatus = newStatus;
	}

	public MUDObject getTarget() {
		return this.target;
	}

	public void setTarget(final MUDObject m) {
		this.target = m;
	}

	/**
	 * Get the player's money for the current currency system as an array
	 * of integers, where position indicates denominations
	 * 
	 * @return
	 */
	public Coins getMoney() {
		return this.money;
	}

	// none of this handles player weight, etc
	// if I want to control money by how much weight you can carry
	// then I need to determine a standard weight for the money
	// and calculate that, then decide if the player can hold it
	public void setMoney(final Coins c) {
		this.money = c;
		//this.money = this.money.add(c);
		
		/*if( c.isMoreOrEqual(Coins.copper(0)) ) {
			this.money = this.money.add(c);
		}
		else {
			this.money = this.money.subtractCopper( c.numOfCopper() );
		}*/
		
		// TODO this does not belong here, probably belongs as part of take command or take all
		/*double temp = MudUtils.calculateWeight(this);
		double remaining = this.capacity - temp;
		
		if( c.numOfCopper() * ( 0.0625 ) < remaining ) {
			this.money = this.money.add(c);
		}
		else {
			final int toTake = (int) (remaining / 0.0625 );
			
			this.money.add( c.subtractCopper( toTake ) );
		}*/
	}

	/**
	 * Get and return the player's current level
	 * 
	 * NOTE: affected by negative levels
	 * 
	 * @return
	 */
	public int getLevel() {
		return this.level - this.negativeLevels;
	}

	/**
	 * Set the player's level to a new level.
	 * 
	 * @param changeLevel
	 */
	public void changeLevelBy(final int changeLevel) {
		this.level += changeLevel;
	}

	/**
	 * Gets the player's experience as an integer
	 * 
	 * @return the player's experience points as an int
	 */
	public int getXP() {
		return this.xp;
	}

	/**
	 * Modify Player experience value
	 * 
	 * @param xp
	 */
	public void setXP(final int xp) {
		this.xp += xp;
	}

	/**
	 * Gets and returns the amount of experience needed before the next
	 * level is achieved.
	 * 
	 * @return long the amount of experience needed for the player
	 * to 'level up' to the next level
	 */
	public int getXPToLevel() {
		return getXPToLevel(level+1);
	}
	
	private int getXPToLevel(final int level) {
		/* level[n] = level[n-1] + (n * 1000) */
		if( level <= 0 ) return 0;
		else {
			return getXPToLevel(level - 1) + ( level * 1000 );
		}
	}

	public int getHP() {
		return this.hp;
	}

	public void setHP(final int hp) {
		// you can only set HP up to the total hp
		if( Utils.range(this.hp + hp, 0, getTotalHP()) ) {
			this.hp += hp;
		}
		else {
			if( hp > 0 ) this.hp = getTotalHP();
			else         this.hp = 0;
		}
	}
	
	// TODO use formula to calculate this from stats, etc
	public int getTotalHP() {
		return this.totalhp;
	}

	public int getMana() {
		return this.mana;
	}

	public void setMana(final int mana) {
		// you can only set MANA up to total mana
		if( Utils.range(this.mana + mana, 0, getTotalMana()) ) {
			this.mana += mana;
		}
		else {
			if( mana > 0 ) this.mana = getTotalMana();
			else           this.mana = 0;
		}
	}
	
	// TODO use formula to calculate this from stats, etc
	public int getTotalMana() {
		return this.totalmana;
	}
	
	public int getAbility(final Ability ability) {
		return this.stats.get(ability) + statMod[ability.getId()];
	}

	public void setAbility(final Ability ability, final int abilityValue) {
		this.stats.put(ability, abilityValue);
	}

	public int getAbilityMod(final Ability ability) {
		return this.statMod[ability.getId()];
	}

	public void setAbilityMod(final Ability ability, final int abilityMod) {
		this.statMod[ability.getId()] = abilityMod;
	}

	public void addSkill(final Skill skill) {
		this.skills.put(skill,  0);
	}

	public int getSkill(final Skill skill) {
		return this.skills.get(skill) + skillMod[skill.getId()];
	}

	public void setSkill(final Skill skill, final int skillValue) {
		if( this.skills.containsKey(skill) ) {
			this.skills.put(skill, skillValue);	
		}
	}

	public int getSkillMod(final Skill skill) {
		return this.skillMod[skill.getId()];
	}

	public void setSkillMod(final Skill skill, final int skillMod) {
		this.skillMod[skill.getId()] = skillMod;
	}

	public List<Item> getInventory() {
		return this.inventory;
	}

	public void addSlot(final String name, final Slot slot) {
		if( !this.slots.containsKey(name) ) {
			this.slots.put(name, slot);
		}
	}

	public void removeSlot(final String name) {
		this.slots.remove(name);
	}

	public Slot getSlot(final String key) {
		if( slots.containsKey(key) ) {
			return slots.get(key);
		}

		return null;
	}
	
	public Slot getSlot(final Item item) {
		for(final Slot slot : getSlots().values()) {
			if( slot.isType(item.getItemType()) && slot.getSlotType() == item.getSlotType() ) {
				return slot;
			}
		}
		
		return null;
	}

	public List<Slot> getSlots(final String key) {
		List<Slot> slots = new LinkedList<Slot>();

		for(final String s : getSlots().keySet() ) {
			if( s.startsWith( key.toLowerCase() ) ) {
				slots.add( getSlot(s) );
			}
		}

		return slots;
	}

	public Map<String, Slot> getSlots() {
		return Collections.unmodifiableMap( this.slots );
	}

	public Map<Skill, Integer> getSkills() {
		return Collections.unmodifiableMap( this.skills );
	}

	public Map<Ability, Integer> getStats() {
		return Collections.unmodifiableMap( this.stats );
	}
	
	/* Quests */
	
	public void addQuest(final Quest newQuest) {
		this.quests.add(newQuest);
	}
	
	public void removeQuest(final Quest oldQuest) {
		this.quests.remove(oldQuest);
	}

	/**
	 * 
	 * @param quest
	 * @return
	 */
	public boolean hasQuest(final Quest quest) {
		boolean success = false;

		for(final Quest quest1 : this.quests) {
			if( quest1.getId() == quest.getId() ) {
				success = true;
				break;
			}
		}
		
		return success;
	}
	
	/**
	 * Get the quest with the specified id, if the player has it.
	 * 
	 * @param id
	 * @return
	 */
	public Quest getQuest(final int id) {
		Quest quest = null;
		
		for(final Quest q : this.quests) {
			if( q.getId() == id ) {
				quest = q;
				break;
			}
		}

		return quest;
	}

	/**
	 * 
	 * @return
	 */
	public List<Quest> getQuests() {
		return Collections.unmodifiableList(this.quests);
	}

	public int getCapacity() {
		return this.capacity;
	}

	/*public Character getInvType() {
		return this.invType;
	}*/

	public boolean isCaster() {
		return this.getPClass().isCaster();
	}

	public MailBox getMailBox() {
		return this.mailbox;
	}

	public boolean hasItem(final Item item) {
		return this.inventory.contains(item);
	}
	
	// stuff for storing names of other players
	public void addName(final String tName) {
		this.names.add(tName);
	}

	public void removeName(final String tName) {
		this.names.remove(tName);
	}
	
	public List<String> getNames() {
		return Collections.unmodifiableList(this.names);
	}
	
	public boolean knowsName(final String tName) {
		return this.names.contains(tName);
	}

	/**
	 * <b>Get C(lass) Name</b><br />
	 * 
	 * Returns the name and number combination assigned to the
	 * Player on login that indicates that they are the Xth player
	 * of that class who is currently connected<br/>
	 * 
	 * @return class-based name assigned to the player
	 */
	public String getCName() {
		return this.cName;
	}

	public void setCName(final String newCName) {
		this.cName = newCName;
	}

	public void setController(final boolean isController) {
		this.controller = isController;
	}

	public boolean isController() {
		return this.controller;
	}

	public PlayerMode getMode() {
		return this.mode;
	}

	public void setMode(final PlayerMode newMode) {
		this.mode = newMode;
	}

	/**
	 * isMoving
	 * 
	 * Check and see if this Player is currently moving.
	 * 
	 * @return
	 */
	public boolean isMoving() {
		return this.moving;
	}

	/**
	 * Mark this player as moving
	 * 
	 * @param isMoving
	 */
	public void setMoving(final boolean isMoving) {
		this.moving = isMoving;
	}

	/**
	 * isFlying
	 * 
	 * Check and see if this player is currently flying.
	 * 
	 * @return
	 */
	public boolean isFlying() {
		return this.flying;
	}

	/**
	 * Mark this player as flying (implied in the air)
	 * 
	 * @param isFlying
	 */
	public void setFlying(final boolean isFlying) {
		this.flying = isFlying;
	}

	public Point getDestination() {
		return this.destination;
	}

	public void setDestination(final Point newDest) {
		this.destination = newDest;
	}

	public void changePosition(final int cX, final int cY, final int cZ) {
		this.pos.changeX(cX);
		this.pos.changeY(cY);
		this.pos.changeZ(cZ);
	}

	public Deque<Spell> getSpellQueue() {
		return this.spellQueue;
	}

	public SpellBook getSpellBook() {
		return this.spells;
	}

	public int getLineLimit() {
		return this.lineLimit;
	}

	public void setLineLimit(final int newLineLimit) {
		this.lineLimit = newLineLimit;
	}

	public State getState() {
		return this.state;
	}

	private void setState(final State newState) {
		this.state = newState;
	}

	public void updateCurrentState() {
		final int hp = getHP();
		
		switch(getState()) {
		case ALIVE:
			if (hp <= -10)    setState(State.DEAD);
			else if (hp <= 0) setState(State.INCAPACITATED);
			break;
		case INCAPACITATED:
			if ( hp > 0 )         setState(State.ALIVE);
			else if ( hp <= -10 ) setState(State.DEAD);
			break;
		case DEAD: // only resurrection spells or divine intervention can bring you back from the dead
			break;
		default:
			break;
		}
	}

	/**
	 * Check to see if the player's physical/health state is that specified.
	 * 
	 * @param checkState
	 * @return
	 */
	public boolean isState(final State checkState) {
		return this.state == checkState;
	}

	public Pager getPager() {
		return this.pager;
	}

	public void setPager(final Pager newPager) {
		this.pager = newPager;
	}

	/* Name Reference Table (NRT) methods */

	public Integer getNameRef(final String key) {
		return this.nameRef.get(key);
	}

	public Set<String> getNameReferences() {
		return this.nameRef.keySet();
	}

	public void setNameRef(final String key, final Integer value) {
		this.nameRef.put(key, value);
	}

	public void clearNameRefs() {
		this.nameRef.clear();
	}
	
	/* End NRT methods */
	
	/**
	 * Determines if the player has sufficient experience to
	 * "level up" to the next level.
	 * 
	 * NOTE: does not check maximum levels that server has
	 * 
	 * @return
	 */
	public boolean isLevelUp() {
		if ( getXP() >= getXPToLevel() ) {
			return true;
		}
		else {
			return false;
		}
	}

	public int getAC() {
		return getArmorClass();
	}

	public int getArmorClass() {
		Integer armorClass = 10;
		
		Slot armor_slot = slots.get("armor");
		Slot shield_slot =  slots.get("weapon1");
		
		Item item_a = null;
		Item item_s = null;
		
		if( armor_slot != null )  item_a = armor_slot.getItem();
		if( shield_slot != null ) item_s = shield_slot.getItem();
		
		Shield shield = null;
		Armor armor = null;
		
		if( item_s != null && item_s instanceof Shield ) shield = (Shield) item_s;
		if( item_a != null && item_a instanceof Armor )  armor = (Armor) item_a;
		
		if( armor != null ) {
			armorClass += armor.getArmorBonus();
			
			if( shield != null ) armorClass += shield.getShieldBonus();
		}
		else if( shield != null ) {
			armorClass += shield.getShieldBonus();
		}
		
		return armorClass + getAbilityMod(Abilities.STRENGTH);
	}

	public int getSpeed() {
		return this.speed;
	}

	public void setSpeed(final int newSpeed) {
		this.speed = newSpeed;
	}

	public Alignments getAlignment() {
		return this.alignment;
	}

	public void setAlignment(final Alignments alignment) {
		this.alignment = alignment;
	}

	public void setAlignment(final int newAlignment) {
		this.alignment = Alignments.values()[newAlignment];
	}
	
	public Faction getFaction() {
		return this.faction;
	}
	
	public void setFaction(final Faction newFaction) {
		this.faction = newFaction;
	}

	public void modifyReputation(final Faction faction, final Integer value) {
		setReputation(faction, getReputation(faction) + value);
	}

	public void setReputation(final Faction faction, final Integer value) {
		this.reputation.put(faction, value);
	}

	public Integer getReputation(final Faction faction) {
		return reputation.get(faction);
	}

	/**
	 * addConfigOption
	 * 
	 * Adds a new config option to the config map and sets an initial value.
	 * 
	 * @param option
	 * @param initialValue
	 */
	private void addConfigOption(final String option, final Boolean initialValue) {
		this.config.put(option, initialValue);
	}

	/**
	 * setConfigOption
	 * 
	 * Sets the value of the specified option, if it exists.
	 * 
	 * NOTE: options which don't exist can't be set
	 * 
	 * @param option some config option
	 * @param value  boolean value (true/false)
	 */
	public void setConfigOption(final String option, final Boolean newValue) {
		if( this.config.containsKey(option) ) {
			this.config.put(option, newValue);
		}
	}

	/**
	 * getConfigOption
	 * 
	 * Get the current value of the specified option, if it exists.
	 * 
	 * @param option
	 * @return
	 */
	public Boolean getConfigOption(final String option) {
		if( this.config.containsKey(option) ) {
			return this.config.get(option);
		}

		return false;
	}

	public Boolean hasConfigOption(final String option) {
		return this.config.containsKey(option);
	}

	/**
	 * Get Map containing config options
	 * 
	 * ? rename to getConfig
	 * 
	 * NOTE: this reference is NOT modifiable (no add or remove possible)
	 * 
	 * @return
	 */
	public Map<String, Boolean> getConfig() {
		return Collections.unmodifiableMap(this.config);
	}

	public void setLastSpell(final Spell last) {
		if( this.isCaster() ) {
			this.lastSpell = last;
		}
	}

	/**
	 * 
	 * @return
	 */
	public Spell getLastSpell() {
		if( this.isCaster() ) return this.lastSpell;
		else                  return null;
	}
	
	public Quest getActiveQuest() {
		return this.active_quest;
	}
	
	public void setActiveQuest(final Quest newQuest) {
		this.active_quest = newQuest;
	}
	
	public Map<String, Landmark> getLandmarks() {
		return this.landmarks;
	}
	
	public boolean isInitialized() {
		return this.initialized;
	}

	public boolean isNew() {
		return this.isNew;
	}
	
	public void setNew(final boolean state) {
		this.isNew = state;
	}
	
	// set weapons...
	public Weapon getWeapon(final boolean primary) {
		if( primary ) return this.primary;
		else          return this.secondary;
	}
	
	public void setWeapon(final Weapon weapon, final boolean primary) {
		if( primary ) this.primary = weapon;
		else          this.secondary = weapon;
	}
	
	/* Commands */
	
	public void addCommand(final String cmdName, final Command cmd) {
		this.commandMap.put(cmdName, cmd);
	}
	
	public void removeCommand(final String cmdName) {
		this.commandMap.remove(cmdName);
	}
	
	public boolean hasCommand(final String cmdName) {
		return this.commandMap.containsKey(cmdName) && this.commandMap.get(cmdName) != null;
	}
	
	public Command getCommand(final String cmdName) {
		return this.commandMap.get(cmdName);
	}
	
	public Map<String, Command> getCommands() {
		return Collections.unmodifiableMap( this.commandMap );
	}
	
	/* End Commands */

	// Editors
	
	public boolean hasEditor(final String name) {
		return editMap.containsKey(name);
	}
	
	/**
	 * 
	 * 
	 * @return
	 */
	public Editors getEditor() {
		return this.editor;
	}

	public void setEditor(final Editors editor) {
		this.editor = editor;
	}

	public EditorData getEditorData() {
		return this.edd;
	}

	public void setEditorData(final EditorData newEdD) {
		this.edd = newEdD;
	}

	/**
	 * Translate the persistent aspects of the player into the string
	 * format used by the database
	 */
	public String toDB() {
		//final String[] output = new String[14];
		final String[] output = new String[13];
		
		output[0] = getDBRef() + "";                      // database reference number
		output[1] = getName();                            // name
		output[2] = TypeFlag.asLetter(this.type) + "";    // flags
		output[2] = output[2] + getFlagsAsString();
		output[3] = getDesc();                            // description
		output[4] = getLocation() + "";                   // location
		output[5] = getPass();                            // password

		final StringBuilder sb = new StringBuilder();
		
		int abilities = ruleset.getAbilities().length;
		int count = 1;

		for(final Ability ability : ruleset.getAbilities()) {
			sb.append( this.stats.get(ability) );
			
			if( count < abilities) sb.append(",");
			
			count++;
		}

		output[6] = sb.toString();                        // stats
		output[7] = getMoney().toString(false);           // money
		output[8] = access + "";                          // permissions level
		output[9] = race.getId() + "";                    // race
		output[10] = pclass.getId() + "";                 // class
		output[11] = status;                              // status
		
		output[12] = "" + ((owner != null) ? owner.getDBRef() : -1);
		
		return Utils.join(output, "#");
	}
	
	// TODO remove?
	public String toJSON() {
		final StringBuilder sb = new StringBuilder();

		int count = 0;

		sb.append("{\n");

		sb.append("\t\"dbref\"" + ": \"" + getDBRef() + "\",\n");
		sb.append("\t\"name\"" + ": \"" + getName() + "\",\n");
		sb.append("\t\"flags\"" + ": \"" + TypeFlag.asLetter(this.type) + getFlagsAsString() + "\",\n");
		sb.append("\t\"desc\"" + ": \"" + getDesc() + "\",\n");
		sb.append("\t\"location\"" + ": \"" + getLocation() + "\",\n");

		sb.append("\n");

		sb.append("\t\"pass\"" + ": \"" + getPass() + "\",\n");

		sb.append("\n");

		sb.append("\t\"stats\"" + ":\n");

		sb.append("\t{\n");

		int abilities = ruleset.getAbilities().length;
		count = 1;

		for(Ability ability : ruleset.getAbilities()) {
			sb.append("\t\t\"" + ability.getAbrv() + "\": \"" + this.stats.get(ability) + "\"");
			if( count < abilities ) sb.append(",\n");
			else                    sb.append("\n");
			count++;
		}

		sb.append("\t},\n");

		sb.append("\n");

		sb.append("\t\"skill\"" + ":\n");

		sb.append("\t{\n");

		int skills = ruleset.getSkills().length;
		count = 1;

		for(Skill skill : ruleset.getSkills()) {
			final String skillName = skill.getName();
			sb.append("\t\t\"" + skillName + "\": \"" + this.stats.get(skillName) + "\"");
			if( count < skills ) sb.append(",\n");
			else                    sb.append("\n");
			count++;
		}

		sb.append("\t},\n");

		sb.append("\n");

		sb.append("\t\"money\"" + ":\n");

		sb.append("\t{\n");

		String[] mn = { "platinum", "gold", "silver", "copper" };
		count = 0;

		for(final String value : getMoney().toString(false).split(",")) {
			sb.append("\t\t\"" + mn[count] + "\": \"" + value + "\"");
			if( count < 4 ) sb.append(",\n");
			else            sb.append("\n");
			count++;
		}

		sb.append("\t},\n");

		sb.append("\n");

		sb.append("\t\"names\"" + ":\n");

		sb.append("\t{\n");

		int numNames = names.size();
		count = 0;

		for(final String name : names) {
			sb.append("\t\t\"name\"" + ": \"" + name + "\"");
			if( count < numNames ) sb.append(",\n");
			else                   sb.append("\n");
			count++;
		}

		sb.append("\t},\n");

		sb.append("\n");
		
		sb.append( Utils.jsonify("access",  getAccess())       );
		sb.append( Utils.jsonify("race",    race.getId())      );
		sb.append( Utils.jsonify("class",   pclass.getId())    );
		sb.append( Utils.jsonify("status",  status)            );
		sb.append( Utils.jsonify("state",   state.ordinal())   );
		sb.append( Utils.jsonify("pstatus", pstatus.ordinal()) );

		/*sb.append("\t\"access\"" + ": \"" + getAccess() + "\",\n");
		sb.append("\t\"race\"" + ": \"" + race.getId() + "\",\n");
		sb.append("\t\"class\"" + ": \"" + pclass.getId() + "\",\n");
		sb.append("\t\"status\"" + ": \"" + status + "\",\n");
		sb.append("\t\"state\"" + ": \"" + state.ordinal() + "\",\n");
		sb.append("\t\"pstatus\"" + ": \"" + pstatus.ordinal() + "\"\n");*/

		sb.append("}\n");

		return sb.toString();
	}
}