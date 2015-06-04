package mud.quest;

/**
 * Task Types
 * 
 * NONE     - No Specific Type
 * RETRIEVE - Obtain a specific object from somewhere else
 * COLLECT  - Collect a certain number of the specified object and return them to the quest giver
 * KILL     - Kill something/someone (number of creatures, an individual, ?)
 * 
 * @author Jeremy
 *
 */
public enum TaskType {
	NONE, RETRIEVE, COLLECT, KILL
}