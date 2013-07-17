package mud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mud.objects.Player;

public class Party {
	private static int MAX_SIZE = 6;
	private int size;
	private List<Player> members;

	private Player leader;

	public Party() {
		members = new ArrayList<Player>(MAX_SIZE);
		size = MAX_SIZE;
		leader = null;
	}

	public Party(int initialSize) {
		members = new ArrayList<Player>(initialSize);
		size = initialSize;
		leader = null;
	}

	public Party(Player...players) {
		members = new ArrayList<Player>(players.length);
		members.addAll(Arrays.asList(players));
		size = members.size();
		leader = members.get(0);
	}

	public void addPlayer(Player newPlayer) {
		if( members.size() == 0 ) {
			leader = newPlayer;
		}
		members.add(newPlayer);
	}

	public Player removePlayer(int index) {
		Player player = members.remove(index);
		
		if( leader == player ) {
			leader = members.get(0);
		}
		
		return player;
	}

	public boolean removePlayer(String name) {
		Player player = null;
		
		for(Player player1 : members) {
			if( player1.getName().equals(name) ) {
				player = player1;
				break;
			}
		}
		
		return members.remove(player);
	}
	
	public boolean removePlayer(final Player player) {
		return this.members.remove(player);
	}
	
	public boolean hasPlayer(final Player player) {
		return this.members.contains(player);
	}
	
	public List<Player> getPlayers() {
		return this.members;
	}
}