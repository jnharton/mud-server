package mud.magic;

public enum School {
	ABJURATION("Abjuration"),
	CONJURATION("Conjuration"),
	DIVINATION("Divination"),
	ENCHANTMENT("Enchantment"),
	EVOCATION("Evocation"),
	ILLUSION("Illusion"),
	INVOCATION("Invocation"),
	TRANSMUTATION("Transmutation"),
	NECROMANCY("Necromancy"),
	OTHER("Other");
	
	final private String name;
	
	School(String name) {
		this.name = name;
	}
	
	public String toString() {
		return this.name;
	}
}