package mud.utils;

import mud.net.Client;
import mud.objects.Player;

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
	private Client client;
	private Player sender;    // player who sent the message
	private Player recipient; // player the message was sent to.
	private String message;   // String to store the body or content of the message.
	private Integer location; //
	
	private Boolean wasSent;  // has this message been sent

	/**
	 * Basic message unit, useful only for holding a message;
	 * 
	 * @param tempMessage
	 */
	public Message(String tempMessage) {
		this.message = tempMessage.trim();
	}
	
	/**
	 * A message without a sender, but which is intended for a particular recipient
	 * 
	 * system message?
	 * 
	 * @param tempMessage
	 * @param tempRecipient
	 */
	public Message(String tempMessage, Player tempRecipient) {
		this.recipient = tempRecipient;
		this.message = tempMessage.trim();
	}
	
	/**
	 * A message without a recipient, implying that it is meant for some general group of recipients.
	 * 
	 * Broadcast?
	 * 
	 * @param tempSender
	 * @param tempMessage
	 */
	public Message(Player tempSender, String tempMessage) {
		this.sender = tempSender;
		this.message = tempMessage;
		this.location = tempSender.getLocation();
	}
	
	/**
	 * "Normal" message, which has both a sender and a recipient
	 * 
	 * @param tempMessage
	 * @param tempRecipient
	 */
	public Message(Player tempSender, String tempMessage, Player tempRecipient) {
		this.sender = tempSender;
		this.message = tempMessage.trim();
		this.recipient = tempRecipient;
	}
	
	/**
	 * A message intended to be sent to a whole room, but which
	 * originates from some non-player/non-client sender
	 * 
	 * @param tempSender
	 * @param tempMessage
	 * @param tRoom
	 */
	public Message(String tempMessage, Integer tLocation) {
		this.message = tempMessage.trim();
		this.location = tLocation;
	}
	
	public Message(Client tempClient, String tempMessage) {
		this.client = tempClient;
		this.message = tempMessage;
	}
	
	public Client getClient() {
		return this.client;
	}
	
	public void setClient(Client newClient) {
		this.client = newClient;
	}
	
	public Player getSender() {
		return this.sender;
	}
	
	public void setSender(Player newSender) {
		this.sender = newSender;
	}
	
	public Player getRecipient() {
		return this.recipient;
	}
	
	public void setRecipient(Player newRecipient) {
		this.recipient = newRecipient;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public void setMessage(String newMessage) {
		this.message = newMessage;
	}
	
	public Integer getLocation() {
		return this.location;
	}
	
	public void setLocation(Integer newLocation) {
		this.location = newLocation;
	}
	
	public boolean sent() {
		return this.wasSent;
	}
	
	public void markSent() {
		this.wasSent = true;
	}
}