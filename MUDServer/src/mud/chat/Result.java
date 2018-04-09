package mud.chat;

public enum Result {
	CREATE,
	DESTROY,
	JOIN("Joined channel: %s"),
	CURR_LISTEN,
	CURR_NOLISTEN,
	LEAVE("Left channel: %s"),
	MODIFY_OK,
	MODIFY_NOK,
	NO_CHANNEL,
	NIL,
	RESTRICTED,
	WRONG_PASS;
	
	private String message = "";
	
	private Result() {
	}
	
	private Result(final String rMessage) {
		this.message = rMessage;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public String formatMessage(final Object...parameters) {
		return String.format(message, parameters);
	}
}