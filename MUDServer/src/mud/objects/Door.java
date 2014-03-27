package mud.objects;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.interfaces.Lockable;
import mud.utils.Utils;

public class Door extends Exit implements Lockable<Item> {
	
	private boolean isLocked = false;
	
	public Door() {
		this.eType = ExitType.DOOR;
	}
	
	public Door(int tempDBRef, String tempName, final EnumSet<ObjectFlag> flagsNotUsed, String tempDesc, int tempLoc, int tempDestination) {
		super(tempDBRef, tempName, flagsNotUsed, tempDesc, tempLoc, tempDestination);
		
		this.eType = ExitType.DOOR;
	}
	
	@Override
	public boolean lock() {
		this.isLocked = true;
		return true;
	}
	
	@Override
	public boolean lock(Item key) {
		return lock();
	}


	@Override
	public boolean unlock() {
		if (this.getExitType() == ExitType.DOOR) {
			this.isLocked = false;
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean unlock(Item key) {
		return unlock();
	}

	@Override
	public boolean isLocked() {
		return this.isLocked;
	}

	@Override
	public void setKey(Item key) {
		// TODO Auto-generated method stub
	}

	@Override
	public Item getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasKey(Player p) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";           // database reference number
		output[1] = this.getName();                 // name
		output[2] = this.getFlagsAsString();        // flags
		output[3] = this.getDesc();                 // description
		output[4] = this.getLocation() + "";        // location (a.k.a source)
		output[5] = this.getDestination() + "";     // destination
		output[6] = this.eType.ordinal() + "";      // exit type
		output[7] = (this.isLocked() ? 1 : 0) + ""; // lock state
		return Utils.join(output, "#");
	}
}