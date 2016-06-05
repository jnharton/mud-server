package mud.objects.exits;

import java.util.Arrays;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.interfaces.Lockable;
import mud.objects.Exit;
import mud.objects.ExitType;
import mud.objects.Item;
import mud.utils.Tuple;
import mud.utils.Utils;

public class Door extends Exit implements Lockable<Item> {
	public Tuple<Integer, String> side1;
	public Tuple<Integer, String> side2;
	
	private boolean isLocked = false;
	private boolean requiresKey = false;            // does it require a key?
	
	private Item key = null;
	
	public Door(int tempDBRef, String tempName, final EnumSet<ObjectFlag> flagsNotUsed, String tempDesc, int tempLoc, int tempDestination) {
		super(tempDBRef, tempName, flagsNotUsed, tempDesc, tempLoc, tempDestination);
		
		this.eType = ExitType.DOOR;
	}
	
	public void init() {
		if( this.name.contains("/") ) {
			String[] temp1 = name.split("/");
			
			System.out.println(Arrays.asList(temp1));
			System.out.println(temp1[0]);
			System.out.println(temp1[1]);
			
			this.side1 = new Tuple<Integer, String>(this.location, temp1[0]);
			
			System.out.println("Side 1");
			System.out.println("INTEGER: " + this.side1.one);
			System.out.println(" STRING: " + this.side1.two);
			
			this.side2 = new Tuple<Integer, String>(this.destination, temp1[1]);
			
			System.out.println("Side 2");
			System.out.println("INTEGER: " + this.side2.one);
			System.out.println(" STRING: " + this.side2.two);
		}
	}
	
	public String getName(final Integer source) {
		String name = "";
		
		if( this.side1 != null && this.side1.one.equals(source) ) name = this.side1.two;
		if( this.side2 != null && this.side2.one.equals(source) ) name = this.side2.two;
		
		return name;
	}
	
	@Override
	public boolean lock() {
		this.isLocked = true;
		return true;
	}
	
	@Override
	public boolean lock(final Item key) {
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
	public void setKey(final Item key) {
		if( key != null ) {
			if( !this.requiresKey ) this.requiresKey = true;
			this.key = key;
		}
		else {
			this.requiresKey = false;
			this.key = null;
		}
	}

	@Override
	public Item getKey() {
		return this.key;
	}
	
	public boolean isKey(final Item item) {
		return this.key == item;
	}
	
	@Override
	public boolean requiresKey() {
		return this.requiresKey;
	}
	
	public String toDB() {
		// TODO decide what to do with aliases... 
		final String[] names = this.getName().split("/");

		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();

		for(final String s : this.getAliases()) {
			int i = s.indexOf("|");
			int l = s.length();
			
			if( s.startsWith(names[0]) )     sb1.append( s.substring(i + 1, l) );
			else if( s.startsWith(names[1]) ) sb2.append( s.substring(i + 1, l) );
		}
		
		final String name1 = sb1.toString();
		final String name2 = sb2.toString();

		// -----
		boolean test = name1.equals("") && name2.equals("");

		final String[] output = new String[9];

		output[0] = this.getDBRef() + "";                                // database reference number
		output[1] = this.getName();                                      // name
		output[1] = output[1] + (test ? "" : ";" + name1 + "/" + name2); // name 
		output[2] = type + getFlagsAsString();                           // flags;
		output[3] = this.getDesc();                                      // description
		output[4] = this.getLocation() + "";                             // location (a.k.a source)

		output[5] = this.getDestination() + "";                          // destination
		output[6] = this.eType.ordinal() + "";                           // exit type

		output[7] = (this.isLocked() ? 1 : 0) + "";                      // lock state
		output[8] = ((this.key != null) ?this.key.getDBRef() : -1) + ""; // key info

		return Utils.join(output, "#");
	}
}