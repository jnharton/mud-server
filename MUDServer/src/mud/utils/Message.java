package mud.utils;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import mud.net.Client;
import mud.objects.Player;
import mud.objects.Room;

/**
 * really should consider reverting back to string names, but Player objects
 * are more convenient.
 */

/**
 * Message class to encapsulate messages transmitted from one
 * connected player to another.
 * 
 * @author Jeremy N. Harton
 * 
 */
public class Message
{
	private Player sender;           // player who sent the message
	private Player recipient;        // player the message was sent to.
	private String message;          // String to store the body or content of the message.
	private Integer location;        //
	
	private MessageType type;        //
	private Boolean wasSent = false; // has this message been sent
	
	/*
	 * BROADCAST
	 * BROADCAST_PLAYER
	 * BROADCAST_LOCAL
	 * SYSTEM
	 * NORMAL
	 * NONE
	 */
	public enum MessageType { BROADCAST, BROADCAST_PLAYER, BROADCAST_LOCAL, SYSTEM, NORMAL, NONE };

	/**
	 * Basic message unit, useful only for holding a message;
	 * 
	 * @param tempMessage
	 */
	public Message(String tempMessage) {
		this.message = Utils.trim(tempMessage);
		
		this.type = MessageType.NONE;
	}
	
	/**
	 * this is a kludge.
	 * 
	 * @param tempMessage
	 * @param type
	 */
	public Message(String tempMessage, int type) {
		this.message = tempMessage;
		
		this.type = MessageType.values()[type];
	}
	
	/**
	 * "Normal" message, which has both a sender and a recipient in
	 * addition to the message.
	 * 
	 * @param tempMessage
	 * @param tempRecipient
	 */
	public Message(final Player tempSender, final String tempMessage, final Player tempRecipient) {
		this.sender = tempSender;
		this.message = Utils.trim(tempMessage);
		this.recipient = tempRecipient;
		
		this.type = MessageType.NORMAL;
	}
	
	/**
	 * Broadcast (Local) -- initiated by server
	 * 
	 * @param tempSender
	 * @param tempMessage
	 * @param tRoom
	 */
	public Message(final String tempMessage, final Room tLocation) {
		this.message = Utils.trim(tempMessage);
		this.location = tLocation.getDBRef();
		
		this.type = MessageType.BROADCAST_LOCAL;
	}
	
	/**
	 * Direct Message (Say)
	 * 
	 * @param tempSender
	 * @param tempMessage
	 */
	public Message(final Player tempSender, final String tempMessage) {
		this.sender = tempSender;
		this.message = tempMessage;
		this.location = tempSender.getLocation();
		
		this.type = MessageType.BROADCAST_PLAYER;
	}
	
	/**
	 * System Message -- for a particular player
	 * 
	 * @param tempMessage
	 * @param tempRecipient
	 */
	public Message(final String tempMessage, final Player tempRecipient) {
		this.recipient = tempRecipient;
		this.message = Utils.trim(tempMessage);
		
		this.type = MessageType.SYSTEM;
	}
	
	public MessageType getType() {
		return this.type;
	}
	
	public Player getSender() {
		return this.sender;
	}
	
	public void setSender(final Player newSender) {
		this.sender = newSender;
	}
	
	public Player getRecipient() {
		return this.recipient;
	}
	
	public void setRecipient(final Player newRecipient) {
		this.recipient = newRecipient;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(final String newMessage) {
		this.message = newMessage;
	}
	
	public Integer getLocation() {
		return this.location;
	}
	
	public void setLocation(final Integer newLocation) {
		this.location = newLocation;
	}
	
	public boolean sent() {
		return this.wasSent;
	}
	
	public void markSent() {
		this.wasSent = true;
	}
	
	@Override
	public String toString() {
		return this.message;
	}
}