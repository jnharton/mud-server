package mud.events;

import java.util.EventListener;

public interface PortalEventListener extends EventListener {
	public void handlePortalEvent(PortalEvent pe);
}
