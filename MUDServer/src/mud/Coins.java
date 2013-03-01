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

/**
 * 
 * @author joshgit
 *
 */
public class Coins
{
    static final private int 
        SILVER_RATIO = 100,
        GOLD_RATIO = 100 * 100,
        PLATINUM_RATIO = 100 * 100 * 100;

    // 230 copper coins: Coins.copper(230)
    // 6 silver coins: Coins.silver(6)
    // etc.
    static public Coins copper(final int n) {
        return new Coins(n);
    }

    static public Coins silver(final int n) {
        return new Coins(SILVER_RATIO * n);
    }

    static public Coins gold(final int n) {
        return new Coins(GOLD_RATIO * n);
    }

    static public Coins platinum(final int n) {
        return new Coins(PLATINUM_RATIO * n);
    }

    static public Coins fromArray(final int[] n) {
        return Coins.platinum(n[0]).add(Coins.gold(n[1])).add(Coins.silver(n[2])).add(Coins.copper(n[3]));
    }

    // store everything as lowest denomination
    final private int copperValue;

	private Coins(final int n) {
        copperValue = n;
	}

    // 230 copper coins minus 5 copper coins: Coins.copper(230).subtractCopper(6)
    // 6 silver coins minus 230 copper coins: Coins.silver(6).subtractCopper(230)
    // etc.
    public Coins subtractCopper(final int n) {
        return new Coins(copperValue - n);
    }

    public Coins subtractSilver(final int n) {
        return new Coins(copperValue - SILVER_RATIO * n);
    }

    public Coins subtractGold(final int n) {
        return new Coins(copperValue - GOLD_RATIO * n);
    }

    public Coins subtractPlatinum(final int n) {
        return new Coins(copperValue - PLATINUM_RATIO * n);
    }

    // Coins.copper(230).numOfSilver() => 2
    // etc.
    public int numOfCopper() {
        return copperValue;
    }

    public int numOfSilver() {
        return copperValue / SILVER_RATIO;
    }

    public int numOfGold() {
        return copperValue / GOLD_RATIO;
    }

    public int numOfPlatinum() {
        return copperValue / PLATINUM_RATIO;
    }

    public boolean isMoreOrEqual(final Coins other) {
        return copperValue >= other.copperValue;
    }

    public Coins add(final Coins other) {
        return new Coins(copperValue + other.copperValue);
    }

    public int[] toArray() {
        final int[] coins = new int[4];
        coins[3] = numOfPlatinum();
        coins[2] = subtractPlatinum(coins[3]).numOfGold();
        coins[1] = subtractPlatinum(coins[3]).subtractGold(coins[2]).numOfSilver();
        coins[0] = subtractPlatinum(coins[3]).subtractGold(coins[2]).subtractSilver(coins[1]).numOfCopper();
        return coins;
    }

    // false -> comma separated for saving to db
    // true -> units separated
    public String toString(final boolean showUnits) {
        final StringBuilder buf = new StringBuilder();

        Coins left = this;
        if (left.numOfPlatinum() > 0) {
            buf.append(left.numOfPlatinum());
            buf.append(showUnits ? "pp " : ",");
        }
        else if(!showUnits) { buf.append( 0 + ","); }

        left = left.subtractPlatinum(left.numOfPlatinum());
        if (left.numOfGold() > 0) {
            buf.append(left.numOfGold());
            buf.append(showUnits ? "gp " : ",");
        }
        else if(!showUnits) { buf.append( 0 + ","); }

        left = left.subtractGold(left.numOfGold());
        if (left.numOfSilver() > 0) {
            buf.append(left.numOfSilver());
            buf.append(showUnits ? "sp " : ",");
        }
        else if(!showUnits) { buf.append( 0 + ","); }

        left = left.subtractSilver(left.numOfSilver());
        if (left.numOfCopper() > 0) {
            buf.append(left.numOfCopper());
            buf.append(showUnits ? "cp " : ",");
        }
        else if(!showUnits) { buf.append(0); }

        return buf.length() > 0 ? buf.toString().trim() : "0cp";
    }

    @Override
    public String toString() {
        return toString(true);
    }

}
