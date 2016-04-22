package mud.chat;

public class NoSuchChannelException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoSuchChannelException(final String channelName) {
		super(channelName);
	}
}