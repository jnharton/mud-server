package mud;

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