package mud.foe;

import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import mud.Command;
import mud.ObjectFlag;
import mud.foe.items.PipBuck;
import mud.foe.misc.Device;
import mud.foe.misc.FileSystem;
import mud.foe.misc.FileSystem.File;
import mud.foe.misc.Port;
import mud.foe.misc.User;
import mud.foe.misc.Module;
import mud.interfaces.ExtraCommands;
import mud.net.Client;
import mud.objects.Thing;
import mud.utils.Time;
import mud.utils.Utils;
import mud.utils.loginData;

/**
 * 
 * @author  Jeremy
 * @version 0
 * 
 * This class represents a Stable-Tec terminal from Fallout Equestria, which
 * itself resembles a Vault-Tec terminal from Fallout(*)
 * 
 * Fallout is a Copyrighted/Trademarked name belonging to Bethesda Softworks,
 * a.k.a 'Bethesda'
 */
public class Terminal extends Thing implements Device, ExtraCommands {
	// power state:  NO_POWER, POWER, POWER_OFF
	// login state:  LOGGED_IN, LOGGED_OUT, GET_USER, GET_PASS
	// usable state: USABLE, LOCKOUT, BROKEN

	public enum Power { NO_POWER, POWER_OFF, POWER_ON };
	public enum Login { LOGGED_IN, LOGGED_OUT, GET_USER, GET_PASS, LOCKOUT};
	public enum Use { USABLE, BROKEN };

	/* the states above are pseudo-independent. Obviously usable/broken
	 * is a world level hardware state that effects whether the device 
	 * can ever be used, while the power states indicate whether the device
	 * has power OR if, having power, the device is on or off. If the
	 * device is usable, has power, and is ON then the login state comes in to
	 * play. If the login state is knocked into lockout (which is a persistent
	 * value/state independent of power) the device will become entirely unusable
	 * unless it is reset in some fashion.
	 * 
	 * The power being ON or OFF means that there is power available. Somehow cutting power
	 * will force the terminal into NO power so that a power connect will have to be noticed somehow
	 * before it can be turned on again
	 */

	//enum Screens { HACK, LOGIN, PROMPT };

	private String name = ""; // ?

	private Power power_state = Power.POWER_OFF;
	private Login login_state = Login.LOGGED_OUT;
	private Use usable_state = Use.USABLE;

	// Hardware
	private LinkedList<Port> ports;          // all ports
	private Hashtable<Device, Port> devices; // no null values

	public int id = 0; // device ids

	// Firmware/Software?
	private FileSystem fs;
	private Hashtable<String, User> users;             // no null values

	// I/O
	private Queue<String> input;
	private Queue<String> output;

	private BufferedReader reader;

	// Screen
	private Queue<String> screen_buffer;
	private int screen_height = 25;

	// user data
	private String current_user = "";
	private String current_dir = "/";

	// meta-state for controlling execution/thread
	private boolean running = false;
	public boolean paused = false;
	private boolean startup = true;

	// misc?
	private loginData ldat;
	private static String PASSWORD = "CMC-ARE-AWESOME";

	/*
	 * Relevant to their use in a game, running should mean that the terminal
	 * is powered on and so on, whereas paused should mean that the terminal is not
	 * presently in use but is only relevant if running is TRUE.
	 */
	public Terminal(final String tName) {
		super(tName);
		this.ldat = new loginData(0);
	}

	public Terminal(final String tName, final String tDesc) {
		this(tName);
		this.desc = tDesc;
	}

	public Terminal(final String tName, final String tDesc, final Power tPState, final Login tLState, final Use tUState) {
		this(tName, tDesc);

		if(tPState != null) this.power_state = tPState;
		else                this.power_state = Power.POWER_OFF;

		if(tLState != null) this.login_state = tLState;
		else                this.login_state = Login.LOGGED_OUT;

		if(tUState != null) this.usable_state = tUState;
		else                this.usable_state = Use.USABLE;
	}

	public Terminal(int tempDBRef, String tempName, EnumSet<ObjectFlag> tempFlags, String tempDesc, int tempLoc) {
		super(tempDBRef, tempName, tempFlags, tempDesc, tempLoc);
	}

