package mud.objects.items;

import java.util.ArrayList;

import mud.objects.Item;
import mud.utils.Utils;

public class Pack extends Clothing {
	/**
	 * 
	 */

	ArrayList<Item> contents;
	int size = 10;     // 10 items
	int capacity = 50; // 50lbs.

	public Pack() {
		this.contents = new ArrayList<Item>(size);
	}

	public void put(Item i) {
		if (this.contents.size() < size) {
			this.contents.add(i);
		}
	}

	public void put(String name) {

	}

	public Item get(int index) {
		Item i = this.contents.get(index);
		this.contents.remove(i);
		return i;
	}

	public Item search(String name) {
		for (Item i : this.contents) {
			if (i.getName().equals(name) || i.getName().equals(name.toLowerCase())) {
				return i;
			}
		}
		return null;
	}
	
	@Override
	public String toDB() {
		//final String[] output = new String[10];
		//return Utils.join(output, "#");
		
		return super.toDB();
	}
}