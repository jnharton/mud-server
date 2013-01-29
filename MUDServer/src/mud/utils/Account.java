package mud.utils;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import mud.net.Client;

import mud.objects.Player;

//public class MUDAccount
public class Account implements Serializable {

	/*
	 * class to represent a game account, which may contain
	 * one or more players/characters that are all tied to
	 * a single entity (person) who connects to the game via
	 * a client
	 * 
	 * are accounts all loaded into memory, accessed from disk, or loaded on demand?
	 * 
	 * "passive properties
	 * username - login username
	 * password - login password
	 * status   - active, inactive, frozen, locked, archived
	 * > active is normal, inactive after period of time, frozen for breaking certain rules/TOS, locked
	 * > for indefinite player absence, archived after a certain period of time
	 * age      - account age in days, months, and years
	 * 
	 * "active properties"
	 * current client object
	 * current active player
	 * 
	 * player storage/reference -- how do I associate players with an account
	 * 	player data will be stored as pfiles, an association will exist in a database between account ids and player ids
	 *  account->player
	 *  001234->2356
	 *  001234->3361
	 *  001234->4522
	 * when an account connects, certain information from these player files will be accessed and displayed
	 * upon choosing a character, that file will be loaded up
	 * 
	 */
	
	// static constants
	/*
	 * STATUS
	 * Active Accounts - normal state
	 * Inactive Accounts - haven't been played recently, flagged as inactive
	 * Suspended Accounts - temporarily banned or suspended for the time being for behavioral infractions
	 * Frozen Accounts - permanently banned and not yet purged (PURGE)
	 * Locked Accounts - accounts that are locked out and cannot be logged into (for instance, in the case of a hacked account)
	 * Archived Accounts - accounts archived after 3-6 months of being inactive (inactive timer reset whenever a successful login occurs),
	 * not usable until unarchived and restored to active status.
	 */
	private static enum Status { ACTIVE, INACTIVE, SUSPENDED, FROZEN, LOCKED, ARCHIVED };
	private static Calendar calendar;

	// passive properties (might be modified, but not frequently)
	public Date created;                  // creation date
	public Date modified;                 // modification date (when any of these passive properties were last modified)
	public Date archived;                 // archival date (null, unless account was archived; if unarchived, then when it was last archived)
	private int id;                       // id
	private Status status;                // status
	private String username;              // username
	private String password;              // password
	private int charLimit = 3;            // character limit
	private ArrayList<Player> characters; // all the characters that exist for an account

	// active properties (current state)
	transient private Client client;
	transient private Player player;
	
	transient private boolean online;

	/**
	 * 
	 */
	public Account() {
		Account.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
		
		this.id = 0;
		this.status = Status.ACTIVE;
		this.created = new Date(Account.calendar.get(Calendar.MONTH), Account.calendar.get(Calendar.DATE), Account.calendar.get(Calendar.YEAR));
	}
	
	/**
	 * 
	 * @param aId
	 */
	public Account(int aId) {
		Account.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
		
		this.created = new Date(Account.calendar.get(Calendar.MONTH), Account.calendar.get(Calendar.DATE), Account.calendar.get(Calendar.YEAR));
		this.modified = new Date(Account.calendar.get(Calendar.MONTH), Account.calendar.get(Calendar.DATE), Account.calendar.get(Calendar.YEAR));
		this.id = aId;
		this.status = Status.ACTIVE;
	}
	
	/**
	 * 
	 * @param aId
	 */
	public Account(int aId, String aUsername, String aPassword, int aCharLimit) {
		Account.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
		
		this.created = new Date(Account.calendar.get(Calendar.MONTH), Account.calendar.get(Calendar.DATE), Account.calendar.get(Calendar.YEAR));
		this.modified = new Date(Account.calendar.get(Calendar.MONTH), Account.calendar.get(Calendar.DATE), Account.calendar.get(Calendar.YEAR));
		this.id = aId;
		this.status = Status.ACTIVE;
		this.username = aUsername;
		this.password = aPassword;
		this.charLimit = aCharLimit;
		this.characters = new ArrayList<Player>(aCharLimit);
	}
	