	static public void main(String[] args) {
		/*Terminal term = new Terminal("Terminal", "", Terminal.Power.POWER_ON);

		term.init();
		term.setup();

		Scanner s = new Scanner(System.in);

		if( term.power_state == Terminal.Power.POWER_ON ) {
			term.writeToScreen( Utils.center( "STABLE-TEC INDUSTRIES UNIFIED OPERATING SYSTEM", 80 ) );
			term.writeToScreen( Utils.center( "COPYRIGHT 1015-1020 STABLE-TEC INDUSTRIES", 80 ) );

			while( term.power_state == Terminal.Power.POWER_ON ) {

				while( !term.screen_buffer.isEmpty() ) {
					System.out.println( term.screen_buffer.poll() );
				}

				switch( term.login_state ) {
				case LOGGED_OUT:
					System.out.print("User?> ");
					String user = s.nextLine();
					System.out.print("Password?> ");
					String pass = s.nextLine();
					term.login(user, pass);
					break;
				case LOGGED_IN:
					System.out.print("TERM> ");
					term.processInput( s.nextLine() );
					break;
				case LOCKOUT:
					break;
				default:
					term.power_state = Terminal.Power.POWER_OFF;
					break;
				}
			}
		}

		s.close();

		/*term.power_state = Power.POWER_ON;

		new Thread(term).start();*/
	}

	public void init() {
		ports = new LinkedList<Port>();
		devices = new Hashtable<Device, Port>();

		fs = new FileSystem();
		users = new Hashtable<String, User>();

		screen_buffer = new LinkedList<String>();

		input = new LinkedList<String>();
		output = new LinkedList<String>();

		running = true;
		paused = false;

		addUser("admin", PASSWORD);
	}

	public void exec() {
		if( this.running ) {
			if( !this.paused ) {
				if( this.input != null && this.output != null ) {
					int code = -1;
					String s = "";

					synchronized(input) {
						s = input.poll();

						if( s != null ) {
							code = processInput(s); // process input
						}
					}

					synchronized(output) {
						if( !this.screen_buffer.isEmpty() ) {
							s = this.screen_buffer.poll();

							while( s != null ) {
								output.add(s);

								s = this.screen_buffer.poll();
							}
						}
					}

					if( code == 0 ) {
						this.paused = true;
					}
				}
			}
		}
	}

	public String getDeviceName() {
		return "Stable-Tec Terminal";
	}

	public DeviceType getDeviceType() {
		return DeviceType.TERMINAL;
	}

	/* connect <device> to <port> */
	public boolean connect(final Device device) {
		if( hasPort( device.getDeviceType() ) ) {
			if( !isDeviceConnected( device ) ) {
				devices.put(device, getPort( device.getDeviceType() ));
			}
		}

		return false;
	}

	/* disconnect <device> */
	public void disconnect(final Device device) {
		if( isDeviceConnected( device ) ) {
			devices.remove( device );
		}
	}

	private Port getPort(final DeviceType deviceType) {
		for(final Port port : ports) {
			if( port.type == deviceType ) {
				if( !devices.containsValue(port) ) return port;
			}
		}

		return null;
	}

	private boolean hasPort(final DeviceType deviceType) {
		boolean hasPort = false;

		if( getPort(deviceType) != null ) hasPort = true;

		return hasPort;
	}

	/* called to enumerate what devices are connected */
	private void scanPorts() {
		if( ports.size() != 0 ) {
			if( devices.size() != 0 ) {
				for(final Device dev : devices.keySet()) {
					System.out.println(devices.get( dev ) + " : " + dev);
				}
			}
			else System.out.println("No Connected Devices");
		}
		else System.out.println("No Ports");

	}

	public List<String> getScreen() {
		return Arrays.asList( (String[]) screen_buffer.toArray() );
	}

	public boolean isDeviceConnected( Device device ) {
		return devices.containsKey( device );
	}


	/* Terminal Interaction Stuff */

