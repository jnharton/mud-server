package mud.utils;

import java.util.LinkedList;
import java.util.List;

import mud.MUDServer;
import mud.ObjectFlag;
import mud.misc.Zone;
import mud.net.Client;
import mud.objects.Player;

public class Console {
	private MUDServer server;
	private Client client;
	
	public Console(MUDServer server, Client client) {
		this.server = server;
		this.client = client;
	}

	public void processInput(final String _input, final Client client) {
		String input = _input.trim();
		
		if( input.equalsIgnoreCase("help") ) {
			client.writeln("Available Commands: clients, help, logout, mem, gc, quit");
		}
		else if ( input.equalsIgnoreCase("clients") ) {
			int cn = 0;
			client.writeln("-- " + Utils.padRight("Clients ", '-', 69));
			for (Client c : server.getClients()) {
				final String ident = c.isConsole() ? "Console" : server.loginCheck(c) ? "Logged-In" : "Not Logged-In";
				
				if (c != null) {
					client.writeln(cn + " " + c.getIPAddress() + " " + c.toString() + " " + ident );
				}

				//client.writeln(cn + " " + "---.---.---.--- null");
				cn++;
			}
		}
		else if( input.equalsIgnoreCase("mem") ) {
			client.writeln(server.checkMem());
		}
		else if( input.equalsIgnoreCase("gc") ) {
			System.gc();
			client.writeln("Garbage Collection requested.");
		}
		else if( input.equalsIgnoreCase("logout") ) {
			client.write("Logging out of Console...");
			client.setConsole(false);
			client.writeln("Done.");
		}
		else if( input.equalsIgnoreCase("who") ) {
			List<String> output = new LinkedList<String>();

			int n = 0;

			output.add("Player     Class     S Race      Idle Location");

			output.add(Utils.padRight("", '-', 74));

			for (final Player player : server.getPlayers())
			{
				try {
					String name = player.getName();                       // need to limit name to 10 characters
					String playerClass = player.getPClass().getName();
					String playerGender = player.getGender().toString();
					String race = player.getRace().toString();
					Integer location = player.getLocation();              // set room # limit to 5 characters (max. 99999)
					String roomName = server.getRoom(location).getName(); // truncate to 24 characters?
					String locString = "";

					//Zone zone = getZone( getRoom( location ) );
					Zone zone = server.getRoom( location ).getZone();

					if( zone != null ) {
						locString = zone.getName();
					}
					else { locString = roomName; }

					String idle = player.getIdleString();

					output.add(Utils.padRight(name, 10) + " " + Utils.padRight(playerClass, 9) + " " + Utils.padRight(playerGender, 1) + " " + Utils.padRight(race, 9) + " " + Utils.padRight(idle, 4) + " " + locString);
					
					n++;
				}
				catch(NullPointerException npe) {
					System.out.println("--- Stack Trace ---");
					npe.printStackTrace();
				}
			}
			
			output.add(Utils.padRight("", '-', 74));
			
			output.add(n + " players currently online.");

			client.write( output );
		}
		else {
			client.writeln("Not Implemented!");
		}
	}
}