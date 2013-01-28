package mud;

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
		
		if (name.toLowerCase().contains("heal")) {
			type = Type.HEAL;
		}
		else if (name.toLowerCase().contains("invis")) {
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
		
		if (name.toLowerCase().contains("heal")) {
			type = Type.HEAL;
		}
		else if (name.toLowerCase().contains("invis")) {
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