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

public class Time {
	public int hour;
	public int minute;
	public int second;
	
	public Time(int nHour, int nMinute, int nSecond) {
		this.hour = nHour;
		this.minute = nMinute;
		this.second = nSecond;
	}
	
	/**
	 * Create a new time object from a give timestring
	 * in the format described below:
	 * 	hh:mm:ss
	 * @return
	 */
	public static Time fromString(String timeString) {
		//String[] data = timeString.split(":");
		int[] timeData = Utils.stringsToInts(timeString.split(":"));
		
		if( timeData.length >= 3 ) {
			//int hour = Utils.toInt(data[0], -1);
			//int minute = Utils.toInt(data[1], -1);
			//int second = Utils.toInt(data[2], -1);
			
			return new Time(timeData[0], timeData[1], timeData[2]);
			//return new Time(hour, minute, second);
		}

		return null;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		
		if (this.hour < 10) sb.append("0" + String.valueOf(this.hour));
		else                sb.append(String.valueOf(this.hour));
		
		sb.append(":");
		
		if (this.minute < 10) sb.append("0" + String.valueOf(this.minute));
		else                  sb.append(String.valueOf(this.minute));
		
		sb.append(":");
		
		if (this.second < 10) sb.append("0" + String.valueOf(this.second));
		else                  sb.append(String.valueOf(this.second));
		
		//return hour() + ":" + minute() + ":" + second();
		return sb.toString();
	}
}
