package mud.chat;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import mud.objects.Player;
import mud.utils.Message;

public class ChatChannel { 
	private String name;      // channel name
	private String shortname; // short version of channel name
	
	private int restrict;     // restrict access based on some integer
	
	private boolean hidden;     // can everyone see this channel
	private boolean restricted; // is channel access restricted?
	private boolean protect;    // is the channel protected (is there a password set?)
	
	private String password;
	
	// output format
	private String chan_color;    // channel title color
	private String sender_color;  // color of sender
	private String text_color;    // the color of the channel text
	
	private List<Player> listeners;  // players who are listening to the channel
	private Queue<Message> messages; // messages 'written' to the channel
	
	public ChatChannel(final String name) {
		this(name, "magenta", "orange", "green");
	}
	
	public ChatChannel(final String name, final String chanColor, String senderColor, final String textColor) {
		this.name = name;
		this.shortname = name.substring(0, 3).toLowerCase();
		
		this.restrict = 0;
		this.hidden = false;
		this.protect = false;
		this.password = "";
		
		this.chan_color = chanColor;
		this.sender_color = senderColor;
		this.text_color = textColor;
		
		this.listeners = new ArrayList<Player>();
		this.messages = new LinkedBlockingQueue<Message>();
	}
	
	/**
	 * Get the name of the channel.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set the name of the channel.
	 * 
	 * @param chanName
	 */
	public void setName(final String chanName) {
		this.name = chanName;
		this.shortname = chanName.substring(0, 3).toLowerCase();
	}
	
	public String getShortName() {
		return this.shortname;
	}
	
	public void setShortName(final String shortName) {
		this.shortname = shortName;
	}
	
	/**
	 * Get the integer based restriction data
	 * 
	 * @return
	 */
	public int getRestrict() {
		return this.restrict;
	}
	
	/**
	 * Set a restriction based on an integer (access level?)
	 * 
	 * @param newRestrict
	 */
	public void setRestrict(int newRestrict) {
		this.restrict = newRestrict;
	}
	
	public boolean isHidden() {
		return this.hidden;
	}
	
	public void setHidden(boolean state) {
		this.hidden = state;
	}
	
	public boolean isProtected() {
		return this.protect;
	}
	
	public boolean checkPassword(final String password) {
		return this.password == null || this.password.equals(password);
	}
	
	public void setPassword(final String newPassword) {
		this.password = newPassword;
		
		if( this.password.equals("") ) {
			this.protect = false;
		}
		else this.protect = true;
	}
	
	/**
	 * Get the name of the color to be used for displaying the channel name.
	 * 
	 * @return
	 */
	public String getChanColor() {
		return this.chan_color;
	}
	
	public void setChanColor(final String newColor) {
		this.chan_color = newColor;
	}
	
	public String getSenderColor() {
		return this.sender_color;
	}
	
	public void setSenderColor(final String newColor) {
		this.sender_color = newColor;
	}
	
	/**
	 * Get the name of the color to be used for displaying the channel text.
	 * 
	 * @return
	 */
	public String getTextColor() {
		return this.text_color;
	}
	
	public void setTextColor(final String newColor) {
		this.text_color = newColor;
	}
	
	// is there some reason the addListener(...), removeListener(...) and getListeners(...) methods
	// were marked synchronized?
	/**
	 * Add a listener (Player object) to this chat channel
	 * 
	 * @param p the player object to add to listeners
	 * @return whether we succeeded in adding the player to listeners
	 */
	public boolean addListener(final Player p) {
		return this.listeners.add(p);
	}
	
	/**
	 * Remove a listener (player object) from this chat channel
	 * 
	 * @param p the player object to remove from listeners
	 * @return whether we succeeded in removing the player from listeners
	 */
	public boolean removeListener(final Player p) {
		return this.listeners.remove(p);
	}
	
	public List<Player> getListeners() {
		return Collections.unmodifiableList(this.listeners);
	}
	
	/**
	 * Determine if Player, p, is listening to this chat channel
	 * 
	 * NOTE: Gagging a channel is considered not listening
	 * 
	 * @param p the player to look for
	 * @return whether or not they are listening to this channel (true/false)
	 */
	public boolean isListener(Player p) {
		return this.listeners.contains(p);
	}
	
	public void write(final String message) {
		this.messages.add( new Message(message) );
	}
	
	public void write(final Player player, final String message) {
		this.messages.add( new Message(player, null, message) );
	}
	
	public Message getNextMessage() {
		return this.messages.poll();
	}
}