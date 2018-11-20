package mud.misc;

public class Ore extends Resource {
	enum OreType { ORE, VEIN };
	
	OreType t = OreType.VEIN;
	
	public boolean _rich = false;
	
	public Ore(final String name, final boolean rich) {
		super(name);
		
		this._rich = rich;
	}

	@Override
	public ResType getType() {
		return Resource.ResType.ORE;
	}
	
	@Override
	public String getName() {
		return String.format("%s %s", _name, "Ore");
	}
	
	@Override
	public String getDisplayName() {
		if( _rich ) return String.format("%s %s %s", "Rich", _name, t.name());
		else        return String.format("%s %s", _name, t.name());
	}
}