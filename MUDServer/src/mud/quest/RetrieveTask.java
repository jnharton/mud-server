package mud.quest;

import mud.objects.Room;
import mud.utils.Data;

public class RetrieveTask extends Task {
	public boolean hasItem = false;
	
	public RetrieveTask(final String tDescription, final Room location, final Data objectiveData) {
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
	public void update() {
		if ( this.hasItem ) {
			this.isComplete = true;
		}
		else { this.isComplete = false; }
	}
	
	protected Task getCopy() {
		return new RetrieveTask(this);
	}
}