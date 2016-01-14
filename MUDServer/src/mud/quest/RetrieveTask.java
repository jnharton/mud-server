package mud.quest;

import mud.objects.Room;
import mud.utils.Data;

public class RetrieveTask extends Task {
	public boolean hasItem = false;
	
	public RetrieveTask(String tDescription, Room location, Data objectiveData) {
		super(tDescription, TaskType.RETRIEVE, location);
	}

	private RetrieveTask(RetrieveTask rt) {
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

	@Override
	protected Task clone() {
		return new RetrieveTask(this);
	}
}