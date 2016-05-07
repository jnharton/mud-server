package mud.objects.items;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.objects.Item;
import mud.objects.ItemTypes;

public class Clothing extends Item {
	/**
	 * Additional stuff that I need to figure into the persistence:
	 * - modifiers
	 * - type
	 * - some kind of prototype object id (so I can just say, go find that object and use
	 * it's properties)
	 * 	- this would mean I'd need prototype objects either elsewhere or that are normally loaded
	 *  - possibly I could add a static array/table of some kind that I will modify on startup
	 *  to include all possible prototypes (i.e. the ones that actually exist)
	 */

	// type - cloak, boots, pants, shirt, undergarment?
	
	public Clothing() {
		this("clothing", "a piece of clothing");
	}
	
	public Clothing(final String name, final String description) {
		super(-1, name, description);
		
		this.item_type = ItemTypes.CLOTHING;
		
		this.equippable = true;
		
		this.weight = 0.0;                 // the weight of the clothing
	}
	
	protected Clothing(final Clothing template) {
		super(template);
		// TODO make sure to properly do a deep copy
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other constructors
	 * that has parameters.
	 * 
	 * @param tempDBREF
	 * @param tempName
	 * @param tempDesc
	 * @param tempLoc
	 */
	public Clothing(int tempDBREF, String tempName, String tempDesc, int tempLoc) {
		super(tempDBREF, tempName, EnumSet.noneOf(ObjectFlag.class), tempDesc, tempLoc);
		
		this.type = TypeFlag.ITEM;
		
		this.item_type = ItemTypes.CLOTHING;
		
		this.equippable = true;
		
		this.weight = 0.0;                 // the weight of the clothing
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Clothing getCopy() {
		return new Clothing(this);
	}
	
	@Override
	public String toDB() {
		//final String[] output = new String[10];	
		//return Utils.join(output, "#");
		
		return super.toDB();
	}
	
	@Override
	public String toString() {
		return getName();
	}
}