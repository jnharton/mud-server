package mud.objects;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.Currency;
import mud.net.Client;
import mud.utils.Bank;

public class Banker extends NPC implements mud.interfaces.Banker {
	
	private Bank bank;
	
	public Banker(final int tempDBRef, final String tempName, final String tempPass, final EnumSet<ObjectFlag> tempFlags, 
            final String tempDesc, final String tempTitle, final String tempPStatus, final int tempLoc, final String[] tempMoney) {

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
