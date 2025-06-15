package mud.utils;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.util.LinkedHashMap;

import mud.game.Coins;

public class Bank {
	
	private String name;
	private LinkedHashMap<Integer, BankAccount> accounts;
	
	private int nextId = 0;
	
	public Bank(final String bankName) {
		this.name = bankName;
		this.accounts = new LinkedHashMap<Integer, BankAccount>(1, 0.75f);
	}
	
	public String getName() {
		return this.name;
	}
	
	/**
	 * This function is for the purpose of loading BankAccount objects into this bank's
	 * accounts collection. It should be used for loading BankAccount objects recovered
	 * from persistent data when loading the MUDServer game data.
	 *  
	 * @param id
	 * @param account
	 * @return was the account added successfully? (true/false)
	 */
	public boolean addAcount( final int id, final BankAccount account ) {
		if( !this.accounts.containsKey(id) ) {
			this.accounts.put( id, account );
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 */
	public void openAccount() {
		final int id = nextId++;
		this.accounts.put( id, new BankAccount( id ) );
	}
	
	public BankAccount getAccount(int acctId) {
		return this.accounts.get(acctId);
	}
	
	/**
	 * 
	 * @param id
	 */
	public void closeAccount(final int id) {
		this.accounts.remove(id);
	}
	
	/**
	 * 
	 * @param id
	 * @param deposit
	 */
	public void deposit(int id, Coins deposit) {
		BankAccount account = accounts.get(id);
		
		if( account != null ) {
			account.deposit(deposit);
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param withdrawal
	 * @return
	 */
	public Coins withdraw(int id, Coins withdrawal) {
		BankAccount account = accounts.get(id);
		
		if( account != null ) {
			return account.withdraw(withdrawal);
		}
		
		return null;
	}
}