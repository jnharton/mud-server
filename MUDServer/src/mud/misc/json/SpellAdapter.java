package mud.misc.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	public Spell read(JsonReader reader) throws IOException {
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
				case "class":
					if( reader.peek() == JsonToken.BEGIN_ARRAY ) { reader.beginArray(); }
					List<SpellClass> spellClasses = spell.getSpellClasses();
					while( reader.peek() != JsonToken.END_ARRAY ) {
						spellClasses.add( SpellClass.valueOf( reader.nextString().toUpperCase() ) );
					}
					reader.endArray();
					break;
				case "level":
					spell.setLevel( reader.nextInt() );
					break;
				case "school":
					spell.setSchool( reader.nextString() );
					break;
				case "manacost":
					spell.setManaCost( reader.nextInt() );
					break;
				case "name":
					spell.setName( reader.nextString() );
					break;
				case "castmsg":
					spell.setCastMessage(reader.nextString());
					break;
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
					HashMap<String, Reagent> reagents = spell.getReagents();
					while( reader.peek() != JsonToken.END_ARRAY ) {
						String data = reader.nextString();
						try { reagents.put(data, new Reagent(data)); }
						catch (Exception e) { e.printStackTrace(); }
					}
					reader.endArray();
					break;
				default:
					break;
				}
			}
			catch(IllegalStateException ise) {
				System.out.println("Problem Token: " + name);
				break;
			}
		}
		while( reader.hasNext() );

		reader.endObject();

		return spell;
	}

	@Override
	public void write(JsonWriter writer, Spell spell) throws IOException {
		writer.beginObject();

		writer.name("class");
		
		writer.beginArray();
		
		for(mud.magic.SpellClass spellClass : spell.getSpellClasses()) {
			writer.value(spellClass.toString().toLowerCase());
		}
		
		writer.endArray();

		writer.name("level");
		writer.value(spell.getLevel());

		writer.name("school");
		writer.value(spell.getSchool().toString().toLowerCase());

		writer.name("manacost");
		writer.value(spell.getManaCost());

		writer.name("name");
		writer.value(spell.getName());
		
		writer.name("castmsg");
		writer.value(spell.getCastMessage());
		
		// Effects
		writer.name("effects");
		writer.beginArray();
		
		for(Effect effect : spell.getEffects()) {
			writer.value(effect.toString());
		}
		
		writer.endArray();
		
		// Reagents
		writer.name("reagents");
		
		writer.beginArray();
		
		for(Reagent reagent : spell.getReagents().values()) {
			writer.value(reagent.toString());
		}
		
		writer.endArray();
		
		writer.endObject();
	}
}