package mud.combat;

import mud.events.SayEvent;
import mud.events.SayEventListener;

/**
 * Strategy
 * 
 * Defines Mobile/NPC behavior
 * 
 * ## idea inspired by / borrowed from: Wasteland-MUD by thezboe ##
 * 
 * @author Jeremy
 *
 */
public abstract class Strategy implements SayEventListener {
	public abstract void onRoomChange();    // what the mob should do when it moves to another room
	public abstract void onDetectHostile(); // what should be done when a hostile is detected
}