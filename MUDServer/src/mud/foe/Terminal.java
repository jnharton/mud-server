package mud.foe;

import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

import mud.Constants;
import mud.ObjectFlag;
import mud.foe.misc.Device;
import mud.foe.misc.FileSystem;
import mud.foe.misc.FileSystem.File;
import mud.foe.misc.IODevice;
import mud.foe.misc.Port;
import mud.foe.misc.User;
import mud.foe.misc.Module;
import mud.objects.Thing;
import mud.utils.Pair;
import mud.utils.Time;
import mud.utils.Utils;
import mud.utils.LoginData;

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
public class Terminal extends Thing implements IODevice {
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
	
	private int id = 0; // device ids

	private Power power_state = Power.POWER_OFF;
	private Login login_state = Login.LOGGED_OUT;
	private Use usable_state = Use.USABLE;

	// Hardware
	private List<Port> ports;          // all ports
	private Map<Device, Port> devices; // no null values

	// Firmware/Software?
	private FileSystem fs;
	private Map<String, User> users;   // no null values
	
	//private Time localTime;
	
	// I/O
	private Queue<String> input;
	private Queue<String> output;

	// Screen
	private Queue<String> screen_buffer;
	private int screen_height = 25;

	// user data
	private String current_user = "";
	private String current_dir = "/";

	// meta-state for controlling execution/thread
	private boolean running;
	private boolean paused;
	
	private boolean startup = true;
	
	private boolean initialized = false;
	
	// misc?
	private LoginData ldat;
	
	private String input_state = "";
	private Pair<String> action = null;
	//private String data = "";
	
	private static String OVERRIDE = "CMC-ARE-AWESOME"; // hard coded back door...

	/*
	 * Relevant to their use in a game, running should mean that the terminal
	 * is powered on and so on, whereas paused should mean that the terminal is not
	 * presently in use but is only relevant if running is TRUE.
	 */
	public Terminal(final String tName) {
		super(tName, "");
	}

	public Terminal(final String tName, final String tDesc) {
		// TODO would like initial power state to be on, but this conflicts with powerOn()...
		this(tName, tDesc, Power.POWER_OFF, Login.LOGGED_OUT, Use.USABLE);
	}

	public Terminal(final String tName, final String tDesc, final Power tPState, final Login tLState, final Use tUState) {
		super(tName, tDesc);
		
		this.thing_type = FOEThingTypes.TERMINAL;
		
		this.ldat = new LoginData(Constants.USERNAME);

		if(tPState != null) this.power_state = tPState;
		else                this.power_state = Power.POWER_OFF;

		if(tLState != null) this.login_state = tLState;
		else                this.login_state = Login.LOGGED_OUT;

		if(tUState != null) this.usable_state = tUState;
		else                this.usable_state = Use.USABLE;
	}

	public Terminal(final int dbref, final String name, final EnumSet<ObjectFlag> flags, final String description, final int location) {
		super(dbref, name, flags, description, location);
	}

	static public void main(String[] args) {
		final Terminal term = new mud.foe.Terminal("Terminal", "A Stable-Tec terminal, old pre-war technology whose durability is plain to see. On the screen, passively glowing green text indicates that it awaits input.");
		
		term.init();
		term.powerOn();

		Scanner s = new Scanner(System.in);
		
		//
		
		while( !term.isPaused() ) {
			term.exec();

			String line = term.read();

			while(line != null) {
				System.out.print( line );
				
				line = term.read();
			}

			if( term.isPaused() ) {
				System.out.println("Stopped using terminal.");
			}
			
			if( s.hasNextLine() ) {
				term.write( s.nextLine() );
			}
		}
		
		//

		s.close();
	}
	
	// removed setPowerState(...), the only problem I might encounter is the notion
	// of supplying power to a formerly unpowered terminal

	public Power getPowerState() {
		return this.power_state;
	}
	
	// remove setLoginState(...) as nothing external should need to change this

