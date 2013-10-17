package mud.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;

import mud.net.Client;

import mud.Alignments;
import mud.Classes;
import mud.ObjectFlag;
import mud.Abilities;
import mud.Coins;
import mud.Editor;
import mud.MUDObject;
import mud.PClass;
import mud.Race;
import mud.TypeFlag;
import mud.Race.Subraces;
import mud.Races;
import mud.Skill;
import mud.Skills;
import mud.Slot;
import mud.SlotType;

import mud.MUDServer.PlayerMode;

import mud.interfaces.Equippable;
import mud.interfaces.Wearable;
import mud.magic.Spell;
import mud.magic.SpellBook;
import mud.objects.items.Armor;
import mud.objects.items.ClothingType;
import mud.objects.items.Handed;
import mud.objects.items.Shield;

import mud.quest.Quest;
import mud.quest.Task;
import mud.quest.TaskType;

import mud.utils.EditList;
import mud.utils.Landmark;
import mud.utils.MailBox;
import mud.utils.Pager;
import mud.utils.Point;
import mud.utils.cgData;
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
public class Player extends MUDObject
{	
	private static final EnumSet<ObjectFlag> _FLAGS = EnumSet.of(ObjectFlag.PLAYER);
	private static final String _STATUS = "NEW";
	private static final String _DESC = "There is nothing to see.";
	private static final Coins _MONEY = new Coins(10, 50, 50, 100); // default_money (10pp, 50gp, 50sp, 100cp)
	private static final Integer[] _STATS = { 0, 0, 0, 0, 0, 0 };

	// levels: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
	private static int[] levelXP = { 0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 66000 };
	
	/**
	 * Player State
	 * Alive - alive, INCAPACITATED - incapacitated (hp < 0 && hp > -10), DEAD (hp < -10)
	 * @author Jeremy
	 */
	public static enum State { ALIVE, INCAPACITATED, DEAD };
	
	public static enum Status { ACTIVE, BANNED };

	/**
	 * private variable are those that are intended only for the player class
	 * protected variable are those that are intended to be inherited by sub-classes
	 */

	// SuperGame Stuff
	private int id;                                   // unique player id (for the account system) **UNUSED
	private boolean idle_state = false;               // Whether or not the player is currently idle (default: false) **UNUSED
	private int idle = 0;                             // the amount of time that the player has been idle (MU)
	private String pass;                              // The player's password in it's hashed state (timed lockout w/ password verification)
	private PlayerMode mode = PlayerMode.NORMAL;      // Play Mode (default: normal)
	protected String cName = "";                      // name that show up for players who have initiated greeting, etc
	protected String status;                          // The player's status (MU)
	protected String title;                           // The player's title (for where, MU)
	private Status pstatus;                           // whether or not the player has been banned
	protected ArrayList<String> names;                // names of other players that the player actually knows

	final private MailBox mailbox = new MailBox();    // player mailbox

	protected int access = 0;                         // the player's access level (permissions) - 0=player,1=admin (default: 0)

	private boolean isNew;                            // is the player new? (e.g. hasn't done chargen)

	private boolean controller = false;               // place to indicate if we are controlling an npc (by default, we are not)
	private HashMap<String, EditList> editorsMap = new HashMap<String, EditList>(); // array of lists belonging to this player

	// preferences (ACCOUNT DATA?)
	private int lineLimit = 80;                       // how wide the client's screen is in columns (shouldn't be in Player class)
	public int invDispWidth = 60;                     // display width for the complex version of inventory display
	private Character invType = 'C';                  // S = simple, C = Complex (candidate for being a config option, not a single variable)
	private LinkedHashMap<String, Boolean> config;    // player preferences for player configurable options

	// utility
	private HashMap<String, Integer> nameRef;         // store name references to dbref numbers (i.e. $this->49)

	// Editors, General
	private Editor editor;

	/* Editor Data */ 

	// Character Editor
	private cgData cgd = null;

	// List Editor
	private EditList currentEdit;

	public EditList getEditList() {
		return currentEdit;
	}

	public void startEditing(final String name) {
		currentEdit = new EditList(name);
	}
	
	/* get an existing list to edit */
	public void loadEditList(final String name) {
		currentEdit = editorsMap.get(name);
	}
	
	/* load a list to edit from some other source (files?) */
	public void loadEditList(final String name, final List<String> lines) {
		currentEdit = new EditList(name, lines);
	}

