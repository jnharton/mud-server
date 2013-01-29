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
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
/**
 * Log Class
 * 
 * NOTE: this still needs work as it needs a better way to properly figure out where
 * to save files to and having a way to change the calendar easily and or maybe
 * the details of the Calendar object (timezone, etc) without creating a new calendar object
 * 
 * @author Jeremy N. Harton
 *
 */
public class Log
{
	private static String DATA_DIR = "data//";
	
	private Calendar calendar;
	
	private String date;
	private String time;
	private String filename;
	private File file;
	private PrintWriter output;

	//private int length;

	private boolean isOpen;
	private boolean isFull;

	// time, date, filename, etc
	// logging principle
	// open a new log file on start and restart respectively, close it on shutdown or crash
	// keep track of everything, with time stamps, the player doing it and the action involved, code executed, results, etc
	// log has automatic flushing by default, change this?, configurable?

	/**
	 * Default, No Argument Log constructor
	 */
	public Log()
	{
		this.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
		
		this.date = str(month() + 1) + "-" + str(day()) + "-" + str(year());
		this.time = str(hour()) + "-" + str(minute());
		this.filename = "log_" + this.date + "_" + this.time + ".txt";

		//this.length = 0;

		this.isOpen = false;
		this.isFull = false;
	}

	/**
	 * Alternate Log constructor that takes a specific filename
	 * and prefixes it to the default name in place of 'log'.
	 * 
	 * NOTE: Should this allow an entirely alternate name, i.e. is that
	 * something a person might expect?
	 * 
	 * @param filename
	 */
	public Log(String filename)
	{
		this.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
		
		this.date = str(month() + 1) + "-" + str(day()) + "-" + str(year());
		this.time = str(hour()) + "-" + str(minute());
		this.filename = filename + "_" + this.date + "_" + this.time + ".txt";

		//this.length = 0;

		this.isOpen = false;
		this.isFull = false;
	}

