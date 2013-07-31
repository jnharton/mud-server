package mud.utils;

/**
 * A class representing storage of configuration data
 * 
 * @author Jeremy
 *
 */
public class ConfigObject {
	private String name;
	private String typeName;
	private Object object = null;
	private Class objectClass;
	
	public ConfigObject(String name, Object object) {
		this.name = name;
		this.typeName = object.getClass().getName();
		this.object = object;
		this.objectClass = object.getClass();
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getTypeName() {
		return this.typeName;
	}
	
	public Object getObject() {
		return object;
	}
	
	public Boolean getBoolean() {
		if(objectClass == Boolean.class) {
			return (Boolean) object;
		}
		else { return null; }			
	}
	
	public String getString() {
		if(objectClass == String.class) {
			return (String) object;
		}
		else { return null; }
	}
}