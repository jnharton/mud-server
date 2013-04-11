package mud.events;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 */

//import java.util.EventListener;

public interface EventSource {
	public void fireEvent(String message);
	
	//public void addEventListener(EventListener listener);
	//public void removeEventListener(EventListener listener);
}