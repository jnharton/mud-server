package mud.foe.items;

import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

import mud.Command;
import mud.Constants;
import mud.MUDObject;
import mud.ObjectFlag;
import mud.TypeFlag;
import mud.foe.FOEItemTypes;
import mud.foe.FOESlotTypes;
import mud.foe.misc.Module;
import mud.interfaces.ExtraCommands;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.ItemType;
import mud.objects.ItemTypes;

/**
 * The disruptor module provides the following functionality via
 * a PipBuck:
 * - scrambling of EFS (Eyes-Forward Sparkle) units within range
 * - on a unarmed touch attack it can be used to disable high tech
 * equipment such as power armor and robots
 * @author Jeremy
 *
 */
public class Disruptor extends Item implements Module, ExtraCommands {
	private boolean enabled;
	private boolean charged;
	private boolean charging;
	
	private int charge;
	
	private Map<String, Command> commands = new Hashtable<String, Command>() {
		{
			put("disrupt", new Command(MUDObject.parent, "Emit a disruption pulse.") {
				public void execute(final String arg, final Client client) {
					send("disrupt: command not implemented", client);
				}
				public int getAccessLevel() { return Constants.USER; }
			});
		}
	};
	
	public Disruptor() {
		this(-1);
	}
	
	public Disruptor(int dbref) {
		super(dbref, "Disruptor", EnumSet.noneOf(ObjectFlag.class), "", -1);
		
		this.type = TypeFlag.ITEM;
		
		this.item_type = FOEItemTypes.DISRUPTOR;
		this.equip_type = ItemTypes.NONE;
		this.slot_type = FOESlotTypes.NONE;
		
		this.enabled = false;
		this.charged = false;
		this.charging = false;
		
		this.charge = 0;
	}

	@Override
	public String getName() {
		return "Disruptor";
	}
	
	/**
	 * The concept here is that there could be different ones
	 */
	public int getVersion() {
		return 0;
	}
	
	public int getPowerReq() {
		return 6;
	}
	
	public void enable() {
		if( !enabled ) {
			enabled = true;
			init();
		}
	}
	
	public void disable() {
		if( enabled ) {
			enabled = false;
			deinit();
		}
	}
	
	public void init() {
		charging = true;
	}
	
	public void deinit() {
		charging = false;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	public Map<String, Command> getCommands() {
		return this.commands;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}