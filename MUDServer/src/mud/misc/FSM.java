package mud.misc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import mud.utils.Tuple;
import mud.utils.Utils;

/**
 * A Finite State Machine (FSM) implemented in Java
 * 
 * http://gamedevelopment.tutsplus.com/tutorials/finite-state-machines-theory-and-implementation--gamedev-11867
 * 
 * @author Jeremy
 *
 */
public class FSM {
	private Stack<String> stateStack;
	
	private String previous;
	private String current;
	
	private List<String> states;
	
	// map a state name to a function and the object on which it is a method
	private Hashtable<String, Tuple<Object, Method>> stateFunctions; 
	private Hashtable<String, List<Transition>> transitionTbl;
	
	//private Hashtable<Tuple<String, Condition>, String> transitionTbl;
	// (state, condition), new state
	// or?
	// state, (condition, new state)
	// or?
	// state, list(transitions) = state, list(condition, new state)
	
	private Hashtable<String, Integer> values;
	
	public int value = 0;
	
	public FSM() {
		this.previous = "";
		this.current = "test";
		
		this.stateStack = new Stack<String>();
		this.stateStack.push( this.current );
		
		this.transitionTbl = new Hashtable<String, List<Transition>>();
	}
	
	public FSM(final String...states) {
		this();
		
		this.states = new ArrayList<String>();
		
		this.states.addAll( Utils.mkList(states) );
	}
	
	public static void main(String[] args) {
		final FSM sm = new FSM();
		
		sm.value = 5;
		
		while( !sm.getCurrState().equals("exit") ) { 
			sm.updateState();
			System.out.println(sm.getPrevState() + " => " + sm.getCurrState());
		}
		
		final FSM a1 = new FSM();
		
		a1.setStates("wander", "attack", "evade", "find aid");
		
		final FSM a2 = new FSM();
		
		a2.setStates("wander", "attack", "evade", "find aid");
	}
	
	public String getPrevState() {
		return this.previous;
	}
	
	public String getCurrState() {
		return this.current;
	}
	
	public void updateState() {
		switch(current) {
		case "test":
			if( testFunc() ) {
				this.previous = this.current;
				this.current = "test2";
			}
			break;
		case "test2":
			this.value--;
			if( test2Func() ) {
				this.previous = this.current;
				this.current = "exit";
			}
			break;
		case "exit":
			break;
		default:
			break;
		}
	}
	
	private boolean testFunc() {
		return (new Condition(Condition.GT, this.value, 4)).check();
	}
	
	private boolean test2Func() {
		return (new Condition(Condition.LT, this.value, 1)).check();
	}
	
	public void updateState2() {
		final List<Transition> tsns = this.transitionTbl.get( this.current );
		
		for(final Transition tsn : tsns) {
		}
	}
	
	public void setStates(final String...states) {
	}
	
	//public void addTransition(String origState, Condition cond, String newState) {
	public void addTransition(String origState, Transition transition) {
		//this.transitionTbl.put(new Tuple<String, Condition>(origState, cond), newState);
		if( !"".equals(origState) && origState != null ) {
			if( this.transitionTbl.containsKey(origState) ) {
				this.transitionTbl.get(origState).add(transition);
			}
		}
	}
}