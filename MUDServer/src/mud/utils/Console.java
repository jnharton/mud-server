package mud.utils;

import mud.MUDServer;
import mud.net.Client;

public class Console {
	private MUDServer server;
	private Client client;
	
	public Console(MUDServer server, Client client) {
		this.server = server;
		this.client = client;
	}

	public void processInput(final String _input, final Client client) {
		String input = _input.trim();
		
		if( input.equals("help") ) {
			client.writeln("Available Commands: clients, help, mem, gc, quit");
		}
		else if ( input.equals("clients") ) {
			int cn = 0;
			client.writeln("-- " + Utils.padRight("Clients ", '-', 69));
			for (Client c : server.s.getClients()) {
				final String ident = c.isConsole() ? "Console" : server.loginCheck(c) ? "Logged-In" : "Not Logged-In";
				if (c != null) {
					if(c == client) {
						client.writeln(cn + " " + Utils.padRight(c.ip()) + " " + c.toString() + "[YOU]" + " " + ident );
					}
					else {
						client.writeln(cn + " " + c.ip() + " " + c.toString() + " " + ident);
					}
				}
				else {
					client.writeln(cn + " " + "---.---.---.--- null");
				}
				cn++;
			}
		}
		else if( input.equals("mem") ) {
			client.writeln(server.checkMem());
		}
		else if( input.equals("gc") ) {
			System.gc();
			client.writeln("Garbage Collection requested.");
		}
		else if( input.equals("logout") ) {
			client.write("Logging out of Console...");
			client.setConsole(false);
			client.writeln("Done.");
		}
		else {
			client.writeln("Not Implemented!");
		}
	}
}