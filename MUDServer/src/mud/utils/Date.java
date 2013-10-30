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

import java.io.Serializable;
import java.util.regex.Pattern;

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
	private static final long serialVersionUID = 1L;

	private static int MAX_DAY = 31;
	private static int MAX_MONTH = 12;
	private static int MAX_YEAR = 9999;

	private int month; // 1-12 (12 possible months)
	private int day;   // 1-31 (31 possible days)
	private int year;  // 0000-9999 (10,000 possible years)

	// date parsing regexp
	public static final String datePattern = "(\\d)(\\d)(-)(\\d)(\\d)(-)((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";

	// assume 1 index day and month input 
	public Date(int month, int day) {
		this(month, day, -1);
	}

	public Date(int month, int day, int year) {
		if( month >= 1 && month <= 12 ) {
			this.month = month;
		}
		else {
			this.month = 1; 
		}

		if( day >= 1 && day <= 31 ) {
			this.day = day;
		}
		else {
			this.day = 1;
		}

		if( year >= 0 && year <= 9999 ) {
			this.year = year;
		}
		else {
			this.year = 0;
		}
	}
	
	public Date(Date toCopy) {
		this.day = toCopy.day;
		this.month = toCopy.month;
		this.year = toCopy.year;
	}

	/**
	 * returns a number between 1 and 30
	 * 
	 * @return
	 */
	public int getDay() {
		return this.day;
	}

	/**
	 * returns a number between 1 and 12
	 * 
	 * @return
	 */
	public int getMonth() {
		return this.month;
	}

	/**
	 * returns a number between 0 and MAX_YEAR
	 *  
	 * @return
	 */
	public int getYear() {
		return this.year;
	}

	class DateFormatException extends IllegalArgumentException {
		public DateFormatException() {}

		public DateFormatException(String s) {
			super(s);
		}
	}

	// Date Pattern: MM-DD-YYYY
	public Date parseDate(String s) throws DateFormatException {
		if( s.matches(datePattern) ) {
			int[] dateData = Utils.stringsToInts(s.split("-"));
			return new Date(dateData[0], dateData[1], dateData[2]);
		}
		else {
			throw new DateFormatException("Does not match date format: MM-DD-YYYY");
		}
	}

	/**
	 * 
	 * @return string that represents a date
	 */
	public String toString() {
		return this.month + "-" + this.day + "-" + this.year;
	}
}