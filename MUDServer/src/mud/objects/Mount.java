package mud.objects;

import mud.interfaces.Ridable;

/**
 * MUDObject -> Creature -> Mount
 * 
 * A mount is a special type of ridable creature, that a player can ride on?
 * 
 * @author Jeremy
 *
 */
public class Mount extends Creature implements Ridable {
	public enum MountType { WARHORSE, WARPONY, HORSE, PONY, DESTRIER };
	public enum Speed { SLOW, MEDIUM, FAST };
	
	private String name;
	private MountType type;
	private int speed;
	//private Speed speedClass; // slow, medium, fast
	
	public Mount(String name) {
		this.name = name;
		this.type = MountType.HORSE;
		this.speed = 8;
		//this.speedClass = Speed.FAST;
	}
	
	public String getName() {
		return this.name;
	}
	
	public MountType getType() {
		return this.type;
	}
	
	public int getSpeed() {
		return this.speed;
	}

	@Override
	public void mount() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unmount() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLargeEnough() {
		// TODO Auto-generated method stub
		return false;
	}
}