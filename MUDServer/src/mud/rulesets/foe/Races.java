package mud.rulesets.foe;

import mud.game.Race;

public final class Races {
	public static final Race NONE = new Race(FOESpecial.getInstance(), "None", 0, false, false, new Integer[] { 0, 0, 0, 0, 0, 0, 0 });
	public static final Race EARTH = new Race(FOESpecial.getInstance(), "Earth", 1, false, false, new Integer[] { 1, 0, 1, 0, 0, 0, 0 });
	public static final Race PEGASUS = new Race(FOESpecial.getInstance(), "Pegasus", 2, true, false, new Integer[] { 0, 1, 0, 0, 0, 1, 0 });
	public static final Race UNICORN = new Race(FOESpecial.getInstance(), "Unicorn", 3, true, false, new Integer[] { 0, 0, 0, 1, 1, 0, 0 });
	public static final Race ALICORN = new Race(FOESpecial.getInstance(), "Alicorn", 4, true, false, new Integer[] { 1, 1, 1, 1, 1, 1, 0 });
	
	static private final Race[] myValues = {
			NONE, EARTH, PEGASUS, UNICORN
	};

	public static Race getRace(int id) {
		return myValues[id];
	}

	public static Race getRace(String name) {
		Race race = null;

		for(final Race r : myValues) {
			if( r.getName().equalsIgnoreCase(name) ) {
				race = r;
			}
		}

		return race;
	}
}