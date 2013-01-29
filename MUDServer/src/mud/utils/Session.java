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

import java.util.ArrayList;

import mud.net.Client;
import mud.objects.Player;

/**
 * Session class, ostensibly to track player sessions (play time, etc)
 * 
 * implementation note: use to track a single player
 * 	track:
 * 		player
 * 		client
 * 		time connected
 * 		time online
 * 		time idle
 * 		time disconnected
 * 		actions taken (command)
 * 	possible objects to use:
 * 		Player
 * 		Client
 * 		Time
 * 		Log
 * 
 * @author Jeremy
 *
 */
public class Session {
	private Account account;  // account in use
	private Client client;    // network client 
	private Player player;    // current player
	
	public Time connect;      // connection time
	public Time disconnect;   // disconnect time
	
	private Log session;      // session log
	private Log transactions; //
	
	/*
	 * this would be false if the player's connection failed,a
	 */
	public boolean connected;
	
	public Session(Client client, Account account, Player player) {
		this.client = client;
		this.account = account;
		this.player = player;
	}
	
	public Session(Client client, Player player) {
		this.client = client;
		this.account = null;
		this.player = player;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * Get the network client for this session.
	 * 
	 * @return
	 */
	public Client getClient() {
		return this.client;
	}
	
	/**
	 * Set the network client for this session.
	 * 
	 * NOTE: A client may only be set when a new session is created
	 * or an old, disconnected session is reconnected to.
	 * 
	 * @param newClient
	 */
	public void setClient(Client newClient) {
		if ( !connected ) { this.client = newClient; }
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	/**
	 * Translate session data into a text-based file format
	 * to be stored on disk
	 * 
	 * account - just use account id/number
	 * client - ip address
	 * player - just their name
	 * 
	 * connect time - MM/DD/YY HH:MM:SS
	 * disconnect time - MM/DD/YY HH:MM:SS
	 * 
	 * log - transmitted information?
	 * 
	 * I'd also like to record changes to player, transactions, etc just in case those get lost
	 * 
	 * @return a string array to be written to a file
	 */
	public String[] toFile() {
		ArrayList<String> output = new ArrayList<String>();
		
		// account id, client ip, player name
		output.add(String.valueOf(account.getId()));
		output.add(client.ip());
		output.add(player.getName());
		
		// formatting
		output.add("");
		
		// connect, disconnect time
		output.add("connect: " + connect.toString());
		output.add("disconnect: " + disconnect.toString());
		
		// log
		
		return (String[]) output.toArray();
	}
}