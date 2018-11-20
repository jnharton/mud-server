package mud.misc;

/**
 * a resource node
 * 
 * @author Jeremy
 *
 */
public class Node {
	Resource resource;
	
	public boolean depleted = false;
	
	public Node(final Resource res) {
		this.resource = res;
	}
	
	public String getName() {
		return this.resource.getDisplayName();
	}
	
	public Resource getResource() {
		return this.resource;
	}
}