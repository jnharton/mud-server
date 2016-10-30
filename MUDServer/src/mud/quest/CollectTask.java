package mud.quest;

import mud.objects.Room;
import mud.utils.Data;

public class CollectTask extends Task {
	public Integer toCollect = 0;
	public Integer collects = 0;
	
	public CollectTask(final String tDescription, final Room location, final Data objectiveData) {
		super(TaskType.COLLECT, tDescription, location);
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
	
	protected Task getCopy() {
		return new CollectTask(this);
	}
}