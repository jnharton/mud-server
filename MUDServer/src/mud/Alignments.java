package mud;

public enum Alignments {
	NONE("None"),                       // 0
	LAWFUL_GOOD("Lawful Good"),         // 1
	NEUTRAL_GOOD("Neutral Good"),       // 2
	CHAOTIC_GOOD("Chaotic Good"),       // 3
	LAWFUL_NEUTRAL("Lawful Neutral"),   // 4
	NEUTRAL("Neutral"),                 // 5
	CHAOTIC_NEUTRAL("Chaotic Neutral"), // 6
	LAWFUL_EVIL("Lawful Evil"),         // 7
	NEUTRAL_EVIL("Neutral Evil"),       // 8
	CHAOTIC_EVIL("Chaotic Evil");       // 9
	
	private String stringRep;
	
	private Alignments(String stringRep) {
		this.stringRep = stringRep;
	}
	
	@Override
	public String toString() {
		return this.stringRep;
	}
}