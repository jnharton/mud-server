package mud.utils;

import java.util.HashMap;

public class edData {
	private HashMap<String, Object> objects;
	
	public edData() {
		this.objects = new HashMap<String, Object>(1, 0.75f);
	}
	 
	public void addObject(String key, Object object) {
		this.objects.put(key, object);
	}
	
	public Object removeObject(String key) {
		return this.objects.remove(key);
	}
	
	public Object getObject(String key) {
		return this.objects.get(key);
	}
	
	public boolean setObject(String key, Object object) {
		boolean success = false;
		
		if( this.objects.containsKey(key) ) {
			this.objects.put(key, object);
			success = true;
		}
		else {
			System.out.println("Editor Data (error): Key does not exist.");
		}
		
		return success;
	}
	
	public HashMap<String, Object> getObjects() {
		return this.objects;
	}
}