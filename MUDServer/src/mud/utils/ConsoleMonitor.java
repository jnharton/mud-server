package mud.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mud.MUDServer;
import mud.net.Client;

public class ConsoleMonitor {
	private MUDServer server;
	private Map<Client, Console> consoles;
	
	public ConsoleMonitor(final MUDServer server) {
		this.server = server;
		this.consoles = new HashMap<Client, Console>();
	}
	
	public void addConsole(final Client client) {
		final Console console = new Console(client);
		console.setMonitor(this);
		this.consoles.put(client, console);
	}
	
	public Console getConsole(final Client client) {
		return this.consoles.get(client);
	}
	
	public void removeConsole(final Client client) {
		this.consoles.remove(client);
	}
	
	public boolean hasConsole(final Client client) {
		boolean response = false;
		
		if( consoles.containsKey(client) ) {
			if( consoles.get(client) != null ) {
				response = true;
			}
		}
		
		return response;
	}
	
	protected void fireEvent(final Console source, final String message) {
		if( source != null ) {
			if( message.equals("logout") ) {
				removeConsole(source.getClient());
			}
		}
	}
}