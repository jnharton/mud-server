package mud.quest;

import java.util.LinkedList;
import java.util.List;

import mud.game.Coins;
import mud.objects.Item;

public class Reward {
	private Coins coins;
	private List<Item> items;

	public Reward(final Coins coins, final Item...items) {
		this.coins = Coins.copper( coins.numOfCopper() );
		this.items = new LinkedList<Item>();
		
		for(final Item item : items) {
			this.items.add(item);
		}
	}
	
	public Coins getCoins() {
		return Coins.copper( this.coins.numOfCopper() );
	}
	
	public List<Item> getItems() {
		return this.items;
	}
}