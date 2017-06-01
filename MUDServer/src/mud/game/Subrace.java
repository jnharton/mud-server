package mud.game;

public class Subrace {
	public Race parentRace;
	public String name;     // name of the subrace
	public String alt;      // common alternate name for the subrace

	public Subrace(Race pRace, String name, String alt) {
		this.parentRace = pRace;
		this.name = name;
		this.alt = alt;
	}
}