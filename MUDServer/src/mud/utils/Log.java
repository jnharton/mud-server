package mud.utils;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Log Class
 * 
 * NOTE: this still needs work as it needs a better way to properly figure out where
 * to save files to and having a way to change the calendar easily and or maybe
 * the details of the Calendar object (timezone, etc) without creating a new calendar object
 * 
 * NOTE2: as things currently stand, any Log object created without a max length
 * parameter will have a max length of -1 (infinite) which may result in very large
 * log files if the game does not crash or is not shutdown/restarted.
 * 
 * @author Jeremy N. Harton
 *
 */
public class Log {
	public static enum Type { LOG, ERROR, DEBUG, CHAT };
	
	private static final Integer BUFFER_SIZE = 1000;
	private static final Integer MAX_LOG_SIZE = 100000;
	
	private String LOG_DIR;
	
	private String name;
	private String filename;
	private Type type;
	private PrintWriter output;
	
	// status
	private boolean isOpen = false;
	private boolean isFull = false;
	
	// options
	private boolean useBuffer = false;
	private boolean useTimestamp = true;
	
	// other
	private int lines_written;
	private int log_num = 1;
	
	private List<String> buffer = null;

	// time, date, filename, etc
	// logging principle
	// open a new log file on start and restart respectively, close it on shutdown or crash
	// keep track of everything, with time stamps, the player doing it and the action involved, code executed, results, etc
	// log has automatic flushing by default, change this?, configurable?

	/**
	 * Default, No Argument Log constructor
	 */
	public Log() {
		this("log");
	}

	/**
	 * Alternate Log constructor that takes a specific filename
	 * and prefixes it to the default name in place of 'log'.
	 * 
	 * @param name
	 */
	public Log(final String name) {
		this(name, false);
	}
	
	public Log(final String name, final boolean buffer) {
		this(name, buffer, true);
	}
	
	public Log(final String name, final boolean buffer, final boolean timestamp) {
		// TODO reduce redundant timestamp code
		final TimeZone tz = TimeZone.getTimeZone("America/New_York");
		final Locale lc = new Locale("ENGLISH", "US");
		
		final Calendar cal = Calendar.getInstance(tz, lc);
		
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		
		getTimeString('-');
		
		final String date = (month + 1) + "-" + day + "-" + year;
		final String time = hour + "-" + minute;
		
		this.name = name;
		this.filename = name + "_" + date + "_" + time + ".txt";
		
		this.useBuffer = buffer;
		this.useTimestamp = timestamp;
		
		if( this.useBuffer ) this.buffer = new ArrayList<String>(Log.BUFFER_SIZE);
		
		this.lines_written = 0;
	}
	
	public void setLogDirectory(final String dir) {
		this.LOG_DIR = dir;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getFileName() {
		return this.filename;
	}
	
	public Type getType() {
		return this.type;
	}
	
	/**
	 * A method to determine if the log is open (active)
	 * 
	 * @return boolean representing whether the log (and correspondingly the file it's output is tied to) is open
	 */
	public boolean isOpen() {
		return this.isOpen;
	}

	public boolean isFull() {
		return this.isFull;
	}
	
	public int getLinesWritten() {
		return this.lines_written;
	}
	
	/**
	 * Opens the log (and it's associated file) for writing.
	 */
	public void openLog()
	{
		if ( !this.isOpen ) {
			final File file = new File( resolvePath(this.LOG_DIR, this.filename) );
			
			try {
				// make sure we have a real file
				if( !file.exists() ) {
					System.out.println( String.format("%s: %s", name, "Creating file...") );
					file.createNewFile();
				}
				
				this.output = new PrintWriter(file);
				this.isOpen = true;
			}
			catch (final FileNotFoundException fnfe) {
				System.out.println("File not found!");
				System.out.println("--- Stack Trace ---");
				fnfe.printStackTrace();
			}
			catch (final IOException e) {
				System.out.println("Bad path: " + file);
				System.out.println("--- Stack Trace ---");
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Log File already open.");
		}
	}

	/**
	 * 
	 * @param message
	 */
	public void writeln(final String message)
	{
		if ( this.isOpen ) {
			if ( !this.isFull ) {
				final String timeString = getTimeString();
				
				String logString = "";
				
				if( useTimestamp ) logString = "[" + timeString + "] " + message;
				else               logString = message;

				logString.trim();

				if( this.useBuffer ) {
					// flush buffer to file if full
					if( this.buffer.size() >= Log.BUFFER_SIZE ) {
						for(final String s : this.buffer) this.output.println(s);
						
						this.output.flush();
						this.buffer.clear();
					}
					
					this.buffer.add( logString );
				}
				else {
					this.output.println(logString);
					this.output.flush();
				}
				
				this.lines_written++;

				if ( this.lines_written == Log.MAX_LOG_SIZE ) {
					this.isFull = true;
				}
			}
			else {
				if( this.useBuffer ) {
					// flush ENTIRE buffer since we're closing this log file
					for(final String s : this.buffer) this.output.println(s);
					
					this.output.flush();
					this.buffer.clear();
				}
				
				// report full log
				System.out.println("Log Full!");
				
				// close current log file and open a new one and call write again
				closeLog();
				
				// change filename (e.g. test_date_time.log > test1_date_time.log)
				final String prefix = filename.substring(0, filename.indexOf('_'));
				
				filename.replace(prefix, prefix + this.log_num);
				
				this.log_num++;

				// open log
				openLog();
			}
		}
		else System.out.println("Log File not open.[" + getFileName() + "]");
	}

	/**
	 * A method to close the log (and it's corresponding file).
	 * 
	 * NOTE: Doing so does result in the stream being flushed.
	 */
	public void closeLog()
	{
		// if the log is open
		if ( this.isOpen ) {
			this.output.flush();
			this.output.close();
			
			this.isOpen = false;
		}
		else {
			System.out.println("Log File not open. [" + getFileName() + "]");
		}
	}
	
	private String getTimeString() {
		return getTimeString(':');
	}
	
	/**
	 * Get a Calendar instance for the purposes of checking the time, so that log entries
	 * can be timestamped. This gets the current time and generates a string.
	 * 
	 * @return
	 */
	private String getTimeString(final char sep) {
		final TimeZone tz = TimeZone.getTimeZone("America/New_York");
		final Locale lc = new Locale("ENGLISH", "US");
		
		final Calendar cal = Calendar.getInstance(tz, lc);
		
		String hour, minute, second;
		
		int h = cal.get(Calendar.HOUR);
		int m = cal.get(Calendar.MINUTE);
		int s = cal.get(Calendar.SECOND);

		if (h < 10) hour = "0" + h;
		else        hour = "" + h;

		if (m < 10) minute = "0" + m;
		else        minute = "" + m;

		if (s < 10) second = "0" + s;
		else        second = "" + s;
		
		return hour + sep + minute + sep + second ;
	}
	
	private final String resolvePath(final String path, final String...dirs) {
		return "" + Paths.get(path, dirs).toAbsolutePath();
	}
}