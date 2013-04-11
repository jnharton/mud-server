package mud.events;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 */

import java.util.EventObject;
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
