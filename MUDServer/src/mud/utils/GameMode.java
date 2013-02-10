package mud.utils;

public enum GameMode {
	NORMAL("Normal"),
	WIZARD("Wizard"),
	MAINTENANCE("Maintenance");

	final private String name;

	GameMode(String name) {
		this.name = name;
	}

    static public boolean isValidString(final char c) {
		switch (c) {
            case 'n':
            case 'm':
            case 'w':   return true;
            default:    return false;
        }
    }

    static public GameMode fromString(final char c) {
		switch (c) {
            case 'w':   return GameMode.WIZARD;
            case 'm':   return GameMode.MAINTENANCE;
            default:    return GameMode.NORMAL;
        }
    }

	public String toString() {
		return this.name;
	}
}
