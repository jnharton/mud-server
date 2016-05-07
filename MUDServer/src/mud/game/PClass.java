package mud.game;

public class PClass {
	private String name;
	private String abrv;
	private int id;
	private String hit_dice;
	private boolean npc;
	private boolean caster;
	private String color;
	
	/**
	 * Default PClass constructor, mostly intended for loading user specified, "soft-coded" classes
	 * from files. That is, information for the class will be loaded from a file.
	 */
	public PClass() {
		this.name = "unnamed";
		this.abrv = "noabrv";
		this.id = -1;
		this.hit_dice = "0d0";
		this.npc = false;
		this.caster = false;
		this.color = "";
	}
	
	/**
	 * The constructor for a enum class that represent D&D-like Player Classes
	 * 
	 * @param name the name of the class
	 * @param id the unique numerical id of the class (essentially meaningless)
	 * @param hit_dice the hit dice (D&D multi-sided dice, in multiples of 2)
	 * @param npc a boolean value indicating whether the class is an NPC class
	 * @param caster a boolean value indicating whether the class is a caster (spell-casting) class
	 * @param color the name of an ansi color used in some certain circumstances when printing the class abbreviation
	 */
	public PClass(String name, String abrv, int id, String hit_dice, boolean npc, boolean caster, String color) {
		this.name = name;
		this.abrv = abrv;
		this.id = id;
		this.hit_dice = hit_dice;
		this.npc = npc;
		this.caster = caster;
		this.color = color;
	}
	
	/**
	 * 
	 * @return String the name of the class
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * 
	 * @return String hit dice string for the class
	 */
	public String getHDString() { // HD = hit dice
		return this.hit_dice;
	}
	
	/**
	 * 
	 * @return Integer hit dice integer for the class (the number of sides the class' hit dice has)
	 */
	public Integer getHD() {
		return Integer.parseInt(this.hit_dice.substring(1,-1));
	}
	
	public String getAbrv() {
		return this.abrv;
	}
	
	/**
	 * 
	 * @return int the 'id' of the class, which is just a meaningless number representing it
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * 
	 * @return boolean true/false value indicates whether a class is an NPC class or not.
	 */
	public boolean isNPC() {
		return this.npc;
	}
	
	/**
	 * 
	 * @return boolean true/false value indicates whether a class is a caster (spell-casting) class or not.
	 */
	public boolean isCaster() {
		return this.caster;
	}
	
	public String getColor() {
		return this.color;
	}
	
	public String toString() {
		return this.name;
	}
}