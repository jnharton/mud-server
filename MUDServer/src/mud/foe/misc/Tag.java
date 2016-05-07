package mud.foe.misc;

/**
 * Used to provide the "tagging feature of a pipbuck
 * 
 * @author Jeremy
 *
 */
public class Tag {
	private Integer id;
	private String name;
	
	public Tag(final String name, final Integer id) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}