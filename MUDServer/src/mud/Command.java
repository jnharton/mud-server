package mud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import mud.interfaces.ODBI;
import mud.magic.Spell;
import mud.misc.Effect;
import mud.misc.ProgramInterpreter;
import mud.misc.TimeLoop;
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
import mud.utils.Time;

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
	
	protected Command(final String description) {
		this.description = description;
	}
	
	protected void init(final MUDServer mParent) {
		this.parent = mParent;
		
		if( mParent != null ) {
			this.db = mParent.getDBInterface();
		}
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
	public abstract void execute(final String arg, final Client client);
	
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
	public abstract int getAccessLevel();
	
	/**
	 * method that passes the thing to be sent for debugging to
	 * the MUDServer instance's send method, eliminating the
	 * need to prefix the command with 'parent.' anywhere but here.
	 * 
	 * @param toSend
	 */
	protected final void send(final String toSend, final Client client) {
		// TODO eliminate with intermediate object
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
	protected final void debug(final String toSend) {
		// TODO eliminate with intermediate object
		this.parent.debug(toSend);
	}
	
	protected final void addMessage(final Message msg) {
		// TODO eliminate with intermediate object
		parent.addMessage(msg);
	}
	
	protected final String gameError(final String source, final int type) {
		// TODO eliminate with intermediate object
		return this.parent.gameError(source, type);
	}
	
	protected final ProgramInterpreter getProgramInterpreter() {
		return parent.getProgramInterpreter();
	}
	
	protected final void scheduleAtFixedRate(final TimerTask task, final long delay, final long period) {
		parent.timer.scheduleAtFixedRate(task, delay, period);
	}
	
	protected final String colors(final String arg, final String cc) {
		return parent.colors(arg,  cc);
	}
	
	/*protected final String[] getHelpFile(final String name) {
		return parent.getHelpFile(name);
	}
	
	protected final String[] getTopicFile(final String name) {
		return parent.getTopicFile(name);
	}*/
	
	protected final void examine(final MUDObject m, final Client client) {
		if( m.isType(TypeFlag.ROOM) ) {
			parent.examine((Room) m, client);
		}
		else if( m.isType(TypeFlag.PLAYER) || m.isType(TypeFlag.NPC) ) {
			parent.examine((Player) m, client);
		}
		else {
			parent.examine(m, client);
		}
	}
	
	protected final Spell getSpell(final String name) {
		return parent.getSpell(name);
	}
	
	protected final MUDObject getObject(final String name) {
		return this.db.getByName(name);
	}
	
	protected final MUDObject getObject(Integer dbref) {
		return this.db.get(dbref);
	}
	
	/**
	 * method that calls the database interface's getPlayer
	 * method on the object provided and returns either a Player object
	 * or null.
	 * 
	 * @param name
	 * @return
	 */
	protected final Player getPlayer(final String name) {
		return this.db.getPlayer(name);
	}
	
	protected final Player getPlayer(final String name, boolean online) {
		if( online ) return parent.getPlayer(name);
		else         return getPlayer(name);
	}
	
	protected final Player getPlayer(final int DBRef) {
		return getPlayer(DBRef, true);
	}
	
	protected final Player getPlayer(final int DBRef, boolean online) {
		if( online ) return parent.getPlayer(DBRef);
		else         return getPlayer(DBRef);
	}
	
	protected final Player getPlayer(final Client client) {
		return parent.getPlayer(client);
	}
	
	protected final NPC getNPC(final String npcName) {
		return this.db.getNPC(npcName);
	}
	
	protected final NPC getNPC(final int DBRef) {
		return this.db.getNPC(DBRef);
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
	protected final Room getRoom(final String name) {
		return this.db.getRoomByName(name);
	}
	
	protected final Room getRoom(final int DBRef) {
		return this.db.getRoomById(DBRef);
	}
	
	protected final Exit getExit(final String exitName) {
		return this.db.getExit(exitName);
	}
	
	protected final Exit getExit(final int DBRef) {
		return this.db.getExit(DBRef);
	}
	
	protected final Item getItem(final String itemName) {
		return this.db.getItem(itemName);
	}
	
	protected final Item getItem(final int DBRef) {
		return this.db.getItem(DBRef);
	}
	
	protected final ArrayList<Player> getPlayers() {
		return parent.getPlayers();
	}
	
	protected final List<Creature> getCreaturesByRoom(final Room room) {
		return this.db.getCreaturesByRoom(room);
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
	
	protected final void handleDeath(final Player player) {
		parent.handleDeath(player);
	}
	
	protected final void handleDeath(final Creature creature, final Player player) {
		parent.handleDeath(creature, player);
	}
	
	protected final void handle_mail(final String input, final Client client) {
		parent.handle_mail(input, client);
	}
	
	protected final Map<String, String> getAliases() {
		return parent.getAliases();
	}
	
	protected Item findItem(final List<Item> items, final Integer itemDBRef) {
		return parent.findItem(items, itemDBRef);
	}
	
	protected Item findItem(final List<Item> items, final String itemName) {
		return parent.findItem(items, itemName);
	}
	
	protected Time getGameTime() {
		final TimeLoop game_time = parent.game_time;
		
		return new Time(game_time.getHours(), game_time.getMinutes(), game_time.getSeconds());
	}
	
	/**
	 * Initialize a Command object so that it has a reference to the current
	 * instance of MUDServer.
	 * 
	 * @param cmd
	 */
	protected void initCmd(final Command cmd) {
		cmd.init(parent);
	}
	
	protected void addHostile(final Creature hostile) {
		parent.hostiles.add( hostile );
	}
	
	protected void removeHostile(final Creature hostile) {
		parent.hostiles.remove( hostile );
	}
}