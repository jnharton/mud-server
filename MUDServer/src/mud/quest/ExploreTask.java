/**
 * 
 */
package mud.quest;

import mud.MUDObject;
import mud.utils.Data;

/**
 * @author jnhar
 *
 */
public class ExploreTask extends Task {
	private Integer exploration_pts;
	private Integer total_exploration_pts;

	public ExploreTask(final String description, final Integer location, final Data objectiveData) {
		super(TaskType.EXPLORE, description, location);
		
		final Object o = objectiveData.getObject("toExplore");

		if( o != null ) {
			final Integer n = (Integer) objectiveData.getObject("toExplore");
			this.total_exploration_pts = n;
		}
		else {
			this.total_exploration_pts = 0;
		}

		this.exploration_pts = 0;
		
		// TODO fix kludge
		this.objective = objectiveData;
	}

	/**
	 * @param template
	 */
	public ExploreTask(final ExploreTask et) {
		super(et);
		
		this.exploration_pts = 0;
		this.total_exploration_pts = et.total_exploration_pts;
	}

	@Override
	public String getProgress() {
		return String.format("%s / %s", this.exploration_pts, this.total_exploration_pts);
	}

	@Override
	protected boolean update(TaskUpdate update) {
		final Data udata = update.getData();
		
		final Integer n = (Integer) udata.getObject("exploration");
		
		this.exploration_pts += n;
		
		if (this.exploration_pts == this.total_exploration_pts) {
			this.isComplete = true;
			
		}
		//else this.isComplete = false;
		
		return true;
	}

	@Override
	protected Task getCopy() {
		return new ExploreTask(this);
	}
}