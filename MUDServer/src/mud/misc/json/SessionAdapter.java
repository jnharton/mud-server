package mud.misc.json;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import mud.misc.Session;

public class SessionAdapter extends TypeAdapter<Session> {

	@Override
	public Session read(JsonReader reader) throws IOException {
		reader.beginObject();
		reader.endObject();
		
		return null;
	}

	@Override
	public void write(JsonWriter writer, Session value) throws IOException {
		writer.beginObject();
		writer.endObject();
	}

}