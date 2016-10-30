package mud.misc;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import mud.interfaces.Editor;
import mud.objects.Player;
import mud.quest.Quest;
import mud.utils.EditorData;
import mud.utils.Utils;

public class QuestEditor implements Editor {
	private Player player;
	private Quest quest;
	private EditorData data;
	
	public Queue<String> messages;
	
	public QuestEditor(final Player player, final Quest quest, final EditorData data) {
		this.player = player;
		this.quest = quest;
		this.data = data;
		
		this.messages = new LinkedList<String>();
	}
	
	@Override
	public void abort() {
		// TODO Auto-generated method stub
	}

	@Override
	public void done() {
		// indicate the chosen action
		this.messages.add("< Aborting Changes... >");

		// clear edit flag
		quest.Edit_Ok = true;

		// reset editor and player status
		player.setStatus( (String) data.getObject("pstatus") );
		player.setEditor(Editors.NONE);

		// clear editor data
		player.setEditorData(null);

		// exit
		this.messages.add("< Exiting... >");
	}

	@Override
	public void help() {
		synchronized( messages ) {
			this.messages.add("Quest Editor -- Help");
			this.messages.add(Utils.padRight("", '-', 40));
			this.messages.add("abort");
			this.messages.add("desc <new description>");
			this.messages.add("done");
			this.messages.add("help");
			this.messages.add("name <new name>");
			this.messages.add("save");
			this.messages.add("setloc");
			this.messages.add("show");
			this.messages.add("zones");
		}		
	}
	
	@Override
	public void save() {
	}
	
	public void save(final List<Quest> quests, final Quest quest) {
		quest.init(); // ensure that we have a valid quest id

		quest.setName( (String) data.getObject("name") );
		quest.setDescription( (String) data.getObject("desc") );

		// save tasks (add new ones, update existing ones, remove deleted ones)

		if( !quests.contains( quest) ) {
			quests.add( quest ); // add quest to global quest table
		}
		else {
			quests.set( quests.indexOf(quest), quest ); // replace existing quest with updated versio
		}
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void help(String arg) {
		// TODO Auto-generated method stub
		
	}
}