	/* save the current list */
	public void saveCurrentEditor() {
		editorsMap.put(currentEdit.name, currentEdit);
	}
	
	/* stop editing -- clears the current list in a final manner */
	public void abortEditing() {
		currentEdit = null;
	}

	// Miscellaneous Editor
	private EditorData edd = null;

	// Game Stuff (most set 'protected' so that an npc can basically have player characteristics
	protected MUDObject target;                    // Target -- player/npc that will be used for generic interaction
	protected Race race;                           // Race
	protected Character gender;                    // Gender
	protected PClass pclass;                       // Class
	protected Alignments alignment;                // Alignment
	protected Handed handed = Handed.RIGHT;        // which hand is dominant (irr. but enum encompasses that and weapons hand req.)
	protected int hp;                              // Hit Points
	protected int totalhp;                         // Total Hit Points
	protected int mana;                            // Mana
	protected int totalmana;                       // Total Mana
	protected int speed;                           // Movement Speed (largely pointless without a coordinate system)
	protected int capacity;                        // Carrying Capacity (pounds/lbs)
	protected int level;                           // Level
	protected int xp;                              // Experience
	protected Coins money;                         // Money (D&D, MUD)

	protected SpellBook spells = null;             // spells [null if not a spellcaster]
	protected LinkedList<Spell> spellQueue = null; // spell queue [null if not a spellcaster]
	protected Spell lastSpell = null;              // last spell cast [null if not a spellcaster]

	private State state = State.ALIVE;             // character's "state of health" (ALIVE, INCAPACITATED, DEAD)

	protected LinkedHashMap<Abilities, Integer> stats;             // Player Statistics (D&D, MUD)
	protected LinkedHashMap<Skill, Integer> skills;                // Player Skills (D&D, MUD)

	protected ArrayList<Item> inventory = new ArrayList<Item>(10); // Player Inventory (D&D, MUD, MU)
	protected LinkedHashMap<String, Slot> slots;                   // the player's equipped gear

	private ArrayList<Quest> quests;                               // the player's quests

	// movement
	protected boolean moving;
	protected Point destination;

	// leveling up
	private boolean levelup = false; // is this player ready to "level up" (true=yes,false=no)
	private int featPts;             // points available for selecting new feats (unused)
	private int skillPts;            // points available for increasing skills (unused)
	
	//*in some ways i'd rather not assign skill points for leveling up, but i also don't
	// really like classless systems
	//*i'm thinking that feats make sense at a level, but gaining skills ought to be by what 
	// you use the most (hence, 'acquiring' the skill)

	// BitSet to record what item creation feats this player has:
	// Brew Potion (0) Craft Magic Arms And Armor (1) Craft Rod  (2) Craft Staff  (3)
	// Craft Wand  (4) Craft Wondrous Item        (5) Forge Ring (6) Scribe Scroll(7)
	private BitSet item_creation_feats = new BitSet(8);

	// temporary states
	private int[] statMod = new int[6];   // temporary modifications to stats (i.e. stat drains, etc)
	private int[] skillMod = new int[44]; // temporary modifications to skills
	private int negativeLevels = 0;

	// borrowed from DIKU -> ROM, etc?
	// h - hitpoints, H - max hitpoints
	// mv - moves, MV - total moves
	// m - mana, M - total mana
	private String custom_prompt = "< %h/%H  %mv/%MV %m/%M >"; // ACCOUNT DATA?

	private Pager pager = null; // a pager (ex. 'less' for linux), displays a page's/screen's worth of text at a time

	private Client client;
	
	// Knowledge?
	
	public Map<String, Landmark> landmarks = new HashMap<String, Landmark>(); // contains "landmarks", which are places you've been and h

