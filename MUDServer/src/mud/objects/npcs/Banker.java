package mud.objects.npcs;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.misc.Coins;
import mud.misc.Currency;
import mud.net.Client;
import mud.objects.NPC;
import mud.objects.Player;
import mud.utils.Bank;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Banker extends NPC implements BankerI {
	
	private Bank bank;
	
	public Banker(final int tempDBRef, final String tempName, final String tempPass, final EnumSet<ObjectFlag> tempFlags, 
            final String tempDesc, final String tempTitle, final String tempPStatus, final int tempLoc, final Coins tempMoney) {

        super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc, tempMoney);
	}
	
	@Override
	public void interact(Player player) {
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
