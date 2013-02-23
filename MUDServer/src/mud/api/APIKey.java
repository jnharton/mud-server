package mud.api;

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

public class APIKey {
	// constants
	final static private int MAX_KEY_LENGTH = 16;
		
	private String key;
	
	public APIKey(String keyString) {
		this.key = keyString;
	}
	
	public boolean isValid() {
		boolean isValid = false;
		
		// check input data to be sure that it contains only alphanumeric data (no symbols)
		// also, check max size and possible range of keys
		if( key.length() <= MAX_KEY_LENGTH ) { // is it the right length
			if( key.matches("^[A-Za-z0-9]$") ) {
				isValid = true;
			}
		}
		
		return isValid;
	}
	
	public String toString() {
		return this.key;
	}
}