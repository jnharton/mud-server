package mud.objects.items;

import java.util.ArrayList;
import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;

import mud.interfaces.Equippable;
import mud.interfaces.Storage;

import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;

import mud.utils.Utils;

public class Container extends Item implements Storage<Item>, Equippable<Item> {
	
	private int size = 5;
	private boolean accessible;
	private boolean full;
	
	private ArrayList<Item> contents;

	private int displayWidth = 30; // the width for the container display box (check whenever something added)
	private Character top = '-';
	private Character side = '|';
	private Character bottom = '-';

	public Container() {
		super(-1, "Container", EnumSet.noneOf(ObjectFlag.class), "A generic container", 4);
		this.type = TypeFlag.ITEM;
		
		this.equippable = false;
		this.equip_type = ItemType.CONTAINER; // the type of equipment it is
		this.item_type = ItemType.CONTAINER;
		
		this.weight = 1.0;
		
		this.accessible = true;
		this.full = false;
		
		this.contents = new ArrayList<Item>(5);
	}
	
	// initial size
	public Container(int size) {
		this();
		
		this.size = size;
	}
	
	// initial size, base weight
	public Container(int size, double weight) {
		this(size);
		
		this.weight = weight;
	}

	public Container(String name) {
		this(5, 0.0);
		this.name = name;
		
		this.equippable = false;
		this.equip_type = ItemType.CONTAINER; // the type of equipment it is
		this.item_type = ItemType.CONTAINER;
		
		this.weight = 1.0;
		
		this.accessible = true;
		this.full = false;
		
		this.contents = new ArrayList<Item>(5);
	}
	
	public Container(String name, int size) {
		this(size, 0.0);
		this.name = name;
	}
	
	public Container(String name, int size, double weight) {
		this(size, weight);
		this.name = name;
	}
	
	public ArrayList<String> look() {
		ArrayList<String> result = new ArrayList<String>();
		
		result.add(name + " (#" + getDBRef() + ")");
		result.add(this.desc);
		for (Item item : contents) {
			result.add(item.getName() + " (#" + item.getName() + ")");
		}
		
		return result;
	}
	
	public Item retrieve(int index) {
		/*check display width, change if necessary (shorten)
		 * this means each item must track how wide it's name/string version is
		 * note: will retrieve only the first item if no specifier
		 */
		for (Item item : this.contents) {
			if (item.getDBRef() == index) {
				return this.contents.get(index);
			}
		}
		
		return null; 
	}

	
	public Item retrieve(String itemName) {
		// check display width, change if necessary (shorten)
		// this means each item must track how wide it's name/string version is
		// note: will retrieve only the first item if no specifier
		for (Item item : this.contents) {
			if (item.getName().equals(itemName)) {
				this.weight -= item.getWeight();
				return this.contents.remove(this.contents.indexOf(item));
			}
		}
		return null;
	}
	
	public void insert(Item item) {
		// check display width, change if necessary (shorten/lengthen)
		this.contents.add(item);
		this.weight += item.getWeight();
	}
	
	@Override
	public void equip() {
		// TODO Auto-generated method stub

	}

	@Override
	public void equip(Player p) {
		// TODO Auto-generated method stub
	}

	@Override
	public Container unequip() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* internal ArrayList wrapper functions */
	
	public void add(Item element) {
		this.contents.add(element);
	}
	
	public Item remove(int index) {
		return this.contents.remove(index);
	}
	
	public boolean contains(Item element) {
		return this.contents.contains(element);
	}
	
	public int indexOf(Item element) {
		return this.contents.indexOf(element);
	}

	public boolean isFull() {
		return this.full;
	}
	
	public String getTop() {
		return Utils.padRight("", top, displayWidth);
	}
	
	public String getSide() {
		return side.toString();
	}
	
	public String getBottom() {
		return Utils.padRight("", bottom, displayWidth);
	}
	
	public int getDisplayWidth() {
		return this.displayWidth;
	}
	
	public ArrayList<Item> getContents() {
		return this.contents;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";          // database reference number
		output[1] = this.getName();                // name
		output[2] = this.getFlagsAsString();       // flags
		output[3] = this.getDesc();                // description
		output[4] = this.getLocation() + "";       // location
		output[5] = this.item_type.ordinal() + ""; // item type
		return Utils.join(output, "#");
	}

	@Override
	public String getName() {
		return this.name;
	}
}