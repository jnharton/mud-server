package mud;

/**
 * Script is a container for code that the ProgramInterpreter
 * can run.
 * 
 * @author Jeremy
 *
 */
public class Script {
	private String text;
	
	public Script(String scriptText) {
		this.text = scriptText;
	}
	
	public String getText() {
		return this.text;
	}
}