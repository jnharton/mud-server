package mud.objects.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import mud.ObjectFlag;
import mud.interfaces.Storage;
import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.utils.Utils;

public class Container extends Item implements Storage<Item> {
	// these only need to be private if they are non-static, non-final and thus changeable
	public static final Character top = '-';
	public static final Character side = '|';
	public static final Character bottom = '-';
	
	public static final int DEFAULT_SIZE = 5;
	
	private int size;
	private List<Item> contents;
	
	//protected Map<String, Slot> slots = null; // handles objects which hold specific things, like sheaths for swords

	private int displayWidth = 30; // the width for the container display box (check whenever something added)

	public Container() {
		super(-1, "Container", "A generic container");
		
		this.item_type = ItemTypes.CONTAINER;
		
		this.size = Container.DEFAULT_SIZE;
		
		this.contents = new ArrayList<Item>(this.size);
	}

	public Container(final String name) {
		this(name, "A generic container", Container.DEFAULT_SIZE);
	}
	
	public Container(final String name, final String description) {
		this(name, description, Container.DEFAULT_SIZE);
	}

	public Container(final String name, final String description, final int size) {
		super(-1, name, description);

		this.item_type = ItemTypes.CONTAINER;
		
		this.equippable = false;

		this.size = size;

		this.contents = new ArrayList<Item>(this.size);
	}
	
	// TODO does not copy contents, should it?
	protected Container(final Container template) {
		super(template);
		
		this.item_type = ItemTypes.CONTAINER;
		
		this.size = template.size;
		
		this.contents = new ArrayList<Item>(this.size);
		
		this.displayWidth = template.displayWidth;
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * @param dbref
	 * @param name
	 * @param flags
	 * @param description
	 * @param location
	 * @param size
	 */
	public Container(final int dbref, final String name, final EnumSet<ObjectFlag> flags, final String description, final int location, final int size)
	{
		super(dbref, name, flags, description, location);
		
		this.item_type = ItemTypes.CONTAINER;
		
		this.size = size;
		
		this.contents = new ArrayList<Item>(this.size);
	}
	
	@Override
	public Double getWeight() {
		final Double containerWeight = super.getWeight();
		
		Double temp = 0.0;
		
		for(final Item item : this.contents) {
			temp += item.getWeight();
		}
		
		return (containerWeight + temp);
	}
	
	public int getDisplayWidth() {
		return this.displayWidth;
	}
	
	public Item retrieve(final String itemName) {
		Item item = null;
		
		// check display width, change if necessary (shorten)
		// this means each item must track how wide it's name/string version is
		// note: will retrieve only the first item if no specifier
		
		for (final Item item1 : this.contents) {
			if (item1.getName().equals(itemName)) {
				this.weight -= item1.getWeight();
				
				return this.contents.remove(this.contents.indexOf(item));
			}
		}
		
		return null;
	}
	
	public Item retrieve(int index) {
		Item item = null;
		
		/*check display width, change if necessary (shorten)
		 * this means each item must track how wide it's name/string version is
		 * note: will retrieve only the first item if no specifier
		 */
		for (final Item item1 : this.contents) {
			if (item1.getDBRef() == index) {
				item = this.contents.remove(index);
			}
		}
		
		return item;
	}
	
	public boolean insert(final Item item) {
		boolean success = false;
		
		if( !isFull() ) {
			this.weight += item.getWeight();
			this.contents.add(item);
			success = true;
		}
		
		return success;
	}
	
	public boolean isEmpty() {
		return this.contents.size() == 0;
	}
	
	public boolean isFull() {
		return this.contents.size() == this.size;
	}
	
	public List<Item> getContents() {
		return Collections.unmodifiableList(this.contents);
	}
	
	@Override
	public Container getCopy() {
		return new Container(this);
	}
	
	@Override
	public String toDB() {
		/*final StringBuilder sb = new StringBuilder();
		
		int n = 0;
		
		for(final Item item : this.contents) {
			sb.append(item.getDBRef());
			if( n < this.contents.size() ) sb.append(",");	
			n++;
		}*/
		
		final String[] output = new String[1];
		
		output[0] = "" + this.size;
		//output[1] = sb.toString();
		
		/*
		 * NOTE: this uses the toDB() method of the parent Item and simply appends the container size to the end 
		 */
		return super.toDB() + "#" + Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		return this.getName();
	}
}