package mud.quest;

import mud.objects.Room;
import mud.utils.Data;

public class CollectTask extends Task {
	public Integer toCollect = 0;
	public Integer collects = 0;
	
	public CollectTask(String tDescription, TaskType tType, Room location, Data objectiveData) {
		super(tDescription, tType, location);
		
		this.taskType = TaskType.COLLECT;
	}
	
	private CollectTask(final CollectTask ct) {
		super(ct);
		
		this.collects = 0;
		this.toCollect = ct.toCollect;
	}
	
	public String getProgress() {
		return "" + this.collects + " / " + this.toCollect;
	}
	
	public void update() {
		if (this.collects == this.toCollect) {
			this.isComplete = true;
		}
		else { this.isComplete = false; }
	}

	@Override
	protected Task clone() {
		return new CollectTask(this);
	}
}