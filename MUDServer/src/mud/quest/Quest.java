package mud.quest;

import java.util.ArrayList;
import java.util.Arrays;

import mud.objects.Player;
import mud.utils.Date;

/**
 * Quest Class
 * 
 * An object to represent quests given to players to complete.
 * 
 * @see "Last Work: 6/4/2012"
 * 
 * @author Jeremy
 *
 */
public class Quest {
	private static int lastId = 0;      // the last quest id issued
	
	private int id;                     // quest id
	private String name;                // quest name
	private String description;         // quest description (does it need a short and long version or just this?)
	private Date issueDate;             // the in-game date the quest was given
	private boolean isComplete = false; // is the quest completed? (this should put it in a deletion queue if we delete completed quests)
	
	public String location = "Quest Loc"; // quest location

	private ArrayList<Task> tasks;      // a list of tasks that must be completed to finish the quest

	public Quest( String qName, String qDescription, Task...tasks ) {
		this.id = lastId++;
		this.name = qName;
		this.description = qDescription;
		this.tasks = new ArrayList<Task>();
		
		for (Task task : tasks) { this.tasks.add(task); }
	}
	
	/**
	 * Quest - Copy Constructor
	 * 
	 * Create a new quest object, using an existing one as
	 * a template.
	 * 
	 * @param template
	 */
	public Quest( Quest template ) {
		this.id = lastId++;
		this.name = template.name;
		this.description = template.description;
		this.tasks = new ArrayList<Task>();
		
		for (Task task : template.getTasks()) { this.tasks.add(task); }
	}
	
	public void addTask(Task newTask) {
		this.tasks.add(newTask);
	}
	
	public void removeTask(Task toRemove) {
		this.tasks.remove(toRemove);
	}
	
	/**
	 * Get the quest id
	 * 
	 * @return
	 */
	public int getId() {
		return this.id;
	}

	public String getName() { 
		return this.name;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}

	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String newDescription) {
		this.description = newDescription;
	}
	
	public ArrayList<Task> getTasks() {
		return this.tasks;
	}
	
	private void update() {
		for (Task task : this.tasks) {
			if ( !task.isComplete() ) {
				return;
			}
		}
		
		this.isComplete = true;
	}
	
	/**
	 * Update the quest
	 * 
	 * @param qu the QuestUpdate to apply
	 * @return true - quest updated, false - quest not updated
	 */
	public boolean update(QuestUpdate qu) {

		boolean questChanged = applyUpdate(qu);

		if (questChanged) {
			update();
			return true;
		}
		else {
			return false;
		}
	}

	
	/**
	 * Applies the update, changing the status of this Quest
	 * to match the new data.
	 * 
	 * NOTE: It is assumed herein that each QuestUpdate will only
	 * contain one TaskUpdate per Task in the Quest
	 * 
	 * @param update
	 * @return boolean indicates whether or not we changed the Quest
	 */
	private boolean applyUpdate(QuestUpdate update) {
		int toRemove = -1;
		boolean questChanged = false;
		
		for (Task task : this.tasks) {
			for (TaskUpdate tu : update.taskUpdates) {
				if ( tu.taskId == task.getId() ) {
					if ( task.update(tu) ) { questChanged = true; } // update task state
					toRemove = update.taskUpdates.indexOf(tu);     // get index of update for direct removal
					break;
				}
			}
			
			update.taskUpdates.remove(toRemove); // toss the used update
		}
		
		return questChanged;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}

	public String toString() {
		return this.name + "\n" + this.description;
	}

	public boolean isSuitable(Player player) {
		return true;
	}
}