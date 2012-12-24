package mud;

/**
 * Enumeration of Seasons with the months
 * that they start and end
 * 
 * @author Jeremy
 *
 */
public enum Seasons {
	SPRING("Spring", 3, 6),
	SUMMER("Summer", 6, 9),
	AUTUMN("Autumn", 9, 12),
	WINTER("Winter", 12, 3);

	private String name;
	public int beginMonth;
	public int endMonth;

	private Seasons(String name, int beginMonth, int endMonth) {
		this.name = name;
		this.beginMonth = beginMonth;
		this.endMonth = endMonth;
	}

	public String getName() {
		return this.name;
	}

	public String toString() {
		return this.name;
	}
}