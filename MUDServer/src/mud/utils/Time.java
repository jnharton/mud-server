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
	
	public String hour() {
		if(this.hour < 10) {
			return "0" + String.valueOf(this.hour);
		}
		else {
			return String.valueOf(this.hour);
		}
	}
	
	public String minute() {
		if(this.minute < 10) {
			return "0" + String.valueOf(this.minute);
		}
		else {
			return String.valueOf(this.minute);
		}
	}
	
	public String second() {
		if(this.second < 10) {
			return "0" + String.valueOf(this.second);
		}
		else {
			return String.valueOf(this.second);
		}
	}
	
	@Override
	public String toString() {
		return hour() + ":" + minute() + ":" + second();
	}
}