package mud.events;

import java.util.EventObject;

import mud.objects.Player;
import mud.objects.Room;

public class SayEvent extends EventObject {
	private String message;
	
	public SayEvent(Room source, String message) {
		super(source);
		this.message = message;
	}
	
	public String getMessage() {
		return this.message;
	}
}
