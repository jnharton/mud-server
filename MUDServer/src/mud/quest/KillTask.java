package mud.quest;

import mud.MUDObject;
import mud.objects.Room;
import mud.utils.Data;

public class KillTask extends Task {
	public Integer toKill = 0;
	public Integer kills = 0;
	
	public KillTask(final String description, final Room location, final Data objectiveData) {
		super(TaskType.KILL, description, location);
		
		final Object o = objectiveData.getObject("toKill");

		if( o != null ) {
			final Integer k = (Integer) objectiveData.getObject("toKill");
			this.toKill = k;
		}
		else {
			this.toKill = 0;
		}

		this.kills = 0;

		final Object o1 = objectiveData.getObject("target");

		if( o1 != null ) {
			final MUDObject m = (MUDObject) objectiveData.getObject("target");
		}
		
		// TODO fix kludge
		this.objective = objectiveData;
	}
	
	private KillTask(final KillTask kt) {
		super(kt);
		
		this.kills = 0;
		this.toKill = kt.toKill;
	}
	
	public String getProgress() {
		return "" + this.kills + " / " + this.toKill;
	}
	
	public void update() {
		if (this.kills == this.toKill) {
			this.isComplete = true;
		}
		else { this.isComplete = false; }
	}
	
	protected Task getCopy() {
		return new KillTask(this);
	}
}