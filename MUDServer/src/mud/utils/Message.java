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
	public static enum MessageType {
		BROADCAST, BROADCAST_PLAYER, BROADCAST_LOCAL, SYSTEM, NORMAL, NONE
	};
	
	private MessageType type;        //
	
	private Player sender;           // player who sent the message
	private Player recipient;        // player the message was sent to.
	private String message;          // String to store the body or content of the message.
	
	private Integer location;        //
	
	private Boolean wasSent = false; // has this message been sent

	/**
	 * Basic message unit, useful only for holding a message;
	 * 
	 * @param tempMessage
	 */
	public Message(final String tempMessage) {
		this(null, null, tempMessage);
	}
	
	public Message(final String tempMessage, final MessageType tType) {
		this(null, null, tempMessage);
		
		this.type = tType;
	}
	
	/**
	 * Broadcast (Local) -- initiated by server
	 * 
	 * @param tempSender
	 * @param tempMessage
	 * @param tRoom
	 */
	public Message(final String tempMessage, final Room tLocation) {
		this(null, null, tempMessage, tLocation);
	}
	
	public Message(final Player tSender, final Player tRecipient, final String tMessage) {
		this(tSender, tRecipient, tMessage, null);
	}
	
	public Message(final Player tSender, final Player tRecipient, final String tMessage, final Room tLocation) {
		this.sender = tSender;
		this.recipient = tRecipient;
		this.message = Utils.trim(tMessage);
		
		this.location = (tLocation != null) ? tLocation.getDBRef() : -1;
		
		boolean noSend = (this.sender == null);
		boolean noRecip = (this.recipient == null);
		boolean Location = (this.location != -1);
		
		if( noSend && noRecip && Location) this.type = MessageType.BROADCAST_LOCAL;  //
		else if( noSend && noRecip )       this.type = MessageType.NONE;
		else if( noSend )                  this.type = MessageType.SYSTEM;           // System Message
		else if( noRecip )                 this.type = MessageType.BROADCAST_PLAYER; // Direct Message
		else                               this.type = MessageType.NORMAL;
	}
	
	public MessageType getType() {
		return this.type;
	}
	
	public Player getSender() {
		return this.sender;
	}
	
	public Player getRecipient() {
		return this.recipient;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public Integer getLocation() {
		return this.location;
	}
	
	public boolean wasSent() {
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