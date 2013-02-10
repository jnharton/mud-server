package mud.utils;

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

	private int id;      // an internal id for the bank account
	private int[] money; // a four slot array to hold money (these represent denominations
	
	/**
	 * 
	 * @param id
	 */
	public BankAccount(int id) {
		this.id = id;
		this.money = new int[] { 0, 0, 0, 0 };
	}
	
	/**
	 * 
	 * @param id
	 * @param newMoney
	 */
	public BankAccount(int id, int[] newMoney) 
	{
		this.id = id;
		this.money = new int[] { 0, 0, 0, 0 };
		if (newMoney.length <= 4) {
			for (int i = 0; i < this.money.length; i++) {
				this.money[i] = newMoney[i];
			}
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
	 * 
	 * @return the internal representation of the money
	 */
	public int[] getBalance() {
		/*
		 * possibly insecure, if we can modify it after
		 * being returned and actually change the one in the bank
		 */
		return this.money;
	}
	
	/**
	 * 
	 * @param deposit
	 */
	public void deposit(int[] deposit) {
		if (deposit.length >= 4) {
			for (int i = 0; i < this.money.length; i++) {
				this.money[i] += deposit[i];
			}
		}
	}
	
	/**
	 * 
	 * @param withdrawal
	 * @return
	 */
	public int[] withdraw(int[] withdrawal) {
		int[] n = new int[4];
		if (withdrawal.length >= 4) {
			for (int i = 0; i < this.money.length; i++) {
				if (withdrawal[i] < this.money[i]) {
					this.money[i] -= withdrawal[i];
					n[i] += withdrawal[i];
				}
			}
			return n;
		}
		return null;
	}
}