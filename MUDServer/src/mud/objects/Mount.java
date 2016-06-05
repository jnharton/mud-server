package mud.objects;

import mud.TypeFlag;
import mud.interfaces.Ridable;
import mud.objects.creatures.Size;

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
	
	private MountType mtype;
	private int speed;
	//private Speed speedClass; // slow, medium, fast
	
	public Mount(String name) {
		super(name, "");
		
		this.mtype = MountType.HORSE;
		this.speed = 8;
		//this.speedClass = Speed.FAST;
	}
	
	/*public MountType getType() {
		return this.mtype;
	}*/
	
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
	public boolean isLargeEnough(Size riderSize) {
		// TODO Auto-generated method stub
		return false;
	}
}