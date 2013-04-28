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

public enum RequestType {
	DATA, STATS, NONE;

	public static RequestType getType(String typeString) {
		if( typeString.equals("request-data") ) { return RequestType.DATA; }
		else if( typeString.equals("request-stats") ) { return RequestType.STATS; }
		else { return RequestType.NONE; }
	}
}