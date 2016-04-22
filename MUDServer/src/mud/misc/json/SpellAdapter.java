package mud.misc.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mud.magic.Reagent;
import mud.magic.Spell;
import mud.magic.SpellClass;
import mud.misc.Effect;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class SpellAdapter extends TypeAdapter<Spell> {

	@Override
	public Spell read(final JsonReader reader) throws IOException {
		Spell spell = new Spell();

		String name = "";

		reader.beginObject();

		/*String sName = null;
		String sSchool = null;
		String sCastMsg = null;
		ArrayList<Effect> sEffects = new ArrayList<Effect>();*/

		do {
			if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				continue;
			}

			try {
				name = reader.nextName();
				
				System.out.println("Name: " + name);

				switch(name) {
				case "name":     spell.setName( reader.nextString() );      break;
				case "school":   spell.setSchool( reader.nextString() );    break;
				case "class":
					if( reader.peek() == JsonToken.BEGIN_ARRAY ) { reader.beginArray(); }
					
					final List<SpellClass> spellClasses = spell.getSpellClasses();
					
					while( reader.peek() != JsonToken.END_ARRAY ) {
						final String className = reader.nextString().toUpperCase();
						
						spellClasses.add( SpellClass.valueOf( className ) );
					}
					
					reader.endArray();
					
					break;
				case "level":    spell.setLevel( reader.nextInt() );        break;
				case "manacost": spell.setManaCost( reader.nextInt() );     break;
				case "castmsg":  spell.setCastMessage(reader.nextString()); break;
				case "effects":
					if( reader.peek() == JsonToken.BEGIN_ARRAY ) { reader.beginArray(); }
					
					List<Effect> effects = spell.getEffects();
					
					while( reader.peek() != JsonToken.END_ARRAY ) {
						effects.add( new Effect( reader.nextString() ) );
					}
					
					reader.endArray();
					
					break;
				case "reagents":
					if( reader.peek() == JsonToken.BEGIN_ARRAY ) { reader.beginArray(); }
					
					Map<String, Reagent> reagents = spell.getReagents();
					
					while( reader.peek() != JsonToken.END_ARRAY ) {
						String data = reader.nextString();
						
						try {
							// uses exception for error checkings
							reagents.put(data, new Reagent(data));
						}
						catch (final Exception e) {
							e.printStackTrace();
						}
					}
					
					reader.endArray();
					
					break;
				case "targets":
					String[] targets = new String[3];
					Integer index = 0;
					
					Arrays.fill(targets, "");
					
					if( reader.peek() == JsonToken.BEGIN_ARRAY ) { reader.beginArray(); }
					
					while( reader.peek() != JsonToken.END_ARRAY ) {
						final String data = reader.nextString();
						
						if( index < 3 ) {
							targets[index] = data;
							index++;
						}
					}
					
					reader.endArray();
					
					spell.target = Spell.encodeTargets(targets);
					
					break;
				default:
					break;
				}
			}
			catch(final IllegalStateException ise) {
				System.out.println("Problem Token: " + name);
				break;
			}
		}
		while( reader.hasNext() );

		reader.endObject();

		return spell;
	}

	@Override
	public void write(final JsonWriter writer, final Spell spell) throws IOException {
		writer.beginObject();
		
		//
		writer.name("class");
		
		writer.beginArray();
		
		for(mud.magic.SpellClass spellClass : spell.getSpellClasses()) {
			writer.value(spellClass.toString().toLowerCase());
		}
		
		writer.endArray();
		
		//
		writer.name("level");
		writer.value(spell.getLevel());
		
		//
		writer.name("school");
		writer.value(spell.getSchool().toString().toLowerCase());
		
		//
		writer.name("manacost");
		writer.value(spell.getManaCost());
		
		//
		writer.name("name");
		writer.value(spell.getName());
		
		//
		writer.name("castmsg");
		writer.value(spell.getCastMessage());
		
		// Effects
		writer.name("effects");
		writer.beginArray();
		
		for(final Effect effect : spell.getEffects()) {
			writer.value(effect.toString());
		}
		
		writer.endArray();
		
		// Reagents
		writer.name("reagents");
		
		writer.beginArray();
		
		for(final Reagent reagent : spell.getReagents().values()) {
			writer.value(reagent.toString());
		}
		
		writer.endArray();
		
		writer.endObject();
	}
}