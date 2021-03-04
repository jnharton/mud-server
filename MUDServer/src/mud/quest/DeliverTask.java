package mud.quest;

import mud.utils.Data;

public class DeliverTask extends Task {
	
	public DeliverTask(final String tDescription, final Integer location, final Data objectiveData) {
		super(TaskType.DELIVER, tDescription, location);
		
		// initialize task data as necessary
	}

	public DeliverTask(final Task template) {
		super(template);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getProgress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean update(final TaskUpdate update) {
		return true;
	}

	@Override
	protected Task getCopy() {
		// TODO Auto-generated method stub
		return null;
	}

}
