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
	public String script;
	
	public Trigger(String newScript) {
		this.script = newScript;
	}
	
	public String exec() {
		/*if( !(this.script.contains("{") || this.script.contains("}")) ) {
			return this.script;
		}*/
		return "";
	}
}