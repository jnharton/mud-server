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
	
	protected TaskType taskType;  // what kind of task is it? (see TaskType)
	
	protected String name;        // TODO do we need task names?
	protected String description; //
	protected Integer location;   // a specific location associated with the task

	protected Data objective = null;
	
	protected boolean isComplete = false; // is the task complete?
	
	/**
	 * Create a task with a description and task type.
	 * @param tType task type
	 * @param tDescription task description
	 */
	protected Task(final TaskType tType, final String tDescription, final Integer location) {
		this.taskType = tType;
		this.description = tDescription;
		this.location = location;
	}
	
//	public abstract Task(final String tDescription, final Room location, final Data objectiveData);
	
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

	public Integer getLocation() {
		return location;
	}
	
	public Data getObjective() {
		return this.objective;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}
	
	public abstract String getProgress();

	
	public boolean applyUpdate(final TaskUpdate update) {
		boolean taskChanged = update(update);

		if ( taskChanged ) return true;
		else               return false;
	}

	/**
	 * Evaluates the update, changing the status of this Task
	 * to match the new data.
	 * 
	 * NOTE: we can assume that the provided task update will be for this task
	 * 
	 * @param update
	 * @return TODO
	 */
	protected abstract boolean update(final TaskUpdate update);
	
	protected abstract Task getCopy();

	@Override
	public String toString() {
		return this.description + "(" + this.taskType + ")";
	}

	public String toDisplay() {
		final StringBuilder buf = new StringBuilder();
		buf.append(isComplete() ? Colors.GREEN : Colors.CYAN).append("      o ").append(getDescription());
		buf.append(Colors.MAGENTA).append(" ( ").append("%LOCATION%").append(" ) ").append(Colors.CYAN);
		buf.append("[+]\n");
		return buf.toString();
	}
}