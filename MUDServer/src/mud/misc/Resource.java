package mud.misc;

import java.util.Collections;
import java.util.Map;

import mud.Constants;

public abstract class Resource {
	public enum ResType { NONE, MINERAL, ORE, PLANT, WOOD };
	
	protected String _name;
	
	private Map<String, String> properties;
	
	protected Resource(final String name) {
		this._name = name;
	}
	
	public abstract String getName();
	public abstract String getDisplayName();
	
	public abstract ResType getType();
	
	public Map<String, String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}
	
	/**
	 * Retrieve the property by it's key
	 * 
	 * @param key property name
	 * @return property value
	 */
	public final String getProperty(final String key) {
		final String value = this.properties.get(key); 
		
		return (value != null) ? value : Constants.NONE;
	}
	
	/**
	 * Set a property on this object, where the name
	 * of the property is the key and the value of the
	 * property is the value.
	 * 
	 * @param key   property name
	 * @param value property value
	 */
	public final void setProperty(final String key, final String value) {
		this.properties.put(key,  value);
	}
}