package mud.quest;

/**
 * Task Class
 * 
 * Defines a task object that is a sub-unit of a quest and has a description, a type, and completion status.
 * 
 * @author Jeremy
 *
 */
public class Task {
	private int id;
	
	private String description;
	private TaskType taskType;
	private boolean isComplete = false; // is the task complete? (this being true should be reflected as marked "complete"/"finished"/etc
	
	public String location = "Task Loc";
	
	public Integer toKill = 0;
	public Integer kills = 0;
	
	public Integer toCollect = 0;
	public Integer collects = 0;
	
	public boolean hasItem = false;
	
	/**
	 * Create a task with a description and the NONE task type.
	 * 
	 * @param tDescription task description
	 */
	public Task(String tDescription) {
		this.description = tDescription;
		this.taskType = TaskType.NONE;
	}
	
	/**
	 * Create a task with a description and task type.
	 * 
	 * @param tDescription task description
	 * @param tType task type
	 */
	public Task(String tDescription, TaskType tType) {
		this.description = tDescription;
		this.taskType = tType;
	}
	
	/**
	 * Create a task with a description and task type, and
	 * some objective data related to the type. 
	 * 
	 * @param tDescription task description
	 * @param tType task type
	 * @param other data about the objectives for the task type
	 */
	public Task(String tDescription, TaskType tType, Object other) {
		this.description = tDescription;
		this.taskType = tType;
		if (this.taskType == TaskType.KILL) {
			if (other instanceof Integer) {
				Integer k = (Integer) other;
				
				this.toKill = k;
				this.kills = 0;
			}
		}
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

	public void setType(TaskType tType) {
		this.taskType = tType;
	}
	
	private void update() {
		// if condition is met, then isComplete = true
		switch(taskType) {
		case NONE:
			break;
		case COLLECT:
			if (this.collects == this.toCollect) {
				this.isComplete = true;
			}
			else { this.isComplete = false; }
			break;
		case KILL:
			if (this.kills == this.toKill) {
				this.isComplete = true;
			}
			else { this.isComplete = false; }
			break;
		case RETRIEVE:
			if ( this.hasItem ) {
				this.isComplete = true;
			}
			else { this.isComplete = false; }
			break;
		default:
		}
	}
	
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
		return false;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}

	public String toString() {
		return this.description + "(" + this.taskType + ")";
	}
}