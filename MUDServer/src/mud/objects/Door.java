package mud.objects;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.interfaces.Lockable;
import mud.utils.Utils;

public class Door extends Exit implements Lockable<Item> {
	
	private boolean isLocked = false;
	
	private Item key = new Item();
	
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
		if( this.key == key ) {
			return lock();
		}
		
		return false;
	}


	@Override
	public boolean unlock() {
		this.isLocked = false;
		return true;
	}
	
	@Override
	public boolean unlock(Item key) {
		if( this.key == key ) {
			return unlock();
		}
		
		return false;
	}

	@Override
	public boolean isLocked() {
		return this.isLocked;
	}

	@Override
	public void setKey(Item key) {
		this.key = key;
	}

	@Override
	public Item getKey() {
		return this.key;
	}

	@Override
	public boolean hasKey(Player p) {
		// TODO Player needs some kind of item check method to simplify this
		if( p.getInventory().contains(this.key) ) {
			return true;
		}
		
		return false;
	}
	
	// TODO check loader etc and fix for new data field
	public String toDB() {
		String[] output = new String[9];
		output[0] = this.getDBRef() + "";                // database reference number
		output[1] = this.getName();                      // name
		output[2] = TypeFlag.asLetter(this.type) + "";   // flags
		output[2] = output[2] + this.getFlagsAsString();
		output[3] = this.getDesc();                      // description
		output[4] = this.getLocation() + "";             // location (a.k.a source)
		output[5] = this.getDestination() + "";          // destination
		output[6] = this.eType.ordinal() + "";           // exit type
		output[7] = (this.isLocked() ? 1 : 0) + "";      // lock state
		output[8] = this.key.getDBRef() + "";            // door's key (an Item) dbref
		return Utils.join(output, "#");
	}
}