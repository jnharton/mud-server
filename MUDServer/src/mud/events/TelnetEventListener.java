package mud.events;

import java.util.EventListener;

public interface TelnetEventListener extends EventListener {
	public void handleTelnetEvent(TelnetEvent pe);
}