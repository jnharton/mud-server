package mud.game;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Faction {
	private String name;
	private Map<Faction, Integer> reputation; // what this faction thinks of other factions
	
	public Faction(String factionName) {
		this.name = factionName;
	}
	
	public Faction(String factionName, Faction...factions) {
		this(factionName, Arrays.asList(factions));
	}
	
	public Faction(String factionName, List<Faction> factions) {
		this(factionName);
		
		for(final Faction f : factions) {
			this.setReputation(f, 0);
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public void addReputation(final Faction faction, final Integer value) {
		setReputation(faction, getReputation(faction) + value);
	}
	
	public void subtractReputation(final Faction faction, final Integer value) {
		setReputation(faction, getReputation(faction) - value);
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
}