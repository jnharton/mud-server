package mud.objects.items;

import java.lang.reflect.ParameterizedType;

import java.util.ArrayList;
import java.util.EnumSet;

import mud.ObjectFlag;

import mud.interfaces.Equippable;
import mud.interfaces.Storage;
import mud.interfaces.Wearable;

import mud.net.Client;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.Player;

import mud.utils.Utils;

public class Container<E extends Item> extends Item implements Storage<E>, Equippable<Item>, Wearable<Item> {
	
	Class contentType;
	
	private int size = 5;
	private boolean accessible;
	private boolean full;
	
	private ArrayList<E> contents;

	private int dispWidth = 30; // the width for the container display box (check whenever something added)
	private String top = "------------------------------";
	private String side = "|";
	private String bottom = "------------------------------";

	public Container() {
		super(-1, "Container", EnumSet.of(ObjectFlag.ITEM), "A generic container", 4);
		
		this.equippable = false;
		this.equip_type = ItemType.CONTAINER; // the type of equipment it is
		this.item_type = ItemType.CONTAINER;
		
		this.weight = 1.0;
		
		this.accessible = true;
		this.full = false;
		
		this.contents = new ArrayList<E>(5);
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
		for (E item : contents) {
			result.add(item.getName() + " (#" + item.getName() + ")");
		}
		
		return result;
	}
	
	public E retrieve(int index) {
		/*check display width, change if necessary (shorten)
		 * this means each item must track how wide it's name/string version is
		 * note: will retrieve only the first item if no specifier
		 */
		for (E item : this.contents) {
			if (item.getDBRef() == index) {
				return this.contents.get(index);
			}
		}
		
		return null; 
	}

	
	public E retrieve(String itemName) {
		// check display width, change if necessary (shorten)
		// this means each item must track how wide it's name/string version is
		// note: will retrieve only the first item if no specifier
		for (E item : this.contents) {
			if (item.getName().equals(itemName)) {
				this.weight -= item.getWeight();
				return this.contents.remove(this.contents.indexOf(item));
			}
		}
		return null;
	}
	
	public void insert(E i) {
		// check display width, change if necessary (shorten/lengthen)
		this.contents.add(i);
		this.weight += i.getWeight();
	}

	@Override
	public void wear(String arg, Client client) {
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
	public Container<E> unequip() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* internal ArrayList wrapper functions */
	
	public void add(E element) {
		this.contents.add(element);
	}
	
	public E remove(int index) {
		return this.contents.remove(index);
	}
	
	public boolean contains(E element) {
		return this.contents.contains(element);
	}
	
	public int indexOf(E element) {
		return this.contents.indexOf(element);
	}

	public boolean isFull() {
		return this.full;
	}
	
	public String getTop() {
		return top;
	}
	
	public String getSide() {
		return side;
	}
	
	public String getBottom() {
		return bottom;
	}
	
	public int getDisplayWidth() {
		return this.dispWidth;
	}
	
	public ArrayList<E> getContents() {
		return this.contents;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";          // potion database reference number
		output[1] = this.getName();                      // potion name
		output[2] = this.getFlagsAsString();                     // potion flags
		output[3] = this.getDesc();                      // potion description
		output[4] = this.getLocation() + "";       // potion location
		output[5] = this.item_type.ordinal() + ""; // item type
		return Utils.join(output, "#");
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}