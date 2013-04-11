package mud.events;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 */

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