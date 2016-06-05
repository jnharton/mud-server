package mud.objects;

public class Terrain {
	// NON,FOR,MSH,HILL,MTN,DST,AQU,SKY?
	
	public static final Terrain NONE     = new Terrain("NONE", 1);
	public static final Terrain FOREST   = new Terrain("FOREST", 0.75);
	public static final Terrain MARSH    = new Terrain("MARSH", 0.25);
	public static final Terrain HILLS    = new Terrain("HILLS", 1);
	public static final Terrain MOUNTAIN = new Terrain("MOUNTAIN", 0.5);
	public static final Terrain DESERT   = new Terrain("DESERT", 0.3);
	public static final Terrain PLAINS   = new Terrain("PLAINS", 1);
	public static final Terrain AQUATIC  = new Terrain("AQUATIC", 0.1);
	public static final Terrain SKY      = new Terrain("SKY", 1);
	
	private String name;
	private double movement_affect;
	
	public Terrain(final String name, final double moveSpeedAffect) {
		this.name = name;
		this.movement_affect = moveSpeedAffect;
	}
}