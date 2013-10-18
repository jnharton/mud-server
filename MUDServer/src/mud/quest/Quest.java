package mud.quest;

import java.util.ArrayList;
import java.util.Arrays;

import mud.misc.Zone;
import mud.objects.Player;
import mud.utils.Date;

import mud.Colors;

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
	private Date expireDate;            // the in-game date that the quests expires (for time-limited things?)
	private boolean isComplete = false; // is the quest completed? (this should put it in a deletion queue if we delete completed quests)
	
	private Zone location;
	
	public boolean Edit_Ok = true;

	final private ArrayList<Task> tasks;      // a list of tasks that must be completed to finish the quest
	
	public Quest() {
		this.tasks = new ArrayList<Task>();
	}

	public Quest( String qName, String qDescription, Zone qLocation, Task...tasks ) {
		this.id = lastId++;
		this.name = qName;
		this.description = qDescription;
		this.location = qLocation;
		this.tasks = new ArrayList<Task>(Arrays.asList(tasks));
	}
	
	/**
	 * Quest - Copy Constructor
	 * 
	 * Create a new quest object, using an existing one as
	 * a template.
	 * 
	 * NOTE: duplicated/copied quests have the same id as the source
	 * 
	 * @param template
	 */
	public Quest( Quest template ) {
		this.id = template.id;
		this.name = template.name;
		this.description = template.description;
		this.location = template.location;
		this.tasks = new ArrayList<Task>(template.getTasks());
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
		boolean questChanged = false;
		
		for (final Task task : this.tasks) {
			for (final TaskUpdate tu : new ArrayList<TaskUpdate>(update.taskUpdates)) {
				if ( tu.taskId == task.getId() ) {
					if ( task.update(tu) ) {
                        questChanged = true;
                    }
					update.taskUpdates.remove(tu);
					break;
				}
			}
		}
		
		return questChanged;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}
	
	public Zone getLocation() {
		return this.location;
	}
	
	public void setExpires(Date expirationDate) {
		this.expireDate = expirationDate;
	}

    @Override
	public String toString() {
		return this.name + "\n" + this.description;
	}

	public String toDisplay(boolean useColor) {
        final StringBuilder buf = new StringBuilder();
        if( useColor ) {
        	buf.append(Colors.YELLOW).append("   o ").append(getName());
        	buf.append(Colors.MAGENTA).append(" ( ").append(location.getName()).append(" ) ").append(Colors.CYAN);
        }
        else {
        	buf.append("   o ").append(getName());
        	buf.append(" ( ").append(location.getName()).append(" ) ");
        }
        
        buf.append("\n");
        
        for (final Task task : getTasks()) {
            buf.append(task.toDisplay());
        }
        return buf.toString();
	}
	
	/**
	 * Perform a series of check to determine the suitability
	 * of this quest for the specified player.
	 * 
	 * - level range?
	 * - alignment?
	 * - class?
	 * - party? party size?
	 * 
	 * @param player
	 * @return
	 */
	public boolean isSuitable(final Player player) {
		return true;
	}
}