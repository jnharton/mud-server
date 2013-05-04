package mud;

import mud.Races.Subraces;

public class Race {
	private String name;
	private Subraces sub;
	private int id;
	private Integer[] statAdj;
	private boolean restricted;
	
	public Race(String name, int id, boolean restricted) {
		this(name, id, new Integer[] { 0, 0, 0, 0 , 0, 0 }, restricted);
	}

	public Race(String name, int id, Integer[] statAdj, boolean restricted) {
		this.name = name;
		this.id = id;
		this.statAdj = statAdj;
		this.restricted = restricted;
	}
	
	public int getId() {
		return this.id;
	}

	public Integer[] getStatAdjust() {
		return this.statAdj;
	}
	
	public Subraces getSubrace() {
		return this.sub;
	}

	public void setSubrace(Subraces sub) {
		this.sub = sub;
	}

	public String getName() {
		return this.name;
	}

	public boolean isRestricted() {
		return this.restricted;
	}

	public String toString() {
		if (sub == null) {
			return this.name;
		}
		else {
			return this.sub.name;
		}
	}
}