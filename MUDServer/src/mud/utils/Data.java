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

/**
 * Store a collection of arbitrary objects indexed by string keys,
 * where there is a layer of abstraction around the map access.
 * 
 * @author Jeremy
 *
 */
public class Data {
	private HashMap<String, Object> objects;
	private HashMap<Object, Boolean> locks;
	
	public Data() {
		this.objects = new HashMap<String, Object>(1, 0.75f);
		this.locks = new HashMap<Object, Boolean>(1, 0.75f);
	}
	 
	public boolean addObject(final String key, final Object object) {
		boolean success = false;
		
		if( !this.objects.containsKey(key) ) {
			this.objects.put(key, object);
			success = true;
		}
		else {
			System.out.println("Data (error): Key already exists!");
		}
		
		return success;
	}
	
	public Object removeObject(final String key) {
		return this.objects.remove(key);
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
	
	public HashMap<String, Object> getObjects() {
		return this.objects;
	}
	
	public LinkedList<Object> getObjects(String pattern) {
		LinkedList<Object> objectList = new LinkedList<Object>();
		
		for(String key : this.objects.keySet()) {
			if( key.matches(pattern) ) {
				objectList.add( this.objects.get(key) );
			}
		}
		
		return objectList;
	}
	
	public void lockObject(final String key) {
		final Object obj = getObject( key );
		
		if( obj != null ) {
			if( !this.locks.containsKey( obj ) ) {
				this.locks.put( getObject(key), true );
			}
		}
		
		/*if( this.objects.containsKey(key) ) {
			this.locks.put( getObject(key), true );
		}*/
	}
	
	public void unlockObject(final String key) {
		final Object obj = getObject( key );
		
		if( obj != null ) {
			if( this.locks.containsKey( obj ) ) {
				this.locks.remove( obj );
			}
		}
	}

	public boolean isLocked(final Object obj) {
		if( obj != null ) {
			if( this.locks.containsKey(obj) ) {
				return true;
			}
			else return false;
		}
		else return false;
	}
	
	public boolean hasLock(final String key) {
		final Object obj = getObject( key );
		
		if( obj != null ) {
			if( this.locks.containsKey( obj ) ) {
				return true;
			}
			else return false;
		}
		
		return false; // an object that doesn't exist can't really be locked can it 
	}
}