package mud.chat;

public enum Result {
	CREATE,
	DESTROY,
	JOIN,
	CURR_LISTEN,
	CURR_NOLISTEN,
	LEAVE,
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
}