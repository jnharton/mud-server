package mud.misc.json;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import mud.objects.Player;
import mud.utils.Account;
import mud.utils.Account.Status;
import mud.utils.Date;
import mud.utils.Utils;

public class AccountAdapter extends TypeAdapter<Account> {

	@Override
	public Account read(final JsonReader reader) throws IOException {
		int id = -1;

		Date created = null;
		Date modified = null;
		Date archived = null;

		Status status = Status.ACTIVE;

		String username = "";
		String password = ""; // should come in as a hash

		int charLimit = 5;

		String lastIPAddress = "127.0.0.1";

		//
		String name = "";

		reader.beginObject();

		do {
			if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				continue;
			}

			try {
				name = reader.nextName();

				System.out.println("Name: " + name);

				switch(name) {
				case "id":         id = reader.nextInt();                            break;
				case "created":    created = Date.parseDate( reader.nextString() );  break;
				case "modified":   modified = Date.parseDate( reader.nextString() ); break;
				case "archived":   archived = Date.parseDate( reader.nextString() ); break;
				case "status":     status = Status.valueOf( reader.nextString() );   break;
				case "username":   username = reader.nextString();                   break;
				case "password":   password = reader.nextString();                   break;
				case "charlimit":  charLimit = reader.nextInt();                     break;
				case "lastipaddr": lastIPAddress = reader.nextString();              break;
				default: break;
				}
			}
			catch(final IllegalStateException ise) {
				System.out.println("Problem Token: " + name);
				break;
			}
		}
		while( reader.hasNext() );

		reader.endObject();

		return new Account(id, status, created, modified, username, password, charLimit, new Player[0]);
	}

	@Override
	public void write(final JsonWriter writer, final Account account) throws IOException {
		writer.beginObject();

		writer.name("id");
		writer.value( account.getId() );

		writer.name("created");
		writer.value( account.getCreated().toString() );

		writer.name("modfied");
		writer.value( account.getModified().toString() );
		
		// TODO figure this out and fix it
		writer.name("archived");
		//writer.value( account.getArchived().toString() );
		//writer.value( account.getArchived() != null ? account.getArchived().toString() : "00-00-0000" );
		writer.value("00-00-0000");

		writer.name("status");
		writer.value( account.getStatus().toString() );

		writer.name("username");
		writer.value( account.getUsername() );

		writer.name("password");
		writer.value( account.getPassword() );

		writer.name("charlimit");
		writer.value( account.getCharLimit() );

		writer.name("lastipaddr");
		writer.value( account.getLastIPAddress() );

		writer.endObject();
	}

}