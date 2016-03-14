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
	
	public Console(final MUDServer server, final Client client) {
		this.server = server;
		this.client = client;
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
		
		if( command.equalsIgnoreCase("help") ) {
			final List<String> commands = Utils.mkList(
					"clientinfo", "clients", "gc", "help", "logout", "mem", "who", "quit");
			client.writeln("Available Commands: " + Utils.join(commands, ","));
		}
		else if( command.equalsIgnoreCase("clientinfo") ) {
			if( !arg.equals("") ) {
				int c = Utils.toInt(arg, -1);
				
				if( c != -1 ) {
					final Client client1 = server.getClients().get(c);
					
					client.writeln("-- " + client1);
					client.writeln("telnet:  " + client1.usingTelnet());
					client.writeln("console: " + client1.isConsole());
					client.writeln("");
					
					try {
						String playerName = server.getPlayer(client).getName();
						client.writeln("player: " + playerName);
					}
					catch(NullPointerException npe) {
						npe.printStackTrace();
					}
				}
			}
		}
		else if( command.equalsIgnoreCase("clients") ) {
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
		else if( command.equalsIgnoreCase("mem") ) {
			client.writeln(Utils.checkMem());
		}
		else if( command.equalsIgnoreCase("gc") ) {
			System.gc();
			client.writeln("Garbage Collection requested.");
		}
		else if( command.equalsIgnoreCase("logout") ) {
			/*client.write("Logging out of Console...");
			client.setConsole(false);
			client.writeln("Done.");*/
			
			client.setConsole(false);
			
			client.writeln("Logged out of Console.");
			
			client.writeln("");
			
			client.write( Utils.mkList(
					"To connect to your character use 'connect <playername> <password>'",
					"To connect to your account use 'connect <username> <password>'",
					"To create a character use 'create <playername> <password>'",
					"To connect as a guest use 'connect guest'"
					));
			client.writeln("");
			
			//client.writeln("Mode: " + ser
		}
		else if( command.equalsIgnoreCase("who") ) {
			List<String> output = new LinkedList<String>();

			int n = 0;

			output.add("Player     Class     S Race      Idle Location");

			output.add(Utils.padRight("", '-', 74));

			for (final Player player : server.getPlayers())
			{
				try {
					String name = player.getName();                       // need to limit name to 10 characters
					String playerClass = player.getPClass().getName();
					String playerGender = "" + player.getGender();
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