package mud.crafting;

public class Recipe {
	private String name;
	
	// component items (what do we need to make it?)
	private String[] components;
	
	// item created?
	
	public Recipe(final String _name) {
		this.name =_name;
		
		this.components = new String[0];
	}
	
	public Recipe(final String _name, final String..._components) {
		this.name =_name;
		
		this.components = _components;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String[] getComponents() {
		return this.components;
	}
}