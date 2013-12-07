package mud;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

public class Trigger {
	private Script script;
	private int delay;
	
	public Trigger(String tScript) {
		this(tScript, 0);
	}
	
	public Trigger(String tScript, int delay) {
		this.script = new Script(tScript);
		this.delay = delay;
	}
	
	public Script getScript() {
		return this.script;
	}
	
	public void setScript(String newScript) {
		this.script = new Script(newScript);
	}
	
	public int getDelay() {
		return this.delay;
	}
	
	public void setDelay(int newDelay) {
		this.delay = newDelay;
	}
	
	public String exec() {
		/*if( !(this.script.contains("{") || this.script.contains("}")) ) {
			return this.script;
		}*/
		return this.script.getText();
	}
}