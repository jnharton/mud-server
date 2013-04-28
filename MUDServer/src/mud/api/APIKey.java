package mud.api;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

/**
 * An object that represents an API key.
 * 
 * @author Jeremy
 *
 */
public class APIKey {
	/** Maximum length of an API key */
	final static private int MAX_KEY_LENGTH = 16;
	
	/** An alphanumeric string (0-9, a-Z, A-Z) */
	private String key;
	
	/**
	 * APIKey
	 * 
	 * Construct an APIKey object from an input string
	 * 
	 * @param keyString The string to be used as an API key
	 */
	public APIKey(String keyString) {
		this.key = keyString;
	}
	
	/**
	 * isValid
	 * 
	 * Validates this APIKey for basic limiting criteria:
	 * 
	 * - is the key less than or equal to the max length
	 * - does the key contain only alphanumeric characters 
	 * 
	 * @return boolean true if the key is valid, false otherwise.
	 */
	public boolean isValid() {
		boolean isValid = false;
		
		// check input data to be sure that it contains only alphanumeric data (no symbols)
		// also, check max size and possible range of keys
		if( key.length() <= MAX_KEY_LENGTH ) { // is it the right length
			if( key.matches("[A-Za-z0-9]*") ) {
				isValid = true;
			}
		}
		
		return isValid;
	}
	
	@Override
	public boolean equals(Object obj) {
		if( obj instanceof APIKey ) {
			APIKey key = (APIKey) obj;
			return equals(key);
		}
		else { return false; }
	}
	
	public boolean equals(APIKey apikey) {
		if( this.key.equals( apikey.toString() ) ) {
			return true;
		}
		else { return false; }
	}
	
	@Override
	public int hashCode() {
		/* just give us the hashcode of the internal string, this should
		 * ensure that any "key" that is this string is considered the same object.
		 */
		return System.identityHashCode(key);
	}
	
	/**
	 * Returns the string representation of the key, i.e. the key itself.
	 */
	public String toString() {
		return this.key;
	}
}