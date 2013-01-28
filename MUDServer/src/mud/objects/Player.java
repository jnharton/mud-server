package mud.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import mud.Abilities;
import mud.Classes;
import mud.Editor;
import mud.MUDObject;
import mud.MUDServer.State;
import mud.Point;
import mud.Races;
import mud.Races.Subraces;
import mud.Skill;
import mud.Skills;
import mud.Slot;
import mud.SlotType;

import mud.MUDServer.PlayerMode;

import mud.interfaces.Equippable;
import mud.magic.Spell;
import mud.magic.SpellBook;
import mud.objects.items.ClothingType;
import mud.objects.items.Handed;

import mud.quest.Quest;
import mud.quest.Task;
import mud.quest.TaskType;


// mud.utils
import mud.utils.MailBox;
import mud.utils.Pager;
import mud.utils.cgData;
import mud.utils.Utils;
import mud.utils.edData;


//Player Class
// need to weed out usage of send() and debugP() since they make
// this class dependent on the existence of my code to work
// in particular in the functions: look(), examine(), equip(...), and unequip(...)
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

	// Default Player Data
	/*private final String start_flags = "P";                       // default flag string
	private final String start_status = "NEW";                    // default status string
	private final String start_desc = "There is nothing to see."; // default desc string
	private final Integer start_room = 9;                         // default starting room
	private final Integer[] start_stats = { 0, 0, 0, 0, 0, 0 };   // default stats
	private final Integer[] start_money = { 0, 0, 0, 0 };         // default money*/
		
	// levels: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
	private static int[] levelXP = { 0, 1000, 3000, 6000, 10000, 15000, 21000, 28000, 36000, 45000, 55000, 66000 };
	
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
	protected ArrayList<String> names;                // names of other players that the player actually knows
	private MailBox mailbox;                          // player mailbox

	protected int access = 0;                         // the player's access level (permissions) - 0=player,1=admin (default: 0)

	private boolean controller = false;               // place to indicate if we are controlling an npc (by default, we are not)
	private HashMap<String, ArrayList<String>> lists; // array of lists belonging to this player

	// preferences (ACCOUNT DATA?)
	private int lineLimit = 80;                       // how wide the client's screen is in columns (shouldn't be in Player class)
	public int invDispWidth = 60;                     // display width for the complex version of inventory display
	private Character invType = 'C';                  // S = simple, C = Complex (candidate for being a config option, not a single variable)
	private LinkedHashMap<String, String> config;     // player preferences for player configurable options
	
	// utility
	private HashMap<String, Integer> nameRef;         // store name references to dbref numbers (i.e. $this->49)

	// Editors, General
	private Editor editor;
	
	/* Editor Data */ 
	
	// Character Editor
	private cgData cgd = null;

	// List Editor
	public String listname;                 // the name of the current list being edited
	public ArrayList<String> nlist;         // contents of list being edited
	public int nline;                       // current line in editor
	public int nsize;                       // space/size (used) in current list

	// Miscellaneous Editor
	private edData edd = null;

	// Game Stuff (most set 'protected' so that an npc can basically have player characteristics
	protected MUDObject target;             // Target -- player/npc that will be used for generic interaction
	protected Races race;                   // Race
	protected Character gender;             // Gender
	protected Classes pclass;               // Class
	protected Handed handed = Handed.RIGHT; // which hand is dominant (irr. but enum encompasses that and weapons hand req.)
	protected int hp;                       // Hit Points
	protected int totalhp;                  // Total Hit Points
	protected int mana;                     // Mana
	protected int totalmana;                // Total Mana
	protected int speed;                    // Movement Speed (largely pointless without a coordinate system)
	protected int capacity;                 // Carrying Capacity (pounds/lbs)
	protected Integer level;                // Level
	protected int xp;                       // Experience
	protected Integer[] money;              // Money (D&D, MUD)

	protected SpellBook spells;             // spells [null if not a wizard]
	public LinkedList<Spell> spellQueue;    // spell queue [null if not a wizard]

	protected State state = State.ALIVE;    // character's "state of health" (ALIVE, INCAPACITATED, DEAD)

	protected LinkedHashMap<Abilities, Integer> stats;             // Player Statistics (D&D, MUD)
	protected LinkedHashMap<Skill, Integer> skills;                // Player Skills (D&D, MUD)

	protected ArrayList<Item> inventory = new ArrayList<Item>(10); // Player Inventory (D&D, MUD, MU)
	protected LinkedHashMap<String, Slot> slots;                   // the player's equipped gear

	private ArrayList<Quest> quests;                               // the player's quests

	// movement
	protected boolean moving;
	protected Point destination;

	// leveling up
	public boolean levelup = false;         // is this player ready to "level up" (true=yes,false=no)
	private int featPts;                     // points available for selecting new feats (unused)
	//private int skillPts; // points available for increasing skills (unused)
	//*in some ways i'd rather not assign skill points for leveling up, but i also don't like classless system
	//*i'm thinking that feats make sense at a level, but gaining skills ought to be by what you use the most (hence, 'acquiring' the skill)
	
	private int[] statMod = new int[] { 0, 0, 0, 0, 0, 0 }; // current modifications to stats (i.e. stat drains, etc)
	
	// borrowed from DIKU -> ROM, etc?
	// h - hitpoints, H - max hitpoints
	// mv - moves, MV - total moves
	// m - mana, M - total mana
	private String custom_prompt = "< %h/%H  %mv/%MV %m/%M >"; // ACCOUNT DATA?

	private Pager pager = null; // a pager (ex. 'less' for linux), displays a page's/screen's worth of text at a time

	/**
	 * No argument constructor for subclasses
	 * 
	 * NOTE: subclasses must initialize the members they wish to use,
	 * however they can not initialize private members of this class.
	 * 
	 */
	public Player() {}

	public Player(int tempDBREF) { super(tempDBREF); }

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

	public Player(int tempDBREF, String tempName, String tempFlags, String tempDesc, int tempLoc, String tempTitle, String tempPass, String tempPStatus , Integer[] tempStats, Integer[] tempMoney)
	{
		// use the MUDObject constructor to handle some of the construction?
		//super(tempDBREF, tempName, tempFlags, tempDesc, tempLoc);
		super(tempDBREF);

		this.race = Races.NONE;
		this.gender = 'N';
		this.pclass = Classes.NONE;
		this.pclass = Classes.NONE;

		this.hp = 10;
		this.totalhp = 10;
		this.mana = 40;
		this.totalmana = 40;
		this.speed = 0;
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
		this.slots.put("helmet", new Slot(SlotType.HEAD, ItemType.HELMET));
		this.slots.put("necklace", new Slot(SlotType.NECK, ItemType.NECKLACE));
		this.slots.put("armor", new Slot(SlotType.BODY, ItemType.ARMOR));
		this.slots.put("cloak", new Slot(SlotType.BODY, ClothingType.CLOAK));
		this.slots.put("ring1", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring2", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring3", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring4", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring5", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring6", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring7", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring8", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring9", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("ring10", new Slot(SlotType.FINGER, ItemType.RING));
		this.slots.put("gloves", new Slot(SlotType.HANDS, ClothingType.GLOVES));
		this.slots.put("weapon", new Slot(SlotType.RHAND, ItemType.WEAPON));
		this.slots.put("weapon1", new Slot(SlotType.LHAND, ItemType.WEAPON));
		this.slots.put("belt", new Slot(SlotType.WAIST, ClothingType.BELT));;
		this.slots.put("boots", new Slot(SlotType.FEET, ClothingType.BOOTS));

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

		// add an initial quest?
		this.quests.add(new Quest("Test", "A basic quest for testing purposes", new Task("Obtain dominion jewel", TaskType.RETRIEVE)));

		// instantiate mailbox
		this.mailbox = new MailBox();

		// instantiate list of known names (memory - names)
		this.names = new ArrayList<String>(); // we get a new blank list this way, not a loaded state

		// initialize list editor variables
		this.lists = new HashMap<String, ArrayList<String>>(1, 0.75f); // need to be loading saved lists
		this.editor = Editor.NONE;
		this.nlist = null;
		this.nline = 0;

		// instantiate name reference table
		this.nameRef = new HashMap<String, Integer>(10, 0.75f); // start out assuming 10 name references
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
	public int getAccess() { return this.access; }

	/**
	 * Set Access
	 * 
	 * @param newAccessLevel integer representing a level of permissions
	 */
	public void setAccess(int newAccessLevel) { this.access = newAccessLevel; }

	/**
	 * Get Player Class
	 * 
	 * @return a Classes object that represents the player's character class
	 */
	public Classes getPClass() { return this.pclass; }

	/**
	 * Set Player Class
	 * 
	 * @param playerClass the character class to set on the player
	 */
	public void setPClass(Classes playerClass) { this.pclass = playerClass; }

	public Races getPlayerRace() { return this.race; }

	public void setPlayerRace(Races race) { this.race = race; }

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
	public Integer[] getMoney() {
		return this.money;
	}

	public int getMoney(int index) {
		return this.money[index];
	}

	// none of this handles player weight, etc
	// if I want to control money by how much weight you can carry
	// then I need to determine a standard weight for the money
	// and calculate that, then decide if the player can hold it
	public void setMoney(int type, int amount) {
		this.money[type] += amount;
	}
	
	public void setMoney(int[] money) {
		for (int i = 0; i < 4; i++) {
			setMoney(i, money[i]);
		}
	}
	
	public void setMoney(Integer[] money) {
		for (int i = 0; i < 4; i++) {
			setMoney(i, money[i]);
		}
	}

	/**
	 * Get and return the player's current level
	 * 
	 * @return
	 */
	public Integer getLevel() { return this.level; }
	
	/**
	 * Set the player's level to a new level.
	 * 
	 * @param changeLevel
	 */
	public void setLevel(Integer changeLevel) {
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
		return stats.get(ability) + statMod[ability.ordinal()];
	}
	
	public int getAbility(String abilityName) {
		return stats.get(abilityName);
	}

	public int getSkill(Skill skill) {
		return skills.get(skill);
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
		this.editor = editor;
	}

	public edData getEditorData() {
		return this.edd;
	}

	public void setEditorData(edData newEdD) {
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

	public HashMap<String, ArrayList<String>> getLists() {
		return this.lists;
	}

	public LinkedList<Spell> getSpellQueue() {
		return this.spellQueue;
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
	
	/**
	 * Translate the persistent aspects of the player into the string
	 * format used by the database
	 */
	public String toDB() {
		String[] output = new String[11];
		output[0] = this.getDBRef() + "";           // player database reference number
		output[1] = this.getName();                       // player name
		output[2] = this.getFlags();                      // player flags
		output[3] = this.getDesc();                       // player description
		output[4] = this.getLocation() + "";        // player location
		output[5] = this.getPass();                       // player password
		output[6] = stats.get(Abilities.STRENGTH) +
				"," + stats.get(Abilities.DEXTERITY) +
				"," + stats.get(Abilities.CONSTITUTION) +
				"," + stats.get(Abilities.INTELLIGENCE) +
				"," + stats.get(Abilities.WISDOM) +
				"," + stats.get(Abilities.CHARISMA);
		output[7] = "" + this.getMoney(0) + "," + this.getMoney(1) + "," + this.getMoney(2) + "," + this.getMoney(3); // player money
		output[8] = this.access + "";               // player permissions level
		output[9] = race.getId() + "";              // player race
		output[10] = pclass.getId() + "";           // player class
		return Utils.join(output, "#");
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
}