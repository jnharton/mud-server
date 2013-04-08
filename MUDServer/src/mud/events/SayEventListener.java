package mud.events;

import java.util.EventListener;

public interface SayEventListener extends EventListener {
	  public void handleSayEvent(SayEvent se);
}