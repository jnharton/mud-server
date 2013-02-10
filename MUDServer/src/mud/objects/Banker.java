package mud.objects;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.Coins;
import mud.Currency;
import mud.net.Client;
import mud.utils.Bank;

public class Banker extends NPC implements BankerI {
	
	private Bank bank;
	
	public Banker(final int tempDBRef, final String tempName, final String tempPass, final EnumSet<ObjectFlag> tempFlags, 
            final String tempDesc, final String tempTitle, final String tempPStatus, final int tempLoc, final Coins tempMoney) {

        super(tempDBRef, tempName, tempPass, tempFlags, tempDesc, tempTitle, tempPStatus, tempLoc, tempMoney);
	}
	
	@Override
	public void interact(Client client) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deposit(final Coins c) {
	}

	@Override
	public Coins withdraw(final Coins c) {
		return Coins.copper(0);
	}
}