	/**
	 * 
	 * @param aCreated
	 * @param aModified
	 * @param aId
	 * @param aStatus
	 * @param aUsername
	 * @param aPassword
	 * @param aCharLimit
	 * @param aCharacters
	 */
	public Account(Date aCreated, Date aModified, int aId, Status aStatus, String aUsername, String aPassword, int aCharLimit, Player...aCharacters) {
		this.created = aCreated;
		this.modified = aModified;
		this.id = aId;
		this.status = aStatus;
		this.username = aUsername;
		this.password = aPassword;
		this.charLimit = aCharLimit;
		this.characters = new ArrayList<Player>(aCharacters.length);
		for (Player player : aCharacters) {
			this.characters.add(player);
		}
	}
	
	/**
	 * 
	 * @param newId
	 */
	public void setId(int newId) {
		this.id = newId;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Set account status
	 * 
	 * @param newStatus the status to change the account status too
	 */
	public void setStatus(Status newStatus) {
		this.status = newStatus;
	}
	
	/**
	 * Get account status
	 * 
	 * @return the account status as an ordinal of the Status enum
	 */
	public Status getStatus() {
		return this.status;
	}

	/**
	 * Set username
	 * 
	 * @param tUsername the username to change this account to
	 */
	public void setUsername(String tUsername) {
		this.username = tUsername;
	}
	
	/**
	 * Get username
	 * 
	 * @return username string
	 */
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Set account password
	 * 
	 * @param newPassword the password to set on the account 
	 */
	public void setPassword(String newPassword) {
		this.password = newPassword;
	}
	
	/**
	 * Get account password
	 * 
	 * @return
	 */
	public String getPassword() {
		return this.password;
	}
	
	/**
	 * Link an existing character to this account
	 * 
	 * @param newCharacter the character to link
	 */
	public void linkCharacter(Player newCharacter) {
		this.characters.add(newCharacter);
	}
	
	/**
	 * Unlink an existing character currently tied to this account
	 * 
	 * @param curr Character the character to unlink
	 */
	public void unlinkCharacter(Player currCharacter) {
		this.characters.remove(currCharacter);
	}
	
	/**
	 * 
	 * @param tClient
	 */
	public void setClient(Client newClient) {
		this.client = newClient;
	}
	
	/**
	 * 
	 * @return
	 */
	public Client getClient() {
		return this.client;
	}
	
	/**
	 * 
	 * @param tPlayer
	 */
	public void setPlayer(Player newPlayer) {
		this.player = newPlayer;
	}
	
	/**
	 * 
	 * @return
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	public ArrayList<Player> getCharacters() {
		return this.characters;
	}
	
	public void setOnline(boolean online) {
		this.online = online;
	}
	
	/*public boolean isOnline() {
		return online;
	}*/
	
	public String isOnline() {
		if( online ) {
			return "Yes";
		}
		else {
			return "No";
		}
	}
	
	public String display() {
		String username = Utils.padRight(getUsername(), 8);
		String id = Utils.padRight(String.valueOf(getId()), 6);
		String name;
		if (player != null) { name = Utils.padRight(player.getName(), 40); }
		else { name = Utils.padRight("null", 40); };
		String state = Utils.padRight(isOnline(), 6);
		String creationDate = Utils.padRight(created.toString(), 10);
		
		return username + " " + id + " " + name + " " + state + " " + creationDate;
	}
	
	/**
	 * 
	 */
	public String toString() {
		return "";
	}
	
	public static void main(String[] args) {
		Account a = new Account();
		Account b = new Account(5);
		Account c = new Account(00123);
		System.out.println("Id: " + a.id);
		System.out.println("Created " + a.created);
		System.out.println("Id: " + b.id);
		System.out.println("Created " + b.created);
		System.out.println("Id: " + c.id);
		System.out.println("Created " + c.created);
		
	}
}