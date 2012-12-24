package mud.interfaces;

import mud.Currency;

public interface Banker extends Interactive {
	public int[] withdraw(Currency currency, int amount);
	
	public int[] withdraw(int[] money);
	
	public void deposit(int[] money);
}