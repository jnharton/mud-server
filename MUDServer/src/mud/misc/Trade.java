package mud.misc;

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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import mud.game.Coins;
import mud.interfaces.Stackable;
import mud.objects.Item;
import mud.objects.Player;
import mud.utils.Tuple;

/**
 * This class maintains the data needed for direct item trading between players.
 * 
 * @author jnhar
 *
 */
public class Trade {
	// public or method access?
	public final Player p1;
	public final Player p2;
	
	private final Coins p1_coins; // money player 1 is offering
	private final Coins p2_coins; // money player 2 is offering
	
	public final List<Item> p1_items; // items player 1 is offering to trade
	public final List<Item> p2_items; // items player 2 is offering to trade
	
	public final Hashtable<Player, Tuple<Coins, List<Item>>> offers;
	
	public boolean p1_confirm = false;
	public boolean p2_confirm = false;
	
	public Trade(final Player player1, final Player player2) {
		this.p1 = player1;
		this.p2 = player2;
		
		this.p1_coins = Coins.copper(0);
		this.p2_coins = Coins.copper(0);
		
		this.p1_items = new ArrayList<Item>(10);
		this.p2_items = new ArrayList<Item>(10);
		
		this.offers = new Hashtable<Player, Tuple<Coins, List<Item>>>();
	}
	
	public Tuple<Player, Player> getTraders() {
		return new Tuple<Player, Player>(p1, p2); 
	}
	
	public void addCoins(final Player player, final Coins coins) {
		if( player == p1 ) {
			p1_coins.add( coins );
		}
		else if( player == p2 ) {
			p2_coins.add( coins );
		}
	}
	
	public void removeCoins(final Player player, final Coins coins) {
		if( player == p1 ) {
			p1_coins.subtractCopper( coins.numOfCopper() );
		}
		else if( player == p2 ) {
			p2_coins.subtractCopper( coins.numOfCopper() );
		}
	}
	
	public void addItem(final Player player, final Item item) {
		if( player == p1 ) {
			if( item instanceof Stackable) {
				p1_items.add( ((Stackable<Item>) item).split(1) );
			}
			else  p1_items.add( item );
		}
		else if( player == p2 ) {
			if( item instanceof Stackable) {
				p2_items.add( ((Stackable<Item>) item).split(1) );
			}
			else p2_items.add( item );
		}
	}
	
	public void removeItem(final Player player, final Item item) {
		if( player == p1 ) {
			p1_items.remove( item );
		}
		else if( player == p2 ) {
			p2_items.remove( item );
		}
	}
}