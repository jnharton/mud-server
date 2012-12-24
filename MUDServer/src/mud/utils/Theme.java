package mud.utils;

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