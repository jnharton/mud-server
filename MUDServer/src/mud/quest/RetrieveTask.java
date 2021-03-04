package mud.quest;

import mud.utils.Data;

public class RetrieveTask extends Task {
	private boolean hasItem = false;
	
	public RetrieveTask(final String tDescription, final Integer location, final Data objectiveData) {
		super(TaskType.RETRIEVE, tDescription, location);
	}

	private RetrieveTask(final RetrieveTask rt) {
		super(rt);
		
		rt.hasItem = false;
	}

	@Override
	public String getProgress() {
		return ( this.hasItem ) ? "true" : "false";
	}

	@Override
	public boolean update(final TaskUpdate update) {
		if ( this.hasItem ) {
			this.isComplete = true;
		}
		//else { this.isComplete = false; }
		
		return true;
	}
	
	protected Task getCopy() {
		return new RetrieveTask(this);
	}
}