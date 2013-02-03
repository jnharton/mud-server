package mud.objects.things;

import java.util.ArrayList;

import mud.interfaces.Lockable;
import mud.interfaces.Storage;

import mud.objects.Item;
import mud.objects.Thing;

import mud.utils.Utils;

public class Chest extends Thing implements Lockable<Item>, Storage<Item> {
	
	private Item key;
	private boolean isLocked;
	private boolean full;
	
	private final ArrayList<Item> contents;
	
	public Chest() {
		this.contents = new ArrayList<Item>();
	}
	
	public Chest(String name, String desc) {
		super(-1, "Public Chest", "T", "A chest", 8);
		this.key = null;
		this.isLocked = false;
		
		this.contents = new ArrayList<Item>();
	}
	
	public Chest(boolean isLocked) {
		super();
		this.key = null;
		this.isLocked = isLocked;
		
		this.contents = new ArrayList<Item>();
	}
	
	public Chest(Item key, boolean isLocked) {
		super();
		this.key = key;
		this.isLocked = isLocked;
		
		this.contents = new ArrayList<Item>();
	}
	
	public Chest(int tempDBRef, String tempName, String tempFlags, String tempDesc, int tempLoc) {
		this.contents = new ArrayList<Item>();
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
	 */
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";    // chest database reference number
		output[1] = this.getName();                // chest name
		output[2] = this.getFlags();               // chest flags
		output[3] = this.getDesc();                // chest description
		output[4] = this.getLocation() + ""; // chest location (a.k.a parent)
		output[5] = "*";                           // blank field
		output[6] = "*";                           // blank field
		output[7] = "*";                           // blank field
		String output1 = Utils.join(output, "#");
		return output1;
	}
}