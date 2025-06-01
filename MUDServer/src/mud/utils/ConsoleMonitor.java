package mud.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mud.MUDServer;
import mud.misc.Zone;
import mud.net.Client;
import mud.objects.Player;

public class ConsoleMonitor {
	private MUDServer server;
	private Map<Client, Console> consoles;
	
	private List<Console> free_consoles;
	
	private String[] credentials = new String[] { "admin", "admin" };
	
	public ConsoleMonitor(final MUDServer server) {
		this.server = server;
		this.consoles = new HashMap<Client, Console>();
		
		this.free_consoles = new ArrayList<Console>(1);
	}
	
	public void createConsole(final Client client) {
		final Console console = new Console(client);
		console.setMonitor(this);
		
		this.consoles.put(client, console);
	}
	
	public Console getConsole(final Client client) {
		return this.consoles.get(client);
	}
	
	public void destroyConsole(final Client client) {
		this.consoles.remove(client);
	}
	
	public void purgeConsoles() {
		
	}
	
	public boolean hasConsole(final Client client) {
		boolean response = false;
		
		if( this.consoles.containsKey(client) ) {
			if( this.consoles.get(client) != null ) {
				response = true;
			}
		}
		
		return response;
	}
	
	protected void fireEvent(final Console source, final String message) {
		if( source != null ) {
			Client client = source.getClient();
			
			if( message.equals("checkmemory") ) {
				source.write( "" );
			}
			else if( message.equals("clients") ) {
				int cn = 0;
				
				source.write("-- " + Utils.padRight("Clients ", '-', 69));
				
				for (final Client c : server.getClients()) {
					final String ident = this.hasConsole(client) ? "Console" : server.loginCheck(c) ? "Logged-In" : "Not Logged-In";
					
					if (c != null) {
						source.write(cn + " " + c.getIPAddress() + " " + c.toString() + " " + ident );
					}

					//source.write(cn + " " + "---.---.---.--- null");
					cn++;
				}
			}
			else if( message.equals("logout") ) {
				source.write("Logged out of Console.");
				this.destroyConsole(source.getClient());
			}
			else if( message.equals("who") ) {
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

						String idle = "IDLE?"; //player.getIdleString();

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
			else if (message.equalsIgnoreCase("modereset") ) {
				server.changeMode(GameMode.WIZARD);
			}
		}
	}
}