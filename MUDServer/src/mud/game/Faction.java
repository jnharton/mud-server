package mud.game;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Faction {
	public static int HOSTILE = -100;
	public static int NEUTRAL = 0;
	public static int FRIENDLY = 100;
	
	private String name;
	private Map<Faction, Integer> reputation; // what this faction thinks of other factions
	
	public Faction(final String factionName) {
		this(factionName, new LinkedList<Faction>());
	}
	
	public Faction(final String factionName, final List<Faction> factions) {
		this.name = factionName;
		
		for(final Faction f : factions) {
			this.setReputation(f, NEUTRAL);
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public void modifyReputation(final Faction faction, final Integer value) {
		setReputation(faction, getReputation(faction) + value);
	}
	
	public void setReputation(final Faction faction, final Integer value) {
		this.reputation.put(faction, value);
	}
	
	public Integer getReputation(final Faction faction) {
		return reputation.get(faction);
	}
	
	@Override
	public String toString() {
		return getName();
	}
}