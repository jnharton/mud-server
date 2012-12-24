package mud.objects;

import mud.Currency;
import mud.net.Client;
import mud.utils.Bank;

public class Banker extends NPC implements mud.interfaces.Banker {
	
	private Bank bank;
	
	public Banker(int tempDBRef, String tempName, String tempPass, String tempFlags, String tempDesc, String tempTitle, String tempPStatus, int tempLoc, String[] tempMoney) {
		super(tempDBRef, tempName, tempPass, tempFlags, tempDesc, tempTitle, tempPStatus, tempLoc, tempMoney);
	}
	
	@Override
	public void interact(Client client) {
		// TODO Auto-generated method stub
	}

	
	public int[] withdraw(Currency currency, int amount) {
		return new int[]{};
	}
	
	public void deposit(int[] money) {
	}

	@Override
	public int[] withdraw(int[] money) {
		// TODO Auto-generated method stub
		return null;
	}
}