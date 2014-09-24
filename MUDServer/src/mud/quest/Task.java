package mud.quest;

import mud.Colors;
import mud.objects.Creature;
import mud.objects.Room;

/**
 * Task Class
 * 
 * Defines a task object that is a sub-unit of a quest and has a description, a type, and completion status.
 * 
 * @author Jeremy
 *
 */
public class Task {
	private int id = 0;
	
	private String description;
	private TaskType taskType;
	private boolean isComplete = false; // is the task complete? (this being true should be reflected as marked "complete"/"finished"/etc
	
	public Room location = null;
	
	public Integer toKill = 0;
	public Integer kills = 0;
	
	public Integer toCollect = 0;
	public Integer collects = 0;
	
	public Creature objective = null;
	public boolean hasItem = false;
	
	/**
	 * Create a task with a description and the NONE task type.
	 * 
	 * @param tDescription task description
	 */
	public Task(String tDescription) {
		this(tDescription, TaskType.NONE, null);
	}
	
	/**
	 * Create a task with a description and task type.
	 * 
	 * @param tDescription task description
	 * @param tType task type
	 */
	public Task(String tDescription, TaskType tType, Room location) {
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
	public Task(String tDescription, TaskType tType, Room location, Object...other) {
		this.description = tDescription;
		this.taskType = tType;
		this.location = location;
		if (this.taskType == TaskType.KILL) {
			if( other.length == 2 ) {
				if (other[0] instanceof Integer) {
					final Integer k = (Integer) other[0];
					
					this.toKill = k;
					this.kills = 0;
				}
				if(other[1] instanceof Creature) {
					final Creature c = (Creature) other[1];
					
					this.objective = c;
				}
			}
		}
	}
	
	/**
	 * Copy Constructor
	 * 
	 * @param template
	 */
	public Task(Task template) {
		this.description = template.description;
		this.taskType = template.taskType;
		this.location = template.location;
		
		if (template.taskType == TaskType.KILL) {
			this.toKill = template.toKill;
			this.kills = 0;
			
			if(template.objective != null) {
				this.objective = template.objective;
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
	
	public boolean isType(TaskType tType) {
		return this.taskType == tType;
	}
	
	public Room getLocation() {
		return location;
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
		return true;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}

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
