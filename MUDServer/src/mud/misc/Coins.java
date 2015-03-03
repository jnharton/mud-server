package mud.misc;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * 
 * @author joshgit
 *
 */
public class Coins
{
    /*static final private int 
        SILVER_RATIO = 100,
        GOLD_RATIO = 100 * 100,
        PLATINUM_RATIO = 100 * 100 * 100;*/
    static final private int 
    	SILVER_RATIO = 100,
    	GOLD_RATIO = SILVER_RATIO * 100,
    	PLATINUM_RATIO = GOLD_RATIO * 100;

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
    
    /**
     * 
     * @author Jeremy
     * 
     * @param n
     * @return
     */
    static public Coins fromArray(final int[] n) {
        return Coins.platinum(n[0]).add(Coins.gold(n[1])).add(Coins.silver(n[2])).add(Coins.copper(n[3]));
    }

    // store everything as lowest denomination
    final private int copperValue;

	private Coins(final int n) {
        copperValue = n;
	}
	
	public Coins(final int platinum, final int gold, final int silver, final int copper) {
        copperValue = (PLATINUM_RATIO * platinum) + (GOLD_RATIO * gold) + (SILVER_RATIO * silver) + copper;
	}
	
	public Coins(final int[] money) {
		copperValue = (PLATINUM_RATIO * money[0]) + (GOLD_RATIO * money[1]) + (SILVER_RATIO * money[2]) + money[3];
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
    
    public Coins fromString() {
    	return null;
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
    
    public static void debug() {
    	System.out.println( Coins.copper(95) );
    	System.out.println( Coins.copper(105) );
    	System.out.println( Coins.silver(95) );
    	System.out.println( Coins.silver(105) );
    	System.out.println( Coins.gold(95) );
    	System.out.println( Coins.gold(105) );
    	System.out.println( Coins.platinum(15) );
    }

    @Override
    public String toString() {
        return toString(true);
    }
}