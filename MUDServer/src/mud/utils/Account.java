package mud.utils;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import mud.net.Client;
import mud.objects.Player;

//public class MUDAccount
public class Account implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	 *  
	 *  ex.
	 *  nathan.acct (id 5)
	 *  2356.pfile
	 *  000005->2356
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
	public static enum Status { ACTIVE, INACTIVE, SUSPENDED, FROZEN, LOCKED, ARCHIVED };
	
	private static Calendar calendar;
	
	// passive properties (might be modified, but not frequently)
	private final Date created; // creation date
	private Date modified;      // modification date (when any of these passive properties were last modified)
	private Date archived;      // archival date (null, unless account was archived; if unarchived, then when it was last archived)
	
	private final int id;    // id
	private Status status;   // status
	private String username; // username
	private String password; // password
	
	private int charLimit = 3;     // character limit
	
	private boolean disabled = false;
	
	private String lastIPAddress;

	// active properties (current state)
	private transient ArrayList<Player> characters; // all the characters that exist for an account
	
	private transient Client client;  // the client object for the the player that is in-game
	private transient Player player;  // the in-game player
	
	private transient boolean online; // is there a Player in-game from this account/is the account logged in
	
	/**
	 * 
	 * @param aId
	 */
	public Account(int aId) {
		Account.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));

		this.id = aId;
		this.status = Status.ACTIVE;
		
		final Date newDate = new Date(Account.calendar.get(Calendar.MONTH), Account.calendar.get(Calendar.DATE), Account.calendar.get(Calendar.YEAR));

		this.created = new Date(newDate);
		this.modified = new Date(newDate);
	}

	/**
	 * 
	 * @param aId
	 */
	public Account(int aId, String aUsername, String aPassword, int aCharLimit) {
		this(aId);

		this.username = aUsername;
		this.password = aPassword;
		
		this.charLimit = aCharLimit;
		this.characters = new ArrayList<Player>(aCharLimit);
	}

	/**
	 * Account Loading constructor...
	 * @param aId
	 * @param aStatus
	 * @param aCreated
	 * @param aModified
	 * @param aUsername
	 * @param aPassword
	 * @param aCharLimit
	 * @param aCharacters
	 */
	public Account(int aId, Status aStatus, Date aCreated, Date aModified, String aUsername, String aPassword, int aCharLimit, Player...aCharacters) {
		this.id = aId;             // the account id
		this.status = aStatus;     // the status of the account

		this.created = aCreated;   // account's creation date
		this.modified = aModified; // account's last modification date

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
	 * defensive copying used
	 * 
	 * @return
	 */
	public Date getCreated() {
		return new Date(this.created);
	}
	
	public void setModified(Date modDate) {
		this.modified = modDate;
	}
	
	/**
	 * 
	 * defensive copying used
	 * 
	 * @return
	 */
	public Date getModified() {
		return new Date(this.modified);
	}
	
	public void setArchived(Date arcDate) {
		this.archived = arcDate;
	}
	
	/**
	 * 
	 * defensive copying used
	 * 
	 * @return
	 */
	public Date getArchived() {
		return new Date(this.archived);
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
		
		Account.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
		final Date newDate = new Date(Account.calendar.get(Calendar.MONTH), Account.calendar.get(Calendar.DATE), Account.calendar.get(Calendar.YEAR));
		setModified(newDate);
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
		
		Account.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
		final Date newDate = new Date(Account.calendar.get(Calendar.MONTH), Account.calendar.get(Calendar.DATE), Account.calendar.get(Calendar.YEAR));
		setModified(newDate);
	}

	/**
	 * Get account password
	 * 
	 * @return
	 */
	public String getPassword() {
		return this.password;
	}
	
	public Integer getCharLimit() {
		return this.charLimit;
	}

	/**
	 * Link an existing character to this account
	 * 
	 * @param newCharacter the character to link
	 */
	public boolean linkCharacter(Player newCharacter) {
		if( this.characters.size() < charLimit ) {
			//this.playerIds.add(newCharacter.getDBRef());
			return this.characters.add(newCharacter);
		}

		return false;
	}

	/**
	 * Unlink an existing character currently tied to this account
	 * 
	 * @param currCharacter the character to unlink
	 */
	public boolean unlinkCharacter(Player currCharacter) {
		if( this.characters.contains(currCharacter) ) {
			//this.playerIds.remove(currCharacter.getDBRef());
			return this.characters.remove(currCharacter);
		}

		return false;
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

	public List<Player> getCharacters() {
		return Collections.unmodifiableList(this.characters);
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
	
	public void setLastIPAddress(final String ipAddress) {
		this.lastIPAddress = ipAddress;
	}
	
	public String getLastIPAddress() {
		return this.lastIPAddress;
	}
	
	public boolean isDisabled() {
		return this.disabled;
	}

	public String display() {
		String username = Utils.padRight(getUsername(), 8);
		String id = Utils.padRight(String.valueOf(getId()), 6);
		String name;
		if (player != null) { name = Utils.padRight(player.getName(), 40); }
		else { name = Utils.padRight("- No Player -", 40); };
		String state = Utils.padRight(isOnline(), 6);
		String creationDate = Utils.padRight(created.toString(), 10);

		return username + " " + id + " " + name + " " + state + " " + creationDate;
	}

	public static void main(String[] args) {
		Account b = new Account(5);
		Account c = new Account(123);
		System.out.println("Id: " + b.id);
		System.out.println("Created " + b.created);
		System.out.println("Modified " + b.modified);
		System.out.println("Id: " + c.id);
		System.out.println("Created " + c.created);
		System.out.println("Modified " + c.modified);
	}
}