package mud.utils;

import java.io.Serializable;

/**
 * 
 * note: this class has self imposed limitations on the dates that will be stored, there is
 * no reason that cannot be changed, nothing in Java inherently creates those limits
 * 
 * @author Jeremy
 *
 */
public class Date implements Serializable {
	/**
	 * 
	 */
	
	private int month; // 0-11 (12 possible months)
	private int day;   // 0-29 (30 possible days)
	private int year;  // 0000-9999 (10,000 possible years)

	public Date() {
	}
	
	// assume 1 index day and month input 
	public Date(int month, int day) {
		if (month > 12) { this.month = 11; }
		else { this.month = month; }
		if (day > 30) { this.day = 29; }
		else { this.day = day; }
	}
	
	public Date(int month, int day, int year) {
		if (month > 12) { this.month = 11; }
		else { this.month = month; }
		if (day > 30) { this.day = 29; }
		else { this.day = day; }
		if (year > 9999) { this.year = 9999; }
		else { this.year = year; }
	}

	// returns a number between 0 and 29 (1 and 30)
	public int getDay() {
		return this.day;
	}

	/**
	 * returns a number between 0 and 11 (1 and 12)
	 * 
	 * @return
	 */
	public int getMonth() {
		return this.month;
	}
	
	/**
	 * returns a number between 0 and 9999 (1 and 10000)
	 *  
	 * @return
	 */
	public int getYear() {
		return this.year;
	}
	
	/**
	 * 
	 * @return string that represents a date
	 */
	public String toString() {
		return this.month + "-" + this.day + "-" + this.year;
	}
}