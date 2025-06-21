package mud.events;

import java.util.EventObject;

import mud.net.Client;

@SuppressWarnings("serial")
public class TelnetEvent extends EventObject {
	String message;
	
	public TelnetEvent(Client source, String message) {
		super(source);
		this.message = message;
	}
}