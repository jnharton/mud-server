package mud.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Recipe {
	private String name;
	
	// component items (what do we need to make it?)
	private ArrayList<String> components;
	
	// item created?
	
	public Recipe(final String _name) {
		this.name =_name;
		
		this.components = new ArrayList<String>(0);
	}
	
	public Recipe(final String _name, final String..._components) {
		this.name =_name;
		
		this.components = new ArrayList<String>(_components.length);
		
		this.components.addAll( Arrays.asList(_components) );
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<String> getComponents() {
		return Collections.unmodifiableList(this.components);
	}
}