package mud;

import mud.objects.Player;

public class Currency {
	private final String name;   // name of the currency
	private final String abbrev; // abbreviation to use when a short form is required
	private final Currency base; // a smaller form of currency that the value of this is based o
	private final int value;     // the equivalent value of this in a smaller form of currency
	private final int index;
	
	Currency(String name, String abbrev, Currency base, int value, int index) {
		this.name = name;     // name is the name the player sees
		this.abbrev = abbrev; // the common two letter abbreviate (used in indicating money)
		this.base = base;     // base
		this.value = value;   // value is the number of the base unit that equals it
		this.index = index;   // index is the part of the money array on the player that holds that kind of currency
	}
	
	public String toString() {
		return this.name;
	}
	
	// this will always only return the amount asked for (if you have the required input) or none OR
	// if you give all your money of a type, then it will give as much as it can convert
	// NOTE: If this class has been modified to change the currency types/values, these conversions need changing
	// TODO: don't set actual values, just return the amount of the new currency obtained
	// TODO: replace with a version that doesn't require access to the player object
	public int convert(Player temp, Currency newCurrency, int amount, int supply) {
		if(amount != 0 && supply == 0) { // same as asking for as much as you can get with the supply you give
			if((temp.getMoney(this.index) * this.value) - (newCurrency.value * amount) >= 0) {
				// sets player money (shouldn't be done here if currency conversion is not an OOC transaction
				temp.setMoney(this.index, temp.getMoney(this.index) - ((newCurrency.value * amount) / this.value));
				temp.setMoney(newCurrency.index, amount);
				return amount;
			}
			else {
				// you don't have enough money to get that much out
				return -1;
			}
		}
		else {
			return -1;
		}
	}
	
	public String getAbbrev() {
		return this.abbrev;
	}
	
	public Integer toInteger() {
		return this.value;
	}
}