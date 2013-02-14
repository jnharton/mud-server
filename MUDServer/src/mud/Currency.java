package mud;

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

public class Currency {
	// value is relative to copper coins
	public static Currency COPPER = new Currency("Copper", "cp", null, 1, 0);
	public static Currency SILVER = new Currency("Silver", "sp", COPPER, 100, 1);
	public static Currency GOLD = new Currency("Gold", "gp", COPPER, 10000, 2);
	public static Currency PLATINUM = new Currency("Platinum", "pp", COPPER, 1000000, 3);

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

	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name;
	}
	
	/**
	 * Get Abbreviation
	 * 
	 * Return an abbreviated short forum of the currency name
	 * 
	 * @return
	 */
	public String getAbbrev() {
		return this.abbrev;
	}
	
	/**
	 * Return the value of a single unit of this currency
	 * in terms of a different currency. If it isn't based
	 * on another currency, return -1.
	 * 
	 * @return
	 */
	public Integer toInteger() {
		if (base != null) {
			return this.value;
		}
		else {
			return -1;
		}
	}

	public static Currency fromInt(int index) {
		switch(index) {
			case 0:
				return COPPER;
			case 1:
				return SILVER;
			case 2:
				return GOLD;
			case 3:
				return PLATINUM;
			default:
				return COPPER;
		}
	}
	
}