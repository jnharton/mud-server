package mud.utils;

/*
Copyright (c) 2012 Jeremy N. Harton

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Store a collection of arbitrary objects indexed by string keys,
 * where there is a layer of abstraction around the map access.
 * 
 * @author Jeremy
 *
 */
public class EditorData {
	private HashMap<String, Object> objects;
	
	public EditorData() {
		this.objects = new HashMap<String, Object>(1, 0.75f);
	}
	 
	public boolean addObject(final String key, final Object object) {
		boolean success = false;
		
		if( !this.objects.containsKey(key) ) {
			this.objects.put(key, object);
			success = true;
		}
		else {
			System.out.println("Editor Data (error): Key already exists!");
		}
		
		return success;
	}
	
	/**
	 * Get the object in the map that this key
	 * is mapped to.
	 * 
	 * @param key
	 * @return the mapped object (or null)
	 */
	public Object getObject(final String key) {
		return this.objects.get(key);
	}
	
	/**
	 * Set the object mapped to the specified key to
	 * be a different object.
	 * 
	 * NOTE: Only usable for objects added with
	 * addObject(...)
	 * 
	 * @param key
	 * @param object
	 * @return
	 */
	public boolean setObject(final String key, final Object object) {
		boolean success = false;
		
		if ( this.objects.containsKey(key) ) {
			this.objects.put(key, object);
			success = true;
		}
		else {
			System.out.println("Editor Data (error): Key does not exist.");
		}
		
		return success;
	}
	
	public Object removeObject(final String key) {
		if( this.objects.containsKey(key) ) {
			return this.objects.remove(key);
		}
		else {
			return false;
		}
	}
	
	public HashMap<String, Object> getObjects() {
		return this.objects;
	}
	
	public List<String> getKeysByPrefix(final String prefix) {
		final List<String> keyList = new LinkedList<String>();

		for(final String key : this.objects.keySet()) {
			if( key.startsWith(prefix) ) {
				keyList.add( key );
			}
		}

		return keyList;
	}
}