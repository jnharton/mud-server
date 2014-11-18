package mud.utils;

import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.items.Armor;
import mud.objects.items.Weapon;

public class ItemFactory {
	public static Item createItem(ItemType type) {
		Item item = null;
		
		switch(type) {
		case ARMOR:
			item = new Armor();
			break;
			
		case WEAPON:
			item = new Weapon();
			break;
			
		default:
			// throw some exception
			break;
		}
		
		return item;
	}
}