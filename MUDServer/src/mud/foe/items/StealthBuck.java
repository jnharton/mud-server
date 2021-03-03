package mud.foe.items;

import java.util.Hashtable;
import java.util.Map;
import java.util.TimerTask;

import mud.Command;
import mud.foe.FOEItemTypes;
import mud.foe.FOESlotTypes;
import mud.foe.FOEGameUtils;
import mud.foe.misc.Module;
import mud.interfaces.ExtraCommands;
import mud.misc.Effect;
import mud.misc.Effect.DurationType;
import mud.net.Client;
import mud.objects.Item;
import mud.objects.Player;

/**
 * The StealthBuck modules provides the following functionality via
 * a PipBuck:
 * - time-limited "invisibility" in the visible frequencies of light
 * - primitive masking of electronic signals (hides your pipbuck tags)
 * @author Jeremy
 *
 */
public final class StealthBuck extends Item implements ExtraCommands, Module {
	private static final Effect stealth = new Effect("stealth", null, DurationType.PERMANENT, -1);
	private static final Effect jam = new Effect("Jam", null, DurationType.PERMANENT, -1);

	private boolean enabled;

	private boolean charged;
	private boolean charging;

	private int charge;
	private int maxCharge = 50;

	private boolean stealth_mode = false;
	
	private TimerTask tt;

	private static final Map<String, Command> commands = new Hashtable<String, Command>();

	{
		commands.put("stealth", new Command("turn stealth field ON or OFF.") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);        // get player
				final PipBuck p = FOEGameUtils.getPipBuck(player); // get pipbuck

				if( p != null ) {
					final Module module = p.getModule("StealthBuck");

					if( module != null ) {
						if( stealth_mode ) {
							tt.cancel();

							//
							//player.removeEffect( new Effect("stealth") );
							player.removeEffect(stealth);
							//removeEffect(player, "stealth");

							stealth_mode = false;

							send("Stealth Field DISABLED", client);

							//

							charging = true;

							tt = new TimerTask() {
								@Override
								public void run() {
									if( charging ) {
										if( charge < maxCharge ) {
											charge += 1;
										}
										else {
											charging = false;
										}
									}
									else cancel();
								}
							};

							this.scheduleAtFixedRate(tt, 0, 1000);
						}
						else {
							if( charge > 0 ) {
								tt.cancel();

								//

								send("Stealth Field ENABLED", client);

								stealth_mode = true;

								applyEffect(player, new Effect(stealth));

								//

								charging = false;
								charged = false;

								tt = new TimerTask() {
									@Override
									public void run() {
										if( !charging ) {
											if( charge > 0 ) {
												charge -= 1;
											}
											else {
												//player.removeEffect("stealth");
												player.removeEffect(stealth);

												stealth_mode = false;

												send("Stealth Field DISABLED", client);

												cancel();
											}
										}
									}
								};

								this.scheduleAtFixedRate(tt, 0, 1000);
							}
							else send("stealth: no charge", client);
						}
					}
				}
			}
		});
	}

	public StealthBuck() {
		this(-1);
	}

	public StealthBuck(int dbref) {
		super(dbref, "StealthBuck", "");

		this.item_type = FOEItemTypes.STEALTH_BUCK;
		this.slot_type = FOESlotTypes.NONE;

		this.enabled = false;
		this.charged = false;
		this.charging = false;

		this.charge = 0;
	}

	protected StealthBuck(final StealthBuck template) {
		super( template );

		this.item_type = FOEItemTypes.STEALTH_BUCK;
		this.slot_type = FOESlotTypes.NONE;

		this.enabled = false;
		this.charged = false;
		this.charging = false;

		this.charge = 0;
	}

	public Map<String, Command> getCommands() {
		return StealthBuck.commands;
	}

	@Override
	public String getModuleName() {
		return "StealthBuck";
	}

	public int getVersion() {
		return 0;
	}

	public int getPowerReq() {
		return 3;
	}

	public void enable() {
		if( !this.enabled ) {
			this.enabled = true;
		}
	}

	public void disable() {
		if( this.enabled ) {
			this.enabled = false;
			this.charging = false;
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
	
	@Override
	public StealthBuck getCopy() {
		return new StealthBuck(this);
	}
	
	@Override
	public String toString() {
		return getName();
	}
}