package mud.foe.items;

import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map;

import mud.Command;
import mud.Constants;
import mud.ObjectFlag;
import mud.TypeFlag;
import mud.foe.FOEItemTypes;
import mud.foe.FOESlotTypes;
import mud.foe.misc.Module;
import mud.interfaces.ExtraCommands;
import mud.misc.Effect;
import mud.misc.Effect.DurationType;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.objects.Player;

/**
 * The StealthBuck modules provides the following functionality via
 * a PipBuck:
 * - time-limited "invisibility" in the visible frequencies of light
 * - primitive masking of electronic signals (hides your pipbuck tags)
 * @author Jeremy
 *
 */
public final class StealthBuck extends Item implements Module, ExtraCommands {
	private static final Effect stealth = new Effect("stealth", null, DurationType.PERMANENT, -1);
	private static final Effect jam = new Effect("Jam", null, DurationType.PERMANENT, -1);
	
	private boolean enabled;
	
	private boolean charged;
	private boolean charging;
	private int charge;
	
	private boolean stealth_mode = false;
	
	private Map<String, Command> commands = new Hashtable<String, Command>() {
		{
			put("stealth", new Command("turn stealth field ON or OFF.") {
				public void execute(final String arg, final Client client) {
					final Player player = getPlayer(client);
					
					if( stealth_mode ) {
						stealth_mode = false;
						charging = true;
						
						player.removeEffect("stealth");
						
						send("Stealth Field DISABLED", client);
					}
					else {
						if( charge > 0 ) {
							charging = false;
							charged = false;
							stealth_mode = true;
							
							applyEffect(player, new Effect(stealth));
							
							send("Stealth Field ENABLED", client);
						}
					}
				}
				
				public int getAccessLevel() { return Constants.USER; }
			});
		}
	};
	
	public StealthBuck() {
		this(-1);
	}
	
	public StealthBuck(int dbref) {
		super(dbref, "StealthBuck", "");
		
		this.item_type = FOEItemTypes.STEALTH_BUCK;
		this.equip_type = ItemTypes.NONE;
		this.slot_type = FOESlotTypes.NONE;
		
		this.enabled = false;
		this.charged = false;
		this.charging = false;
		
		this.charge = 0;
	}

	@Override
	public String getName() {
		return "StealthBuck";
	}
	
	public int getVersion() {
		return 0;
	}
	
	public int getPowerReq() {
		return 3;
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