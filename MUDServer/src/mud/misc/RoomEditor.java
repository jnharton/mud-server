package mud.misc;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import mud.MUDObject;
import mud.ObjectFlag;
import mud.TypeFlag;
import mud.interfaces.Editor;
import mud.net.Client;
import mud.objects.Exit;
import mud.objects.Item;
import mud.objects.Player;
import mud.objects.Room;
import mud.objects.Thing;
import mud.utils.EditorData;
import mud.utils.MudUtils;
import mud.utils.Utils;

public class RoomEditor implements Editor {
	final EditorData data;
	final Client client;
	
	public RoomEditor(final EditorData editData, final Client client) {
		this.data = editData;
		this.client = client;
	}
	
	// TODO where to get client data from?
	
	
	public void exec(final String cmd, final String args) {
		String rcmd = "";
		String rarg = "";

		//EditorData data = player.getEditorData();

		if (rcmd.equals("abort")) {}
		else if ( rcmd.equals("addexit") ) {}
		else if (rcmd.equals("addthing")) {}
		else if (rcmd.equals("additem")) {}
		else if (rcmd.equals("desc")) {}
		else if (rcmd.equals("dim")) {}
		else if (rcmd.equals("dirset")) {}
		else if (rcmd.equals("done")) {}
		else if (rcmd.equals("flags")) {}
		else if (rcmd.equals("help")) {}
		else if (rcmd.equals("items")) {}
		else if (rcmd.equals("layout")) {}
		else if( rcmd.equals("modify") ) {}
		else if (rcmd.equals("name")) {}
		else if (rcmd.equals("rooms")) {}
		else if (rcmd.equals("save")) {}
		else if (rcmd.equals("setflag")) {}
		else if (rcmd.equals("setlocation")) {}
		else if (rcmd.equals("setzone")) {}
		else if (rcmd.equals("show")) {}
		else if (rcmd.equals("trigger")) {}
		else if (rcmd.equals("zones")) {}
		else { send("No such command."); }
	}
	
	public void save() {
	}
	
	public void done() {
	}
	
	public void abort() {
		/*
		// clear edit flag on Room
		((Room) data.getObject("room")).Edit_Ok = true;
		
		// exit
		client.writeln("< Exiting... >");
		
		// reset editor and player status
		player.setStatus((String) data.getObject("pstatus"));
		player.setEditor(Editors.NONE);
		*/
	}
	
	@Override
	public void help() {
		// output help information
		final List<String> output = (List<String>) Utils.mkList(
				"Room Editor -- Help",
				Utils.padRight("", '-', 74),
				"abort                           abort the editor (no changes will be kept)",
				"addexit <name> <destination>    creates a new exit",
				"additem <prototype key>         creates a new instance of the indicated",
				"                                prototype",
				"addthing <name/dbref>           move an existing object to the room (not",
				"                                sure how it should work)",
				"desc <param> <new description>  change/set the room description",
				"dim <dimension> <size>          change a dimension of the room (x/y/z)",
				"dirset <direction> <exit dbref> associate an exit with a cardinal direction",
				"done                            finish editing (save & exit)",
				"help                            shows this help information",
				"items                           list available item prototypes",
				"npcs",
				"layout                          display a 2D layout visualization",
				"modify                          change layout",
				"name <new name>                 change/set the room name",
				"rooms                           list the other rooms that are in the same",
				"                                zone as this one",
				"save                            save changes to the room",
				"setlocation                     change the location (deprecated?)",
				"setzone                         set the zone that this room belongs to",
				"show                            show basic information about the room",
				"trigger <type> <data>           setup a trigger of the specified type with",
				"                                the specified data",
				"zones                           list the zones that exist",
				Utils.padRight("", '-', 74)
				);

		this.client.write(output);
	}
	
	@Override
	public void help(final String arg) {
		// output help information specific to the command name given
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}
	
	private void send(final String s) {
		this.client.write(s);
	}
}