	public int processInput(final String input) {
		String[] args = input.split(" ");

		String cmd = "";
		String arg = "";

		if( args.length > 1 ) {
			cmd = args[0];
			arg = args[1];
		}
		else cmd = input;

		int code = 1;

		if( login_state == Login.GET_USER ) {
			// store input as user
			ldat.username = input;

			writeToScreen( "Password?> " );
			
			login_state = Login.GET_PASS;
		}
		else if( login_state == Login.GET_PASS ) {
			// store input as password
			ldat.password = input;

			if( login( ldat.username, ldat.password ) ) {
				writeToScreen("Logged in as \'" + ldat.username + "\'." );
				writeToScreen( "\n\n" );
				writeToScreen( "TERM> ");
				
				login_state = Login.LOGGED_IN;
			}
			else {
				writeToScreen( "" );
				writeToScreen( "No such user." );
				writeToScreen( "" );

				writeToScreen( "User?> " );

				login_state = Login.GET_USER;
			}
		}
		else if( login_state == Login.LOGGED_IN ) {
			if( cmd.equalsIgnoreCase("cd") ) {
				if( current_dir.equals("/") ) {
					if( !arg.equals("") ) {
						if( fs.hasDir(arg) ) current_dir = arg;
						else                 writeToScreen( "Invalid Directory." );
					}
				}
				else if( arg.equals("..") ) current_dir = "/";

				writeToScreen( "Changed directory to " + current_dir );
			}
			else if( cmd.equalsIgnoreCase("copy") ) { // ex. copy admin userguide test
				String[] params = arg.split(",");

				if( params.length == 3 ) {
					fs.copyFile(params[0], params[1], params[2]);
					writeToScreen( "'" + params[1] + "' copied to " + params[2] + " from " + params[0] + "." );
				}
			}
			else if( cmd.equalsIgnoreCase("delete") ) {
				String[] params = arg.split(",");

				if( params.length == 2 ) {
					fs.deleteFile(params[0], params[1]);
					writeToScreen( "'" + params[1] + "' deleted." );
				}
				else if( params.length == 1) {
					fs.deleteFile(current_dir, params[1]);
					writeToScreen( "'" + params[1] + "' deleted." );
				}
			}
			else if( cmd.equalsIgnoreCase("done") ) {
				return 0;
			}
			else if ( cmd.equalsIgnoreCase("help") ) {
				writeToScreen( "Commands" );
				writeToScreen( "\t cd, copy, delete, done, help, logout, ls, mkdir, pwd, time, view" );
			}
			else if( cmd.equalsIgnoreCase("ls") ) {
				if( !arg.equals("") ) {
					if( arg.equals("-v") ) {
						/*String[] dirs = fs.getDirectoryNames(current_dir).split(" ");
					for(String dirName : dirs) {
						writeToScreen( "D " + dirName);
					}*/
						for(final String fileName : fs.files.keySet()) {
							final File file = fs.files.get(fileName);

							if( file.isDir ) writeToScreen( "D " + fileName + "\n" );
							else             writeToScreen( "F " + fileName + "\n" );
						}
					}
				}
				else writeToScreen( fs.getDirectoryNames(current_dir) );
			}
			else if( cmd.equalsIgnoreCase("mkdir") ) {
				boolean newDirCreated = fs.newDir(arg);

				if( newDirCreated ) {
					writeToScreen( "Created new directory: " + arg );
				}
			}
			else if( cmd.equalsIgnoreCase("logout") ) {
				writeToScreen( "Logging out..." );

				login_state = Terminal.Login.LOGGED_OUT;

				writeToScreen( "Logged out." );

				writeToScreen( Utils.center( "STABLE-TEC INDUSTRIES UNIFIED OPERATING SYSTEM", 80 ) );
				writeToScreen( Utils.center( "COPYRIGHT 1015-1020 STABLE-TEC INDUSTRIES", 80 ) );
				writeToScreen( "" );

				writeToScreen( "User?> " );

				login_state = Login.GET_USER;
			}
			else if( cmd.equalsIgnoreCase("pwd") ) {
				if( !current_dir.equals("/") ) {
					writeToScreen( "/" + current_dir );
				}
				else writeToScreen("/");
			}
			else if( input.equalsIgnoreCase("time") ) {
				writeToScreen( getTime().toString() );
			}
			else if( cmd.equalsIgnoreCase("view") ) {
				if( fs.hasFile(current_dir, arg) ) {
					writeToScreen("Displaying File...");

					final FileSystem.File file = fs.getFile(current_dir, arg);

					for(String str : file.getContents()) {
						writeToScreen( str );
					}
				}
				else writeToScreen( "No such file." );
			}

			writeToScreen( "\n" + getPrompt() );
		}

		return code;
	}

	private boolean login(final String user, final String pass) {
		if( this.users.containsKey(user) ) {
			if( this.users.get(user).getPassword().equals( pass ) ) {
				this.login_state = Login.LOGGED_IN;

				this.current_user = user;
				this.current_dir = "/" + user;

				return true;
			}
		}

		return false;
	}

	/**
	 * getTime
	 * 
	 * get a brand new time object that holds the current time
	 * 
	 * NOTES:
	 * - includes hours, minutes, and seconds
	 * - only holds the exact time when called, does not do any counting or do anything else
	 * - this is the "real world" time not the GAME time.
	 * 
	 * @return
	 */
	private Time getTime() {
		// get current data
		Calendar rightNow = Calendar.getInstance();

		// get the hour, minute, and second
		int hour = rightNow.get(Calendar.HOUR);
		int minute = rightNow.get(Calendar.MINUTE);
		int second = rightNow.get(Calendar.SECOND);

		// return a new time object with the current time
		return new Time(hour, minute, second);
	}

	private void writeToScreen(final String line) {
		/*if( screen_buffer.size() < screen_height ) {
			screen_buffer.add(line);
		}
		else {
			screen_buffer.poll();    // toss out an old line?
			screen_buffer.add(line);
		}*/

		this.screen_buffer.add(line);
	}

