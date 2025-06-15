package mud.interfaces;

import mud.game.Coins;

public interface BankerI extends Interactive
{
	public Coins withdraw(final Coins money);
	public void deposit(final Coins money);
}
