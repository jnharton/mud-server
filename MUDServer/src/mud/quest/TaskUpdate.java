package mud.quest;

/**
 * A TaskUpdate inherently contains updates to a task, and is potentially
 * very similar to a task object. It should be "applied" to a task to "update"
 * the current status of the task.
 * 
 * @author Jeremy
 *
 */
public class TaskUpdate extends Update {
	public int taskId;
	
	public TaskUpdate(int taskId) {
		this.taskId = taskId;
	}
}