	private void clearScreen() {
		this.screen_buffer.clear();
	}

	/**
	 * Add a user to the terminal (ADMIN privileges required)
	 * @param username
	 * @param password
	 */
	private void addUser(final String username, final String password) {
		// if such a user does not exist already
		if( !this.users.containsKey(username) ) {
			final User user = new User(username, password, 0);

			this.users.put(username, user); // put the username and password in the users table

			initUser(username); // intialize user space
		}
	}

	private void initUser(final String username) {
		if( this.users.containsKey(username) ) {
			this.fs.newDir(username);
			fs.newFile(username, "user_guide", new String[]{ "Using your new Stable-Tec terminal" });
		}
	}

	private void deleteUser(final String username, final String password) {
		if( this.users.containsKey(username) ) {
			if( this.users.get(username).equals( password ) ) {
				this.users.remove(username);
			}
		}
	}

	public void handle_login(final String input, final Client client) {
		switch( login_state ) {
		case LOGGED_OUT:
			switch(ldat.state) {
			case 0:
				client.writeln("User?> ");
				ldat.state = 1;
				break;
			case 1:
				ldat.username = input;
				ldat.state = 2;
			case 2:
				client.writeln("Password?> ");
				ldat.state = 3;
				break;
			case 3:
				ldat.password = input;
				login(ldat.username, ldat.password);
				break;
			default:
				break;
			}

			/*
			client.writeln("User?> ");
			//output.print("User?> ");
			String user = s.nextLine();
			//output.print("Password?> ");
			client.writeln("Password?> ");
			String pass = s.nextLine();
			login(user, pass);
			 */

			break;
		case LOGGED_IN:
			client.writeln("TERM>");
			//System.out.print("TERM> ");
			//processInput( s.nextLine() );
			processInput( input );
			break;
		case LOCKOUT:
			break;
		default:
			power_state = Terminal.Power.POWER_OFF;
			break;
		}
	}

	public String getPrompt() {
		String prompt = "";

		// if the terminal is ON and USABLE
		if( checkStatus(Power.POWER_ON, Use.USABLE) ) {
			switch( this.login_state ) {
			case LOGGED_IN: prompt = "TERM> "; break;
			case LOGGED_OUT:
				switch( this.login_state ) {
				case GET_USER: prompt = "User?> "; break;
				case GET_PASS: prompt = "Pass?> "; break;
				default:       prompt = "";        break;
				}
			default:        prompt = "> ";     break;
			}
		}

		return prompt;
	}

	public void setLoginState(Login newLoginState) {
		this.login_state = newLoginState;
	}

	public Login getLoginState() {
		return this.login_state;
	}

	public void setPowerState(Power newPowerState) {
		this.power_state = newPowerState;
	}

	public Power getPowerState() {
		return this.power_state;
	}

	public void write(final String string) {
		this.input.add(string);
	}

	public String read() {
		return output.poll();
	}

	@Override
	public List<Module> getModules() {
		return null;
	}

	@Override
	public Map<String, Command> getCommands() {
		return null;
	}
	// add to the above 'turn off', 'reset'
	// turn off: power off the terminal
	// reset: reset the terminal

	@Override
	public String toString() {
		return getName();
	}

	private boolean checkStatus(Power state1, Use state2) {
		return this.power_state == state1 && this.usable_state == state2;
	}

	public String powerOn() {
		String report = "no power";

		if( !(this.power_state == Power.NO_POWER) ) {
			if( this.power_state == Power.POWER_OFF ) {
				if( this.usable_state == Use.USABLE ) {
					this.power_state = Power.POWER_ON;

					//set initial screen data
					writeToScreen( Utils.center( "STABLE-TEC INDUSTRIES UNIFIED OPERATING SYSTEM", 80 ) );
					writeToScreen( Utils.center( "COPYRIGHT 1015-1020 STABLE-TEC INDUSTRIES", 80 ) );
					writeToScreen( "" );

					if( login_state == Login.LOGGED_OUT ) {
						writeToScreen( "User?> " );
						login_state = Login.GET_USER;
					}

					report = "usable";
				}

				report = "unusable";
			}
			else report = "power on";
		}

		return report;
	}

	public String powerOff() {
		String report = "no power";

		if( !(this.power_state == Power.NO_POWER) ) {
			if( this.power_state == Power.POWER_ON ) {
				if( this.usable_state == Use.USABLE ) {
					this.login_state = Login.LOGGED_OUT;

					writeToScreen("Powering Down...");

					this.power_state = Power.POWER_OFF;

					report = "usable";
				}

				report = "unusable";
			}
			else report = "power off";	
		}

		return report;
	}
}