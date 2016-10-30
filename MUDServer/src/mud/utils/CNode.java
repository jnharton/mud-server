package mud.utils;

import java.util.ArrayList;
import java.util.List;

import mud.misc.Script;

public class CNode {
	private Integer id;
	private String text;
	private String response;
	private List<CNode> options;
	
	public boolean ends = false;
	
	private Script script;
	
	// player says, npc responds
	public CNode(final Integer id, final String text, final String response) {
		this(id, text, response, new ArrayList<CNode>());
	}
	
	public CNode(final Integer id, final String text, final String response, final List<CNode> options) {
		this(id, text, response, options, false);
	}
	
	public CNode(final Integer id, final String text, final String response, final List<CNode> options, final boolean ends) {
		this(id, text, response, options, ends, null);
	}
	
	public CNode(final Integer id, final String text, final String response, final List<CNode> options, final boolean ends, Script script) {
		this.id = id;
		this.text = text;
		this.response = response;
		this.options = options;
		
		this.script = script;
		
		this.ends = ends;
	}
	
	public CNode(final CNode template) {
		
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public String getText() {
		return this.text;
	}
	
	public String getResponse() {
		return this.response;
	}
	
	public List<CNode> getOptions() {
		return this.options;
	}
	
	public Script getScript() {
		return this.script;
	}
	
	public void addOption(final CNode option) {
		this.options.add(option);
	}
	
	public void addOptions(final CNode...options) {
		for(final CNode cn : options) {
			addOption(cn);
		}
	}
	
	/*public void removeOption(final CNode option) {
		this.options.remove(option);
	}
	
	public void removeOption(int index) {
		this.options.remove(index);
	}*/
}