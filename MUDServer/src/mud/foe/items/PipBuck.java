package mud.foe.items;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mud.Command;
import mud.Constants;
import mud.MUDObject;

import mud.foe.FOEItemTypes;
import mud.foe.FOESlotTypes;
import mud.foe.misc.Device;
import mud.foe.misc.FileSystem;
import mud.foe.misc.Module;
import mud.foe.misc.Tag;

import mud.interfaces.ExtraCommands;
import mud.misc.Slot;
import mud.net.Client;

import mud.objects.Item;
import mud.objects.ItemTypes;
import mud.objects.Player;
import mud.objects.Room;

import mud.utils.Time;
import mud.utils.Utils;

// TODO need to deal with these commands having a null parent here...
public class PipBuck extends Item implements Device, ExtraCommands {
	private Integer id;
	private String name;
	private FileSystem fs;

	private List<Module> modules;
	private List<Tag> tags;

	// TODO these should probably be private variables
	boolean efs_enabled = false;

	private int max_power = 6;
	private int current_power = 6;

	private static final Map<String, Command> commands = new Hashtable<String, Command>();
	
	{
		commands.put("enable", new Command("enable a module") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				if( p != null ) {
					if( arg.equalsIgnoreCase("efs") ) {
						p.efs_enabled = true;
						send("Eyes-Forward Sparkle ENABLED", client);
						return;
					}

					// get the intended module
					final Module module = p.getModule(arg);

					// enable the module
					if( module != null ) {
						if( module.getPowerReq() <= p.current_power) {
							send("Enabling Module: " + module.getModuleName(), client);

							p.enableModule( module );

							if( module instanceof ExtraCommands ) {
								ExtraCommands ec = (ExtraCommands) module;

								for(Map.Entry<String, Command> cmdE : ec.getCommands().entrySet()) {
									final String text = cmdE.getKey();
									final Command cmd = cmdE.getValue();

									initCmd(cmd);

									player.addCommand( cmdE.getKey(), cmdE.getValue() );
									
									final String moduleName = module.getModuleName();

									debug("Added " + cmdE.getKey() + " to player's command map from " + p.getName() + " module: " + moduleName);
								}
							}
						}
						else send("PipBuck: Insufficent power to enable ", client);
					}
				}
			}
			public int getAccessLevel() { return Constants.USER; }
		});

		commands.put("disable", new Command("disable a module") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				if( p != null ) {
					if( arg.equalsIgnoreCase("efs") ) {
						p.efs_enabled = false;
						send("Eyes-Forward Sparkle DISABLED", client);
						return;
					}

					// get the intended module
					final Module module = p.getModule(arg);

					// disable the module
					if( module != null ) {
						final String moduleName = module.getModuleName();
						
						if( module instanceof ExtraCommands ) {
							ExtraCommands ec = (ExtraCommands) module;

							for(Map.Entry<String, Command> cmd : ec.getCommands().entrySet()) {
								//p.commands.remove( cmd.getKey() );
								player.removeCommand( cmd.getKey() );
								
								debug("Removed " + cmd.getKey() + " from " + p.getName() + " module: " + moduleName + " from player's command map.");
							}
						}

						send("Disabling Module: " + moduleName, client);

						p.disableModule( module );
					}
				}
			}
		});
		
		commands.put("charge", new Command("charge a module") {
			@Override
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				if( p != null ) {
					// get the intended module
					final Module module = p.getModule(arg);

					// enable the module
					if( module != null ) {
						final String moduleName = module.getModuleName();

						if( module.isEnabled() ) {
							if( module.requiresCharging() ) {
								if( !module.isCharged() ) {
									if( !module.isCharging() ) {
										this.scheduleAtFixedRate(module.charge(), 1000, 1); // kludge

										send("charging your " + moduleName.toLowerCase() + "...", client);
									}
									else send("your " + moduleName.toLowerCase() + " is charging.", client);
								}
								else {
									send("your " + moduleName.toLowerCase() + " is fully charged.", client);
								}
							}
						}
					}
					else {
						for(final Module mod : modules) {
							if( mod.requiresCharging() ) {
								send(mod.getModuleName().toLowerCase() + ": " + mod.getCharge(), client);
							}
						}
					}
				}
			}
		});

		commands.put("register", new Command("register a pipbuck tag") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				// register <tag name>=<tag id>
				if( p != null ) {
					final String[] args = arg.split("=");

					if( args.length == 2 ) {
						final String tagName = args[0];
						final Integer tagId = Utils.toInt(args[1], -1);

						if( tagId != -1 ) {
							p.setTag(tagName, tagId);
						}
					}
				}
			}
		});

		commands.put("slot", new Command("attach a module to your device") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				if( p != null ) {
					// get modules list
					final List<Module> modules = p.getModules();

					Module module = null;
					Item item = null;

					for(final Item item2 : player.getInventory()) {
						if( item2 != null ) {
							if(item2 instanceof Module) {
								debug("Found a Module - \'" + item2.getName() + "\'");

								item = item2;
								module = (Module) item;
								
								final String moduleName = module.getModuleName();
								
								if( !modules.contains(module) && moduleName.equalsIgnoreCase(arg) ) {
									break;
								}
							}
							else debug("Not a Module - \'" + item2.getName() + "\'");
						}
					}

					player.getInventory().remove( item );

					// slot module
					if( module != null ) {
						final String moduleName = module.getModuleName();
						
						p.addModule( module );
						
						send("You slot the " + moduleName + " into your Pipbuck.", client);
					}
					else send("You don't have such a module.", client);
				}
			}
		});

		commands.put("tags", new Command("list your registed pipbuck tags") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				if( p != null ) {
					send("--- Tags", client);

					for(final Tag tag : p.tags) {
						send("" + tag, client);
					}
				}
			}

		});
		commands.put("unslot", new Command("detach a module from your device") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				if( p != null ) {
					// NOTE: we know that modules are Items, so this is reasonably safe...
					final Module module = p.getModule(arg);
					final Item item = (Item) module;

					// unslot module
					if( module != null ) {
						final String moduleName = module.getModuleName();
						
						p.removeModule( module );
						
						send("You unslot the " + moduleName + " from your Pipbuck.", client);
						
						player.getInventory().add( item );
					}
					else send("You don't have such a module.", client);
				}
			}
		});

		commands.put("modules", new Command("list the modules attached to your device") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				if( p != null ) {
					final List<Module> modules = p.getModules();

					if( modules.size() > 0 ) {
						send("Modules", client);
						
						for(Module module : modules) {
							send(module.getModuleName(), client);
						}
					}
					else send("No Modules", client);
				}
			}
		});

		commands.put("vp",
				new Command("view pipbuck") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				if( p != null ) {
					if( arg.equals("") ) {
						// get the current amount of ambient radiation (rads/sec)
						int rads = 0;
						
						final Room room = getRoom( player.getLocation() );
						
						if( room.hasProperty("_game/rads") ) {
							rads = Integer.parseInt( room.getProperty("_game/rads") );
						}
						
						// TODO really need a nice utility function for this (did it elsewhere in main class I think)
						final Time game_time = getGameTime();

						int hours = game_time.hour;
						int minutes = game_time.minute;
						
						String time = (hours < 9 ? " " : "") + hours + ":" + (minutes < 9 ? "0" : "") + minutes;
						
						// TODO consider using String.format for time here

						send("Looking at your Pipbuck, you note that: ", client);

						send(Utils.padRight("", '-', 40), client);

						send("the time is " + time, client);
						send("current radiation exposure is: " + rads + " rads/sec.", client);
						
						final StringBuilder sb = new StringBuilder();

						for(final Module module : p.getModules()) {
							final String moduleName = module.getModuleName();
							
							String cnString;
							
							if( module.isEnabled() ) cnString = colors(moduleName, "green");
							else                     cnString = colors(moduleName, "yellow");
							
							sb.append(cnString).append(", ");
						}

						if( sb.length() > 2 ) sb.delete(sb.length() - 2, sb.length());

						send("Currently slotted modules: ", client);
						send(sb.toString(), client);

						final int curr_p = p.getPower();
						final int max_p = p.getMaxPower();

						sb.delete(0, sb.length());

						sb.append( Utils.padRight("",  '|', curr_p) );
						sb.append( Utils.padRight("", ' ', max_p - curr_p) );
						
						String power_s = sb.toString();
						
						//send("Remaining Power: [" + colors(sb.toString(), "green") + "]" + " ", client);

						sb.delete(0, sb.length());
						
						final Module module = p.getModule("StealthBuck");
						
						String stealth_s = (module != null && player.hasEffect("stealth")) ? " STEALTH" : "";
						
						send("Remaining Power: [" + colors(power_s, "green") + "]" + stealth_s , client);

						send(Utils.padRight("", '-', 40), client);
					}
					else if( arg.equalsIgnoreCase("items") ) {
						send(Utils.padRight("", '-', 40), client);

						for(final Item item : player.getInventory()) {
							if( item.getItemType() == ItemTypes.ARMOR ) {
								send(colors(item.getName(), "red"), client);
							}
							else if( item.getItemType() == ItemTypes.BOOK ) {
								send(colors(item.getName(), "cyan"), client);
							}
							else if( item.getItemType() == ItemTypes.WEAPON ) {
								send(colors(item.getName(), "purple2"), client);
							}
							else {
								send(colors(item.getName(), "yellow"), client);
							}
						}

						send(Utils.padRight("", '-', 40), client);
					}
				}
			}
		});

		// scan/efs
		commands.put("efs", new Command("eyes-forward sparkle") {
			public void execute(final String arg, final Client client) {
				final Player player = getPlayer(client);      // get player
				final PipBuck p = PipBuck.getPipBuck(player); // get pipbuck

				if( p != null ) {
					if( p.efs_enabled ) {
						// the idea here is to look for living things and/or pipbuck tags and mark them as:
						// hostile, neutral, friendly (red, yellow, green)
						final Room room = getRoom( player.getLocation() );

						// get a list of all living creatures in range
						final List<MUDObject> list = new LinkedList<MUDObject>();

						// for each creature decide if they are a threat
						for(final MUDObject obj : list) {
						}

						// it'd be nice to use tags to see if we know/know of any of the identified creatures
						// scan for other pipbucks
						// check against known ids (tags are set/acquired and associated with device ids)
					}
				}
			}
		});
	}

	public PipBuck(final String name) {
		this(-1, name);
	}

	public PipBuck(final int dbref, final String name) {
		super(dbref, name, "A Stable-Tec PipBuck");

		this.item_type = FOEItemTypes.PIPBUCK;
		this.slot_type = FOESlotTypes.LFHOOF;

		this.equippable = true;

		this.fs = new FileSystem();
		this.modules = new LinkedList<Module>();
	}

	protected PipBuck(final PipBuck template) {
		super( template );
		
		this.item_type = FOEItemTypes.PIPBUCK;
		this.slot_type = FOESlotTypes.LFHOOF;
		
		this.fs = template.fs; // TODO need to give FileSystem a clone method
		this.fs = new FileSystem();
		this.modules = new LinkedList<Module>();
	}

	/*public String getName() {
		return this.name;
	}*/

	public String getDeviceName() {
		return "";
	}

	public DeviceType getDeviceType() {
		return DeviceType.PIPBUCK;
	}

	/**
	 * Add a module to the PipBuck
	 * 
	 * @param mod
	 * @return
	 */
	public boolean addModule(Module mod) {
		return this.modules.add(mod);
	}

	/**
	 * Remove a module from the PipBuck
	 * 
	 * @param mod
	 * @return
	 */
	public boolean removeModule(Module mod) {
		return this.modules.remove(mod);
	}

	public Module getModule(final String moduleName) {
		for(final Module module : modules) {
			if( moduleName.equalsIgnoreCase( module.getModuleName()) ) {
				return module;
			}
		}

		return null;
	}

	@Override
	public List<Module> getModules() {
		return this.modules;
	}

	private void enableModule(final Module module) {
		this.current_power -= module.getPowerReq();
		module.enable();
	}

	private void disableModule(final Module module) {
		module.disable();
		this.current_power += module.getPowerReq();
	}
	
	public Map<String, Command> getCommands() {
		return PipBuck.commands;
	}

	public int getPower() {
		return this.current_power;
	}

	public int getMaxPower() {
		return this.max_power;
	}

	public boolean setTag(final String tag, final Integer id) {
		boolean valid_tag = true;
		
		// test for appropriate format
		if( tag.length() != 10 ) {
			valid_tag = false;
		}
		else {
			final String alpha = tag.substring(0, 3);
			final String numeric = tag.substring(3, tag.length());

			for(final char ch : alpha.toCharArray()) {
				if( !Character.isLetter(ch) ) valid_tag = false;
			}

			for(final char ch : numeric.toCharArray()) {
				if( !Character.isDigit(ch) ) valid_tag = false;
			}
		}
		
		if( valid_tag ) {
			this.tags.add( new Tag(tag, id) );
		}
		
		return valid_tag;
	}

	@Override
	public String toString() {
		return name + "(" + type.toString() + ")";
	}
	
	@Override
	public PipBuck getCopy() {
		return new PipBuck(this);
	}

	static final PipBuck getPipBuck(final Player player) {
		PipBuck p = null;

		final Slot slot = player.getSlots().get("special");
		final Slot slot1 = player.getSlots().get("special2");

		if( slot != null && !slot.isEmpty() )       p = (PipBuck) slot.getItem();
		else if( slot1 != null && !slot.isEmpty() ) p = (PipBuck) slot.getItem();

		return p;
	}
}