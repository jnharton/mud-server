package mud.quest;

import mud.MUDObject;
import mud.TypeFlag;
import mud.misc.Colors;
import mud.objects.Creature;
import mud.objects.Room;
import mud.utils.Data;

/**
 * Task Class
 * 
 * Defines a task object that is a sub-unit of a quest and has a description, a type, and completion status.
 * 
 * @author Jeremy
 *
 */
public abstract class Task {
	private int id = 0;
	
	protected TaskType taskType;          // what kind of task is it? (see TaskType)
	
	protected String name;
	protected String description;
	protected Room location = null;        // a specific location associated with the task, may be null
	
	protected boolean isComplete = false; // is the task complete?

	public Data objective = null;
	
	/**
	 * Create a task with a description and task type.
	 * 
	 * @param tDescription task description
	 * @param tType task type
	 */
	protected Task(String tDescription, TaskType tType, Room location) {
		this.description = tDescription;
		this.taskType = tType;
		this.location = location;
	}

	/**
	 * Create a task with a description and task type, and
	 * some objective data related to the type. 
	 * 
	 * @param tDescription task description
	 * @param tType task type
	 * @param other data about the objectives for the task type
	 */
	/*public Task(String tDescription, TaskType tType, Room location, Data objectiveData) {
		this.description = tDescription;
		this.taskType = tType;
		this.location = location;
		
		this.objective = objectiveData;
	}*/

	/**
	 * Copy Constructor
	 * 
	 * @param template
	 */
	public Task(Task template) {
		this.description = template.description;
		this.taskType = template.taskType;
		this.location = template.location;

		/*if (template.taskType == TaskType.KILL) {
			this.toKill = template.toKill;
			this.kills = 0;

			if(template.objective != null) {
				this.objective = new Data( template.objective );
			}
		}*/
	}

	public int getId() {
		return this.id;
	}

	public void setId(int newId) {
		this.id = newId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String tDescription) {
		this.description = tDescription;
	}

	public TaskType getType() {
		return this.taskType;
	}

	public boolean isType(TaskType tType) {
		return this.taskType == tType;
	}

	public Room getLocation() {
		return location;
	}
	
	public abstract String getProgress();
	
	public abstract void update();
	
	public boolean update(TaskUpdate update) {
		boolean taskChanged = applyUpdate(update);

		if (taskChanged) {
			update();
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Evaluates the update, changing the status of this Task
	 * to match the new data.
	 * 
	 * @param update
	 * @return boolean indicates whether or not we changed the Task
	 */
	private boolean applyUpdate(TaskUpdate update) {
		return true;
	}

	public boolean isComplete() {
		return this.isComplete;
	}
	
	@Override
	protected abstract Task clone();

	@Override
	public String toString() {
		return this.description + "(" + this.taskType + ")";
	}

	public String toDisplay() {
		final StringBuilder buf = new StringBuilder();
		buf.append(isComplete() ? Colors.GREEN : Colors.CYAN).append("      o ").append(getDescription());
		buf.append(Colors.MAGENTA).append(" ( ").append(location.getName()).append(" ) ").append(Colors.CYAN);
		buf.append("[+]\n");
		return buf.toString();
	}
}