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
	
	private int month; // 0-11 (12 possible months)
	private int day;   // 0-29 (30 possible days)
	private int year;  // 0000-9999 (10,000 possible years)
	
	// date parsing regexp
	String re1="(\\d)";	// Any Single Digit 1
    String re2="(\\d)";	// Any Single Digit 2
    String re3="(-)";	// Any Single Character 1
    String re4="(\\d)";	// Any Single Digit 3
    String re5="(\\d)";	// Any Single Digit 4
    String re6="(-)";	// Any Single Character 2
    String re7="((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";	// Year 1
	
	private final String datePattern = re1+re2+re3+re4+re5+re6;

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
	
	class DateFormatException extends IllegalArgumentException {
		public DateFormatException() {}
		
		public DateFormatException(String s) {
			super(s);
		}
	}
	
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