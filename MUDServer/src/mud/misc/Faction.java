package mud.misc;

import java.util.Map;

public class Faction {
	private String name;
	private Map<Faction, Integer> opinion; // what this faction thinks of other factions
	
	public Faction(String factionName) {
		this.name = factionName;
	}
	
	public String getName() {
		return this.name;
	}
}