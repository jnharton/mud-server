package mud.quest;

import mud.utils.Data;

/**
 * A TaskUpdate inherently contains updates to a task, and is potentially
 * very similar to a task object. It should be "applied" to a task to "update"
 * the current status of the task.
 * 
 * @author Jeremy
 *
 */
public class TaskUpdate extends Update {
	private int taskId;
	private Data objectiveData;
	
	public TaskUpdate(int taskId, final Data objectiveData) {
		this.taskId = taskId;
	}
	
	public int getTaskId() {
		return this.taskId;
	}
	
	public Data getData() {
		return this.objectiveData;
	}
}