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

	private static int MAX_YEAR = 9999;

	private int month; // 1-12 (12 possible months)
	private int day;   // 1-31 (31 possible days)
	private int year;  // 0000-9999 (10,000 possible years)

	// date parsing regexp
	transient static private String re1="(\\d)";	// Any Single Digit 1
	transient static private String re2="(\\d)";	// Any Single Digit 2
	transient static private String re3="(-)";	// Any Single Character 1
	transient static private String re4="(\\d)";	// Any Single Digit 3
	transient static private String re5="(\\d)";	// Any Single Digit 4
	transient static private String re6="(-)";	// Any Single Character 2
	transient static private String re7="((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";	// Year 1

	//transient static private final String datePattern = re1+re2+re3+re4+re5+re6;
	transient static private final String datePattern = 
			"(\\d)(\\d)(-)(\\d)(\\d)(-)((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";

	public Date() {
	}

	// assume 1 index day and month input 
	public Date(int month, int day) {
		if( month >= 1 && month <= 12 ) {
			this.month = month;
		}
		else {
			if( month < 1 ) { this.month = 1; }
			else if( month > 12 ) { this.month = 11; } 
		}

		if( day >= 1 && day <= 31 ) {
			this.day = day;
		}
		else {
			if( day < 1 ) { this.day = 1; }
			else if( day > 31 ) { this.day = 31; }
		}
	}

	public Date(int month, int day, int year) {
		this(month, day);

		if( year >= 0 && year <= MAX_YEAR ) {
			this.year = year;
		}
		else {
			if( year > MAX_YEAR ) { this.year = MAX_YEAR; }
			else if( year < 0 ) { this.year = 0; }
		}
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