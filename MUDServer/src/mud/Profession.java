package mud;

public class Profession {
	private static int EXP_PER_RANK = 2000;
	private static int MAX_RANKS = 100;
	
	private String name;
	private int ranks;
	private int exp;
	
	public Profession( final String pName ) {
		this.name = pName;
		
		this.ranks = 0;
		this.exp = 0;
	}
	
	// loading constructor (for loading up existing Profession data)
	// jewelry:5,10000 profession:ranks,experience
	public Profession( final String pName, int pRanks, int pExp ) {
		this.name = pName;
		this.ranks = pRanks;
		this.exp = pExp;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getRanks() {
		return this.ranks;
	}
	
	public int getExp() {
		return this.exp;
	}
	
	public boolean increaseRank() {
		if( (ranks + 1) <= MAX_RANKS && exp >= (ranks + 1) * EXP_PER_RANK ) {
			ranks++;
			return true;
		}
		
		return false;
	}
	
	public boolean increaseExp(int exp_gain) {
		if( exp + exp_gain <= MAX_RANKS * EXP_PER_RANK ) {
			exp += exp_gain;;
			return true;
		}
		
		return false;
	}
}