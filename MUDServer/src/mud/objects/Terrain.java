package mud.objects;

public class Terrain {
	// NON,FOR,MSH,HILL,MTN,DST,AQU,SKY?
	
	public static final Terrain NONE     = new Terrain(1);
	public static final Terrain FOREST   = new Terrain(0.75);
	public static final Terrain MARSH    = new Terrain(0.25);
	public static final Terrain HILLS    = new Terrain(1);
	public static final Terrain MOUNTAIN = new Terrain(0.5);
	public static final Terrain DESERT   = new Terrain(0.3);
	public static final Terrain PLAINS   = new Terrain(1);
	public static final Terrain AQUATIC  = new Terrain(0.1);
	public static final Terrain SKY      = new Terrain(1);
	
	private double movement_affect;
	
	public Terrain(double moveSpeedAffect) {
		this.movement_affect = moveSpeedAffect;
	}
}