package mud.events;

import java.util.EventObject;

public class PortalEvent extends EventObject {
	private String message;
	
	public PortalEvent(Object source) {
		super(source);
	}
	
	public String getMessage() {
		return this.message;
	}
}