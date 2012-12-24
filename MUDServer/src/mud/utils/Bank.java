package mud.utils;

import java.util.HashMap;

public class Bank {
	
	HashMap<Integer, BankAccount> accounts;
	
	Bank() {
		accounts = new HashMap<Integer, BankAccount>(1, 0.75f);
	}
	
	public void deposit(int id, int[] deposit) {
		BankAccount account = accounts.get(id);
		account.deposit(deposit);
	}
	
	public int[] withdraw(int id, int[] withdrawal) {
		BankAccount account = accounts.get(id);
		return account.withdraw(withdrawal);
	}

}