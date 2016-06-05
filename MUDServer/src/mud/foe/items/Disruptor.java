package mud.foe.items;

import java.util.Hashtable;
import java.util.Map;
import java.util.TimerTask;

import mud.Command;
import mud.MUDObject;
import mud.foe.FOEItemTypes;
import mud.foe.FOESlotTypes;
import mud.foe.misc.Module;
import mud.interfaces.ExtraCommands;
import mud.misc.Effect;
import mud.misc.Effect.DurationType;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.Player;

/**
 * The disruptor module provides the following functionality via
 * a PipBuck:
 * - scrambling of EFS (Eyes-Forward Sparkle) units within range
 * - on a unarmed touch attack it can be used to disable high tech
 * equipment such as power armor and robots
 * @author Jeremy
 *
 */
public class Disruptor extends Item implements ExtraCommands, Module {
	// type: electric? magic?
	private static final Effect disrupt = new Effect("disrupt", null, DurationType.INSTANTANEOUS, -1);
	
	private boolean enabled;
	
	private boolean charged; // fully charged
	private boolean charging;
	
	private int charge;
	private int maxCharge = 30;
	
	private TimerTask tt;

	private static final Map<String, Command> commands = new Hashtable<String, Command>();

	{		
		commands.put("disrupt", new Command("Emit a disruption pulse.") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				
				if( charge >= 10 ) {
					final MUDObject target = player.getTarget();

					if( target != null ) {
						charge = charge - 10;
						
						if( charged ) charged = false;
						
						applyEffect(target, new Effect(disrupt));
					}
				}
				else {
					send("disrupt: insufficient charge.", client);
				}
			}
		});
	}

	public Disruptor() {
		this(-1);
	}

	public Disruptor(int dbref) {
		super(dbref, "Disruptor", "");
		
		this.item_type = FOEItemTypes.DISRUPTOR;
		this.slot_type = FOESlotTypes.NONE;
		
		this.enabled = false;
		
		this.charged = false;
		this.charging = false;
		
		this.charge = 0;
	}
	
	protected Disruptor(final Disruptor template) {
		super(template);
		
		this.item_type = FOEItemTypes.DISRUPTOR;
		this.slot_type = FOESlotTypes.NONE;
		
		this.enabled = false;
		
		this.charged = false;
		this.charging = false;
		
		this.charge = 0;
	}
	
	public Map<String, Command> getCommands() {
		return Disruptor.commands;
	}

	@Override
	public String getModuleName() {
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
		if( !this.enabled ) {
			this.enabled = true;
		}
	}
	
	public void disable() {
		if( this.enabled ) {
			this.enabled = false;
			
			// stop charging
			if( this.charging ) {
				this.tt.cancel();
				
				this.charging = false;
			}
		}
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
	
	@Override
	public boolean requiresCharging() {
		return true;
	}
	
	@Override
	public TimerTask charge() {
		this.charging = true;
		
		this.tt = new TimerTask() {
			@Override
			public void run() {
				if( charging ) {
					if( charge < maxCharge ) {
						charge += 1;
					}
					else {
						charging = false;
						charged = true;
					}
				}
				else cancel();
			}
		};
		
		return this.tt;
	}
	
	@Override
	public int getCharge() {
		return this.charge;
	}

	@Override
	public boolean isCharged() {
		return this.charged;
	}

	@Override
	public boolean isCharging() {
		return this.charging;
	}
	
	public Disruptor getCopy() {
		return new Disruptor(this);
	}
	
	@Override
	public String toString() {
		return getName();
	}
}