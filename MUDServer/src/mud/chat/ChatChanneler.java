package mud.chat;

import java.util.*;

import mud.objects.Player;
import mud.utils.Log;

public class ChatChanneler
{
	private HashMap<String, ChatChannel> channels;
	private Log log;
	
	boolean enable_logging = false;

	public ChatChanneler() {
		this.channels = new HashMap<String, ChatChannel>();
	}
	
	public ChatChanneler(Log log) {
	}
    
    public List<String> getChannelNames() {
    	return Collections.unmodifiableList( new LinkedList<String>(channels.keySet()) );
    }
    
    /**
     * Get the extant chat channels.
     * 
     * NOTE: returned collection is unmodifiable
     * 
     * @return
     */
    public Collection<ChatChannel> getChatChannels() {
    	return Collections.unmodifiableCollection(channels.values());
    }

	public boolean hasChannel(final String channelName) {
		boolean channelExists = false;
		
		if( channels.containsKey(channelName) ) {
			final ChatChannel channel = channels.get(channelName);
			
			if( channel != null ) {
				channelExists = true;
			}
			else {
				// test to see whether a mapping to null exists...
				if( channels.containsKey(channelName) ) {
					channels.remove(channelName); // explicit remove a mapping to a null value
				}
			}
		}
		
		return channelExists;
	}

	public ChatChannel makeChannel(final String channelName) {
		ChatChannel c = null;
		
		if (!channels.containsKey(channelName)) {
			c = new ChatChannel(channelName);
			channels.put(channelName, c);
		}
		
		return c;
	}
	
	public void destroyChannel(final String channelName) {
		if (channels.containsKey(channelName)) {
			channels.remove(channelName);
		}
	}
	
	public boolean modifyRestriction(final String channelName, final int newRestrict) {
		boolean success = false;
		
		final ChatChannel channel = channels.get(channelName);
		
		if( newRestrict != channel.getRestrict() ) {
			channel.setRestrict(newRestrict);
			success = true;
		}
		
		return success;
	}
	
	/**
	 * 
	 * NOTE: this used to have an exception, because I'd like to properly report an invalid/
	 * non-existent channel... unless a failed add means that there is no such channel?
	 * 
	 * NOTE2: elsewhere the messages claim that a failed add is a restricted channel, while
	 * 
	 * NOTE3: an unsuccessful add, without a thrown exception, implies a restriction failure.
	 * 
	 * @param player
	 * @param channelName
	 * @return
	 * @throws Exception
	 */
	public boolean add(final Player player, final String channelName) throws NoSuchChannelException {
		boolean success = false;
		
		final ChatChannel channel = channels.get(channelName);
		
		if( channel != null ) {
			if( player.getAccess() >= channel.getRestrict() ) {
				success = channel.addListener(player);
			}
		}
		else {
			// test to see whether a mapping to null exists...
			if( channels.containsKey(channelName) ) {
				channels.remove(channelName); // explicit remove a mapping to a null value
			}
			
			throw new NoSuchChannelException(channelName);
		}
		
		return success;
	}

	public boolean remove(final Player player, final String channelName) throws NoSuchChannelException {
		boolean success = false;
		
		final ChatChannel channel = channels.get(channelName);
		
		if( channel != null ) {
			success = channel.removeListener(player);
		}
		else {
			// test to see whether a mapping to null exists...
			if( channels.containsKey(channelName) ) {
				channels.remove(channelName); // explicit remove a mapping to a null value
			}
			
			throw new NoSuchChannelException(channelName);
		}
		
		return success;
	}
	
	public boolean isPlayerListening(final String channelName, final Player player) {
		/*if( hasChannel(channelName) ) {
		}*/
		return channels.containsKey(channelName) && channels.get(channelName).isListener(player);
	}

	public boolean send(final String channelName, final Player player, final String message) {
		boolean success = false;

		final ChatChannel channel = channels.get(channelName);

		if (channel != null) {
			if( player.getAccess() >= channel.getRestrict() ) {
				channel.write(player, message); // add message to ChatChannel message queue
				//mud.debug("(" + channelName + ") <" + player.getName() + "> " + message + "\n");

				success = true;
			}
		}
		
		return success;
	}
	
	// should be used for system messages...
	public boolean send(final String channelName, final String message) {
		final ChatChannel chan = channels.get(channelName);

		if (chan != null) {
			chan.write(message); // add message to ChatChannel message queue
			//mud.debug("(" + channelName + ") " + message + "\n");
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public String resolveShortName(final String shortName) {
    	String name = "";
    	
    	for(final ChatChannel ch : this.getChatChannels()) {
    		if( ch.getShortName().equalsIgnoreCase(shortName) ) {
    			name = shortName;
    			break;
    		}
    	}
    	
    	return name;
    }
	
	public List<Player> getListeners(final String channelName) {
    	return channels.get(channelName).getListeners();
    }
}