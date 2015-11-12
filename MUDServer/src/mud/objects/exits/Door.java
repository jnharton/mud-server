package mud.objects.exits;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.interfaces.Lockable;
import mud.objects.Exit;
import mud.objects.ExitType;
import mud.objects.Item;
import mud.utils.Tuple;
import mud.utils.Utils;

public class Door extends Exit implements Lockable<Item> {
	private Tuple<Integer, String> side1;
	private Tuple<Integer, String> side2;
	
	private boolean isLocked = false;
	
	private Item key = null;
	
	/*public Door() {
		this.eType = ExitType.DOOR;
	}*/
	
	public Door(int tempDBRef, String tempName, final EnumSet<ObjectFlag> flagsNotUsed, String tempDesc, int tempLoc, int tempDestination) {
		super(tempDBRef, tempName, flagsNotUsed, tempDesc, tempLoc, tempDestination);
		
		this.eType = ExitType.DOOR;
	}
	
	public String getName(final Integer origin) {
		String name = "";
		
		if( side1 != null ) {
			if( side1.one == origin ) name = side1.two;
		}
		else if( side2 != null ) {
			if( side2.one == origin ) name = side2.two;
		}
		
		return name;
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
	
	// TODO check loader etc and fix for new data field
	public String toDB() {
		String[] output = new String[9];
		output[0] = this.getDBRef() + "";                // database reference number

		final String[] names = this.getName().split("/");

		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();

		for(String s : this.getAliases()) {
			int i = s.indexOf("|");
			int l = s.length();
			
			if( s.startsWith(names[0]) ) {
				sb1.append( s.substring(i + 1, l) );
			}
			else if( s.startsWith(names[1]) ) {
				sb2.append( s.substring(i + 1, l) );
			}
		}
		
		//output[1] = this.getName();                      // name
		output[1] = this.getName() + ";" + sb1.toString() + "/" + sb2.toString(); // name
		
		output[2] = TypeFlag.asLetter(this.type) + "";   // flags
		output[2] = output[2] + this.getFlagsAsString();
		output[3] = this.getDesc();                      // description
		output[4] = this.getLocation() + "";             // location (a.k.a source)
		output[5] = this.getDestination() + "";          // destination
		output[6] = this.eType.ordinal() + "";           // exit type
		output[7] = (this.isLocked() ? 1 : 0) + "";      // lock state
		
		if( this.key != null ) {
			output[8] = this.key.getDBRef() + "";        // door's key (an Item) dbref (-1 if there is no key)
		}
		else output[8] = -1 + "";
		
		return Utils.join(output, "#");
	}
}