package mud.events;

//import java.util.EventListener;

public interface EventSource {
	public void fireEvent(String message);
	
	//public void addEventListener(EventListener listener);
	//public void removeEventListener(EventListener listener);
}