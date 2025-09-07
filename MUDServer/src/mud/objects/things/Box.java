package mud.objects.things;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mud.ObjectFlag;
import mud.interfaces.Closeable;
import mud.interfaces.Lockable;
import mud.interfaces.Storage;
import mud.objects.Item;
import mud.objects.Thing;
import mud.objects.ThingTypes;
import mud.utils.Utils;

public class Box extends Thing implements Closeable, Lockable<Item>, Storage<Item> {
	private boolean isOpen = false;
	
	private Item key = null;
	private boolean isLocked = false;
	
	private boolean full = false;
	private boolean empty = true;
	private int size = 10;
	
	private List<Item> contents;
	private Map<String, Integer> contentMap;
	
	public Box() {
		this("Box", "A box.");
		this.flags = EnumSet.noneOf(ObjectFlag.class);
	}
	
	public Box(final String name, final String desc) {
		super(name, desc);
		
		this.thing_type = ThingTypes.CONTAINER;
		
		this.contents = new ArrayList<Item>();
		this.contentMap = new HashMap<String, Integer>();
	}
	
	/**
	 * Object Loading Constructor
	 * @param dbref
	 */
	public Box(final int tempDBRef, final String tempName, final EnumSet<ObjectFlag> tempFlags, final String tempDesc, final int tempLoc) {
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc);
		
		this.thing_type = ThingTypes.CONTAINER;
		
		this.contents = new ArrayList<Item>();
		this.contentMap = new HashMap<String, Integer>();
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
	public boolean lock() {
		if( this.key == null ) {
			isLocked = true;
			return true;
		}
		
		return false;
	}

	@Override
	public boolean lock(final Item key) {
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
	public boolean unlock(final Item key) {
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
	
	public boolean contains(final Item item) {
		return contentMap.keySet().contains(item.getName());
	}
	
	@Override
	public List<Item> getContents() {
		return Collections.unmodifiableList(this.contents);
	}
	
	@Override
	public boolean insert(final Item item) {
		boolean success = false;

		if( !isFull() ) {
			if( item != null ) {
				this.contents.add( item );
				this.contentMap.put( item.getName(), this.contents.indexOf(item) );
				
				if( this.contents.size() > 0 )          this.empty = false;
				if( this.contents.size() == this.size ) this.full = true;
				
				success = true;
			}
		}

		return success;
	}

	public Item retrieve(int index) {
		if( !empty ) {
			final Item item = this.contents.remove(index);
			
			if( item != null ) {
				this.contentMap.remove( item.getName() );
				if( this.contents.size() == 0 ) empty = true;
			}
			
			return item;
		}
		
		return null;
	}
	
	@Override
	public Item retrieve(final String tName) {
		if( this.contentMap.containsKey(tName) ) {
			return this.retrieve( this.contentMap.get(tName) );
		}
		
		return null;
	}
	
	@Override
	public boolean isEmpty() {
		return this.empty;
	}
	
	@Override
	public boolean isFull() {
		return this.full;
	}
	
	@Override
	public void open() {
		if( !this.isLocked ) {
			this.isOpen = true;
		}
	}

	@Override
	public void close() {
		this.isOpen = false;
	}
	
	@Override
	public boolean isOpen() {
		return this.isOpen;
	}
	
	@Override
	public boolean requiresKey() {
		if( this.key != null ) return true;
		else                   return false;
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
		String[] output = new String[2];
		
		output[0] = ((this.key != null) ? this.key.getDBRef() : -1) + ""; // key
		output[1] = ((this.isLocked ) ? 1 : 0) + "";                      // locked?
		
		return super.toDB() + "#" + Utils.join(output, "#");
	}
}