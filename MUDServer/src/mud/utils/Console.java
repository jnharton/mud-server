package mud.utils;

import java.util.List;

import mud.net.Client;

public class Console {
	private ConsoleMonitor cm;
	private Client client;
	
	public Console(final Client client) {
		this.client = client;
	}
	
	public void setMonitor(final ConsoleMonitor cmon) {
		this.cm = cmon;
	}
	
	public Client getClient() {
		return this.client;
	}

	public void processInput(final String _input) {
		String command = "";
		String arg = "";
		
		final String[] temp = _input.trim().split(" ");
		
		if( temp.length > 1 ) {
			command = temp[0];
			arg = temp[1];
		}
		else {
			command = _input.trim();
		}
		
		if( command.equalsIgnoreCase("help") || command.equals("?") ) {
			final List<String> commands = Utils.mkList( "clientinfo", "clients", "gc", "help", "logout", "mem", "who", "quit");
			client.writeln("Available Commands: " + Utils.join(commands, ", "));
		}
		else if( command.equalsIgnoreCase("clientinfo") ) {
			/*if( !arg.equals("") ) {
				int c = Utils.toInt(arg, -1);
				
				if( c != -1 ) {
					final Client client1 = server.getClients().get(c);
					
					client.writeln("-- " + client1);
					client.writeln("telnet:  " + client1.usingTelnet());
					client.writeln("console: " + cm.hasConsole(client1));
					client.writeln("");
					
					try {
						String playerName = server.getPlayer(client).getName();
						client.writeln("player: " + playerName);
					}
					catch(NullPointerException npe) {
						npe.printStackTrace();
					}
				}
			}*/
			
			client.writeln("clientinfo: disabled");
		}
		else if( command.equalsIgnoreCase("clients") ) {
			/*int cn = 0;
			
			client.writeln("-- " + Utils.padRight("Clients ", '-', 69));
			
			for (final Client c : server.getClients()) {
				final String ident = cm.hasConsole(client) ? "Console" : server.loginCheck(c) ? "Logged-In" : "Not Logged-In";
				
				if (c != null) {
					client.writeln(cn + " " + c.getIPAddress() + " " + c.toString() + " " + ident );
				}

				//client.writeln(cn + " " + "---.---.---.--- null");
				cn++;
			}*/
			
			//client.writeln("clientinfo: disabled");
			
			cm.fireEvent(this, "clients");
		}
		else if( command.equalsIgnoreCase("mem") ) {
			//client.writeln(Utils.checkMem());
			client.writeln("mem: disabled");
			cm.fireEvent(this, "checkmemory");
		}
		else if( command.equalsIgnoreCase("gc") ) {
			/*System.gc();
			client.writeln("Garbage Collection requested.");*/
			client.writeln("gc: disabled");
		}
		else if( command.equalsIgnoreCase("logout") ) {
			cm.fireEvent(this, "logout");
		}
		else if( command.equalsIgnoreCase("who") ) {
			cm.fireEvent(this, "who");
		}
		else {
			client.writeln("No such command.");
		}
	}
	
	public void write(final String message) {
		this.client.writeln(message);
	}
}