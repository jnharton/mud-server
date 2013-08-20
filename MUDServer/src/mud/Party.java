package mud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mud.objects.Player;

public class Party {
	private static int MAX_SIZE = 6;
	private List<Player> members;

	private Player leader;

	public Party() {
		members = new ArrayList<Player>();
		leader = null;
	}

	public Party(Player...players) {
		members = new ArrayList<Player>(players.length);
		members.addAll(Arrays.asList(players));
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