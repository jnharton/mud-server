package mud.api;

import java.util.List;

import mud.objects.Player;

public interface MUDServerAPI {
	public List<Player> getPlayers();
}