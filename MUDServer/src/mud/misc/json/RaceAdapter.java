package mud.misc.json;

import java.io.IOException;

import mud.game.Race;
import mud.interfaces.Ruleset;
import mud.rulesets.d20.D20;
import mud.rulesets.foe.FOESpecial;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class RaceAdapter extends TypeAdapter<Race> {

	@Override
	public Race read(JsonReader reader) throws IOException {
		Race newRace = null;
		
		String rules = ""; // TODO fix kludge
		String name = "noname";
		Integer id = -1;
		Integer[] statAdj = new Integer[7];
		boolean playable = false;
		boolean restricted = false;
		boolean canFly = false;
		
		String key = "";

		reader.beginObject();

		do {
			if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				continue;
			}

			try {
				key = reader.nextName();

				System.out.println("Key: " + key);
				
				// i'm omitting subrace here.
				switch(key) {
				case "rules":
					rules = reader.nextString();
					break;
				case "name":
					name = reader.nextString();
					break;
				case "id":
					id = reader.nextInt();
					break;
				case "statAdj":
					if( reader.peek() == JsonToken.BEGIN_ARRAY ) { reader.beginArray(); }
					int index = 0;
					int temp1;
					while( reader.peek() != JsonToken.END_ARRAY ) {
						temp1 = reader.nextInt();
						System.out.println("int: " + temp1);
						statAdj[index] = temp1;
						index++;
					}
					reader.endArray();
					break;
				case "playable":
					playable = reader.nextBoolean();
					break;
				case "restricted":
					restricted = reader.nextBoolean();
					break;
				case "canFly":
					canFly = reader.nextBoolean();
					break;
				default: break;
				}
			}
			catch(IllegalStateException ise) {
				System.out.println("Problem Token: " + key);
				break;
			}
		}
		while( reader.hasNext() );

		reader.endObject();
		
		switch(rules) {
		case "foe.FOESpecial":
			return new Race(FOESpecial.getInstance(), name, id, statAdj, restricted, canFly);
		case "d20.D20":
			return new Race(D20.getInstance(), name, id, statAdj, restricted, canFly);
		default:
			return new Race(null, name, id, statAdj, restricted, canFly);
		}
	}

	@Override
	public void write(JsonWriter writer, Race race) throws IOException {
		writer.beginObject();
		
		writer.name("rules");
		writer.value(race.getRules().getName());
		
		writer.name("name");
		writer.value(race.getName());
		
		writer.name("id");
		writer.value(race.getId());
		
		writer.name("statAdj");
		
		writer.beginArray();
		
		for(Integer i : race.getStatAdjust()) writer.value(i);
		
		writer.endArray();
		
		writer.name("restricted");
		writer.value(race.isRestricted());
		
		writer.name("canFly");
		writer.value(race.canFly());
		
		writer.endObject();
	}
}