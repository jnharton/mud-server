package mud.quest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mud.colors.Colors;
import mud.misc.Zone;
import mud.objects.Player;
import mud.utils.Date;

/**
 * Quest Class
 * 
 * An object to represent quests given to players to complete.
 * 
 * @see "Last Work: 11/4/2013"
 * 
 * @author Jeremy
 *
 */
public final class Quest {
	private static int lastId = 0;       // the last quest id issued
	
	private int id;                      // quest id (int for easier comparison)
	
	private String name;                 // quest name
	private String description;          // quest description (does it need a short and long version or just this?)
	private Zone location;               // the quest region (for instance if you must kill the kobolds in region X for the reward)
	
	private Date issueDate = null;       // the in-game date the quest was given
	private Date expireDate = null;      // the in-game date that the quests expires (for time-limited things?)
	
	private List<Task> tasks;            // a list of tasks that must be completed to finish the quest
	
	private Reward reward;
	
	private boolean isComplete = false;  // is the quest completed? (this should put it in a deletion queue if we delete completed quests)
	private boolean isIgnored = false;   // is the quest being ignored? (i.e. it shouldn't show up in the main quest list for the player)
	private boolean isCopy = false;      // is this quest the original object or a copy
	private boolean isRepeatable = true; // can you repeat the quest
	
	public boolean Edit_Ok = true;       // can the quest be edited safely (no one else is editing it)
	
	/**
	 * Construct a "blank quest" (for editing purposes)
	 */
	public Quest(final String qName, final String qDescription) {
		this.id = -1;
		
		this.name = qName;
		this.description = qDescription;
		this.location = null;
		
		this.tasks = new ArrayList<Task>();
	}
	
	/**
	 * Construct a quest with no tasks.
	 * 
	 * @param qName
	 * @param qDescription
	 * @param qLocation
	 */
	public Quest(final String qName, final String qDescription, final Zone qLocation) {
		this.id = lastId++;
		
		this.name = qName;
		this.description = qDescription;
		this.location = qLocation;
		
		this.tasks = new ArrayList<Task>();
	}
	
	/**
	 * Construct a task with the specified tasks
	 * 
	 * @param qName
	 * @param qDescription
	 * @param qLocation
	 * @param tasks
	 */
	public Quest(final String qName, final String qDescription, final Zone qLocation, final Task...tasks) {
		this(qName, qDescription, qLocation);
		
		this.tasks.addAll(Arrays.asList(tasks));
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
	private Quest(final Quest template) {
		this.id = template.id;
		
		this.name = template.name;
		this.description = template.description;
		this.location = template.location;
		
		this.tasks = new ArrayList<Task>();
		
		for(final Task task : template.getTasks()) {
			this.tasks.add( task.getCopy() );
		}
		
		if( !(template.reward == null) ) {
			//this.reward = new Reward( template.reward.getCoins(), ((Item[]) template.reward.getItems().toArray()));
			this.reward = template.reward;
		}
		else System.out.println("Quest: reward is NULL.");
		
		if( !this.isCopy ) this.isCopy = true;
	}
	
	public void addTask(final Task newTask) {
		if( !isCopy ) this.tasks.add(newTask);
	}
	
	public void removeTask(final Task toRemove) {
		if( !isCopy ) this.tasks.remove(toRemove);
	}
	
	/**
	 * Get the quest id
	 * 
	 * @return
	 */
	public int getId() {
		return this.id;
	}
	
	public void setName(final String newName) {
		if( !isCopy ) this.name = newName;
	}

	public String getName() { 
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(final String newDescription) {
		if( !isCopy ) this.description = newDescription;
	}
	
	public Zone getLocation() {
		return this.location;
	}
	
	public void setLocation(final Zone newLocation) {
		if( !isCopy ) this.location = newLocation;
	}
	
	public Date getIssued() {
		return this.issueDate;
	}
	
	public void setIssued(final Date issuedDate) {
		this.issueDate = issuedDate;
	}
	
	public Date getExpires() {
		return this.expireDate;
	}
	
	public void setExpires(final Date expirationDate) {
		this.expireDate = expirationDate;
	}
	
	public List<Task> getTasks() {
		return this.tasks;
	}
	
	/**
	 * 
	 * @param complete include completed tasks?
	 * @return
	 */
	public List<Task> getTasks(boolean completed) {
		final List<Task> tasks = new ArrayList<Task>();
		
		if( completed ) {
			tasks.addAll( this.tasks );
			
			/*for(final Task task : this.tasks) {
				tasks.add(task);
			}*/
		}
		else {
			for(final Task task : this.tasks) {
				if( !task.isComplete() ) tasks.add(task);
			}
		}
		
		return tasks;
	}
	
	public Reward getReward() {
		return this.reward;
	}
	
	public void setReward(final Reward newReward) {
		this.reward = newReward;
	}
	
	public void init() {
		if( this.id == -1 ) this.id = lastId++;
	}
	
	public boolean isComplete() {
		return this.isComplete;
	}
	
	public boolean isCopy() {
		return this.isCopy;
	}
	
	public boolean isIgnored() {
		return this.isIgnored;
	}
	
	public boolean isRepeatable() {
		return this.isRepeatable;
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
		// TODO quest suitability, this is a kludge
		return true;
	}
	
	public void setIgnore(boolean ignore) {
		if( isCopy ) this.isIgnored = ignore;
	}
	
	/**
	 * call this after a quest update has been applied to
	 * check and see if the quest has been completed.
	 */
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
	public boolean update(final QuestUpdate qu) {

		boolean questChanged = applyUpdate(qu);

		if (questChanged) {
			this.isComplete = true;
			
			for (final Task task : this.tasks) {
				if ( !task.isComplete() ) {
					this.isComplete = false;
					break;
				}
			}
			
			
		}
		else {
			return false;
		}
		
		return true;
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
	private boolean applyUpdate(final QuestUpdate update) {
		boolean questChanged = false;
		
		for (final Task task : this.tasks) {
			for (final TaskUpdate taskUpdate : new ArrayList<TaskUpdate>(update.taskUpdates)) {
				if ( taskUpdate.getTaskId() == task.getId() ) {
					if ( task.applyUpdate(taskUpdate) ) {
                        questChanged = true;
                    }
					
					update.taskUpdates.remove(taskUpdate);
					
					break;
				}
			}
		}
		
		System.out.println("Quest State: " + questChanged);
		
		return questChanged;
	}
	
	public Quest getCopy() {
		return new Quest(this);
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
	
	@Override
	public boolean equals(final Object obj) {
		if(obj == this) {
			return true;
		}
		
		if(!(obj instanceof Quest)) {
			return false;
		}
		
		final Quest quest = (Quest) obj;
		
		return this.getId() == quest.getId();
	}
	
	@Override
	public String toString() {
		return this.name + "\n" + this.description;
	}
}