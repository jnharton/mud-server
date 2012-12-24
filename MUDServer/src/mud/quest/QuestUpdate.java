package mud.quest;

import java.util.ArrayList;

/**
 * A QuestUpdate inherently contains updates to a task, and is potentially
 * very similar to a Quest object. It should be "applied" to a Quest to "update"
 * the current status of that Quest.
 * 
 * By definition, any QuestUpdate contains within itself zero or more TaskUpdate(s)
 * which will be applied to some Task within the Quest after identifying the task
 * to which they ought to be applied (the TaskUpdate is either OWNED by the Task,
 * or at least is identified with it by the TaskUpdate holding an id number for
 * the Task it should update).
 * 
 * @author Jeremy
 *
 */
public class QuestUpdate extends Update {
	int questId;
	
	ArrayList<TaskUpdate> taskUpdates;
}