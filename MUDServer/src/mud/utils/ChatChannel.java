package mud.utils;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import mud.MUDServer;
import mud.net.Client;
import mud.net.Server;
import mud.objects.Player;

public class ChatChannel implements Runnable {

	private Server parent;
	private MUDServer parent1;
	
	private int id;
	private String name;                   // the name of the channel
	private String chan_color = "magenta"; // channel title color
	private String text_color = "green";   // the color of the channel text
	
	private int restrict;                  // restrict access based on some integer
	
	//private ArrayList<Message> messages;   // messages sent to the channel
	
	private ConcurrentLinkedQueue<Message> messages;
	private ArrayList<Player> listeners;   // players who are listening to the channel

	public ChatChannel(Server parent, MUDServer parent1, int id, String name) {
		this.parent = parent;
		this.parent1 = parent1;
		this.id = id;
		this.name = name;
		this.messages = new ConcurrentLinkedQueue<Message>();
		this.listeners = new ArrayList<Player>(10);
	}
	
	public ChatChannel(Server s, MUDServer ms, int id, String name, String channel_color, String text_color) {
		this(s, ms, id, name);
		this.chan_color = channel_color;
		this.text_color = text_color;
	}

	@Override
	public void run() {
		Client client;
		
		// while the game is running, and the time thread is not suspended
		while( parent1.isRunning() ) {
			// if client is a logged in player, send them any messages queued for them
			// Send any pages, messages, etc to their respective recipients, or to a list of recipients?
			synchronized(this.listeners) {
				for(Player player : this.listeners) { // for every "listening player
					if(1 == 1) { // check for gag?
						for(Message msg : this.messages) { // for the list of messages
							try {
								client = parent1.getClient(player);

								client.write("(" + parent1.colors(this.name, this.chan_color) + ") " + "<" + msg.getSender().getName() + "> " + parent1.colors(msg.getMessage(), this.text_color) + "\r\n"); // send the message
								parent1.debug("(" + this.name + ") " + "<" + msg.getSender().getName() + "> " + msg.getMessage() + "\n");										
								parent1.debug("chat message sent successfully"); 
							}
							catch(NullPointerException npe) {
								parent1.debug("Game [chat channel: " + this.getName() + "] > Null Message.");
								npe.printStackTrace();
							}
						}
					}
				}
			}

			/*for(Message msg : this.messages) { // for the list of messages
				for(Player player : this.listeners) { // for every "listening player
					if(1 == 1) {
						try {
							client = parent1.getClient(player);

							client.write("(" + parent1.colors(this.name, this.chan_color) + ") " + "<" + msg.getSender().getName() + "> " + parent1.colors(msg.getMessage(), this.text_color) + "\r\n"); // send the message
							parent1.debug("(" + this.name + ") " + "<" + msg.getSender().getName() + "> " + msg.getMessage() + "\n");										
							parent1.debug("chat message sent successfully");
						}
						catch(NullPointerException npe) {
							parent1.debug("Game [chat channel: " + this.getName() + "] > Null Message.");
							npe.printStackTrace();
						}
					}
				}
			}*/
			
			/*
			 * maybe instead of doing this I should just process the next message in the queue, and never "clear" it?
			 */
			// we've sent all the messages, so clear out the list
			this.messages.clear();
		}

	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getName() {
		return this.name;
	}
	
	public void setRestrict(int newRestrict) {
		this.restrict = newRestrict;
	}
	
	public int getRestrict() {
		return this.restrict;
	}

	synchronized public void write(String message) {
		Message m = new Message(message);
		this.messages.add(m);
		parent1.debug("new chat message sent to " + getName());
		parent1.debug(m.getMessage());
	}

	synchronized public void write(Client client, String message) {
		Message m = new Message(client, message);
		this.messages.add(m);
		parent1.debug("new chat message sent to " + getName());
		parent1.debug(m.getMessage());
	}
	
	synchronized public void write(Player player, String message) {
		Message m = new Message(player, message);
		this.messages.add(m);
		parent1.debug("new chat message sent to " + getName());
		parent1.debug(m.getMessage());
	}
	
	public ArrayList<Player> getListeners() {
		synchronized(this.listeners) {
			return this.listeners;
		}
	}
	
	synchronized public ConcurrentLinkedQueue<Message> getMessages() {
		return this.messages;
	}
	
	/**
	 * Add a listener (Player object) to this chat channel
	 * <br />
	 * <br />
	 * <i>synchronized</i>: on listeners arraylist
	 * 
	 * @param p the player object to add to listeners
	 * @return whether we succeeded in adding the player to listeners
	 */
	public boolean addListener(Player p) {
		synchronized(this.listeners) {
			return this.listeners.add(p);
		}
	}
	
	/**
	 * Determine if Player, p, is listening to this chat channel
	 * 
	 * NOTE: Gagging a channel is considered not listening
	 * 
	 * @param p the player to look for
	 * @return whether or not they are listening to this channel (true/false)
	 */
	synchronized public boolean isListener(Player p) {
		return this.listeners.contains(p);
	}
	
	/**
	 * Remove a listener (player object) from this chat channel
	 * <br />
	 * <br />
	 * <i>synchronized</i>: on listeners arraylist
	 * 
	 * @param p the player object to remove from listeners
	 * @return whether we succeeded in removing the player from listeners
	 */
	public boolean removeListener(Player p) {
		synchronized(this.listeners) {
			return this.listeners.remove(p);
		}
	}
	
	public void setID(int newID) {
		this.id= newID;
	}
	
	public int getID() {
		return this.id;
	}
}