package mud.objects.things;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.interfaces.Lockable;
import mud.interfaces.Storage;
import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Thing;
import mud.objects.ThingType;
import mud.utils.Utils;

public class Box extends Thing implements Lockable<Item>, Storage<Item> {
	
	private Item key = null;
	private boolean isLocked = false;
	private boolean isOpen = false;
	private boolean full = false;
	private boolean empty = true;
	private int size = 10;
	
	private Map<String, Integer> contentMap = new HashMap<String, Integer>();
	
	public Box() {
		this("Chest", "A chest");
		this.flags = EnumSet.noneOf(ObjectFlag.class);
	}
	
	public Box(String name, String desc) {
		super(-1, name, EnumSet.noneOf(ObjectFlag.class), desc, 8);
		this.type = TypeFlag.ITEM;
		thing_type = ThingType.CONTAINER;
	}
	
	public Box(boolean isLocked) {
		this();
		this.isLocked = isLocked;
	}
	
	public Box(Item key, boolean isLocked) {
		this();
		this.key = key;
		this.isLocked = isLocked;
	}
	
	/**
	 * Object Loading Constructor
	 * @param dbref
	 */
	public Box(int dbref, String name, String desc, int location) {
		super(dbref, name, EnumSet.noneOf(ObjectFlag.class), desc, location);
		this.type = TypeFlag.ITEM;
		thing_type = ThingType.CONTAINER;
	}
	
	public Box(int tempDBRef, String tempName, String tempFlags, String tempDesc, int tempLoc) {
	}
	
	@Override
	public void setKey(Item key) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Item getKey() {
		return this.key;
	}
	
	@Override
	public boolean hasKey(Player p) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean lock() {
		if( this.key == null ) {
			isLocked = true;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean lock(Item key) {
		if( this.key == null ) return lock();
		else {
			if( this.key == key ) {
				isLocked = true;
				return true;
			}
			
			return false;
		}
	}
	
	@Override
	public boolean unlock() {
		if( this.key == null ) {
			isLocked = false;
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean unlock(Item key) {
		if( this.key == null ) return lock();
		else {
			if( this.key == key ) {
				isLocked = false;
				return true;
			}
			
			return false;
		}
	}

	@Override
	public boolean isLocked() {
		return this.isLocked;
	}
	
	public void insert(Item item) {
		if( !full ) {
			this.contents.add( item );
			this.contentMap.put( item.getName(), this.contents.indexOf(item) );
			if( this.contents.size() == this.size ) full = true;
		}
	}
	
	public Item retrieve(int index) {
		if( !empty ) {
			Item item = contents.remove(index);
			
			if( item != null ) {
				this.contentMap.remove( item.getName() );
				if( this.contents.size() == 0 ) empty = true;
			}
			
			return item;
		}
		
		return null;
	}

	public Item retrieve(String tName) {
		if( contentMap.containsKey(tName) ) {
			return this.retrieve( contentMap.get(tName) );
		}
		
		return null;
	}
	
	public boolean isEmpty() {
		return this.empty;
	}

	public boolean isFull() {
		return this.full;
	}
	
	public boolean isOpen() {
		return this.isOpen;
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