package mud.quest;

import mud.colors.Colors;
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
	
	// TODO do we need task names?
	protected String name;
	protected String description;
	protected Room location = null;       // a specific location associated with the task, may be null

	protected Data objective = null;
	
	protected boolean isComplete = false; // is the task complete?
	
	/**
	 * Create a task with a description and task type.
	 * @param tType task type
	 * @param tDescription task description
	 */
	protected Task(final TaskType tType, final String tDescription, final Room location) {
		this.taskType = tType;
		this.description = tDescription;
		this.location = location;
	}

	/**
	 * Copy Constructor
	 * 
	 * @param template
	 */
	public Task(final Task template) {
		this.taskType = template.taskType;
		
		//this.name = template.name;
		this.description = template.description;
		this.location = template.location;
		
		this.objective = template.objective;
	}

	public int getId() {
		return this.id;
	}

	public void setId(final int newId) {
		this.id = newId;
	}
	
	public TaskType getType() {
		return this.taskType;
	}
	
	public boolean isType(final TaskType tType) {
		return this.taskType == tType;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String tDescription) {
		this.description = tDescription;
	}

	public Room getLocation() {
		return location;
	}
	
	public Data getObjective() {
		return this.objective;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}
	
	public abstract String getProgress();
	
	public abstract void update();
	
	public boolean update(final TaskUpdate update) {
		boolean taskChanged = applyUpdate(update);

		if ( taskChanged ) {
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
	private boolean applyUpdate(final TaskUpdate update) {
		return true;
	}
	
	protected abstract Task getCopy();

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