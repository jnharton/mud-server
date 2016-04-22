package mud.events;

import java.util.EventListener;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 */

//import java.util.EventListener;

public interface EventSource {
	public void fireEvent(final String message);
	
	/*
	public void addEventListener(final EventListener listener);
	public void removeEventListener(final EventListener listener);
	*/
}