	/**
	 * Opens the log (and it's associated file) for writing.
	 */
	public void openLog()
	{
		if (!this.isOpen) {
			try {
				this.file = new File(DATA_DIR + "logs/" + this.filename);
				this.output = new PrintWriter(file);
			}
			catch(FileNotFoundException fnfe) {
				try {
					this.file.createNewFile();
					this.output = new PrintWriter(file);
					
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			this.isOpen = true;
		}
		else {
			System.out.println("Game> Log File already open, debug coding.");
		}
	}
	
	/**
	 * 
	 * @param b
	 */
	public void write(Byte b) {
		if (this.isOpen) {
			if (!this.isFull) {
				this.output.print(b + ' ');
				this.output.flush();
			}
		}
		else
		{
			System.out.println("ASMU> Log File not open, debug coding.");
		}
	}
	
	/**
	 * 
	 * @param b
	 */
	public void writeln(Byte b) {
		if (this.isOpen) {
			if (!this.isFull) {
				this.output.println(b + ' ');
				this.output.flush();
			}
		}
		else
		{
			System.out.println("ASMU> Log File not open, debug coding.");
		}
	}

	/**
	 * 
	 * @param message
	 */
	public void write(String message)
	{
		if (this.isOpen)
		{
			if (!this.isFull) {
				// update calendar
				this.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
				
				//
				String hour, minute, second;
				if (hour() < 10) { hour = "0" + str(hour()); }
				else { hour = str(hour()); }
				if (minute() < 10) { minute = "0" + str(minute()); }
				else { minute = str(minute()); }
				if (second() < 10) { second = "0" + str(second()); }
				else { second = str(second()); }
				String logString = "[" + hour + ":" + minute + ":" + second + "] " + message;
				this.output.print(logString);
				this.output.flush();
				//this.length++;
				//if (this.length == max_log_size) {
				//	this.isFull = true;
				//}
			}
		}
		else
		{
			System.out.println("ASMU> Log File not open, debug coding.");
		}
	}
	
	/**
	 * 
	 * @param message
	 */
	public void writeln(String message)
	{
		if (this.isOpen)
		{
			if (!this.isFull) {
				// update calendar
				this.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
				
				//
				String hour, minute, second;
				if (hour() < 10) { hour = "0" + str(hour()); }
				else { hour = str(hour()); }
				if (minute() < 10) { minute = "0" + str(minute()); }
				else { minute = str(minute()); }
				if (second() < 10) { second = "0" + str(second()); }
				else { second = str(second()); }
				String logString = "[" + hour + ":" + minute + ":" + second + "] " + message;
				this.output.println(logString);
				this.output.flush();
				//this.length++;
				//if (this.length == max_log_size) {
				//	this.isFull = true;
				//}
			}
		}
		else
		{
			System.out.println("ASMU> Log File not open, debug coding.");
		}
	}

	// method for logging player actions
	/**
	 * 
	 * @param temp
	 * @param action
	 */
	public void write(String playerName, int playerLoc, String action)
	{
		if (this.isOpen)
		{
			if (!this.isFull) {
				// update calendar
				this.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
				
				//
				String hour, minute, second;
				if (hour() < 10) { hour = "0" + str(hour()); }
				else { hour = str(hour()); }
				if (minute() < 10) { minute = "0" + str(minute()); }
				else { minute = str(minute()); }
				if (second() < 10) { second = "0" + str(second()); }
				else { second = str(second()); }
				String logString = "log failure";
				logString = "[" + hour + ":" + minute + ":" + second + "] (" + playerName + ") {Location: #" + playerLoc +  "}  " + action;
				this.output.print(logString);
				this.output.flush();
				//this.length++;
				// need a way to get the max log size
				//if (this.length == max_log_size) {
				//	this.isFull = true;
				//}
			}
		}
		else
		{
			System.out.println("ASMU> Log File not open, debug coding.");
		}
	}
	
	// method for logging player actions
		/**
		 * 
		 * @param temp
		 * @param action
		 */
		public void writeln(String playerName, int playerLoc, String action)
		{
			if (this.isOpen)
			{
				if (!this.isFull) {
					// update calendar
					this.calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"), new Locale("ENGLISH", "US"));
					
					//
					String hour, minute, second;
					if (hour() < 10) { hour = "0" + str(hour()); }
					else { hour = str(hour()); }
					if (minute() < 10) { minute = "0" + str(minute()); }
					else { minute = str(minute()); }
					if (second() < 10) { second = "0" + str(second()); }
					else { second = str(second()); }
					String logString = "log failure";
					logString = "[" + hour + ":" + minute + ":" + second + "] (" + playerName + ") {Location: #" + playerLoc +  "}  " + action;
					this.output.println(logString);
					this.output.flush();
					//this.length++;
					// need a way to get the max log size
					//if (this.length == max_log_size) {
					//	this.isFull = true;
					//}
				}
			}
			else
			{
				System.out.println("Game> Log File not open, debug coding.");
			}
		}

	/**
	 * A method to close the log (and it's corresponding file).
	 * 
	 * NOTE: Doing so does result in the stream being flushed.
	 */
	public void closeLog()
	{
		// if the log is open
		if ( this.isOpen() ) {
			this.output.flush();
			this.output.close();
			this.isOpen = false;
		}
		else {
			System.out.println("Game> Log File not open, debug coding.");
		}
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

	private int second() {
		return this.calendar.get(Calendar.SECOND);
	}
	private int minute() {
		return this.calendar.get(Calendar.MINUTE);
	}

	private int hour() {
		return this.calendar.get(Calendar.HOUR);
	}
	private int day() {
		return this.calendar.get(Calendar.DAY_OF_MONTH);
	}

	private int month() {
		return this.calendar.get(Calendar.MONTH);
	}

	private int year() {
		return this.calendar.get(Calendar.YEAR);
	}

	public static String str(Object o) {
		return o.toString();
	}
}