package mud;

import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import mud.utils.Log;

public final class Logger {
	private final Map<String, Boolean> config;
	private final Map<String, Log> logs;
	
	private String directory = "";
	
	private boolean started = false;

	public Logger() {
		this.config = new LinkedHashMap<String, Boolean>();
		this.logs = new Hashtable<String, Log>();
	}

	public Logger(final Log...logFiles) {
		this();

		for(final Log log : logFiles) {
			this.logs.put(log.getName(), log);
		}

	}
	/**
	 * addConfigOption
	 * 
	 * Adds a new config option to the config map and sets an initial value.
	 * 
	 * @param option
	 * @param initialValue
	 */
	public void addConfigOption(final String option, final Boolean initialValue) {
		this.config.put(option, initialValue);
	}

	/**
	 * setConfigOption
	 * 
	 * Sets the value of the specified option, if it exists.
	 * 
	 * NOTE: options which don't exist can't be set
	 * 
	 * @param option some config option
	 * @param value  boolean value (true/false)
	 */
	public void setConfigOption(final String option, final Boolean newValue) {
		if( this.config.containsKey(option) ) {
			this.config.put(option, newValue);
		}
	}

	/**
	 * getConfigOption
	 * 
	 * Get the current value of the specified option, if it exists.
	 * 
	 * @param option
	 * @return
	 */
	public Boolean getConfigOption(final String option) {
		if( this.config.containsKey(option) ) {
			return this.config.get(option);
		}

		return false;
	}

	public Boolean hasConfigOption(final String option) {
		return this.config.containsKey(option);
	}

	/**
	 * Get Map containing config options
	 * 
	 * ? rename to getConfig
	 * 
	 * NOTE: this reference is NOT modifiable (no add or remove possible)
	 * 
	 * @return
	 */
	public Map<String, Boolean> getConfig() {
		return Collections.unmodifiableMap(this.config);
	}
	
	public void setDirectory(final String dirPath) {
		this.directory = dirPath;
	}
	
	public void register(final Log log) {
		if( this.logs.containsKey( log.getName() ) ) {
			System.out.println("That log conflicts with an existing log by the same name.");
		}
		else {
			log.setLogDirectory(directory);
			
			this.logs.put(log.getName(), log);
			
			if( started ) {
				log.openLog();
			}
		}
	}
	
	public Log getLog(final String name) {
		Log l = null;
		
		if( this.logs.containsKey( name ) ) {
			l = logs.get(name);
		}
		
		return l;
	}

	public void start() {
		for(final Log log : this.logs.values()) {
			log.openLog();
		}

		this.started = true;
	}

	public void stop() {
		for(final Log log : this.logs.values()) {
			log.closeLog();
		}
		
		this.started = false;
	}

	// MUDServer
	// public void attach(Logger l) { ... }
	// ...
	// e.g. Logger.log("debug", "this is a log string)
	// use constant strings to refer to the log
	public void log(final String logName, final String logString) {
		final Log log = logs.get(logName);

		if( log != null ) {
			log.writeln(logString);
		}
		else {
			System.out.println("No such Log!");
		}
	}

	/*public void log(final String logName, final String...logData) {
		final Log log = logs.get(logName);

		if( log != null ) {
			switch(log.getType()) {
			case LOG:
				// TODO employ pattern matching?
				if( logData.length >= 3 ) {
					logAction(log, logData[0], Utils.toInt(logData[1], -1), logData[2]);
				}
				else log.writeln( String.join(" ", logData) );
				break;
			case ERROR:
				break;
			case DEBUG:
				break;
			case CHAT:
				break;
			default:
				break;
			}
		}
		else {
			System.out.println("No such Log!");
		}
	}*/

	/*private void log() {}

	private void logAction() {}

	public void logAction(final Log log, final String playerName, final Integer playerLoc, final String action) {
		log.writeln( String.format("(%s) {Location: #%d}  %s", playerName, playerLoc, action) );
	}

	private void logChat() {}

	private void logConnect() {}

	private void logDebug() {}

	private void logDisconnect() {}*/
}