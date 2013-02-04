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
			if (i.getName().equals(name)) {
				return i;
			}
			else if (i.getName().equals(name.toLowerCase()))
			{
				return i;
			}
		}
		return null;
	}
	
	public String toDB() {
		String[] output = new String[8];
		output[0] = this.getDBRef() + "";           // pack database reference number
		output[1] = this.getName();                 // pack name
		output[2] = this.getFlagsAsString();        // pack flags
		output[3] = this.getDesc();                 // pack description
		output[4] = this.getLocation() + "";        // pack location
		output[5] = this.item_type.ordinal() + "";  // item type
		output[6] = "*";                            // nothing (placeholder)
		output[7] = "*";                            // nothing (placeholder)
		return Utils.join(output, "#");
	}
}