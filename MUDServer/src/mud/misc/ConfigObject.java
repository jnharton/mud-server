package mud.misc;

/**
 * A class representing storage of configuration data
 * 
 * @author Jeremy
 *
 */
public class ConfigObject {
	private String name;
	private Object object;
	
	private Class<? extends Object> objectClass;
	
	public ConfigObject(final String name, final Object object) {
		this.name = name;
		this.object = object;
		
		this.objectClass = object.getClass();
	}
	
	public String getName() {
		return this.name;
	}
	
	public Object getObject() {
		return object;
	}
	
	public Boolean getBoolean() {
		if(objectClass == Boolean.class) {
			return (Boolean) object;
		}
		else {
			return false;
		}
	}
	
	public Integer getInteger() {
		if(objectClass == Integer.class) {
			return (Integer) object;
		}
		else {
			return -1;
		}
	}
	
	public String getString() {
		if(objectClass == String.class) {
			return (String) object;
		}
		else {
			return "";
		}
	}
}