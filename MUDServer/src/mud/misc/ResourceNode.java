package mud.misc;

/**
 * a resource node
 * 
 * @author Jeremy
 *
 */
public class ResourceNode {
	Resource resource;
	
	private int units_available = 0;
	
	public ResourceNode(final Resource res) {
		this.resource = res;
		this.units_available = -1;
	}
	
	public String getName() {
		return this.resource.getDisplayName();
	}
	
	public Resource getResource() {
		return this.resource;
	}
	
	public Boolean isDepleted() {
		if( units_available == 0 )
			return true;
		else {
			// units_available == -1 (infinite)
			// units_available > 0 (some)
			return false;
		}
	}
}