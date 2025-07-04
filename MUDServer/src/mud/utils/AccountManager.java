package mud.utils;

/*
 * Copyright (c) 2014 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: If the text of LICENSE.txt differs from the web site referenced, then
 * the license distributed with the source is the one that must be followed.
 */

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import mud.objects.Player;

public final class AccountManager {
	private Integer last_account_id = -1;
	
	private final Map<Integer, Account> iamap; // integer account map
	
	public AccountManager() {
		this.iamap = new Hashtable<Integer, Account>();
	}
	
	public int nextId() {
		return ++(this.last_account_id);
	}
	
	public Account addAccount(final String name, final String password, final int char_limit) {
		int aId = nextId();
		Account account = new Account(aId, name, password, char_limit);
		
		//this.iamap.put( aId, new Account(aId, name, password, char_limit) );
		this.iamap.put( aId, account );
		
		return account;
	}
	
	public void addAccount(final Account account) {
		// if we don't already have an account with that ID
		if( !this.iamap.containsKey( account.getId() ) ) {
			this.iamap.put( account.getId(), account );
			
			// if this account has an id later than our current last, update that
			if( account.getId() > this.last_account_id ) {
				this.last_account_id = account.getId();
			}
		}
	}
	
	
	// TODO these functions duplicate effort...
	public void removeAccount(final Account toRemove) {
		this.iamap.remove(toRemove.getId());
	}
	
	public void removeAccount(final Integer id) {
		final Account toRemove = this.iamap.get(id);
		
		removeAccount(toRemove);
	}
	
	/**
	 * Get Account
	 * 
	 * retrieve the Account from iamap which has exactly the Id (integer value) provided.
	 * @param accountId
	 * @return
	 */
	public Account getAccount(int accountId) {
		// TODO this may not work given that IAMAP uses Integer(s)
		//return iamap.get(accountId);
		Account a = null;
		
		for(final Account account : iamap.values()) {
			if( account.getId() == accountId ) {
				a = account;
				break;
			}
		}
		
		return a;
	}
	
	/**
	 * Get Account
	 * 
	 * retrieve the Account from iamap which has the exact username provided
	 * 
	 * @param name
	 * @return
	 */
	public Account getAccount(final String name) {
		Account a = null;
		
		for (final Account account : iamap.values()) {
			if (account.getUsername().equals(name) ) {
				a = account;
				break;
			}
		}
		
		return a;
	}
	
	/**
	 * Get Account
	 * 
	 * retrieve the Account from iamap to which the specified Player has been linked, if there is one
	 * 
	 * @param player
	 * @return
	 */
	public Account getAccount(final Player player) {
		Account a = null;
		
		// TODO properly fix this, without kludging
		for(final Account account : iamap.values()) {
			if( account.getCharacters().contains(player) ) {
				a = account;
				break;
			}
		}
		
		return a;
	}
	
	public Account getAccount(final String name, final String pass) {
		Account a = null;
		
		
		System.out.println( String.format("Name: %s Password: %s", Utils.padRight("'" + name + "'", 14), Utils.padRight("'" + pass + "'", 18)) );
		
		for (final Account account : iamap.values()) {
			System.out.println(account.getId());
			System.out.println(account.getUsername());
			System.out.println(account.getPassword());
			
			// TODO should we be using a hash call here?
			//if (account.getUsername().equals(name) && account.getPassword().equals( Utils.hash(pass) )) {
			if (account.getUsername().equals(name) && account.getPassword().equals(pass)) {
				a = account;
				System.out.println("Match!");
				break;
			}
			else {
				System.out.println("No Match!");
			}
		}
		
		return a;
	}
	
	public Collection<Account> getAccounts() {
		return Collections.unmodifiableCollection(iamap.values());
	}
	
	public int numAccounts() {
		return this.iamap.size();
	}
}