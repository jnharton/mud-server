package mud.utils;

import java.util.LinkedList;
import java.util.List;

import mud.objects.Room;

/**
 * Landmark
 * 
 * A class the encapsulates a named landmark, it's
 * location/itself (if the place is a room), and routes to get
 * there
 * 
 * Route(examples):
 * 	<origin dbref>:nw, n, n, ne, n, e, e, e, s, s, se, ne, n
 *  9:inn
 * 
 * 
 * @author Jeremy
 *
 */
public class Landmark {
	private String name;         // name of the landmark
	private Room room;           // the named place or the room which encompasses it (for a specific thing in a room)
	private List<String> routes; // way of getting there
	
	public Landmark(final String name) {
		this.name = name;
		this.routes = new LinkedList<String>();
	}
	
	public Landmark(final String name, final List<String> routes) {
		this.name = name;
		this.routes = new LinkedList<String>();
		for(String route : routes) this.routes.add(route);
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<String> getRoutes() {
		return this.routes;
	}
	
	public void addRoute(final String route) {
		routes.add( route );
	}
	
	public void removeRoute(final String route) {
		routes.remove(route);
	}
}