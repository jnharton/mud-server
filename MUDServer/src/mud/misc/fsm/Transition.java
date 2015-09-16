package mud.misc.fsm;

final class Transition {
	private Condition test;
	private String state;
	
	public Transition(Condition testCondition, String newState) {
		this.test = testCondition;
		this.state = newState;
	}
	
	public Condition getCondition() {
		return this.test;
	}
	
	public String getState() {
		return this.state;
	}
}