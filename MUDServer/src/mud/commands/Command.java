package mud.commands;

import java.util.ArrayList;
import java.util.TimerTask;

import mud.Effect;
import mud.MUDObject;
import mud.MUDServer;
import mud.ProgramInterpreter;
import mud.magic.Spell;
import mud.net.Client;
import mud.objects.Creature;
import mud.objects.Exit;
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
	protected static int USER = 0;   // basic user permissions
	protected static int ADMIN = 1;  //
	protected static int WIZARD = 2; //
	protected static int GOD = 3;    // Pff, such arrogant idiots we are! (anyway, max permissions)
	
	private MUDServer parent;

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
	
	/**
	 * getAcessLevel
	 * 
	 * returns the specified access permissions required to use
	 * the command
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
		examine(m, client);
	}
	
	protected final Spell getSpell(final String name) {
		return parent.getSpell(name);
	}
	
	protected final MUDObject getObject(final String name) {
		return parent.getObject(name);
	}
	
	protected final MUDObject getObject(final String objectName, final Client client) {
		return parent.getObject(objectName, client);
	}
	
	protected final MUDObject getObject(Integer dbref) {
		return parent.getObject(dbref);
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
	
	protected final void handleDeath(Creature creature, Client client) {
		parent.handleDeath(creature, client);
	}
	
	protected final void handleMail(final String input, final Client client) {
		handleMail(input, client);
	}
}