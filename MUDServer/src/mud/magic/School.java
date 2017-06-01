package mud.magic;

import java.util.ArrayList;
import java.util.List;

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
	
	static List<School> schools = new ArrayList<School>();
	
	School(String name) {
		this.name = name; 
	}
	
	public String toString() {
		return this.name;
	}
}