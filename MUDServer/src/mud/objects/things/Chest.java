package mud.objects.things;

import java.util.ArrayList;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.interfaces.Lockable;
import mud.interfaces.Storage;

import mud.objects.Item;
import mud.objects.Thing;
import mud.objects.ThingType;

import mud.utils.Utils;

public class Chest extends Thing implements Lockable<Item>, Storage<Item> {
	
	private Item key = null;
	private boolean isLocked = false;
	private boolean full = false;
	
	private final ArrayList<Item> contents = new ArrayList<Item>();
	
	public Chest() {
		this("Public Chest", "A chest");
	}
	
	public Chest(String name, String desc) {
		super(-1, name, EnumSet.of(ObjectFlag.THING), desc, 8);
		thing_type = ThingType.CHEST;
	}
	
	/**
	 * Object Loading Constructor
	 * @param dbref
	 */
	public Chest(int dbref) {
		super(-1, "Public Chest", EnumSet.of(ObjectFlag.THING), "A chest", 8);
	}
	
	public Chest(boolean isLocked) {
		super();
		this.isLocked = isLocked;
	}
	
	public Chest(Item key, boolean isLocked) {
		super();
		this.key = key;
		this.isLocked = isLocked;
	}
	
	public Chest(int tempDBRef, String tempName, String tempFlags, String tempDesc, int tempLoc) {
	}

	@Override
	public void lock() {
		isLocked = true;
	}
	
	@Override
	public void unlock() {
		isLocked = false;
	}

	@Override
	public boolean isLocked() {
		return this.isLocked;
	}
	
	@Override
	public void insert(Item i) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Item retrieve(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Item retrieve(String tName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFull() {
		return this.full;
	}
	
	/**
	 * possible extra data:
	 * - size of the chest in terms of items
	 * - lock state (locked/unlocked)
	 * 
	 * NOTE(3-18-2013): key and lockstate not preserved here, full doesn't need to be persisted
	 * since it can be determined after loading the contents
	 */
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";           // database reference number
		output[1] = this.getName();                 // name
		output[2] = this.getFlagsAsString();        // flags
		output[3] = this.getDesc();                 // description
		output[4] = this.getLocation() + "";        // location
		output[5] = this.thing_type.ordinal() + ""; // thing type
		output[6] = "*";                            // key
		output[7] = "*";                            // locked?
		return Utils.join(output, "#");
	}
}
