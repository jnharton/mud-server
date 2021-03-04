package mud.quest;

import mud.MUDObject;
import mud.utils.Data;

public class KillTask extends Task {
	private Integer toKill = 0;
	private Integer kills = 0;
	
	public KillTask(final String description, final Integer location, final Data objectiveData) {
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
	
	public boolean update(final TaskUpdate update) {
		final Data ud = update.getData();
		
		final Integer k = (Integer) ud.getObject("kills");
		
		this.kills += k;
		
		if (this.kills == this.toKill) {
			this.isComplete = true;
			
		}
		//else this.isComplete = false;
		
		return true;
	}
	
	protected Task getCopy() {
		return new KillTask(this);
	}
}