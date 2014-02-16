package mud.interfaces;

import java.util.Map;
import mud.objects.Item;
import mud.objects.Thing;

public interface Module {
	public void init(); // initialize the module
	public Map<String, Item> getItemPrototypes();
	public Map<String, Thing> getThingPrototypes();
}