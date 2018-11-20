package mud.events;

import java.util.EventObject;

import mud.net.Client;

public class TelnetEvent extends EventObject {
	String message;
	
	public TelnetEvent(Client source, String message) {
		super(source);
		this.message = message;
	}
}