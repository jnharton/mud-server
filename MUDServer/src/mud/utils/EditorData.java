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

public class EditorData {
	private HashMap<String, Object> objects;
	
	public EditorData() {
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
		
		if ( this.objects.containsKey(key) ) {
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
	
	public LinkedList<Object> getObjects(String pattern) {
		LinkedList<Object> objectList = new LinkedList<Object>();
		
		for(String key : this.objects.keySet()) {
			if( key.matches(pattern) ) {
				objectList.add( this.objects.get(key) );
			}
		}
		
		return objectList;
	}
}