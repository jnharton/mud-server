package mud.chat;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;

import mud.Constants;
import mud.objects.Player;
import mud.utils.Log;
import mud.utils.MudUtils;

public class ChatChanneler
{
	private Map<String, ChatChannel> channels;
	private Log log;
	
	private boolean enable_logging = false;

	public ChatChanneler() {
		this.channels = new LinkedHashMap<String, ChatChannel>();
	}
	
	public ChatChanneler(boolean enable_logging) {
		this();
		
		this.enable_logging = true;
		
		this.log = new Log("chat");
		this.log.openLog();
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
		if( hasChannel(channelName) ) {
			channels.remove(channelName);
		}
	}
	
	public Result modifyRestriction(final String channelName, final int newRestrict) {
		Result result = Result.NIL;
		
		final ChatChannel channel = channels.get(channelName);
		
		if( channel != null ) {
			if( newRestrict != channel.getRestrict() ) {
				channel.setRestrict(newRestrict);
				result = Result.MODIFY_OK;
			}
			else result = Result.MODIFY_NOK;
			
			//if( player.getAccess() >= channel.getRestrict() ) {}
		}
		else result = Result.NO_CHANNEL;
		
		return result;
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
	 * 
	 * @return
	 */
	public Result add(final Player player, final String channelName) {
		return add(player, channelName, "");
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
	 * @param password
	 * 
	 * @return
	 */
	public Result add(final Player player, final String channelName, final String password) {
		Result result = Result.NIL;
		
		boolean valid = false;
		
		final ChatChannel channel = channels.get(channelName);
		
		if( channel != null ) {
			if( !channel.isListener(player) ) {
				// do we pass the restriction?
				if( player.getAccess() >= channel.getRestrict() ) {
					if( channel.isProtected() ) {
						if( channel.checkPassword(password) || MudUtils.checkAccess(player, Constants.ADMIN) ) {
							valid = true;
						}
						else result = Result.WRONG_PASS; // message about incorrect password
					}
					else valid = true;
				}
				else result = Result.RESTRICTED; // message about channel restriction
				
				if( valid ) {
					if( channel.addListener(player) ) {
						result = Result.JOIN;
					}
				}
			}
			else result = Result.CURR_LISTEN; // message about already listening to that channel
		}
		else {
			// test to see whether a mapping to null exists...
			if( channels.containsKey(channelName) ) {
				channels.remove(channelName); // explicit remove a mapping to a null value
			}
			
			result = Result.NO_CHANNEL;
		}
		
		return result;
	}

	public Result remove(final Player player, final String channelName) {
		Result result = Result.NIL;
		
		final ChatChannel channel = channels.get(channelName);
		
		if( channel != null ) {
			if( channel.isListener(player) ) {
				if( channel.removeListener(player) ) {
					result = Result.LEAVE;
				}
			}
			else result = Result.CURR_NOLISTEN;
		}
		else {
			// test to see whether a mapping to null exists...
			if( channels.containsKey(channelName) ) {
				channels.remove(channelName); // explicit remove a mapping to a null value
			}
			
			result = Result.NO_CHANNEL;
		}
		
		return result;
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