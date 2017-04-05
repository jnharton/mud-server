package mud.foe.misc;

public enum Result {
	INSUFFICIENT_POWER("Insufficent power to enable."),
	INCOMPATIBLE,
	GLITCHED,
	DAMAGED,
	BROKEN,
	ENABLED,
	DISABLED;

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