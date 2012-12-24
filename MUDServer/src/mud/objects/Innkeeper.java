package mud.objects;

import java.util.ArrayList;

import mud.MUDServer;
import mud.interfaces.*;

public class Innkeeper extends NPC implements Vendor {
	
	private MUDServer parent;
	private ArrayList<Item> stock;
	
	public Innkeeper(MUDServer parent) {
		this.parent = parent;
		this.stock = new ArrayList<Item>();
	}

	public ArrayList<Item> list() {
		return null;
	}

	public Item buy(String name) {
		return null;
	}

	public void sell(Item item) {
	}
	
	public boolean hasItem(String name) {
		for(Item item : this.stock) {
			if(item.getName().equals(name) == true) {
				return true;
			}
		}
		
		return false;
	}

	public Item getItem(String name) {
		for(Item item : this.stock) {
			if(item.getName().equals(name) == true) {
				return item;
			}
		}
		
		return null;
	}
}