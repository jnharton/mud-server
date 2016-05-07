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
	public static enum Status { ACTIVE, INACTIVE, SUSPENDED, FROZEN, LOCKED, ARCHIVED };
	/*
	 * Active    - normal state
	 * Inactive  - haven't been played recently, flagged as inactive
	 * Suspended - temporarily banned or suspended for the time being for behavioral infractions
	 * Frozen    - permanently banned and not yet purged (PURGE)
	 * Locked    - locked out and cannot be logged into (for instance, in the case of a hacked account)
	 * Archived  - archived after 3-6 months of being inactive (inactive timer reset whenever a successful login occurs)
	 */
	
	/* passive properties (might be modified, but not frequently) */
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

	/* active properties (current state) */
	private transient ArrayList<Player> characters; // all the characters that exist for an account
	
	private transient Client client;  // the client object for the the player that is in-game
	private transient Player player;  // the in-game player
	
	private transient boolean online; // is there a Player in-game from this account/is the account logged in
	
	/**
	 * 
	 * @param aId
	 */
	public Account(int aId) {
		this.id = aId;
		this.status = Status.ACTIVE;
		
		final Date newDate = Account.getDate();

		this.created = new Date(newDate);
		this.modified = new Date(newDate);
		this.archived = null;
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
	 * 
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
		this.archived = null;

		this.username = aUsername;
		this.password = aPassword;

		this.charLimit = aCharLimit;
		this.characters = new ArrayList<Player>(aCharacters.length);

		for (Player player : aCharacters) {
			this.characters.add(player);
		}
	}
	
	/**
	 * get the date this account was created
	 * 
	 * @return
	 */
	public Date getCreated() {
		// defensive copying
		return new Date(this.created);
	}
	
	/**
	 * get the date this account was last modified
	 * 
	 * @return
	 */
	public Date getModified() {
		// defensive copying
		return new Date(this.modified);
	}
	
	/**
	 * set the date this account was last modified
	 * 
	 * @param modDate
	 */
	private void setModified(final Date modDate) {
		this.modified = modDate;
	}
	
	/**
	 * get the account archived date
	 * 
	 * @return Date
	 */
	public Date getArchived() {
		// defensive copy
		return new Date(this.archived);
	}
	
	/**
	 * set the account archived date
	 * 
	 * @param archiveDate
	 */
	private void setArchived(final Date archiveDate) {
		this.archived = archiveDate;
	}

	/**
	 * get the account id
	 * 
	 * @return int
	 */
	public int getId() {
		return this.id;
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
	 * Set account status
	 * 
	 * @param newStatus the status to change the account status too
	 */
	public void setStatus(final Status newStatus) {
		this.status = newStatus;
		
		setModified( Account.getDate() );
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
	 * Get account password
	 * 
	 * @return
	 */
	public String getPassword() {
		return this.password;
	}
	
	/**
	 * Set account password
	 * 
	 * @param newPassword the password to set on the account 
	 */
	public void setPassword(final String newPassword) {
		this.password = newPassword;
		
		setModified( Account.getDate() );
	}
	
	public Integer getCharLimit() {
		return this.charLimit;
	}
	
	public void setCharLimit(final Integer newCharLimit) {
		this.charLimit = newCharLimit;
		
		setModified( Account.getDate() );
	}
	
	public void disable() {
		this.disabled = true;
	}
	
	public void enable() {
		this.disabled = false;
	}
	
	public boolean isDisabled() {
		return this.disabled;
	}
	
	public String getLastIPAddress() {
		return this.lastIPAddress;
	}
	
	/*public void setLastIPAddress(final String ipAddress) {
		this.lastIPAddress = ipAddress;
	}*/

	/**
	 * Link an existing character to this account
	 * 
	 * @param newCharacter the character to link
	 */
	public boolean linkCharacter(final Player newCharacter) {
		boolean success = false;
		
		if( this.characters.size() < charLimit ) {
			//this.playerIds.add(newCharacter.getDBRef());
			success = this.characters.add(newCharacter);
			
			if( success ) {
				setModified( Account.getDate() );
			}
		}

		return success;
	}

	/**
	 * Unlink an existing character currently tied to this account
	 * 
	 * @param currCharacter the character to unlink
	 */
	public boolean unlinkCharacter(final Player currCharacter) {
		boolean success = false;
		
		if( this.characters.contains(currCharacter) ) {
			//this.playerIds.remove(currCharacter.getDBRef());
			success = this.characters.remove(currCharacter);
			
			if( success ) {
				setModified( Account.getDate() );
			}
		}

		return success;
	}
	
	public List<Player> getCharacters() {
		return Collections.unmodifiableList(this.characters);
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
	 * @param tClient
	 */
	public void setClient(final Client newClient) {
		if( newClient == null ) this.lastIPAddress = this.client.getInput();
		
		this.client = newClient;
	}
	
	/**
	 * 
	 * @return
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * 
	 * @param tPlayer
	 */
	public void setPlayer(final Player newPlayer) {
		this.player = newPlayer;
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
		
		if (player != null) name = Utils.padRight(player.getName(), 40);
		else                name = Utils.padRight("- No Player -", 40);
		
		String state = Utils.padRight(isOnline(), 6);
		String creationDate = Utils.padRight(created.toString(), 10);

		return username + " " + id + " " + name + " " + state + " " + creationDate;
	}
	
	private static Date getDate() {
		// TODO should I get the timezone and local as separate variables first? should they use the default?
		
		//final Calendar calendar = Calendar.getInstance(tz, lc);
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
		
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		int year = calendar.get(Calendar.YEAR);
				
		return new Date(month, day, year);
	}
}