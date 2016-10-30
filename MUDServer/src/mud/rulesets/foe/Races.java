package mud.rulesets.foe;

import mud.game.Race;

public final class Races {
	static private final Race[] myValues = {};

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