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

import mud.MUDServer;
import mud.objects.Player;

public final class AccountManager {
	private Integer last_account_id = -1;
	
	private MUDServer parent;
	
	private final Hashtable<Integer, Account> iamap;
	private final Hashtable<Player, Account> pamap;
	
	public AccountManager(MUDServer ms) {
		this.parent = ms;
		this.iamap = new Hashtable<Integer, Account>();
		this.pamap = new Hashtable<Player, Account>();
	}
	
	public void addAccount(final String name, final String password, final int char_limit) {
		int aId = nextId();
		iamap.put( aId, new Account(aId, name, password, char_limit) );
	}
	
	public void addAccount(Account account) {
		if( !iamap.containsKey(account.getId()) ) {
			iamap.put( account.getId(), account );
			
			/*for(Player p : account.getCharacters()) {
				pamap.put(p, account);
			}*/
			
			if( account.getId() > last_account_id ) last_account_id = account.getId();
		}
	}

	public void removeAccount() {
	}

	public Account getAccount(int accountId) {
		return iamap.get(accountId);
	}

	public Account getAccount(Player player) {
		// TODO properly fix this, without kludging
		// kludge
		for(Account account : iamap.values()) {
			if( account.getCharacters().contains(player) ) {
				return account;
			}
		}
		
		return null;
		//return pamap.get(player);
	}
	
	public Account getAccount(final String name, final String pass) {
		for (final Account account : iamap.values()) {
			if (account.getUsername().equals(name) && account.getPassword().equals(pass)) {
				return account;
			}
		}

		return null;
	}
	
	public Collection<Account> getAccounts() {
		return Collections.unmodifiableCollection(iamap.values());
	}
	
	public int numAccounts() {
		return this.iamap.size();
	}
	
	public int nextId() {
		return ++last_account_id;
	}
}