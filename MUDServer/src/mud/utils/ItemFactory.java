package mud.utils;

import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;
import mud.objects.items.Armor;
import mud.objects.items.Weapon;

public class ItemFactory {
	// TODO fix this kludge
	private static final int ARMOR = 0;
	private static final int WEAPON = 15;
	
	public static Item createItem(ItemType type) {
		Item item = null;
		
		switch(type.getId()) {
		case ARMOR:
			//item = new Armor();
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