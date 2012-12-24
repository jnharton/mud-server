package mud;

public class Effect
{
	public static enum Type { ACID, BURN, HEAL, INVIS, POISON };
	public static enum DurationType { INSTANTANEOUS, PERMANENT, ROUNDS, SPECIAL };
	
	private int id;                // int
	private static int lastId = 0; //
	private String name;           // name of effect (ex. 'invisibility', 'acid')
	private Type type;             // damage, visibility, resistance, etc
	private String durationType;   // temporary vs. permanent, etc
	private int duration;          // effects duration in seconds

	// explicit super constructor for subclassing
	protected Effect() {
	}

	public Effect(String eName) {
		id = nextID();
		name = eName;
		
		if(name.toLowerCase().contains("heal") == true) {
			type = Type.HEAL;
		}
		else if(name.toLowerCase().contains("invis") == true) {
			type = Type.INVIS;
		}
	}

	public Effect(String eName, Type eType, String eDurationType, int eDuration)
	{
		id = nextID();
		name = eName;
		type = eType;
		durationType = eDurationType;
		duration = eDuration;
		
		if(name.toLowerCase().contains("heal") == true) {
			type = Type.HEAL;
		}
		else if(name.toLowerCase().contains("invis") == true) {
			type = Type.INVIS;
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String eName) {
		this.name = eName;
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getDurationType() {
		return durationType;
	}

	public void setDurationType(String durationType) {
		this.durationType = durationType;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public static int nextID() {
		return lastId++;
	}

	public String toString() {
		return this.name;
	}
}