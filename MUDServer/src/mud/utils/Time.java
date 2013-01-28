package mud.utils;

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
		if (this.hour < 10) {
			return "0" + String.valueOf(this.hour);
		}
		else {
			return String.valueOf(this.hour);
		}
	}
	
	public String minute() {
		if (this.minute < 10) {
			return "0" + String.valueOf(this.minute);
		}
		else {
			return String.valueOf(this.minute);
		}
	}
	
	public String second() {
		if (this.second < 10) {
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