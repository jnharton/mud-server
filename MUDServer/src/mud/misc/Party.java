package mud.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	private static int MAX_SIZE = 6;
	private List<Player> members;

	private Player leader;
	
	/**
	 * Create a new, empty party without a leader.
	 */
	public Party() {
		members = new ArrayList<Player>();
		leader = null;
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
	public Party(Player...players) {
		if( players.length > MAX_SIZE) {
			members = new ArrayList<Player>(MAX_SIZE);
			members.addAll(Arrays.asList(players).subList(0, MAX_SIZE));
		}
		else {
			members = new ArrayList<Player>(players.length);
			members.addAll(Arrays.asList(players));
		}
		leader = members.get(0);
	}

	public boolean addPlayer(Player newPlayer) {
		if( members.size() < MAX_SIZE ) {
			if( members.size() == 0 ) {
				leader = newPlayer;
			}

			return members.add(newPlayer);
		}
		
		return false;
	}
	
	public boolean removePlayer(final Player player) {
		if( members.size() > 1 ) {
			if( leader == player ) {
				leader = members.get(1);
			}
			
			return this.members.remove(player);
		}
		
		return false;
	}

	public boolean hasPlayer(final Player player) {
		return this.members.contains(player);
	}
	
	public Player getLeader() {
		return this.leader;
	}

	public boolean isLeader(final Player player) {
		return leader == player;
	}

	public List<Player> getPlayers() {
		return this.members;
	}
}