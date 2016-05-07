package mud.misc;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mud.chat.ChatChannel;
import mud.objects.Player;

/**
 * Party represents a group of players that are together for the sake
 * of combat, etc. The class stores a list of the members, but keeps
 * a separate reference to the party leader
 * 
 * NOTE: a maximum of 6 players to a party is hardcoded here.
 * 
 * @author Jeremy
 *
 */
public class Party {
	private static final int MAX_SIZE = 6;

	private Player leader;
	private List<Player> members;

	private ChatChannel party_chat;

	/**
	 * Create a new, empty party without a leader.
	 */
	public Party() {
		this.leader = null;
		this.members = new LinkedList<Player>();
		
		this.party_chat = null;
	}

	/**
	 * Create a new part which includes the specified players. This will
	 * only add up to MAX_SIZE players from amongst those specified, first
	 * come first serve.
	 * 
	 * NOTE: The first player specified will be made the leader of the party
	 * 
	 * @param players
	 */
	public Party(final Player...players) {
		if( players.length > Party.MAX_SIZE) {
			this.members = new LinkedList<Player>();
			this.members.addAll( Arrays.asList(players).subList(0, Party.MAX_SIZE) );
		}
		else {
			this.members = new LinkedList<Player>();
			this.members.addAll( Arrays.asList(players) );
		}

		this.leader = this.members.get(0);
		
		this.party_chat = null;
	}
	
	public Player getLeader() {
		return this.leader;
	}

	public void setLeader(final Player newLeader) {
		this.leader = newLeader;
	}

	public boolean isLeader(final Player player) {
		return (this.leader == player);
	}

	public boolean addPlayer(final Player newPlayer) {
		boolean success = false;
		
		if( this.members.size() < MAX_SIZE ) {
			if( this.members.size() == 0 ) {
				this.leader = newPlayer;
			}

			success = this.members.add(newPlayer);
			
			if( success ) {
				this.party_chat.addListener(newPlayer);
			}
		}
		
		return success;
	}

	public boolean removePlayer(final Player currPlayer) {
		boolean success = false;
		
		if( this.members.size() > 1 ) {
			if( this.leader == currPlayer ) {
				this.leader = this.members.get(0);
			}

			success = this.members.remove(currPlayer);
			
			if( success ) {
				this.party_chat.removeListener(currPlayer);
			}
		}
		
		return success;
	}

	public boolean hasPlayer(final Player player) {
		return this.members.contains(player);
	}
	
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(this.members);
	}
	
	public ChatChannel getChannel() {
		return this.party_chat;
	}
	
	public void setChannel(final ChatChannel newChannel) {
		this.party_chat = newChannel;
	}
}