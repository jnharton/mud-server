package mud.utils;

import java.util.ArrayList;
import java.util.List;

import mud.misc.Script;

public class CNode {
	private Integer id;
	private String text;
	private String response;
	//private Condition cond;
	private List<CNode> options;
	
	private Script script;
	
	public boolean starts = false;
	public boolean ends = false;
	
	// player says, npc responds
	public CNode(final Integer id, final String nText, final String response) {
		this(id, nText, response, new ArrayList<CNode>());
	}
	
	public CNode(final Integer id, final String nText, final String response, List<CNode> options) {
		this(id, nText, response, options, null);
	}
	
	public CNode(final Integer id, final String nText, final String response, List<CNode> options, Script script) {
		this.id = id;
		this.text = nText;
		this.response = response;
		this.options = options;
		
		this.script = script;
		
		this.ends = false;
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
	
	public void addOption(final CNode option) {
		this.options.add(option);
	}
	
	public void addOptions(final CNode...options) {
		for(final CNode cn : options) {
			addOption(cn);
		}
	}
	
	public void removeOption(final CNode option) {
		this.options.remove(option);
	}
	
	public void removeOption(int index) {
		this.options.remove(index);
	}
	
	public Script getScript() {
		return this.script;
	}
}