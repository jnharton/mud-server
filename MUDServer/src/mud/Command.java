package mud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import mud.interfaces.ODBI;
import mud.magic.Spell;
import mud.misc.Effect;
import mud.misc.ProgramInterpreter;
import mud.net.Client;
import mud.objects.Creature;
import mud.objects.Exit;
import mud.objects.Item;
import mud.objects.NPC;
import mud.objects.Player;
import mud.objects.Room;
import mud.utils.EffectTimer;
import mud.utils.Message;
import mud.utils.SpellTimer;

/*
 * Copyright (c) 2012 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public abstract class Command {
	private MUDServer parent;
	private ODBI db;
	private String description;

	/**
	 * Construct a command object with a parent
	 * MUDServer. Cannot construct an ordinary Command
	 * object because it is an abstract class. This
	 * is effectively a dummy constructor for subclasses.
	 * 
	 * @param mParent
	 */
	protected Command(MUDServer mParent) {
		this.parent = mParent;
		this.db = this.parent.getDBInterface();
	}
	
	protected Command(MUDServer mParent, String description) {
		this(mParent);
		this.description = description;
	}
	
	/**
	 * execute
	 * 
	 * This is where the code that constitutes what the command
	 * does will go.
	 * 
	 * @param arg
	 * @param client
	 */
	public abstract void execute(String arg, Client client);
	
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * getAcessLevel
	 * 
	 * returns the specified access permissions required to use
	 * the command
	 * 
	 * note: default is USER
	 * 
	 * @return int representing access level
	 */
	/*public int getAccessLevel() {
		return Constants.USER;
	}*/
	
	public abstract int getAccessLevel();
	
	/**
	 * method that passes the thing to be sent for debugging to
	 * the MUDServer instance's send method, eliminating the
	 * need to prefix the command with 'parent.' anywhere but here.
	 * 
	 * @param toSend
	 */
	protected final void send(String toSend, Client client) {
		this.parent.send(toSend, client);
	}
	
	/**
	 * method that passes the thing to be sent for debugging to
	 * the MUDServer instance's send debug method, eliminating the
	 * need to prefix the command with 'parent.' anywhere but here.
	 * 
	 * NOTE: doesn't allow passing a debugLevel parameter...
	 * 
	 * @param toSend
	 */
	protected final void debug(String toSend) {
		this.parent.debug(toSend);
	}
	
	protected final void addMessage(final Message msg) {
		parent.addMessage(msg);
	}
	
	protected final String gameError(String source, int type) {
		return this.parent.gameError(source, type);
	}
	
	protected final ProgramInterpreter getProgInt() {
		return parent.getProgInt();
	}
	
	protected final void scheduleAtFixedRate(final TimerTask task, final long delay, final long period) {
		parent.timer.scheduleAtFixedRate(task, delay, period);
		
	}
	
	protected final String colors(final String arg, final String cc) {
		return parent.colors(arg,  cc);
	}
	
	protected final String[] getHelpFile(String name) {
		return parent.getHelpFile(name);
	}
	
	protected final String[] getTopicFile(String name) {
		return parent.getTopicFile(name);
	}
	
	protected final void examine(final MUDObject m, final Client client) {
		if( m.isType(TypeFlag.ROOM) ) {
			parent.examine((Room) m, client);
			return;
		}
		else if( m.isType(TypeFlag.PLAYER) || m.isType(TypeFlag.NPC) ) {
			parent.examine((Player) m, client);
			return;
		}
		else {
			parent.examine(m, client);
		}
	}
	
	/*protected final void examine(final Room r, final Client client) {
		parent.examine(r, client);
	}
	
	protected final void examine(final Player p, final Client client) {
		parent.examine(p, client);
	}*/
	
	protected final Spell getSpell(final String name) {
		return parent.getSpell(name);
	}
	
	protected final MUDObject getObject(final String name) {
		return db.getByName(name);
		//return parent.getObject(name);
	}
	
	/*protected final MUDObject getObject(final String objectName, final Client client) {
		return parent.getObject(objectName, client);
	}*/
	
	protected final MUDObject getObject(Integer dbref) {
		return db.get(dbref);
		//return parent.getObject(dbref);
	}
	
	/**
	 * method that calls the parent MUDServer instance's getPlayer
	 * method on the object provided and returns either a Player object
	 * or null (when the object can't be used to get a player, or when
	 * no player was found)
	 * 
	 * @param object
	 * @return
	 */
	protected final Player getPlayer(Object object) {
		if( object instanceof String ) {
			return parent.getPlayer((String) object);
		}
		else if( object instanceof Integer ) {
			return parent.getPlayer((Integer) object);
		}
		else if( object instanceof Client ) {
			return parent.getPlayer((Client) object);
		}
		
		return null;
	}
	
	protected final NPC getNPC(Object object) {
		if( object instanceof String ) {
			return parent.getNPC((String) object);
		}
		else if( object instanceof Integer ) {
			return parent.getNPC((Integer) object);
		}
		
		return null;
	}
	
	/**
	 * method that calls the parent MUDServer instance's getRoom
	 * method on the object provided and returns either a Room object
	 * or null (when the object can't be used to get a room, or when
	 * no room was found)
	 * 
	 * @param object
	 * @return
	 */
	protected final Room getRoom(Object object) {
		if( object instanceof String ) {
			return parent.getRoom((String) object);
		}
		else if( object instanceof Player ) {
			return parent.getRoom((Player) object);
		}
		else if( object instanceof Integer ) {
			return parent.getRoom((Integer) object);
		}
		
		return null;
	}
	
	protected final Exit getExit(final String exitName) {
		return parent.getExit(exitName);
	}
	
	protected final Item getItem(Object object) {
		/*if( object instanceof String ) {
			return parent.getItem((String) object);
		}*/
		if( object instanceof Integer ) {
			return parent.getItem((Integer) object);
		}
		
		return null;
	}
	
	protected final ArrayList<Player> getPlayers() {
		return parent.getPlayers();
	}
	
	protected final void addSpellTimer(final Player player, final SpellTimer s) {
		parent.getSpellTimers(player).add(s);
	}
	
	protected final void addEffectTimer(final Player player, final EffectTimer e) {
		parent.getEffectTimers(player).add(e);
	}
	
	protected final boolean applyEffect(final MUDObject m, final Effect effect) {
		return parent.applyEffect(m, effect);
	}
	
	protected final void handleDeath(Player player) {
		parent.handleDeath(player);
	}
	
	protected final void handleDeath(Creature creature, Player player) {
		parent.handleDeath(creature, player);
	}
	
	protected final void handle_mail(final String input, final Client client) {
		parent.handle_mail(input, client);
	}
	
	protected final Map<String, String> getAliases() {
		return parent.getAliases();
	}
	
	protected final List<Creature> getCreaturesByRoom(final Room room) {
		return parent.getCreaturesByRoom(room);
	}
	
	protected Item findItem(final List<Item> items, final Integer itemDBRef) {
		return parent.findItem(items, itemDBRef);
	}
	
	protected Item findItem(final List<Item> items, final String itemName) {
		return parent.findItem(items, itemName);
	}
	protected void addAlias(final String command, final String alias) {
		parent.addAlias(command, alias);
	}
}