	public Login getLoginState() {
		return this.login_state;
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
	
	public boolean isDeviceConnected( Device device ) {
		return devices.containsKey( device );
	}

	private Port getPort(final DeviceType deviceType) {
		for(final Port port : ports) {
			if( port.getPortType() == deviceType ) {
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

	/*public List<String> getScreen() {
		return Arrays.asList( (String[]) screen_buffer.toArray() );
	}*/
	
	public void setPaused(boolean value) {
		this.paused = value;
	}
	
	public boolean isPaused() {
		return this.paused;
	}
	
	public void init() {
		if( !this.initialized ) {
			// initialize port list and device map
			this.ports = new LinkedList<Port>();
			this.devices = new Hashtable<Device, Port>();

			this.fs = new FileSystem();
			this.users = new Hashtable<String, User>();

			// initialize input and output 
			this.input = new LinkedList<String>();
			this.output = new LinkedList<String>();
			
			//
			this.screen_buffer = new LinkedList<String>();

			// add users
			addUser("admin", OVERRIDE);

			this.users.get("admin").setPerm(4);

			this.initialized = true;
			
			// TODO running/paused should dependent on power state and use state...
			// start running, not paused
			this.running = true;
			this.paused = true;
			
			System.out.println("Terminal Initialized!");
		}
	}

	public void exec() {
		System.out.println("Terminal.exec");
		
		if( this.running && !this.paused ) {
			// if both input and output streams aren't null
			if( this.input != null && this.output != null ) {
				int code = -1;
				String s = "";

				synchronized(input) {
					// read a line of input
					s = input.poll();
					
					// if it's valid, process input and get result
					if( s != null ) {
						writeToScreen(s + "\n");
						
						code = processInput(s);
					}
				}

				synchronized(output) {
					if( !this.screen_buffer.isEmpty() ) {
						// get a line of output
						s = this.screen_buffer.poll();

						// if it's valid, output it and grab the next line
						while( s != null ) {
							output.add(s);

							s = this.screen_buffer.poll();
						}
					}
				}

				if( code == 0 ) this.paused = true; // this.running = false;
			}
		}
	}
	
	public String powerOn() {
		// TODO need to rework the logic below and maybe move the screen writes somewhere else
		String report = "no power";

		if( !(this.power_state == Power.NO_POWER) ) {
			if( this.power_state == Power.POWER_OFF ) {
				if( this.usable_state == Use.USABLE ) {
					this.power_state = Power.POWER_ON;
					
					System.out.println("Terminal On and Usable!");

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
				else report = "unusable";
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
	
	/* Terminal Interaction Stuff */

	private int processInput(final String input) {
		/*
		 * -- commands
		 * cd     change current directory
		 * copy   copy a file
		 * delete delete a file
		 * done   stop using the terminal (META)
		 * help   print help information
		 * ls     list files
		 * mkdir  make a new directory
		 * logout logout of the current user
		 * pwd    print working directory
		 * time   show the current time
		 * date?  show the current date
		 * view   print a file out to the console
		 */
		String[] args = input.split(" ");

		String cmd = "";
		String arg = "";

		if( args.length > 1 ) {
			cmd = args[0];
			arg = Utils.join( Arrays.asList(args).subList(1, args.length), " ");
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
			if( input_state == "confirm" ) {
				if( input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes") ) {
					doAction(action);
					action = null;
					input_state = "";
				}
				else if( input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no") ) {
					action = null;
					input_state = "";
				}
			}
			else if( cmd.equalsIgnoreCase("cd") ) {
				if( current_dir.equals("/") ) {
					if( !arg.equals("") && !arg.startsWith("../") ) {
						if( fs.hasDir(arg) ) current_dir = current_dir + arg;
						else                 writeToScreen( "Invalid Directory." );
					}
				}
				else if( arg.equals("..") ) current_dir = "/";
				else if( arg.equals(".") )  current_dir = "/" + current_user + "/";

				writeToScreen( "Changed directory to " + current_dir );
			}
			else if ( cmd.equalsIgnoreCase("new") ) {
				// commence edit session?
				// create a new file?
			}
			else if( cmd.equalsIgnoreCase("copy") ) { // ex. copy admin userguide test
				final String[] params = arg.split(",");

				if( params.length == 3 ) {
					fs.copyFile(params[0], params[1], params[2]);
					writeToScreen( "'" + params[1] + "' copied to " + params[2] + " from " + params[0] + "." );
				}
				else {
					writeToScreen("COPY: Error, insufficient parameters.");
				}
			}
			else if( cmd.equalsIgnoreCase("delete") ) {
				String[] params = arg.split(",");

				if( params.length == 2 ) {
					fs.deleteFile(params[0], params[1]);
					writeToScreen( "file deleted ('" + params[1] + "')" );
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
			else if ( cmd.equalsIgnoreCase("clear") ) {
				clearScreen();
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
					writeToScreen( current_dir );
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
			else if( users.get(current_user).getPerm() == 4 ) {
				// ADMIN commands
				if( cmd.equalsIgnoreCase("adduser") ) {
					requestAction("add_user", arg);
					return code;
				}
				else if( cmd.equalsIgnoreCase("deluser") ) {
					if( !arg.contains(" ") ) {
						requestAction("delete_user", arg);
						return code;
					}
				}
				else if( cmd.equalsIgnoreCase("modperm") ) {
					if( !arg.contains(" ") ) {
						requestAction("modify_user_perms", arg);
						return code;
					}
				}
				else if( cmd.equalsIgnoreCase("users") ) {
					for(final User user : users.values()) {
						writeToScreen(user.getName() + "\n");
					}
				}
			}

			writeToScreen( "\n" + getPrompt() );
		}

		return code;
	}
	
	private void requestAction(final String actionKey, final String actionParams) {
		switch(actionKey) {
		case "add_user":
			writeToScreen("CONFIRM adding user 'Stormy' (yes/no)?");
			input_state = "confirm";
			action = new Pair<String>(actionKey, actionParams);
			break;
		case "delete_user":
			writeToScreen("* WARNING: deleting user will destroy all their files, irreversably. *");
			writeToScreen("CONFIRM deleting user 'Stormy' (yes/no)?");
			input_state = "confirm";
			action = new Pair<String>(actionKey, actionParams);
			break;
		case "modify_user_perms":
			writeToScreen("CONFIRM modifying user permissions for Stormy (yes/no)?");
			input_state = "confirm";
			action = new Pair<String>(actionKey, actionParams);
			break;
		}
	}
	
	private void doAction(final Pair<String> action) {
		switch(action.one) {
		case "add_user":
			final String[] temp = action.two.split(" ");
			
			if( temp.length == 2 ) {
				if( addUser(temp[0], temp[1]) ) {
					writeToScreen("ADDUSER: Added user '" + temp[0] + "' with password '" + temp[1] + "'.");
				}
				else {
					writeToScreen("ADDUSER: Error, that user already exists!");
				}
			}
			else writeToScreen("ADDUSER: Error, missing parameter.");
			
			break;
		case "delete_user":
			if( deleteUser(action.two, users.get(action.two).getPassword()) ) {
				writeToScreen("DELUSER: Deleted user '" + action.two + "'.");
			}
			else {
				writeToScreen("DELUSER: Error, no such user!");
			}
			
			break;
		case "modify_user_perms":
			final String[] temp1 = action.two.split(" ");
			
			if( temp1.length == 2 ) {
				final User user = users.get(temp1[0]);
				
				if( user != null ) {
					final Integer oldPerm = user.getPerm();
					final Integer newPerm = Utils.toInt(temp1[0], oldPerm);
					
					if( newPerm != oldPerm ) {
						user.setPerm( newPerm );
						writeToScreen("MODPERM: Permissions changed to " + user.getPerm() + " for user '" + user.getName() + "'.");
					}
					else {
						writeToScreen("MODPERM: Error, new permissions same as old permissions, no changes made.");
					}
				}
				else {
					writeToScreen("MODPERM: Error, no such user!");
				}
			}
			else writeToScreen("MODPERM: Error, missing parameter.");
			
			break;
		}
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
	private boolean addUser(final String username, final String password) {
		return addUser(username, password, 0);
	}
	
	private boolean addUser(final String username, final String password, final Integer permission) {
		boolean success = false;
		
		// if such a user does not exist already
		if( !this.users.containsKey(username) ) {
			final User user = new User(username, password, permission);
			
			// put the username and password in the users table
			this.users.put(username, user);
			
			// initialize user space
			initUser(username);
			
			success = true;
		}
		
		return success;
	}

	private boolean deleteUser(final String username, final String password) {
		boolean success = false;
		
		if( this.users.containsKey(username) ) {
			if( this.users.get(username).equals( password ) ) {
				this.users.remove(username);
				
				success = true;
			}
		}
		
		return success;
	}
	
	private void initUser(final String username) {
		if( this.users.containsKey(username) ) {
			this.fs.newDir(username);
			fs.newFile(username, "user_guide", new String[]{ "Using your new Stable-Tec terminal" });
		}
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

	private String getPrompt() {
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
	
	// add to the above 'turn off', 'reset'
	// turn off: power off the terminal
	// reset: reset the terminal

	public boolean checkStatus(final Power state1, final Use state2) {
		return this.power_state == state1 && this.usable_state == state2;
	}
	
	/* Interfaces - Implemented Methods */
	
	@Override
	public String getDeviceName() {
		return "Stable-Tec Terminal";
	}
	
	@Override
	public DeviceType getDeviceType() {
		return DeviceType.TERMINAL;
	}
	
	@Override
	public List<Module> getModules() {
		return null;
	}
	
	@Override
	/**
	 * Write a line to the terminal input.
	 * 
	 * @param string
	 */
	public void write(final String string) {
		this.input.add(string);
	}
	
	@Override
	/**
	 * Read a line of terminal output.
	 * 
	 * @return
	 */
	public String read() {
		return output.poll();
	}
	
	@Override
	public String toString() {
		return getName();
	}
}