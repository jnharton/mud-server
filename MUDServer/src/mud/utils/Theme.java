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

/*
 * This class is intended to have theme file data loaded into
 * it, so that I don't need to retain separate global variables
 * for all of this related data.
 * 
 * Ideally, it would have "state", in the sense that it would hold
 * the current date, etc.
 */
public class Theme {
	String name;
	String mud_name;
	String motd_file;
	
	private int day;
	private int month;
	private int year;
	
	private String season;
	String reckon;
	
	String[] months;
	String[] months_alt;
	
	public Theme() {
	}
	
	public String getName() {
		return mud_name;
	}
	
	public void setName(String newName) {
		this.mud_name = newName;
	}
	
	public String getSeason() {
		return this.season;
	}
	
	public void setSeason(String newSeason) {
		this.season = newSeason;
	}
	
	public int getDay() {
		return this.day;
	}
	
	public void setDay(int newDay) {
		this.day = newDay;
	}
	
	public int getMonth() {
		return this.month;
	}
	
	public void setMonth(int newMonth) {
		this.month = newMonth;
	}
	
	public int getYear() {
		return this.year;
	}
	
	public void setYear(int newYear) {
		this.year = newYear;
	}
}