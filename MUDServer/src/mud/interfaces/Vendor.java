package mud.interfaces;

import java.util.ArrayList;

import mud.objects.Item;

public interface Vendor {
	public ArrayList<Item> list();
	
	public Item buy(String name);
	
	public void sell(Item item);

	public boolean hasItem(String arg);

	public Item getItem(String arg);
}