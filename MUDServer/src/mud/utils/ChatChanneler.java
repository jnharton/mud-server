package mud.utils;

import java.util.*;

import mud.MUDServer;
import mud.objects.Player;

public class ChatChanneler
{
	//static final private String chan_color = "magenta"; // channel title color
	//static private String text_color = "green";         // the color of the channel text

	final private MUDServer mud;
	private HashMap<String, ChatChannel> channels = new HashMap<String, ChatChannel>();
	//final private HashMap<String, Set<Player>> chanNamePlayersMap = new HashMap<String, Set<Player>>();

	public ChatChanneler(final MUDServer mud) {
		this.mud = mud;
	}

    public List<Player> getListeners(final String channelName) {
    	return channels.get(channelName).getListeners();
    	
        //return new ArrayList<Player>(chanNamePlayersMap.get(channelName));
    }

    public Collection<String> getChannelNames() {
    	return new ArrayList<String>(channels.keySet());
    	
        //return new ArrayList<String>(chanNamePlayersMap.keySet());
    }

	public boolean hasChannel(final String channelName) {
		return channels.containsKey(channelName);
		
        //return chanNamePlayersMap.containsKey(channelName);
	}

	public boolean isPlayerListening(final String channelName, final Player player) {
		// TODO: revise to determine listening status
		return channels.containsKey(channelName);
		
        //return chanNamePlayersMap.containsKey(channelName) && chanNamePlayersMap.get(channelName).contains(player);
	}

	public void makeChannel(final String channelName) {
		if (!channels.containsKey(channelName)) {
			channels.put(channelName, new ChatChannel(channelName));
		}
		
        /*if (!chanNamePlayersMap.containsKey(channelName)) {
            chanNamePlayersMap.put(channelName, new HashSet<Player>());
        }*/
	}

	public void add(final Player player, final String channelName) throws Exception {
		if (!channels.containsKey(channelName)) {
            throw new Exception("No channel by the name of " + channelName);
        }
        channels.get(channelName).addListener(player);
        
        /*if (!chanNamePlayersMap.containsKey(channelName)) {
            throw new Exception("No channel by the name of " + channelName);
        }
        chanNamePlayersMap.get(channelName).add(player);*/
	}

	public void remove(final Player player, final String channelName) {
		if (channels.containsKey(channelName)) {
            channels.get(channelName).removeListener(player);
        }
		
        /*if (chanNamePlayersMap.containsKey(channelName)) {
            chanNamePlayersMap.get(channelName).remove(player);
        }*/
	}

	public void send(final String channelName, final Player player, final String message) {
		ChatChannel chan = channels.get(channelName);
		
		String name = chan.getName(), chan_color = chan.getChanColor(), text_color = chan.getTextColor();
		
		if (chan != null) {
            player.getClient().write("(" + mud.colors(name, chan_color) + ") " + "<" + player.getName() + "> " + mud.colors(message, text_color) + "\r\n");
            mud.debug("(" + channelName + ") <" + player.getName() + "> " + message + "\n");										
        }
		
        /*if (chanNamePlayersMap.get(channelName) != null) {
            player.getClient().write("(" + mud.colors(channelName, this.chan_color) + ") " + "<" + player.getName() + "> " + mud.colors(message, this.text_color) + "\r\n");
            mud.debug("(" + channelName + ") <" + player.getName() + "> " + message + "\n");										
        }*/
	}

	public void send(final String channelName, final String message) {
		ChatChannel chan = channels.get(channelName);

		String name = chan.getName(), chan_color = chan.getChanColor(), text_color = chan.getTextColor();

		if (chan != null) {
			for (final Player player : channels.get(channelName).getListeners()) {
                player.getClient().write("(" + mud.colors(name, chan_color) + ") " + mud.colors(message, text_color) + "\r\n");
                mud.debug("(" + channelName + ") " + message + "\n");										
            }
		}

		/*if (chanNamePlayersMap.get(channelName) != null) {
            for (final Player player : chanNamePlayersMap.get(channelName)) {
                player.getClient().write("(" + mud.colors(channelName, this.chan_color) + ") " + mud.colors(message, this.text_color) + "\r\n");
                mud.debug("(" + channelName + ") " + message + "\n");										
            }
        }*/
	}
}