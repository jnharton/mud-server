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

import mud.objects.Player;

public final class AccountManager {
	private Integer last_account_id = -1;
	
	private final Hashtable<Integer, Account> iamap; // integer account map
	
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
	
	public void removeAccount(final Account toRemove) {
		this.iamap.remove(toRemove);
	}
	
	public void removeAccount(final Integer id) {
		final Account toRemove = this.iamap.get(id);
		
		removeAccount(toRemove);
	}

	public Account getAccount(int accountId) {
		return iamap.get(accountId);
	}

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
		
		for (final Account account : iamap.values()) {
			if (account.getUsername().equals(name) && account.getPassword().equals(pass)) {
				a = account;
				break;
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