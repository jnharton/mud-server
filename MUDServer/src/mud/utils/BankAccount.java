package mud.utils;

import mud.misc.Coins;

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

/**
 * BankAccount
 * 
 * A class designed to represent a simple bank account
 * which has an id number, and stored numbers representing
 * money in the bank.
 * 
 * NOTE: This design is this way because it is intended for use
 * in the MUDServer and thusly represents real midieval-esque
 * money which could be reduced to a single denomination representation,
 * but is not because it would necessitate a translation step between
 * the actual money the player has and the way it's stored in the bank.
 * That is not desired, and is thus avoided.
 * 
 * @author Jeremy
 *
 */
public class BankAccount {

	private int id;        // an internal id for the bank account
	private Coins balance; // a Coins object to hold money
	
	/**
	 * 
	 * @param id
	 */
	public BankAccount(int id) {
		this(id, new Coins(new int[] { 0, 0, 0, 0 }));
	}
	
	/**
	 * 
	 * @param id
	 * @param newMoney
	 */
	public BankAccount(final int id, final Coins newMoney) 
	{
		this.id = id;
		this.balance = Coins.copper( newMoney.numOfCopper() );
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
	 * 
	 * @return the internal representation of the money
	 */
	public Coins getBalance() {
		return Coins.copper( this.balance.numOfCopper() );
	}
	
	/**
	 * 
	 * @param deposit
	 */
	public void deposit(final Coins deposit) {
		this.balance = this.balance.add( deposit );
	}
	
	/**
	 * 
	 * @param withdrawal
	 * @return
	 */
	public Coins withdraw(final Coins withdrawal) {
		this.balance = this.balance.subtract(withdrawal);
		return withdrawal;
	}
}