	/**
	 * No argument constructor for subclasses
	 * 
	 * NOTE: subclasses must initialize the members they wish to use,
	 * however they can not initialize private members of this class.
	 * 
	 */
	public Player(int tempDBREF, final String tempName, final String tempPass, final int startingRoom) {
		super(tempDBREF);
		type = TypeFlag.PLAYER;

		name = tempName;
		pass = tempPass;

		isNew = true;

		this.name = tempName;
		this.race = Races.NONE;
		this.gender = 'N';
		this.pclass = Classes.NONE;
		this.alignment = Alignments.NONE;
		
		this.desc = _DESC;
		this.title = "Newbie";

		this.hp = 10;
		this.totalhp = 10;
		this.mana = 0;
		this.totalmana = 0;
		this.speed = 1;
		this.capacity = 100;
		this.level = 0;
		this.xp = 0;
		this.money = _MONEY;
		this.location = startingRoom;
		this.target = null;

		this.pass = tempPass;
		this.flags = _FLAGS;
		this.status = _STATUS;
		this.locks = "";

		// instantiate slots
		this.slots = new LinkedHashMap<String, Slot>(11, 0.75f);

		// initialize slots
		this.slots.put("helmet", new Slot(new SlotType[] { SlotType.HEAD }, ItemType.HELMET));
		this.slots.put("necklace", new Slot(new SlotType[] { SlotType.NECK }, ItemType.NECKLACE));
		this.slots.put("armor", new Slot(new SlotType[] { SlotType.CHEST }, ItemType.ARMOR));
		this.slots.put("cloak", new Slot(new SlotType[] { SlotType.BACK }, ClothingType.CLOAK));
		this.slots.put("ring1", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring2", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring3", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring4", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring5", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring6", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("gloves", new Slot(new SlotType[] { SlotType.HANDS }, ClothingType.GLOVES));
		this.slots.put("weapon", new Slot(new SlotType[] { SlotType.RHAND }, ItemType.WEAPON));
		this.slots.put("weapon1", new Slot(new SlotType[] { SlotType.LHAND }, ItemType.WEAPON));
		this.slots.put("belt", new Slot(new SlotType[] { SlotType.WAIST }, ClothingType.BELT));;
		this.slots.put("boots", new Slot(new SlotType[] { SlotType.FEET }, ClothingType.BOOTS));

		// instantiate stats
		stats = new LinkedHashMap<Abilities, Integer>(6, 0.75f);

		// initialize stats
		this.stats.put(Abilities.STRENGTH, _STATS[0]);     // Strength
		this.stats.put(Abilities.DEXTERITY, _STATS[1]);    // Dexterity
		this.stats.put(Abilities.CONSTITUTION, _STATS[2]); // Constitution
		this.stats.put(Abilities.INTELLIGENCE, _STATS[3]); // Intelligence
		this.stats.put(Abilities.WISDOM, _STATS[4]);       // Wisdom
		this.stats.put(Abilities.CHARISMA, _STATS[5]);     // Charisma
		
		// instantiate skills
		skills = new LinkedHashMap<Skill, Integer>(36, 0.75f);
		
		// these should be all -1, since no class is specified initially
		this.skills.put(Skills.APPRAISE, -1);            this.skills.put(Skills.BALANCE, -1);            this.skills.put(Skills.BLUFF, -1);
		this.skills.put(Skills.CLIMB, -1);               this.skills.put(Skills.CONCENTRATION, -1);      this.skills.put(Skills.CRAFT, -1);
		this.skills.put(Skills.DECIPHER_SCRIPT, -1);     this.skills.put(Skills.DIPLOMACY, -1);          this.skills.put(Skills.DISGUISE, -1);
		this.skills.put(Skills.ESCAPE_ARTIST, -1);       this.skills.put(Skills.GATHER_INFORMATION, -1); this.skills.put(Skills.HANDLE_ANIMAL, -1);
		this.skills.put(Skills.HEAL, -1);                this.skills.put(Skills.HIDE, -1);               this.skills.put(Skills.INTIMIDATE, -1);
		this.skills.put(Skills.JUMP, -1);

		this.skills.put(Skills.KNOWLEDGE, -1);           this.skills.put(Skills.KNOWLEDGE_ARCANA, -1);   this.skills.put(Skills.KNOWLEDGE_DUNGEONEERING, -1); 
		this.skills.put(Skills.KNOWLEDGE_GEOGRAPHY, -1); this.skills.put(Skills.KNOWLEDGE_HISTORY, -1);  this.skills.put(Skills.KNOWLEDGE_LOCAL, -1);         
		this.skills.put(Skills.KNOWLEDGE_NATURE, -1);    this.skills.put(Skills.KNOWLEDGE_NOBILITY, -1); this.skills.put(Skills.KNOWLEDGE_PLANAR, -1);        
		this.skills.put(Skills.KNOWLEDGE_RELIGION, -1);

		this.skills.put(Skills.LISTEN, -1);              this.skills.put(Skills.MOVE_SILENTLY, -1);      this.skills.put(Skills.NAVIGATION, -1);
		this.skills.put(Skills.PERFORM, -1);             this.skills.put(Skills.PROFESSION, -1);         this.skills.put(Skills.RIDE, -1);
		this.skills.put(Skills.SEARCH, -1);              this.skills.put(Skills.SENSE_MOTIVE, -1);       this.skills.put(Skills.SLEIGHT_OF_HAND, -1);
		this.skills.put(Skills.SPEAK_LANGUAGE, -1);      this.skills.put(Skills.SPELLCRAFT, -1);         this.skills.put(Skills.SPOT, -1);
		this.skills.put(Skills.SURVIVAL, -1);            this.skills.put(Skills.SWIM, -1);               this.skills.put(Skills.TRACKING, -1);
		this.skills.put(Skills.TUMBLE, -1);              this.skills.put(Skills.USE_MAGIC_DEVICE, -1);   this.skills.put(Skills.USE_ROPE, -1);

		// instantiate quest list
		this.quests = new ArrayList<Quest>();

		// instantiate list of known names (memory - names)
		this.names = new ArrayList<String>(); // we get a new blank list this way, not a loaded state

		// initialize list editor variables
		this.editor = Editor.NONE;

		this.config = new LinkedHashMap<String, Boolean>();
		this.config.put("global-nameref-table", false); // use the global name reference table instead of a local one (default: false)
		this.config.put("pinfo-brief", true);           // make your player info output brief/complete (default: true)
		this.config.put("prompt_enabled", false);       // enable/disable the prompt (default: false)
		this.config.put("msp_enabled", false);          // enable/disable MUD Sound Protocol, a.k.a. MSP (default: false)
		this.config.put("complex-inventory", false);    // use/don't use complex inventory display (default: false)
		this.config.put("pager_enabled", false);        // enabled/disable the help pager view (default: false)
		this.config.put("show-weather", true);          // show weather information in room descriptions (default: true)
		this.config.put("tagged-chat", false);          // "tag" the beginning chat lines with CHAT for the purpose of triggers, etc (default: false)

		// instantiate name reference table
		this.nameRef = new HashMap<String, Integer>(10, 0.75f); // start out assuming 10 name references

		// initialize modification counters to 0
		Arrays.fill(statMod, 0);
		Arrays.fill(skillMod, 0);
		
		// mark player as new
		isNew = true;
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

	public Player(final int tempDBREF, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc, 
			final String tempTitle, final String tempPass, final String tempPStatus, final Integer[] tempStats, final Coins tempMoney)
	{
		// use the MUDObject constructor to handle some of the construction?
		//super(tempDBREF, tempName, tempFlags, tempDesc, tempLoc);
		super(tempDBREF);
		type = TypeFlag.PLAYER;

		this.race = Races.NONE;
		this.gender = 'N';
		this.pclass = Classes.NONE;
		this.alignment = Alignments.NONE;

		this.hp = 10;
		this.totalhp = 10;
		this.mana = 40;
		this.totalmana = 40;
		this.speed = 1;
		this.capacity = 200;
		this.level = 0;
		this.xp = 0;
		this.money = tempMoney; // use default money criteria from server config or stored player money in future

		this.name = tempName;
		this.pass = tempPass;
		this.flags = tempFlags;
		this.locks = ""; // should take tempLocks argument
		this.desc = tempDesc;
		this.status = tempPStatus;
		this.title = tempTitle;
		this.location = tempLoc;
		this.target = null;

		// instantiate slots
		this.slots = new LinkedHashMap<String, Slot>(11, 0.75f);

		// initialize slots
		this.slots.put("helmet", new Slot(new SlotType[] { SlotType.HEAD }, ItemType.HELMET));
		this.slots.put("necklace", new Slot(new SlotType[] { SlotType.NECK }, ItemType.NECKLACE));
		this.slots.put("armor", new Slot(new SlotType[] { SlotType.CHEST }, ItemType.ARMOR));
		this.slots.put("cloak", new Slot(new SlotType[] { SlotType.BACK }, ClothingType.CLOAK));
		this.slots.put("ring1", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring2", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring3", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring4", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring5", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("ring6", new Slot(new SlotType[] { SlotType.FINGER }, ItemType.RING));
		this.slots.put("gloves", new Slot(new SlotType[] { SlotType.HANDS }, ClothingType.GLOVES));
		this.slots.put("weapon", new Slot(new SlotType[] { SlotType.RHAND }, ItemType.WEAPON));
		this.slots.put("weapon1", new Slot(new SlotType[] { SlotType.LHAND }, ItemType.WEAPON));
		this.slots.put("belt", new Slot(new SlotType[] { SlotType.WAIST }, ClothingType.BELT));;
		this.slots.put("boots", new Slot(new SlotType[] { SlotType.FEET }, ClothingType.BOOTS));

		// instantiate stats
		stats = new LinkedHashMap<Abilities, Integer>(6, 0.75f);

		// initialize stats
		this.stats.put(Abilities.STRENGTH, tempStats[0]);     // Strength
		this.stats.put(Abilities.DEXTERITY, tempStats[1]);    // Dexterity
		this.stats.put(Abilities.CONSTITUTION, tempStats[2]); // Constitution
		this.stats.put(Abilities.INTELLIGENCE, tempStats[3]); // Intelligence
		this.stats.put(Abilities.WISDOM, tempStats[4]);       // Wisdom
		this.stats.put(Abilities.CHARISMA, tempStats[5]);     // Charisma

		// instantiate skills
		skills = new LinkedHashMap<Skill, Integer>(36, 0.75f);

		// initialize skills
		// these should be all -1, since no class is specified initially
		this.skills.put(Skills.APPRAISE, -1);            this.skills.put(Skills.BALANCE, -1);            this.skills.put(Skills.BLUFF, -1);
		this.skills.put(Skills.CLIMB, -1);               this.skills.put(Skills.CONCENTRATION, -1);      this.skills.put(Skills.CRAFT, -1);
		this.skills.put(Skills.DECIPHER_SCRIPT, -1);     this.skills.put(Skills.DIPLOMACY, -1);          this.skills.put(Skills.DISGUISE, -1);
		this.skills.put(Skills.ESCAPE_ARTIST, -1);       this.skills.put(Skills.GATHER_INFORMATION, -1); this.skills.put(Skills.HANDLE_ANIMAL, -1);
		this.skills.put(Skills.HEAL, -1);                this.skills.put(Skills.HIDE, -1);               this.skills.put(Skills.INTIMIDATE, -1);
		this.skills.put(Skills.JUMP, -1);

		this.skills.put(Skills.KNOWLEDGE, -1);           this.skills.put(Skills.KNOWLEDGE_ARCANA, -1);   this.skills.put(Skills.KNOWLEDGE_DUNGEONEERING, -1); 
		this.skills.put(Skills.KNOWLEDGE_GEOGRAPHY, -1); this.skills.put(Skills.KNOWLEDGE_HISTORY, -1);  this.skills.put(Skills.KNOWLEDGE_LOCAL, -1);         
		this.skills.put(Skills.KNOWLEDGE_NATURE, -1);    this.skills.put(Skills.KNOWLEDGE_NOBILITY, -1); this.skills.put(Skills.KNOWLEDGE_PLANAR, -1);        
		this.skills.put(Skills.KNOWLEDGE_RELIGION, -1);

		this.skills.put(Skills.LISTEN, -1);              this.skills.put(Skills.MOVE_SILENTLY, -1);      this.skills.put(Skills.NAVIGATION, -1);
		this.skills.put(Skills.PERFORM, -1);             this.skills.put(Skills.PROFESSION, -1);         this.skills.put(Skills.RIDE, -1);
		this.skills.put(Skills.SEARCH, -1);              this.skills.put(Skills.SENSE_MOTIVE, -1);       this.skills.put(Skills.SLEIGHT_OF_HAND, -1);
		this.skills.put(Skills.SPEAK_LANGUAGE, -1);      this.skills.put(Skills.SPELLCRAFT, -1);         this.skills.put(Skills.SPOT, -1);
		this.skills.put(Skills.SURVIVAL, -1);            this.skills.put(Skills.SWIM, -1);               this.skills.put(Skills.TRACKING, -1);
		this.skills.put(Skills.TUMBLE, -1);              this.skills.put(Skills.USE_MAGIC_DEVICE, -1);   this.skills.put(Skills.USE_ROPE, -1);

		// instantiate quest list
		this.quests = new ArrayList<Quest>();

		// instantiate list of known names (memory - names)
		this.names = new ArrayList<String>(); // we get a new blank list this way, not a loaded state

		// initialize list editor variables
		this.editor = Editor.NONE;

		this.config = new LinkedHashMap<String, Boolean>();
		this.config.put("global-nameref-table", false); // use the global name reference table instead of a local one (default: false)
		this.config.put("pinfo-brief", true);           // make your player info output brief/complete (default: true)
		this.config.put("prompt_enabled", false);       // enable/disable the prompt (default: false)
		this.config.put("msp_enabled", false);          // enable/disable MUD Sound Protocol, a.k.a. MSP (default: false)
		this.config.put("complex-inventory", false);    // use/don't use complex inventory display (default: false)
		this.config.put("pager_enabled", false);        // enabled/disable the help pager view (default: false)
		this.config.put("show-weather", true);          // show weather information in room descriptions (default: true)
		this.config.put("tagged-chat", false);          // "tag" the beginning chat lines with CHAT for the purpose of triggers, etc (default: false)

		// instantiate name reference table
		this.nameRef = new HashMap<String, Integer>(10, 0.75f); // start out assuming 10 name references

		// initialize modification counters to 0
		Arrays.fill(statMod, 0);
		Arrays.fill(skillMod, 0);

		// mark player as not new
		isNew = false;
	}

	public void setClient(final Client c) {
		this.client = c;
	}

	public Client getClient() {
		return client;
	}

	public void addName(String tName) {
		this.names.add(tName);
	}

	public void removeName(String tName) {
		this.names.remove(tName);
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
	public void setAccess(int newAccessLevel) {
		this.access = newAccessLevel;
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
	public void setPClass(PClass playerClass) {
		this.pclass = playerClass;

		// do some initialization
		if( playerClass.isCaster() ) {
			this.spells = new SpellBook();
			this.spellQueue = new LinkedList<Spell>();
			this.lastSpell = null;
		}
	}

	public Race getPRace() { return this.race; }

	public void setPlayerRace(Race race) { this.race = race; }

	public Character getGender() { return this.gender; }

	public void setGender(Character newGender) { this.gender = newGender; }

	/**
	 * Get player title
	 * 
	 * @return
	 */
	public String getTitle() { return this.title; }

	/**
	 * Set player title
	 * 
	 * @param newTitle
	 */
	public void setTitle(String newTitle) { this.title = newTitle; }

	@Override
	public boolean setName(String newName) { return false; }

	// get the players password (BAD, BAD, BAD!!!) -- do not let this have any normal access
	// note: as of ?/?/2011 this should not be a problem since only the hashed version is ever stored
	// might not be as safe as possible, but actual password can't be lost
	public String getPass() { return this.pass; }

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
	public void setPass(String newPass) {
		this.pass = Utils.hash(newPass);
	}

	// get the player's idle time
	public int getIdle() {
		return this.idle;
	}

	public String getIdleString() {
		if ( this.idle > 0 ) {
			if (this.idle > 60) {
				int m = this.idle / 60;
				int s = this.idle % 60;
				return m + "m" + s + "s";
			}
			else {
				return this.idle + "s";
			}
		}
		else {
			return "----";
		}
	}

	// get the player's status
	public String getStatus() { return this.status; }

	// set the player's status
	public void setStatus(String arg) { this.status = arg; }
	
	public Status getPStatus() {
		return this.pstatus;
	}
	
	public void setPStatus(Status newStatus) {
		this.pstatus = newStatus;
	}

	public MUDObject getTarget() {
		if (this.target instanceof NPC) {
			return (NPC) this.target;
		}
		else {
			return this.target;
		}
	}

	public void setTarget(MUDObject m) {
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
		this.money.add(c);
	}

	/**
	 * Get and return the player's current level
	 * 
	 * NOTE: affected by negative levels
	 * 
	 * @return
	 */
	public int getLevel() {
		return this.level - negativeLevels;
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
	public int getXP() { return this.xp; }

	/**
	 * It may not be the best idea, but we'll do level checks here,
	 * that way when your total xp exceeds the total xp you need for
	 * the next level you'll be 'flagged' as ready to 'levelup';
	 * 
	 * NOTE: It would be good to inform the player when that happens, but that shouldn't be done here
	 * 
	 * @param xp
	 */
	public void setXP(int xp) {
		this.xp += xp;
	}

	/**
	 * Gets and returns the amount of experience needed before the next
	 * level is achieved.
	 * 
	 * @return long the amount of experience needed for the player
	 * to 'level up' to the next level
	 */
	public int getXPToLevel() { return Player.levelXP[level]; }

	public int getHP() {
		return this.hp;
	}

	public void setHP(int hp) {
		this.hp += hp;
	}

	public int getTotalHP() {
		return this.totalhp;
	}

	public void setTotalHP(int hp) {
		this.totalhp = hp;
	}

	public int getMana() {
		return this.mana;
	}

	public void setMana(int mana) {
		this.mana += mana;
	}

	public int getTotalMana() {
		return this.totalmana;
	}

	public void setTotalMana(int mana) {
		this.totalmana = mana;
	}

	public int getAbility(Abilities ability) {
		return this.stats.get(ability) + statMod[ability.ordinal()];
	}

	public void setAbility(Abilities ability, int abilityValue) {
		this.stats.put(ability, abilityValue);
	}

	public void setAbilityMod(Abilities ability, int abilityMod) {
		this.statMod[ability.ordinal()] = abilityMod;
	} 

	public int getSkill(Skill skill) {
		return this.skills.get(skill) + skillMod[skill.getId()];
	}

	public void setSkill(Skill skill, int skillValue) {
		this.skills.put(skill, skillValue);
	}

	public int getSkillMod(Skill skill) {
		return this.skillMod[skill.getId()];
	}

	public void setSkillMod(Skill skill, int skillMod) {
		this.skillMod[skill.getId()] = skillMod;
	}

	public ArrayList<Item> getInventory() {
		return this.inventory;
	}

	public LinkedHashMap<String, Slot> getSlots() {
		return this.slots;
	}

	public LinkedHashMap<Skill, Integer> getSkills() {
		return this.skills;
	}

	public LinkedHashMap<Abilities, Integer> getStats() {
		return this.stats;
	}

	public ArrayList<Quest> getQuests() {
		return this.quests;
	}
	
	public boolean hasQuest( Quest quest ) {
		for(Quest quest1 : this.quests) {
			if( quest.getId() == quest1.getId() ) return true;
		}
		
		return false;
	}

	public int getCapacity() {
		return this.capacity;
	}

	public Character getInvType() {
		return this.invType;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public Editor getEditor() {
		return this.editor;
	}

	public void setEditor(Editor editor) {
		//if( editor != Editor.NONE ) this.status = "EDT";
		if( editor != Editor.NONE ) setStatus("EDT");
		this.editor = editor;
	}

	public EditorData getEditorData() {
		return this.edd;
	}

	public void setEditorData(EditorData newEdD) {
		this.edd = newEdD;
	}

	public cgData getCGData() {
		return this.cgd;
	}

	public void setCGData(cgData newCGD) {
		this.cgd = newCGD;
	}

	public boolean isCaster() {
		return this.getPClass().isCaster();
	}

	public MailBox getMailBox() {
		return this.mailbox;
	}

	public void equip(Item item, Slot slot) {
		slot.insert(item);
	}

	public void equip(Item item, String location) {
		if ( this.slots.containsKey(location) ) {
			this.slots.get(location).insert(item);
		}
	}

	public void unequip(Item item, Slot slot) {
		if (slot.isFull()) {
			if (slot.getItem() == item) {
				slot.remove();
			}
		}
	}

	public void unequip(Item item, String location) {
		if ( this.slots.containsKey(location) ) {
			this.slots.get(location);
			this.inventory.add(item);
		}
	}

	public void unequip(Item item) {
		this.inventory.add(item);
	}

	public ArrayList<String> getNames() {
		return this.names;
	}

	/**
	 * Get Class Name
	 * 
	 * Returns the name and number combination assigned to the
	 * Player on login that indicates that they are the Xth player
	 * of that class who is currently connected
	 * 
	 * @return
	 */
	public String getCName() {
		return this.cName;
	}

	public void setCName(String newCName) {
		this.cName = newCName;
	}

	public void setController(boolean isController) {
		this.controller = isController;
	}

	public boolean isController() {
		return this.controller;
	}

	public PlayerMode getMode() {
		return this.mode;
	}

	public void setMode(PlayerMode newMode) {
		this.mode = newMode;
	}

	public boolean isMoving() {
		return this.moving;
	}

	public void setMoving(boolean isMoving) {
		this.moving = isMoving;
	}

	public Point getDestination() {
		return this.destination;
	}

	public void setDestination(Point newDest) {
		this.destination = newDest;
	}

	public boolean hasEditor(final String name) {
		return editorsMap.containsKey(name);
	}

	public LinkedList<Spell> getSpellQueue() {
		return this.spellQueue;
	}

	public SpellBook getSpellBook() {
		return this.spells;
	}

	public int getLineLimit() {
		return this.lineLimit;
	}

	public void setLineLimit(int newLineLimit) {
		this.lineLimit = newLineLimit;
	}

	public State getState() {
		return this.state;
	}

	public void setState(State newState) {
		this.state = newState;
	}

	public void updateCurrentState() {

		final int hp = getHP();
		switch(getState()) {
		case ALIVE:
			if (hp <= -10) {
				setState(State.DEAD);
			}
			else if (hp <= 0) {
				setState(State.INCAPACITATED);
			}
			break;
		case INCAPACITATED:
			if ( hp > 0 ) {
				setState(State.ALIVE);
			}
			else if ( hp <= -10 ) {
				setState(State.DEAD);
			}
			break;
		case DEAD: // only resurrection spells or divine intervention can bring you back from the dead
		/*if ( hp > 0 ) {
                    setState(State.ALIVE);
                }
                else if ( hp > -10) {
                    setState(State.INCAPACITATED);
                }*/
			break;
		default:
			break;
		}
	}

	/**
	 * Check to see if the player's is that specified.
	 * 
	 * @param checkState
	 * @return
	 */
	public boolean isState(State checkState) {
		return this.state == checkState;
	}

	public Pager getPager() {
		return this.pager;
	}

	public void setPager(Pager newPager) {
		this.pager = newPager;
	}

	public Integer getNameRef(String key) {
		return this.nameRef.get(key);
	}

	public Set<String> getNameReferences() {
		return this.nameRef.keySet();
	}

	public void setNameRef(String key, Integer value) {
		this.nameRef.put(key, value);
	}

	public void clearNameRefs() {
		this.nameRef.clear();
	}

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
		Armor armor = (Armor) slots.get("armor").getItem();
		Shield shield = (Shield) slots.get("weapon1").getItem();

		if( armor != null && armor instanceof Armor ) {
			if( shield != null && shield instanceof Shield ) {
				return 10 + armor.getArmorBonus() + shield.getShieldBonus();
			}

			return 10 + armor.getArmorBonus();
		}
		else if( shield != null && shield instanceof Shield ) {
			return 10 + shield.getShieldBonus();
		}

		return 10;
	}

	public int getSpeed() {
		return this.speed;
	}

	public void setSpeed(int newSpeed) {
		this.speed = newSpeed;
	}

	public Alignments getAlignment() {
		return this.alignment;
	}

	public void setAlignment(Alignments alignment) {
		this.alignment = alignment;
	}

	public void setAlignment(int newAlignment) {
		this.alignment = Alignments.values()[newAlignment];
	}

	public Map<String, Boolean> getConfig() {
		return this.config;
	}

	public void setLastSpell(Spell last) {
		if( this.isCaster() ) {
			this.lastSpell = last;
		}
	}

	/**
	 * 
	 * @return
	 */
	public Spell getLastSpell() {
		if( this.isCaster() ) {
			return this.lastSpell;
		}
		else { return null; }
	}

	public boolean isNew() {
		return isNew;
	}
	
	public void wear(Wearable<Item> w) {
		List<Slot> sList = new LinkedList<Slot>();
		
		for(String s : this.slots.keySet()) {
			if( w.getType().toLowerCase().equals( s ) ) {
				sList.add( this.slots.get(s) );
			}
		}
		
		for(Slot slot : sList) {
			//if( slot.isEmpty() ) slot.insert( w );
		}
	}

	/**
	 * Translate the persistent aspects of the player into the string
	 * format used by the database
	 */
	public String toDB() {
		String[] output = new String[13];
		output[0] = getDBRef() + "";                      // database reference number
		output[1] = getName();                            // name
		output[2] = getFlagsAsString();                   // flags
		output[3] = getDesc();                            // description
		output[4] = getLocation() + "";                   // location
		output[5] = getPass();                            // password
		output[6] = stats.get(Abilities.STRENGTH) +       // stats
				"," + stats.get(Abilities.DEXTERITY) +
				"," + stats.get(Abilities.CONSTITUTION) +
				"," + stats.get(Abilities.INTELLIGENCE) +
				"," + stats.get(Abilities.WISDOM) +
				"," + stats.get(Abilities.CHARISMA);
		output[7] = getMoney().toString(false);           // money
		output[8] = access + "";                          // permissions level
		output[9] = race.getId() + "";                    // race
		output[10] = pclass.getId() + "";                 // class
		output[11] = status;                              // status
		output[12] = state.ordinal() + "";                // ALIVE/INCAPACITATED/DEAD
		output[13] = pstatus.ordinal() + "";              // ACTIVE/BANNED
		return Utils.join(output, "#");
	}

	@Override
	public String toJSON() {
		return null;
	}
}