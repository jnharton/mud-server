package mud.objects.items;

import java.util.EnumSet;

import mud.ObjectFlag;
import mud.TypeFlag;
import mud.interfaces.Usable;
import mud.interfaces.Wearable;
import mud.misc.Effect;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;
import mud.utils.Utils;

public class Clothing extends Item implements Usable<Clothing>, Wearable<Clothing>
{
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

	// type - cloak, boots, pants, shirt, undergarment
	public ClothingType clothing;
	
	private Effect effect = null;
	
	public Clothing() {
		super(-1, "clothing", "a piece of clothing");
		
		/*this.name = "clothing";
		this.flags = EnumSet.noneOf(ObjectFlag.class);
		this.desc = "a piece of clothing";
		this.location = -1;*/
		
		this.item_type = ItemTypes.CLOTHING;
		this.equip_type = ItemTypes.CLOTHING; // the type of equipment it is
		
		this.equipped = false;
		this.equippable = true;
		
		this.clothing = ClothingType.NONE; // the actual type of clothing
		this.weight = 0.0;                 // the weight of the clothing
	}

	public Clothing(final String name, final String description, ClothingType cType, double cWeight)
	{
		super(-1, name, description);
		
		this.item_type = ItemTypes.CLOTHING;
		this.equip_type = ItemTypes.CLOTHING; // the type of equipment it is
		
		this.equipped = false;
		this.equippable = true;
		
		this.clothing = cType;               // the actual type of clothing
		this.weight = cWeight;               // the weight of the clothing
	}
	
	protected Clothing(final Clothing template) {
		super(template);
		
		this.clothing = template.clothing;
	}
	
	/**
	 * Object Loading Constructor
	 * 
	 * Use this only for testing purposes and loading objects into the
	 * server database, for anything else, use one of the other constructors
	 * that has parameters.
	 * @param tempDBREF
	 * @param tempName
	 * @param tempDesc
	 * @param tempLoc
	 * @param tCharges
	 * @param spellName
	 */
	public Clothing(int tempDBREF, String tempName, String tempDesc, int tempLoc, int cMod, ClothingType cType) {
		super(tempDBREF, tempName, EnumSet.noneOf(ObjectFlag.class), tempDesc, tempLoc);
		this.type = TypeFlag.ITEM;
		this.equippable = true;
		this.equip_type = ItemTypes.CLOTHING;
		this.item_type = ItemTypes.CLOTHING;
		
		this.clothing = cType;
	}
	
	public Effect getEffect() {
		return this.effect;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toDB() {
		String[] output = new String[10];
		
		output[0] = this.getDBRef() + "";         // clothing database reference number
		output[1] = this.getName();               // clothing name
		output[2] = this.getFlagsAsString();      // clothing flags
		output[3] = this.getDesc();               // clothing description
		output[4] = this.getLocation() + "";      // clothing location (a.k.a parent)
		output[5] = this.item_type.getId() + "";  // item type
		output[6] = this.equip_type.getId() + ""; // equip type
		output[7] = this.slot_type.getId() + "";  // slot type
		
		output[8] = this.clothing.ordinal() + ""; // clothing type
		output[9] = "*";                          // blank (modifier?)
		
		return Utils.join(output, "#");
	}
	
	@Override
	public String toString() {
		//return this.mod + " " + this.name;
		return getName();
	}
	
	@Override
	public Clothing clone() {
		return new Clothing(this);
	}
}