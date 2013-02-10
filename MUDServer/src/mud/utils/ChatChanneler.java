package mud.utils;

import java.util.*;

import mud.MUDServer;
import mud.net.Client;
import mud.objects.Player;

public class ChatChanneler
{
	static final private String chan_color = "magenta"; // channel title color
	static private String text_color = "green";         // the color of the channel text

	final private MUDServer mud;
	final private HashMap<String, Set<Player>> chanNamePlayersMap = new HashMap<String, Set<Player>>();

	public ChatChanneler(final MUDServer mud) {
		this.mud = mud;
	}

    public List<Player> getListeners(final String channelName) {
        return new ArrayList<Player>(chanNamePlayersMap.get(channelName));
    }

    public Collection<String> getChannelNames() {
        return new ArrayList<String>(chanNamePlayersMap.keySet());
    }

	public boolean hasChannel(final String channelName) {
        return chanNamePlayersMap.containsKey(channelName);
	}

	public boolean isPlayerListening(final String channelName, final Player player) {
        return chanNamePlayersMap.containsKey(channelName) && chanNamePlayersMap.get(channelName).contains(player);
	}

	public void makeChannel(final String channelName) {
        if (!chanNamePlayersMap.containsKey(channelName)) {
            chanNamePlayersMap.put(channelName, new HashSet<Player>());
        }
	}

	public void add(final Player player, final String channelName) throws Exception {
        if (!chanNamePlayersMap.containsKey(channelName)) {
            throw new Exception("No channel by the name of " + channelName);
        }
        chanNamePlayersMap.get(channelName).add(player);
	}

	public void remove(final Player player, final String channelName) {
        if (chanNamePlayersMap.containsKey(channelName)) {
            chanNamePlayersMap.get(channelName).remove(player);
        }
	}

	public void send(final String channelName, final Player player, final String message) {
        if (chanNamePlayersMap.get(channelName) != null) {
            player.getClient().write("(" + mud.colors(channelName, this.chan_color) + ") " + "<" + player.getName() + "> " + mud.colors(message, this.text_color) + "\r\n");
            mud.debug("(" + channelName + ") <" + player.getName() + "> " + message + "\n");										
        }
	}

	public void send(final String channelName, final String message) {
        if (chanNamePlayersMap.get(channelName) != null) {
            for (final Player player : chanNamePlayersMap.get(channelName)) {
                player.getClient().write("(" + mud.colors(channelName, this.chan_color) + ") " + mud.colors(message, this.text_color) + "\r\n");
                mud.debug("(" + channelName + ") " + message + "\n");										
            }
        }
	}

}
