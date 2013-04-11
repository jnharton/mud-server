package mud.events;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 */

import java.util.EventListener;

public interface PortalEventListener extends EventListener {
	public void handlePortalEvent(PortalEvent pe);
}
